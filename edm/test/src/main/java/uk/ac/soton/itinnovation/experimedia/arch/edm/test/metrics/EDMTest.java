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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.NoDataException;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IEntityDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMeasurementDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMeasurementSetDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMetricGroupDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IReportDAO;

/**
 *
 * @author Vegard Engen
 */
public class EDMTest
{
	private static final Logger log = LoggerFactory.getLogger(EDMTest.class);

    public static void main(String[] args) throws Exception
    {
        UUID expUUID = UUID.fromString("bfe4c710-61ba-46f8-a519-be2f7808192e");
        UUID entityUUID = UUID.fromString("5718cd67-4310-4b2c-aeb9-9b72314630ca");
        UUID attributeUUID = UUID.fromString("4f2817b5-603a-4d02-a032-62cfca314962");
        UUID mGenUUID = UUID.fromString("782e5097-2e29-4219-a984-bf48dfcd7f63");
        UUID mGrpUUID = UUID.fromString("189064a5-f1d8-41f2-b2c1-b88776841009");
        UUID mSetUUID = UUID.fromString("2b915932-41b1-45d7-b4f6-2de4f30020b8");
        UUID reportUUID = UUID.fromString("165c8058-5c67-4f92-ae34-df7ee2129821");
        
        boolean withSubClasses = true;
        
        IMonitoringEDM edm = EDMInterfaceFactory.getMonitoringEDM();
        if (!edm.isDatabaseSetUpAndAccessible())
        {
            log.error("EDM not set up properly!");
            System.exit(1);
        }
    
        log.info("DB check: " + edm.isDatabaseSetUpAndAccessible());
        
        // clear the database
        log.info("Clearing the database");
        edm.clearMetricsDatabase();
        
        //-- Create and get experiments --//
        //experiments(edm, expUUID);
        
        //entities(edm, entityUUID, attributeUUID, expUUID, withSubClasses);
        
        //metricGenerator(edm, expUUID, entityUUID, mGenUUID, withSubClasses);
        //metricGeneratorCompleteChain(edm, expUUID, entityUUID, attributeUUID, mGenUUID, mGrpUUID, mSetUUID);
        
        //metricGroup(edm, mGenUUID, mGrpUUID, withSubClasses);
        //metricGroupCompleteChain(edm, attributeUUID, mGenUUID, mGrpUUID, mSetUUID);
        
        //measurementSet(edm, attributeUUID, mGrpUUID, mSetUUID, withSubClasses);
        //measurementSetViolation(edm, attributeUUID, mGrpUUID, mSetUUID);
        
        //measurement(edm, mSetUUID);
        
        experimentCompleteChain(edm, expUUID, entityUUID, attributeUUID, mGenUUID, mGrpUUID, mSetUUID);
        
        //printExperimentDetails(edm, expUUID, withSubClasses);
        
        //saveReport(edm, mSetUUID, reportUUID);
        
        //getReport(edm, mSetUUID, reportUUID);
        
        //updateAndDeleteReportTests(edm, mSetUUID);
        
        //reportSynchronisationTests(edm, mSetUUID);
        
        duplicateMeasurementsTests(edm, mSetUUID);
    }
    
