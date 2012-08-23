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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DatabaseConnector;

/**
 *
 * @author Vegard Engen
 */
public class MetricGroupHelper
{
    static Logger log = Logger.getLogger(MetricGroupHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(MetricGroup mGroup, DatabaseConnector dbCon, boolean checkForMetricGenerator) throws Exception
    {
        if (mGroup == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The MetricGroup object is NULL - cannot save that..."));
        }
        
        // check if all the required information is given; uuid, metric generator UUID, name, measurement sets (if any are given)
        
        if (mGroup.getUUID() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The MetricGroup UUID is NULL"));
        }
        
        if (mGroup.getMetricGeneratorUUID() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The MetricGroup's metric generator UUID is NULL"));
        }
        
        if (mGroup.getName() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The MetricGrou name is NULL"));
        }
        
        // check if it exists in the DB already
        try {
            if (objectExists(mGroup.getUUID(), dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The MetricGroup already exists; the UUID is not unique"));
            }
        } catch (Exception ex) {
            throw ex;
        }
        
        // check if the metric generator exists, if it should be checked...
        if (checkForMetricGenerator)
        {
            if (!MetricGeneratorHelper.objectExists(mGroup.getUUID(), dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The MetricGenerator for the MetricGroup doesn't exit"));
            }
        }
        
        // if any measurements, validate them too!
        if ((mGroup.getMeasurementSets() != null) && !mGroup.getMeasurementSets().isEmpty())
        {
            for (MeasurementSet mSet : mGroup.getMeasurementSets())
            {
                ValidationReturnObject validationReturn = MeasurementSetHelper.isObjectValidForSave(mSet, dbCon, false); // false = don't check for measurement set existing as this won't be saved yet!
                if (!validationReturn.valid)
                {
                    return validationReturn;
                }
                else if (!mSet.getMetricGroupUUID().equals(mGroup.getUUID()))
                {
                    return new ValidationReturnObject(false, new RuntimeException("The MetricGroup UUID of a MeasurementSet is not equal to the MetricGroup that it's supposed to be saved with (measurement set UUID " + mSet.getUUID().toString() + ")"));
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
     * @param mGrp
     * @param valueNames
     * @param values 
     */
    public static void getTableNamesAndValues(MetricGroup mGrp, List<String> valueNames, List<String> values)
    {
        if (mGrp == null)
            return;
        
        if ((valueNames == null) || (values == null))
            return;

        valueNames.add("mGrpUUID");
        values.add("'" + mGrp.getUUID().toString() + "'");
        
        valueNames.add("mGenUUID");
        values.add("'" + mGrp.getMetricGeneratorUUID().toString() + "'");
        
        valueNames.add("name");
        values.add("'" + mGrp.getName().toString() + "'");
        
        if (mGrp.getDescription() != null)
        {
            valueNames.add("description");
            values.add("'" + mGrp.getDescription() + "'");
        }
    }
    
    public static boolean objectExists(UUID uuid, DatabaseConnector dbCon) throws Exception
    {
        return DBUtil.objectExistsByUUID("MetricGroup", "mGrpUUID", uuid, dbCon);
    }
}
