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
//      Created Date :          04-Oct-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.lwtClient;

import java.util.*;
import org.slf4j.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared.*;




public class EntryPoint
{
	private static final Logger logger = LoggerFactory.getLogger(EntryPoint.class);
	private static PeriodicMetricLogParserTool lwtLogTool;
	private static PeriodicMetricLogParserTool generalServiceLogTool;

	private static MetricGenerator metGen;

	private static Entity vas;
	private static Entity ts;
	private static Entity ws;

	private static MeasurementSet VASResponseTime;
	private static MeasurementSet VASCPUUsage;
	private static MeasurementSet VASMemoryUsage;

	private static MeasurementSet TSResponseTime;
	private static MeasurementSet TSCPUUsage;
	private static MeasurementSet TSMemoryUsage;

	private static MeasurementSet WSResponseTime;
	private static MeasurementSet WSCPUUsage;
	private static MeasurementSet WSMemoryUsage;

    // Main entry point
    public static void main( String args[] )
    {
		// Get EXPERIMonitor EM properties to connect to service (in src/main/resources)
        Properties emProps = Utilitybox.getProperties( EntryPoint.class, "em" );

        // Create vas_rt very simple metric model
        MetricGenerator	metGen  = createSimpleModel();

        // Create vas_rt simple ECC Logger
        ECCSimpleLogger	eccLogger = new ECCSimpleLogger();

        // Try connecting to the ECC...
        try
        {
            logger.info( "Starting service metrics client..." );
            eccLogger.initialise( "Lift service metrics client", emProps, metGen );
        }
        catch ( Exception ex )
        {
            // Notify if failed
            String msg = "Could not start ECC logger: " + ex.getMessage();
            logger.error( msg, ex );
        }

        // Sleep until we are ready
        while ( !eccLogger.isReadyToPush() ) {
            try {
                logger.info("ZZzZzzzzzzZZZzzZz");
                Thread.sleep(1000);
            }
            catch ( InterruptedException ex )
            {
                logger.info("Ready to send data!");
            }
        }

		//VAS//////////////////////////////////////////////////////////////////////////////////////
		lwtLogTool = new PeriodicMetricLogParserTool("lwt.txt", vas);
        // Start sending data
		boolean lwtServiceRunning = true;

		while ( lwtServiceRunning )
		{
			//check for break:
			if (lwtLogTool.hasFinished) {
				logger.info("Stopping now");
				lwtServiceRunning = false;
				break;
			}

			try {
				// Do cool simulation codey bit & push whole report to ECC using Simon's new and shiny method
				eccLogger.pushSimpleMetric("LWTService", "Average response time", lwtLogTool.createReport(VASResponseTime, metGen, 60));
				eccLogger.pushSimpleMetric("LWTService", "CPU usage", lwtLogTool.createReport(VASCPUUsage, metGen, 60));
				eccLogger.pushSimpleMetric("LWTService", "Memory usage", lwtLogTool.createReport(VASMemoryUsage, metGen, 60));
			} catch (Exception e) {
				logger.error("Error pushing metric", e);
			}
		}

		// twitter service/////////////////////////////////////////////////////////////////////////
		generalServiceLogTool = new PeriodicMetricLogParserTool("standardService.txt", ts);
		// Start sending data
		boolean twitterServiceRunning = true;

		while ( twitterServiceRunning )
		{
			//check for break:
			if (generalServiceLogTool.hasFinished) {
				logger.info("Stopping now");
				twitterServiceRunning = false;
				break;
			}

			try {
				// Do cool simulation codey bit & push whole report to ECC using Simon's new and shiny method
				eccLogger.pushSimpleMetric("TwitterService", "Average response time", generalServiceLogTool.createReport(TSResponseTime, metGen, 60));
				eccLogger.pushSimpleMetric("TwitterService", "CPU usage", generalServiceLogTool.createReport(TSCPUUsage, metGen, 60));
				eccLogger.pushSimpleMetric("TwitterService", "Memory usage", generalServiceLogTool.createReport(TSMemoryUsage, metGen, 60));
			} catch (Exception e) {
				logger.error("Error pushing metric", e);
			}
		}

		// weather service ////////////////////////////////////////////////////////////////////////
		generalServiceLogTool = new PeriodicMetricLogParserTool("standardService.txt", ws);
		// Start sending data
		boolean weatherServiceRunning = true;

		while ( weatherServiceRunning )
		{
			//check for break:
			if (generalServiceLogTool.hasFinished) {
				logger.info("Stopping now");
				weatherServiceRunning = false;
				break;
			}

			try {
				// Do cool simulation codey bit & push whole report to ECC using Simon's new and shiny method
				eccLogger.pushSimpleMetric("WeatherService", "Average response time", generalServiceLogTool.createReport(WSResponseTime, metGen, 60));
				eccLogger.pushSimpleMetric("WeatherService", "CPU usage", generalServiceLogTool.createReport(WSCPUUsage, metGen, 60));
				eccLogger.pushSimpleMetric("WeatherService", "Memory usage", generalServiceLogTool.createReport(WSMemoryUsage, metGen, 60));
			} catch (Exception e) {
				logger.error("Error pushing metric", e);
			}
		}

        eccLogger.shutdown();

		logger.info("Done!");

        System.exit( 0 );
    }

