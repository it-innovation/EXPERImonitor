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
#include "EMLiveMonitor.h"

#include "AMQPFullInterfaceBase.h"
#include "ModelBase.h"

using namespace ecc_emClient_spec;
using namespace ecc_amqpAPI_impl;
using namespace ecc_commonDataModel;

using namespace boost;
using namespace boost::container;



namespace ecc_emClient_impl
{

EMLiveMonitor::EMLiveMonitor( AMQPBasicSubscriptionService::ptr_t sService,
                              AMQPBasicChannel::ptr_t             channel,
                              AMQPMessageDispatch::ptr_t          dispatch,
                              const UUID&                         providerID,
                              const UUID&                         userID,
                              const bool                          isProvider )
: EMBaseInterface( sService, channel, isProvider )
{
  interfaceName    = L"IEMLiveMonitor";
  interfaceVersion = L"0.1";
            
  interfaceProviderID = providerID;
  interfaceUserID     = userID;
    
  AMQPFullInterfaceBase::ptr_t fullFace = 
      AMQPFullInterfaceBase::ptr_t( new AMQPFullInterfaceBase( sService, channel ) );
  
  setAMQPFaceAndDispatch( dynamic_pointer_cast<AbstractAMQPInterface>(fullFace), dispatch );
}

EMLiveMonitor::~EMLiveMonitor()
{
}

void EMLiveMonitor::shutdown()
{
  EMBaseInterface::shutdown();

  userListener = NULL;
}

// IEMLiveMonitor ------------------------------------------------------------
void EMLiveMonitor::setUserListener( IEMLiveMonitor_UserListener::ptr_t listener )
{ userListener = listener; }
  
// User methods --------------------------------------------------------------
// Method ID = 7
void EMLiveMonitor::notifyReadyToPush()
{
  EXEParamList emptyParams;

  executeMethod( 7, emptyParams );
}
  
// Method ID = 8
void EMLiveMonitor::pushMetric( Report::ptr_t report )
{
  EXEParamList paramsList;

  paramsList.push_back( report );
    
  executeMethod( 8, paramsList );
}
  
// Method ID = 9
void EMLiveMonitor::notifyPushingCompleted()
{
  EXEParamList emptyParams;

  executeMethod( 9, emptyParams );
}
  
// Method ID = 10
void EMLiveMonitor::notifyReadyForPull()
{
  EXEParamList emptyParams;

  executeMethod( 10, emptyParams );
}
  
// Method ID = 11
void EMLiveMonitor::sendPulledMetric( Report::ptr_t report )
{
  EXEParamList paramsList;

  paramsList.push_back( report );
    
  executeMethod( 11, paramsList );
}
  
// Protected methods ---------------------------------------------------------
void EMLiveMonitor::onInterpretMessage( const int& methodID, const JSONTree& jsonTree )
{    
  switch ( methodID )
  {
    case ( 1 ) :
    {
      if ( userListener )
        userListener->onStartPushing( interfaceProviderID );
        
    } break;
        
    case ( 2 ) :
    {
      if ( userListener )
      {
        JSONTreeIt tIt = jsonTree.begin(); // Get past method ID first
        ++tIt;

        UUID reportID = getJSON_UUID( *tIt );

        userListener->onReceivedPush( interfaceProviderID, reportID );
      }
        
    } break;
        
    case ( 3 ) :
    {
      if ( userListener )
        userListener->onStopPushing( interfaceProviderID );
        
    } break;
        
    case ( 4 ) :
    {
      if ( userListener )
      {
        JSONTreeIt tIt = jsonTree.begin(); // Get past method ID first
        ++tIt;

        UUID msID = getJSON_UUID( *tIt );

        userListener->onPullMetric( interfaceProviderID, msID );
      }
        
    } break;
        
    case ( 5 ) :
    {
      if ( userListener )
      {
        JSONTreeIt tIt = jsonTree.begin(); // Get past method ID first
        ++tIt;

        UUID msID = getJSON_UUID( *tIt );

        userListener->onPullMetricTimeOut( interfaceProviderID, msID );
      }
        
    } break;
        
    case ( 6 ) :
    {
      if ( userListener )
        userListener->onPullingStopped( interfaceProviderID );
        
    } break;
        
    case ( 12 ) :
    {
      if ( userListener )
      {
        JSONTreeIt tIt = jsonTree.begin(); // Get past method ID first
        ++tIt;

        UUID reportID = getJSON_UUID( *tIt );

        userListener->onReceivedPull( interfaceProviderID, reportID );
      }
    } break;
  }
}

} // namespace