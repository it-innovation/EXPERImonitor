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
//      Created Date :          05-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMTest_Listener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.faces.AMQPFullInterfaceBase;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMByteWrapper;

import com.google.gson.JsonArray;
import java.util.*;




public class EMTest extends EMBaseInterface
                    implements IEMTest
{
  private EMByteWrapper    byteWrapper;
  private IEMTest_Listener testListener;
  
  
  
  public EMTest( AMQPBasicChannel channel,
                 AMQPMessageDispatch dispatch,
                 UUID providerID,
                 UUID userID,
                 boolean isProvider )
  {
    super( channel, isProvider );
    
    interfaceName = "IECCTest";
    interfaceVersion = "0.1";
            
    interfaceProviderID = providerID;
    interfaceUserID     = userID;
    
    byteWrapper = new EMByteWrapper();
    
    AMQPFullInterfaceBase fullFace = new AMQPFullInterfaceBase( channel );
    initialiseAMQP( fullFace, dispatch );
  }
  
  // IECCTest ------------------------------------------------------------------
  @Override
  public void setListener( IEMTest_Listener listener )
  { testListener = listener; }
  
  @Override
  // Method ID = 1
  public void sendData( UUID senderID, int dataSize, byte[] dataBody )
  {
    if ( dataSize > 0 && dataBody != null )
    {
      ArrayList<Object> params = new ArrayList<Object>();
      params.add( senderID );
      params.add( new Integer(dataSize) );
      
      // Encode bytes in Base64
      String encodedData = byteWrapper.encode( dataBody );
      params.add( encodedData );
      
      executeMethod( 1, params );
    }
  }
  
  // Protected methods ---------------------------------------------------------
  @Override
  protected void onInterpretMessage( int methodID, JsonArray methodData )
  {
    switch ( methodID )
    {
      case 1 :
      {
        if ( testListener != null )
        {
          UUID senderID = jsonMapper.fromJson( methodData.get(1), UUID.class );
          
          int dataSize = jsonMapper.fromJson( methodData.get(2), int.class );
          
          // De-encode Base64
          String encodedData = jsonMapper.fromJson( methodData.get(3), String.class )  ;      
          byte[] dataBody = byteWrapper.decode( encodedData );
          
          testListener.onReceivedData( senderID, dataSize, dataBody );
        }
      } break;
    } 
  }
}
