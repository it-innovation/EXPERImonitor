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
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.mon.dao;

import java.sql.Connection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Metric;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.EDMUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DatabaseConnector;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DatabaseType;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IEntityDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMeasurementDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMeasurementSetDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMetricDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMetricGroupDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IReportDAO;

/**
 * A DAO class for the Experiment Data Manager.
 * 
 * @author Vegard Engen
 */
public class ExperimentDataManagerDAO implements IExperimentDAO, IEntityDAO, IMetricGeneratorDAO, IMetricGroupDAO, IMeasurementSetDAO, IMetricDAO, IMeasurementDAO, IReportDAO
{
    private Map<String,String> configs;
    private DatabaseConnector dbCon;
    
    private static Logger log = Logger.getLogger(ExperimentDataManagerDAO.class);
    
    /**
     * Experiment Data Manager DAO constructor, which reads the configuration file
     * and sets up a DatabaseConnector object to access the database for
     * storing and retrieving data.
     * 
     * @throws Exception 
     */
    public ExperimentDataManagerDAO() throws Exception
    {
        try {
            configs = EDMUtil.getConfigs();
        } catch (Exception ex){
            log.error("Caught an exception when reading the config file: " + ex.getMessage());
            throw new RuntimeException("Caught an exception when reading the config file: " + ex.getMessage(), ex);
        }
        
        try {
            dbCon = new DatabaseConnector(configs.get("dbURL"), configs.get("dbName"), configs.get("dbUsername"),configs.get("dbPassword"), DatabaseType.fromValue(configs.get("dbType")));
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
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to save experiment, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to save experiment, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            ExperimentHelper.saveExperiment(exp, connection);
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
            return ExperimentHelper.getExperiment(expUUID, withSubClasses, connection, true);
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
            return ExperimentHelper.getExperiments(withSubClasses, connection);
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
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to save entity, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to save entity, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            EntityHelper.saveEntity(entity, connection, true);
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
            return EntityHelper.getEntity(entityUUID, withAttributes, connection, true);
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
            return EntityHelper.getEntities(withAttributes, connection);
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
            return EntityHelper.getEntitiesForExperiment(expUUID, withAttributes, connection, true);
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
            return EntityHelper.getEntitiesForMetricGenerator(mGenUUID, withAttributes, connection, true);
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
            AttributeHelper.saveAttribute(attrib, connection, true);
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
            return AttributeHelper.getAttribute(attribUUID, connection, true);
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
            return AttributeHelper.getAttributesForEntity(entityUUID, connection, true);
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
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to save metric generator, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to save metric generator, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            MetricGeneratorHelper.saveMetricGenerator(metricGen, experimentUUID, connection, true);
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
            return MetricGeneratorHelper.getMetricGenerator(metricGenUUID, withSubClasses, connection, true);
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
            return MetricGeneratorHelper.getMetricGenerators(withSubClasses, connection);
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
            return MetricGeneratorHelper.getMetricGeneratorsForExperiment(expUUID, withSubClasses, connection, true);
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
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to save metric group, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to save metric group, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            MetricGroupHelper.saveMetricGroup(metricGroup, connection, true);
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
            return MetricGroupHelper.getMetricGroup(metricGroupUUID, withSubClasses, connection, true);
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
            return MetricGroupHelper.getMetricGroupsForMetricGenerator(metricGenUUID, withSubClasses, connection, true);
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
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to save measurement set, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to save measurement set, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            MeasurementSetHelper.saveMeasurementSet(measurementSet, connection, true);
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
            return MeasurementSetHelper.getMeasurementSet(measurementSetUUID, withMetric, connection, true);
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
            return MeasurementSetHelper.getMeasurementSetForMetricGroup(metricGroupUUID, withMetric, connection, true);
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
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to save metric, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to save metric, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            MetricHelper.saveMetric(metric, connection, true);
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
            return MetricHelper.getMetric(metricUUID, connection, true);
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
            return MetricHelper.getMetricForMeasurementSet(measurementSetUUID, connection, true);
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
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to save measurement, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to save measurement, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            MeasurementHelper.saveMeasurement(measurement, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }
    
    @Override
    public void saveMeasurementsForSet(Set<Measurement> measurements, UUID mSetUUID) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to save measurements for set, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to save measurements for set, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            MeasurementHelper.saveMeasurementsForSet(measurements, mSetUUID, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
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
            return MeasurementHelper.getMeasurement(measurementUUID, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }
    
    
    //---------------------------- REPORT ------------------------------------//
    
    
    @Override
    public void saveReport(Report report) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to save report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to save report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            ReportHelper.saveReport(report, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }

    @Override
    public Report getReport(UUID reportUUID) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return ReportHelper.getReport(reportUUID, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }

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
            return ReportHelper.getReportWithData(reportUUID, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }

    @Override
    public Report getReportForLatestMeasurement(UUID measurementSetUUID) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return ReportHelper.getReportForLatestMeasurement(measurementSetUUID, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
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
            return ReportHelper.getReportForLatestMeasurementWithData(measurementSetUUID, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }

    @Override
    public Report getReportForAllMeasurements(UUID measurementSetUUID) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return ReportHelper.getReportForAllMeasurements(measurementSetUUID, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
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
            return ReportHelper.getReportForAllMeasurementsWithData(measurementSetUUID, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }

    @Override
    public Report getReportForMeasurementsAfterDate(UUID measurementSetUUID, Date fromDate) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return ReportHelper.getReportForMeasurementsAfterDate(measurementSetUUID, fromDate, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
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
            return ReportHelper.getReportForMeasurementsAfterDateWithData(measurementSetUUID, fromDate, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }

    @Override
    public Report getReportForMeasurementsForTimePeriod(UUID measurementSetUUID, Date fromDate, Date toDate) throws Exception
    {
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to get report, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        try {
            return ReportHelper.getReportForMeasurementsForTimePeriod(measurementSetUUID, fromDate, toDate, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
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
            return ReportHelper.getReportForMeasurementsForTimePeriodWithData(measurementSetUUID, fromDate, toDate, connection, true);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (DBUtil.isConnected(connection))
                connection.close();
        }
    }
    
    
    //---------------------------- OTHER -------------------------------------//
    
    
    public void clearMetricsDatabase() throws Exception
    {
        log.debug("Clearing the database!");
        Connection connection = null;
        try {
            connection = dbCon.getConnection();
        } catch (Exception ex) {
            log.error("Unable to clear the database, because a connection to the database cannot be made: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to clear the database, because a connection to the database cannot be made: " + ex.getMessage(), ex);
        }
        log.debug("Starting transaction");
        connection.setAutoCommit(false);
        
        // experiments (cascade will delete metric generators etc
        try {
            ExperimentHelper.deleteAllExperiments(connection, false);
        } catch (Exception ex) {
            log.debug("Rolling back delete transaction and closing the DB connection");
            connection.rollback();
            connection.close();
            throw ex;
        }
        
        // entities (in case some entities were created not in a metric group)
        try {
            EntityHelper.deleteAllEntities(connection, false);
        } catch (Exception ex) {
            log.debug("Rolling back delete transaction and closing the DB connection");
            connection.rollback();
            connection.close();
            throw ex;
        }
        
        // metrics
        try {
            MetricHelper.deleteAllMetrics(connection, false);
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
}
