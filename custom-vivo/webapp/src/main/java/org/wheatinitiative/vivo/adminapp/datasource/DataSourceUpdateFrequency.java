package org.wheatinitiative.vivo.adminapp.datasource;

public enum DataSourceUpdateFrequency {

    DAILY("daily"), WEEKLY("weekly"), WEEKDAYS("every weekday"),
    WEEKENDS("Saturday and Sunday only"), MONTHLY("monthly");
    
    private String label;
    
    private DataSourceUpdateFrequency(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return this.label;
    }
    
}
