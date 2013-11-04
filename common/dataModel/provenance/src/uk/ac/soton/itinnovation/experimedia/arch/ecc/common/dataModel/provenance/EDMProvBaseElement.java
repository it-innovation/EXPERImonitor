/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2013
//
// Copyright in this software belongs to University of Southampton
// IT Innovation Centre of Gamma House, Enterprise Road, 
// Chilworth Science Park, Southampton, SO16 7NS, UK.
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
//      Created By :            Stefanie Wiegand
//      Created Date :          04-Oct-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.UUID;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvTriple.TRIPLE_TYPE;

/**
 * @author sw
 *
 * This is a class which implements the functionality shared by the three Provenance elements
 * (Agent, Activity and Entity). All the information for prov individuals is stored in this
 * class in triple form.
 * 
 */
public class EDMProvBaseElement {

    protected UUID instanceID;
    protected PROV_TYPE provType = PROV_TYPE.ePROV_UNKNOWN_TYPE;
    protected String iri;
    protected String prefix;
    protected String uniqueIdentifier;
	protected HashMap<UUID, EDMProvTriple> triples;
    
    protected static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z^^xsd:dateTime'");
	
    public enum PROV_TYPE { ePROV_UNKNOWN_TYPE, 
                            ePROV_ENTITY, 
                            ePROV_AGENT, 
                            ePROV_ACTIVITY };

    /**
     * Creates an EDMProvBaseElement
     * 
     * @param prefix the prefix of the element
     * @param a unique identifier. This could be something like domain_uniqueID, e.g. facebook_56735762153. It needs to be unique across clients.
     * @param label a human readable name
     */
    protected EDMProvBaseElement(String prefix, String uniqueIdentifier, String label) {
    	this.instanceID = UUID.randomUUID();
    	this.prefix = prefix;
    	this.uniqueIdentifier = uniqueIdentifier;
        this.iri = prefix + ":" + uniqueIdentifier;
        this.triples = new HashMap<UUID, EDMProvTriple>();
        
        EDMProvBaseElement.format.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        if (label!=null) {
        	EDMProvTriple triple = new EDMProvTriple(this.iri, "rdfs:label", label, TRIPLE_TYPE.ANNOTATION_PROPERTY);
        	triples.put(triple.getID(), triple);
        }
    }
    
    /**
     * Returns the label, if it exists. Otherwise falls back to IRI
     * 
     * @return the label or IRI
     */
    public String getFriendlyName() {
    	for (Entry<UUID, EDMProvTriple> e: this.getTriples(null, "rdfs:label").entrySet()) {
    		return e.getValue().getObject();
    	}
    	return this.iri;
    }
    
    public String toString() {
    	String contents = "[" + this.getProvType() + "] " + this.iri + "\n";
    	for(Entry<UUID, EDMProvTriple> e: this.triples.entrySet()) {
    		contents += "\t" + e.getValue().toString() + "\n";
    	}
    	return contents;
    }
    
    /**
     * Checks for the existence of a certain triple
     * 
     * @param triple The triple for which it should be checked
     * @return Whether that triple is already in the element or not
     */
    public boolean contains(EDMProvTriple triple) {
    	boolean contains = false;
    	for (Entry<UUID, EDMProvTriple> e: this.triples.entrySet()) {
    		if (e.getValue().equals(triple)) {
    			contains = true;
    			break;
    		}
    	}
    	return contains;
    }
    
    /**
     * Returns triples of a specific type and/or prefix.
     * null means it isn't a restriction, so getTriples(null, null);
     * returns the exact same result as getTriples();
     * 
     * @param type the type of triples that should be returned
     * @return the triples of the specified type
     */
    public HashMap<UUID, EDMProvTriple> getTriples(EDMProvTriple.TRIPLE_TYPE type, String prefix) {
    	HashMap<UUID, EDMProvTriple> result = new HashMap<UUID, EDMProvTriple>();

    	//check all triples
    	for (Entry<UUID, EDMProvTriple> e : triples.entrySet()) {
    		//check for type if applicable
    		if (type!=null && e.getValue().getType()!=type) {
            	 continue;
    		}
    		//check for prefix if applicable
    		if (prefix !=null && !e.getValue().getPredicatePrefix().equals(prefix)) {
    			continue;
    		}
        	result.put(e.getKey(), e.getValue());
    	}

    	return result;
    }
    
    /**
     * Return specific triples
     * 
     * @param pred The predicate of the triple
     * @return All the triples that match the given predicate
     */
    public HashMap<UUID, EDMProvTriple> getTriplesWithPredicate(String pred) {
      HashMap<UUID, EDMProvTriple> result = new HashMap<UUID, EDMProvTriple>();
      
      if (pred != null) {
        for (Entry<UUID, EDMProvTriple> e : triples.entrySet()) {
          if (e.getValue().hasPredicate(pred)) {
            result.put(e.getKey(), e.getValue());
          }
        }
      }
      
      return result;
    }

    /**
     * Adds a triple to the element with the element itself as subject
     * 
     * @param predicate the predicate of the new triple
     * @param object the object of the new triple
     */
    public void addTriple(String predicate, String object) {
    	this.addTriple(predicate, object, TRIPLE_TYPE.UNKNOWN_TYPE);
    }
    
    public void addTriple(String predicate, String object, TRIPLE_TYPE type) {
    	this.addTriple(this.iri, predicate, object, type);
    }
    
    protected void addTriple(String subject, String predicate, String object, TRIPLE_TYPE type) {
    	EDMProvTriple newTriple = new EDMProvTriple(subject, predicate, object, type);
    	if (!this.triples.containsValue(newTriple)) {
    		this.triples.put(newTriple.getID(), newTriple);
    	}
    }
    
    /**
     * Remove a specified triple from the list of triples.
     * The subject is always the element itself.
     * 
     * @param predicate the predicate of the triple to remove
     * @param object the object of the triple to remove
     */
    public void removeTriple(String predicate, String object) {
    	this.removeTriple(this.iri, predicate, object);
    }
    
    protected void removeTriple(String subject, String predicate, String object) {
    	EDMProvTriple triple = new EDMProvTriple(subject, predicate, object);
    	if (this.triples.containsValue(triple)) {
    		this.triples.remove(triple);
    	}
    }
    
    /**
     * Adds a class assertion making the element an individual of that class.
     * 
     * @param c the class to which the element should belong
     */
    public void addOwlClass(String c) {
    	this.addTriple(this.iri, "rdf:type", c, TRIPLE_TYPE.CLASS_ASSERTION);
    }
    
    /**
     * Removes the class assertion
     * 
     * @param c The class of which the assertion should be removed
     */
    public void removeOwlClass(String c) {
    	//TODO: what if this is called on an existing element (already in store)?
    	this.removeTriple(this.iri, "rdf:type", c);
    }
    
    //GETTERS/SETTERS//////////////////////////////////////////////////////////////////////////////

    /**
     * Get the element's IRI
     * 
     * @see org.semanticweb.owlapi.model.IRI
     * @return a String representation of the IRI
     */
	public String getIri() {
		return iri;
	}

	protected void setIri(String iri) {
		this.iri = iri;
	}
	
    public UUID getInstanceID() {
        return instanceID;
    }
    
    public PROV_TYPE getProvType() {
        return provType;
    }

    public HashMap<UUID, EDMProvTriple> getTriples() {
        return triples;
    }

    public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}

}
