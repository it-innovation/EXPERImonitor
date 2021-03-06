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
//      Created Date :          19-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.IEMMetricGenSetup;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.faces.AMQPFullInterfaceBase;

import com.google.gson.JsonArray;
import java.util.*;



public class EMMetricGenSetup extends EMBaseInterface
                              implements IEMMetricGenSetup
{
  private IEMSetup_ProviderListener providerListener;
  private IEMSetup_UserListener     userListener;
  
  public EMMetricGenSetup( AMQPBasicChannel channel,
                           AMQPMessageDispatch dispatch,
                           UUID providerID,
                           UUID userID,
                           boolean isProvider )
  {
    super( channel, isProvider );
    
    interfaceName = "IEMMetricGenSetup";
    interfaceVersion = "0.1";
            
    interfaceProviderID = providerID;
    interfaceUserID     = userID;
    
    AMQPFullInterfaceBase fullFace = new AMQPFullInterfaceBase( channel );
    initialiseAMQP( fullFace, dispatch );
  }
  
  // IEMMonitorSetup -----------------------------------------------------------
  @Override
  public void setProviderListener( IEMSetup_ProviderListener listener )
  { providerListener = listener; }
  
  @Override
  public void setUserListener( IEMSetup_UserListener listener)
  { userListener = listener; }
  
  // Provider methods ----------------------------------------------------------
  // Method ID = 1
  @Override
  public void setupMetricGenerator( UUID genID )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( genID );
    
    executeMethod( 1, params );
  }
  
  // Method ID = 2
  @Override
  public void setupTimeOut( UUID genID )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( genID );
    
    executeMethod( 2, params );
  }
  
  // User methods --------------------------------------------------------------
  // Method ID = 3
  @Override
  public void notifyReadyToSetup()
  {
    executeMethod( 3, null );
  }
  
  // Method ID = 4
  @Override
  public void notifyMetricGeneratorSetupResult( UUID genID, Boolean success )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( genID );
    params.add( success );
    
    executeMethod( 4, params );
  }
  
  // Protected methods ---------------------------------------------------------
  @Override
  protected void onInterpretMessage( int methodID, JsonArray methodData )
  {   
    switch ( methodID )
    {
      case 1 :
      { 
        if ( userListener != null )
        {
          UUID genID = jsonMapper.fromJson( methodData.get(1), UUID.class );
          userListener.onSetupMetricGenerator( interfaceProviderID, genID );
        }
        
      } break;
        
      case 2 :
      {
        if ( userListener != null )
        {
          UUID genID = jsonMapper.fromJson( methodData.get(1), UUID.class );
          userListener.onSetupTimeOut( interfaceProviderID, genID );
        }
        
      } break;
        
      case 3 :
      {
        if ( providerListener != null )
          providerListener.onNotifyReadyToSetup( interfaceUserID );
        
      } break;
        
      case 4 :
      {
        if ( providerListener != null )
        {
          UUID genID      = jsonMapper.fromJson( methodData.get(1), UUID.class );
          Boolean success = jsonMapper.fromJson( methodData.get(2), Boolean.class );
          
          providerListener.onNotifyMetricGeneratorSetupResult( interfaceUserID, 
                                                               genID, 
                                                               success );
        }
        
      } break;
    }
  }
}
