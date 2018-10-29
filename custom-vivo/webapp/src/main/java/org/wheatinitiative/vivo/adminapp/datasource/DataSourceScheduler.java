package org.wheatinitiative.vivo.adminapp.datasource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.wheatinitiative.vivo.datasource.DataSourceDescription;
import org.wheatinitiative.vivo.datasource.DataSourceUpdateFrequency;
import org.wheatinitiative.vivo.datasource.SparqlEndpointParams;
import org.wheatinitiative.vivo.datasource.dao.DataSourceDao;
import org.wheatinitiative.vivo.datasource.service.DataSourceDescriptionSerializer;
import org.wheatinitiative.vivo.datasource.util.http.HttpUtils;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange.Operation;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread.WorkLevel;

public class DataSourceScheduler implements ServletContextListener, ChangeListener {

    private ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private HashMap<String, ScheduledFuture<?>> uriToFuture =  
            new HashMap<String, ScheduledFuture<?>>();
    private Model aboxModel;
    private DataSourceDao dataSourceDao;
    private RDFService rdfService;
    private HttpUtils httpUtils = new HttpUtils();
    private static final String DATASOURCE_CONFIG_PROPERTY_PREFIX = "datasource.";
    private Map<String, String> datasourceConfigurationProperties = new HashMap<String, String>();
    private static final String DEFAULT_NAMESPACE_PROPERTY = "Vitro.defaultNamespace"; 
    private static final String ENDPOINT_USERNAME_PROPERTY = "sparqlEndpoint.username";
    private String endpointUsername;
    private static final String ENDPOINT_PASSWORD_PROPERTY = "sparqlEndpoint.password";
    private String endpointPassword;
    
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    
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
            log.info("Attempting to cancel all scheduled tasks...");
            try {
                for(ScheduledFuture<?> f : uriToFuture.values()) {
                    try {
                        f.cancel(true);        
                    } catch (Exception e) {
                        log.debug(e, e);
                    }
                }
            } catch (Exception e) {
                log.debug(e, e);
            }
            log.info("Attempting to shut down scheduler...");
            scheduler.shutdown();
            log.info("Task scheduler shut down successfully.");
            scheduler.destroy();
            log.info("Task scheduler destroyed successfully.");
            sce.getServletContext().setAttribute(this.getClass().getName(), null);         
        } catch (Exception e) {
            log.debug(e, e);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        populateDataSourceRelatedConfigurationProperties(
                ConfigurationProperties.getBean(sce).getPropertyMap());
        try {
            this.aboxModel = ModelAccess.on(sce.getServletContext()
                    ).getOntModelSelector().getABoxModel();
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
        scheduleDataSources();
    }
    
    private void populateDataSourceRelatedConfigurationProperties(
            Map<String, String> configurationProperties) {
        for(String key : configurationProperties.keySet()) {
            if(!key.startsWith(DATASOURCE_CONFIG_PROPERTY_PREFIX)) {
                continue;
            }
            datasourceConfigurationProperties.put(
                    key, configurationProperties.get(key));
        }
        datasourceConfigurationProperties.put(DEFAULT_NAMESPACE_PROPERTY, 
                configurationProperties.get(DEFAULT_NAMESPACE_PROPERTY));
        this.endpointUsername = configurationProperties.get(ENDPOINT_USERNAME_PROPERTY);
        this.endpointPassword = configurationProperties.get(ENDPOINT_PASSWORD_PROPERTY);        
    }
    
    /*
     * Add the global configuration properties to a parameter map
     */
    private void includeDataSourceRelatedConfigurationProperties(
            Map<String, Object> parameters) {
        for(String key : this.datasourceConfigurationProperties.keySet()) {
            parameters.put(key, this.datasourceConfigurationProperties.get(key));
        }
    }
    
    /*
     * Allow username and password for endpoint to be specified in 
     * runtime.properties instead of RDF
     */
    private void addSparqlEndpointCredentialsFromConfigurationProperties(
            SparqlEndpointParams endpointParameters) {
        if(endpointParameters.getUsername() == null) {
            endpointParameters.setUsername(endpointUsername);
        }
        if(endpointParameters.getPassword() == null) {
            endpointParameters.setPassword(endpointPassword);
        }        
    }
    
    private void scheduleDataSources() {
        for(DataSourceDescription dataSource : this.dataSourceDao.listDataSources()) {
            cancel(dataSource.getConfiguration().getURI());
            schedule(dataSource);
        }
    }
    
    private void schedule(DataSourceDescription dataSource) {
        // If 'schedule immediately after' has been set, remove any specific 
        // next update date and exit.
        if(dataSource.getScheduleAfterURI() != null) {
            deleteNextUpdateDateTime(dataSource.getConfiguration().getURI());
        } else if (dataSource.getNextUpdate() == null) {
            return;
        } else if (dataSource.getUpdateFrequency() != null){
            computeNextUpdateAndScheduleTask(dataSource);
        }
    }
    
    private void deleteNextUpdateDateTime(String dataSourceURI) {
        muteChangeListener(dataSourceURI);
        aboxModel.removeAll(
                aboxModel.getResource(dataSourceURI),
                aboxModel.getProperty(DataSourceDao.NEXTUPDATE), 
                null);
        unmuteChangeListener(dataSourceURI);
        return;
    }
    
    private void setNextUpdate(String dataSourceURI, LocalDateTime nextUpdate) {
        deleteNextUpdateDateTime(dataSourceURI);
        muteChangeListener(dataSourceURI);
        aboxModel.add(
                aboxModel.getResource(dataSourceURI),
                aboxModel.getProperty(DataSourceDao.NEXTUPDATE), 
                nextUpdate.toString(DateTimeFormat.forPattern(
                        DATE_TIME_PATTERN)), XSDDatatype.XSDdateTime);
        unmuteChangeListener(dataSourceURI);
    }
    
    private void computeNextUpdateAndScheduleTask(DataSourceDescription dataSource) {
        try {
            LocalDateTime nextUpdate = DateTimeFormat.forPattern(
                    DATE_TIME_PATTERN).parseDateTime(
                            dataSource.getNextUpdate()).toLocalDateTime();
            // Give ourselves a buffer of five minutes to avoid the chance 
            // of scheduling something that won't get run because the time
            // has already passed.
            LocalDateTime now = new LocalDateTime().plusMinutes(5);
            int giveUp = 100;
            while(now.isAfter(nextUpdate) && giveUp > 0) {
                giveUp--;
                nextUpdate = advanceByFrequency(nextUpdate, 
                        dataSource.getUpdateFrequency());
            }
            setNextUpdate(dataSource.getConfiguration().getURI(), nextUpdate);
            scheduleTask(dataSource.getConfiguration().getURI(), nextUpdate);
        } catch (Exception e) {
            log.error(e, e);
            deleteNextUpdateDateTime(dataSource.getConfiguration().getURI());
        }
    }
    
    private void scheduleTask(String dataSourceURI, LocalDateTime dateTime) {
        Runnable task = new DataSourceStarter(
                dataSourceURI, true, new DataSourceTimestamper(aboxModel));
        this.uriToFuture.put(dataSourceURI, scheduler.schedule(task,
                dateTime.toDateTime().toDate()));
        log.info("Scheduled " + dataSourceURI + " for " + dateTime.toString());
    }
    
    private LocalDateTime advanceByFrequency(LocalDateTime nextUpdate, 
            DataSourceUpdateFrequency updateFrequency) {
        if(DataSourceUpdateFrequency.DAILY == updateFrequency) {
            return nextUpdate.plusDays(1);
        } else if(DataSourceUpdateFrequency.WEEKLY == updateFrequency) {
            return nextUpdate.plusWeeks(1);
        } else if(DataSourceUpdateFrequency.MONTHLY == updateFrequency) {
            // For now, schedule next run on same day of the week instead of
            // truly monthly.
            return nextUpdate.plusWeeks(4);
        } else {
            return nextUpdate;
        }
    }
    
    public void startNow(String dataSourceURI) {
        VitroBackgroundThread starter = new VitroBackgroundThread( 
                new DataSourceStarter(dataSourceURI, true, 
                        new DataSourceTimestamper(aboxModel)), 
                                dataSourceURI + "-starter");
        starter.setWorkLevel(WorkLevel.WORKING);
        starter.start();
    }
    
    public void stopNow(String dataSourceURI) {
        VitroBackgroundThread starter = new VitroBackgroundThread( 
                new DataSourceStopper(dataSourceURI), 
                                dataSourceURI + "-stopper");
        starter.setWorkLevel(WorkLevel.WORKING);
        starter.start();
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
    
    private class DataSourceTimestamper {
        
        private Model model;

        public DataSourceTimestamper(Model model) {
            this.model = model;
        }
        
        private void timestampLastUpdate(String dataSourceURI) {
            LocalDateTime now = new LocalDateTime();
            String timestampStr = now.toString(DateTimeFormat.forPattern(
                    DATE_TIME_PATTERN));
            Resource dataSource = model.getResource(dataSourceURI);
            Property lastUpdate = model.getProperty(DataSourceDao.LASTUPDATE);
            model.removeAll(dataSource, lastUpdate, null);
            model.add(dataSource, lastUpdate, timestampStr, XSDDatatype.XSDdateTime);
        }
        
    }
    
    private class DataSourceStarter implements Runnable {

        private String dataSourceURI;
        private DataSourceTimestamper timestamper;
        private boolean runNextSourceInChain = false;
        private int START_POLL_INTERVAL = 100; // ms
        private int FINISH_POLL_INTERVAL = 3000; // ms
        
        public DataSourceStarter(String dataSourceURI, boolean runNextSourceInChain,
                DataSourceTimestamper timestamper) {
            this.dataSourceURI = dataSourceURI;
            this.timestamper = timestamper;
            this.runNextSourceInChain = runNextSourceInChain;
        }
        
        @Override
        public void run() {
            timestamper.timestampLastUpdate(dataSourceURI);
            DataSourceDescription desc = getDataSourceDescription(
                    dataSourceURI);
            includeDataSourceRelatedConfigurationProperties(
                    desc.getConfiguration().getParameterMap());
            addSparqlEndpointCredentialsFromConfigurationProperties(
                    desc.getConfiguration().getEndpointParameters());
            desc.getStatus().setRunning(true);
            updateService(desc.getConfiguration().getDeploymentURI(), desc);
            schedule(desc);
            int polls = 50;
            while(!isRunning(desc.getConfiguration().getDeploymentURI()) && polls > 0) {
                polls--;
                try {
                    Thread.sleep(START_POLL_INTERVAL);
                } catch(InterruptedException e) {
                    this.runNextSourceInChain = false;
                }
            }
            if(runNextSourceInChain) {
                boolean brk = false;
                while(brk || isRunning(desc.getConfiguration().getDeploymentURI())) {
                    try {
                        Thread.sleep(FINISH_POLL_INTERVAL);                        
                    } catch (InterruptedException e) {
                        brk = true;
                        this.runNextSourceInChain = false;
                    }
                }
                for(DataSourceDescription dataSource : dataSourceDao.listDataSources()) {
                    log.info(dataSource.getConfiguration().getURI() + " is scheduled to run after " + dataSource.getScheduleAfterURI());
                    if(dataSourceURI.equals(dataSource.getScheduleAfterURI())) {
                        log.info("Starting" + dataSource.getConfiguration().getURI());
                        startNow(dataSource.getConfiguration().getURI());
                    }
                }
            }
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
    
    protected boolean isRunning(String deploymentURI) {
        DataSourceDescriptionSerializer serializer = 
                new DataSourceDescriptionSerializer();        
        try {
            String result = httpUtils.getHttpResponse(deploymentURI);
            DataSourceDescription dataSource = serializer.unserialize(result);
            return dataSource.getStatus().isRunning();
        } catch (Exception e) {
            log.error(e, e);
            return false;
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
    
    Set<String> mutedForChangeListening = new HashSet<String>();
    
    private void muteChangeListener(String dataSourceURI) {
        mutedForChangeListening.add(dataSourceURI);
    }
    
    private void unmuteChangeListener(String dataSourceURI) {
        mutedForChangeListening.remove(dataSourceURI);
    }
    
    protected void doAddedModel(Model additions) {
        doChangedDataSources(additions);
    }
    
    protected void doRemovedModel(Model removals) {
        doChangedDataSources(removals);
    }

    private void doChangedDataSources(Model changes) {
        if(log.isDebugEnabled()) {
            log.debug("Heard " + changes.size() + " changes");
        }
        StmtIterator sit = changes.listStatements();
        while(sit.hasNext()) {
            Statement stmt = sit.next();
            if(stmt.getSubject().isURIResource() 
                    && mutedForChangeListening.contains(
                            stmt.getSubject().asResource().getURI())) {
                continue;
            }
            if(DataSourceDao.NEXTUPDATE.equals(stmt.getPredicate().getURI())
                    || DataSourceDao.UPDATEFREQUENCY.equals(stmt.getPredicate().getURI())
                    || DataSourceDao.SCHEDULEAFTER.equals(stmt.getPredicate().getURI())
                    ) {
                if(stmt.getSubject().isURIResource()) {
                    log.debug("Scheduling based on heard change");
                    schedule(dataSourceDao.getDataSource(
                            stmt.getSubject().asResource().getURI()));   
                }                
            }
        }
    }
    
}
