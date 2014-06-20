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
//      Created Date :          2014-02-25
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.client.dao;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.client.dao.IClientDAO;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.client.EDMClientEx;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.metrics.db.*;

import java.util.*;
import java.sql.*;





public class EDMClientDAO implements IClientDAO
{		
	private DatabaseConnector dbConnector;

	public EDMClientDAO( DatabaseConnector dc )
	{
		dbConnector = dc;
	}
	
	// IClientDAO ----------------------------------------------------------------
	@Override
	public void clearAllClients() throws Exception
	{
		Connection dbConnection = createDBConnection(); // throws up connections directly
		
		boolean succeeded = false;
		try
		{
			String query = "DELETE from Client";
			
			PreparedStatement pstmt = dbConnection.prepareStatement( query );
      pstmt.executeUpdate();
			
			succeeded = true;
		}
		catch ( Exception ex )
		{ throw ex; /*throw the exception up directly*/ }
		finally
		{
			if ( succeeded )
				dbConnection.commit();
			else
				dbConnection.rollback();
			
			if ( DBUtil.isConnected(dbConnection) ) dbConnection.close();
		}
	}
	
	@Override
	public void addClientConnected( EMClient client ) throws Exception
	{
		Connection dbConnection = createDBConnection(); // throws up connections directly
		
		validateClient( client ); // throws if there's a problem
		
		boolean succeeded = false;
		try
		{
			UUID clientID = client.getID();
			
			// If client already exists, throw it
			if ( DBUtil.objectExistsByUUID( "Client", "clientUUID", clientID, 
																			dbConnection, false ))
				throw new RuntimeException( "Client persistence: cannot add client as coonected: already connected" );
			
			// Otherwise, add client to the database
			String query = "INSERT INTO Client (clientUUID, name) VALUES (?, ?)";
			PreparedStatement pstmt = dbConnection.prepareStatement( query );
			pstmt.setObject( 1, clientID, java.sql.Types.OTHER );
			pstmt.setObject( 2, client.getName(), java.sql.Types.OTHER );
			
			pstmt.executeUpdate();
			succeeded = true;
		}
		catch ( Exception ex )
		{ throw ex; /*throw the exception up directly*/ }
		finally
		{
			if ( succeeded )
				dbConnection.commit();
			else
				dbConnection.rollback();
			
			if ( DBUtil.isConnected(dbConnection) ) dbConnection.close();
		}
	}
	
	@Override
	public Set<EMClient> getConnectedClients() throws Exception
	{
		HashSet<EMClient> connectedClients = new HashSet<EMClient>();
		
		Connection dbConnection = createDBConnection(); // throws up connections directly
		
		try
		{
			String query= "SELECT * from Client";
			PreparedStatement pstmt = dbConnection.prepareStatement( query );
			
			ResultSet rs = pstmt.executeQuery();
			while ( rs.next() )
			{
				String idVal   = rs.getString( "clientUUID" );
				String nameVal = rs.getString( "name" );
				
				if ( idVal != null && nameVal != null )
				{
					EDMClientEx client = new EDMClientEx( UUID.fromString(idVal),
																								nameVal );
					
					connectedClients.add( client );
				}
			}
		}
		catch ( Exception ex )
		{ throw ex; /*throw the exception up directly*/ }
		finally
		{			
			if ( DBUtil.isConnected(dbConnection) ) dbConnection.close();
		}
		
		return connectedClients;
	}
	
	@Override
	public boolean isClientConnected( UUID clientID ) throws Exception
	{
		// Safety first
		if ( clientID == null ) throw new IllegalArgumentException( "Client persistence: client UUID is invalid" );
		
		boolean result = false;
		
		Connection dbConnection = createDBConnection(); // throws up connections directly
		
		try
		{
			result = DBUtil.objectExistsByUUID( "Client", "clientUUID", clientID, 
																					dbConnection, false );
		}
		catch ( Exception ex )
		{ throw ex; /*throw the exception up directly*/ }
		finally
		{	
			if ( DBUtil.isConnected(dbConnection) ) dbConnection.close();
		}
		
		return result;
	}
	
	@Override
	public void removeClientConnected( EMClient client ) throws Exception
	{
		Connection dbConnection = createDBConnection(); // throws up connections directly
		
		validateClient( client ); // throws if there's a problem
		
		boolean succeeded = false;
		try
		{	
			String query = "DELETE from Client where clientUUID = ?";
      
			PreparedStatement pstmt = dbConnection.prepareStatement(query);
      pstmt.setObject( 1, client.getID(), java.sql.Types.OTHER );
      
			pstmt.executeUpdate();
			succeeded = true;
		}
		catch ( Exception ex )
		{ throw ex; /*throw the exception up directly*/ }
		finally
		{
			if ( succeeded )
				dbConnection.commit();
			else
				dbConnection.rollback();
			
			if ( DBUtil.isConnected(dbConnection) ) dbConnection.close();
		}
	}
	
	// Private methods -----------------------------------------------------------
	private Connection createDBConnection() throws Exception
	{
		if ( dbConnector == null ) throw new RuntimeException( "Client DAO: Could not clear all clients - dbConnector is null" );
		
		Connection dbConnection = null;
		try
		{
			dbConnection = dbConnector.getConnection( Connection.TRANSACTION_READ_COMMITTED );
		}
		catch ( Exception ex )
		{
			throw new RuntimeException( "Client DAO: Could not create DB connection" );
		}
		
		if ( dbConnection.isClosed() )
			throw new RuntimeException( "Client DAO: Could use DB connection - it is closed" );
		
		dbConnection.setAutoCommit( false );
		
		return dbConnection;
	}
	
	private void validateClient( EMClient client ) throws Exception
	{
		if ( client == null ) throw new IllegalArgumentException( "Client persistence validation: Client is null" );
		
		if ( client.getID() == null ) throw new IllegalArgumentException( "Client persistence validation: Client's ID is null" );
		
		if ( client.getName() == null ) throw new IllegalArgumentException( "Client persistence validation: Client's name is null" );
	}
}
