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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
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
        /* commented out due to time saving - the DB may throw errors later
        try {
            if (objectExists(measurement.getUUID(), dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Measurement already exists; the UUID is not unique"));
            }
        } catch (Exception ex) {
            throw ex;
        }*/
        
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
    
    public static ValidationReturnObject areObjectsValidForSave(Set<Measurement> measurements, UUID mSetUUID, DatabaseConnector dbCon, boolean checkForMeasurementSet) throws Exception
    {
        if (measurements == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Measurement object is NULL - cannot save that..."));
        }
        
        if (mSetUUID == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The MeasurementSET UUID given is NULL"));
        }
        
        // check if all the required information is given; uuid, mSetUUID, timeStamp, value
        for (Measurement measurement : measurements)
        {
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
            /* commented out to save time; DB may throw errors later
            try {
                if (objectExists(measurements.getUUID(), dbCon))
                {
                    return new ValidationReturnObject(false, new RuntimeException("The Measurement already exists; the UUID is not unique"));
                }
            } catch (Exception ex) {
                throw ex;
            }
            */
        }
        
        // if checking for measurement set, if flag is set
        if (checkForMeasurementSet)
        {
            if (!MeasurementSetHelper.objectExists(mSetUUID, dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Measurements's MeasurementSet does not exist (UUID: " + mSetUUID.toString() + ")"));
            }
        }
        
        return new ValidationReturnObject(true);
    }
    
    public static String getSqlInsertQuery(Measurement measurement)
    {
        String query = "INSERT INTO Measurement (measurementUUID, mSetUUID, timeStamp, value) VALUES ("
                    + "'" + measurement.getUUID().toString() + "', "
                    + "'" + measurement.getMeasurementSetUUID().toString() + "', "
                    + String.valueOf(measurement.getTimeStamp().getTime()) + ", "
                    + "'" + measurement.getValue() + "')";
        
        return query;
    }
    
    public static boolean objectExists(UUID uuid, DatabaseConnector dbCon) throws Exception
    {
        return DBUtil.objectExistsByUUID("Measurement", "measurementUUID", uuid, dbCon);
    }
    
    public static void saveMeasurement(Measurement measurement, DatabaseConnector dbCon, boolean closeDBcon) throws Exception
    {
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        ValidationReturnObject returnObj = MeasurementHelper.isObjectValidForSave(measurement, dbCon, true);
        if (!returnObj.valid)
        {
            log.error("Cannot save the Measurement object: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = getSqlInsertQuery(measurement);
            ResultSet rs = dbCon.executeQuery(query, Statement.RETURN_GENERATED_KEYS);
            
            // check if the result set got the generated table key
            if (rs.next()) {
                String key = rs.getString(1);
                log.debug("Saved Measurement with key: " + key);
            } else {
                throw new RuntimeException("No index returned after saving Measurement");
            }
        } catch (Exception ex) {
            log.error("Error while saving Measurement: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving Measurement: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
    }
    
    public static void saveMeasurementsForSet(Set<Measurement> measurements, UUID mSetUUID, DatabaseConnector dbCon, boolean closeDBcon) throws Exception
    {
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        ValidationReturnObject returnObj = MeasurementHelper.areObjectsValidForSave(measurements, mSetUUID, dbCon, true);
        if (!returnObj.valid)
        {
            log.error("Cannot save the Measurement object: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            // iterate over each measurement and save
            for (Measurement measurement : measurements)
            {
                String query = getSqlInsertQuery(measurement);
                ResultSet rs = dbCon.executeQuery(query, Statement.RETURN_GENERATED_KEYS);

                // check if the result set got the generated table key
                if (rs.next()) {
                    String key = rs.getString(1);
                    log.debug("Saved Measurement with key: " + key);
                } else {
                    throw new RuntimeException("No index returned after saving Measurement");
                }
            }
        } catch (Exception ex) {
            log.error("Error while saving Measurement: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving Measurement: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
    }
    
    public static Measurement getMeasurement(UUID measurementUUID, DatabaseConnector dbCon, boolean closeDBcon) throws Exception
    {
        if (measurementUUID == null)
        {
            log.error("Cannot get a Measurement object with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot get a Measurement object with the given UUID because it is NULL!");
        }
        
        /*if (!MeasurementHelper.objectExists(measurementUUID, dbCon))
        {
            log.error("There is no Measurement with the given UUID: " + measurementUUID.toString());
            throw new RuntimeException("There is no Measurement with the given UUID: " + measurementUUID.toString());
        }*/
        
        Measurement measurement = null;
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = "SELECT * FROM Measurement WHERE measurementUUID = '" + measurementUUID + "'";
            ResultSet rs = dbCon.executeQuery(query);
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                String mSetUUIDstr = rs.getString("mSetUUID");
				String timeStampStr = rs.getString("timeStamp");
                String value = rs.getString("value");
                
                Date timeStamp = null;
                UUID mSetUUID = null;
                
                if (timeStampStr == null)
                    throw new RuntimeException("Could not get the time stamp from the Measurement instance");
                
                if (mSetUUIDstr == null)
                    throw new RuntimeException("Could not get the measurement set UUID from the Measurement instance");
                
                if (value == null)
                    throw new RuntimeException("Could not get the value from the Measurement instance");
                
                try {
                    timeStamp = new Date(Long.parseLong(timeStampStr));
                } catch (Exception ex) {
                    throw new RuntimeException("Unable to retrieve Measurement instance, because the timestamp in the DB could not be converted!", ex);
                }
                
                try {
                    mSetUUID = UUID.fromString(mSetUUIDstr);
                } catch (Exception ex) {
                    throw new RuntimeException("Unable to retrieve Measurement instance, because the timestamp in the DB could not be converted!", ex);
                }
                
                measurement = new Measurement(measurementUUID, mSetUUID, timeStamp, value);
            }
            else // nothing in the result set
            {
                log.error("There is no Measurement with the given UUID: " + measurementUUID.toString());
                throw new RuntimeException("There is no Measurement with the given UUID: " + measurementUUID.toString());
            }
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
        
        return measurement;
    }

    public static Set<Measurement> getMeasurementsForTimePeriod(UUID mSetUUID, Date fromDate, Date toDate, DatabaseConnector dbCon, boolean closeDBcon) throws Exception
    {
        if (mSetUUID == null)
        {
            log.error("Cannot get Measurement objects for the MeasurementSet with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot get Measurement objects for the MeasurementSet with the given UUID because it is NULL!");
        }
        
        /*if (!MeasurementSetHelper.objectExists(mSetUUID, dbCon))
        {
            log.error("There is no MeasurementSet with the given UUID: " + mSetUUID.toString());
            throw new RuntimeException("There is no MeasurementSet with the given UUID: " + mSetUUID.toString());
        }*/
        
        Set<Measurement> measurements = new HashSet<Measurement>();
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = "SELECT * FROM Measurement WHERE mSetUUID = '" + mSetUUID + "' AND "
                    + "timeStamp BETWEEN " + fromDate.getTime() + " AND " + toDate.getTime();
            ResultSet rs = dbCon.executeQuery(query);
            
            // check if anything got returned (connection closed in finalise method)
            while (rs.next())
            {
                String measurementUUIDstr = rs.getString("measurementUUID");
				String timeStampStr = rs.getString("timeStamp");
                String value = rs.getString("value");
                
                try {
                    Date timeStamp = new Date(Long.parseLong(timeStampStr));
                    UUID measurementUUID = UUID.fromString(measurementUUIDstr);
                    measurements.add(new Measurement(measurementUUID, mSetUUID, timeStamp, value));
                } catch (Exception ex) {
                    log.error("Unable to process a measurement, but continuing: " + ex.getMessage(), ex);
                }
            }
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
        
        return measurements;
    }
}
