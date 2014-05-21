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

#include "AMQPFullInterfaceBase.h"


using namespace boost;
using namespace std;


namespace ecc_amqpAPI_impl
{

  AMQPFullInterfaceBase::AMQPFullInterfaceBase( AMQPBasicSubscriptionService::ptr_t sService,
                                                AMQPBasicChannel::ptr_t             inChannel,
                                                AMQPBasicChannel::ptr_t             outChannel )
    : AbstractAMQPInterface( sService, inChannel, outChannel )
  {
  }

  AMQPFullInterfaceBase::~AMQPFullInterfaceBase()
  {
  }

  bool AMQPFullInterfaceBase::initialise( const String& iName,
                                          const UUID&   providerID,
                                          const UUID&   userID,
                                          const bool    asProvider )
  {
    interfaceReady = false;
      
    // Safety first
    if ( setInitParams(iName, providerID, userID, asProvider) == false )
        return false;
      
    // Get RabbitMQ channel
    AmqpClient::Channel::ptr_t channelImpl = outAMQPChannel->getChannelImpl();

    if ( channelImpl )
    {      
      // Declare the appropriate exchange
      if ( actingAsProvider )
        // Exchange type: direct, non-auto delete, non durable
        channelImpl->DeclareExchange( toNarrow(providerExchangeName) );
      else
        // Exchange type: direct, non-auto delete, non durable
        channelImpl->DeclareExchange( toNarrow(userExchangeName) );
      
      // Create queue and subscription
      createQueue( false );                 // Out channel for out-going
      createSubscriptionComponent( true );  // In channel for subscription

      // Finished
      interfaceReady = true;
    }
      
    return interfaceReady;
  }

  // Private methods ---------------------------------------------------------
  bool AMQPFullInterfaceBase::setInitParams( const String&  iName,
                                             const UUID&    providerID,
                                             const UUID&    userID,
                                             const bool     asProvider )
  {
    // Safety first
    if ( iName.empty() || !inAMQPChannel || !outAMQPChannel ) return false;
      
    createInterfaceExchangeNames( iName );
      
    actingAsProvider   = asProvider;
    providerQueueName  = interfaceName + L"_" + uuidToWide(providerID) + L"[P]";
    userQueueName      = interfaceName + L"_" + uuidToWide(userID) + L"[U]";
      
    return true;
  }
  
} // namespace
