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
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.NoDataException;

/**
 * A helper class for validating and executing queries for the Entities.
 * 
 * @author Vegard Engen
 */
public class EntityDAOHelper
{
    static Logger log = Logger.getLogger(EntityDAOHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(Entity entity, Connection connection, boolean closeDBcon) throws Exception
    {
        if (entity == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Entity object is NULL - cannot save that..."));
        }
        
        // check if all the required information is given; uuid, name
        
        if (entity.getUUID() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Entity UUID is NULL"));
        }
        
        if (entity.getName() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Entity name is NULL"));
        }
        /*
        // check if it exists in the DB already
        try {
            if (objectExists(entity.getUUID(), dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Entity already exists; the UUID is not unique"));
            }
        } catch (Exception ex) {
            throw ex;
        }
        */
        // validate attributes if there are any
        if ((entity.getAttributes() != null) && !entity.getAttributes().isEmpty())
        {
            for (Attribute attrib : entity.getAttributes())
            {
                /*ValidationReturnObject validationReturn = AttributeHelper.isObjectValidForSave(attrib, dbCon, false); // false = don't check for entity existing as this won't be saved yet!
                if (!validationReturn.valid)
                {
                    return validationReturn;
                }
                else if (!attrib.getEntityUUID().equals(entity.getUUID()))
                {
                    return new ValidationReturnObject(false, new RuntimeException("The Entity UUID of an Attribute is not equal to the Entity that it's supposed to be saved with (attribute UUID " + attrib.getUUID().toString() + ")"));
                }*/
                
                if (attrib == null)
                {
                    if (entity.getAttributes().size() > 0)
                        return new ValidationReturnObject(false, new IllegalArgumentException("One or more the Entity's attributes are NULL"));
                    else
                        return new ValidationReturnObject(false, new IllegalArgumentException("The Entity's attribute is NULL"));
                }
            }
        }
        
        return new ValidationReturnObject(true);
    }
    
    public static boolean objectExists(UUID uuid, Connection connection, boolean closeDBcon) throws Exception
    {
        return DBUtil.objectExistsByUUID("Entity", "entityUUID", uuid, connection, closeDBcon);
    }
    
    public static void saveEntity(Entity entity, Connection connection, boolean closeDBcon) throws Exception
    {
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        // will validate the attributes too, if any are given
        ValidationReturnObject returnObj = EntityDAOHelper.isObjectValidForSave(entity, connection, false);
        if (!returnObj.valid)
        {
            log.error("Cannot save the Entity object: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot save the Entity because the connection to the DB is closed");
            throw new RuntimeException("Cannot save the Entity because the connection to the DB is closed");
        }
        
        boolean exception = false;
        try {
            if (closeDBcon)
            {
                log.debug("Starting transaction");
                connection.setAutoCommit(false);
            }
            
            String query = "INSERT INTO Entity (entityUUID, entityID, name, description) VALUES (?, ?, ?, ?)";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, entity.getUUID(), java.sql.Types.OTHER);
            pstmt.setString(2, entity.getEntityID());
            pstmt.setString(3, entity.getName());
            pstmt.setString(4, entity.getDescription());
            
            pstmt.executeUpdate();
        } catch (Exception ex) {
            exception = true;
            log.error("Error while saving entity: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving entity: " + ex.getMessage(), ex);
        } finally {
            if (exception && closeDBcon)
            {
                log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                connection.rollback();
                connection.close();
            }
        }
        
        try {
            // save any attributes if not NULL
            if ((entity.getAttributes() != null) && !entity.getAttributes().isEmpty())
            {
                log.debug("Saving " + entity.getAttributes().size() + " attribute(s) for the entity");
                for (Attribute attrib : entity.getAttributes())
                {
                    if (attrib != null)
                        AttributeDAOHelper.saveAttribute(attrib, connection, false); // flag not to close the DB connection
                }
            }
        } catch (Exception ex) {
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
    
    public static Entity getEntity(UUID entityUUID, boolean withAttributes, Connection connection, boolean closeDBcon) throws Exception
    {
        if (entityUUID == null)
        {
            log.error("Cannot get an Entity object with the given UUID because it is NULL!");
            throw new IllegalArgumentException("Cannot get an Entity object with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Entity because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Entity because the connection to the DB is closed");
        }
        
        Entity entity = null;
        boolean exception = false;
        try {
            
            String query = "SELECT * FROM Entity WHERE entityUUID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, entityUUID, java.sql.Types.OTHER);
            ResultSet rs = pstmt.executeQuery();
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                String entityID = rs.getString("entityID");
                String name = rs.getString("name");
				String description = rs.getString("description");
                
                entity = new Entity(entityUUID, entityID, name, description);
            }
            else // nothing in the result set
            {
                log.error("There is no entity with the given UUID: " + entityUUID.toString());
                throw new NoDataException("There is no entity with the given UUID: " + entityUUID.toString());
            }
            
        } catch (NoDataException nde) {
            throw nde;
        } catch (Exception ex) {
            exception = true;
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (exception || (closeDBcon && !withAttributes))
                connection.close();
        }
        
        // check if there's any metric generators
        if (withAttributes)
        {
            Set<Attribute> attributes = null;

            try {
                attributes = AttributeDAOHelper.getAttributesForEntity(entityUUID, connection, false); // don't close the connection
            } catch (NoDataException nde) {
                log.debug("There were no attributes for the entity");
            } catch (Exception ex) {
                log.error("Caught an exception when getting attributes for entity (UUID: " + entityUUID.toString() + "): " + ex.getMessage());
                throw new RuntimeException("Unable to get Entity object due to an issue with getting its attributes: " + ex.getMessage(), ex);
            } finally {
                if (closeDBcon)
                    connection.close();
            }

            entity.setAttributes(attributes);
        }
        
        return entity;
    }

    public static Set<Entity> getEntities(boolean withAttributes, Connection connection) throws Exception
    {
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Entity objects because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Entity objects because the connection to the DB is closed");
        }
        
        Set<Entity> entities = new HashSet<Entity>();
        try {
            String query = "SELECT entityUUID FROM Entity";
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            
            // check if anything got returned
            if (!rs.next())
            {
                log.debug("There are no entities stored in the EDM.");
                throw new NoDataException("There are no entities stored in the EDM.");
            }
            
            // process each result item
            do {
                String uuidStr = rs.getString("entityUUID");
                
                if (uuidStr == null)
                {
                    log.error("Skipping an entity, which does not have a UUID in the DB");
                    continue;
                }
                
                entities.add(getEntity(UUID.fromString(uuidStr), withAttributes, connection, false));
            } while (rs.next());
            
        } catch (NoDataException nde) {
            throw nde;
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            connection.close();
        }
        
        return entities;
    }
    
