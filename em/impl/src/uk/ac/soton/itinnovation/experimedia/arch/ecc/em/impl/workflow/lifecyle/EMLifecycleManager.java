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
//      Created Date :          13-Aug-2012
//      Created for Project :   experimedia-arch-ecc-em-impl
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecyle;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.IAMQPMessageDispatchPump;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.EMConnectionManagerListener;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.dataModel.EMClient;

import java.util.UUID;




public class EMLifecycleManager implements EMConnectionManagerListener
{
  private AMQPBasicChannel        emChannel;
  private UUID                    emProviderID;
  private AMQPMessageDispatchPump generalMsgPump;

  
  public EMLifecycleManager()
  {
  }
  
  public boolean initialise( UUID providerID, 
                             AMQPBasicChannel channel )
  {
    if ( providerID != null && channel != null )
    {
      emProviderID = providerID;
      emChannel = channel;
      
      generalMsgPump = new AMQPMessageDispatchPump( "EM General message pump",
                                                    IAMQPMessageDispatchPump.ePumpPriority.NORMAL );
      
      generalMsgPump.startPump();
      
      return true;
    }
    
    return false;
  }
  
  // EMConnectionManagerListener -----------------------------------------------
  @Override
  public void onClientRegistered( EMClient client )
  {
    if ( client != null )
    {
      // Create a new IEMMonitor interface for the client
      
      // Send them a confirmation message 
    }
  }
}
