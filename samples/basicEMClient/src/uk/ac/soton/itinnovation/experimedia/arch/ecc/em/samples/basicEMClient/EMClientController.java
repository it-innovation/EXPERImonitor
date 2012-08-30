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
//      Created Date :          15-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.samples.basicEMClient;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import java.util.*;





public class EMClientController implements EMIAdapterListener,
                                           EMClientViewListener
{
  private AMQPBasicChannel   amqpChannel;
  private EMInterfaceAdapter emiAdapter;
  private EMClientView       clientView;
  private String             clientName;
 
  private Entity                        entityBeingObserved;
  private Attribute                     entityAttribute;
  private HashMap<UUID,MetricGenerator> metricGenerators;
  
  
  public EMClientController()
  {
    metricGenerators = new HashMap<UUID,MetricGenerator>();
  }
  
  public void start( String rabbitServerIP,
                     UUID expMonitorID,
                     UUID clientID ) throws Exception
  {
    if ( rabbitServerIP != null &&
         expMonitorID   != null &&
         clientID       != null )
    {
      // Create connection to Rabbit server ------------------------------------
      AMQPConnectionFactory amqpFactory = new AMQPConnectionFactory();
      amqpFactory.setAMQPHostIPAddress( rabbitServerIP );
      try
      {
        amqpFactory.connectToAMQPHost();
        amqpChannel = amqpFactory.createNewChannel();
      }
      catch (Exception e ) { throw e; }
      
      // Set up a simple view --------------------------------------------------
      Date date = new Date();
      clientName = date.toString();
      clientView = new EMClientView( clientName, this );
      clientView.setVisible( true );
      
      // Create EM interface adapter, listen to it...
      emiAdapter = new EMInterfaceAdapter( this );
      
      // ... and try registering with the EM.
      
      try { emiAdapter.registerWithEM( clientName,
                                       amqpChannel, 
                                       expMonitorID, clientID ); }
      catch ( Exception e ) 
      { throw e; }
    }
  }
  
  // EMIAdapterListener --------------------------------------------------------
  @Override
  public void onEMConnectionResult( boolean connected )
  {
    if ( connected )
      clientView.setStatus( "Connected to EM" );
    else
      clientView.setStatus( "Refused connection to EM" );
  }
  
  @Override
  public void onPopulateMetricGeneratorInfo()
  {
    clientView.setStatus( "Sending metric gen info to EM" );
    
    entityBeingObserved = new Entity();
    entityBeingObserved.setName( "EM Client host" );
    
    entityAttribute = new Attribute();
    entityAttribute.setName( "Client RAM usage" );
    entityAttribute.setDescription( "Very simple measurement of total bytes used" );
    entityAttribute.setEntityUUID( entityBeingObserved.getUUID() );
    entityBeingObserved.addtAttribute( entityAttribute );
    
    // Mock up some metric generators
    MetricGenerator metricGen = new MetricGenerator();
    metricGen.setName( "MGEN " + clientName );
    metricGen.setDescription( "Metric generator demonstration" );
    metricGen.addEntity( entityBeingObserved );         
    metricGenerators.put( metricGen.getUUID(), metricGen );
    
    MetricGroup mg = new MetricGroup();
    mg.setName( "Demo group" );
    mg.setDescription( "A single group to contain metrics" );
    mg.setMetricGeneratorUUID( metricGen.getUUID() );
    metricGen.addMetricGroup( mg );
    
    MeasurementSet ms = new MeasurementSet();
    ms.setAttributeUUID( entityAttribute.getUUID() );             
    mg.addMeasurementSets( ms );                              
    
    //TODO: Get this right!
    Metric memMetric = new Metric();
    //TODO: Unit set-up for mem usage
    ms.setMetric( memMetric );
    
    clientView.addLogMessage( "Discovered generator: " + metricGen.getName() );
    
    // Send metric generators (just one) to the EM
    HashSet mgSet = new HashSet<MetricGenerator>();
    mgSet.addAll( metricGenerators.values() );
    emiAdapter.setMetricGenerators( mgSet );
  }
  
  @Override
  public void onSetupMetricGenerator( UUID genID, Boolean[] resultOUT )
  {
    clientView.setStatus( "Setting up generators" );
    
    // Just signal that the metric generator is ready
    resultOUT[0] = true;
    
    clientView.addLogMessage( "Completed generator set-up" );
  }
  
  @Override
  public void onStartPushingMetricData()
  {
    clientView.addLogMessage( "Enabling metric push" );
    clientView.enablePush( true );
  }
  
  @Override
  public void onLastPushProcessed( UUID lastReportID )
  {
    // Got the last push, so allow another
    clientView.enablePush( true );
  }
  
  @Override
  public void onStopPushingMetricData()
  {
    clientView.addLogMessage( "Disabling metric push" );
    clientView.enablePush( false );
  }
  
  @Override
  public void onPullMetric( UUID measurementSetID, Report reportOut )
  {
    // We've only got one MeasurementSet so we don't need to search on ID
    // Create a sample measurement
    Measurement measurement = new Measurement();
    measurement.setTimeStamp( new Date() );
    measurement.setValue( takeMeasurement() );
     
    // Create an empty instance of our measurement set
    MeasurementSet sampleSet = createMeasurementSetEmptySample();
    sampleSet.addMeasurement( measurement );
    
    reportOut.setMeasurementSet( sampleSet );
  }
  
  @Override
  public void onGetTearDownResult( Boolean[] resultOUT )
  {
    clientView.setStatus( "Tearing down" );
    clientView.addLogMessage( "Tearing down metric generators" );
    
    // Signal we've successfully torn-down
    resultOUT[0] = true;
  }
  
  // EMClientViewListener ------------------------------------------------------
  @Override
  public void onPushDataClicked()
  {
    // Create a sample measurement
    Measurement measurement = new Measurement();
    measurement.setTimeStamp( new Date() );
    measurement.setValue( takeMeasurement() );
     
    // Create an empty instance of our measurement set
    MeasurementSet sampleSet = createMeasurementSetEmptySample();
    sampleSet.addMeasurement( measurement );
    
    Report randomReport = new Report();
    randomReport.setMeasurementSet( sampleSet );
    
    // ... and report!
    emiAdapter.pushMetric( randomReport );
  }
  
  // Private method ------------------------------------------------------------
  private MeasurementSet createMeasurementSetEmptySample()
  {
    // Get our only metric generator
    MetricGenerator metGen = metricGenerators.values().iterator().next();
    metGen.getMetricGroups().iterator().next();
    
    // Get our only metric group
    MetricGroup mg = metGen.getMetricGroups().iterator().next();
    MeasurementSet currentMS = mg.getMeasurementSets().iterator().next();
    
    return new MeasurementSet( currentMS, false );
  }
  
  private String takeMeasurement()
  {
    Runtime rt = Runtime.getRuntime();
    
    // Just take a very rough measurement
    String memVal = Long.toString( rt.totalMemory() - rt.freeMemory() );
    
    clientView.addLogMessage( "Memory measurement (bytes): " + memVal );
    return memVal;
  }
}
