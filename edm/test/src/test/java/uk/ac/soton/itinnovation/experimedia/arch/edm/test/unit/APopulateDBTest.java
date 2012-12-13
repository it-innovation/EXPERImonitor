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
//      Created for Project :   
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.edm.test.unit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import junit.framework.*;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IEntityDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMeasurementSetDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMetricGroupDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IReportDAO;
import uk.ac.soton.itinnovation.experimedia.arch.edm.test.general.PopulateDB;

/**
 * A test class that populates the database according to the PopulateDB script and
 * validates that everything that should have been stored is retrieved correctly.
 * 
 * This class doesn't test saving invalid data - specific test classes for the 
 * different entities in the metric data model test this.
 * 
 * @author Vegard Engen
 */
@RunWith(JUnit4.class)
public class APopulateDBTest extends TestCase
{
    IMonitoringEDM edm = null;
    static Logger log = Logger.getLogger(APopulateDBTest.class);
    
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(APopulateDBTest.class);

        log.info("EDM PopulateDB Test Complete");
        System.exit(0);
    }
    
    public APopulateDBTest()
    {
        super();
    }
    
    @BeforeClass
    public static void populateDB() throws Exception
    {
        log.info("PopulateDB tests");
        IMonitoringEDM edm = null;
        try {
            edm = EDMInterfaceFactory.getMonitoringEDM();
        } catch (Exception ex) {
            log.error("Unable to get Monitoring EDM: " + ex.toString());
            throw ex;
        }
        
        try {
            edm.clearMetricsDatabase();
            PopulateDB.populateWithTestData();
        } catch (Exception ex) {
            log.error("Unable to clear the metrics database and populate it: " + ex.toString());
            throw ex;
        }
    }
    
    @Before
    public void beforeEachTest() throws Exception
    {
        edm = EDMInterfaceFactory.getMonitoringEDM();
    }
    
    @Test
    public void testGetExperiments()
    {
        log.info(" - checking experiments");
        IExperimentDAO expDAO = null;
        try {
            expDAO = edm.getExperimentDAO();
        } catch (Exception ex) {
            fail("Unable to get Experiment DAO: " + ex.toString());
        }
        
        Set<Experiment> exp = null;
        try {
            exp = expDAO.getExperiments(false);
        } catch (Exception ex) {
            fail("Unable to get experiments due to an exception: " + ex.getMessage());
        }
        
        assertNotNull("Experiment set returned from DB is NULL", exp);
        assertTrue("Experiment set returned from DB should have contained 1 experiment, but contained " + exp.size() + " experiment(s)", exp.size() == 1);
    }
    
    @Test
    public void testGetEnties()
    {
        log.info(" - checking entities");
        IEntityDAO entityDAO = null;
        try {
            entityDAO = edm.getEntityDAO();
        } catch (Exception ex) {
            fail("Unable to get Entity DAO: " + ex.toString());
        }
        
        Set<Entity> entities = null;
        try {
            entities = entityDAO.getEntities(false);
        } catch (Exception ex) {
            fail("Unable to get entities due to an exception: " + ex.getMessage());
        }
        
        assertNotNull("Entity set returned from DB is null", entities);
        assertTrue("Entity set returned from DB should have contained 3 entities, but contained " + entities.size() + " entities", entities.size() == 3);
    }
    
    @Test
    public void testGetAttributes()
    {
        log.info(" - checking attributes for entities");
        IEntityDAO entityDAO = null;
        try {
            entityDAO = edm.getEntityDAO();
        } catch (Exception ex) {
            fail("Unable to get Entity DAO: " + ex.toString());
        }
        
        // map of how many attributes there should be for each entity
        Map<UUID, Integer> uuidNumMap = new HashMap<UUID, Integer>();
        uuidNumMap.put(PopulateDB.entity1UUID, 3);
        uuidNumMap.put(PopulateDB.entity2UUID, 3);
        uuidNumMap.put(PopulateDB.entity3UUID, 2);
        
        // check attributes for each entity
        for (UUID entityUUID : uuidNumMap.keySet())
        {
            Set<Attribute> attributes = null;
            
            try {
                attributes = entityDAO.getAttributesForEntity(entityUUID);
            } catch (Exception ex) {
                fail("Unable to get attributes due to an exception: " + ex.getMessage());
            }
            
            assertNotNull("Attribute set returned from DB is null", attributes);
            assertTrue("Attribute set returned from DB should have contained " + uuidNumMap.get(entityUUID) + " entities, but contained " + attributes.size() + " entities", attributes.size() == uuidNumMap.get(entityUUID));
        }
    }
    
    @Test
    public void testGetMetricGenerators()
    {
        log.info(" - checking metric generators");
        IMetricGeneratorDAO metricGeneratorDAO = null;
        try {
            metricGeneratorDAO = edm.getMetricGeneratorDAO();
        } catch (Exception ex) {
            fail("Unable to get MetricGenerator DAO: " + ex.toString());
        }
        
        Set<MetricGenerator> metricGenerators = null;
        
        try {
            metricGenerators = metricGeneratorDAO.getMetricGenerators(false);
        } catch (Exception ex) {
            fail("Unable to get metric generators due to an exception: " + ex.getMessage());
        }
        
        assertNotNull("MetricGenerator set returned from DB is null", metricGenerators);
        assertTrue("MetricGenerator set returned from DB should have contained 3 entries, but contained " + metricGenerators.size() + " entries", metricGenerators.size() == 3);
    }
    
    @Test
    public void testGetMetricGroups()
    {
        log.info(" - checking metric groups for metric generators");
        IMetricGroupDAO mGroupDAO = null;
        try {
            mGroupDAO = edm.getMetricGroupDAO();
        } catch (Exception ex) {
            fail("Unable to get MetricGroup DAO: " + ex.toString());
        }
        
        // map of how many metric groups there should be for each metric generator
        Map<UUID, Integer> uuidNumMap = new HashMap<UUID, Integer>();
        uuidNumMap.put(PopulateDB.mGen1UUID, 1);
        uuidNumMap.put(PopulateDB.mGen2UUID, 1);
        uuidNumMap.put(PopulateDB.mGen3UUID, 1);
        
        // check metric groups for each metric generator
        for (UUID uuid : uuidNumMap.keySet())
        {
            Set<MetricGroup> metricGroups = null;
            
            try {
                metricGroups = mGroupDAO.getMetricGroupsForMetricGenerator(uuid, false);
            } catch (Exception ex) {
                fail("Unable to get MetricGroup set due to an exception: " + ex.getMessage());
            }
            
            assertNotNull("MetricGroup set returned from DB is null", metricGroups);
            assertTrue("MetricGroup set returned from DB should have contained " + uuidNumMap.get(uuid) + " entries, but contained " + metricGroups.size() + " entries", metricGroups.size() == uuidNumMap.get(uuid));
        }
    }
    
    @Test
    public void testMeasurementSets()
    {
        log.info(" - checking measurement sets for metric groups");
        IMeasurementSetDAO mSetDAO = null;
        try {
            mSetDAO = edm.getMeasurementSetDAO();
        } catch (Exception ex) {
            fail("Unable to get MeasurementSet DAO: " + ex.toString());
        }
        
        // map of how many measurement sets there should be for each metric group
        Map<UUID, Integer> uuidNumMap = new HashMap<UUID, Integer>();
        uuidNumMap.put(PopulateDB.mGrp1UUID, 3);
        uuidNumMap.put(PopulateDB.mGrp2UUID, 3);
        uuidNumMap.put(PopulateDB.mGrp3UUID, 2);
        
        // check metric groups for each metric generator
        for (UUID uuid : uuidNumMap.keySet())
        {
            Set<MeasurementSet> measurementSets = null;
            
            try {
                measurementSets = mSetDAO.getMeasurementSetForMetricGroup(uuid, true);
            } catch (Exception ex) {
                fail("Unable to get MeasurementSet set due to an exception: " + ex.getMessage());
            }
            
            assertNotNull("MeasurementSet set returned from DB is null", measurementSets);
            assertTrue("MeasurementSet set returned from DB should have contained " + uuidNumMap.get(uuid) + " entries, but contained " + measurementSets.size() + " entries", measurementSets.size() == uuidNumMap.get(uuid));
            
            // check that the metric data has been set
            for (MeasurementSet mSet : measurementSets)
            {
                assertNotNull("Measurement set is NULL", mSet);
                assertNotNull("Measurement set's UUID is NULL", mSet.getUUID());
                assertNotNull("Measurement set's metric is NULL", mSet.getMetric());
                assertNotNull("Measurement set's metric type is NULL", mSet.getMetric().getMetricType());
                assertNotNull("Measurement set's metric unit is NULL", mSet.getMetric().getUnit());
                assertNotNull("Measurement set's Attribute UUID is NULL", mSet.getAttributeUUID());
            }
        }
    }
    
    @Test
    public void testReports()
    {
        log.info(" - checking reports of all measurements for measurement sets");
        IReportDAO reportDAO = null;
        try {
            reportDAO = edm.getReportDAO();
        } catch (Exception ex) {
            fail("Unable to get Report DAO: " + ex.toString());
        }
        
        // map of how many measurements there should be for each measurement set
        Map<UUID, Integer> uuidNumMap = new HashMap<UUID, Integer>();
        uuidNumMap.put(PopulateDB.mGrp1mSet1UUID, 20);
        uuidNumMap.put(PopulateDB.mGrp1mSet2UUID, 20);
        uuidNumMap.put(PopulateDB.mGrp1mSet3UUID, 20);
        uuidNumMap.put(PopulateDB.mGrp2mSet1UUID, 20);
        uuidNumMap.put(PopulateDB.mGrp2mSet2UUID, 20);
        uuidNumMap.put(PopulateDB.mGrp2mSet3UUID, 20);
        uuidNumMap.put(PopulateDB.mGrp3mSet1UUID, 20);
        uuidNumMap.put(PopulateDB.mGrp3mSet2UUID, 20);
        
        // check metric groups for each metric generator
        for (UUID uuid : uuidNumMap.keySet())
        {
            Report report = null;
            
            try {
                report = reportDAO.getReportForAllMeasurements(uuid, false);
            } catch (Exception ex) {
                fail("Unable to get report for all measurements for measurement set with UUID '" + uuid.toString() + "'");
            }
            
            assertNotNull("Report returned from DB is null", report);
            assertTrue("Report returned from DB should have contained " + uuidNumMap.get(uuid) + " measurements, but contained " + report.getNumberOfMeasurements(), report.getNumberOfMeasurements() == uuidNumMap.get(uuid));
        }
    }
}
