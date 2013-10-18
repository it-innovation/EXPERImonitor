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


public class EDMActivity extends EDMProvBaseElement {

	public EDMActivity(String iri) {
		super(iri);
		this.addOwlClass("prov:Activity");
	}
	
	public EDMEntity generateEntity(String entity) throws DataFormatException {
    EDMProvFactory factory = EDMProvFactory.getInstance();
    
		EDMEntity newEntity = factory.getEntity(entity);	
		newEntity.addProperty(entity, "prov:wasGeneratedBy", this.iri);
    
    factory.elementUpdated(this); // Queue to re-send in next report
    
		return newEntity;
	}
	
	public EDMEntity deriveEntity(EDMEntity entity) throws DataFormatException {
		EDMProvFactory factory = EDMProvFactory.getInstance();
    
    EDMEntity derivation = factory.getEntity(entity.iri + "_derivation" +
			String.valueOf(System.currentTimeMillis() / 1000L));
		
    derivation.addProperty(derivation.iri, "prov:wasDerivedFrom", this.iri);
    
    factory.elementUpdated(this); // Queue to re-send in next report
    
		return derivation;
	}
	
	public void associateWith(EDMAgent agent) {
		agent.addProperty(agent.iri, "prov:wasAssociatedWith", this.iri);
    
    EDMProvFactory.getInstance().elementUpdated(this); // Queue to re-send in next report
	}
	
	public void useEntity(EDMEntity entity) {
		this.useEntity(entity.iri);
    // Updated below in overloaded method
	}
	
	public void useEntity(String entity) {
		this.addProperty(this.iri, "prov:used", entity);
    
    EDMProvFactory.getInstance().elementUpdated(this); // Queue to re-send in next report
	}
	
	public void informActivity(EDMActivity activity) {
		activity.addProperty(activity.iri, "prov:wasInformedBy", this.iri);
    
    EDMProvFactory.getInstance().elementUpdated(this); // Queue to re-send in next report
	}

}
