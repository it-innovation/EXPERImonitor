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
//      Created Date :          13-May-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#include "stdafx.h"

#include <SimpleAmqpClient/SimpleAmqpClient.h>

#include <iostream>
#include <stdlib.h>

using namespace AmqpClient;


int _tmain(int argc, _TCHAR* argv[])
{
  std::cout << "Starting rabbitmq-c test application" << std::endl;

  Channel::ptr_t channel = Channel::Create();

	channel->DeclareQueue("alanqueue");

	channel->BindQueue("alanqueue", "amq.direct", "alankey");

	BasicMessage::ptr_t msg_in = BasicMessage::Create();

	msg_in->Body("This is a small message.");

	channel->BasicPublish("amq.direct", "alankey", msg_in);

	channel->BasicConsume("alanqueue", "consumertag");

	Envelope::ptr_t msg_env = channel->BasicConsumeMessage( "consumertag" );

	std::cout << "Message text: " << msg_env->Message()->Body() << std::endl;

	return 0;
}

