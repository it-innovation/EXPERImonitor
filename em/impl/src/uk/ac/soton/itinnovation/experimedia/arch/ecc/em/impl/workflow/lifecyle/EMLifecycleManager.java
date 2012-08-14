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
//      Created By :            sgc
//      Created Date :          13-Aug-2012
//      Created for Project :   experimedia-arch-ecc-em-impl
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecyle;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.EMConnectionManagerListener;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.dataModel.EMClient;

import java.util.*;





public class EMLifecycleManager implements EMConnectionManagerListener,
                                           GeneratorDiscoveryPhaseListener
{
  private AMQPBasicChannel emChannel;
  private UUID             emProviderID;
  
  private HashMap<Integer, AbstractEMLCPhase> lifecyclePhases;
  private int lifecycleIndex     = 0;
  private int lifecycleLastIndex = 1;
  
  private EMLifecycleManagerListener lifecycleListener;
  
  
  public EMLifecycleManager() 
  {
    lifecyclePhases = new HashMap<Integer, AbstractEMLCPhase>();
  }
  
  public void initialise( AMQPBasicChannel channel, 
                          UUID providerID,
                          EMLifecycleManagerListener listener )
  {
    emChannel         = channel;
    emProviderID      = providerID;
    lifecycleListener = listener;

    // Create life-cycle phases
    EMGeneratorDiscoveryPhase gdp = new EMGeneratorDiscoveryPhase( emChannel,
                                                                   emProviderID,
                                                                   this );
    lifecyclePhases.put( 1, gdp );
  }
  
  public void iterateLifecycle()
  {
    if ( lifecycleIndex < lifecycleLastIndex && !lifecyclePhases.isEmpty() )
    {
      lifecycleIndex++;
      
      AbstractEMLCPhase lcPhase = lifecyclePhases.get( lifecycleIndex );
      if ( lcPhase != null )
        try { lcPhase.start(); }
        catch( Exception e ) 
        { 
          System.out.println("Could not start lifecycle phase: " + lcPhase.getName() );
          System.out.println("Life-cycle exception: " + e.getMessage() );
        }
    }
  }
  
  // EMConnectionManagerListener -----------------------------------------------
  @Override
  public void onClientRegistered( EMClient client )
  {
    // Attach client to GeneratorDiscoveryPhase
    if ( client          != null && 
         lifecyclePhases != null &&
         lifecycleIndex  == 0 ) // Only register client if we've not started a life-cycle
    {
      AbstractEMLCPhase discovery = lifecyclePhases.get( 1 );
      if ( discovery != null ) discovery.addClient( client );
    }
  }
  
  // GeneratorDiscoveryPhaseListener -------------------------------------------
  @Override
  public void onSentRegisterationConfirmation( Set<UUID> clientIDs )
  {
    
  }
  
  @Override
  public void onDiscoveryPhaseCompleted()
  {
    
  }
}
