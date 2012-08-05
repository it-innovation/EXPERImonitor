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
//      Created Date :          29-Jul-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.base;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.eccInterface.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.dataModel.EMMethodPayload;

import org.yaml.snakeyaml.*;
import java.util.*;






public abstract class ECCBaseInterface implements MessageDispatchListener
{
  protected String               interfaceName;
  protected String               interfaceVersion;
  protected boolean              isProvider;
  protected Yaml                 yamlUtil;
  
  protected AMQPBasicChannel     amqpChannel;
  protected AbstractECCInterface amqpInterface;
  protected UUID                 interfaceUserID;
  protected UUID                 interfaceProviderID;
  

  
  
  // MessageDispatchListener ---------------------------------------------------
  @Override
  public void onSimpleMessageDispatched( String queueName, byte[] data )
  {        
    EMMethodPayload empl = (EMMethodPayload) yamlUtil.load( new String(data) );
    if ( empl != null ) onInterpretMessage( empl );
  }
  
  // Protected methods ---------------------------------------------------------
  protected ECCBaseInterface( AMQPBasicChannel channel,
                              boolean asProvider )
  {
    amqpChannel = channel;
    isProvider  = asProvider;
    yamlUtil    = new Yaml();
  }
  
  protected void initialiseAMQP( AbstractECCInterface eccIFace )
  {
    amqpInterface = eccIFace;
    
    if ( amqpInterface != null )
    {
      ECCInterfaceMessageDispatch dispatch = new ECCInterfaceMessageDispatch();
      amqpInterface.setMessageDispatch( dispatch );
      dispatch.start( this );
      
      String faceName = interfaceName + " " + interfaceVersion;
      
      if ( eccIFace instanceof ECCHalfInterfaceBase )
      {
        ECCHalfInterfaceBase halfFace = (ECCHalfInterfaceBase) eccIFace;
        halfFace.initialise( faceName, interfaceProviderID, isProvider );
      }
      else if ( eccIFace instanceof ECCFullInterfaceBase )
      {
        ECCFullInterfaceBase fullFace = (ECCFullInterfaceBase) eccIFace;
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
      
      EMMethodPayload empl = new EMMethodPayload();
      empl.setMethodID( methodID );
      empl.setParameters( parameters );
      
      amqpInterface.sendBasicMessage( yamlUtil.dump(empl) );
      result = true;
    }
    
    return result;
  }
  
  // Derriving classes must implement
  protected abstract void onInterpretMessage( EMMethodPayload payload );
}
