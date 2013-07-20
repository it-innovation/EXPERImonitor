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
//      Created Date :          15-May-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#include "stdafx.h"

#include "AMQPBasicSubscriptionProcessor.h"

#include "ECCUtils.h"

#include <amqp.h>

using namespace AmqpClient;

using namespace std;
using namespace boost;
using namespace boost::uuids;





namespace ecc_amqpAPI_impl
{
  
  AMQPBasicSubscriptionProcessor::AMQPBasicSubscriptionProcessor( AMQPBasicSubscriptionService::ptr_t service,
                                                                  Channel::ptr_t                      channel,
                                                                  const String&                       qName,
                                                                  AMQPMessageDispatch::ptr_t          dispatch )
  : processorID( createRandomUUID() )
  {
    subscriptionService = service;
    amqpChannelImpl     = channel;
    queueName           = qName;
    messageDispatch     = dispatch;
  }

  AMQPBasicSubscriptionProcessor::~AMQPBasicSubscriptionProcessor()
  {
    if ( subscriptionService )
      subscriptionService->unsubscribe( processorID );
  }

  void AMQPBasicSubscriptionProcessor::initialiseSubscription()
  {
    if ( subscriptionService )
      subscriptionService->subscribe( shared_from_this() );
  }

  uuids::uuid AMQPBasicSubscriptionProcessor::getProcessorID()
  { return processorID; }

  void AMQPBasicSubscriptionProcessor::handleBasicMessage( Envelope::ptr_t envolope )
  {
    if ( envolope )
    {
      BasicMessage::ptr_t msg = envolope->Message();

      if ( msg )
      {
        const string payload = msg->Body();
        
        if ( !payload.empty() )
          messageDispatch->addMessage( toNarrow(queueName), payload );
      }
      
      amqpChannelImpl->BasicAck( envolope );
    }
  }

}