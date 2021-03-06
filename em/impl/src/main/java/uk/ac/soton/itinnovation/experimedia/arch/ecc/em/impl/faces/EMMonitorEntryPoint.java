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
//      Created Date :          31-Jul-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMMonitorEntryPoint_ProviderListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.faces.AMQPHalfInterfaceBase;

import com.google.gson.JsonArray;
import java.util.*;





public class EMMonitorEntryPoint extends EMBaseInterface
                                 implements IEMMonitorEntryPoint
{
  private IEMMonitorEntryPoint_ProviderListener providerListener;
  
  
  public EMMonitorEntryPoint( AMQPBasicChannel    channel,
                              AMQPMessageDispatch dispatch,
                              UUID                providerID,
                              boolean             isProvider )
  {
    super( channel, isProvider );
    interfaceName = "IECCMonitorEntryPoint";
    interfaceVersion = "0.1";
    
    interfaceProviderID = providerID;
    
    AMQPHalfInterfaceBase entryPoint = new AMQPHalfInterfaceBase( channel );
    initialiseAMQP( entryPoint, dispatch );
  }
  
  // IECCMonitorEntryPoint -----------------------------------------------------
  @Override
  public void setListener( IEMMonitorEntryPoint_ProviderListener listener )
  { providerListener = listener; }
  
  // Method ID = 1
  @Override
  public void registerAsEMClient( UUID userID, String userName )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( userID.toString() );
    params.add( userName );
    
    executeMethod( 1, params );
  }
  
  // Protected methods ---------------------------------------------------------
  @Override
  protected void onInterpretMessage( int methodID, JsonArray methodData )
  {
    // 'RegisterAsEMClient' method (ID = 1) ------------------------------------
    if ( methodID == 1 && providerListener != null )
    {      
      UUID userID = jsonMapper.fromJson( methodData.get(1), UUID.class );
      String userName = jsonMapper.fromJson( methodData.get(2), String.class );
      
      providerListener.onRegisterAsEMClient( userID, userName );
    }
  }
}
