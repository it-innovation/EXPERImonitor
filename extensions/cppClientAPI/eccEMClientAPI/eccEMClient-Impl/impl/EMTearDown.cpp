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
#include "EMTearDown.h"

#include "AMQPFullInterfaceBase.h"
#include "ModelBase.h"

using namespace ecc_emClient_spec;
using namespace ecc_amqpAPI_impl;
using namespace ecc_commonDataModel;

using namespace boost;
using namespace boost::container;



namespace ecc_emClient_impl
{
  
EMTearDown::EMTearDown( AMQPBasicSubscriptionService::ptr_t sService,
                        AMQPBasicChannel::ptr_t             channel,
                        AMQPMessageDispatch::ptr_t          dispatch,
                        const UUID&                         providerID,
                        const UUID&                         userID,
                        const bool                          isProvider )
: EMBaseInterface( sService, channel, isProvider )
{
  interfaceName    = L"IEMTearDown";
  interfaceVersion = L"0.1";
            
  interfaceProviderID = providerID;
  interfaceUserID     = userID;
    
  AMQPFullInterfaceBase::ptr_t fullFace = 
      AMQPFullInterfaceBase::ptr_t( new AMQPFullInterfaceBase( sService, channel ) );
  
  setAMQPFaceAndDispatch( fullFace, dispatch );
}

EMTearDown::~EMTearDown()
{
}

// IECCTearDown --------------------------------------------------------------
void EMTearDown::setUserListener( IEMTearDown_UserListener::ptr_t listener )
{ userListener = listener; }
  
// User methods --------------------------------------------------------------
// Method ID = 3
void EMTearDown::notifyReadyToTearDown()
{
  executeMethod( 3, EXEParamList() );
}
  
// Method ID = 4
void EMTearDown::sendTearDownResult( const bool success )
{
  EXEParamList paramsList;

  //paramsList.Add( success );
    
  executeMethod( 4, paramsList );
}
  
// Protected methods ---------------------------------------------------------  
void EMTearDown::onInterpretMessage( const int& methodID, 
                                     const JSONTree& jsonTree )
{
  switch ( methodID )
  {
    case ( 1 ) :
    {
      if ( userListener )
        userListener->onTearDownMetricGenerators( interfaceProviderID );
        
    } break;
        
    case ( 2 ) :
    {
      if ( userListener )
        userListener->onTearDownTimeOut( interfaceProviderID );
        
    } break;
  }
}

} // namespace
