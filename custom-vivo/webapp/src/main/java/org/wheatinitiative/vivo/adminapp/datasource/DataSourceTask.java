package org.wheatinitiative.vivo.adminapp.datasource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataSourceTask implements Runnable {

    private static final Log log = LogFactory.getLog(DataSourceTask.class);

    @Override
    public void run() {
        //TODO
        log.info("Hi there.  I need to have some code from ServiceInvoker "
                + "refactored over here, please");
        log.info("Done for now");
    }

}
