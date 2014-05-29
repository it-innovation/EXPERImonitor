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
//      Created Date :          02-May-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.ecc.service.test.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricCalculator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricHelper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricType;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Unit;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IReportDAO;
import uk.co.soton.itinnovation.ecc.service.Application;
import uk.co.soton.itinnovation.ecc.service.domain.DatabaseConfiguration;
import uk.co.soton.itinnovation.ecc.service.domain.EccAttribute;
import uk.co.soton.itinnovation.ecc.service.domain.EccConfiguration;
import uk.co.soton.itinnovation.ecc.service.domain.EccCounterMeasurement;
import uk.co.soton.itinnovation.ecc.service.domain.EccCounterMeasurementSet;
import uk.co.soton.itinnovation.ecc.service.domain.EccEntity;
import uk.co.soton.itinnovation.ecc.service.domain.EccMeasurement;
import uk.co.soton.itinnovation.ecc.service.domain.EccMeasurementSet;
import uk.co.soton.itinnovation.ecc.service.services.ConfigurationService;
import uk.co.soton.itinnovation.ecc.service.services.DataService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // will reset everything after each test, comment this out if you want to test as singleton
public class ServiceTest {

    private final Logger logger = LoggerFactory.getLogger(ServiceTest.class);

    private EccConfiguration eccConfig;
    private DatabasePopulator dbPopulator;

    @Autowired
    DataService dataService;

    @Autowired
    ConfigurationService configurationService;

    public ServiceTest() {
        dbPopulator = new DatabasePopulator();
    }

    @Before
    public void setUp() {

        Assert.assertTrue(configurationService.isInitialised());

        // Safer as doesn't depend on availability of http://config.experimedia.eu
        // Online can be a separate test
        eccConfig = configurationService.getLocalConfiguration();
        Assert.assertNotNull(eccConfig);

        Assert.assertNotNull(dataService);
        Assert.assertTrue(dataService.start(eccConfig.getDatabaseConfig()));

        Assert.assertTrue(dbPopulator.initialise(eccConfig));
    }

    @Test
    public void testGetAllExperiments() {
        Assert.assertTrue(dbPopulator.isDataReady());

        // Check all experiments are there
        for (Experiment exp : dataService.getAllExperiments(true, true)) {
            Assert.assertTrue(dbPopulator.doesExperimentExist(exp));
        }
    }

    @Test
    public void testGetExperiment() {
        Assert.assertTrue(dbPopulator.isDataReady());

        for (UUID expID : dbPopulator.getExperimentIDs()) {
            Assert.assertNotNull(dataService.getExperiment(expID.toString(), true));
        }
    }

    @Test
    public void testGetEntity() {
        Assert.assertTrue(dbPopulator.isDataReady());

        for (UUID expID : dbPopulator.getExperimentIDs()) {
            Set<UUID> entityIDs = dbPopulator.getExperimentEntities(expID).keySet();

            int expectedEntCount = DatabasePopulator.METRIC_GENERATOR_COUNT * DatabasePopulator.ENTITY_COUNT;
            Assert.assertTrue(entityIDs.size() == expectedEntCount);

            for (UUID entID : entityIDs) {
                EccEntity ent = dataService.getEccEntity(entID.toString(), true);
                Assert.assertNotNull(ent);
                Assert.assertNotNull(ent.getAttributes());
            }
        }
    }

    @Test
    public void testGetEntityForExperiment() {
        Assert.assertTrue(dbPopulator.isDataReady());

        for (UUID expID : dbPopulator.getExperimentIDs()) {
            Map<UUID, Entity> expectedEntities = dbPopulator.getExperimentEntities(expID);
            ArrayList<EccEntity> entities = dataService.getEntitiesForExperiment(expID.toString(), true);

            Assert.assertTrue(entities.size() == expectedEntities.size());

            for (EccEntity eccEnt : entities) {
                Assert.assertNotNull(eccEnt);
                Assert.assertTrue(expectedEntities.containsKey(eccEnt.getUuid()));
            }
        }
    }

    @Test
    public void testGetAttribute() {
        Assert.assertTrue(dbPopulator.isDataReady());

        for (UUID expID : dbPopulator.getExperimentIDs()) {
            Set<UUID> attrIDs = dbPopulator.getExperimentAttributes(expID).keySet();

            int expectedAttrCount = DatabasePopulator.METRIC_GENERATOR_COUNT
                    * DatabasePopulator.ENTITY_COUNT
                    * DatabasePopulator.ATTRIBUTE_COUNT;

            Assert.assertTrue(attrIDs.size() == expectedAttrCount);

            for (UUID attrID : attrIDs) {
                EccAttribute att = dataService.getEccAttribute(attrID.toString());
                Assert.assertNotNull(att);
                Assert.assertNotNull(att.getEntityUUID());
                Assert.assertNotNull(att.getType());
                Assert.assertNotNull(att.getUnit());
            }
        }
    }

