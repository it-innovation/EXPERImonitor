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

import java.util.UUID;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
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
    static Logger log = Logger.getLogger(MetricGeneratorTest.class);
    
    @BeforeClass
    public static void beforeClass()
    {
        log.info("MetricGenerator tests");
    }
    
    @Before
    public void beforeEachTest() throws Exception
    {
        edm = EDMInterfaceFactory.getMonitoringEDM();
        edm.clearMetricsDatabase();
        mGenDAO = edm.getMetricGeneratorDAO();
        PopulateDB.saveExperiment(edm, PopulateDB.expUUID);
        PopulateDB.saveEntity1(edm, PopulateDB.entity1UUID, PopulateDB.entity1attribute1UUID, PopulateDB.entity1attribute2UUID, PopulateDB.entity1attribute3UUID);
    }

    @Test
    public void testSaveMetricGenerator_validFull()
    {
        log.info(" - saving MetricGenerator");
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
        log.info(" - saving MetricGenerator (minimal)");
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
        log.info(" - saving MetricGenerator with a new entity");
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
        log.info(" - saving MetricGenerator with no UUID");
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
        log.info(" - saving duplicate MetricGenerator");
        MetricGenerator metricGenerator1 = new MetricGenerator(PopulateDB.mGen1UUID, "Experiment MetricGenerator 1", null);
        metricGenerator1.addEntity(new Entity(UUID.randomUUID(), "Entity", "Description"));
        
        try {
            mGenDAO.saveMetricGenerator(metricGenerator1, PopulateDB.expUUID);
        } catch (Exception ex) { 
            fail("Unable to save MetricGenerator: " + ex.getMessage());
        }
        
        MetricGenerator metricGenerator2 = new MetricGenerator(PopulateDB.mGen1UUID, "Experiment MetricGenerator 2", null);
        metricGenerator2.addEntity(new Entity(UUID.randomUUID(), "Entity", "Description"));
        
        try {
            mGenDAO.saveMetricGenerator(metricGenerator2, PopulateDB.expUUID);
            fail("Saved MetricGenerator with the same UUID as an existing MetricGenerator");
        } catch (Exception ex) { }
    }
    
    @Test
    public void testSaveMetricGenerator_noEntity()
    {
        log.info(" - saving MetricGenerator with no entity");
        MetricGenerator metricGenerator = new MetricGenerator(PopulateDB.mGen1UUID, "Experiment MetricGenerator", null);
        
        try {
            mGenDAO.saveMetricGenerator(metricGenerator, PopulateDB.expUUID);
            fail("Saved MetricGenerator without an Entity");
        } catch (Exception ex) { }
    }
    
    @Test
    public void testSaveMetricGenerator_errorEntity()
    {
        log.info(" - saving MetricGenerator with erroronous entity");
        MetricGenerator metricGenerator = new MetricGenerator(PopulateDB.mGen1UUID, "Experiment MetricGenerator", null);
        metricGenerator.addEntity(new Entity()); // missing values that cannot be null
        
        try {
            mGenDAO.saveMetricGenerator(metricGenerator, PopulateDB.expUUID);
            fail("Saved MetricGenerator with an erroronous Entity");
        } catch (Exception ex) { }
    }
}
