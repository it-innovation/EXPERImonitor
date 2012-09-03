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
//      Created Date :          2012-08-28
//      Created for Project :   
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.test;

import java.util.Date;
import java.util.Random;
import java.util.UUID;
import static javax.measure.unit.SI.*;
import static javax.measure.unit.NonSI.*;
import javax.measure.unit.Unit;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Metric;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricType;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.ExperimentDataManager;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IEntityDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IReportDAO;

/**
 * A helper class to populate the metrics database with testdata.
 * 
 * @author Vegard Engen
 */
public class PopulateDB
{
    public static UUID expUUID = UUID.fromString("bfe4c710-61ba-46f8-a519-be2f7808192e");
    
    public static UUID entity1UUID = UUID.fromString("5718cd67-4310-4b2c-aeb9-9b72314630ca");
    public static UUID entity1attribute1UUID = UUID.fromString("4f2817b5-603a-4d02-a032-62cfca314962");
    public static UUID entity1attribute2UUID = UUID.fromString("cd42b215-5235-4591-8be5-2d403911cb59");
    public static UUID entity1attribute3UUID = UUID.fromString("a460987f-2ef8-4519-91f2-4a23954b16bd");
    
    public static UUID entity2UUID = UUID.fromString("6718cd67-4310-4b2c-aeb9-9b72314630ca");
    public static UUID entity2attribute1UUID = UUID.fromString("6f2817b5-603a-4d02-a032-62cfca314962");
    public static UUID entity2attribute2UUID = UUID.fromString("6d42b215-5235-4591-8be5-2d403911cb59");
    public static UUID entity2attribute3UUID = UUID.fromString("6460987f-2ef8-4519-91f2-4a23954b16bd");
    
    public static UUID entity3UUID = UUID.fromString("7718cd67-4310-4b2c-aeb9-9b72314630ca");
    public static UUID entity3attribute1UUID = UUID.fromString("7f2817b5-603a-4d02-a032-62cfca314962");
    public static UUID entity3attribute2UUID = UUID.fromString("7d42b215-5235-4591-8be5-2d403911cb59");
    
    public static UUID mGen1UUID = UUID.fromString("782e5097-2e29-4219-a984-bf48dfcd7f63");
    public static UUID mGrp1UUID = UUID.fromString("189064a5-f1d8-41f2-b2c1-b88776841009");
    public static UUID mGrp1mSet1UUID = UUID.fromString("2b915932-41b1-45d7-b4f6-2de4f30020b8");
    public static UUID mGrp1mSet2UUID = UUID.fromString("3b915932-41b1-45d7-b4f6-2de4f30020b7");
    public static UUID mGrp1mSet3UUID = UUID.fromString("4b915932-41b1-45d7-b4f6-2de4f30020b6");
    
    public static UUID mGen2UUID = UUID.fromString("882e5097-2e29-4219-a984-bf48dfcd7f63");
    public static UUID mGrp2UUID = UUID.fromString("889064a5-f1d8-41f2-b2c1-b88776841009");
    public static UUID mGrp2mSet1UUID = UUID.fromString("8b915932-41b1-45d7-b4f6-2de4f30020b8");
    public static UUID mGrp2mSet2UUID = UUID.fromString("8b915932-41b1-45d7-b4f6-2de4f30020b7");
    public static UUID mGrp2mSet3UUID = UUID.fromString("8b915932-41b1-45d7-b4f6-2de4f30020b6");
    
    public static UUID mGen3UUID = UUID.fromString("982e5097-2e29-4219-a984-bf48dfcd7f63");
    public static UUID mGrp3UUID = UUID.fromString("989064a5-f1d8-41f2-b2c1-b88776841009");
    public static UUID mGrp3mSet1UUID = UUID.fromString("9b915932-41b1-45d7-b4f6-2de4f30020b8");
    public static UUID mGrp3mSet2UUID = UUID.fromString("9b915932-41b1-45d7-b4f6-2de4f30020b7");
    
    
    
    static Logger log = Logger.getLogger(PopulateDB.class);
    