    @Test
    public void testGetAttributesForExperiment() {
        Assert.assertTrue(dbPopulator.isDataReady());

        for (UUID expID : dbPopulator.getExperimentIDs()) {
            Map<UUID, Attribute> expectedAttributes = dbPopulator.getExperimentAttributes(expID);
            ArrayList<EccAttribute> attributes = dataService.getAttributesForExperiment(expID.toString());

            Assert.assertTrue(attributes.size() == expectedAttributes.size());

            for (EccAttribute attr : attributes) {
                Assert.assertNotNull(attr);
                Assert.assertTrue(expectedAttributes.containsKey(attr.getUuid()));
            }
        }
    }

    @Test
    public void testGetMeasurementsForAttributeAfter() {
        for (UUID expID : dbPopulator.getExperimentIDs()) {
            Map<UUID, Attribute> attributes = dbPopulator.getExperimentAttributes(expID);

            for (Attribute attr : attributes.values()) {
                // Get test data
                List<Measurement> testMeasures = getTestMeasurements(attr.getUUID(),
                        expID,
                        DatabasePopulator.MEASUREMENT_SUB_SAMPLE,
                        true);

                // Get 'after' samples time frame
                int targetIndex = testMeasures.size() - 1;
                long targetTime = testMeasures.get(targetIndex).getTimeStamp().getTime();
                targetTime -= 1000;

                // Retrieve data to check
                EccAttribute eccAttr = dataService.getEccAttribute(attr.getUUID().toString());
                Assert.assertNotNull(eccAttr);

                Set<EccMeasurementSet> eccMSets = dataService.getMeasurementsForAttributeAfter(expID.toString(),
                        attr.getUUID().toString(),
                        targetTime,
                        DatabasePopulator.MEASUREMENT_SUB_SAMPLE);
                Assert.assertTrue(!eccMSets.isEmpty());

                EccMeasurementSet eccms = eccMSets.iterator().next();
                Assert.assertNotNull(eccms);
                Assert.assertNotNull(eccms.getData());
                Assert.assertNotNull(eccms.getData().size() == testMeasures.size());

                Assert.assertTrue(validateMeasurementSets(testMeasures, eccms));
            }
        }
    }

    @Test
    public void testGetMeasurementsForAttributeBefore() {
        for (UUID expID : dbPopulator.getExperimentIDs()) {
            Map<UUID, Attribute> attributes = dbPopulator.getExperimentAttributes(expID);

            for (Attribute attr : attributes.values()) {
                // Get test data
                List<Measurement> testMeasures = getTestMeasurements(attr.getUUID(),
                        expID,
                        DatabasePopulator.MEASUREMENT_SUB_SAMPLE,
                        true);
                // Get 'before' samples time frame
                long targetTime = testMeasures.get(0).getTimeStamp().getTime();
                targetTime += 1000;

                // Retrieve data to check
                EccAttribute eccAttr = dataService.getEccAttribute(attr.getUUID().toString());
                Assert.assertNotNull(eccAttr);

                Set<EccMeasurementSet> eccMSets = dataService.getMeasurementsForAttributeBefore(expID.toString(),
                        attr.getUUID().toString(),
                        targetTime,
                        DatabasePopulator.MEASUREMENT_SUB_SAMPLE);
                Assert.assertTrue(!eccMSets.isEmpty());

                EccMeasurementSet eccms = eccMSets.iterator().next();
                Assert.assertNotNull(eccms);
                Assert.assertNotNull(eccms.getData());
                Assert.assertNotNull(eccms.getData().size() == testMeasures.size());

//                Assert.assertTrue(validateMeasurementSets(testMeasures, eccms));
            }
        }
    }

