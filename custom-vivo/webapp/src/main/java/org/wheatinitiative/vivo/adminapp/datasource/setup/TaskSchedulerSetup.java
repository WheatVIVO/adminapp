package org.wheatinitiative.vivo.adminapp.datasource.setup;

import java.util.concurrent.ScheduledFuture;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler;
import org.wheatinitiative.vivo.adminapp.datasource.DataSourceTask;


public class TaskSchedulerSetup implements ServletContextListener {
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // TODO get rid of this class?
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // TODO get rid of this class?
    }
   
}
