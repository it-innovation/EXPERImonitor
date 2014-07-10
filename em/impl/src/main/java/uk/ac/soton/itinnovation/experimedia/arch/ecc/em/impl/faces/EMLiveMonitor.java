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
//      Created Date :          22-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.IEMLiveMonitor;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.faces.AMQPFullInterfaceBase;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import com.google.gson.JsonArray;
import java.util.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvReport;





public class EMLiveMonitor extends EMBaseInterface
                           implements IEMLiveMonitor
{
  private IEMLiveMonitor_ProviderListener providerListener;
  private IEMLiveMonitor_UserListener     userListener;
  
  
  public EMLiveMonitor( AMQPBasicChannel    channel,
                        AMQPMessageDispatch dispatch,
                        UUID                providerID,
                        UUID                userID,
                        boolean             isProvider )
  {
    super( channel, isProvider );
    interfaceName = "IEMLiveMonitor";
    interfaceVersion = "0.1";
            
    interfaceProviderID = providerID;
    interfaceUserID     = userID;
    
    AMQPFullInterfaceBase fullFace = new AMQPFullInterfaceBase( channel );
    initialiseAMQP( fullFace, dispatch );
  }
  
  @Override
  public void setProviderListener( IEMLiveMonitor_ProviderListener listener )
  { providerListener = listener; }
  
  @Override
  public void setUserListener( IEMLiveMonitor_UserListener listener )
  { userListener = listener; }
  
  // IEMLiveMonitor ------------------------------------------------------------
  // Provider methods ----------------------------------------------------------
  // Method ID = 1
  @Override
  public void startPushing()
  {
    executeMethod( 1, null );
  }
  
  // Method ID = 2
  @Override
  public void notifyPushReceived( UUID lastReportID )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( lastReportID );
    
    executeMethod( 2, params );
  }
  
  // Method ID = 3
  @Override
  public void stopPushing()
  {
    executeMethod( 3, null );
  }
  
  // Method ID = 4
  @Override
  public void pullMetric( UUID measurementSetID )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( measurementSetID );
    
    executeMethod( 4, params );
  }
  
  // Method ID = 5
  @Override
  public void pullMetricTimeOut( UUID measurementSetID )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( measurementSetID );
    
    executeMethod( 5, params );
  }
  
  // Method ID = 6
  @Override
  public void pullingStopped()
  {
    executeMethod( 6, null );
  }
  
  // Method ID = 12
  @Override
  public void notifyPullReceived( UUID lastReportID )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( lastReportID );
    
    executeMethod( 12, params );
  }
  
  // User methods --------------------------------------------------------------
  // Method ID = 7
  @Override
  public void notifyReadyToPush()
  {
    executeMethod( 7, null );
  }
  
  // Method ID = 8
  @Override
  public void pushMetric( Report report )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( report );
    
    executeMethod( 8, params );
  }
  
  // Method ID = 13
  @Override
  public void pushPROVStatement( EDMProvReport statement )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( statement );
    
    executeMethod( 13, params );
  }
  
  // Method ID = 9
  @Override
  public void notifyPushingCompleted()
  {
    executeMethod( 9, null );
  }
  
  // Method ID = 10
  @Override
  public void notifyReadyForPull()
  {
    executeMethod( 10, null );
  }
  
  // Method ID = 11
  @Override
  public void sendPulledMetric( Report report )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( report );
    
    executeMethod( 11, params );
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
          userListener.onStartPushing( interfaceProviderID );
        
      } break;
        
      case 2 :
      {
        if ( userListener != null )
        {
          UUID reportID = jsonMapper.fromJson( methodData.get(1), UUID.class );
          userListener.onReceivedPush( interfaceProviderID, reportID );
        }
        
      } break;
        
      case 3 :
      {
        if ( userListener != null )
          userListener.onStopPushing( interfaceProviderID );
        
      } break;
        
      case 4 :
      {
        if ( userListener != null )
        {
          UUID msID = jsonMapper.fromJson( methodData.get(1), UUID.class );
          userListener.onPullMetric( interfaceProviderID, msID );
        }
        
      } break;
        
      case 5 :
      {
        if ( userListener != null )
        {
          UUID msID = jsonMapper.fromJson( methodData.get(1), UUID.class );
          userListener.onPullMetricTimeOut( interfaceProviderID, msID );
        }
        
      } break;
        
      case 6 :
      {
        if ( userListener != null )
          userListener.onPullingStopped( interfaceProviderID );
        
      } break;
        
      case 7 :
      {
        if ( providerListener != null )
          providerListener.onNotifyReadyToPush( interfaceUserID );
        
      } break;
        
      case 8 :
      {
        if ( providerListener != null )
        {
          Report report = jsonMapper.fromJson( methodData.get(1), Report.class );
          providerListener.onPushMetric( interfaceUserID, report );
        }
        
      } break;
        
      case 9 :
      {
        if ( providerListener != null )
          providerListener.onNotifyPushingCompleted( interfaceUserID );
        
      } break;
        
      case 10 :
      {
        if ( providerListener != null )
          providerListener.onNotifyReadyForPull( interfaceUserID );
        
      } break;
        
      case 11 :
      {
        if ( providerListener != null )
        {          
          Report report = jsonMapper.fromJson( methodData.get(1), Report.class );
          providerListener.onSendPulledMetric( interfaceUserID, report );
        }
        
      } break;
        
      case 12 :
      {
        if ( userListener != null )
        {
          UUID reportID = jsonMapper.fromJson( methodData.get(1), UUID.class );
          userListener.onReceivedPull( interfaceProviderID, reportID );
        }
      } break;
        
      case 13 :
      {
        if ( providerListener != null )
        {
          EDMProvReport statement = jsonMapper.fromJson( methodData.get(1), EDMProvReport.class );
          providerListener.onPushPROVStatement( interfaceUserID, statement );
        }
      } break;
    }
  }
}
