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

#include "ECCClientController.h"

#include "MetricHelper.h"

#include <iostream>

using namespace ecc_amqpAPI_impl;
using namespace ecc_commonDataModel;

using namespace boost;
using namespace std;



ECCClientController::ECCClientController()
  : connectedToECC(false)
{
}

ECCClientController::~ECCClientController()
{
    emiAdapter = NULL;
    
    inAMQPChannel = NULL;
    outAMQPChannel = NULL;
}

void ECCClientController::start( const String& rabbitServerIP,
                                 const UUID&   expMonitorID,
                                 const UUID&   clientID )
{
  if ( !rabbitServerIP.empty() && !connectedToECC )
  {
    wcout << L"Attempting to connect to Rabbit" << endl;

    amqpFactory = AMQPConnectionFactory::ptr_t( new AMQPConnectionFactory() );

    try
    {
      // Set up AMQP factory to connect to the RabbitMQ server
      amqpFactory->setAMQPHostIPAddress( rabbitServerIP );
      amqpFactory->connectToAMQPHost();
      inAMQPChannel  = amqpFactory->createNewChannel();
      outAMQPChannel = amqpFactory->createNewChannel();

      cout << "Connected and channel created." << endl;

      // Create a name for this client (based on the time)
      TimeStamp date = getCurrentTime();
      clientName     = L"C++ Client (" + timeStampToString(date) + L")";

      // Create an adapter that will manage the 'low level' ECC communications
      emiAdapter = 
        EMInterfaceAdapter::ptr_t( new EMInterfaceAdapter( shared_from_this() ) );

      // Try registering with the ECC (see onEMConnectionResult(..) for result)
      emiAdapter->registerWithEM( clientName,
                                  inAMQPChannel, outAMQPChannel,
                                  expMonitorID,
                                  clientID );
    }
    catch ( const String& e ) 
    { wcout << L"Had problems connecting to RabbitMQ: " << e; }
  }
}

void ECCClientController::stop()
{
    if ( emiAdapter ) emiAdapter->disconnectFromEM();

    emiAdapter = NULL;

    if ( amqpFactory )
    {
        amqpFactory->closeDownConnection();
        amqpFactory = NULL;

        inAMQPChannel = NULL;
        outAMQPChannel = NULL;
    }    
}

// EMInterfaceAdapterListener ------------------------------------------------
void ECCClientController::onEMConnectionResult( const bool              connected, 
                                                const Experiment::ptr_t expInfo )
{
  wcout << L"Got connection result: " ;

  // If we successfully connected to the ECC, then display some basic info
  if ( connected )
  {
    wcout << L"CONNECTION OK" << endl << endl;
    wcout << L"Linked to experiment: " + expInfo->getExperimentID() << endl;

    connectedToECC = true;
  }
  else
  {
    wcout << L"CONNECTION REFUSED BY ECC" << endl;
    connectedToECC = false;
  }
}
    
void ECCClientController::onEMDeregistration( const String& reason )
{
    wcout << L"Got disconnection from the ECC: " << reason << endl;

    connectedToECC = false;
}
    
void ECCClientController::onDescribeSupportedPhases( EMPhaseSet& phasesOUT )
{
    // We are going to just support the live monitoring phase...
    // ... we support 'Discovery' by default

    wcout << L"Specifying phase compatibility (Live monitoring only)" << endl;

    phasesOUT.insert( ecc_commonDataModel::eEMLiveMonitoringPhase );

}

void ECCClientController::onDescribePushPullBehaviours( bool* pushPullOUT )
{
    // We're going to support only PULLing behaviour
    wcout << L"Specifying only pull behaviour" << endl;

    pushPullOUT[0] = false; // We will not PUSH data in this demo
    pushPullOUT[1] = true;  // We will response to PULL requests in this demo
}
    
void ECCClientController::onPopulateMetricGeneratorInfo()
{
    wcout << "Creating metric generator info..." << endl;

    Entity::ptr_t app = Entity::ptr_t( new Entity( createRandomUUID(),
                                                   L"Client app",
                                                   L"The thing we are observing") );

    Attribute::ptr_t alphaAttr = 
      MetricHelper::createAttribute( L"Random stream Alpha",
                                     L"A random number stream",
                                     app );

    Attribute::ptr_t betaAttr 
      = MetricHelper::createAttribute( L"Random stream Beta",
                                       L"Another random number stream",
                                       app );

    // Create our metric generator (we'll use this later during live monitoring)
    metricGenerator = MetricGenerator::ptr_t( new MetricGenerator( createRandomUUID(),
                                              L"Client metric generator",
                                              L"The only metric generator for this client" ) );
    // Associate an entity with the generator
    metricGenerator->addEntity( app );

    // Create a group of metric relating to memory
    MetricGroup::ptr_t group = 
      MetricHelper::createMetricGroup( L"Sample metric group",
                                       L"Metrics relating to the app",
                                       metricGenerator );

    // Create measurment sets for our two metrics
    MeasurementSet::ptr_t alphaSet =
        MetricHelper::createMeasurementSet( alphaAttr,
                                            RATIO,
                                            Unit::ptr_t( new Unit(L"Random point") ),
                                            group );  

    MeasurementSet::ptr_t betaSet =
        MetricHelper::createMeasurementSet( betaAttr,
                                            RATIO,
                                            Unit::ptr_t( new Unit(L"Random point") ),
                                            group );

    // Create some measurement function delegates to do the actual measuring
    MeasurementDelegate alphaDelegate = &ECCClientController::measureAlphaDelegate;
    MeasurementDelegate betaDelegate  = &ECCClientController::measureBetaDelegate;

    // Map delegates to the appropriate measurement set IDs (we'll use these when PULLED by the ECC)
    delegateMap.insert( DelegateMap::value_type( alphaSet->getID(), alphaDelegate ) );
    delegateMap.insert( DelegateMap::value_type( betaSet->getID(),  betaDelegate ) );

    // Wrap up our metric generator and send to the ECC
    MetricGenerator::Set mgSet;
    mgSet.insert( metricGenerator );

    // Send to the ECC
    emiAdapter->sendMetricGenerators( mgSet );

    wcout << L"...sent metric generators to ECC" << endl;
}
    
