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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;

/**
 * A helper class for validating and executing queries for the Entities.
 * 
 * @author Vegard Engen
 */
public class EntityHelper
{
    static Logger log = Logger.getLogger(EntityHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(Entity entity, Connection connection, boolean closeDBcon) throws Exception
    {
        if (entity == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Entity object is NULL - cannot save that..."));
        }
        
        // check if all the required information is given; uuid, name
        
        if (entity.getUUID() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Entity UUID is NULL"));
        }
        
        if (entity.getName() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Entity name is NULL"));
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
                        return new ValidationReturnObject(false, new NullPointerException("One or more the Entity's attributes are NULL"));
                    else
                        return new ValidationReturnObject(false, new NullPointerException("The Entity's attribute is NULL"));
                }
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
     * @param entity
     * @param valueNames
     * @param values 
     */
    public static void getTableNamesAndValues(Entity entity, List<String> valueNames, List<String> values)
    {
        if (entity == null)
            return;
        
        if ((valueNames == null) || (values == null))
            return;
        
        valueNames.add("entityUUID");
        values.add("'" + entity.getUUID().toString() + "'");
        
        valueNames.add("name");
        values.add("'" + entity.getName() + "'");
        
        if (entity.getDescription() != null)
        {
            valueNames.add("description");
            values.add("'" + entity.getDescription() + "'");
        }
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
        ValidationReturnObject returnObj = EntityHelper.isObjectValidForSave(entity, connection, false);
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
            
            // get the table names and values according to what's available in the object
            List<String> valueNames = new ArrayList<String>();
            List<String> values = new ArrayList<String>();
            EntityHelper.getTableNamesAndValues(entity, valueNames, values);
            
            String query = DBUtil.getInsertIntoQuery("Entity", valueNames, values);
            ResultSet rs = DBUtil.executeQuery(connection, query, Statement.RETURN_GENERATED_KEYS);
            
            // check if the result set got the generated table key
            if (rs.next()) {
                String key = rs.getString(1);
                log.debug("Saved entity " + entity.getName() + " with key: " + key);
            } else {
                exception = true;
                throw new RuntimeException("No index returned after saving entity " + entity.getName());
            }//end of debugging
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
                        AttributeHelper.saveAttribute(attrib, connection, false); // flag not to close the DB connection
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
            throw new NullPointerException("Cannot get an Entity object with the given UUID because it is NULL!");
        }
        
        /*if (!EntityHelper.objectExists(entityUUID, dbCon))
        {
            log.error("There is no entity with the given UUID: " + entityUUID.toString());
            throw new RuntimeException("There is no entity with the given UUID: " + entityUUID.toString());
        }*/
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Entity because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Entity because the connection to the DB is closed");
        }
        
        Entity entity = null;
        boolean exception = false;
        try {
            
            String query = "SELECT * FROM Entity WHERE entityUUID = '" + entityUUID + "'";
            ResultSet rs = DBUtil.executeQuery(connection, query);
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                String name = rs.getString("name");
				String description = rs.getString("description");
                
                entity = new Entity(entityUUID, name, description);
            }
            else // nothing in the result set
            {
                log.error("There is no entity with the given UUID: " + entityUUID.toString());
                throw new RuntimeException("There is no entity with the given UUID: " + entityUUID.toString());
            }
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
                attributes = AttributeHelper.getAttributesForEntity(entityUUID, connection, false); // don't close the connection
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
            ResultSet rs = DBUtil.executeQuery(connection, query);
            
            // check if anything got returned (connection closed in finalise method)
            while (rs.next())
            {
                String uuidStr = rs.getString("entityUUID");
                
                if (uuidStr == null)
                {
                    log.error("Skipping an entity, which does not have a UUID in the DB");
                    continue;
                }
                
                entities.add(getEntity(UUID.fromString(uuidStr), withAttributes, connection, false));
            }

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
            throw new NullPointerException("Cannot get Entity objects for the given experiment, because the UUID provided is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Entity objects for the experiment because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Entity objects for the experiment because the connection to the DB is closed");
        }
        
        Set<Entity> entities = new HashSet<Entity>();
        
        try {
            String query = "SELECT entityUUID FROM MetricGenerator_Entity "
                    + "WHERE mGenUUID = (SELECT mGenUUID FROM MetricGenerator WHERE expUUID = '" + expUUID.toString() + "')";
            ResultSet rs = DBUtil.executeQuery(connection, query);
            
            // check if anything got returned (connection closed in finalise method)
            while (rs.next())
            {
                String uuidStr = rs.getString("entityUUID");
                
                if (uuidStr == null)
                {
                    log.debug("Skipping an entity, which does not have a UUID in the DB");
                    continue;
                }
                
                entities.add(getEntity(UUID.fromString(uuidStr), withAttributes, connection, false));
            }

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
            throw new NullPointerException("Cannot get Entity objects for the given metric generator, because the UUID provided is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Entity objects for the metric generator because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Entity objects for the metric generator because the connection to the DB is closed");
        }
        
        Set<Entity> entities = new HashSet<Entity>();
        
        try {
            String query = "SELECT entityUUID FROM MetricGenerator_Entity WHERE mGenUUID = '" + mGenUUID + "'";
            ResultSet rs = DBUtil.executeQuery(connection, query);
            
            // check if anything got returned (connection closed in finalise method)
            while (rs.next())
            {
                String entityUUIDstr = rs.getString("entityUUID");
                if (entityUUIDstr == null)
                {
                    log.error("Unable to get Entity UUID from the DB for the MetricGenerator");
                    throw new RuntimeException("Unable to get Entity UUID from the DB for the MetricGenerator");
                }
                
                entities.add(getEntity(UUID.fromString(entityUUIDstr), withAttributes, connection, false));
            }
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
            DBUtil.executeQuery(connection, query);
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
            throw new NullPointerException("Cannot delete entity object with the given UUID because it is NULL!");
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
            String query = "DELETE from Entity where entityUUID = '" + entityUUID + "'";
            DBUtil.executeQuery(connection, query);
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