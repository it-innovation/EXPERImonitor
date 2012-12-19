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
//      Created Date :          2012-08-22
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.edm.test.unit;

import java.io.FileOutputStream;
import java.util.Properties;
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
    AGeneralTest.class,
    APopulateDBTest.class,
    ExperimentTest.class,
    EntityTest.class,
    MetricGeneratorTest.class,
    ReportTest.class
})
public class EDMTestSuite
{
    static Logger log = Logger.getLogger(EDMTestSuite.class);
    static String propertiesFile = "edm-test.properties";
    
    @BeforeClass
    public static void beforeClass()
    {
        log.info("Starting EDM Test Suite");
        log.debug("dbURL: " + System.getProperty("dbURL"));
        log.debug("dbName: " + System.getProperty("dbName"));
        log.debug("dbUsername: " + System.getProperty("dbUsername"));
        log.debug("dbPassword: " + System.getProperty("dbPassword"));
        log.debug("dbType: " + System.getProperty("dbType"));
        
        String[] args = {System.getProperty("dbURL"), System.getProperty("dbName"), System.getProperty("dbUsername"), System.getProperty("dbPassword"), System.getProperty("dbType")};
        try {
            setUpPropertiesFile(args);
        } catch (Exception ex) {
            log.error("Failed to set up the properties file for the EDM for doing the testing: " + ex.toString());
        }
    }

    public static void main(String[] args) throws Exception
    {
        log.info("Starting EDM Test Suite");
        setUpPropertiesFile(args);
        
        Result result = org.junit.runner.JUnitCore.runClasses(
            AGeneralTest.class,
            APopulateDBTest.class,
            ExperimentTest.class,
            EntityTest.class,
            MetricGeneratorTest.class,
            ReportTest.class);

        if (processResults(result)) {
            System.exit(0);
        } else {
            System.exit(1);
        }
    }
    
    public static void setUpPropertiesFile(String[] args) throws Exception
    {
        Properties prop = new Properties();
        
        if ((args != null) && (args.length == 5))
        {
            log.debug("Getting properties from the arguments");
            prop.setProperty("dbURL", args[0]);
            prop.setProperty("dbName", args[1]);
            prop.setProperty("dbUsername", args[2]);
            prop.setProperty("dbPassword", args[3]);
            prop.setProperty("dbType", args[4]);
        }
        else
        {
            log.debug("Using default edm.properties file");
            try {
                prop.load(EDMTestSuite.class.getClassLoader().getResourceAsStream("edm.properties"));
            } catch (Exception ex) {
                log.error("Error with loading configuration file edm.properties: " + ex.getMessage(), ex);
                throw new RuntimeException("Error with loading configuration file edm.properties: " + ex.getMessage(), ex);
            }
        }
        
        try {
            prop.store(new FileOutputStream("target/classes/" + propertiesFile), null);
        } catch (Exception ex) {
    		log.error("Unable to write properties file: " + ex.toString(), ex);
        }
    }
    
    public static boolean processResults(Result result)
    {
        log.info("");
        if (result.wasSuccessful()) {
            log.info("EDM tests completed successfully!");
        } else {
            log.info("EDM tests finished, but with failures!");
        }
        
        log.info("Run: " + result.getRunCount() + "  Failed: " + result.getFailureCount() + "  Ignored: " + result.getIgnoreCount());
        log.info("");
        if (result.getFailureCount() > 0)
        {
            log.info("Errors:");
            for (Failure failure : result.getFailures()) {
                log.info(failure.toString());
            }
            
            return false;
        }
        
        return true;
    }
}