/////////////////////////////////////////////////////////////////////////
//
// � University of Southampton IT Innovation Centre, 2013
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

#include <amqp.h>

#include <boost/uuid/uuid_generators.hpp>

using namespace AmqpClient;

using namespace std;
using namespace boost;
using namespace boost::uuids;





namespace ecc_amqpAPI_impl
{
  
  AMQPBasicSubscriptionProcessor::AMQPBasicSubscriptionProcessor( AMQPBasicSubscriptionService::ptr_t service,
                                                                  Channel::ptr_t channel,
                                                                  wstring qName,
                                                                  AMQPMessageDispatch::ptr_t dispatch )
  : processorID(boost::uuids::random_generator()())
  {
    subscriptionService = service;
    amqpChannelImpl     = channel;
    queueName           = qName;
    messageDispatch     = dispatch;

    if ( subscriptionService )
      subscriptionService->subscribe( AMQPBasicSubscriptionProcessor::ptr_t(this) ); 
  }

  AMQPBasicSubscriptionProcessor::~AMQPBasicSubscriptionProcessor()
  {
    if ( subscriptionService )
      subscriptionService->unsubscribe( AMQPBasicSubscriptionProcessor::ptr_t(this) );
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
        amqp_bytes_t payload = msg->getAmqpBody();

        if ( payload.bytes != NULL )
          messageDispatch->addMessage( queueName, (byte*) payload.bytes );
      }
      
      amqpChannelImpl->BasicAck( envolope );
    }
  }

}