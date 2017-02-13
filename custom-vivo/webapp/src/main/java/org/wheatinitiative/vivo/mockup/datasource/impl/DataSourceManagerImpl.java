package org.wheatinitiative.vivo.mockup.datasource.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wheatinitiative.vivo.mockup.datasource.DataSource;
import org.wheatinitiative.vivo.mockup.datasource.DataSourceManager;
import org.wheatinitiative.vivo.mockup.datasource.DataSourceStatus;
import org.wheatinitiative.vivo.mockup.datasource.DataSourceUpdateFrequency;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceGraph;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;

public class DataSourceManagerImpl implements DataSourceManager {

    private static final String ADMIN_APP_TBOX = 
            "http://vivo.wheatinitiative.org/ontology/adminapp/";
    private static final String DATASOURCE = ADMIN_APP_TBOX + "DataSource";
    private static final String SPARQLENDPOINT = ADMIN_APP_TBOX + "SparqlEndpoint";
    private static final String USESSPARQLENDPOINT = ADMIN_APP_TBOX + "usesSparqlEndpoint";
    private static final String DEPLOYMENTURI = ADMIN_APP_TBOX + "deploymentURI";
    private static final String PRIORITY = ADMIN_APP_TBOX + "priority";
    private static final String LASTUPDATE = ADMIN_APP_TBOX + "lastUpdate";
    private static final String NEXTUPDATE = ADMIN_APP_TBOX + "nextUpdate";
    private static final String SERVICEURI = ADMIN_APP_TBOX + "serviceURI";
    private static final String ENDPOINTURI = ADMIN_APP_TBOX + "endpointURI";
    private static final String ENDPOINTUSERNAME = ADMIN_APP_TBOX + "endpointUsername";
    private static final String ENDPOINTPASSWORD = ADMIN_APP_TBOX + "endpointPassword";
    private static final String GRAPHURI = ADMIN_APP_TBOX + "graphURI";
    
    private static final Log log = LogFactory.getLog(DataSourceManager.class);
    
    private RDFService rdfService;
    private Model model;
    
    public DataSourceManagerImpl(RDFService rdfService) {
        this.rdfService = rdfService;
        this.model = RDFServiceGraph.createRDFServiceModel(
                new RDFServiceGraph(rdfService));
    }
    
    String DATASOURCES_QUERY = "CONSTRUCT { \n" +
            "    ?dataSource ?p ?o . \n" +
            "    ?dataSource <" + USESSPARQLENDPOINT +"> ?endpoint . \n" +
            "    ?endpoint ?endpointP ?endpointO \n" +
            "} WHERE { \n" +
            "    ?dataSource a <" + SPARQLENDPOINT + "> . \n" +
            "    ?dataSource ?p ?o . \n" +
            "    OPTIONAL { ?dataSource <" + PRIORITY + "> ?priority } \n" +
            "    OPTIONAL { \n" +
            "        ?dataSource <" + USESSPARQLENDPOINT +"> ?endpoint . \n" +
            "        ?endpoint ?endpointP ?endpointO \n" +
            "    } \n" +
            "} ORDER BY ?priority \n";
    
    String DATASOURCE_BY_GRAPH = "CONSTRUCT { \n" +
            "    ?dataSource ?p ?o . \n" +
            "    ?dataSource <" + USESSPARQLENDPOINT +"> ?endpoint . \n" +
            "    ?endpoint ?endpointP ?endpointO \n" +
            "} WHERE { \n" +
            "    ?dataSource <" + GRAPHURI + "> ?graphURI . \n" +
            "    ?dataSource ?p ?o . \n" +
            "    OPTIONAL { ?dataSource <" + PRIORITY + "> ?priority } \n" +
            "    OPTIONAL { \n" +
            "        ?dataSource <" + USESSPARQLENDPOINT +"> ?endpoint . \n" +
            "        ?endpoint ?endpointP ?endpointO \n" +
            "    } \n" +
            "} ORDER BY ?priority \n";
    
    @Override
    public List<DataSource> listDataSources() { 
        return listDataSources(construct(DATASOURCES_QUERY));
    }
    
    private List<DataSource> listDataSources(Model model) {
        List<DataSource> dataSources = new ArrayList<DataSource>();
        ResIterator resIt = model.listResourcesWithProperty(
                RDF.type, model.getResource(DATASOURCE));
        while(resIt.hasNext()) {
            Resource res = resIt.next();
            if(res.isURIResource()) {
                dataSources.add(this.getDataSource(res.getURI(), model));
            }
        }
        return dataSources;
    }
    
