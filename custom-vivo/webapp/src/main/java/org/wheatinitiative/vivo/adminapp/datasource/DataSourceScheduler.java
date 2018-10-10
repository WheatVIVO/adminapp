package org.wheatinitiative.vivo.adminapp.datasource;

import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.wheatinitiative.vivo.datasource.DataSourceDescription;
import org.wheatinitiative.vivo.datasource.dao.DataSourceDao;
import org.wheatinitiative.vivo.datasource.service.DataSourceDescriptionSerializer;
import org.wheatinitiative.vivo.datasource.util.http.HttpUtils;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange.Operation;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

public class DataSourceScheduler implements ServletContextListener, ChangeListener {

    private ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private HashMap<String, ScheduledFuture<?>> uriToFuture =  
            new HashMap<String, ScheduledFuture<?>>();
    private DataSourceDao dataSourceDao;
    private RDFService rdfService;
    private HttpUtils httpUtils = new HttpUtils();
    
    private static final int TWENTYFOUR_HOURS = 60 * 60 * 24 * 1000; // ms
    
    private static final Log log = LogFactory.getLog(DataSourceScheduler.class);
    
    public static DataSourceScheduler getInstance(ServletContext ctx) {
        Object o = ctx.getAttribute(DataSourceScheduler.class.getName());
        if (o instanceof DataSourceScheduler) {
            return (DataSourceScheduler) o;
        } else {
            throw new RuntimeException("No DataSourceScheduler was set up "
                    + "in the supplied context");
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            for(ScheduledFuture<?> f : uriToFuture.values()) {
                f.cancel(true);        
            }
            scheduler.shutdown();
            log.info("Task scheduler shut down successfully.");
            scheduler.destroy();
            log.info("Task scheduler destroyed successfully.");
            sce.getServletContext().setAttribute(this.getClass().getName(), null);         
        } catch (Exception e) {
            // ignore for now - possible NPEs as things are destroyed
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            this.rdfService = ModelAccess.on(
                    sce.getServletContext()).getRDFService();
            this.dataSourceDao = new DataSourceDao(
                    new RDFServiceModelConstructor(this.rdfService));
        } catch (Exception e) {
            throw new RuntimeException(this.getClass().getSimpleName() 
                    + " must be run after the context's RDFService is set up.");
        }
        scheduler.setPoolSize(20);
        scheduler.initialize();
        sce.getServletContext().setAttribute(this.getClass().getName(), this);
        try {
            RDFServiceUtils.getRDFServiceFactory(
                    sce.getServletContext()).registerListener(this);
        } catch (RDFServiceException e) {
            throw new RuntimeException(e);
        }
        if(!sce.getServletContext().getContextPath().isEmpty()) {
            String scheduleTasksInAllContexts = ConfigurationProperties.getBean(
                    sce).getProperty("org.wheatinitiative.vivo.scheduleTasksInAllContexts"); 
            if( ("FALSE".equals(scheduleTasksInAllContexts))) {
                // guard against scheduling the same tasks twice if VIVO happens
                // to be deployed a second time in a context other than root
                log.warn("Not scheduling tasks because this is not the root context");
                return;
            }            
        }
        log.info("Task scheduler set up");
        // TODO schedule tasks based on RDF statements 
    }
    
    public void startNow(String dataSourceURI) {
        new DataSourceStarter(dataSourceURI).run();
    }
    
    public void stopNow(String dataSourceURI) {
        new DataSourceStopper(dataSourceURI).run();
    }
    
    private void cancel(String dataSourceURI) {
        ScheduledFuture<?> future = this.uriToFuture.get(dataSourceURI);
        if(future != null) {
            future.cancel(true);
        }
        this.uriToFuture.put(dataSourceURI, null);
    }

    private DataSourceDescription getDataSourceDescription(
            String dataSourceURI) {
        DataSourceDescription ds = this.dataSourceDao.getDataSource(
                dataSourceURI);
        if(ds == null) {
            throw new RuntimeException("DataSource " + dataSourceURI 
                    + "not found");
        }
        return ds;
    }
    
    private class DataSourceStarter implements Runnable {

        private String dataSourceURI;
        
        public DataSourceStarter(String dataSourceURI) {
            this.dataSourceURI = dataSourceURI;
        }
        
        @Override
        public void run() {
            DataSourceDescription desc = getDataSourceDescription(
                    dataSourceURI);            
            desc.getStatus().setRunning(true);
            updateService(desc.getConfiguration().getDeploymentURI(), desc);            
        }
        
    }
    
    private class DataSourceStopper implements Runnable {

        private String dataSourceURI;
        
        public DataSourceStopper(String dataSourceURI) {
            this.dataSourceURI = dataSourceURI;
        }
        
        @Override
        public void run() {
            DataSourceDescription desc = getDataSourceDescription(
                    dataSourceURI);            
            desc.getStatus().setRunning(false);
            updateService(desc.getConfiguration().getDeploymentURI(), desc);            
        }
        
    }
    
    protected DataSourceDescription updateService(
            String deploymentURL, DataSourceDescription description) {
        if(deploymentURL == null) {
            throw new RuntimeException("deployment URL may not be null");
        }
        DataSourceDescriptionSerializer serializer = 
                new DataSourceDescriptionSerializer();
        String json = serializer.serialize(description);
        String result = httpUtils.getHttpPostResponse(
                deploymentURL, json, "application/json");
        try {
            DataSourceDescription desc = serializer.unserialize(result);
            return desc;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Exception parsing response from " + deploymentURL 
                            + ": \n" + result);
        }
    }

    @Override
    public void notifyEvent(String arg0, Object arg1) {
        // don't care        
    }

    @Override
    public void notifyModelChange(ModelChange modelChange) {
        Model change = RDFServiceUtils.parseModel(
                modelChange.getSerializedModel(), 
                modelChange.getSerializationFormat());
        if(modelChange.getOperation().equals(Operation.ADD)) {
            doAddedModel(change);
        } else if (Operation.REMOVE.equals(modelChange.getOperation())) {
            doRemovedModel(change);
        } else {
            log.error("Unrecognized model change operation " 
                    + modelChange.getOperation());
        }
    }
    
    protected void doAddedModel(Model additions) {
        // TODO method stub
    }
    
    protected void doRemovedModel(Model removals) {
        // TODO method stub
    }

}
