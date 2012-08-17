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
//      Created Date :          2012-08-13
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec;

import java.util.Date;
import java.util.Set;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

/**
 * Interface for the EDM.
 * 
 * OBS: the get methods will return all sub-classes except for measurements. To
 * get actual measurements for a measurement set, one of the specific getMeasurement(s)
 * methods need to be called, passing on the UUID of the measurement set (and
 * any other arguments for the respective method).
 * 
 * OBS: no delete methods yet.
 * OBS: no update methods yet.
 * 
 * @author Vegard Engen
 */
public interface IExperimentDataManager
{
    //-------------------------- EXPERIMENT ----------------------------------//
    
    /**
     * Save an experiment (must have a unique UUID).
     * @param exp The experiment instance to be saved (must have a unique UUID).
     * @throws Exception If there's a technical issue or an experiment with the same UUID already exists.
     */
    public void saveExperiment(Experiment exp) throws Exception;
    
    /**
     * Get an experiment instance according to an experiment UUID.
     * @param expUUID The UUID of the experiment.
     * @return An experiment instance with all sub-classes except for measurements.
     * @throws Exception If there's a technical issue or there is no experiment with the given UUID.
     */
    public Experiment getExperiment(UUID expUUID) throws Exception;
    
    /**
     * Get all existing experiments.
     * @return Empty set if no experiments exist.
     * @throws Exception If there's a technical issue.
     */
    public Set<Experiment> getExperiments() throws Exception;
    
    
    //--------------------------- ENTITY -------------------------------------//
    
    
    /**
     * Saves an entity instance, which must have a unique UUID. Any attributes that
     * the entity may have will also be saved.
     * @param ent The Entity instance to be saved (must have a unique UUID).
     * @throws Exception If there's a technical issue or an entity with the same UUID already exists.
     */
    public void saveEntity(Entity ent) throws Exception;
    
    /**
     * Get an entity instance from a UUID, which will include any attributes the
     * entity may have.
     * @param entityUUID The UUID of the entity.
     * @return An Entity instance.
     * @throws Exception If there's a technical issue or there is no entity with the given UUID.
     */
    public Entity getEntity(UUID entityUUID) throws Exception;
    
    /**
     * Get all the existing entities, if any.
     * @return Empty set if there are no entities.
     * @throws Exception If there's a technical issue.
     */
    public Set<Entity> getEntities() throws Exception;
    
    /**
     * Get all the entities for a specific experiment, if any.
     * @param expUUID The experiment UUID.
     * @return An empty set if no entities monitored in the given experiment exist.
     * @throws Exception If there's a technical issue or there is no experiment with the given UUID.
     */
    public Set<Entity> getEntitiesForExperiment(UUID expUUID) throws Exception;
    
    
    //------------------------- ATTRIBUTE ------------------------------------//
    
    
    /**
     * Saves an attribute instance, which must have the UUID of an already saved
     * Entity, and must have a unique UUID itself.
     * @param attrib The attribute instance that should be saved.
     * @throws Exception If there's a technical issue or an attribute with the same UUID already exists.
     */
    public void saveAttribute(Attribute attrib) throws Exception;
    
    /**
     * Get an attribute instance according to the UUID, if it exists.
     * @param attribUUID The attribute UUID.
     * @return An attribute instance, if it exists.
     * @throws Exception If there's a technical issue or there is no attribute with the given UUID.
     */
    public Attribute getAttribute(UUID attribUUID) throws Exception;
    
    /**
     * Get all attribute instances for an entity, according to the entity UUID.
     * @param entityUUID The entity UUID.
     * @return Empty set if no attributes exist for the entity.
     * @throws Exception If there's a technical issue or there is no entity with the given UUID.
     */
    public Set<Attribute> getAttributesForEntity(UUID entityUUID) throws Exception;
    
    
    //---------------------- METRIC GENERATOR --------------------------------//
    
    
    /**
     * Saves a metric generator instance, which must have a unique UUID. The 
     * experiment UUID is required, which must refer to an already existing
     * experiment.
     * 
     * Any sub-classes of the metric generator instance will also be saved (if
     * not null).
     * @param metricGen The metric generator instance to be saved.
     * @param experimentUUID The experiment UUID.
     * @throws Exception If there's a technical issue or a metric generator with the same UUID already exists.
     */
    public void saveMetricGenerator(MetricGenerator metricGen, UUID experimentUUID) throws Exception;
    
