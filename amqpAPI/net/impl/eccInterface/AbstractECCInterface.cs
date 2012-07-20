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

using uk.ac.soton.itinnovation.experimedia.arch.ecc.commsAPI.impl.amqp;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.commsAPI.impl.eccInterface
{
  public abstract class AbstractECCInterface
  {
    protected AMQPBasicChannel              amqpChannel;
    protected ECCBasicSubscriptionProcessor subProcessor;
    protected ECCInterfaceMessageDispatch   msgDispatch;

    protected String  interfaceName;
    protected String  providerExchangeName, userExchangeName;
    protected String  providerQueueName, userQueueName;
    protected String  providerRoutingKey, userRoutingKey;
    protected String  basicMsgConsumerTag;
    protected Boolean interfaceReady = false;
    protected Boolean actingAsProvider;


    public Boolean sendBasicMessage(String message)
    {
      // Safety first
      if ( !interfaceReady       || 
           message       == null ||
           interfaceName == null ||
           (providerRoutingKey == null && userRoutingKey == null) ) 
        return false;

      byte[] messageBody = System.Text.Encoding.UTF8.GetBytes(message);

      IModel channelImpl = (IModel)amqpChannel.getChannelImpl();

      if (actingAsProvider)
        channelImpl.BasicPublish( interfaceName,
                                  userRoutingKey,
                                  null, // Properties
                                  messageBody);
      else
        channelImpl.BasicPublish( interfaceName,
                                  providerRoutingKey,
                                  null, // Properties
                                  messageBody);

      return true;
    }

    public void removeInterface()
    {
      // Wind down subscription processor
      if (subProcessor != null)
      {
        if (subProcessor.isProcessing())
        {
          subProcessor.stopProcessing();
          while (subProcessor.isProcessing())
          {
            // TODO: Change this!
            // Wait indefinitely.
          }

          subProcessor = null;
        }
      }

      // Close channel
      if (amqpChannel != null) amqpChannel.close();
    }

    public void setMessageDispatch(ECCInterfaceMessageDispatch dispatch)
    { msgDispatch = dispatch; }

    // Protected methods ---------------------------------------------------------
    protected AbstractECCInterface(AMQPBasicChannel channel)
    {
      amqpChannel = channel;
    }

    protected void createInterfaceExchangeNames(String iName)
    {
      interfaceName = iName;
      providerExchangeName = iName + "_Provider";
      userExchangeName = iName + "_User";
    }

    protected void createQueue( IModel channel,
                                String queueName,
                                String routingKey )
    {
      channel.QueueDeclare( queueName,
                            false,  // Durable
                            false,  // Exclusive
                            true,   // Auto-delete
                            null ); // Args

      channel.QueueBind( queueName,
                         interfaceName,
                         "" );    // Args
    }

    protected void createSubscriptionComponent(String queueName)
    {
      subProcessor = new ECCBasicSubscriptionProcessor();

      subProcessor.initialise((IModel)amqpChannel.getChannelImpl(),
                               queueName,
                               true,
                               msgDispatch);

      subProcessor.startProcessing();
    }
  }
}
