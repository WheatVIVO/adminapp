/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * Use two OntModelCaches as one.
 * 
 * If both caches contain models with the same name, a warning will be written
 * to the log, and the model from the primary cache will be used.
 * 
 * Any new models will be created on the primary cache.
 */
public class JoinedOntModelCache implements OntModelCache {
	private static final Log log = LogFactory.getLog(JoinedOntModelCache.class);

	private final OntModelCache primary;
	private final OntModelCache secondary;

	public JoinedOntModelCache(OntModelCache primary, OntModelCache secondary) {
		this.primary = primary;
		this.secondary = secondary;

// extremely expensive with TDB : see below
//		Set<String> duplicateNames = new HashSet<String>(primary.getModelNames());
//		duplicateNames.retainAll(secondary.getModelNames());
//		if (!duplicateNames.isEmpty()) {
//			log.warn("These model names appear in both caches: "
//					+ duplicateNames);
//		}
	}

    protected boolean isKnownSecondaryModel(String name) {
        return (ModelNames.USER_ACCOUNTS.equals(name)
                || ModelNames.DISPLAY.equals(name) 
                || ModelNames.DISPLAY_DISPLAY.equals(name)
                || ModelNames.DISPLAY_TBOX.equals(name)
                );
    }
	
    /*
	 * Local mod: Calling getModelNames() under TDB is very expensive
	 */
	@Override
	public OntModel getOntModel(String name) {
	    if(isKnownSecondaryModel(name)) {
	        return secondary.getOntModel(name);
	    } else {
    	    OntModel sec = secondary.getOntModel(name);
    	    if(sec.isEmpty()) {
    	        return primary.getOntModel(name);
    	    } else {
    	        OntModel pri = primary.getOntModel(name);
    	        if(!pri.isEmpty()) {
    	            return pri;
    	        } else {
    	            return sec;
    	        }
    	    }
	    }
	    //original logic
		//if (primary.getModelNames().contains(name)) {
		//	return primary.getOntModel(name);
		//}
		//if (secondary.getModelNames().contains(name)) {
		//	return secondary.getOntModel(name);
		//}
		//return primary.getOntModel(name);
	}

	@Override
	public SortedSet<String> getModelNames() {
		SortedSet<String> allNames = new TreeSet<String>(primary.getModelNames());
		allNames.addAll(secondary.getModelNames());
		return allNames;
	}

	@Override
	public String toString() {
		return "JoinedOntModelCache[" + ToString.hashHex(this) + ", primary="
				+ primary + ", secondary=" + secondary + "]";
	}

}
