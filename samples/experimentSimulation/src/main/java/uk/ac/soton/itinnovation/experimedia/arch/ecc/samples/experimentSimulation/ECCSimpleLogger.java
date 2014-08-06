/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2014
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
//      Created Date :          23-Jul-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.experimentSimulation;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared.*;

import java.util.*;
import org.slf4j.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvReport;



/**
 * Use this class to connect to the ECC and push metrics with a simple metric model
 *
 * @author Simon Crowle
 */
public class ECCSimpleLogger
{
    private static final Logger sLog = LoggerFactory.getLogger(ECCSimpleLogger.class);

    private AMQPBasicChannel		amqpChannel;
    private EMInterfaceAdapter		emAdapter;
    private EMIAListener			emAdapterListener;
	public MetricGenerator			metricGenerator;
	private ExperimentDataGenerator provGenerator;
    private boolean					connectedToECC;
    private boolean					readyToPush;
    private boolean					shuttingDown;


    public ECCSimpleLogger()
    {}

    /**
     * Initialises the logger with basic parameters to allow connection to the ECC
     *
     * @param clientName - Name of the client reported to the ECC
     * @param emProps    - EM properties used to connect to the ECC
     * @param metGen     - Metric Generator representing the metric model
     * @throws Exception - throws if input parameters are incorrect; if already connected to ECC or shutting down
     */
    public void initialise( String clientName,
                            Properties emProps,
                            MetricGenerator metGen ) throws Exception
    {
        // Safety first
        if ( emProps == null || metGen == null ) throw new Exception( "Could not initialise: input param(s) invalid" );
        if ( connectedToECC ) throw new Exception( "Could not initialise: already connected to ECC" );
        if ( shuttingDown ) throw new Exception( "Could not initialise: shutting down" );

        metricGenerator = metGen;
        readyToPush = false;

        // Try connecting to the ECC
        try
        {
            sLog.info( "Connecting to EXPERIMonitor" );

            // AMQP bits
            AMQPConnectionFactory connectFactory = new AMQPConnectionFactory();
            connectFactory.connectToAMQPHost( emProps );
            amqpChannel = connectFactory.createNewChannel();

            // ECC interface adapter
            emAdapterListener = new EMIAListener();
            emAdapter         = new EMInterfaceAdapter( emAdapterListener );

            // Try registering with ECC
            UUID emID =  UUID.fromString( (String) emProps.get("Monitor_ID") );
            emAdapter.registerWithEM( clientName, amqpChannel,
                                      emID, UUID.randomUUID() );
        }
        catch ( Exception ex )
        { sLog.error( "Could not connect to EM: " + ex.getMessage()); }
    }

    /**
     * Use this method to shutdown the connection with the ECC
     */
    public void shutdown()
    {
        // Only try shutting down if we're connected and not doing so already
        if ( connectedToECC && !shuttingDown )
        {
            shuttingDown = true;

            try
            {
                sLog.info( "Disconnecting from EXPERIMonitor" );
                emAdapter.disconnectFromEM();
            }
            catch ( Exception ex )
            {
                String problem = "Problem disconnecting from EXPERIMonitor: " + ex.getMessage();
                sLog.error( problem );
            }

        }
        else
            sLog.info( "Not connected to the ECC: no shutdown action performed" );

        sLog.info( "Shutting down" );
        emAdapter = null;
        amqpChannel = null;
    }

    /**
     * Use this method to see if the logger is ready to push data to the ECC
     *
     * @return - returns true if the logger is ready
     */
    public boolean isReadyToPush()
    {
        return readyToPush;
    }

