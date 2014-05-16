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
//      Created Date :          09-Apr-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.co.soton.itinnovation.ecc.service.process;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.client.IEDMClientPersistence;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.client.dao.IClientDAO;

import org.slf4j.*;

import java.util.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;




public class ExperimentStateModel
{
    private final Logger stateModelLog = LoggerFactory.getLogger(getClass());
    
    private IEDMClientPersistence clientPersistence;
	private IClientDAO            clientDAO;
    
    private Experiment activeExperiment;
    private EMPhase    activeExpPhase = EMPhase.eEMUnknownPhase;

    
    public ExperimentStateModel()
    {
    }
    
    public void reset()
	{
		clientDAO         = null;
		clientPersistence = null;
        activeExperiment  = null;
        activeExpPhase    = EMPhase.eEMUnknownPhase;
	}
	
	public void initialise( Properties dbConfig ) throws Exception
	{
		if ( dbConfig == null ) throw new Exception( "Could not initialise dashboard state model: Database config is null" );
		
		clientPersistence = EDMInterfaceFactory.getEDMClientPersistence( dbConfig ); // throws up if there's a problem
		clientDAO         = clientPersistence.getClientDAO();
	}
	
	public boolean isClientConnected( EMClient client )
	{
		boolean result = false;
		
		if ( client != null )
			try
			{
				result = clientDAO.isClientConnected( client.getID() );
			}
			catch ( Exception ex )
			{
				stateModelLog.error( "Could not test client connection: " + ex.getMessage() );
			}
		
		return result;
	}
	
	public void setClientConnectedState( EMClient client, boolean connected )
	{
		// Safety first
		if ( client == null ) stateModelLog.error( "Could not persist client connection: client is null" );
		if ( clientDAO == null ) stateModelLog.error( "Could not persist client connection: Client DAO is null" );
		
		stateModelLog.info( "Updating client connection state: " + client.getID() + " (\"" + client.getName() + "\")");
		
		if ( connected )
			setClientConnected( client );
		else
			setClientDisconnected( client );
	}
	
	public Map<UUID,String> getConnectedClientInfo()
	{
		HashMap<UUID,String> clientInfo = new HashMap<UUID,String>();
		
		// Safety first
		if ( clientDAO == null ) stateModelLog.error( "Could not get connected client IDs: Client DAO is null" );
		
		try
		{
			Set<EMClient> thinClients = clientDAO.getConnectedClients(); // These are NOT properly initialised clients
			for ( EMClient tC : thinClients )
				clientInfo.put( tC.getID(), tC.getName() );
		}
		catch ( Exception ex )
		{
			stateModelLog.error( "Could not get connected client IDs: " + ex.getMessage() );
		}
		
		return clientInfo;
	}
    
    public void setActiveExperiment( Experiment exp )
    {
        activeExperiment = exp;
    }
    
    public Experiment getActiveExperiment()
    {
        return activeExperiment;
    }
    
    public boolean isExperimentActive()
    {
        return (activeExperiment != null);
    }
    
    public void setCurrentPhase( EMPhase phase )
    {
        activeExpPhase = phase;
    }
    
    public EMPhase getCurrentPhase()
    {
        return activeExpPhase;
    }
    
	// Private methods -----------------------------------------------------------
	private void setClientConnected( EMClient client )
	{
		try
		{
			if ( clientDAO.isClientConnected(client.getID()) )
				stateModelLog.warn( "Tried to set connected state of client already connected: " + client.getID() + " (\"" + client.getName() + "\")");
			else
			{
				clientDAO.addClientConnected( client );
				stateModelLog.info( "Client is connected: " + client.getID() + " (\"" + client.getName() + "\")");
			}
		}
		catch ( Exception ex )
		{
			String msg = "Could not persist client connection state: " + client.getID() + " (\"" + client.getName() + "\")" + ex.getMessage();
			stateModelLog.error( msg );
		}
	}
	
	private void setClientDisconnected( EMClient client )
	{
		try
		{
			if ( clientDAO.isClientConnected(client.getID()) )
			{
				clientDAO.removeClientConnected( client );
				stateModelLog.info( "Client is disconnected: " + client.getID() + " (\"" + client.getName() + "\")");
			}
			else
				stateModelLog.warn( "Tried to disconnect an already disconencted client: " + client.getID() + " (\"" + client.getName() + "\")");
		}
		catch ( Exception ex )
		{
			String msg = "Could not persist client connection state: " + client.getID() + " (\"" + client.getName() + "\")" + ex.getMessage();
			stateModelLog.error( msg );
		}
	}
}
