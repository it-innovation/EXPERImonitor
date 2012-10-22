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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.headerlessECCClient;

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;




public class EntryPoint
{
    public static final Logger clientLogger = Logger.getLogger( EntryPoint.class );
  
    public static void main( String args[] )
    {
        // Get some configuration information from somewhere
        Properties emProps  = getProperties( "em");
        Properties edmProps = getProperties( "edm" );
        
        if ( emProps != null )
        {
              ECCHeaderlessClient client = new ECCHeaderlessClient( "Headerless Client " + new Date().toString() );

              // Try connecting to AMQP bus
              if ( connectToAMQPBus( client, emProps ) )
              {
                  // Attempt to register with the Experiment Monitor
                  if ( tryRegisterWithECCMonitor( client, emProps, edmProps ) )
                      clientLogger.info( "Successfully attempteded client registration... waiting for ECC response" );
                  
                  // Wait for a manual exit
                  System.out.println( "Type EXIT to stop client" );
                  boolean continueMonitoring = true;
                  BufferedReader br = new BufferedReader( new InputStreamReader(System.in) );
                  
                  while ( continueMonitoring )
                  {
                      String command = null;
                      try { command = br.readLine(); }
                      catch ( IOException ioe )
                      {
                          clientLogger.error( "Cannot read commands from CLI!" );
                          continueMonitoring = false;
                      }
                      
                      if ( command.contains("EXIT") ) continueMonitoring = false;                      
                  }
                  
                  // Tidy up
                  client.disconnectFromECCMonitor();
                  
                  System.exit( 1 );
              }  
        }
        else { clientLogger.error( "Could not find client configuration properties" ); }
    }
    
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
          propsStream = EntryPoint.class.getResourceAsStream( "/main/resources/" +
                                                              configName + ".properties" );
     
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
    
    private static boolean connectToAMQPBus( ECCHeaderlessClient client,
                                             Properties          emProps )
    {
        // Safety first
        if ( client == null || emProps == null ) return false;
        
        // Get shared properties first
        String rabbitServerIP   = emProps.getProperty( "Monitor_IP" );
        String rabbitServerPort = emProps.getProperty( "MonitorPort" );
        String eccMonitorID     = emProps.getProperty( "MonitorID" );
        
        boolean connectedOK = false;
        
        // Proceed only if these at least exist
        if ( rabbitServerIP   != null &&
             rabbitServerPort != null &&
             eccMonitorID     != null )
        {
            int portNumber = Integer.parseInt( rabbitServerPort );
          
            // Now check to see if we're using a verified connection type
            if ( emProps.containsKey("Keystore") && 
                 emProps.containsKey("KeystorePassword") )
            {
                InputStream ksStream = EntryPoint.class.getResourceAsStream( emProps.getProperty("Keystore") ); 
                String ksPassword    = emProps.getProperty( "KeystorePassword" );

                try
                {
                    connectedOK = client.tryVerifiedConnectToAMQPBus( rabbitServerIP,
                                                                      portNumber,
                                                                      ksStream, 
                                                                      ksPassword );
                }
                catch ( Exception e )
                { clientLogger.error( "Could not connect to AMQP Bus: " + e.getMessage() ); }
            }
            else
            {
                // Might still try connect to the (unverified) AMQP server using SSL
                boolean useSSL = false;
                if ( emProps.containsKey("Monitor_Use_SSL") )
                    useSSL = ( emProps.getProperty( "Monitor_Use_SSL" ).equals("true") );
                
                try
                { connectedOK = client.tryConnectToAMQPBus( rabbitServerIP,
                                                            portNumber,
                                                            useSSL ); }
                catch ( Exception e )
                { 
                  clientLogger.error( "Could not connect to " +
                                      (useSSL ? "(SSL)" : "(insecure)") +
                                      "AMQP Bus: " + e.getMessage() );
                }
            }
        }
        
        return connectedOK;
    }
    
    private static boolean tryRegisterWithECCMonitor( ECCHeaderlessClient client,
                                                      Properties          emProps,
                                                      Properties          edmProps )
    {
        boolean registrationAttempt = false;
        
        // Try to create local data persistence (not critical, but very useful)
        if ( client.initialiseLocalDataManagement(edmProps) )
            clientLogger.info( "Successfully created EDMAgent and measurement scheduling" );
        else
            clientLogger.warn( "Could not create local EDMAgent - will continue, but will not schedule/store measurements" );

        // Make sure we have a valid UUID to connect to EM/ECC
        UUID expMonitorID = null;

        try { expMonitorID = UUID.fromString( emProps.getProperty( "MonitorID" ) ); }
        catch ( IllegalArgumentException iae )
        { clientLogger.error( "EM/ECC ID is invalid" ); }

        if ( expMonitorID != null )
        try
        {
            if ( client.tryRegisteringWithECCMonitor( expMonitorID,
                                                      UUID.randomUUID() ) ) registrationAttempt = true;
        }
        catch ( Exception e )
        { clientLogger.error( "Could not attempt registration with EM/ECC: " + e.getMessage() ); }
        
        return registrationAttempt;
    }
}