    // will include everything except for measurements, if available
    /**
     * Get a metric generator instance from a UUID. Will include all sub-classes
     * except for measurements, if available.
     * @param metricGenUUID The metric generator UUID.
     * @return A metric generator instance if it exists.
     * @throws Exception If there's a technical issue or there is no metric generator with the given UUID.
     */
    public MetricGenerator getMetricGenerator(UUID metricGenUUID) throws Exception;
    
    /**
     * Get all metric generators. Will include all sub-classes except for measurements,
     * if available.
     * @return Empty set if there are no metric generators.
     * @throws Exception If there's a technical issue.
     */
    public Set<MetricGenerator> getMetricGenerators() throws Exception;
    
    /**
     * Get all metric generators for an experiment. Will include all sub-classes
     * except for measurements, if available.
     * @param expUUID The experiment UUID.
     * @return Empty set if no metric generators exist for the experiment.
     * @throws Exception If there's a technical issue or there is no experiment with the given UUID.
     */
    public Set<MetricGenerator> getMetricGeneratorsForExperiment(UUID expUUID) throws Exception;
    
    
    //------------------------- METRIC GROUP ---------------------------------//
    
    
    /**
     * Save a metric group, which must have a unique UUID and refer to an existing
     * metric generator (by its UUID).
     * 
     * Any sub-classes of the metric group will also be saved (if not null).
     * @param metricGroup
     * @throws Exception If there's a technical issue or a metric group with the same UUID already exists.
     */
    public void saveMetricGroup(MetricGroup metricGroup) throws Exception;
    
    // will include everytrhing except for measurements, if available
    /**
     * Get the a metric group instance from a UUID. Will include all sub-classes
     * except for measurements, if available.
     * @param metricGroupUUID The metric group UUID.
     * @return A metric group instance, if it exists.
     * @throws Exception If there's a technical issue or there is no metric group with the given UUID.
     */
    public MetricGroup getMetricGroup(UUID metricGroupUUID) throws Exception;
    
    /**
     * Get all metric groups for a metric generator. Will include any sub-classes,
     * except for measurements, if available.
     * @param metricGenUUID The UUID of the metric generator.
     * @return A metric group instance, if it exists.
     * @throws Exception If there's a technical issue or there is no metric generator with the given UUID.
     */
    public Set<MetricGroup> getMetricGroupsForMetricGenerator(UUID metricGenUUID) throws Exception;
    
    
    //------------------------ MEASUREMENT SET -------------------------------//
    
    
    /**
     * Save a measurement set, which must have a unique UUID and refer to an 
     * existing metric group (by its UUID).
     * 
     * Any sub-classes of the measurement set will also be saved (if not null).
     * @param measurementSet The measurement set instance to be saved.
     * @throws Exception If there's a technical issue or a measurement set with the same UUID already exists.
     */
    public void saveMeasurementSet(MeasurementSet measurementSet) throws Exception;
    
    /**
     * Get a measurement set instance according to its UUID. Will include sub-classes
     * except for measurements, if available.
     * @param measurementSetUUID The UUID of the measurement set.
     * @return A measurement set object, if it exists.
     * @throws Exception If there's a technical issue or there is no measurement set with the given UUID.
     */
    public MeasurementSet getMeasurementSet(UUID measurementSetUUID) throws Exception;
    
    /**
     * Get all measurement sets for a metric group. Will include any sub-classes
     * except for measurements, if available.
     * @param metricGroupUUID The UUID of the metric group.
     * @return A measurement set object, if it exists.
     * @throws Exception If there's a technical issue or there is no metric group with the given UUID.
     */
    public Set<MeasurementSet> getMeasurementSetForMetricGroup(UUID metricGroupUUID) throws Exception;
    
    
    //---------------------------- REPORT ------------------------------------//
    
    
    /**
     * Saves a report, which must have a unique UUID and refer to an existing
     * measurement set (by its UUID).
     * @param report The report object to be saved.
     * @throws Exception If there's a technical issue or a report with the same UUID already exists.
     */
    public void saveReport(Report report) throws Exception;
    
