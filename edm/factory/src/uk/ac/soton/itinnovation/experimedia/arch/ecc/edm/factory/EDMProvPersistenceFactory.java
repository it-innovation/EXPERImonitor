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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory;

import java.util.Properties;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.dao.EDMProvDataStoreImpl;

/**
 * This class manages access to the provenance store.
 */
public final class EDMProvPersistenceFactory {
	
	private static Properties props;
	private static Logger logger;

	private static EDMProvPersistenceFactory factory;
	
	private EDMProvPersistenceFactory(java.util.Properties props) {}
	
	/**
	 * Returns an instance of the persistence factory
	 * 
	 * @param props the properties
	 * @return the factory
	 */
	public static synchronized EDMProvPersistenceFactory getInstance(Properties props) {
		
		EDMProvPersistenceFactory.props = props;
		logger = Logger.getLogger(EDMProvPersistenceFactory.class);

		if (factory==null) {
			factory = new EDMProvPersistenceFactory(props);
			logger.debug("Created new EDMProvPersistenceFactory");
		} else {
			logger.debug("Returned existing EDMProvPersistenceFactory");
		}
		
        return factory;	
	}
	


	/**
	 * Returns the specified store
	 * 
	 * @param experimentID	The experimentID, which is at the same time the repositoryID
	 * @param owlimServerURL the location of the OWLIMLite server
	 * @return the store
	 */
    public EDMProvDataStoreImpl getStore(String experimentID, String owlimServerURL) {

		Properties newprops = props;
		newprops.setProperty("owlim.sesameServerURL", owlimServerURL);
		newprops.setProperty("owlim.repositoryID", experimentID);
		
		return getStore(newprops);
    }
	
	/**
	 * Returns the standard store
	 * 
	 * @return the store
	 */
	public EDMProvDataStoreImpl getStore() {
		return getStore(props);
	}
		
	/**
	 * Returns the store defined in the properties
	 * 
	 * @param props the properties
	 * @return the store
	 */
	public EDMProvDataStoreImpl getStore(Properties props) {
		
		EDMProvDataStoreImpl store = null;
		
		try {
			store = new EDMProvDataStoreImpl(props);
		} catch (Exception e) {
			logger.error("Error creating new EDMProvStoreWrapper", e);
		}
		
		return store;
	}



}
