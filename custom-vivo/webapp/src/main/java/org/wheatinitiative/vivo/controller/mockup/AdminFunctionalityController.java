package org.wheatinitiative.vivo.controller.mockup;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * A temporary controller for dispatching to mockup pages for the process
 * of iteratively designing the admin functionality
 * @author Brian Lowe
 *
 */
public class AdminFunctionalityController extends FreemarkerHttpServlet {

    private static final Log log = LogFactory.getLog(
            AdminFunctionalityController.class);
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        String featureName = vreq.getParameter("feature");
        if(featureName == null) {
            throw new RuntimeException("Parameter 'feature' must be specified");
        }
        String templateName = featureName + ".ftl"; 
        log.debug("requestedFeature='" + featureName + "', templateName='"
                + templateName + "'");
        Map<String, Object> data = new HashMap<String, Object>();
        
        if("configureDataSources".equals(featureName)) {
               // in case we need to supply actual data to the mockup
        } else {
            throw new RuntimeException("Unrecognized feature name " 
                    + featureName);
        }
        
        return new TemplateResponseValues(templateName, data);
    }    
    
}
