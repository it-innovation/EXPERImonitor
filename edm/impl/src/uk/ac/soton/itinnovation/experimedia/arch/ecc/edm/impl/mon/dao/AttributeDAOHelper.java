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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.NoDataException;

/**
 * A helper class for validating and executing queries for the Attributes.
 * 
 * @author Vegard Engen
 */
public class AttributeDAOHelper
{
    static Logger log = Logger.getLogger(AttributeDAOHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(Attribute attrib, boolean checkForEntity, Connection connection, boolean closeDBcon) throws Exception
    {
        if (attrib == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Attribute object is NULL - cannot save that..."));
        }
        
        // check if all the required information is given; uuid, name
        
        if (attrib.getUUID() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Attribute UUID is NULL"));
        }
        
        if (attrib.getName() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Attribute name is NULL"));
        }
        
        // check if it exists in the DB already
        /*try {
            if (objectExists(attrib.getUUID(), connection))
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
            if (!EntityDAOHelper.objectExists(attrib.getEntityUUID(), connection, closeDBcon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Attribute's Entity does not exist (UUID: " + attrib.getEntityUUID().toString() + ")"));
            }
        }
        
        return new ValidationReturnObject(true);
    }
    
    public static boolean objectExists(UUID uuid, Connection connection, boolean closeDBcon) throws Exception
    {
        return DBUtil.objectExistsByUUID("Attribute", "attribUUID", uuid, connection, closeDBcon);
    }
    
    public static void saveAttribute(Attribute attrib, Connection connection, boolean closeDBcon) throws Exception
    {
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        ValidationReturnObject returnObj = AttributeDAOHelper.isObjectValidForSave(attrib, false, connection, false);
        if (!returnObj.valid)
        {
            log.error("Cannot save the Attribute object: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot save the Attribute because the connection to the DB is closed");
            throw new RuntimeException("Cannot save the Attribute because the connection to the DB is closed");
        }
        
        try {
            String query = "INSERT INTO Attribute (attribUUID, entityUUID, name, description) VALUES (?, ?, ?, ?)";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, attrib.getUUID(), java.sql.Types.OTHER);
            pstmt.setObject(2, attrib.getEntityUUID(), java.sql.Types.OTHER);
            pstmt.setString(3, attrib.getName());
            pstmt.setString(4, attrib.getDescription());
            
            pstmt.executeUpdate();
        } catch (Exception ex) {
            log.error("Error while saving entity: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving attribute: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
    }
    
    public static Attribute getAttribute(UUID attribUUID, Connection connection, boolean closeDBcon) throws Exception
    {
        if (attribUUID == null)
        {
            log.error("Cannot get an Attribute object with the given UUID because it is NULL!");
            throw new IllegalArgumentException("Cannot get an Attribute object with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Attribute because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Attribute because the connection to the DB is closed");
        }
        
        Attribute attribute = null;
        try {
            String query = "SELECT * FROM Attribute WHERE attribUUID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, attribUUID, java.sql.Types.OTHER);
            ResultSet rs = pstmt.executeQuery();
            
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
                throw new NoDataException("There is no attribute with the given UUID: " + attribUUID.toString());
            }
        } catch (NoDataException nde) {
            throw nde;
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
        
        return attribute;
    }
    
    public static Set<Attribute> getAttributesForEntity(UUID entityUUID, Connection connection, boolean closeDBcon) throws Exception
    {
        if (entityUUID == null)
        {
            log.error("Cannot get any Attribute objects for an Entity with the given UUID because it is NULL!");
            throw new IllegalArgumentException("Cannot get any Attribute objects for an Entity with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Attribute objects for the Entity because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Attribute objects for the Entity because the connection to the DB is closed");
        }
        
        Set<Attribute> attributes = new HashSet<Attribute>();
        try {
            String query = "SELECT * FROM Attribute WHERE entityUUID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, entityUUID, java.sql.Types.OTHER);
            ResultSet rs = pstmt.executeQuery();
            
            // check if anything got returned
            if (!rs.next())
            {
                if (!EntityDAOHelper.objectExists(entityUUID, connection, false))
                {
                    log.debug("There is no entity with the UUID = " + entityUUID.toString());
                    throw new NoDataException("There is no entity with the UUID = " + entityUUID.toString());
                }
                log.debug("There are no attributes for the given entity (UUID = " + entityUUID.toString() + ").");
                throw new NoDataException("There are no attributes for the given entity (UUID = " + entityUUID.toString() + ").");
            }
            
            // process each result item
            do {
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
            }while (rs.next());
            
        } catch (NoDataException nde) {
            throw nde;
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
        
        return attributes;
    }
}
