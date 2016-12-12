package org.wheatinitiative.vivo.mockup.controller;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.wheatinitiative.vivo.datasource.DataSourceConfiguration;
import org.wheatinitiative.vivo.datasource.DataSourceDescription;
import org.wheatinitiative.vivo.datasource.DataSourceStatus;
import org.wheatinitiative.vivo.datasource.service.DataSourceDescriptionSerializer;
import org.wheatinitiative.vivo.datasource.util.http.HttpUtils;

public class ServiceInvoker extends HttpServlet {

    private HttpUtils httpUtils = new HttpUtils();
    
    public void doGet(HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        String service = request.getParameter("service");
        String serviceURL = request.getRequestURI();
        if("rcuk".equals(service)) {
            serviceURL = "http://localhost:8080/wheatvivo-adminapp/dataSource/rcuk";
        }    
        DataSourceConfiguration configuration = 
                new DataSourceConfiguration();
        List<String> queryTerms = new ArrayList<String>();
        queryTerms.add("wheat");
        configuration.getParameterMap().put("queryTerms", queryTerms);
        DataSourceStatus status = new DataSourceStatus();
        status.setRunning(true); // invoke the connector
        DataSourceDescription description = new DataSourceDescription(
                configuration, status);
        DataSourceDescriptionSerializer serializer = 
                new DataSourceDescriptionSerializer();
        String json = serializer.serialize(description);
        String result = httpUtils.getHttpPostResponse(
                serviceURL, json, "application/json");
        Writer out = response.getWriter();
        try {
            out.write(result);
        } finally {
            out.flush();
            out.close();
        }
    }
    
}
