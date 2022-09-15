package org.wheatinitiative.vivo.adminapp.controller;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * Outputs the faux property context RDF from the display model so that
 * services (such as merge services) can make use of it in addition to the
 * normal RDF that's available in the main triple store.
 * @author Brian Lowe
 *
 */
@WebServlet(name = "FauxPropertyContextExportController", urlPatterns = {"/fauxPropertyContexts/*"} )
public class FauxPropertyContextExportController extends HttpServlet {

    private static final String APPLICATION_CONTEXT_NS = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#";
    private final Log log = LogFactory.getLog(FauxPropertyContextExportController.class);
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        VitroRequest vreq = new VitroRequest(request);    
        Model displayModel = vreq.getDisplayModel();
        Model contexts = ModelFactory.createDefaultModel();
        String queryStr = "DESCRIBE ?x WHERE { \n" 
                + "    ?x a <" + APPLICATION_CONTEXT_NS + "ConfigContext> \n"  
                + "} \n";
        QueryExecution qe = QueryExecutionFactory.create(queryStr, displayModel);
        try {
            qe.execDescribe(contexts);
            OutputStream os = response.getOutputStream();
            response.setContentType("text/turtle");
            contexts.write(os, "TTL");
            os.flush();
        } finally {
            qe.close();
        }
    }
    
}
