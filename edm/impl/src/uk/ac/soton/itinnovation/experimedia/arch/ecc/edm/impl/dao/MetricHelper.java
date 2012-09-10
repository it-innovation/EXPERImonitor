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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import javax.measure.unit.Unit;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Metric;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricType;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DatabaseConnector;

/**
 *
 * @author Vegard Engen
 */
public class MetricHelper
{
static Logger log = Logger.getLogger(MetricHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(Metric metric, DatabaseConnector dbCon, boolean closeDBcon) throws Exception
    {
        if (metric == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Metric object is NULL - cannot save that..."));
        }
        
        // check if all the required information is given; uuid, type and unit
        
        if (metric.getUUID() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Metric UUID is NULL"));
        }
        
        if (metric.getMetricType() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Metric type is NULL"));
        }
        
        // TOOD: include check for unit again when serialisation issue has been sorted by SGC
        /*if (metric.getUnit() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Metric unit is NULL"));
        }*/
        
        // check if it exists in the DB already
        /*
        try {
            if (objectExists(metric.getUUID(), dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Metric already exists; the UUID is not unique"));
            }
        } catch (Exception ex) {
            throw ex;
        }*/
        
        return new ValidationReturnObject(true);
    }
    
    public static boolean objectExists(UUID uuid, DatabaseConnector dbCon, boolean closeDBcon) throws Exception
    {
        return DBUtil.objectExistsByUUID("Metric", "metricUUID", uuid, dbCon, closeDBcon);
    }
    
    public static void saveMetric(DatabaseConnector dbCon, Metric metric, boolean closeDBcon) throws Exception
    {
        log.debug("Saving metric");
        
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        ValidationReturnObject returnObj = MetricHelper.isObjectValidForSave(metric, dbCon, closeDBcon);
        if (!returnObj.valid)
        {
            log.error("Cannot save the Metric object: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            byte[] unitBytes = null;
            
            if (metric.getUnit() != null)
            {
                //metric.setUnit(Unit.ONE);
            
                // serialising unit
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);

                oos.writeObject(metric.getUnit());
                oos.flush();
                oos.close();
                bos.close();

                unitBytes = bos.toByteArray();
            }
        
            String query = "INSERT INTO Metric (metricUUID, mType, unit) VALUES (?, ?, ?)";
            PreparedStatement pstmt = dbCon.getConnection().prepareStatement(query);
            pstmt.setObject(1, metric.getUUID(), java.sql.Types.OTHER);
            pstmt.setString(2, metric.getMetricType().name());
            pstmt.setObject(3, unitBytes);
            
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
                dbCon.close();
        }
    }
    
    public static Metric getMetric(DatabaseConnector dbCon, UUID metricUUID, boolean closeDBcon) throws Exception
    {
        if (metricUUID == null)
        {
            log.error("Cannot get a Metric object with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot get a Metric object with the given UUID because it is NULL!");
        }
        
        if (!MetricHelper.objectExists(metricUUID, dbCon, closeDBcon))
        {
            log.error("There is no metric with the given UUID: " + metricUUID.toString());
            throw new RuntimeException("There is no metric with the given UUID: " + metricUUID.toString());
        }
        
        Metric metric = null;
        
        try {
            if (dbCon.isClosed())
                dbCon.connect();
        
            String query = "SELECT * FROM Metric WHERE metricUUID = '" + metricUUID.toString() + "'";
            ResultSet rs = dbCon.executeQuery(query);
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                ByteArrayInputStream bais;
                ObjectInputStream ins;
                Unit unit = null;

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
                }
                
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
                throw new RuntimeException("There is no metric with the given UUID: " + metricUUID.toString());
            }
        } catch (Exception ex) {
            log.error("Error while getting metric: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while getting metric: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
        
        return metric;
    }
    
    public static Metric getMetricForMeasurementSet(DatabaseConnector dbCon, UUID measurementSetUUID, boolean closeDBcon) throws Exception
    {
        if (measurementSetUUID == null)
        {
            log.error("Cannot get a Metric object for the given measurement set, because its given UUID is NULL!");
            throw new NullPointerException("Cannot get a Metric object for the given measurement set, because its given UUID is NULL!");
        }
        
        if (!MeasurementSetHelper.objectExists(measurementSetUUID, dbCon, closeDBcon))
        {
            log.error("There is no measurement set with the given UUID: " + measurementSetUUID.toString());
            throw new RuntimeException("There is no measurement set with the given UUID: " + measurementSetUUID.toString());
        }
        
        Metric metric = null;
        
        try {
            if (dbCon.isClosed())
                dbCon.connect();
        
            // TODO: validate that this is correct!
            String query = "SELECT * FROM Metric WHERE metricUUID = (SELECT metricUUID FROM MeasurementSet WHERE mSetUUID = '" + measurementSetUUID + "')";
            ResultSet rs = dbCon.executeQuery(query);
            
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
                log.error("Didn't find any metric for measurement set with the given UUID: " + measurementSetUUID.toString());
                throw new RuntimeException("Didn't find any metric for measurement set with the given UUID: " + measurementSetUUID.toString());
            }
        } catch (Exception ex) {
            log.error("Error while getting metric for measurement set: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while getting metric for measurement set: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
        
        return metric;
    }
}
