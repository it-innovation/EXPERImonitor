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
using Newtonsoft.Json;






namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces
{

public class EMPostReport : EMBaseInterface,
                            IEMPostReport
{
  private IEMPostReport_UserListener userListener;
  
  public EMPostReport( AMQPBasicChannel channel,
                       AMQPMessageDispatch dispatch,
                       Guid providerID,
                       Guid userID,
                       bool isProvider ) : base( channel, isProvider )
  {
    interfaceName = "IEMPostReport";
    interfaceVersion = "0.1";
            
    interfaceProviderID = providerID;
    interfaceUserID     = userID;
    
    AMQPFullInterfaceBase fullFace = new AMQPFullInterfaceBase( channel );
    initialiseAMQP( fullFace, dispatch );
  }
  
  // IECCReport ----------------------------------------------------------------
  public void setUserListener( IEMPostReport_UserListener listener )
  { userListener = listener; }
  
  // User methods --------------------------------------------------------------
  // Method ID = 4
  public void notifyReadyToReport()
  {
    executeMethod( 4, null );
  }
  
  // Method ID = 5
  public void sendReportSummary( EMPostReportSummary summary )
  {
    List<Object> paramsList = new List<Object>();
    paramsList.Add( summary );
    
    executeMethod( 5, paramsList );
  }
  
  // Method ID = 6
  public void sendDataBatch( EMDataBatch populatedBatch )
  {
    List<Object> paramsList = new List<Object>();
    paramsList.Add( populatedBatch );
    
    executeMethod( 6, paramsList );
  }
  
  // Protected methods ---------------------------------------------------------
  protected override void onInterpretMessage( int methodID, List<string> jsonMethodData )
  {
    switch ( methodID )
    {
      case ( 1 ) :
      {
        if ( userListener != null )
          userListener.onRequestPostReportSummary( interfaceProviderID );
        
      } break;
        
      case ( 2 ) :
      {
        if ( userListener != null )
        {
          EMDataBatch batch = JsonConvert.DeserializeObject<EMDataBatch>( jsonMethodData[1] );      

          userListener.onRequestDataBatch( interfaceProviderID, batch );
        }
        
      } break;
        
      case ( 3 ) :
      {
        if ( userListener != null )
        {
          Guid id = new Guid(jsonMethodData[1]);  
          userListener.notifyReportBatchTimeOut( interfaceProviderID, id );
        }
        
      } break; 
    }
  }
}

} // namespace