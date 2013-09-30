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
//      Created Date :          14-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecylePhases;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.IEMDiscovery;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces.EMDiscovery;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;

import java.util.*;




public class EMGeneratorDiscoveryPhase extends AbstractEMLCPhase
                                       implements IEMDiscovery_ProviderListener
{
  private final Object acceleratorLock = new Object();
  
  private Experiment                        currentExperiment;
  private EMGeneratorDiscoveryPhaseListener phaseListener;
  
  private HashSet<UUID> clientsExpectingGeneratorInfo;
  
  
  public EMGeneratorDiscoveryPhase( AMQPBasicChannel channel,
                                    UUID providerID,
                                    EMGeneratorDiscoveryPhaseListener listener )
  {
    super( EMPhase.eEMDiscoverMetricGenerators, channel, providerID );
    
    phaseListener                 = listener;
    clientsExpectingGeneratorInfo = new HashSet<UUID>();
    phaseMsgPump.startPump();
    
    phaseState = "Waiting for clients";
  }
  
  public void setExperimentInfo( Experiment expInfo ) throws Exception
  {
    // Safety first
    if ( expInfo == null ) throw new Exception( "Experiment info is NULL" );
    if ( phaseActive ) throw new Exception( "Already started phase - cannot change experiment info now" );
  
    currentExperiment = expInfo;
  }
  
  // AbstractEMLCPhase ---------------------------------------------------------
  @Override
  public void reset()
  {
    phaseActive = false;
    clearAllClients();
    
    currentExperiment = null;
    clientsExpectingGeneratorInfo.clear();
  }
  
  @Override
  public void start() throws Exception
  {
    if ( phaseActive )               throw new Exception( "Phase already active" );
    if ( !hasClients() )             throw new Exception( "No clients available for this phase" );
    if ( currentExperiment == null ) throw new Exception( "Experiment info is NULL" );
  
    phaseActive = true;
    
    // Set up metric generator description list
    synchronized ( acceleratorLock )
    {
      Set<EMClientEx> currClients   = getCopySetOfCurrentClients();
      Iterator<EMClientEx> clientIt = currClients.iterator();
    
      while ( clientIt.hasNext() )
      {
        EMClientEx client = clientIt.next();
        client.setCurrentPhaseActivity( EMPhase.eEMDiscoverMetricGenerators );
        
        clientsExpectingGeneratorInfo.add( client.getID() ); 
      }
      
      // Confirm registration with clients...
      clientIt = currClients.iterator();
      while ( clientIt.hasNext() )
      {
        EMClientEx client = clientIt.next();
        IEMDiscovery monFace = client.getDiscoveryInterface();

        monFace.registrationConfirmed( true,
                                       currentExperiment.getUUID(),
                                       currentExperiment.getExperimentID(),
                                       currentExperiment.getName(),
                                       currentExperiment.getDescription(),
                                       currentExperiment.getStartTime() );
      }
    }
    
    phaseState = "Getting client information";
    //... remainder of protocol implemented through events
  }
  
  @Override
  public void controlledStop() throws Exception
  { 
    phaseListener.onDiscoveryPhaseCompleted();
  }
  
  @Override
  public void hardStop()
  {
    phaseActive = false;
    phaseMsgPump.stopPump();
  }
  
  @Override
  public void setupClientInterface( EMClientEx client )
  {
    if ( client.getDiscoveryInterface() == null )
    {
      // Create a new IEMMonitor interface for the client
      AMQPMessageDispatch dispatch = new AMQPMessageDispatch();
      phaseMsgPump.addDispatch( dispatch );

      EMDiscovery discoverFace = new EMDiscovery( emChannel,
                                                  dispatch,
                                                  emProviderID,
                                                  client.getID(),
                                                  true );
      discoverFace.setProviderListener( this );
      client.setDiscoveryInterface( discoverFace );
    }
  }
  
  @Override
  public void accelerateClient( EMClientEx client ) throws Exception
  {
    if ( client == null ) throw new Exception( "Cannot accelerate client (discovery) - client is null" );
    
    // Initially assume all clients will offer metric generators
    synchronized ( acceleratorLock )
    {
      client.setCurrentPhaseActivity( EMPhase.eEMDiscoverMetricGenerators );
      
      // Need to manually add/setup accelerated clients
      addClient( client ); // setup included in this overridden method
      
      // Add in client to generator info waiting list
      UUID id = client.getID();
      if ( !clientsExpectingGeneratorInfo.contains(id) )
        clientsExpectingGeneratorInfo.add( id );
    
      // Get discovery interface and send registration confirmation
      IEMDiscovery monFace = client.getDiscoveryInterface();
    
      monFace.registrationConfirmed( true,
                                     currentExperiment.getUUID(),
                                     currentExperiment.getExperimentID(),
                                     currentExperiment.getName(),
                                     currentExperiment.getDescription(),
                                     currentExperiment.getStartTime() );
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
      if ( client.isNotifiedOfTimeOut(EMPhaseTimeOut.eEMTODiscoveryTimeOut) ) 
        throw new Exception( "Time-out already sent to client" );
      
      client.addTimeOutNotification( EMPhaseTimeOut.eEMTODiscoveryTimeOut );
      client.getDiscoveryInterface().discoveryTimeOut();
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
      
      clientsExpectingGeneratorInfo.remove( clientID );
      removeClient( clientID );
    }
  }
  
  // IEMMonitor_ProviderListener -----------------------------------------------
  @Override
  public void onReadyToInitialise( UUID senderID )
  {
    EMClientEx client = getClient( senderID );

    if ( client != null )
      client.getDiscoveryInterface().requestActivityPhases();
  }
  
  @Override
  public void onSendActivityPhases( UUID senderID, 
                                    EnumSet<EMPhase> supportedPhases )
  {
    EMClientEx client = getClient( senderID );

    if ( client != null && supportedPhases != null )
    {
      // Tell Lifecycle manager about supported phases by client
      client.setSupportedPhases( supportedPhases );
      phaseListener.onClientPhaseSupportReceived( client );

      // If we have relevant phases, go get metric generator info
      if ( supportedPhases.contains( EMPhase.eEMLiveMonitoring )        ||
           supportedPhases.contains( EMPhase.eEMPostMonitoringReport) )
        client.getDiscoveryInterface().discoverMetricGenerators();
      else
      {
        synchronized ( acceleratorLock )
        { clientsExpectingGeneratorInfo.remove( client.getID() ); }
      }
    }
  }
  
  @Override
  public void onSendDiscoveryResult( UUID senderID,
                                     Boolean discoveredGenerators )
  {
    EMClientEx client = getClient( senderID );

    if ( client != null )
    {
      client.setGeneratorDiscoveryResult( discoveredGenerators );
      phaseListener.onClientDiscoveryResult( client );

      // Find out about metric generators if some have been found
      if ( discoveredGenerators )
        client.getDiscoveryInterface().requestMetricGeneratorInfo();
      else
      {
        // Otherwise don't expect any metric generator info
        synchronized ( acceleratorLock )
        { clientsExpectingGeneratorInfo.remove( client.getID() ); }
      }
    }
  }
  
  @Override
  public void onSendMetricGeneratorInfo( UUID senderID,
                                         Set<MetricGenerator> generators )
  {
    EMClientEx client = getClient( senderID );

    if ( client != null )
    {
      client.appendMetricGenerators( generators );
      
      // Always notify, even if the set is empty
      phaseListener.onClientMetricGeneratorsFound( client, generators );

      // Remove from the list of expected generators
      synchronized ( acceleratorLock )
      { clientsExpectingGeneratorInfo.remove( client.getID() ); }
    }
    
    // If the phase is currently 'active' then determine it has completed
    if ( phaseActive && clientsExpectingGeneratorInfo.isEmpty() )
    {
      phaseActive = false;
      phaseListener.onDiscoveryPhaseCompleted();
    }
    else // Otherwise, accelerate the client
      if ( client.isPhaseAccelerating() ) 
        phaseListener.onDiscoveryPhaseCompleted( client );
    
  }
  
  @Override
  public void onEnableEntityMetricCollection( UUID senderID,
                                              UUID entityID, 
                                              boolean enabled )
  {
    EMClientEx client = getClient( senderID );

    if ( client != null )
    {
      // Try enabling/disabling entity (& notify if successful (ie., entity exists))
      if ( client.setEntityEnabled( entityID, enabled ) )
        phaseListener.onClientEnabledMetricCollection( client, entityID, enabled );
    }
  }
  
  @Override
  public void onClientDisconnecting( UUID senderID )
  {
    // Always respond to this event, irrespective of whether the phase is active
    EMClientEx client = getClient( senderID );

    if ( client != null )
    {
      synchronized ( acceleratorLock )
      { clientsExpectingGeneratorInfo.remove( client.getID() ); }
      
      phaseListener.onClientIsDisconnected( client );
    }
  }
  
  // Protected methods ---------------------------------------------------------
  @Override
  protected boolean addClient( EMClientEx client )
  {
    boolean result = false;
    
    if ( super.addClient(client) )
    {
      setupClientInterface( client );      
      result = true;
    }
    
    return result;
  }
}
