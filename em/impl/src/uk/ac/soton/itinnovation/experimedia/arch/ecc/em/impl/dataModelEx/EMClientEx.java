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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import java.util.*;





public class EMClientEx extends EMClient
{
  private final Object pullLock = new Object();
 
  private IEMDiscovery      discoveryFace;
  private IEMMetricGenSetup setupFace;
  private IEMLiveMonitor    liveFace;
  private IEMPostReport     postFace;
  private IEMTearDown       tearDownFace;
  
  // Internal Set-up stage support
	private HashMap<UUID, MetricGenerator> historicMetricGenerators;
	
  private ArrayList<UUID> generatorsToSetup;
  
  // Internal Monitoring state support
  private Map<UUID, EMMeasurementSetInfo> msSetInfoCache;
  private HashSet<UUID>                   currentMeasurementSetPulls;
  private LinkedList<UUID>                orderedCurrentMeasurementSetPulls;
  private UUID                            expectedPullMSID;
  
  // Internal Post Report state support
  private UUID currentRequestedBatchMSID;
 
  
  public EMClientEx( UUID id, String name )
  {
    super( id, name );
    
		historicMetricGenerators = new HashMap<UUID, MetricGenerator>();
		generatorsToSetup        = new ArrayList<UUID>();
		
    msSetInfoCache                    = new HashMap<UUID, EMMeasurementSetInfo>();
    currentMeasurementSetPulls        = new HashSet<UUID>();
    orderedCurrentMeasurementSetPulls = new LinkedList<UUID>();
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
  
	public void resetPhaseStates()
	{
		// Leave historic record of metric generators intact for this client
		
		if ( generatorsToSetup != null ) generatorsToSetup.clear();
		if ( msSetInfoCache != null ) msSetInfoCache.clear();
		if ( currentMeasurementSetPulls != null ) currentMeasurementSetPulls.clear();
		if ( orderedCurrentMeasurementSetPulls != null ) orderedCurrentMeasurementSetPulls.clear();
		
		expectedPullMSID = null;
		currentRequestedBatchMSID = null;
		currentPhase = EMPhase.eEMUnknownPhase;
		isPhaseAccelerating = false;
		supportedPhases.clear();
		metricGenerators.clear();
		discoveredGenerators = false;
		isPushCapable = false;
		isPullCapable = false;
		currentMGSetupID = null;
		generatorsSetupOK.clear();
		isQueueingMSPulls = false;
		postReportSummary = null;
		postReportOutstandingBatches.clear();
		tearDownSuccessful = false;
		timeOutsCalled.clear();
	}
	
	public void clearHistoricMetricGenerators()
	{
		historicMetricGenerators.clear();
	}
	
	public Set<MetricGenerator> getCopyOfHistoricMetricGenerators()
	{
		HashSet<MetricGenerator> histMG = new HashSet<MetricGenerator>();
		
		histMG.addAll( historicMetricGenerators.values() );
		
		return histMG;
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
  
  public void appendMetricGenerators( Set<MetricGenerator> generators )
  {    
    if ( generators != null )
    {
      // Find out which of these generators are new for this client
      HashSet<MetricGenerator> newGenerators = new HashSet<MetricGenerator>();
      Set<UUID> existingGenIDs               = metricGenerators.keySet();
      
      Iterator<MetricGenerator> metIt = generators.iterator();
      while ( metIt.hasNext() )
      {
        MetricGenerator mg = metIt.next();
        UUID            mgID = mg.getUUID();
        
        // If metric generator is new for the client during this *connection*,
				// flag it for possible set-up
        if ( !existingGenIDs.contains(mgID) )
          newGenerators.add( mg );
        else
        {
          // Otherwise, just update the existing metric generator info for this client
          metricGenerators.remove( mgID );
          metricGenerators.put( mgID, mg );
        }
				
				// If the metric generator is new for the complete history of the client
				// (it may re-connect with new generators, add it to the history)
				if ( !historicMetricGenerators.containsKey(mgID) )
					historicMetricGenerators.put( mgID, mg );
      }
      
      // Make ready the new metric generators for subsequent phases & add to
      // existing collection
      metIt = newGenerators.iterator();
      while ( metIt.hasNext() )
      {
        MetricGenerator mGen = metIt.next();
        UUID genID = mGen.getUUID();
        
        generatorsToSetup.add( genID );
        metricGenerators.put( genID, mGen );
      }
      
      // Cache all new measurement set info for Live Monitoring state later
      Map<UUID,MeasurementSet> mSets = 
              MetricHelper.getAllMeasurementSets( metricGenerators.values() );
      
      // Add new measurement sets to the info cache
      Iterator<MeasurementSet> msIt = mSets.values().iterator();
      while ( msIt.hasNext() )
      {
        MeasurementSet ms = msIt.next();
        UUID msID = ms.getID();
        
        if ( !msSetInfoCache.containsKey(msID) )
          msSetInfoCache.put( ms.getID(), new EMMeasurementSetInfo(ms) );
      }      
    }
  }
  
  public boolean setEntityEnabled( UUID entID, boolean enabled )
  {
    boolean success = false;
    
    // Find entity first
    Map<UUID, MeasurementSet> entityMSets = 
            MetricHelper.getMeasurementSetsForEntity( entID, metricGenerators.values() );
    
    // If we've found measurement sets, update their measurement set infos
    if ( !entityMSets.isEmpty() )
    {
      Iterator<UUID> msIDIt = entityMSets.keySet().iterator();
      while ( msIDIt.hasNext() )
      {
        EMMeasurementSetInfo msInfo = msSetInfoCache.get( msIDIt.next() );

        if ( msInfo != null )
          msInfo.setEntityEnabled( enabled );
      }
    }
    
    return success;
  }
  
  public boolean setEntityLiveEnabled( UUID entID, boolean enabled )
  {
    boolean success = false;
    
    // Find entity first
    Map<UUID, MeasurementSet> entityMSets = 
            MetricHelper.getMeasurementSetsForEntity( entID, metricGenerators.values() );
    
    // If we've found measurement sets, update their measurement set infos
    if ( !entityMSets.isEmpty() )
    {
      Iterator<UUID> msIDIt = entityMSets.keySet().iterator();
      while ( msIDIt.hasNext() )
      {
        EMMeasurementSetInfo msInfo = msSetInfoCache.get( msIDIt.next() );

        if ( msInfo != null )
            msInfo.setLiveEnabled( enabled );
      }
    }
    
    return success;
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
  
  public boolean isMeasurementSetQueuedForPull( UUID msID )
  {
    boolean isPulling;
    
    synchronized( pullLock )
    { isPulling = currentMeasurementSetPulls.contains(msID); }
    
    return isPulling;
  }
  
  public Set<UUID> getCopyOfCurrentMeasurementSetPullIDs()
  {
    HashSet<UUID> pullCopy = new HashSet<UUID>();
    
    synchronized ( pullLock )
    { pullCopy.addAll( currentMeasurementSetPulls ); }
    
    return pullCopy;
  }
  
  public void addPullingMeasurementSetID( UUID msID )
  {
    if ( msID != null )
    {
      synchronized ( pullLock )
      {
        if ( !currentMeasurementSetPulls.contains(msID) )
        {
          currentMeasurementSetPulls.add( msID );
          orderedCurrentMeasurementSetPulls.add( msID );
          
          isQueueingMSPulls = !currentMeasurementSetPulls.isEmpty();
        }
      }
    }
  }
  
  public void removePullingMeasurementSetID( UUID msID )
  { 
    if ( msID != null )
    {
      synchronized ( pullLock )
      {
        currentMeasurementSetPulls.remove( msID );
        orderedCurrentMeasurementSetPulls.remove( msID ); // Should be first in list
        
        // We are now free to iterate to the next MS...
        if ( msID.equals(expectedPullMSID) ) expectedPullMSID = null;
        
        // ... and ensure our state of currently pulling remains accurate
        isQueueingMSPulls = ( !currentMeasurementSetPulls.isEmpty() || 
                              expectedPullMSID != null );
      }
    }
  }
  
  public UUID iterateNextValidMSToBePulled()
  {
    UUID nextID = null;
    
    synchronized ( pullLock )
    {
      // Need to take care here - MUST keep track of what we are expecting back
      // and not try to iterate further forward until we know we have the result
      if ( expectedPullMSID == null && !orderedCurrentMeasurementSetPulls.isEmpty() )
      {
        nextID = orderedCurrentMeasurementSetPulls.getFirst();
        while ( nextID != null )
        {
          // Must validate the MS if we are to pull it
          if ( validateMSReadyForPull(nextID) )
          {
            expectedPullMSID = nextID;
            break;
          }
          else // If this is not valid, remove it (do not requeue here!)
          {
            removePullingMeasurementSetID( nextID );
            
            if ( !orderedCurrentMeasurementSetPulls.isEmpty() )
              nextID = orderedCurrentMeasurementSetPulls.getFirst();
            else
              nextID = null;
          }
        }
      }
    }
    
    // Note that this will be NULL if we've not got our expected result back yet
    return nextID;
  }
  
  public void requeueMSForPulling( UUID msID )
  {
    if ( msID != null )
    {
      synchronized ( pullLock )
      {
        if ( currentMeasurementSetPulls.contains(msID) && 
             !msID.equals(expectedPullMSID) )
        {
          // Remove from current position in queue
          orderedCurrentMeasurementSetPulls.remove( msID );
          
          // Push to back of queue
          orderedCurrentMeasurementSetPulls.add( msID );
        }
      }
    }
  }
  
  public void clearAllLiveMeasurementSetPulls()
  {
    synchronized ( pullLock )
    {
      currentMeasurementSetPulls.clear(); 
      orderedCurrentMeasurementSetPulls.clear();
      
      isQueueingMSPulls = false;
    }
  }
  
  public EMMeasurementSetInfo getMSInfo( UUID msID )
  { return msSetInfoCache.get( msID ); }
  
  public void updatePullReceivedForMS( UUID msID )
  {
    if ( msID != null )
    {
      EMMeasurementSetInfo info = msSetInfoCache.get( msID );
      if ( info != null ) info.updatePullDate( new Date() );
    }
  }
  
  public boolean validateMSReadyForPull( UUID msID )
  {
    // Safety first
    if ( msID == null )   return false;
    if ( !isPullCapable ) return false;
    
    EMMeasurementSetInfo info = msSetInfoCache.get( msID );
    if ( info == null ) return false;
    
    // If client has 'disabled' the entity, then skip this measurement
    if ( !info.isEntityEnabled() ) return false;
    
    // If the ECC dashboard user has stopped live monitoring for this measuement, skip it
    if ( !info.isLiveEnabled() ) return false;    
    
    // Check measurement rule next
    MeasurementSet ms = info.getMeasurementSet();
    if ( ms == null ) return false;
    
    boolean ruleOK = false;
    switch ( ms.getMeasurementRule() )
    {        
      case eFIXED_COUNT : 
      {
        if ( info.getPullCount() < ms.getMeasurementCountMax() )
          ruleOK = true;
        
      } break;
        
      case eINDEFINITE : ruleOK = true;
    }
    
    if ( !ruleOK ) return false;
    
    // Now check time interval
    Date lastDate = info.getLastPullDate();
    if ( lastDate != null )
    {
      long ellapsedTime = new Date().getTime() - lastDate.getTime();
    
      if ( ellapsedTime < ms.getSamplingInterval() ) 
        return false;
    }
    
    // Looks like we can pull the measurement set
    return true;
  }
  
  // Post-report phase state ---------------------------------------------------
  public void setPostReportSummary( EMPostReportSummary report )
  { postReportSummary = report; }
  
  public EMDataBatch getCurrentExpectedDataBatch()
  {
    EMDataBatch targetBatch = null;
    
    if ( currentRequestedBatchMSID != null )
      targetBatch = postReportOutstandingBatches.get( currentRequestedBatchMSID );
    
    return targetBatch;
  }
  
  // Shared phase state --------------------------------------------------------
  public UUID getCurrentRequestBatchMSID()
  { return currentRequestedBatchMSID; }
  
  public void setCurrentRequestBatchMSID( UUID id )
  { currentRequestedBatchMSID = id; }
  
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
    if ( currentRequestedBatchMSID != null )
      postReportOutstandingBatches.remove( currentRequestedBatchMSID );
    
    currentRequestedBatchMSID = null;
    
    if ( !postReportOutstandingBatches.isEmpty() )
    {
      Iterator<UUID> msIDIt = postReportOutstandingBatches.keySet().iterator();
      currentRequestedBatchMSID = msIDIt.next();
    }
    
    return currentRequestedBatchMSID;
  }
  
  public void clearAllBatching()
  {
    postReportOutstandingBatches.clear();
    currentRequestedBatchMSID = null;
  }
  
  // Tear-down phase state -----------------------------------------------------
  public void setTearDownResult( boolean success )
  { tearDownSuccessful = success; }
}
 
