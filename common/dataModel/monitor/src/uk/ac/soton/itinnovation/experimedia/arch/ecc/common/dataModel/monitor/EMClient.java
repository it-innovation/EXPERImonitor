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
//      Created Date :          13-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;

import java.util.*;




public class EMClient
{
  protected UUID    clientID;
  protected String  clientName;
  protected boolean clientConnected = false;
  
  // Discovery phase states
  protected EnumSet<EMPhase>         supportedPhases;
  protected boolean                  discoveredGenerators = false;
  protected HashSet<MetricGenerator> metricGenerators;
  
  
  public EMClient( UUID id, String name )
  {
    clientID = id;
    clientName = name;
    
    supportedPhases  = EnumSet.noneOf( EMPhase.class );
    metricGenerators = new HashSet<MetricGenerator>();
  }
  
  public UUID getID()
  { return clientID; }
  
  public String getName()
  { return clientName; }
  
  public boolean isConnected()
  { return clientConnected; }
  
  public EnumSet<EMPhase> getCopyOfSupportedPhases()
  {
    EnumSet<EMPhase> phaseCopy = EnumSet.noneOf( EMPhase.class );
    phaseCopy.addAll( supportedPhases );
    
    return phaseCopy;
  }
  
  public boolean getGeneratorDiscoveryResult()
  { return discoveredGenerators; }
  
  public Set<MetricGenerator> getCopyOfMetricGenerators()
  {
    HashSet<MetricGenerator> mgCopies = new HashSet<MetricGenerator>();
    
    Iterator<MetricGenerator> copyIt = metricGenerators.iterator();
    while ( copyIt.hasNext() )
    {
      MetricGenerator clone = new MetricGenerator( copyIt.next() );
      mgCopies.add( clone );
    }
    
    return mgCopies;
  }
}
