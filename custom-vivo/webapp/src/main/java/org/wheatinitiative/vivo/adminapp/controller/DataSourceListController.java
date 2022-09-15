package org.wheatinitiative.vivo.adminapp.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wheatinitiative.vivo.adminapp.datasource.DataSourceManager;
import org.wheatinitiative.vivo.adminapp.datasource.RDFServiceModelConstructor;
import org.wheatinitiative.vivo.datasource.DataSourceDescription;
import org.wheatinitiative.vivo.datasource.DataSourceStatus;
import org.wheatinitiative.vivo.datasource.dao.DataSourceDao;
import org.wheatinitiative.vivo.datasource.service.DataSourceDescriptionSerializer;
import org.wheatinitiative.vivo.datasource.util.http.HttpUtils;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * A controller for retrieving for display lists of available 
 * data, merging and publishing services
 * @author Brian Lowe
 *
 */
@WebServlet(name = "DataSourceListController", urlPatterns = {"/listDataSources/*"} )
public class DataSourceListController extends FreemarkerHttpServlet {

    private static final String LIST_DATA_SOURCES_TEMPLATE = 
            "listDataSources.ftl";
    private static final Log log = LogFactory.getLog(
            DataSourceListController.class);
    private static HttpUtils httpUtils = new HttpUtils();
    protected static final AuthorizationRequest REQUIRED_ACTIONS = 
            SimplePermission.USE_MISCELLANEOUS_ADMIN_PAGES.ACTION;
    
    @Override
    protected AuthorizationRequest requiredActions(VitroRequest vreq) {
        return REQUIRED_ACTIONS;
    }
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) 
            throws IOException {
        DataSourceDao dsm = new DataSourceDao(
                new RDFServiceModelConstructor(vreq.getRDFService()));
        String type = vreq.getParameter("type");
        log.debug("Data source type: " + type);
        List<DataSourceDescription> sources;
        if("merge".equals(type)) {
            sources = dsm.listMergeDataSources(); 
        } else if("publish".equals(type)) {
            sources = dsm.listPublishDataSources();
        } else {
            sources = dsm.listDataSources();
        }        
        return doListDataSources(sources, vreq);
    }    
    
    private TemplateResponseValues doListDataSources(
            List<DataSourceDescription> descriptions, VitroRequest vreq) 
                    throws IOException {
        Map<String, Object> data = new HashMap<String, Object>();
        descriptions = pollStatus(descriptions);
        data.put("dataSources", descriptions);
        data.put("type", vreq.getParameter("type"));
        return new TemplateResponseValues(
                LIST_DATA_SOURCES_TEMPLATE, data);
    }
    
    private List<DataSourceDescription> pollStatus(
            List<DataSourceDescription> dataSources) throws IOException {
        for (DataSourceDescription dataSource : dataSources) {
            if (dataSource.getConfiguration().getDeploymentURI() != null) {
                DataSourceDescription description = pollService(
                        dataSource.getConfiguration().getDeploymentURI());                
                dataSource.setStatus(description.getStatus());
            }
        }
        return dataSources;
    }
    
    private DataSourceDescription pollService(String serviceURL) 
            throws IOException {
        DataSourceDescriptionSerializer serializer = 
                new DataSourceDescriptionSerializer();        
        try {
            String result = httpUtils.getHttpResponse(serviceURL);
            // TODO add wrapper / convenience method that retains status code
            return serializer.unserialize(result);
        } catch (Exception e) {
            log.error(e, e);
            DataSourceDescription error = new DataSourceDescription();
            DataSourceStatus errorStatus = new DataSourceStatus();
            errorStatus.setStatusOk(false);
            errorStatus.setMessage("connector not responding");
            error.setStatus(errorStatus);
            return error;
        }
    }
    
}
