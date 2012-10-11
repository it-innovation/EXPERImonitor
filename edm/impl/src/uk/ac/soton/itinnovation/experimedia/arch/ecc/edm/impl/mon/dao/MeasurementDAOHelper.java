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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.NoDataException;

/**
 * A helper class for validating and executing queries for the Measurements.
 * 
 * @author Vegard Engen
 */
public class MeasurementDAOHelper
{
    static Logger log = Logger.getLogger(MeasurementDAOHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(Measurement measurement, boolean checkForMeasurementSet, Connection connection, boolean closeDBcon) throws Exception
    {
        if (measurement == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Measurement object is NULL - cannot save that..."));
        }
        
        // check if all the required information is given; uuid, mSetUUID, timeStamp, value
        
        if (measurement.getUUID() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Measurement UUID is NULL"));
        }
        
        if (measurement.getMeasurementSetUUID() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The MeasurementSet UUID is NULL"));
        }
        
        if (measurement.getTimeStamp() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Measurement timestamp is NULL"));
        }
        
        if (measurement.getValue() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Measurement value is NULL"));
        }
        
        // check if it exists in the DB already
        /* commented out due to time saving - the DB may throw errors later
        try {
            if (objectExists(measurement.getUUID(), connection))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Measurement already exists; the UUID is not unique"));
            }
        } catch (Exception ex) {
            throw ex;
        }*/
        
        // if checking for measurement set, if flag is set
        if (checkForMeasurementSet)
        {
            if (!MeasurementSetDAOHelper.objectExists(measurement.getMeasurementSetUUID(), connection, closeDBcon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Measurements's MeasurementSet does not exist (UUID: " + measurement.getMeasurementSetUUID().toString() + ")"));
            }
        }
        
        return new ValidationReturnObject(true);
    }
    
    public static ValidationReturnObject areObjectsValidForSave(Set<Measurement> measurements, UUID mSetUUID, boolean checkForMeasurementSet, Connection connection, boolean closeDBcon) throws Exception
    {
        if (measurements == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Measurement object is NULL - cannot save that..."));
        }
        
        if (mSetUUID == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The MeasurementSET UUID given is NULL"));
        }
        
        // check if all the required information is given; uuid, mSetUUID, timeStamp, value
        for (Measurement measurement : measurements)
        {
            if (measurement.getUUID() == null)
            {
                return new ValidationReturnObject(false, new IllegalArgumentException("The Measurement UUID is NULL"));
            }

            if (measurement.getMeasurementSetUUID() == null)
            {
                return new ValidationReturnObject(false, new IllegalArgumentException("The MeasurementSet UUID is NULL"));
            }

            if (measurement.getTimeStamp() == null)
            {
                return new ValidationReturnObject(false, new IllegalArgumentException("The Measurement timestamp is NULL"));
            }

            if (measurement.getValue() == null)
            {
                return new ValidationReturnObject(false, new IllegalArgumentException("The Measurement value is NULL"));
            }
            
            // check if it exists in the DB already
            /* commented out to save time; DB may throw errors later
            try {
                if (objectExists(measurements.getUUID(), connection))
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
            if (!MeasurementSetDAOHelper.objectExists(mSetUUID, connection, closeDBcon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Measurements's MeasurementSet does not exist (UUID: " + mSetUUID.toString() + ")"));
            }
        }
        
        return new ValidationReturnObject(true);
    }
    
    public static boolean objectExists(UUID uuid, Connection connection, boolean closeDBcon) throws Exception
    {
        return DBUtil.objectExistsByUUID("Measurement", "measurementUUID", uuid, connection, closeDBcon);
    }
    
    public static void saveMeasurement(Measurement measurement, Connection connection, boolean closeDBcon) throws Exception
    {
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        ValidationReturnObject returnObj = MeasurementDAOHelper.isObjectValidForSave(measurement, true, connection, false);
        if (!returnObj.valid)
        {
            log.error("Cannot save the Measurement object: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot save the Measurement because the connection to the DB is closed");
            throw new RuntimeException("Cannot save the Measurement because the connection to the DB is closed");
        }
        
        try {
            String query = "INSERT INTO Measurement (measurementUUID, mSetUUID, timeStamp, value, synchronised) VALUES (?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, measurement.getUUID(), java.sql.Types.OTHER);
            pstmt.setObject(2, measurement.getMeasurementSetUUID(), java.sql.Types.OTHER);
            pstmt.setLong(3, measurement.getTimeStamp().getTime());
            pstmt.setString(4, measurement.getValue());
            pstmt.setBoolean(5, false);
            
            pstmt.executeUpdate();
        } catch (Exception ex) {
            log.error("Error while saving Measurement: " + ex.getMessage());
            throw new RuntimeException("Error while saving Measurement: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
    }
    
    public static void saveMeasurementsForSet(Set<Measurement> measurements, UUID mSetUUID, Connection connection, boolean closeDBcon) throws Exception
    {
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        ValidationReturnObject returnObj = MeasurementDAOHelper.areObjectsValidForSave(measurements, mSetUUID, true, connection, closeDBcon);
        if (!returnObj.valid)
        {
            log.error("Cannot save the Measurement objects: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot save the Measurement objects because the connection to the DB is closed");
            throw new RuntimeException("Cannot save the Measurement objects because the connection to the DB is closed");
        }
        
        try {
            // iterate over each measurement and save
            for (Measurement measurement : measurements)
            {
                String query = "INSERT INTO Measurement (measurementUUID, mSetUUID, timeStamp, value, synchronised) VALUES (?, ?, ?, ?, ?)";
            
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setObject(1, measurement.getUUID(), java.sql.Types.OTHER);
                pstmt.setObject(2, measurement.getMeasurementSetUUID(), java.sql.Types.OTHER);
                pstmt.setLong(3, measurement.getTimeStamp().getTime());
                pstmt.setString(4, measurement.getValue());
                pstmt.setBoolean(5, false);

                pstmt.executeUpdate();
            }
        } catch (Exception ex) {
            log.error("Error while saving Measurement: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving Measurement: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
            {
                connection.close();
            }
        }
    }
    
    public static Measurement getMeasurement(UUID measurementUUID, Connection connection, boolean closeDBcon) throws Exception
    {
        if (measurementUUID == null)
        {
            log.error("Cannot get a Measurement object with the given UUID because it is NULL!");
            throw new IllegalArgumentException("Cannot get a Measurement object with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Measurement because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Measurement because the connection to the DB is closed");
        }
        
        Measurement measurement = null;
        try {
            String query = "SELECT * FROM Measurement WHERE measurementUUID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, measurementUUID, java.sql.Types.OTHER);
            ResultSet rs = pstmt.executeQuery();
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                String mSetUUIDstr = rs.getString("mSetUUID");
				String timeStampStr = rs.getString("timeStamp");
                String value = rs.getString("value");
                Boolean synchronised = null;
                
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
                
                try {
                    synchronised = rs.getBoolean("synchronised");
                } catch (NullPointerException npe) {
                    throw new RuntimeException("Could not get the synchronised flag from the database.", npe);
                } catch (Exception ex) {
                    throw new RuntimeException("Could not get the synchronised flag from the database.", ex);
                }
                
                measurement = new Measurement(measurementUUID, mSetUUID, timeStamp, value);
                measurement.setSynchronised(synchronised);
            }
            else // nothing in the result set
            {
                log.error("There is no Measurement with the given UUID: " + measurementUUID.toString());
                throw new NoDataException("There is no Measurement with the given UUID: " + measurementUUID.toString());
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
        
        return measurement;
    }

    public static Set<Measurement> getMeasurementsForTimePeriod(UUID mSetUUID, Date fromDate, Date toDate, Connection connection, boolean closeDBcon) throws Exception
    {
        if (mSetUUID == null)
        {
            log.error("Cannot get Measurement objects for the MeasurementSet with the given UUID because it is NULL!");
            throw new IllegalArgumentException("Cannot get Measurement objects for the MeasurementSet with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Measurement objects because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Measurement objects because the connection to the DB is closed");
        }
        
        Set<Measurement> measurements = new HashSet<Measurement>();
        try {
            String query = "SELECT * FROM Measurement WHERE mSetUUID = ? AND "
                    + "timeStamp BETWEEN ? AND ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, mSetUUID, java.sql.Types.OTHER);
            pstmt.setLong(2, fromDate.getTime());
            pstmt.setLong(3, toDate.getTime());
            ResultSet rs = pstmt.executeQuery();
            
            // check if anything got returned
            if (!rs.next())
            {
                log.debug("There are no measurements for the given time period (" + fromDate + " - " + toDate + ").");
                throw new NoDataException("There are no measurements for the given time period (" + fromDate + " - " + toDate + ").");
            }
            
            // process each result item
            do {
                String measurementUUIDstr = rs.getString("measurementUUID");
				String timeStampStr = rs.getString("timeStamp");
                String value = rs.getString("value");
                
                try {
                    Date timeStamp = new Date(Long.parseLong(timeStampStr));
                    UUID measurementUUID = UUID.fromString(measurementUUIDstr);
                    Boolean synchronised = rs.getBoolean("synchronised");;
                    
                    Measurement measurement = new Measurement(measurementUUID, mSetUUID, timeStamp, value);
                    measurement.setSynchronised(synchronised);
                    measurements.add(measurement);
                } catch (Exception ex) {
                    log.error("Unable to process a measurement, but continuing: " + ex.getMessage(), ex);
                }
            } while (rs.next());
            
        } catch (NoDataException nde) {
            throw nde;
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
        
        return measurements;
    }
}
