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
//      Created Date :          16-Oct-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance;

import java.util.UUID;

public class EDMProvTriple {
	
	public enum TRIPLE_TYPE {
		UNKNOWN_TYPE, 
		CLASS_ASSERTION, 
		OBJECT_PROPERTY,
		DATA_PROPERTY,
		ANNOTATION_PROPERTY
	};
	
	private UUID   tripleID;
	private String subject;
	private String predicate;
	private String object;
	private String predicatePrefix;
	private TRIPLE_TYPE type;
	
	public EDMProvTriple(String subject, String predicate, String object) {
		this(subject, predicate, object, TRIPLE_TYPE.UNKNOWN_TYPE);
	}
	
	public EDMProvTriple(String subject, String predicate, String object, TRIPLE_TYPE type) {
		this.tripleID = UUID.randomUUID();
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.type = type;
		//get predicatePrefix from predicate
		if (this.predicate.indexOf(":")>0) {
			this.predicatePrefix = predicate.substring(0, predicate.indexOf(":")).trim();
		} else {
			this.predicatePrefix = null;
		}
		//attach predicate from type
		if (this.type==TRIPLE_TYPE.CLASS_ASSERTION &&
			(this.predicate.equals("") || this.predicate==null)) {
			this.predicate = "rdf:type";
		//attach type from predicate
		} else if (this.type==TRIPLE_TYPE.UNKNOWN_TYPE && this.predicate.equals("rdf:type")) {
			this.type = TRIPLE_TYPE.CLASS_ASSERTION;
		}
		
	}
	
	public String toString() {
		return "[" + getType() + "] " + getSubject() + " " + getPredicate() + " " + getObject();
	}
	
	public boolean equals(EDMProvTriple t) {
		//TODO: special cases: full prefix is case insensitive but individual name is case sensitive.
		// not sure about predicate.
		if (this.subject.equals(t.getSubject())
			&& this.predicate.equals(t.getPredicate())
			&& this.object.equals(t.getObject())) {
			return true;
		} else {
			return false;
		}
	}
  
	public boolean hasPredicate(String pred) {
		if (pred == null || predicate == null) 
			return false;
    
		if (predicate.equals(pred))
			return true;
    
		return false;
	}

	//GETTERS/SETTERS//////////////////////////////////////////////////////////////////////////////
	
	public UUID getID() {
		return tripleID;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getPredicate() {
		return predicate;
	}
	
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}
	
	public String getObject() {
		return object;
	}
	
	public void setObject(String object) {
		this.object = object;
	}

	public TRIPLE_TYPE getType() {
		return type;
	}

	public void setType(TRIPLE_TYPE type) {
		this.type = type;
	}

	public String getPredicatePrefix() {
		return predicatePrefix;
	}

	public void setPredicatePrefix(String prefix) {
		this.predicatePrefix = prefix;
	}

}
