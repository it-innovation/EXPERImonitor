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
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.dao;

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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DatabaseConnector;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DatabaseType;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IEntityDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMeasurementDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMeasurementSetDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMetricDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMetricGroupDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IReportDAO;

/**
 * A DAO class for the Experiment Data Manager.
 * 
 * @author Vegard Engen
 */
public class ExperimentDataManagerDAO implements IExperimentDAO, IEntityDAO, IMetricGeneratorDAO, IMetricGroupDAO, IMeasurementSetDAO, IMetricDAO, IMeasurementDAO, IReportDAO
{
    private Map<String,String> configs;
    private DatabaseConnector dbCon;
    
    static Logger log = Logger.getLogger(ExperimentDataManagerDAO.class);
    
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
        } catch (Exception ex) {
            log.error("Failed to create DatabaseConnector: " + ex.getMessage(), ex);
            throw new RuntimeException("Failed to create DatabaseConnector: " + ex.getMessage(), ex);
        }
    }
    
    
    //-------------------------- EXPERIMENT ----------------------------------//
    
    
    @Override
    public void saveExperiment(Experiment exp) throws Exception
    {
        ExperimentHelper.saveExperiment(exp, dbCon);
    }
    
    @Override
    public Experiment getExperiment(UUID expUUID, boolean withSubClasses) throws Exception
    {
        return ExperimentHelper.getExperiment(expUUID, withSubClasses, dbCon, true);
    }
    
    @Override
    public Set<Experiment> getExperiments(boolean withSubClasses) throws Exception
    {
        return ExperimentHelper.getExperiments(withSubClasses, dbCon);
    }
    
    
    //--------------------------- ENTITY -------------------------------------//
    
    
    @Override
    public void saveEntity(Entity entity) throws Exception
    {
        EntityHelper.saveEntity(entity, dbCon, true);
    }

    @Override
    public Entity getEntity(UUID entityUUID, boolean withAttributes) throws Exception
    {
        return EntityHelper.getEntity(entityUUID, withAttributes, dbCon, true);
    }

    @Override
    public Set<Entity> getEntities(boolean withAttributes) throws Exception
    {
        return EntityHelper.getEntities(withAttributes, dbCon);
    }

    @Override
    public Set<Entity> getEntitiesForExperiment(UUID expUUID, boolean withAttributes) throws Exception
    {
        return EntityHelper.getEntitiesForExperiment(expUUID, withAttributes, dbCon);
    }
    
    @Override
    public Set<Entity> getEntitiesForMetricGenerator(UUID mGenUUID, boolean withAttributes) throws Exception
    {
        return EntityHelper.getEntitiesForMetricGenerator(mGenUUID, withAttributes, dbCon, true);
    }

    
    //------------------------- ATTRIBUTE ------------------------------------//
    
    @Override
    public void saveAttribute(Attribute attrib) throws Exception
    {
        AttributeHelper.saveAttribute(attrib, dbCon, true);
    }

    @Override
    public Attribute getAttribute(UUID attribUUID) throws Exception
    {
        return AttributeHelper.getAttribute(attribUUID, dbCon, true);
    }
    
    @Override
    public Set<Attribute> getAttributesForEntity(UUID entityUUID) throws Exception
    {
        return AttributeHelper.getAttributesForEntity(entityUUID, dbCon, true);
    }
    
    
    //---------------------- METRIC GENERATOR --------------------------------//
    
    
    @Override
    public void saveMetricGenerator(MetricGenerator metricGen, UUID experimentUUID) throws Exception
    {
        // flag set to check for experiment in the validation process
        MetricGeneratorHelper.saveMetricGenerator(metricGen, experimentUUID, dbCon, true);
    }

    @Override
    public MetricGenerator getMetricGenerator(UUID metricGenUUID, boolean withSubClasses) throws Exception
    {
        return MetricGeneratorHelper.getMetricGenerator(metricGenUUID, withSubClasses, dbCon, true);
    }

    @Override
    public Set<MetricGenerator> getMetricGenerators(boolean withSubClasses) throws Exception
    {
        return MetricGeneratorHelper.getMetricGenerators(withSubClasses, dbCon);
    }

    @Override
    public Set<MetricGenerator> getMetricGeneratorsForExperiment(UUID expUUID, boolean withSubClasses) throws Exception
    {
        return MetricGeneratorHelper.getMetricGeneratorsForExperiment(expUUID, withSubClasses, dbCon, true);
    }
    
    
    //------------------------- METRIC GROUP ---------------------------------//
    
    
    @Override
    public void saveMetricGroup(MetricGroup metricGroup) throws Exception
    {
        MetricGroupHelper.saveMetricGroup(metricGroup, dbCon, true);
    }

    @Override
    public MetricGroup getMetricGroup(UUID metricGroupUUID, boolean withSubClasses) throws Exception
    {
        return MetricGroupHelper.getMetricGroup(metricGroupUUID, withSubClasses, dbCon, true);
    }

    @Override
    public Set<MetricGroup> getMetricGroupsForMetricGenerator(UUID metricGenUUID, boolean withSubClasses) throws Exception
    {
        return MetricGroupHelper.getMetricGroupsForMetricGenerator(metricGenUUID, withSubClasses, dbCon, true);
    }
    
    
    //------------------------ MEASUREMENT SET -------------------------------//
    
    
    @Override
    public void saveMeasurementSet(MeasurementSet measurementSet) throws Exception
    {
        MeasurementSetHelper.saveMeasurementSet(measurementSet, dbCon, true);
    }

    @Override
    public MeasurementSet getMeasurementSet(UUID measurementSetUUID, boolean withMetric) throws Exception
    {
        return MeasurementSetHelper.getMeasurementSet(measurementSetUUID, withMetric, dbCon, true);
    }

    @Override
    public Set<MeasurementSet> getMeasurementSetForMetricGroup(UUID metricGroupUUID, boolean withMetric) throws Exception
    {
        return MeasurementSetHelper.getMeasurementSetForMetricGroup(metricGroupUUID, withMetric, dbCon, true);
    }
    
    
    //---------------------------- METRIC ------------------------------------//
    
    
    @Override
    public void saveMetric(Metric metric) throws Exception
    {
        MetricHelper.saveMetric(dbCon, metric, true);
    }
    
    @Override
    public Metric getMetric(UUID metricUUID) throws Exception
    {
        return MetricHelper.getMetric(dbCon, metricUUID, true);
    }
    
    @Override
    public Metric getMetricForMeasurementSet(UUID measurementSetUUID) throws Exception
    {
        return MetricHelper.getMetricForMeasurementSet(dbCon, measurementSetUUID, true);
    }
    
    
    //------------------------- MEASUREMENT ----------------------------------//
    
    
    @Override
    public void saveMeasurement(Measurement measurement) throws Exception
    {
        MeasurementHelper.saveMeasurement(measurement, dbCon, true);
    }
    
    @Override
    public void saveMeasurementsForSet(Set<Measurement> measurements, UUID mSetUUID) throws Exception
    {
        MeasurementHelper.saveMeasurementsForSet(measurements, mSetUUID, dbCon, true);
    }
    
    @Override
    public Measurement getMeasurement(UUID measurementUUID) throws Exception
    {
        return MeasurementHelper.getMeasurement(measurementUUID, dbCon, true);
    }
    
    
    //---------------------------- REPORT ------------------------------------//
    
    
    @Override
    public void saveReport(Report report) throws Exception
    {
        ReportHelper.saveReport(report, dbCon, true);
    }

    @Override
    public Report getReport(UUID reportUUID) throws Exception
    {
        return ReportHelper.getReport(reportUUID, dbCon);
    }

    @Override
    public Report getReportWithData(UUID reportUUID) throws Exception
    {
        return ReportHelper.getReportWithData(reportUUID, dbCon);
    }

    @Override
    public Report getReportForLatestMeasurement(UUID measurementSetUUID) throws Exception
    {
        return ReportHelper.getReportForLatestMeasurement(measurementSetUUID, dbCon);
    }

    @Override
    public Report getReportForLatestMeasurementWithData(UUID measurementSetUUID) throws Exception
    {
        return ReportHelper.getReportForLatestMeasurementWithData(measurementSetUUID, dbCon);
    }

    @Override
    public Report getReportForAllMeasurements(UUID measurementSetUUID) throws Exception
    {
        return ReportHelper.getReportForAllMeasurements(measurementSetUUID, dbCon);
    }

    @Override
    public Report getReportForAllMeasurementsWithData(UUID measurementSetUUID) throws Exception
    {
        return ReportHelper.getReportForAllMeasurementsWithData(measurementSetUUID, dbCon);
    }

    @Override
    public Report getReportForMeasurementsAfterDate(UUID measurementSetUUID, Date fromDate) throws Exception
    {
        return ReportHelper.getReportForMeasurementsAfterDate(measurementSetUUID, fromDate, dbCon);
    }

    @Override
    public Report getReportForMeasurementsAfterDateWithData(UUID measurementSetUUID, Date fromDate) throws Exception
    {
        return ReportHelper.getReportForMeasurementsAfterDateWithData(measurementSetUUID, fromDate, dbCon);
    }

    @Override
    public Report getReportForMeasurementsForTimePeriod(UUID measurementSetUUID, Date fromDate, Date toDate) throws Exception
    {
        return ReportHelper.getReportForMeasurementsForTimePeriod(measurementSetUUID, fromDate, toDate, dbCon);
    }

    @Override
    public Report getReportForMeasurementsForTimePeriodWithData(UUID measurementSetUUID, Date fromDate, Date toDate) throws Exception
    {
        return ReportHelper.getReportForMeasurementsForTimePeriodWithData(measurementSetUUID, fromDate, toDate, dbCon);
    }
}
