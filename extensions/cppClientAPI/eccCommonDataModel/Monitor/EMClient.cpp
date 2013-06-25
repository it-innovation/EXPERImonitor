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
//      Created Date :          21-June-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "EMClient.h"

using namespace boost::uuids;
using namespace std;




namespace ecc_commonDataModel
{

//class EMClient
//{
//  // Experiment states
//  protected Guid             clientID;
//  protected string           clientName;
//  protected bool             isClientDisconnecting = false;
//  protected bool             isClientConnected = false;
//  protected EMPhase          currentPhase;
//  protected bool             isClientPhaseAccelerating;
//  protected HashSet<EMPhase> supportedPhases;
//  
//  // Discovery phase states
//  protected HashSet<MetricGenerator> metricGenerators;
//  protected bool                     discoveredGenerators = false;
//  protected bool                     isClientPushCapable  = false;
//  protected bool                     isClientPullCapable  = false;
//  
//  // Setup phase states
//  protected Guid          currentMGSetupID;
//  protected HashSet<Guid> generatorsSetupOK;
//  
//  // Live monitoring phase states
//  protected volatile bool isQueueingMSPulls;
//  
//  // Post-report phase states
//  protected EMPostReportSummary           postReportSummary;
//  protected Dictionary<Guid, EMDataBatch> postReportOutstandingBatches; // Indexed by MeasurementSet ID
//  
//  // Tear-down phase
//  protected bool tearDownSuccessful = false;
//  
//  // Multi-phase time-out states
//  protected HashSet<EMPhaseTimeOut> timeOutsCalled;
//  
//  
//  public String toString()
//  { return clientName; }
//  
//  /**
//   * Gets the ID of the client.
//   * 
//   * @return - ID of the client.
//   */
//  public Guid getID()
//  { return clientID; }
//  
//  /**
//   * Gets the name of the client.
//   * 
//   * @return - Name of the client.
//   */
//  public string getName()
//  { return clientName; }
//  
//  /**
//   * Specifies whether the client is connected to the EM.
//   * 
//   * @return - True if connected.
//   */
//  public bool isConnected()
//  { return isClientConnected; }
//  
//  /**
//   * Return true if the client is currently in the process of being disconnected
//   * from the EM.
//   * 
//   * @return 
//   */
//  public bool isDisconnecting()
//  { return isClientDisconnecting; }
//  
//  public bool isPhaseAccelerating()
//  { return isClientPhaseAccelerating; }
//  
//  public EMPhase getCurrentPhaseActivity()
//  { return currentPhase; }
//  
//  /**
//   * Determines whether the client supports the specified experiment phase. Note:
//   * the actual phases that the client supports will only be known after the 
//   * discovery phase has completed. (It is assumed all clients support discovery).
//   * 
//   * @param  phase - phase which the client is queried to support
//   * @return       - returns true if the client supports the phase.
//   */
//  public bool supportsPhase( EMPhase phase )
//  {
//      // All clients MUST support discovery phase
//      if ( phase == EMPhase.eEMDiscoverMetricGenerators ) return true;
//      
//      // Return bad news if we've got no supported phases
//      if ( supportedPhases.Count == 0 ) return false;
//      
//      bool supported = false;
//
//      foreach (EMPhase ep in supportedPhases )
//      {
//          if ( ep == phase )
//          {
//              supported = true;
//              break;
//          }
//      }
//        
//    return supported;
//  }
//  
//  /**
//   * Returns the client's support for the phases the EM executes.
//   * 
//   * @return - A set of supported phases.
//   */
//  public HashSet<EMPhase> getCopyOfSupportedPhases()
//  {
//      HashSet<EMPhase> supported = new HashSet<EMPhase>();
//
//      foreach (EMPhase phase in supportedPhases )
//          supported.Add( phase );
//    
//      return supported;
//  }
//  
//  /**
//   * Returns the result of the discovery process this client reported to the EM during
//   * the discovery phase.
//   * 
//   * @return 
//   */
//  public bool getGeneratorDiscoveryResult()
//  { return discoveredGenerators; }
//  
//  /**
//   * Returns the MetricGenerators the client reported to the EM during the discovery phase.
//   * 
//   * @return 
//   */
//  public HashSet<MetricGenerator> getCopyOfMetricGenerators()
//  {
//      return new HashSet<MetricGenerator>(metricGenerators);
//  }
//  
//  /**
//   * Returns whether the client has declared it is capable of pushing. This
//   * information is only updated after starting the Live Monitoring phase.
//   * 
//   * @return 
//   */
//  public bool isPushCapable()
//  { return isClientPushCapable; }
//  
//  /**
//   * Returns whether the client has declared it is capable of being pulled.
//   * This information is only updated after starting the Live Monitoring phase.
//   * @return 
//   */
//  public bool isPullCapable()
//  { return isClientPullCapable; }
//  
//  /**
//   * Use this method to determine whether the client is currently setting up
//   * a metric generator.
//   * 
//   * @return - Returns true if a metric generator is being set up.
//   */
//  public bool isSettingUpMetricGenerator()
//  { return (currentMGSetupID != null); }
//  
//  /**
//   * Returns the set-up result the client reported (if supported) during the the
//   * set-up phase.
//   * 
//   * @return 
//   */
//  public bool metricGeneratorsSetupOK()
//  {
//      if ( metricGenerators.Count == 0 )  return false;
//      if ( generatorsSetupOK.Count == 0 ) return false;
//
//      foreach ( MetricGenerator mGen in metricGenerators )
//          if ( !generatorsSetupOK.Contains( mGen.uuid ) ) return false;
//      
//      return true;
//  }
//  
//  /**
//   * Specifies whether the client is currently generating metric data requested
//   * by the ECC.
//   * 
//   * @return - true if currently generating data
//   */
//  public bool isPullingMetricData()
//  { return isQueueingMSPulls; }
//  
//  /**
//   * Returns the post report summary the client reported (if supported) during the
//   * post-reporting phase.
//   * 
//   * @return 
//   */
//  public EMPostReportSummary getPostReportSummary()
//  { return postReportSummary; }
//  
//  /**
//   * Use this method to determine if the client is currently generating post-report
//   * data for the EM.
//   * 
//   * @return - Returns true if the client is generating post-report data.
//   */
//  public bool isCreatingPostReportBatchData()
//  { return postReportOutstandingBatches.Count > 0; }
//  
//  /**
//   * Returns the result of the tear-down process the client executed (if it supports it)
//   * during the tear-down phase.
//   * 
//   * @return 
//   */
//  public bool getTearDownResult()
//  { return tearDownSuccessful; }
//  
//  /**
//   * Returns a copy of the time-outs sent to this client by the EM.
//   * 
//   * @return - Set of known time-outs sent to the client.
//   */
//  public HashSet<EMPhaseTimeOut> getCopyOfSentTimeOuts()
//  {
//      HashSet<EMPhaseTimeOut> timeOuts = new HashSet<EMPhaseTimeOut>();
//
//      foreach( EMPhaseTimeOut to in timeOutsCalled )
//          timeOuts.Add( to );
//
//      return timeOuts;
//  }
//  
//  /**
//   * Use this method to determine if the client has had a specific time-out
//   * sent to them by the EM
//   * 
//   * @param timeout - Specific time-out of interest
//   * @return        - Returns true if the time-out has been sent
//   */
//  public bool isNotifiedOfTimeOut( EMPhaseTimeOut timeout )
//  { return timeOutsCalled.Contains( timeout ); }
//  
//  // Protected methods ---------------------------------------------------------
//  /**
//   * Constructor of the client representing a user of the EM. ID must be random.
//   * Clients should only be constructed by the EM.
//   * 
//   * @param id    - Random UUID of the client
//   * @param name  - Human recognisable name of the client
//   */
//  protected EMClient( Guid id, string name )
//  {
//    clientID = id;
//    clientName = name;
//    
//    supportedPhases = new HashSet<EMPhase>();
//    timeOutsCalled  = new HashSet<EMPhaseTimeOut>();
//    
//    metricGenerators  = new HashSet<MetricGenerator>();
//    generatorsSetupOK = new HashSet<Guid>();
//    postReportOutstandingBatches = new Dictionary<Guid, EMDataBatch>();
//  }
//};

} // namespace
