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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;

import java.util.UUID;


public class OrderedThruExeNode extends BaseThruExeNode
{
  
  public OrderedThruExeNode( UUID               id,
                             ThruPutByteStore   store,
                             AMQPBasicChannel   senderChannel,
                             EMInterfaceFactory userFactory )
  {
    super( id, store, senderChannel, userFactory );
  }
  
  // BaseThruExeNode------------------------------------------------------------
  @Override
  public void sendData()
  {
    String logID = senderID.toString();
    nodeLogger.info( logID + " executing ordered send test." );
    
    for ( Integer streamSize : byteStore.getStreamSizes() )
    {
      nodeLogger.info( logID + " sending " + streamSize.toString() + " byte stream." );
      
      for ( int pushRun : byteStore.getStreamPushes() )
      {
        byte[] stream = byteStore.getByteStreamOfSize( streamSize );
        
        for ( int i = 0; i < pushRun; i++ )
          senderTestFace.sendData( senderID, streamSize, stream );
      }
    }
  }
}
