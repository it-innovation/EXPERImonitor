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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.IEMReport;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.listeners.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPMessageDispatch;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.faces.AMQPFullInterfaceBase;

import java.util.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.dataModel.EMMethodPayload;




public class EMReport extends EMBaseInterface
                       implements IEMReport
{
  private IECCReport_ProviderListener providerListener;
  private IECCReport_UserListener     userListener;
  
  public EMReport( AMQPBasicChannel channel,
                    AMQPMessageDispatch dispatch,
                    UUID providerID,
                    UUID userID,
                    boolean isProvider )
  {
    super( channel, isProvider );
    interfaceName = "IECCReport";
    interfaceVersion = "0.1";
            
    interfaceProviderID = providerID;
    interfaceUserID     = userID;
    
    AMQPFullInterfaceBase fullFace = new AMQPFullInterfaceBase( channel );
    initialiseAMQP( fullFace, dispatch );
  }
  
  // IECCReport ----------------------------------------------------------------
  @Override
  public void setProviderListener( IECCReport_ProviderListener listener )
  { providerListener = listener; }
  
  @Override
  public void setUserListener( IECCReport_UserListener listener )
  { userListener = listener; }
  
  // Provider methods ----------------------------------------------------------
  // Method ID = 1
  @Override
  public void requestReportSummary()
  {
    executeMethod( 1, null );
  }
  
  // Method ID = 2
  @Override
  public void requestNextReportMetadata()
  {
    executeMethod( 2, null );
  }
  
  // Method ID = 3
  @Override
  public void requestDataPart( /* data model */ )
  {
    //TODO: Data model
    executeMethod( 3, null );
  }
  
  // Method ID = 4
  @Override
  public void notifyReportTimeOut( /* data model */ ) 
  {
    //TODO: Data model
    executeMethod( 4, null );
  }
  
  // User methods --------------------------------------------------------------
  // Method ID = 5
  @Override
  public void notifyReadyToReport()
  {
    executeMethod( 5, null );
  }
  
  // Method ID = 6
  @Override
  public void sendReportSummary( /* data model here */ )
  {
    //TODO: Data model
    executeMethod( 6, null );
  }
  
  // Method ID = 7
  @Override
  public void sendReportMetaData( /* data model here */ )
  {
    //TODO: Data model
    executeMethod( 7, null );
  }
  
  // Method ID = 8
  @Override
  public void sendDataPart( /* data model here */ )
  {
    //TODO: Data model
    executeMethod( 8, null );
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
          userListener.onRequestReportSummary( interfaceProviderID );
        
      } break;
        
      case ( 2 ) :
      {
        if ( userListener != null )
          userListener.onRequestNextReportMetadata( interfaceProviderID );
        
      } break;
        
      case ( 3 ) :
      {
        //TODO: Data model
        if ( userListener != null )
          userListener.onRequestDataPart( interfaceProviderID );
        
      } break;
        
      case ( 4 ) :
      {
        //TODO: Data model
        if ( userListener != null )
          userListener.onNotifyReportTimeOut( interfaceProviderID );
        
      } break;
        
      case ( 5 ) :
      {
        if ( providerListener != null )
          providerListener.onNotifyReadyToReport( interfaceUserID );
        
      } break;
        
      case ( 6 ) :
      {
        //TODO: Data model
        if ( providerListener != null )
          providerListener.onSendReportSummary( interfaceUserID );
        
      } break;
        
      case ( 7 ) :
      {
        //TODO: Data model
        if ( providerListener != null )
          providerListener.onSendReportMetaData( interfaceUserID );
        
      } break;
        
      case ( 8 ) :
      {
        //TODO: Data model
        if ( providerListener != null )
          providerListener.onSendDataPart( interfaceUserID );
        
      } break;
        
    }
  }
}
