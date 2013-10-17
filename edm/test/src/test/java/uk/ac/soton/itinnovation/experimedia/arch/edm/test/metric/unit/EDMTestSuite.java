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
package uk.ac.soton.itinnovation.experimedia.arch.edm.test.metric.unit;

import java.io.FileOutputStream;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.loggin.impl.Log4JImpl;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.IECCLogger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.Logger;

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
    static IECCLogger log;
    static String propertiesFile = "edm-test.properties";
    
    @BeforeClass
    public static void beforeClass()
    {
        // Configure logging system
        Logger.setLoggerImpl( new Log4JImpl() );
        log = Logger.getLogger(EDMTestSuite.class);
        
        log.info("Starting EDM Test Suite");
        
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
        
        if ((args != null) && (args.length == 5) && isArgsValid(args))
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
        
        log.info("EDM database details");
        log.info("  dbURL: " + prop.getProperty("dbURL"));
        log.info("  dbName: " + prop.getProperty("dbName"));
        log.info("  dbUsername: " + prop.getProperty("dbUsername"));
        log.info("  dbPassword: " + prop.getProperty("dbPassword"));
        log.info("  dbType: " + prop.getProperty("dbType"));
        
        try {
            log.debug("Writing properties file to: /target/classes/" + propertiesFile);
            prop.store(new FileOutputStream("target/classes/" + propertiesFile), null);
        } catch (Exception ex) {
    		log.error("Unable to write properties file: " + ex.toString(), ex);
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
        
        return true;
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