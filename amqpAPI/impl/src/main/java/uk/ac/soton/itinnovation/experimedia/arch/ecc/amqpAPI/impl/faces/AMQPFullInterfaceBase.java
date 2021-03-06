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
//      Created Date :          11-Jul-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.faces;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;

import com.rabbitmq.client.Channel;

import java.util.UUID;
import java.io.IOException;




public class AMQPFullInterfaceBase extends AbstractAMQPInterface
{
  public AMQPFullInterfaceBase( AMQPBasicChannel channel )
  {
    super (channel);
  }

  public boolean initialise( String  iName,
                             UUID    providerID,
                             UUID    userID,
                             boolean asProvider )
  {
    interfaceReady = false;

    // Safety first
    if ( setInitParams(iName, providerID, userID, asProvider) == false )
      return false;
    
    // Get RabbitMQ channel
    Channel channelImpl = (Channel) amqpChannel.getChannelImpl();

    try
    {      
      // Declare the appropriate exchanges
      channelImpl.exchangeDeclare( providerExchangeName, "direct" );
      channelImpl.exchangeDeclare( userExchangeName, "direct" );
      
      // Create queue and subscription
      createQueue();
      createSubscriptionComponent();
      
      // Finished
      interfaceReady = true;
  
    }
    catch (IOException ioe) 
    {
        String err = "Could not initialise AMQP interface " + iName + ": " + ioe.getMessage();
        amqpIntLogger.error( err, ioe );
    }
    
    return interfaceReady;
  }

  // Private methods -----------------------------------------------------------
  private boolean setInitParams( String iName,
                                 UUID providerID,
                                 UUID userID,
                                 boolean asProvider)
  {
    // Safety first
    if ( iName       == null ||
         providerID  == null ||
         userID      == null ||
         amqpChannel == null)
      return false;

    createInterfaceExchangeNames( iName );
    
    actingAsProvider   = asProvider;
    providerQueueName  = interfaceName + "_" + providerID.toString() + "[P]";
    userQueueName      = interfaceName + "_" + userID.toString() + "[U]";

    return true;
  }
}
