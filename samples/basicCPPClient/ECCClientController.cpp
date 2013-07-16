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

using namespace ecc_commonDataModel;

using namespace boost;
using namespace std;



ECCClientController::ECCClientController()
{
}


ECCClientController::~ECCClientController()
{
  emiAdapter = NULL;
  amqpChannel = NULL;
}

void ECCClientController::start( const String& rabbitServerIP,
                                 const UUID&   expMonitorID,
                                 const UUID&   clientID )
{
  if ( !rabbitServerIP.empty() )
  {
    wcout << L"Attempting to connect to Rabbit" << endl;

    try
    {
      amqpFactory.setAMQPHostIPAddress( rabbitServerIP );
      amqpFactory.connectToAMQPHost();
      amqpChannel = amqpFactory.createNewChannel();

      cout << "Connected and channel created." << endl;

      TimeStamp date = getCurrentTime();
      clientName     = L"C++ Client (" + timeStampToString(date) + L")";

      emiAdapter = 
        EMInterfaceAdapter::ptr_t( new EMInterfaceAdapter( shared_from_this() ) );

      emiAdapter->registerWithEM( clientName,
                                  amqpChannel,
                                  expMonitorID,
                                  clientID );
    }
    catch ( const String& e ) 
    { wcout << L"Had problems connecting to RabbitMQ: " << e; }
  }
}

void ECCClientController::stop()
{
  emiAdapter->disconnectFromEM();
}

// EMInterfaceAdapterListener ------------------------------------------------
void ECCClientController::onEMConnectionResult( const bool              connected, 
                                                const Experiment::ptr_t expInfo )
{
  wcout << L"Got connection result: " ;

  if ( connected )
  {
    wcout << L"CONNECTION OK" << endl << endl;

    wcout << L"Linked to experiment: " + expInfo->getExperimentID() << endl;
    
  }
  else
    wcout << L"CONNECTION REFUSED BY ECC" << endl;
}
    
void ECCClientController::onEMDeregistration( const String& reason )
{
  wcout << L"Got disconnection from the ECC: " << reason << endl;

  stop();

  wcout << L"Client has stopped." << endl;
}
    
void ECCClientController::onDescribeSupportedPhases( EMPhaseSet& phasesOUT )
{
  // We are going to just support live monitoring
  // ... we MUST support the discovery phase by default, but don't need to include

  wcout << L"Specifying phase compatibility (Live monitoring only)" << endl;

  phasesOUT.insert( ecc_commonDataModel::eEMLiveMonitoringPhase );

}

void ECCClientController::onDescribePushPullBehaviours( bool* pushPullOUT )
{
  // We're going to support both push and pull
  wcout << L"Specifying only pull behaviour" << endl;

  pushPullOUT[0] = false;  // In this demo we are just going to be pull
  pushPullOUT[1] = true;   // ECC will pull metrics from this client
}
    
void ECCClientController::onPopulateMetricGeneratorInfo()
{
  wcout << "Creating metric generator info..." << endl;

  Entity::ptr_t app = Entity::ptr_t( new Entity( createRandomUUID(),
                                                 L"Client app",
                                                 L"The thing we are observing") );

  Attribute::ptr_t phyRAMUage = 
    MetricHelper::createAttribute( L"Random stream Alpha",
                                   L"A random number stream",
                                   app );

  Attribute::ptr_t virtualMEMUsage 
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
                                     L"Memory metrics relating to the app",
                                     metricGenerator );

  // Create measurment sets for our two metrics
  MetricHelper::createMeasurementSet( phyRAMUage,
                                      RATIO,
                                      Unit::ptr_t( new Unit(L"Random point") ),
                                      group );
                                                             

  MetricHelper::createMeasurementSet( virtualMEMUsage,
                                      RATIO,
                                      Unit::ptr_t( new Unit(L"Random point") ),
                                      group );

  // Wrap up our metric generator and send to the ECC
  MetricGenerator::Set mgSet;
  mgSet.insert( metricGenerator );

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

  MeasurementSet::ptr_t targetSet = MetricHelper::getMeasurementSet( metricGenerator, measurementSetID );

  if ( targetSet )
  {
    Report::ptr_t newReport = MetricHelper::createEmptyMeasurementReport( targetSet );

    int r = rand();
    r = (r >> 8);
    String value = intToString( r );

    Measurement::ptr_t m = Measurement::ptr_t( new Measurement(value) );
    newReport->getMeasurementSet()->addMeasurement( m );
    newReport->setNumberOfMeasurements( 1 );

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