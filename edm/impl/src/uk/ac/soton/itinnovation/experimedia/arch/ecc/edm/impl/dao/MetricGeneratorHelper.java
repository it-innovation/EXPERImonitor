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
    
    public static ValidationReturnObject isObjectValidForSave(MetricGenerator mg, UUID expUUID, DatabaseConnector dbCon) throws Exception
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
        
        // check if the experiment exists in the DB
        try {
            if (!ExperimentHelper.objectExists(expUUID, dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Experiment specified for the MetricGenerator does not exist! UUID not found: " + expUUID.toString()));
            }
        } catch (Exception ex) {
            throw ex;
        }
        
        // check the entities
        if ((mg.getEntities() == null) || mg.getEntities().isEmpty())
        {
            return new ValidationReturnObject(false, new NullPointerException("The MetricGenerator does not have any entities defined"));
        }
        else
        {
            for (UUID entityUUID : mg.getEntities())
            {
                if (entityUUID == null)
                {
                    if (mg.getEntities().size() > 0)
                        return new ValidationReturnObject(false, new NullPointerException("One or more MetricGenerator Entity UUIDs are NULL"));
                    else
                        return new ValidationReturnObject(false, new NullPointerException("The MetricGenerator's Entity UUID is NULL"));
                }
                else if (!EntityHelper.objectExists(entityUUID, dbCon))
                {
                    return new ValidationReturnObject(false, new RuntimeException("An Entity the Metric Generator points to does not exist in the DB (entity UUID: " + entityUUID.toString() + ")"));
                }
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
                else if (!MetricGroupHelper.objectExists(mGrp.getUUID(), dbCon))
                {
                    return new ValidationReturnObject(false, new RuntimeException("A MetricGroup the Metric Generator points to does not exist in the DB (UUID: " + mGrp.toString() + ")"));
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
}
