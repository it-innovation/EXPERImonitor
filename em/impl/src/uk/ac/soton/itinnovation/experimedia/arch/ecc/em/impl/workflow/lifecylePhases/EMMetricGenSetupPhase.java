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
  private EMMetricGenSetupPhaseListener phaseListener;
  
  
  public EMMetricGenSetupPhase( AMQPBasicChannel channel,
                                UUID providerID,
                                EMMetricGenSetupPhaseListener listener )
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
    if ( !hasClients()  ) throw new Exception( "No clients available for this phase" );
  
    // Create the set-up interface
    for ( EMClientEx client : getCopySetOfCurrentClients() )
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
    
    phaseMsgPump.startPump();
    phaseActive = true;
    
    // Request clients do the same (and wait to start set-up process)
    for ( EMClientEx client : getCopySetOfCurrentClients() )
      client.getDiscoveryInterface().createInterface( EMInterfaceType.eEMSetup );
    
    phaseState = "Wait for clients to signal ready to set-up";
  }
  
  @Override
  public void controlledStop() throws Exception
  { throw new Exception( "Not yet supported for this phase"); }
  
  @Override
  public void hardStop()
  {
    phaseActive = false;
    phaseMsgPump.stopPump();
    
    if ( phaseListener != null ) phaseListener.onSetupPhaseCompleted();
  }
  
  // IEMSetup_ProviderListener -------------------------------------------------
  @Override
  public void onNotifyReadyToSetup( UUID senderID )
  {
    if ( phaseActive )
    {
      EMClientEx client = getClient( senderID );
      
      if ( client != null )
      {
        if ( client.hasGeneratorToSetup() )
        {
          UUID genID = client.getNextGeneratorToSetup();
          client.getSetupInterface().setupMetricGenerator( genID );
        }
        else 
          if ( phaseListener != null ) 
            phaseListener.onMetricGenSetupResult( client, false );
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
      EMClientEx client = getClient( senderID );
      
      if ( client != null )
      {
        if ( genID != null && success ) client.addSuccessfulSetup( genID );
        
        if ( client.hasGeneratorToSetup() )
        {
          UUID nextGen = client.getNextGeneratorToSetup();
          client.getSetupInterface().setupMetricGenerator( nextGen );
        }
        else
        {
          if ( phaseListener != null )
            phaseListener.onMetricGenSetupResult( client, 
                                                  client.metricGeneratorsSetupOK() );
          
          // Check to see if we've finished trying to set up all clients
          boolean allAttemptsMade       = true;
          Iterator<EMClientEx> clientIt = getCopySetOfCurrentClients().iterator();

          while ( clientIt.hasNext() )
            if ( clientIt.next().hasGeneratorToSetup() )
            {
              allAttemptsMade = false;
              break;
            }

          if ( allAttemptsMade ) hardStop();
        }
      }
    }
  }
}
