/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.controller.ajax;

import java.io.IOException;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.VitroAjaxController;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.jena.QueryUtils;

public class GeoFocusMapLocations extends AbstractAjaxResponder {

    private static final Log log = LogFactory.getLog(GeoFocusMapLocations.class.getName());
    private List<Map<String,String>>  geoLocations;
    private static String GEO_FOCUS_QUERY = ""
        + "PREFIX geo: <http://aims.fao.org/aos/geopolitical.owl#> \n"
        + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  \n"
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" 
        + "PREFIX core: <http://vivoweb.org/ontology/core#>  \n"
        + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>  \n"
        + "PREFIX vivoc: <http://vivo.library.cornell.edu/ns/0.1#>  \n"
        + "PREFIX afn:  <http://jena.hpl.hp.com/ARQ/function#> "
	+ "PREFIX obo:      <http://purl.obolibrary.org/obo/> \n" 
        + "SELECT DISTINCT ?label ?location (afn:localname(?location) AS ?localName) (COUNT(DISTINCT ?person) AS ?count)  \n"
        + "WHERE { { \n"
        + "    ?location rdf:type core:GeographicRegion .  \n"
        + "    ?location rdfs:label ?label .   \n"
        + "    ?location obo:RO_0001015 ?person .  \n"
        + "    FILTER (NOT EXISTS {?location a core:StateOrProvince}) \n"
        + "} UNION {   \n"
        + "    ?location rdf:type core:GeographicRegion .  \n"
        + "    ?location <http://purl.obolibrary.org/obo/BFO_0000051> ?sublocation  . \n"
        + "    ?location rdfs:label ?label .  \n"
        + "    ?sublocation obo:RO_0001015 ?person .  \n"
        + "} UNION {   \n"
        + "    ?location rdf:type core:GeographicRegion .  \n"
        + "    ?location geo:hasMember ?sublocation  . \n"
        + "    ?location rdfs:label ?label .  \n"
        + "    ?sublocation obo:RO_0001015 ?person .  \n"
        + "} }  \n"
        + "GROUP BY ?label ?location  \n";
    
	public GeoFocusMapLocations(HttpServlet parent, VitroRequest vreq,
			HttpServletResponse resp) {
		super(parent, vreq, resp);
    }

	@Override
	public String prepareResponse() throws IOException, JSONException {
		try {
            geoLocations = getGeoLocations(vreq);
            
            String response = "[";
            String geometry = "{\"geometry\": {\"type\": \"Point\",\"coordinates\": \"\"},";
            String typeProps = "\"type\": \"Feature\",\"properties\": {\"mapType\": \"\",";
            String previousLabel = "";
            
            for (Map<String, String> map: geoLocations) {
                String label = map.get("label");
                String html  = map.get("count");
                String uri = map.get("location");
                String local = map.get("localName");
                if ( uri != null ) {
                    uri = UrlBuilder.urlEncode(uri);
                }
                Integer count    = Integer.parseInt(map.get("count"));
                String radius   = String.valueOf(calculateRadius(count));
                String name = "";
                
                if ( label != null && !label.equals(previousLabel) ) {
                    if ( label.contains("Ivoire") ) {
                        name = "Ivory Coast";
                    }
                    else if ( label.contains("United States of America") ) {
                        name = "United States of America";
                    }
                    else if ( label.contains("United Kingdom") ) {
                        name = "United Kingdom";
                    }
                    else {
                        name = label;
                    }
                    String tempStr = geometry; //+label
                    tempStr += typeProps //+ label 
                                        + "\"popupContent\": \""
                                        + name 
                                        + "\",\"html\":"
                                        + html 
                                        + ",\"radius\":"
                                        + radius
                                        + ",\"uri\": \""
                                        + uri
                                        + "\",\"local\": \""
                                        + local
                                        + "\"}},";                 
                    response +=  tempStr;
                    previousLabel = label;
                }
            }
			if ( response.lastIndexOf(",") > 0 ) {
			    response = response.substring(0, response.lastIndexOf(","));
			}
			response += " ]";
			if ( log.isDebugEnabled() ) {
				log.debug(response);
			}
			return response;
		} catch (Exception e) {
			log.error("Failed geographic focus locations", e);
			return EMPTY_RESPONSE;
		}
	}
           
    private List<Map<String,String>>  getGeoLocations(VitroRequest vreq) {
          
        String queryStr = GEO_FOCUS_QUERY;
        log.debug("queryStr = " + queryStr);
        List<Map<String,String>>  locations = new ArrayList<Map<String,String>>();
        try {
            ResultSet results = QueryUtils.getQueryResults(queryStr, vreq);
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                locations.add(QueryUtils.querySolutionToStringValueMap(soln));
            }
        } catch (Exception e) {
            log.error(e, e);
        }    
       
        return locations;
    }
    private Integer calculateRadius(Integer count) {
          
        int radius = 8;
        if ( count != null ) {
            if ( count < 4 ) {
                radius = 8;
            }
            else if ( count < 7 ) {
                radius = 10;
            }
            else if ( count < 10 ) {
                radius = 12;
            }
            else if ( count < 16 ) {
                radius = 14;
            }
            else if ( count < 21 ) {
                radius = 16;
            }
            else if ( count < 26 ) {
                radius = 18;
            }
            else {
                radius = 20;
            }
        }

        return radius;
    }
}
