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
  private IEMDiscovery monitorFace;
  
 
  public EMClientEx( UUID id, String name )
  {
    super( id, name );
  }
  
  public void destroyAllInterfaces()
  {
    monitorFace = null;
  }
  
  public IEMDiscovery getEMMonitorInterface()
  { return monitorFace; }
  
  public void setEMMonitorInterface( IEMDiscovery face )
  { monitorFace = face; }
  
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
  }
}
