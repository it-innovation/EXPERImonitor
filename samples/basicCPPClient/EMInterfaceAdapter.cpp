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

#include "StdAfx.h"

#include "EMInterfaceAdapter.h"

#include "IAMQPMessageDispatchPump.h"

#include "EMInterfaceFactory.h"

#include <exception>

using namespace ecc_amqpAPI_spec;
using namespace ecc_amqpAPI_impl;
using namespace ecc_commonDataModel;
using namespace ecc_emClient_impl;

using namespace boost;
using namespace std;


EMInterfaceAdapter::EMInterfaceAdapter( EMIAdapterListener::ptr_t listener )
{
    emiListener = listener;
}


EMInterfaceAdapter::~EMInterfaceAdapter()
{
}

void EMInterfaceAdapter::registerWithEM( const String& name,
                                         AMQPBasicChannel::ptr_t channel,
                                         const UUID& eccID,
                                         const UUID& thisAppID )
{
    // Safety first
    if ( !channel ) throw L"AMQP Channel is null";
  
    amqpChannel     = channel;
    clientName      = name;
    expMonitorID    = eccID;
    clientID        = thisAppID;

    // Create interface factory to support interfaces required by the EM
    interfaceFactory = EMInterfaceFactory::ptr_t( new EMInterfaceFactory( amqpChannel, 
                                                                          false ) );

    // Create dispatch pump (only need to do this once)
    dispatchPump = interfaceFactory->createDispatchPump( L"EM Client pump", 
                                                         MINIMUM );
    dispatchPump->startPump();

    // Create a dispatch (for entry point interface) and add to the pump
    IAMQPMessageDispatch::ptr_t dispatch = interfaceFactory->createDispatch();
    dispatchPump->addDispatch( dispatch );

    // Create our entry point interface
    entryPointFace = interfaceFactory->createEntryPoint( expMonitorID, dispatch );

    // Create the principal interface (IEMDiscovery ahead of time)
    dispatch = interfaceFactory->createDispatch();
    dispatchPump->addDispatch( dispatch );
    discoveryFace = interfaceFactory->createDiscovery( expMonitorID, 
                                                       clientID, 
                                                       dispatch );

    discoveryFace->setUserListener( shared_from_this() );

    //.. and finally, try registering with the EM!
    entryPointFace->registerAsEMClient( clientID, clientName );
}

void EMInterfaceAdapter::disconnectFromEM()
{        
    // Say goodbye to ECC
    if ( discoveryFace )
      discoveryFace->clientDisconnecting();
     
    // Tidy up anyway
    dispatchPump->stopPump();
    
    // Dispose of interfaces
    if ( entryPointFace )
    {
      entryPointFace->shutdown();
      entryPointFace  = NULL;
    }
    
    if ( discoveryFace )
    {
      discoveryFace->shutdown();
      discoveryFace = NULL;
    }
    
    if ( setupFace )
    {
      setupFace->shutdown();
      setupFace = NULL;
    }
    
    if ( liveMonitorFace )
    {
      liveMonitorFace->shutdown();
      liveMonitorFace = NULL;
    }
   
    if ( postReportFace )
    {
      postReportFace->shutdown();
      postReportFace = NULL;
    }

    if ( tearDownFace )
    {
      tearDownFace->shutdown();
      tearDownFace = NULL;
    }
        
    amqpChannel = NULL;
}

Experiment::ptr_t EMInterfaceAdapter::getExperimentInfo()
{
    return currentExperiment;
}

void EMInterfaceAdapter::sendMetricGenerators( const MetricGenerator::Set& generators )
{
    if ( discoveryFace ) discoveryFace->sendMetricGeneratorInfo( generators );
}

void EMInterfaceAdapter::pushMetric( Report::ptr_t report )
{
    if ( report && liveMonitorFace ) liveMonitorFace->pushMetric( report );
}


// IEMDiscovery_UserListener -------------------------------------------------