    public static void experiments(IMonitoringEDM edm, UUID expUUID) throws Exception
    {
        boolean withSubClasses = true;
        IExperimentDAO expDAO = null;
        try {
            expDAO = edm.getExperimentDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Experiment DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        log.info("Saving experiment");
        Experiment exp = new Experiment();
        exp.setUUID(expUUID);
        exp.setName("Experiment");
        exp.setDescription("An experiment description...");
        exp.setStartTime(new Date(Long.parseLong("1345642421005")));
        exp.setEndTime(new Date(Long.parseLong("1440413831014")));
        exp.setExperimentID("/locations/experiment/1337");
        try {
            expDAO.saveExperiment(exp);
            log.info("Experiment '" + exp.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save experiment: " + ex.getMessage());
        }
        
        // create a random experiment (so we get more in the list)
        log.info("Saving random experiment");
        Experiment expRand = new Experiment();
        expRand.setName("Random Experiment");
        expRand.setDescription("A very boring description...");
        expRand.setStartTime(new Date());
        try {
            expDAO.saveExperiment(expRand);
            log.info("Experiment '" + expRand.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save experiment: " + ex.getMessage());
        }
        
        log.info("Getting experiment object");
        Experiment expFromDB = null;
        try {
            expFromDB = expDAO.getExperiment(expUUID, withSubClasses);
            //exp2 = expDAO.getExperiment(UUID.fromString("5718cd67-4310-4b2c-aeb9-9b72314630ca"));
            //exp2 = expDAO.getExperiment(UUID.fromString("3fe0769d-ffae-4173-9c24-07ff7819b5cb"));
        } catch (NoDataException nde) {
            log.info("There was no experiment with UUID " + expUUID + ": " + nde.getMessage());
        } catch (Exception ex) {
            log.error("Unable to get experiment: " + ex.getMessage());
        }
        
        if (expFromDB != null)
        {
            log.info("Experiment details:");
            if (expFromDB.getUUID() != null) log.info("  - UUID:  " + expFromDB.getUUID());
            if (expFromDB.getName() != null) log.info("  - Name:  " + expFromDB.getName());
            if (expFromDB.getDescription() != null) log.info("  - Desc:  " + expFromDB.getDescription());
            if (expFromDB.getStartTime() != null) log.info("  - Start: " + expFromDB.getStartTime() + " (" + expFromDB.getStartTime().getTime() + ")");
            if (expFromDB.getEndTime() != null) log.info("  - End:   " + expFromDB.getEndTime() + " (" + expFromDB.getEndTime().getTime() + ")");
            if (expFromDB.getExperimentID() != null) log.info("  - ID:    " + expFromDB.getExperimentID());

            if ((expFromDB.getMetricGenerators() == null) || expFromDB.getMetricGenerators().isEmpty())
                log.info("  - There are NO metric generators");
            else
                log.info("  - There are " + expFromDB.getMetricGenerators().size() + " metric generators");
        }
        
        log.info("Getting all experiments");
        Set<Experiment> experiments = null;
        
        try {
            experiments = expDAO.getExperiments(withSubClasses);
            
            if (experiments != null)
            {
                log.info("Got " + experiments.size() + " experiment(s):");
                for (Experiment expFromDB2 : experiments)
                {
                    log.info(" * Experiment details:");
                    if (expFromDB2.getUUID() != null) log.info("  - UUID:  " + expFromDB2.getUUID());
                    if (expFromDB2.getName() != null) log.info("  - Name:  " + expFromDB2.getName());
                    if (expFromDB2.getDescription() != null) log.info("  - Desc:  " + expFromDB2.getDescription());
                    if (expFromDB2.getStartTime() != null) log.info("  - Start: " + expFromDB2.getStartTime() + " (" + expFromDB2.getStartTime().getTime() + ")");
                    if (expFromDB2.getEndTime() != null) log.info("  - End:   " + expFromDB2.getEndTime() + " (" + expFromDB2.getEndTime().getTime() + ")");
                    if (expFromDB2.getExperimentID() != null) log.info("  - ID:    " + expFromDB2.getExperimentID());
                }
            }
        } catch (Exception ex) {
            log.error("");
        }
    }
    
    public static void entities(IMonitoringEDM edm, UUID entityUUID, UUID attributeUUID, UUID expUUID, boolean withSubClasses) throws Exception
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
        entity.setEntityID("/locations/epcc/3321");
        entity.setName("VM");
        entity.setDescription("A Virtual Machine");
        entity.addAttribute(new Attribute(attributeUUID, entityUUID, "CPU", "CPU performance"));
        entity.addAttribute(new Attribute(UUID.randomUUID(), entityUUID, "Network", "Network performance"));
        entity.addAttribute(new Attribute(UUID.randomUUID(), entityUUID, "Disk", "Disk performance"));
        
        try {
            entityDAO.saveEntity(entity);
            log.info("Entity '" + entity.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save entity: " + ex.getMessage());
        }
        
        log.info("Saving an extra attribute for the VM Entity");
        try {
            Attribute attrib = new Attribute(UUID.randomUUID(), entityUUID, "Another attribute", "A random attribute for debugging");
            entityDAO.saveAttribute(attrib);
            log.info("Attribute '" + attrib.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save attribute: " + ex.getMessage());
        }
        
        log.info("Saving random entity");
        Entity randomEntity = new Entity();
        randomEntity.setName("Random entity");
        randomEntity.setDescription("Blabla");
        randomEntity.addAttribute(new Attribute(UUID.randomUUID(), randomEntity.getUUID(), "height", "The height of the random entity"));
        
        try {
            entityDAO.saveEntity(randomEntity);
            log.info("Entity '" + randomEntity.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save entity: " + ex.getMessage());
        }
        
        log.info("Getting Entity from the DB");
        Entity entityFromDB = null;
        try {
            entityFromDB = entityDAO.getEntity(entityUUID, withSubClasses);
        } catch (Exception ex) {
            log.error("Unable to get entity: " + ex.getMessage());
        }
        
        log.info("Entity details:");
        if (entityFromDB.getUUID() != null) log.info("  - UUID:  " + entityFromDB.getUUID());
        if (entityFromDB.getEntityID() != null) log.info("  - ID:    " + entityFromDB.getEntityID());
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
        }
        
        log.info("Getting all entities");
        Set<Entity> entities = null;
        try {
            entities = entityDAO.getEntities(withSubClasses);
        } catch (Exception ex) {
            log.error("Unable to get entities: " + ex.getMessage());
        }
        
        if (entities != null)
        {
            log.info("Got " + entities.size() + " entities:");
            for (Entity ent : entities)
            {
                log.info(" * Entity details:");
                if (ent.getUUID() != null) log.info("   - UUID:  " + ent.getUUID());
                if (ent.getEntityID() != null) log.info("   - ID:    " + ent.getEntityID());
                if (ent.getName() != null) log.info("   - Name:  " + ent.getName());
                if (ent.getDescription() != null) log.info("   - Desc:  " + ent.getDescription());
                if ((ent.getAttributes() == null) || ent.getAttributes().isEmpty()) {
                    log.info("   - There are NO attributes");
                } else {
                    log.info("   - There are " + ent.getAttributes().size() + " attributes");
                    for (Attribute attrib : ent.getAttributes())
                    {
                        if (attrib != null) {
                            log.info("     - Attribute details:");
                            if (attrib.getUUID() != null) log.info("       - UUID:  " + attrib.getUUID());
                            if (attrib.getName() != null) log.info("       - Name:  " + attrib.getName());
                            if (attrib.getDescription() != null) log.info("       - Desc:  " + attrib.getDescription());
                        }
                    }
                } // end else there are attributes
            } // end for all entities
        } // end if entities == null
        
        log.info("Getting all entities for experiment " + expUUID.toString());
        entities = null;
        try {
            entities = entityDAO.getEntitiesForExperiment(expUUID, withSubClasses);
        } catch (Exception ex) {
            log.error("Unable to get entities: " + ex.getMessage());
        }
        
        if (entities != null)
        {
            log.info("Got " + entities.size() + " entities:");
            for (Entity ent : entities)
            {
                log.info(" * Entity details:");
                if (ent.getUUID() != null) log.info("   - UUID:  " + ent.getUUID());
                if (ent.getEntityID() != null) log.info("   - ID:    " + ent.getEntityID());
                if (ent.getName() != null) log.info("   - Name:  " + ent.getName());
                if (ent.getDescription() != null) log.info("   - Desc:  " + ent.getDescription());
                if ((ent.getAttributes() == null) || ent.getAttributes().isEmpty()) {
                    log.info("   - There are NO attributes");
                } else {
                    log.info("   - There are " + ent.getAttributes().size() + " attributes");
                    for (Attribute attrib : ent.getAttributes())
                    {
                        if (attrib != null) {
                            log.info("     - Attribute details:");
                            if (attrib.getUUID() != null) log.info("       - UUID:  " + attrib.getUUID());
                            if (attrib.getName() != null) log.info("       - Name:  " + attrib.getName());
                            if (attrib.getDescription() != null) log.info("       - Desc:  " + attrib.getDescription());
                        }
                    }
                } // end else there are attributes
            } // end for all entities
        } // end if entities == null
    }
    
    public static void metricGenerator(IMonitoringEDM edm, UUID expUUID, UUID entityUUID, UUID mGenUUID, boolean withSubClasses) throws Exception
    {
        IMetricGeneratorDAO metricGeneratorDAO = null;
        try {
            metricGeneratorDAO = edm.getMetricGeneratorDAO();
        } catch (Exception ex) {
            log.error ("Unable to get MetricGenerator DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        log.info("Saving Experiment MetricGenerator");
        MetricGenerator metricGenerator = new MetricGenerator(mGenUUID, "Experiment MetricGenerator", "A description");
        metricGenerator.addEntity(new Entity(entityUUID));
        
        try {
            metricGeneratorDAO.saveMetricGenerator(metricGenerator, expUUID);
            log.info("MetricGenerator saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save MetricGenerator: " + ex.getMessage());
        }
        
        log.info("Saving a random MetricGenerator");
        MetricGenerator randomMetricGenerator = new MetricGenerator(UUID.randomUUID(), "Random MetricGenerator", "A random description");
        randomMetricGenerator.addEntity(new Entity(entityUUID));
        
        try {
            metricGeneratorDAO.saveMetricGenerator(randomMetricGenerator, expUUID);
            log.info("MetricGenerator saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save MetricGenerator: " + ex.getMessage());
        }
        
        log.info("Getting metric generator by uuid: " + mGenUUID.toString());
        MetricGenerator metricGeneratorFromDB = null;
        
        try {
            metricGeneratorFromDB = metricGeneratorDAO.getMetricGenerator(mGenUUID, withSubClasses);
        } catch (Exception ex) {
            log.error("Unable to get MetricGenerator from the DB");
        }
        
        if (metricGeneratorFromDB != null)
        {
            printMetricGeneratorDetails(metricGeneratorFromDB);
        }
        
        log.info("Getting all metric generators");
        Set<MetricGenerator> metricGeneratorsFromDB = null;
        
        try {
            metricGeneratorsFromDB = metricGeneratorDAO.getMetricGenerators(withSubClasses);
        } catch (Exception ex) {
            log.error("Unable to get MetricGenerator set from the DB");
        }
        
        if (metricGeneratorsFromDB != null)
        {
            log.info("Got " + metricGeneratorsFromDB.size() + " metric generator(s):");
            
            for (MetricGenerator mGen : metricGeneratorsFromDB)
            {
                printMetricGeneratorDetails(mGen);
            }
        }
        
        UUID randomExpUUID = UUID.fromString("bd6a9cea-c69f-44c3-b89b-6a96b1f9538f");
        log.info("Saving a random MetricGenerator for random experiment");
        MetricGenerator randomMetricGenerator2 = new MetricGenerator(UUID.randomUUID(), "Random MetricGenerator", "A random description");
        randomMetricGenerator2.addEntity(new Entity(entityUUID));
        
        try {
            metricGeneratorDAO.saveMetricGenerator(randomMetricGenerator2, randomExpUUID);
            log.info("MetricGenerator saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save MetricGenerator: " + ex.getMessage());
        }
        
        log.info("Getting all metric generators for experiment: " + expUUID.toString());
        metricGeneratorsFromDB = null;
        
        try {
            metricGeneratorsFromDB = metricGeneratorDAO.getMetricGeneratorsForExperiment(expUUID, withSubClasses);
        } catch (Exception ex) {
            log.error("Unable to get MetricGenerator set from the DB");
        }
        
        if (metricGeneratorsFromDB != null)
        {
            log.info("Got " + metricGeneratorsFromDB.size() + " metric generator(s):");
            
            for (MetricGenerator mGen : metricGeneratorsFromDB)
            {
                printMetricGeneratorDetails(mGen);
            }
        }
    }
    
    public static void metricGeneratorCompleteChain(IMonitoringEDM edm, UUID expUUID, UUID entityUUID, UUID attribUUID, UUID mGenUUID, UUID mGrpUUID, UUID mSetUUID) throws Exception
    {
        IMetricGeneratorDAO metricGeneratorDAO = null;
        try {
            metricGeneratorDAO = edm.getMetricGeneratorDAO();
        } catch (Exception ex) {
            log.error ("Unable to get MetricGenerator DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
//----- METRIC GENERATOR
        log.info("Creating Experiment MetricGenerator");
        MetricGenerator metricGenerator = new MetricGenerator(mGenUUID, "Experiment MetricGenerator", "A description");
        //MetricGenerator metricGenerator = new MetricGenerator(mGenUUID, null, "A description");
        metricGenerator.addEntity(new Entity(entityUUID));
        
//----- ENTITY
        log.info("Creating VM entity");
        Entity entity = new Entity(entityUUID, "VM", "A Virtual Machine");
        //Entity entity = new Entity(entityUUID, null, "A Virtual Machine");
        entity.addAttribute(new Attribute(attribUUID, entityUUID, "CPU", "CPU performance"));
        entity.addAttribute(new Attribute(UUID.randomUUID(), entityUUID, "Network", "Network performance"));
        entity.addAttribute(new Attribute(UUID.randomUUID(), entityUUID, "Disk", "Disk performance"));
        metricGenerator.addEntity(entity);
        
//----- METRIC GROUP
        log.info("Creating QoS MetricGroup");
        MetricGroup metricGroup = new MetricGroup(mGrpUUID, mGenUUID, "Quality of Service", "A group of QoS metrics");
        //MetricGroup metricGroup = new MetricGroup(mGrpUUID, mGenUUID, null, "A group of QoS metrics");
        metricGenerator.addMetricGroup(metricGroup);
        
//----- MEASUREMENT SET
        log.info("Creating QoS Measurement set");
        Metric metric = new Metric(UUID.randomUUID(), MetricType.RATIO, null);
        //Metric metric = new Metric(UUID.randomUUID(), null, null);
        MeasurementSet mSet = new MeasurementSet(mSetUUID, attribUUID, mGrpUUID, metric);
        //MeasurementSet mSet = new MeasurementSet(mSetUUID, attribUUID, mGrpUUID, null);
        metricGroup.addMeasurementSets(mSet);
        
//----- MEASUREMENTS
        log.info("Creating random measurements for QoS measurement set");
        int numMeasurements = 5;
        Random rand = new Random();
        rand.setSeed(new Date().getTime());
        for (int i = 0; i < numMeasurements; i++)
        {
            long timeStamp = new Date().getTime() - (1000 * (numMeasurements-i));
            Measurement measurement = new Measurement(UUID.randomUUID(), mSetUUID, new Date(timeStamp), String.valueOf(rand.nextInt(500)));
            //Measurement measurement = new Measurement(UUID.randomUUID(), mSetUUID, new Date(timeStamp), null);
            mSet.addMeasurement(measurement);
        }
        
        log.info("Saving metric generator (with all sub-classes)");
        try {
            metricGeneratorDAO.saveMetricGenerator(metricGenerator, expUUID);
            log.info("Metric generator '" + metricGenerator.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save Metric generator: " + ex.getMessage());
        }
    }
    
    
    public static void metricGroup(IMonitoringEDM edm, UUID mGenUUID, UUID mGrpUUID, boolean withSubClasses) throws Exception
    {
        IMetricGroupDAO metricGroupDAO = null;
        try {
            metricGroupDAO = edm.getMetricGroupDAO();
        } catch (Exception ex) {
            log.error ("Unable to get MetricGroup DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        log.info("Saving QoS MetricGroup");
        MetricGroup metricGroup = new MetricGroup(mGrpUUID, mGenUUID, "Quality of Service", "A group of QoS metrics");
        
        try {
            metricGroupDAO.saveMetricGroup(metricGroup);
            log.info("MetricGroup saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save MetricGroup: " + ex.getMessage(), ex);
        }
        
        log.info("Saving Random MetricGroup");
        MetricGroup randomGroup = new MetricGroup(UUID.randomUUID(), mGenUUID, "Random Metric Group", "A random group of metrics");
        
        try {
            metricGroupDAO.saveMetricGroup(randomGroup);
            log.info("MetricGroup saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save MetricGroup: " + ex.getMessage(), ex);
        }
        
        log.info("Getting metric group with UUID " + mGrpUUID.toString());
        MetricGroup metricGroupFromDB = null;
        try {
            metricGroupFromDB = metricGroupDAO.getMetricGroup(mGrpUUID, withSubClasses);
        } catch (Exception ex) {
            log.error("Unable to get metric group: " + ex.getMessage(), ex);
        }
        printMetricGroupDetails(metricGroupFromDB);
        
        log.info("Getting metric groups for Metric Generator: " + mGenUUID.toString());
        Set<MetricGroup> metricGroups = null;
        try {
            metricGroups = metricGroupDAO.getMetricGroupsForMetricGenerator(mGenUUID, withSubClasses);
        } catch (Exception ex) {
            log.error("Unable to get metric groups: " + ex.getMessage(), ex);
        }
        
        if (metricGroups == null)
        {
            log.error("Set of metric groups == NULL!");
        }
        else
        {
            log.info("Got " + metricGroups.size() + " metric group(s)");
            
            for (MetricGroup mGrp : metricGroups)
            {
                printMetricGroupDetails(mGrp);
            }
        }
    }
    
    public static void metricGroupCompleteChain(IMonitoringEDM edm, UUID attribUUID, UUID mGenUUID, UUID mGrpUUID, UUID mSetUUID) throws Exception
    {
        IMetricGroupDAO metricGroupDAO = null;
        try {
            metricGroupDAO = edm.getMetricGroupDAO();
        } catch (Exception ex) {
            log.error ("Unable to get MetricGroup DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
//----- METRIC GROUP
        log.info("Creating QoS MetricGroup");
        MetricGroup metricGroup = new MetricGroup(mGrpUUID, mGenUUID, "Quality of Service", "A group of QoS metrics");
        //MetricGroup metricGroup = new MetricGroup(mGrpUUID, mGenUUID, null, "A group of QoS metrics");
        
//----- MEASUREMENT SET
        log.info("Creating QoS Measurement set");
        Metric metric = new Metric(UUID.randomUUID(), MetricType.RATIO, null);
        //Metric metric = new Metric(UUID.randomUUID(), null, null);
        MeasurementSet mSet = new MeasurementSet(mSetUUID, attribUUID, mGrpUUID, metric);
        //MeasurementSet mSet = new MeasurementSet(mSetUUID, attribUUID, mGrpUUID, null);
        metricGroup.addMeasurementSets(mSet);
        
//----- MEASUREMENTS
        log.info("Creating random measurements for QoS measurement set");
        int numMeasurements = 5;
        Random rand = new Random();
        rand.setSeed(new Date().getTime());
        for (int i = 0; i < numMeasurements; i++)
        {
            long timeStamp = new Date().getTime() - (1000 * (numMeasurements-i));
            Measurement measurement = new Measurement(UUID.randomUUID(), mSetUUID, new Date(timeStamp), String.valueOf(rand.nextInt(500)));
            //Measurement measurement = new Measurement(UUID.randomUUID(), mSetUUID, new Date(timeStamp), null);
            mSet.addMeasurement(measurement);
        }
        
        log.info("Saving metric group (with all sub-classes)");
        try {
            metricGroupDAO.saveMetricGroup(metricGroup);
            log.info("Metric group '" + metricGroup.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save Metric Group: " + ex.getMessage());
        }
    }
    
    public static void measurementSet(IMonitoringEDM edm, UUID attribUUID, UUID mGrpUUID, UUID mSetUUID, boolean withSubClasses) throws Exception
    {
        IMeasurementSetDAO mSetDAO = null;
        try {
            mSetDAO = edm.getMeasurementSetDAO();
        } catch (Exception ex) {
            log.error ("Unable to get MeasurementSet DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        log.info("Saving QoS Measurement set");
        Metric metric = new Metric(UUID.randomUUID(), MetricType.RATIO, new Unit("milli seconds"));
        MeasurementSet mSet = new MeasurementSet(mSetUUID, attribUUID, mGrpUUID, metric);
        //MeasurementSet mSet = new MeasurementSet(mSetUUID, attribUUID, mGrpUUID, null);
        
        try {
            mSetDAO.saveMeasurementSet(mSet);
            log.info("MeasurementSet saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save MeasurementSet: " + ex.getMessage(), ex);
        }
        
        log.info("Saving Random Measurement set");
        Metric randomMetric = new Metric(UUID.randomUUID(), MetricType.INTERVAL, new Unit("milli seconds"));
        //Metric randomMetric = new Metric(UUID.randomUUID(), MetricType.INTERVAL, null);
        //Metric randomMetric = new Metric(UUID.randomUUID(), null, new Unit("milli seconds"));
        MeasurementSet randomMSet = new MeasurementSet(UUID.randomUUID(), attribUUID, mGrpUUID, randomMetric);
        
        try {
            mSetDAO.saveMeasurementSet(randomMSet);
            log.info("MeasurementSet saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save MeasurementSet: " + ex.getMessage(), ex);
        }
        
        log.info("Getting measurement sets for measurement group: " + mGrpUUID.toString());
        Set<MeasurementSet> measurementSets = null;
        try {
            measurementSets = mSetDAO.getMeasurementSetForMetricGroup(mGrpUUID, withSubClasses);
        } catch (Exception ex) {
            log.error("Unable to get MeasurementSet: " + ex.getMessage(), ex);
        }
        
        if (measurementSets == null)
        {
            log.info("There are no mesurement sets for the metric group: " + mGrpUUID.toString());
        }
        else
        {
            for (MeasurementSet mSett : measurementSets)
            {
                printMeasurementSetDetails(mSett);
            }
        }
        
    }
    
    public static void measurementSetViolation(IMonitoringEDM edm, UUID attribUUID, UUID mGrpUUID, UUID mSetUUID) throws Exception
    {
        boolean withSubClasses = false;
        IMeasurementSetDAO mSetDAO = null;
        try {
            mSetDAO = edm.getMeasurementSetDAO();
        } catch (Exception ex) {
            log.error ("Unable to get MeasurementSet DAO: " + ex.getMessage());
            throw ex;
        }
        
        log.info("Saving Measurement set with given UUID (for the metric group | attribute combo given)");
        Metric metric = new Metric(UUID.randomUUID(), MetricType.RATIO, new Unit("milli seconds"));
        MeasurementSet mSet = new MeasurementSet(mSetUUID, attribUUID, mGrpUUID, metric);
        
        try {
            mSetDAO.saveMeasurementSet(mSet);
            log.info("MeasurementSet saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save MeasurementSet: " + ex.getMessage());
        }
        
        log.info("Saving Measurement set with random UUID (for the metric group | attribute combo given)");
        Metric randomMetric = new Metric(UUID.randomUUID(), MetricType.INTERVAL, new Unit("milli seconds"));
        MeasurementSet randomMSet = new MeasurementSet(UUID.randomUUID(), attribUUID, mGrpUUID, randomMetric);
        
        try {
            mSetDAO.saveMeasurementSet(randomMSet);
            log.info("MeasurementSet saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save MeasurementSet: " + ex.getMessage());
        }
    }
    
    public static void measurement(IMonitoringEDM edm, UUID mSetUUID) throws Exception
    {
        IMeasurementDAO measurementDAO = null;
        try {
            measurementDAO = edm.getMeasurementDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Measurement DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        log.info("Saving random measurement for QoS measurement set");
        Random rand = new Random();
        rand.setSeed(new Date().getTime());
        Measurement measurement = new Measurement(UUID.randomUUID(), mSetUUID, new Date(), String.valueOf(rand.nextLong()));
        
        try {
            measurementDAO.saveMeasurement(measurement);
            log.info("Measurement saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save Measurement: " + ex.getMessage(), ex);
        }
    }
    
    public static void experimentCompleteChain(IMonitoringEDM edm, UUID expUUID, UUID entityUUID, UUID attribUUID, UUID mGenUUID, UUID mGrpUUID, UUID mSetUUID) throws Exception
    {
        boolean withSubClasses = true;
        
        IExperimentDAO expDAO = null;
        try {
            expDAO = edm.getExperimentDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Experiment DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
//----- EXPERIMENT
        log.info("Creating experiment");
        Experiment exp = new Experiment();
        exp.setUUID(expUUID);
        exp.setName("Experiment");
        exp.setDescription("An experiment description...");
        exp.setStartTime(new Date(Long.parseLong("1345642421005")));
        exp.setEndTime(new Date(Long.parseLong("1440413831014")));
        exp.setExperimentID("/locations/experiment/1337");
        
//----- METRIC GENERATOR
        log.info("Creating Experiment MetricGenerator");
        MetricGenerator metricGenerator = new MetricGenerator(mGenUUID, "Experiment MetricGenerator", "A description");
        //MetricGenerator metricGenerator = new MetricGenerator(mGenUUID, null, "A description");
        exp.addMetricGenerator(metricGenerator);
        
//----- ENTITY
        log.info("Creating VM entity");
        Entity entity = new Entity(entityUUID, "/locations/epcc/3321", "VM", "A Virtual Machine");
        //Entity entity = new Entity(entityUUID, null, "A Virtual Machine");
        entity.addAttribute(new Attribute(attribUUID, entityUUID, "CPU", "CPU performance"));
        entity.addAttribute(new Attribute(UUID.randomUUID(), entityUUID, "Network", "Network performance"));
        entity.addAttribute(new Attribute(UUID.randomUUID(), entityUUID, "Disk", "Disk performance"));
        metricGenerator.addEntity(entity);
        
//----- METRIC GROUP
        log.info("Creating QoS MetricGroup");
        MetricGroup metricGroup = new MetricGroup(mGrpUUID, mGenUUID, "Quality of Service", "A group of QoS metrics");
        //MetricGroup metricGroup = new MetricGroup(mGrpUUID, mGenUUID, null, "A group of QoS metrics");
        metricGenerator.addMetricGroup(metricGroup);
        
//----- MEASUREMENT SET
        log.info("Creating QoS Measurement set");
        Metric metric = new Metric(UUID.randomUUID(), MetricType.RATIO, null);
        //Metric metric = new Metric(UUID.randomUUID(), null, null);
        MeasurementSet mSet = new MeasurementSet(mSetUUID, attribUUID, mGrpUUID, metric);
        //MeasurementSet mSet = new MeasurementSet(mSetUUID, attribUUID, mGrpUUID, null);
        metricGroup.addMeasurementSets(mSet);
        
//----- MEASUREMENTS
        log.info("Creating random measurements for QoS measurement set");
        int numMeasurements = 5;
        Random rand = new Random();
        rand.setSeed(new Date().getTime());
        for (int i = 0; i < numMeasurements; i++)
        {
            long timeStamp = new Date().getTime() - (1000 * (numMeasurements-i));
            Measurement measurement = new Measurement(UUID.randomUUID(), mSetUUID, new Date(timeStamp), String.valueOf(rand.nextInt(500)));
            //Measurement measurement = new Measurement(UUID.randomUUID(), mSetUUID, new Date(timeStamp), null);
            mSet.addMeasurement(measurement);
        }
        
        log.info("Saving experiment (with all sub-classes)");
        try {
            expDAO.saveExperiment(exp);
            log.info("Experiment '" + exp.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save experiment: " + ex.getMessage());
        }
    }
    
    
    public static void saveReport(IMonitoringEDM edm, UUID mSetUUID, UUID reportUUID) throws Exception
    {
        IReportDAO reportDAO = null;
        try {
            reportDAO = edm.getReportDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Report DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        log.info("Saving Report for measurement set " + mSetUUID.toString());
        
        Report report = getReportWithRandomMeasurements(reportUUID, mSetUUID, 5);
        
        try {
            reportDAO.saveReport(report, true);
            log.info("Report saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save Report: " + ex.getMessage(), ex);
            
            // save the report with a random UUID...
            log.info("Trying to save the same report, but with a random UUID");
            report.setUUID(UUID.randomUUID());
            try {
                reportDAO.saveReport(report, true);
                log.info("Report saved successfully!");
            } catch (Exception ex2) {
                log.error("Unable to save Report: " + ex2.getMessage(), ex2);

                report.setUUID(UUID.randomUUID());
            }
        }
        
        log.info("Saving Measurements for Report for measurement set " + mSetUUID.toString());
        Report report2 = getReportWithRandomMeasurements(UUID.randomUUID(), mSetUUID, 5);
        
        try {
            reportDAO.saveMeasurements(report2);
            log.info("Measurements for Report saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save measurements for Report: " + ex.getMessage(), ex);
        }
    }
    
    
    private static Report getReportWithRandomMeasurements(UUID reportUUID, UUID mSetUUID, int numMeasurements)
    {
        MeasurementSet mSet = new MeasurementSet(mSetUUID);
        Random rand = new Random();
        rand.setSeed(new Date().getTime());
        long timeStampFrom = 0;
        long timeStampTo = 0;
        for (int i = 0; i < numMeasurements; i++)
        {
            long timeStamp = new Date().getTime() - (1000 * (numMeasurements-i));
            Measurement measurement = new Measurement(UUID.randomUUID(), mSetUUID, new Date(timeStamp), String.valueOf(rand.nextInt(500)));
            //Measurement measurement = new Measurement(UUID.randomUUID(), mSetUUID, new Date(timeStamp), null);
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
        
        Report report = new Report(reportUUID, mSet, new Date(), new Date(timeStampFrom), new Date(timeStampTo), mSet.getMeasurements().size());
        return report;
    }
    
    public static void getReport(IMonitoringEDM edm, UUID mSetUUID, UUID reportUUID) throws Exception
    {
        IReportDAO reportDAO = null;
        try {
            reportDAO = edm.getReportDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Report DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
//----- GET REPORT BY UUID
        log.info("Getting report by UUID");
        Report report = null;
        try {
            report = reportDAO.getReport(reportUUID, false);
        } catch (NoDataException ex) {
            log.error("Unable to get Report: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
        
        log.info("Getting report by UUID - WITH DATA");
        report = null;
        try {
            report = reportDAO.getReport(reportUUID, true);
        } catch (NoDataException ex) {
            log.error("Unable to get Report: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
        
//----- GET REPORT FOR LATEST VALUE
        log.info("Getting report for latest value");
        report = null;
        try {
            report = reportDAO.getReportForLatestMeasurement(mSetUUID, false);
        } catch (NoDataException ex) {
            log.error("Unable to get Report: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
        
        log.info("Getting report for latest value - WITH DATA");
        report = null;
        try {
            report = reportDAO.getReportForLatestMeasurement(mSetUUID, true);
        } catch (NoDataException ex) {
            log.error("Unable to get Report: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
        
//----- GET REPORT FROM DATE
        Date fromDate = new Date(Long.parseLong("1346146199684"));
        int numMeasurements = 100;
        log.info("Getting report for " + numMeasurements + " measurements from date " + fromDate);
        report = null;
        try {
            report = reportDAO.getReportForMeasurementsFromDate(mSetUUID, fromDate, false);
        } catch (NoDataException ex) {
            log.error("Unable to get Report: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
        
        log.info("Getting report for " + numMeasurements + " measurements from date " + fromDate + " --- WITH DATA");
        report = null;
        try {
            report = reportDAO.getReportForMeasurementsFromDate(mSetUUID, fromDate, true);
        } catch (NoDataException ex) {
            log.error("Unable to get Report: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
        
//----- GET REPORT FOR TIME PERIOD
        fromDate = new Date(Long.parseLong("1346146187675"));
        Date toDate = new Date(Long.parseLong("1346146199684"));
        log.info("Getting report for period " + fromDate + " - " + toDate);
        report = null;
        try {
            report = reportDAO.getReportForMeasurementsForTimePeriod(mSetUUID, fromDate, toDate, false);
        } catch (NoDataException ex) {
            log.error("Unable to get Report: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
        
        log.info("Getting report for period " + fromDate + " - " + toDate + " --- WITH DATA");
        report = null;
        try {
            report = reportDAO.getReportForMeasurementsForTimePeriod(mSetUUID, fromDate, toDate, true);
        } catch (NoDataException ex) {
            log.error("Unable to get Report: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
        
//----- GET REPORT FOR ALL MEASUREMENTS
        log.info("Getting report for all measurements");
        report = null;
        try {
            report = reportDAO.getReportForAllMeasurements(mSetUUID, false);
        } catch (NoDataException ex) {
            log.error("Unable to get Report: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
        
        log.info("Getting report for all measurements --- WITH DATA");
        report = null;
        try {
            report = reportDAO.getReportForAllMeasurements(mSetUUID, true);
        } catch (NoDataException ex) {
            log.error("Unable to get Report: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
        
        
        Random rand = new Random();
        Set<UUID> measurements = new HashSet<UUID>();
        for (Measurement m : report.getMeasurementSet().getMeasurements())
        {
            if (rand.nextBoolean()) measurements.add(m.getUUID());
        }
        
        log.info("Setting 1 measurement to be synced");
        try {
            IMeasurementDAO measurementDAO = edm.getMeasurementDAO();
            measurementDAO.setSyncFlagForAMeasurement(measurements.iterator().next(), true);
        } catch (Exception ex) {
            log.error ("Unable to get Measurement DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        log.info("Setting " + measurements.size() + " measurements to be synced");
        try {
            IMeasurementDAO measurementDAO = edm.getMeasurementDAO();
            measurementDAO.setSyncFlagForMeasurements(measurements, true);
        } catch (Exception ex) {
            log.error ("Unable to get Measurement DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        log.info("Getting report for the 100 last unsynced measurements --- WITH DATA");
        report = null;
        try {
            report = reportDAO.getReportForUnsyncedMeasurementsFromDate(mSetUUID, fromDate, 100, true);
        } catch (NoDataException ex) {
            log.error("Unable to get Report: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
        
        log.info("Deleting synchronised measurements");
        try {
            IMeasurementDAO measurementDAO = edm.getMeasurementDAO();
            measurementDAO.deleteSynchronisedMeasurements();
            log.info("Successfully deleted any synchronised measurements");
        } catch (Exception ex) {
            log.error ("Unable to get Measurement DAO: " + ex.getMessage(), ex);
            throw ex;
        }
    }
    
    
    public static void updateAndDeleteReportTests(IMonitoringEDM edm, UUID mSetUUID) throws Exception
    {
        IReportDAO reportDAO = null;
        try {
            reportDAO = edm.getReportDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Report DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        log.info("Saving Report for measurement set " + mSetUUID.toString());
        
        UUID reportUUID = UUID.randomUUID();
        Report report = getReportWithRandomMeasurements(reportUUID, mSetUUID, 5);
        
        try {
            reportDAO.saveReport(report, true);
            log.info("Report saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save Report: " + ex.getMessage(), ex);
        }
        
//----- GET REPORT BY UUID
        log.info("Getting report by UUID");
        report = null;
        try {
            report = reportDAO.getReport(reportUUID, true);
        } catch (NoDataException ex) {
            log.error("Unable to get Report: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
        
//----- SET SYNCED FLAG FOR MEASUREMENTS FOR REPORT BY UUID
        log.info("Setting sync flag for measurements for report by UUID: " + reportUUID);
        try {
            reportDAO.setReportMeasurementsSyncFlag(reportUUID, true);
            log.info("Report updated successfully");
        } catch (Exception ex) {
            log.error("Unable to update Report: " + ex.getMessage(), ex);
        }
        
//----- DELETE REPORT BY UUID
        log.info("Deleting report by UUID: " + reportUUID);
        try {
            reportDAO.deleteReport(reportUUID, true);
            log.info("Report deleted successfully");
        } catch (Exception ex) {
            log.error("Unable to delete Report: " + ex.getMessage(), ex);
        }
    }
    
    public static void reportSynchronisationTests(IMonitoringEDM edm, UUID mSetUUID) throws Exception
    {
        IReportDAO reportDAO = null;
        try {
            reportDAO = edm.getReportDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Report DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        int numReports = 5;
        int numMeasurements = 2;
        
        List<UUID> reportUUIDList = new ArrayList<UUID>();
        log.info("Saving " + numReports + " Reports with " + numMeasurements + " measurements each for measurement set " + mSetUUID.toString());
        for (int i = 0; i < numReports; i++)
        {
            UUID reportUUID = UUID.randomUUID();
            Report report = getReportWithRandomMeasurements(reportUUID, mSetUUID, numMeasurements);

            try {
                reportDAO.saveReport(report, true);
                reportUUIDList.add(reportUUID);
                log.info("Report saved successfully!");
            } catch (Exception ex) {
                log.error("Unable to save Report: " + ex.getMessage(), ex);
            }
        }
        
//----- SET SYNCED FLAG FOR MEASUREMENTS FOR REPORT BY UUID
        Random rand = new Random();
        int counter = 0;
        for (UUID reportUUID : reportUUIDList)
        {
            if (rand.nextBoolean())
            {
                log.info("Setting sync flag for measurements for report by UUID: " + reportUUID);
                try {
                    reportDAO.setReportMeasurementsSyncFlag(reportUUID, true);
                    counter++;
                    log.info("Report updated successfully");
                } catch (Exception ex) {
                    log.error("Unable to update Report: " + ex.getMessage(), ex);
                }
            }
        }
        log.info("Set the sync flag for " + counter + " Reports");
        
//----- GET REPORT FOR UNSYNC'ED MEASUREMENTS
        log.info("Should now get a report with " + ((numReports-counter) * numMeasurements) + " unsynchronised measurements");
        Report report = null;
        try {
            Date date = new Date(new Date().getTime() - 10000L);
            log.info("Getting Report for last 100 unsync'ed measurements from date: " + date + " ("+ date.getTime() + ")");
            report = reportDAO.getReportForUnsyncedMeasurementsFromDate(mSetUUID, date, 100, true);
        } catch (NoDataException ex) {
            log.error("Unable to get Report: " + ex.toString());
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.toString(), ex);
        }
        printReportDetails(report);
    }
    
    public static void duplicateMeasurementsTests(IMonitoringEDM edm, UUID mSetUUID) throws Exception
    {
        IReportDAO reportDAO = null;
        try {
            reportDAO = edm.getReportDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Report DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        Report report = null;
        int numMeasurements = 1;
        log.info("Saving 2 Reports with " + numMeasurements + " measurement(s) for measurement set " + mSetUUID.toString());

        Report report1 = getReportWithRandomMeasurements(UUID.randomUUID(), mSetUUID, numMeasurements);
        Report report2 = getReportWithRandomMeasurements(UUID.randomUUID(), mSetUUID, numMeasurements);
        try {
            reportDAO.saveReport(report1, true);
            reportDAO.saveReport(report2, true);
            log.info("Reports saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save Report: " + ex.getMessage(), ex);
        }
        
        // should now be 2 measurements in the DB - no reports
        try {
            report = reportDAO.getReportForAllMeasurements(mSetUUID, true);
            
            if (report.getNumberOfMeasurements() == null) {
                log.error("Got a report, but the number of measurements argument is NULL");
            } else if (report.getNumberOfMeasurements() != 2) {
                log.error("Should have been 2 measurements in the database, but got a report with " + report.getNumberOfMeasurements());
            }
        } catch (Exception ex) {
            log.error("Failed to get a report for all measurements: " + ex.toString());
        }
        
//----- SAVING REPORT WITH DUPLICATE MEASUREMENT
        log.info("Creating and saving Measurements for new Report and adding a measurement already saved in the DB");
        Report reportWithDuplicates = getReportWithRandomMeasurements(UUID.randomUUID(), mSetUUID, 10);
        reportWithDuplicates.getMeasurementSet().addMeasurement(report1.getMeasurementSet().getMeasurements().iterator().next());
        reportWithDuplicates.getMeasurementSet().addMeasurements(getReportWithRandomMeasurements(UUID.randomUUID(), mSetUUID, 10).getMeasurementSet().getMeasurements());
        reportWithDuplicates.getMeasurementSet().addMeasurement(report2.getMeasurementSet().getMeasurements().iterator().next());
        reportWithDuplicates.getMeasurementSet().addMeasurements(getReportWithRandomMeasurements(UUID.randomUUID(), mSetUUID, 10).getMeasurementSet().getMeasurements());
        
        try {
            reportDAO.saveMeasurements(reportWithDuplicates);
            log.info("Measurements for Report saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save Measurements for Report: " + ex.getMessage(), ex);
        }
        
        // should now be 32 measurements in the DB - no reports
        try {
            report = reportDAO.getReportForAllMeasurements(mSetUUID, true);
            
            if (report.getNumberOfMeasurements() == null) {
                log.error("Got a report, but the number of measurements argument is NULL");
            } else if (report.getNumberOfMeasurements() != 32) {
                log.error("Should have been 32 measurements in the database, but got a report with " + report.getNumberOfMeasurements());
            }
        } catch (Exception ex) {
            log.error("Failed to get a report for all measurements: " + ex.toString());
        }
        
//----- NEED TO TEST THIS TOO
        log.info("Creating and saving Report with a measurement already saved in the DB");
        Report reportWithDuplicates2 = getReportWithRandomMeasurements(UUID.randomUUID(), mSetUUID, 10);
        reportWithDuplicates2.getMeasurementSet().addMeasurement(report1.getMeasurementSet().getMeasurements().iterator().next());
        try {
            reportDAO.saveReport(reportWithDuplicates2, true);
            log.info("Report saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save Report: " + ex.getMessage(), ex);
        }
        
        // should now be 42 measurements in the DB - and 1 report
        try {
            report = reportDAO.getReportForAllMeasurements(mSetUUID, true);
            
            if (report.getNumberOfMeasurements() == null) {
                log.error("Got a report, but the number of measurements argument is NULL");
            } else if (report.getNumberOfMeasurements() != 42) {
                log.error("Should have been 42 measurements in the database, but got a report with " + report.getNumberOfMeasurements());
            }
        } catch (Exception ex) {
            log.error("Failed to get a report for all measurements: " + ex.toString());
        }
        
        log.info("Trying to save a measurement that's already in the database");
        IMeasurementDAO measurementDAO = null;
        try {
            measurementDAO = edm.getMeasurementDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Measurement DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        try {
            measurementDAO.saveMeasurement(report1.getMeasurementSet().getMeasurements().iterator().next());
        } catch (Exception ex) {
            log.error("Unable to save measurement: " + ex.toString());
        }
    }
    
    public static void printReportDetails(Report report)
    {
        if (report != null)
        {
            log.info(" * Report details");
            log.info("    - Report UUID:  " + report.getUUID().toString());
            log.info("    - MSet   UUID:  " + report.getMeasurementSet().getID().toString());
            log.info("    - Report date:  " + report.getReportDate() + " (" + report.getReportDate().getTime() + ")");
            log.info("    - From date:    " + report.getFromDate() + " (" + report.getFromDate().getTime() + ")");
            log.info("    - To date:      " + report.getToDate() + " (" + report.getToDate().getTime() + ")");
            log.info("    - Num measures: " + report.getNumberOfMeasurements());
            
            if ((report.getMeasurementSet().getMeasurements() == null) || report.getMeasurementSet().getMeasurements().isEmpty()) {
                log.info("    * No measurements data!");
            } else {
                log.info("    * Got " + report.getMeasurementSet().getMeasurements().size() + " measurements:");
                
                for (Measurement m : report.getMeasurementSet().getMeasurements())
                {
                    log.info("      - " + m.getUUID() + "\t" + m.getValue() + "\t" + m.getTimeStamp() + "(" + m.getTimeStamp().getTime() + ")");
                }
            }
        }
    }
    
    public static void printExperimentDetails(IMonitoringEDM edm, UUID expUUID, boolean withSubClasses) throws Exception
    {
        IExperimentDAO expDAO = null;
        try {
            expDAO = edm.getExperimentDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Experiment DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        log.info("Getting experiment object");
        Experiment expFromDB = null;
        try {
            expFromDB = expDAO.getExperiment(expUUID, withSubClasses);
        } catch (Exception ex) {
            log.error("Unable to get experiment: " + ex.getMessage());
        }
        
        printExperimentDetails(expFromDB);
    }

    public static void printExperimentDetails(Experiment experiment) throws Exception
    {
        if (experiment == null)
            return;
        
        log.info("Experiment details:");
        log.info("  * Basic info");
        if (experiment.getUUID() != null) log.info("    - UUID:  " + experiment.getUUID());
        if (experiment.getName() != null) log.info("    - Name:  " + experiment.getName());
        if (experiment.getDescription() != null) log.info("    - Desc:  " + experiment.getDescription());
        if (experiment.getStartTime() != null) log.info("    - Start: " + experiment.getStartTime() + " (" + experiment.getStartTime().getTime() + ")");
        if (experiment.getEndTime() != null) log.info("    - End:   " + experiment.getEndTime() + " (" + experiment.getEndTime().getTime() + ")");
        if (experiment.getExperimentID() != null) log.info("    - ID:    " + experiment.getExperimentID());
        
        if ((experiment.getMetricGenerators() == null) || experiment.getMetricGenerators().isEmpty()) {
            log.info("  * There are NO metric generators");
        } else {
            log.info("  * There's " + experiment.getMetricGenerators().size() + " metric generator(s)");
            
            for (MetricGenerator mGen : experiment.getMetricGenerators())
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

                                log.info("            - MeasurementSet details:");
                                log.info("              - UUID: " + mSet.getID());
                                log.info("              - Attribute UUID: " + mSet.getAttributeID());
                                
                                if (mSet.getMetric() != null)
                                {
                                    log.info("              - Metric:");
                                    if (mSet.getMetric().getUUID() != null) log.info("                  - UUID:  " + mSet.getMetric().getUUID());
                                    if (mSet.getMetric().getMetricType() != null) log.info("                  - Type:  " + mSet.getMetric().getMetricType());
                                    if (mSet.getMetric().getUnit() != null) log.info("                  - Unit:  " + mSet.getMetric().getUnit());
                                }
                            }
                        }
                    }
                }
                
                if ((mGen.getEntities() == null) || mGen.getEntities().isEmpty()){
                    log.info("      * There are NO entities in the metric generator");
                } else {
                    log.info("      * There's " + mGen.getEntities().size() + " entity/entities in the metric generator");

                    for (Entity entity : mGen.getEntities())
                    {
                        if (entity.getUUID() != null) log.info("        - UUID:  " + entity.getUUID());
                        if (entity.getEntityID() != null) log.info("        - ID:    " + entity.getEntityID());
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
            } // end for each metric generator
        }
    }
    
    public static void printMetricGeneratorDetails(MetricGenerator mGen) throws Exception
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
                        log.info("            - MeasurementSet details:");
                        log.info("              - UUID: " + mSet.getID());
                        log.info("              - Attribute UUID:" + mSet.getAttributeID());
                        
                        if (mSet.getMetric() != null)
                        {
                            log.info("              - Metric:");
                            if (mSet.getMetric().getUUID() != null) log.info("                  - UUID:  " + mSet.getMetric().getUUID());
                            if (mSet.getMetric().getMetricType() != null) log.info("                  - Type:  " + mSet.getMetric().getMetricType());
                            if (mSet.getMetric().getUnit() != null) log.info("                  - Unit:  " + mSet.getMetric().getUnit());
                        }
                    }
                }
            }
        }
    }
    
    public static void printMetricGroupDetails(MetricGroup mGrp) throws Exception
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
                log.info("            - MeasurementSet details:");
                log.info("              - UUID: " + mSet.getID());
                log.info("              - Attribute UUID:" + mSet.getAttributeID());

                if (mSet.getMetric() != null)
                {
                    log.info("              - Metric:");
                    if (mSet.getMetric().getUUID() != null) log.info("                  - UUID:  " + mSet.getMetric().getUUID());
                    if (mSet.getMetric().getMetricType() != null) log.info("                  - Type:  " + mSet.getMetric().getMetricType());
                    if (mSet.getMetric().getUnit() != null) log.info("                  - Unit:  " + mSet.getMetric().getUnit());
                }
            }
        }
    }
    
    public static void printMeasurementSetDetails(MeasurementSet mSet) throws Exception
    {
        log.info("            - MeasurementSet details:");
        log.info("              - UUID: " + mSet.getID());
        log.info("              - Attribute UUID:" + mSet.getAttributeID());

        if (mSet.getMetric() != null)
        {
            log.info("              - Metric:");
            if (mSet.getMetric().getUUID() != null) log.info("                  - UUID:  " + mSet.getMetric().getUUID());
            if (mSet.getMetric().getMetricType() != null) log.info("                  - Type:  " + mSet.getMetric().getMetricType());
            if (mSet.getMetric().getUnit() != null) log.info("                  - Unit:  " + mSet.getMetric().getUnit());
        }
    }
    
}
