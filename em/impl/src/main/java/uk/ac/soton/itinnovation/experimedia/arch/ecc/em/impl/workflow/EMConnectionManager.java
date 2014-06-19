/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2012
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
//      Created Date :          13-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.IAMQPMessageDispatchPump;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMMonitorEntryPoint_ProviderListener;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces.EMMonitorEntryPoint;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;

import java.util.*;






public class EMConnectionManager implements IEMMonitorEntryPoint_ProviderListener
{
  private final Object            clientListLock = new Object();
  private UUID                    entryPointID;
  private EMMonitorEntryPoint     entryPointInterface;
  private AMQPMessageDispatchPump entryPointPump;
  
  private boolean entryPointOpen = false;
  
  private HashMap<UUID, EMClientEx>   historicClients;   // Clients connected at least once
  private HashMap<UUID, EMClientEx>   connectedClients;    // Clients currently actually connected
  private EMConnectionManagerListener connectionListener;
  
  
  public EMConnectionManager()
  {
    historicClients  = new HashMap<UUID, EMClientEx>();
    connectedClients = new HashMap<UUID, EMClientEx>();
  }
  
  public boolean initialise( UUID epID, 
                             AMQPBasicChannel channel )
  {
    if ( !entryPointOpen )
    {
      if ( epID != null && channel != null )
      {
        entryPointID    = epID;

        AMQPMessageDispatch dispatch = new AMQPMessageDispatch();

        entryPointPump = new AMQPMessageDispatchPump( "EM Entry Point pump",
                                                      IAMQPMessageDispatchPump.ePumpPriority.MINIMUM );

        entryPointPump.addDispatch( dispatch );

        entryPointPump.startPump();

        entryPointInterface = new EMMonitorEntryPoint( channel,
                                                       dispatch,
                                                       entryPointID,
                                                       true );
        entryPointInterface.setListener( this );

        entryPointOpen = true;
      }
    }
    
    return entryPointOpen;
  }
  
  public void shutdown()
  {
    // We no longer send disconnect messages to clients when the ECC shuts down
		// (the experiment life-cycle is managed elsewhere)
		
    entryPointPump.stopPump();
    entryPointInterface.shutdown();
  }
  
  public void setListener( EMConnectionManagerListener listener )
  { connectionListener = listener; }
  
  public boolean isEntryPointOpen()
  { return entryPointOpen; }
  
  public void clearAllHistoricClients()
  { synchronized( clientListLock ) { historicClients.clear(); } }
  
  public void disconnectAllClients( String reason )
  { 
    // Get copy of all clients to remove
    HashSet<UUID> clientIDs = new HashSet<UUID>();
    
    synchronized( clientListLock )
    {
      Iterator<EMClientEx> cIt = connectedClients.values().iterator();
      while ( cIt.hasNext() )
      { clientIDs.add( cIt.next().getID() ); }
    }
    
    // Disconnect and remove clients
    Iterator<UUID> idIt = clientIDs.iterator();
    while ( idIt.hasNext() )
    { disconnectAndRemoveClient( idIt.next(), reason ); }
  }
  
  public int getConnectedClientCount()
  {
    int count = 0;
    
    synchronized ( clientListLock )
    { count = connectedClients.size(); }
    
    return count; 
  }
  
  public EMClientEx getClient( UUID clientID )
  { return connectedClients.get( clientID ); }
  
  public void removeDisconnectedClient( UUID clientID ) throws Exception
  {
    if ( clientID == null ) throw new Exception( "Cannot remove client: UUID is invalid" );
    
    EMClientEx client;
    
    synchronized( clientListLock )
    { client = connectedClients.get( clientID ); }
    
    if ( client != null )
    {
      if ( client.isConnected() ) throw new Exception( "Cannot remove disconnected client (" +
                                                        clientID.toString() + "): client is still connected" );
      
      client.setIsDisconnecting( false );
      client.destroyAllInterfaces();
      
      synchronized( clientListLock )
      { connectedClients.remove( clientID ); }
    }
    else throw new Exception( "Cannot remove disconnected client: Client is unknown" );    
  }
  
  public void disconnectAndRemoveClient( UUID clientID, String reason )
  {
    // Destroy all ECC interfaces and set as disconnected
    EMClientEx client;
    
    synchronized( clientListLock )
    { client = connectedClients.get( clientID ); }
    
    if ( client != null && client.isConnected() )
    {
      client.getDiscoveryInterface().deregisteringThisClient( reason );
      
      client.setIsConnected( false );
      client.setIsDisconnecting( true );
      client.destroyAllInterfaces();
      
      synchronized( clientListLock )
      { connectedClients.remove( clientID ); }
    }
  }
  
  public Set<EMClientEx> getCopyOfConnectedClients()
  {
    HashSet<EMClientEx> currClients = new HashSet<EMClientEx>();
    
    synchronized( clientListLock )
    { currClients.addAll( connectedClients.values() ); }
    
    return currClients;
  }
  
  public Set<EMClientEx> getCopyOfAllKnownClients()
  {
    HashSet<EMClientEx> knownClients = new HashSet<EMClientEx>();
    
    synchronized( clientListLock )
    { knownClients.addAll( historicClients.values() ); }
    
    return knownClients;
  }
	
    public void reRegisterEMClient( UUID userID, String userName )
    {
        if ( userID != null && userName != null )
        {
            // Only re-register client if it is not already connected
            if ( !connectedClients.containsKey( userID ) )
            {
                boolean clientKnown	= historicClients.containsKey( userID );
                
                EMClientEx incomingClient = createRegisteredClient( userID, userName );

                // Notify listener of new connection
                if ( incomingClient != null )
                {
                    incomingClient.setIsReRegistering( true );
                    connectionListener.onClientRegistered( incomingClient, clientKnown );
                }
            }
        }
    }
  
  // IEMMonitorEntryPoint_ProviderListener -------------------------------------
  @Override
  public void onRegisterAsEMClient( UUID userID, String userName )
  {
    if ( userID != null && userName != null )
    {
			boolean clientKnown				= historicClients.containsKey( userID );
			EMClientEx incomingClient = createRegisteredClient( userID, userName );
      
      // Notify listener of new connection
      if ( incomingClient != null )
      {
        incomingClient.setIsConnected( true );
        connectionListener.onClientRegistered( incomingClient, clientKnown );
      }
    }
  }
	
	// Private methods -----------------------------------------------------------
	private EMClientEx createRegisteredClient( UUID userID, String userName )
	{
		boolean    clientKnown, clientAlreadyConnected;
		EMClientEx incomingClient = null;

		// Find out what we know about this client
		synchronized( clientListLock )
		{ 
			clientKnown            = historicClients.containsKey( userID );
			clientAlreadyConnected = connectedClients.containsKey( userID );

			// If unknown, create the new client
			if ( !clientKnown )
			{
				incomingClient = new EMClientEx( userID, userName );
				historicClients.put( userID, incomingClient );
			}
			else
				incomingClient = historicClients.get( userID );
		}

		// If not already connected, put client on connected list
		if ( incomingClient != null && !clientAlreadyConnected )
			synchronized( clientListLock ) { connectedClients.put( userID, incomingClient ); }
		
		return incomingClient;
	}
}
