
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

import java.io.IOException;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared.*;

import java.util.*;
import org.slf4j.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvReport;


public class EntryPoint
{
    private static final Logger logger = LoggerFactory.getLogger(EntryPoint.class);

    public static void main(String[] args)
    {
        // Get EXPERIMonitor EM properties to connect to service (in src/main/resources)
        Properties emProps = Utilitybox.getProperties( EntryPoint.class, "em" );

        // Create a very simple metric model
        MetricGenerator			metGen  = createSimpleModel();

        // Create a simple ECC Logger
        ECCSimpleLogger			eccLogger = new ECCSimpleLogger();

        // Try connecting to the ECC...
        try
        {
            logger.info( "Starting Experiment simulation client..." );
            eccLogger.initialise( "Experiment simulation", emProps, metGen );
        }
        catch ( Exception ex )
        {
            // Notify if failed
            String msg = "Could not start ECC logger: " + ex.getMessage();
            logger.error( msg, ex );
        }

		//Create a prov experiment data generator
		ExperimentDataGenerator provGen = createProvGen(args, eccLogger);

        // Press 'q' to exit...
        //logger.info( "Press 'q' key to exit demo" );
        boolean running = true;

        // Start simulation loop
        while ( running )
        {
            // Get an input from the keyboard
            try
            {
                // Send a metric (if we are ready to do so)
                if ( provGen.getEccLogger().isReadyToPush() )
                {
					logger.debug("ready to push");
                    try
                    {

						//push prov
						if (provGen.processNextLog()) {
							EDMProvReport report = provGen.getFactory().getProvFactory().createProvReport();
							logger.debug("Processed log line:\n" + provGen.getCurrentLog().toString() + "\nCurrent triples:\n"
									+ report.toString());
							provGen.getEccLogger().pushProv(report);
						} else {
							logger.info("End of log reached, stopping client");
							running = false;
						}

                    }
                    catch ( Exception ex )
                    {
                        // Catch & log problems
                        logger.error( "Failed to send metric: " + ex.getMessage() );
                    }
                } else {
					logger.debug("NOT ready to push");
					Thread.sleep(1000);
				}
            }
			catch (Throwable t)
            //catch (IOException ioe)
            {
                // Yikes! Is there a keyboard available?
                logger.error( "Could not read keyboard" );
                running = false;
            }
        }
        
        // Wait a short period before shutting down (final pushes may need processing)
        try { Thread.sleep(1000); } 
        catch ( Throwable t )
        { logger.info( "Shutting down now..." ); }

        // Clean up
        eccLogger.shutdown();

        logger.info( "Finishing up demo" );

        System.exit( 0 );
    }

    private static MetricGenerator createSimpleModel()
    {
        // Metric Generator
        MetricGenerator metGen = new MetricGenerator();
        metGen.setName( "Experiment Simulator metric generator" );
        metGen.setDescription("Created: " + (new Date()).toString() );

        // A simple metric group belonging to the metric generator
        MetricGroup group = MetricHelper.createMetricGroup( "Demo group", "Data set for demo", metGen );

        // That's all for now - will create Entities representing participants later

        return metGen;
    }

	private static ExperimentDataGenerator createProvGen(String[] args, ECCSimpleLogger eccLogger) {
		ExperimentDataGenerator provGen = new ExperimentDataGenerator();

		String logfile = null;
		if (args.length>0) {
			logfile = args[0];
			logger.info("Using logfile " + logfile);
		} else {
			logger.info("No logfile given");
		}

		provGen.init(logfile, "PerfectLog", eccLogger);

		return provGen;
	}
}
