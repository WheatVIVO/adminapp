package org.wheatinitiative.vivo.adminapp.datasource;

import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

public class DataSourceScheduler implements ServletContextListener, ChangeListener {
    
    TaskScheduler scheduler = new DefaultManagedTaskScheduler();
    
    private HashMap<String, DataSourceTask> uriToTask = 
            new HashMap<String, DataSourceTask>();
    private HashMap<DataSourceTask, ScheduledFuture<?>> taskToFuture =  
            new HashMap<DataSourceTask, ScheduledFuture<?>>();
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        for(ScheduledFuture<?> f : this.taskToFuture.values()) {
            f.cancel(true);
        }
        sce.getServletContext().setAttribute(this.getClass().getName(), null);
        try {
            RDFServiceUtils.getRDFServiceFactory(
                    sce.getServletContext()).unregisterListener(this);
        } catch (RDFServiceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().setAttribute(this.getClass().getName(), this);
        try {
            RDFServiceUtils.getRDFServiceFactory(
                    sce.getServletContext()).registerListener(this);
        } catch (RDFServiceException e) {
            throw new RuntimeException(e);
        }
        
        scheduleTestTask();
    }
    
    private void scheduleTestTask() {
        DataSourceTask testTask = new DataSourceTask();
        this.uriToTask.put("http://example.com/testTask", testTask);
        this.taskToFuture.put(testTask, scheduler.scheduleAtFixedRate(
                new DataSourceTask(), 10000 /* ms */));
    }

    @Override
    public void notifyEvent(String arg0, Object arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void notifyModelChange(ModelChange arg0) {
        // TODO Auto-generated method stub        
    }

  
    
    
}
