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
	
  private UUID   tripleID;
	private String subject;
	private String predicate;
	private String object;
	
	public EDMProvTriple(String subject, String predicate, String object) {
    this.tripleID = UUID.randomUUID();
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}
	
	public boolean equals(EDMProvTriple t) {
		if (this.subject.equals(t.getSubject())
			&& this.predicate.equals(t.getPredicate())
			&& this.object.equals(t.getObject())) {
			return true;
		} else {
			return false;
		}
	}
  
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

}
