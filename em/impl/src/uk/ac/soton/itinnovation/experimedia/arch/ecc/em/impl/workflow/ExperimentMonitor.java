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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecylePhases.EMLifecycleManager;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvReport;

import java.util.*;




public class ExperimentMonitor implements IExperimentMonitor,
                                          IEMLifecycleListener
{
  private final Logger emLogger = LoggerFactory.getLogger(getClass());
  
  private AMQPConnectionFactory amqpConnectionFactory;
  private AMQPBasicChannel      amqpChannel;
  private UUID                  entryPointID;
  
  private EMConnectionManager           connectionManager;
  private EMLifecycleManager            lifecycleManager;
  private HashSet<IEMLifecycleListener> lifecycleListeners;
  
  private IExperimentMonitor.eStatus monitorStatus = IExperimentMonitor.eStatus.NOT_YET_INITIALISED;
  
  
  public ExperimentMonitor()
  {
    lifecycleListeners    = new HashSet<IEMLifecycleListener>();
    amqpConnectionFactory = new AMQPConnectionFactory();
  }
 
  // IExperimentMonitor --------------------------------------------------------
  @Override
  public eStatus getStatus()
  { return monitorStatus; }
  
  @Override
  public void openEntryPoint( String rabbitServerIP, UUID epID ) throws Exception
  {
    // Safety first
    if ( rabbitServerIP == null || rabbitServerIP.equals("") )
      throw new Exception( "Rabbit server IP is invalid" );
    
    if ( epID == null ) 
      throw new Exception( "Entry point ID is null" );
    
    entryPointID = epID;
    
    // Try initialising a connection with the Rabbit Server
    try
    { 
      basicInitialise( rabbitServerIP );
      initialiseManagers();
    }
    catch( Exception e ) { throw e; }
  }
  
  @Override
  public void openEntryPoint( Properties emProps ) throws Exception
  {
    // Safety first
    if ( emProps == null ) throw new Exception( "Configuration properties are NULL" );
    
    String epVal = emProps.getProperty( "Monitor_ID" );
    entryPointID = UUID.fromString( epVal );
    
    if ( entryPointID == null ) throw new Exception( "Configuration of entry point ID is invalid" );
    
    // Now try connecting and opening the entry point
    try
    {
      configInitialise( emProps );
      
      initialiseManagers();
    }
    catch ( Exception ex )
    {
      String msg = "Had problems opening EM entry point: " + ex.getMessage();
      emLogger.error( msg );
      
      throw new Exception( msg, ex ); 
    }
  }
  
  @Override
  public void shutDown()
  {
    try
    {
			// Tidy up experiment life-cycle first
			if ( lifecycleManager != null )
			{
				lifecycleManager.endLifecycle();
				lifecycleManager.shutdown();
			}
			
			// Then tidy up the connection manager
      if ( connectionManager != null ) connectionManager.shutdown();
      
      amqpChannel = null;
      
      amqpConnectionFactory.closeDownConnection();
    }
    catch ( Exception e )
    {
      emLogger.error( "Trying to shut down Experiment monitor & lifecycle, but: " +
                      e.getMessage() ); 
    }
  }
  
  @Override
  public EMClient getClientByID( UUID id )
  {
    return connectionManager.getClient( id );
  }
  
  @Override
  public Set<EMClient> getAllConnectedClients()
  {    
    return getSimpleClientSet( connectionManager.getCopyOfConnectedClients() );
  }
  
  @Override
  public Set<EMClient> getAllKnownClients()
  {
    return getSimpleClientSet( connectionManager.getCopyOfAllKnownClients() );
  }
  
  @Override
  public Set<EMClient> getCurrentPhaseClients()
  {
    HashSet<EMClient> clients = new HashSet<EMClient>();
    
    Set<EMClientEx> exClients = lifecycleManager.getCopySetOfCurrentPhaseClients();
    Iterator<EMClientEx> exIt = exClients.iterator();
    
    while ( exIt.hasNext() )
      clients.add( exIt.next() );
    
    return clients;
  }
  
  @Override
  public void deregisterClient( EMClient client, String reason ) throws Exception
  {
    // Safety first
    if ( client == null ) throw new Exception( "Cannot de-register client: client is NULL" );
    if ( !client.isConnected() ) throw new Exception( "Cannot de-register client: is already disconnected" );
    if ( client.isDisconnecting() ) throw new Exception( "Alreadying trying to de-register client" );
    
    EMClientEx clientEx = (EMClientEx) client;
    clientEx.getDiscoveryInterface().deregisteringThisClient( reason );
  }
  
	@Override
	public void tryReRegisterClients( Map<UUID, String> clientInfo ) throws Exception
	{
		// Safety first
		if ( clientInfo == null ) throw new Exception( "Could not re-register clients: client info is null" );
		if ( connectionManager == null || lifecycleManager == null ) throw new Exception( "Could not re-register clients: internal managers are not ready" );
		
		// Issue a manual register event on behalf of apparently connected clients -
		// they should respond correctly if they are still connected
		for ( UUID clID : clientInfo.keySet() )
			connectionManager.reRegisterEMClient( clID, clientInfo.get(clID) );
	}
	
  @Override
  public void forceClientDisconnection( EMClient client ) throws Exception
  {
    // Safety first
    if ( client == null ) throw new Exception( "Cannot de-register client: client is NULL" );
    if ( !client.isConnected() ) throw new Exception( "Cannot de-register client: is already disconnected" );
    
    // Don't go through registration - just remove
    lifecycleManager.onClientIsDisconnected( (EMClientEx) client, client.getID() );
  }
  
  @Override
  public void addLifecyleListener( IEMLifecycleListener listener )
  { lifecycleListeners.add(listener); }
  
  @Override
  public void removeLifecycleListener( IEMLifecycleListener listener )
  { lifecycleListeners.remove(listener); }
  
  @Override
  public EMPhase startLifecycle( Experiment expInfo ) throws Exception
  {
    if ( expInfo == null ) throw new Exception( "Experiment info is NULL" );
    
    if ( monitorStatus != IExperimentMonitor.eStatus.ENTRY_POINT_OPEN )
      throw new Exception( "Cannot start life-cycle: client entry point is not open" );
    
    if ( lifecycleManager.isLifecycleActive() )
      throw new Exception( "Lifecycle has already started" );
    
		// Start lifecyle at the beginning
		EMPhase startPhase = EMPhase.eEMDiscoverMetricGenerators;
		
    startLifecycle( expInfo, startPhase );
		
		return startPhase;
  }
  
  @Override
  public void startLifecycle( Experiment expInfo, EMPhase startPhase ) throws Exception
  {
    if ( expInfo == null ) throw new Exception( "Experiment info is NULL" );
    
    if ( monitorStatus != IExperimentMonitor.eStatus.ENTRY_POINT_OPEN )
      throw new Exception( "Cannot start life-cycle: client entry point is not open" );
		
		if ( lifecycleManager.isLifecycleActive() )
      throw new Exception( "Lifecycle has already started" );
    
    try 
    {
      lifecycleManager.setExperimentInfo( expInfo );
			
			// Add currently (still) connected clients and add them in to the new lifecycle
			Set<EMClientEx> connectedClients = connectionManager.getCopyOfConnectedClients();
			
			// Notify listener of existing clients already connected
			for ( IEMLifecycleListener lcl : lifecycleListeners )
				for ( EMClientEx client : connectedClients )
					lcl.onClientConnected( client, true );
			
			// Then start experiment lifecycle
      lifecycleManager.startLifeCycleAt( startPhase,
																				 connectedClients );
    }
    catch ( Exception ex ) { throw ex; /* Throw this up*/ }
  }
  
  @Override
  public EMPhase getCurrentPhase()
  {
    EMPhase currentPhase = EMPhase.eEMUnknownPhase;
    
    if ( lifecycleManager != null )
        currentPhase = lifecycleManager.getCurrentPhase();
    
    return currentPhase;
  }
  
  @Override
  public boolean isCurrentPhaseActive()
  { return lifecycleManager.isCurrentPhaseActive(); }
  
  @Override
  public void goToNextPhase() throws Exception
  {
    lifecycleManager.iterateLifecycle();
  }
  
  @Override
  public void endLifecycle() throws Exception
  {
    if ( !lifecycleManager.isLifecycleActive() ) throw new Exception( "Cannot end lifecycle: it is not active" );
    
    lifecycleManager.endLifecycle();
    
		// Clients are no longer disconnected from the EM after an experiment lifecycle ends
    
    // Notify EM client of reset completion
    onLifecycleEnded();
  }
  
  @Override
  public void resetLifecycle() throws Exception
  {
    // Only reset if we have ended the lifecycle
    if ( lifecycleManager.isLifecycleActive() ) throw new Exception( "Lifecycle must be ended first" );
        
    lifecycleManager.resetLifecycle();
		
		// Reset currently known client states & metric generator history before starting experiment
		Set<EMClientEx> connectedClients = connectionManager.getCopyOfAllKnownClients();
		for ( EMClientEx client : connectedClients )
		{
			client.resetPhaseStates();
			client.clearHistoricMetricGenerators();
		}
  }  
  
  @Override
  public void pullMetric( EMClient client, UUID measurementSetID ) throws Exception
  {
    try { lifecycleManager.tryPullMetric(client, measurementSetID); }
    catch ( Exception e ) { throw e; }
  }
  
  @Override
  public void pullAllMetrics( EMClient client ) throws Exception
  {
    try { lifecycleManager.tryPullAllMetrics( client ); }
    catch ( Exception e ) { throw e; }
  }
  
  @Override
  public void requestDataBatches( EMClient client, UUID measurementSetID ) throws Exception
  {
    try { lifecycleManager.tryRequestDataBatch( client, measurementSetID ); }
    catch ( Exception e ) { throw e; }
  }
  
  @Override
  public void getAllDataBatches( EMClient client ) throws Exception
  {
    try { lifecycleManager.tryGetAllDataBatches( client ); }
    catch ( Exception e ) { throw e; }
  }
  
  @Override
  public void notifyClientOfTimeOut( EMClient client ) throws Exception
  {
    try { lifecycleManager.tryClientTimeOut( client ); }
    catch ( Exception e ) { throw e; }
  }
  
  // IEMLifecycleListener ------------------------------------------------------
  @Override
  public void onClientConnected( EMClient client, boolean reconnected )
  {
    Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
    
    while ( listIt.hasNext() )
    { listIt.next().onClientConnected( client, reconnected ); }
  }
  
  @Override
  public void onClientDisconnected( UUID clientID )
  {
		if ( clientID != null )
		{
			EMClientEx client = connectionManager.getClient( clientID );
			
			// Update client state and notify
			if ( client != null ) 
			{
				client.setIsConnected( false );
				
				// Notify listeners first...
				for ( IEMLifecycleListener listener : lifecycleListeners )
						listener.onClientDisconnected( clientID );
				
				client.resetPhaseStates();
				
				// ... before removing permenantly
				try
				{
					connectionManager.removeDisconnectedClient( clientID );
				}
				catch ( Exception ex )
				{ emLogger.warn( "Could not properly clean up disconnected client: " + ex.getMessage() ); }
			}
		}
		else emLogger.warn( "Got disconnection notice from unknown client!" );
	    
  }
  
  @Override
  public void onClientStartedPhase( EMClient client, EMPhase phase )
  {
    Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
    
    while ( listIt.hasNext() )
    { listIt.next().onClientStartedPhase(client, phase); }
  }
  
  @Override
  public void onLifecyclePhaseStarted( EMPhase phase )
  {
    Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
    
    while ( listIt.hasNext() )
    { listIt.next().onLifecyclePhaseStarted( phase ); }
  }
  
  @Override
  public void onLifecyclePhaseCompleted( EMPhase phase )
  {    
    for ( IEMLifecycleListener listIt : lifecycleListeners )
      listIt.onLifecyclePhaseCompleted( phase );
  }
  
  @Override
  public void onNoFurtherLifecyclePhases()
  {
    Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
    
    while ( listIt.hasNext() )
    { listIt.next().onNoFurtherLifecyclePhases(); }
  }
  
  @Override
  public void onLifecycleEnded()
  {
    // This event is not geneated by the Lifecycle Manager, but used internally
    // to signal to EM clients that the reset process has completed.
    Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator(); 
    
    while ( listIt.hasNext() )
    { listIt.next().onLifecycleEnded(); }
  }
  
  @Override
  public void onFoundClientWithMetricGenerators( EMClient emClient, 
                                                 Set<MetricGenerator> newGens )
  {
		EMClientEx client = (EMClientEx) emClient;
		
		if ( client != null && newGens != null )
		{
			Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
    
			while ( listIt.hasNext() )
			{ listIt.next().onFoundClientWithMetricGenerators( client, newGens ); }
			
			// Reset re-registering state if we've got metric generators (connection is good)
			if ( client.isReRegistering() ) client.setIsReRegistering( false );
		}
  }
  
  @Override
  public void onClientEnabledMetricCollection( EMClient client, 
                                               UUID entityID, boolean enabled )
  {
    Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
    
    while ( listIt.hasNext() )
    { listIt.next().onClientEnabledMetricCollection( client, entityID, enabled ); }
  }
  
  @Override
  public void onClientSetupResult( EMClient client, boolean success )
  {
    Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
    
    while ( listIt.hasNext() )
    { listIt.next().onClientSetupResult( client, success ); }
  }
  
  @Override
  public void onClientDeclaredCanPush( EMClient client )
  {
    Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
    
    while ( listIt.hasNext() )
    { listIt.next().onClientDeclaredCanPush(client); }
  }
  
  @Override
  public void onClientDeclaredCanBePulled( EMClient client )
  {
    Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
    
    while ( listIt.hasNext() )
    { listIt.next().onClientDeclaredCanBePulled(client); }
  }
  
  @Override
  public void onGotMetricData( EMClient client, Report report )
  {
    Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
    
    while ( listIt.hasNext() )
    { listIt.next().onGotMetricData( client, report ); }
  }
  
  @Override
  public void onGotPROVData( EMClient client, EDMProvReport statement )
  {
    Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
    
    while ( listIt.hasNext() )
    { listIt.next().onGotPROVData( client, statement ); }
  }
  
  @Override
  public void onGotSummaryReport( EMClient client, EMPostReportSummary summary )
  {
    Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
    
    while ( listIt.hasNext() )
    { listIt.next().onGotSummaryReport( client, summary ); }
  }
  
  @Override
  public void onGotDataBatch( EMClient client, EMDataBatch batch )
  {
    Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
    
    while ( listIt.hasNext() )
    { listIt.next().onGotDataBatch( client, batch ); }
  }
  
  @Override
  public void onDataBatchMeasurementSetCompleted( EMClient client, UUID measurementSetID )
  {
    Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
    
    while ( listIt.hasNext() )
    { listIt.next().onDataBatchMeasurementSetCompleted( client, measurementSetID ); }
  }
  
  @Override
  public void onAllDataBatchesRequestComplete( EMClient client )
  {
    Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
    
    while ( listIt.hasNext() )
    { listIt.next().onAllDataBatchesRequestComplete( client ); }
  }
  
  @Override
  public void onClientTearDownResult( EMClient client, boolean success )
  {
    Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
    
    while ( listIt.hasNext() )
    { listIt.next().onClientTearDownResult( client, success ); }
  }
  
  // Private methods -----------------------------------------------------------
  private void basicInitialise( String rabbitServerIP ) throws Exception
  {
   if ( amqpConnectionFactory.isConnectionValid() ) // Try to close down any previous connection
     amqpConnectionFactory.closeDownConnection();
    
    if ( !amqpConnectionFactory.setAMQPHostIPAddress(rabbitServerIP) )
      throw new Exception( "Could not set the server IP correctly" );
    
    amqpConnectionFactory.connectToAMQPHost();
    if ( !amqpConnectionFactory.isConnectionValid() ) throw new Exception( "Could not connect to Rabbit server" );
    
    amqpChannel = amqpConnectionFactory.createNewChannel();
    if ( amqpChannel == null ) throw new Exception( "Could not create AMQP channel" );
  }
  
  private void configInitialise( Properties emProps ) throws Exception
  {
    if ( amqpConnectionFactory.isConnectionValid() ) // Try to close down any previous connection
     amqpConnectionFactory.closeDownConnection();
    
    try
    {
      amqpConnectionFactory.connectToAMQPHost( emProps );
      if ( !amqpConnectionFactory.isConnectionValid() ) throw new Exception( "Could not connect to Rabbit server" );
      
      amqpChannel = amqpConnectionFactory.createNewChannel();
      if ( amqpChannel == null ) throw new Exception( "Could not create AMQP channel" );
    }
    catch ( Exception e )
    { throw e; }
  }
  
  private void initialiseManagers() throws Exception
  {
    // Tidy up previous managers, if the exist
    if ( lifecycleManager != null )
      lifecycleManager.shutdown();
    
    if ( connectionManager != null )
      connectionManager.shutdown();
      
    // Create new managers
    connectionManager = new EMConnectionManager();
    lifecycleManager  = new EMLifecycleManager();
    
    monitorStatus = IExperimentMonitor.eStatus.INITIALISED;
    
    if ( monitorStatus != IExperimentMonitor.eStatus.INITIALISED ) 
      throw new Exception( "Not in a state to open entry point" );
    
    // Initialise connection manager
    if ( !connectionManager.initialise( entryPointID, amqpChannel ) )
      throw new Exception( "Could not open entry point interface!" );
    
    // Link connection manager to lifecycle manager
    connectionManager.setListener( lifecycleManager );
    
    // Initialise lifecycle manager
    lifecycleManager.initialise( amqpChannel, entryPointID, this );
  
    monitorStatus = IExperimentMonitor.eStatus.ENTRY_POINT_OPEN;
  }
  
  private Set<EMClient> getSimpleClientSet( Set<EMClientEx> exClients )
  {
    HashSet<EMClient> simpleClients = new HashSet<EMClient>();
    Iterator<EMClientEx> exIt       = exClients.iterator();
    
    while ( exIt.hasNext() )
    {  simpleClients.add( (EMClient) exIt.next() ); }
    
    return simpleClients;
  }
}
