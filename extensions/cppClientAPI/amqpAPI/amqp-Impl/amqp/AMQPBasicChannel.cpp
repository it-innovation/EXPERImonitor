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
 
  AMQPBasicChannel::AMQPBasicChannel( Channel::ptr_t channel  )
  { 
    amqpChannel = channel;
  }

  AMQPBasicChannel::~AMQPBasicChannel()
  {}

  Channel::ptr_t AMQPBasicChannel::getChannelImpl()
  { return amqpChannel; }

  bool AMQPBasicChannel::isOpen()
  {
    return ( amqpChannel != NULL );
  }

  void AMQPBasicChannel::close()
  {
    if ( amqpChannel != NULL )
      amqpChannel.reset(); // Release the reference to this channel
  }

} // namespace