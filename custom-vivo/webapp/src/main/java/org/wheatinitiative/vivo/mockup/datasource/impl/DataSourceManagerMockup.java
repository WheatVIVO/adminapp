package org.wheatinitiative.vivo.mockup.datasource.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private Map<String, DataSource> graphToSourceMap;
    
    public static DataSourceManagerMockup getInstance() {
        if(instance == null) {
            instance = new DataSourceManagerMockup();
        }
        return instance;
    }
    
    private DataSourceManagerMockup() {
        try {
            initializeDataSourceMaps();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void initializeDataSourceMaps() throws ParseException {
        dataSourceMap = new HashMap<String, DataSource>();
        graphToSourceMap = new HashMap<String, DataSource>();
        DataSource prodinra = new OaiPmhDataSourceMockup();
        prodinra.setURI("http://vivo.wheatinitiative.org/individual/dataSource1");
        prodinra.setName("Prodinra");
        prodinra.setPriority(1);
        prodinra.setLastUpdate(getDate(-3, 0, 21));
        prodinra.setNextUpdate(getDate(+4, 0, 15));
        prodinra.setServiceURL("http://oai.prodinra.inra.fr/ft");
        prodinra.setDeploymentURL("http://localhost:8080/wheatvivo-adminapp/dataSource/prodinra");
        DataSourceStatusMockup prodinraStatus = new DataSourceStatusMockup();
        prodinraStatus.setStatusOk(true);
        prodinraStatus.setCompletionPercentage(100);
        prodinraStatus.setProcessedRecords(392);
        prodinraStatus.setTotalRecords(392);
        prodinraStatus.setErrorRecords(0);
        prodinra.setStatus(prodinraStatus);
        dataSourceMap.put(prodinra.getURI(), prodinra);
        graphToSourceMap.put("http://vitro.mannlib.cornell.edu/a/graph/Prodinra", prodinra);
        graphToSourceMap.put("http://vitro.mannlib.cornell.edu/a/graph/Prodinra-affiliations-anon", prodinra);
        graphToSourceMap.put("http://vitro.mannlib.cornell.edu/a/graph/Prodinra-affiliations-named", prodinra);
        graphToSourceMap.put("http://vitro.mannlib.cornell.edu/a/graph/Prodinra-constructions", prodinra);
        graphToSourceMap.put("http://vitro.mannlib.cornell.edu/a/graph/Prodinra-named", prodinra);
        graphToSourceMap.put("http://vitro.mannlib.cornell.edu/a/graph/Prodinra-types", prodinra);
        DataSource usda = new VivoDataSourceMockup();
        usda.setURI("http://vivo.wheatinitiative.org/individual/dataSource2");
        usda.setName("VIVO USDA");
        usda.setPriority(11);
        usda.setLastUpdate(getDate(-6, 1, 35));
        usda.setNextUpdate(null);
        usda.setDeploymentURL("http://localhost:8080/wheatvivo-adminapp/dataSource/usda");
        DataSourceStatusMockup usdaStatus = new DataSourceStatusMockup();
        usdaStatus.setStatusOk(false);
        usda.setStatus(usdaStatus);
        dataSourceMap.put(usda.getURI(), usda);
        DataSource texasAm = new VivoDataSourceMockup();
        texasAm.setURI("http://vivo.wheatinitiative.org/individual/dataSource3");
        texasAm.setName("VIVO Texas A&M University");
        texasAm.setPriority(12);
        texasAm.setLastUpdate(getDate(-4, 2, 15));
        prodinra.setNextUpdate(getDate(+3, 2, 01));
        DataSourceStatusMockup texasAmStatus = new DataSourceStatusMockup();
        texasAmStatus.setStatusOk(true);
        texasAm.setStatus(texasAmStatus);
        dataSourceMap.put(texasAm.getURI(), texasAm);
        DataSource rcuk = new OaiPmhDataSourceMockup();
        rcuk.setURI("http://vivo.wheatinitiative.org/individual/dataSource4");
        rcuk.setName("RCUK");
        rcuk.setPriority(2);
        rcuk.setLastUpdate(getDate(-2, 0, 19));
        rcuk.setNextUpdate(getDate(+5, 0, 16));
        rcuk.setServiceURL("http://http://gtr.rcuk.ac.uk/gtr/api/");
        rcuk.setDeploymentURL(
                "http://localhost:8080/wheatvivo-adminapp/dataSource/rcuk");
        DataSourceStatusMockup rcukStatus = new DataSourceStatusMockup();
        rcukStatus.setStatusOk(true);
        rcukStatus.setCompletionPercentage(17);
        rcukStatus.setProcessedRecords(100);
        rcukStatus.setTotalRecords(592);
        rcukStatus.setErrorRecords(0);
        rcuk.setStatus(rcukStatus);
        dataSourceMap.put(rcuk.getURI(), rcuk);
        graphToSourceMap.put("http://vitro.mannlib.cornell.edu/a/graph/RCUK", rcuk);
        DataSource wi = new DataSourceMockup();
        wi.setURI("http://vivo.wheatinitiative.org/individual/dataSource5");
        wi.setName("Wheat Initiative Website");
        wi.setPriority(5);
        wi.setLastUpdate(getDate(-3, 0, 17));
        wi.setNextUpdate(getDate(+4, 0, 19));
        wi.setServiceURL("http://www.wheatinitiative.org/administration/users/csv");
        wi.setDeploymentURL(
                "http://localhost:8080/wheatvivo-adminapp/dataSource/wheatinitiative");
        DataSourceStatusMockup wiStatus = new DataSourceStatusMockup();
        wiStatus.setStatusOk(true);
        wiStatus.setCompletionPercentage(100);
        wiStatus.setProcessedRecords(88);
        wiStatus.setTotalRecords(88);
        wiStatus.setErrorRecords(0);
        wi.setStatus(wiStatus);
        dataSourceMap.put(wi.getURI(), wi);
        graphToSourceMap.put("http://vitro.mannlib.cornell.edu/a/graph/wheatinitiative", wi);
    } 
    
    private Date getDate(int daysOffset, int hours, int minutes) {
        DateTime dt = new DateTime();
        if(daysOffset >= 0) {
            dt = dt.plusDays(daysOffset);
        } else {
            dt = dt.minusDays(Math.abs(daysOffset));
        }
        MutableDateTime mdt = dt.toMutableDateTimeISO();
        mdt.setHourOfDay(hours);
        mdt.setMinuteOfHour(minutes);
        return mdt.toDate();
    }
    
    private class DataSourceMockup implements DataSource {
        
        private String uri;
        private String name;
        private int priority;
        private Date lastUpdate;
        private Date nextUpdate;
        private DataSourceStatus status;
        private String serviceURL;
        private String deploymentURL;
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
        public String getDeploymentURL() {
            return this.deploymentURL;
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
        public void setDeploymentURL(String deploymentURL) {
            this.deploymentURL = deploymentURL;
        }

        @Override
        public void setUpdateFrequency(
                DataSourceUpdateFrequency updateFrequency) {
            this.updateFrequency = updateFrequency;
        }

        @Override
        public int getPriority() {
            return this.priority;
        }

        @Override
        public void setPriority(int priority) {
            this.priority = priority;
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
    
    private class DataSourcePriorityComparator implements Comparator<DataSource> {

        @Override
        public int compare(DataSource o1, DataSource o2) {
            if(o1 == null && o2 != null) {
                return 1;
            } else if (o2 == null && o1 != null) {
                return -1;
            } else if (o1 == null && o1 == null) {
                return 0;
            } else {
                return o1.getPriority() - o2.getPriority();
            }
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
        List<DataSource> dataSources = new ArrayList<DataSource>(dataSourceMap.values());
        Collections.sort(dataSources, new DataSourcePriorityComparator());
        return dataSources;
    }

    @Override
    public DataSource getDataSource(String URI) {
        return dataSourceMap.get(URI);
    }
    
    @Override 
    public DataSource getDataSourceByGraphURI(String graphURI) {
        return graphToSourceMap.get(graphURI);
    }
    
}
