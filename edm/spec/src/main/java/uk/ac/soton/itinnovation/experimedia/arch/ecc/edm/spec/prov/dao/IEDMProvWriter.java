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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvReport;

/**
 * This class manages all write access to the EDM prov store.
 */
public interface IEDMProvWriter {

	/**
	 * Imports an ontology into the store by persisting all its triples.
	 *
	 * @param ontologypath the path where the ontology is located. Can be on file or on the internet
	 * @param baseURI the ontology's base URI
	 * @param prefix the ontology's short prefix
	 * @param resourcepathclass any class in the resource path so the main/resources directory can be accessed
	 */
	void importOntology(String ontologypath, String baseURI, String prefix, Class resourcepathclass);

	/**
	 * Stores a prov report in the store.
	 *
	 * @param report the report to store
	 */
    void storeReport(EDMProvReport report);

	/**
	 * Clears the repository with the given ID.
	 *
	 * @param repositoryID
	 */
	void clearRepository(String repositoryID);

	/**
	 * Disconnects from the EDM prov store.
	 */
	void disconnect();

}
