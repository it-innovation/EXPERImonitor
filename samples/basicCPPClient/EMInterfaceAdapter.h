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
//      Created Date :          08-Jul-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#pragma once

#include "EMInterfaceFactory.h"
#include "AMQPBasicSubscriptionService.h"
#include "IAMQPMessageDispatchPump.h"

#include "IEMDiscovery_UserListener.h"
#include "IEMSetup_UserListener.h"
#include "IEMLiveMonitor_UserListener.h"
#include "IEMPostReport_UserListener.h"
#include "IEMTearDown_UserListener.h"

#include "EMPhase.h"
#include "Experiment.h"
#include "MetricGenerator.h"
#include "Report.h"

#include "EMInterfaceAdapterListener.h"

class EMInterfaceAdapter : public boost::enable_shared_from_this<EMInterfaceAdapter>,
                           public ecc_emClient_spec::IEMDiscovery_UserListener,
                           public ecc_emClient_spec::IEMSetup_UserListener,
                           public ecc_emClient_spec::IEMLiveMonitor_UserListener,
                           public ecc_emClient_spec::IEMPostReport_UserListener,
                           public ecc_emClient_spec::IEMTearDown_UserListener
{

public:

    typedef boost::shared_ptr<EMInterfaceAdapter> ptr_t;

    EMInterfaceAdapter( EMIAdapterListener::ptr_t listener );

    virtual ~EMInterfaceAdapter();

    void registerWithEM( const String& name,
                         ecc_amqpAPI_impl::AMQPBasicChannel::ptr_t channel,
                         const UUID& eccID,
                         const UUID& clientID );

    void disconnectFromEM();

    ecc_commonDataModel::Experiment::ptr_t getExperimentInfo();

    void sendMetricGenerators( const ecc_commonDataModel::MetricGenerator::Set& generators );

    void pushMetric( ecc_commonDataModel::Report::ptr_t report );


    // IEMDiscovery_UserListener -------------------------------------------------
  
    virtual void onCreateInterface( const UUID& senderID, 
                                    const ecc_commonDataModel::EMInterfaceType& type );

    virtual void onRegistrationConfirmed( const UUID&      senderID, 
                                          const bool       confirmed,
                                          const UUID&      expUniqueID,
                                          const String&    expNamedID,
                                          const String&    expName,
                                          const String&    expDescription,
                                          const TimeStamp& createTime );

    virtual void onDeregisteringThisClient( const UUID&   senderID,
                                            const String& reason );

    virtual void onRequestActivityPhases( const UUID& senderID );

    virtual void onDiscoverMetricGenerators( const UUID& senderID );

    virtual void onRequestMetricGeneratorInfo( const UUID& senderID );

    virtual void onDiscoveryTimeOut( const UUID& senderID );

    virtual void onSetStatusMonitorEndpoint( const UUID& senderID,
                                             const String& endPoint );

    // IEMSetup_UserListener -----------------------------------------------------

    virtual void onSetupMetricGenerator( const UUID& senderID, const UUID& genID );

    virtual void onSetupTimeOut( const UUID& senderID, const UUID& genID );

    // IEMLiveMonitor_UserListener -----------------------------------------------
    virtual void onStartPushing( const UUID& senderID );

    virtual void onReceivedPush( const UUID& senderID, const UUID& lastReportID );

    virtual void onStopPushing( const UUID& senderID );

    virtual void onPullMetric( const UUID& senderID, const UUID& measurementSetID );

    virtual void onPullMetricTimeOut( const UUID& senderID, const UUID& measurementSetID );

    virtual void onPullingStopped( const UUID& senderID );

    virtual void onReceivedPull( const UUID& senderID, const UUID& lastReportID );

    // IEMPostReport_UserListener ------------------------------------------------
    virtual void onRequestPostReportSummary( const UUID& senderID );

    virtual void onRequestDataBatch( const UUID& senderID, 
                                     ecc_commonDataModel::EMDataBatch::ptr_t reqBatch );

    virtual void notifyReportBatchTimeOut( const UUID& senderID, const UUID& batchID );

    // IEMTearDown_UserListener --------------------------------------------------
    virtual void onTearDownMetricGenerators( const UUID& senderID );

    virtual void onTearDownTimeOut( const UUID& senderID );

private:
  
    EMIAdapterListener::ptr_t emiListener;
    
    String                                    clientName;
    ecc_amqpAPI_impl::AMQPBasicChannel::ptr_t amqpChannel;
    UUID                                      expMonitorID;
    UUID                                      clientID;
    ecc_commonDataModel::Experiment::ptr_t    currentExperiment;

    // ECC Interfaces
    ecc_emClient_impl::EMInterfaceFactory::ptr_t      interfaceFactory;
    ecc_amqpAPI_spec::IAMQPMessageDispatchPump::ptr_t dispatchPump;
    ecc_emClient_spec::IEMMonitorEntryPoint::ptr_t    entryPointFace;
    ecc_emClient_spec::IEMDiscovery::ptr_t            discoveryFace;
    ecc_emClient_spec::IEMMetricGenSetup::ptr_t       setupFace;
    ecc_emClient_spec::IEMLiveMonitor::ptr_t          liveMonitorFace;
    ecc_emClient_spec::IEMPostReport::ptr_t           postReportFace;
    ecc_emClient_spec::IEMTearDown::ptr_t             tearDownFace;

    // Supported phases & metric Generators
    ecc_commonDataModel::EMPhaseSet supportedPhases;

};

