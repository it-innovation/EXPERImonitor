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
//      Created Date :          29-Jul-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.IAMQPMessageDispatchListener;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.faces.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import org.apache.log4j.Logger;
import com.google.gson.*;
import java.io.UnsupportedEncodingException;
import java.util.*;




public abstract class EMBaseInterface implements IAMQPMessageDispatchListener
{
  protected final static Logger faceDebugLogger = Logger.getLogger( EMBaseInterface.class );
  
  protected String     interfaceName;
  protected String     interfaceVersion;
  protected boolean    isProvider;
  protected Gson       jsonMapper;
  protected JsonParser jsonParser;
  
  protected AMQPBasicChannel      amqpChannel;
  protected AbstractAMQPInterface amqpInterface;
  protected UUID                  interfaceUserID;
  protected UUID                  interfaceProviderID;
  
  
  // IAMQPMessageDispatchListener ----------------------------------------------
  @Override
  public void onSimpleMessageDispatched( String queueName, byte[] data )
  {
    if ( queueName != null && data != null )
    {
      try
      { 
        String jsonData = new String( data, "UTF8" );
        
        JsonArray jsonItems = jsonParser.parse( jsonData ).getAsJsonArray();
        int methodID        = jsonMapper.fromJson( jsonItems.get(0), int.class );

        // DEBUG ---------------------------------------------------------------
        faceDebugLogger.debug( "BaseFACE:Msg: [" +methodID+ "] " + (isProvider ? interfaceUserID: interfaceProviderID) );

        onInterpretMessage( methodID, jsonItems );
      }
      catch (UnsupportedEncodingException e) {}
    } 
  }
  
  // Protected methods ---------------------------------------------------------
  protected EMBaseInterface( AMQPBasicChannel channel,
                             boolean asProvider )
  {
    amqpChannel = channel;
    isProvider  = asProvider;
    jsonMapper  = new Gson();
    jsonParser  = new JsonParser();
  }
  
  protected void initialiseAMQP( AbstractAMQPInterface eccIFace,
                                 AMQPMessageDispatch msgDispatch )
  {
    if ( eccIFace != null && msgDispatch != null )
    {
      amqpInterface = eccIFace;
      
      msgDispatch.setListener( this );
      amqpInterface.setMessageDispatch( msgDispatch );
   
      
      String faceName = interfaceName + " " + interfaceVersion;
      
      if ( eccIFace instanceof AMQPHalfInterfaceBase )
      {
        AMQPHalfInterfaceBase halfFace = (AMQPHalfInterfaceBase) eccIFace;
        halfFace.initialise( faceName, interfaceProviderID, isProvider );
      }
      else if ( eccIFace instanceof AMQPFullInterfaceBase )
      {
        AMQPFullInterfaceBase fullFace = (AMQPFullInterfaceBase) eccIFace;
        fullFace.initialise( faceName, interfaceProviderID, interfaceUserID, isProvider );
      }
    }
  }
  
  protected boolean executeMethod( int methodID, List<Object> parameters )
  {
    boolean result = false;
    
    if ( amqpInterface != null )
    {
      if ( parameters == null ) parameters = new ArrayList<Object>();
      
      Collection methodList = new ArrayList();
      methodList.add( methodID );
      methodList.addAll( parameters );
   
      // DEBUG -----------------------------------------------------------------
      faceDebugLogger.debug( "BaseFACE:Exe: [" +methodID+ "] " + (isProvider ? interfaceUserID : interfaceProviderID ) );
      
      String payloadData = jsonMapper.toJson( methodList );
      amqpInterface.sendBasicMessage( payloadData );

      result = true;
    }
    
    return result;
  }
  
  // Derriving classes must implement
  protected abstract void onInterpretMessage( int methodID, JsonArray methodData );
}
