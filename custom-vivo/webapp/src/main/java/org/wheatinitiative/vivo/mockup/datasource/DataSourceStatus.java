package org.wheatinitiative.vivo.mockup.datasource;

public interface DataSourceStatus {

    public abstract boolean isStatusOk();
    
    public abstract String getMessage();
    
    public abstract boolean isRunning();
    
    public abstract int getCompletionPercentage();
    
    public abstract int getTotalRecords();
    
    public abstract int getProcessedRecords();
    
    public abstract int getErrorRecords();
    
}
