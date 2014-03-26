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
//      Created By :            Stefanie Wiegand
//      Created Date :          2014-03-05
//      Created for Project :   Experimedia
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.edm.test.prov.unit;

import java.io.FileOutputStream;
import java.util.Properties;
import uk.ac.soton.itinnovation.experimedia.arch.edm.test.metric.unit.*;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Runs all unit tests.
 * @author Vegard Engen
 */
@RunWith(Suite.class)
@SuiteClasses({ 
    EDMProvStoreWrapperTest.class,
	EDMProvWriterTest.class,
	EDMProvReaderTest.class,
	SPARQLProvTranslatorTest.class
})
public class EDMProvTestSuite {
    private static Logger logger;
    private static final String propertiesFile = "prov.properties";
    
    @BeforeClass
    public static void beforeClass()
    {
        // Configure logging system
        logger = Logger.getLogger(EDMTestSuite.class);        
        logger.info("Starting EDM Prov Test Suite");
		
		String[] args = {
			System.getProperty("owlim.repoTemplate"),
			System.getProperty("owlim.sesameServerURL"),
			System.getProperty("owlim.repositoryID"),
			System.getProperty("owlim.repositoryName"),
			System.getProperty("ont.Prefix"),
			System.getProperty("ont.BaseURI")
		};
		
        try {
            setUpPropertiesFile(args);
        } catch (Exception e) {
            logger.error("Failed to set up the properties file ", e);
        }
    }

    public static void main(String[] args) throws Exception {
        logger.info("Starting EDM Prov Test Suite");
		
		setUpPropertiesFile(args);
        
        Result result = org.junit.runner.JUnitCore.runClasses(
            EDMProvStoreWrapperTest.class,
			EDMProvWriterTest.class,
			EDMProvReaderTest.class,
			SPARQLProvTranslatorTest.class
		);

        if (processResults(result)) {
            System.exit(0);
        } else {
            System.exit(1);
        }
    }
	
	public static void setUpPropertiesFile(String[] args) throws Exception {
        Properties prop = new Properties();
        
        if ((args != null) && (args.length == 6) && isArgsValid(args))
        {
            logger.debug("Getting properties from the arguments");
            prop.setProperty("owlim.repoTemplate", args[0]);
            prop.setProperty("owlim.sesameServerURL", args[1]);
            prop.setProperty("owlim.repositoryID", args[2]);
            prop.setProperty("owlim.repositoryName", args[3]);
            prop.setProperty("ont.Prefix", args[4]);
			prop.setProperty("ont.BaseURI", args[5]);
        }
        else
        {
            logger.debug("Using default edm.properties file");
            try {
                prop.load(EDMTestSuite.class.getClassLoader().getResourceAsStream("edm.properties"));
            } catch (Exception ex) {
                logger.error("Error with loading configuration file edm.properties: " + ex.getMessage(), ex);
                throw new RuntimeException("Error with loading configuration file edm.properties: " + ex.getMessage(), ex);
            }
        }
        
        logger.debug("EDM database details");
        logger.debug("  owlim.repoTemplate:    " + prop.getProperty("owlim.repoTemplate"));
        logger.debug("  owlim.sesameServerURL: " + prop.getProperty("owlim.sesameServerURL"));
        logger.debug("  owlim.repositoryID:    " + prop.getProperty("owlim.repositoryID"));
        logger.debug("  owlim.repositoryName:  " + prop.getProperty("owlim.repositoryName"));
        logger.debug("  ont.Prefix:            " + prop.getProperty("ont.Prefix"));
        logger.debug("  ont.BaseURI:           " + prop.getProperty("ont.BaseURI"));
		
        try {
            logger.debug("Writing properties file to: /target/classes/" + propertiesFile);
            prop.store(new FileOutputStream("target/classes/" + propertiesFile), null);
        } catch (Exception ex) {
    		logger.error("Unable to write properties file: " + ex.toString(), ex);
        }
    }
    
    private static boolean isArgsValid(String[] args)
    {
        if ((args[0] == null) || args[0].isEmpty()) {
            return false;
        }
        if ((args[1] == null) || args[1].isEmpty()) {
            return false;
        }
        if ((args[2] == null) || args[2].isEmpty()) {
            return false;
        }
        if ((args[3] == null) || args[3].isEmpty()) {
            return false;
        }
        if ((args[4] == null) || args[4].isEmpty()) {
            return false;
        }
		if ((args[5] == null) || args[5].isEmpty()) {
            return false;
        }
        
        return true;
    }
    
    public static boolean processResults(Result result) {
        logger.info("");
        if (result.wasSuccessful()) {
            logger.info("EDM Prov tests completed successfully!");
        } else {
            logger.info("EDM Prov tests finished, but with failures!");
        }
        
        logger.info("Run: " + result.getRunCount() + "  Failed: " + result.getFailureCount() + "  Ignored: " + result.getIgnoreCount());
        logger.info("");
        if (result.getFailureCount() > 0)
        {
            logger.info("Errors:");
            for (Failure failure : result.getFailures()) {
                logger.info(failure.toString());
            }
            
            return false;
        }
        
        return true;
    }
	
	public static String getPropertiesFile() {
		return propertiesFile;
	}
}