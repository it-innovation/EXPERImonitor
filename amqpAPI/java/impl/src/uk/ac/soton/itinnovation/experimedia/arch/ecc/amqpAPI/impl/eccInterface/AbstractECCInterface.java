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
//      Created Date :          08-Jul-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.eccInterface;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;

import com.rabbitmq.client.*;

import java.io.*;




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
  protected boolean interfaceReady = false;
  protected boolean actingAsProvider;
  
  
  public boolean sendBasicMessage( String message )
  {
    // Safety first
    if ( !interfaceReady        || 
          message       == null ||
          interfaceName == null ||
          (providerRoutingKey == null && userRoutingKey == null) ) 
      return false;
    
    byte[] messageBody;
    
    try { messageBody = message.getBytes( "UTF8" ); }
    catch (UnsupportedEncodingException uee) { return false; }
    
    try
    {
      Channel channelImpl = (Channel) amqpChannel.getChannelImpl();

      if ( actingAsProvider )
        channelImpl.basicPublish( interfaceName,
                                  userRoutingKey,
                                  null, // Properties
                                  messageBody );
      else
        channelImpl.basicPublish( interfaceName,
                                  providerRoutingKey,
                                  null, // Properties
                                  messageBody );
    }
    catch(IOException ioe) { return false; }
    
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

  public void setMessageDispatch( ECCInterfaceMessageDispatch dispatch )
  { msgDispatch = dispatch; }
  
  // Protected methods ---------------------------------------------------------
  protected AbstractECCInterface(AMQPBasicChannel channel)
  {
    amqpChannel = channel;
  }
  
  protected void createInterfaceExchangeNames( String iName )
  {
    interfaceName        = iName;
    providerExchangeName = iName + "_Provider";
    userExchangeName     = iName + "_User";
  }

  protected void createQueue( Channel channel,
                              String queueName,
                              String routingKey )
  {
    try
    {
      channel.queueDeclare( queueName,
                            false,  // Durable
                            false,  // Exclusive
                            true,   // Auto-delete
                            null ); // Args
  
      channel.queueBind( queueName,
                         interfaceName,
                         "" );    // Args
    }
    catch (IOException ioe) {}
    
  }

  protected void createSubscriptionComponent(String queueName)
  {
    subProcessor = new ECCBasicSubscriptionProcessor();

    subProcessor.initialise( (Channel) amqpChannel.getChannelImpl(),
                              queueName,
                              true,
                              msgDispatch );

    subProcessor.startProcessing();
  }
}
