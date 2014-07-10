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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMDiscovery_UserListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMDiscovery_ProviderListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.faces.AMQPFullInterfaceBase;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;

import com.google.gson.*;
import java.util.*;



public class EMDiscovery extends EMBaseInterface
                         implements IEMDiscovery
{
  private IEMDiscovery_ProviderListener providerListener;
  private IEMDiscovery_UserListener     userListener;
    
  
  public EMDiscovery( AMQPBasicChannel    channel,
                      AMQPMessageDispatch dispatch,
                      UUID                providerID,
                      UUID                userID,
                      boolean             isProvider )
  {
    super( channel, isProvider );
    interfaceName = "IEMDiscovery";
    interfaceVersion = "0.1";
            
    interfaceProviderID = providerID;
    interfaceUserID     = userID;
    
    AMQPFullInterfaceBase fullFace = new AMQPFullInterfaceBase( channel );
    initialiseAMQP( fullFace, dispatch );
  }
  
  // IECCMonitor ---------------------------------------------------------------
  @Override
  public void setProviderListener( IEMDiscovery_ProviderListener listener )
  { providerListener = listener; }
  
  @Override
  public void setUserListener( IEMDiscovery_UserListener listener)
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
  public void registrationConfirmed( Boolean confirmed,
                                     UUID    expUniqueID,
                                     String  expNamedID,
                                     String  expName,
                                     String  expDescription,
                                     Date    createTime )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( confirmed );
    params.add( expUniqueID );
    params.add( expNamedID );
    params.add( expName );
    params.add( expDescription );
    params.add( createTime );
    
    executeMethod( 2, params );
  }
  
  // Method ID = 13
  @Override
  public void deregisteringThisClient( String reason )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( reason );
    
    executeMethod( 13, params );
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
  
  // Method ID = 14
  @Override
  public void enableEntityMetricCollection( UUID entityID, boolean enabled )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( entityID );
    params.add( enabled );
    
    executeMethod( 14, params );
  }
  
  // Method ID = 12
  @Override
  public void clientDisconnecting()
  {
    executeMethod( 12, null );
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
          EMInterfaceType type = jsonMapper.fromJson( methodData.get(1), EMInterfaceType.class );
          userListener.onCreateInterface( interfaceProviderID, type );
        }
        
      } break;
        
      case 2 :
      {
        if ( userListener != null )
        {
          boolean confirmed = jsonMapper.fromJson( methodData.get(1), Boolean.class );
          UUID  expUniqueID = jsonMapper.fromJson( methodData.get(2), UUID.class );
          String expNamedID = jsonMapper.fromJson( methodData.get(3), String.class );
          String    expName = jsonMapper.fromJson( methodData.get(4), String.class );
          String    expDesc = jsonMapper.fromJson( methodData.get(5), String.class ); 
          Date   createTime = jsonMapper.fromJson( methodData.get(6), Date.class );

          userListener.onRegistrationConfirmed( interfaceProviderID, 
                                                confirmed, expUniqueID, 
                                                expNamedID, expName, expDesc,
                                                createTime );
        }
      } break;
        
      case 3 :
      {
        if ( userListener != null )
          userListener.onRequestActivityPhases( interfaceProviderID );
        
      } break;
        
      case 4 :
      {
        if ( userListener != null )
          userListener.onDiscoverMetricGenerators( interfaceProviderID );
        
      } break;
        
      case 5 :
      {
        if ( userListener != null )
          userListener.onRequestMetricGeneratorInfo( interfaceProviderID );
        
      } break;
        
      case 6 :
      {
        if ( userListener != null )
          userListener.onDiscoveryTimeOut( interfaceProviderID );
        
      } break;
        
      case 7 :
      {
        if ( userListener != null )
        {
          String endPoint = jsonMapper.fromJson( methodData.get(1), String.class );
          userListener.onSetStatusMonitorEndpoint( interfaceProviderID, endPoint );
        } 
        
      } break;
        
      case 8 :
      {
        if ( providerListener != null )
          providerListener.onReadyToInitialise( interfaceUserID );
        
      } break;
        
      case 9 :
      {
        if ( providerListener != null )
        {
          EnumSet<EMPhase> phases = EnumSet.noneOf( EMPhase.class );
          JsonArray phaseArray    = (JsonArray) methodData.get(1);
          
          for ( JsonElement el : phaseArray )
          {
            String enumVal = jsonMapper.fromJson( el, String.class );
            phases.add( EMPhase.valueOf(enumVal) );
          }
                
          providerListener.onSendActivityPhases( interfaceUserID, phases );
        }
        
      } break;
        
      case 10 :
      {
        if ( providerListener != null )
        {
          Boolean result = jsonMapper.fromJson( methodData.get(1), Boolean.class );
          providerListener.onSendDiscoveryResult( interfaceUserID, result );
        }
          
      } break;
        
      case 11 :
      {
        if ( providerListener != null )
        {          
          Set<MetricGenerator> generators = new HashSet<MetricGenerator>();
          
          JsonArray genArray = (JsonArray) methodData.get(1);
          for ( JsonElement el : genArray )
          {
            MetricGenerator mg = jsonMapper.fromJson( el, MetricGenerator.class );
            generators.add( mg );
          }
         
          providerListener.onSendMetricGeneratorInfo( interfaceUserID, generators );
        }
        
      } break;
        
      case 12 :
      {
        if ( providerListener != null )
          providerListener.onClientDisconnecting( interfaceUserID );
        
      } break;
        
      case 13 :
      {
        if ( userListener != null )
        {
          String reason = jsonMapper.fromJson( methodData.get(1), String.class );
          userListener.onDeregisteringThisClient( interfaceProviderID, reason );
        }
        
      } break;
        
      case 14 :
      {
        if ( providerListener != null )
        {
          UUID entityID   = jsonMapper.fromJson( methodData.get(1), UUID.class );
          boolean enabled = jsonMapper.fromJson( methodData.get(2), Boolean.class );
          
          providerListener.onEnableEntityMetricCollection( interfaceUserID,
                                                           entityID, enabled );
        }
      }
    }
  }
}
