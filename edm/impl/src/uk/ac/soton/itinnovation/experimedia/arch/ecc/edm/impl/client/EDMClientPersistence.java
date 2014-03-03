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
//      Created By :            Simon Crowle
//      Created Date :          25-Feb-2014
//      Created for Project :   experimedia-arch-ecc-edm-impl-metrics
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.client;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.client.IEDMClientPersistence;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.client.dao.IClientDAO;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.client.dao.EDMClientDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.metrics.db.*;

import java.util.Properties;




public class EDMClientPersistence implements IEDMClientPersistence
{
	static IECCLogger persistLog = Logger.getLogger( EDMClientPersistence.class );
	
	private DatabaseConnector dbConnector;
	
	public EDMClientPersistence( Properties dbConfig ) throws Exception
	{
		// Safety first
		if ( dbConfig == null ) throw new IllegalArgumentException( "EDM Client Peristence could not be created as config is null" );
		
		// Try setting up
		try
		{
			dbConnector = new DatabaseConnector( dbConfig.getProperty("dbURL"), 
																					 dbConfig.getProperty("dbName"), 
																					 dbConfig.getProperty("dbUsername"),
																					 dbConfig.getProperty("dbPassword"), 
																					 DatabaseType.fromValue(dbConfig.getProperty("dbType")) );
		}
		catch ( Throwable ex )
		{
			String msg = "EDM Client Persistence could not create database connector: " + ex.getMessage();
			persistLog.error( msg, ex );
			throw new RuntimeException( msg, ex );
		}
		
	}
	
	// IEDMClientPersistence -----------------------------------------------------
	@Override
	public IClientDAO getClientDAO() throws Exception
	{
		if ( dbConnector == null ) throw new RuntimeException( "Could not create Client DAO - database connector is null" );
		
		EDMClientDAO clientDAO = new EDMClientDAO( dbConnector );
		
		return clientDAO;
	}
}
