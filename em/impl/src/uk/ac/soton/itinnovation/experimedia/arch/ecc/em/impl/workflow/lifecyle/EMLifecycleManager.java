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
//      Created By :            sgc
//      Created Date :          13-Aug-2012
//      Created for Project :   experimedia-arch-ecc-em-impl
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecyle;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.IEMLifecycleListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.IEMLiveMonitor;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.EMConnectionManagerListener;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecyle.phases.*;

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
  private final Logger lmLogger = Logger.getLogger( EMLifecycleManager.class );
  
  private AMQPBasicChannel emChannel;
  private UUID             emProviderID;
  
  private EnumMap<EMPhase, AbstractEMLCPhase> lifecyclePhases;
  private EMPhase currentPhase = EMPhase.eEMUnknownPhase;
  private boolean windingCurrPhaseDown = false;
  
  private final Object clientLock = new Object();
  
  private IEMLifecycleListener lifecycleListener;
  
  
  public EMLifecycleManager() 
  {
    lifecyclePhases = new EnumMap<EMPhase, AbstractEMLCPhase>( EMPhase.class );
  }
  
  public void initialise( AMQPBasicChannel channel, 
                          UUID providerID,
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
  { return (currentPhase.getIndex() > 0); }
  
  public EMPhase getCurrentPhase()
  { return currentPhase; }
  
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
      
      IEMLiveMonitor monitor = clientEx.getLiveMonitorInterface();
      if ( monitor == null ) throw new Exception( "Could not get client live monitor interface" );
      
      monitor.pullMetric( measurementSetID );
    }
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
          lifecycleListener.onClientConnected( client );
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
    // If not metric generators are available, remove client from appropriate phases
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
