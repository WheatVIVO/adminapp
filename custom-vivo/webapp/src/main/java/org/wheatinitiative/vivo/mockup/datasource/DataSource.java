package org.wheatinitiative.vivo.mockup.datasource;

import java.util.Date;

public interface DataSource {

    public abstract String getName();
    
    public abstract Date getLastUpdate();
    
    public abstract Date getNextUpdate();
    
    public abstract DataSourceStatus getStatus();
    
}
