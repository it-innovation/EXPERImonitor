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
//      Created Date :          2012-12-13
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.edm.test.unit;

import java.util.Properties;
import java.util.UUID;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.loggin.impl.Log4JImpl;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.IECCLogger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.edm.test.general.PopulateDB;

/**
 *
 * @author Vegard Engen
 */
@RunWith(JUnit4.class)
public class MetricGeneratorTest extends TestCase
{
    IMonitoringEDM edm = null;
    IMetricGeneratorDAO mGenDAO = null;
    static IECCLogger log;
    
    @BeforeClass
    public static void beforeClass()
    {
        // Configure logging system
        Logger.setLoggerImpl( new Log4JImpl() );
        log = Logger.getLogger(MetricGeneratorTest.class);
        
        log.info("MetricGenerator tests executing...");
    }
    
    @Before
    public void beforeEachTest()
    {
        try {
            Properties prop = getProperties();
            edm = EDMInterfaceFactory.getMonitoringEDM(prop);
            edm.clearMetricsDatabase();
            mGenDAO = edm.getMetricGeneratorDAO();
            PopulateDB.saveExperiment(edm, PopulateDB.expUUID);
            PopulateDB.saveEntity1(edm, PopulateDB.entity1UUID, PopulateDB.entity1attribute1UUID, PopulateDB.entity1attribute2UUID, PopulateDB.entity1attribute3UUID);
        } catch (Exception ex) {
            log.error("Unable to set up EDM and populate DB with necessary data to perform the MetricGenerator tests: " + ex.toString());
        }
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
    public void testSaveMetricGenerator_validFull()
    {
        log.debug(" - saving MetricGenerator");
        
        if ((edm == null) || (mGenDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        MetricGenerator metricGenerator = new MetricGenerator(PopulateDB.mGen1UUID, "Experiment MetricGenerator", "A description");
        metricGenerator.addEntity(new Entity(PopulateDB.entity1UUID));
        
        try {
            mGenDAO.saveMetricGenerator(metricGenerator, PopulateDB.expUUID);
        } catch (Exception ex) {
            fail("Unable to save MetricGenerator: " + ex.getMessage());
        }
    }
    
    @Test
    public void testSaveMetricGenerator_validMinimal()
    {
        log.debug(" - saving MetricGenerator (minimal)");
        
        if ((edm == null) || (mGenDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        MetricGenerator metricGenerator = new MetricGenerator(PopulateDB.mGen1UUID, "Experiment MetricGenerator", null);
        metricGenerator.addEntity(new Entity(PopulateDB.entity1UUID));
        
        try {
            mGenDAO.saveMetricGenerator(metricGenerator, PopulateDB.expUUID);
        } catch (Exception ex) {
            fail("Unable to save MetricGenerator: " + ex.getMessage());
        }
    }
    
    @Test
    public void testSaveMetricGenerator_newEntity()
    {
        log.debug(" - saving MetricGenerator with a new entity");
        
        if ((edm == null) || (mGenDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        MetricGenerator metricGenerator = new MetricGenerator(PopulateDB.mGen1UUID, "Experiment MetricGenerator", null);
        metricGenerator.addEntity(new Entity(UUID.randomUUID(), "Entity", "Description"));
        
        try {
            mGenDAO.saveMetricGenerator(metricGenerator, PopulateDB.expUUID);
        } catch (Exception ex) {
            fail("Unable to save MetricGenerator with new Entity: " + ex.getMessage());
        }
    }
    
    @Test
    public void testSaveMetricGenerator_noUUID()
    {
        log.debug(" - saving MetricGenerator with no UUID");
        
        if ((edm == null) || (mGenDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        MetricGenerator metricGenerator = new MetricGenerator(null, "Experiment MetricGenerator", null);
        metricGenerator.addEntity(new Entity(UUID.randomUUID(), "Entity", "Description"));
        
        try {
            mGenDAO.saveMetricGenerator(metricGenerator, PopulateDB.expUUID);
            fail("Saved MetricGenerator without an Entity");
        } catch (Exception ex) { }
    }
    
    @Test
    public void testSaveMetricGenerator_duplicate()
    {
        log.debug(" - saving duplicate MetricGenerator");
        
        if ((edm == null) || (mGenDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        MetricGenerator metricGenerator1 = new MetricGenerator(PopulateDB.mGen1UUID, "Experiment MetricGenerator 1", null);
        metricGenerator1.addEntity(new Entity(UUID.randomUUID(), "Entity", "Description"));
        
        try {
            mGenDAO.saveMetricGenerator(metricGenerator1, PopulateDB.expUUID);
        } catch (Exception ex) { 
            fail("Unable to save MetricGenerator: " + ex.getMessage());
        }
        
        MetricGenerator metricGenerator2 = new MetricGenerator(PopulateDB.mGen1UUID, "Experiment MetricGenerator 2", null);
        metricGenerator2.addEntity(new Entity(UUID.randomUUID(), "Entity", "Description"));
        
        // EDM now aggregates metric generators (duplicate entities/attributes/measurements are not allowed however)
        try {
            mGenDAO.saveMetricGenerator(metricGenerator2, PopulateDB.expUUID);
        } catch (Exception ex)
        {
          fail("Saved MetricGenerator with the same UUID as an existing MetricGenerator");
        }
    }
    
    @Test
    public void testSaveMetricGenerator_noEntity()
    {
        log.debug(" - saving MetricGenerator with no entity");
        
        if ((edm == null) || (mGenDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        MetricGenerator metricGenerator = new MetricGenerator(PopulateDB.mGen1UUID, "Experiment MetricGenerator", null);
        
        try {
            mGenDAO.saveMetricGenerator(metricGenerator, PopulateDB.expUUID);
            fail("Saved MetricGenerator without an Entity");
        } catch (Exception ex) { }
    }
    
    @Test
    public void testSaveMetricGenerator_errorEntity()
    {
        log.debug(" - saving MetricGenerator with erroronous entity");
        
        if ((edm == null) || (mGenDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        MetricGenerator metricGenerator = new MetricGenerator(PopulateDB.mGen1UUID, "Experiment MetricGenerator", null);
        metricGenerator.addEntity(new Entity()); // missing values that cannot be null
        
        try {
            mGenDAO.saveMetricGenerator(metricGenerator, PopulateDB.expUUID);
            fail("Saved MetricGenerator with an erroronous Entity");
        } catch (Exception ex) { }
    }
}
