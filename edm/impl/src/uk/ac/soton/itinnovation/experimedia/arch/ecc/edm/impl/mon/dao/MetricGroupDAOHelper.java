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
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.NoDataException;

/**
 * A helper class for validating and executing queries for the Metric Groups.
 * 
 * @author Vegard Engen
 */
public class MetricGroupDAOHelper
{
    static Logger log = Logger.getLogger(MetricGroupDAOHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(MetricGroup mGroup, boolean checkForMetricGenerator, Connection connection, boolean closeDBcon) throws Exception
    {
        if (mGroup == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The MetricGroup object is NULL - cannot save that..."));
        }
        
        // check if all the required information is given; uuid, metric generator UUID, name, measurement sets (if any are given)
        
        if (mGroup.getUUID() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The MetricGroup UUID is NULL"));
        }
        
        if (mGroup.getMetricGeneratorUUID() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The MetricGroup's metric generator UUID is NULL"));
        }
        
        if (mGroup.getName() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The MetricGroup name is NULL"));
        }
        
        /*
        // check if it exists in the DB already
        try {
            if (objectExists(mGroup.getUUID(), connection))
            {
                return new ValidationReturnObject(false, new RuntimeException("The MetricGroup already exists; the UUID is not unique"));
            }
        } catch (Exception ex) {
            throw ex;
        }*/
        
        // check if the metric generator exists, if it should be checked...
        if (checkForMetricGenerator)
        {
            if (!MetricGeneratorDAOHelper.objectExists(mGroup.getMetricGeneratorUUID(), connection, closeDBcon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The MetricGenerator for the MetricGroup doesn't exit"));
            }
        }
        
        // if any measurements, validate them too!
        if ((mGroup.getMeasurementSets() != null) && !mGroup.getMeasurementSets().isEmpty())
        {
            for (MeasurementSet mSet : mGroup.getMeasurementSets())
            {
                ValidationReturnObject validationReturn = MeasurementSetDAOHelper.isObjectValidForSave(mSet, false, connection, closeDBcon); // false = don't check for measurement set existing as this won't be saved yet!
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
    
    public static boolean objectExists(UUID uuid, Connection connection, boolean closeDBcon) throws Exception
    {
        return DBUtil.objectExistsByUUID("MetricGroup", "mGrpUUID", uuid, connection, closeDBcon);
    }
    
    public static void saveMetricGroup(MetricGroup metricGroup, Connection connection, boolean closeDBcon) throws Exception
    {
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        // will validate the measurement sets too, if any are given
        ValidationReturnObject returnObj = MetricGroupDAOHelper.isObjectValidForSave(metricGroup, false, connection, false);
        if (!returnObj.valid)
        {
            log.error("Cannot save the MetricGroup object: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot save the MetricGroup because the connection to the DB is closed");
            throw new RuntimeException("Cannot save the MetricGroup because the connection to the DB is closed");
        }
        
        boolean exception = false;
        try {
            if (closeDBcon)
            {
                log.debug("Starting transaction");
                connection.setAutoCommit(false);
            }
            
            String query = "INSERT INTO MetricGroup (mGrpUUID, mGenUUID, name, description) VALUES (?, ?, ?, ?)";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, metricGroup.getUUID(), java.sql.Types.OTHER);
            pstmt.setObject(2, metricGroup.getMetricGeneratorUUID(), java.sql.Types.OTHER);
            pstmt.setString(3, metricGroup.getName());
            pstmt.setString(4, metricGroup.getDescription());
            
            pstmt.executeUpdate();
        } catch (Exception ex) {
            exception = true;
            log.error("Error while saving MetricGroup: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving MetricGroup: " + ex.getMessage(), ex);
        } finally {
            if (exception && closeDBcon)
            {
                log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                connection.rollback();
                connection.close();
            }
        }
        
        try {
            // save any measurement sets if not NULL
            if ((metricGroup.getMeasurementSets() != null) && !metricGroup.getMeasurementSets().isEmpty())
            {
                log.debug("Saving " + metricGroup.getMeasurementSets().size() + " measurement set(s) for the metric group");
                for (MeasurementSet mSet : metricGroup.getMeasurementSets())
                {
                    if (mSet != null)
                        MeasurementSetDAOHelper.saveMeasurementSet(mSet, connection, false); // flag not to close the DB connection
                }
            }
        } catch (Exception ex) {
            exception = true;
            throw ex;
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
    
    public static MetricGroup getMetricGroup(UUID metricGroupUUID, boolean withSubClasses, Connection connection, boolean closeDBcon) throws Exception
    {
        if (metricGroupUUID == null)
        {
            log.error("Cannot get a MetricGroup object with the given UUID because it is NULL!");
            throw new IllegalArgumentException("Cannot get a MetricGroup object with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the MetricGroup because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the MetricGroup because the connection to the DB is closed");
        }
        
        /*if (!MetricGroupHelper.objectExists(metricGroupUUID, connection))
        {
            log.error("There is no MetricGroup with the given UUID: " + metricGroupUUID.toString());
            throw new RuntimeException("There is no MetricGroup with the given UUID: " + metricGroupUUID.toString());
        }*/
        
        MetricGroup metricGroup = null;
        boolean exception = false;
        try {
            String query = "SELECT * FROM MetricGroup WHERE mGrpUUID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, metricGroupUUID, java.sql.Types.OTHER);
            ResultSet rs = pstmt.executeQuery();
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                String mGenUUID = rs.getString("mGenUUID");
                String name = rs.getString("name");
				String description = rs.getString("description");
                
                metricGroup = new MetricGroup(metricGroupUUID, UUID.fromString(mGenUUID), name, description);
            }
            else // nothing in the result set
            {
                log.error("There is no MetricGroup with the given UUID: " + metricGroupUUID.toString());
                throw new NoDataException("There is no MetricGroup with the given UUID: " + metricGroupUUID.toString());
            }
        } catch (NoDataException nde) {
            throw nde;
        } catch (Exception ex) {
            exception = true;
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (exception || (closeDBcon && !withSubClasses))
            {
                connection.close();
            }
        }
        
        // check if there's any measurement sets
        if(withSubClasses)
        {
            Set<MeasurementSet> measurementSets = null;

            try {
                measurementSets = MeasurementSetDAOHelper.getMeasurementSetForMetricGroup(metricGroupUUID, withSubClasses, connection, false); // don't close the connection
            } catch (NoDataException nde) {
                log.debug("There were no measurement sets for the metric group");
            } catch (Exception ex) {
                log.error("Caught an exception when getting measurement sets for MetricGroup (UUID: " + metricGroupUUID.toString() + "): " + ex.getMessage());
                throw new RuntimeException("Caught an exception when getting measurement sets for MetricGroup (UUID: " + metricGroupUUID.toString() + "): " + ex.getMessage(), ex);
            } finally {
                if (closeDBcon)
                {
                    connection.close();
                }
            }

            metricGroup.setMeasurementSets(measurementSets);
        }
        
        return metricGroup;
    }
    
    public static Set<MetricGroup> getMetricGroupsForMetricGenerator(UUID metricGenUUID, boolean withSubClasses, Connection connection, boolean closeDBcon) throws Exception
    {
        if (metricGenUUID == null)
        {
            log.error("Cannot get any MetricGroup objects for a MetricGenerator with the given UUID because it is NULL!");
            throw new IllegalArgumentException("Cannot get any MetricGroup objects for a MetricGenerator with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the MetricGroup because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the MetricGroup because the connection to the DB is closed");
        }
        
        Set<MetricGroup> metricGroups = new HashSet<MetricGroup>();
        
        try {
            String query = "SELECT mGrpUUID FROM MetricGroup WHERE mGenUUID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, metricGenUUID, java.sql.Types.OTHER);
            ResultSet rs = pstmt.executeQuery();
            
            // check if anything got returned
            if (!rs.next())
            {
                if (!MetricGeneratorDAOHelper.objectExists(metricGenUUID, connection, false))
                {
                    log.debug("There is no metric generator with UUID " + metricGenUUID.toString());
                    throw new NoDataException("There is no metric generator with UUID " + metricGenUUID.toString());
                }
                log.debug("There are no metric groups for the given metric generator (UUID = " + metricGenUUID.toString() + ").");
                throw new NoDataException("There are no metric groups for the given metric generator (UUID = " + metricGenUUID.toString() + ").");
            }
            
            // process each result item
            do {
                String uuidStr = rs.getString("mGrpUUID");
                
                if (uuidStr == null)
                {
                    log.debug("Skipping a MetricGroup, which does not have a UUID in the DB");
                    continue;
                }
                
                metricGroups.add(getMetricGroup(UUID.fromString(uuidStr), withSubClasses, connection, false));
            } while (rs.next());
            
        } catch (NoDataException nde) {
            throw nde;
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
            {
                connection.close();
            }
        }
        
        return metricGroups;
    }
}
