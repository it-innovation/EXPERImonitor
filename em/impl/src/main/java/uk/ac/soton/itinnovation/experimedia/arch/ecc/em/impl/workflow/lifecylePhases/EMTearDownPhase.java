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
  private final Object controlledStopLock = new Object();
  private final Object acceleratorLock    = new Object();
  
  private EMTearDownPhaseListener phaseListener;
  private HashSet<UUID>           clientsStillToTearDown;
  
  
  public EMTearDownPhase( AMQPBasicChannel channel,
                          UUID providerID,
                          EMTearDownPhaseListener listener )
  {
    super( EMPhase.eEMTearDown, channel, providerID );
    
    phaseListener          = listener;
    clientsStillToTearDown = new HashSet<UUID>();
    phaseMsgPump.startPump();
    
    phaseState = "Ready to start tear-down process";
  }
  
  // AbstractEMLCPhase ---------------------------------------------------------
  @Override
  public void reset()
  {
    phaseActive = false;
    
    clearAllClients();
    
    clientsStillToTearDown.clear();
  }
  
  @Override
  public void start() throws Exception
  {
    if ( phaseActive ) throw new Exception( "Phase already active" );
    
    phaseActive     = true;
    
    // Create tear-down interfaces
    synchronized ( acceleratorLock )
    {
      Set<EMClientEx> currClients   = getCopySetOfCurrentClients();
      Iterator<EMClientEx> clientIt = currClients.iterator();
    
      // Create the set-up interface and prepare to tear-down clients
      while ( clientIt.hasNext() )
      {
        EMClientEx client = clientIt.next();
        client.setCurrentPhaseActivity( EMPhase.eEMTearDown );
        setupClientInterface( client );
        
        clientsStillToTearDown.add( client.getID() );
      }
      
      // Request clients do the same
      clientIt = currClients.iterator();
      while ( clientIt.hasNext() )
      {
        EMClientEx client = clientIt.next();
        client.getDiscoveryInterface().createInterface( EMInterfaceType.eEMTearDown );
      }
    }
          
    phaseState = "Waiting for client to signal ready to tear down";
    //... remainder of protocol implemented through events
  }
  
  @Override
  public void controlledStop()
  {
    if ( !clientsStillToTearDown.isEmpty() )
      phaseLogger.warn( "Stopping Tear-Down phase will some clients yet to finish tearing down" );
    
    phaseActive = false;
  }
  
  @Override
  public void hardStop()
  {
    phaseActive     = false;
    phaseMsgPump.stopPump();
  }
  
  @Override
  public void setupClientInterface( EMClientEx client )
  {
    if ( client.getTearDownInterface() == null )
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
  }
  
  @Override
  public void accelerateClient( EMClientEx client ) throws Exception
  {
    if ( !phaseActive ) throw new Exception( "Cannot accelerate client (tear-down) - phase is not active" );
    if ( client == null ) throw new Exception( "Cannot accelerate client (tear-down) - client is null" );
    
    synchronized ( acceleratorLock )
    {
      client.setCurrentPhaseActivity( EMPhase.eEMSetUpMetricGenerators );

      // Need to manually add/setup accelerated clients
      addClient( client );
      setupClientInterface( client );

      clientsStillToTearDown.add( client.getID() );

      // Tell client to initialise their tear-down phase
      client.getDiscoveryInterface().createInterface( EMInterfaceType.eEMTearDown );
    }
  }
  
  @Override
  public void timeOutClient( EMClientEx client ) throws Exception
  {
    // Safety first
    if ( client == null ) throw new Exception( "Could not time-out: client is null" );    
    
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
  public void onClientHasBeenDeregistered( EMClientEx client )
  {
    if ( client != null )
    {
      synchronized ( controlledStopLock )
      {
        UUID clientID = client.getID();
      
        clientsStillToTearDown.remove( clientID );

        removeClient( clientID );
      }
    }
  }
  
  // IEMTearDown_ProviderListener ----------------------------------------------
  @Override
  public void onNotifyReadyToTearDown( UUID senderID )
  {
    EMClientEx client = getClient( senderID );

    if ( client != null )
      client.getTearDownInterface().tearDownMetricGenerators();
  }
  
  @Override
  public void onNotifyTearDownResult( UUID senderID, Boolean success )
  {
    EMClientEx client = getClient( senderID );

    if ( client != null )
    {
      client.setTearDownResult( success );
      
      clientsStillToTearDown.remove( client.getID() );
      phaseListener.onClientTearDownResult( client, success );
      phaseListener.onTearDownPhaseCompleted( client );          
    }
  }
}
