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
//      Created Date :          15-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.ecc.em.samples.basicEMClient;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;

import java.util.UUID;







public class EMClientController
{
  private AMQPBasicChannel         amqpChannel;
  private IAMQPMessageDispatchPump dispatchPump;
  
  // EM Interfaces
  private IEMMonitorEntryPoint entryPoint;
  
  private EMClientView clientView;
  
  public EMClientController()
  {
  }
  
  public void start( String rabbitServerIP,
                     UUID expMonitorID,
                     UUID clientID ) throws Exception
  {
    if ( rabbitServerIP != null &&
         expMonitorID   != null &&
         clientID       != null )
    {
      // Create connection to Rabbit server ------------------------------------
      AMQPConnectionFactory amqpFactory = new AMQPConnectionFactory();
      amqpFactory.setAMQPHostIPAddress( rabbitServerIP );
      try
      {
        amqpFactory.connectToAMQPHost();
        amqpChannel = amqpFactory.createNewChannel();
      }
      catch (Exception e ) { throw e; }
      
      // Set up a simple view --------------------------------------------------
      clientView = new EMClientView();
      clientView.setVisible( true );
      
      // Create our first EM interface -----------------------------------------
      EMInterfaceFactory interfaceFactory = new EMInterfaceFactory( amqpChannel, false );
      
      // Create dispatch pump (only need to do this once)
      dispatchPump = interfaceFactory.createDispatchPump( "EM Client pump", 
                                                          IAMQPMessageDispatchPump.ePumpPriority.NORMAL);
      dispatchPump.startPump();
      
      // Create a dispatch and add to the pump
      IAMQPMessageDispatch dispatch = interfaceFactory.createDispatch();
      dispatchPump.addDispatch( dispatch );
      
      // Crate our entry point interface
      entryPoint = interfaceFactory.createEntryPoint( expMonitorID, dispatch );
      
      //.. and finally, try registering with the EM!
      entryPoint.registerAsEMClient( clientID, "Simple EM Client" );
    }
  }
}
