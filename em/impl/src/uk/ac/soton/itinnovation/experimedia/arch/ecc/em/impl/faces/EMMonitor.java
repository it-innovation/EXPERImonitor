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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMMonitor_UserListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMMonitor_ProviderListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.faces.AMQPFullInterfaceBase;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;

import java.util.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;




public class EMMonitor extends EMBaseInterface
                       implements IEMMonitor
{
  private IEMMonitor_ProviderListener providerListener;
  private IEMMonitor_UserListener     userListener;
    
  
  public EMMonitor( AMQPBasicChannel    channel,
                    AMQPMessageDispatch dispatch,
                    UUID                providerID,
                    UUID                userID,
                    boolean             isProvider )
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
  public void setProviderListener( IEMMonitor_ProviderListener listener )
  { providerListener = listener; }
  
  @Override
  public void setUserListener( IEMMonitor_UserListener listener)
  { userListener = listener; }
  
  // Provider ------------------------------------------------------------------
  // Method ID = 1
  @Override
  public void createInterface( EMInterfaceType type )
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
  public void requestMetricGeneratorInfo()
  {
    executeMethod( 5, null );
  }
  
  // Method ID = 6
  @Override
  public void discoveryTimeOut()
  {
    executeMethod( 6, null );
  }
  
  // Method ID = 7
  @Override
  public void setStatusMonitorEndpoint( String endPoint )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( endPoint );
    
    executeMethod( 7, params );
  }
  
  // User methods --------------------------------------------------------------
  // Method ID = 8
  @Override
  public void readyToInitialise()
  {
    executeMethod( 8, null );
  }
  
  // Method ID = 9
  @Override
  public void sendActivePhases( EnumSet<EMPhase> supportedPhases )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( supportedPhases );
    
    executeMethod( 9, params );
  }
  
  // Method ID = 10
  @Override
  public void sendDiscoveryResult( Boolean discoveredGenerators )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( discoveredGenerators );
    
    executeMethod( 10, params );
  }
  
  // Method ID = 11
  @Override 
  public void sendMetricGeneratorInfo( Set<MetricGenerator> generators )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( generators );
    
    executeMethod( 11, params );
  }
  
  // Method ID = 12
  @Override
  public void clientDisconnecting()
  {
    executeMethod( 12, null );
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
          EMInterfaceType type = EMInterfaceType.valueOf( (String) params.get( 0 ) );
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
          userListener.onRequestMetricGeneratorInfo( interfaceProviderID );
        
      } break;
        
      case ( 6 ) :
      {
        if ( userListener != null )
          userListener.onDiscoveryTimeOut( interfaceProviderID );
        
      } break;
        
      case ( 7 ) :
      {
        if ( userListener != null )
        {
          String endPoint = (String) params.get( 0 );
          userListener.onSetStatusMonitorEndpoint( interfaceProviderID, endPoint );
        } 
        
      } break;
        
      case ( 8 ) :
      {
        if ( providerListener != null )
          providerListener.onReadyToInitialise( interfaceUserID );
        
      } break;
        
      case ( 9 ) :
      {
        if ( providerListener != null )
        {
          ArrayList<String> stringPhases = (ArrayList<String>) params.get(0);
          EnumSet<EMPhase> phases = EnumSet.noneOf( EMPhase.class );
          
          for ( String phaseString : stringPhases )
            phases.add( EMPhase.valueOf(phaseString) );
                
          providerListener.onSendActivityPhases( interfaceUserID, phases );
        }
        
      } break;
        
      case ( 10 ) :
      {
        if ( providerListener != null )
        {
          Boolean result = (Boolean) params.get( 0 );
          providerListener.onSendDiscoveryResult( interfaceUserID, result );
        }
          
      } break;
        
      case ( 11 ) :
      {
        if ( providerListener != null )
        {
          //TODO: Write jolly MetricGenerator de-serializer :(
          ArrayList<LinkedHashMap> serialGens
             = new ArrayList<LinkedHashMap>( (ArrayList<LinkedHashMap>) params.get(0) );
          
          Set<MetricGenerator> generators = new HashSet<MetricGenerator>();
          
          for ( LinkedHashMap lhm : serialGens )
          {
            MetricGenerator mg = new MetricGenerator( UUID.fromString( (String) lhm.get("uuid") ),
                                                      (String) lhm.get("name"),
                                                      (String) lhm.get("description") );
            generators.add( mg );
          }
          
          providerListener.onSendMetricGeneratorInfo( interfaceUserID, generators );
        }
        
      } break;
        
      case ( 12 ) :
      {
        if ( providerListener != null )
          providerListener.onClientDisconnecting( interfaceUserID );
        
      } break;
    }
  }
}
