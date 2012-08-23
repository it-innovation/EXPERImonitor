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

import java.util.List;
import java.util.UUID;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DatabaseConnector;

/**
 *
 * @author Vegard Engen
 */
public class EntityHelper
{
    static Logger log = Logger.getLogger(EntityHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(Entity entity, DatabaseConnector dbCon) throws Exception
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
        
        // check if it exists in the DB already
        try {
            if (objectExists(entity.getUUID(), dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Entity already exists; the UUID is not unique"));
            }
        } catch (Exception ex) {
            throw ex;
        }
        
        // validate attributes if there are any
        if ((entity.getAttributes() != null) && !entity.getAttributes().isEmpty())
        {
            for (Attribute attrib : entity.getAttributes())
            {
                ValidationReturnObject validationReturn = AttributeHelper.isObjectValidForSave(attrib, dbCon, false); // false = don't check for entity existing as this won't be saved yet!
                if (!validationReturn.valid)
                {
                    return validationReturn;
                }
                else if (!attrib.getEntityUUID().equals(entity.getUUID()))
                {
                    return new ValidationReturnObject(false, new RuntimeException("The Entity UUID of an Attribute is not equal to the Entity that it's supposed to be saved with (attribute UUID " + attrib.getUUID() + ")"));
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
    
    public static boolean objectExists(UUID uuid, DatabaseConnector dbCon) throws Exception
    {
        return DBUtil.objectExistsByUUID("Entity", "entityUUID", uuid, dbCon);
    }
}
