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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.IEMMonitor;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMMonitor_ProviderListener;


import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces.EMMonitor;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.dataModel.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;

import java.util.*;






public class EMGeneratorDiscoveryPhase extends AbstractEMLCPhase
                                       implements IEMMonitor_ProviderListener
{
  private GeneratorDiscoveryPhaseListener phaseListener;
  
  private HashSet<EMClientEx> clientsDiscovering;
  
  
  public EMGeneratorDiscoveryPhase( AMQPBasicChannel channel,
                                       UUID providerID,
                                       GeneratorDiscoveryPhaseListener listener )
  {
    super( EMPhase.eEMDiscoverMetricGenerators, channel, providerID );
    
    phaseListener = listener;
    
    clientsDiscovering = new HashSet<EMClientEx>();
    
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
      
      EMMonitor monitorFace = new EMMonitor( emChannel,
                                             dispatch,
                                             emProviderID,
                                             client.getID(),
                                             true );
      
      client.setEMMonitorInterface( monitorFace );
      
      phaseState = "Waiting to start phase";
    }
    
    return false;
  }
  
  @Override
  public void start() throws Exception
  {
    if ( phaseClients.isEmpty() ) throw new Exception( "No clients available for this phase" );
  
    // Confirm registration with clients...
    for ( EMClientEx client : phaseClients.values() )
    {
      IEMMonitor monFace = client.getEMMonitorInterface();
      monFace.registrationConfirmed( true );
    }    
    //... remainder of protocol implemented through events
  }
  
  @Override
  public void stop() throws Exception
  {
    
  }
  
  // IEMMonitor_ProviderListener -----------------------------------------------
  @Override
  public void onReadyToInitialise( UUID senderID )
  {
    EMClientEx client = phaseClients.get( senderID );
    if ( client != null )
    {
      // Assumes client has already created IEMMonitor interface
      
      AMQPMessageDispatch dispatch = new AMQPMessageDispatch();
      
      EMMonitor monitorFace = new EMMonitor( emChannel,
                                             dispatch,
                                             emProviderID,
                                             senderID,
                                             true );
              
      phaseMsgPump.addDispatch( dispatch );
      
      client.setEMMonitorInterface( monitorFace );
      
      monitorFace.requestActivityPhases();
    }
  }
  
  @Override
  public void onSendActivityPhases( UUID senderID, 
                                    EnumSet<EMPhase> supportedPhases )
  {
    EMClientEx client = phaseClients.get( senderID );
    
    if ( client != null && supportedPhases != null )
    {
      // Tell Lifecycle manager about supported phases by client
      client.setSupportedPhases( supportedPhases );
      phaseListener.onClientPhaseSupportReceived( client );
      
      // Ask clients to start discovering metric generators
      clientsDiscovering.add( client );
      
      client.getEMMonitorInterface().discoverMetricGenerators(); 
    }
  }
  
  @Override
  public void onSendDiscoveryResult( UUID senderID
                                     /* Data model under development*/ )
  {
    
  }
  
  @Override
  public void onClientDisconnecting( UUID senderID )
  {
    
  }
}
