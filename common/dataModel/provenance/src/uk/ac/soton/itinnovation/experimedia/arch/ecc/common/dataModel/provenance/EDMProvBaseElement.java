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
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.UUID;

public class EDMProvBaseElement {

    private   UUID instanceID;
    protected String iri;
    protected LinkedList<EDMProvTriple> triples;
    
    protected static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z^^xsd:dateTime'");
	
    public EDMProvBaseElement() {
        instanceID = UUID.randomUUID();
        triples = new LinkedList<EDMProvTriple>();
    }
    
    protected EDMProvBaseElement(String iri) {
        this.instanceID = UUID.randomUUID();
        this.setIri(iri);
        this.triples = new LinkedList<EDMProvTriple>();
        
        EDMProvBaseElement.format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    public String toString() {
    	String contents = this.iri + "\n";
    	for(EDMProvTriple t: this.triples) {
    		contents += "\t" + t.getSubject() + " " + t.getPredicate() + " " + t.getObject() + "\n";
    	}
    	return contents;
    }
    
    public UUID getInstanceID() {
        return instanceID;
    }
    
    public boolean contains(EDMProvTriple triple) {
    	boolean contains = false;
    	for (EDMProvTriple t: this.triples) {
    		if (t.equals(triple)) {
    			contains = true;
    			break;
    		}
    	}
    	return contains;
    }
    
    public LinkedList<EDMProvTriple> getTriples() {
      return triples;
    }
    
    public void addProperty(String predicate, String object) {
    	this.addProperty(this.iri, predicate, object);
    }
    
    public void addProperty(String subject, String predicate, String object) {
    	EDMProvTriple newTriple = new EDMProvTriple(subject, predicate, object);
    	if (!this.triples.contains(newTriple)) {
    		this.triples.add(newTriple);
    	}
    }
    
    public void removeProperty(String predicate, String object) {
    	this.removeProperty(this.iri, predicate, object);
    }
    
    public void removeProperty(String subject, String predicate, String object) {
    	EDMProvTriple triple = new EDMProvTriple(subject, predicate, object);
    	if (this.triples.contains(triple)) {
    		this.triples.remove(triple);
    	}
    }
    
    public void addOwlClass(String c) {
    	this.addProperty(this.iri, "rdf:type", c);
    }
    
    public void removeOwlClass(String c) {
    	this.removeProperty(this.iri, "rdf:type", c);
    }

	public String getIri() {
		return iri;
	}

	public void setIri(String iri) {
		this.iri = iri;
	}

}
