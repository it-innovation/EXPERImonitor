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
//      Created By :            sgc
//      Created Date :          11-Jul-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using RabbitMQ.Client;

using uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.eccInterface
{
  public class ECCHalfInterfaceBase : AbstractECCInterface
  {
    public ECCHalfInterfaceBase(AMQPBasicChannel channel) : base(channel)
    {
    }

    public Boolean initialise( String iName,
                               Guid targetID,
                               Boolean asProvider)
    {
      interfaceReady = false;

      // Safety first
      if (setInitParams(iName, targetID, asProvider) == false)
        return false;

      // Get RabbitMQ channel
      IModel channelImpl = (IModel)amqpChannel.getChannelImpl();
      String targetQueueName = interfaceName + targetID.ToString();

      if (actingAsProvider)
      {
        channelImpl.ExchangeDeclare(interfaceName, ExchangeType.Fanout );
        createQueue(channelImpl, targetQueueName, providerRoutingKey);
        createSubscriptionComponent(targetQueueName);
      }
      else
      {
        channelImpl.ExchangeDeclare(interfaceName, ExchangeType.Fanout);
        createQueue(channelImpl, targetQueueName, userRoutingKey);
      }

      // Finished
      interfaceReady = true;

      return interfaceReady;
    }

    // Private methods -----------------------------------------------------------
    private Boolean setInitParams( String iName,
                                   Guid targetID,
                                   Boolean asProvider)
    {
      // Safety first
      if ( iName       == null ||
           targetID    == null ||
           amqpChannel == null)
        return false;

      createInterfaceExchangeNames(iName);

      actingAsProvider = asProvider;
      providerQueueName = interfaceName + "_" + targetID.ToString();
      userQueueName = interfaceName + "_" + targetID.ToString();
      providerRoutingKey = "";
      userRoutingKey = "";

      return true;
    }
  }
}
