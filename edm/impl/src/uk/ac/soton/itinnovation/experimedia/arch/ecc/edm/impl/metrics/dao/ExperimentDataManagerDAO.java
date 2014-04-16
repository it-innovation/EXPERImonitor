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
//      Created Date :          2012-08-21
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.metrics.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Metric;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.IECCLogger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.metrics.db.DBUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.metrics.db.DatabaseConnector;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.metrics.db.DatabaseType;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.NoDataException;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IEntityDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMeasurementDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMeasurementSetDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMetricDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMetricGroupDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IReportDAO;

/**
 * A DAO class for the Monitoring Experiment Data Manager.
 * 
 * @author Vegard Engen
 */
public class ExperimentDataManagerDAO implements IExperimentDAO, IEntityDAO, IMetricGeneratorDAO, IMetricGroupDAO, IMeasurementSetDAO, IMetricDAO, IMeasurementDAO, IReportDAO
{
    private DatabaseConnector dbCon;
    private static IECCLogger log = Logger.getLogger(ExperimentDataManagerDAO.class);
    
    /**
     * 
     * @param configParams Configuration parameters, which will be used instead of reading default config file from disk.
     * @throws Exception 
     */
    public ExperimentDataManagerDAO(Properties configParams) throws Exception
    {
        if (configParams == null)
        {
            log.error("Properties (config) object is NULL - cannot instantiate the EDM DAO");
            throw new IllegalArgumentException("Properties (config) object is NULL - cannot instantiate the EDM DAO");
        }
        
        initDatabaseConnector(configParams);
    }
    
