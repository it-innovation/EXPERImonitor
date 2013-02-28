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
//      Created Date :          16-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces.EMBaseInterface;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;

import java.util.*;





public class EMClientEx extends EMClient
{ 
  private IEMDiscovery      discoveryFace;
  private IEMMetricGenSetup setupFace;
  private IEMLiveMonitor    liveFace;
  private IEMPostReport     postFace;
  private IEMTearDown       tearDownFace;
  
  // Internal Set-up stage support
  private ArrayList<UUID> generatorsToSetup;
  
  // Internal Shared state support
  private UUID currentRequestedMSID;
 
  
  public EMClientEx( UUID id, String name )
  {
    super( id, name );
    
    generatorsToSetup = new ArrayList<UUID>();
  }
  
  public void destroyAllInterfaces()
  {
    EMBaseInterface face = (EMBaseInterface) discoveryFace;
    if ( face != null ) face.shutdown();
    
    face = (EMBaseInterface) setupFace;
    if ( face != null ) face.shutdown();
    
    face = (EMBaseInterface) liveFace;
    if ( face != null ) face.shutdown();
    
    face = (EMBaseInterface) postFace;
    if ( face != null ) face.shutdown();
    
    face = (EMBaseInterface) tearDownFace;
    if ( face != null ) face.shutdown();
    
    discoveryFace = null;
    setupFace     = null;
    liveFace      = null;
    postFace      = null;
    tearDownFace  = null;
  }
  
  public IEMDiscovery getDiscoveryInterface()
  { return discoveryFace; }
  
  public void setDiscoveryInterface( IEMDiscovery face )
  { discoveryFace = face; }
  
  public IEMMetricGenSetup getSetupInterface()
  { return setupFace; }
  
  public void setSetupInterface( IEMMetricGenSetup face )
  { setupFace = face; }
  
  public void setLiveMonitorInterface( IEMLiveMonitor face )
  { liveFace = face; }
  
  public IEMLiveMonitor getLiveMonitorInterface()
  { return liveFace; }
  
  public void setPostReportInterface( IEMPostReport face )
  { postFace = face; }
  
  public IEMPostReport getPostReportInterface()
  { return postFace; }
  
  public IEMTearDown getTearDownInterface()
  { return tearDownFace; }
  
  public void setTearDownInterface( IEMTearDown face )
  { tearDownFace = face; }
  
  public void addTimeOutNotification( EMPhaseTimeOut timeoutSent )
  { timeOutsCalled.add( timeoutSent ); }
  
  // Experiment state ----------------------------------------------------------
  public void setCurrentPhaseActivity( EMPhase phase )
  { currentPhase = phase; }
  
  public void setIsPhaseAccelerating( boolean accelerate )
  { isPhaseAccelerating = accelerate; }
  
  // Discovery phase state -----------------------------------------------------
  public void setIsConnected( boolean connected )
  { isConnected = connected; }
  
  public void setIsDisconnecting( boolean disCon )
  { isDisconnecting = disCon; }
  
  public void setSupportedPhases( EnumSet<EMPhase> phases )
  { supportedPhases = phases; }
  
  public void setGeneratorDiscoveryResult( boolean discovered )
  { discoveredGenerators = discovered; }
  
  public void setMetricGenerators( Set<MetricGenerator> generators )
  {    
    if ( generators != null ) metricGenerators = 
            (HashSet<MetricGenerator>) generators;
    
    generatorsToSetup.clear();
    generatorsSetupOK.clear();
    
    // Copy UUIDs of generators into set-up set
    Iterator<MetricGenerator> genIt = metricGenerators.iterator();
    while ( genIt.hasNext() )
      generatorsToSetup.add( genIt.next().getUUID() );
  }
  
  // Setup phase state ---------------------------------------------------------  
  public boolean hasGeneratorToSetup()
  { return ( !generatorsToSetup.isEmpty() ); }
  
