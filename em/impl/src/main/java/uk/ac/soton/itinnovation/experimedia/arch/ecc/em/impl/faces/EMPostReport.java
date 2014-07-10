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


import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMPostReport_UserListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMPostReport_ProviderListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.IEMPostReport;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPMessageDispatch;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.faces.AMQPFullInterfaceBase;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;

import com.google.gson.JsonArray;
import java.util.*;




public class EMPostReport extends EMBaseInterface
                          implements IEMPostReport
{
  private IEMPostReport_ProviderListener providerListener;
  private IEMPostReport_UserListener     userListener;
  
  public EMPostReport( AMQPBasicChannel channel,
                       AMQPMessageDispatch dispatch,
                       UUID providerID,
                       UUID userID,
                       boolean isProvider )
  {
    super( channel, isProvider );
    interfaceName = "IEMPostReport";
    interfaceVersion = "0.1";
            
    interfaceProviderID = providerID;
    interfaceUserID     = userID;
    
    AMQPFullInterfaceBase fullFace = new AMQPFullInterfaceBase( channel );
    initialiseAMQP( fullFace, dispatch );
  }
  
  // IECCReport ----------------------------------------------------------------
  @Override
  public void setProviderListener( IEMPostReport_ProviderListener listener )
  { providerListener = listener; }
  
  @Override
  public void setUserListener( IEMPostReport_UserListener listener )
  { userListener = listener; }
  
  // Provider methods ----------------------------------------------------------
  // Method ID = 1
  @Override
  public void requestPostReportSummary()
  {
    executeMethod( 1, null );
  }
  
  // Method ID = 2
  @Override
  public void requestDataBatch( EMDataBatch reqBatch )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( reqBatch );
    
    executeMethod( 2, params );
  }
  
  // Method ID = 3
  @Override
  public void notifyReportBatchTimeOut( UUID batchID )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( batchID );
    
    executeMethod( 3, params );
  }
  
  // User methods --------------------------------------------------------------
  // Method ID = 4
  @Override
  public void notifyReadyToReport()
  {
    executeMethod( 4, null );
  }
  
  // Method ID = 5
  @Override
  public void sendReportSummary( EMPostReportSummary summary )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( summary );
    
    executeMethod( 5, params );
  }
  
  // Method ID = 6
  @Override
  public void sendDataBatch( EMDataBatch populatedBatch )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( populatedBatch );
    
    executeMethod( 6, params );
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
          userListener.onRequestPostReportSummary( interfaceProviderID );
        
      } break;
        
      case 2 :
      {
        if ( userListener != null )
        {
          EMDataBatch batch = (EMDataBatch) jsonMapper.fromJson( methodData.get(1), EMDataBatch.class );
          
          userListener.onRequestDataBatch( interfaceProviderID, batch );
        }
        
      } break;
        
      case 3 :
      {
        if ( userListener != null )
        {
          UUID id = jsonMapper.fromJson( methodData.get(1), UUID.class );
          
          userListener.notifyReportBatchTimeOut( interfaceProviderID, id );
        }
        
      } break;
        
      case 4 :
      {
        if ( providerListener != null )
          providerListener.onNotifyReadyToReport( interfaceUserID );
        
      } break;
        
      case 5 :
      {
        if ( providerListener != null )
        {
          EMPostReportSummary summary =
              jsonMapper.fromJson( methodData.get(1), EMPostReportSummary.class );
          
          providerListener.onSendReportSummary( interfaceUserID, summary );
        }
        
      } break;
        
      case 6 :
      {
        if ( providerListener != null )
        {
          EMDataBatch batch = 
              jsonMapper.fromJson( methodData.get(1), EMDataBatch.class );
          
          providerListener.onSendDataBatch( interfaceUserID, batch );
        }
        
      } break;   
    }
  }
}
