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
//      Created Date :          21-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecylePhases;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMSetup_ProviderListener;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces.EMMetricGenSetup;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;

import java.util.*;





public class EMMetricGenSetupPhase extends AbstractEMLCPhase
                                   implements IEMSetup_ProviderListener
{
  private final Object controlledStopLock = new Object();
  private final Object acceleratorLock    = new Object();
  
  private EMMetricGenSetupPhaseListener phaseListener;
  private HashSet<UUID>                 clientsSettingUp;
  
  private volatile boolean setupStopping; // Atomic
  
  
  public EMMetricGenSetupPhase( AMQPBasicChannel channel,
                                UUID providerID,
                                EMMetricGenSetupPhaseListener listener )
  {
    super( EMPhase.eEMSetUpMetricGenerators, channel, providerID );
    
    phaseListener    = listener;
    clientsSettingUp = new HashSet<UUID>();
    phaseMsgPump.startPump();
    
    phaseState = "Ready to request client set-up";
  }
  
  // AbstractEMLCPhase ---------------------------------------------------------
  @Override
  public void reset()
  {
    synchronized ( controlledStopLock )
    { clientsSettingUp.clear(); }
    
    clearAllClients();
    
    phaseActive   = false;
    setupStopping = false;
  }
  
  @Override
  public void start() throws Exception
  {
    if ( phaseActive ) throw new Exception( "Phase already active" );
    if ( !hasClients()  ) throw new Exception( "No clients available for this phase" );
    
    phaseActive = true;
    
    // Initialise client set-up list
    synchronized ( acceleratorLock )
    {
      Set<EMClientEx> currClients   = getCopySetOfCurrentClients();
      Iterator<EMClientEx> clientIt = currClients.iterator();
    
      // Create the set-up interface
      while ( clientIt.hasNext() )
      {
        EMClientEx client = clientIt.next();
        client.setCurrentPhaseActivity( EMPhase.eEMSetUpMetricGenerators );
        setupClientInterface( client );
        
        clientsSettingUp.add( client.getID() );
      }
    
      // Request clients do the same (and wait to start set-up process)
      clientIt = currClients.iterator();
      while ( clientIt.hasNext() )
      {
        EMClientEx client = clientIt.next();
        client.getDiscoveryInterface().createInterface( EMInterfaceType.eEMSetup );
      }
    }
  
    phaseState = "Wait for clients to signal ready to set-up";
    //... remainder of protocol implemented through events
  }
  
  @Override
  public void controlledStop() throws Exception
  {
    if ( phaseActive && !setupStopping )
    {
      phaseActive   = false;
      setupStopping = true;
      
      synchronized ( controlledStopLock )
      {
        if ( clientsSettingUp.isEmpty() )
          phaseListener.onSetupPhaseCompleted();
      }
    }
  }
  
  @Override
  public void hardStop()
  {
    phaseActive   = false;
    setupStopping = false;
    
    phaseMsgPump.stopPump();
  }
  
  @Override
  public void accelerateClient( EMClientEx client ) throws Exception
  {
    if ( client == null ) throw new Exception( "Cannot accelerate client (setup) - client is null" );
    
    synchronized ( acceleratorLock )
    {
      client.setCurrentPhaseActivity( EMPhase.eEMSetUpMetricGenerators );
      
      // Need to manually add/setup accelerated clients
      addClient( client );
      setupClientInterface( client );
      
      // Add in client to set-up waiting list
      UUID id = client.getID();
      if ( !clientsSettingUp.contains(id) ) clientsSettingUp.add( id );
      
      // Tell client to initialise their set-up phase
      client.getDiscoveryInterface().createInterface( EMInterfaceType.eEMSetup );
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
      if ( client.isNotifiedOfTimeOut(EMPhaseTimeOut.eEMTOSetUpTimeOut) ) 
        throw new Exception( "Time-out already sent to client" );
      
      if ( !client.isSettingUpMetricGenerator() )
        throw new Exception( "Client is not currently setting up a metric generator" );
      
      client.addTimeOutNotification( EMPhaseTimeOut.eEMTOSetUpTimeOut );
      client.getSetupInterface().setupTimeOut( client.getCurrentMetricGenSetupID() );
    }
    else
      throw new Exception( "This client cannot be timed-out in Post-report phase" );
  }
  
  @Override
  public void onClientHasBeenDeregistered( EMClientEx client )
  {
    if ( client != null )
    {
      UUID clientID = client.getID();
      
      synchronized( controlledStopLock )
      {
        clientsSettingUp.remove( clientID );
        removeClient( clientID );
      }
    }
  }
  
  // IEMSetup_ProviderListener -------------------------------------------------
  @Override
  public void onNotifyReadyToSetup( UUID senderID )
  {
    if ( !setupStopping )
    {
      EMClientEx client = getClient( senderID );

      if ( client != null )
      {
        if ( client.hasGeneratorToSetup() )
        {
          UUID genID = client.iterateNextMGToSetup();
          client.getSetupInterface().setupMetricGenerator( genID );
        }
        else
          clientsSettingUp.remove( senderID ); // Don't ask again
      }
    }
  }
  
  @Override
  public void onNotifyMetricGeneratorSetupResult( UUID senderID,
                                                  UUID genID,
                                                  Boolean success )
  {
    EMClientEx client = getClient( senderID );

    if ( client != null )
    {
      if ( genID != null && success ) client.addSuccessfulSetup( genID );

      // Try setting up another MG (if it exists)
      UUID nextGen = client.iterateNextMGToSetup();

      if ( nextGen != null )
        client.getSetupInterface().setupMetricGenerator( nextGen );
      else
      {
        // Otherwise, finish up and report the result
        clientsSettingUp.remove( senderID );

        phaseListener.onMetricGenSetupResult( client, 
                                              client.metricGeneratorsSetupOK() );
      }
    }
    
    // Notify end of phase (if phase is active)
    if ( phaseActive && clientsSettingUp.isEmpty() )
    {
      phaseActive = false;
      phaseListener.onSetupPhaseCompleted();
    }
    else // Otherwise, accelerate client
      if ( client.isPhaseAccelerating() )
        phaseListener.onSetupPhaseCompleted( client );
  }
  
  // Private methods -----------------------------------------------------------
  private void setupClientInterface( EMClientEx client )
  {
    if ( client.getSetupInterface() == null )
    {
      AMQPMessageDispatch dispatch = new AMQPMessageDispatch();
      phaseMsgPump.addDispatch( dispatch );

      UUID clientID = client.getID();
      EMMetricGenSetup face = new EMMetricGenSetup( emChannel,
                                                    dispatch,
                                                    emProviderID,
                                                    clientID,
                                                    true );
      face.setProviderListener( this );
      client.setSetupInterface( face );
    }  
  }
}
