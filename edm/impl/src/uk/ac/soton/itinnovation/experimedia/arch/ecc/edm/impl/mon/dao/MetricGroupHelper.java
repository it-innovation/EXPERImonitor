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
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.mon.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;

/**
 *
 * @author Vegard Engen
 */
public class MetricGroupHelper
{
    static Logger log = Logger.getLogger(MetricGroupHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(MetricGroup mGroup, boolean checkForMetricGenerator, Connection connection, boolean closeDBcon) throws Exception
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
            return new ValidationReturnObject(false, new NullPointerException("The MetricGroup name is NULL"));
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
            if (!MetricGeneratorHelper.objectExists(mGroup.getMetricGeneratorUUID(), connection, closeDBcon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The MetricGenerator for the MetricGroup doesn't exit"));
            }
        }
        
        // if any measurements, validate them too!
        if ((mGroup.getMeasurementSets() != null) && !mGroup.getMeasurementSets().isEmpty())
        {
            for (MeasurementSet mSet : mGroup.getMeasurementSets())
            {
                ValidationReturnObject validationReturn = MeasurementSetHelper.isObjectValidForSave(mSet, false, connection, closeDBcon); // false = don't check for measurement set existing as this won't be saved yet!
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
    
    public static boolean objectExists(UUID uuid, Connection connection, boolean closeDBcon) throws Exception
    {
        return DBUtil.objectExistsByUUID("MetricGroup", "mGrpUUID", uuid, connection, closeDBcon);
    }
    
    public static void saveMetricGroup(MetricGroup metricGroup, Connection connection, boolean closeDBcon) throws Exception
    {
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        // will validate the measurement sets too, if any are given
        ValidationReturnObject returnObj = MetricGroupHelper.isObjectValidForSave(metricGroup, false, connection, false);
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
            
            // get the table names and values according to what's available in the
            // object
            List<String> valueNames = new ArrayList<String>();
            List<String> values = new ArrayList<String>();
            MetricGroupHelper.getTableNamesAndValues(metricGroup, valueNames, values);
            
            String query = DBUtil.getInsertIntoQuery("MetricGroup", valueNames, values);
            ResultSet rs = DBUtil.executeQuery(connection, query, Statement.RETURN_GENERATED_KEYS);
            
            // check if the result set got the generated table key
            if (rs.next()) {
                String key = rs.getString(1);
                log.debug("Saved MetricGroup " + metricGroup.getName() + " with key: " + key);
            } else {
                exception = true;
                throw new RuntimeException("No index returned after saving MetricGroup " + metricGroup.getName());
            }
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
                        MeasurementSetHelper.saveMeasurementSet(mSet, connection, false); // flag not to close the DB connection
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
            throw new NullPointerException("Cannot get a MetricGroup object with the given UUID because it is NULL!");
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
            String query = "SELECT * FROM MetricGroup WHERE mGrpUUID = '" + metricGroupUUID + "'";
            ResultSet rs = DBUtil.executeQuery(connection, query);
            
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
                throw new RuntimeException("There is no MetricGroup with the given UUID: " + metricGroupUUID.toString());
            }
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
                measurementSets = MeasurementSetHelper.getMeasurementSetForMetricGroup(metricGroupUUID, withSubClasses, connection, false); // don't close the connection
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
            throw new NullPointerException("Cannot get any MetricGroup objects for a MetricGenerator with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the MetricGroup because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the MetricGroup because the connection to the DB is closed");
        }
        
        Set<MetricGroup> metricGroups = new HashSet<MetricGroup>();
        
        try {
            String query = "SELECT mGrpUUID FROM MetricGroup WHERE mGenUUID = '" + metricGenUUID.toString() + "'";
            ResultSet rs = DBUtil.executeQuery(connection, query);
            
            // check if anything got returned (connection closed in finalise method)
            while (rs.next())
            {
                String uuidStr = rs.getString("mGrpUUID");
                
                if (uuidStr == null)
                {
                    log.debug("Skipping a MetricGroup, which does not have a UUID in the DB");
                    continue;
                }
                
                metricGroups.add(getMetricGroup(UUID.fromString(uuidStr), withSubClasses, connection, false));
            }
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
