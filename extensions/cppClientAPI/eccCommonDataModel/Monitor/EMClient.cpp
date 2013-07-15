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

EMClient::EMClient( const UUID& id, const String& name )
  : isClientDisconnecting( false ),
    isClientConnected( false ),
    currentPhase( eEMUnknownPhase ),
    isClientPhaseAccelerating( false ),
    discoveredGenerators( false ),
    isClientPushCapable( false ),
    isClientPullCapable( false ),
    tearDownSuccessful( false )
{
  clientID = id;
  clientName = name;
}

EMClient::~EMClient()
{
}
    
UUID EMClient::getID()
{
  return clientID;
}
  
String EMClient::getName()
{
  return clientName;
}
  
bool EMClient::isConnected()
{
  return isClientConnected;
}
  
bool EMClient::isDisconnecting()
{
  return isClientDisconnecting;
}
  
bool EMClient::isPhaseAccelerating()
{
  return isClientPhaseAccelerating;
}
  
EMPhase EMClient::getCurrentPhaseActivity()
{
  return currentPhase;
}
  
bool EMClient::supportsPhase( const EMPhase& phase )
{
  // All clients MUST support discovery phase
  if ( phase == eEMDiscoverMetricGeneratorsPhase ) return true;

  if ( supportedPhases.empty() ) return false;

  bool supported = false;

  EMPhaseSet::iterator psIt = supportedPhases.begin();
  while ( psIt != supportedPhases.end() )
  {
    EMPhase nextPhase = *psIt;
    if ( phase == nextPhase )
    {
      supported = true;
      break;
    }

    ++psIt;
  }

  return supported;
}
  
EMPhaseSet EMClient::getCopyOfSupportedPhases()
{
  EMPhaseSet phaseCopy;
  phaseCopy.insert( supportedPhases.begin(), supportedPhases.end() );

  return phaseCopy;
}
  
bool EMClient::getGeneratorDiscoveryResult()
{
  return discoveredGenerators;
}
  
MetricGenerator::Set EMClient::getCopyOfMetricGenerators()
{
  MetricGenerator::Set mgCopies;

  MetricGenerator::Set::iterator copyIt = metricGenerators.begin();
  while ( copyIt != metricGenerators.end() )
  {
    MetricGenerator::ptr_t clone = MetricGenerator::ptr_t( new MetricGenerator( *copyIt ) );
    mgCopies.insert( clone );

    ++copyIt;
  }

  return mgCopies;
}
  
bool EMClient::isPushCapable() 
{
  return isClientPushCapable;
} 

bool EMClient::isPullCapable() 
{
  return isClientPullCapable;
}
  
bool EMClient::isSettingUpMetricGenerator()
{
  return ( !currentMGSetupID.is_nil() );
}
  
bool EMClient::metricGeneratorsSetupOK() 
{
  if ( metricGenerators.empty() )  return false;
  if ( generatorsSetupOK.empty() ) return false;

  MetricGenerator::Set::iterator genIt = metricGenerators.begin();
  while ( genIt != metricGenerators.end() )
  {
    UUID metGenID = (*genIt)->getUUID();
    if ( generatorsSetupOK.find( metGenID ) == generatorsSetupOK.end() ) return false;

    ++genIt;
  }

  return true;
}
  
bool EMClient::isPullingMetricData()
{
  return isQueueingMSPulls;
}  

EMPostReportSummary::ptr_t EMClient::getPostReportSummary()
{
  return postReportSummary;
}
  
bool EMClient::isCreatingPostReportBatchData()
{
  return !postReportOutstandingBatches.empty();
}  

bool EMClient::getTearDownResult()
{
  return tearDownSuccessful;
}
  
EMPhaseTimeOutSet EMClient::getCopyOfSentTimeOuts()
{
  EMPhaseTimeOutSet timeoutSetCopy;
  timeoutSetCopy.insert( timeOutsCalled.begin(), timeOutsCalled.end() );

  return timeoutSetCopy;
}
  
bool EMClient::isNotifiedOfTimeOut( const EMPhaseTimeOut& timeout )
{
  EMPhaseTimeOutSet::const_iterator fIt = timeOutsCalled.find( timeout );

  if ( fIt != timeOutsCalled.end() ) return true;

  return false;
}

} // namespace
