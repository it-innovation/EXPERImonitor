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
//      Created By :            Simon Crowle
//      Created Date :          08-Jul-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.faces;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import com.rabbitmq.client.*;

import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractAMQPInterface
{
  private AMQPBasicSubscriptionProcessor subProcessor;
  
  protected Logger amqpIntLogger = LoggerFactory.getLogger(getClass());
  protected AMQPBasicChannel    amqpChannel;
  protected AMQPMessageDispatch msgDispatch;

  protected String  interfaceName;
  protected String  providerExchangeName, userExchangeName;
  protected String  providerQueueName, userQueueName;
  protected String  providerRoutingKey, userRoutingKey;
  protected String  subListenQueue;
  protected boolean interfaceReady = false;
  protected boolean actingAsProvider;
  
  public void shutdown()
  {
    if ( amqpChannel != null )
    {
      // Clear up queue, if it exists
      if ( subListenQueue != null )
        try 
        {
          if ( amqpChannel.isOpen() )
          {
            Channel channel = (Channel) amqpChannel.getChannelImpl();
            channel.queueDelete( subListenQueue );
            subProcessor = null;
            msgDispatch  = null;
          }
          else 
              amqpIntLogger.warn( "Could not close AMQP channel: already closed" );
              
        }
        catch (IOException ioe)
        { amqpIntLogger.error( "Could not delete AMQP queue: " + ioe.getMessage() ); }
    }
      
    // Tidy up (channel is managed elsewhere)     
    interfaceName        = null;
    providerExchangeName = null;
    userExchangeName     = null;
    providerQueueName    = null;
    userQueueName        = null;
    providerRoutingKey   = null;
    userRoutingKey       = null;
    subListenQueue       = null;
    interfaceReady       = false;
  }
  
  public synchronized boolean sendBasicMessage( String message )
  {
    // Safety first
    if ( !interfaceReady        || 
          message       == null ||
          (providerRoutingKey == null && userRoutingKey == null) ) 
      return false;
    
    boolean result = false;
    
    // Make sure producer sends to user (or other way around) - targets are reversed
    String targetExchange = actingAsProvider ? userExchangeName : providerExchangeName;
    String targetRouteKey = actingAsProvider ? userRoutingKey   : providerRoutingKey;
    
    try
    {
      if ( amqpChannel.isOpen() )
      {
        Channel channelImpl = (Channel) amqpChannel.getChannelImpl();
        
        byte[] messageBody = message.getBytes( "UTF-8" );
        
        channelImpl.basicPublish( targetExchange,
                                  targetRouteKey,
                                  null, // Properties
                                  messageBody );
      
        result = true;
      }
      else
          amqpIntLogger.error( "Could not send AMQP message: channel closed" );
              
    }
    catch (IOException ioe) 
    { amqpIntLogger.error( "Could not send AMQP message: " + ioe.getMessage() ); }
    
    return result;
  }

  public void setMessageDispatch( AMQPMessageDispatch dispatch )
  { msgDispatch = dispatch; }
  
  // Protected methods ---------------------------------------------------------
  protected AbstractAMQPInterface(AMQPBasicChannel channel)
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
    assignBindings();
    
    String targetExchange = actingAsProvider ? providerExchangeName : userExchangeName;
    String targetRouteKey = actingAsProvider ? providerRoutingKey   : userRoutingKey;
    
    try
    {
      Channel channel = (Channel) amqpChannel.getChannelImpl();
      
      channel.queueDeclare( subListenQueue,
                            false,  // Durable
                            false,  // Exclusive
                            true,   // Auto-delete
                            null ); // Args
  
      channel.queueBind( subListenQueue,
                         targetExchange,
                         targetRouteKey ); // Args
    }
    catch (IOException ioe)
    { amqpIntLogger.error( "Could not create AMQP queue: " + ioe.getMessage() ); }
    
  }
  
  protected void createSubscriptionComponent()
  {
    Channel channel = (Channel) amqpChannel.getChannelImpl();
    
    subProcessor = new AMQPBasicSubscriptionProcessor( channel,
                                                       subListenQueue,
                                                       msgDispatch );
  
    try { channel.basicConsume( subListenQueue, false, subProcessor ); }
    catch ( IOException ioe )
    {
        String err = "AMQP Interface could not create subscription component: " + ioe.getMessage();
        amqpIntLogger.error( err, ioe );
    }
  }
  
  protected void assignBindings()
  {
    String puCompositeQueue = providerQueueName + "/" + userQueueName;
    
    if ( actingAsProvider )
      subListenQueue = puCompositeQueue;
    else
      subListenQueue = userQueueName;
    
    providerRoutingKey = "RK_" + puCompositeQueue;
    userRoutingKey     = "RK_" + userQueueName;
  }
}
