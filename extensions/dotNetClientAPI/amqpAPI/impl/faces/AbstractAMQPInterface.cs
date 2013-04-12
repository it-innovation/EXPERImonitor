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
//      Created Date :          08-Apr-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

using uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp;

using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec;

using RabbitMQ.Client;
using System;
using System.Text;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.faces
{

public abstract class AbstractAMQPInterface
{
  private readonly IECCLogger amqpIntLogger = Logger.getLogger(typeof(AbstractAMQPInterface));
  private readonly Object sendMessageLock   = new Object();

  private UTF8Encoding utf8Encode = new UTF8Encoding();
  private AMQPBasicSubscriptionProcessor subProcessor;
  
  protected AMQPBasicChannel    amqpChannel;
  protected AMQPMessageDispatch msgDispatch;

  protected string interfaceName;
  protected string providerExchangeName, userExchangeName;
  protected string providerQueueName, userQueueName;
  protected string providerRoutingKey, userRoutingKey;
  protected string subListenQueue;
  protected bool   interfaceReady = false;
  protected bool   actingAsProvider;
  
  public void shutdown()
  {
      if ( amqpChannel != null )
      {
          // Clear up queue, if it exists
          if ( subListenQueue != null )
              try 
              {
                  IModel channel = (IModel) amqpChannel.getChannelImpl();
                  channel.QueueDelete( subListenQueue );
                  subProcessor = null;
                  msgDispatch  = null;
              }
              catch (Exception e)
              { amqpIntLogger.error( "Could not delete AMQP queue: " + e.Message ); }
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
  
  public bool sendBasicMessage( string message )
  {
      // Safety first
      if ( !interfaceReady        || 
            message       == null ||
          (providerRoutingKey == null && userRoutingKey == null) ) 
      return false;

      bool result = false;

      lock ( sendMessageLock )
      {
          // Make sure producer sends to user (or other way around) - targets are reversed
          string targetExchange = actingAsProvider ? userExchangeName : providerExchangeName;
          string targetRouteKey = actingAsProvider ? userRoutingKey   : providerRoutingKey;
          
          try
          {
              IModel channelImpl = (IModel) amqpChannel.getChannelImpl();
                            
              channelImpl.BasicPublish( targetExchange,
                                        targetRouteKey,
                                        null, // Properties
                                        utf8Encode.GetBytes(message));
              result = true;
          }
          catch(Exception) {}
      }
      
      return result;
  }

  public void setMessageDispatch( AMQPMessageDispatch dispatch )
  { msgDispatch = dispatch; }
  
  // Protected methods ---------------------------------------------------------
  protected AbstractAMQPInterface(AMQPBasicChannel channel)
  {
    amqpChannel = channel;
  }
  
  protected virtual void createInterfaceExchangeNames( String iName )
  {
    interfaceName        = iName;
    providerExchangeName = iName + " [P]";
    userExchangeName     = iName + " [U]";
  }

  protected virtual void createQueue()
  {
      assignBindings();
      
      string targetExchange = actingAsProvider ? providerExchangeName : userExchangeName;
      string targetRouteKey = actingAsProvider ? providerRoutingKey   : userRoutingKey;
      
      try
      {
          IModel channel = (IModel) amqpChannel.getChannelImpl();
          
          channel.QueueDeclare( subListenQueue,
                                false,  // Durable
                                false,  // Exclusive
                                true,   // Auto-delete
                                null ); // Args

          channel.QueueBind( subListenQueue,
                             targetExchange,
                             targetRouteKey ); // Args
      }
      catch (Exception ioe)
      { amqpIntLogger.error( "Could not create AMQP queue: " + ioe.Message ); }
    
  }
  
  protected virtual void createSubscriptionComponent()
  {
      IModel channel = (IModel) amqpChannel.getChannelImpl();
      
      subProcessor = new AMQPBasicSubscriptionProcessor( channel,
                                                         subListenQueue,
                                                         msgDispatch );
      
      try { channel.BasicConsume( subListenQueue, false, subProcessor ); }
      catch ( Exception ) {}
  }
  
  protected virtual void assignBindings()
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

} // namespace
