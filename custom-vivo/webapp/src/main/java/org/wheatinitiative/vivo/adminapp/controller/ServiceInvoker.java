package org.wheatinitiative.vivo.adminapp.controller;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;
import org.wheatinitiative.vivo.adminapp.datasource.RDFServiceModelConstructor;
import org.wheatinitiative.vivo.datasource.DataSourceDescription;
import org.wheatinitiative.vivo.datasource.dao.DataSourceDao;
import org.wheatinitiative.vivo.datasource.service.DataSourceDescriptionSerializer;
import org.wheatinitiative.vivo.datasource.util.http.HttpUtils;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;

public class ServiceInvoker extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(ServiceInvoker.class);
    private static final String REDIRECT_PAGE = "/listDataSources";
    private HttpUtils httpUtils = new HttpUtils();
    protected static final AuthorizationRequest REQUIRED_ACTIONS = 
            SimplePermission.USE_MISCELLANEOUS_ADMIN_PAGES.ACTION;
    
    @Override
    protected AuthorizationRequest requiredActions(VitroRequest vreq) {
        return REQUIRED_ACTIONS;
    }
    
    // It would be awfully nice to send an error if someone tries to GET this 
    // servlet, but it would mean overriding everything in FreemarkerHttpServlet,
    // which implements doGet() and then calls
    // doGet() from doPost(), instead of putting the processing in a separate 
    // method and calling it from each.
//    @Override 
//    public void doGet(HttpServletRequest request, HttpServletResponse response) 
//            throws IOException {
//        // GETting this servlet is not allowed
//        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
//    }
    
    @Override
    public ResponseValues processRequest(VitroRequest request) 
            throws IOException, URISyntaxException {
        VitroRequest vreq = new VitroRequest(request);
        DataSourceDao dsm = new DataSourceDao(
                new RDFServiceModelConstructor(vreq.getRDFService()));
        String dataSourceURI = vreq.getParameter("uri");
        DataSourceDescription dataSource = dsm.getDataSource(dataSourceURI);
        String defaultNamespace = ConfigurationProperties.getBean(
                request.getSession().getServletContext()).getProperty(
                        "Vitro.defaultNamespace");
        if(defaultNamespace != null) {
            dataSource.getConfiguration().getParameterMap().put(
                    "Vitro.defaultNamespace", defaultNamespace);
        }
        String deploymentURL = dataSource.getConfiguration().getDeploymentURI();
        if(deploymentURL != null) {
            if(vreq.getParameter("start") != null) {
                log.info("Starting data source");
                this.startService(dataSource);
            } else if (vreq.getParameter("stop") != null) {
                log.info("Stopping data source");
                this.stopService(dataSource);
            }
        }
        URIBuilder locationB = new URIBuilder(REDIRECT_PAGE);
        String type = vreq.getParameter("type");
        if(type != null) {
            locationB.addParameter("type", type);        
        }
        return new RedirectResponseValues(locationB.toString(), 
                HttpServletResponse.SC_SEE_OTHER);
    }
    
    private DataSourceDescription startService(DataSourceDescription description) {
        description.getStatus().setRunning(true);
        return updateService(description.getConfiguration().getDeploymentURI(),
                description);
    }
    
    private DataSourceDescription stopService(DataSourceDescription description) {
        description.getStatus().setRunning(false);
        return updateService(description.getConfiguration().getDeploymentURI(),
                description);         
    }
    
    private DataSourceDescription updateService(
            String serviceURL, DataSourceDescription description) {
        DataSourceDescriptionSerializer serializer = 
                new DataSourceDescriptionSerializer();
        String json = serializer.serialize(description);
        String result = httpUtils.getHttpPostResponse(
                serviceURL, json, "application/json");
        try {
            DataSourceDescription desc = serializer.unserialize(result);
            return desc;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Exception parsing response from " + serviceURL + ": \n" + result);
        }
    }
    
}
