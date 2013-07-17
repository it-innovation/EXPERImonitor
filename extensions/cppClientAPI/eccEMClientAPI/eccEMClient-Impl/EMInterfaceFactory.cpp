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

EMInterfaceFactory::EMInterfaceFactory( AMQPBasicChannel::ptr_t channel, 
                                        bool                    createProviders )
{
  amqpChannel       = channel;
  generateProviders = createProviders;

  amqpSubscriptService = AMQPBasicSubscriptionService::ptr_t( new AMQPBasicSubscriptionService() );
  amqpSubscriptService->startService( 100 );
}

EMInterfaceFactory::~EMInterfaceFactory()
{
  amqpSubscriptService->stopService();
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
  EMMonitorEntryPoint::ptr_t ep( new EMMonitorEntryPoint( amqpSubscriptService, 
                                                          amqpChannel,
                                                          dynamic_pointer_cast<AMQPMessageDispatch>( dispatch ),
                                                          providerID,
                                                          generateProviders ) );
  ep->initialiseAMQPComms();

  return ep;  
}

IEMDiscovery::ptr_t EMInterfaceFactory::createDiscovery( const UUID& providerID,
                                                         const UUID& userID,
                                                         IAMQPMessageDispatch::ptr_t dispatch )
{
  EMDiscovery::ptr_t disc ( new EMDiscovery( amqpSubscriptService,
                                             amqpChannel,
                                             dynamic_pointer_cast<AMQPMessageDispatch>( dispatch ),
                                             providerID,
                                             userID,
                                             generateProviders) );
  disc->initialiseAMQPComms();

  return disc;
}

IEMMetricGenSetup::ptr_t EMInterfaceFactory::createSetup( const UUID& providerID,
                                                          const UUID& userID,
                                                          IAMQPMessageDispatch::ptr_t dispatch )
{
  EMMetricGenSetup::ptr_t mgs( new EMMetricGenSetup( amqpSubscriptService, 
                                                     amqpChannel,
                                                     dynamic_pointer_cast<AMQPMessageDispatch>( dispatch ),
                                                     providerID,
                                                     userID,
                                                     generateProviders) );
  mgs->initialiseAMQPComms();

  return mgs;
}

IEMLiveMonitor::ptr_t EMInterfaceFactory::createLiveMonitor( const UUID& providerID,
                                                             const UUID& userID,
                                                             IAMQPMessageDispatch::ptr_t dispatch )
{
  EMLiveMonitor::ptr_t lm( new EMLiveMonitor( amqpSubscriptService, 
                                              amqpChannel,
                                              dynamic_pointer_cast<AMQPMessageDispatch>( dispatch ),
                                              providerID,
                                              userID,
                                              generateProviders) );
  lm->initialiseAMQPComms();

  return lm;
}

IEMPostReport::ptr_t EMInterfaceFactory::createPostReport( const UUID& providerID,
                                                           const UUID& userID,
                                                           IAMQPMessageDispatch::ptr_t dispatch )
{
  EMPostReport::ptr_t pr( new EMPostReport( amqpSubscriptService, 
                                            amqpChannel,
                                            dynamic_pointer_cast<AMQPMessageDispatch>( dispatch ),
                                            providerID,
                                            userID,
                                            generateProviders) );
  pr->initialiseAMQPComms();

  return pr;
}

IEMTearDown::ptr_t EMInterfaceFactory::createTearDown( const UUID& providerID,
                                                       const UUID& userID,
                                                       IAMQPMessageDispatch::ptr_t dispatch )
{
  EMTearDown::ptr_t td( new EMTearDown( amqpSubscriptService, 
                                        amqpChannel,
                                        dynamic_pointer_cast<AMQPMessageDispatch>( dispatch ),
                                        providerID,
                                        userID,
                                        generateProviders) );
  td->initialiseAMQPComms();

  return td;
}

} // namespace
