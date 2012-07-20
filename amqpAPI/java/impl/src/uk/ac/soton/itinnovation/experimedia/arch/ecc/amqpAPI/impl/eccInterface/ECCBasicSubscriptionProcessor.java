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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.eccInterface;

import com.rabbitmq.client.*;
import java.io.IOException;




class ECCBasicSubscriptionProcessor implements Runnable
{
  private Channel                     amqpChannel;
  private QueueingConsumer            consumer;
  private ECCInterfaceMessageDispatch messageDispatch;

  private String  subQueueName;
  private String  consumerTag;
  private Thread  procThread;
  private boolean isProcessingSubs = false;
  private boolean isGettingMessage = false;

  public ECCBasicSubscriptionProcessor()
  {
  }

  public boolean initialise( Channel channel,
                             String  queueName,
                             boolean requireAck,
                             ECCInterfaceMessageDispatch dispatch )
  {
    // Safety first
    if ( channel   == null || 
         queueName == null ) return false;

    amqpChannel     = channel;
    consumer        = new QueueingConsumer( amqpChannel );
    subQueueName    = queueName;
    messageDispatch = dispatch;

    try
    { consumerTag = amqpChannel.basicConsume( queueName,
                                              requireAck,
                                              consumer ); }
    catch (IOException ioe) { return false; }

    if (consumerTag == null) return false;

    return true;
  }

  public boolean startProcessing()
  {
    if (!isProcessingSubs)
    {
      procThread = new Thread( this );
      procThread.setPriority( Thread.MIN_PRIORITY );
      isProcessingSubs = true;
      procThread.start();
    }

    return true;
  }

  public void stopProcessing()
  {
    if (isProcessingSubs)
    {
      while (isGettingMessage)
      { /* Wait unless current message is finished */ }

      isProcessingSubs = false;
      procThread = null;
    }
  }

  public boolean isProcessing()
  { return isProcessingSubs; }

  @Override
  public void run()
  {
    if ( messageDispatch != null )
      while (isProcessingSubs)
      {
        isGettingMessage = true;

        try
        {
          QueueingConsumer.Delivery delivery = consumer.nextDelivery();

          if ( delivery != null )
            messageDispatch.addMessage( subQueueName, delivery.getBody() );
        }
        catch (InterruptedException ie) {}

        isGettingMessage = false;
      }
  }
}
