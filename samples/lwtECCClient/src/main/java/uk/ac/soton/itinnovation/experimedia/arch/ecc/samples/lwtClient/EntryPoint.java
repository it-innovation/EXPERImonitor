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
	private static PeriodicMetricLogParserTool logTool;

	private static MetricGenerator metGen;

	private static MeasurementSet ResponseTime;
	private static MeasurementSet CPUUsage;
	private static MeasurementSet MemoryUsage;

    // Main entry point
    public static void main( String args[] )
    {
		// Get EXPERIMonitor EM properties to connect to service (in src/main/resources)
        Properties emProps = Utilitybox.getProperties( EntryPoint.class, "em" );

        // Create a very simple metric model
        MetricGenerator	metGen  = createSimpleModel();

        // Create a simple ECC Logger
        ECCSimpleLogger	eccLogger = new ECCSimpleLogger();

		//Create Log tool
		logTool = new PeriodicMetricLogParserTool();

        // Try connecting to the ECC...
        try
        {
            logger.info( "Starting Lift Waiting Time client..." );
            eccLogger.initialise( "Lift waiting time client", emProps, metGen );
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

        // Start sending data
		boolean lwtServiceRunning = true;

		while ( lwtServiceRunning )
		{
			//check for break:
			if (logTool.hasFinished) {
				logger.info("Stopping now");
				lwtServiceRunning = false;
				break;
			}

			try {
				// Do cool simulation codey bit & push whole report to ECC using Simon's new and shiny method
				eccLogger.pushSimpleMetric("LWTService", "Response time", logTool.createReport(ResponseTime, metGen, 60));
				eccLogger.pushSimpleMetric("LWTService", "CPU usage", logTool.createReport(CPUUsage, metGen, 60));
				eccLogger.pushSimpleMetric("LWTService", "Memory usage", logTool.createReport(MemoryUsage, metGen, 60));
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
		 // A simple entity
        Entity e = new Entity();
        e.setName( "LWTService" );
		e.setEntityID("http://experimedia.eu/ontologies/ExperimediaExperimentExplorer#entity_lwtService");
        e.setDescription( "Lift waiting time service" );
        metGen.addEntity( e );

        // A simple attribute
        Attribute a = MetricHelper.createAttribute( "Average response time", "The Server's response time", e );
		Attribute b = MetricHelper.createAttribute( "CPU usage", "The Server's CPU usage in percent", e );
		Attribute c = MetricHelper.createAttribute( "Memory usage", "The Server's memory usage in percent", e );

        // A measurement set associated with the attribute
        ResponseTime = MetricHelper.createMeasurementSet( a, MetricType.RATIO,
                                           new Unit( "ms" ),
                                           group );

		CPUUsage = MetricHelper.createMeasurementSet( b, MetricType.RATIO,
                                           new Unit( "%" ),
                                           group );

		MemoryUsage = MetricHelper.createMeasurementSet( c, MetricType.RATIO,
                                           new Unit( "%" ),
                                           group );

        return metGen;
    }
}
