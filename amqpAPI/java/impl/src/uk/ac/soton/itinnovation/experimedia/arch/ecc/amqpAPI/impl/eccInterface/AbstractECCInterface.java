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
  private ECCBasicSubscriptionProcessor subProcessor;
  
  protected AMQPBasicChannel            amqpChannel;
  protected ECCInterfaceMessageDispatch msgDispatch;

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
          (providerRoutingKey == null && userRoutingKey == null) ) 
      return false;
    
    // Make sure producer sends to user (or other way around) - targets are reversed
    String targetExchange = actingAsProvider ? userExchangeName : providerExchangeName;
    String targetRouteKey = actingAsProvider ? userRoutingKey : providerRoutingKey;
    
    byte[] messageBody;
    
    try { messageBody = message.getBytes( "UTF8" ); }
    catch (UnsupportedEncodingException uee) { return false; }
    
    try
    {
      Channel channelImpl = (Channel) amqpChannel.getChannelImpl();
      
      channelImpl.basicPublish( targetExchange,
                                targetRouteKey,
                                null, // Properties
                                messageBody );
    }
    catch(IOException ioe) { return false; }
    
    return true;
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
    providerExchangeName = iName + " [P]";
    userExchangeName     = iName + " [U]";
  }

  protected void createQueue()
  {
    String targetExchange = actingAsProvider ? providerExchangeName : userExchangeName;
    String targetQueue    = actingAsProvider ? providerQueueName    : userQueueName;
    String targetRouteKey = actingAsProvider ? providerRoutingKey   : userRoutingKey;
    
    try
    {
      Channel channel = (Channel) amqpChannel.getChannelImpl();
      
      channel.queueDeclare( targetQueue,
                            false,  // Durable
                            false,  // Exclusive
                            true,   // Auto-delete
                            null ); // Args
  
      channel.queueBind( targetQueue,
                         targetExchange,
                         targetRouteKey );    // Args
    }
    catch (IOException ioe) {}
    
  }

  protected void createSubscriptionComponent()
  {
    Channel    channel = (Channel) amqpChannel.getChannelImpl();
    String targetQueue = actingAsProvider ? providerQueueName : userQueueName;
    
    subProcessor = new ECCBasicSubscriptionProcessor( channel,
                                                      targetQueue,
                                                      msgDispatch );
    
    try { channel.basicConsume( targetQueue, false, subProcessor ); }
    catch ( IOException ioe ) {}
  }
}
