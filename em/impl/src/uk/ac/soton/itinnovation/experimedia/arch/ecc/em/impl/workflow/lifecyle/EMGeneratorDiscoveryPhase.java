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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecyle;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.IEMMonitor;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMMonitor_ProviderListener;


import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces.EMMonitor;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.dataModel.EMClient;

import java.util.*;





class EMGeneratorDiscoveryPhase extends AbstractEMLCPhase
                                implements IEMMonitor_ProviderListener
{
  private GeneratorDiscoveryPhaseListener phaseListener;
  
  protected EMGeneratorDiscoveryPhase( AMQPBasicChannel channel,
                                       UUID providerID,
                                       GeneratorDiscoveryPhaseListener listener )
  {
    super( "Generator discovery phase", channel, providerID );
    
    phaseListener = listener;
    
    phaseState = "Waiting for clients";
  }
  
  // AbstractEMLCPhase ---------------------------------------------------------
  @Override
  protected boolean addClient( EMClient client )
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
  protected void start() throws Exception
  {
    if ( phaseClients.isEmpty() ) throw new Exception( "No clients available for this phase" );
  
    // Confirm registration with clients...
    for ( EMClient client : phaseClients.values() )
    {
      IEMMonitor monFace = client.getEMMonitorInterface();
      monFace.registrationConfirmed( true );
    }
    
    // Notify listener
    if ( phaseListener != null ) 
      phaseListener.onSentRegisterationConfirmation( phaseClients.keySet() );
    
    //... remainder of protocol implemented through events
  }
  
  @Override
  protected void stop() throws Exception
  {
    
  }
  
  // IEMMonitor_ProviderListener -----------------------------------------------
  @Override
  public void onReadyToInitialise( UUID senderID )
  {
    
  }
  
  @Override
  public void onSendActivityPhases( UUID senderID, 
                                    List<IEMMonitor.EMSupportedPhase> supportedPhases )
  {
    
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
