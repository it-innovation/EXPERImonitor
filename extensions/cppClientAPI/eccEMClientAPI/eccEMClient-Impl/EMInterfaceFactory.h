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

#pragma once

#include "IAMQPMessageDispatchPump.h"
#include "IAMQPMessageDispatch.h"

#include "IEMMonitorEntryPoint.h"
#include "IEMDiscovery.h"
#include "IEMMetricGenSetup.h"
#include "IEMLiveMonitor.h"
#include "IEMPostReport.h"
#include "IEMTearDown.h"

#include "AMQPBasicSubscriptionService.h"
#include "AMQPBasicChannel.h"

#include "ECCUtils.h"




namespace ecc_emClient_impl
{

/**
  * EMInterfaceFactory is a simple factory class that generates EM 
  * client/producer interfaces.
  * 
  * @author sgc
  */
class EMInterfaceFactory
{
public:

  typedef boost::shared_ptr<EMInterfaceFactory> ptr_t;

    /**
      * Construction of the factory requires a properly constructed AMQPBasicChannel
      * (see the AMQPConnectionFactory) and a flag as to whether the factory will
      * create 'user' or 'provider' interfaces. Only 'createProviders' if you are
      * implementing an EM yourself, otherwise you should act as a user.
      * 
      * @param channel         - A properly configured AMQP channel
      * @param createProviders - Set a 'false' to act as a user
      */
    EMInterfaceFactory( ecc_amqpAPI_impl::AMQPBasicSubscriptionService::ptr_t service,
                        ecc_amqpAPI_impl::AMQPBasicChannel::ptr_t             channel, 
                        bool                                                  createProviders );

    /**
      * Creates a message dispatch pump that controls AMQP message subscriptions
      * 
      * @param name      - Name of the pump (creates an associated Thread of the same name)
      * @param priority  - The processing resource to be allocated to the pump
      * @return          - Returns an instance of the pump
      */
    ecc_amqpAPI_spec::IAMQPMessageDispatchPump::ptr_t createDispatchPump( const String& name,
                                                                          const ecc_amqpAPI_spec::ePumpPriority priority );

    /**
      * Creates a dispatch that is a) added to a pump for message processing and
      * b) assigned to an AMQP interface to listen for specific message types.
      * (See the interface creation methods)
      * 
      * @return - Returns an instance of a dispatch
      */
    ecc_amqpAPI_spec::IAMQPMessageDispatch::ptr_t createDispatch();

    /**
      * Creates an 'Entry-point' interface to the EM. Users should use this interface
      * to initialise a connection to the EM.
      * 
      * @param providerID    - UUID of the EM with which to connect. Must be pre-determined.
      * @param dispatch      - Dispatch used to process the messages of this interface.
      * @return              - Instance of this interface.
      */
    ecc_emClient_spec::IEMMonitorEntryPoint::ptr_t createEntryPoint( const UUID& providerID,
                                                                     ecc_amqpAPI_spec::IAMQPMessageDispatch::ptr_t dispatch );

    /**
      * Creates an 'Discovery' interface connection with the EM
      * 
      * Users must use this interface to describe which phases of the monitoring
      * process they support and what MetricGenerators they are able to provide.
      * 
      * @param providerID    - UUID of the EM with which to connect. Must be pre-determined.
      * @param dispatch      - Dispatch used to process the messages of this interface.
      * @return              - Instance of this interface.
      */
    ecc_emClient_spec::IEMDiscovery::ptr_t createDiscovery( const UUID& providerID,
                                                            const UUID& userID,
                                                            ecc_amqpAPI_spec::IAMQPMessageDispatch::ptr_t dispatch );

    /**
      * Creates an 'Setup' interface connection with the EM
      * 
      * Users can use this interface to coordinate specific setting up processes
      * of their MetricGenerators with the EM.
      * 
      * @param providerID    - UUID of the EM with which to connect. Must be pre-determined.
      * @param dispatch      - Dispatch used to process the messages of this interface.
      * @return              - Instance of this interface.
      */
    ecc_emClient_spec::IEMMetricGenSetup::ptr_t createSetup( const UUID& providerID,
                                                             const UUID& userID,
                                                             ecc_amqpAPI_spec::IAMQPMessageDispatch::ptr_t dispatch );

    /**
      * Creates an 'Live Monitor' interface connection with the EM
      * 
      * Users can use this interface to send (through pushing or pulling) live 
      * metric data to the EM.
      * 
      * @param providerID    - UUID of the EM with which to connect. Must be pre-determined.
      * @param dispatch      - Dispatch used to process the messages of this interface.
      * @return              - Instance of this interface.
      */
    ecc_emClient_spec::IEMLiveMonitor::ptr_t createLiveMonitor( const UUID& providerID,
                                                                const UUID& userID,
                                                                ecc_amqpAPI_spec::IAMQPMessageDispatch::ptr_t dispatch );

    /**
      * Creates an 'Post Report' interface connection with the EM
      * 
      * Users can use this interface to send metric data that could not be sent
      * during the live monitoring process in non-real-time batched form.
      * 
      * @param providerID    - UUID of the EM with which to connect. Must be pre-determined.
      * @param dispatch      - Dispatch used to process the messages of this interface.
      * @return              - Instance of this interface.
      */
    ecc_emClient_spec::IEMPostReport::ptr_t createPostReport( const UUID& providerID,
                                                              const UUID& userID,
                                                              ecc_amqpAPI_spec::IAMQPMessageDispatch::ptr_t dispatch );

    /**
      * Creates an 'Tear-down' interface connection with the EM
      * 
      * Users can use this interface coordinate and report on any specific
      * tear-down processes associated with the monitoring process.
      * 
      * @param providerID    - UUID of the EM with which to connect. Must be pre-determined.
      * @param dispatch      - Dispatch used to process the messages of this interface.
      * @return              - Instance of this interface.
      */
    ecc_emClient_spec::IEMTearDown::ptr_t createTearDown( const UUID& providerID,
                                                          const UUID& userID,
                                                          ecc_amqpAPI_spec::IAMQPMessageDispatch::ptr_t dispatch );

private:

  ecc_amqpAPI_impl::AMQPBasicSubscriptionService::ptr_t amqpSubscriptService;
  ecc_amqpAPI_impl::AMQPBasicChannel::ptr_t             amqpChannel;
  bool                                                  generateProviders;
};

} // namespace
