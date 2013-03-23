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
//      Created By :            Vegard Engen
//      Created Date :          2012-08-21
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl;

import java.util.Properties;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.IECCLogger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.Logger;

/**
 * A utility class for the Experiment Data Manager.
 * 
 * @author Vegard Engen
 */
public class EDMUtil
{
    static IECCLogger log = Logger.getLogger(EDMUtil.class);
    private static String configFileName = "edm.properties";
    
    public static Properties getConfigs() throws Exception
    {
        Properties prop = new Properties();
        
        try {
            prop.load(EDMUtil.class.getClassLoader().getResourceAsStream(configFileName));
        } catch (Exception ex) {
            log.error("Error with loading configuration file edm.properties: " + ex.getMessage(), ex);
            throw new RuntimeException("Error with loading configuration file " + configFileName + ": " + ex.getMessage(), ex);
        }
        
        return prop;
    }
    
    public static boolean isConfigValid(Properties prop)
    {
        if (prop == null)
        {
            log.error("The Properties object is NULL");
            return false;
        }
        
        String[] expectedConfigNames = new String[] {
            "dbURL", "dbName", "dbUsername", "dbPassword", "dbType"
        };
        
         // get the expected config parameters and add to the map
        for (String configName : expectedConfigNames)
        {
            try {
                prop.getProperty(configName);
            } catch (Exception ex) {
                log.error("Error with reading the '" + configName + "' parameter: " + ex.toString(), ex);
                return false;
            }
        }
        
        return true;
    }
}
