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

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;

public class DataSourceScheduler implements ServletContextListener, ChangeListener {

    private static final Log log = LogFactory.getLog(DataSourceScheduler.class);
    
    private ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private DataSourceDao dataSourceDao;
    private RDFService rdfService;
    private HttpUtils httpUtils = new HttpUtils();
    
    private HashMap<String, ScheduledFuture<?>> uriToFuture =  
            new HashMap<String, ScheduledFuture<?>>();
    
    public static DataSourceScheduler getInstance(ServletContext ctx) {
        Object o = ctx.getAttribute(DataSourceScheduler.class.getName());
        if (o instanceof DataSourceScheduler) {
            return (DataSourceScheduler) o;
        } else {
            return null;
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
        scheduler.setThreadFactory(
                new VitroBackgroundThread.Factory("DataSourceTask"));
        scheduler.initialize();
        sce.getServletContext().setAttribute(
                DataSourceScheduler.class.getName(), this);
        try {
            RDFServiceUtils.getRDFServiceFactory(
                    sce.getServletContext()).registerListener(this);
        } catch (RDFServiceException e) {
            throw new RuntimeException(e);
        }
        scheduleTestTask();
        // TODO scheduleAllTasks();
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            for(ScheduledFuture<?> f : this.uriToFuture.values()) {
                f.cancel(true);        
            }
            scheduler.shutdown();
            scheduler.destroy();
            sce.getServletContext().setAttribute(
                    this.getClass().getName(), null);
            RDFServiceUtils.getRDFServiceFactory(
                    sce.getServletContext()).unregisterListener(this);
        } catch (Exception e) {
            // ignore for now - possible NPEs as things are destroyed
        }
    }
    
    private void scheduleTestTask() {
        this.uriToFuture.put("http://example.com/testTask", 
                scheduler.scheduleAtFixedRate(new TestTask(), 10000 /* ms */));
    }
        
    public void startNow(String dataSourceURI) {
        new DataSourceStarter(dataSourceURI).run();
    }
    
    public void stopNow(String dataSourceURI) {
        new DataSourceStopper(dataSourceURI).run();
    }
    
    public void startAsync(String dataSourceURI) {
        scheduler.execute(new DataSourceStarter(dataSourceURI));
    }
    
    public void stopSync(String dataSourceURI) {
        scheduler.execute(new DataSourceStopper(dataSourceURI));
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
        // nothing to do right now
    }

    @Override
    public void notifyModelChange(ModelChange arg0) {
        // TODO listen for changes and schedule / cancel tasks as necessary        
    }
    
    public class TestTask implements Runnable {
        @Override
        public void run() {
            log.info("Hi there; I'm a scheduled task, but I'm not doing very much.");
        }
    }

}