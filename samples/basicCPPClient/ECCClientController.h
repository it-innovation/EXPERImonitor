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

#include "AMQPConnectionFactory.h"
#include "EMInterfaceAdapter.h"
#include "EMInterfaceAdapterListener.h"




class ECCClientController : public boost::enable_shared_from_this<ECCClientController>,
                            public EMIAdapterListener
{

public:
  
    typedef boost::shared_ptr<ECCClientController> ptr_t;

    ECCClientController();
  
    virtual ~ECCClientController();

    void start( const String& rabbitServerIP,
                const UUID& expMonitorID,
                const UUID& clientID );

    void stop();

    // EMIAdapterListener -------------------------------------------------

    // Discovery phase events
    virtual void onEMConnectionResult( const bool connected, 
                                       ecc_commonDataModel::Experiment::ptr_t expInfo );
    
    virtual void onEMDeregistration( const String& reason );
    
    virtual void onDescribeSupportedPhases( ecc_commonDataModel::EMPhaseSet& phasesOUT );

    virtual void onDescribePushPullBehaviours( bool* pushPullOUT );
    
    virtual void onPopulateMetricGeneratorInfo();
    
    virtual void onDiscoveryTimeOut();

    // Set up phase events (not implemented in this demo) _________________
    virtual void onSetupMetricGenerator( const UUID& metricGeneratorID, 
                                         bool*       resultOUT );
  
    virtual void onSetupTimeOut( const UUID& metricGeneratorID );
    
    // Live monitoring phase events _______________________________________
    virtual void onLiveMonitoringStarted();

    virtual void onStartPushingMetricData();
    
    virtual void onPushReportReceived( const UUID& lastReportID );
    
    virtual void onStopPushingMetricData();
    
    virtual void onPullReportReceived( const UUID& reportID );
    
    virtual void onPullMetric( const UUID& measurementSetID, 
                               ecc_commonDataModel::Report::ptr_t reportOUT );
    
    virtual void onPullMetricTimeOut( const UUID& measurementSetID );

    virtual void onPullingStopped();

    // Post reporting phase events (not implemented in this demo) _________
    virtual void onPopulateSummaryReport( ecc_commonDataModel::EMPostReportSummary::ptr_t summaryOUT );

    virtual void onPopulateDataBatch( ecc_commonDataModel::EMDataBatch::ptr_t batchOut );
    
    virtual void onReportBatchTimeOut( const UUID& batchID );

    // Tear down phase events (not implemented in this demo) ______________
    virtual void onGetTearDownResult( bool* resultOUT );
    
    virtual void onTearDownTimeOut();

private:

    // AMQP/ECC--------------------------------------------------------------------
    ecc_amqpAPI_impl::AMQPConnectionFactory::ptr_t amqpFactory;
    ecc_amqpAPI_impl::AMQPBasicChannel::ptr_t      inAMQPChannel;
    ecc_amqpAPI_impl::AMQPBasicChannel::ptr_t      outAMQPChannel;
    EMInterfaceAdapter::ptr_t                      emiAdapter;
    bool                                           connectedToECC;
  
    // Client and metric ----------------------------------------------------------
    String                                      clientName;
    ecc_commonDataModel::MetricGenerator::ptr_t metricGenerator;

    // Measurement functions ------------------------------------------------------
    typedef boost::function<ecc_commonDataModel::Report::ptr_t (ECCClientController::ptr_t, UUID msID)> MeasurementDelegate;
  
    typedef boost::container::map<UUID, MeasurementDelegate> DelegateMap;

    DelegateMap delegateMap;

    // Example measurement delegate functions
    ecc_commonDataModel::Report::ptr_t measureAlphaDelegate( UUID msID );

    ecc_commonDataModel::Report::ptr_t measureBetaDelegate( UUID msID );

    ecc_commonDataModel::Report::ptr_t createRandomMeasurement( UUID msID ); // Common function used to create random data


};

