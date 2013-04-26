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
//      Created Date :          10-Apr-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

using uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.faces;

using System;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using Newtonsoft.Json.Converters;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces
{

public abstract class EMBaseInterface : IAMQPMessageDispatchListener
{
  protected static IECCLogger faceLogger = Logger.getLogger( typeof(EMBaseInterface) );
  protected static JsonSerializerSettings dateSerialSettings = new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.IsoDateFormat };

  protected string  interfaceName;
  protected string  interfaceVersion;
  protected bool    isProvider;
  
  protected AMQPBasicChannel      amqpChannel;
  protected AbstractAMQPInterface amqpInterface;
  protected Guid                  interfaceUserID;
  protected Guid                  interfaceProviderID;

  
  

  public EMBaseInterface( AMQPBasicChannel channel,
                          bool asProvider )
  {
    amqpChannel = channel;
    isProvider  = asProvider;
  }

  ~EMBaseInterface()
  {
      shutdown();
  }
  
  public void shutdown()
  {
      if (amqpInterface != null)
      {
          amqpInterface.shutdown();
          amqpInterface = null;
          // Channel is managed elsewhere
      }
  }
  
  // IAMQPMessageDispatchListener ----------------------------------------------
  public void onSimpleMessageDispatched( String queueName, byte[] data )
  {
      if ( queueName != null && data != null )
      {
      try
      {
          string jsonData = System.Text.Encoding.UTF8.GetString(data);

          List<string> jsonItems = JsonConvert.DeserializeObject<List<string>>(jsonData);
          int methodID = JsonConvert.DeserializeObject<int>( jsonItems[0] );

          onInterpretMessage( methodID, jsonItems );
      }
      catch (Exception e) 
      { faceLogger.error( "Could not re-encode Rabbit data" + e.Message); }
    }
  }
  
  // Protected methods ---------------------------------------------------------  
  protected void initialiseAMQP( AbstractAMQPInterface eccIFace,
                                 AMQPMessageDispatch msgDispatch )
  {
    if ( eccIFace != null && msgDispatch != null )
    {
      amqpInterface = eccIFace;
      
      msgDispatch.setListener( this );
      amqpInterface.setMessageDispatch( msgDispatch );
   
      
      String faceName = interfaceName + " " + interfaceVersion;
      
      if ( eccIFace.GetType() == typeof(AMQPHalfInterfaceBase) )
      {
        AMQPHalfInterfaceBase halfFace = (AMQPHalfInterfaceBase) eccIFace;
        halfFace.initialise( faceName, interfaceProviderID, isProvider );
      }
      else if ( eccIFace.GetType() == typeof(AMQPFullInterfaceBase) )
      {
        AMQPFullInterfaceBase fullFace = (AMQPFullInterfaceBase) eccIFace;
        fullFace.initialise( faceName, interfaceProviderID, interfaceUserID, isProvider );
      }
    }
  }
  
  protected bool executeMethod( int methodID, List<Object> parameters )
  {
        bool result = false;
    
        if ( amqpInterface != null )
        {
            if ( parameters == null ) parameters = new List<Object>();

            List<Object> methodList = new List<Object>();
            methodList.Add( methodID );

            foreach (Object param in parameters)
                methodList.Add(param);

            string payloadData = JsonConvert.SerializeObject(methodList);

            if (!amqpInterface.sendBasicMessage(payloadData))
                faceLogger.error("Could not execute method " + methodID);
            else
                result = true;
        }
    
    return result;
  }
  
  // Derriving classes must implement
  protected abstract void onInterpretMessage(int methodID, List<string> jsonItems);
}

} // namespace