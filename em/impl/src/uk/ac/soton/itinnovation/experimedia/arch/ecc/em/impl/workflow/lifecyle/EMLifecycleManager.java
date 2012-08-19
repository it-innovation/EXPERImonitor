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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.EMConnectionManagerListener;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecyle.phases.*;

import java.util.*;






public class EMLifecycleManager implements EMConnectionManagerListener,
                                           GeneratorDiscoveryPhaseListener
{
  private AMQPBasicChannel emChannel;
  private UUID             emProviderID;
  
  private EnumMap<EMPhase, AbstractEMLCPhase> lifecyclePhases;
  private EMPhase currentPhase = EMPhase.eEMUnknownPhase;
  
  private EMLifecycleManagerListener lifecycleListener;
  
  
  public EMLifecycleManager() 
  {
    lifecyclePhases = new EnumMap<EMPhase, AbstractEMLCPhase>( EMPhase.class );
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
    lifecyclePhases.put( EMPhase.eEMDiscoverMetricGenerators, gdp );
  }
  
  public boolean isLifecycleStarted()
  { return (currentPhase.getIndex() > 0); }
  
  public void iterateLifecycle()
  {
    if ( currentPhase != EMPhase.eEMProtocolComplete && !lifecyclePhases.isEmpty() )
    {
      currentPhase = currentPhase.nextPhase();
      
      AbstractEMLCPhase lcPhase = lifecyclePhases.get( currentPhase );
      if ( lcPhase != null )
        try { lcPhase.start(); }
        catch( Exception e ) 
        { 
          System.out.println("Could not start lifecycle phase: " + lcPhase.getPhaseType() );
          System.out.println("Life-cycle exception: " + e.getMessage() );
        }
    }
  }
  
  public void endLifecycle()
  {
    currentPhase = EMPhase.eEMUnknownPhase;
    
    //TODO: Tidy up
  }
  
  // EMConnectionManagerListener -----------------------------------------------
  @Override
  public void onClientRegistered( EMClientEx client )
  {
    // Attach client to GeneratorDiscoveryPhase
    if ( client          != null && 
         lifecyclePhases != null &&
         currentPhase    == EMPhase.eEMUnknownPhase ) // Only register client if we've not started a life-cycle
    {
      AbstractEMLCPhase discovery = 
          lifecyclePhases.get( EMPhase.eEMDiscoverMetricGenerators );
      
      if ( discovery != null ) discovery.addClient( client );
    }
  }
  
  // GeneratorDiscoveryPhaseListener -------------------------------------------
  @Override
  public void onClientPhaseSupportReceived( EMClientEx client )
  {
    EnumSet<EMPhase> clientPhases = client.getCopyOfSupportedPhases();
    Iterator<EMPhase> phaseIt = clientPhases.iterator();
    
    while ( phaseIt.hasNext() )
    {
      AbstractEMLCPhase phase = lifecyclePhases.get( phaseIt.next() );
      if ( phase != null )
        phase.addClient( client );
    }
  }
  
  @Override
  public void onClientMetricGeneratorsFound( UUID clientID )
  {
    lifecycleListener.onFoundClientWithMetricGenerators( clientID );
  }
  
  @Override
  public void onDiscoveryPhaseCompleted()
  {
    
  }
}
