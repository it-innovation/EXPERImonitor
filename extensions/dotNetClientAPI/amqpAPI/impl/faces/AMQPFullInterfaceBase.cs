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

using System;
using RabbitMQ.Client;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.faces
{

public class AMQPFullInterfaceBase : AbstractAMQPInterface
{
  public AMQPFullInterfaceBase( AMQPBasicChannel channel ) : base( channel )
  {
  }

  public bool initialise( string iName,
                          Guid providerID,
                          Guid userID,
                          bool asProvider )
  {
      interfaceReady = false;
      
      // Safety first
      if ( setInitParams(iName, providerID, userID, asProvider) == false )
          return false;
      
      // Get RabbitMQ channel
      IModel channelImpl = (IModel) amqpChannel.getChannelImpl();
      
      try
      {
          // Declare the appropriate exchanges
          channelImpl.ExchangeDeclare( providerExchangeName, "direct" );
          channelImpl.ExchangeDeclare( userExchangeName, "direct" );
      
          // Create queue and subscription
          createQueue();
          createSubscriptionComponent();

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

  // Private methods -----------------------------------------------------------
  private bool setInitParams( String iName,
                              Guid providerID,
                              Guid userID,
                              bool asProvider)
  {
      // Safety first
      if ( iName       == null ||
           providerID  == null ||
           userID      == null ||
           amqpChannel == null)
          return false;
      
      createInterfaceExchangeNames( iName );
      
      actingAsProvider   = asProvider;
      providerQueueName  = interfaceName + "_" + providerID.ToString() + "[P]";
      userQueueName      = interfaceName + "_" + userID.ToString() + "[U]";
      
      return true;
  }
}

} // namespace
