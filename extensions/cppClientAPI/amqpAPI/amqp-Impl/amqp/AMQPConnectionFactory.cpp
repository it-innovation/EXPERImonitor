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

#include "AMQPConnectionFactory.h"
#include "AMQPBasicChannel.h"
#include "AMQPBasicSubscriptionService.h"

#include <boost/asio.hpp>
#include <boost/regex.hpp>

#include <exception>

using namespace AmqpClient;
using namespace std;
using namespace boost;
using namespace boost::asio;


namespace ecc_amqpAPI_impl
{

  AMQPConnectionFactory::AMQPConnectionFactory()
    : userName( L"guest" ),
      userPass( L"guest" ),
      amqpHostIP( L"127.0.0.1" ), 
      amqpPortNumber(5672), 
      connectionEstablished( false )
  {
  }

  AMQPConnectionFactory::~AMQPConnectionFactory()
  {
  }

  bool AMQPConnectionFactory::setAMQPHostIPAddress( const String& addr )
  {
    bool ipSuccess = false;

    string source = toNarrow( addr );
    smatch matcher;
    regex  ipPattern( "\\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)" );

    if ( regex_search( source, matcher, ipPattern ) )
    {
      amqpHostIP = addr;
      ipSuccess  = true;
    }
    else throw L"Failed to parse IP address";

    return ipSuccess;
  }
    
  void AMQPConnectionFactory::closeDownConnection()
  {
    // Close down the subscription service...
    amqpSubscriptionService.stopService();

    connectionEstablished = false;
  }

  bool AMQPConnectionFactory::setAMQPHostPort( const int port )
  {
    if ( port < 1 ) return false;

    amqpPortNumber = port;

    return true;
  }

  void AMQPConnectionFactory::setRabbitUserLogin( const String& name, const String& password )
  {
    if ( !name.empty() && !password.empty() )
    {
      userName = name;
      userPass = password;
    }
  }

  wstring AMQPConnectionFactory::getLocalIP()
  {
    wstring localIPValue = L"unknown";

    io_service      io_service;
    ip::tcp::socket socket( io_service );
    string ipVal  = socket.remote_endpoint().address().to_string();

    if ( !ipVal.empty() )
      localIPValue = toWide( ipVal );
      
    return localIPValue;
  }

  void AMQPConnectionFactory::connectToAMQPHost()
  {
    // Safety first
    if ( amqpHostIP.empty() )
      throw L"AMQP Host IP not correct";

    // Try a throw-away connection
    connectionEstablished = false;

    if ( createChannelImpl() ) connectionEstablished = true;

    if ( !connectionEstablished )
      throw L"Could not connect to AMQP host";
  }

  /*
  void AMQPConnectionFactory::connectToAMQPSSLHost()
  {

  }

  void AMQPConnectionFactory::connectToVerifiedAMQPHost( InputStream keystore,
                                                         String      password )
  {

  }
  */

  bool AMQPConnectionFactory::isConnectionValid()
  { return connectionEstablished; }

  AMQPBasicChannel::ptr_t AMQPConnectionFactory::createNewChannel()
  {
    AMQPBasicChannel::ptr_t newChannel;

    if ( connectionEstablished )
    {
      Channel::ptr_t channelImpl = createChannelImpl();

      // If implementation is OK, encapsulate it
      if ( channelImpl )
        newChannel = AMQPBasicChannel::ptr_t( new AMQPBasicChannel(channelImpl) );
      else
        connectionEstablished = false; // Indicate we don't have the ability to connect
    }

    return newChannel;
  }

  AMQPBasicSubscriptionService::ptr_t AMQPConnectionFactory::getSubscriptionService()
  {
    return AMQPBasicSubscriptionService::ptr_t( &amqpSubscriptionService );
  }

  // Private methods -----------------------------------------------------------
  Channel::ptr_t AMQPConnectionFactory::createChannelImpl()
  {
    Channel::ptr_t channelImpl;

    if ( !amqpHostIP.empty() )
    {
      string ip =  toNarrow( amqpHostIP );

      channelImpl = Channel::Create( ip, amqpPortNumber,
                                     toNarrow( userName ), 
                                     toNarrow( userPass ) );
    }

    return channelImpl;
  }
}
