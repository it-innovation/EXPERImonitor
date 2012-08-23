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
        try {
            if (objectExists(attrib.getUUID(), dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Attribute already exists; the UUID is not unique"));
            }
        } catch (Exception ex) {
            throw ex;
        }
        
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
}
