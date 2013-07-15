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
#include "EMMonitorEntryPoint.h"

#include "AMQPHalfInterfaceBase.h"
#include "StringWrapper.h"
#include "UUIDWrapper.h"

using namespace ecc_amqpAPI_impl;
using namespace ecc_commonDataModel;

using namespace boost;
using namespace boost::container;



namespace ecc_emClient_impl
{

EMMonitorEntryPoint::EMMonitorEntryPoint( AMQPBasicSubscriptionService::ptr_t sService,
                                          AMQPBasicChannel::ptr_t             channel,
                                          AMQPMessageDispatch::ptr_t          dispatch,
                                          const UUID&                         providerID,
                                          bool                                isProvider ) 
: EMBaseInterface( sService, channel, isProvider )
{
    
  interfaceName    = L"IECCMonitorEntryPoint";
  interfaceVersion = L"0.1";
    
  interfaceProviderID = providerID;

  AMQPHalfInterfaceBase::ptr_t halfFace = 
      AMQPHalfInterfaceBase::ptr_t( new AMQPHalfInterfaceBase( sService, channel ) );
  
  setAMQPFaceAndDispatch( dynamic_pointer_cast<AbstractAMQPInterface>(halfFace), dispatch );
}

EMMonitorEntryPoint::~EMMonitorEntryPoint()
{
}
  
// IECCMonitorEntryPoint -----------------------------------------------------
// Method ID = 1
void EMMonitorEntryPoint::registerAsEMClient( const UUID& userID, const String& userName )
{
  EXEParamList paramsList;

  paramsList.push_back( ModelBase::ptr_t( new UUIDWrapper(userID) ) );
  paramsList.push_back( ModelBase::ptr_t( new StringWrapper(userName) ) );
    
  executeMethod( 1, paramsList );
}

void EMMonitorEntryPoint::onInterpretMessage( const int& methodID, const JSONTree& jsonTree )
{
  // Not implemented for the client side
}

} // namespace