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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Metric;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;

/**
 * A helper class for validating and executing queries for the Measurement Sets.
 * 
 * @author Vegard Engen
 */
public class MeasurementSetDAOHelper
{
    static Logger log = Logger.getLogger(MeasurementSetDAOHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(MeasurementSet mSet, boolean checkForMetricGroup, Connection connection, boolean closeDBcon) throws Exception
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
        /*else
        {
            // check if metric exists in the DB already
            try {
                if (MetricHelper.objectExists(mSet.getMetric().getUUID(), connection))
                {
                    return new ValidationReturnObject(false, new RuntimeException("The MeasurementSet's metric already exists; the UUID of the metric is not unique"));
                }
            } catch (Exception ex) {
                throw ex;
            }
        }*/
        /*
        // check if it exists in the DB already
        try {
            if (objectExists(mSet.getUUID(), connection))
            {
                return new ValidationReturnObject(false, new RuntimeException("The MeasurementSet already exists; the UUID is not unique"));
            }
        } catch (Exception ex) {
            throw ex;
        }*/
        
        // check that the metric group exists, if flagged to check
        if (checkForMetricGroup)
        {
            if (!MetricGroupDAOHelper.objectExists(mSet.getMetricGroupUUID(), connection, closeDBcon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The MetricGroup for the MeasurementSet doesn't exit (UUID: " + mSet.getMetricGroupUUID().toString() + ")"));
            }
        }
        
        // check that the attribute exists
        /*if (!AttributeHelper.objectExists(mSet.getAttributeUUID(), connection))
        {
            return new ValidationReturnObject(false, new RuntimeException("The Attribute for the MeasurementSet doesn't exit (UUID: " + mSet.getAttributeUUID().toString() + ")"));
        }*/
        
        // if any measurements, validate them too!
        if ((mSet.getMeasurements() != null) && !mSet.getMeasurements().isEmpty())
        {
            for (Measurement measurement : mSet.getMeasurements())
            {
                ValidationReturnObject validationReturn = MeasurementDAOHelper.isObjectValidForSave(measurement, false, connection, closeDBcon); // false = don't check for measurement set existing as this won't be saved yet!
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
    
    public static boolean objectExists(UUID uuid, Connection connection, boolean closeDBcon) throws Exception
    {
        return DBUtil.objectExistsByUUID("MeasurementSet", "mSetUUID", uuid, connection, closeDBcon);
    }
    
    public static void saveMeasurementSet(MeasurementSet measurementSet, Connection connection, boolean closeDBcon) throws Exception
    {
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        // will validate the metric and measurements too, if any are given
        ValidationReturnObject returnObj = MeasurementSetDAOHelper.isObjectValidForSave(measurementSet, false, connection, false);
        if (!returnObj.valid)
        {
            log.error("Cannot save the MeasurementSet object: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot save the MeasurementSet because the connection to the DB is closed");
            throw new RuntimeException("Cannot save the MeasurementSet because the connection to the DB is closed");
        }
        
        if (closeDBcon)
        {
            log.debug("Starting transaction");
            connection.setAutoCommit(false);
        }
        
        
        boolean exception = false;
        // save the metric
        try {
            MetricDAOHelper.saveMetric(measurementSet.getMetric(), connection, false);
        } catch (Exception ex) {
            exception = true;
            log.error("Cannot save the MeasurementSet object because of an error in saving the metric! " + ex.getMessage());
            throw new RuntimeException("Cannot save the MeasurementSet object because of an error in saving the metric! " + ex.getMessage(), ex);
        } finally {
            if (exception && closeDBcon)
            {
                log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                connection.rollback();
                connection.close();
            }
        }
        
        // saving the measurement set
        try {
            String query = "INSERT INTO MeasurementSet (mSetUUID, mGrpUUID, attribUUID, metricUUID) VALUES "
                    + "(?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, measurementSet.getUUID(), java.sql.Types.OTHER);
            pstmt.setObject(2, measurementSet.getMetricGroupUUID(), java.sql.Types.OTHER);
            pstmt.setObject(3, measurementSet.getAttributeUUID(), java.sql.Types.OTHER);
            pstmt.setObject(4, measurementSet.getMetric().getUUID(), java.sql.Types.OTHER);
            
            pstmt.executeUpdate();
        } catch (Exception ex) {
            exception = true;
            log.error("Error while saving MeasurementSet: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving MeasurementSet: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
            {
                if (exception) {
                    log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                    connection.rollback();
                }
                else {
                    log.debug("Committing the transaction and closing the connection");
                    connection.commit();
                }
                connection.close();
            }
        }
    }
    
    public static MeasurementSet getMeasurementSet(UUID measurementSetUUID, boolean withMetric, Connection connection, boolean closeDBcon) throws Exception
    {
        // get the measurement set with metric, but not with any measurements
        if (measurementSetUUID == null)
        {
            log.error("Cannot get a MeasurementSet object with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot get a MeasurementSet object with the given UUID because it is NULL!");
        }
        
        /*if (!MeasurementSetHelper.objectExists(measurementSetUUID, connection))
        {
            log.error("There is no MeasurementSet with the given UUID: " + measurementSetUUID.toString());
            throw new RuntimeException("There is no MeasurementSet with the given UUID: " + measurementSetUUID.toString());
        }*/
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the MeasurementSet because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the MeasurementSet because the connection to the DB is closed");
        }
        
        MeasurementSet measurementSet = null;
        String metricUUIDstr = null;
        boolean exception = false;
        try {
            String query = "SELECT * FROM MeasurementSet WHERE mSetUUID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, measurementSetUUID, java.sql.Types.OTHER);
            ResultSet rs = pstmt.executeQuery();
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                String mGenUUIDstr = rs.getString("mGrpUUID");
                String attribUUIDstr = rs.getString("attribUUID");
                metricUUIDstr = rs.getString("metricUUID");
                
                measurementSet = new MeasurementSet();
                measurementSet.setUUID(measurementSetUUID);
                measurementSet.setMetricGroupUUID(UUID.fromString(mGenUUIDstr));
                measurementSet.setAttributeUUID(UUID.fromString(attribUUIDstr));
            }
            else // nothing in the result set
            {
                log.error("There is no MeasurementSet with the given UUID: " + measurementSetUUID.toString());
                throw new RuntimeException("There is no MeasurementSet with the given UUID: " + measurementSetUUID.toString());
            }
        } catch (Exception ex) {
            exception = true;
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (exception || (closeDBcon && !withMetric))
                connection.close();
        }
        
        // get the metric
        if (withMetric)
        {
            if (metricUUIDstr == null)
            {
                log.error("Unable to get the MeasurementSet from the object because the metricUUID couldn't be retrieved to get the Metric object");
                throw new RuntimeException("Unable to get the MeasurementSet from the object because the metricUUID couldn't be retrieved to get the Metric object");
            }

            try {
                Metric metric = MetricDAOHelper.getMetric(UUID.fromString(metricUUIDstr), connection, false); // don't close the connection
                measurementSet.setMetric(metric);
            } catch (Exception ex) {
                log.error("Caught an exception when getting the Metric for the MeasurementSet (UUID: " + measurementSetUUID.toString() + "): " + ex.getMessage());
                throw new RuntimeException("Caught an exception when getting the Metric for MeasurementSet (UUID: " + measurementSetUUID.toString() + "): " + ex.getMessage(), ex);
            } finally {
                if (closeDBcon)
                    connection.close();
            }
        }
        
        return measurementSet;
    }
    
    public static Set<MeasurementSet> getMeasurementSetForMetricGroup(UUID metricGroupUUID, boolean withMetric, Connection connection, boolean closeDBcon) throws Exception
    {
        if (metricGroupUUID == null)
        {
            log.error("Cannot get any MeasurementSet objects for a MetricGroup with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot get any MeasurementSet objects for a MetricGroup with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the MeasurementSet objects for the MetricGroup because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the MeasurementSet objects for the MetricGroup because the connection to the DB is closed");
        }
        
        Set<MeasurementSet> measurementSets = new HashSet<MeasurementSet>();
        
        try {
            String query = "SELECT mSetUUID FROM MeasurementSet WHERE mGrpUUID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, metricGroupUUID, java.sql.Types.OTHER);
            ResultSet rs = pstmt.executeQuery();
            
            // check if anything got returned (connection closed in finalise method)
            while (rs.next())
            {
                String uuidStr = rs.getString("mSetUUID");
                
                if (uuidStr == null)
                {
                    log.error("Skipping a MeasurementSet, which does not have a UUID in the DB");
                    continue;
                }
                
                measurementSets.add(getMeasurementSet(UUID.fromString(uuidStr), withMetric, connection, false));
            }
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
        
        return measurementSets;
    }
}