    @Test
    public void testGetCounterMeasurementsForAttributeAfter() {
        for (UUID expID : dbPopulator.getExperimentIDs()) {
            Map<UUID, Attribute> attributes = dbPopulator.getExperimentAttributes(expID);

            for (Attribute attr : attributes.values()) {
                // Get 'after' samples time frame
                List<Measurement> testMeasures = getTestMeasurements(attr.getUUID(),
                        expID,
                        DatabasePopulator.MEASUREMENT_COUNT,
                        true);

                int targetIndex = testMeasures.size() - 1;
                long targetTime = testMeasures.get(targetIndex).getTimeStamp().getTime();
                targetTime -= 1000;

                // Get frequency data
                MeasurementSet attrMset
                        = dbPopulator.getMeasurementSetsForAttribute(attr.getUUID(), expID).values().iterator().next();

                // Get frequency count
                Map<String, Integer> testFrequencies
                        = MetricCalculator.countValueFrequencies(attrMset.getMeasurements());

                // Retrieve data to check
                EccAttribute eccAttr = dataService.getEccAttribute(attr.getUUID().toString());
                Assert.assertNotNull(eccAttr);

                EccCounterMeasurementSet eccMSet
                        = dataService.getCounterMeasurementsForAttributeAfter(expID.toString(),
                                attr.getUUID().toString(),
                                targetTime,
                                DatabasePopulator.MEASUREMENT_COUNT);
                Assert.assertTrue(eccMSet.getData().isEmpty());

//                Assert.assertTrue( validateFrequencySets(testFrequencies, eccMSet) );
            }
        }
    }

    @Test
    public void testGetCounterMeasurementsForAttributeUntilAfter() {
        for (UUID expID : dbPopulator.getExperimentIDs()) {
            Map<UUID, Attribute> attributes = dbPopulator.getExperimentAttributes(expID);

            for (Attribute attr : attributes.values()) {
                // Get 'until after' samples time frame
                List<Measurement> testMeasures = getTestMeasurements(attr.getUUID(),
                        expID,
                        DatabasePopulator.MEASUREMENT_COUNT,
                        true);
                // Get 'after' samples time frame
                int targetIndex = testMeasures.size() - 1;
                long targetTime = testMeasures.get(targetIndex).getTimeStamp().getTime();
                targetTime -= 1000;

                // Get frequency data
                MeasurementSet attrMset
                        = dbPopulator.getMeasurementSetsForAttribute(attr.getUUID(), expID).values().iterator().next();

                // Get frequency count
                Map<String, Integer> testFrequencies
                        = MetricCalculator.countValueFrequencies(attrMset.getMeasurements());

                // Retrieve data to check
                EccAttribute eccAttr = dataService.getEccAttribute(attr.getUUID().toString());
                Assert.assertNotNull(eccAttr);

//                Set<EccCounterMeasurementSet> eccMSets
//                        = dataService.getCounterMeasurementsForAttributeBeforeAndExcluding(expID.toString(),
//                                attr.getUUID().toString(),
//                                targetTime,
//                                DatabasePopulator.MEASUREMENT_COUNT);
//                Assert.assertTrue(!eccMSets.isEmpty());
//
//                Assert.assertTrue(validateFrequencySets(testFrequencies, eccMSets));
            }
        }
    }

    // Private data classes/methods --------------------------------------------
    private List<Measurement> getTestMeasurements(UUID attrID, UUID expID, int range, boolean tail) {
        MeasurementSet attrMset
                = dbPopulator.getMeasurementSetsForAttribute(attrID, expID).values().iterator().next();

        List<Measurement> attrMeasures = MetricHelper.sortMeasurementsByDateLinear(attrMset.getMeasurements());

        attrMeasures = MetricHelper.truncateMeasurements(attrMeasures, range, tail);

        return attrMeasures;
    }

    private boolean validateMeasurementSets(List<Measurement> srcMS,
            EccMeasurementSet eccms) {
        boolean failed = false;

        if (srcMS != null && eccms != null) {
            Iterator<Measurement> srcIt = srcMS.iterator();
            Iterator<EccMeasurement> trgIt = eccms.getData().iterator();

            while (srcIt.hasNext()) {
                if (trgIt.hasNext()) {
                    Measurement sM = srcIt.next();
                    EccMeasurement tM = trgIt.next();

                    Assert.assertTrue(sM.getValue().equals(tM.getValue()));
                } else {
                    failed = true;
                    break;
                }
            }
        }

        return !failed;
    }

