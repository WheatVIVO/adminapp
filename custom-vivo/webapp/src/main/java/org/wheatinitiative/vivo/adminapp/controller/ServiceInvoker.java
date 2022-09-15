package org.wheatinitiative.vivo.adminapp.controller;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;
import org.wheatinitiative.vivo.adminapp.datasource.DataSourceScheduler;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;

@WebServlet(name = "ServiceInvoker", urlPatterns = {"/invokeService/*"} )
public class ServiceInvoker extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(ServiceInvoker.class);
    private static final String REDIRECT_PAGE = "/listDataSources";
    protected static final AuthorizationRequest REQUIRED_ACTIONS = 
            SimplePermission.USE_MISCELLANEOUS_ADMIN_PAGES.ACTION;
    
    @Override
    protected AuthorizationRequest requiredActions(VitroRequest vreq) {
        return REQUIRED_ACTIONS;
    }
        
    @Override
    public ResponseValues processRequest(VitroRequest request) 
            throws IOException, URISyntaxException {
        VitroRequest vreq = new VitroRequest(request);
        String dataSourceURI = vreq.getParameter("uri");
        DataSourceScheduler scheduler = DataSourceScheduler.getInstance(
                getServletContext());
        if(vreq.getParameter("start") != null) {
            log.info("Starting data source");
            scheduler.startNow(dataSourceURI);
        } else if (vreq.getParameter("stop") != null) {
            log.info("Stopping data source");
            scheduler.stopNow(dataSourceURI);
        }
        try {
            Thread.sleep(1000); // wait for things to start before redirecting user
        } catch (InterruptedException e) {
            // ignore
        }
        URIBuilder locationB = new URIBuilder(REDIRECT_PAGE);
        String type = vreq.getParameter("type");
        if(type != null) {
            locationB.addParameter("type", type);        
        }
        return new RedirectResponseValues(locationB.toString(), 
                HttpServletResponse.SC_SEE_OTHER);
    }
    
}
