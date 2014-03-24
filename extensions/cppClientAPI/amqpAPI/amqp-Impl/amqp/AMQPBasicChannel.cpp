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

#include "AMQPBasicChannel.h"

using namespace AmqpClient;




namespace ecc_amqpAPI_impl
{

  bool AMQPBasicChannel::amqpQueueExists( AMQPConnectionFactory::ptr_t conFactory, const String& queueName )
  {
    // Safety first
		if ( !conFactory ) throw L"Could not test amqp queue: amqp connection factory is null";
    if ( queueName.length() == 0 ) throw L"Could not test amqp queue: queue name is empty";
		if ( !conFactory->isConnectionValid() ) throw L"Could not test amqp queue: factory connection is invalid";
		
		// Need to create an independent connection & channel to test for a queue -
		// a negative result automatically closes a channel.
    AMQPBasicChannel::ptr_t bc = conFactory->createNewChannel();
		Channel::ptr_t channelImpl = bc->getChannelImpl();
		
		bool result = false;
		String resultInfo = L"AMQP queue ( " + queueName + L") existence result: ";
		
		if ( channelImpl )
		{
			// Try passively declaring the queue to see if it exists
			try
			{
        channelImpl->DeclareQueue( toNarrow(queueName), true, false, false, true ); 
				result = true;
				resultInfo += L"exists";
			}
			catch ( AmqpException& ae )
			{ 
				resultInfo += L"does not exist";
				// Channel will be automatically closed in this case
			}

      channelImpl.reset();
		}
		else resultInfo += L" could not test: channel is null or closed";
		
		return result;
  }
 
  AMQPBasicChannel::AMQPBasicChannel( Channel::ptr_t channel  )
    : channelOpen( false )
  { 
    amqpChannel = channel;
  }

  AMQPBasicChannel::~AMQPBasicChannel()
  {}

  Channel::ptr_t AMQPBasicChannel::getChannelImpl()
  { return amqpChannel; }

  void AMQPBasicChannel::setOpen( bool open )
  {
    if ( amqpChannel != NULL ) channelOpen = open;
  }

  bool AMQPBasicChannel::isOpen()
  {
    if ( amqpChannel != NULL )
        return channelOpen;

    return false;
  }

  void AMQPBasicChannel::close()
  {
    if ( amqpChannel != NULL )
      if ( channelOpen )
      {
        // Release the reference to this channel
        amqpChannel.reset();
      }
  }

} // namespace