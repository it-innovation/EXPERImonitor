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
//      Created Date :          16-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;

import java.util.*;




public class EMClientEx extends EMClient
{ 
  private IEMDiscovery      discoveryFace;
  private IEMMetricGenSetup setupFace;
  private IEMLiveMonitor    liveFace;
  private IEMPostReport     postFace;
  private IEMTearDown       tearDownFace;
  
  private ArrayList<UUID> generatorsToSetup;
 
  public EMClientEx( UUID id, String name )
  {
    super( id, name );
    
    generatorsToSetup = new ArrayList<UUID>();
  }
  
  public void destroyAllInterfaces()
  {
    discoveryFace = null;
    setupFace     = null;
    liveFace      = null;
    postFace      = null;
    tearDownFace  = null;
  }
  
  public IEMDiscovery getDiscoveryInterface()
  { return discoveryFace; }
  
  public void setDiscoveryInterface( IEMDiscovery face )
  { discoveryFace = face; }
  
  public IEMMetricGenSetup getSetupInterface()
  { return setupFace; }
  
  public void setSetupInterface( IEMMetricGenSetup face )
  { setupFace = face; }
  
  public void setLiveMonitorInterface( IEMLiveMonitor face )
  { liveFace = face; }
  
  public IEMLiveMonitor getLiveMonitorInterface()
  { return liveFace; }
  
  public void setPostReportInterface( IEMPostReport face )
  { postFace = face; }
  
  public IEMPostReport getPostReportInterface()
  { return postFace; }
  
  public IEMTearDown getTearDownInterface()
  { return tearDownFace; }
  
  public void setTearDownInterface( IEMTearDown face )
  { tearDownFace = face; }
  
  // Discovery phase state -----------------------------------------------------
  public void setIsConnected( boolean connected )
  { clientConnected = connected; }
  
  public void setSupportedPhases( EnumSet<EMPhase> phases )
  { supportedPhases = phases; }
  
  public void setGeneratorDiscoveryResult( boolean discovered )
  { discoveredGenerators = discovered; }
  
  public void setMetricGenerators( Set<MetricGenerator> generators )
  {
    if ( generators != null ) metricGenerators = 
            (HashSet<MetricGenerator>) generators;
    
    generatorsToSetup.clear();
    
    // Copy UUIDs of generators into set-up set
    Iterator<MetricGenerator> genIt = metricGenerators.iterator();
    while ( genIt.hasNext() )
      generatorsToSetup.add( genIt.next().getUUID() );
  }
  
  // Setup phase state ---------------------------------------------------------
  public boolean hasGeneratorToSetup()
  { return ( !generatorsToSetup.isEmpty() ); }
  
  public UUID getNextGeneratorToSetup()
  { 
    if ( !generatorsToSetup.isEmpty() )
      return generatorsToSetup.remove( 0 );
    
    return null;
  }
  
  public void addSuccessfulSetup( UUID genID )
  { if ( genID != null ) generatorsSetupOK.add( genID ); }
  
  // Post-report phase state ---------------------------------------------------
  public void setPostReportSummary( EMPostReportSummary report )
  { postReportSummary = report; }
  
  // Tear-down phase state -----------------------------------------------------
  public void setTearDownResult( boolean success )
  { tearDownSuccessful = success; }
}
