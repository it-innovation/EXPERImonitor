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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.EMConnectionManagerListener;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;

import java.util.*;
import org.apache.log4j.Logger;





public class EMLifecycleManager implements EMConnectionManagerListener,
                                           EMGeneratorDiscoveryPhaseListener,
                                           EMMetricGenSetupPhaseListener,
                                           EMLiveMonitorPhaseListener,
                                           EMPostReportPhaseListener,
                                           EMTearDownPhaseListener
{
  private final Logger lmLogger   = Logger.getLogger( EMLifecycleManager.class );
  private final Object clientLock = new Object();
  
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
      // Stop current phase
      AbstractEMLCPhase killPhase = lifecyclePhases.get( currentPhase );
      
      if ( killPhase != null ) killPhase.hardStop();
      
      currentPhase = currentPhase.nextPhase();
      
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
          String errorMsg = "Could not start lifecycle phase: " + lcPhase.getPhaseType() + "\n";
          errorMsg       += "Life-cycle exception: " + e.getMessage();
          
          lmLogger.error( errorMsg );
        }
    }
    
    return currentPhase;
  }
  
  public void windCurrentPhaseDown()
  {
    AbstractEMLCPhase windDownPhase = lifecyclePhases.get( currentPhase );
    windingCurrPhaseDown            = false;
    
    if ( windDownPhase != null )
      try 
      { 
        windDownPhase.controlledStop();
        windingCurrPhaseDown = true;
      }
      catch ( Exception e )
      {
        String msg = "Did not wind-down this phase: " + windDownPhase.toString() + e.getMessage();
        lmLogger.info( msg );
        
        try { windDownPhase.hardStop(); }
        catch ( Exception hs )
        { lmLogger.error( "Could not stop phase " + windDownPhase.toString() ); }
      }
  }
  
  public void endLifecycle()
  {
    currentPhase = EMPhase.eEMUnknownPhase;
    
    //TODO: Tidy up
  }
  
  public void tryPullMetric( EMClient client, UUID measurementSetID ) throws Exception
  {
    if ( client == null || measurementSetID == null ) throw new Exception( "Pull parameters are invalid" );
    if ( currentPhase != EMPhase.eEMLiveMonitoring ) throw new Exception( "Not in metric pulling compatible phase" );
      
    synchronized ( clientLock )
    {
      EMClientEx clientEx = (EMClientEx) client;
      if ( clientEx == null ) throw new Exception( "Client is invalid" );
      
      // Don't try pulling if we've already made a request
      if ( clientEx.isPullingMetricData() ) throw new Exception( "Still waiting for pull metric data from client" );
      
      IEMLiveMonitor monitor = clientEx.getLiveMonitorInterface();
      if ( monitor == null ) throw new Exception( "Could not get client live monitor interface" );
      
      clientEx.setPullingMeasurementSetID( measurementSetID );
      monitor.pullMetric( measurementSetID );
    }
  }
  
  public void tryRequestDataBatch( EMClient client, EMDataBatch batch ) throws Exception
  {
    if ( currentPhase != EMPhase.eEMPostMonitoringReport )
      throw new Exception( "Not in data batch requesting compatible phase" );
    
    if ( client == null ) throw new Exception( "Client is null" );
    if ( batch  == null ) throw new Exception( "Batch is null" );
    if ( batch.getMeasurementSet() == null ) throw new Exception( "Batch Measurement set is null" );
    if ( batch.getDataStart() == null ||
         batch.getDataEnd()   == null ) throw new Exception( "Batch date stamps are null" );
    
    synchronized ( clientLock )
    {
      EMClientEx clientEx = (EMClientEx) client;
      if ( clientEx == null ) throw new Exception( "Client is invalid" );
      
      IEMPostReport postReport = clientEx.getPostReportInterface();
      if ( postReport == null ) throw new Exception( "Could not get client post report interface" );
      
      clientEx.setCurrentPostReportBatchID( batch.getID() );
      postReport.requestDataBatch( batch );
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
