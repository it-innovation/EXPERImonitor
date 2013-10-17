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
//      Created Date :          2012-08-13
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.edm.test.metrics;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Metric;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricType;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Unit;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.loggin.impl.Log4JImpl;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.IECCLogger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.metrics.MonitoringEDMUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMeasurementDAO;

/**
 *
 * @author Vegard Engen
 */
public class EDMStorageTest
{
    static IECCLogger log;
    static final String fixedString32 = "10101010101010101010101010101010";
    
    public static void main(String[] args) throws Exception
    {
        // Configure logging system
        Logger.setLoggerImpl( new Log4JImpl() );
        log = Logger.getLogger(EDMStorageTest.class);
      
        Properties edmConfig = MonitoringEDMUtil.getConfigs();
        log.info("isConfigValid() = " + MonitoringEDMUtil.isConfigValid(edmConfig));
        IMonitoringEDM edm = EDMInterfaceFactory.getMonitoringEDM(edmConfig);
        
        UUID expUUID = UUID.fromString("bfe4c710-61ba-46f8-a519-be2f7808192e");
        UUID entityUUID = UUID.fromString("5718cd67-4310-4b2c-aeb9-9b72314630ca");
        UUID attributeUUID = UUID.fromString("4f2817b5-603a-4d02-a032-62cfca314962");
        UUID mGenUUID = UUID.fromString("782e5097-2e29-4219-a984-bf48dfcd7f63");
        UUID mGrpUUID = UUID.fromString("189064a5-f1d8-41f2-b2c1-b88776841009");
        UUID mSetUUID = UUID.fromString("2b915932-41b1-45d7-b4f6-2de4f30020b8");
        UUID reportUUID = UUID.fromString("165c8058-5c67-4f92-ae34-df7ee2129821");
        
        //log.info("Clearing the database");
        edm.clearMetricsDatabase();
        
//----- CASE 1
        
        //case1noMeasurements(edm, expUUID, entityUUID, attributeUUID, mGenUUID, mGrpUUID, mSetUUID);        
        //case1withMeasurements(edm, expUUID, entityUUID, attributeUUID, mGenUUID, mGrpUUID, mSetUUID, 86400);
        //case1withMeasurements(edm, expUUID, entityUUID, attributeUUID, mGenUUID, mGrpUUID, mSetUUID, 604800);
        //case1withMeasurements(edm, expUUID, entityUUID, attributeUUID, mGenUUID, mGrpUUID, mSetUUID, 2419200);

//----- CASE 2
        
        //case2noMeasurements(edm, expUUID, mGenUUID, mGrpUUID);
        //case2withMeasurements(edm, expUUID, mGenUUID, mGrpUUID, 86400); // 1 day
        //case2withMeasurements(edm, expUUID, mGenUUID, mGrpUUID, 172800); // 2 days
        //case2withMeasurements(edm, expUUID, mGenUUID, mGrpUUID, 259200); // 3 days
        //case2withMeasurements(edm, expUUID, mGenUUID, mGrpUUID, 345600); // 4 days
        //case2withMeasurements(edm, expUUID, mGenUUID, mGrpUUID, 432000); // 5 days
        //case2withMeasurements(edm, expUUID, mGenUUID, mGrpUUID, 518400); // 6 days
        //case2withMeasurements(edm, expUUID, mGenUUID, mGrpUUID, 604800); // 7 days
        
//----- CASE 3
        
        //case3noMeasurements(edm, expUUID, mGenUUID, mGrpUUID);
    }
    