    private boolean validateFrequencySets(Map<String, Integer> srcFreqs,
            Set<EccCounterMeasurementSet> countSets) {
        Assert.assertNotNull(srcFreqs);
        Assert.assertNotNull(countSets);

        EccCounterMeasurementSet ecms = countSets.iterator().next();
        Assert.assertNotNull(ecms);

        ArrayList<EccCounterMeasurement> counters = ecms.getData();
        Assert.assertNotNull(counters);

        for (EccCounterMeasurement counter : counters) {
            Integer srcCount = srcFreqs.get(counter.getName());
            Assert.assertNotNull(srcCount);

            Assert.assertTrue(srcCount.intValue() == counter.getCounter());
        }

        return true;
    }

    private class DatabasePopulator {

        private IMonitoringEDM expDataManager;
        private IExperimentDAO experimentDAO;
        private IMetricGeneratorDAO metricGenDAO;
        private IReportDAO reportDAO;

        private HashMap<UUID, Experiment> testData;
        private boolean dataReady = false;

        // Create a time-stamp that we can manually increment
        private long timeInc = 1396306800000L;

        public final static int EXPERIMENT_COUNT = 5;
        public final static int METRIC_GENERATOR_COUNT = 4;
        public final static int ENTITY_COUNT = 3;
        public final static int ATTRIBUTE_COUNT = 2;
        public final static int MEASUREMENT_COUNT = 10;
        public final static int MEASUREMENT_SUB_SAMPLE = 5;

        public DatabasePopulator() {
        }

        public boolean initialise(EccConfiguration config) {
            logger.info("Trying to initialise database populator");

            dataReady = false;

            testData = null;
            expDataManager = null;
            experimentDAO = null;
            metricGenDAO = null;
            reportDAO = null;

            if (config != null) {
                DatabaseConfiguration dbConfig = config.getDatabaseConfig();
                if (dbConfig != null) {
                    Properties props = new Properties();
                    props.put("dbPassword", dbConfig.getUserPassword());
                    props.put("dbName", dbConfig.getDatabaseName());
                    props.put("dbType", dbConfig.getDatabaseType());
                    props.put("dbURL", dbConfig.getUrl());
                    props.put("dbUsername", dbConfig.getUserName());

                    // Try getting data accessors
                    try {
                        expDataManager = EDMInterfaceFactory.getMonitoringEDM(props);

                        boolean accessible = expDataManager.isDatabaseSetUpAndAccessible();

                        Assert.assertTrue(accessible);

                        if (accessible) {
                            expDataManager.clearMetricsDatabase();
                            experimentDAO = expDataManager.getExperimentDAO();
                            metricGenDAO = expDataManager.getMetricGeneratorDAO();
                            reportDAO = expDataManager.getReportDAO();

                            if (experimentDAO != null
                                    && metricGenDAO != null
                                    && reportDAO != null) {
                                createData();

                                dataReady = pushToDatabase();

                                logger.info("initialised ok");
                            }
                        }
                    } catch (Exception ex) {
                        Assert.fail(ex.getMessage());
                    }
                }
            }

            Assert.assertTrue(dataReady);

            return dataReady;
        }

        public boolean isDataReady() {
            return dataReady;
        }

        public boolean doesExperimentExist(Experiment exp) {
            if (exp != null) {
                return testData.containsKey(exp.getUUID());
            }

            return false;
        }

        public Set<UUID> getExperimentIDs() {
            return testData.keySet();
        }

        public Experiment getExperiment(UUID expID) {
            return testData.get(expID);
        }

        public Map<UUID, Entity> getExperimentEntities(UUID expID) {
            Map<UUID, Entity> entities = new HashMap<UUID, Entity>();

            if (expID != null) {
                Experiment exp = testData.get(expID);
                if (exp != null) {
                    entities = MetricHelper.getAllEntities(exp.getMetricGenerators());
                }
            }

            return entities;
        }

        public Map<UUID, Attribute> getExperimentAttributes(UUID expID) {
            Map<UUID, Attribute> attributes = new HashMap<UUID, Attribute>();

            for (Entity entity : getExperimentEntities(expID).values()) {
                for (Attribute attr : entity.getAttributes()) {
                    attributes.put(attr.getUUID(), attr);
                }
            }

            return attributes;
        }

        public Map<UUID, MeasurementSet> getMeasurementSetsForAttribute(UUID attrID, UUID expID) {
            HashMap<UUID, MeasurementSet> resultSet = new HashMap<UUID, MeasurementSet>();

            if (attrID != null && expID != null) {
                Experiment exp = getExperiment(expID);
                Attribute attr = getExperimentAttributes(exp.getUUID()).get(attrID);

                Map<UUID, MeasurementSet> msets
                        = MetricHelper.getAllMeasurementSets(exp.getMetricGenerators());

                for (MeasurementSet ms : msets.values()) {
                    if (ms.getAttributeID().equals(attrID)) {
                        resultSet.put(ms.getID(), ms);
                        break;
                    }
                }
            }

            return resultSet;
        }

