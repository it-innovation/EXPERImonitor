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
//      Created Date :          10-Apr-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

using uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor;

using System.Collections.Generic;
using System.Diagnostics;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.basicDotNetClient
{

public class ECCClientController : EMIAdapterListener
{
    private log4net.ILog clientLogger;

    private AMQPBasicChannel   amqpChannel;
    private EMInterfaceAdapter emiAdapter;
    private string             clientName;

    delegate Measurement ITakeMeasurement();
    private  Dictionary<System.Guid, ITakeMeasurement> samplers;

    public ECCClientController()
    {
        // Configure logging system
        clientLogger = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
    }
    
    public void start( string rabbitServerIP,
                       string username,
                       string password,
                       System.Guid expMonitorID,
                       System.Guid clientID )
    {
        if ( rabbitServerIP != null &&
             expMonitorID   != null &&
             clientID       != null )
        {
            clientLogger.Info( "Trying to connect to Rabbit server on " + rabbitServerIP );

            // Create connection to Rabbit server -------------------------------
            AMQPConnectionFactory amqpFactory = new AMQPConnectionFactory();
            amqpFactory.setAMQPHostIPAddress( rabbitServerIP );
            try
            {
                if (username != null && password != null)
                    amqpFactory.setLoginDetails(username, password);

                amqpFactory.connectToAMQPHost();
                amqpChannel = amqpFactory.createNewChannel();
            }
            catch (System.Exception e ) 
            {
                clientLogger.Error( "Could not connect to Rabbit server" );
                throw e; 
            }

            // Create a simple name
            System.DateTime date = System.DateTime.Now;
            clientName = "C# Client (" + date.ToString() + ")";

            // Create EM interface adapter, listen to it...
            emiAdapter = new EMInterfaceAdapter( this );

            // ... and try registering with the ECC -----------------------------
            try { emiAdapter.registerWithEM( clientName,
                                             amqpChannel, 
                                             expMonitorID, clientID ); }
            catch ( System.Exception e ) 
            {
                clientLogger.Error( "Could not attempt registration with ECC" );
                throw e; 
            }
        }
    }

    public void stop()
    {
        if (emiAdapter != null) emiAdapter.disconnectFromEM();
    }

    // EMIAdapterListener --------------------------------------------------------
    public void onEMConnectionResult( bool connected, Experiment expInfo )
    {
        if ( connected )
        {
          clientLogger.Info( "Connected to EM" );
          clientLogger.Info( "Linked to experiment: " + expInfo.name );
        }
        else
          clientLogger.Info( "Refused connection to EM" );
    }

    public void onEMDeregistration( string reason )
    {
        clientLogger.Info( "Got disconnected from EM: " + reason );
        
        try
        { emiAdapter.disconnectFromEM(); }
        catch ( System.Exception e )
        { clientLogger.Error( "Had problems disconnecting from EM: " + e.Message ); }
        
        // Apologise to the user
        clientLogger.Info( "ECC disconnected this client: " + reason );

        System.Environment.Exit(0);
    }
    
    public void onDescribeSupportedPhases( HashSet<EMPhase> phasesREF )
    {
        // We are going to just support live monitoring
        // ... we MUST support the discovery phase by default, but don't need to include

        clientLogger.Info("Specifying phase compatibility (Live monitoring only)");
        phasesREF.Add( EMPhase.eEMLiveMonitoring );
    }
    
    public void onDescribePushPullBehaviours( ref bool[] pushPullREF )
    {
        // We're going to support both push and pull
        clientLogger.Info("Specifying only pull behaviour");

        pushPullREF[0] = false;  // In this demo we are just going to be pull
        pushPullREF[1] = true;   // ECC will pull metrics from this client
    }
    
    public void onPopulateMetricGeneratorInfo()
    {
        clientLogger.Info("Creating metric generator info...");

        // Describe what we are going measure
        Entity app = new Entity( System.Guid.NewGuid(),
                                 "Client app",
                                 "The thing we are observing");

        Attribute phyRAMUage = MetricHelper.createAttribute( "Physical memory usage",
                                                             "Physical memory used by the app",
                                                             app );

        Attribute virtualMEMUsage = MetricHelper.createAttribute( "Virtual memory usage",
                                                                  "Virtual memory used by the app",
                                                                  app );

        // Create a data structure in which to put measurements
        MetricGenerator mGen = new MetricGenerator( System.Guid.NewGuid(),
                                                    "Client metric generator",
                                                    "The only metric generator for this client" );
        // Associate an entity with the generator
        mGen.addEntity(app);

        // Create a group of metric relating to memory
        MetricGroup group = MetricHelper.createMetricGroup( "Memory metrics",
                                                            "Memory metrics relating to the app",
                                                            mGen );

        // Create measurment sets for our two metrics (and use Guids to point to sampling methods)
        System.Guid physMem_ID = MetricHelper.createMeasurementSet( phyRAMUage,
                                                                    MetricType.RATIO,
                                                                    new Unit("bytes"),
                                                                    group )
                                                                    .msetID;

        System.Guid virtualMem_ID = MetricHelper.createMeasurementSet( virtualMEMUsage,
                                                                       MetricType.RATIO,
                                                                       new Unit("bytes"),
                                                                       group )
                                                                       .msetID;

        // Point to sampling delegates for each metric
        samplers = new Dictionary<System.Guid, ITakeMeasurement>();
        samplers.Add(physMem_ID,    new ITakeMeasurement(samplePhysicalMEM));
        samplers.Add(virtualMem_ID, new ITakeMeasurement(sampleVirtualMEM));

        // Describe what we have created on the console
        clientLogger.Info(MetricHelper.describeGenerator(mGen));

        // Wrap up metric generators and send to the ECC
        HashSet<MetricGenerator> mgSet = new HashSet<MetricGenerator>();
        mgSet.Add(mGen);

        emiAdapter.setMetricGenerators(mgSet);        
    }

    public void onDiscoveryTimeOut()
    { clientLogger.Info( "Got discovery time-out message" ); }
    
    public void onSetupMetricGenerator( System.Guid genID, ref bool[] resultREF )
    { /*Not implemented in this demo */ }
    
    public void onSetupTimeOut( System.Guid metricGeneratorID )
    { /*Not implemented in this demo */ }
    
    public void onLiveMonitoringStarted()
    {
        clientLogger.Info( "ECC has started Live Monitoring process" );
    }

    public void onStartPushingMetricData()
    {
        // Allow the human user to manually push some data
        clientLogger.Info( "Enabling metric push" );
    }

    public void onPushReportReceived( System.Guid reportID )
    { /*Not implemented in this demo */ }
    
    public void onPullReportReceived( System.Guid reportID )
    {
    }

    public void onPullMetricTimeOut( System.Guid measurementSetID )
    { clientLogger.Info( "Got live pull time-out message" ); }

    public void onStopPushingMetricData()
    {
        clientLogger.Info( "Disabling metric push" );
    }

    /*
    * Note that 'reportREF' is an out-style parameter provided by the adapter
    */
    public void onPullMetric( System.Guid measurementSetID, Report reportREF )
    {
        if (measurementSetID != null)
        {
            ITakeMeasurement sampler = samplers[measurementSetID];

            if (sampler != null)
            {
                Measurement m = sampler();
                reportREF.measurementSet.addMeasurement(m);
                reportREF.numberOfMeasurements = 1;

                clientLogger.Info("Send pull measurement for " + measurementSetID.ToString());
            }
            else clientLogger.Info("Got a pull, but could not find sampler for measurement requested");
        }
        else clientLogger.Info("Got a pull without a valid guid.");
    }

    public void onPullingStopped()
    {
        clientLogger.Info( "ECC has stopped pulling" );
    }
    
    /*
    * Note that the summaryOUT parameter is an OUT parameter supplied by the
    * adapter
    */
    public void onPopulateSummaryReport( EMPostReportSummary summaryREF )
    { /* Not implemented in this demo */ }

    public void onPopulateDataBatch( EMDataBatch batchREF )
    { /* Not implemented in this demo */ }
    
    public void onReportBatchTimeOut( System.Guid batchID )
    { /* Not implemented in this demo */ }

    public void onGetTearDownResult( ref bool[] resultREF )
    { /* Not implemented in this demo */ }

    public void onTearDownTimeOut()
    { /* Not implemented in this demo */ }

    // Private methods -----------------------------------------------------------
    private Measurement samplePhysicalMEM()
    {
        Measurement m = new Measurement();
        Process proc  = System.Diagnostics.Process.GetCurrentProcess();
        m.value       = proc.WorkingSet64.ToString();
        
        return m;
    }

    private Measurement sampleVirtualMEM()
    {
        Measurement m = new Measurement();
        Process proc  = System.Diagnostics.Process.GetCurrentProcess();
        m.value       = proc.VirtualMemorySize64.ToString();

        return m;
    }
}

} // namespace