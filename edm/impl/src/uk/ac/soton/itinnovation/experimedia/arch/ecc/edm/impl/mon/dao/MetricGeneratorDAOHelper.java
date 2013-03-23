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
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.mon.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.IECCLogger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.NoDataException;

/**
 * A helper class for validating and executing queries for the Metric Generators.
 * 
 * @author Vegard Engen
 */
public class MetricGeneratorDAOHelper
{
    static IECCLogger log = Logger.getLogger(MetricGeneratorDAOHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(MetricGenerator mg, UUID expUUID, boolean checkForExperiment, Connection connection, boolean closeDBcon) throws Exception
    {
        if (mg == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The MetricGenerator object is NULL - cannot save that..."));
        }
        
        // check if all the required information is given; uuid, name, at least one entity, at least one metric group (which should have at least one measurement set)
        
        if (mg.getUUID() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The MetricGenerator UUID is NULL"));
        }
        
        if (mg.getName() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The MetricGenerator name is NULL"));
        }
        
        // check if it exists in the DB already
        /*
        try {
            if (objectExists(mg.getUUID(), connection))
            {
                return new ValidationReturnObject(false, new RuntimeException("The MetricGenerator already exists; the UUID is not unique"));
            }
        } catch (Exception ex) {
            throw ex;
        }
        */
        
        if (expUUID == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Experiment UUID is NULL"));
        }
        
        // check if the experiment exists in the DB, if flagged to do so
        if (checkForExperiment)
        {
            log.debug("Checking if experiment exists (UUID " + expUUID.toString() + ")");
            try {
                if (!ExperimentDAOHelper.objectExists(expUUID, connection, closeDBcon))
                {
                    return new ValidationReturnObject(false, new IllegalArgumentException("The Experiment specified for the MetricGenerator does not exist! UUID not found: " + expUUID.toString()));
                }
                log.debug("The experiment exists");
            } catch (Exception ex) {
                throw ex;
            }
        }
        
        // check the entities
        if ((mg.getEntities() == null) || mg.getEntities().isEmpty())
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The MetricGenerator does not have any entities defined"));
        }
        else
        {
            for (Entity entity : mg.getEntities())
            {
                if (entity == null)
                {
                    if (mg.getEntities().size() > 0)
                        return new ValidationReturnObject(false, new IllegalArgumentException("One or more MetricGenerator Entitis are NULL"));
                    else
                        return new ValidationReturnObject(false, new IllegalArgumentException("The MetricGenerator's Entity is NULL"));
                }
                /*else if (!EntityHelper.objectExists(entity, connection))
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
                        return new ValidationReturnObject(false, new IllegalArgumentException("One or more MetricGroup objects are NULL"));
                    else
                        return new ValidationReturnObject(false, new IllegalArgumentException("The MetricGenerator's MetricGroup is NULL"));
                }
                /*else if (!MetricGroupHelper.objectExists(mGrp.getUUID(), connection))
                {
                    return new ValidationReturnObject(false, new RuntimeException("A MetricGroup the Metric Generator points to does not exist in the DB (UUID: " + mGrp.getUUID().toString() + ")"));
                }*/
            }
        }
        
        return new ValidationReturnObject(true);
    }
    
    public static boolean objectExists(UUID uuid, Connection connection, boolean closeDBcon) throws Exception
    {
        return DBUtil.objectExistsByUUID("MetricGenerator", "mGenUUID", uuid, connection, closeDBcon);
    }
    
    public static void saveMetricGenerator(MetricGenerator metricGen, UUID experimentUUID, Connection connection, boolean closeDBcon) throws Exception
    {
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        // will validate the metric groups too, if any are given
        // final flag is to check that the experiment is in the DB already
        ValidationReturnObject returnObj = MetricGeneratorDAOHelper.isObjectValidForSave(metricGen, experimentUUID, true, connection, false);
        if (!returnObj.valid)
        {
            log.error("Cannot save the MetricGenerator object: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot save the MetricGenerator because the connection to the DB is closed");
            throw new RuntimeException("Cannot save the MetricGenerator because the connection to the DB is closed");
        }
        
        if (closeDBcon)
        {
            log.debug("Starting transaction");
            connection.setAutoCommit(false);
        }
        
        boolean exception = false;
        // save any entities if not NULL and if not already existing
        if ((metricGen.getEntities() != null) && !metricGen.getEntities().isEmpty())
        {
            log.debug("The metric generator has got " + metricGen.getEntities().size() + " entities, which will now be saved (hopefully...)");
            try {
                for (Entity entity : metricGen.getEntities())
                {
                    // check if the entity exists; if not, then save it
                    if (!EntityDAOHelper.objectExists(entity.getUUID(), connection, false))
                    {
                        EntityDAOHelper.saveEntity(entity, connection, false);
                    }
                }
            } catch (Exception ex) {
                exception = true;
                throw ex;
            } finally {
                if (exception && closeDBcon)
                {
                    log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                    connection.rollback();
                    connection.close();
                }
            }
        }
        
        try {
            String query = "INSERT INTO MetricGenerator (mGenUUID, expUUID, name, description) VALUES (?, ?, ?, ?)";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, metricGen.getUUID(), java.sql.Types.OTHER);
            pstmt.setObject(2, experimentUUID, java.sql.Types.OTHER);
            pstmt.setString(3, metricGen.getName());
            pstmt.setString(4, metricGen.getDescription());
            
            pstmt.executeUpdate();

        } catch (Exception ex) {
            exception = true;
            log.error("Error while saving MetricGenerator: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving MetricGenerator: " + ex.getMessage(), ex);
        } finally {
            if (exception && closeDBcon)
            {
                log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                connection.rollback();
                connection.close();
            }
        }
        
