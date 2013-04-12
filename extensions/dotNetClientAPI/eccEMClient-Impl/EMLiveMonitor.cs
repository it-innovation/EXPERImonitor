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

using uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.faces;

using System;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics;
using System.Collections.Generic;
using Newtonsoft.Json;





namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces
{

public class EMLiveMonitor : EMBaseInterface,
                             IEMLiveMonitor
{
  private IEMLiveMonitor_UserListener userListener;
  
  
  public EMLiveMonitor( AMQPBasicChannel    channel,
                        AMQPMessageDispatch dispatch,
                        Guid                providerID,
                        Guid                userID,
                        bool                isProvider ) : base( channel, isProvider )
  {
    interfaceName = "IEMLiveMonitor";
    interfaceVersion = "0.1";
            
    interfaceProviderID = providerID;
    interfaceUserID     = userID;
    
    AMQPFullInterfaceBase fullFace = new AMQPFullInterfaceBase( channel );
    initialiseAMQP( fullFace, dispatch );
  }
  
  // IEMLiveMonitor ------------------------------------------------------------
  public void setUserListener( IEMLiveMonitor_UserListener listener )
  { userListener = listener; }
  
  // User methods --------------------------------------------------------------
  // Method ID = 7
  public void notifyReadyToPush()
  {
    executeMethod( 7, null );
  }
  
  // Method ID = 8
  public void pushMetric( Report report )
  {
    List<Object> paramsList = new List<Object>();
    paramsList.Add( report );
    
    executeMethod( 8, paramsList );
  }
  
  // Method ID = 9
  public void notifyPushingCompleted()
  {
    executeMethod( 9, null );
  }
  
  // Method ID = 10
  public void notifyReadyForPull()
  {
    executeMethod( 10, null );
  }
  
  // Method ID = 11
  public void sendPulledMetric( Report report )
  {
    List<Object> paramsList = new List<Object>();
    paramsList.Add( report );
    
    executeMethod( 11, paramsList );
  }
  
  // Protected methods ---------------------------------------------------------
  protected override void onInterpretMessage( int methodID, List<string> jsonMethodData )
  {    
    switch ( methodID )
    {
      case ( 1 ) :
      {
        if ( userListener != null )
          userListener.onStartPushing( interfaceProviderID );
        
      } break;
        
      case ( 2 ) :
      {
        if ( userListener != null )
        {
          Guid reportID = new Guid(jsonMethodData[1]);
          userListener.onReceivedPush( interfaceProviderID, reportID );
        }
        
      } break;
        
      case ( 3 ) :
      {
        if ( userListener != null )
          userListener.onStopPushing( interfaceProviderID );
        
      } break;
        
      case ( 4 ) :
      {
        if ( userListener != null )
        {
          Guid msID = new Guid(jsonMethodData[1]);
          userListener.onPullMetric( interfaceProviderID, msID );
        }
        
      } break;
        
      case ( 5 ) :
      {
        if ( userListener != null )
        {
          Guid msID = new Guid(jsonMethodData[1]);
          userListener.onPullMetricTimeOut( interfaceProviderID, msID );
        }
        
      } break;
        
      case ( 6 ) :
      {
        if ( userListener != null )
          userListener.onPullingStopped( interfaceProviderID );
        
      } break;
        
      case ( 12 ) :
      {
        if ( userListener != null )
        {
          Guid reportID = new Guid(jsonMethodData[1]);
          userListener.onReceivedPull( interfaceProviderID, reportID );
        }
      } break;
    }
  }
}

} // namespace