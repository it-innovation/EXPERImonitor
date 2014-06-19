/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2014
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
//      Created Date :          2014-01-20
//      Created for Project :   Experimedia
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.prov.dao;

import java.util.Date;
import java.util.Set;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvBaseElement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvDataContainer;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMTriple;

public interface IEDMProvReader {

	/**
	 * This method returns only the prov type and label of an element, making it possible to print
	 * it in a UI. This can be considered as an existence check for an element.
	 * 
	 * @param IRI the element's IRI
	 * @return the element's representation in the EDMProv data model
	 */
    public EDMProvBaseElement getElementCore(String IRI);

	/**
	 * This method returns a prov element (agent, entity or activity), including all of its
	 * incoming and outgoing relationships.
	 * 
	 * @param IRI the element's IRI
	 * @return the element's representation in the EDMProv data model
	 */
	public EDMProvBaseElement getElement(String IRI);
	
	/**
	 * This method returns a element (core version) including relationships (incoming and outgoing)
	 * to all the activities within the given timeframe that are directly connected to the element.
	 * 
	 * @param IRI the element's IRI
	 * @param start start date
	 * @param end end date
	 * @return the core element including the relevant relationships.
	 */
    public EDMProvBaseElement getElement(String IRI, Date start, Date end);

	/**
	 * This method an EDMProvDataContainer containing
	 *		a) all activities (containing all related triples) and
	 *		b) core elements (label and type) of those elements that are directly connected.
	 * Note that start and end dates are optional, so getElements(null,null) will return all the elements of all time.
	 * 
	 * @param start start date
	 * @param end end date
	 * @return the relevant elements
	 */
    public EDMProvDataContainer getElements(Date start, Date end);

	/**
	 * This method works like getElements(Date start, Date end) except that it filters for a prov type.
	 * Allowed values are: EDMProvBaseElement.PROV_TYPE.ePROV_UNKNOWN_TYPE, 
     *                     EDMProvBaseElement.PROV_TYPE.ePROV_ENTITY, 
     *                     EDMProvBaseElement.PROV_TYPE.ePROV_AGENT, 
     *                     EDMProvBaseElement.PROV_TYPE.ePROV_ACTIVITY
	 * where ePROV_UNKNOWN_TYPE is irrelevant and will be ignored.
	 * 
	 * @param type the type of prov element to be returned
	 * @param start start date
	 * @param end end date
	 * @return all the matching elements
	 */
    public EDMProvDataContainer getElements(EDMProvBaseElement.PROV_TYPE type, Date start, Date end);

	/**
	 * This little swiss army knife method returns all the triples from the store, that match the
	 * given arguments. As long as at least one of the arguments is supplied, all of the other
	 * arguments are optional.
	 * 
	 * @param subjectIRI the subject
	 * @param predicate the predicate
	 * @param objectIRI the object
	 * @return 
	 */
    public Set<EDMTriple> getTriples(String subjectIRI, String predicate, String objectIRI);

	/**
	 * Disconnect all the contained objects from whatever they are connected to.
	 */
	public void disconnect();

}
