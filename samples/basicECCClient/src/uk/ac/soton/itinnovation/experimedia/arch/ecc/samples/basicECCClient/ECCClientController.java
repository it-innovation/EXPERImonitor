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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.basicECCClient;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;

import org.apache.log4j.Logger;

import java.util.*;
import javax.swing.JOptionPane;




public class ECCClientController implements EMIAdapterListener,
                                            ECCClientViewListener
{
    private final Logger clientLogger = Logger.getLogger( ECCClientController.class );

    private AMQPBasicChannel   amqpChannel;
    private EMInterfaceAdapter emiAdapter;
    private ECCClientView      clientView;
    private String             clientName;

    private Entity                        entityBeingObserved;
    private Attribute                     entityAttribute;
    private HashMap<UUID,MetricGenerator> metricGenerators;

    private Measurement firstMeasurement;
    private Measurement currentMeasurement;
    
    private HashMap<UUID, Report> pendingPushReports;
    private HashMap<UUID, Report> pendingPullReports;



    public ECCClientController()
    {
        metricGenerators   = new HashMap<UUID,MetricGenerator>();
        pendingPushReports = new HashMap<UUID, Report>();
        pendingPullReports = new HashMap<UUID, Report>();
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
            clientView = new ECCClientView( clientName, this );
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
    public void onEMConnectionResult( boolean connected, Experiment expInfo )
    {
        if ( connected )
        {
          clientView.setStatus( "Connected to EM" );
          clientView.addLogMessage( "Linked to experiment: " + expInfo.getName() );
        }
        else
          clientView.setStatus( "Refused connection to EM" );
    }

    @Override
    public void onEMDeregistration( String reason )
    {
        clientView.addLogMessage( "Got disconnected from EM: " + reason );
        
        try
        { emiAdapter.disconnectFromEM(); }
        catch ( Exception e )
        { clientLogger.error( "Had problems disconnecting from EM: " + e.getMessage() ); }
        
        // Apologise to the user
        JOptionPane.showMessageDialog( null, "ECC disconnected this client: " + reason );
        
        clientView.dispose();
        System.exit(0);
    }
    
    @Override
    public void onDescribeSupportedPhases( EnumSet<EMPhase> phasesOUT )
    {
        // We're going to support all phases (although we won't do much in some of them)
        // ... we MUST support the discovery phase by default, but don't need to include
        phasesOUT.add( EMPhase.eEMSetUpMetricGenerators );
        phasesOUT.add( EMPhase.eEMLiveMonitoring );
        phasesOUT.add( EMPhase.eEMPostMonitoringReport );
        phasesOUT.add( EMPhase.eEMTearDown );
    }
    
    @Override
    public void onDescribePushPullBehaviours( Boolean[] pushPullOUT )
    {
        // We're going to support both push and pull
        pushPullOUT[0] = true;
        pushPullOUT[1] = true;
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
        memMetric.setUnit( new Unit("bytes") );
        ms.setMetric( memMetric );

        clientView.addLogMessage( "Discovered generator: " + metricGen.getName() );

        // Ready our metric generator for the EM
        HashSet mgSet = new HashSet<MetricGenerator>();
        mgSet.addAll( metricGenerators.values() );
        emiAdapter.setMetricGenerators( mgSet );
    }

    @Override
    public void onDiscoveryTimeOut()
    { clientView.addLogMessage( "Got discovery time-out message" ); }
    
    @Override
    public void onSetupMetricGenerator( UUID genID, Boolean[] resultOUT )
    {
        clientView.setStatus( "Setting up generators" );

        // Just signal that the metric generator is ready
        resultOUT[0] = true;

        clientView.addLogMessage( "Completed generator set-up" );
    }
    
    @Override
    public void onSetupTimeOut( UUID metricGeneratorID )
    { clientView.addLogMessage( "Got set-up time-out message" ); }
    
    @Override
    public void onLiveMonitoringStarted()
    {
        clientView.addLogMessage( "ECC has started Live Monitoring process" );
    }

    @Override
    public void onStartPushingMetricData()
    {
        // Allow the human user to manually push some data
        clientView.addLogMessage( "Enabling metric push" );
        clientView.enablePush( true );
    }

    @Override
    public void onPushReportReceived( UUID reportID )
    {
        // We'll use this report with the 'MiniEDM' later, but for now...
        pendingPushReports.remove( reportID );
      
        // Got the last push, so allow another manual push
        clientView.enablePush( true );
    }
    
    @Override
    public void onPullReportReceived( UUID reportID )
    {
        // We'll use this report with the 'MiniEDM' later, but for now...
        pendingPullReports.remove( reportID );
    }
    
    @Override
    public void onPullMetricTimeOut( UUID measurementSetID )
    { clientView.addLogMessage( "Got live pull time-out message" ); }

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
        
        // Store for confirmation of pull later
        pendingPullReports.put( reportOut.getUUID(), reportOut );
    }

    @Override
    public void onPullingStopped()
    {
        clientView.addLogMessage( "ECC has stopped pulling" );
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
    public void onPopulateDataBatch( EMDataBatch batchOUT )
    {
        // We've only stored the first and the last measurements of a single
        // MeasurementSet, so just send that
        MeasurementSet ms = createMeasurementSetEmptySample();
        ms.addMeasurement( firstMeasurement );
        ms.addMeasurement( currentMeasurement );
        
        Report batchRep = new Report();
        batchRep.setFromDate( firstMeasurement.getTimeStamp() );
        batchRep.setToDate( currentMeasurement.getTimeStamp() );
        batchRep.setMeasurementSet( ms );
        batchRep.setNumberOfMeasurements( 2 );
        
        batchOUT.setBatchReport( batchRep );
    }
    
    @Override
    public void onReportBatchTimeOut( UUID batchID )
    { clientView.addLogMessage( "Got post-report time-out message" ); }

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

        // ... and store for confirmation of push, then report!
        pendingPushReports.put( sampleReport.getUUID(), sampleReport );
        emiAdapter.pushMetric( sampleReport );
    }
    
    @Override
    public void onClientViewClosed()
    {
        // Need to notify that we're leaving...
        try { emiAdapter.disconnectFromEM(); }
        catch ( Exception e )
        { clientLogger.error( "Could not cleanly disconnect from EM:\n" + e.getMessage() ); }
    }
    
    @Override
    public void onTearDownTimeOut()
    { clientView.addLogMessage( "Got tear-down time-out message" ); }

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
        currentMeasurement.setMeasurementSetUUID( snapshotMS.getID() );
        currentMeasurement.setTimeStamp( new Date() );
        currentMeasurement.setValue( memVal );

        // Store this if it is the first ever measurement
        if ( firstMeasurement == null ) firstMeasurement = currentMeasurement;

        clientView.addLogMessage( "Memory measurement (bytes): " + memVal );
    }
}
