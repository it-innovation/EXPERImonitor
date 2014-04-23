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

#include <SimpleAmqpClient/SimpleAmqpClient.h>

#include "AMQPConnectionFactory.h"



namespace ecc_amqpAPI_impl
{
  // Forward declarations
  class AMQPConnectionFactory;


  class AMQPBasicChannel
  {
  public:

    typedef boost::shared_ptr<AMQPBasicChannel> ptr_t;

    static bool amqpQueueExists( boost::shared_ptr<AMQPConnectionFactory> conFactory, const String& queueName );

    AMQPBasicChannel( AmqpClient::Channel::ptr_t channel );

    virtual ~AMQPBasicChannel();

    AmqpClient::Channel::ptr_t getChannelImpl();

    void setOpen( bool open );

    bool isOpen();

    void close();

  private:
     AmqpClient::Channel::ptr_t amqpChannel;

     bool channelOpen;
  };

}