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
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.test;

import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Mass;
import javax.measure.unit.Unit;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Metric;

import javax.measure.Measure;
import javax.measure.unit.*;
import javax.measure.quantity.*;
import static javax.measure.unit.SI.*;
import static javax.measure.unit.NonSI.*;
import static javax.measure.unit.Dimension.*;


import static javax.measure.unit.SystemOfUnits.*;
import static javax.measure.unit.UnitFormat.*;
import org.apache.log4j.Logger;
import org.jscience.physics.amount.Amount;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricType;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.ExperimentDataManager;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.dao.ExperimentDataManagerDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IEntityDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMeasurementDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMeasurementSetDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMetricDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMetricGroupDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IReportDAO;

/**
 *
 * @author Vegard Engen
 */
public class EDMTest
{
    static Logger log = Logger.getLogger(EDMTest.class);
    
    public static void main(String[] args) throws Exception
    {
        Metric m = new Metric();
        m.setUnit(MINUTE);
        //Unit<Mass> unit = GRAM;
        Unit min = MINUTE;//SECONDS.times(60);
        Unit<Duration> sec = SECOND;
        Unit<Duration> ms = MILLI(SECOND);
        
        Unit metre = METRE;
        Unit<Length> cm = CENTI(METER);
        Unit<Length> FOOT = METER.times(3048).divide(10000); // Exact.
        
        Unit<Volume> cl = CENTI(LITER);
        
        Unit<Velocity> mps = METRES_PER_SECOND;
        
        // Conversion between units.
        System.out.println(KILO(METRE).getConverterTo(MILE).convert(10));
        
        // Dimension checking (allows/disallows conversions)    
        System.out.println(ELECTRON_VOLT.isCompatible(WATT.times(HOUR)));

        // Retrieval of the unit dimension (depends upon the current model).
        System.out.println(ELECTRON_VOLT.getDimension());
        
        Amount<Length> x = Amount.valueOf(100, NonSI.INCH);
        
        System.out.println("bps? " + BIT.divide(SECOND));
        
        Unit bps = BIT.divide(SECOND);
        m.setUnit(BIT.divide(SECOND));
        System.out.println(m.getUnit().getClass());
        
        
        
        // tweets per second
        
        
        // cpu load
        
        
        // number of VMs
        
        ExperimentDataManager edm = new ExperimentDataManager();
        UUID expUUID = UUID.fromString("bfe4c710-61ba-46f8-a519-be2f7808192e");
        UUID entityUUID = UUID.fromString("5718cd67-4310-4b2c-aeb9-9b72314630ca");
        UUID attributeUUID = UUID.fromString("4f2817b5-603a-4d02-a032-62cfca314962");
        UUID mGenUUID = UUID.fromString("782e5097-2e29-4219-a984-bf48dfcd7f63");
        UUID mGrpUUID = UUID.fromString("189064a5-f1d8-41f2-b2c1-b88776841009");
        UUID mSetUUID = UUID.fromString("2b915932-41b1-45d7-b4f6-2de4f30020b8");
        UUID reportUUID = UUID.fromString("165c8058-5c67-4f92-ae34-df7ee2129821");
        
        //-- Create and get experiments --//
        //experiments(edm, expUUID);
        
        //entities(edm, entityUUID, attributeUUID, expUUID);
        
        //metrics(edm);
        
        //metricGenerator(edm, expUUID, entityUUID, mGenUUID);
        
        //metricGroup(edm, mGenUUID, mGrpUUID);
        
        //measurementSet(edm, attributeUUID, mGrpUUID, mSetUUID);
        
        //measurement(edm, mSetUUID);
        
        //experimentCompleteChain(edm, expUUID, entityUUID, attributeUUID, mGenUUID, mGrpUUID, mSetUUID);
        
        //printDetailsForExperiment(edm, expUUID);
        
        //saveReport(edm, mSetUUID, reportUUID);
        
        //getReport(edm, mSetUUID, reportUUID);
    }
    
    public static void experiments(ExperimentDataManager edm, UUID expUUID) throws Exception
    {
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
            expFromDB = expDAO.getExperiment(expUUID);
            //exp2 = expDAO.getExperiment(UUID.fromString("5718cd67-4310-4b2c-aeb9-9b72314630ca"));
            //exp2 = expDAO.getExperiment(UUID.fromString("3fe0769d-ffae-4173-9c24-07ff7819b5cb"));
        } catch (Exception ex) {
            log.error("Unable to get experiment: " + ex.getMessage());
        }
        
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
        
        log.info("Getting all experiments");
        Set<Experiment> experiments = null;
        
