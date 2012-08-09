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
//      Created Date :          02-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.faces.AMQPFullInterfaceBase;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.dataModel.EMMethodPayload;

import java.util.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPMessageDispatch;




public class ECCMonitor extends ECCBaseInterface
                        implements IECCMonitor
{
  private IECCMonitor_ProviderListener providerListener;
  private IECCMonitor_UserListener     userListener;
  
  public enum EMInterfaceType { eECCMetricEnumerator,
                                eECCMetricCalibration,
                                eECCMonitorControl,
                                eECCReport,
                                eECCTearDown,
                                eECCTestInterface };
  
  public enum EMMonitorPhases { eEnumerateMetrics,
                                eMetricCalibration,
                                eMonitorControl,
                                eReport };
  
  
  public ECCMonitor( AMQPBasicChannel channel,
                     AMQPMessageDispatch dispatch,
                     UUID providerID,
                     UUID userID,
                     boolean isProvider )
  {
    super( channel, isProvider );
    interfaceName = "IECCMonitor";
    interfaceVersion = "0.1";
            
    interfaceProviderID = providerID;
    interfaceUserID     = userID;
    
    AMQPFullInterfaceBase fullFace = new AMQPFullInterfaceBase( channel );
    initialiseAMQP( fullFace, dispatch );
  }
  
  // IECCMonitor ---------------------------------------------------------------
  @Override
  public void setProviderListener( IECCMonitor_ProviderListener listener )
  { providerListener = listener; }
  
  @Override
  public void setUserListener( IECCMonitor_UserListener listener)
  { userListener = listener; }
  
  // Provider ------------------------------------------------------------------
  // Method ID = 1
  @Override
  public void createInterface( IECCMonitor.EMInterfaceType type )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( type );
    
    executeMethod( 1, params );
  }
  
  // Method ID = 2
  @Override
  public void registrationConfirmed( Boolean confirmed )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( confirmed );
    
    executeMethod( 2, params );
  }
  
  // Method ID = 3
  @Override
  public void requestActivityPhases()
  {
    executeMethod( 3, null );
  }
  
  // Method ID = 4
  @Override
  public void discoverMetricGenerators()
  {
    executeMethod( 4, null );
  }
  
  // Method ID = 5
  @Override
  public void discoveryTimeOut()
  {
    executeMethod( 5, null );
  }
  
  // Method ID = 6
  @Override
  public void setStatusMonitorEndpoint( /* Data model under development */ )
  {
    executeMethod( 6, null );
  }
  
  // User methods --------------------------------------------------------------
  // Method ID = 7
  @Override
  public void readyToInitialise()
  {
    executeMethod( 7, null );
  }
  
  // Method ID = 8
  @Override
  public void sendActivePhases( List<EMSupportedPhase> supportedPhases )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( supportedPhases );
    
    executeMethod( 8, params );
  }
  
  // Method ID = 9
  @Override
  public void sendDiscoveryResult( /* Data model under development*/ )
  {
    executeMethod( 9, null );
  }
  
  // Method ID = 10
  @Override
  public void clientDisconnecting()
  {
    executeMethod( 10, null );
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
        {
          IECCMonitor.EMInterfaceType type = (IECCMonitor.EMInterfaceType) params.get( 0 );
          userListener.onCreateInterface( interfaceProviderID, type );
        }
        
      } break;
        
      case ( 2 ) :
      {
        if ( userListener != null )
        {
          boolean confirmed = (Boolean) params.get( 0 );
          userListener.onRegistrationConfirmed( interfaceProviderID, confirmed );
        }
      } break;
        
      case ( 3 ) :
      {
        if ( userListener != null )
          userListener.onRequestActivityPhases( interfaceProviderID );
        
      } break;
        
      case ( 4 ) :
      {
        if ( userListener != null )
          userListener.onDiscoverMetricGenerators( interfaceProviderID );
        
      } break;
        
      case ( 5 ) :
      {
        if ( userListener != null )
          userListener.onDiscoveryTimeOut( interfaceProviderID );
        
      } break;
        
      case ( 6 ) :
      {
        if ( userListener != null )
          userListener.onSetStatusMonitorEndpoint( interfaceProviderID );
        
      } break;
        
      case ( 7 ) :
      {
        if ( providerListener != null )
          providerListener.onReadyToInitialise( interfaceUserID );
        
      } break;
        
      case ( 8 ) :
      {
        if ( providerListener != null )
        {
          List<EMSupportedPhase> phases = (List<EMSupportedPhase>) params.get( 0 );       
          providerListener.onSendActivityPhases( interfaceUserID, phases );
        }
        
      } break;
        
      case ( 9 ) :
      {
        if ( providerListener != null )
          providerListener.onSendDiscoveryResult( interfaceUserID );
        
      } break;
        
      case ( 10 ) :
      {
        if ( providerListener != null )
          providerListener.onClientDisconnecting( interfaceUserID );
        
      } break;
    }
  }
}
