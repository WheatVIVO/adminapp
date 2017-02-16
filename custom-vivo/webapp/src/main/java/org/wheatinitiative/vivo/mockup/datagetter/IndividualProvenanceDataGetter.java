package org.wheatinitiative.vivo.mockup.datagetter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wheatinitiative.vivo.datasource.DataSourceDescription;
import org.wheatinitiative.vivo.mockup.datasource.DataSourceManager;
import org.wheatinitiative.vivo.mockup.datasource.impl.DataSourceManagerImpl;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.DataGetter;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.IndividualTemplateModel;

public class IndividualProvenanceDataGetter implements DataGetter {

    private static final String KB2_GRAPH = "http://vitro.mannlib.cornell.edu/default/vitro-kb-kb2";
    private static final String INF_GRAPH = "http://vitro.mannlib.cornell.edu/default/vitro-kb-inf";
    
    private VitroRequest vreq;
    private DataSourceManager mgr;
    
    private static final Log log = LogFactory.getLog(IndividualProvenanceDataGetter.class);
    
    /**
     * Constructor with display model and data getter URI that will be called by reflection.
     */
    public IndividualProvenanceDataGetter(VitroRequest vreq, Model displayModel, 
            String dataGetterURI){
        try {
            log.info("Constructing datagetter");
            this.vreq = vreq; 
            this.mgr = new DataSourceManagerImpl(vreq.getRDFService());
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
            throw new RuntimeException("Unexpected type for 'individual': expected " + 
                    IndividualTemplateModel.class.getSimpleName() + ", was " + 
                    o.getClass().getSimpleName());
        }
        return additionalData;
    }
    
    private List<Source> getSources(IndividualTemplateModel ind) {
        long start = System.currentTimeMillis();
        List<Source> sources = new ArrayList<Source>();
        String individualURI = ind.getUri();
        String query = "SELECT DISTINCT ?graph WHERE { GRAPH ?graph { <" + 
                individualURI + "> ?p ?o } }";
        GraphURIGetter graphGetter = new GraphURIGetter();
        try {
            vreq.getRDFService().sparqlSelectQuery(query, graphGetter);
        } catch (RDFServiceException e) {
            throw new RuntimeException(e);
        }
        Set<DataSourceDescription> dataSources = new HashSet<DataSourceDescription>();
        for (String graphURI : graphGetter.getGraphURIs()) {
            if(KB2_GRAPH.equals(graphURI) || INF_GRAPH.equals(graphURI)) {
                continue;
            }
            DataSourceDescription dataSource = mgr.getDataSourceByGraphURI(graphURI);
            if (dataSource != null) {
                log.debug("Found data source " + dataSource.getConfiguration().getName() + 
                        " for individual " + individualURI);
                dataSources.add(dataSource);
            }
        }
        for (DataSourceDescription dataSource : dataSources) {
            sources.add(new Source(dataSource.getConfiguration().getName(), 
                    dataSource.getConfiguration().getURI()));
        }
        long duration = System.currentTimeMillis() - start;
        String logMessage = duration + " ms to get graphs for individual "
                + individualURI;
        if(duration <= 50) {
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
        
        public Source(String name, String uri) {
            this.name = name;
            this.uri = uri;
        }
        
        public String getName() {
            return this.name;
        }
        
        public String getUri() {
            return this.uri;
        }
        
    }

}
