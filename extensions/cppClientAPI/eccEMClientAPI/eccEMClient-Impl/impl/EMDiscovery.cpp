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
//      Created Date :          04-Jul-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "EMDiscovery.h"

#include "AMQPFullInterfaceBase.h"
#include "ModelBase.h"
#include "EMInterfaceType.h"

#include "BoolWrapper.h"
#include "StringWrapper.h"
#include "UUIDWrapper.h"
#include "ArrayWrapper.h"

using namespace ecc_amqpAPI_impl;
using namespace ecc_commonDataModel;
using namespace ecc_emClient_spec;

using namespace boost;
using namespace boost::container;



namespace ecc_emClient_impl
{

EMDiscovery::EMDiscovery( AMQPBasicSubscriptionService::ptr_t sService,
                          AMQPBasicChannel::ptr_t             inChannel,
                          AMQPBasicChannel::ptr_t             outChannel,
                          AMQPMessageDispatch::ptr_t          dispatch,
                          const UUID&                         providerID,
                          const UUID&                         userID,
                          const bool                          isProvider )
: EMBaseInterface( sService, inChannel, outChannel, isProvider )
{
  interfaceName    = L"IEMDiscovery";
  interfaceVersion = L"0.1";
            
  interfaceProviderID = providerID;
  interfaceUserID     = userID;
    
  AMQPFullInterfaceBase::ptr_t fullFace = 
      AMQPFullInterfaceBase::ptr_t( new AMQPFullInterfaceBase( sService, inChannel, outChannel ) );
  
  setAMQPFaceAndDispatch( dynamic_pointer_cast<AbstractAMQPInterface>(fullFace), dispatch );
}

EMDiscovery::~EMDiscovery()
{}
  
// IECCMonitor ---------------------------------------------------------------
void EMDiscovery::setUserListener( IEMDiscovery_UserListener::ptr_t listener)
{ userListener = listener; }

void EMDiscovery::shutdown()
{
  EMBaseInterface::shutdown();

  userListener = NULL;
}

// User methods --------------------------------------------------------------
// Method ID = 8
void EMDiscovery::readyToInitialise()
{
  EXEParamList emptyParams;

  executeMethod( 8, emptyParams );
}
  
// Method ID = 9
void EMDiscovery::sendActivePhases( const EMPhaseSet& supportedPhases )
{
  EXEParamList paramsList;

  ArrayWrapper::ptr_t phaseList = ArrayWrapper::ptr_t( new ArrayWrapper() );

  EMPhaseSet::const_iterator spIt = supportedPhases.begin();
  while ( spIt != supportedPhases.end() )
  {
    EMPhase phase = *spIt;

    switch ( phase )
    {
    case eEMDiscoverMetricGeneratorsPhase : 
      {
        StringWrapper::ptr_t sw = StringWrapper::ptr_t( new StringWrapper(L"eEMDiscoverMetricGenerators") );
        phaseList->addModel( sw );
      } break;
    
    case eEMSetUpMetricGeneratorsPhase : 
      {
        StringWrapper::ptr_t sw = StringWrapper::ptr_t( new StringWrapper(L"eEMSetUpMetricGenerators") );
        phaseList->addModel( sw );
      } break;
    
    case eEMLiveMonitoringPhase : 
      {
        StringWrapper::ptr_t sw = StringWrapper::ptr_t( new StringWrapper(L"eEMLiveMonitoring") );
        phaseList->addModel( sw );
      } break;
    
    case eEMPostMonitoringReportPhase : 
      {
        StringWrapper::ptr_t sw = StringWrapper::ptr_t( new StringWrapper(L"eEMPostMonitoringReport") );
        phaseList->addModel( sw );
      } break;
    
    case eEMTearDownPhase : 
      {
        StringWrapper::ptr_t sw = StringWrapper::ptr_t( new StringWrapper(L"eEMTearDown") );
        phaseList->addModel( sw );
      } break;
    }

    ++spIt;
  }

  paramsList.push_back( phaseList );
    
  executeMethod( 9, paramsList );
}
  
// Method ID = 10
void EMDiscovery::sendDiscoveryResult( const bool discoveredGenerators )
{
  EXEParamList paramsList;

  BoolWrapper::ptr_t bw = BoolWrapper::ptr_t( new BoolWrapper(discoveredGenerators) );
  paramsList.push_back( bw );
    
  executeMethod( 10, paramsList );
}
  
// Method ID = 11
void EMDiscovery::sendMetricGeneratorInfo( const MetricGenerator::Set& generators )
{
  EXEParamList paramsList;

  ArrayWrapper::ptr_t mgList = ArrayWrapper::ptr_t( new ArrayWrapper() );

  MetricGenerator::Set::const_iterator mgIt = generators.begin();
  while ( mgIt != generators.end() )
  {
    mgList->addModel( *mgIt );
    ++mgIt;
  }

  paramsList.push_back( mgList );
    
  executeMethod( 11, paramsList );
}

// Method ID = 14
void EMDiscovery::enableEntityMetricCollection( const UUID& entityID, const bool enabled )
{
  EXEParamList paramsList;

  paramsList.push_back( UUIDWrapper::ptr_t( new UUIDWrapper(entityID) ) );

  paramsList.push_back( BoolWrapper::ptr_t( new BoolWrapper(enabled) ) );
    
  executeMethod( 14, paramsList );
}
  
// Method ID = 12
void EMDiscovery::clientDisconnecting()
{
  EXEParamList emptyParams;

  executeMethod( 12, emptyParams );
}
  
// Protected methods ---------------------------------------------------------
void EMDiscovery::onInterpretMessage( const int& methodID, const JSONTree& jsonTree )
{
  switch ( methodID )
  {
    case ( 1 ) :
    {
      if ( userListener )
      {
        JSONTreeIt tIt = jsonTree.begin(); // Get past method ID first
        ++tIt;

        // Need to convert from enum string values manually here
        String enumVal = getJSON_String( *tIt );

        EMInterfaceType faceType = eEMUnknownInface;

        if ( enumVal.compare(L"eEMSetup") == 0 )            faceType = eEMSetup;
        else if ( enumVal.compare(L"eEMLiveMonitor") == 0 ) faceType = eEMLiveMonitor;
        else if ( enumVal.compare(L"eEMPostReport") == 0 )  faceType = eEMPostReport;
        else if ( enumVal.compare(L"eEMTearDown") == 0  )   faceType = eEMTearDown;

        if (faceType != eEMUnknownInface)
          userListener->onCreateInterface( interfaceProviderID, faceType );
      }
        
    } break;
        
    case ( 2 ) :
    {
      if ( userListener )
      {
        JSONTreeIt tIt = jsonTree.begin(); // Get past method ID first
        ++tIt;

        bool confirmed       = getJSON_bool( *tIt );      ++tIt;
        UUID expUniqueID     = getJSON_UUID( *tIt );      ++tIt;
        String expNameID     = getJSON_String( *tIt);     ++tIt;
        String expName       = getJSON_String( *tIt);     ++tIt;
        String expDesc       = getJSON_String( *tIt);     ++tIt;
        TimeStamp createTime = getJSON_TimeStamp( *tIt ); ++tIt;

        userListener->onRegistrationConfirmed( interfaceProviderID, 
                                               confirmed, expUniqueID, 
                                               expNameID, expName, expDesc,
                                               createTime );
      }
    } break;
        
    case ( 3 ) :
    {
      if ( userListener )
        userListener->onRequestActivityPhases( interfaceProviderID );
        
    } break;
        
    case ( 4 ) :
    {
      if ( userListener )
        userListener->onDiscoverMetricGenerators( interfaceProviderID );
        
    } break;
        
    case ( 5 ) :
    {
      if ( userListener )
        userListener->onRequestMetricGeneratorInfo( interfaceProviderID );
        
    } break;
        
    case ( 6 ) :
    {
      if ( userListener )
        userListener->onDiscoveryTimeOut( interfaceProviderID );
        
    } break;
        
    case ( 7 ) :
    {
      if ( userListener )
      {
        JSONTreeIt tIt = jsonTree.begin(); // Get past method ID first
        ++tIt;

        String endPoint = getJSON_String( *tIt );
        userListener->onSetStatusMonitorEndpoint( interfaceProviderID, endPoint );
      } 
        
    } break;
          
    case ( 13 ) :
    {
      if ( userListener )
      {
        JSONTreeIt tIt = jsonTree.begin(); // Get past method ID first
        ++tIt;

        String reason = getJSON_String( *tIt );
        userListener->onDeregisteringThisClient( interfaceProviderID, reason );
      }
        
    } break;
  }
}

} // namespace