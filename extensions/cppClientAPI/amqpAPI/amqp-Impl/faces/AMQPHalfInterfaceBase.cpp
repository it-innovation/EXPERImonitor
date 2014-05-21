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

#include "AMQPHalfInterfaceBase.h"

using namespace boost;
using namespace std;




namespace ecc_amqpAPI_impl
{

  AMQPHalfInterfaceBase::AMQPHalfInterfaceBase( AMQPBasicSubscriptionService::ptr_t sService,
                                                AMQPBasicChannel::ptr_t             inChannel,
                                                AMQPBasicChannel::ptr_t             outChannel )
    : AbstractAMQPInterface( sService, inChannel, outChannel )
  {
  }

  AMQPHalfInterfaceBase::~AMQPHalfInterfaceBase()
  {
  }

  bool AMQPHalfInterfaceBase::initialise( const String& iName, 
                                          const UUID&   targetID, 
                                          bool          asProvider )
  {
    interfaceReady = false;
      
    // Safety first
    if ( setInitParams(iName, targetID, asProvider) == false )
        return false;
      
    // Get RabbitMQ channel
    AmqpClient::Channel::ptr_t channelImpl = outAMQPChannel->getChannelImpl();

    if ( channelImpl )
    {
      // Exchange type: direct, non-auto delete, non durable
      channelImpl->DeclareExchange( toNarrow(providerExchangeName) );

      createQueue( false ); // Use out-going channel for sending messages

      // If we're a provider, then listen to messages sent
      if ( actingAsProvider ) createSubscriptionComponent( inAMQPChannel ); // In channel for subscription

      // Finished
      interfaceReady = true;
    }
    
    return interfaceReady;
  }
  
  void AMQPHalfInterfaceBase::createInterfaceExchangeNames( const String& iName )
  {
    interfaceName        = iName;
    providerExchangeName = iName + L" [P]";
    userExchangeName     = providerExchangeName; // Single direction of traffic only
  }

  void AMQPHalfInterfaceBase::assignBindings()
  {
    subListenQueue = actingAsProvider ? providerQueueName + L"/" + providerQueueName
                                      : userQueueName     + L"/" + userQueueName;
  
    wstring uniRoute = L"RK_ " + (actingAsProvider ? providerQueueName : userQueueName);
    
    providerRoutingKey = uniRoute;
    userRoutingKey     = uniRoute;
  }

  bool AMQPHalfInterfaceBase::setInitParams( const String& iName, const UUID& targetID, const bool asProvider )
  {
    // Safety first
    if ( iName.empty() || !inAMQPChannel || !outAMQPChannel ) return false;
      
    createInterfaceExchangeNames( iName );
      
    actingAsProvider   = asProvider;
    providerQueueName  = interfaceName + L"_" + uuidToWide(targetID) + L"[P]";
    userQueueName      = providerQueueName; // One direct of traffic only
      
    return true;
  }

} // namespace
