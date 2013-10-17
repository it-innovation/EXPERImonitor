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
//      Created Date :          2012-12-12
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.edm.test.metric.unit;

import java.util.Properties;
import junit.framework.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.loggin.impl.Log4JImpl;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.IECCLogger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.edm.test.metrics.PopulateDB;

@RunWith(JUnit4.class)
public class AGeneralTest extends TestCase
{
    static IECCLogger log;
    
    @BeforeClass
    public static void beforeClass()
    {
        // Configure logging system
        Logger.setLoggerImpl( new Log4JImpl() );
        log = Logger.getLogger(AGeneralTest.class);
        
        log.info("General tests executing...");
    }
    
    public Properties getProperties()
    {
        Properties prop = new Properties();
        
        try {
            prop.load(AGeneralTest.class.getClassLoader().getResourceAsStream(EDMTestSuite.propertiesFile));
        } catch (Exception ex) {
            log.error("Error with loading configuration file " + EDMTestSuite.propertiesFile + ": " + ex.getMessage(), ex);
            return null;
        }
        
        return prop;
    }
    
    @Test
    public void testDatabaseConnection()
    {
        //log.info(" - testDatabaseConnection");
        IMonitoringEDM edm = null;
        Properties prop = getProperties();
        try {
            edm = EDMInterfaceFactory.getMonitoringEDM(prop);
        } catch (Exception ex) {
            fail("Unable to get Monitoring EDM: " + ex.toString());
        }
        
        assertTrue("A connection to the database couldn't be made, or the correct schema is not in place", edm.isDatabaseSetUpAndAccessible());
    }
    
    @Test
    public void testClearDatabase()
    {
        //log.info(" - testClearDatabase");
        IMonitoringEDM edm = null;
        Properties prop = getProperties();
        try {
            edm = EDMInterfaceFactory.getMonitoringEDM(prop);
        } catch (Exception ex) {
            fail("Unable to get Monitoring EDM: " + ex.toString());
        }
        
        try {
            edm.clearMetricsDatabase();
        } catch (Throwable t) {
            fail("Unable to clear the database: " + t.toString());
        }
    }
    
    @Test
    public void testPopulateDB()
    {
        //log.info(" - testPopulateDB");
        IMonitoringEDM edm = null;
        Properties prop = getProperties();
        try {
            edm = EDMInterfaceFactory.getMonitoringEDM(prop);
        } catch (Exception ex) {
            fail("Unable to get Monitoring EDM: " + ex.toString());
        }
        
        try {
            PopulateDB.populateWithTestData(edm);
        } catch (Exception ex) {
            fail("Unable to populate the DB with test data: " + ex.toString());
        }
    }
}
