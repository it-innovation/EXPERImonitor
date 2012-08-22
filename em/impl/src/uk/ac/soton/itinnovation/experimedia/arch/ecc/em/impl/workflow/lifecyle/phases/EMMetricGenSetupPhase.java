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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecyle.phases;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMSetup_ProviderListener;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces.EMMetricGenSetup;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;

import java.util.*;





public class EMMetricGenSetupPhase extends AbstractEMLCPhase
                                   implements IEMSetup_ProviderListener
{
  private EMNMetricGenSetupPhaseListener phaseListener;
  
  
  public EMMetricGenSetupPhase( AMQPBasicChannel channel,
                                UUID providerID,
                                EMNMetricGenSetupPhaseListener listener )
  {
    super( EMPhase.eEMSetUpMetricGenerators, channel, providerID );
    
    phaseListener = listener;
    
    phaseState = "Ready to request client set-up";
  }
  
  // AbstractEMLCPhase ---------------------------------------------------------
  @Override
  public void start() throws Exception
  {
    if ( phaseActive ) throw new Exception( "Phase already active" );
    if ( phaseClients.isEmpty() ) throw new Exception( "No clients available for this phase" );
  
    phaseActive = true;
    
    // Create the set-up interface
    for ( EMClientEx client : phaseClients.values() )
    {
      AMQPMessageDispatch dispatch = new AMQPMessageDispatch();
      phaseMsgPump.addDispatch( dispatch );
    
      EMMetricGenSetup face = new EMMetricGenSetup( emChannel,
                                                    dispatch,
                                                    emProviderID,
                                                    client.getID(),
                                                    true );
      
      face.setProviderListener( this );
      client.setSetupInterface( face );             
    }
    
    // Request clients do the same (and wait to start set-up process)
    for ( EMClientEx client : phaseClients.values() )
      client.getDiscoveryInterface().createInterface( EMInterfaceType.eEMSetup );
    
    phaseState = "Wait for clients to signal ready to set-up";
  }
  
  @Override
  public void stop() throws Exception
  {
    phaseActive = false;
  }
  
  
  // IEMSetup_ProviderListener -------------------------------------------------
  @Override
  public void onNotifyReadyToSetup( UUID senderID )
  {
    if ( phaseActive )
    {
      EMClientEx client = phaseClients.get( senderID );
      
      if ( client != null )
      {
        System.out.println("Client " + client.getName() + " ready to setup" );
      }
    }
  }
  
  @Override
  public void onNotifyMetricGeneratorSetupResult( UUID senderID,
                                                  UUID genID,
                                                  Boolean success )
  {
    if ( phaseActive )
    {
      EMClientEx client = phaseClients.get( senderID );
      
      if ( client != null )
      {
        System.out.println("Client " + client.getName() + " has set up MG " + genID.toString() + (success ? "OK" : "FAILED") );
      }
    }
  }
}
