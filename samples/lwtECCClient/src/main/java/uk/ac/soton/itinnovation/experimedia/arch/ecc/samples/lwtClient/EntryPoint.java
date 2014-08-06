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

import java.util.Date;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricHelper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricType;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Unit;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared.Utilitybox;


public class EntryPoint
{
	public static final Logger logger = LoggerFactory.getLogger(EntryPoint.class);
	private static MeasurementSet ms;

    // Main entry point
    public static void main( String args[] )
    {
		// Get EXPERIMonitor EM properties to connect to service (in src/main/resources)
        Properties emProps = Utilitybox.getProperties( EntryPoint.class, "em" );

        // Create a very simple metric model
        MetricGenerator	metGen  = createSimpleModel();

        // Create a simple ECC Logger
        ECCSimpleLogger	eccLogger = new ECCSimpleLogger();

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

		boolean lwtServiceRunning = true;

		while ( lwtServiceRunning )
		{

			// Do cool simulation codey bit
			Report report = new Report();
			report.setMeasurementSet(ms);
			eccLogger.logTool.createReport(report, 60);
			
			//TODO: push whole report to ECC using Simon's new and shiny method
		}
    }

	private static MetricGenerator createSimpleModel()
    {
        // Metric Generator
        MetricGenerator metGen = new MetricGenerator();
        metGen.setName( "Lift waiting time metric generator" );
        metGen.setDescription("Created: " + (new Date()).toString() );

        // A simple metric group belonging to the metric generator
        MetricGroup group = MetricHelper.createMetricGroup( "Demo group", "Data set for demo", metGen );

		//Prov-metric link
		 // A simple entity
        Entity e = new Entity();
        e.setName( "LWTService" );
        e.setDescription( "Lift waiting time service" );
        metGen.addEntity( e );

        // A simple attribute
        Attribute a = MetricHelper.createAttribute( "Response time", "The Server's response time", e );

        // A measurement set associated with the attribute
        ms = MetricHelper.createMeasurementSet( a, MetricType.RATIO,
                                           new Unit( "milliseconds" ),
                                           group );

        return metGen;
    }
}
