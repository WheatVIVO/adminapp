package org.wheatinitiative.vivo.mockup.datasource.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.semarglproject.vocab.XSD;
import org.wheatinitiative.vivo.datasource.DataSourceDescription;
import org.wheatinitiative.vivo.datasource.SparqlEndpointParams;
import org.wheatinitiative.vivo.mockup.datasource.DataSourceManager;

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

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;

public class DataSourceManagerImpl implements DataSourceManager {

    private static final String ADMIN_APP_TBOX = 
            "http://vivo.wheatinitiative.org/ontology/adminapp/";
    private static final String DATASOURCE = ADMIN_APP_TBOX + "DataSource";
    private static final String SPARQLENDPOINT = ADMIN_APP_TBOX + "SparqlEndpoint";
    private static final String USESSPARQLENDPOINT = ADMIN_APP_TBOX + "usesSparqlEndpoint";
    private static final String USESQUERYTERMSET = ADMIN_APP_TBOX + "usesQueryTermSet";
    private static final String QUERYTERM = ADMIN_APP_TBOX + "queryTerm";
    private static final String DEPLOYMENTURI = ADMIN_APP_TBOX + "deploymentURI";
    private static final String PRIORITY = ADMIN_APP_TBOX + "priority";
    private static final String LASTUPDATE = ADMIN_APP_TBOX + "lastUpdate";
    private static final String NEXTUPDATE = ADMIN_APP_TBOX + "nextUpdate";
    private static final String SERVICEURI = ADMIN_APP_TBOX + "serviceURI";
    private static final String ENDPOINTURI = ADMIN_APP_TBOX + "endpointURI";
    private static final String ENDPOINTUPDATEURI = ADMIN_APP_TBOX + "endpointUpdateURI";
    private static final String ENDPOINTUSERNAME = ADMIN_APP_TBOX + "username";
    private static final String ENDPOINTPASSWORD = ADMIN_APP_TBOX + "password";
    private static final String GRAPHURI = ADMIN_APP_TBOX + "graphURI";
    
    private static final Log log = LogFactory.getLog(DataSourceManager.class);
    
    private RDFService rdfService;
    
    public DataSourceManagerImpl(RDFService rdfService) {
        this.rdfService = rdfService;
    }
    
    String DATASOURCES_QUERY = "CONSTRUCT { \n" +
            "    ?dataSource ?p ?o . \n" +
            "    ?endpoint ?endpointP ?endpointO . \n" +
            "    ?queryTermSet ?queryTermP ?queryTermO \n" +
            "} WHERE { \n" +
            "    ?dataSource a <" + DATASOURCE + "> . \n" +
            "    ?dataSource ?p ?o . \n" +
            "    OPTIONAL { \n" +
            "        ?dataSource <" + USESSPARQLENDPOINT +"> ?endpoint . \n" +
            "        ?endpoint ?endpointP ?endpointO \n" +
            "    } \n" +
            "    OPTIONAL { \n" +
            "        ?dataSource <" + USESQUERYTERMSET +"> ?queryTermSet . \n" +
            "        ?queryTermSet ?queryTermP ?queryTermO \n" +
            "    } \n" +
            "} \n";
    
    String DATASOURCE_BY_GRAPH = "CONSTRUCT { \n" +
            "    ?dataSource ?p ?o . \n" +
            "    ?endpoint ?endpointP ?endpointO . \n" +
            "    ?queryTermSet ?queryTermP ?queryTermO \n" +
            "} WHERE { \n" +
            "    ?dataSource <" + GRAPHURI + "> ?graphURI . \n" +
            "    ?dataSource ?p ?o . \n" +
            "    OPTIONAL { \n" +
            "        ?dataSource <" + USESSPARQLENDPOINT +"> ?endpoint . \n" +
            "        ?endpoint ?endpointP ?endpointO \n" +
            "    } \n" +
            "    OPTIONAL { \n" +
            "        ?dataSource <" + USESQUERYTERMSET +"> ?queryTermSet . \n" +
            "        ?queryTermSet ?queryTermP ?queryTermO \n" +
            "    } \n" +
            "} \n";
    
    @Override
    public List<DataSourceDescription> listDataSources() { 
        return listDataSources(construct(DATASOURCES_QUERY));
    }
    
