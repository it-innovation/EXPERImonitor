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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.EMConnectionManagerListener;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import java.util.*;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMDataBatchEx;




public class EMLifecycleManager implements EMConnectionManagerListener,
                                           EMGeneratorDiscoveryPhaseListener,
                                           EMMetricGenSetupPhaseListener,
                                           EMLiveMonitorPhaseListener,
                                           EMPostReportPhaseListener,
                                           EMTearDownPhaseListener
{
  private final Logger lmLogger         = Logger.getLogger( EMLifecycleManager.class );
  private final Object clientLock       = new Object();
  private final int    batchRequestSize = 50;
  
  private AMQPBasicChannel emChannel;
  private UUID             emProviderID;
  
  private Experiment                          currentExperiment;
  private EnumMap<EMPhase, AbstractEMLCPhase> lifecyclePhases;
  
  private EMPhase currentPhase         = EMPhase.eEMUnknownPhase;
  private boolean windingCurrPhaseDown = false;
 
  
  
  private IEMLifecycleListener lifecycleListener;
  
  
  public EMLifecycleManager() 
  {
    lifecyclePhases = new EnumMap<EMPhase, AbstractEMLCPhase>( EMPhase.class );
  }
  
  public void initialise( AMQPBasicChannel     channel, 
                          UUID                 providerID,
                          IEMLifecycleListener listener )
  {    
    emChannel         = channel;
    emProviderID      = providerID;
    lifecycleListener = listener;

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
  
  public boolean isLifecycleStarted()
  { return (!currentPhase.equals(EMPhase.eEMUnknownPhase)); }
  
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
  
  public boolean isWindingCurrentPhaseDown()
  { return windingCurrPhaseDown; }
  
  public boolean isCurrentPhaseActive()
  {
    AbstractEMLCPhase currPhase = lifecyclePhases.get( currentPhase );
    
    if ( currPhase != null )
      return currPhase.isActive();
    
    return false;
  }
  
  public EMPhase iterateLifecycle()
  {
    if ( currentPhase != EMPhase.eEMProtocolComplete && !lifecyclePhases.isEmpty() )
    {
      // try to stop current phase
      AbstractEMLCPhase killPhase = lifecyclePhases.get( currentPhase );
      
      if ( killPhase != null && killPhase.isActive() ) 
        killPhase.hardStop();
      
      // Find out what the next phase is
      currentPhase = currentPhase.nextPhase();
      
      // If there are no more phases to execute, notify we're done
      if ( currentPhase.equals(EMPhase.eEMProtocolComplete) )
      {
        lifecycleListener.onNoFurtherLifecyclePhases();
      }
      else // Otherwise, try the next phase
      {
        AbstractEMLCPhase lcPhase = lifecyclePhases.get( currentPhase );
        if ( lcPhase != null )
          try
          { 
            lcPhase.start();

            // Notify listener
            lifecycleListener.onLifecyclePhaseStarted( lcPhase.getPhaseType() );
          }
          catch( Exception e ) 
          {
            String msg = "Could not start lifecycle phase: " + lcPhase.getPhaseType() + "\n"
                       + "Life-cycle exception: " + e.getMessage();

            lmLogger.warn( msg );
            lmLogger.info( "Trying next phase: " + currentPhase.nextPhase() );
            currentPhase = iterateLifecycle();
          }
      }
    }
    
    return currentPhase;
  }
  
  public void windCurrentPhaseDown()
  {
    if ( !windingCurrPhaseDown )
    {
      AbstractEMLCPhase windDownPhase = lifecyclePhases.get( currentPhase );
      if ( windDownPhase != null )
        try 
        {
          windingCurrPhaseDown = true;
          windDownPhase.controlledStop();
        }
        catch ( Exception e )
        {
          windingCurrPhaseDown = false;
          String msg = "Did not wind-down this phase: " + windDownPhase.toString() + e.getMessage();
          lmLogger.info( msg );

          try { windDownPhase.hardStop(); }
          catch ( Exception hs )
          { lmLogger.error( "Could not stop phase " + windDownPhase.toString() ); }
        }
    }
    else
      lmLogger.warn( "Request to wind-down ignored: already winding current phase down" );
  }
  
  public void endLifecycle()
  {
    currentPhase = EMPhase.eEMUnknownPhase;
  }
  
  public void tryPullMetric( EMClient client, UUID measurementSetID ) throws Exception
  {
    // Safety first
    if ( client == null || measurementSetID == null ) throw new Exception( "Pull parameters are invalid" );
    if ( currentPhase != EMPhase.eEMLiveMonitoring )  throw new Exception( "Not in metric pulling compatible phase" );
    if ( client.isPullingMetricData() )               throw new Exception( "Client is already being pulled for data" );
    
    synchronized ( clientLock )
    {
      EMClientEx clientEx = (EMClientEx) client;
      if ( clientEx == null ) throw new Exception( "Client is invalid" );

      clientEx.addPullingMeasurementSetID( measurementSetID );
      clientEx.getLiveMonitorInterface().pullMetric( measurementSetID );
    }
  }
  
  public void tryPullAllMetrics( EMClient client ) throws Exception
  {
    // Safety first
    if ( currentPhase != EMPhase.eEMLiveMonitoring )  throw new Exception( "Not in metric pulling compatible phase" );
    if ( client.isPullingMetricData() )               throw new Exception( "Client is already being pulled for data" );
    
    // Get all MeasurementSets available
    Set<MeasurementSet> targetMSets = 
            MetricHelper.getAllMeasurementSets( client.getCopyOfMetricGenerators() );
    
    // If we don't have any MeasurementSets, then back out
    if ( targetMSets.isEmpty() )
      throw new Exception( "Client (apparently) has no measurement sets to pull!" );
    else
    {
      synchronized ( clientLock ) // Otherwise, collect, stack and post request for the first one
      {
        EMClientEx clientEx = (EMClientEx) client;
        if ( clientEx == null ) throw new Exception( "Client is invalid" );
        
        Iterator<MeasurementSet> msIt = targetMSets.iterator();
        UUID lastMSID = null;
        
        while ( msIt.hasNext() )
        {
          lastMSID = msIt.next().getUUID();
          clientEx.addPullingMeasurementSetID( lastMSID ); 
        }
        
        clientEx.getLiveMonitorInterface().pullMetric( lastMSID );
      }
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
        EMDataBatchEx newBatch = new EMDataBatchEx( report.getMeasurementSet().getUUID(),
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
      clientEx.iterateNextMSForBatching();
      EMDataBatch firstBatch = clientEx.getCurrentDataBatch();
      
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
  public void onClientRegistered( EMClientEx client )
  {
    // Attach client to GeneratorDiscoveryPhase
    if ( client          != null && 
         lifecyclePhases != null &&
         currentPhase    == EMPhase.eEMUnknownPhase ) // Only register client if we've not started a life-cycle
    {
      AbstractEMLCPhase discovery = 
          lifecyclePhases.get( EMPhase.eEMDiscoverMetricGenerators );
      
      if ( discovery != null )
        if ( discovery.addClient( client ) )
        {
          client.setIsConnected( true );
          lifecycleListener.onClientConnected( client );
        }
    }
  }
  
  // GeneratorDiscoveryPhaseListener -------------------------------------------
  @Override
  public void onClientPhaseSupportReceived( EMClientEx client )
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
  public void onClientMetricGeneratorsFound( EMClientEx client )
  {
    lifecycleListener.onFoundClientWithMetricGenerators( client );
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
      if ( cleanPhase != null ) cleanPhase.onClientUnexpectedlyRemoved( client );
      
      // Go to the next phase
      nextPhase = nextPhase.nextPhase();
    }
    
    // Next, remove the client from the current phase
    AbstractEMLCPhase thisPhase = lifecyclePhases.get( currentPhase );
    if ( thisPhase != null ) thisPhase.onClientUnexpectedlyRemoved( client );
    
    // Finally, notify listener of this disconnection
    client.setIsConnected( false );
    lifecycleListener.onClientDisconnected( client );
  }
  
  @Override
  public void onDiscoveryPhaseCompleted()
  {
    windingCurrPhaseDown = false;
    lifecycleListener.onLifecyclePhaseCompleted( EMPhase.eEMDiscoverMetricGenerators );
  }
  
  // EMNMetricGenSetupPhaseListener --------------------------------------------
  @Override
  public void onMetricGenSetupResult( EMClient client, boolean success )
  {
    lifecycleListener.onClientSetupResult( client, success );
  }
  
  @Override
  public void onSetupPhaseCompleted()
  {
    windingCurrPhaseDown = false;
    lifecycleListener.onLifecyclePhaseCompleted( EMPhase.eEMSetUpMetricGenerators );
  }
  
  // EMLiveMonitorPhaseListener ------------------------------------------------
  @Override
  public void onGotMetricData( EMClientEx client, Report report )
  {
    lifecycleListener.onGotMetricData( client, report );
  }
  
  @Override
  public void onLiveMonitorPhaseCompleted()
  {
    windingCurrPhaseDown = false;
    lifecycleListener.onLifecyclePhaseCompleted( EMPhase.eEMLiveMonitoring );
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
  public void onPostReportPhaseCompleted()
  {
    windingCurrPhaseDown = false;
    lifecycleListener.onLifecyclePhaseCompleted( EMPhase.eEMPostMonitoringReport );
  }
  
  // EMTearDownPhaseListener ---------------------------------------------------
  @Override
  public void onClientTearDownResult( EMClientEx client, boolean success )
  {
    lifecycleListener.onClientTearDownResult( client, success );
  }

  @Override
  public void onTearDownPhaseCompleted()
  {
    windingCurrPhaseDown = false;
    lifecycleListener.onLifecyclePhaseCompleted( EMPhase.eEMTearDown );
  }
}