    public static Set<Entity> getEntitiesForExperiment(UUID expUUID, boolean withAttributes, Connection connection, boolean closeDBcon) throws Exception
    {
        if (expUUID == null)
        {
            log.error("Cannot get Entity objects for the given experiment, because the UUID provided is NULL!");
            throw new IllegalArgumentException("Cannot get Entity objects for the given experiment, because the UUID provided is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Entity objects for the experiment because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Entity objects for the experiment because the connection to the DB is closed");
        }
        
        Set<Entity> entities = new HashSet<Entity>();
        
        try {
            String query = "SELECT DISTINCT entityUUID FROM MetricGenerator_Entity "
                    + "WHERE mGenUUID IN (SELECT mGenUUID FROM MetricGenerator WHERE expUUID = ?)";
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
                log.debug("There are no entities for the given experiment (UUID = " + expUUID.toString() + ").");
                throw new NoDataException("There are no entities for the given experiment (UUID = " + expUUID.toString() + ").");
            }
            
            // process each result item
            do {
                String uuidStr = rs.getString("entityUUID");
                
                if (uuidStr == null)
                {
                    log.debug("Skipping an entity, which does not have a UUID in the DB");
                    continue;
                }
                
                entities.add(getEntity(UUID.fromString(uuidStr), withAttributes, connection, false));
            } while (rs.next());
            
        } catch (NoDataException nde) {
            throw nde;
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            connection.close();
        }
        
