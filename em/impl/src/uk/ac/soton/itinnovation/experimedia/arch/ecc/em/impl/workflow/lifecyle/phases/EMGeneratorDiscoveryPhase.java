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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecyle.phases;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.IEMDiscovery;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces.EMDiscovery;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;

import java.util.*;





public class EMGeneratorDiscoveryPhase extends AbstractEMLCPhase
                                       implements IEMDiscovery_ProviderListener
{
  private EMGeneratorDiscoveryPhaseListener phaseListener;
  
  private HashSet<UUID> clientsExpectingGeneratorInfo;
  
  
  public EMGeneratorDiscoveryPhase( AMQPBasicChannel channel,
                                    UUID providerID,
                                    EMGeneratorDiscoveryPhaseListener listener )
  {
    super( EMPhase.eEMDiscoverMetricGenerators, channel, providerID );
    
    phaseListener = listener;
    
    clientsExpectingGeneratorInfo = new HashSet<UUID>();
    
    // Start pump early as clients connect in this phase
    phaseMsgPump.startPump();
    
    phaseState = "Waiting for clients";
  }
  
  // AbstractEMLCPhase ---------------------------------------------------------
  @Override
  public boolean addClient( EMClientEx client )
  {
    if ( super.addClient(client) )
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
      
      phaseState = "Waiting to start phase";
      
      return true;
    }
    
    return false;
  }
  
  @Override
  public void start() throws Exception
  {
    if ( phaseActive ) throw new Exception( "Phase already active" );
    if ( !hasClients() ) throw new Exception( "No clients available for this phase" );
  
    phaseActive = true;
    
    // Confirm registration with clients...
    for ( EMClientEx client : getCopySetOfCurrentClients() )
    {
      IEMDiscovery monFace = client.getDiscoveryInterface();
      monFace.registrationConfirmed( true );
      client.setIsConnected( true );

      // Initially assume all clients will offer metric generators
      clientsExpectingGeneratorInfo.add( client.getID() );
    }
    
    phaseState = "Getting client information";
    //... remainder of protocol implemented through events
  }
  
  @Override
  public void controlledStop() throws Exception
  { throw new Exception( "Not yet supported for this phase"); }
  
  @Override
  public void hardStop()
  {
    // Don't stop this message pump as continuously listen for disconnecting clients here
    
    phaseActive = false;
  }
  
  // IEMMonitor_ProviderListener -----------------------------------------------
  @Override
  public void onReadyToInitialise( UUID senderID )
  {
    if ( phaseActive )
    {
      EMClientEx client = getClient( senderID );

      if ( client != null )
        client.getDiscoveryInterface().requestActivityPhases();
    }
  }
  
  @Override
  public void onSendActivityPhases( UUID senderID, 
                                    EnumSet<EMPhase> supportedPhases )
  {
    if ( phaseActive )
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
          clientsExpectingGeneratorInfo.remove( senderID );
      }
    }
  }
  
  @Override
  public void onSendDiscoveryResult( UUID senderID,
                                     Boolean discoveredGenerators )
  {
    if ( phaseActive )
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
          clientsExpectingGeneratorInfo.remove( senderID );
          // Otherwise don't expect any metric generator info
      }
    }
  }
  
  @Override
  public void onSendMetricGeneratorInfo( UUID senderID,
                                         Set<MetricGenerator> generators )
  {
    if ( phaseActive )
    {
        EMClientEx client = getClient( senderID );

        if ( client != null )
        {
          client.setMetricGenerators( generators );
          phaseListener.onClientMetricGeneratorsFound( client );
          
          // Remove from the list of expected generators
          clientsExpectingGeneratorInfo.remove( senderID );
        }
        
        // If we've got all the metric generator info we need, finish this phase
        if ( clientsExpectingGeneratorInfo.isEmpty() )
        {
          hardStop();
          phaseListener.onDiscoveryPhaseCompleted();
        }
    }
  }
  
  @Override
  public void onClientDisconnecting( UUID senderID )
  {
    EMClientEx client = getClient( senderID );

    if ( client != null )
    {
      clientsExpectingGeneratorInfo.remove( senderID );

      client.destroyAllInterfaces();
      client.setIsConnected( false );

      phaseListener.onClientIsDisconnected( client );
    }
  }
}
