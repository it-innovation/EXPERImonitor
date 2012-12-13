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
package uk.ac.soton.itinnovation.experimedia.arch.edm.test.unit;

import junit.framework.*;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.edm.test.general.PopulateDB;

@RunWith(JUnit4.class)
public class AGeneralTest extends TestCase
{
    static Logger log = Logger.getLogger(AGeneralTest.class);
    
    @BeforeClass
    public static void beforeClass()
    {
        log.info("General tests");
    }
    
    @Test
    public void testDatabaseConnection()
    {
        log.info(" - testDatabaseConnection");
        IMonitoringEDM edm = null;
        try {
            edm = EDMInterfaceFactory.getMonitoringEDM();
        } catch (Exception ex) {
            fail("Unable to get Monitoring EDM: " + ex.toString());
        }
        
        assertTrue("A connection to the database couldn't be made, or the correct schema is not in place", edm.isDatabaseSetUpAndAccessible());
    }
    
    @Test
    public void testClearDatabase()
    {
        log.info(" - testClearDatabase");
        IMonitoringEDM edm = null;
        try {
            edm = EDMInterfaceFactory.getMonitoringEDM();
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
        log.info(" - testPopulateDB");
        IMonitoringEDM edm = null;
        try {
            edm = EDMInterfaceFactory.getMonitoringEDM();
        } catch (Exception ex) {
            fail("Unable to get Monitoring EDM: " + ex.toString());
        }
        
        try {
            PopulateDB.populateWithTestData(edm);
        } catch (Exception ex) {
            log.error("Unable to populate the DB with test data: " + ex.toString(), ex);
            fail("Unable to populate the DB with test data: " + ex.toString());
        }
    }
}
