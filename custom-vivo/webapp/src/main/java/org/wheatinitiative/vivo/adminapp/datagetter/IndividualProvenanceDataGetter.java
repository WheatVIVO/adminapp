package org.wheatinitiative.vivo.adminapp.datagetter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wheatinitiative.vivo.adminapp.datasource.RDFServiceModelConstructor;
import org.wheatinitiative.vivo.datasource.DataSourceDescription;
import org.wheatinitiative.vivo.datasource.dao.DataSourceDao;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.DataGetter;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.IndividualTemplateModel;

public class IndividualProvenanceDataGetter implements DataGetter {

    private static final String KB2_GRAPH = "http://vitro.mannlib.cornell.edu/default/vitro-kb-kb2";
    private static final String INF_GRAPH = "http://vitro.mannlib.cornell.edu/default/vitro-kb-inf";
    
    private VitroRequest vreq;
    private DataSourceDao mgr;
    
    private static final Log log = LogFactory.getLog(IndividualProvenanceDataGetter.class);
    
    /**
     * Constructor with display model and data getter URI that will be called by reflection.
     */
    public IndividualProvenanceDataGetter(VitroRequest vreq, Model displayModel, 
            String dataGetterURI){
        try {
            log.debug("Constructing datagetter");
            this.vreq = vreq; 
            this.mgr = new DataSourceDao(new RDFServiceModelConstructor(
                    vreq.getRDFService()));
        } catch (Exception e) {
            // because the code that invokes this by reflection is stupid
            // and doesn't log the nested exception
            log.error(e, e);
            throw new RuntimeException(e);
        }
    } 
    
    @Override
    public Map<String, Object> getData(Map<String, Object> existingData) {
        Map<String, Object> additionalData = new HashMap<String, Object>();
        Object o = existingData.get("individual");
        if(o instanceof IndividualTemplateModel) {
            IndividualTemplateModel individual = (IndividualTemplateModel) o;
            List<Source> sources = getSources(individual);
            additionalData.put("sources", sources);
        } else {
            String errorMsg = "Unexpected type for 'individual': expected " + 
                    IndividualTemplateModel.class.getSimpleName() + ", was " + 
                    o.getClass().getSimpleName();
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        return additionalData;
    }
    
    private List<Source> getSources(IndividualTemplateModel ind) {
        log.debug("Getting sources for " + ind.getUri());
        long start = System.currentTimeMillis();
        List<Source> sources = new ArrayList<Source>();
        String individualURI = ind.getUri();
        String query = "SELECT DISTINCT ?graph WHERE { GRAPH ?graph { <" + 
                individualURI + "> ?p ?o } } ORDER BY DESC(?g)";
        GraphURIGetter graphGetter = new GraphURIGetter();
        log.debug(query);
        try {
            vreq.getRDFService().sparqlSelectQuery(query, graphGetter);
        } catch (RDFServiceException e) {
            log.error(e, e);
            throw new RuntimeException(e);
        }
        Set<String> graphURISet = new HashSet<String>();
        for (String graph : graphGetter.getGraphURIs()) {
            String[] graphParts = graph.split("-", 2);
            String graphURI = graphParts[0];
            String dateTime = null;
            if(graphParts.length > 1) {
                dateTime = graphParts[1].replaceAll("T", " ");
            }
            if(KB2_GRAPH.equals(graphURI) || INF_GRAPH.equals(graphURI)) {
                continue;
            }
            DataSourceDescription dataSource = mgr.getDataSourceByGraphURI(graphURI);
            if (dataSource != null) {
                log.debug("Found data source " + dataSource.getConfiguration().getName() + 
                        " for individual " + individualURI);
                if(!graphURISet.contains(graphURI)) {
                    graphURISet.add(graphURI);
                    sources.add(new Source(dataSource.getConfiguration().getName(), 
                        dataSource.getConfiguration().getURI(), dateTime));
                }
            } else {
                log.debug("Checking for merge rule with URI " + graphURI);
                try {
                    Individual mergeRuleInd = vreq.getWebappDaoFactory()
                            .getIndividualDao().getIndividualByURI(graphURI);
                    if(mergeRuleInd != null) {
                        sources.add(new Source(
                                mergeRuleInd.getName(), mergeRuleInd.getURI(), null));
                    }
                } catch (Exception e) {
                    log.error(e, e);
                }
            }
        }
        long duration = System.currentTimeMillis() - start;
        String logMessage = duration + " ms to get graphs for individual "
                + individualURI;
        if(duration <= 100) {
            log.debug(logMessage);
        } else if (duration < 250) {
            log.info(logMessage);
        } else {
            log.warn(logMessage);
        }
        return sources;
    }
    
    private class GraphURIGetter extends ResultSetConsumer {
        private HashSet<String> sources = new HashSet<String>();
        
        @Override
        protected void processQuerySolution(QuerySolution soln) {
            Resource graph = soln.getResource("graph");
            sources.add(graph.getURI());
        }
        
        public Set<String> getGraphURIs() {
            return sources;
        }
    }
    
    public class Source {
        
        private String name;
        private String uri;
        private String dateTime;
        
        public Source(String name, String uri, String dateTime) {
            this.name = name;
            this.uri = uri;
            this.dateTime = dateTime;
        }
        
        public String getName() {
            return this.name;
        }
        
        public String getUri() {
            return this.uri;
        }
        
        public String getDateTime() {
            return this.dateTime;
        }
        
    }

}
