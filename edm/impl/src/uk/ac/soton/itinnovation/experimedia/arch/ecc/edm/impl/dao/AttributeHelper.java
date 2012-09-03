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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DatabaseConnector;

/**
 *
 * @author Vegard Engen
 */
public class AttributeHelper
{
    static Logger log = Logger.getLogger(AttributeHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(Attribute attrib, DatabaseConnector dbCon, boolean checkForEntity) throws Exception
    {
        if (attrib == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Attribute object is NULL - cannot save that..."));
        }
        
        // check if all the required information is given; uuid, name
        
        if (attrib.getUUID() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Attribute UUID is NULL"));
        }
        
        if (attrib.getName() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Attribute name is NULL"));
        }
        
        // check if it exists in the DB already
        /*try {
            if (objectExists(attrib.getUUID(), dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Attribute already exists; the UUID is not unique"));
            }
        } catch (Exception ex) {
            throw ex;
        }*/
        
        if (attrib.getEntityUUID() == null)
        {
            return new ValidationReturnObject(false, new RuntimeException("The Attribute's Entity UUID is NULL"));
        }
        
        // if checking for entity; may be false if this is called from the saveEntity method
        if (checkForEntity)
        {
            if (!EntityHelper.objectExists(attrib.getEntityUUID(), dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Attribute's Entity does not exist (UUID: " + attrib.getEntityUUID().toString() + ")"));
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
     * @param attrib
     * @param valueNames
     * @param values 
     */
    public static void getTableNamesAndValues(Attribute attrib, List<String> valueNames, List<String> values)
    {
        if (attrib == null)
            return;
        
        if ((valueNames == null) || (values == null))
            return;
        
        valueNames.add("attribUUID");
        values.add("'" + attrib.getUUID().toString() + "'");
        
        valueNames.add("entityUUID");
        values.add("'" + attrib.getEntityUUID().toString() + "'");
        
        valueNames.add("name");
        values.add("'" + attrib.getName() + "'");
        
        if (attrib.getDescription() != null)
        {
            valueNames.add("description");
            values.add("'" + attrib.getDescription() + "'");
        }
    }
    
    public static boolean objectExists(UUID uuid, DatabaseConnector dbCon) throws Exception
    {
        return DBUtil.objectExistsByUUID("Attribute", "attribUUID", uuid, dbCon);
    }
    
    public static void saveAttribute(Attribute attrib, DatabaseConnector dbCon, boolean closeDBcon) throws Exception
    {
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        ValidationReturnObject returnObj = AttributeHelper.isObjectValidForSave(attrib, dbCon, true);
        if (!returnObj.valid)
        {
            log.error("Cannot save the Attribute object: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            // get the table names and values according to what's available in the
            // object
            List<String> valueNames = new ArrayList<String>();
            List<String> values = new ArrayList<String>();
            AttributeHelper.getTableNamesAndValues(attrib, valueNames, values);
            
            String query = DBUtil.getInsertIntoQuery("Attribute", valueNames, values);
            ResultSet rs = dbCon.executeQuery(query, Statement.RETURN_GENERATED_KEYS);
            
            // check if the result set got the generated table key
            if (rs.next()) {
                String key = rs.getString(1);
                log.debug("Saved attribute " + attrib.getName() + ", with key: " + key);
            } else {
                throw new RuntimeException("No index returned after saving attribute " + attrib.getName());
            }//end of debugging
        } catch (Exception ex) {
            log.error("Error while saving entity: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving attribute: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
    }
    
    public static Attribute getAttribute(UUID attribUUID, DatabaseConnector dbCon, boolean closeDBcon) throws Exception
    {
        if (attribUUID == null)
        {
            log.error("Cannot get an Attribute object with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot get an Attribute object with the given UUID because it is NULL!");
        }
        
        /*if (!AttributeHelper.objectExists(attribUUID, dbCon))
        {
            log.error("There is no attribute with the given UUID: " + attribUUID.toString());
            throw new RuntimeException("There is no attribute with the given UUID: " + attribUUID.toString());
        }*/
        
        Attribute attribute = null;
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = "SELECT * FROM Attribute WHERE attribUUID = '" + attribUUID + "'";
            ResultSet rs = dbCon.executeQuery(query);
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                String name = rs.getString("name");
				String description = rs.getString("description");
                String entityUUIDstr = rs.getString("entityUUID");
                
                if (entityUUIDstr == null)
                {
                    throw new RuntimeException("The attribute instance doesn't have an entity UUID");
                }
                
                attribute = new Attribute(attribUUID, UUID.fromString(entityUUIDstr), name, description);
            }
            else // nothing in the result set
            {
                log.error("There is no attribute with the given UUID: " + attribUUID.toString());
                throw new RuntimeException("There is no attribute with the given UUID: " + attribUUID.toString());
            }
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
        
        return attribute;
    }
    
    public static Set<Attribute> getAttributesForEntity(UUID entityUUID, DatabaseConnector dbCon, boolean closeDBcon) throws Exception
    {
        if (entityUUID == null)
        {
            log.error("Cannot get any Attribute objects for an Entity with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot get any Attribute objects for an Entity with the given UUID because it is NULL!");
        }
        
        /*if (!EntityHelper.objectExists(entityUUID, dbCon))
        {
            log.error("There is no entity with the given UUID: " + entityUUID.toString());
            throw new RuntimeException("There is no entity with the given UUID: " + entityUUID.toString());
        }*/
        
        Set<Attribute> attributes = new HashSet<Attribute>();;
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = "SELECT * FROM Attribute WHERE entityUUID = '" + entityUUID + "'";
            ResultSet rs = dbCon.executeQuery(query);
            
            // check if anything got returned (connection closed in finalise method)
            while (rs.next())
            {
                String attribUUIDstr = rs.getString("attribUUID");
                String entityUUIDstr = rs.getString("entityUUID");
                String name = rs.getString("name");
				String description = rs.getString("description");
                
                if (attribUUIDstr == null)
                {
                    throw new RuntimeException("The attribute instance doesn't have a UUID");
                }
                
                if (entityUUIDstr == null)
                {
                    throw new RuntimeException("The attribute instance doesn't have an entity UUID");
                }
                
                attributes.add(new Attribute(UUID.fromString(attribUUIDstr), UUID.fromString(entityUUIDstr), name, description));
            }
            
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
        
        return attributes;
    }
}