    public static void case1noMeasurements(IMonitoringEDM edm, UUID expUUID, UUID entityUUID, UUID attribUUID, UUID mGenUUID, UUID mGrpUUID, UUID mSetUUID) throws Exception
    {
        log.info("============== CASE 1   --   WITHOUT MEASUREMENTS ==============");
        
        // clearing the database
        log.info("Clearing the database");
        edm.clearMetricsDatabase();
        
        IExperimentDAO expDAO = null;
        try {
            expDAO = edm.getExperimentDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Experiment DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
//----- EXPERIMENT
        log.info("Creating Experiment");
        Experiment exp = new Experiment();
        exp.setUUID(expUUID);
        exp.setName(fixedString32);
        exp.setDescription(fixedString32);
        exp.setStartTime(new Date(Long.parseLong("1345642421005")));
        exp.setEndTime(new Date(Long.parseLong("1440413831014")));
        exp.setExperimentID(fixedString32);
        
//----- METRIC GENERATOR
        log.info("Creating MetricGenerator");
        MetricGenerator metricGenerator = new MetricGenerator(mGenUUID, fixedString32, fixedString32);
        exp.addMetricGenerator(metricGenerator);
        
//----- ENTITY
        log.info("Creating Entity");
        Entity entity = new Entity(entityUUID, fixedString32, fixedString32, fixedString32);
        entity.addAttribute(new Attribute(attribUUID, entityUUID, fixedString32, fixedString32));
        metricGenerator.addEntity(entity);
        
//----- METRIC GROUP
        log.info("Creating MetricGroup");
        MetricGroup metricGroup = new MetricGroup(mGrpUUID, mGenUUID, fixedString32, fixedString32);
        metricGenerator.addMetricGroup(metricGroup);
        
//----- MEASUREMENT SET
        log.info("Creating MeasurementSet");
        Metric metric = new Metric(UUID.randomUUID(), MetricType.RATIO, new Unit(fixedString32));
        MeasurementSet mSet = new MeasurementSet(mSetUUID, attribUUID, mGrpUUID, metric);
        metricGroup.addMeasurementSets(mSet);

//----- SAVING EXPERIMENT WITH ALL SUB CLASSES
        log.info("Saving Experiment (with all sub-classes)");
        try {
            expDAO.saveExperiment(exp);
            log.info("Experiment '" + exp.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save experiment: " + ex.getMessage());
        }
    }
    
    public static void case1withMeasurements(IMonitoringEDM edm, UUID expUUID, UUID entityUUID, UUID attribUUID, UUID mGenUUID, UUID mGrpUUID, UUID mSetUUID, int numMeasurements) throws Exception
    {
        log.info("============== CASE 1   --   WITH MEASUREMENTS ==============");
        
        // clearing the database
        log.info("Clearing the database");
        edm.clearMetricsDatabase();
        
        IExperimentDAO expDAO = null;
        try {
            expDAO = edm.getExperimentDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Experiment DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
//----- EXPERIMENT
        log.info("Creating Experiment");
        Experiment exp = new Experiment();
        exp.setUUID(expUUID);
        exp.setName(fixedString32);
        exp.setDescription(fixedString32);
        exp.setStartTime(new Date(Long.parseLong("1345642421005")));
        exp.setEndTime(new Date(Long.parseLong("1440413831014")));
        exp.setExperimentID(fixedString32);
        
//----- METRIC GENERATOR
        log.info("Creating MetricGenerator");
        MetricGenerator metricGenerator = new MetricGenerator(mGenUUID, fixedString32, fixedString32);
        exp.addMetricGenerator(metricGenerator);
        
//----- ENTITY
        log.info("Creating Entity");
        Entity entity = new Entity(entityUUID, fixedString32, fixedString32, fixedString32);
        entity.addAttribute(new Attribute(attribUUID, entityUUID, fixedString32, fixedString32));
        metricGenerator.addEntity(entity);
        
//----- METRIC GROUP
        log.info("Creating MetricGroup");
        MetricGroup metricGroup = new MetricGroup(mGrpUUID, mGenUUID, fixedString32, fixedString32);
        metricGenerator.addMetricGroup(metricGroup);
        
//----- MEASUREMENT SET
        log.info("Creating MeasurementSet");
        Metric metric = new Metric(UUID.randomUUID(), MetricType.RATIO, new Unit(fixedString32));
        MeasurementSet mSet = new MeasurementSet(mSetUUID, attribUUID, mGrpUUID, metric);
        metricGroup.addMeasurementSets(mSet);

//----- SAVING EXPERIMENT WITH ALL SUB CLASSES
        log.info("Saving Experiment (with all sub-classes)");
        try {
            expDAO.saveExperiment(exp);
            log.info("Experiment '" + exp.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save experiment: " + ex.getMessage());
        }
        
//----- SAVING MEASUREMENTS
        generateAndStoreMeasurements(edm, mSetUUID, numMeasurements);
    }
    
 // CASE 2
    
    public static void case2noMeasurements(IMonitoringEDM edm, UUID expUUID, UUID mGenUUID, UUID mGrpUUID) throws Exception
    {
        log.info("============== CASE 2   --   WITHOUT MEASUREMENTS ==============");
        
        // clearing the database
        log.info("Clearing the database");
        edm.clearMetricsDatabase();
        
        IExperimentDAO expDAO = null;
        try {
            expDAO = edm.getExperimentDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Experiment DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
//----- SET-UP
        int numEntities = 1;
        int numAttributes = 10;
        Map<UUID, List<UUID>> entityAttribMap = generateEntityAndAttributeUUIDs(numEntities, numAttributes);
        Map<UUID, UUID> mSetAttribMap = getMSetAttributeUUIDs(entityAttribMap);
        
//----- EXPERIMENT
        log.info("Creating Experiment");
        Experiment exp = new Experiment();
        exp.setUUID(expUUID);
        exp.setName(fixedString32);
        exp.setDescription(fixedString32);
        exp.setStartTime(new Date(Long.parseLong("1345642421005")));
        exp.setEndTime(new Date(Long.parseLong("1440413831014")));
        exp.setExperimentID(fixedString32);
        
//----- METRIC GENERATOR
        log.info("Creating MetricGenerator");
        MetricGenerator metricGenerator = new MetricGenerator(mGenUUID, fixedString32, fixedString32);
        exp.addMetricGenerator(metricGenerator);
        
//----- ENTITY
        log.info("Creating " + numEntities + " Entity object(s) with " + numAttributes + " attributes (each)");
        for (UUID entityUUID : entityAttribMap.keySet())
        {
            Entity entity = new Entity(entityUUID, fixedString32, fixedString32, fixedString32);
            
            for (UUID attribUUID : entityAttribMap.get(entityUUID))
            {
                entity.addAttribute(new Attribute(attribUUID, entityUUID, fixedString32, fixedString32));
            }
            
            metricGenerator.addEntity(entity);
        }
        
//----- METRIC GROUP
        log.info("Creating MetricGroup");
        MetricGroup metricGroup = new MetricGroup(mGrpUUID, mGenUUID, fixedString32, fixedString32);
        metricGenerator.addMetricGroup(metricGroup);
        
//----- MEASUREMENT SET
        log.info("Creating " + mSetAttribMap.size() + " MeasurementSet object(s)");
        for (UUID mSetUUID : mSetAttribMap.keySet())
        {
            Metric metric = new Metric(UUID.randomUUID(), MetricType.RATIO, new Unit(fixedString32));
            MeasurementSet mSet = new MeasurementSet(mSetUUID, mSetAttribMap.get(mSetUUID), mGrpUUID, metric);
            metricGroup.addMeasurementSets(mSet);
        }
        
//----- SAVING EXPERIMENT WITH ALL SUB CLASSES
        log.info("Saving Experiment (with all sub-classes)");
        try {
            expDAO.saveExperiment(exp);
            log.info("Experiment '" + exp.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save experiment: " + ex.getMessage());
        }
    }

    public static void case2withMeasurements(IMonitoringEDM edm, UUID expUUID, UUID mGenUUID, UUID mGrpUUID, int numMeasurementsPerSet) throws Exception
    {
        log.info("============== CASE 2   --   WITHOUT MEASUREMENTS ==============");
        
        // clearing the database
        log.info("Clearing the database");
        edm.clearMetricsDatabase();
        
        IExperimentDAO expDAO = null;
        try {
            expDAO = edm.getExperimentDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Experiment DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
//----- SET-UP
        int numEntities = 1;
        int numAttributes = 10;
        Map<UUID, List<UUID>> entityAttribMap = generateEntityAndAttributeUUIDs(numEntities, numAttributes);
        Map<UUID, UUID> mSetAttribMap = getMSetAttributeUUIDs(entityAttribMap);
        
//----- EXPERIMENT
        log.info("Creating Experiment");
        Experiment exp = new Experiment();
        exp.setUUID(expUUID);
        exp.setName(fixedString32);
        exp.setDescription(fixedString32);
        exp.setStartTime(new Date(Long.parseLong("1345642421005")));
        exp.setEndTime(new Date(Long.parseLong("1440413831014")));
        exp.setExperimentID(fixedString32);
        
//----- METRIC GENERATOR
        log.info("Creating MetricGenerator");
        MetricGenerator metricGenerator = new MetricGenerator(mGenUUID, fixedString32, fixedString32);
        exp.addMetricGenerator(metricGenerator);
        
//----- ENTITY
        log.info("Creating " + numEntities + " Entity object(s) with " + numAttributes + " attributes (each)");
        for (UUID entityUUID : entityAttribMap.keySet())
        {
            Entity entity = new Entity(entityUUID, fixedString32, fixedString32, fixedString32);
            
            for (UUID attribUUID : entityAttribMap.get(entityUUID))
            {
                entity.addAttribute(new Attribute(attribUUID, entityUUID, fixedString32, fixedString32));
            }
            
            metricGenerator.addEntity(entity);
        }
        
//----- METRIC GROUP
        log.info("Creating MetricGroup");
        MetricGroup metricGroup = new MetricGroup(mGrpUUID, mGenUUID, fixedString32, fixedString32);
        metricGenerator.addMetricGroup(metricGroup);
        
//----- MEASUREMENT SET
        log.info("Creating " + mSetAttribMap.size() + " MeasurementSet object(s)");
        for (UUID mSetUUID : mSetAttribMap.keySet())
        {
            Metric metric = new Metric(UUID.randomUUID(), MetricType.RATIO, new Unit(fixedString32));
            MeasurementSet mSet = new MeasurementSet(mSetUUID, mSetAttribMap.get(mSetUUID), mGrpUUID, metric);
            metricGroup.addMeasurementSets(mSet);
        }
        
//----- SAVING EXPERIMENT WITH ALL SUB CLASSES
        log.info("Saving Experiment (with all sub-classes)");
        try {
            expDAO.saveExperiment(exp);
            log.info("Experiment '" + exp.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save experiment: " + ex.getMessage());
        }
        
//----- SAVING MEASUREMENTS
        int counter = 0;
        long storageTimeElapsed = 0;
        for (UUID mSetUUID : mSetAttribMap.keySet())
        {
            log.info("Creating random measurements for set " + ++counter + "/" + mSetAttribMap.size());
            storageTimeElapsed += generateAndStoreMeasurements(edm, mSetUUID, numMeasurementsPerSet);
        }
        log.info("Total storage time:  " + getTimeElapsedString(storageTimeElapsed));
    }
    
 // CASE 3
    
    public static void case3noMeasurements(IMonitoringEDM edm, UUID expUUID, UUID mGenUUID, UUID mGrpUUID) throws Exception
    {
        log.info("============== CASE 3   --   WITHOUT MEASUREMENTS ==============");
        
        // clearing the database
        log.info("Clearing the database");
        edm.clearMetricsDatabase();
        
        IExperimentDAO expDAO = null;
        try {
            expDAO = edm.getExperimentDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Experiment DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
//----- SET-UP
        int numEntities = 10;
        int numAttributes = 10;
        Map<UUID, List<UUID>> entityAttribMap = generateEntityAndAttributeUUIDs(numEntities, numAttributes);
        Map<UUID, UUID> mSetAttribMap = getMSetAttributeUUIDs(entityAttribMap);
        
//----- EXPERIMENT
        log.info("Creating Experiment");
        Experiment exp = new Experiment();
        exp.setUUID(expUUID);
        exp.setName(fixedString32);
        exp.setDescription(fixedString32);
        exp.setStartTime(new Date(Long.parseLong("1345642421005")));
        exp.setEndTime(new Date(Long.parseLong("1440413831014")));
        exp.setExperimentID(fixedString32);
        
//----- METRIC GENERATOR
        log.info("Creating MetricGenerator");
        MetricGenerator metricGenerator = new MetricGenerator(mGenUUID, fixedString32, fixedString32);
        exp.addMetricGenerator(metricGenerator);
        
//----- ENTITY
        log.info("Creating " + numEntities + " Entity object(s) with " + numAttributes + " attributes (each)");
        for (UUID entityUUID : entityAttribMap.keySet())
        {
            Entity entity = new Entity(entityUUID, fixedString32, fixedString32, fixedString32);
            
            for (UUID attribUUID : entityAttribMap.get(entityUUID))
            {
                entity.addAttribute(new Attribute(attribUUID, entityUUID, fixedString32, fixedString32));
            }
            
            metricGenerator.addEntity(entity);
        }
        
//----- METRIC GROUP
        log.info("Creating MetricGroup");
        MetricGroup metricGroup = new MetricGroup(mGrpUUID, mGenUUID, fixedString32, fixedString32);
        metricGenerator.addMetricGroup(metricGroup);
        
//----- MEASUREMENT SET
        log.info("Creating " + mSetAttribMap.size() + " MeasurementSet object(s)");
        for (UUID mSetUUID : mSetAttribMap.keySet())
        {
            Metric metric = new Metric(UUID.randomUUID(), MetricType.RATIO, new Unit(fixedString32));
            MeasurementSet mSet = new MeasurementSet(mSetUUID, mSetAttribMap.get(mSetUUID), mGrpUUID, metric);
            metricGroup.addMeasurementSets(mSet);
        }
        
//----- SAVING EXPERIMENT WITH ALL SUB CLASSES
        log.info("Saving Experiment (with all sub-classes)");
        try {
            expDAO.saveExperiment(exp);
            log.info("Experiment '" + exp.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save experiment: " + ex.getMessage());
        }
    }
    
    public static void case3withMeasurements(IMonitoringEDM edm, UUID expUUID, UUID mGenUUID, UUID mGrpUUID, int numMeasurementsPerSet) throws Exception
    {
        log.info("============== CASE 3   --   WITH MEASUREMENTS ==============");
        
        // clearing the database
        log.info("Clearing the database");
        edm.clearMetricsDatabase();
        
        IExperimentDAO expDAO = null;
        try {
            expDAO = edm.getExperimentDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Experiment DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
//----- SET-UP
        int numEntities = 10;
        int numAttributes = 10;
        Map<UUID, List<UUID>> entityAttribMap = generateEntityAndAttributeUUIDs(numEntities, numAttributes);
        Map<UUID, UUID> mSetAttribMap = getMSetAttributeUUIDs(entityAttribMap);
        
//----- EXPERIMENT
        log.info("Creating Experiment");
        Experiment exp = new Experiment();
        exp.setUUID(expUUID);
        exp.setName(fixedString32);
        exp.setDescription(fixedString32);
        exp.setStartTime(new Date(Long.parseLong("1345642421005")));
        exp.setEndTime(new Date(Long.parseLong("1440413831014")));
        exp.setExperimentID(fixedString32);
        
//----- METRIC GENERATOR
        log.info("Creating MetricGenerator");
        MetricGenerator metricGenerator = new MetricGenerator(mGenUUID, fixedString32, fixedString32);
        exp.addMetricGenerator(metricGenerator);
        
//----- ENTITY
        log.info("Creating " + numEntities + " Entity object(s) with " + numAttributes + " attributes (each)");
        for (UUID entityUUID : entityAttribMap.keySet())
        {
            Entity entity = new Entity(entityUUID, fixedString32, fixedString32, fixedString32);
            
            for (UUID attribUUID : entityAttribMap.get(entityUUID))
            {
                entity.addAttribute(new Attribute(attribUUID, entityUUID, fixedString32, fixedString32));
            }
            
            metricGenerator.addEntity(entity);
        }
        
//----- METRIC GROUP
        log.info("Creating MetricGroup");
        MetricGroup metricGroup = new MetricGroup(mGrpUUID, mGenUUID, fixedString32, fixedString32);
        metricGenerator.addMetricGroup(metricGroup);
        
//----- MEASUREMENT SET
        log.info("Creating " + mSetAttribMap.size() + " MeasurementSet object(s)");
        for (UUID mSetUUID : mSetAttribMap.keySet())
        {
            Metric metric = new Metric(UUID.randomUUID(), MetricType.RATIO, new Unit(fixedString32));
            MeasurementSet mSet = new MeasurementSet(mSetUUID, mSetAttribMap.get(mSetUUID), mGrpUUID, metric);
            metricGroup.addMeasurementSets(mSet);
        }
        
//----- SAVING EXPERIMENT WITH ALL SUB CLASSES
        log.info("Saving Experiment (with all sub-classes)");
        try {
            expDAO.saveExperiment(exp);
            log.info("Experiment '" + exp.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save experiment: " + ex.getMessage());
        }
        
//----- SAVING MEASUREMENTS
        int counter = 0;
        long storageTimeElapsed = 0;
        for (UUID mSetUUID : mSetAttribMap.keySet())
        {
            log.info("Creating random measurements for set " + ++counter + "/" + mSetAttribMap.size());
            storageTimeElapsed += generateAndStoreMeasurements(edm, mSetUUID, numMeasurementsPerSet);
        }
        log.info("Total storage time:  " + getTimeElapsedString(storageTimeElapsed));
    }
    
    
    public static long generateAndStoreMeasurements(IMonitoringEDM edm, UUID mSetUUID, int numMeasurements) throws Exception
    {
        IMeasurementDAO measurementDAO = null;
        try {
            measurementDAO = edm.getMeasurementDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Measurement DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        long createTimeElapsed = 0L;
        long storageTimeElapsed = 0L;
        
        int maxBatchSize = 100000;
        
        if (numMeasurements < maxBatchSize)
            log.info("Creating " + numMeasurements + " random measurements for measurement set " + mSetUUID.toString());
        else
            log.info("Creating " + numMeasurements + " random measurements in batches for measurement set " + mSetUUID.toString());
        
        int tot = 0;
        int counter = 0;
        do {
            int num = 0;
            
            if ((numMeasurements - tot) > maxBatchSize)
                num = maxBatchSize;
            else
                num = (numMeasurements - tot);
            
            tot += num;
            if (numMeasurements > 100000)
                log.info(" * Batch #" + ++counter);
            log.info("  - Creating " + num + " measurements");
            
            long startCreate = System.nanoTime();

            Set<Measurement> measurementSet = new HashSet<Measurement>();

            for (int i = 0; i < num; i++)
            {
                Measurement measurement = new Measurement(UUID.randomUUID(), mSetUUID, new Date(), fixedString32);
                measurementSet.add(measurement);
            }

            long finishCreateStartStore = System.nanoTime();

            log.info("  - Storing " + measurementSet.size() + " measurements in the database");
            try {
                measurementDAO.saveMeasurementsForSet(measurementSet, mSetUUID);
                log.info("  - Measurements saved successfully!");
            } catch (Exception ex) {
                log.error("  - Unable to save Measurement: " + ex.getMessage(), ex);
            }

            long finishStore = System.nanoTime();
            createTimeElapsed += (finishCreateStartStore-startCreate);
            storageTimeElapsed += (finishStore-finishCreateStartStore);
            
            if (tot < numMeasurements)
                log.info("  - Still " + (numMeasurements-tot) + " measurements to go...");
            
        } while (tot < numMeasurements);
        
        log.info(" * Creation time: " + getTimeElapsedString(createTimeElapsed));
        log.info(" * Storage time:  " + getTimeElapsedString(storageTimeElapsed));
        
        return storageTimeElapsed;
    }
    
    private static String getTimeElapsedString(long timeElapsedNano)
    {
        long ms = timeElapsedNano/1000000;
        long s = ms/1000;
        
        return new String(s + "s and " + (ms - (s*1000)) + "ms");
    }
    
    public static Map<UUID, List<UUID>> generateEntityAndAttributeUUIDs(int numEntities, int numAttributes)
    {
        Map<UUID, List<UUID>> entityAttributeUUIDs = new HashMap<UUID, List<UUID>>();
        
        for (int i = 0; i < numEntities; i++)
        {
            List<UUID> attributeUUIDs = new ArrayList<UUID>();

            for(int j = 0; j < numAttributes; j++)
                attributeUUIDs.add(UUID.randomUUID());
            
            entityAttributeUUIDs.put(UUID.randomUUID(), attributeUUIDs);
        }
        
        return entityAttributeUUIDs;
    }

    public static Map<UUID, UUID> getMSetAttributeUUIDs(Map<UUID, List<UUID>> entityAttributeUUIDs)
    {
        Map<UUID, UUID> mSetAttributeUUIDs = new HashMap<UUID, UUID>();
        
        for (UUID entityUUID : entityAttributeUUIDs.keySet())
        {
            for (UUID attributeUUID : entityAttributeUUIDs.get(entityUUID))
            {
                mSetAttributeUUIDs.put(UUID.randomUUID(), attributeUUID);
            }
        }
        
        return mSetAttributeUUIDs;
    }
}
