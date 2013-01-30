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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecylePhases.EMLifecycleManager;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.IEMLifecycleListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;

import java.util.*;
import org.apache.log4j.Logger;





public class ExperimentMonitor implements IExperimentMonitor,
                                          IEMLifecycleListener
{
  private final Logger emLogger = Logger.getLogger( ExperimentMonitor.class );
  
  private IExperimentMonitor.eStatus monitorStatus = IExperimentMonitor.eStatus.NOT_YET_INITIALISED;
  private AMQPBasicChannel           amqpChannel;
  private UUID                       entryPointID;
  
  private EMConnectionManager           connectionManager;
  private EMLifecycleManager            lifecycleManager;
  private HashSet<IEMLifecycleListener> lifecycleListeners;
  
  private boolean isResettingMonitor;
  
  
  public ExperimentMonitor()
  {
    lifecycleListeners = new HashSet<IEMLifecycleListener>();
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
    catch ( Exception e ) { throw e; }
  }
  
  @Override
  public Set<EMClient> getAllConnectedClients()
  {    
    return getSimpleClientSet( connectionManager.getCopyOfConnectedClients() );
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
      throw new Exception( "Not in a state ready to start lifecycle" );
    
    if ( connectionManager.getConnectedClientCount() == 0 )
      throw new Exception( "No clients connected to monitor" );
    
    if ( lifecycleManager.isLifecycleActive() )
      throw new Exception( "Lifecycle has already started" );
    
    lifecycleManager.setExperimentInfo( expInfo );
    
    return lifecycleManager.iterateLifecycle();
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
  public EMPhase getNextPhase()
  { return lifecycleManager.getNextActivePhase(); }
  
  @Override
  public boolean isCurrentPhaseActive()
  { return lifecycleManager.isCurrentPhaseActive(); }
  
  @Override
  public void stopCurrentPhase() throws Exception
  {
    if ( lifecycleManager.isWindingCurrentPhaseDown() )
    {
      throw new Exception( "Could not stop as currently winding down phase: " 
                           + lifecycleManager.getCurrentPhase().toString() );
    }
    else
      lifecycleManager.windCurrentPhaseDown();
  }
  
  @Override
  public void goToNextPhase() throws Exception
  {
    if ( lifecycleManager.isWindingCurrentPhaseDown() )
    {
      throw new Exception( "Current winding down phase: " 
                           + lifecycleManager.getCurrentPhase().toString() );
    }
    else
      lifecycleManager.iterateLifecycle();
  }
  
  @Override
  public void endLifecycle() throws Exception
  {
    if ( lifecycleManager.isLifecycleActive() )
      lifecycleManager.endLifecycle();
  }
  
  @Override
  public void resetLifecycle() throws Exception
  {
    // We have an active life-cycle, try stopping it first
    if ( lifecycleManager.isLifecycleActive() )
    {
      isResettingMonitor = true;
      lifecycleManager.endLifecycle();
    }
    else
      resetExperimentMonitor(); // Otherwise, just reset
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
  public void onClientDisconnected( EMClient client )
  {
    connectionManager.disconnectAndRemoveClient( client.getID() );
    
    Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
    
    while ( listIt.hasNext() )
    { listIt.next().onClientDisconnected( client ); }
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
    // If we're currently in the process of resetting, then we're ready to
    // execute a full reset
    if ( isResettingMonitor )
    {
      // Send a de-registration message to clients, but don't wait for a reponse
      Iterator<EMClientEx> cIt = connectionManager.getCopyOfConnectedClients().iterator();
      while ( cIt.hasNext() )
      {
        EMClientEx client = cIt.next();
        try { deregisterClient( client, "ECC is resetting" ); }
        catch ( Exception e )
        { emLogger.error( "Problems de-registering client during reset: " + e.getMessage() ); }
      }
      
      resetExperimentMonitor();
    }
    else // otherwise, announce the end of this phase
    {
      Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
    
      while ( listIt.hasNext() )
      { listIt.next().onLifecyclePhaseCompleted( phase ); }
    }
  }
  
  @Override
  public void onNoFurtherLifecyclePhases()
  {
    Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
    
    while ( listIt.hasNext() )
    { listIt.next().onNoFurtherLifecyclePhases(); }
  }
  
  @Override
  public void onLifecycleReset()
  {
    // This event is not geneated by the Lifecycle Manager, but used internally
    // to signal to EM clients that the reset process has completed.
    Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator(); 
    
    while ( listIt.hasNext() )
    { listIt.next().onLifecycleReset(); }
  }
  
  @Override
  public void onFoundClientWithMetricGenerators( EMClient client )
  {
    Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
    
    while ( listIt.hasNext() )
    { listIt.next().onFoundClientWithMetricGenerators( client ); }
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
    AMQPConnectionFactory amqpCF = new AMQPConnectionFactory();
    
    if ( !amqpCF.setAMQPHostIPAddress(rabbitServerIP) )
      throw new Exception( "Could not set the server IP correctly" );
    
    amqpCF.connectToAMQPHost();
    if ( !amqpCF.isConnectionValid() ) throw new Exception( "Could not connect to Rabbit server" );
    
    amqpChannel = amqpCF.createNewChannel();
    if ( amqpChannel == null ) throw new Exception( "Could not create AMQP channel" );
  }
  
  private void configInitialise( Properties emProps ) throws Exception
  {
    AMQPConnectionFactory amqpCF = new AMQPConnectionFactory();
    try
    {
      amqpCF.connectToAMQPHost( emProps );
      if ( !amqpCF.isConnectionValid() ) throw new Exception( "Could not connect to Rabbit server" );
      
      amqpChannel = amqpCF.createNewChannel();
      if ( amqpChannel == null ) throw new Exception( "Could not create AMQP channel" );
    }
    catch ( Exception e ) { throw e; }
  }
  
  private void initialiseManagers() throws Exception
  {
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
  
  private void resetExperimentMonitor()
  {
    connectionManager.disconnectAllClients();
    lifecycleManager.resetLifecycle();
    
    isResettingMonitor = false;
    
    // Notify EM client of reset completion
    onLifecycleReset();
  }
}