void ECCClientController::onDiscoveryTimeOut()
{
    /* Not implemented in this demo */
}

void ECCClientController::onSetupMetricGenerator( const UUID& metricGeneratorID, 
                                                  bool* resultOUT )
{
    /* Not implemented in this demo */
}
    
void ECCClientController::onSetupTimeOut( const UUID& metricGeneratorID )
{
    /* Not implemented in this demo */
}
    
void ECCClientController::onLiveMonitoringStarted()
{
    wcout << L"Live monitoring has started" << endl;
}

void ECCClientController::onStartPushingMetricData()
{
    wcout << L"Got command to start pushing (we said we wouldn't, so shouldn't see this)" << endl;
}
    
void ECCClientController::onPushReportReceived( const UUID& lastReportID )
{
    wcout << L"Got push confirmed (shouldn't see this)" << endl;
}
    
void ECCClientController::onStopPushingMetricData()
{
    wcout << L"Got command stop pushing (shouldn't see this)" << endl;
}
    
void ECCClientController::onPullReportReceived( const UUID& reportID )
{
    wcout << L"Got confirmed pull (report ID): " << uuidToWide(reportID) << endl;
}
    
void ECCClientController::onPullMetric( const UUID& measurementSetID, 
                                        Report::ptr_t reportOUT )
{
    wcout << L"Got pull request for measurement set: " << uuidToWide(measurementSetID) << endl;

    // Select the correct delegate to make this measurement (based on measurement set ID)
    DelegateMap::iterator delIt = delegateMap.find( measurementSetID );

    if ( delIt != delegateMap.end() )
    {
        MeasurementDelegate mDelegate = delIt->second;
      
        // Create a measurement report using the delegate call
        Report::ptr_t newReport = mDelegate( shared_from_this(), measurementSetID );
  
        // Copy the report into the OUT paramter of this method (it gets sent to the ECC)
        reportOUT->copyReport( newReport, true );
    }
}
    
void ECCClientController::onPullMetricTimeOut( const UUID& measurementSetID )
{
    /* Not implemented in this demo */
}

void ECCClientController::onPullingStopped()
{
    wcout << L"ECC has stopped pulling metrics" << endl;
}

void ECCClientController::onPopulateSummaryReport( EMPostReportSummary::ptr_t summaryOUT )
{
    /* Not implemented in this demo */
}

void ECCClientController::onPopulateDataBatch( EMDataBatch::ptr_t batchOut )
{
    /* Not implemented in this demo */
}
    
void ECCClientController::onReportBatchTimeOut( const UUID& batchID )
{
    /* Not implemented in this demo */
}

void ECCClientController::onGetTearDownResult( bool* resultOUT )
{
    /* Not implemented in this demo */
}
    
void ECCClientController::onTearDownTimeOut()
{
    /* Not implemented in this demo */
}

// Private methods ------------------------------------------------------------------

Report::ptr_t ECCClientController::measureAlphaDelegate( UUID msID )
{
    wcout << L"Creating measurement for ALPHA" << endl;

    // Just going to create a random number; your specific measurement algorithm would go here
    return createRandomMeasurement( msID );
}

Report::ptr_t ECCClientController::measureBetaDelegate( UUID msID )
{
    wcout << L"Creating measurement for BETA" << endl;

    // Just going to create a random number; your specific measurement algorithm would go here
    return createRandomMeasurement( msID );
}

Report::ptr_t ECCClientController::createRandomMeasurement( UUID msID )
{
    // Get the measurement set requested from our metric generator
    MeasurementSet::ptr_t targetSet = MetricHelper::getMeasurementSet( metricGenerator, msID );
    
    // Create a new report based on the specific measurement set
    Report::ptr_t newReport = MetricHelper::createEmptyMeasurementReport( targetSet );

    // Create random value
    int r = rand();
    r = (r >> 8);
    String value = intToString( r );

    // Encapsulate the raw measurement and insert into the report
    Measurement::ptr_t m = Measurement::ptr_t( new Measurement(value) );
    newReport->getMeasurementSet()->addMeasurement( m );
    newReport->setNumberOfMeasurements( 1 );

    return newReport;
}