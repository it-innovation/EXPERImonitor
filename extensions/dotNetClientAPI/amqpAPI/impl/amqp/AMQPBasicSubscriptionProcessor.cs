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

using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec;

using RabbitMQ.Client;
using System;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp
{

public class AMQPBasicSubscriptionProcessor : QueueingBasicConsumer
{
  private readonly IECCLogger subProcLogger = Logger.getLogger( typeof(AMQPBasicSubscriptionProcessor) );
  
  private string              queueName;
  private AMQPMessageDispatch messageDispatch;

  
  public AMQPBasicSubscriptionProcessor( IModel channel,
                                         string qName,
                                         AMQPMessageDispatch dispatch ) : base( channel )
  {
      queueName       = qName;
      messageDispatch = dispatch;
  }

  public override void HandleBasicDeliver( string consumerTag,
                                           ulong            deliveryTag,
                                           bool             redelivered,
                                           string           exchange,
                                           string           routingKey,
                                           IBasicProperties properties,
                                           byte[]           body )
  {
      try
      {
          messageDispatch.addMessage( queueName, body );
          Model.BasicAck( deliveryTag, false );
      }
      catch (Exception)
      { subProcLogger.error( "Could not send AMQP acknowledgement" ); }
  }
}

} // namespace
