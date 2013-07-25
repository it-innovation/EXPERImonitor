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
//      Created Date :          18-Mar-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.basicAndroidClient;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared.*;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.*;



/**
 * The ClientController manages interactive messages from both the user (via the
 * ClientView) and the ECC (via the EMInterfaceAdapter). Messages from the ECC
 * are delivered from a background thread and some changes on the UI must be queued
 * if they come from this source.
 * 
 */
public class ClientController implements EMIAdapterListener,
                                         ClientViewListener 
{
    private AMQPConnectionFactory amqpFactory;    // AMQP connection components
    private AMQPBasicChannel      amqpChannel;
    private EMInterfaceAdapter    emiAdapter;     // ECC adapter and application state
    private boolean               connected;
    private boolean               pushingAllowed;

    private MetricGenerator generator;            // ECC metric generator model
    private UUID            measurementSetID;     // MeasurementSet ID of the single metric this sample provides
    private UUID            sentReportID;         // Last report ID pushed to the ECC (used for traffic control)

    private ClientView  clientView;               // User interface
    private UIMessenger uiMessenger;              // Queued message handler (for background changes to the UI)

    private final int UI_LOGMESSAGE   = 0;        // Queued UI message types
    private final int UI_ENABLE_PUSH  = 1;
    private final int UI_CONNECTED    = 2;
    private final int UI_DISCONNECTED = 3;

    /**
     * Constructor for the Android controller
     * 
     * @param act - Android Activity instance (must not be null).
     */
    public ClientController( Activity act ) 
    {
        // Create the user interface and queued message handler
        clientView  = new ClientView( act, this );
        uiMessenger = new UIMessenger( Looper.getMainLooper() );

        // Create AMQP/ECC connectivity components
        amqpFactory = new AMQPConnectionFactory();
        emiAdapter  = new EMInterfaceAdapter( this );

        // Say hello
        queueLogMessage( "\nWelcome." );
    }

    /**
     * Tries to close down the connection with the ECC.
     */
    public void closeDown()
    {
        // Try disconnecting from the ECC
        if ( emiAdapter != null )
            tryDisconnectingFromECC();
    }

    // EMIAdapterListener ------------------------------------------------------
    /**
     * Message representing an acknowledgement from the ECC of the client's connection attempt.
     * If ok, you should get details of the experiment this client is currently associated with.
     * 
     * @param connected - Connection request result. True if ECC has accepted the client.
     * @param expInfo   - Experiment instance information for the client.
     */
    @Override
    public void onEMConnectionResult( boolean connected, Experiment expInfo )
    {
        if ( connected )
        {
            // Display connection success to user
            queueLogMessage( "Successful registration with the ECC" );
            queueConnectionChangeOnView( UI_CONNECTED );

            // If experiment information is OK, present it
            if ( expInfo != null )
            {
                String info = "Experiment ID: " + expInfo.getExperimentID() + "\n";
                info += "Experiment started: " + expInfo.getStartTime().toString();

                queueLogMessage( info );
            }
            // Otherwise note problems
            else queueLogMessage( "Did not get experiment information" );
        }
        else queueLogMessage( "Registration with the ECC denied" );
    }

    /**
     * Event representing a disconnection of this client from the ECC.
     * 
     * @param reason - String providing the reason for the ECC disconnecting the client.
     */
    @Override
    public void onEMDeregistration( String reason )
    {
        String deInfo = "Got deregistered from ECC";
        if ( reason != null ) deInfo += ": " + reason;

        // Make sure we don't try to push anything any more
        pushingAllowed = false;
        
        // Send messages to the user
        queueLogMessage( deInfo );
        queueEnablePushOnView();
        queueConnectionChangeOnView( UI_DISCONNECTED );
    }

    /**
     * Message from the ECC requesting that the client describes which experimentation
     * phases it supports.
     * 
     * @param phasesOUT - OUT parameter to be filled with the phases this client will repond to.
     */
    @Override
    public void onDescribeSupportedPhases( EnumSet<EMPhase> phasesOUT )
    {
        // Just going to support set-up, live monitoring and tear-down
        phasesOUT.add( EMPhase.eEMSetUpMetricGenerators );
        phasesOUT.add( EMPhase.eEMLiveMonitoring );
        phasesOUT.add( EMPhase.eEMTearDown );
    }

    /**
     * Message from the ECC asking whether this client can push, pull or do both.
     * 
     * @param pushPullOUT - OUT parameter to be filled with two booleans at indices [0] and [1]
     */
    @Override
    public void onDescribePushPullBehaviours( Boolean[] pushPullOUT )
    {
        pushPullOUT[0] = true;  // Allow pushing
        pushPullOUT[1] = false; // Do not pull
    }

    /**
     * Message from the ECC (via the adapter) to describe the metric model for this client.
     * Once you have constructed your metric model, you should call the adapter method 'setMetricGenerators(..)'
     */
    @Override
    public void onPopulateMetricGeneratorInfo()
    {
        queueLogMessage( "Creating metric generator..." );
        
        emiAdapter.sendMetricGenerators( createMetricGeneratorSet() );
    }

    @Override
    public void onDiscoveryTimeOut()
    { /*Not implemented in this demo*/ }

    /**
     * ECC message requesting that the client perform and set-up required for the metric
     * generator it described during the discovery phase.
     * 
     * @param metricGeneratorID - ID of the Metric Generator to set up
     * @param resultOUT         - OUT parameter representing the success of the set-up
     */
    @Override
    public void onSetupMetricGenerator( UUID metricGeneratorID, Boolean[] resultOUT )
    {
        // Make sure we have the metric generator the ECC thinks we have
        if ( generator != null && generator.getUUID().equals(metricGeneratorID) )
        {
          queueLogMessage( "Setting up client OK" );
          resultOUT[0] = true; // Just say we've been successful; nothing actually to do
        }
        else
          queueLogMessage( "Got set-up request but generator is unknown" );
    }
    
    @Override
    public void onSetupTimeOut( UUID metricGeneratorID )
    { /*Not implemented in this demo*/ }

    /**
     * Event from the ECC notifying that the client that Live monitoring has started.
     * 
     */
    @Override
    public void onLiveMonitoringStarted()
    {
        queueLogMessage( "Live monitoring has started" );
    }

    /**
     * Event from the ECC notifying the client that it can start pushing metric data.
     * 
     */
    @Override
    public void onStartPushingMetricData()
    {
        queueLogMessage( "Metric pushing enabled" );
        pushingAllowed = true;
        
        queueEnablePushOnView();
    }

    /**
     * Notification from the ECC that the Report (referenced by the UUID) has been
     * received. You should use this event to manage network traffic between this
     * client and the ECC. It is strongly recommended to only send one report at a time
     * and wait for an acknowledgement before sending further data.
     * 
     * @param lastReportID 
     */
    @Override
    public void onPushReportReceived( UUID lastReportID )
    {
        if ( lastReportID != null )
        {
          queueLogMessage( "ECC received push report: " + lastReportID );

          // Confirm the ECC has got the report we last sent; then reset report 
          // ID so that we are ready to send the next report
          if ( lastReportID.equals(sentReportID) )
            sentReportID = null;
        }
    }

    /**
     * Notification from the ECC that Live monitoring is finishing, so stop pushing
     * metric data.
     * 
     */
    @Override
    public void onStopPushingMetricData()
    {
        pushingAllowed = false;
        
        queueEnablePushOnView();
        queueLogMessage( "ECC signalled stop push" );
    }

    @Override
    public void onPullReportReceived( UUID reportID )
    { /*Not implemented in this demo*/ }

    @Override
    public void onPullMetric( UUID measurementSetID, Report reportOUT )
    {
        /* You should not receive pull requests from the ECC */
        queueLogMessage( "Got pull!?" ); 
    }

    @Override
    public void onPullMetricTimeOut( UUID measurementSetID )
    { /*Not implemented in this demo*/ }

    @Override
    public void onPullingStopped()
    { /*Not implemented in this demo*/ }

    @Override
    public void onPopulateSummaryReport( EMPostReportSummary summaryOUT )
    { /*Not implemented in this demo*/ }

    @Override
    public void onPopulateDataBatch( EMDataBatch batchOut )
    { /*Not implemented in this demo*/ }

    @Override
    public void onReportBatchTimeOut( UUID batchID )
    { /*Not implemented in this demo*/ }

    /**
     * Request from the ECC for a tear-down process to be run for this client.
     * Send the ECC the result of your tear-down activity.
     * 
     * @param resultOUT - OUT parameter represent success (true) or not of your client tear-down
     */
    @Override
    public void onGetTearDownResult( Boolean[] resultOUT )
    {
        queueLogMessage( "Tear-down is completed" );
        resultOUT[0] = true;
    }

    @Override
    public void onTearDownTimeOut()
    { /*Not implemented in this demo*/ }

    // ClientViewListener ------------------------------------------------------
    @Override
    public void onSliderValueChanged( int value )
    {
        // Make sure we're actually connected and in Live Monitoring phase
        if ( connected && pushingAllowed )
        {
            // Make sure we're not waiting for a report acknowledgement from the ECC
            // before sending another report. It is not recommended to send very high
            // frequency data to the ECC. See onPushReportReceived(..) method for
            // acknowledgments
            if ( sentReportID == null ) 
            {
                // Get the measurement we want for this push by using the measurement set ID we stored
                MeasurementSet ms = MetricHelper.getMeasurementSet( generator, measurementSetID );
                
                // Create an empty report ready for adding new measurements
                Report newReport  = MetricHelper.createEmptyMeasurementReport( ms );

                if ( newReport != null )
                {
                    // Create a measurement and add it to our report
                    Measurement sample = new Measurement( Integer.toString(value) );
                    newReport.getMeasurementSet().addMeasurement( sample );

                    // Push the report
                    emiAdapter.pushMetric( newReport );
                    
                    // Remember the ID of the report so we can confirm the ECC got the data
                    sentReportID = newReport.getUUID();
                    
                    queueLogMessage( "Pushed report: " + sentReportID.toString() );
              }
            }  
        }
    }

    /**
     * Connection button click event; either try connecting or disconnecting
     */
    @Override
    public void onConnectionButtonClicked()
    {
        if ( !connected )
        {
            String ipValue = clientView.getServerIPValue();
            connected = tryConnectingToECC( ipValue );
        }
        else
            tryDisconnectingFromECC();
    }

    // Private methods ---------------------------------------------------------
    private boolean tryConnectingToECC( String serverIP )
    {
        // Create connection to Rabbit server ----------------------------------
        amqpFactory.setAMQPHostIPAddress( serverIP );

        try
        {
            queueLogMessage( "Trying to create AMQP connection..." );
            amqpFactory.connectToAMQPHost();
            amqpChannel = amqpFactory.createNewChannel();
            
            queueLogMessage( "...connection created." );
        }
        catch (Exception e )
        {
            // Error message here
            queueLogMessage( "Could not create connection: " + e.getMessage() );
            return false;
        }    

        // Try registering with the ECC
        try
        {
            // Going to used fixed UUID for the ECC server
            UUID expMonitorID = UUID.fromString( "00000000-0000-0000-0000-000000000000" );
            
            // Going to use a random ID representing this client here. You could
            // use a constant one to represent the exact same client connecting/disconnecting over time
            UUID clientID = UUID.randomUUID();

            queueLogMessage( "Trying to register with ECC..." );

            emiAdapter.registerWithEM( "Basic Andrdoid client",
                                       amqpChannel, 
                                       expMonitorID, clientID ); 
        }
        catch ( Exception e ) 
        {
            queueLogMessage( "Could not attempt registration: " + e.getMessage() );
            return false;
        }

        return true;
    }

    private boolean tryDisconnectingFromECC()
    {
        boolean disconnectMsgSent = false;

        try
        {
            // Reset connection state
            connected = false;
            emiAdapter.disconnectFromEM();
            disconnectMsgSent = true;
            
            // Send disconnection messages
            queueLogMessage( "Sent disconnection message..." );
            queueConnectionChangeOnView( UI_DISCONNECTED );
        }
        catch ( Exception e )
        { queueLogMessage( "Could not send disconnection message" ); }

        // Tidy up after if not connected
        if ( amqpChannel != null ) amqpChannel.close();
        if ( amqpFactory != null ) amqpFactory.closeDownConnection();

        return disconnectMsgSent;
    }

    private HashSet<MetricGenerator> createMetricGeneratorSet()
    {
        // Create an entity to observe (the user interface)
        Entity androidUI = new Entity( UUID.randomUUID(),
                                       "Android UI",
                                       "The user interface of the Android client" );

        // Describe something about the entity we are interested in measuring
        Attribute sliderValue 
                = MetricHelper.createAttribute( "Slider value", 
                                                "The value of the slider set by the user", 
                                                androidUI );

        // Describe a metric generator that execute the observations
        generator = new MetricGenerator( UUID.randomUUID(),
                                         "Android UI metric generator",
                                         "Generates metrics associated with the Android UI" );
        
        // Associate the entity with the metric generator
        generator.addEntity( androidUI );

        // Create a group in which measurements will be kept (let's say it has something to do with QoE)
        MetricGroup group = MetricHelper.createMetricGroup( "QoE Interactions",
                                                            "User interactions metric group",
                                                            generator );

        // Create a measurement set in which measurements will be stored; keep the ID
        // of this measurement set so that we can easily retrieve it later
        measurementSetID = MetricHelper.createMeasurementSet( sliderValue, 
                                                              MetricType.RATIO, 
                                                              new Unit( "Slider value" ), 
                                                              group ).getID();

        // Add our metric generator into a collection and we're done
        HashSet<MetricGenerator> mgSet = new HashSet<MetricGenerator>();
        mgSet.add( generator );

        return mgSet;
    }

    // Queues for UI updates ---------------------------------------------------
    // Changes to the UI that arrive from background threads must be queued in
    // the Android platform
    // -------------------------------------------------------------------------
    private void queueLogMessage( String msg )
    {
        Message newMessage = uiMessenger.obtainMessage( UI_LOGMESSAGE, msg );
        newMessage.sendToTarget();
    }

    private void queueEnablePushOnView()
    {
        Message newMessage = uiMessenger.obtainMessage( UI_ENABLE_PUSH, pushingAllowed );
        newMessage.sendToTarget();
    }

    private void queueConnectionChangeOnView( int state )
    {
        Message newMessage = uiMessenger.obtainMessage( state );
        newMessage.sendToTarget();
    }

    // UI update functions (do NOT call directly; you must use queuing)
    // -------------------------------------------------------------------------
    private void echoToLog( String message )
    { 
        if ( message != null ) clientView.addLogText( message );
    }

    private void setPushEnable( boolean enable )
    {
        clientView.setPushEnabled(enable);
    }

    private void setConnectionState( boolean connected )
    {
        if ( connected )
            clientView.setConnectionFunction( false );
        else
        {
            clientView.clearLogText();
            clientView.setPushEnabled( false );
            clientView.addLogText( "Connection to ECC has been reset" );
            clientView.setConnectionFunction( true );
        }
    }

    // Private event handling (executes UI changes based on message state) -----
    private class UIMessenger extends Handler
    {
        public UIMessenger( Looper looper )
        { super( looper ); }

        @Override
        public void handleMessage( Message message )
        {
            switch ( message.what )
            {
                case UI_LOGMESSAGE   : echoToLog( (String) message.obj ); break;

                case UI_ENABLE_PUSH  : setPushEnable( (Boolean) message.obj ); break;

                case UI_CONNECTED    : setConnectionState( true ); break;

                case UI_DISCONNECTED : setConnectionState( false ); break;
            }
        }
    }
}

