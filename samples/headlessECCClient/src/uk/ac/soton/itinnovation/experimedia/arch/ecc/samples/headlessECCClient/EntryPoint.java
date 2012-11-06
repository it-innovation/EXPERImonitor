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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.headlessECCClient;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDMAgent;

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;




public class EntryPoint
{
    public static final Logger clientLogger = Logger.getLogger( EntryPoint.class );
  
    public static void main( String args[] )
    {
        // If have an argument to clear the EDMAgent database, do so, then exit
        if ( args.length == 1 && args[0].equals( "deleteLocalData") )
        {
          Properties edmProps = getProperties( "edm" );
          
          if ( edmProps != null ) deleteLocalData( edmProps );
        }
        
        // Now initialise the client proper
        initialiseClient();
    }
    
    // Private methods ---------------------------------------------------------
    private static Properties getProperties( String configName )
    {
        Properties targetProperties = null;
        InputStream propsStream     = null;
        
        // Look for file on class path first...
        File propFile = new File( configName + ".properties" );
        if ( propFile.exists() )
            try
            { propsStream = (InputStream) new FileInputStream( propFile ); }
            catch ( IOException ioe )
            { clientLogger.error( "Could not open client configuration properties file" ); }
        
        // ... but if it doesn't exist, try pulling the same from internal resources
        if ( propsStream == null ) 
        {
          ClassLoader cl = EntryPoint.class.getClassLoader();
          propsStream = cl.getResourceAsStream( configName + ".properties" );
        }
     
        // If we've got a good stream, try loading it
        if ( propsStream != null )
        {
          targetProperties = new Properties();
          try { targetProperties.load( propsStream ); }
          catch ( IOException ioe )
          { 
            clientLogger.error( "Could not load client configuration properties" );
            targetProperties = null;
          }
        }
        
        // Tidy up
        if ( propsStream != null )
          try 
          { propsStream.close(); }
          catch ( IOException ioe )
          { clientLogger.error( "Could not close client properties stream (and/or file)" ); }
        
        // Return properties
        return targetProperties;
    }
    
    private static boolean deleteLocalData( Properties edmProps )
    {
        try
        {
            IMonitoringEDMAgent edmAgent = EDMInterfaceFactory.getMonitoringEDMAgent( edmProps );
            if ( edmAgent.isDatabaseSetUpAndAccessible() )
              edmAgent.clearMetricsDatabase();

            return true;
        }
        catch ( Exception e )
        { clientLogger.error( "Could not clear EDM data" + e.getMessage() ); }
        
        return false;
    }
    
    private static void initialiseClient()
    {
        // Get some configuration information from somewhere
        Properties emProps       = getProperties( "em");
        Properties edmProps      = getProperties( "edm" );
        ECCHeadlessClient client = new ECCHeadlessClient( "Headless Client " + new Date().toString() );
        
        // Try to set up some data storage within the client -------------------
        if ( edmProps != null )
        {
            if ( client.initialiseLocalDataManagement(edmProps) )
                clientLogger.info( "Successfully created EDMAgent and measurement scheduling" );
            else
                clientLogger.warn( "Could not create local EDMAgent - will continue, but will not schedule/store measurements" );
        }
        
        // Now try to connect to the ECC ---------------------------------------
        if ( emProps != null )
        {
            clientLogger.info( "Trying to connect & register with the ECC Experiment monitor..." );
            
            // First to get a sensible ID
            UUID expMonitorID = null;

            try { expMonitorID = UUID.fromString( emProps.getProperty( "Monitor_ID" ) ); }
            catch ( IllegalArgumentException iae )
            { clientLogger.error( "EM/ECC ID is invalid" ); }

            // Now try to connect and register
            if ( expMonitorID != null )
            try
            {
                client.tryConnectToAMQPBus( emProps );
                
                client.tryRegisteringWithECCMonitor( expMonitorID,
                                                     UUID.randomUUID() );
            }
            catch ( Exception e )
            { clientLogger.error( "Could not attempt registration with EM/ECC: " + e.getMessage() ); }
        }
        else { clientLogger.error( "Could not find client configuration properties" ); }
    }
}