    /**
     * Use this method to push a single metric sample to the EXPERIMonitor
     *
     * @param entityName    - Name of the entity the metric belongs to
     * @param attributeName - Name of the attribute the metric value represents
     * @param value         - The actual measurement value (in string format)
     * @throws Exception    - Throws if not ready to push; the input parameters invalid; or the Entity/Attribute cannot be found in the metric model
     */
    public synchronized void pushMetric( String entityName, String attributeName, String value ) throws Exception
    {
        // Safety first
        if ( !readyToPush )
        {
            String err = "Cannot push metric data: ECC/client connection not ready";

            sLog.error( err );
            throw new Exception( err );
        }

        if ( entityName == null || attributeName == null || value == null )
        {
            String err = "Could not push metric data: input parameter(s) invalid";

            sLog.error( err );
            throw new Exception( err );
        }

        Entity entity = MetricHelper.getEntityFromName( entityName, metricGenerator );
        if ( entity == null )
        {
            String err = "Could not push metric data: entity not found";

            sLog.error( err );
            throw new Exception( err );
        }

        Attribute attr = MetricHelper.getAttributeByName( attributeName, entity );
        if ( attr == null )
        {
            String err = "Could not push metric data: attribute not found";

            sLog.error( err );
            throw new Exception( err );
        }

        MeasurementSet ms = MetricHelper.getMeasurementSetForAttribute( attr, metricGenerator );
        if ( ms == null )
        {
            String err = "Could not push metric data: measurement set not found";

            sLog.error( err );
            throw new Exception( err );
        }

        Measurement m = new Measurement();
        m.setValue( value );

        Report report = MetricHelper.createMeasurementReport( ms, m );

        emAdapter.pushMetric( report );

        try
        {
            wait();
        }
        catch( InterruptedException ie )
        { sLog.info( "Push acknowledgement interrupted" ); }
    }

	public synchronized void pushProv(EDMProvReport report) {
		emAdapter.pushPROVStatement(report);
	}

    // Private methods/classes -------------------------------------------------
    private synchronized void pushedReportReceived()
    {
        sLog.info( "Pushed reported received" );
        notifyAll();
    }

    private void disconnectConfirmed()
    {
        shuttingDown = false;
        connectedToECC = false;
    }

    // EMIAdapterListener ------------------------------------------------------
    /**
     * This class handles ECC events (generated by the EMIAdapter)
     *
     */
    private class EMIAListener implements EMIAdapterListener
    {
        @Override
        public void onEMConnectionResult( boolean connected, Experiment expInfo )
        {
            if ( connected )
            {
                sLog.info( "Connected to ECC OK. Experiment name: " + expInfo.getName() );
                connectedToECC = true;
            }
            else
            {
                sLog.info( "Connection to ECC Refused" );
                connectedToECC = false;
            }
        }

        @Override
        public void onEMDeregistration( String reason )
        {
            sLog.info( "Got disconnected from ECC: " + reason );
            disconnectConfirmed();
        }

        @Override
        public void onDescribeSupportedPhases( EnumSet<EMPhase> phasesOUT )
        {
            sLog.info( "Describing phase support for ECC" );
            phasesOUT.add( EMPhase.eEMLiveMonitoring );
        }

        @Override
        public void onDescribePushPullBehaviours( Boolean[] pushPullOUT )
        {
            sLog.info( "Describing push/pull support for ECC" );
            pushPullOUT[0] = true;  // Push only
            pushPullOUT[1] = false; // NO pulling
        }

        @Override
        public void onPopulateMetricGeneratorInfo()
        {
            sLog.info( "Sending metric model to ECC" );

            HashSet<MetricGenerator> metGens = new HashSet<MetricGenerator>();
            metGens.add( metricGenerator );
            emAdapter.sendMetricGenerators( metGens );
        }

        @Override
        public void onDiscoveryTimeOut() {}

        @Override
        public void onSetupMetricGenerator( UUID metricGeneratorID, Boolean[] resultOUT ){}

        @Override
        public void onSetupTimeOut( UUID metricGeneratorID ) {}

        @Override
        public void onLiveMonitoringStarted()
        {
            sLog.info( "Live monitoring started" );
        }

        @Override
        public void onStartPushingMetricData()
        {
            sLog.info( "Ready to start pushing data" );
            readyToPush = true;
        }

        @Override
        public void onPushReportReceived( UUID lastReportID )
        {
            pushedReportReceived();
        }

        @Override
        public void onStopPushingMetricData()
        {
            sLog.info( "Stopping metric push" );
            readyToPush = false;
        }

        @Override
        public void onPullReportReceived( UUID reportID ) {}

        @Override
        public void onPullMetric( UUID measurementSetID, Report reportOUT ) {}

        @Override
        public void onPullMetricTimeOut( UUID measurementSetID ) {}

        @Override
        public void onPullingStopped() {}

        @Override
        public void onPopulateSummaryReport( EMPostReportSummary summaryOUT ) {}

        @Override
        public void onPopulateDataBatch( EMDataBatch batchOut ) {}

        @Override
        public void onReportBatchTimeOut( UUID batchID ) {}

        @Override
        public void onGetTearDownResult( Boolean[] resultOUT ) {}

        @Override
        public void onTearDownTimeOut() {}
    }
}
