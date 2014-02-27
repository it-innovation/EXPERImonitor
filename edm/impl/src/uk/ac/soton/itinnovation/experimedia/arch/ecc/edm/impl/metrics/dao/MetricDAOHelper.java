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
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.metrics.dao;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Metric;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricType;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Unit;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.IECCLogger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.metrics.db.DBUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.NoDataException;

/**
 * A helper class for validating and executing queries for the Metrics.
 * 
 * @author Vegard Engen
 */
public class MetricDAOHelper
{
static IECCLogger log = Logger.getLogger(MetricDAOHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(Metric metric, Connection connection, boolean closeDBcon) throws Exception
    {
        if (metric == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Metric object is NULL - cannot save that..."));
        }
        
        // check if all the required information is given; uuid, type and unit
        
        if (metric.getUUID() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Metric UUID is NULL"));
        }
        
        if (metric.getMetricType() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Metric type is NULL"));
        }
        
        // TOOD: include check for unit again when serialisation issue has been sorted by SGC
        /*if (metric.getUnit() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Metric unit is NULL"));
        }*/
        
        // check if it exists in the DB already
        /*
        try {
            if (objectExists(metric.getUUID(), connection))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Metric already exists; the UUID is not unique"));
            }
        } catch (Exception ex) {
            throw ex;
        }*/
        
        return new ValidationReturnObject(true);
    }
    
    public static boolean objectExists(UUID uuid, Connection connection, boolean closeDBcon) throws Exception
    {
        return DBUtil.objectExistsByUUID("Metric", "metricUUID", uuid, connection, closeDBcon);
    }
    
    public static void saveMetric(Metric metric, Connection connection, boolean closeDBcon) throws Exception
    {
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        ValidationReturnObject returnObj = MetricDAOHelper.isObjectValidForSave(metric, connection, false);
        if (!returnObj.valid)
        {
            log.error("Cannot save the Metric object: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot save the Metric because the connection to the DB is closed");
            throw new RuntimeException("Cannot save the Metric because the connection to the DB is closed");
        }
        
        try {
            /*
            byte[] unitBytes = null;
            
            if (metric.getUnit() != null)
            {
                // serialising unit
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);

                oos.writeObject(metric.getUnit());
                oos.flush();
                oos.close();
                bos.close();

                unitBytes = bos.toByteArray();
            }
            */
            String query = "INSERT INTO Metric (metricUUID, mType, unit) VALUES (?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, metric.getUUID(), java.sql.Types.OTHER);
            pstmt.setString(2, metric.getMetricType().name());
            if (metric.getUnit() == null)
                pstmt.setObject(3, null);
            else
                pstmt.setObject(3, metric.getUnit().getName());
            //pstmt.setObject(3, unitBytes);
            
            int rowCount = pstmt.executeUpdate();
            
            // check if the result set got the generated table key
            if (rowCount > 0) {
                log.debug("Saved metric with uuid: " + metric.getUUID().toString());
            } else {
                throw new RuntimeException("Metric did not get saved in the database (PreparedStatement returned 0 rows)");
            }//end of debugging
        } catch (Exception ex) {
            log.error("Error while saving metric: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving metric: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
    }
    
    public static Metric getMetric(UUID metricUUID, Connection connection, boolean closeDBcon) throws Exception
    {
        if (metricUUID == null)
        {
            log.error("Cannot get a Metric object with the given UUID because it is NULL!");
            throw new IllegalArgumentException("Cannot get a Metric object with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Metric because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Metric because the connection to the DB is closed");
        }
        
        /*if (!MetricHelper.objectExists(metricUUID, connection, closeDBcon))
        {
            log.error("There is no metric with the given UUID: " + metricUUID.toString());
            throw new RuntimeException("There is no metric with the given UUID: " + metricUUID.toString());
        }*/
        
        Metric metric = null;
        
        try {
            String query = "SELECT * FROM Metric WHERE metricUUID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, metricUUID, java.sql.Types.OTHER);
            ResultSet rs = pstmt.executeQuery();
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                Unit unit = null;
                String unitName = rs.getString("unit");
                if (unitName != null)
                    unit = new Unit(unitName);

                /*ByteArrayInputStream bais;
                /ObjectInputStream ins;
                if (rs.getBytes("unit") != null)
                {
                    try {
                        bais = new ByteArrayInputStream(rs.getBytes("unit"));
                        ins = new ObjectInputStream(bais);
                        unit =(Unit)ins.readObject();
                        ins.close();
                    }
                    catch (Exception e) {
                        log.error("Unable to read the unit from the database: " + e.getMessage());
                        throw new RuntimeException("Unable to read the unit from the database", e);
                    }
                }*/
                
                String metricTypeStr = rs.getString("mType");
                if (metricTypeStr == null)
                {
                    log.error("Unable to get the metric type from the database");
                    throw new RuntimeException("Unable to get the metric type from the database");
                }
                
                metric = new Metric(metricUUID, MetricType.fromValue(metricTypeStr), unit);
            }
            else // nothing in the result set
            {
                log.error("There is no metric with the given UUID: " + metricUUID.toString());
                throw new NoDataException("There is no metric with the given UUID: " + metricUUID.toString());
            }
        } catch (NoDataException nde) {
            throw nde;
        } catch (Exception ex) {
            log.error("Error while getting metric: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while getting metric: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
        
        return metric;
    }
    
    public static Metric getMetricForMeasurementSet(UUID measurementSetUUID, Connection connection, boolean closeDBcon) throws Exception
    {
        if (measurementSetUUID == null)
        {
            log.error("Cannot get a Metric object for the given measurement set, because its given UUID is NULL!");
            throw new IllegalArgumentException("Cannot get a Metric object for the given measurement set, because its given UUID is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Metric for the MeasurementSet because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Metric for the MeasurementSet because the connection to the DB is closed");
        }
        
        /*if (!MeasurementSetHelper.objectExists(measurementSetUUID, connection, closeDBcon))
        {
            log.error("There is no measurement set with the given UUID: " + measurementSetUUID.toString());
            throw new RuntimeException("There is no measurement set with the given UUID: " + measurementSetUUID.toString());
        }*/
        
        Metric metric = null;
        
        try {
            String query = "SELECT * FROM Metric WHERE metricUUID = (SELECT metricUUID FROM MeasurementSet WHERE mSetUUID = ?)";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, measurementSetUUID, java.sql.Types.OTHER);
            ResultSet rs = pstmt.executeQuery();
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                String uuidStr = rs.getString("metricUUID");
                if (uuidStr == null)
                {
                    log.error("Unable to get the metric UUID from the database");
                    throw new RuntimeException("Unable to get the metric UUID from the database");
                }
                
                String metricTypeStr = rs.getString("mType");
                if (metricTypeStr == null)
                {
                    log.error("Unable to get the metric type from the database");
                    throw new RuntimeException("Unable to get the metric type from the database");
                }
                ByteArrayInputStream bais;
                ObjectInputStream ins;
                Unit unit;

                try {
                    bais = new ByteArrayInputStream(rs.getBytes("unit"));
                    ins = new ObjectInputStream(bais);
                    unit =(Unit)ins.readObject();
                    ins.close();
                }
                catch (Exception e) {
                    log.error("Unable to read the unit from the database");
                    throw new RuntimeException("Unable to read the unit from the database");
                }
                
                metric = new Metric(UUID.fromString(uuidStr), MetricType.fromValue(metricTypeStr), unit);
            }
            else // nothing in the result set
            {
                if (!ExperimentDAOHelper.objectExists(measurementSetUUID, connection, false))
                {
                    log.debug("There is no measurement set with UUID " + measurementSetUUID.toString());
                    throw new NoDataException("There is no measurement set with UUID " + measurementSetUUID.toString());
                }
                log.error("Didn't find any metric for measurement set with the given UUID: " + measurementSetUUID.toString());
                throw new NoDataException("Didn't find any metric for measurement set with the given UUID: " + measurementSetUUID.toString());
            }
        } catch (NoDataException nde) {
            throw nde;
        } catch (Exception ex) {
            log.error("Error while getting metric for measurement set: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while getting metric for measurement set: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
        
        return metric;
    }
    
    public static void deleteAllMetrics(Connection connection, boolean closeDBcon) throws Exception
    {
        log.debug("Deleting all metrics");
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot delete the metrics because the connection to the DB is closed");
            throw new RuntimeException("Cannot delete the metrics because the connection to the DB is closed");
        }
        
        if (closeDBcon)
        {
            log.debug("Starting transaction");
            connection.setAutoCommit(false);
        }
        
        boolean exception = false;
        
        try {
            String query = "DELETE from Metric";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.executeUpdate();
        } catch (Exception ex) {
            exception = true;
            log.error("Unable to delete metrics: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to delete metrics: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
            {
                if (exception) {
                    log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                    connection.rollback();
                } else {
                    log.debug("Committing the transaction and closing the connection");
                    connection.commit();
                }
            }
        }
    }
    
    public static void deleteMetric(UUID metricUUID, Connection connection, boolean closeDBcon) throws Exception
    {
        log.debug("Deleting metric");
        
        if (metricUUID == null)
        {
            log.error("Cannot delete metric object with the given UUID because it is NULL!");
            throw new IllegalArgumentException("Cannot delete metric object with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot delete the metric because the connection to the DB is closed");
            throw new RuntimeException("Cannot delete the metric because the connection to the DB is closed");
        }
        
        if (closeDBcon)
        {
            log.debug("Starting transaction");
            connection.setAutoCommit(false);
        }
        
        boolean exception = false;
        
        try {
            String query = "DELETE from Metric where metricUUID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, metricUUID, java.sql.Types.OTHER);
            pstmt.executeUpdate();
        } catch (Exception ex) {
            exception = true;
            log.error("Unable to delete metric: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to delete metric: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
            {
                if (exception) {
                    log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                    connection.rollback();
                } else {
                    log.debug("Committing the transaction and closing the connection");
                    connection.commit();
                }
            }
        }
    }
}