	private static MetricGenerator createSimpleModel()
    {
        // Metric Generator
        metGen = new MetricGenerator();
        metGen.setName( "Lift waiting time metric generator" );
        metGen.setDescription("Created: " + (new Date()).toString() );

        // A simple metric group belonging to the metric generator
        MetricGroup group = MetricHelper.createMetricGroup( "Demo group", "Data set for demo", metGen );

		//Prov-metric link

		// Service entities
        vas = new Entity();
        vas.setName( "LWTService" );
		vas.setEntityID("http://experimedia.eu/ontologies/ExperimediaExperimentExplorer#entity_lwtService");
        vas.setDescription( "Lift waiting time service" );
        metGen.addEntity( vas );

		ts = new Entity();
        ts.setName( "TwitterService" );
		ts.setEntityID("http://experimedia.eu/ontologies/ExperimediaExperimentExplorer#entity_twitterService");
        ts.setDescription( "Lift waiting time service" );
        metGen.addEntity( ts );

		ws = new Entity();
        ws.setName( "WeatherService" );
		ws.setEntityID("http://experimedia.eu/ontologies/ExperimediaExperimentExplorer#entity_weatherService");
        ws.setDescription( "Lift waiting time service" );
        metGen.addEntity( ws );

        // attributes
        Attribute vas_rt = MetricHelper.createAttribute( "Average response time", "The Server's response time", vas );
		Attribute vas_cpu = MetricHelper.createAttribute( "CPU usage", "The Server's CPU usage in percent", vas );
		Attribute vas_mem = MetricHelper.createAttribute( "Memory usage", "The Server's memory usage in percent", vas );

		Attribute ts_rt = MetricHelper.createAttribute( "Average response time", "The Server's response time", ts );
		Attribute ts_cpu = MetricHelper.createAttribute( "CPU usage", "The Server's CPU usage in percent", ts );
		Attribute ts_mem = MetricHelper.createAttribute( "Memory usage", "The Server's memory usage in percent", ts );

		Attribute ws_rt = MetricHelper.createAttribute( "Average response time", "The Server's response time", ws );
		Attribute ws_cpu = MetricHelper.createAttribute( "CPU usage", "The Server's CPU usage in percent", ws );
		Attribute ws_mem = MetricHelper.createAttribute( "Memory usage", "The Server's memory usage in percent", ws );

        // measurement sets associated with the attributes
        VASResponseTime = MetricHelper.createMeasurementSet( vas_rt, MetricType.RATIO,
                                           new Unit( "ms" ),
                                           group );

		VASCPUUsage = MetricHelper.createMeasurementSet( vas_cpu, MetricType.RATIO,
                                           new Unit( "%" ),
                                           group );

		VASMemoryUsage = MetricHelper.createMeasurementSet( vas_mem, MetricType.RATIO,
                                           new Unit( "%" ),
                                           group );



		TSResponseTime = MetricHelper.createMeasurementSet( ts_rt, MetricType.RATIO,
                                           new Unit( "ms" ),
                                           group );

		TSCPUUsage = MetricHelper.createMeasurementSet( ts_cpu, MetricType.RATIO,
                                           new Unit( "%" ),
                                           group );

		TSMemoryUsage = MetricHelper.createMeasurementSet( ts_mem, MetricType.RATIO,
                                           new Unit( "%" ),
                                           group );



		WSResponseTime = MetricHelper.createMeasurementSet( ws_rt, MetricType.RATIO,
                                           new Unit( "ms" ),
                                           group );

		WSCPUUsage = MetricHelper.createMeasurementSet( ws_cpu, MetricType.RATIO,
                                           new Unit( "%" ),
                                           group );

		WSMemoryUsage = MetricHelper.createMeasurementSet( ws_mem, MetricType.RATIO,
                                           new Unit( "%" ),
                                           group );

        return metGen;
    }
}
