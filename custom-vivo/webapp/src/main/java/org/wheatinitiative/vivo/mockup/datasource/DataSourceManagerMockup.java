package org.wheatinitiative.vivo.mockup.datasource;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

public class DataSourceManagerMockup implements DataSourceManager {

    @Override
    public List<DataSource> listDataSources() {
        List<DataSource> dataSources = new ArrayList<DataSource>();
        try {
            dataSources.add(new DataSourceMockup(
                    "Prodinra", getDate(-3, 0, 21), getDate(+4, 0, 15), true));
            dataSources.add(new DataSourceMockup(
                    "VIVO USDA", getDate(-6, 1, 35), null, false));
            dataSources.add(new DataSourceMockup(
                    "VIVO Texas A&M University", getDate(-4, 2, 15), getDate(+3, 2, 01), true));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return dataSources;
    }
    
    private Date getDate(int daysOffset, int hours, int minutes) {
        DateTime dt = new DateTime();
        if(daysOffset >= 0) {
            dt.plusDays(daysOffset);
        } else {
            dt.minusDays(daysOffset);
        }
        MutableDateTime mdt = dt.toMutableDateTimeISO();
        mdt.setHourOfDay(hours);
        mdt.setMinuteOfHour(minutes);
        return mdt.toDate();
    }
    
    private class DataSourceMockup implements DataSource {
        
        private String name;
        private Date lastUpdate;
        private Date nextUpdate;
        private DataSourceStatus status;
        
        public DataSourceMockup(String name, Date firstUpdate, 
                Date nextUpdate, boolean ok) throws ParseException {
            this.name = name;
            this.lastUpdate = firstUpdate;
            this.nextUpdate = nextUpdate;
            this.status = new DataSourceStatusMockup(ok, null);
        }
        
        public String getName() {
            return this.name;
        }
        
        public Date getLastUpdate() {
            return this.lastUpdate;
        }
        
        public Date getNextUpdate() {
            return this.nextUpdate;
        }
        
        public DataSourceStatus getStatus() {
            return this.status;
        }
    }
    
    private class DataSourceStatusMockup implements DataSourceStatus {
        
        private boolean ok;
        private String message;
        
        public DataSourceStatusMockup(boolean ok, String message) {
            this.ok = ok;
            this.message = message;
        }

        @Override
        public boolean isStatusOk() {
            return ok;
        }

        @Override
        public String getMessage() {
            return message;
        }     
        
    }
}
