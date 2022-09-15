package org.wheatinitiative.vivo.servlet.setup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;

/**
 * A class that creates labels for faux property context resources
 * in a "content" model that is visible to N3 editing
 * @author Brian Lowe
 *
 */
public class FauxPropertyLabeler implements ServletContextListener {

    private static final Log log = LogFactory.getLog(FauxPropertyLabeler.class);
    private static final String GRAPH_URI = "http://vivo.wheatinitiative.org/graph/x/fauxProperties";
    private static final String ADD_QUERY_STR = "\n"
            + "PREFIX : <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#>  \n" 
            + "CONSTRUCT { \n" 
            + "    ?context <" + RDFS.label.getURI() + "> ?label . \n"
            + "    ?context a :ConfigContext . \n" 
            + "} WHERE { \n" 
            + "    ?context a :ConfigContext . \n"
            + "    ?context :hasConfiguration ?configuration . \n"
            + "    ?configuration :displayName ?label . \n"
            + "} \n";
    private static final String REMOVE_QUERY_STR = "\n"
            + "CONSTRUCT { \n"
            + "    ?s ?p ?o \n"
            + "} WHERE { \n" 
            + "    GRAPH <" + GRAPH_URI + "> { ?s ?p ?o } \n" 
            + "} \n";
    
    private RDFService rdfService;
    private Model displayModel;
    
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        // nothing to destroy for now        
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        try {
            rdfService = ModelAccess.on(arg0.getServletContext()).getRDFService();
            displayModel = ModelAccess.on(
                    arg0.getServletContext()).getOntModelSelector().getDisplayModel();
            updateFauxPropertyLabels();
        } catch (RuntimeException e) {
            log.error(e, e);
            throw(e);
        }
        log.info("Faux property context labels updated in content model.");
    }
    
    private void updateFauxPropertyLabels() {
        Model add = ModelFactory.createDefaultModel();
        Model remove = ModelFactory.createDefaultModel();
        QueryExecution addEx = QueryExecutionFactory.create(ADD_QUERY_STR,
                displayModel);
        try {
            addEx.execConstruct(add);
        } finally {
            addEx.close();
        }
        try {
            rdfService.sparqlConstructQuery(REMOVE_QUERY_STR, remove);
        } catch (RDFServiceException e) {
            throw new RuntimeException(e);
        }
        log.info(remove.size() + " faux property triples in content model");
        log.info(add.difference(remove).size() + " triples to add");
        log.info(remove.difference(add).size() + " triples to remove");
        ChangeSet change = rdfService.manufactureChangeSet();
        change.addAddition(turtleStreamFromModel(
                add.difference(remove)), RDFService.ModelSerializationFormat.N3, 
                GRAPH_URI);
        change.addRemoval(turtleStreamFromModel(
                remove.difference(add)), RDFService.ModelSerializationFormat.N3, 
                GRAPH_URI);
        try {
            rdfService.changeSetUpdate(change);
        } catch (RDFServiceException e) {
            throw new RuntimeException(e);
        }
    }
    
    private InputStream turtleStreamFromModel(Model model) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        model.write(outputStream, "TTL");
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

}