  public UUID getCurrentMetricGenSetupID()
  { return currentMGSetupID; }
  
  public UUID iterateNextMGToSetup()
  { 
    if ( !generatorsToSetup.isEmpty() )
      currentMGSetupID = generatorsToSetup.remove( 0 );
    else
      currentMGSetupID = null;
    
    return currentMGSetupID;
  }
  
  public void addSuccessfulSetup( UUID genID )
  { if ( genID != null ) generatorsSetupOK.add( genID ); }
  
  // Live monitoring phase (and post-reporting) states -------------------------  
  public void setIsPushCapable( boolean push )
  { isPushCapable = push; }
  
  public void setIsPullCapable( boolean pull )
  { isPullCapable = pull; }
  
  public void addPullingMeasurementSetID( UUID msID )
  {
    synchronized ( pullLock )
    { currentMeasurementSetPulls.add( msID ); }
  }
  
  public boolean isCurrentlyPullingMeasurementSetID( UUID msID )
  {
    boolean isPullingMS;
    
    synchronized ( pullLock )
    { isPullingMS = currentMeasurementSetPulls.contains( msID ); }
    
    return isPullingMS;  
  }
  
  public void removePullingMeasurementSetID( UUID msID )
  {
    synchronized ( pullLock )
    { if ( msID != null ) currentMeasurementSetPulls.remove(msID); }
  }
  
  public UUID iterateNextMSForPulling()
  {
    synchronized ( pullLock )
    {
      if ( currentMeasurementSetPulls.isEmpty() )
        currentRequestedMSID = null;
      else
        currentRequestedMSID = currentMeasurementSetPulls.iterator().next();
    }
    
    return currentRequestedMSID;
  }
  
  public void clearAllLiveMeasurementSetPulls()
  {
    synchronized ( pullLock )
    {
      currentMeasurementSetPulls.clear(); 
      currentRequestedMSID = null;
    }
  }
  
  // Post-report phase state ---------------------------------------------------
  public void setPostReportSummary( EMPostReportSummary report )
  { postReportSummary = report; }
  
  public EMDataBatch getCurrentExpectedDataBatch()
  {
    EMDataBatch targetBatch = null;
    
    if ( currentRequestedMSID != null )
      targetBatch = postReportOutstandingBatches.get( currentRequestedMSID );
    
    return targetBatch;
  }
  
  public void addDataForBatching( EMDataBatchEx batch ) throws Exception
  {
    // Safety first
    if ( batch == null ) throw new Exception( "Data batch is NULL" );
    
    // Check we're not already trying to batch this MeasurementSet
    UUID msID = batch.getExpectedMeasurementSetID();
    
    if ( postReportOutstandingBatches.containsKey( msID ) )
      throw new Exception( "Client is already waiting to batch measurement set: " + msID.toString() );
    
    postReportOutstandingBatches.put( msID, batch );
  }
  
  public UUID iterateNextMSForBatching()
  {
    if ( currentRequestedMSID != null )
      postReportOutstandingBatches.remove( currentRequestedMSID );
    
    currentRequestedMSID = null;
    
    if ( !postReportOutstandingBatches.isEmpty() )
    {
      Iterator<UUID> msIDIt = postReportOutstandingBatches.keySet().iterator();
      currentRequestedMSID = msIDIt.next();
    }
    
    return currentRequestedMSID;
  }
  
  public void clearAllBatching()
  {
    postReportOutstandingBatches.clear();
    currentRequestedMSID = null;
  }
  
  // Tear-down phase state -----------------------------------------------------
  public void setTearDownResult( boolean success )
  { tearDownSuccessful = success; }
  
  // Shared phase state --------------------------------------------------------
  public UUID getCurrentRequestedMeasurementSetID()
  { return currentRequestedMSID; }
  
  public void setCurrentRequestedMeasurementSetID( UUID id )
  { currentRequestedMSID = id; }
}
 
