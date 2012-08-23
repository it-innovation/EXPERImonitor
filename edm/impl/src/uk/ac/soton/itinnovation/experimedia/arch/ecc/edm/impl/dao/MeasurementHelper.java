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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DatabaseConnector;

/**
 *
 * @author Vegard Engen
 */
public class MeasurementHelper
{
    static Logger log = Logger.getLogger(AttributeHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(Measurement measurement, DatabaseConnector dbCon, boolean checkForMeasurementSet) throws Exception
    {
        if (measurement == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Measurement object is NULL - cannot save that..."));
        }
        
        // check if all the required information is given; uuid, mSetUUID, timeStamp, value
        
        if (measurement.getUUID() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Measurement UUID is NULL"));
        }
        
        if (measurement.getMeasurementSetUUID() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The MeasurementSet UUID is NULL"));
        }
        
        if (measurement.getTimeStamp() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The MeasurementSet timestamp is NULL"));
        }
        
        if (measurement.getValue() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The MeasurementSet value is NULL"));
        }
        
        // check if it exists in the DB already
        try {
            if (objectExists(measurement.getUUID(), dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Measurement already exists; the UUID is not unique"));
            }
        } catch (Exception ex) {
            throw ex;
        }
        
        // if checking for measurement set, if flag is set
        if (checkForMeasurementSet)
        {
            if (!MeasurementSetHelper.objectExists(measurement.getMeasurementSetUUID(), dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Measurements's MeasurementSet does not exist (UUID: " + measurement.getMeasurementSetUUID().toString() + ")"));
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
     * @param measurement
     * @param valueNames
     * @param values 
     */
    public static void getTableNamesAndValues(Measurement measurement, List<String> valueNames, List<String> values)
    {
        if (measurement == null)
            return;
        
        if ((valueNames == null) || (values == null))
            return;
        
        valueNames.add("measurementUUID");
        values.add("'" + measurement.getUUID().toString() + "'");
        
        valueNames.add("mSetUUID");
        values.add("'" + measurement.getMeasurementSetUUID().toString() + "'");
        
        valueNames.add("timeStamp");
        values.add(String.valueOf(measurement.getTimeStamp().getTime()));
        
        valueNames.add("value");
        values.add("'" + measurement.getValue() + "'");
    }
    
    public static boolean objectExists(UUID uuid, DatabaseConnector dbCon) throws Exception
    {
        return DBUtil.objectExistsByUUID("Measurement", "measurementUUID", uuid, dbCon);
    }
}
