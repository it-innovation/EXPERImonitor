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

public class EDMAgent extends EDMProvBaseElement {

	public EDMAgent(String iri) {
		super(iri);
    
    this.provType = PROV_TYPE.ePROV_AGENT;
		this.addOwlClass("prov:Agent");
	}
	
	public EDMActivity startActivity(String activity, String timestamp) throws DataFormatException {
		EDMProvFactory factory = EDMProvFactory.getInstance();
	    
		EDMActivity newActivity = factory.getActivity(activity);
		newActivity.addProperty(activity, "prov:startedAtTime", format.format(new Date(Long.valueOf(timestamp)*1000)));
		newActivity.addProperty(activity, "prov:wasStartedBy", this.iri);
    
		factory.elementUpdated(this); // Queue to re-send in next report
    
		return newActivity;
	}
	
	public EDMActivity startActivity(String activity) throws DataFormatException {
		return startActivity(activity, String.valueOf(System.currentTimeMillis() / 1000L));
	}
	
	public void stopActivity(EDMActivity activity, String timestamp) throws DataFormatException {
		activity.addProperty("prov:endedAtTime", format.format(new Date(Long.valueOf(timestamp)*1000)));
		activity.addProperty("prov:wasEndedBy", this.iri);
    
		EDMProvFactory.getInstance().elementUpdated(this); // Queue to re-send in next report
	}
	
	public void stopActivity(EDMActivity activity) throws DataFormatException {
		stopActivity(activity, String.valueOf(System.currentTimeMillis() / 1000L));
	}
	
	public EDMActivity doDiscreteActivity(String activity, String timestamp) throws DataFormatException {
		EDMActivity discreteActivity = this.startActivity(activity, timestamp);
		this.stopActivity(discreteActivity, timestamp);
    
		EDMProvFactory.getInstance().elementUpdated(this); // Queue to re-send in next report
    
		return discreteActivity;
	}
	
	public EDMActivity doDiscreteActivity(String activity) throws DataFormatException {
		return doDiscreteActivity(activity, String.valueOf(System.currentTimeMillis() / 1000L));
	}
	
	public void actOnBehalfOf(EDMAgent agent) {
		this.addProperty(this.iri, "prov:actedOnBehalfOf", agent.iri);
    
		EDMProvFactory.getInstance().elementUpdated(this); // Queue to re-send in next report
	}

}
