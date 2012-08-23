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
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl;

import java.util.Date;
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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.dao.ExperimentDataManagerDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IEntityDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IExperimentDAO;

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
        Unit<Duration> min = MINUTE;//SECONDS.times(60);
        Unit<Duration> sec = SECOND;
        Unit<Duration> ms = MILLI(SECOND);
        
        Unit<Length> metre = METRE;
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
        
        
        // tweets per second
        
        
        // cpu load
        
        
        // number of VMs
        
        ExperimentDataManager edm = new ExperimentDataManager();
        UUID expUUID = UUID.fromString("bfe4c710-61ba-46f8-a519-be2f7808192e");
        UUID entityUUID = UUID.fromString("5718cd67-4310-4b2c-aeb9-9b72314630ca");
        UUID attributeUUID = UUID.fromString("4f2817b5-603a-4d02-a032-62cfca314962");
        
        //-- Create and get experiments --//
        experiments(edm, expUUID);
        
        entities(edm, entityUUID, attributeUUID);
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
        
        log.info("Saving Banana experiment");
        Experiment exp = new Experiment();
        exp.setUUID(expUUID);
        exp.setName("Banana Experiment");
        exp.setDescription("A very boring description...");
        exp.setStartTime(new Date(Long.parseLong("1345642421005")));
        exp.setEndTime(new Date());
        exp.setExperimentID("3543");
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
        expRand.setStartTime(new Date(Long.parseLong("1345642421005")));
        try {
            expDAO.saveExperiment(expRand);
            log.info("Experiment '" + expRand.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save experiment: " + ex.getMessage());
        }
        
        log.info("Getting Banana experiment object");
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
    
    public static void entities(ExperimentDataManager edm, UUID entityUUID, UUID attributeUUID) throws Exception
    {
        IEntityDAO entityDAO = null;
        try {
            entityDAO = edm.getEntityDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Entity DAO: " + ex.getMessage(), ex);
            throw ex;
        }
        
        log.info("Saving banana entity");
        Entity entity = new Entity();
        entity.setUUID(entityUUID);
        entity.setName("Banana");
        entity.setDescription("A yellow, edible, herb... yes, herb!");
        entity.addtAttribute(new Attribute(attributeUUID, entityUUID, "skin colour", "Colour of the slippery outer part of the banana that can be left on floors as a risk to health and safety"));
        
        try {
            entityDAO.saveEntity(entity);
            log.info("Entity '" + entity.getName() + "' saved successfully!");
        } catch (Exception ex) {
            log.error("Unable to save entity: " + ex.getMessage());
        }
        
        log.info("Saving an extra attribute for the Banana Entity");
        try {
            Attribute attrib = new Attribute(UUID.randomUUID(), entityUUID, "random attribute", "A random attribute for debugging");
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
    }
}