    /**
     * Main method that makes a call to the populateWithTestData() method.
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception
    {
        populateWithTestData();
    }
    
    /**
     * A method to populate the database with an entity, experiment, metric generator,
     * metric group, measurement sets and reports with measurements for each of the
     * measurement sets.
     * @throws Exception 
     */
    public static void populateWithTestData() throws Exception
    {
        ExperimentDataManager edm = new ExperimentDataManager();
        
        saveExperiment(edm, expUUID);
        
        saveEntity1(edm, entity1UUID, entity1attribute1UUID, entity1attribute2UUID, entity1attribute3UUID);
        saveEntity2(edm, entity2UUID, entity2attribute1UUID, entity2attribute2UUID, entity2attribute3UUID);
        saveEntity3(edm, entity3UUID, entity3attribute1UUID, entity3attribute2UUID);
        
        saveMetricGenerator1(edm, expUUID, entity1UUID, entity1attribute1UUID, entity1attribute2UUID, entity1attribute3UUID, mGen1UUID, mGrp1UUID, mGrp1mSet1UUID, mGrp1mSet2UUID, mGrp1mSet3UUID);
        saveMetricGenerator2(edm, expUUID, entity2UUID, entity2attribute1UUID, entity2attribute2UUID, entity2attribute3UUID, mGen2UUID, mGrp2UUID, mGrp2mSet1UUID, mGrp2mSet2UUID, mGrp2mSet3UUID);
        saveMetricGenerator3(edm, expUUID, entity3UUID, entity3attribute1UUID, entity3attribute2UUID, mGen3UUID, mGrp3UUID, mGrp3mSet1UUID, mGrp3mSet2UUID);
        
        saveReportWithRandomMeasurements(edm, mGrp1mSet1UUID, 20, 2000, 6000);
        saveReportWithRandomMeasurements(edm, mGrp1mSet2UUID, 20, 500000, 1000000);
        saveReportWithRandomMeasurements(edm, mGrp1mSet3UUID, 20, 100, 400);
        
        saveReportWithRandomMeasurements(edm, mGrp2mSet1UUID, 20, 5, 20);
        saveReportWithRandomMeasurements(edm, mGrp2mSet2UUID, 20, 5, 20);
        saveReportWithRandomMeasurements(edm, mGrp2mSet3UUID, 20, 20, 40);
        
        saveReportWithRandomMeasurements(edm, mGrp3mSet1UUID, 20, 150, 300);
        saveReportWithRandomMeasurements(edm, mGrp3mSet2UUID, 20, 100, 999);
        
        printDetailsForExperiment(edm, expUUID);
    }
    
