package org.wheatinitiative.vivo.adminapp.form.generator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.FauxPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldOptions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.DefaultObjectPropertyFormGenerator;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.EditConfigurationGenerator;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

public class MergesOnObjectPropertyGenerator 
        extends DefaultObjectPropertyFormGenerator 
        implements EditConfigurationGenerator {

    private static final Log log = LogFactory.getLog(
            MergesOnObjectPropertyGenerator.class);
    protected RDFService rdfService;
    protected WebappDaoFactory unfilteredWadf;

    @Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq,
            HttpSession session) throws Exception {
        rdfService = vreq.getRDFService();
        // the filtered one doesn't implement getFauxPropertiesForBaseUri()
        unfilteredWadf = vreq.getUnfilteredWebappDaoFactory();
        return super.getEditConfiguration(vreq, session);
    }

    @Override
    protected void setFields(EditConfigurationVTwo editConfiguration, 
            VitroRequest vreq, String predicateUri, 
            List<VClass> rangeTypes) throws Exception {
        log.info("setting fields");
        FieldVTwo field = new FieldVTwo();
        field.setName("objectVar");             
        List<String> validators = new ArrayList<String>();
        validators.add("nonempty");
        field.setValidators(validators);                
        field.setOptions(new ObjectAndFauxPropertyFieldOptions());                
        Map<String, FieldVTwo> fields = new HashMap<String, FieldVTwo>();
        fields.put(field.getName(), field);                             
        editConfiguration.setFields(fields);
    }    

    protected class ObjectAndFauxPropertyFieldOptions implements FieldOptions {

        public Map<String, String> getOptions(
                EditConfigurationVTwo editConfig, 
                String fieldName, 
                WebappDaoFactory wDaoFact) throws Exception {
            Map<String, String> optionsMap = new HashMap<String, String>();
            List<ObjectProperty> objectProperties = 
                    unfilteredWadf.getObjectPropertyDao().getAllObjectProperties();
            for(ObjectProperty op : objectProperties) {
                optionsMap.put(op.getURI(), op.getDomainPublic());
            }
            List<FauxProperty> fauxProperties = getFauxProperties(
                    objectProperties, unfilteredWadf);
            VClassDao vcDao = unfilteredWadf.getVClassDao();
            for(FauxProperty fp : fauxProperties) {
                String label = fp.getDisplayName();
                String domainURI = fp.getDomainURI();
                if(domainURI != null) {
                    VClass domainClass = vcDao.getVClassByURI(domainURI);
                    if(domainClass != null) {
                        label += " (" + domainClass.getName() + ")";
                    }
                }
                optionsMap.put(fp.getContextUri(), label);
            }
            return optionsMap;
        }

        private List<FauxProperty> getFauxProperties(
                List<ObjectProperty> objectProperties, 
                WebappDaoFactory wDaoFact) {
            List<FauxProperty> fauxProperties = new ArrayList<FauxProperty>();
            FauxPropertyDao fpDao = wDaoFact.getFauxPropertyDao();
            for(ObjectProperty op : objectProperties) {
                String propertyURI = op.getURI();
                if(propertyURI != null) { // shouldn't be, of course
                    fauxProperties.addAll(
                            fpDao.getFauxPropertiesForBaseUri(op.getURI()));
                }
            }            
            return fauxProperties;            
        }
        
        @Override
        public Comparator<String[]> getCustomComparator() {
            // no custom comparator; returning null per contract
            return null;
        }

    }


    
}
