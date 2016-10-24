package org.wheatinitiative.vivo.mockup.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wheatinitiative.vivo.mockup.datasource.DataSource;
import org.wheatinitiative.vivo.mockup.datasource.DataSourceManager;
import org.wheatinitiative.vivo.mockup.datasource.impl.DataSourceManagerMockup;

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

    private static final String CONFIGURE_DATA_SOURCES_TEMPLATE = 
            "configureDataSources.ftl";
    private static final String EDIT_DATA_SOURCE_TEMPLATE = 
            "editDataSource.ftl";
    private static final Log log = LogFactory.getLog(
            AdminFunctionalityController.class);
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        String featureName = vreq.getParameter("feature");
        if(featureName == null) {
            throw new RuntimeException("Parameter 'feature' must be specified");
        }
        log.debug("requestedFeature='" + featureName + "'");
        Map<String, Object> data = new HashMap<String, Object>();
        if("configureDataSources".equals(featureName)) {
            return doConfigureDataSources(data, vreq);
        } else if ("editDataSource".equals(featureName)) {
            return doEditDataSource(data, vreq);
        } else {
            throw new RuntimeException("Unrecognized feature name " 
                    + featureName);
        }
    }    
    
    private TemplateResponseValues doConfigureDataSources(
            Map<String, Object> data, VitroRequest vreq) {
        DataSourceManager dsm = DataSourceManagerMockup.getInstance();
        List<DataSource> dataSources = dsm.listDataSources();
        data.put("dataSources", dataSources);
        return new TemplateResponseValues(CONFIGURE_DATA_SOURCES_TEMPLATE, data);
    }
    
    private TemplateResponseValues doEditDataSource( 
            Map<String, Object> data, VitroRequest vreq) {
        DataSourceManager dsm = DataSourceManagerMockup.getInstance();
        String dataSourceURI = vreq.getParameter("uri");
        DataSource dataSource = dsm.getDataSource(dataSourceURI);
        if(vreq.getParameter("submit") != null) {
            log.info("Updating data source");
            updateDataSource(dataSource, vreq);
            // "forward" to list of data sources
            return doConfigureDataSources(data, vreq);
        } else if (vreq.getParameter("cancel") != null) {
            return doConfigureDataSources(data, vreq);
        }
        data.put("dataSource", dataSource);
        String templateName = getTemplateName(dataSource);
        return new TemplateResponseValues(templateName, data);
    }
    
    private void updateDataSource(DataSource dataSource, VitroRequest vreq) {
        String name = vreq.getParameter("name");
        if(name != null) {
            dataSource.setName(name);
        }
        String priorityStr = vreq.getParameter("priority");
        int priority = -1;
        try {
            priority = Integer.parseInt(priorityStr);
        } catch (NumberFormatException e) {
            log.error(e, e);
        }
        if(priority > -1) {
            dataSource.setPriority(priority);
        }
    }
    
    // for returning a template based on the subclass of DataSource
    private String getTemplateName(DataSource dataSource) {
        return EDIT_DATA_SOURCE_TEMPLATE; // for now we have only one
    }
    
}
