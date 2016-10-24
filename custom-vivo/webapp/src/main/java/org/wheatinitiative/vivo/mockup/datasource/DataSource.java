package org.wheatinitiative.vivo.mockup.datasource;

import java.util.Date;

public interface DataSource {

    public abstract String getURI();
    
    public abstract String getName();

    public abstract int getPriority();
    
    public abstract Date getLastUpdate();
    
    public abstract Date getNextUpdate();
    
    public abstract DataSourceStatus getStatus();
    
    public abstract String getServiceURL();
    
    public abstract DataSourceUpdateFrequency getUpdateFrequency();
    
    public abstract void setURI(String URI);
    
    public abstract void setName(String name);
    
    public abstract void setPriority(int priority);
    
    public abstract void setLastUpdate(Date lastUpdate);
    
    public abstract void setNextUpdate(Date nextUpdate);
    
    public abstract void setStatus(DataSourceStatus status);
    
    public abstract void setServiceURL(String serviceURL);
    
    public abstract void setUpdateFrequency(DataSourceUpdateFrequency updateFrequency);
    
}