    private Model construct(String queryStr) {
        Model model = ModelFactory.createDefaultModel();
        try {
            rdfService.sparqlConstructQuery(queryStr, model);
            return model;
        } catch (RDFServiceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DataSource getDataSource(String URI) {
        String dataSourceQuery = DATASOURCES_QUERY
                .replaceAll("\\?dataSource", "<" + URI + ">");
        return getDataSource(URI, construct(dataSourceQuery));
    }
    
    private DataSource getDataSource(String URI, Model model) {
        DataSource ds = new DataSourceImpl();
        ds.setURI(URI);
        ds.setName(getStringValue(URI, RDFS.label.getURI(), model));
        ds.setDeploymentURL(getStringValue(URI, DEPLOYMENTURI, model));        
        ds.setLastUpdate(getDateValue(URI, LASTUPDATE, model));
        ds.setNextUpdate(getDateValue(URI, NEXTUPDATE, model));
        ds.setPriority(getIntValue(URI, PRIORITY, model));
        ds.setServiceURL(getStringValue(URI, SERVICEURI, model));        
        ds.setUpdateFrequency(DataSourceUpdateFrequency.WEEKLY);
        StmtIterator endpit = model.listStatements(model.getResource(URI), 
                model.getProperty(USESSPARQLENDPOINT), (RDFNode) null);
        try {
            while(endpit.hasNext()) {
                Statement endps = endpit.next();
                if(endps.getObject().isURIResource()) {
                    String endpoint = endps.getObject().asResource().getURI();
                    ds.setEndpointURL(getStringValue(endpoint, ENDPOINTURI, model));
                    ds.setEndpointUsername(getStringValue(endpoint, ENDPOINTUSERNAME, model));
                    ds.setEndpointPassword(getStringValue(endpoint, ENDPOINTPASSWORD, model));
                    ds.setEndpointURL(getStringValue(URI, GRAPHURI, model));  
                    break;
                }                   
            }
        } finally {
            endpit.close();
        }
        
        return ds;
    }
    
    private String getStringValue(String subjectURI, String propertyURI, 
            Model model) {
        StmtIterator sit = model.listStatements(model.getResource(subjectURI), 
                model.getProperty(propertyURI), (RDFNode) null);
        try {
            while(sit.hasNext()) {
                Statement stmt = sit.next();
                if(stmt.getObject().isLiteral()) {
                    return stmt.getObject().asLiteral().getLexicalForm();
                }
            }
            return null;
        } finally {
            sit.close();
        }
    }
    
    private Date getDateValue(String subjectURI, String propertyURI, 
            Model model) {
        StmtIterator sit = model.listStatements(model.getResource(subjectURI), 
                model.getProperty(propertyURI), (RDFNode) null);
        try {
            while(sit.hasNext()) {
                Statement stmt = sit.next();
                if(stmt.getObject().isLiteral()) {
                    Literal lit = stmt.getObject().asLiteral();
                    Object obj = lit.getValue();
                    if(obj instanceof XSDDateTime) {
                        XSDDateTime dateTime = (XSDDateTime) obj;
                        return dateTime.asCalendar().getTime();
                    }
                }
            }
            return null;
        } finally {
            sit.close();
        }
    }
    
    private int getIntValue(String subjectURI, String propertyURI, 
            Model model) {
        StmtIterator sit = model.listStatements(model.getResource(subjectURI), 
                model.getProperty(propertyURI), (RDFNode) null);
        try {
            while(sit.hasNext()) {
                Statement stmt = sit.next();
                if(stmt.getObject().isLiteral()) {
                    Literal lit = stmt.getObject().asLiteral();
                    Object obj = lit.getValue();
                    if(obj instanceof Integer) {
                        Integer intg = (Integer) obj;
                        return intg;
                    }
                }
            }
            return Integer.MAX_VALUE;
        } finally {
            sit.close();
        }
    }

    @Override
    public DataSource getDataSourceByGraphURI(String graphURI) {
        String dataSourceQuery = DATASOURCE_BY_GRAPH
                .replaceAll("\\?graphURI", "<" + graphURI + ">");
        List<DataSource> dataSources = listDataSources(
                construct(dataSourceQuery));
        if(dataSources.isEmpty()) {
            return null;
        } else {
            return dataSources.get(0);
        }
    }
    
    // in second round of refactoring, replace with actual classes from
    // DataSources library
    private class DataSourceImpl implements DataSource {        
        private String uri;
        private String name;
        private int priority;
        private Date lastUpdate;
        private Date nextUpdate;
        private DataSourceStatus status;
        private String serviceURL;
        private String deploymentURL;
        private DataSourceUpdateFrequency updateFrequency;
        private String endpointURL;
        private String endpointUsername;
        private String endpointPassword;
        private String resultsGraphURL;
        
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
                this.status = new DataSourceStatusImpl();    
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

        @Override
        public String getEndpointURL() {
            return this.endpointURL;
        }

        @Override
        public String getEndpointUsername() {
            return this.endpointUsername;
        }

        @Override
        public String getEndpointPassword() {
            return this.endpointPassword;
        }

        @Override
        public String getResultsGraphURI() {
            return this.resultsGraphURL;
        }

        @Override
        public void setEndpointURL(String endpointURL) {
            this.endpointURL = endpointURL;
        }

        @Override
        public void setEndpointUsername(String endpointUsername) {
            this.endpointUsername = endpointUsername;   
        }

        @Override
        public void setEndpointPassword(String endpointPassword) {
            this.endpointPassword = endpointPassword;
        }

        @Override
        public void setResultsGraphURI(String resultsGraphURI) {
            this.resultsGraphURL = resultsGraphURI;
        }
    }

    private class DataSourceStatusImpl implements DataSourceStatus {
        
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


}
