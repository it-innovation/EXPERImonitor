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


public class EDMActivity extends EDMProvBaseElement {

	public EDMActivity(String prefix, String uniqueIdentifier, String label) {
		
		super(prefix, uniqueIdentifier, label);

		this.provType = PROV_TYPE.ePROV_ACTIVITY;
		this.addOwlClass("prov:Activity");
	}
	
	// PROV FUNCTIONAL CLASSES HERE: //////////////////////////////////////////////////////////////

	public EDMEntity generateEntity(String uniqueIdentifier, String entityLabel) throws DataFormatException, DatatypeConfigurationException {
		return generateEntity(uniqueIdentifier, entityLabel, String.valueOf(System.currentTimeMillis() / 1000L));
	}
	
	public EDMEntity generateEntity(String uniqueIdentifier, String label, String timestamp) throws DataFormatException, DatatypeConfigurationException {
		EDMProvFactory factory = EDMProvFactory.getInstance();
    
		EDMEntity newEntity = (EDMEntity) factory.createElement(uniqueIdentifier, label, PROV_TYPE.ePROV_ENTITY);	
		newEntity.addTriple(label, "prov:wasGeneratedBy", this.iri, TRIPLE_TYPE.OBJECT_PROPERTY);
		newEntity.addTriple(label, "prov:generatedAtTime", format.format(new Date(Long.valueOf(timestamp)*1000)), TRIPLE_TYPE.DATA_PROPERTY);
		factory.elementUpdated(this); // Queue to re-send in next report
    
		return newEntity;
	}

	public EDMEntity deriveEntity(EDMEntity entity, String derivationLabel) throws DataFormatException, DatatypeConfigurationException {
		EDMProvFactory factory = EDMProvFactory.getInstance();
    
		String newUniqueIdentifier = entity.getUniqueIdentifier() + "_derivation_"
			+ String.valueOf(System.currentTimeMillis() / 1000L);
		EDMEntity derivation = (EDMEntity) factory.createElement(newUniqueIdentifier, derivationLabel, PROV_TYPE.ePROV_ENTITY);
		
		derivation.addTriple(derivation.iri, "prov:wasDerivedFrom", this.iri, TRIPLE_TYPE.OBJECT_PROPERTY);
    
		factory.elementUpdated(this); // Queue to re-send in next report
    
		return derivation;
	}
	
	public void invalidateEntity(EDMEntity entity, String timestamp) {
		entity.addTriple(entity.iri, "prov:wasInvalidatedBy", this.iri, TRIPLE_TYPE.OBJECT_PROPERTY);
		entity.addTriple(entity.iri, "prov:invalidatedAtTime", format.format(new Date(Long.valueOf(timestamp)*1000)), TRIPLE_TYPE.DATA_PROPERTY);
	}
	
	public void invalidateEntity(EDMEntity entity) {
		invalidateEntity(entity, String.valueOf(System.currentTimeMillis() / 1000L));
	}
	
	public void associateWith(EDMAgent agent) {
		agent.addTriple(agent.iri, "prov:wasAssociatedWith", this.iri, TRIPLE_TYPE.OBJECT_PROPERTY);
    
		EDMProvFactory.getInstance().elementUpdated(this); // Queue to re-send in next report
	}
	
	public void useEntity(EDMEntity entity) {
		this.useEntity(entity.iri);
		// Updated below in overloaded method
	}
	
	private void useEntity(String entity) {
		this.addTriple(this.iri, "prov:used", entity, TRIPLE_TYPE.OBJECT_PROPERTY);
    
		EDMProvFactory.getInstance().elementUpdated(this); // Queue to re-send in next report
	}
	
	public void informActivity(EDMActivity activity) {
		activity.addTriple(activity.iri, "prov:wasInformedBy", this.iri, TRIPLE_TYPE.OBJECT_PROPERTY);
    
		EDMProvFactory.getInstance().elementUpdated(this); // Queue to re-send in next report
	}
	
	public void influenceActivity(EDMActivity activity) {
		activity.addTriple(activity.iri, "prov:wasInfluencedBy", this.iri, TRIPLE_TYPE.OBJECT_PROPERTY);
		this.addTriple(this.iri, "prov:influenced", activity.iri, TRIPLE_TYPE.OBJECT_PROPERTY);
	}

}
