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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DatabaseConnector;

/**
 *
 * @author Vegard Engen
 */
public class MeasurementSetHelper
{
    static Logger log = Logger.getLogger(MeasurementSetHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(MeasurementSet mSet, DatabaseConnector dbCon, boolean checkForMetricGroup) throws Exception
    {
        if (mSet == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The MeasurementSet object is NULL - cannot save that..."));
        }
        
        // check if all the required information is given; uuid, metric group UUID, attribute UUID, metric, measurements (if any)
        
        if (mSet.getUUID() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The MeasurementSet UUID is NULL"));
        }
        
        if (mSet.getMetricGroupUUID() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The MeasurementSet's measurement group UUID is NULL"));
        }
        
        if (mSet.getAttributeUUID() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The MeasurementSet's attribute UUID is NULL"));
        }
        
        if (mSet.getMetric() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The MeasurementSet's metric is NULL"));
        }
        else
        {
            // check if metric exists in the DB already
            try {
                if (MetricHelper.objectExists(mSet.getMetric().getUUID(), dbCon))
                {
                    return new ValidationReturnObject(false, new RuntimeException("The MeasurementSet's metric already exists; the UUID of the metric is not unique"));
                }
            } catch (Exception ex) {
                throw ex;
            }
        }
        
        // check if it exists in the DB already
        try {
            if (objectExists(mSet.getUUID(), dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The MeasurementSet already exists; the UUID is not unique"));
            }
        } catch (Exception ex) {
            throw ex;
        }
        
        // check that the metric group exists, if flagged to check
        if (checkForMetricGroup)
        {
            if (!MetricGroupHelper.objectExists(mSet.getMetricGroupUUID(), dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The MetricGroup for the MeasurementSet doesn't exit (UUID: " + mSet.getMetricGroupUUID().toString() + ")"));
            }
        }
        
        // check that the attribute exists
        if (!AttributeHelper.objectExists(mSet.getAttributeUUID(), dbCon))
        {
            return new ValidationReturnObject(false, new RuntimeException("The Attribute for the MeasurementSet doesn't exit (UUID: " + mSet.getAttributeUUID().toString() + ")"));
        }
        
        // if any measurements, validate them too!
        if ((mSet.getMeasurements() != null) && !mSet.getMeasurements().isEmpty())
        {
            for (Measurement measurement : mSet.getMeasurements())
            {
                ValidationReturnObject validationReturn = MeasurementHelper.isObjectValidForSave(measurement, dbCon, false); // false = don't check for measurement set existing as this won't be saved yet!
                if (!validationReturn.valid)
                {
                    return validationReturn;
                }
                else if (!measurement.getMeasurementSetUUID().equals(mSet.getUUID()))
                {
                    return new ValidationReturnObject(false, new RuntimeException("The MeasurementSet UUID of a Measurement is not equal to the MeasurementSet that it's supposed to be saved with (measurement UUID " + measurement.getUUID().toString() + ")"));
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
     * @param mSet
     * @param valueNames
     * @param values 
     */
    public static void getTableNamesAndValues(MeasurementSet mSet, List<String> valueNames, List<String> values)
    {
        if (mSet == null)
            return;
        
        if ((valueNames == null) || (values == null))
            return;

        valueNames.add("mSetUUID");
        values.add("'" + mSet.getUUID().toString() + "'");
        
        valueNames.add("mGrpUUID");
        values.add("'" + mSet.getMetricGroupUUID().toString() + "'");
        
        valueNames.add("attribUUID");
        values.add("'" + mSet.getAttributeUUID().toString() + "'");
        
        valueNames.add("metricUUID");
        values.add("'" + mSet.getMetric().getUUID().toString() + "'");
    }
    
    public static boolean objectExists(UUID uuid, DatabaseConnector dbCon) throws Exception
    {
        return DBUtil.objectExistsByUUID("MeasurementSet", "mSetUUID", uuid, dbCon);
    }
}
