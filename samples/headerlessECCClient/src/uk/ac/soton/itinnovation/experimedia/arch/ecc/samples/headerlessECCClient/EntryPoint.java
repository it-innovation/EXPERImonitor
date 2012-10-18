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
        try
        {          
            ECCHeaderlessClient client      = new ECCHeaderlessClient( "Headerless Client " + new Date().toString() );
            boolean usingVerifiedConnection = false; //( args.length == 1 ); // Expect a keystore password from CLI here
            boolean connectedToAMQPBus      = false;
            
            InputStream is = EntryPoint.class.getResourceAsStream( "/main/resources/client.properties" );
            if ( is == null ) throw new Exception( "Could not find client properties file" );
                
            Properties props = new Properties();
            props.load( is );
            
            if ( usingVerifiedConnection )
            {
                String rabbitServerIP      = props.getProperty( "Monitor_IP" );
                InputStream ksStream       = EntryPoint.class.getResourceAsStream( props.getProperty("Keystore") ); 
                String certificatePassword = args[0];
                
                connectedToAMQPBus = client.tryVerifiedConnectToAMQPBus( rabbitServerIP, 
                                                                         ksStream, 
                                                                         certificatePassword );
            }
            else
            {
                String rabbitServerIP = props.getProperty( "Monitor_IP" );
                boolean useSSL = ( props.getProperty( "Monitor_Use_SSL" ).equals("true") );
              
                if ( useSSL )
                    connectedToAMQPBus = client.tryConnectToAMQPBus( rabbitServerIP, true );
                else
                    connectedToAMQPBus = client.tryConnectToAMQPBus( rabbitServerIP, false );
            }
            
            if ( connectedToAMQPBus )
            {
                // Try to create local data persistence (not critical, but very useful)
                if ( client.initialiseLocalDataManagement() )
                    clientLogger.info( "Successfully created EDMAgent and measurement scheduling" );
                else
                    clientLogger.warn( "Could not create local EDMAgent - will continue, but will not schedule/store measurements" );
                
                UUID expMonitorID = UUID.fromString( props.getProperty( "MonitorID" ) );
                
                if ( client.tryRegisteringWithECCMonitor( expMonitorID,
                                                          UUID.randomUUID() ) )
                {
                    // Successfully connected and registered with the EM/ECC - the client will continue from here
                    clientLogger.info( "Successfully registered with EM/ECC... engaging with monitoring process" );
                }
            }
        }
        catch ( Exception e )
        { clientLogger.error("Problem starting: could not connect to the ECC: " + e.getMessage()); }
    
    }
}
