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
//      Created Date :          04-Jan-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.em.test.v1.eccMonitor.thruPut;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.IEMTest;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;

import uk.ac.soton.itinnovation.experimedia.arch.em.test.common.ECCBaseTest;

import java.util.UUID;
import org.apache.log4j.Logger;



public abstract class BaseThruExeNode
{
  protected Logger nodeLogger = Logger.getLogger( BaseThruExeNode.class );
  
  protected UUID                     senderID;
  protected IAMQPMessageDispatchPump sendPump;
  protected IAMQPMessageDispatch     sendDispatch;
  protected ThruPutByteStore         byteStore;
  protected IEMTest                  senderTestFace;
  
  
  // Derived classes to implement ----------------------------------------------
  public abstract void sendData();
  
  // Protected methods ---------------------------------------------------------
  protected BaseThruExeNode( UUID               id,
                             ThruPutByteStore   store,
                             AMQPBasicChannel   senderChannel,
                             EMInterfaceFactory userFactory )
  {
    senderID  = id;
    byteStore = store;
    sendPump  = userFactory.createDispatchPump( id.toString() + " thruput pump", 
                                                IAMQPMessageDispatchPump.ePumpPriority.MINIMUM );
    
    sendDispatch = userFactory.createDispatch();
    sendPump.addDispatch( sendDispatch );
    sendPump.startPump();
    
    senderTestFace = userFactory.createTest( ECCBaseTest.EMProviderUUID,
                                             senderID, 
                                             sendDispatch );
  }
}
