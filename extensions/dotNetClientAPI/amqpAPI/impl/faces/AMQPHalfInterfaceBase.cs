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

using RabbitMQ.Client;
using System;





namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.faces
{

public class AMQPHalfInterfaceBase : AbstractAMQPInterface
{


  public AMQPHalfInterfaceBase( AMQPBasicChannel channel ) : base( channel )
  {

  }

  public bool initialise( string iName,
                          Guid   targetID,
                          bool   asProvider )
  {
      interfaceReady = false;
      
      // Safety first
      if ( setInitParams(iName, targetID, asProvider) == false )
          return false;
    
      
      // Get RabbitMQ channel
      IModel channelImpl = (IModel) amqpChannel.getChannelImpl();

      // Either act only as a provider or user of the interface
      // (But always use the same exchange name)
      try
      {
          channelImpl.ExchangeDeclare( providerExchangeName, "direct" );
          createQueue();
          
          // If we're a provider, then listen to messages sent
          if ( actingAsProvider ) createSubscriptionComponent();

          // Finished
          interfaceReady = true;
      }
      catch (Exception ioe) 
      {
          String err = "Could not initialise AMQP interface " + iName + ": " + ioe.Message;
          amqpIntLogger.Error(err, ioe);
      }
    
      return interfaceReady;
  }
  
  // Protected methods ---------------------------------------------------------
  protected override void createInterfaceExchangeNames( String iName )
  {
    interfaceName        = iName;
    providerExchangeName = iName + " [P]";
    userExchangeName     = providerExchangeName; // Single direction of traffic only
  }
  
  protected override void assignBindings()
  {
    subListenQueue = actingAsProvider ? providerQueueName + "/" + providerQueueName
                                      : userQueueName     + "/" + userQueueName;
  
    String uniRoute = "RK_ " + (actingAsProvider ? providerQueueName : userQueueName);
    
    providerRoutingKey = uniRoute;
    userRoutingKey     = uniRoute;
  }

  // Private methods -----------------------------------------------------------
  private bool setInitParams( String iName,
                              Guid targetID,
                              bool asProvider )
  {
      // Safety first
      if ( iName       == null ||
           targetID    == null ||
           amqpChannel == null )
          return false;
      
      createInterfaceExchangeNames( iName );
      
      actingAsProvider   = asProvider;
      providerQueueName  = interfaceName + "_" + targetID.ToString() + "[P]";
      userQueueName      = providerQueueName; // One direct of traffic only
      
      return true;
  }
}

} // namespace
