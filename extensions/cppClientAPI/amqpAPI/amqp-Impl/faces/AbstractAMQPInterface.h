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
//      Created Date :          15-May-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#pragma once

#include "AMQPBasicChannel.h"
#include "AMQPBasicSubscriptionService.h"
#include "AMQPBasicSubscriptionProcessor.h"
#include "AMQPMessageDispatch.h"




namespace ecc_amqpAPI_impl
{

  class AbstractAMQPInterface
  {
  public:

    typedef boost::shared_ptr<AbstractAMQPInterface> ptr_t;

    void shutdown();
  
    bool sendBasicMessage( const String& message );

    void setMessageDispatch( AMQPMessageDispatch::ptr_t dispatch );
  
  protected:

    AbstractAMQPInterface( AMQPBasicSubscriptionService::ptr_t sService,
                           AMQPBasicChannel::ptr_t             channel );

    virtual ~AbstractAMQPInterface();

    virtual void createInterfaceExchangeNames( const String& iName );

    virtual void createQueue();
  
    virtual void createSubscriptionComponent();

    virtual void assignBindings();

    AMQPBasicChannel::ptr_t    amqpChannel;
    AMQPMessageDispatch::ptr_t msgDispatch;

    String interfaceName;
    String providerExchangeName, userExchangeName;
    String providerQueueName,    userQueueName;
    String providerRoutingKey,   userRoutingKey;
    String subListenQueue;
    bool   interfaceReady; // = false;
    bool   actingAsProvider;

  private:
      //IECCLogger amqpIntLogger = Logger.getLogger(typeof(AbstractAMQPInterface));
      boost::mutex sendMessageMutex;
    
      AMQPBasicSubscriptionService::ptr_t   subscriptService;
      AMQPBasicSubscriptionProcessor::ptr_t subProcessor;
  };

} // namespace