void EMInterfaceAdapter::onCreateInterface( const UUID& senderID, 
                                            const EMInterfaceType& type )
{
    if ( senderID == expMonitorID )
    {      
        switch (type)
        {
        case eEMSetup :
        {
            if ( !setupFace )
            {
                IAMQPMessageDispatch::ptr_t dispatch = interfaceFactory->createDispatch();
                dispatchPump->addDispatch( dispatch );
          
                setupFace = interfaceFactory->createSetup( expMonitorID, 
                                                            clientID, 
                                                            dispatch );
          
                setupFace->setUserListener( shared_from_this() );
                setupFace->notifyReadyToSetup();
            }

        } break;
    
        case eEMLiveMonitor :
        {
            if ( !liveMonitorFace )
            {
                IAMQPMessageDispatch::ptr_t dispatch = interfaceFactory->createDispatch();
                dispatchPump->addDispatch( dispatch );
          
                liveMonitorFace = interfaceFactory->createLiveMonitor( expMonitorID, 
                                                                        clientID, 
                                                                        dispatch );
          
                liveMonitorFace->setUserListener( shared_from_this() );

                // Find out whether we can push or pull, or both
                bool pushPull[2];
                        
                if ( emiListener )
                {
                    emiListener->onDescribePushPullBehaviours( pushPull );
            
                    // Notify EM of behaviours
                    if ( pushPull[0] ) liveMonitorFace->notifyReadyToPush();
                    if ( pushPull[1] ) liveMonitorFace->notifyReadyForPull();
                            
                    // Tell listener that the Live Monitoring phase has begun
                    emiListener->onLiveMonitoringStarted();
                }
                else
                {
                    // Legacy behaviour: report that we can both push and be pulled
                    liveMonitorFace->notifyReadyToPush();
                    liveMonitorFace->notifyReadyForPull();
                }
            }
        } break;

          case eEMPostReport :
          {
              if ( !postReportFace )
              {
                  IAMQPMessageDispatch::ptr_t dispatch = interfaceFactory->createDispatch();
                  dispatchPump->addDispatch( dispatch );
            
                  postReportFace = interfaceFactory->createPostReport( expMonitorID, 
                                                                        clientID, 
                                                                        dispatch );
            
                  postReportFace->setUserListener( shared_from_this() );
                  postReportFace->notifyReadyToReport();
              }

          } break;
      
          case eEMTearDown :
          {
              if ( !tearDownFace )
              {
                  IAMQPMessageDispatch::ptr_t dispatch = interfaceFactory->createDispatch();
                  dispatchPump->addDispatch( dispatch );
            
                  tearDownFace = interfaceFactory->createTearDown( expMonitorID, 
                                                                    clientID, 
                                                                    dispatch );
            
                  tearDownFace->setUserListener( shared_from_this() );
                  tearDownFace->notifyReadyToTearDown();
              }

          } break;
        }
    }
}

void EMInterfaceAdapter::onRegistrationConfirmed( const UUID&      senderID, 
                                                  const bool       confirmed,
                                                  const UUID&      expUniqueID,
                                                  const String&    expNamedID,
                                                  const String&    expName,
                                                  const String&    expDescription,
                                                  const TimeStamp& createTime )
{
    if ( senderID == expMonitorID )
    {
        // Create the experiment information for this client
        currentExperiment = Experiment::ptr_t( new Experiment( expUniqueID,
                                                               expNamedID,
                                                               expName,
                                                               expDescription,
                                                               createTime ) );
    
        if ( emiListener )
          emiListener->onEMConnectionResult( confirmed, currentExperiment );
    
        discoveryFace->readyToInitialise();
    }
}

void EMInterfaceAdapter::onDeregisteringThisClient( const UUID&   senderID,
                                                    const String& reason )
{
    if ( senderID == expMonitorID )
        if ( emiListener ) emiListener->onEMDeregistration( reason );
}

void EMInterfaceAdapter::onRequestActivityPhases( const UUID& senderID )
{
    if ( senderID == expMonitorID )
    {
        if ( emiListener )
            emiListener->onDescribeSupportedPhases( supportedPhases );
    
        discoveryFace->sendActivePhases( supportedPhases );
    }
}

void EMInterfaceAdapter::onDiscoverMetricGenerators( const UUID& senderID )
{
    // Just assume that all metric generators have been discovered
    if ( senderID == expMonitorID ) discoveryFace->sendDiscoveryResult( true );
}

void EMInterfaceAdapter::onRequestMetricGeneratorInfo( const UUID& senderID )
{
    if ( senderID == expMonitorID )
        if ( emiListener ) emiListener->onPopulateMetricGeneratorInfo();
}

void EMInterfaceAdapter::onDiscoveryTimeOut( const UUID& senderID )
{
    if ( emiListener ) emiListener->onDiscoveryTimeOut();
}