        return entities;
    }
    
    public static Set<Entity> getEntitiesForMetricGenerator(UUID mGenUUID, boolean withAttributes, Connection connection, boolean closeDBcon) throws Exception
    {
        if (mGenUUID == null)
        {
            log.error("Cannot get Entity objects for the given metric generator, because the UUID provided is NULL!");
            throw new IllegalArgumentException("Cannot get Entity objects for the given metric generator, because the UUID provided is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Entity objects for the metric generator because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Entity objects for the metric generator because the connection to the DB is closed");
        }
        
        Set<Entity> entities = new HashSet<Entity>();
        
        try {
            String query = "SELECT entityUUID FROM MetricGenerator_Entity WHERE mGenUUID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, mGenUUID, java.sql.Types.OTHER);
            ResultSet rs = pstmt.executeQuery();
            
            // check if anything got returned
            if (!rs.next())
            {
                if (!MetricGeneratorDAOHelper.objectExists(mGenUUID, connection, false))
                {
                    log.debug("There is no metric generator with UUID " + mGenUUID.toString());
                    throw new NoDataException("There is no metric generator with UUID " + mGenUUID.toString());
                }
                
                log.debug("There are no entities for the given metric generator (UUID = " + mGenUUID.toString() + ").");
                throw new NoDataException("There are no entities for the given metric generator (UUID = " + mGenUUID.toString() + ").");
            }
            
            // process each result item
            do {
                String entityUUIDstr = rs.getString("entityUUID");
                if (entityUUIDstr == null)
                {
                    log.error("Unable to get Entity UUID from the DB for the MetricGenerator");
                    throw new RuntimeException("Unable to get Entity UUID from the DB for the MetricGenerator");
                }
                
                entities.add(getEntity(UUID.fromString(entityUUIDstr), withAttributes, connection, false));
            } while (rs.next());
            
        } catch (NoDataException nde) {
            throw nde;
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
        
        return entities;
    }
    
    public static void deleteAllEntities(Connection connection, boolean closeDBcon) throws Exception
    {
        log.debug("Deleting all entities");
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot delete the entities because the connection to the DB is closed");
            throw new RuntimeException("Cannot delete the entities because the connection to the DB is closed");
        }
        
        if (closeDBcon)
        {
            log.debug("Starting transaction");
            connection.setAutoCommit(false);
        }
        
        boolean exception = false;
        
        try {
            String query = "DELETE from Entity";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.executeUpdate();
        } catch (Exception ex) {
            exception = true;
            log.error("Unable to delete entities: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to delete entities: " + ex.getMessage(), ex);
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
            }
        }
    }
    
    public static void deleteEntity(UUID entityUUID, Connection connection, boolean closeDBcon) throws Exception
    {
        log.debug("Deleting entity");
        
        if (entityUUID == null)
        {
            log.error("Cannot delete entity object with the given UUID because it is NULL!");
            throw new IllegalArgumentException("Cannot delete entity object with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot delete the entity because the connection to the DB is closed");
            throw new RuntimeException("Cannot delete the entity because the connection to the DB is closed");
        }
        
        if (closeDBcon)
        {
            log.debug("Starting transaction");
            connection.setAutoCommit(false);
        }
        
        boolean exception = false;
        
        try {
            String query = "DELETE from Entity where entityUUID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, entityUUID, java.sql.Types.OTHER);
            pstmt.executeUpdate();
        } catch (Exception ex) {
            exception = true;
            log.error("Unable to delete entity: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to delete entity: " + ex.getMessage(), ex);
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
            }
        }
    }
}