        private void createData() {
            logger.info("Creating test data");

            testData = new HashMap<UUID, Experiment>();

            for (int i = 0; i < EXPERIMENT_COUNT; i++) {
                Experiment exp = new Experiment();
                exp.setName("Test Experiment " + i);
                exp.setDescription("Test experiment description " + i);
                exp.setStartTime(new Date());

                createExperimentData(i, exp);
                testData.put(exp.getUUID(), exp);
            }

            logger.info("Finished creating data");
        }

        // Private methods -----------------------------------------------------
        private void createExperimentData(int index, Experiment exp) {
            for (int i = 0; i < METRIC_GENERATOR_COUNT; i++) {
                // Metric Generator and Group
                MetricGenerator metGen = new MetricGenerator();
                metGen.setName("EXP" + index + " Metric generator" + i);

                MetricGroup mg = MetricHelper.createMetricGroup(metGen.getName() + " Metric Group",
                        "", metGen);

                // Entities and associated measurements to go with
                for (int u = 0; u < ENTITY_COUNT; u++) {
                    String eName = metGen.getName() + " Entity " + u;

                    Entity entity = createEntityAndData(eName, mg);
                    metGen.addEntity(entity);
                }

                exp.addMetricGenerator(metGen);
            }
        }

        private Entity createEntityAndData(String eName, MetricGroup mg) {
            Entity entity = new Entity();
            entity.setName(eName);

            // Create some attributes
            for (int i = 0; i < ATTRIBUTE_COUNT; i++) {
                String aName = eName + " Attribute " + i;
                Attribute attr = MetricHelper.createAttribute(aName, "No desc", entity);

                createAttributeData(attr, mg);
            }

            return entity;
        }

        private void createAttributeData(Attribute attr, MetricGroup mg) {
            MeasurementSet ms = MetricHelper.createMeasurementSet(attr,
                    MetricType.NOMINAL,
                    new Unit("Test unit"),
                    mg);

            // Create some actual measurements (duplicate some values for frequency tests later)
            Measurement m = new Measurement("A");
            m.setTimeStamp(new Date(timeInc));
            ms.addMeasurement(m);
            timeInc += 1000;

            m = new Measurement("B");
            m.setTimeStamp(new Date(timeInc));
            ms.addMeasurement(m);
            timeInc += 1000;

            m = new Measurement("B");
            m.setTimeStamp(new Date(timeInc));
            ms.addMeasurement(m);
            timeInc += 1000;

            m = new Measurement("C");
            m.setTimeStamp(new Date(timeInc));
            ms.addMeasurement(m);
            timeInc += 1000;

            m = new Measurement("C");
            m.setTimeStamp(new Date(timeInc));
            ms.addMeasurement(m);
            timeInc += 1000;

            m = new Measurement("C");
            m.setTimeStamp(new Date(timeInc));
            ms.addMeasurement(m);
            timeInc += 1000;

            m = new Measurement("D");
            m.setTimeStamp(new Date(timeInc));
            ms.addMeasurement(m);
            timeInc += 1000;

            m = new Measurement("D");
            m.setTimeStamp(new Date(timeInc));
            ms.addMeasurement(m);
            timeInc += 1000;

            m = new Measurement("D");
            m.setTimeStamp(new Date(timeInc));
            ms.addMeasurement(m);
            timeInc += 1000;

            m = new Measurement("D");
            m.setTimeStamp(new Date(timeInc));
            ms.addMeasurement(m);
        }

        private boolean pushToDatabase() {
            boolean result = true;

            logger.info("Sending test data to database");

            for (Experiment exp : testData.values()) {
                try {
                    // Save experiment
                    experimentDAO.saveExperiment(exp);

                    // and the data
                    for (MetricGenerator mg : exp.getMetricGenerators()) {
                        Map<UUID, MeasurementSet> msSets = MetricHelper.getAllMeasurementSets(mg);

                        for (MeasurementSet ms : msSets.values()) {
                            Date now = new Date();
                            reportDAO.saveReport(MetricHelper.createMeasurementReport(ms, now, now), true);
                        }
                    }
                } catch (Exception ex) {
                    logger.error("Data send failure: " + ex.getMessage());
                    result = false;
                }
            }
            logger.info("Finished sending test data to database");

            return result;
        }
    }
}
