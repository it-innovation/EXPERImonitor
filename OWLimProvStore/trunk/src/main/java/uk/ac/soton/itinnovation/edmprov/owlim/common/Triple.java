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
//      Created By :            Vegard Engen
//      Created Date :          2013-08-29
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.edmprov.owlim.common;

import java.io.Serializable;

/**
 * A basic data structure for a triple, encapsulating strings for: subject,
 * predicate and object. Also, optionally, a relationship type and context 
 * can be used.
 * @author Vegard Engen
 */
public class Triple implements Serializable
{
	private String subject;
	private String predicate;
	private String object;
	private RelationshipType relationshipType;
	private String context;

	public Triple()
	{
		relationshipType = RelationshipType.UNKNOWN;
	}
	
	public Triple(String s, String p, String o)
	{
		this();
		this.subject = s;
		this.predicate = p;
		this.object = o;
	}
	
	public Triple(String s, String p, String o, RelationshipType r)
	{
		this.subject = s;
		this.predicate = p;
		this.object = o;
		this.relationshipType = r;
	}
	
	public Triple(String s, String p, String o, RelationshipType r, String c)
	{
		this.subject = s;
		this.predicate = p;
		this.object = o;
		this.relationshipType = r;
		this.context = c;
	}
	
	public Triple(Triple t)
	{
		if (t == null) {
			return;
		}
		
		this.subject = t.getSubject();
		this.predicate = t.getPredicate();
		this.object = t.getObject();
		this.relationshipType = t.getRelationshipType();
		this.context = t.getContext();
	}
	
	/**
	 * @return the subject
	 */
	public String getSubject()
	{
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject)
	{
		this.subject = subject;
	}

	/**
	 * @return the predicate
	 */
	public String getPredicate()
	{
		return predicate;
	}

	/**
	 * @param predicate the predicate to set
	 */
	public void setPredicate(String predicate)
	{
		this.predicate = predicate;
	}

	/**
	 * @return the object
	 */
	public String getObject()
	{
		return object;
	}

	/**
	 * @param object the object to set
	 */
	public void setObject(String object)
	{
		this.object = object;
	}

	/**
	 * @return the relationshipType
	 */
	public RelationshipType getRelationshipType()
	{
		return relationshipType;
	}

	/**
	 * @param relationshipType the relationshipType to set
	 */
	public void setRelationshipType(RelationshipType relationshipType)
	{
		this.relationshipType = relationshipType;
	}

	/**
	 * @return the context
	 */
	public String getContext()
	{
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(String context)
	{
		this.context = context;
	}
	
	@Override
	public String toString()
	{
		String str = "";
		
		str += "s = " + ((subject != null) ? subject : "NULL");
		str += ", o = " + ((object != null) ? object : "NULL");
		str += ", p = " + ((predicate != null) ? predicate : "NULL");
		str += ", r = " + ((relationshipType != null) ? relationshipType : "NULL");
		str += ", c = " + ((context != null) ? context : "NULL");
		
		return str;
	}
}