    /**
     * Get a report object from its UUID.
     * @param reportUUID The report UUID.
     * @return A report object, if it exists.
     * @throws Exception If there's a technical issue or there is no report with the given UUID.
     */
    public Report getReport(UUID reportUUID) throws Exception;
    
    /**
     * Get a report object for a measurement set.
     * @param measurementSetUUID The measurement set UUID.
     * @return A report object, if it exists.
     * @throws Exception If there's a technical issue or there is no measurement set with the given UUID.
     */
    public Report getReportForMeasurementSet(UUID measurementSetUUID) throws Exception;
    
    
    //---------------------------- METRIC ------------------------------------//
    
    
    /**
     * Saves a metric, which must have a unique UUID and refer to an existing
     * measurement set (by its UUID).
     * @param metric The metric object to be saved.
     * @throws Exception If there's a technical issue or a metric with the same UUID already exists.
     */
    public void saveMetric(Metric metric) throws Exception;
    
    /**
     * Get a metric object according to its UUID.
     * @param metricUUID The metric UUID.
     * @return A metric object, if it exists.
     * @throws Exception If there's a technical issue or there is no metric with the given UUID.
     */
    public Metric getMetric(UUID metricUUID) throws Exception;
    
    /**
     * Get a metric object for a measurement set.
     * @param measurementSetUUID The measurement set UUID.
     * @return A metric object, if it exists.
     * @throws Exception If there's a technical issue or there is no measurement set with the given UUID.
     */
    public Metric getMetricForMeasurementSet(UUID measurementSetUUID) throws Exception;
    
    
    //------------------------- MEASUREMENT ----------------------------------//
    
    
    /**
     * Saves a measurement, which must have a unique UUID and refer to an existing
     * measurement set (by its UUID).
     * @param measurement
     * @throws Exception If there's a technical issue or a measurement with the same UUID already exists.
     */
    public void saveMeasurement(Measurement measurement) throws Exception;
    
    /**
     * Get a measurement according to its UUID.
     * @param measurementUUID The measurement UUID.
     * @return A measurement object, if it exists.
     * @throws Exception If there's a technical issue or there is no measurement with the given UUID.
     */
    public Measurement geMeasurement(UUID measurementUUID) throws Exception;
    
    /**
     * Get the latest measurement for a particular measurement set.
     * @param measurementSetUUID The measurement set UUID.
     * @return A measurement object, if exists.
     * @throws Exception If there's a technical issue or there is no measurement set with the given UUID.
     */
    public Measurement getLatestMeasurementForMeasurementSet(UUID measurementSetUUID) throws Exception;
    
    /**
     * Get all measurements for a particular measurement set.
     * @param measurementSetUUID The measurement set UUID.
     * @return Empty set if there are no measurements.
     * @throws Exception If there's a technical issue or there is no measurement set with the given UUID.
     */
    public Set<Measurement> getAllMeasurementsForMeasurementSet(UUID measurementSetUUID) throws Exception;
    
    /**
     * Get all measurements for a particular measurement set after a given date.
     * @param measurementSetUUID The measurement set UUID.
     * @param fromDate The date from which measurements should be given.
     * @return Empty set if there are no measurements.
     * @throws Exception If there's a technical issue; if there is no measurement set with the given UUID; if the date is invalid.
     */
    public Set<Measurement> getMeasurementsForMeasurementSetAfterDate(UUID measurementSetUUID, Date fromDate) throws Exception;
    
    /**
     * Get the measurements for a particular measurement set within a given time
     * period.
     * @param measurementSetUUID The measurement set UUID.
     * @param fromDate The from date of the time period.
     * @param toDate The to date of the time period.
     * @return Empty set if there are no measurements.
     * @throws Exception If there's a technical issue; if there is no measurement set with the given UUID; if the dates are invalid.
     */
    public Set<Measurement> getMeasurementsForMeasurementSetForTimePeriod(UUID measurementSetUUID, Date fromDate, Date toDate) throws Exception;
}
