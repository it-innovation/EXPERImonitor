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
//      Created Date :          2012-08-22
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.mon.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;

/**
 *
 * @author Vegard Engen
 */
public class ExperimentHelper
{
    static Logger log = Logger.getLogger(ExperimentHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(Experiment exp) throws Exception
    {
        if (exp == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Experiment object is NULL - cannot save that..."));
        }
        
        // check if all the required information is given; uuid, name
        
        if (exp.getUUID() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Experiment UUID is NULL"));
        }
        
        if (exp.getName() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Experiment name is NULL"));
        }
        /*
        // check if it exists in the DB already
        try {
            if (objectExists(exp.getUUID(), dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Experiment already exists; the UUID is not unique"));
            }
        } catch (Exception ex) {
            throw ex;
        }*/
        
        return new ValidationReturnObject(true);
    }
    
    /**
     * Checks the available parameters in the object and adds to the lists, the
     * table names and values accordingly.
     * 
     * OBS: it is assumed that the object has been validated to have at least the
     * minimum information.
     * 
     * @param exp
     * @param valueNames
     * @param values 
     */
    public static void getTableNamesAndValues(Experiment exp, List<String> valueNames, List<String> values)
    {
        if (exp == null)
            return;
        
        if ((valueNames == null) || (values == null))
            return;
        
        valueNames.add("expUUID");
        values.add("'" + exp.getUUID().toString() + "'");
        
        valueNames.add("name");
        values.add("'" + exp.getName() + "'");
        
        if (exp.getDescription() != null)
        {
            valueNames.add("description");
            values.add("'" + exp.getDescription() + "'");
        }
        
        if (exp.getStartTime() != null)
        {
            valueNames.add("startTime");
            values.add(String.valueOf(exp.getStartTime().getTime()));
        }
        
        if (exp.getEndTime() != null)
        {
            valueNames.add("endTime");
            values.add(String.valueOf(exp.getEndTime().getTime()));
        }
        
        if (exp.getExperimentID() != null)
        {
            valueNames.add("expID");
            values.add("'" + exp.getExperimentID() + "'");
        }
    }
    
    public static boolean objectExists(UUID uuid, Connection connection, boolean closeDBcon) throws Exception
    {
        return DBUtil.objectExistsByUUID("Experiment", "expUUID", uuid, connection, closeDBcon);
    }
    
    public static void saveExperiment(Experiment exp, Connection connection) throws Exception
    {
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        ValidationReturnObject returnObj = ExperimentHelper.isObjectValidForSave(exp);
        if (!returnObj.valid)
        {
            log.error("Cannot save the Experiment object: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot save the experiment because the connection to the DB is closed");
            throw new RuntimeException("Cannot save the experiment because the connection to the DB is closed");
        }
        
        boolean exception = false;
        try {
            log.debug("Starting transaction");
            connection.setAutoCommit(false);
            
            // get the table names and values according to what's available in the object
            List<String> valueNames = new ArrayList<String>();
            List<String> values = new ArrayList<String>();
            ExperimentHelper.getTableNamesAndValues(exp, valueNames, values);
            
            String query = DBUtil.getInsertIntoQuery("Experiment", valueNames, values);
            ResultSet rs = DBUtil.executeQuery(connection, query, Statement.RETURN_GENERATED_KEYS);
            
            // check if the result set got the generated table key
            if (rs.next()) {
                String key = rs.getString(1);
                log.debug("Saved experiment " + exp.getName() + " with key: " + key);
            } else {
                exception = true;
                throw new RuntimeException("No index returned after saving experiment " + exp.getName());
            }//end of debugging
        } catch (Exception ex) {
            exception = true;
            log.error("Error while saving experiment: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving experiment: " + ex.getMessage(), ex);
        } finally {
            if (exception)
            {
                log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                connection.rollback();
                connection.close();
            }
        }
        
        try {
            // save any metric generators if not NULL
            if ((exp.getMetricGenerators() != null) && !exp.getMetricGenerators().isEmpty())
            {
                log.debug("Saving " + exp.getMetricGenerators().size() + " metric generator(s) for the experiment");
                for (MetricGenerator mg : exp.getMetricGenerators())
                {
                    if (mg != null)
                        MetricGeneratorHelper.saveMetricGenerator(mg, exp.getUUID(), connection, false); // don't close the DB con
                }
            }
        } catch (Exception ex) {
            exception = true;
            throw new RuntimeException ("Unable to save experiment, because there were errors in saving sub-classes: " + ex.getMessage(), ex);
        } finally {
            if (exception) {
                log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                connection.rollback();
            } else {
                log.debug("Committing the transaction and closing the connection");
                connection.commit();
            }
            
            connection.close();
        }
    }
    
    /**
     * Overloaded method, with the option to set a flag whether to close the
     * DB connection or not.
     * @param expUUID
     * @param closeDBcon
     * @return
     * @throws Exception 
     */
    public static Experiment getExperiment(UUID expUUID, boolean withSubClasses, Connection connection, boolean closeDBcon) throws Exception
    {
        if (expUUID == null)
        {
            log.error("Cannot get an Experiment object with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot get an Experiment object with the given UUID because it is NULL!");
        }
        
        /*if (!ExperimentHelper.objectExists(expUUID, dbCon))
        {
            log.error("There is no experiment with the given UUID: " + expUUID.toString());
            throw new RuntimeException("There is no experiment with the given UUID: " + expUUID.toString());
        }*/
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the experiment because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the experiment because the connection to the DB is closed");
        }
        
        Experiment exp = null;
        boolean exception = false;
        try {
            String query = "SELECT * FROM Experiment WHERE expUUID = '" + expUUID + "'";
            ResultSet rs = DBUtil.executeQuery(connection, query);
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                String name = rs.getString("name");
				String description = rs.getString("description");
				String startTimeStr = rs.getString("startTime");
                String endTimeStr = rs.getString("endTime");
                String expID = rs.getString("expID");
                
                Date startTime = null;
                Date endTime = null;
                
                if (startTimeStr != null)
                    startTime = new Date(Long.parseLong(startTimeStr));
                
                if (endTimeStr != null)
                    endTime = new Date(Long.parseLong(endTimeStr));
                
                exp = new Experiment(expUUID, expID, name, description, startTime, endTime);
            }
            else // nothing in the result set
            {
                log.error("There is no experiment with the given UUID: " + expUUID.toString());
                throw new RuntimeException("There is no experiment with the given UUID: " + expUUID.toString());
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
        
        if (withSubClasses)
        {
            // check if there's any metric generators
            Set<MetricGenerator> metricGenerators = null;

            try {
                metricGenerators = MetricGeneratorHelper.getMetricGeneratorsForExperiment(expUUID, withSubClasses, connection, false);
            } catch (Exception ex) {
                log.error("Caught an exception when getting metric generators for experiment (UUID: " + expUUID.toString() + "): " + ex.getMessage());
            } finally {
                if (closeDBcon)
                {
                    connection.close();
                }
            }

            exp.setMetricGenerators(metricGenerators);
        }
        
        return exp;
    }
    
    public static Set<Experiment> getExperiments(boolean withSubClasses, Connection connection) throws Exception
    {
        Set<Experiment> experiments = new HashSet<Experiment>();
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the experiments because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the experiments because the connection to the DB is closed");
        }
        
        try {
            String query = "SELECT expUUID FROM Experiment";
            ResultSet rs = DBUtil.executeQuery(connection, query);
            
            // iterate over all returned records
            while (rs.next())
            {
                String uuidStr = rs.getString("expUUID");
                
                if (uuidStr == null)
                {
                    log.error("Skipping an Experiment, which does not have a UUID in the DB");
                    continue;
                }
                
                experiments.add(getExperiment(UUID.fromString(uuidStr), withSubClasses, connection, false));
            }
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            connection.close();
        }
        
        return experiments;
    }
    
    
    public static void deleteAllExperiments(Connection connection, boolean closeDBcon) throws Exception
    {
        log.debug("Deleting all experiments");
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot delete the experiments because the connection to the DB is closed");
            throw new RuntimeException("Cannot delete the experiments because the connection to the DB is closed");
        }
        
        if (closeDBcon)
        {
            log.debug("Starting transaction");
            connection.setAutoCommit(false);
        }
        
        boolean exception = false;
        
        try {
            String query = "DELETE from Experiment";
            DBUtil.executeQuery(connection, query);
        } catch (Exception ex) {
            exception = true;
            log.error("Unable to delete experiments: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to delete experiments: " + ex.getMessage(), ex);
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
    
    public static void deleteExperiment(UUID experimentUUID, Connection connection, boolean closeDBcon) throws Exception
    {
        log.debug("Deleting experiment");
        
        if (experimentUUID == null)
        {
            log.error("Cannot delete Experiment object with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot delete Experiment object with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot delete the experiments because the connection to the DB is closed");
            throw new RuntimeException("Cannot delete the experiments because the connection to the DB is closed");
        }
        
        if (closeDBcon)
        {
            log.debug("Starting transaction");
            connection.setAutoCommit(false);
        }
        
        boolean exception = false;
        
        try {
            String query = "DELETE from Experiment where expUUID = '" + experimentUUID + "'";
            DBUtil.executeQuery(connection, query);
        } catch (Exception ex) {
            exception = true;
            log.error("Unable to delete experiment: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to delete experiment: " + ex.getMessage(), ex);
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
