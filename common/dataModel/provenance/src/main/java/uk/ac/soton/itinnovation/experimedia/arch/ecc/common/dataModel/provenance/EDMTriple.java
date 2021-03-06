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

/**
 * A class to handle RDF triples which use the long form, e.g.
 * http://www.w3.org/1999/02/22-rdf-syntax-ns#type instead of rdf:type
 */
public class EDMTriple {

	public enum TRIPLE_TYPE {
		CLASS_ASSERTION,
		OBJECT_PROPERTY,
		DATA_PROPERTY,
		ANNOTATION_PROPERTY,
		UNKNOWN_TYPE
	};

	protected static final String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

	private UUID   tripleID;
	private String subject;
	private String predicate;
	private String object;
	private String predicatePrefix;
	private TRIPLE_TYPE type;

	/**
	 * Create a triple of unknown type
	 *
	 * @param subject
	 * @param predicate
	 * @param object
	 */
	public EDMTriple(String subject, String predicate, String object) {
		this(subject, predicate, object, TRIPLE_TYPE.UNKNOWN_TYPE);
	}

	/**
	 * Create a triple of a specific type.
	 *
	 * @param subject
	 * @param predicate
	 * @param object
	 * @param type
	 */
	public EDMTriple(String subject, String predicate, String object, TRIPLE_TYPE type) {
		this.tripleID = UUID.randomUUID();
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.type = type;

		//get predicatePrefix from predicate
		this.predicatePrefix = splitURI(predicate, 0);

		//attach predicate from type
		if (this.type==TRIPLE_TYPE.CLASS_ASSERTION &&
			(this.predicate.equals("") || this.predicate==null)) {
			this.predicate = rdfType;
		//attach type from predicate
		} else if (this.type==TRIPLE_TYPE.UNKNOWN_TYPE && this.predicate.equals(rdfType)) {
			this.type = TRIPLE_TYPE.CLASS_ASSERTION;
		}

	}

	/**
	 * Splits a URI into the prefex and local name. This works for both, short and long prefixes.
	 *
	 *
	 * @param URI the URI to be split
	 * @param part which part of the URI to return: 0 = prefix, 1 = local name
	 * @return
	 */
	public static String splitURI(String URI, int part) {

		if (URI==null || URI.isEmpty()) {
			return null;
		}

		String result = null;
		int splitIndex = -1;

		if (URI.indexOf("#")>0) {
			splitIndex = URI.indexOf("#");
		} else if (URI.lastIndexOf("/")>0) {
			splitIndex = URI.lastIndexOf("/");
		} else if (URI.lastIndexOf(":")>0) {
			splitIndex = URI.lastIndexOf(":");
		}

		if (splitIndex>=0) {
			//get prefix
			if (part==0) {
				result = URI.substring(0, splitIndex+1).trim();
			//get local name
			} else {
				result = URI.substring(splitIndex+1).trim();
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return "[" + getType() + "] " + getSubject() + " " + getPredicate() + " " + getObject();
	}

	/**
	 * Compare two triples. They are equal if they have the same subject, predicate and object.
	 *
	 * @param t The triple to compare to this one
	 * @return whether the triples are equal
	 */
	public boolean equals(EDMTriple t) {
		//currently ignoring special case: full prefix is case insensitive but individual name is case sensitive.
		if (this.subject.equals(t.getSubject())
			&& this.predicate.equals(t.getPredicate())
			&& this.object.equals(t.getObject())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Check whether the triple has the given predicate.
	 *
	 * @param pred
	 * @return
	 */
	public boolean hasPredicate(String pred) {
		if (pred == null || predicate == null) {
			return false;
		} else if (predicate.equals(pred)) {
			return true;
		}

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
