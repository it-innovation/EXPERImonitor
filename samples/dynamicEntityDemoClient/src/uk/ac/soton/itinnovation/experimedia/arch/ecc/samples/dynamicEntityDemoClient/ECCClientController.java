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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.dynamicEntityDemoClient;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.loggin.impl.Log4JImpl;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;

import java.util.*;
import javax.swing.JOptionPane;




public class ECCClientController implements EMIAdapterListener,
                                            ECCClientViewListener,
                                            ECCNewEntityViewListener
{
    private final IECCLogger clientLogger;

    private AMQPBasicChannel   amqpChannel;
    private EMInterfaceAdapter emiAdapter;
    private ECCClientView      clientView;
    private String             clientName;

    private MetricGenerator metricGenerator;
    private MetricGroup     metricGroup;
 


    public ECCClientController()
    {
        // Configure logging system
        Logger.setLoggerImpl( new Log4JImpl() );
        clientLogger = Logger.getLogger( ECCClientController.class );
      
        // Only one metric generator and metric group
        metricGenerator = new MetricGenerator();
        metricGenerator.setName( "Demo metric generator" );
        
        // Only one metric group
        metricGroup = MetricHelper.createMetricGroup( "Example group", "Group to add new metrics in", metricGenerator );
        
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
            clientView = new ECCClientView( clientName, this, this );
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
       HashSet<MetricGenerator> mgSet = new HashSet<MetricGenerator>();
       mgSet.add( metricGenerator );
       
       emiAdapter.sendMetricGenerators( mgSet );
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
        // Got the last push, so allow another manual push
        clientView.enablePush( true );
    }
    
    @Override
    public void onPullReportReceived( UUID reportID )
    {

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
    public void onPullMetric( UUID measurementSetID, Report reportOut)
    {
        // Create an empty instance of our measurement set
        MeasurementSet targetSet = MetricHelper.getMeasurementSet( metricGenerator, measurementSetID );

        // Create a copy of the measurement set, but with no measurements in
        Report newReport = MetricHelper.createEmptyMeasurementReport( targetSet );
        
        // Generate a simulated measurement using a random number
        int ran = 1 +(int)(Math.random()*20);
        String mes = ""+ ran;
        Measurement m = new Measurement( mes );
        
        // Add measurement
        newReport.getMeasurementSet().addMeasurement( m );
        newReport.setNumberOfMeasurements( 1 );
       
        // Copy report into OUT parameter
        reportOut.copyReport( newReport, true );
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
//        if ( firstMeasurement   ==  null ) snapshotMeasurement();
//        if ( currentMeasurement ==  null ) snapshotMeasurement();
//
//        // Create a new report for this summary
//        Report report = new Report();
//        report.setReportDate( new Date() );
//        report.setFromDate( firstMeasurement.getTimeStamp() );
//        report.setToDate( currentMeasurement.getTimeStamp() );
//        report.setNumberOfMeasurements( 2 );
//
//        // We've only got one of each...
//        MetricGenerator mGen = metricGenerators.values().iterator().next();
//        MetricGroup mGroup = mGen.getMetricGroups().iterator().next();
//        MeasurementSet mSet = mGroup.getMeasurementSets().iterator().next();
//
//        // Add our one MeasurementSet data to the report and add that to the summary report
//        report.setMeasurementSet( mSet );
//        summaryOUT.addReport( report );
    }

    @Override
    public void onPopulateDataBatch( EMDataBatch batchOUT )
    {
        // We've only stored the first and the last measurements of a single
        // MeasurementSet, so just send that
//        MeasurementSet ms = createMeasurementSetEmptySample();
//        ms.addMeasurement( firstMeasurement );
//        ms.addMeasurement( currentMeasurement );
//        
//        Report batchRep = new Report();
//        batchRep.setFromDate( firstMeasurement.getTimeStamp() );
//        batchRep.setToDate( currentMeasurement.getTimeStamp() );
//        batchRep.setMeasurementSet( ms );
//        batchRep.setNumberOfMeasurements( 2 );
//        
//        batchOUT.setBatchReport( batchRep );
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
//        MeasurementSet sampleSet = createMeasurementSetEmptySample();
//
//        // Take a current measurement
//        snapshotMeasurement();
//        sampleSet.addMeasurement( currentMeasurement );
//
//        // Set up report (has only a single measure)
//        Date date           = new Date();
//        Report sampleReport = new Report();
//
//        sampleReport.setMeasurementSet( sampleSet );
//        sampleReport.setReportDate(date );
//        sampleReport.setFromDate( date );
//        sampleReport.setToDate( date );
//        sampleReport.setMeasurementSet( sampleSet );
//        sampleReport.setNumberOfMeasurements( 1 );
//
//        // ... and store for confirmation of push, then report!
//        pendingPushReports.put( sampleReport.getUUID(), sampleReport );
//        emiAdapter.pushMetric( sampleReport );
    }
    
    @Override
    public void onEntityMetricCollectionEnabled( UUID senderID, UUID entityID, boolean enabled )
    {
    
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

    
    // ECCNewEntityViewListener events -----------------------------------------
    @Override
    public void onNewEntityInfoEntered(String entityName, String attName, String attDesc )
    {

        // Create a new entity to be observed (this Java VM)
        Entity entityBeingObserved = new Entity();
        entityBeingObserved.setName( entityName );

        // Create an attribute to observe
        Attribute entityAttribute = MetricHelper.createAttribute(attName, attDesc, entityBeingObserved);

        // Link entity with metric generator
        metricGenerator.addEntity( entityBeingObserved );
        
        // ... a single MeasurementSet (representing the measures for the attibute)
        MetricHelper.createMeasurementSet( entityAttribute, 
                                           MetricType.RATIO, 
                                           new Unit("Request count"), 
                                           metricGroup );
        
        clientView.addLogMessage( "Created new entity: " + entityBeingObserved.getName() );        
    }
    
    
    // Private method ------------------------------------------------------------
//    private MeasurementSet createMeasurementSetEmptySample()
//    {
//        // Get our only metric generator
//        MetricGenerator metGen = metricGenerators.values().iterator().next();
//        metGen.getMetricGroups().iterator().next();
//
//        // Get our only metric group
//        MetricGroup mg = metGen.getMetricGroups().iterator().next();
//        MeasurementSet currentMS = mg.getMeasurementSets().iterator().next();
//
//        return new MeasurementSet( currentMS, false );
//    }

//    private void snapshotMeasurement()
//    {
//        // Just take a very rough measurement
//        Runtime rt = Runtime.getRuntime();
//        String memVal = Long.toString( rt.totalMemory() - rt.freeMemory() );
//
//        // Get the (single) measurement set for this snapshot (just to get the ID actually)
//        MeasurementSet snapshotMS = createMeasurementSetEmptySample();
//
//        // Update the latest measurement
//        currentMeasurement = new Measurement();
//        currentMeasurement.setMeasurementSetUUID( snapshotMS.getID() );
//        currentMeasurement.setTimeStamp( new Date() );
//        currentMeasurement.setValue( memVal );
//
//        // Store this if it is the first ever measurement
//        if ( firstMeasurement == null ) firstMeasurement = currentMeasurement;
//
//        clientView.addLogMessage( "Memory measurement (bytes): " + memVal );
//    }
}
