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
//      Created Date :          10-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.IEMTearDown;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.listeners.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.faces.AMQPFullInterfaceBase;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.dataModel.EMMethodPayload;

import java.util.*;


public class EMTearDown extends EMBaseInterface
                         implements IEMTearDown
{
  private IECCTearDown_ProviderListener providerListener;
  private IECCTearDown_UserListener     userListener;
  
  
  public EMTearDown( AMQPBasicChannel channel,
                      AMQPMessageDispatch dispatch,
                      UUID providerID,
                      UUID userID,
                      boolean isProvider )
  {
    super( channel, isProvider );
    interfaceName = "IECCTearDown";
    interfaceVersion = "0.1";
            
    interfaceProviderID = providerID;
    interfaceUserID     = userID;
    
    AMQPFullInterfaceBase fullFace = new AMQPFullInterfaceBase( channel );
    initialiseAMQP( fullFace, dispatch );
  }
  
  // IECCTearDown --------------------------------------------------------------
  @Override
  public void setProviderListener( IECCTearDown_ProviderListener listener )
  { providerListener = listener; }
  
  @Override
  public void setUserListener( IECCTearDown_UserListener listener )
  { userListener = listener; }
  
  // Provider methods ----------------------------------------------------------
  // Method ID = 1
  @Override
  public void tearDownMetricGenerators()
  {
    executeMethod( 1, null );
  }
  
  @Override
  // Method ID = 2
  public void tearDownTimeOut()
  {
    executeMethod( 2, null );
  }
  
  // User methods --------------------------------------------------------------
  @Override
  // Method ID = 3
  public void notifyReadyToTearDown()
  {
    executeMethod( 3, null );
  }
  
  @Override
  // Method ID = 4
  public void sendTearDownResult( /* data model */ )
  {
    //TODO: Data model
    executeMethod( 4, null );
  }
  
  // Protected methods ---------------------------------------------------------
  @Override
  protected void onInterpretMessage( EMMethodPayload payload )
  {
    List<Object> params = payload.getParameters();
    
    switch ( payload.getMethodID() )
    {
      case ( 1 ) :
      {
        if ( userListener != null )
          userListener.onTearDownMetricGenerators( interfaceProviderID );
        
      } break;
        
      case ( 2 ) :
      {
        if ( userListener != null )
          userListener.onTearDownTimeOut( interfaceProviderID );
        
      } break;
        
      case ( 3 ) :
      {
        if ( providerListener != null )
        {
          providerListener.onNotifyReadyToTearDown( interfaceUserID );
        }
        
      } break;
        
      case ( 4 ) :
      {
        //TODO: Data model
        if ( providerListener != null )
          providerListener.onNotifyTearDownResult( interfaceUserID );
        
      } break;
    }
  }
}
