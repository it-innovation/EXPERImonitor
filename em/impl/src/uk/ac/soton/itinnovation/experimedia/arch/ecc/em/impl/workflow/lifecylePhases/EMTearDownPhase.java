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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecylePhases;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMTearDown_ProviderListener;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces.EMTearDown;

import java.util.*;




public class EMTearDownPhase extends AbstractEMLCPhase
                                     implements IEMTearDown_ProviderListener
{
  private EMTearDownPhaseListener phaseListener;
  
  private HashSet<UUID> clientsStillToTearDown;
  
  
  public EMTearDownPhase( AMQPBasicChannel channel,
                          UUID providerID,
                          EMTearDownPhaseListener listener )
  {
    super( EMPhase.eEMTearDown, channel, providerID );
    
    phaseListener = listener;
    
    clientsStillToTearDown = new HashSet<UUID>();
    
    phaseState = "Ready to start tear-down process";
  }
  
  // AbstractEMLCPhase ---------------------------------------------------------
  @Override
  public void start() throws Exception
  {
    if ( phaseActive ) throw new Exception( "Phase already active" );
    if ( !hasClients() ) throw new Exception( "No clients available for this phase" );
    
    // Create the tear-down interface
    for ( EMClientEx client : getCopySetOfCurrentClients() )
    {
      AMQPMessageDispatch dispatch = new AMQPMessageDispatch();
      phaseMsgPump.addDispatch( dispatch );
      
      EMTearDown face = new EMTearDown( emChannel, dispatch,
                                        emProviderID, client.getID(), true );
      
      // Add client to the tear-down list
      clientsStillToTearDown.add( client.getID() );
      
      face.setProviderListener( this );
      client.setTearDownInterface( face );
    }
    
    phaseMsgPump.startPump();
    phaseActive = true;
    
    // Request clients do the same
    for ( EMClientEx client : getCopySetOfCurrentClients() )
      client.getDiscoveryInterface().createInterface( EMInterfaceType.eEMTearDown );
    
    phaseState = "Waiting for client to signal ready to tear down";
  }
  
  @Override
  public void controlledStop() throws Exception
  { throw new Exception( "Not yet supported for this phase"); }
  
  @Override
  public void hardStop()
  {
    phaseMsgPump.stopPump();
    phaseActive = false;
    if ( phaseListener != null ) phaseListener.onTearDownPhaseCompleted();
  }
  
  @Override
  public void timeOutClient( EMClientEx client ) throws Exception
  {
    // Safety first
    if ( client == null ) throw new Exception( "Could not time-out: client is null" );
    if ( !phaseActive )   throw new Exception( "Could not time-out: phase not active" );      
    
    // Check this client is registered with this phase first
    if ( isClientRegisteredInPhase(client) )
    {
      if ( client.isNotifiedOfTimeOut(EMPhaseTimeOut.eEMTOTearDownTimeOut) ) 
        throw new Exception( "Time-out already sent to client" );
      
      client.addTimeOutNotification( EMPhaseTimeOut.eEMTOTearDownTimeOut );
      client.getTearDownInterface().tearDownTimeOut();
    }
    else
      throw new Exception( "This client cannot be timed-out in Tear-down phase" );
  }
  
  @Override
  public void onClientUnexpectedlyRemoved( EMClientEx client )
  {
    if ( client != null )
    {
      UUID clientID = client.getID();
      
      clientsStillToTearDown.remove( clientID );
      removeClient( clientID );
    }
  }
  
  // IEMTearDown_ProviderListener ----------------------------------------------
  @Override
  public void onNotifyReadyToTearDown( UUID senderID )
  {
    if ( phaseActive )
    {
      EMClientEx client = getClient( senderID );
      
      if ( client != null )
        client.getTearDownInterface().tearDownMetricGenerators();
    }
  }
  
  @Override
  public void onNotifyTearDownResult( UUID senderID, Boolean success )
  {
    if ( phaseActive )
    {
      EMClientEx client = getClient( senderID );
      
      if ( client != null )
      {
        client.setTearDownResult( success );
        clientsStillToTearDown.remove( client.getID() );
        
        if ( phaseListener != null )
          phaseListener.onClientTearDownResult( client, success );
      }
      
      // Notify phase completion if all results are in
      if ( clientsStillToTearDown.isEmpty() ) hardStop();
    }
  }
}
