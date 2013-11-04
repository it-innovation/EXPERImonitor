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

import java.util.Date;
import java.util.zip.DataFormatException;

import javax.xml.datatype.DatatypeConfigurationException;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvTriple.TRIPLE_TYPE;

public class EDMAgent extends EDMProvBaseElement {

	
	public EDMAgent(String prefix, String uniqueIdentifier, String label) {
		super(prefix, uniqueIdentifier, label);
    
		this.provType = PROV_TYPE.ePROV_AGENT;
		this.addOwlClass("prov:Agent");
	}
	
	// PROV FUNCTIONAL CLASSES HERE: //////////////////////////////////////////////////////////////
	
	public EDMActivity startActivity(String uniqueIdentifier, String label, String timestamp) throws DataFormatException, DatatypeConfigurationException {
		EDMProvFactory factory = EDMProvFactory.getInstance();
	    
		EDMActivity newActivity = (EDMActivity) factory.createElement(uniqueIdentifier, label, PROV_TYPE.ePROV_ACTIVITY);
		newActivity.addTriple(newActivity.iri, "prov:startedAtTime", format.format(new Date(Long.valueOf(timestamp)*1000)), TRIPLE_TYPE.DATA_PROPERTY);
		newActivity.addTriple(newActivity.iri, "prov:wasStartedBy", this.iri, TRIPLE_TYPE.OBJECT_PROPERTY);
    
		factory.elementUpdated(this); // Queue to re-send in next report
    
		return newActivity;
	}
	
	public EDMActivity startActivity(String uniqueIdentifier, String label) throws DataFormatException, DatatypeConfigurationException {
		return startActivity(uniqueIdentifier, label, String.valueOf(System.currentTimeMillis() / 1000L));
	}
	
	public void stopActivity(EDMActivity activity, String timestamp) throws DataFormatException {
		activity.addTriple("prov:endedAtTime", format.format(new Date(Long.valueOf(timestamp)*1000)), TRIPLE_TYPE.DATA_PROPERTY);
		activity.addTriple("prov:wasEndedBy", this.iri, TRIPLE_TYPE.OBJECT_PROPERTY);
    
		EDMProvFactory.getInstance().elementUpdated(this); // Queue to re-send in next report
	}
	
	public void stopActivity(EDMActivity activity) throws DataFormatException {
		stopActivity(activity, String.valueOf(System.currentTimeMillis() / 1000L));
	}
	
	public EDMActivity doDiscreteActivity(String uniqueIdentifier, String label, String timestamp) throws DataFormatException, DatatypeConfigurationException {
		EDMActivity discreteActivity = startActivity(uniqueIdentifier, label, timestamp);
		this.stopActivity(discreteActivity, timestamp);
    
		EDMProvFactory.getInstance().elementUpdated(this); // Queue to re-send in next report
    
		return discreteActivity;
	}
	
	public EDMActivity doDiscreteActivity(String uniqueIdentifier, String label) throws DataFormatException, DatatypeConfigurationException {
		return doDiscreteActivity(uniqueIdentifier, label, String.valueOf(System.currentTimeMillis() / 1000L));
	}
	
	public void actOnBehalfOf(EDMAgent agent) {
		this.addTriple(this.iri, "prov:actedOnBehalfOf", agent.iri, TRIPLE_TYPE.OBJECT_PROPERTY);
    
		EDMProvFactory.getInstance().elementUpdated(this); // Queue to re-send in next report
	}

}
