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
  
  // Setup phase states
  protected HashSet<UUID> generatorsSetupOK;
  
  // Post-report phase states
  protected EMPostReportSummary postReportSummary;
  
  // Tear-down phase
  protected boolean tearDownSuccessful = false;
  
  /**
   * Constructor of the client representing a user of the EM. ID must be random.
   * 
   * @param id    - Random UUID of the client
   * @param name  - Human recognisable name of the client
   */
  public EMClient( UUID id, String name )
  {
    clientID = id;
    clientName = name;
    
    supportedPhases   = EnumSet.noneOf( EMPhase.class );
    metricGenerators  = new HashSet<MetricGenerator>();
    generatorsSetupOK = new HashSet<UUID>();
  }
  
  @Override
  public String toString()
  { return clientName; }
  
  /**
   * Gets the ID of the client.
   * 
   * @return - ID of the client.
   */
  public UUID getID()
  { return clientID; }
  
  /**
   * Gets the name of the client.
   * 
   * @return - Name of the client.
   */
  public String getName()
  { return clientName; }
  
  /**
   * Specifies whether the client is connected to the EM.
   * 
   * @return - True if connected.
   */
  public boolean isConnected()
  { return clientConnected; }
  
  /**
   * Returns the client's support for the phases the EM executes.
   * 
   * @return - A set of supported phases.
   */
  public EnumSet<EMPhase> getCopyOfSupportedPhases()
  {
    EnumSet<EMPhase> phaseCopy = EnumSet.noneOf( EMPhase.class );
    phaseCopy.addAll( supportedPhases );
    
    return phaseCopy;
  }
  
  /**
   * Returns the result of the discovery process this client reported to the EM during
   * the discovery phase.
   * 
   * @return 
   */
  public boolean getGeneratorDiscoveryResult()
  { return discoveredGenerators; }
  
  /**
   * Returns the MetricGenerators the client reported to the EM during the discovery phase.
   * 
   * @return 
   */
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
  
  /**
   * Returns the set-up result the client reported (if supported) during the the
   * set-up phase.
   * 
   * @return 
   */
  public boolean metricGeneratorsSetupOK()
  {
    if ( metricGenerators.isEmpty() )  return false;
    if ( generatorsSetupOK.isEmpty() ) return false;
    
    Iterator<MetricGenerator> genIt = metricGenerators.iterator();
    while ( genIt.hasNext() )
      if ( !generatorsSetupOK.contains( genIt.next().getUUID() ) ) return false;
    
    return true;
  }
  
  /**
   * Returns the post report summary the client reported (if supported) during the
   * post-reporting phase.
   * 
   * @return 
   */
  public EMPostReportSummary getPostReportSummary()
  { return postReportSummary; }
  
  /**
   * Returns the result of the tear-down process the client executed (if it supports it)
   * during the tear-down phase.
   * 
   * @return 
   */
  public boolean getTearDownResult()
  { return tearDownSuccessful; }
}
