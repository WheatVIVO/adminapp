package org.wheatinitiative.vivo.mockup.datasource.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.wheatinitiative.vivo.mockup.datasource.CsvXmlDataSource;
import org.wheatinitiative.vivo.mockup.datasource.DataSource;
import org.wheatinitiative.vivo.mockup.datasource.DataSourceManager;
import org.wheatinitiative.vivo.mockup.datasource.DataSourceStatus;
import org.wheatinitiative.vivo.mockup.datasource.DataSourceUpdateFrequency;
import org.wheatinitiative.vivo.mockup.datasource.OaiPmhDataSource;
import org.wheatinitiative.vivo.mockup.datasource.RdfDataSource;
import org.wheatinitiative.vivo.mockup.datasource.VivoDataSource;

public class DataSourceManagerMockup implements DataSourceManager {

    private static DataSourceManagerMockup instance;
    private Map<String, DataSource> dataSourceMap;
    
    public static DataSourceManagerMockup getInstance() {
        if(instance == null) {
            instance = new DataSourceManagerMockup();
        }
        return instance;
    }
    
    private DataSourceManagerMockup() {
        dataSourceMap = new HashMap<String, DataSource>();
        try {
            initializeDataSourceMap();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void initializeDataSourceMap() throws ParseException {
        DataSource prodinra = new OaiPmhDataSourceMockup();
        prodinra.setURI("http://vivo.wheatinitiative.org/individual/dataSource1");
        prodinra.setName("Prodinra");
        prodinra.setLastUpdate(getDate(-3, 0, 21));
        prodinra.setNextUpdate(getDate(+4, 0, 15));
        DataSourceStatusMockup prodinraStatus = new DataSourceStatusMockup();
        prodinraStatus.setStatusOk(true);
        prodinra.setStatus(prodinraStatus);
        dataSourceMap.put(prodinra.getURI(), prodinra);
        DataSource usda = new VivoDataSourceMockup();
        usda.setURI("http://vivo.wheatinitiative.org/individual/dataSource2");
        usda.setName("VIVO USDA");
        usda.setLastUpdate(getDate(-6, 1, 35));
        usda.setNextUpdate(null);
        DataSourceStatusMockup usdaStatus = new DataSourceStatusMockup();
        usdaStatus.setStatusOk(false);
        usda.setStatus(usdaStatus);
        dataSourceMap.put(usda.getURI(), usda);
        DataSource texasAm = new VivoDataSourceMockup();
        texasAm.setURI("http://vivo.wheatinitiative.org/individual/dataSource3");
        texasAm.setName("VIVO Texas A&M University");
        texasAm.setLastUpdate(getDate(-4, 2, 15));
        prodinra.setNextUpdate(getDate(+3, 2, 01));
        DataSourceStatusMockup texasAmStatus = new DataSourceStatusMockup();
        texasAmStatus.setStatusOk(true);
        texasAm.setStatus(texasAmStatus);
        dataSourceMap.put(texasAm.getURI(), texasAm);
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
        
        private String uri;
        private String name;
        private Date lastUpdate;
        private Date nextUpdate;
        private DataSourceStatus status;
        private String serviceURL;
        private DataSourceUpdateFrequency updateFrequency;
        
        public String getURI() {
            return this.uri;
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
            if(this.status == null) {
                this.status = new DataSourceStatusMockup();    
            }
            return this.status;
        }

        @Override
        public String getServiceURL() {
            return this.serviceURL;
        }

        @Override
        public DataSourceUpdateFrequency getUpdateFrequency() {
            return this.updateFrequency;
        }

        @Override
        public void setURI(String URI) {
            this.uri = URI;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public void setLastUpdate(Date lastUpdate) {
            this.lastUpdate = lastUpdate;
        }

        @Override
        public void setNextUpdate(Date nextUpdate) {
            this.nextUpdate = nextUpdate;
        }

        @Override
        public void setStatus(DataSourceStatus status) {
            this.status = status;
        }

        @Override
        public void setServiceURL(String serviceURL) {
            this.serviceURL = serviceURL;
        }

        @Override
        public void setUpdateFrequency(
                DataSourceUpdateFrequency updateFrequency) {
            this.updateFrequency = updateFrequency;
        }
    }
    
    private class DataSourceStatusMockup implements DataSourceStatus {
        
        private boolean ok;
        private String message;
        private boolean isRunning;
        private int completionPercentage;
        private int totalRecords;
        private int processedRecords;
        private int errorRecords;

        @Override
        public boolean isStatusOk() {
            return this.ok;
        }

        @Override
        public String getMessage() {
            return this.message;
        }

        @Override
        public boolean isRunning() {
            return this.isRunning;
        }

        @Override
        public int getCompletionPercentage() {
            return this.completionPercentage;
        }

        @Override
        public int getTotalRecords() {
            return this.totalRecords;
        }

        @Override
        public int getProcessedRecords() {
            return this.processedRecords;
        }

        @Override
        public int getErrorRecords() {
            return this.errorRecords;
        }     

        private void setStatusOk(boolean ok) {
            this.ok = ok;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setRunning(boolean running) {
            this.isRunning = running;
        }

        public void setCompletionPercentage(int completionPercentage) {
            this.completionPercentage = completionPercentage;
        }

        public void setTotalRecords(int totalRecords) {
            this.totalRecords = totalRecords;
        }

        public void setProcessedRecords(int processedRecords) {
            this.processedRecords = processedRecords;
        }

        public void setErrorRecords(int errorRecords) {
            this.errorRecords = errorRecords;
        }             
    }

    private class OaiPmhDataSourceMockup extends DataSourceMockup 
    implements OaiPmhDataSource {
        // nothing specific yet
    }

    private class CsvXmlDataSourceMockup extends DataSourceMockup 
    implements CsvXmlDataSource {
        // nothing specific yet
    }

    private class RdfDataSourceMockup extends DataSourceMockup 
    implements RdfDataSource {
        // nothing specific yet
    }

    private class VivoDataSourceMockup extends RdfDataSourceMockup 
    implements VivoDataSource {
        // nothing specific yet
    }

    @Override
    public List<DataSource> listDataSources() {
        return new ArrayList<DataSource>(dataSourceMap.values());
    }

    @Override
    public DataSource getDataSource(String URI) {
        return dataSourceMap.get(URI);
    }
    
}