    public static void saveExperiment(ExperimentDataManager edm, UUID expUUID) throws Exception
    {
        IExperimentDAO expDAO = null;
        try {
            expDAO = edm.getExperimentDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Experiment DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        log.info("Creating experiment");
        Experiment exp = new Experiment();
        exp.setUUID(expUUID);
        exp.setName("Experiment");
        exp.setDescription("A test experiment");
        exp.setStartTime(new Date(Long.parseLong("1345642421005")));
        exp.setEndTime(new Date(Long.parseLong("1440413831014")));
        exp.setExperimentID("/locations/experiment/1337");
        
        log.info("Saving experiment");
        try {
            expDAO.saveExperiment(exp);
            log.info("Experiment '" + exp.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save experiment: " + ex.getMessage());
        }
    }
    
    /**
     * Saves an entity (VM) with three attributes (CPU, Network, Disk).
     * @param edm Experiment Data Manager object to save the data with.
     * @param entityUUID The UUID of the Entity.
     * @param cpuAttributeUUID The UUID of the CPU attribute of the entity.
     * @param networkAttributeUUID The UUID of the Network attribute of the entity.
     * @param diskAttributeUUID The UUID of the Disk attribute of the entity.
     * @throws Exception 
     */
    public static void saveEntity1(ExperimentDataManager edm, UUID entityUUID, UUID cpuAttributeUUID, UUID networkAttributeUUID, UUID diskAttributeUUID) throws Exception
    {
        IEntityDAO entityDAO = null;
        try {
            entityDAO = edm.getEntityDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Entity DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        log.info("Saving VM entity");
        Entity entity = new Entity();
        entity.setUUID(entityUUID);
        entity.setName("VM");
        entity.setDescription("A Virtual Machine");
        entity.addtAttribute(new Attribute(cpuAttributeUUID, entityUUID, "CPU", "CPU performance"));
        entity.addtAttribute(new Attribute(networkAttributeUUID, entityUUID, "Network", "Network performance"));
        entity.addtAttribute(new Attribute(diskAttributeUUID, entityUUID, "Disk", "Disk performance"));
        
        try {
            entityDAO.saveEntity(entity);
            log.info("Entity '" + entity.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save entity: " + ex.getMessage());
        }
        
        /*log.info("Getting Entity from the DB");
        Entity entityFromDB = null;
        try {
            entityFromDB = entityDAO.getEntity(entityUUID, true);
        } catch (Exception ex) {
            log.error("Unable to get entity: " + ex.getMessage());
        }
        
        log.info("Entity details:");
        if (entityFromDB.getUUID() != null) log.info("  - UUID:  " + entityFromDB.getUUID());
        if (entityFromDB.getName() != null) log.info("  - Name:  " + entityFromDB.getName());
        if (entityFromDB.getDescription() != null) log.info("  - Desc:  " + entityFromDB.getDescription());
        if ((entityFromDB.getAttributes() == null) || entityFromDB.getAttributes().isEmpty()) {
            log.info("  - There are NO attributes");
        } else {
            log.info("  - There are " + entityFromDB.getAttributes().size() + " attributes");
            for (Attribute attrib : entityFromDB.getAttributes())
            {
                if (attrib != null) {
                    log.info("    - Attribute details:");
                    if (attrib.getUUID() != null) log.info("      - UUID:  " + attrib.getUUID());
                    if (attrib.getName() != null) log.info("      - Name:  " + attrib.getName());
                    if (attrib.getDescription() != null) log.info("      - Desc:  " + attrib.getDescription());
                }
            }
        }*/
    }
    
    /**
     * Saves an entity (VM) with three attributes (CPU, Network, Disk).
     * @param edm Experiment Data Manager object to save the data with.
     * @param entityUUID The UUID of the Entity.
     * @param cpuAttributeUUID The UUID of the CPU attribute of the entity.
     * @param networkAttributeUUID The UUID of the Network attribute of the entity.
     * @param diskAttributeUUID The UUID of the Disk attribute of the entity.
     * @throws Exception 
     */
    public static void saveEntity2(ExperimentDataManager edm, UUID entityUUID, UUID cpuAttributeUUID, UUID networkAttributeUUID, UUID diskAttributeUUID) throws Exception
    {
        IEntityDAO entityDAO = null;
        try {
            entityDAO = edm.getEntityDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Entity DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        log.info("Saving AVC entity");
        Entity entity = new Entity();
        entity.setUUID(entityUUID);
        entity.setName("AVC");
        entity.setDescription("Audio Visual Component");
        entity.addtAttribute(new Attribute(cpuAttributeUUID, entityUUID, "File ingest", ""));
        entity.addtAttribute(new Attribute(networkAttributeUUID, entityUUID, "AV streams out", ""));
        entity.addtAttribute(new Attribute(diskAttributeUUID, entityUUID, "Frame rate", ""));
        
        try {
            entityDAO.saveEntity(entity);
            log.info("Entity '" + entity.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save entity: " + ex.getMessage());
        }
        
        /*log.info("Getting Entity from the DB");
        Entity entityFromDB = null;
        try {
            entityFromDB = entityDAO.getEntity(entityUUID, true);
        } catch (Exception ex) {
            log.error("Unable to get entity: " + ex.getMessage());
        }
        
        log.info("Entity details:");
        if (entityFromDB.getUUID() != null) log.info("  - UUID:  " + entityFromDB.getUUID());
        if (entityFromDB.getName() != null) log.info("  - Name:  " + entityFromDB.getName());
        if (entityFromDB.getDescription() != null) log.info("  - Desc:  " + entityFromDB.getDescription());
        if ((entityFromDB.getAttributes() == null) || entityFromDB.getAttributes().isEmpty()) {
            log.info("  - There are NO attributes");
        } else {
            log.info("  - There are " + entityFromDB.getAttributes().size() + " attributes");
            for (Attribute attrib : entityFromDB.getAttributes())
            {
                if (attrib != null) {
                    log.info("    - Attribute details:");
                    if (attrib.getUUID() != null) log.info("      - UUID:  " + attrib.getUUID());
                    if (attrib.getName() != null) log.info("      - Name:  " + attrib.getName());
                    if (attrib.getDescription() != null) log.info("      - Desc:  " + attrib.getDescription());
                }
            }
        }*/
    }
    
    /**
     * Saves an entity (VM) with three attributes (CPU, Network, Disk).
     * @param edm Experiment Data Manager object to save the data with.
     * @param entityUUID The UUID of the Entity.
     * @param cpuAttributeUUID The UUID of the CPU attribute of the entity.
     * @param networkAttributeUUID The UUID of the Network attribute of the entity.
     * @param diskAttributeUUID The UUID of the Disk attribute of the entity.
     * @throws Exception 
     */
    public static void saveEntity3(ExperimentDataManager edm, UUID entityUUID, UUID cpuAttributeUUID, UUID networkAttributeUUID) throws Exception
    {
        IEntityDAO entityDAO = null;
        try {
            entityDAO = edm.getEntityDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Entity DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        log.info("Saving POI entity");
        Entity entity = new Entity();
        entity.setUUID(entityUUID);
        entity.setName("POI Service");
        entity.setDescription("Point Of Interest Service");
        entity.addtAttribute(new Attribute(cpuAttributeUUID, entityUUID, "POI requests", "The number of POI requests"));
        entity.addtAttribute(new Attribute(networkAttributeUUID, entityUUID, "POI response time", "The response time for the POI requests"));
        
        try {
            entityDAO.saveEntity(entity);
            log.info("Entity '" + entity.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save entity: " + ex.getMessage());
        }
        
        /*log.info("Getting Entity from the DB");
        Entity entityFromDB = null;
        try {
            entityFromDB = entityDAO.getEntity(entityUUID, true);
        } catch (Exception ex) {
            log.error("Unable to get entity: " + ex.getMessage());
        }
        
        log.info("Entity details:");
        if (entityFromDB.getUUID() != null) log.info("  - UUID:  " + entityFromDB.getUUID());
        if (entityFromDB.getName() != null) log.info("  - Name:  " + entityFromDB.getName());
        if (entityFromDB.getDescription() != null) log.info("  - Desc:  " + entityFromDB.getDescription());
        if ((entityFromDB.getAttributes() == null) || entityFromDB.getAttributes().isEmpty()) {
            log.info("  - There are NO attributes");
        } else {
            log.info("  - There are " + entityFromDB.getAttributes().size() + " attributes");
            for (Attribute attrib : entityFromDB.getAttributes())
            {
                if (attrib != null) {
                    log.info("    - Attribute details:");
                    if (attrib.getUUID() != null) log.info("      - UUID:  " + attrib.getUUID());
                    if (attrib.getName() != null) log.info("      - Name:  " + attrib.getName());
                    if (attrib.getDescription() != null) log.info("      - Desc:  " + attrib.getDescription());
                }
            }
        }*/
    }
    
    /**
     * Saves a metric generator complete chain of a  metric group, three measurement 
     * sets (one for each of the attributes of the entity created in the saveEntities() method).
     * @param edm Experiment Data Manager object to save the data with.
     * @param expUUID The Experiment UUID.
     * @param entityUUID The Entity UUID.
     * @param attrib1UUID The UUID of the 1st attribute of the entity.
     * @param attrib2UUID The UUID of the 2nd attribute of the entity.
     * @param attrib3UUID The UUID of the 3rd attribute of the entity.
     * @param mGenUUID The UUID of the metric generator
     * @param mGrpUUID The UUID of the metric group.
     * @param mSet1UUID The UUID of the 1st measurement set.
     * @param mSet2UUID The UUID of the 2nd measurement set.
     * @param mSet3UUID The UUID of the 3rd measurement set.
     * @throws Exception 
     */
    public static void saveMetricGenerator1(ExperimentDataManager edm, UUID expUUID, UUID entityUUID, UUID attrib1UUID, UUID attrib2UUID, UUID attrib3UUID, UUID mGenUUID, UUID mGrpUUID, UUID mSet1UUID, UUID mSet2UUID, UUID mSet3UUID) throws Exception
    {
//----- EXPERIMENT
        IMetricGeneratorDAO mGenDAO = null;
        try {
            mGenDAO = edm.getMetricGeneratorDAO();
        } catch (Exception ex) {
            log.error ("Unable to get MetricGenerator DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
//----- METRIC GENERATOR
        log.info("Creating VM MetricGenerator");
        MetricGenerator metricGenerator = new MetricGenerator(mGenUUID, "VM MetricGenerator", "A metric generator");
        metricGenerator.addEntity(new Entity(entityUUID));
        
//----- METRIC GROUP
        log.info("Creating QoS MetricGroup");
        MetricGroup metricGroup = new MetricGroup(mGrpUUID, mGenUUID, "Quality of Service", "A group of QoS metrics");
        metricGenerator.addMetricGroup(metricGroup);
        
//----- MEASUREMENT SETS
        log.info("Creating 1st QoS Measurement set");
        Metric metric1 = new Metric(UUID.randomUUID(), MetricType.RATIO, MILLI(SECOND));
        MeasurementSet mSet1 = new MeasurementSet(mSet1UUID, attrib1UUID, mGrpUUID, metric1);
        metricGroup.addMeasurementSets(mSet1);
        
        log.info("Creating 2nd QoS Measurement set");
        Metric metric2 = new Metric(UUID.randomUUID(), MetricType.RATIO, BIT.divide(SECOND));
        MeasurementSet mSet2 = new MeasurementSet(mSet2UUID, attrib2UUID, mGrpUUID, metric2);
        metricGroup.addMeasurementSets(mSet2);
        
        log.info("Creating 3rd QoS Measurement set");
        Metric metric3 = new Metric(UUID.randomUUID(), MetricType.RATIO, MILLI(SECOND));
        MeasurementSet mSet3 = new MeasurementSet(mSet3UUID, attrib3UUID, mGrpUUID, metric3);
        metricGroup.addMeasurementSets(mSet3);
        
        log.info("Saving metric generator (with all sub-classes)");
        try {
            mGenDAO.saveMetricGenerator(metricGenerator, expUUID);
            log.info("MetricGenerator '" + metricGenerator.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save MetricGenerator: " + ex.getMessage());
        }
    }
    
    /**
     * Saves a metric generator complete chain of a  metric group, three measurement 
     * sets (one for each of the attributes of the entity created in the saveEntities() method).
     * @param edm Experiment Data Manager object to save the data with.
     * @param expUUID The Experiment UUID.
     * @param entityUUID The Entity UUID.
     * @param attrib1UUID The UUID of the 1st attribute of the entity.
     * @param attrib2UUID The UUID of the 2nd attribute of the entity.
     * @param attrib3UUID The UUID of the 3rd attribute of the entity.
     * @param mGenUUID The UUID of the metric generator
     * @param mGrpUUID The UUID of the metric group.
     * @param mSet1UUID The UUID of the 1st measurement set.
     * @param mSet2UUID The UUID of the 2nd measurement set.
     * @param mSet3UUID The UUID of the 3rd measurement set.
     * @throws Exception 
     */
    public static void saveMetricGenerator2(ExperimentDataManager edm, UUID expUUID, UUID entityUUID, UUID attrib1UUID, UUID attrib2UUID, UUID attrib3UUID, UUID mGenUUID, UUID mGrpUUID, UUID mSet1UUID, UUID mSet2UUID, UUID mSet3UUID) throws Exception
    {
//----- EXPERIMENT
        IMetricGeneratorDAO mGenDAO = null;
        try {
            mGenDAO = edm.getMetricGeneratorDAO();
        } catch (Exception ex) {
            log.error ("Unable to get MetricGenerator DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
//----- METRIC GENERATOR
        log.info("Creating AVC MetricGenerator");
        MetricGenerator metricGenerator = new MetricGenerator(mGenUUID, "AVC MetricGenerator", "A metric generator");
        metricGenerator.addEntity(new Entity(entityUUID));
        
//----- METRIC GROUP
        log.info("Creating QoS MetricGroup");
        MetricGroup metricGroup = new MetricGroup(mGrpUUID, mGenUUID, "Quality of Service", "A group of QoS metrics");
        metricGenerator.addMetricGroup(metricGroup);
        
//----- MEASUREMENT SETS
        log.info("Creating 1st QoS Measurement set");
        Metric metric1 = new Metric(UUID.randomUUID(), MetricType.RATIO, Unit.ONE.alternate("Files ingested").divide(MINUTE));
        MeasurementSet mSet1 = new MeasurementSet(mSet1UUID, attrib1UUID, mGrpUUID, metric1);
        metricGroup.addMeasurementSets(mSet1);
        
        log.info("Creating 2nd QoS Measurement set");
        Metric metric2 = new Metric(UUID.randomUUID(), MetricType.RATIO, Unit.ONE.alternate("AV streams out").divide(MINUTE));
        MeasurementSet mSet2 = new MeasurementSet(mSet2UUID, attrib2UUID, mGrpUUID, metric2);
        metricGroup.addMeasurementSets(mSet2);
        
        log.info("Creating 3rd QoS Measurement set");
        Metric metric3 = new Metric(UUID.randomUUID(), MetricType.RATIO, Unit.ONE.alternate("Average frame rate").divide(SECOND));
        MeasurementSet mSet3 = new MeasurementSet(mSet3UUID, attrib3UUID, mGrpUUID, metric3);
        metricGroup.addMeasurementSets(mSet3);
        
        log.info("Saving metric generator (with all sub-classes)");
        try {
            mGenDAO.saveMetricGenerator(metricGenerator, expUUID);
            log.info("MetricGenerator '" + metricGenerator.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save MetricGenerator: " + ex.getMessage());
        }
    }
    
    /**
     * Saves a metric generator complete chain of a  metric group, two measurement 
     * sets (one for each of the attributes of the entity created in the saveEntities() method).
     * @param edm Experiment Data Manager object to save the data with.
     * @param expUUID The Experiment UUID.
     * @param entityUUID The Entity UUID.
     * @param attrib1UUID The UUID of the 1st attribute of the entity.
     * @param attrib2UUID The UUID of the 2nd attribute of the entity.
     * @param mGenUUID The UUID of the metric generator
     * @param mGrpUUID The UUID of the metric group.
     * @param mSet1UUID The UUID of the 1st measurement set.
     * @param mSet2UUID The UUID of the 2nd measurement set.
     * @throws Exception 
     */
    public static void saveMetricGenerator3(ExperimentDataManager edm, UUID expUUID, UUID entityUUID, UUID attrib1UUID, UUID attrib2UUID, UUID mGenUUID, UUID mGrpUUID, UUID mSet1UUID, UUID mSet2UUID) throws Exception
    {
//----- EXPERIMENT
        IMetricGeneratorDAO mGenDAO = null;
        try {
            mGenDAO = edm.getMetricGeneratorDAO();
        } catch (Exception ex) {
            log.error ("Unable to get MetricGenerator DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
//----- METRIC GENERATOR
        log.info("Creating POI Service MetricGenerator");
        MetricGenerator metricGenerator = new MetricGenerator(mGenUUID, "POI Service MetricGenerator", "A metric generator");
        metricGenerator.addEntity(new Entity(entityUUID));
        
//----- METRIC GROUP
        log.info("Creating QoS MetricGroup");
        MetricGroup metricGroup = new MetricGroup(mGrpUUID, mGenUUID, "Quality of Service", "A group of QoS metrics");
        metricGenerator.addMetricGroup(metricGroup);
        
//----- MEASUREMENT SETS
        log.info("Creating 1st QoS Measurement set");
        Metric metric1 = new Metric(UUID.randomUUID(), MetricType.RATIO, Unit.ONE.alternate("POI requests").divide(MINUTE));
        MeasurementSet mSet1 = new MeasurementSet(mSet1UUID, attrib1UUID, mGrpUUID, metric1);
        metricGroup.addMeasurementSets(mSet1);
        
        log.info("Creating 2nd QoS Measurement set");
        Metric metric2 = new Metric(UUID.randomUUID(), MetricType.RATIO, MILLI(SECOND));
        MeasurementSet mSet2 = new MeasurementSet(mSet2UUID, attrib2UUID, mGrpUUID, metric2);
        metricGroup.addMeasurementSets(mSet2);
        
        log.info("Saving metric generator (with all sub-classes)");
        try {
            mGenDAO.saveMetricGenerator(metricGenerator, expUUID);
            log.info("MetricGenerator '" + metricGenerator.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save MetricGenerator: " + ex.getMessage());
        }
    }
    
    /**
     * Print the details for the experiment (to log file). Will use the EDM to get
     * the experiment from the storage.
     * 
     * @param edm Experiment Data Manager object to save the data with.
     * @param expUUID The experiment UUID.
     * @throws Exception 
     */
    public static void printDetailsForExperiment(ExperimentDataManager edm, UUID expUUID) throws Exception
    {
        IExperimentDAO expDAO = null;
        try {
            expDAO = edm.getExperimentDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Experiment DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        IEntityDAO entityDAO = null;
        try {
            entityDAO = edm.getEntityDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Entity DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        log.info("Getting experiment object");
        Experiment expFromDB = null;
        try {
            expFromDB = expDAO.getExperiment(expUUID, true);
        } catch (Exception ex) {
            log.error("Unable to get experiment: " + ex.getMessage());
        }
        
        log.info("Experiment details:");
        log.info("  * Basic info");
        if (expFromDB.getUUID() != null) log.info("    - UUID:  " + expFromDB.getUUID());
        if (expFromDB.getName() != null) log.info("    - Name:  " + expFromDB.getName());
        if (expFromDB.getDescription() != null) log.info("    - Desc:  " + expFromDB.getDescription());
        if (expFromDB.getStartTime() != null) log.info("    - Start: " + expFromDB.getStartTime() + " (" + expFromDB.getStartTime().getTime() + ")");
        if (expFromDB.getEndTime() != null) log.info("    - End:   " + expFromDB.getEndTime() + " (" + expFromDB.getEndTime().getTime() + ")");
        if (expFromDB.getExperimentID() != null) log.info("    - ID:    " + expFromDB.getExperimentID());
        
        if ((expFromDB.getMetricGenerators() == null) || expFromDB.getMetricGenerators().isEmpty()) {
            log.info("  * There are NO metric generators");
        } else {
            log.info("  * There's " + expFromDB.getMetricGenerators().size() + " metric generator(s)");
            
            for (MetricGenerator mGen : expFromDB.getMetricGenerators())
            {
                log.info("    - MetricGenerator details:");
                log.info("      - UUID: " + mGen.getUUID());
                log.info("      - Name: " + mGen.getName());
                if (mGen.getDescription() != null)
                    log.info("      - Desc: " + mGen.getDescription());
                
                if ((mGen.getMetricGroups() == null) || mGen.getMetricGroups().isEmpty()){
                    log.info("      * There are NO metric groups");
                } else {
                    log.info("      * There's " + mGen.getMetricGroups().size() + " metric group(s)");
                    
                    for (MetricGroup mGrp : mGen.getMetricGroups())
                    {
                        log.info("        - MetricGroup details:");
                        log.info("          - UUID: " + mGrp.getUUID());
                        log.info("          - Name: " + mGrp.getName());
                        if (mGrp.getDescription() != null)
                            log.info("          - Desc: " + mGrp.getDescription());
                        
                        if ((mGrp.getMeasurementSets() == null) || mGrp.getMeasurementSets().isEmpty()){
                            log.info("          * There are NO measurement sets");
                        } else {
                            log.info("          * There's " + mGrp.getMeasurementSets().size() + " measurement set(s)");
                            
                            for (MeasurementSet mSet : mGrp.getMeasurementSets())
                            {
                                // get attribute
                                Attribute attrib = null;
                                try {
                                    attrib = entityDAO.getAttribute(mSet.getAttributeUUID());
                                } catch (Exception ex) {
                                    log.error("Unable to get attribute instance according to the UUID: " + mSet.getAttributeUUID().toString());
                                }
                                
                                log.info("            - MeasurementSet details:");
                                log.info("              - UUID: " + mSet.getUUID());
                                log.info("              - Attribute:");
                                if (attrib != null) {
                                    if (attrib.getUUID() != null) log.info("                  - UUID:  " + attrib.getUUID());
                                    if (attrib.getName() != null) log.info("                  - Name:  " + attrib.getName());
                                    if (attrib.getDescription() != null) log.info("                  - Desc:  " + attrib.getDescription());
                                } else {
                                    log.info("                - ERROR: got NULL Attribute from the DB!");
                                }
                                log.info("              - Metric:");
                                if (mSet.getMetric().getUUID() != null) log.info("                  - UUID:  " + mSet.getMetric().getUUID());
                                if (mSet.getMetric().getMetricType() != null) log.info("                  - Type:  " + mSet.getMetric().getMetricType());
                                if (mSet.getMetric().getUnit() != null) log.info("                  - Unit:  " + mSet.getMetric().getUnit());
                            }
                        } // end else there's measurement sets
                    } // end for each metric group
                    
                    if ((mGen.getEntities() == null) || mGen.getEntities().isEmpty()){
                        log.info("      * There are NO entities in the metric generator");
                    } else {
                        log.info("      * There's " + mGen.getMetricGroups().size() + " entity/entities in the metric generator");

                        for (Entity entity : mGen.getEntities())
                        {
                            if (entity.getUUID() != null) log.info("        - UUID:  " + entity.getUUID());
                            if (entity.getName() != null) log.info("        - Name:  " + entity.getName());
                            if (entity.getDescription() != null) log.info("        - Desc:  " + entity.getDescription());
                            if ((entity.getAttributes() == null) || entity.getAttributes().isEmpty()) {
                                log.info("        - There are NO attributes");
                            } else {
                                log.info("        - There are " + entity.getAttributes().size() + " attributes");
                                for (Attribute attrib : entity.getAttributes())
                                {
                                    if (attrib != null) {
                                        log.info("          - Attribute details:");
                                        if (attrib.getUUID() != null) log.info("            - UUID:  " + attrib.getUUID());
                                        if (attrib.getName() != null) log.info("            - Name:  " + attrib.getName());
                                        if (attrib.getDescription() != null) log.info("            - Desc:  " + attrib.getDescription());
                                    }
                                }
                            }
                        }
                    } // end else there's one or more entities
                } // end else there's one or more metric groups
            } // end for each metric generator
        } // end else there's one or more metric generators
        
        
    }
    
    /**
     * Save a report with random measurements for a particular measurement set,
     * given the number of measurements to create and min/max range for the
     * random numbers.
     * 
     * The timestamps of the measurements are calculated to be in the past,
     * 1/10 second apart starting from the current time the value is calculated:
     * 
     * long timeStamp = new Date().getTime() - (10000 * (numMeasurements-i));
     * 
     * Therefore, the greater the number of measurements calculated, the further
     * back in time the timestamps will be generated. Therefore, be careful not 
     * to call this method too often with a too great number of measurements to
     * generate, else you'll create measurements with timestamps before measurements
     * already generated.
     * 
     * @param edm Experiment Data Manager object.
     * @param mSetUUID The measurement set that the report is for.
     * @param numMeasurements The number of measurements to create (randomly).
     * @param min The minimum value of the range for the random measurement values.
     * @param max The maximum value of the range for the random measurement values.
     * @throws Exception 
     */
    public static void saveReportWithRandomMeasurements(ExperimentDataManager edm, UUID mSetUUID, int numMeasurements, int min, int max) throws Exception
    {
        IReportDAO reportDAO = null;
        try {
            reportDAO = edm.getReportDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Report DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        log.info("Saving Report for measurement set " + mSetUUID.toString());
        
        MeasurementSet mSet = new MeasurementSet(mSetUUID);
        Random rand = new Random();
        rand.setSeed(new Date().getTime());
        long timeStampFrom = 0;
        long timeStampTo = 0;
        for (int i = 0; i < numMeasurements; i++)
        {
            long timeStamp = new Date().getTime() - (10000 * (numMeasurements-i));
            int value = (int)Math.round(scale (rand.nextDouble(), 0, 1, min, max));
            Measurement measurement = new Measurement(UUID.randomUUID(), mSetUUID, new Date(timeStamp), String.valueOf(value));
            mSet.addMeasurement(measurement);
            
            if (i == 0)
            {
                timeStampFrom = timeStamp;
                timeStampTo = timeStamp;
            }
            else
            {
                if (timeStamp > timeStampTo)
                    timeStampTo = timeStamp;
                else if (timeStamp < timeStampFrom)
                    timeStampFrom = timeStamp;
            }
        }
        
        Report report = new Report(UUID.randomUUID(), mSet, new Date(), new Date(timeStampFrom), new Date(timeStampTo), mSet.getMeasurements().size());
        
        try {
            reportDAO.saveReport(report);
            log.info("Report saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save Report: " + ex.getMessage(), ex);
            throw ex;
        }
    }
    
    /**
     * Scales a number from one range to another.
     * @param num The number to be scaled.
     * @param oldMin The minimum value of the old/original range.
     * @param oldMax The maximum value of the old/original range.
     * @param newMin The minimum value of the new range.
     * @param newMax The maximum value of the new range.
     * @return 
     */
    private static double scale (double num, double oldMin, double oldMax, double newMin, double newMax)
    {
       return (((num - oldMin) * (newMax - newMin)) / (oldMax - oldMin)) + newMin;
    }
}
