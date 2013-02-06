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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;

import java.util.*;




public class EMClient
{
  protected final Object  pullLock = new Object();
  
  // Experiment states
  protected UUID             clientID;
  protected String           clientName;
  protected boolean          isDisconnecting = false;
  protected boolean          isConnected     = false;
  protected EMPhase          currentPhase    = EMPhase.eEMUnknownPhase;
  protected boolean          isPhaseAccelerating;
  protected EnumSet<EMPhase> supportedPhases;
  
  // Discovery phase states
  protected HashSet<MetricGenerator> metricGenerators;
  protected boolean                  discoveredGenerators = false;
  protected boolean                  isPushCapable        = false;
  protected boolean                  isPullCapable        = false;
  
  // Setup phase states
  protected UUID          currentMGSetupID;
  protected HashSet<UUID> generatorsSetupOK;
  
  // Live monitoring phase states
  protected HashSet<UUID> currentMeasurementSetPulls;
  
  // Post-report phase states
  protected EMPostReportSummary        postReportSummary;
  protected HashMap<UUID, EMDataBatch> postReportOutstandingBatches; // Indexed by MeasurementSet ID
  
  // Tear-down phase
  protected boolean tearDownSuccessful = false;
  
  // Multi-phase time-out states
  protected EnumSet<EMPhaseTimeOut> timeOutsCalled;
  
  
  @Override
  public String toString()
  { return clientName; }
  
  /**
   * Gets the ID of the client.
   * 
   * @return - ID of the client.
   */
  public UUID getID()
  { return clientID; }
  
  /**
   * Gets the name of the client.
   * 
   * @return - Name of the client.
   */
  public String getName()
  { return clientName; }
  
  /**
   * Specifies whether the client is connected to the EM.
   * 
   * @return - True if connected.
   */
  public boolean isConnected()
  { return isConnected; }
  
  /**
   * Return true if the client is currently in the process of being disconnected
   * from the EM.
   * 
   * @return 
   */
  public boolean isDisconnecting()
  { return isDisconnecting; }
  
  public boolean isPhaseAccelerating()
  { return isPhaseAccelerating; }
  
  public EMPhase getCurrentPhaseActivity()
  { return currentPhase; }
  
  /**
   * Determines whether the client supports the specified experiment phase. Note:
   * the actual phases that the client supports will only be known after the 
   * discovery phase has completed. (It is assumed all clients support discovery).
   * 
   * @param  phase - phase which the client is queried to support
   * @return       - returns true if the client supports the phase.
   */
  public boolean supportsPhase( EMPhase phase )
  {
    // All clients MUST support discovery phase
    if ( phase.equals(EMPhase.eEMDiscoverMetricGenerators) ) return true;
    
    // Return bad news if we've got no supported phases
    if ( supportedPhases.isEmpty() ) return false;
    
    boolean supported = false;
    
    Iterator<EMPhase> pIt = supportedPhases.iterator();
    while ( pIt.hasNext() )
    {
      EMPhase nextPhase = pIt.next();
      if ( phase.equals(nextPhase) )
      {
        supported = true;
        break;
      }
    }
    
    return supported;
  }
  
  /**
   * Returns the client's support for the phases the EM executes.
   * 
   * @return - A set of supported phases.
   */
  public EnumSet<EMPhase> getCopyOfSupportedPhases()
  {
    EnumSet<EMPhase> phaseCopy = EnumSet.noneOf( EMPhase.class );
    phaseCopy.addAll( supportedPhases );
    
    return phaseCopy;
  }
  
  /**
   * Returns the result of the discovery process this client reported to the EM during
   * the discovery phase.
   * 
   * @return 
   */
  public boolean getGeneratorDiscoveryResult()
  { return discoveredGenerators; }
  
  /**
   * Returns the MetricGenerators the client reported to the EM during the discovery phase.
   * 
   * @return 
   */
  public Set<MetricGenerator> getCopyOfMetricGenerators()
  {
    HashSet<MetricGenerator> mgCopies = new HashSet<MetricGenerator>();
    
    Iterator<MetricGenerator> copyIt = metricGenerators.iterator();
    while ( copyIt.hasNext() )
    {
      MetricGenerator clone = new MetricGenerator( copyIt.next() );
      mgCopies.add( clone );
    }
    
    return mgCopies;
  }
  
