package org.wheatinitiative.vivo.mockup.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wheatinitiative.vivo.datasource.DataSourceConfiguration;
import org.wheatinitiative.vivo.datasource.DataSourceDescription;
import org.wheatinitiative.vivo.datasource.DataSourceStatus;
import org.wheatinitiative.vivo.datasource.SparqlEndpointParams;
import org.wheatinitiative.vivo.datasource.service.DataSourceDescriptionSerializer;
import org.wheatinitiative.vivo.datasource.util.http.HttpUtils;
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
    private static HttpUtils httpUtils = new HttpUtils();
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) 
            throws IOException {
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
            Map<String, Object> data, VitroRequest vreq) throws IOException {
        DataSourceManager dsm = DataSourceManagerMockup.getInstance();
        List<DataSource> dataSources = dsm.listDataSources();
        dataSources = pollStatus(dataSources);
        data.put("dataSources", dataSources);
        return new TemplateResponseValues(CONFIGURE_DATA_SOURCES_TEMPLATE, data);
    }
    
    private List<DataSource> pollStatus(List<DataSource> dataSources) 
            throws IOException {
        for (DataSource dataSource : dataSources) {
            if (dataSource.getDeploymentURL() != null) {
                DataSourceDescription description = pollService(
                        dataSource.getDeploymentURL());
                dataSource.getStatus().setRunning(
                        description.getStatus().isRunning());
            }
        }
        return dataSources;
    }
    
    private TemplateResponseValues doEditDataSource( 
            Map<String, Object> data, VitroRequest vreq) throws IOException {
        DataSourceManager dsm = DataSourceManagerMockup.getInstance();
        String dataSourceURI = vreq.getParameter("uri");
        DataSource dataSource = dsm.getDataSource(dataSourceURI);
        String deploymentURL = dataSource.getDeploymentURL();
        if(deploymentURL == null) {
            deploymentURL = vreq.getParameter("deploymentURL");
        }
        if(deploymentURL != null) {
            data.put("deploymentURL", deploymentURL);
            DataSourceDescription description = pollService(deploymentURL);
            addParameters(description, vreq, data);
            // pipe the running status from the real status to the mockup
            dataSource.getStatus().setRunning(description.getStatus().isRunning());
            if(deploymentURL != null && vreq.getParameter("start") != null) {
                log.info("Starting data source");
                updateDataSource(dataSource, vreq);
                List<String> queryTerms = new ArrayList<String>();
                queryTerms.add("wheat");
                description.getConfiguration().setQueryTerms(queryTerms);
                this.startService(dataSource.getDeploymentURL(), description);
                // "forward" to list of data sources
                return doConfigureDataSources(data, vreq);
            } else if (vreq.getParameter("stop") != null) {
                log.info("Stopping data source");
                updateDataSource(dataSource, vreq);
                this.stopService(dataSource.getDeploymentURL(), description);
                return doConfigureDataSources(data, vreq);
            }
        }    
        data.put("dataSource", dataSource);
        String templateName = getTemplateName(dataSource);
        return new TemplateResponseValues(templateName, data);
    }
    
    private void addParameters(DataSourceDescription description, 
            VitroRequest vreq, Map<String, Object> data) {
        SparqlEndpointParams params = description.getConfiguration()
                .getEndpointParameters();
        String endpointURI = vreq.getParameter("sparqlEndpointURL");
        String endpointUsername = vreq.getParameter("endpointUsername");
        String endpointPassword = vreq.getParameter("endpointPassword");
        String resultsGraphURI = vreq.getParameter("resultsGraphURI");
        params.setEndpointURI(endpointURI);
        params.setEndpointUpdateURI(endpointURI);
        params.setUsername(endpointUsername);
        params.setPassword(endpointPassword);
        description.getConfiguration().setResultsGraphURI(resultsGraphURI);
        data.put("sparqlEndpointURL", endpointURI);
        data.put("endpointUsername", endpointUsername);
        data.put("endpointPassword", endpointPassword);
        data.put("resultsGraphURI", resultsGraphURI);
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

    private DataSourceDescription startService(String serviceURL, 
            DataSourceDescription description) {
        description.getStatus().setRunning(true);
        return updateService(serviceURL, description);
    }
    
    private DataSourceDescription stopService(String serviceURL, 
            DataSourceDescription description) {
        description.getStatus().setRunning(false);
        return updateService(serviceURL, description);         
    }
    
    private DataSourceDescription pollService(String serviceURL) 
            throws IOException {
        DataSourceDescriptionSerializer serializer = 
                new DataSourceDescriptionSerializer();
        String result = httpUtils.getHttpResponse(serviceURL);
        // TODO add wrapper / convenience method that retains status code
        try {
            return serializer.unserialize(result);
        } catch (Exception e) {
            log.error(e, e);
            DataSourceDescription error = new DataSourceDescription();
            DataSourceStatus errorStatus = new DataSourceStatus();
            errorStatus.setStatusOk(false);
            error.setStatus(errorStatus);
            return error;
        }
    }
    
    private DataSourceDescription updateService(
            String serviceURL, DataSourceDescription description) {
        DataSourceDescriptionSerializer serializer = 
                new DataSourceDescriptionSerializer();
        String json = serializer.serialize(description);
        String result = httpUtils.getHttpPostResponse(
                serviceURL, json, "application/json");
        return serializer.unserialize(result);
    }
    
}
