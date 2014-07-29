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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared.Utilitybox;

import java.util.*;
import org.slf4j.*;




public class EntryPoint
{
    private static final Logger epLog = LoggerFactory.getLogger(EntryPoint.class);

    public static void main(String[] args)
    {
        // Get EXPERIMonitor EM properties to connect to service (in src/main/resources)
        Properties      emProps = Utilitybox.getProperties( EntryPoint.class, "em" );
        
        // Create a very simple metric model
        MetricGenerator metGen  = createSimpleModel();
        
        // Create a simple ECC Logger
        ECCSimpleLogger eccLogger = new ECCSimpleLogger();
        
        // Try connecting to the ECC...
        try
        {
            epLog.info( "Starting Experiment simulation client..." );
            eccLogger.initialise( "Experiment simulation", emProps, metGen );
        }
        catch ( Exception ex )
        {
            // Notify if failed
            String msg = "Could not start ECC logger: " + ex.getMessage();
            epLog.error( msg, ex );
        }
        
        // Press 'q' to exit...
        epLog.info( "Press 'q' key to exit demo" );
        boolean running = true;
        
        // Start simulation loop
        while ( running )
        {
            // Get an input from the keyboard
            try
            {
                if ( System.in.read() == 113 )
                    running = false;
                
                // Send a metric (if we are ready to do so)
                if ( eccLogger.isReadyToPush() )
                {
                    try
                    {
                        // Create a time-stamp
                        long timeStamp = new Date().getTime();
                        
                        // Push the metric (referring to Entity and its attribute)
                        eccLogger.pushMetric( "Simulation service", 
                                              "Last push",
                                              Long.toString(timeStamp) );
                    }
                    catch ( Exception ex )
                    {
                        // Catch & log problems
                        epLog.error( "Failed to send metric: " + ex.getMessage() ); 
                    }
                }
            }
            catch (IOException ioe)
            {
                // Yikes! Is there a keyboard available?
                epLog.error( "Could not read keyboard" );
                running = false;
            }
        }
        
        // Clean up
        eccLogger.shutdown();
        
        epLog.info( "Finishing up demo" );
        
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
        
        // A simple entity
        Entity entity = new Entity();
        entity.setName( "Simulation service" );
        entity.setDescription( "Simple entity for demo purposes only" );
        metGen.addEntity( entity );
        
        // A simple attribute
        Attribute attr = MetricHelper.createAttribute( "Last push", "Time of last push", entity );
        
        // A measurement set associated with the attribute
        MetricHelper.createMeasurementSet( attr, MetricType.RATIO,
                                           new Unit( "milliseconds" ),
                                           group );
        
        return metGen;
    }
}