    private List<DataSourceDescription> listDataSources(Model model) {
        List<DataSourceDescription> dataSources = new ArrayList<DataSourceDescription>();
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
    public DataSourceDescription getDataSource(String URI) {
        String dataSourceQuery = DATASOURCES_QUERY
                .replaceAll("\\?dataSource", "<" + URI + ">");
        return getDataSource(URI, construct(dataSourceQuery));
    }
    
    private DataSourceDescription getDataSource(String URI, Model model) {
        DataSourceDescription ds = new DataSourceDescription();
        ds.getConfiguration().setURI(URI);
        ds.getConfiguration().setName(getStringValue(URI, RDFS.label.getURI(), model));
        ds.getConfiguration().setDeploymentURI(getStringValue(URI, DEPLOYMENTURI, model));        
        //ds.setLastUpdate(getDateValue(URI, LASTUPDATE, model));
        //ds.setNextUpdate(getDateValue(URI, NEXTUPDATE, model));
        ds.getConfiguration().setPriority(getIntValue(URI, PRIORITY, model));
        ds.getConfiguration().setServiceURI(getStringValue(URI, SERVICEURI, model));        
        //ds.setUpdateFrequency(DataSourceUpdateFrequency.WEEKLY);
        StmtIterator endpit = model.listStatements(model.getResource(URI), 
                model.getProperty(USESSPARQLENDPOINT), (RDFNode) null);
        try {
            while(endpit.hasNext()) {
                Statement endps = endpit.next();
                if(endps.getObject().isURIResource()) {
                    String endpoint = endps.getObject().asResource().getURI();
                    SparqlEndpointParams endpointParams = new SparqlEndpointParams();
                    endpointParams.setEndpointURI(getStringValue(endpoint, ENDPOINTURI, model));
                    endpointParams.setEndpointUpdateURI(getStringValue(endpoint, ENDPOINTUPDATEURI, model));
                    endpointParams.setUsername(getStringValue(endpoint, ENDPOINTUSERNAME, model));
                    endpointParams.setPassword(getStringValue(endpoint, ENDPOINTPASSWORD, model));
                    ds.getConfiguration().setEndpointParameters(endpointParams);
                    ds.getConfiguration().setResultsGraphURI(getStringValue(URI, GRAPHURI, model));  
                    break;
                }                   
            }
        } finally {
            endpit.close();
        }
        StmtIterator qtsit = model.listStatements(model.getResource(URI), 
                model.getProperty(USESQUERYTERMSET), (RDFNode) null);
        List<String> queryTerms = new ArrayList<String>();
        try {
            while(qtsit.hasNext()) {
                Statement qts = qtsit.next();
                if(qts.getObject().isURIResource()) {
                    StmtIterator queryTermIt = qts.getObject().asResource()
                            .listProperties(model.getProperty(QUERYTERM));
                    try {
                        while(queryTermIt.hasNext()) {
                            Statement queryTermStmt = queryTermIt.next();
                            if(queryTermStmt.getObject().isLiteral()) {
                                queryTerms.add(queryTermStmt.getObject()
                                        .asLiteral().getLexicalForm());
                            }
                        }
                    } finally {
                        queryTermIt.close();
                    }
                }                   
            }
        } finally {
            endpit.close();
            ds.getConfiguration().setQueryTerms(queryTerms);
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
    public DataSourceDescription getDataSourceByGraphURI(String graphURI) {
        String dataSourceQuery = DATASOURCE_BY_GRAPH
                .replaceAll("\\?graphURI", "\"" + graphURI + "\"^^<" + 
                        XSD.ANY_URI + ">");
        log.debug(dataSourceQuery);
        List<DataSourceDescription> dataSources = listDataSources(
                construct(dataSourceQuery));
        if(dataSources.isEmpty()) {
            return null;
        } else {
            return dataSources.get(0);
        }
    }
    
    
    private class DataSourcePriorityComparator implements Comparator<DataSourceDescription> {

        @Override
        public int compare(DataSourceDescription o1, DataSourceDescription o2) {
            if(o1 == null && o2 != null) {
                return 1;
            } else if (o2 == null && o1 != null) {
                return -1;
            } else if (o1 == null && o1 == null) {
                return 0;
            } else {
                return o1.getConfiguration().getPriority() 
                        - o2.getConfiguration().getPriority();
            }
        }
        
    }
    
}
