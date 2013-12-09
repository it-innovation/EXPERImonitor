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

#include "AMQPBasicChannel.h"
#include "AMQPBasicSubscriptionService.h"

#include <SimpleAmqpClient/SimpleAmqpClient.h>



namespace ecc_amqpAPI_impl
{

  class AMQPConnectionFactory
  {
  public:

    typedef boost::shared_ptr<AMQPConnectionFactory> ptr_t;

    AMQPConnectionFactory();

    virtual ~AMQPConnectionFactory();

    void closeDownConnection();

    bool setAMQPHostIPAddress( const String& addr );

    bool setAMQPHostPort( const int port );

    void setRabbitUserLogin( const String& name, const String& password );

    String getLocalIP();

    void connectToAMQPHost();

    /* Not yet implemented
    void connectToAMQPSSLHost();

    void connectToVerifiedAMQPHost( InputStream keystore, String password );
    */

    bool isConnectionValid();

    AMQPBasicChannel::ptr_t createNewChannel();

    AMQPBasicSubscriptionService::ptr_t getSubscriptionService();

  private:

    AmqpClient::Channel::ptr_t createChannelImpl();

    String amqpHostIP;
    int    amqpPortNumber;
    String userName;
    String userPass;
    bool   connectionEstablished;

    AMQPBasicSubscriptionService amqpSubscriptionService;
  };

}