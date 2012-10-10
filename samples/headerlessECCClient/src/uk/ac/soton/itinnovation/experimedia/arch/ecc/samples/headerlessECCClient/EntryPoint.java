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
import java.util.Properties;
import java.util.UUID;
import org.apache.log4j.Logger;




public class EntryPoint
{
    public static final Logger clientLogger = Logger.getLogger( EntryPoint.class );
  
    public static void main( String args[] )
    {
        try
        {
            String rabbitServerIP           = "127.0.0.1";
            InputStream certificateStream   = null;
            String      certificatePassword = null;
            
            boolean useProperties = true;
            
            if ( useProperties )
            {
                InputStream is = EntryPoint.class.getResourceAsStream( "/main/resources/clientProperties.properties" );
                if ( is == null ) throw new Exception( "Could not find client properties file" );
                
                Properties props = new Properties();
                props.load( is );
                
                rabbitServerIP      = props.getProperty( "ECC_IP" );
                certificateStream   = EntryPoint.class.getResourceAsStream( props.getProperty("Certificate") ); 
                certificatePassword = props.getProperty( "CertificatePassword" );
            }
            
            ECCHeaderlessClient client = new ECCHeaderlessClient();
            
            client.tryConnectToECC( rabbitServerIP, 
                                    certificateStream,
                                    certificatePassword,
                                    UUID.fromString("00000000-0000-0000-0000-000000000000"), 
                                    UUID.randomUUID() );
        }
        catch ( Exception e )
        { clientLogger.error("Problem starting: could not connect to the ECC: " + e.getMessage()); }
    
    }
}