        try {
            experiments = expDAO.getExperiments();
            
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
    
    public static void entities(ExperimentDataManager edm, UUID entityUUID, UUID attributeUUID, UUID expUUID) throws Exception
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
        entity.addtAttribute(new Attribute(attributeUUID, entityUUID, "CPU", "CPU performance"));
        entity.addtAttribute(new Attribute(UUID.randomUUID(), entityUUID, "Network", "Network performance"));
        entity.addtAttribute(new Attribute(UUID.randomUUID(), entityUUID, "Disk", "Disk performance"));
        
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
        randomEntity.addtAttribute(new Attribute(UUID.randomUUID(), randomEntity.getUUID(), "height", "The height of the random entity"));
        
        try {
            entityDAO.saveEntity(randomEntity);
            log.info("Entity '" + randomEntity.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save entity: " + ex.getMessage());
        }
        
        log.info("Getting Entity from the DB");
        Entity entityFromDB = null;
        try {
            entityFromDB = entityDAO.getEntity(entityUUID);
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
        }
        
        log.info("Getting all entities");
        Set<Entity> entities = null;
        try {
            entities = entityDAO.getEntities();
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
            entities = entityDAO.getEntitiesForExperiment(expUUID);
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
    
    public static void metricGenerator(ExperimentDataManager edm, UUID expUUID, UUID entityUUID, UUID mGenUUID) throws Exception
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
            metricGeneratorFromDB = metricGeneratorDAO.getMetricGenerator(mGenUUID);
        } catch (Exception ex) {
            log.error("Unable to get MetricGenerator from the DB");
        }
        
        if (metricGeneratorFromDB != null)
        {
            log.info("MetricGenerator details:");
            log.info("  - UUID: " + metricGeneratorFromDB.getUUID());
            log.info("  - Name: " + metricGeneratorFromDB.getName());
            log.info("  - Desc: " + metricGeneratorFromDB.getDescription());
        }
        
        log.info("Getting all metric generators");
        Set<MetricGenerator> metricGeneratorsFromDB = null;
        
        try {
            metricGeneratorsFromDB = metricGeneratorDAO.getMetricGenerators();
        } catch (Exception ex) {
            log.error("Unable to get MetricGenerator set from the DB");
        }
        
        if (metricGeneratorsFromDB != null)
        {
            log.info("Got " + metricGeneratorsFromDB.size() + " metric generator(s):");
            
            for (MetricGenerator mGen : metricGeneratorsFromDB)
            {
                log.info("  * MetricGenerator details:");
                log.info("    - UUID: " + mGen.getUUID());
                log.info("    - Name: " + mGen.getName());
                log.info("    - Desc: " + mGen.getDescription());
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
        
        log.info("Getting all metric generators for experiment: " + randomExpUUID.toString());
        metricGeneratorsFromDB = null;
        
        try {
            metricGeneratorsFromDB = metricGeneratorDAO.getMetricGeneratorsForExperiment(randomExpUUID);
        } catch (Exception ex) {
            log.error("Unable to get MetricGenerator set from the DB");
        }
        
        if (metricGeneratorsFromDB != null)
        {
            log.info("Got " + metricGeneratorsFromDB.size() + " metric generator(s):");
            
            for (MetricGenerator mGen : metricGeneratorsFromDB)
            {
                log.info("  * MetricGenerator details:");
                log.info("    - UUID: " + mGen.getUUID());
                log.info("    - Name: " + mGen.getName());
                log.info("    - Desc: " + mGen.getDescription());
            }
        }
    }
    
    public static void metricGroup(ExperimentDataManager edm, UUID mGenUUID, UUID mGrpUUID) throws Exception
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
    }
    
    public static void measurementSet(ExperimentDataManager edm, UUID attribUUID, UUID mGrpUUID, UUID mSetUUID) throws Exception
    {
        IMeasurementSetDAO mSetDAO = null;
        try {
            mSetDAO = edm.getMeasurementSetDAO();
        } catch (Exception ex) {
            log.error ("Unable to get MeasurementSet DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        log.info("Saving QoS Measurement set");
        Metric metric = new Metric(UUID.randomUUID(), MetricType.RATIO, MILLI(SECOND));
        MeasurementSet mSet = new MeasurementSet(mSetUUID, attribUUID, mGrpUUID, metric);
        
        try {
            mSetDAO.saveMeasurementSet(mSet);
            log.info("MeasurementSet saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save MeasurementSet: " + ex.getMessage(), ex);
        }
        
        log.info("Saving Random Measurement set");
        Metric randomMetric = new Metric(UUID.randomUUID(), MetricType.INTERVAL, METRES_PER_SECOND);
        MeasurementSet randomMSet = new MeasurementSet(UUID.randomUUID(), attribUUID, mGrpUUID, randomMetric);
        
        try {
            mSetDAO.saveMeasurementSet(randomMSet);
            log.info("MeasurementSet saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save MeasurementSet: " + ex.getMessage(), ex);
        }
    }
    
    public static void measurement(ExperimentDataManager edm, UUID mSetUUID) throws Exception
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
    
    public static void experimentCompleteChain(ExperimentDataManager edm, UUID expUUID, UUID entityUUID, UUID attribUUID, UUID mGenUUID, UUID mGrpUUID, UUID mSetUUID) throws Exception
    {
//----- EXPERIMENT
        
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
        exp.setDescription("An experiment description...");
        exp.setStartTime(new Date(Long.parseLong("1345642421005")));
        exp.setEndTime(new Date(Long.parseLong("1440413831014")));
        exp.setExperimentID("/locations/experiment/1337");
        
//----- METRIC GENERATOR
        
        IMetricGeneratorDAO metricGeneratorDAO = null;
        try {
            metricGeneratorDAO = edm.getMetricGeneratorDAO();
        } catch (Exception ex) {
            log.error ("Unable to get MetricGenerator DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        log.info("Creating Experiment MetricGenerator");
        MetricGenerator metricGenerator = new MetricGenerator(mGenUUID, "Experiment MetricGenerator", "A description");
        metricGenerator.addEntity(new Entity(entityUUID));
        exp.addMetricGenerator(metricGenerator);
        
//----- METRIC GROUP
        log.info("Creating QoS MetricGroup");
        MetricGroup metricGroup = new MetricGroup(mGrpUUID, mGenUUID, "Quality of Service", "A group of QoS metrics");
        metricGenerator.addMetricGroup(metricGroup);
        
//----- MEASUREMENT SET
        log.info("Creating QoS Measurement set");
        Metric metric = new Metric(UUID.randomUUID(), MetricType.RATIO, MILLI(SECOND));
        MeasurementSet mSet = new MeasurementSet(mSetUUID, attribUUID, mGrpUUID, metric);
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
            expFromDB = expDAO.getExperiment(expUUID);
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
                                    log.info("                - Attribute:");
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
                        }
                    }
                }
            }
        }
    }
    
    public static void saveReport(ExperimentDataManager edm, UUID mSetUUID, UUID reportUUID) throws Exception
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
        int numMeasurements = 5;
        Random rand = new Random();
        rand.setSeed(new Date().getTime());
        long timeStampFrom = 0;
        long timeStampTo = 0;
        for (int i = 0; i < numMeasurements; i++)
        {
            long timeStamp = new Date().getTime() - (1000 * (numMeasurements-i));
            Measurement measurement = new Measurement(UUID.randomUUID(), mSetUUID, new Date(timeStamp), String.valueOf(rand.nextInt(500)));
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
        
        try {
            reportDAO.saveReport(report);
            log.info("Report saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save Report: " + ex.getMessage(), ex);
            
            // save the report with a random UUID...
            report.setUUID(UUID.randomUUID());
            try {
                reportDAO.saveReport(report);
                log.info("Report saved successfully!");
            } catch (Exception ex2) {
                log.error("Unable to save Report: " + ex2.getMessage(), ex2);

                report.setUUID(UUID.randomUUID());
            }
        }
    }
    
    public static void getReport(ExperimentDataManager edm, UUID mSetUUID, UUID reportUUID) throws Exception
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
            report = reportDAO.getReport(reportUUID);
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
        
        log.info("Getting report by UUID - WITH DATA");
        report = null;
        try {
            report = reportDAO.getReportWithData(reportUUID);
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
        
//----- GET REPORT FOR LATEST VALUE
        log.info("Getting report for latest value");
        report = null;
        try {
            report = reportDAO.getReportForLatestMeasurement(mSetUUID);
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
        
        log.info("Getting report for latest value - WITH DATA");
        report = null;
        try {
            report = reportDAO.getReportForLatestMeasurementWithData(mSetUUID);
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
        
//----- GET REPORT FROM DATE
        Date fromDate = new Date(Long.parseLong("1346146199684"));
        log.info("Getting report from date " + fromDate);
        report = null;
        try {
            report = reportDAO.getReportForMeasurementsAfterDate(mSetUUID, fromDate);
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
        
        log.info("Getting report from date " + fromDate + " --- WITH DATA");
        report = null;
        try {
            report = reportDAO.getReportForMeasurementsAfterDateWithData(mSetUUID, fromDate);
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
            report = reportDAO.getReportForMeasurementsForTimePeriod(mSetUUID, fromDate, toDate);
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
        
        log.info("Getting report for period " + fromDate + " - " + toDate + " --- WITH DATA");
        report = null;
        try {
            report = reportDAO.getReportForMeasurementsForTimePeriodWithData(mSetUUID, fromDate, toDate);
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
        
//----- GET REPORT FOR ALL MEASUREMENTS
        log.info("Getting report for all measurements");
        report = null;
        try {
            report = reportDAO.getReportForAllMeasurements(mSetUUID);
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
        
        log.info("Getting report for all measurements --- WITH DATA");
        report = null;
        try {
            report = reportDAO.getReportForAllMeasurementsWithData(mSetUUID);
        } catch (Exception ex) {
            log.error("Unable to get Report: " + ex.getMessage(), ex);
        }
        printReportDetails(report);
    }
    
    public static void printReportDetails(Report report)
    {
        if (report != null)
        {
            log.info(" * Report details");
            log.info("    - Report UUID:  " + report.getUUID().toString());
            log.info("    - MSet   UUID:  " + report.getMeasurementSet().getUUID().toString());
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
                    log.info("      - " + m.getValue() + "\t" + m.getTimeStamp() + "(" + m.getTimeStamp().getTime() + ")");
                }
            }
        }
    }
}