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
using System.Threading;

using RabbitMQ.Client;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.eccInterface
{
  // Not for client code usage
  public class ECCBasicSubscriptionProcessor
  {
    private QueueingBasicConsumer       consumer;
    private ECCInterfaceMessageDispatch messageDispatch;

    private String  subQueueName;
    private String  consumerTag;
    private Thread  procThread;
    private Boolean isProcessingSubs = false;
    private Boolean isGettingMessage = false;

    public ECCBasicSubscriptionProcessor()
    {
    }

    public Boolean initialise( IModel amqpChannel,
                               String queueName,
                               Boolean requireAck,
                               ECCInterfaceMessageDispatch dispatch)
    {
      // Safety first
      if ( amqpChannel == null || 
           queueName   == null ) return false;

      consumer = new QueueingBasicConsumer(amqpChannel);
      subQueueName = queueName;
      messageDispatch = dispatch;

      consumerTag = amqpChannel.BasicConsume( queueName,
                                              requireAck,
                                              consumer );

      if (consumerTag == null) return false;

      return true;
    }

    public Boolean startProcessing()
    {
      if (!isProcessingSubs)
      {
        procThread = new Thread(new ThreadStart(this.processMessages));
        procThread.Priority = ThreadPriority.BelowNormal;
        isProcessingSubs = true;
        procThread.Start();
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
        procThread.Abort();
      }
    }

    public Boolean isProcessing()
    { return isProcessingSubs; }

    // Protected methods ---------------------------------------------------------
    protected void processMessages()
    {
      if ( messageDispatch != null )
        while (isProcessingSubs)
        {
          isGettingMessage = true;
        
          RabbitMQ.Client.Events.BasicDeliverEventArgs bdea =
            (RabbitMQ.Client.Events.BasicDeliverEventArgs) consumer.Queue.Dequeue();
        
          if ( bdea != null )
            messageDispatch.addMessage( subQueueName,
                                        bdea.Body );
        
          isGettingMessage = false;
        }
    }
  }
}
