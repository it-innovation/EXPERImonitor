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
//      Created Date :          29-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecyle.phases;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMPostReport_ProviderListener;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces.EMPostReport;


import java.util.UUID;




public class EMPostReportPhase extends AbstractEMLCPhase
                               implements IEMPostReport_ProviderListener
{
  private EMPostReportPhaseListener phaseListener;
  
  public EMPostReportPhase( AMQPBasicChannel channel,
                            UUID providerID,
                            EMPostReportPhaseListener listener )
  {
    super( EMPhase.eEMPostMonitoringReport, channel, providerID );
    
    phaseListener = listener;
    
    phaseState = "Ready to start post-report process";
  }
  
  // AbstractEMLCPhase ---------------------------------------------------------
  @Override
  public void start() throws Exception
  {
    if ( phaseActive ) throw new Exception( "Phase already active" );
    if ( !hasClients() ) throw new Exception( "No clients available for this phase" );
    
    // Create post-report interface
    for ( EMClientEx client : getCopySetOfCurrentClients() )
    {
      AMQPMessageDispatch dispatch = new AMQPMessageDispatch();
      phaseMsgPump.addDispatch( dispatch );
      
      EMPostReport face = new EMPostReport( emChannel, dispatch,
                                            emProviderID, client.getID(), true );
      
      face.setProviderListener( this );
      client.setPostReportInterface( face );
    }
    
    phaseMsgPump.startPump();
    phaseActive = true;
    
    // Request clients create post-report interface
    for ( EMClientEx client : getCopySetOfCurrentClients() )
      client.getDiscoveryInterface().createInterface( EMInterfaceType.eEMPostReport );
    
    phaseState = "Waiting for client to signal ready to report";
  }
  
  @Override
  public void controlledStop() throws Exception
  {
    //TODO
    throw new Exception( "Not yet supported for this phase");
  }
  
  @Override
  public void hardStop()
  {
    phaseMsgPump.stopPump();
    phaseActive = false;
    
    if ( phaseListener != null ) phaseListener.onPostReportPhaseCompleted();
  }
  
  // IEMPostReport_ProviderListener --------------------------------------------
  @Override
  public void onNotifyReadyToReport( UUID senderID )
  {
    if ( phaseActive )
    {
      EMClientEx client = getClient( senderID );
      
      if ( client != null )
        client.getPostReportInterface().requestPostReportSummary();
    }
  }
  
  @Override
  public void onSendReportSummary( UUID senderID, EMPostReportSummary summary )
  {
    if ( phaseActive )
    {
      EMClientEx client = getClient( senderID );
      
      if ( client != null )
      {
        client.setPostReportSummary( summary );
        
        if ( phaseListener != null )
          phaseListener.onGotSummaryReport( client, summary );
      }
    }
  }
  
  @Override
  public void onSendDataBatch( UUID senderID, EMDataBatch populatedBatch )
  {
    if ( phaseActive )
    {
      EMClientEx client = getClient( senderID );
      
      if ( client != null && phaseListener != null )
        phaseListener.onGotDataBatch( client, populatedBatch );
    }
  }
}
