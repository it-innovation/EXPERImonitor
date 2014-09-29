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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared;

import java.io.*;
import org.slf4j.*;
import java.util.Properties;




public class Utilitybox
{
    public static final Logger utilLogger = LoggerFactory.getLogger(Utilitybox.class);
    
    public static Properties getProperties( Class entryPointClass, String configName )
    {
        Properties targetProperties = null;
        InputStream propsStream     = null;
        
        // Look for file on class path first...
        File propFile = new File( configName + ".properties" );
        if ( propFile.exists() )
            try
            { propsStream = (InputStream) new FileInputStream( propFile ); }
            catch ( IOException ioe )
            { utilLogger.error( "Could not open client configuration properties file" ); }
        
        // ... but if it doesn't exist, try pulling the same from internal resources
        if ( propsStream == null ) 
        {
          ClassLoader cl = entryPointClass.getClassLoader();
          propsStream = cl.getResourceAsStream( configName + ".properties" );
        }
     
        // If we've got a good stream, try loading it
        if ( propsStream != null )
        {
          targetProperties = new Properties();
          try { targetProperties.load( propsStream ); }
          catch ( IOException ioe )
          { 
            utilLogger.error( "Could not load client configuration properties" );
            targetProperties = null;
          }
        }
        
        // Tidy up
        if ( propsStream != null )
          try 
          { propsStream.close(); }
          catch ( IOException ioe )
          { utilLogger.error( "Could not close client properties stream (and/or file)" ); }
        
        // Return properties
        return targetProperties;
    }

}
