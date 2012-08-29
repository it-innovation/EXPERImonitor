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
//      Created Date :          2012-08-23
//      Created for Project :   
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DatabaseConnector;

/**
 *
 * @author Vegard Engen
 */
public class MetricGeneratorHelper
{
    static Logger log = Logger.getLogger(MetricGeneratorHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(MetricGenerator mg, UUID expUUID, DatabaseConnector dbCon, boolean checkForExperiment) throws Exception
    {
        if (mg == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The MetricGenerator object is NULL - cannot save that..."));
        }
        
        // check if all the required information is given; uuid, name, at least one entity, at least one metric group (which should have at least one measurement set)
        
        if (mg.getUUID() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The MetricGenerator UUID is NULL"));
        }
        
        if (mg.getName() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The MetricGenerator name is NULL"));
        }
        
        // check if it exists in the DB already
        try {
            if (objectExists(mg.getUUID(), dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The MetricGenerator already exists; the UUID is not unique"));
            }
        } catch (Exception ex) {
            throw ex;
        }
        
        // check if the experiment exists in the DB, if flagged to do so
        if (checkForExperiment)
        {
            try {
                if (!ExperimentHelper.objectExists(expUUID, dbCon))
                {
                    return new ValidationReturnObject(false, new RuntimeException("The Experiment specified for the MetricGenerator does not exist! UUID not found: " + expUUID.toString()));
                }
            } catch (Exception ex) {
                throw ex;
            }
        }
        
        // check the entities
        if ((mg.getEntities() == null) || mg.getEntities().isEmpty())
        {
            return new ValidationReturnObject(false, new NullPointerException("The MetricGenerator does not have any entities defined"));
        }
        else
        {
            for (Entity entity : mg.getEntities())
            {
                if (entity == null)
                {
                    if (mg.getEntities().size() > 0)
                        return new ValidationReturnObject(false, new NullPointerException("One or more MetricGenerator Entitis are NULL"));
                    else
                        return new ValidationReturnObject(false, new NullPointerException("The MetricGenerator's Entity is NULL"));
                }
                /*else if (!EntityHelper.objectExists(entity, dbCon))
                {
                    return new ValidationReturnObject(false, new RuntimeException("An Entity the Metric Generator points to does not exist in the DB (entity UUID: " + entity.toString() + ")"));
                }*/
            }
        }
        
        // if any metric groups, check if they are valid
        if ((mg.getMetricGroups() != null) || !mg.getMetricGroups().isEmpty())
        {
            for (MetricGroup mGrp : mg.getMetricGroups())
            {
                if (mGrp == null)
                {
                    if (mg.getMetricGroups().size() > 0)
                        return new ValidationReturnObject(false, new NullPointerException("One or more MetricGroup objects are NULL"));
                    else
                        return new ValidationReturnObject(false, new NullPointerException("The MetricGenerator's MetricGroup is NULL"));
                }
                /*else if (!MetricGroupHelper.objectExists(mGrp.getUUID(), dbCon))
                {
                    return new ValidationReturnObject(false, new RuntimeException("A MetricGroup the Metric Generator points to does not exist in the DB (UUID: " + mGrp.getUUID().toString() + ")"));
                }*/
            }
        }
        
        return new ValidationReturnObject(true);
    }
    
    /**
     * Checks the available parameters in the object and adds to the lists, the
     * table names and values accordingly.
     * 
     * OBS: it is assumed that the object has been validated to have at least the
     * minimum information.
     * 
     * @param mg
     * @param valueNames
     * @param values 
     */
    public static void getTableNamesAndValues(MetricGenerator mg, UUID expUUID, List<String> valueNames, List<String> values)
    {
        if (mg == null)
            return;
        
        if ((valueNames == null) || (values == null))
            return;

        valueNames.add("mGenUUID");
        values.add("'" + mg.getUUID().toString() + "'");
        
        valueNames.add("expUUID");
        values.add("'" + expUUID.toString() + "'");
        
        valueNames.add("name");
        values.add("'" + mg.getName() + "'");
        
        if (mg.getDescription() != null)
        {
            valueNames.add("description");
            values.add("'" + mg.getDescription() + "'");
        }
    }
    
    public static boolean objectExists(UUID uuid, DatabaseConnector dbCon) throws Exception
    {
        return DBUtil.objectExistsByUUID("MetricGenerator", "mGenUUID", uuid, dbCon);
    }
    
    public static void saveMetricGenerator(DatabaseConnector dbCon, MetricGenerator metricGen, UUID experimentUUID, boolean closeDBcon) throws Exception
    {
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        // will validate the metric groups too, if any are given
        // final flag is to check that the experiment is in the DB already
        ValidationReturnObject returnObj = MetricGeneratorHelper.isObjectValidForSave(metricGen, experimentUUID, dbCon, true);
        if (!returnObj.valid)
        {
            log.error("Cannot save the MetricGenerator object: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        boolean exception = false;
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            // get the table names and values according to what's available in the
            // object
            List<String> valueNames = new ArrayList<String>();
            List<String> values = new ArrayList<String>();
            MetricGeneratorHelper.getTableNamesAndValues(metricGen, experimentUUID, valueNames, values);
            
            String query = DBUtil.getInsertIntoQuery("MetricGenerator", valueNames, values);
            ResultSet rs = dbCon.executeQuery(query, Statement.RETURN_GENERATED_KEYS);
            
            // check if the result set got the generated table key
            if (rs.next()) {
                String key = rs.getString(1);
                log.debug("Saved MetricGenerator " + metricGen.getName() + " with key: " + key);
            } else {
                throw new RuntimeException("No index returned after saving MetricGenerator " + metricGen.getName());
            }
        } catch (Exception ex) {
            exception = true;
            log.error("Error while saving MetricGenerator: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving MetricGenerator: " + ex.getMessage(), ex);
        } finally {
            if ((exception || (metricGen.getMetricGroups() == null) || metricGen.getMetricGroups().isEmpty()) && closeDBcon)
                dbCon.close();
        }
        
        // save any metric groups if not NULL
        try {
            if ((metricGen.getMetricGroups() != null) || !metricGen.getMetricGroups().isEmpty())
            {
                for (MetricGroup mGrp : metricGen.getMetricGroups())
                {
                    if (mGrp != null)
                        MetricGroupHelper.saveMetricGroup(dbCon, mGrp, false); // flag not to close the DB connection
                }
            }
        } catch (Exception ex) {
            exception = true;
            throw ex;
        } finally {
            if (exception && closeDBcon)
                dbCon.close();
        }
        
        // save any entities if not NULL and if not already existing
        try {
            for (Entity entity : metricGen.getEntities())
            {
                // check if the entity exists; if not, then save it
                if (!EntityHelper.objectExists(entity.getUUID(), dbCon))
                {
                    EntityHelper.saveEntity(dbCon, entity, false);
                }
            }
        } catch (Exception ex) {
            exception = true;
            throw ex;
        } finally {
            if (exception && closeDBcon)
                dbCon.close();
        }
        
        try {
            // make link between MG and Entity in MetricGenerator_Entity table
            for (Entity entity : metricGen.getEntities())
            {
                linkMetricGeneratorAndEntity(dbCon, metricGen.getUUID(), entity.getUUID(), false);
            }
        } catch (Exception ex) {
            exception = true;
            throw ex;
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
    }
    
    public static void linkMetricGeneratorAndEntity(DatabaseConnector dbCon, UUID mGenUUID, UUID entityUUID, boolean closeDBcon) throws Exception
    {
        if (mGenUUID == null)
        {
            log.error("Cannot link metric generator and entity because the metric generator UUID is NULL!");
            throw new NullPointerException("Cannot link metric generator and entity because the metric generator UUID is NULL!");
        }
        
        if (entityUUID == null)
        {
            log.error("Cannot link metric generator and entity because the entity UUID is NULL!");
            throw new NullPointerException("Cannot link metric generator and entity because the entity UUID is NULL!");
        }
        
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = "INSERT INTO MetricGenerator_Entity (mGenUUID, entityUUID) VALUES ("
                    + "'" + mGenUUID.toString() + "', '" + entityUUID.toString() + "')";
            ResultSet rs = dbCon.executeQuery(query, Statement.RETURN_GENERATED_KEYS);
            
            // check if the result set got the generated table key
            if (rs.next()) {
                String key = rs.getString(1);
                log.debug("Saved MetricGenerator - Entity link");
            } else {
                throw new RuntimeException("No index returned after saving MetricGenerator - Entity link");
            }
        } catch (Exception ex) {
            log.error("Error while saving MetricGenerator - Entity link: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving MetricGenerator - Entity link: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
    }
    
    public static MetricGenerator getMetricGenerator(DatabaseConnector dbCon, UUID metricGenUUID, boolean closeDBcon) throws Exception
    {
        if (metricGenUUID == null)
        {
            log.error("Cannot get a MetricGenerator object with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot get a MetricGenerator object with the given UUID because it is NULL!");
        }
        
        if (!MetricGeneratorHelper.objectExists(metricGenUUID, dbCon))
        {
            log.error("There is no MetricGenerator with the given UUID: " + metricGenUUID.toString());
            throw new RuntimeException("There is no MetricGenerator with the given UUID: " + metricGenUUID.toString());
        }
        
        MetricGenerator metricGenerator = null;
        boolean exception = false;
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = "SELECT * FROM MetricGenerator WHERE mGenUUID = '" + metricGenUUID + "'";
            ResultSet rs = dbCon.executeQuery(query);
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                String name = rs.getString("name");
				String description = rs.getString("description");
                
                metricGenerator = new MetricGenerator(metricGenUUID, name, description);
            }
            else // nothing in the result set
            {
                log.error("There is no MetricGenerator with the given UUID: " + metricGenUUID.toString());
                throw new RuntimeException("There is no MetricGenerator with the given UUID: " + metricGenUUID.toString());
            }
        } catch (Exception ex) {
            exception = true;
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (exception)
                dbCon.close();
        }
        
        // check if there's any metric groups
        Set<MetricGroup> metricGroups = null;
        
        try {
            metricGroups = MetricGroupHelper.getMetricGroupsForMetricGenerator(dbCon, metricGenUUID, false); // don't close the connection
        } catch (Exception ex) {
            log.error("Caught an exception when getting metric groups for MetricGenerator (UUID: " + metricGenUUID.toString() + "): " + ex.getMessage());
            throw new RuntimeException("Unable to get MetricGenerator object due to an issue with getting its metric groups: " + ex.getMessage(), ex);
        } finally {
            if (exception)
                dbCon.close();
        }
        
        metricGenerator.setMetricGroups(metricGroups);
        
        // get any Entities
        Set<Entity> entities = new HashSet<Entity>();
        
        try {
            entities = EntityHelper.getEntitiesForMetricGenerator(dbCon, metricGenUUID, false); // don't close the connection
        } catch (Exception ex) {
            log.error("Caught an exception when getting metric groups for MetricGenerator (UUID: " + metricGenUUID.toString() + "): " + ex.getMessage());
            throw new RuntimeException("Unable to get MetricGenerator object due to an issue with getting its metric groups: " + ex.getMessage(), ex);
        } finally {
            if (exception)
                dbCon.close();
        }
        
        metricGenerator.setEntities(entities);
        
        return metricGenerator;
    }

    public static Set<MetricGenerator> getMetricGenerators(DatabaseConnector dbCon) throws Exception
    {
        Set<MetricGenerator> metricGenerators = new HashSet<MetricGenerator>();
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = "SELECT mGenUUID FROM MetricGenerator";
            ResultSet rs = dbCon.executeQuery(query);
            
            // check if anything got returned (connection closed in finalise method)
            while (rs.next())
            {
                String uuidStr = rs.getString("mGenUUID");
                
                if (uuidStr == null)
                {
                    log.error("Skipping a MetricGenerator, which does not have a UUID in the DB");
                    continue;
                }
                
                metricGenerators.add(getMetricGenerator(dbCon, UUID.fromString(uuidStr), false));
            }
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            dbCon.close();
        }
        
        return metricGenerators;
    }
    
    /**
     * Overloaded method, with the option to set a flag whether to close the
     * DB connection or not.
     * @param expUUID
     * @param closeDBcon
     * @return
     * @throws Exception 
     */
    public static Set<MetricGenerator> getMetricGeneratorsForExperiment(DatabaseConnector dbCon, UUID expUUID, boolean closeDBcon) throws Exception
    {
        if (expUUID == null)
        {
            log.error("Cannot get any MetricGenerator objects for an Experiment with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot get any MetricGenerator objects for an Experiment with the given UUID because it is NULL!");
        }
        
        Set<MetricGenerator> metricGenerators = new HashSet<MetricGenerator>();
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = "SELECT mGenUUID FROM MetricGenerator WHERE expUUID = '" + expUUID.toString() + "'";
            ResultSet rs = dbCon.executeQuery(query);
            
            // check if anything got returned (connection closed in finalise method)
            while (rs.next())
            {
                String uuidStr = rs.getString("mGenUUID");
                
                if (uuidStr == null)
                {
                    log.error("Skipping a MetricGenerator, which does not have a UUID in the DB");
                    continue;
                }
                
                metricGenerators.add(getMetricGenerator(dbCon, UUID.fromString(uuidStr), false));
            }
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            dbCon.close();
        }
        
        return metricGenerators;
    }
}
