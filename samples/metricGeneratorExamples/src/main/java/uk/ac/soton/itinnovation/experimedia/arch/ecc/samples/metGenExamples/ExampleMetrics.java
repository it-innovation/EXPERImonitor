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
//      Created Date :          26-Feb-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.metGenExamples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import java.util.*;





public class ExampleMetrics
{
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private Entity                   applicationEntity;
  private Entity                   userEntity;
  private HashSet<MetricGenerator> metricGenerators;
  
  
  public ExampleMetrics()
  {}
  
  public void runDemo()
  {
    createSystemEntity();
    
    createUserEntity();
    
    createMetricStructures();
    
    createQoSMeasurementSets();
    
    createQoEMeasurementSets();
    
    MetricGenerator mg = MetricHelper.getMetricGeneratorByName( "Application metric generator",
                                                                metricGenerators );
    logger.info( MetricHelper.describeGenerator(mg) );
    
    mg = MetricHelper.getMetricGeneratorByName( "User metric generator",
                                                 metricGenerators );
    
    logger.info( MetricHelper.describeGenerator(mg) );
    
    demoPopulateQoSData();
    
    demoPopulateQoEData();
  }
  
  // Private methods -----------------------------------------------------------
  private void createSystemEntity()
  {
    applicationEntity = new Entity();
    applicationEntity.setName( "Interactive application" );
    applicationEntity.setDescription( "This entity represents an interactive application a user could use" );
    
    MetricHelper.createAttribute( "Application execution time",
                                  "This attribute represents the total time the application has been running", 
                                  applicationEntity );
    
    MetricHelper.createAttribute( "Application device temperature",
                                  "This attribute represents the temperature of the device running the application", 
                                  applicationEntity ); 
  }
  
  private void createUserEntity()
  {
    userEntity = new Entity();
    userEntity.setName( "User" );
    userEntity.setDescription( "This entity represents the user of an application" );
    
    MetricHelper.createAttribute( "Top five user actions",
                                  "This attribute represents the five most common user actions with the application", 
                                  userEntity );
    
    MetricHelper.createAttribute( "User perceived application speed",
                                  "This attribute represents a scaled response perceived speed of the application (1=slow.. 5=fast)", 
                                  userEntity );
  }
  
  private void createMetricStructures()
  {
    metricGenerators = new HashSet<MetricGenerator>();
    
    MetricGenerator applicationMetricGen = new MetricGenerator();
    applicationMetricGen.setName( "Application metric generator" );
    applicationMetricGen.setDescription( "Represents the logical instrumentation for the application" );
    applicationMetricGen.addEntity( applicationEntity );
    
    MetricHelper.createMetricGroup( "Basic QoS",
                                    "Container for basic QoS metrics", 
                                    applicationMetricGen );
    
    MetricGenerator userMetricGen = new MetricGenerator();
    userMetricGen.setName( "User metric generator" );
    userMetricGen.setDescription( "Represents the logical instrumentation for the user" );
    userMetricGen.addEntity( userEntity );
    
    MetricHelper.createMetricGroup( "Basic QoE",
                                    "Container for basic QoE metrics", 
                                    userMetricGen );
    
    metricGenerators.add( applicationMetricGen );
    metricGenerators.add( userMetricGen );
  }
  
  private void createQoSMeasurementSets()
  {
    MetricGenerator mGen = MetricHelper.getMetricGeneratorByName( "Application metric generator", metricGenerators );
    MetricGroup group    = MetricHelper.getMetricGroupByName( "Basic QoS", mGen.getMetricGroups() );
    
    Attribute attr = MetricHelper.getAttributeByName( "Application execution time", applicationEntity );
    MetricHelper.createMeasurementSet( attr, 
                                       MetricType.RATIO,
                                       new Unit( "seconds"), 
                                       group );
    
    attr = MetricHelper.getAttributeByName( "Application device temperature", applicationEntity );
    MetricHelper.createMeasurementSet( attr, 
                                       MetricType.INTERVAL,
                                       new Unit( "Celcius"), 
                                       group );
  }
  
  private void createQoEMeasurementSets()
  {
    MetricGenerator mGen = MetricHelper.getMetricGeneratorByName( "User metric generator", metricGenerators );
    MetricGroup group    = MetricHelper.getMetricGroupByName( "Basic QoE", mGen.getMetricGroups()  );
    
    Attribute attr = MetricHelper.getAttributeByName( "Top five user actions", userEntity );
    MetricHelper.createMeasurementSet( attr, 
                                       MetricType.NOMINAL,
                                       new Unit( "UI Action dictionary 1"), 
                                       group );
    
    attr = MetricHelper.getAttributeByName( "User perceived application speed", userEntity );
    MetricHelper.createMeasurementSet( attr, 
                                       MetricType.ORDINAL,
                                       new Unit( "Perceived speed"), 
                                       group );
  }
  
  private void demoPopulateQoSData()
  {
    // Create a measurement for execution time
    MetricGenerator mGen = MetricHelper.getMetricGeneratorByName( "Application metric generator", metricGenerators );
    Attribute       attr = MetricHelper.getAttributeByName( "Application execution time", applicationEntity );
    MeasurementSet  ms   = MetricHelper.getMeasurementSetForAttribute( attr, mGen );
    
    // Always create new instances of measurement sets to send to the ECC (not implemented here)
    MeasurementSet newMS = new MeasurementSet( ms, false );
    Measurement        m = new Measurement( "100" );
    newMS.addMeasurement( m );
    logger.info( "Execution time measurement: " + m.getValue() + " (" +
                 newMS.getMetric().getUnit().toString() +")" );
    
    // Device temperature
    attr  = MetricHelper.getAttributeByName( "Application device temperature", applicationEntity );
    ms    = MetricHelper.getMeasurementSetForAttribute( attr, mGen );
    newMS = new MeasurementSet( ms, false );
    m     = new Measurement( "28" );
    newMS.addMeasurement( m );
    logger.info( "Application device temperature measurement: " + m.getValue() + " (" +
                 newMS.getMetric().getUnit().toString() +")" );
  }
  
  private void demoPopulateQoEData()
  {
    // Create a sample for top five user actions
    MetricGenerator mGen = MetricHelper.getMetricGeneratorByName( "User metric generator", metricGenerators );
    Attribute       attr = MetricHelper.getAttributeByName( "Top five user actions", userEntity );
    MeasurementSet  ms   = MetricHelper.getMeasurementSetForAttribute( attr, mGen );
    
    // Always create new instances of measurement sets to send to the ECC (not implemented here)
    MeasurementSet newMS = new MeasurementSet( ms, false );
    Measurement        m = new Measurement( "undo, select, cut, paste, save" );
    newMS.addMeasurement( m );
    logger.info( "Top five actions: " + m.getValue() + " (" +
                 newMS.getMetric().getUnit().toString() +")" );
    
    // Device temperature
    attr  = MetricHelper.getAttributeByName( "User perceived application speed", userEntity );
    ms    = MetricHelper.getMeasurementSetForAttribute( attr, mGen );
    newMS = new MeasurementSet( ms, false );
    m     = new Measurement( "5" );
    newMS.addMeasurement( m );
    logger.info( "User scaled rating: " + m.getValue() + " (" +
                 newMS.getMetric().getUnit().toString() +")" );
  } 
}
