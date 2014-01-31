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

import javax.xml.datatype.DatatypeConfigurationException;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMTriple.TRIPLE_TYPE;

public class EDMEntity extends EDMProvBaseElement {

	public EDMEntity(String prefix, String uniqueIdentifier, String label) {
		super(prefix, uniqueIdentifier, label);
    
		this.setProvType(PROV_TYPE.ePROV_ENTITY);
		this.addOwlClass(EDMProvBaseElement.prov + "Entity");
	}
	
	public EDMActivity startActivity(String uniqueIdentifier, String label) throws DataFormatException, DatatypeConfigurationException {
		EDMActivity newActivity = (EDMActivity) EDMProvFactory.getInstance().getActivity(uniqueIdentifier);

		EDMProvFactory.getInstance().elementUpdated(this); // Queue to re-send in next report
		
		return newActivity;
	}
	
	public void endActivity(EDMActivity activity) {
		activity.addTriple(EDMProvBaseElement.prov + "wasEndedBy", this.getIri(), TRIPLE_TYPE.OBJECT_PROPERTY);
	}

	public void quoteFrom(EDMEntity entity) {
		this.addTriple(EDMProvBaseElement.prov + "wasQuotedFrom", entity.getIri(), TRIPLE_TYPE.OBJECT_PROPERTY);
	}
	
	public void hadPrimarySource(EDMEntity entity) {
		this.addTriple(EDMProvBaseElement.prov + "hadPrimarySource", entity.getIri(), TRIPLE_TYPE.OBJECT_PROPERTY);
	}

	public void wasRevisionOf(EDMEntity entity) {
		this.addTriple(EDMProvBaseElement.prov + "wasRevisionOf", entity.getIri(), TRIPLE_TYPE.OBJECT_PROPERTY);
	}

}
