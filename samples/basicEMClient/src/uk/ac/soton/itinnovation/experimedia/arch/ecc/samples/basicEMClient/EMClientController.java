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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.basicEMClient;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;

import org.apache.log4j.Logger;

import java.util.*;



public class EMClientController implements EMIAdapterListener,
                                           EMClientViewListener
{
    private final Logger clientLogger = Logger.getLogger( EMClientController.class );

    private AMQPBasicChannel   amqpChannel;
    private EMInterfaceAdapter emiAdapter;
    private EMClientView       clientView;
    private String             clientName;

    private Entity                        entityBeingObserved;
    private Attribute                     entityAttribute;
    private HashMap<UUID,MetricGenerator> metricGenerators;

    private Measurement firstMeasurement;
    private Measurement currentMeasurement;



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
            clientLogger.info( "Trying to connect to Rabbit server on " + rabbitServerIP );

            // Create connection to Rabbit server ------------------------------------
            AMQPConnectionFactory amqpFactory = new AMQPConnectionFactory();
            amqpFactory.setAMQPHostIPAddress( rabbitServerIP );
            try
            {
                amqpFactory.connectToAMQPHost();
                amqpChannel = amqpFactory.createNewChannel();
            }
            catch (Exception e ) 
            {
                clientLogger.error( "Could not connect to Rabbit server" );
                throw e; 
            }

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
            {
                clientLogger.error( "Could not attempt registration with EM" );
                throw e; 
            }
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

        // Create a new entity to be observed (this Java VM)
        entityBeingObserved = new Entity();
        entityBeingObserved.setName( "EM Client host" );

        // Create an attribute to observe
        entityAttribute = new Attribute();
        entityAttribute.setName( "Client RAM usage" );
        entityAttribute.setDescription( "Very simple measurement of total bytes used" );
        entityAttribute.setEntityUUID( entityBeingObserved.getUUID() );
        entityBeingObserved.addAttribute( entityAttribute );

        // Create a single metric generator that will represent this metric data generation
        MetricGenerator metricGen = new MetricGenerator();
        metricGen.setName( "MGEN " + clientName );
        metricGen.setDescription( "Metric generator demonstration" );
        metricGen.addEntity( entityBeingObserved );

        // Add our generator to a collection - obviously we only actually have one generator here though
        metricGenerators.put( metricGen.getUUID(), metricGen );

        // Create a single group which will contain...
        MetricGroup mg = new MetricGroup();
        mg.setName( "Demo group" );
        mg.setDescription( "A single group to contain metrics" );
        mg.setMetricGeneratorUUID( metricGen.getUUID() );
        metricGen.addMetricGroup( mg );

        // ... a single MeasurementSet (representing the measures for the attibute)
        MeasurementSet ms = new MeasurementSet();
        ms.setMetricGroupUUID( mg.getUUID() );
        ms.setAttributeUUID( entityAttribute.getUUID() ); // Link the measurement set to the attribute here
        mg.addMeasurementSets( ms );                              

        // Define the metric for this MeasurementSet
        Metric memMetric = new Metric();
        memMetric.setMetricType( MetricType.RATIO );
        memMetric.setUnit( new Unit("Bytes") );
        ms.setMetric( memMetric );

        clientView.addLogMessage( "Discovered generator: " + metricGen.getName() );

        // Ready our metric generator for the EM
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
        // Allow the human user to manually push some data
        clientView.addLogMessage( "Enabling metric push" );
        clientView.enablePush( true );
    }

    @Override
    public void onLastPushProcessed( UUID lastReportID )
    {
        // Got the last push, so allow another manual push
        clientView.enablePush( true );
    }

    @Override
    public void onStopPushingMetricData()
    {
        // Stop manual pushing of data by the human user
        clientView.addLogMessage( "Disabling metric push" );
        clientView.enablePush( false );
    }

    @Override
    /*
    * Note that 'reportOut' is an OUT parameter provided by the adapter
    */
    public void onPullMetric( UUID measurementSetID, Report reportOut )
    {
        // Create an empty instance of our measurement set
        MeasurementSet sampleSet = createMeasurementSetEmptySample();

        // Add a snapshot measurement to it
        snapshotMeasurement();
        sampleSet.addMeasurement( currentMeasurement );

        // Set up report (has only a single measure)
        Date date = new Date();
        reportOut.setReportDate( date );
        reportOut.setFromDate( date );
        reportOut.setToDate( date );
        reportOut.setMeasurementSet( sampleSet );
        reportOut.setNumberOfMeasurements( 1 );
    }

    @Override
    /*
    * Note that the summaryOUT parameter is an OUT parameter supplied by the
    * adapter
    */
    public void onPopulateSummaryReport( EMPostReportSummary summaryOUT )
    {
        // We've only got one MeasurementSet so we'll create a demo summary report
        // and just two measurements.. so we'll use these to create a demo summary

        // If we don't have any measurements, make some up!
        if ( firstMeasurement   ==  null ) snapshotMeasurement();
        if ( currentMeasurement ==  null ) snapshotMeasurement();

        // Create a new report for this summary
        Report report = new Report();
        report.setReportDate( new Date() );
        report.setFromDate( firstMeasurement.getTimeStamp() );
        report.setToDate( currentMeasurement.getTimeStamp() );
        report.setNumberOfMeasurements( 2 );

        // We've only got one of each...
        MetricGenerator mGen = metricGenerators.values().iterator().next();
        MetricGroup mGroup = mGen.getMetricGroups().iterator().next();
        MeasurementSet mSet = mGroup.getMeasurementSets().iterator().next();

        // Add our one MeasurementSet data to the report and add that to the summary report
        report.setMeasurementSet( mSet );
        summaryOUT.addReport( report );
    }

    @Override
    public void onPopulateDataBatch( EMDataBatch batchOut )
    {
        // We've only stored the first and the last measurements of a single
        // MeasurementSet, so just send that
        MeasurementSet ms = batchOut.getMeasurementSet();
        ms.addMeasurement( firstMeasurement );
        ms.addMeasurement( currentMeasurement );
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
        // Create an empty instance of our measurement set
        MeasurementSet sampleSet = createMeasurementSetEmptySample();

        // Take a current measurement
        snapshotMeasurement();
        sampleSet.addMeasurement( currentMeasurement );

        // Set up report (has only a single measure)
        Date date           = new Date();
        Report sampleReport = new Report();

        sampleReport.setMeasurementSet( sampleSet );
        sampleReport.setReportDate(date );
        sampleReport.setFromDate( date );
        sampleReport.setToDate( date );
        sampleReport.setMeasurementSet( sampleSet );
        sampleReport.setNumberOfMeasurements( 1 );

        // ... and report!
        emiAdapter.pushMetric( sampleReport );
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

    private void snapshotMeasurement()
    {
        // Just take a very rough measurement
        Runtime rt = Runtime.getRuntime();
        String memVal = Long.toString( rt.totalMemory() - rt.freeMemory() );

        // Get the (single) measurement set for this snapshot (just to get the ID actually)
        MeasurementSet snapshotMS = createMeasurementSetEmptySample();

        // Update the latest measurement
        currentMeasurement = new Measurement();
        currentMeasurement.setMeasurementSetUUID( snapshotMS.getUUID() );
        currentMeasurement.setTimeStamp( new Date() );
        currentMeasurement.setValue( memVal );

        // Store this if it is the first ever measurement
        if ( firstMeasurement == null ) firstMeasurement = currentMeasurement;

        clientView.addLogMessage( "Memory measurement (bytes): " + memVal );
    }
}