        // save any metric groups if not NULL
        try {
            if ((metricGen.getMetricGroups() != null) || !metricGen.getMetricGroups().isEmpty())
            {
                log.debug("Saving " + metricGen.getMetricGroups().size() + " metric group(s) for the metric generator");
                for (MetricGroup mGrp : metricGen.getMetricGroups())
                {
                    if (mGrp != null)
                        MetricGroupDAOHelper.saveMetricGroup(mGrp, connection, false); // flag not to close the DB connection
                }
            }
        } catch (Exception ex) {
            exception = true;
            throw ex;
        } finally {
            if (exception && closeDBcon)
            {
                log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                connection.rollback();
                connection.close();
            }
        }
        
        try {
            log.debug("Making links between MG and Entity entries in the MetricGenerator_Entity table");
            // make link between MG and Entity in MetricGenerator_Entity table
            for (Entity entity : metricGen.getEntities())
            {
                linkMetricGeneratorAndEntity(metricGen.getUUID(), entity.getUUID(), connection, false);
            }
        } catch (Exception ex) {
            exception = true;
            throw ex;
        } finally {
            if (closeDBcon)
            {
                if (exception) {
                    log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                    connection.rollback();
                } else {
                    log.debug("Committing the transaction and closing the connection");
                    connection.commit();
                }
                connection.close();
            }
        }
    }
    
    public static void linkMetricGeneratorAndEntity(UUID mGenUUID, UUID entityUUID, Connection connection, boolean closeDBcon) throws Exception
    {
        if (mGenUUID == null)
        {
            log.error("Cannot link metric generator and entity because the metric generator UUID is NULL!");
            throw new IllegalArgumentException("Cannot link metric generator and entity because the metric generator UUID is NULL!");
        }
        
        if (entityUUID == null)
        {
            log.error("Cannot link metric generator and entity because the entity UUID is NULL!");
            throw new IllegalArgumentException("Cannot link metric generator and entity because the entity UUID is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot link the MetricGenerator and Entity because the connection to the DB is closed");
            throw new RuntimeException("Cannot link the MetricGenerator and Entity because the connection to the DB is closed");
        }
        
        try {
            String query = "INSERT INTO MetricGenerator_Entity (mGenUUID, entityUUID) VALUES (?, ?)";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, mGenUUID, java.sql.Types.OTHER);
            pstmt.setObject(2, entityUUID, java.sql.Types.OTHER);
            
            pstmt.executeUpdate();
        } catch (Exception ex) {
            log.error("Error while saving MetricGenerator - Entity link: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving MetricGenerator - Entity link: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
    }
    
    public static MetricGenerator getMetricGenerator(UUID metricGenUUID, boolean withSubClasses, Connection connection, boolean closeDBcon) throws Exception
    {
        if (metricGenUUID == null)
        {
            log.error("Cannot get a MetricGenerator object with the given UUID because it is NULL!");
            throw new IllegalArgumentException("Cannot get a MetricGenerator object with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the MetricGenerator because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the MetricGenerator because the connection to the DB is closed");
        }
        
        MetricGenerator metricGenerator = null;
        boolean exception = false;
        try {
            String query = "SELECT * FROM MetricGenerator WHERE mGenUUID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, metricGenUUID, java.sql.Types.OTHER);
            ResultSet rs = pstmt.executeQuery();
            
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
                throw new NoDataException("There is no MetricGenerator with the given UUID: " + metricGenUUID.toString());
            }
        } catch (Exception ex) {
            exception = true;
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (exception || (closeDBcon && !withSubClasses))
            {
                log.debug("closing DB connection");
                connection.close();
            }
        }
        
        // check if there's any metric groups
        if (withSubClasses)
        {
            Set<MetricGroup> metricGroups = null;

            try {
                metricGroups = MetricGroupDAOHelper.getMetricGroupsForMetricGenerator(metricGenUUID, withSubClasses, connection, false); // don't close the connection
            } catch (NoDataException nde) {
                log.debug("There were no metric groups for the metric generator");
            } catch (Exception ex) {
                exception = true;
                log.error("Caught an exception when getting metric groups for MetricGenerator (UUID: " + metricGenUUID.toString() + "): " + ex.getMessage());
                throw new RuntimeException("Unable to get MetricGenerator object due to an issue with getting its metric groups: " + ex.getMessage(), ex);
            } finally {
                if (exception)
                {
                    log.debug("closing DB connection");
                    connection.close();
                }
            }

            metricGenerator.setMetricGroups(metricGroups);

            // get any Entities
            Set<Entity> entities = new HashSet<Entity>();

            try {
                entities = EntityDAOHelper.getEntitiesForMetricGenerator(metricGenUUID, withSubClasses, connection, false); // don't close the connection
            } catch (NoDataException nde) {
                log.debug("There were no entities for the metric generator");
            } catch (Exception ex) {
                log.error("Caught an exception when getting metric groups for MetricGenerator (UUID: " + metricGenUUID.toString() + "): " + ex.getMessage());
                throw new RuntimeException("Unable to get MetricGenerator object due to an issue with getting its metric groups: " + ex.getMessage(), ex);
            } finally {
                if (closeDBcon)
                {
                    log.debug("closing DB connection");
                    connection.close();
                }
            }

            metricGenerator.setEntities(entities);
        }
        
        return metricGenerator;
    }

    public static Set<MetricGenerator> getMetricGenerators(boolean withSubClasses, Connection connection) throws Exception
    {
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the MetricGenerator objects because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the MetricGenerator objects because the connection to the DB is closed");
        }
        
        Set<MetricGenerator> metricGenerators = new HashSet<MetricGenerator>();
        try {
            String query = "SELECT mGenUUID FROM MetricGenerator";
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            
            
            // check if anything got returned
            if (!rs.next())
            {
                log.debug("There are no metric generators in the EDM.");
                throw new NoDataException("There are no metric generators in the EDM.");
            }
            
            // process each result item
            do {
                String uuidStr = rs.getString("mGenUUID");
                
                if (uuidStr == null)
                {
                    log.error("Skipping a MetricGenerator, which does not have a UUID in the DB");
                    continue;
                }
                
                metricGenerators.add(getMetricGenerator(UUID.fromString(uuidStr), withSubClasses, connection, false));
            } while (rs.next());
            
        } catch (NoDataException nde) {
            throw nde;
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            log.debug("closing DB connection");
            connection.close();
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
    public static Set<MetricGenerator> getMetricGeneratorsForExperiment(UUID expUUID, boolean withSubClasses, Connection connection, boolean closeDBcon) throws Exception
    {
        if (expUUID == null)
        {
            log.error("Cannot get any MetricGenerator objects for an Experiment with the given UUID because it is NULL!");
            throw new IllegalArgumentException("Cannot get any MetricGenerator objects for an Experiment with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the MetricGenerator objects for the Experiment because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the MetricGenerator for the Experiment because the connection to the DB is closed");
        }
        
        Set<MetricGenerator> metricGenerators = new HashSet<MetricGenerator>();
        try {
            String query = "SELECT mGenUUID FROM MetricGenerator WHERE expUUID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, expUUID, java.sql.Types.OTHER);
            ResultSet rs = pstmt.executeQuery();
            
            // check if anything got returned
            if (!rs.next())
            {
                if (!ExperimentDAOHelper.objectExists(expUUID, connection, false))
                {
                    log.debug("There is no experiment with UUID " + expUUID.toString());
                    throw new NoDataException("There is no experiment with UUID " + expUUID.toString());
                }
                log.debug("There are no metric generators in the EDM.");
                throw new NoDataException("There are no metric generators in the EDM.");
            }
            
            // process each result item
            do {
                String uuidStr = rs.getString("mGenUUID");
                
                if (uuidStr == null)
                {
                    log.error("Skipping a MetricGenerator, which does not have a UUID in the DB");
                    continue;
                }
                
                metricGenerators.add(getMetricGenerator(UUID.fromString(uuidStr), withSubClasses, connection, false));
            } while (rs.next());
            
        } catch (NoDataException nde) {
            throw nde;
        }  catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
        
        return metricGenerators;
    }
}
