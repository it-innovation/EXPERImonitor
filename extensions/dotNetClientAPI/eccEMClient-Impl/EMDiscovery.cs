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
using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor;

using System;
using System.Collections.Generic;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics;
using Newtonsoft.Json;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces
{

public class EMDiscovery : EMBaseInterface,
                           IEMDiscovery
{
  private IEMDiscovery_UserListener userListener;
    
  
  public EMDiscovery( AMQPBasicChannel    channel,
                      AMQPMessageDispatch dispatch,
                      Guid                providerID,
                      Guid                userID,
                      bool                isProvider ) : base( channel, isProvider )
  {
    interfaceName = "IEMDiscovery";
    interfaceVersion = "0.1";
            
    interfaceProviderID = providerID;
    interfaceUserID     = userID;
    
    AMQPFullInterfaceBase fullFace = new AMQPFullInterfaceBase( channel );
    initialiseAMQP( fullFace, dispatch );
  }
  
  // IECCMonitor ---------------------------------------------------------------
  public void setUserListener( IEMDiscovery_UserListener listener)
  { userListener = listener; }
    
  // User methods --------------------------------------------------------------
  // Method ID = 8
  public void readyToInitialise()
  {
    executeMethod( 8, null );
  }
  
  // Method ID = 9
  public void sendActivePhases( HashSet<EMPhase> supportedPhases )
  {
    List<Object> paramsList = new List<Object>();
    paramsList.Add( supportedPhases );
    
    executeMethod( 9, paramsList );
  }
  
  // Method ID = 10
  public void sendDiscoveryResult( bool discoveredGenerators )
  {
    List<Object> paramsList = new List<Object>();
    paramsList.Add( discoveredGenerators );
    
    executeMethod( 10, paramsList );
  }
  
  // Method ID = 11
  public void sendMetricGeneratorInfo( HashSet<MetricGenerator> generators )
  {
    List<Object> paramsList = new List<Object>();
    paramsList.Add( generators );
    
    executeMethod( 11, paramsList );
  }
  
  // Method ID = 12
  public void clientDisconnecting()
  {
    executeMethod( 12, null );
  }
  
  // Protected methods ---------------------------------------------------------
  protected override void onInterpretMessage( int methodID, List<string> jsonMethodData )
  {
    switch ( methodID )
    {
      case ( 1 ) :
      {
        if ( userListener != null )
        {
            // Need to convert from enum string values manually here
            String enumVal = jsonMethodData[1];
            EMInterfaceType faceType = EMInterfaceType.eEMUnknownInface;

            if (enumVal.Equals("eEMSetup"))            faceType = EMInterfaceType.eEMSetup;
            else if (enumVal.Equals("eEMLiveMonitor")) faceType = EMInterfaceType.eEMLiveMonitor;
            else if (enumVal.Equals("eEMPostReport"))  faceType = EMInterfaceType.eEMPostReport;
            else if (enumVal.Equals("eEMTearDown"))    faceType = EMInterfaceType.eEMTearDown;

            if (faceType != EMInterfaceType.eEMUnknownInface)
                userListener.onCreateInterface( interfaceProviderID, faceType );
        }
        
      } break;
        
      case ( 2 ) :
      {
        if ( userListener != null )
        {
            bool confirmed      = Boolean.Parse(jsonMethodData[1]);
            Guid expUniqueID    = new Guid(jsonMethodData[2]);
            string expNamedID   = jsonMethodData[3];
            string expName      = jsonMethodData[4];
            string expDesc      = jsonMethodData[5];
            DateTime createTime = DateTime.Parse(jsonMethodData[6]);

            userListener.onRegistrationConfirmed( interfaceProviderID, 
                                                  confirmed, expUniqueID, 
                                                  expNamedID, expName, expDesc,
                                                  createTime );
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
          string endPoint = jsonMethodData[1];
          userListener.onSetStatusMonitorEndpoint( interfaceProviderID, endPoint );
        } 
        
      } break;
          
      case ( 13 ) :
      {
        if ( userListener != null )
        {
          string reason = jsonMethodData[1];
          userListener.onDeregisteringThisClient( interfaceProviderID, reason );
        }
        
      } break;
    }
  }
}

} // namespace