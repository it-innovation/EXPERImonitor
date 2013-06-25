/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2013
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

#pragma once

#include "MetricGenerator.h"
#include "EMPostReportSummary.h"
#include "EMDataBatch.h"
#include "EMPhase.h"
#include "EMPhaseTimeOut.h"

#include <boost/uuid/uuid.hpp>

#include <hash_set>
#include <hash_map>


namespace ecc_commonDataModel
{

  class EMClient
  {
  public:
    virtual ~EMClient();
    
    typedef boost::shared_ptr<EMClient> ptr_t;
  
    /**
     * Gets the ID of the client.
     * 
     * @return - ID of the client.
     */
    boost::uuids::uuid getID();
  
    /**
     * Gets the name of the client.
     * 
     * @return - Name of the client.
     */
    std::wstring getName();
  
    /**
     * Specifies whether the client is connected to the EM.
     * 
     * @return - True if connected.
     */
    bool isConnected();
  
    /**
     * Return true if the client is currently in the process of being disconnected
     * from the EM.
     * 
     * @return 
     */
    bool isDisconnecting();
  
    bool isPhaseAccelerating();
  
    EMPhase getCurrentPhaseActivity();
  
    /**
     * Determines whether the client supports the specified experiment phase. Note:
     * the actual phases that the client supports will only be known after the 
     * discovery phase has completed. (It is assumed all clients support discovery).
     * 
     * @param  phase - phase which the client is queried to support
     * @return       - returns true if the client supports the phase.
     */
    bool supportsPhase( EMPhase phase );
  
    /**
     * Returns the client's support for the phases the EM executes.
     * 
     * @return - A set of supported phases.
     */
    std::hash_set<EMPhase> getCopyOfSupportedPhases();
  
    /**
     * Returns the result of the discovery process this client reported to the EM during
     * the discovery phase.
     * 
     * @return 
     */
    bool getGeneratorDiscoveryResult();
  
    /**
     * Returns the MetricGenerators the client reported to the EM during the discovery phase.
     * 
     * @return 
     */
    std::hash_set<MetricGenerator> getCopyOfMetricGenerators();
  
    /**
     * Returns whether the client has declared it is capable of pushing. This
     * information is only updated after starting the Live Monitoring phase.
     * 
     * @return 
     */
    bool isPushCapable();
  
    /**
     * Returns whether the client has declared it is capable of being pulled.
     * This information is only updated after starting the Live Monitoring phase.
     * @return 
     */
    bool isPullCapable();
  
    /**
     * Use this method to determine whether the client is currently setting up
     * a metric generator.
     * 
     * @return - Returns true if a metric generator is being set up.
     */
    bool isSettingUpMetricGenerator();
  
    /**
     * Returns the set-up result the client reported (if supported) during the the
     * set-up phase.
     * 
     * @return 
     */
    bool metricGeneratorsSetupOK();
  
    /**
     * Specifies whether the client is currently generating metric data requested
     * by the ECC.
     * 
     * @return - true if currently generating data
     */
    bool isPullingMetricData();
  
    /**
     * Returns the post report summary the client reported (if supported) during the
     * post-reporting phase.
     * 
     * @return 
     */
    EMPostReportSummary getPostReportSummary();
  
    /**
     * Use this method to determine if the client is currently generating post-report
     * data for the EM.
     * 
     * @return - Returns true if the client is generating post-report data.
     */
    bool isCreatingPostReportBatchData();
  
    /**
     * Returns the result of the tear-down process the client executed (if it supports it)
     * during the tear-down phase.
     * 
     * @return 
     */
    bool getTearDownResult();
  
    /**
     * Returns a copy of the time-outs sent to this client by the EM.
     * 
     * @return - Set of known time-outs sent to the client.
     */
    std::hash_set<EMPhaseTimeOut> getCopyOfSentTimeOuts();
  
    /**
     * Use this method to determine if the client has had a specific time-out
     * sent to them by the EM
     * 
     * @param timeout - Specific time-out of interest
     * @return        - Returns true if the time-out has been sent
     */
    bool isNotifiedOfTimeOut( EMPhaseTimeOut timeout );
  
  protected:

    EMClient( const boost::uuids::uuid& id, const std::wstring& name );

    // Typedefs
    typedef std::hash_set<EMPhase>                         PhaseSet;
    typedef std::hash_set<MetricGenerator>                 MetricSet;
    typedef std::hash_set<boost::uuids::uuid>              UUIDSet;
    typedef std::hash_map<boost::uuids::uuid, EMDataBatch> EMDataBatchMap;

    // Experiment states
    boost::uuids::uuid clientID;
    std::wstring       clientName;
    bool               isClientDisconnecting;  // default false
    bool               isClientConnected;      // default false
    EMPhase            currentPhase;
    bool               isClientPhaseAccelerating;
    PhaseSet           supportedPhases;
  
    // Discovery phase states
    MetricSet metricGenerators;
    bool      discoveredGenerators; // default false
    bool      isClientPushCapable;  // default false
    bool      isClientPullCapable;  // default false
  
    // Setup phase states
    boost::uuids::uuid currentMGSetupID;
    UUIDSet            generatorsSetupOK;
  
    // Live monitoring phase states
    volatile bool isQueueingMSPulls;
  
    // Post-report phase states
    EMPostReportSummary postReportSummary;
    EMDataBatchMap      postReportOutstandingBatches; // Indexed by MeasurementSet ID
  
    // Tear-down phase
    bool tearDownSuccessful;  // default false
  
    // Multi-phase time-out states
    std::hash_set<EMPhaseTimeOut> timeOutsCalled;
  };

} // namespace