  /**
   * Returns whether the client has declared it is capable of pushing. This
   * information is only updated after starting the Live Monitoring phase.
   * 
   * @return 
   */
  public boolean isPushCapable()
  { return isPushCapable; }
  
  /**
   * Returns whether the client has declared it is capable of being pulled.
   * This information is only updated after starting the Live Monitoring phase.
   * @return 
   */
  public boolean isPullCapable()
  { return isPullCapable; }
  
  /**
   * Use this method to determine whether the client is currently setting up
   * a metric generator.
   * 
   * @return - Returns true if a metric generator is being set up.
   */
  public boolean isSettingUpMetricGenerator()
  { return (currentMGSetupID != null); }
  
  /**
   * Returns the set-up result the client reported (if supported) during the the
   * set-up phase.
   * 
   * @return 
   */
  public boolean metricGeneratorsSetupOK()
  {
    if ( metricGenerators.isEmpty() )  return false;
    if ( generatorsSetupOK.isEmpty() ) return false;
    
    Iterator<MetricGenerator> genIt = metricGenerators.iterator();
    while ( genIt.hasNext() )
      if ( !generatorsSetupOK.contains( genIt.next().getUUID() ) ) return false;
    
    return true;
  }
  
  /**
   * Specifies whether the client is currently generating metric data requested
   * by the EM.
   * 
   * @return - true if currently generating data
   */
  public boolean isPullingMetricData()
  {
    boolean isPulling;
    
    synchronized ( pullLock )
    { isPulling = !currentMeasurementSetPulls.isEmpty(); }
    
    return isPulling;
  }
  
  /**
   * Returns the IDs of all MeasurementSet IDs that have been requested by the EM
   * but not yet returned by the client
   * 
   * @return  - Set of IDs of MeasurementSets currently being pulled
   */
  public Set<UUID> getCopyOfCurrentMeasurementSetPullIDs()
  {
    HashSet<UUID> pullCopy = new HashSet<UUID>();
    pullCopy.addAll( currentMeasurementSetPulls );
    
    return pullCopy;
  }
  
  /**
   * Returns the post report summary the client reported (if supported) during the
   * post-reporting phase.
   * 
   * @return 
   */
  public EMPostReportSummary getPostReportSummary()
  { return postReportSummary; }
  
  /**
   * Use this method to determine if the client is currently generating post-report
   * data for the EM.
   * 
   * @return - Returns true if the client is generating post-report data.
   */
  public boolean isCreatingPostReportBatchData()
  { return !postReportOutstandingBatches.isEmpty(); }
  
  /**
   * Returns the result of the tear-down process the client executed (if it supports it)
   * during the tear-down phase.
   * 
   * @return 
   */
  public boolean getTearDownResult()
  { return tearDownSuccessful; }
  
  /**
   * Returns a copy of the time-outs sent to this client by the EM.
   * 
   * @return - Set of known time-outs sent to the client.
   */
  public EnumSet<EMPhaseTimeOut> getCopyOfSentTimeOuts()
  { return EnumSet.copyOf( timeOutsCalled ); }
  
  /**
   * Use this method to determine if the client has had a specific time-out
   * sent to them by the EM
   * 
   * @param timeout - Specific time-out of interest
   * @return        - Returns true if the time-out has been sent
   */
  public boolean isNotifiedOfTimeOut( EMPhaseTimeOut timeout )
  { return timeOutsCalled.contains( timeout ); }
  
  // Protected methods ---------------------------------------------------------
  /**
   * Constructor of the client representing a user of the EM. ID must be random.
   * Clients should only be constructed by the EM.
   * 
   * @param id    - Random UUID of the client
   * @param name  - Human recognisable name of the client
   */
  protected EMClient( UUID id, String name )
  {
    clientID = id;
    clientName = name;
    
    supportedPhases = EnumSet.noneOf( EMPhase.class );
    timeOutsCalled  = EnumSet.noneOf( EMPhaseTimeOut.class );
    
    metricGenerators             = new HashSet<MetricGenerator>();
    generatorsSetupOK            = new HashSet<UUID>();
    currentMeasurementSetPulls   = new HashSet<UUID>();
    postReportOutstandingBatches = new HashMap<UUID, EMDataBatch>();
  }
}
