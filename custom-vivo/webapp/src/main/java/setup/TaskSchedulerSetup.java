package setup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.commonj.TimerManagerTaskScheduler;


public class TaskSchedulerSetup implements ServletContextListener {

    public final static String TASK_SCHEDULER_ATTRIBUTE = 
            TaskSchedulerSetup.class.getName() + "/TaskScheduler";
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        TaskScheduler scheduler = new TimerManagerTaskScheduler();
        
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // TODO Auto-generated method stub
        
    }

}