    private void initDatabaseConnector(Properties configParams) throws Exception
    {
        try {
            dbCon = new DatabaseConnector(configParams.getProperty("dbURL"), configParams.getProperty("dbName"), configParams.getProperty("dbUsername"),configParams.getProperty("dbPassword"), DatabaseType.fromValue(configParams.getProperty("dbType")));
        } catch (Throwable ex) {
            log.error("Failed to create DatabaseConnector: " + ex.getMessage(), ex);
            throw new RuntimeException("Failed to create DatabaseConnector: " + ex.getMessage(), ex);
        }
    }
    
    
    //-------------------------- EXPERIMENT ----------------------------------//
    
    
    @Override
    public void saveExperiment(Experiment exp) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection(Connection.TRANSACTION_READ_COMMITTED);
        } catch (Exception ex) {
            log.error("Unable to save experiment, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to save experiment, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            ExperimentDAOHelper.saveExperiment(exp, connection);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }
    
    @Override
    public void finaliseExperiment(Experiment exp) throws Exception
    {
        Connection connection = null;
          try {
              connection = dbCon.getConnection(Connection.TRANSACTION_READ_COMMITTED);
          } catch (Exception ex) {
              log.error("Unable to finalise experiment, because a connection to the database cannot be made: " + ex.getMessage(), ex);
              throw new RuntimeException("Unable to finalise experiment, because a connection to the database cannot be made: " + ex.getMessage(), ex);
          }
          try {
              ExperimentDAOHelper.finaliseExperiment(exp, connection);
          } catch (Exception ex) {
              throw ex;
          } finally {
              if (DBUtil.isConnected(connection))
                  connection.close();
          }
    }
    
    @Override
    public Experiment getExperiment(UUID expUUID, boolean withSubClasses) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get experiment, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get experiment, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return ExperimentDAOHelper.getExperiment(expUUID, withSubClasses, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }
    
    @Override
    public Set<Experiment> getExperiments(boolean withSubClasses) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get experiments, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get experiments, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return ExperimentDAOHelper.getExperiments(withSubClasses, connection);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }
    
    
    //--------------------------- ENTITY -------------------------------------//
    
    
    @Override
    public void saveEntity(Entity entity) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection(Connection.TRANSACTION_READ_COMMITTED);
        } catch (Exception ex) {
            log.error("Unable to save entity, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to save entity, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            EntityDAOHelper.saveEntity(entity, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }

    @Override
    public Entity getEntity(UUID entityUUID, boolean withAttributes) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get entity, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get entity, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return EntityDAOHelper.getEntity(entityUUID, withAttributes, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }

    @Override
    public Set<Entity> getEntities(boolean withAttributes) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get entities, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get entities, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return EntityDAOHelper.getEntities(withAttributes, connection);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }

    @Override
    public Set<Entity> getEntitiesForExperiment(UUID expUUID, boolean withAttributes) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get entities for experiment, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get entities for experiment, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return EntityDAOHelper.getEntitiesForExperiment(expUUID, withAttributes, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }
    
    @Override
    public Set<Entity> getEntitiesForMetricGenerator(UUID mGenUUID, boolean withAttributes) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get entities for metric generator, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get entities for metric generator, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return EntityDAOHelper.getEntitiesForMetricGenerator(mGenUUID, withAttributes, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }

    
    //------------------------- ATTRIBUTE ------------------------------------//
    
    @Override
    public void saveAttribute(Attribute attrib) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to save attribute, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to save attribute, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            AttributeDAOHelper.saveAttribute(attrib, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }

    @Override
    public Attribute getAttribute(UUID attribUUID) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get attribute, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get attribute, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return AttributeDAOHelper.getAttribute(attribUUID, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }
    
    @Override
    public Set<Attribute> getAttributesForEntity(UUID entityUUID) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get attributes for entity, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get attributes for entity, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return AttributeDAOHelper.getAttributesForEntity(entityUUID, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }
    
    
    //---------------------- METRIC GENERATOR --------------------------------//
    
    
    @Override
    public void saveMetricGenerator(MetricGenerator metricGen, UUID experimentUUID) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection(Connection.TRANSACTION_READ_COMMITTED);
        } catch (Exception ex) {
            log.error("Unable to save metric generator, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to save metric generator, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            MetricGeneratorDAOHelper.saveMetricGenerator(metricGen, experimentUUID, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }

    @Override
    public MetricGenerator getMetricGenerator(UUID metricGenUUID, boolean withSubClasses) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get metric generator, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get metric generator, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return MetricGeneratorDAOHelper.getMetricGenerator(metricGenUUID, withSubClasses, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }

    @Override
    public Set<MetricGenerator> getMetricGenerators(boolean withSubClasses) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get metric generators, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get metric generators, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return MetricGeneratorDAOHelper.getMetricGenerators(withSubClasses, connection);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }

    @Override
    public Set<MetricGenerator> getMetricGeneratorsForExperiment(UUID expUUID, boolean withSubClasses) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get metric generators for experiment, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get metric generators for experiment, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return MetricGeneratorDAOHelper.getMetricGeneratorsForExperiment(expUUID, withSubClasses, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }
    
    
    //------------------------- METRIC GROUP ---------------------------------//
    
    
    @Override
    public void saveMetricGroup(MetricGroup metricGroup) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection(Connection.TRANSACTION_READ_COMMITTED);
        } catch (Exception ex) {
            log.error("Unable to save metric group, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to save metric group, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            MetricGroupDAOHelper.saveMetricGroup(metricGroup, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }

    @Override
    public MetricGroup getMetricGroup(UUID metricGroupUUID, boolean withSubClasses) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get metric group, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get metric group, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return MetricGroupDAOHelper.getMetricGroup(metricGroupUUID, withSubClasses, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }

    @Override
    public Set<MetricGroup> getMetricGroupsForMetricGenerator(UUID metricGenUUID, boolean withSubClasses) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get metric group for metric generator, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get metric group for metric generator, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return MetricGroupDAOHelper.getMetricGroupsForMetricGenerator(metricGenUUID, withSubClasses, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }
    
    
    //------------------------ MEASUREMENT SET -------------------------------//
    
    
    @Override
    public void saveMeasurementSet(MeasurementSet measurementSet) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection(Connection.TRANSACTION_READ_COMMITTED);
        } catch (Exception ex) {
            log.error("Unable to save measurement set, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to save measurement set, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            MeasurementSetDAOHelper.saveMeasurementSet(measurementSet, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }

    @Override
    public MeasurementSet getMeasurementSet(UUID measurementSetUUID, boolean withMetric) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get measurement set, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get measurement set, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return MeasurementSetDAOHelper.getMeasurementSet(measurementSetUUID, withMetric, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }

    @Override
    public Set<MeasurementSet> getMeasurementSetForMetricGroup(UUID metricGroupUUID, boolean withMetric) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get measurement set for metric group, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get measurement set for metric group, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return MeasurementSetDAOHelper.getMeasurementSetForMetricGroup(metricGroupUUID, withMetric, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }
    
    
    //---------------------------- METRIC ------------------------------------//
    
    
    @Override
    public void saveMetric(Metric metric) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection(Connection.TRANSACTION_READ_COMMITTED);
        } catch (Exception ex) {
            log.error("Unable to save metric, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to save metric, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            MetricDAOHelper.saveMetric(metric, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }
    
    @Override
    public Metric getMetric(UUID metricUUID) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get metric, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get metric, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return MetricDAOHelper.getMetric(metricUUID, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }
    
    @Override
    public Metric getMetricForMeasurementSet(UUID measurementSetUUID) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get metric for measurement set, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get metric for measurement set, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return MetricDAOHelper.getMetricForMeasurementSet(measurementSetUUID, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }
    
    
    //------------------------- MEASUREMENT ----------------------------------//
    
    
    @Override
    public void saveMeasurement(Measurement measurement) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection(Connection.TRANSACTION_READ_COMMITTED);
        } catch (Exception ex) {
            log.error("Unable to save measurement, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to save measurement, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        
        try {
            MeasurementDAOHelper.saveMeasurement(measurement, connection);
        } catch (Exception ex) {
            throw ex;
        } finally {
            try { connection.close(); } catch (Exception ex) { log.error("Failed to save measurement: ", ex); }
        }
    }
    
    @Override
    public void saveMeasurementsForSet(Set<Measurement> measurements, UUID mSetUUID) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection(Connection.TRANSACTION_READ_COMMITTED);
            //log.debug("Starting transaction");
            //connection.setAutoCommit(false);
        } catch (Exception ex) {
            log.error("Unable to save measurements for set, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to save measurements for set, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        
        //boolean exception = false;
        try {
            MeasurementDAOHelper.saveMeasurementsForSet(measurements, mSetUUID, connection);
        } catch (Exception ex) {
            //exception = true;
            throw ex;
        } finally {
            /*if (exception) {
                log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                connection.rollback();
            }
            else {
                log.debug("Committing the transaction and closing the connection");
                connection.commit();
            }*/
            try { connection.close(); } catch (Exception ex) { log.error("Failed to save measurement set: ", ex);}
        }
    }
    
    @Override
    public Measurement getMeasurement(UUID measurementUUID) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get measurement, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get measurement, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return MeasurementDAOHelper.getMeasurement(measurementUUID, connection);
        } catch (Exception ex) {
            throw ex;
        } finally {
            try { connection.close(); } catch (Exception ex) { log.error("Failed to get measurement: ", ex); }
        }
    }
    
    @Override
    public void setSyncFlagForAMeasurement(UUID measurementUUID, boolean syncFlag) throws IllegalArgumentException, Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection(Connection.TRANSACTION_READ_COMMITTED);
        } catch (Exception ex) {
            log.error("Unable to set sync flag for measurement, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to set sync flag measurement, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        
        try {
            MeasurementDAOHelper.setSyncFlagForAMeasurement(measurementUUID, syncFlag, connection);
        } catch (Exception ex) {
            throw ex;
        } finally {
            try { connection.close(); } catch (Exception ex) { log.error("Failed to sync measurement: ", ex); }
        }
    }

    @Override
    public void setSyncFlagForMeasurements(Set<UUID> measurements, boolean syncFlag) throws IllegalArgumentException, Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection(Connection.TRANSACTION_READ_COMMITTED);
            //log.debug("Starting transaction");
            //connection.setAutoCommit(false);
        } catch (Exception ex) {
            log.error("Unable to set sync flag for measurements, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to set sync flag measurements, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        
        //boolean exception = false;
        try {
            MeasurementDAOHelper.setSyncFlagForMeasurements(measurements, syncFlag, connection);
        } catch (Exception ex) {
            //exception = true;
            throw ex;
        } finally {
            /*if (exception) {
                log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                connection.rollback();
            }
            else {
                log.debug("Committing the transaction and closing the connection");
                connection.commit();
            }*/
            try { connection.close(); } catch (Exception ex) { log.error("Failed to close db connection: ", ex); }
        }
    }
    
    @Override
    public void deleteSynchronisedMeasurements() throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection(Connection.TRANSACTION_READ_COMMITTED);
            //log.debug("Starting transaction");
            //connection.setAutoCommit(false);
        } catch (Exception ex) {
            log.error("Unable to delete synchronised measurements, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to delete synchronised measurements, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        
        //boolean exception = false;
        try {
            MeasurementDAOHelper.deleteSynchronisedMeasurements(connection);
        } catch (Exception ex) {
            //exception = true;
            throw ex;
        } finally {
            /*if (exception) {
                log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                connection.rollback();
            }
            else {
                log.debug("Committing the transaction and closing the connection");
                connection.commit();
            }*/
            try { connection.close(); } catch (Exception ex) { log.error("Failed to close db connection: ", ex); }
        }
    }

    @Override
    public void deleteMeasurements(Set<UUID> measurements) throws IllegalArgumentException, Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection(Connection.TRANSACTION_READ_COMMITTED);
            //log.debug("Starting transaction");
            //connection.setAutoCommit(false);
        } catch (Exception ex) {
            log.error("Unable to delete measurements, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to delete measurements, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        
        //boolean exception = false;
        try {
            MeasurementDAOHelper.deleteMeasurements(measurements, connection);
        } catch (Exception ex) {
            //exception = true;
            throw ex;
        } finally {
            /*if (exception) {
                log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                connection.rollback();
            }
            else {
                log.debug("Committing the transaction and closing the connection");
                connection.commit();
            }*/
            try { connection.close(); } catch (Exception ex) { log.error("Failed to close db connection: ", ex); }
        }
    }
    
    //---------------------------- REPORT ------------------------------------//
    
    
    @Override
    public void saveReport(Report report, boolean saveMeasurements) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection(Connection.TRANSACTION_READ_COMMITTED);
            //log.debug("Starting transaction");
            //connection.setAutoCommit(false);
        } catch (Exception ex) {
            log.error("Unable to save report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to save report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        //boolean exception = false;
        try {
            ReportDAOHelper.saveReport(report, saveMeasurements, connection);
        } catch (Exception ex) {
            //exception = true;
            throw ex;
        } finally {
            /*if (exception) {
                log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                connection.rollback();
            }
            else {
                log.debug("Committing the transaction and closing the connection");
                connection.commit();
            }*/
            try { connection.close(); } catch (Exception ex) { log.error("Failed to close db connection: ", ex); }
        }
    }
    
    @Override
    public void saveMeasurements(Report report) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection(Connection.TRANSACTION_READ_COMMITTED);
            //log.debug("Starting transaction");
            //connection.setAutoCommit(false);
        } catch (Exception ex) {
            log.error("Unable to save measurements for report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to save measurements for report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        //boolean exception = false;
        try {
            ReportDAOHelper.saveMeasurementsForReport(report, connection);
        } catch (Exception ex) {
            //exception = true;
            throw ex;
        } finally {
            /*if (exception) {
                log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                connection.rollback();
            }
            else {
                log.debug("Committing the transaction and closing the connection");
                connection.commit();
            }*/
            try { connection.close(); } catch (Exception ex) { log.error("Failed to close db connection: ", ex); }
        }
    }

    @Override
    public Report getReport(UUID reportUUID, boolean withMeasurements) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return ReportDAOHelper.getReport(reportUUID, withMeasurements, connection);
        } catch (Exception ex) {
            throw ex;
        } finally {
            try { connection.close(); } catch (Exception ex) { log.error("Failed to close db connection: ", ex); }
        }
    }

    @Override
    public Report getReportForLatestMeasurement(UUID measurementSetUUID, boolean withMeasurements) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return ReportDAOHelper.getReportForLatestMeasurement(measurementSetUUID, withMeasurements, connection);
        } catch (Exception ex) {
            throw ex;
        } finally {
            try { connection.close(); } catch (Exception ex) { log.error("Failed to close db connection: ", ex); }
        }
    }

    @Override
    public Report getReportForAllMeasurements(UUID measurementSetUUID, boolean withMeasurements) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return ReportDAOHelper.getReportForAllMeasurements(measurementSetUUID, withMeasurements, connection);
        } catch (Exception ex) {
            throw ex;
        } finally {
            try { connection.close(); } catch (Exception ex) { log.error("Failed to close db connection: ", ex);  }
        }
    }
    
    @Override
    public Report getReportForTailMeasurements(UUID measurementSetID, Date tailDate, int count, boolean withMeasurements) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return ReportDAOHelper.getReportForTailMeasurements(measurementSetID, tailDate, count, withMeasurements, connection);
        } catch (Exception ex) {
            throw ex;
        } finally {
            try { connection.close(); } catch (Exception ex) { log.error("Failed to close db connection: ", ex);  }
        }
    }

    @Override
    public Report getReportForMeasurementsFromDate(UUID measurementSetUUID, Date fromDate, boolean withMeasurements) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return ReportDAOHelper.getReportForMeasurementsAfterDate(measurementSetUUID, fromDate, withMeasurements, connection);
        } catch (Exception ex) {
            throw ex;
        } finally {
            try { connection.close(); } catch (Exception ex) { log.error("Failed to close db connection: ", ex);  }
        }
    }
    
    @Override
    public Report getReportForMeasurementsForTimePeriod(UUID measurementSetUUID, Date fromDate, Date toDate, boolean withMeasurements) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return ReportDAOHelper.getReportForMeasurementsForTimePeriod(measurementSetUUID, fromDate, toDate, withMeasurements, connection);
        } catch (Exception ex) {
            throw ex;
        } finally {
            try { connection.close(); } catch (Exception ex) { log.error("Failed to close db connection: ", ex);  }
        }
    }

    @Override
    public Report getReportForUnsyncedMeasurementsFromDate(UUID measurementSetUUID, Date fromDate, int numMeasurements, boolean withMeasurements) throws IllegalArgumentException, NoDataException, Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return ReportDAOHelper.getReportForUnsyncedMeasurementsAfterDate(measurementSetUUID, fromDate, numMeasurements, withMeasurements, connection);
        } catch (Exception ex) {
            throw ex;
        } finally {
            try { connection.close(); } catch (Exception ex) { log.error("Failed to close db connection: ", ex); }
        }
    }
    
    @Override
    public void setReportMeasurementsSyncFlag(UUID reportUUID, boolean syncFlag) throws IllegalArgumentException, Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection(Connection.TRANSACTION_READ_COMMITTED);
            log.debug("Starting transaction");
            connection.setAutoCommit(false);
        } catch (Exception ex) {
            log.error("Unable to set sync flag for measurements of report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to set sync flag for measurements of report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        boolean exception = false;
        try {
            ReportDAOHelper.setReportMeasurementsSyncFlag(reportUUID, syncFlag, connection);
        } catch (Exception ex) {
            exception = true;
            throw ex;
        } finally {
            if (exception) {
                log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                connection.rollback();
            }
            else {
                log.debug("Committing the transaction and closing the connection");
                connection.commit();
            }
            try { connection.close(); } catch (Exception ex) { log.error("Failed to close db connection: ", ex); }
        }
    }

    @Override
    public void deleteReport(UUID reportUUID, boolean withMeasurements) throws IllegalArgumentException, Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection(Connection.TRANSACTION_READ_COMMITTED);
            log.debug("Starting transaction");
            connection.setAutoCommit(false);
        } catch (Exception ex) {
            log.error("Unable to delete report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to delete report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        boolean exception = false;
        try {
            ReportDAOHelper.deleteReport(reportUUID, withMeasurements, connection);
        } catch (Exception ex) {
            exception = true;
            throw ex;
        } finally {
            if (exception) {
                log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                connection.rollback();
            }
            else {
                log.debug("Committing the transaction and closing the connection");
                connection.commit();
            }
            try { connection.close(); } catch (Exception ex) { log.error("Failed to close db connection: ", ex); }
        }
    }
    
    
    //---------------------------- OTHER -------------------------------------//
    
    public boolean isDatabaseSetUpAndAccessible()
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.debug("Connection to the database cannot be made: " + ex.getMessage(), ex);
            return false;
        }
        
        try {
            String query = "select relname from pg_stat_user_tables WHERE schemaname='public'";
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            // check if we got anything from the query.
            if (!rs.next())
            {
                log.debug("Query for table schema returned nothing");
                return false;
            }
            
            // create a map of tables and check against the result set
            Map<String, Boolean> tableNameMap = new HashMap<String, Boolean>();
            tableNameMap.put("entity", false);
            tableNameMap.put("attribute", false);
            tableNameMap.put("metricgroup", false);
            tableNameMap.put("measurement", false);
            tableNameMap.put("measurementset", false);
            tableNameMap.put("report", false);
            tableNameMap.put("metricgenerator", false);
            tableNameMap.put("metric", false);
            tableNameMap.put("metrictype", false);
            tableNameMap.put("report_measurement", false);
            tableNameMap.put("metricgenerator_entity", false);
            tableNameMap.put("experiment", false);
            int count = 0;
            do {
                count++;
                tableNameMap.put(rs.getString(1), true);
            } while (rs.next());
            
            if (count < 12)
            {
                log.debug("Less than 12 tables, so not correct!");
                return false;
            }
            
            boolean valid = true;
            for (String key : tableNameMap.keySet())
            {
                if (!tableNameMap.get(key))
                {
                    log.debug("Table '" + key + "' not found");
                    valid = false;
                }
            }
            return valid;
        } catch (SQLException ex) {
            log.debug("Error when executing database query to get table schema: " + ex.toString(), ex);
            return false;
        } finally {
            try { connection.close(); } catch (SQLException ex){ log.error("Failed to close db connection: ", ex); }
        }
    }
    
    
    public void clearMetricsDatabase() throws Exception
    {
        log.debug("Clearing the database!");
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
            log.debug("Starting transaction");
            connection.setAutoCommit(false);
        } catch (Exception ex) {
            log.error("Unable to clear the database, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to clear the database, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        
        // experiments (cascade will delete metric generators etc
        try {
            ExperimentDAOHelper.deleteAllExperiments(connection, false);
        } catch (Exception ex) {
            log.debug("Rolling back delete transaction and closing the DB connection");
            connection.rollback();
            connection.close();
            throw ex;
        }
        
        // entities (in case some entities were created not in a metric group)
        try {
            EntityDAOHelper.deleteAllEntities(connection, false);
        } catch (Exception ex) {
            log.debug("Rolling back delete transaction and closing the DB connection");
            connection.rollback();
            connection.close();
            throw ex;
        }
        
        // metrics
        try {
            MetricDAOHelper.deleteAllMetrics(connection, false);
        } catch (Exception ex) {
            log.debug("Rolling back delete transaction and closing the DB connection");
            connection.rollback();
            connection.close();
            throw ex;
        }
        
        log.debug("Committing delete transaction and closing the DB connection");
        connection.commit();
        connection.close();
    }

    //-------------------------- DEPRECATED ----------------------------------//

    @Override
    public Report getReportWithData(UUID reportUUID) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return ReportDAOHelper.getReport(reportUUID, true, connection);
        } catch (Exception ex) {
            throw ex;
        } finally {
            try { connection.close(); } catch (Exception ex) { log.error("Failed to close db connection: ", ex);  }
        }
    }
    
    @Override
    public Report getReportForLatestMeasurementWithData(UUID measurementSetUUID) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return ReportDAOHelper.getReportForLatestMeasurement(measurementSetUUID, true, connection);
        } catch (Exception ex) {
            throw ex;
        } finally {
            try { connection.close(); } catch (Exception ex) { log.error("Failed to close db connection: ", ex); }
        }
    }
    
    @Override
    public Report getReportForAllMeasurementsWithData(UUID measurementSetUUID) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return ReportDAOHelper.getReportForAllMeasurements(measurementSetUUID, true, connection);
        } catch (Exception ex) {
            throw ex;
        } finally {
            try { connection.close(); } catch (Exception ex) { log.error("Failed to close db connection: ", ex); }
        }
    }
    
    @Override
    public Report getReportForMeasurementsAfterDateWithData(UUID measurementSetUUID, Date fromDate) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return ReportDAOHelper.getReportForMeasurementsAfterDate(measurementSetUUID, fromDate, true, connection);
        } catch (Exception ex) {
            throw ex;
        } finally {
            try { connection.close(); } catch (Exception ex) { log.error("Failed to close db connection: ", ex); }
        }
    }
    
    @Override
    public Report getReportForMeasurementsForTimePeriodWithData(UUID measurementSetUUID, Date fromDate, Date toDate) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return ReportDAOHelper.getReportForMeasurementsForTimePeriod(measurementSetUUID, fromDate, toDate, true, connection);
        } catch (Exception ex) {
            throw ex;
        } finally {
            try { connection.close(); } catch (Exception ex) { log.error("Failed to close db connection: ", ex); }
        }
    }
    
}
