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
//      Created Date :          05-Jul-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "EMInterfaceFactory.h"

#include "EMMonitorEntryPoint.h"
#include "EMDiscovery.h"
#include "EMMetricGenSetup.h"
#include "EMLiveMonitor.h"
#include "EMPostReport.h"
#include "EMTearDown.h"

using namespace ecc_amqpAPI_spec;
using namespace ecc_amqpAPI_impl;
using namespace ecc_emClient_spec;

using namespace boost;


namespace ecc_emClient_impl
{

EMInterfaceFactory::EMInterfaceFactory( AMQPBasicSubscriptionService::ptr_t service,
                                        AMQPBasicChannel::ptr_t             channel, 
                                        bool                                createProviders )
{
  amqpSubscriptService = service;
  amqpChannel          = channel;
  generateProviders    = createProviders;
}

IAMQPMessageDispatchPump::ptr_t EMInterfaceFactory::createDispatchPump( const String& name,
                                                                        const ecc_amqpAPI_spec::ePumpPriority priority )
{
    return IAMQPMessageDispatchPump::ptr_t( new AMQPMessageDispatchPump(name, priority) );
}

IAMQPMessageDispatch::ptr_t EMInterfaceFactory::createDispatch()
{
  return IAMQPMessageDispatch::ptr_t( new AMQPMessageDispatch() );
}

IEMMonitorEntryPoint::ptr_t EMInterfaceFactory::createEntryPoint( const UUID& providerID,
                                                                  IAMQPMessageDispatch::ptr_t dispatch )
{
  return IEMMonitorEntryPoint::ptr_t( new EMMonitorEntryPoint( amqpSubscriptService, 
                                                               amqpChannel,
                                                               dynamic_pointer_cast<AMQPMessageDispatch>( dispatch ),
                                                               providerID,
                                                               generateProviders ) );
}

IEMDiscovery::ptr_t EMInterfaceFactory::createDiscovery( const UUID& providerID,
                                                         const UUID& userID,
                                                         IAMQPMessageDispatch::ptr_t dispatch )
{
  return IEMDiscovery::ptr_t ( new EMDiscovery( amqpSubscriptService,
                                                amqpChannel,
                                                dynamic_pointer_cast<AMQPMessageDispatch>( dispatch ),
                                                providerID,
                                                userID,
                                                generateProviders) );
}

IEMMetricGenSetup::ptr_t EMInterfaceFactory::createSetup( const UUID& providerID,
                                                          const UUID& userID,
                                                          IAMQPMessageDispatch::ptr_t dispatch )
{
  return IEMMetricGenSetup::ptr_t( new EMMetricGenSetup( amqpSubscriptService, 
                                                         amqpChannel,
                                                         dynamic_pointer_cast<AMQPMessageDispatch>( dispatch ),
                                                         providerID,
                                                         userID,
                                                         generateProviders) );
}

IEMLiveMonitor::ptr_t EMInterfaceFactory::createLiveMonitor( const UUID& providerID,
                                                             const UUID& userID,
                                                             IAMQPMessageDispatch::ptr_t dispatch )
{
  return IEMLiveMonitor::ptr_t ( new EMLiveMonitor( amqpSubscriptService, 
                                                    amqpChannel,
                                                    dynamic_pointer_cast<AMQPMessageDispatch>( dispatch ),
                                                    providerID,
                                                    userID,
                                                    generateProviders) );
}

IEMPostReport::ptr_t EMInterfaceFactory::createPostReport( const UUID& providerID,
                                                           const UUID& userID,
                                                           IAMQPMessageDispatch::ptr_t dispatch )
{
  return IEMPostReport::ptr_t ( new EMPostReport( amqpSubscriptService, 
                                                  amqpChannel,
                                                  dynamic_pointer_cast<AMQPMessageDispatch>( dispatch ),
                                                  providerID,
                                                  userID,
                                                  generateProviders) );
}

IEMTearDown::ptr_t EMInterfaceFactory::createTearDown( const UUID& providerID,
                                                       const UUID& userID,
                                                       IAMQPMessageDispatch::ptr_t dispatch )
{
  return IEMTearDown::ptr_t ( new EMTearDown( amqpSubscriptService, 
                                              amqpChannel,
                                              dynamic_pointer_cast<AMQPMessageDispatch>( dispatch ),
                                              providerID,
                                              userID,
                                              generateProviders) );
}

} // namespace
