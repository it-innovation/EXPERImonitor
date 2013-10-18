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
//      Created Date :          07-Oct-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance;

import java.util.zip.DataFormatException;

public class EDMEntity extends EDMProvBaseElement {

	public EDMEntity(String iri) {
		super(iri);
		this.addOwlClass("prov:Entity");
	}
	
	public EDMActivity startActivity(String activity) throws DataFormatException {
		EDMActivity newActivity = EDMProvFactory.getInstance().getActivity(activity);
		
		return newActivity;
	}
	
	public void endActivity(EDMActivity activity) {
		activity.addProperty(activity.iri, "prov:wasEndedBy", this.iri);
	}

	public void quoteFrom(EDMEntity entity) {
		this.addProperty(this.iri, "prov:wasQuotedFrom", entity.iri);
	}
	
	public void hadPrimarySource(EDMEntity entity) {
		this.addProperty(this.iri, "prov:hadPrimarySource", entity.iri);
	}

	public void wasRevisionOf(EDMEntity entity) {
		this.addProperty(this.iri, "prov:wasRevisionOf", entity.iri);
	}

}
