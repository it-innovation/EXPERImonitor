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

#pragma once

#include "AMQPMessageDispatch.h"
#include "AMQPBasicSubscriptionService.h"

#include <SimpleAmqpClient/SimpleAmqpClient.h>




namespace ecc_amqpAPI_impl
{
  // Forward declarations -------------
  class AMQPBasicSubscriptionService;
  // ----------------------------------

  class AMQPBasicSubscriptionProcessor
  {
  public:
    
    typedef boost::shared_ptr<AMQPBasicSubscriptionProcessor> ptr_t;

    AMQPBasicSubscriptionProcessor( boost::shared_ptr<AMQPBasicSubscriptionService> service, // explicit shared_ptr required
                                    AmqpClient::Channel::ptr_t                      channel,
                                    const String&                                   qName,
                                    AMQPMessageDispatch::ptr_t                      dispatch );
    
    virtual ~AMQPBasicSubscriptionProcessor();

    UUID getProcessorID();

    AmqpClient::Channel::ptr_t getAMQPChannel() { return amqpChannelImpl; }

    String getAMQPQueueName() { return queueName; }

    void handleBasicMessage( AmqpClient::Envelope::ptr_t envolope );

  private:
    //IECCLogger subProcLogger = Logger.getLogger( typeof(AMQPBasicSubscriptionProcessor) );

    boost::shared_ptr<AMQPBasicSubscriptionService> subscriptionService; // explicit shared_ptr required
    UUID                                            processorID;
    AmqpClient::Channel::ptr_t                      amqpChannelImpl;
    String                                          queueName;
    AMQPMessageDispatch::ptr_t                      messageDispatch;
  };

}