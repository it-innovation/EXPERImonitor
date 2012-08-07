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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.eccInterface;


import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;

import com.rabbitmq.client.*;

import java.util.UUID;
import java.io.IOException;




public class ECCHalfInterfaceBase extends AbstractECCInterface
{
  public ECCHalfInterfaceBase( AMQPBasicChannel channel )
  {
    super( channel );
  }

  public boolean initialise( String  iName,
                             UUID    targetID,
                             boolean asProvider )
  {
    interfaceReady = false;

    // Safety first
    if ( setInitParams(iName, targetID, asProvider) == false )
      return false;
    
    // Get RabbitMQ channel
    Channel channelImpl = (Channel) amqpChannel.getChannelImpl();

    // Either act only as a provider or user of the interface
    // (But always use the same exchange name)
    try
    {
      channelImpl.exchangeDeclare( providerExchangeName, "fanout" );
      createQueue();
      
      // If we're a provider, then listen to messages sent
      if ( actingAsProvider ) createSubscriptionComponent();
    }
    catch (IOException ioe) {}
    
    // Finished
    interfaceReady = true;

    return interfaceReady;
  }
  
  // Protected methods ---------------------------------------------------------
  @Override
  protected void createInterfaceExchangeNames( String iName )
  {
    interfaceName        = iName;
    providerExchangeName = iName + " [P]";
    userExchangeName     = providerExchangeName; // Single direction of traffic only
  }

  // Private methods -----------------------------------------------------------
  private boolean setInitParams( String iName,
                                 UUID targetID,
                                 boolean asProvider )
  {
    // Safety first
    if ( iName       == null ||
         targetID    == null ||
         amqpChannel == null )
      return false;
    
    createInterfaceExchangeNames( iName );

    actingAsProvider   = asProvider;
    providerQueueName  = interfaceName + "_" + targetID.toString() + "[P]";
    userQueueName      = providerQueueName; // One direct of traffic only
    providerRoutingKey = "";
    userRoutingKey     = "";

    return true;
  }
}
