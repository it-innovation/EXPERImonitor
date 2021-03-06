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

/**
 * This is an interface to the EDM prov store.
 */
public interface IEDMProvDataStore {

	/**
	 * Connects to the store
	 */
	void connect();

	/**
	 * Creates a repository with the given ID and name
	 *
	 * @param repositoryID the ID
	 * @param repositoryName the human readable name
	 */
	void createRepository(String repositoryID, String repositoryName);

	/**
	 * Deletes the repository with the given ID.
	 *
	 * @param repositoryID
	 */
	void deleteRepository(String repositoryID);

	/**
	 * Disconnects from the store.
	 */
	void disconnect();

}
