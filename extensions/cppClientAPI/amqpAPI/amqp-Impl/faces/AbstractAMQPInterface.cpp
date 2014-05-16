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

#include "stdafx.h"

#include "AbstractAMQPInterface.h"

#include <boost/thread/lock_guard.hpp>

using namespace AmqpClient;

using namespace boost;
using namespace std;



namespace ecc_amqpAPI_impl
{
 
void AbstractAMQPInterface::shutdown()
{
  // Deregister from subscription service
  if ( subscriptService && subProcessor ) 
    subscriptService->unsubscribe( subProcessor->getProcessorID() );

  if ( amqpChannel )
  {
    if ( amqpChannel->isOpen() )
    {
       // Clear up queue, if it exists
      if ( !subListenQueue.empty() )
      {
        AmqpClient::Channel::ptr_t channelImpl = amqpChannel->getChannelImpl();
        if ( channelImpl )
        {
          channelImpl->DeleteQueue( toNarrow(subListenQueue) );
                  
          subProcessor     = NULL;
          subscriptService = NULL;
          msgDispatch      = NULL;
        }
      }
    }

    // Tidy up (channel is managed elsewhere)
    interfaceName.clear();
    providerExchangeName.clear();
    userExchangeName.clear();
    providerQueueName.clear();
    userQueueName.clear();
    providerRoutingKey.clear();
    userRoutingKey.clear();
    subListenQueue.clear();
    interfaceReady = false;
  }
}
  
bool AbstractAMQPInterface::sendBasicMessage( const String& message )
{
  // Safety first
  if ( !interfaceReady   || 
        message.empty()  ||
        (providerRoutingKey.empty() && userRoutingKey.empty()) ) 
    return false;

  bool result = false;

  {
    lock_guard<mutex> lock( sendMessageMutex );

    // Make sure producer sends to user (or other way around) - targets are reversed
    wstring& targetExchange = actingAsProvider ? userExchangeName : providerExchangeName;
    wstring& targetRouteKey = actingAsProvider ? userRoutingKey   : providerRoutingKey;
    
    if ( amqpChannel->isOpen() )
    {
      AmqpClient::Channel::ptr_t channelImpl = amqpChannel->getChannelImpl();
      if ( channelImpl )
      {
        BasicMessage::ptr_t bMsg = BasicMessage::Create( toNarrow(message) );

        channelImpl->BasicPublish( toNarrow(targetExchange),
                                   toNarrow(targetRouteKey),
                                   bMsg,
                                   false,
                                   false );
        result = true;
      }
    }
  }

  return result;
}

void AbstractAMQPInterface::setMessageDispatch( AMQPMessageDispatch::ptr_t dispatch )
{ msgDispatch = dispatch; }

AMQPMessageDispatch::ptr_t AbstractAMQPInterface::getMessageDispatch()
{
  return msgDispatch;
}

// Protected methods ---------------------------------------------------------
AbstractAMQPInterface::AbstractAMQPInterface( AMQPBasicSubscriptionService::ptr_t sService,
                                              AMQPBasicChannel::ptr_t             channel )
: interfaceReady(false), actingAsProvider(false)
{
  subscriptService = sService;
  amqpChannel      = channel;
}

AbstractAMQPInterface::~AbstractAMQPInterface()
{
}
 
void AbstractAMQPInterface::createInterfaceExchangeNames( const String& iName )
{
  interfaceName        = iName;
  providerExchangeName = iName + L" [P]";
  userExchangeName     = iName + L" [U]";
}

void AbstractAMQPInterface::createQueue()
{
  assignBindings();

  wstring targetExchange = actingAsProvider ? providerExchangeName : userExchangeName;
  wstring targetRouteKey = actingAsProvider ? providerRoutingKey   : userRoutingKey;

  AmqpClient::Channel::ptr_t channelImpl = amqpChannel->getChannelImpl();
  if ( channelImpl )
    try
    {
      channelImpl->DeclareQueue( toNarrow( subListenQueue ),
                                  false,  // passive
                                  false,  // durable
                                  false,  // exclusive
                                  true ); // auto-delete

      channelImpl->BindQueue( toNarrow( subListenQueue ),
                              toNarrow( targetExchange ),
                              toNarrow( targetRouteKey ) );
                 
    }
    catch ( AmqpException& ae )
    {
      // amqpIntLogger.error( "Could not create AMQP queue: " + ae.what );
    }
}
  
void AbstractAMQPInterface::createSubscriptionComponent()
{
  AmqpClient::Channel::ptr_t channelImpl = amqpChannel->getChannelImpl();
  if ( channelImpl )
  {
    subProcessor = AMQPBasicSubscriptionProcessor::ptr_t(
        new AMQPBasicSubscriptionProcessor( subscriptService,
                                            channelImpl,
                                            subListenQueue,
                                            msgDispatch ) );

    subProcessor->initialiseSubscription();
  }
  //else amqpIntLogger.error( "Could not create subscription component: AMQP channel is NULL" );
}

void AbstractAMQPInterface::assignBindings()
{
  wstring puCompositeQueue = providerQueueName + L"/" + userQueueName;
    
  if ( actingAsProvider )
    subListenQueue = puCompositeQueue;
  else
    subListenQueue = userQueueName;
    
  providerRoutingKey = L"RK_" + puCompositeQueue;
  userRoutingKey     = L"RK_" + userQueueName;
}


} // namespace
