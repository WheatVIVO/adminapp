package org.wheatinitiative.vivo.mockup.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wheatinitiative.vivo.datasource.DataSourceDescription;
import org.wheatinitiative.vivo.datasource.DataSourceStatus;
import org.wheatinitiative.vivo.datasource.SparqlEndpointParams;
import org.wheatinitiative.vivo.datasource.service.DataSourceDescriptionSerializer;
import org.wheatinitiative.vivo.datasource.util.http.HttpUtils;
import org.wheatinitiative.vivo.mockup.datasource.DataSourceManager;
import org.wheatinitiative.vivo.mockup.datasource.impl.DataSourceManagerImpl;

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
        DataSourceManager dsm = new DataSourceManagerImpl(vreq.getRDFService());
        List<DataSourceDescription> dataSources = dsm.listDataSources();
        dataSources = pollStatus(dataSources);
        data.put("dataSources", dataSources);
        return new TemplateResponseValues(CONFIGURE_DATA_SOURCES_TEMPLATE, data);
    }
    
    private List<DataSourceDescription> pollStatus(List<DataSourceDescription> dataSources) 
            throws IOException {
        for (DataSourceDescription dataSource : dataSources) {
            if (dataSource.getConfiguration().getDeploymentURI() != null) {
                DataSourceDescription description = pollService(
                        dataSource.getConfiguration().getDeploymentURI());
                dataSource.getStatus().setRunning(
                        description.getStatus().isRunning());
            }
        }
        return dataSources;
    }
    
    private TemplateResponseValues doEditDataSource( 
            Map<String, Object> data, VitroRequest vreq) throws IOException {
        DataSourceManager dsm = new DataSourceManagerImpl(vreq.getRDFService());
        String dataSourceURI = vreq.getParameter("uri");
        DataSourceDescription dataSource = dsm.getDataSource(dataSourceURI);
        String deploymentURL = dataSource.getConfiguration().getDeploymentURI();
        if(deploymentURL != null) {
            if(vreq.getParameter("start") != null) {
                log.info("Starting data source");
                this.startService(dataSource);
                // "forward" to list of data sources
            } else if (vreq.getParameter("stop") != null) {
                log.info("Stopping data source");
                this.stopService(dataSource);
            }
        }
        return doConfigureDataSources(data, vreq);
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
        try {
            DataSourceDescription desc = serializer.unserialize(result);
            return desc;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Exception parsing response from " + serviceURL + ": \n" + result);
        }
    }
    
}