void EMInterfaceAdapter::onSetStatusMonitorEndpoint( const UUID& senderID,
                                                     const String& endPoint )
{ /*Not implemented in this demo */ }

// IEMSetup_UserListener -----------------------------------------------------
void EMInterfaceAdapter::onSetupMetricGenerator( const UUID& senderID, const UUID& genID )
{
    if ( (senderID == expMonitorID) && setupFace )
    {
        bool result[1];
        result[0] = false;

        if ( emiListener ) emiListener->onSetupMetricGenerator( genID, result );
    
        setupFace->notifyMetricGeneratorSetupResult( genID, result[0] );
    }
}

void EMInterfaceAdapter::onSetupTimeOut( const UUID& senderID, const UUID& genID )
{
    if ( emiListener ) emiListener->onSetupTimeOut( genID );
}

// IEMLiveMonitor_UserListener -----------------------------------------------
void EMInterfaceAdapter::onStartPushing( const UUID& senderID )
{
    if ( senderID == expMonitorID )
        if ( emiListener ) emiListener->onStartPushingMetricData();
}

void EMInterfaceAdapter::onReceivedPush( const UUID& senderID, const UUID& lastReportID )
{
    if ( senderID == expMonitorID )
        if ( emiListener ) emiListener->onPushReportReceived( lastReportID );
}

void EMInterfaceAdapter::onStopPushing( const UUID& senderID )
{
    if ( senderID == expMonitorID )
    {
        if ( emiListener ) emiListener->onStopPushingMetricData();
    
        liveMonitorFace->notifyPushingCompleted();
    }
}

void EMInterfaceAdapter::onPullMetric( const UUID& senderID, const UUID& measurementSetID )
{
    if ( (senderID == expMonitorID) && liveMonitorFace )
    {
        // Make sure we indicate which measurement set this report is carrying
        // (up to the client to actually populate it)
        Report::ptr_t reportOUT = Report::ptr_t( new Report( measurementSetID ) );
    
        if ( emiListener ) emiListener->onPullMetric( measurementSetID, reportOUT );
    
        liveMonitorFace->sendPulledMetric( reportOUT );
    }
}

void EMInterfaceAdapter::onPullMetricTimeOut( const UUID& senderID, const UUID& measurementSetID )
{
    if ( emiListener ) emiListener->onPullMetricTimeOut( measurementSetID );
}

void EMInterfaceAdapter::onPullingStopped( const UUID& senderID )
{
    if ( senderID == expMonitorID )
        if ( emiListener ) emiListener->onPullingStopped();
}

void EMInterfaceAdapter::onReceivedPull( const UUID& senderID, const UUID& lastReportID )
{
    if ( senderID == expMonitorID )
        if ( emiListener ) emiListener->onPullReportReceived( lastReportID );
}

// IEMPostReport_UserListener ------------------------------------------------
void EMInterfaceAdapter::onRequestPostReportSummary( const UUID& senderID )
{
    if ( (senderID == expMonitorID) && postReportFace )
    {
        EMPostReportSummary::ptr_t summary = EMPostReportSummary::ptr_t( new EMPostReportSummary() );
    
        if ( emiListener )
            emiListener->onPopulateSummaryReport( summary );
    
        postReportFace->sendReportSummary( summary );
    }
}

void EMInterfaceAdapter::onRequestDataBatch( const UUID& senderID, 
                                             EMDataBatch::ptr_t reqBatch )
{
    if ( (senderID == expMonitorID) && reqBatch && postReportFace )
    {
        if ( emiListener ) emiListener->onPopulateDataBatch( reqBatch );
    
        postReportFace->sendDataBatch( reqBatch );
    }
}

void EMInterfaceAdapter::notifyReportBatchTimeOut( const UUID& senderID, const UUID& batchID )
{
    if ( emiListener ) emiListener->onReportBatchTimeOut( batchID );
}

// IEMTearDown_UserListener --------------------------------------------------
void EMInterfaceAdapter::onTearDownMetricGenerators( const UUID& senderID )
{
    if ( senderID == expMonitorID )
    {
        bool result[1];
    
        if ( emiListener ) emiListener->onGetTearDownResult( result );
    
        tearDownFace->sendTearDownResult( result[0] );
    }
}

void EMInterfaceAdapter::onTearDownTimeOut( const UUID& senderID )
{
    if ( senderID == expMonitorID )
        if ( emiListener ) emiListener->onTearDownTimeOut();
}