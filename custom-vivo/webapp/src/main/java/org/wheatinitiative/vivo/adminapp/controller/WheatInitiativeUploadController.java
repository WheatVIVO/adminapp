package org.wheatinitiative.vivo.adminapp.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wheatinitiative.vivo.adminapp.datasource.DataSourceScheduler;
import org.wheatinitiative.vivo.datasource.connector.wheatinitiative.WheatInitiative;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

public class WheatInitiativeUploadController extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String TEMPLATE = "wheatInitiativeUpload.ftl";
    private static final Log log = LogFactory.getLog(WheatInitiativeUploadController.class);

    @Override
    protected AuthorizationRequest requiredActions(VitroRequest vreq) {
        return SimplePermission.USE_MISCELLANEOUS_ADMIN_PAGES.ACTION;
    }
 
    @Override 
    public ResponseValues processRequest(VitroRequest vreq) {
        String message = "";
        boolean success = false;
        if(vreq.getParameter("submit") != null) {
            if(vreq.getFiles().isEmpty()) {
                message += "<strong>Please upload an Excel file</strong>";
            } else {
                try {
                    FileItem fi = getFileItem(vreq);
                    // not clear why this is occurring instead of empty file set
                    if(StringUtils.isEmpty(fi.getName())) {
                        message += "<strong>Please upload an Excel file</strong>";
                    } else {
                        InputStream is = fi.getInputStream();                    
                        FileUtils.copyInputStreamToFile(is, new File(
                                ApplicationUtils.instance().getHomeDirectory()
                                .getPath().toAbsolutePath().toString()
                                + DataSourceScheduler.DATA_DIR + WheatInitiative.EXCEL_SUBDIR + "/" + fi.getName()));
                        message += "<p>File " + fi.getName() + " uploaded successfully</p>";
                        success = true;
                    }
                } catch (IOException ioe) {
                    message += "<strong>Unable to upload file to server.  "
                            + "Please check that the " 
                            + DataSourceScheduler.DATA_DIR
                            + " subdirectory of the VIVO home directory is"
                            + " writable by Tomcat.  See log for details.";
                    log.error(ioe, ioe);
                }
            }
        }
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("message", message);
        body.put("success", success);
        return new TemplateResponseValues(TEMPLATE, body);       
    }
    
    protected FileItem getFileItem(VitroRequest vreq) {
        FileItem f = null;
        for (String filename : vreq.getFiles().keySet()) {                        
            List<FileItem> fileitems = vreq.getFiles().get(filename);
            for(FileItem fileitem : fileitems) {
                f = fileitem;
            }
        }
        return f;
    }
    
}
