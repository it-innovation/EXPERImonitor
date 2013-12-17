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
//      Created for Project :   experimedia-arch-ecc-em-impl
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecylePhases;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.IEMLifecycleListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.IEMDiscovery;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.EMConnectionManagerListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvReport;

import java.util.*;




public class EMLifecycleManager implements EMConnectionManagerListener,
                                           EMGeneratorDiscoveryPhaseListener,
                                           EMMetricGenSetupPhaseListener,
                                           EMLiveMonitorPhaseListener,
                                           EMPostReportPhaseListener,
                                           EMTearDownPhaseListener
{
  private final IECCLogger lmLogger         = Logger.getLogger( EMLifecycleManager.class );
  private final Object     clientLock       = new Object();
  private final int        batchRequestSize = 50;
  
  private AMQPBasicChannel emChannel;
  private UUID             emProviderID;
  
  private Experiment                          currentExperiment;
  private EnumMap<EMPhase, AbstractEMLCPhase> lifecyclePhases;
  private HashMap<UUID, EMClientEx>           phaseDeferredClients; // Clients waiting to engage in a future phase
  
  private EMPhase currentPhase = EMPhase.eEMUnknownPhase;
 
  private IEMLifecycleListener lifecycleListener;
  
  
  public EMLifecycleManager() 
  {
    lifecyclePhases      = new EnumMap<EMPhase, AbstractEMLCPhase>( EMPhase.class );
    phaseDeferredClients = new HashMap<UUID, EMClientEx>();
  }
  
  public void initialise( AMQPBasicChannel     channel, 
                          UUID                 providerID,
                          IEMLifecycleListener listener )
  {    
    emChannel         = channel;
    emProviderID      = providerID;
    lifecycleListener = listener;
    
    lifecyclePhases.clear();
    phaseDeferredClients.clear();

    // Create life-cycle phases
    EMGeneratorDiscoveryPhase gdp = new EMGeneratorDiscoveryPhase( emChannel,
                                                                   emProviderID,
                                                                   this );
    
    lifecyclePhases.put( EMPhase.eEMDiscoverMetricGenerators, gdp );
    
    EMMetricGenSetupPhase sup = new EMMetricGenSetupPhase( emChannel, emProviderID, this );
    lifecyclePhases.put( EMPhase.eEMSetUpMetricGenerators, sup );
    
    EMLiveMonitorPhase lmp = new EMLiveMonitorPhase( emChannel, emProviderID, this );
    lifecyclePhases.put( EMPhase.eEMLiveMonitoring, lmp );
    
    EMPostReportPhase prp = new EMPostReportPhase( emChannel, emProviderID, this );
    lifecyclePhases.put( EMPhase.eEMPostMonitoringReport, prp );
    
    EMTearDownPhase tdp = new EMTearDownPhase( emChannel, emProviderID, this );
    lifecyclePhases.put( EMPhase.eEMTearDown, tdp );
  }
  
  public void shutdown()
  {
    Iterator<AbstractEMLCPhase> phaseIt = lifecyclePhases.values().iterator();
    while ( phaseIt.hasNext() )
    { phaseIt.next().hardStop(); }
    
    lifecyclePhases.clear();
  }
  
  public void resetLifecycle()
  {
    // Clear all phases of all clients
    synchronized ( clientLock )
    {
      Iterator<AbstractEMLCPhase> phaseIt = lifecyclePhases.values().iterator();
      while ( phaseIt.hasNext() )
      {
        AbstractEMLCPhase phase = phaseIt.next();
        phase.reset();
      }
    }
    
    phaseDeferredClients.clear();
    
    currentExperiment = null;
    currentPhase      = EMPhase.eEMUnknownPhase;
  }
  
  public boolean isLifecycleActive()
  { 
    return ( !currentPhase.equals(EMPhase.eEMUnknownPhase) &&
             !currentPhase.equals(EMPhase.eEMProtocolComplete) ); 
  }
  
  public void setExperimentInfo( Experiment expInfo ) throws Exception
  { 
    currentExperiment = expInfo;
    
    EMGeneratorDiscoveryPhase dp = (EMGeneratorDiscoveryPhase) lifecyclePhases.get( EMPhase.eEMDiscoverMetricGenerators );
    
    try { dp.setExperimentInfo( expInfo ); }
    catch ( Exception e ) { throw e; }
  }
  
  public Experiment getExperimentInfo()
  { return currentExperiment; }
  
  public EMPhase getCurrentPhase()
  { return currentPhase; }
    
  public Set<EMClientEx> getCopySetOfCurrentPhaseClients()
  {
    Set<EMClientEx> clients = new HashSet<EMClientEx>();
    
    AbstractEMLCPhase currPhase = lifecyclePhases.get( currentPhase );
    if ( currPhase != null )
      clients = currPhase.getCopySetOfCurrentClients();
    
    return clients;
  }
  
  public boolean isCurrentPhaseActive()
  {
    AbstractEMLCPhase currPhase = lifecyclePhases.get( currentPhase );
    
    if ( currPhase != null )
      return currPhase.isActive();
    
    return false;
  }
  
  public void startLifeCycleAt( EMPhase startPhase ) throws Exception
  {
    if ( startPhase == EMPhase.eEMUnknownPhase || startPhase == EMPhase.eEMProtocolComplete )
      throw new Exception( "Cannot start lifecycle at: " + startPhase.toString() );
    
    // Run through all previous phases, making sure they are active
    EMPhase phaseStep = EMPhase.eEMDiscoverMetricGenerators;
    
    while ( !phaseStep.equals(phaseStep, startPhase) )
    {
      String startError = tryStartPhase( phaseStep );
      
      if ( startError == null )
        lmLogger.info( "Started phase: " + phaseStep.name() );
      else
        lmLogger.warn( lmLogger );
      
      // Move forward to next phase
      phaseStep = phaseStep.nextPhase();
    }
    
    // Then start the target phase
    currentPhase      = phaseStep;
    String startError = tryStartPhase( currentPhase );
      
    if ( startError == null )
    {
      lmLogger.info( "Started phase: " + currentPhase.name() );
      
      // Notify listener of target phase
      lifecycleListener.onLifecyclePhaseStarted( currentPhase );
    }
    else
      lmLogger.warn( lmLogger );
  }
  
  public EMPhase iterateLifecycle() throws Exception
  {
    if ( currentPhase != EMPhase.eEMProtocolComplete && !lifecyclePhases.isEmpty() )
    {         
      EMPhase nextPhase = currentPhase.nextPhase();
      
      // If there are no more phases to execute, notify we're done
      if ( nextPhase.equals(EMPhase.eEMProtocolComplete) )
      {
        lifecycleListener.onNoFurtherLifecyclePhases();
      }
      else // Otherwise, try the next phase
      {
        // Before starting the next phase, see if there are any deferred clients
        // waiting to be added in
        tryDeferredClientsForRemaingPhases();
        
        // Try starting the next phase
        String startError = tryStartPhase( nextPhase );
        if ( startError == null )
        {
          lmLogger.info( "Started phase: " + currentPhase.toString() );

          // Notify listener
          lifecycleListener.onLifecyclePhaseStarted( currentPhase );
        }
        else 
          throw new Exception( startError );
      }
    }
    
    return currentPhase;
  }
    
  public void endLifecycle()
  {
    // Clean up deferred clients
    phaseDeferredClients.clear();
    
    // Tidy up current phase, if we have one in progress
    if ( isLifecycleActive() )
    {
      for ( AbstractEMLCPhase phase : lifecyclePhases.values() )
      {
        boolean phaseInFocus = phase.isInFocus();
        
        phase.controlledStop();
        
        if ( phaseInFocus )
          lifecycleListener.onLifecyclePhaseCompleted( phase.getPhaseType() );
      }
    }
    
    // Set end of lifecycle status
    currentPhase = EMPhase.eEMProtocolComplete;
  }
  
  public void tryPullMetric( EMClient client, UUID measurementSetID ) throws Exception
  {
    // Safety first
    EMClientEx clientEx = (EMClientEx) client;
    
    if ( clientEx == null || measurementSetID == null )             throw new Exception( "Pull parameters are invalid" );
    if ( currentPhase != EMPhase.eEMLiveMonitoring )                throw new Exception( "Not in metric pulling compatible phase" );
    if ( clientEx.isMeasurementSetQueuedForPull(measurementSetID) ) throw new Exception( "Measurement set already queued for this client" );
    
    if ( clientEx.validateMSReadyForPull(measurementSetID) )
    {
      clientEx.addPullingMeasurementSetID( measurementSetID );
      clientEx.getLiveMonitorInterface().pullMetric( clientEx.iterateNextValidMSToBePulled() );
    }
    else throw new Exception( "MeasurementSet "  + measurementSetID.toString() +
                              "was not ready for pulling" );
  }
  
  public void tryPullAllMetrics( EMClient client ) throws Exception
  {
    // Safety first
    if ( currentPhase != EMPhase.eEMLiveMonitoring )  throw new Exception( "Not in metric pulling compatible phase" );
    
    EMClientEx clientEx = (EMClientEx) client;
    if ( clientEx == null )               throw new Exception( "Client is invalid" );
    if ( clientEx.isPullingMetricData() ) throw new Exception( "Client is already being pulled for data" );
    
    // Get all MeasurementSets available
    Map<UUID, MeasurementSet> targetMSets = 
            MetricHelper.getAllMeasurementSets( client.getCopyOfMetricGenerators() );
    
    // If we don't have any MeasurementSets, then back out
    if ( targetMSets.isEmpty() )
      throw new Exception( "Client (apparently) has no measurement sets to pull!" );
    else
    {
      Iterator<MeasurementSet> msIt = targetMSets.values().iterator();
      while ( msIt.hasNext() )
      { clientEx.addPullingMeasurementSetID( msIt.next().getID() ); }
      
      // Try to find a valid first measurement set to pull
      UUID nextMS = clientEx.iterateNextValidMSToBePulled();
      
      if ( nextMS != null )
        clientEx.getLiveMonitorInterface().pullMetric( nextMS );
      else
        throw new Exception( "Could not find any measurements ready for pulling" ); 
    }
  }
  
  public void tryRequestDataBatch( EMClient client, UUID measurementSetID ) throws Exception
  {
    // Safety first
    if ( currentPhase != EMPhase.eEMPostMonitoringReport )
      throw new Exception( "Not in data batch requesting compatible phase" );
    
    if ( client.isCreatingPostReportBatchData() ) throw new Exception( "Client is already busy creating a post-report data batch" );    
    if ( client == null || measurementSetID == null ) throw new Exception( "One or more batch request parameters are NULL" );
    
    EMPostReportSummary summary = client.getPostReportSummary();
    if ( summary == null ) throw new Exception( "Cannot automate this measurement set batch: client has not produced a post-report summary" );
    
    Report targetReport = summary.getReport( measurementSetID );
    if ( targetReport == null ) throw new Exception( "Cannot get correct report for this batch request" );
    
    // Create a new batch for this MeasurementSet
    EMDataBatchEx newBatch = new EMDataBatchEx( measurementSetID,
                                                targetReport.getFromDate(),
                                                batchRequestSize );
    
    // And request it
    synchronized ( clientLock )
    {
      EMClientEx clientEx = (EMClientEx) client;
      
      try
      {
        clientEx.addDataForBatching( newBatch );
        clientEx.iterateNextMSForBatching();
        clientEx.getPostReportInterface().requestDataBatch( newBatch );
      }
      catch (Exception e)
      { lmLogger.error( "Could not request data batch from client: " + e.getMessage() ); }
    }
  }
  
  public void tryGetAllDataBatches( EMClient client ) throws Exception
  {
    // Safety first
    if ( currentPhase != EMPhase.eEMPostMonitoringReport )
      throw new Exception( "Not in data batch requesting compatible phase" );
    
    if ( client == null ) throw new Exception( "Client is null" );
    if ( client.isCreatingPostReportBatchData() ) throw new Exception( "Client is busy creating a post-report data batch" );
    
    // Create a set of batches (one per MeasurementSet) that we want
    EMPostReportSummary summary = client.getPostReportSummary();
    if ( summary == null ) throw new Exception( "Cannot automatically batch: client has not produced a post-report summary" );
    
    HashSet<EMDataBatchEx> targetBatches = new HashSet<EMDataBatchEx>();
    
    Iterator<UUID> msIDIt = summary.getReportedMeasurementSetIDs().iterator();
    while ( msIDIt.hasNext() )
    {
      // If we have a valid report (with some potential data pending),
      // then create an 'initial' batch
      Report report = summary.getReport( msIDIt.next() );
      if ( report != null && report.getNumberOfMeasurements() > 0 )
      {
        EMDataBatchEx newBatch = new EMDataBatchEx( report.getMeasurementSet().getID(),
                                                    report.getFromDate(),
                                                    batchRequestSize );
        
        targetBatches.add( newBatch );
      }
    }
    
    // Duck out early if there's nothing to retrieve
    if ( targetBatches.isEmpty() ) throw new Exception( "Client reports that there is no data to request" );
    Iterator<EMDataBatchEx> batchIt = targetBatches.iterator();
    
    // Otherwise, load up all the batches, and start one off
    synchronized( clientLock )
    {
      EMClientEx clientEx = (EMClientEx) client;
        
      while ( batchIt.hasNext() )
      {
        // Load up all the batches possible
        try
        { clientEx.addDataForBatching( batchIt.next() ); }
        catch ( Exception e )
        { lmLogger.error("Could not add batch request to client: " + e.getMessage()); }
      }
      
      // Start a batch
      clientEx.clearAllLiveMeasurementSetPulls();
      clientEx.iterateNextMSForBatching();
      EMDataBatch firstBatch = clientEx.getCurrentExpectedDataBatch();
      
      if ( firstBatch != null )
        clientEx.getPostReportInterface().requestDataBatch( firstBatch );
      else
        lmLogger.error( "Expected first client data batch is NULL" );
    }
  }
  
  public void tryClientTimeOut( EMClient client ) throws Exception
  {
    if ( client == null ) throw new Exception( "Client is null" );
    if ( currentPhase == EMPhase.eEMUnknownPhase ) throw new Exception( "Cannot time-out in unknown phase" );
    
    // Check phase support and that the client
    AbstractEMLCPhase thisPhase = lifecyclePhases.get( currentPhase );
    if ( !thisPhase.isTimeOutSupported() ) throw new Exception( "Current phase does not time-outs" );
    
    // Try the time-out
    try { thisPhase.timeOutClient( (EMClientEx) client ); }
    catch ( Exception e ) { throw e; }
  }
  
  // EMConnectionManagerListener -----------------------------------------------
  @Override
  public void onClientRegistered( EMClientEx client, boolean reconnected )
  {
    // Check client and phases are ok
    if ( client != null ) 
    {
      client.setIsConnected( true );
      lifecycleListener.onClientConnected( client, reconnected );
          
      // If the experiment (proper has not started, just add the client)
      if ( currentPhase == EMPhase.eEMUnknownPhase )
      {
        AbstractEMLCPhase discovery = 
          lifecyclePhases.get( EMPhase.eEMDiscoverMetricGenerators );

        if ( !discovery.addClient( client ) )
          lmLogger.error( "Could not add client to discovery phase (at connection time)" );
      }
      // If the experiment process has already started, flag to accelerate the client
      // through phases already completed so it can catch up
      else if ( currentPhase != EMPhase.eEMProtocolComplete )
      {
        // We'll need to do the preliminary acknowledgements before accelerating
        // if this client is already known to us
        if ( reconnected )
        {
          AbstractEMLCPhase discovery = 
                  lifecyclePhases.get( EMPhase.eEMDiscoverMetricGenerators );
          
          discovery.setupClientInterface( client );
          
          IEMDiscovery monFace = client.getDiscoveryInterface();
          monFace.registrationConfirmed( true,
                                         currentExperiment.getUUID(),
                                         currentExperiment.getExperimentID(),
                                         currentExperiment.getName(),
                                         currentExperiment.getDescription(),
                                         currentExperiment.getStartTime() );
        }
        
        client.setIsPhaseAccelerating( true );
        advanceClientPhase( client );
      }
    }
    else lmLogger.error( "Could not register client; client is null" );
  }
  
  // GeneratorDiscoveryPhaseListener -------------------------------------------
  @Override
  public void onClientPhaseSupportReceived( EMClientEx client )
  {
    // Do not try to add accelerating clients into 'future' phases - the experiment
    // may already be in one of these but the client will not be ready for it.
    if ( !client.isPhaseAccelerating() )
    {
      EnumSet<EMPhase> clientPhases = client.getCopyOfSupportedPhases();
      Iterator<EMPhase> phaseIt = clientPhases.iterator();

      while ( phaseIt.hasNext() )
      {
        AbstractEMLCPhase phase = lifecyclePhases.get( phaseIt.next() );
        if ( phase != null )
          phase.addClient( client );
      }
    }
  }
  
  @Override
  public void onClientDiscoveryResult( EMClientEx client )
  {
    // If no metric generators are available, remove client from appropriate phases
    if ( client.getGeneratorDiscoveryResult() == false )
    {
      UUID clientID = client.getID();
      
      AbstractEMLCPhase phase = lifecyclePhases.get( EMPhase.eEMLiveMonitoring );
      phase.removeClient( clientID );
      
      phase = lifecyclePhases.get( EMPhase.eEMPostMonitoringReport );
      phase.removeClient( clientID );
    }
  }
  
  @Override
  public void onClientMetricGeneratorsFound( EMClientEx client, Set<MetricGenerator> newGens )
  {
    lifecycleListener.onFoundClientWithMetricGenerators( client, newGens );
  }
  
  @Override
  public void onClientEnabledMetricCollection( EMClientEx client, UUID entityID, boolean enabled )
  {
    lifecycleListener.onClientEnabledMetricCollection( client, entityID, enabled );
  }
  
  @Override
  public void onClientIsDisconnected( EMClientEx client )
  {
    // Must carefully remove client references from active and future phases...
    // ... remove client from future phases first
    EMPhase nextPhase = currentPhase.nextPhase();
    
    while ( !nextPhase.equals(EMPhase.eEMUnknownPhase) )
    {
      AbstractEMLCPhase cleanPhase = lifecyclePhases.get( nextPhase );
      
      // Remove the client from this phase
      if ( cleanPhase != null ) cleanPhase.onClientHasBeenDeregistered( client );
      
      // Go to the next phase
      nextPhase = nextPhase.nextPhase();
    }
    
    // Next, remove the client from the current phase
    AbstractEMLCPhase thisPhase = lifecyclePhases.get( currentPhase );
    if ( thisPhase != null ) thisPhase.onClientHasBeenDeregistered( client );
    
    // And from the deferred client list (may or may not be in there)
    phaseDeferredClients.remove( client.getID() );
    
    // Finally, notify listener of this disconnection
    lifecycleListener.onClientDisconnected( client );
  }
  
  @Override
  public void onDiscoveryPhaseCompleted( EMClientEx client )
  {
    if ( client.isPhaseAccelerating() )
      advanceClientPhase( client );
  }
  
  // EMNMetricGenSetupPhaseListener --------------------------------------------
  @Override
  public void onMetricGenSetupResult( EMClientEx client, boolean success )
  {
    lifecycleListener.onClientSetupResult( client, success );
  }
  
  @Override
  public void onSetupPhaseCompleted( EMClientEx client )
  {
    if ( client.isPhaseAccelerating() )
      advanceClientPhase( client );
  }
    
  // EMLiveMonitorPhaseListener ------------------------------------------------
  @Override
  public void onClientDeclaredCanPush( EMClientEx client )
  {
    lifecycleListener.onClientDeclaredCanPush( client );
  }
  
  @Override
  public void onClientDeclaredCanBePulled( EMClientEx client )
  {
    lifecycleListener.onClientDeclaredCanBePulled( client );
  }
  
  @Override
  public void onGotMetricData( EMClientEx client, Report report )
  {
    lifecycleListener.onGotMetricData( client, report );
  }
  
  @Override
  public void onGotPROVData( EMClientEx client, EDMProvReport statement )
  {
    lifecycleListener.onGotPROVData( client, statement );
  }
  
  @Override
  public void onLiveMonitorPhaseCompleted( EMClientEx client )
  {
    if ( client.isPhaseAccelerating() )
      advanceClientPhase( client );
  }
 
  // EMPostReportPhaseListener -------------------------------------------------
  @Override
  public void onGotSummaryReport( EMClientEx client, EMPostReportSummary summary )
  {
    lifecycleListener.onGotSummaryReport( client, summary );
  }
  
  @Override
  public void onGotDataBatch( EMClientEx client, EMDataBatch batch )       
  {
    lifecycleListener.onGotDataBatch( client, batch );
  }
  
  @Override
  public void onDataBatchMeasurementSetCompleted( EMClientEx client,
                                                  UUID measurementSetID )
  {
    lifecycleListener.onDataBatchMeasurementSetCompleted( client, measurementSetID );
  }
  
  @Override
  public void onAllDataBatchesRequestComplete( EMClientEx client )
  {
    lifecycleListener.onAllDataBatchesRequestComplete( client );
  }
  
  @Override
  public void onPostReportPhaseCompleted( EMClientEx client )
  {
    if ( client.isPhaseAccelerating() )
      advanceClientPhase( client );
  }
    
  // EMTearDownPhaseListener ---------------------------------------------------
  @Override
  public void onClientTearDownResult( EMClientEx client, boolean success )
  {
    lifecycleListener.onClientTearDownResult( client, success );
  }

  @Override
  public void onTearDownPhaseCompleted( EMClientEx client )
  {
    // We don't actually need to accelerate the client any further here
  }
  
  // Private methods -----------------------------------------------------------  
  private String tryStartPhase( EMPhase target )
  {
    String startError = null;
    
    // First remove focus from previous phase
    AbstractEMLCPhase lcPhase = lifecyclePhases.get( currentPhase );
    if ( lcPhase != null && lcPhase.isActive() )
      lcPhase.setInFocus( false );
    
    // Now start & focus the next phase
    lcPhase = lifecyclePhases.get( target );
    if ( lcPhase != null && !lcPhase.isActive() )
    {
      try
      {
        lcPhase.start();
        lcPhase.setInFocus( true );
        
        currentPhase = target;
      }
      catch ( Exception ex )
      {
        startError = "Could not start lifecycle phase: " + lcPhase.getPhaseType() + "\n"
                   + "Life-cycle exception: " + ex.getMessage();
      }
    }
    else startError = "Lifecycle phase : " + target.name() + " is not available to start";
    
    return startError;
  }
  
  private void advanceClientPhase( EMClientEx client )
  {
    EMPhase targetClientPhase = client.getCurrentPhaseActivity();
    boolean deferClient       = true;
        
    // If client is behind the current phase, then try accelerating to the next
    // phase they support (before the current, active phase)
    if ( targetClientPhase.compareTo(currentPhase) < 0 )
    {
      targetClientPhase = targetClientPhase.nextPhase();
      
      while ( targetClientPhase != EMPhase.eEMProtocolComplete )
      {
        // Found a phase the client supports, so stop searching
        if ( client.supportsPhase(targetClientPhase) ) { deferClient = false; break; }
        
        // We've got the current phase (client doesn't support it, so defer)
        if ( targetClientPhase == currentPhase ) break;
        
        // Advance to next phase to test
        targetClientPhase = targetClientPhase.nextPhase();
      }
    }
    else deferClient = false; // Client is already aligned with current phase
                              // So do not defer
    
    // Defer the client for later phases if we can't accelerate it yet
    if ( deferClient )
    {
      UUID id = client.getID();
      
      if ( !phaseDeferredClients.containsKey(id) )
        phaseDeferredClients.put( id, client );
    }
    else // Otherwise either accelerate or de-accelerate the client
    {
      // If the next phase for this client is the current one, de-accelerate
      if ( targetClientPhase == currentPhase )
      {
        lifecycleListener.onClientStartedPhase( client, currentPhase );
        deaccelerateClient( client );
      }
      else
      {
        // Otherwise, push them on
        lifecycleListener.onClientStartedPhase( client, targetClientPhase );
        accelerateClient( client, targetClientPhase ); 
      }
    }
  }
  
  private void accelerateClient( EMClientEx client, EMPhase nextPhase )
  {
    AbstractEMLCPhase accPhase = lifecyclePhases.get( nextPhase );
    String accelProblem = null;
    
    if ( accPhase != null )
    {
      try
      { 
        accPhase.accelerateClient( client ); 
      }
      catch ( Exception e ) { accelProblem = "Phase could not accelerate client" + e.getMessage(); }
    }
    else
      accelProblem = "Could not find phase to accelerate to!";

    // Report acceleration problems
    if ( accelProblem != null )
      lmLogger.error( "Could not acclerate client " + client.getName() +
                      "into phase " + nextPhase.name() );
  }
  
  private void deaccelerateClient( EMClientEx client )
  {
    // Add the client to the remaining phases which it supports (need to accelerate
    // into current one as it may have already started)
    addClientToRemainingPhases( client, false );
    
    // Formally de-accelerate the client...
    client.setIsPhaseAccelerating( false );
    
    // ... and accelerate them (for the last time) into the current phase
    accelerateClient( client, currentPhase );    
  }
  
  private void addClientToRemainingPhases( EMClientEx client, boolean addToCurrent )
  {
    // Add client into the remaining phases for the experiment (possibly including the current one)
    EMPhase trailPhase = addToCurrent ? currentPhase : currentPhase.nextPhase();
    
    while ( trailPhase != EMPhase.eEMProtocolComplete )
    {
      // Add client to phase if it supports it
      if ( client.supportsPhase(trailPhase) )
      {
        AbstractEMLCPhase phase = lifecyclePhases.get( trailPhase );
    
        if ( phase != null )
          phase.addClient( client );
      }
      
      trailPhase = trailPhase.nextPhase();
    }
  }
  
  private void tryDeferredClientsForRemaingPhases()
  {
    // Try engaging deferred clients into the current phase
    Iterator<EMClientEx> clientIt = phaseDeferredClients.values().iterator();
    while ( clientIt.hasNext() )
    {
      EMClientEx client = clientIt.next();
      
      if ( client.supportsPhase(currentPhase) )
      {
        // Pull them out of a deferred, accelerated state
        phaseDeferredClients.remove( client.getID() );
        client.setIsPhaseAccelerating( false );
        
        // Reinsert client into remaining phases (including the current one; unstarted)
        addClientToRemainingPhases( client, true );
      }
    }
  }
}
