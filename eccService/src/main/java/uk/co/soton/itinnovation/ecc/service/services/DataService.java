/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2014
//
// Copyright in this library belongs to the University of Southampton
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
//	Created By :			Maxim Bashevoy
//                          Simon Crowle
//	Created Date :			2014-04-02
//	Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.co.soton.itinnovation.ecc.service.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricHelper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IEntityDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IReportDAO;
import uk.co.soton.itinnovation.ecc.service.domain.DatabaseConfiguration;
import uk.co.soton.itinnovation.ecc.service.domain.EccMeasurement;
import uk.co.soton.itinnovation.ecc.service.domain.EccMeasurementSet;
import uk.co.soton.itinnovation.ecc.service.utils.EccMeasurementsComparator;

/**
 * Provides access to data in the database.
 */
@Service("dataService")
public class DataService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private IMonitoringEDM expDataManager;
    private IExperimentDAO experimentDAO;
    private IMetricGeneratorDAO metricGenDAO;
    private IEntityDAO entityDAO;
    private IReportDAO expReportDAO;

    private boolean started = false;

    @Autowired
    ExperimentService experimentService;

    public DataService() {
    }

    /**
     * Initialises the service (empty).
     */
    @PostConstruct
    public void init() {
    }

    /**
     * Ensures the service is shut down properly.
     */
    @PreDestroy
    public void shutdown() {
        logger.debug("Shutting down data service");

        if (started) {
            expReportDAO = null;
            entityDAO = null;
            metricGenDAO = null;
            experimentDAO = null;
            expDataManager = null;
        }

        logger.debug("Data service shut down");
    }

    /**
     * Starts the service (should only be called by
     * {@link ConfigurationService})
     *
     * @param databaseConfiguration
     * @return
     */
    public boolean start(DatabaseConfiguration databaseConfiguration) {
        started = false;

        if (databaseConfiguration != null) {

            // Make sure clear any previously created EDM references
            expDataManager = null;
            experimentDAO = null;
            metricGenDAO = null;
            entityDAO = null;
            expReportDAO = null;

            // Create properties for EDM Factory
            Properties props = new Properties();
            props.put("dbPassword", databaseConfiguration.getUserPassword());
            props.put("dbName", databaseConfiguration.getDatabaseName());
            props.put("dbType", databaseConfiguration.getDatabaseType());
            props.put("dbURL", databaseConfiguration.getUrl());
            props.put("dbUsername", databaseConfiguration.getUserName());

            // Try getting data accessors
            try {
                expDataManager = EDMInterfaceFactory.getMonitoringEDM(props);

                if (!expDataManager.isDatabaseSetUpAndAccessible()) {
                    throw new Exception("Could not access EDM: database is not accessible");
                }

                experimentDAO = expDataManager.getExperimentDAO();
                metricGenDAO = expDataManager.getMetricGeneratorDAO();
                entityDAO = expDataManager.getEntityDAO();
                expReportDAO = expDataManager.getReportDAO();

                started = true;
            } catch (Exception ex) {
                logger.error("Could not start data service: " + ex.getMessage());
            }
        }
        return started;
    }

    public boolean isStarted() {
        return started;
    }

    /**
     * Use this method to retrieve high-level meta-data relating to all known
     * experiments stored in the data service database. This method will return
     * instances of class Experiment but these will not contain metric models.
     *
     * @return - Returns a set of experiment instances (high-level data only)
     */
    public Set<Experiment> getAllKnownExperiments() {

        HashSet<Experiment> result = new HashSet<Experiment>();

        // Safety first
        if (started) {
            try {
                result.addAll(experimentDAO.getExperiments(false));
            } catch (Exception ex) {
                logger.warn("Could not retrieve experiments: " + ex.getMessage());
            }
        } else {
            logger.warn("Cannot get experiments: service not started");
        }

        return result;
    }

    /**
     * Use this method to retrieve high-level experiment meta-data and also all
     * known MetricGenerators currently associated with the experiment.
     *
     * @param expID - UUID of the experiment required
     * @return - Experiment instance with all currently known metric generators
     * @throws Exception - throws if the service is not initialised or there was
     * a problem with the database
     */
    public Experiment getExperimentWithMetricModels(UUID expID) throws Exception {

        // Safety first
        if (!started) {
            throw new Exception("Cannot get experiment: service not started");
        }
        if (expID == null) {
            throw new Exception("Could not get experiment: ID is null");
        }

        Experiment experiment = null;

        try {
            experiment = experimentDAO.getExperiment(expID, true);
        } catch (Exception ex) {
            String msg = "Could not retrieve experiment: " + ex.getMessage();
            logger.warn(msg);

            throw new Exception(msg);
        }

        return experiment;
    }

    /**
     * Use this method to quickly extract all known (metric) entities from an
     * experiment.
     *
     * @param expID - ID of the experiment you need entities from.
     * @return - Returns a set of entities for the experiment (if any exist)
     * @throws Exception - throws if the experiment ID is invalid or there is no
     * experiment that ID
     */
    public Set<Entity> getEntitiesForExperiment(UUID expID) throws Exception {

        // Safety
        if (!started) {
            throw new Exception("Could not get Entities for experiment: service not started");
        }
        if (expID == null) {
            throw new Exception("Could not get Entities for experiment: ID is null");
        }

        HashSet<Entity> entities = new HashSet<Entity>();

        try {
            entities.addAll(entityDAO.getEntitiesForExperiment(expID, true));
        } catch (Exception ex) {
            logger.warn("Could not get entities for experiment (" + expID.toString() + "): " + ex.getMessage());
        }

        return entities;
    }

    /**
     * Use this method to retrieve ALL MeasurementSets (WITHOUT measurement
     * data) related to a specific attribute in an experiment. This method is
     * useful if you want to retrieve all measurement data (irrespective of
     * which MetricGenerator created it) for a specific attribute.
     *
     * @param expID - Non-null experiment ID for the experiment the attribute
     * belongs to
     * @param attr - Non-null attribute for which measurements are sought
     * @return - Returns (a possibly empty) set of measurement sets
     * @throws Exception - throws if the input parameters are null or invalid
     */
    public Set<MeasurementSet> getAllEmptyMeasurementSetsForAttribute(UUID expID, Attribute attr) throws Exception {

        // Safety first
        if (!started) {
            throw new Exception("Cannot get experiments: service not started");
        }
        if (expID == null) {
            throw new Exception("Could not get Measurement Sets for Attribute: experiment ID is null");
        }
        if (attr == null) {
            throw new Exception("Could not get Measurement Sets for Attribute: Attribute is null");
        }

        HashSet<MeasurementSet> mSets = new HashSet<MeasurementSet>();

        // Get metric generators for this experiment
        try {
            Set<MetricGenerator> metGens = metricGenDAO.getMetricGeneratorsForExperiment(expID, true);

            Map<UUID, MeasurementSet> allMemSets = MetricHelper.getMeasurementSetsForAttribute(attr, metGens);
            mSets.addAll(allMemSets.values());
        } catch (Exception ex) {
            String msg = "Could not retrieve all measurement sets for attribute [" + attr.getName() + "]: " + ex.getMessage();

            logger.warn(msg);
            throw new Exception(msg, ex);
        }

        for (MeasurementSet mSet : mSets) {
//            logger.debug("Adding measurement set [" + mSet.getID().toString() + "] to attribute " + attr.getUUID().toString());
            if (mSet.getMetric() == null) {
                logger.warn("Metric for measurement set [" + mSet.getID().toString() + "] is NULL");
            } else {
                if (mSet.getMetric().getMetricType() == null) {
                    logger.warn("Metric type for measurement set [" + mSet.getID().toString() + "] is NULL");
                } else {
                    if (mSet.getMetric().getUnit() == null) {
                        logger.warn("Metric unit for measurement set [" + mSet.getID().toString() + "] is NULL");
                    } else {
                        logger.debug("[" + mSet.getID().toString() + "] type: " + mSet.getMetric().getMetricType().name() + ", unit: " + mSet.getMetric().getUnit().getName());
                    }
                }

            }
        }

        return mSets;
    }

    /**
     * Use this method to retrieve MeasurementSets (WITHOUT measurement data)
     * for a specific attribute from a specific MetricGeneator in an experiment.
     *
     * @param mgen - MetricGenerator that may hold MeasurementSets for an
     * Attribute
     * @param attr - Target Attribute which may have measurements associated
     * with it
     * @return - returns a set of MeasurementSets for the specific
     * MetricGenerator (if they exist)
     * @throws Exception - throws if the parameters are invalid or the
     * MetricGenerator/Attribute could not be found
     */
    public Set<MeasurementSet> getEmptyMeasurementSetsForAttribute(MetricGenerator mgen, Attribute attr) throws Exception {

        // Safety first
        if (!started) {
            throw new Exception("Cannot get experiments: service not started");
        }
        if (mgen == null) {
            throw new Exception("Could not get MeasurementSets for Attribute: MetricGenerator is null");
        }
        if (attr == null) {
            throw new Exception("Could not get MeasurementSets for Attribute: Attribute is null");
        }

        UUID attrID = attr.getUUID();
        if (attrID == null) {
            throw new Exception("Could not get MeasurementSets for Attribute: Attribute ID is null");
        }

        // Create a result set, then try populating
        HashSet<MeasurementSet> resultSet = new HashSet<MeasurementSet>();

        try {
            Map<UUID, MeasurementSet> entityMSets = MetricHelper.getAllMeasurementSets(mgen);

            // If there are measurement sets associated with this entity, add those linked to the target entity
            for (MeasurementSet ms : entityMSets.values()) {

                if (ms != null) {
                    // Check set is mapped to attribute
                    UUID srcAttrID = ms.getAttributeID();

                    // Add it to the result set if good
                    if (srcAttrID != null && srcAttrID.equals(attrID)) {
                        resultSet.add(ms);
                    }
                } else {
                    logger.warn("Found NULL measurement set associated with attribute " + attr.getName());
                }
            }
        } catch (Exception ex) {
            String msg = "Had problems getting MeasurementSet for attribute " + attr.getName() + ": " + ex.getMessage();
            logger.warn(msg);

            throw ex;
        }

        return resultSet;
    }

    /**
     * Use this method to retrieve MeasurementSets that are populated with
     * actual measurements (where they exist). Using this method requires that
     * you specify a start and end range within which measurements are sought.
     *
     * @param expID - Non-null experiment ID for the experiment the attribute
     * belongs to
     * @param attr - Non-null attribute for which measurements are sought
     * @param start - Non-null start date indicating measurements should have
     * been created on or after this point in time
     * @param end - Non-null end date indicating measurements should have been
     * created on or before this point in time
     * @return - Returns a (possibly empty) set of measurement sets
     * @throws Exception - throws if the input parameters are null or invalid
     */
    public Set<MeasurementSet> getMeasurementSetsForAttribute(UUID expID, Attribute attr, Date start, Date end) throws Exception {

        // Safety first
        if (!started) {
            throw new Exception("Cannot get experiments: service not started");
        }
        if (expID == null) {
            throw new Exception("Could not get Measurement Sets for Attribute: experiment ID is null");
        }
        if (attr == null) {
            throw new Exception("Could not get Measurement Sets for Attribute: Attribute is null");
        }
        if (start == null || end == null) {
            throw new Exception("Could not get Measurement Sets for Attribute: date(s) is null");
        }

        HashSet<MeasurementSet> resultSet = new HashSet<MeasurementSet>();

        try {
            // Get the MeasurementSet model first
            Set<MeasurementSet> msetInfo = getAllEmptyMeasurementSetsForAttribute(expID, attr);
            for (MeasurementSet ms : msetInfo) {
                // Then populate with data
                if (ms != null) {

                    Report report = expReportDAO.getReportForMeasurementsForTimePeriod(expID, start, end, true);

                    // Only add non-empty measurement sets
                    if (report.getNumberOfMeasurements() > 0) {
                        resultSet.add(report.getMeasurementSet());
                    }

                } else {
                    String msg = "Had problems retrieving a measurement set: MS ID is null";
                    logger.warn(msg);
                    throw new Exception(msg);
                }
            }
        } catch (Exception ex) {
            String msg = "Had problems retrieving measurement set data for Attribute " + attr.getName() + ": " + ex.getMessage();
            logger.warn(msg);

            throw new Exception(msg, ex);
        }

        return resultSet;
    }

    /**
     *
     * @param attributeId
     * @return
     */
    public EccMeasurementSet getLatestMeasurementsForAttribute(String attributeId) {
        return getTailMeasurementsForAttribute(attributeId, (new Date().getTime()));
    }

    /**
     *
     * @param attributeId the attribute.
     * @param since
     * @return last 10 measurements for the attribute.
     */
    public EccMeasurementSet getTailMeasurementsForAttribute(String attributeId, Long since) {
        EccMeasurementSet result = new EccMeasurementSet();
        ArrayList<EccMeasurement> data = new ArrayList<EccMeasurement>();
        result.setData(data);

        Experiment currentExperiment = experimentService.getActiveExperiment();

        if (currentExperiment != null) {
            try {
                Attribute attr = MetricHelper.getAttributeFromID(UUID.fromString(attributeId), metricGenDAO.getMetricGeneratorsForExperiment(currentExperiment.getUUID(), true));
                Set<MeasurementSet> measurementSets = getTailMeasurementSetsForAttribute(currentExperiment.getUUID(), attr, new Date(since), 10);
                Iterator<MeasurementSet> it = measurementSets.iterator();
                MeasurementSet ms;
                while (it.hasNext()) {
                    ms = it.next();
                    logger.debug("Processing measurement set [" + ms.getID().toString() + "] to attribute " + attr.getUUID().toString());
                    if (ms.getMetric() == null) {
                        logger.warn("Metric for measurement set [" + ms.getID().toString() + "] is NULL");
                    } else {
                        if (ms.getMetric().getMetricType() == null) {
                            logger.warn("Metric type for measurement set [" + ms.getID().toString() + "] is NULL");
                        } else {
                            if (ms.getMetric().getUnit() == null) {
                                logger.warn("Metric unit for measurement set [" + ms.getID().toString() + "] is NULL");
                            } else {
                                logger.debug("Adding [" + ms.getID().toString() + "] type: " + ms.getMetric().getMetricType().name() + ", unit: " + ms.getMetric().getUnit().getName());
                                result.setType(ms.getMetric().getMetricType().name());
                                result.setUnit(ms.getMetric().getUnit().getName());
                                for (Measurement m : ms.getMeasurements()) {
                                    data.add(new EccMeasurement(m.getTimeStamp(), m.getValue()));
                                }
                            }
                        }

                    }

                }
            } catch (Exception e) {
                logger.error("Failed to retrieve data for attribute [" + attributeId + "]", e);
            }

        } else {
            logger.warn("Data requested on current experiment which is NULL");
        }

        // Sort by time stamps
        if (result.getData().size() > 1) {
            Collections.sort(result.getData(), new EccMeasurementsComparator());
        }

        return result;
    }

    /**
     *
     * @param attributeId the attribute.
     * @param since
     * @return last 10 measurements for the attribute.
     */
    public EccMeasurementSet getSinceMeasurementsForAttribute(String attributeId, Long since) {
        EccMeasurementSet result = new EccMeasurementSet();
        ArrayList<EccMeasurement> data = new ArrayList<EccMeasurement>();
        result.setData(data);

        Experiment currentExperiment = experimentService.getActiveExperiment();

        if (currentExperiment != null) {
            try {
                Attribute attr = MetricHelper.getAttributeFromID(UUID.fromString(attributeId), metricGenDAO.getMetricGeneratorsForExperiment(currentExperiment.getUUID(), true));
                Set<MeasurementSet> measurementSets = getSinceMeasurementSetsForAttribute(currentExperiment.getUUID(), attr, new Date(since), 10);
                Iterator<MeasurementSet> it = measurementSets.iterator();
                MeasurementSet ms;
                while (it.hasNext()) {
                    ms = it.next();
                    logger.debug("Processing measurement set [" + ms.getID().toString() + "] to attribute " + attr.getUUID().toString());
                    if (ms.getMetric() == null) {
                        logger.warn("Metric for measurement set [" + ms.getID().toString() + "] is NULL");
                    } else {
                        if (ms.getMetric().getMetricType() == null) {
                            logger.warn("Metric type for measurement set [" + ms.getID().toString() + "] is NULL");
                        } else {
                            if (ms.getMetric().getUnit() == null) {
                                logger.warn("Metric unit for measurement set [" + ms.getID().toString() + "] is NULL");
                            } else {
                                logger.debug("Adding [" + ms.getID().toString() + "] type: " + ms.getMetric().getMetricType().name() + ", unit: " + ms.getMetric().getUnit().getName());
                                result.setType(ms.getMetric().getMetricType().name());
                                result.setUnit(ms.getMetric().getUnit().getName());
                                for (Measurement m : ms.getMeasurements()) {
                                    data.add(new EccMeasurement(m.getTimeStamp(), m.getValue()));
                                }
                            }
                        }

                    }

                }
            } catch (Exception e) {
                logger.error("Failed to retrieve data for attribute [" + attributeId + "]", e);
            }

        } else {
            logger.warn("Data requested on current experiment which is NULL");
        }

        // Sort by time stamps
        if (result.getData().size() > 1) {
            Collections.sort(result.getData(), new EccMeasurementsComparator());
        }

        return result;
    }

    /**
     * Use this method to retrieve historical measurements (if they exist) from
     * a specific point in time. Use the count parameter to specific the maximum
     * number of measurements you want returned for any Measurement Set
     * discovered.
     *
     * @param expID - Non-null ID of the experiment
     * @param attr - Non-null Attribute of interest
     * @param tail - Non-null time stamp from which to work backwards from
     * @param count - Greater than zero maximum number of measurements per
     * measurement set
     * @return - Returns a collection of Measurement Sets
     * @throws Exception - Throws if parameters are invalid or there were
     * problems retrieving data from the database
     */
    public Set<MeasurementSet> getTailMeasurementSetsForAttribute(UUID expID, Attribute attr, Date tail, int count) {

        // Safety first
        if (!started) {
            throw new IllegalStateException("Cannot get experiments: service not started");
        }
        if (expID == null) {
            throw new IllegalArgumentException("Could not get tail Measurement Sets for Attribute: experiment ID is null");
        }
        if (attr == null) {
            throw new IllegalArgumentException("Could not get tail Measurement Sets for Attribute: Attribute is null");
        }
        if (tail == null) {
            throw new IllegalArgumentException("Could not get tail Measurement Sets for Attribute: Date is null");
        }
        if (count < 1) {
            throw new IllegalArgumentException("Could not get tail Measurement Sets for Attribute: date(s) is null");
        }

        logger.debug("Returning " + count + " data points BACK since '" + tail.toString() + "' for attribute [" + attr.getUUID().toString() + "] of experiment [" + expID.toString() + "]");

        HashSet<MeasurementSet> resultSet = new HashSet<MeasurementSet>();

        // Get the MeasurementSet model first
        Set<MeasurementSet> msetInfo;
        try {
            msetInfo = getAllEmptyMeasurementSetsForAttribute(expID, attr);
        } catch (Exception e) {
            logger.error("Failed to return All Empty MeasurementSets For Attribute [" + attr.getUUID().toString() + "]", e);
            return resultSet;
        }

//        try {
        for (MeasurementSet ms : msetInfo) {
            // Then populate with data
            if (ms != null) {
                try {
                    Report report = expReportDAO.getReportForTailMeasurements(ms.getID(), tail, count, true);
                    // Only add non-empty measurement sets
                    MeasurementSet tempMs;
                    if (report.getNumberOfMeasurements() > 0) {
                        tempMs = report.getMeasurementSet();
                        if (ms.getMetric() == null) {
                            logger.warn("Metric for measurement set [" + ms.getID().toString() + "] is NULL");
                        } else {
                            if (ms.getMetric().getMetricType() == null) {
                                logger.warn("Metric type for measurement set [" + ms.getID().toString() + "] is NULL");
                            } else {
                                if (ms.getMetric().getUnit() == null) {
                                    logger.warn("Metric unit for measurement set [" + ms.getID().toString() + "] is NULL");
                                } else {
                                    logger.debug("Adding INITIAL MS [" + ms.getID().toString() + "] type: " + ms.getMetric().getMetricType().name() + ", unit: " + ms.getMetric().getUnit().getName());

                                }
                            }

                        }
                        if (tempMs.getMetric() == null) {
                            logger.warn("Metric for measurement set [" + tempMs.getID().toString() + "] is NULL");
                            tempMs.setMetric(ms.getMetric());
                        } else {
                            if (tempMs.getMetric().getMetricType() == null) {
                                logger.warn("Metric type for measurement set [" + tempMs.getID().toString() + "] is NULL");
                            } else {
                                if (tempMs.getMetric().getUnit() == null) {
                                    logger.warn("Metric unit for measurement set [" + tempMs.getID().toString() + "] is NULL");
                                } else {
                                    logger.debug("Adding REPORTED MS [" + tempMs.getID().toString() + "] type: " + tempMs.getMetric().getMetricType().name() + ", unit: " + tempMs.getMetric().getUnit().getName());

                                }
                            }

                        }

                        resultSet.add(tempMs);
                    }
                } catch (Exception ex) {
                    logger.error("Failed to get report for tail measurements", ex);
                    break;
                }
            } else {
                logger.warn("Failed to retrieve measurement set: MS ID is NULL");
            }
        }
//        } catch (Exception ex) {
//            String msg = "Failed to retrieve tail measurement set data for attribute " + attr.getName() + ": " + ex.getMessage();
//            logger.warn(msg);
//        }

        return resultSet;
    }

    public Set<MeasurementSet> getSinceMeasurementSetsForAttribute(UUID expID, Attribute attr, Date since, int count) {

        // Safety first
        if (!started) {
            throw new IllegalStateException("Cannot get experiments: service not started");
        }
        if (expID == null) {
            throw new IllegalArgumentException("Could not get since Measurement Sets for Attribute: experiment ID is null");
        }
        if (attr == null) {
            throw new IllegalArgumentException("Could not get since Measurement Sets for Attribute: Attribute is null");
        }
        if (since == null) {
            throw new IllegalArgumentException("Could not get since Measurement Sets for Attribute: Date is null");
        }
        if (count < 1) {
            throw new IllegalArgumentException("Could not get since Measurement Sets for Attribute: date(s) is null");
        }

        logger.debug("Returning " + count + " data points FORWARD since '" + since.toString() + "' for attribute [" + attr.getUUID().toString() + "] of experiment [" + expID.toString() + "]");

        HashSet<MeasurementSet> resultSet = new HashSet<MeasurementSet>();

        // Get the MeasurementSet model first
        Set<MeasurementSet> msetInfo;
        try {
            msetInfo = getAllEmptyMeasurementSetsForAttribute(expID, attr);
        } catch (Exception e) {
            logger.error("Failed to return All Empty MeasurementSets For Attribute [" + attr.getUUID().toString() + "]", e);
            return resultSet;
        }

//        try {
        for (MeasurementSet ms : msetInfo) {
            // Then populate with data
            if (ms != null) {
                try {
                    Report report = expReportDAO.getReportForMeasurementsFromDate(ms.getID(), since, true);
                    // Only add non-empty measurement sets
                    MeasurementSet tempMs;
                    if (report.getNumberOfMeasurements() > 0) {
                        tempMs = report.getMeasurementSet();
                        if (ms.getMetric() == null) {
                            logger.warn("Metric for measurement set [" + ms.getID().toString() + "] is NULL");
                        } else {
                            if (ms.getMetric().getMetricType() == null) {
                                logger.warn("Metric type for measurement set [" + ms.getID().toString() + "] is NULL");
                            } else {
                                if (ms.getMetric().getUnit() == null) {
                                    logger.warn("Metric unit for measurement set [" + ms.getID().toString() + "] is NULL");
                                } else {
                                    logger.debug("Adding INITIAL MS [" + ms.getID().toString() + "] type: " + ms.getMetric().getMetricType().name() + ", unit: " + ms.getMetric().getUnit().getName());

                                }
                            }

                        }
                        if (tempMs.getMetric() == null) {
                            logger.warn("Metric for measurement set [" + tempMs.getID().toString() + "] is NULL");
                            tempMs.setMetric(ms.getMetric());
                        } else {
                            if (tempMs.getMetric().getMetricType() == null) {
                                logger.warn("Metric type for measurement set [" + tempMs.getID().toString() + "] is NULL");
                            } else {
                                if (tempMs.getMetric().getUnit() == null) {
                                    logger.warn("Metric unit for measurement set [" + tempMs.getID().toString() + "] is NULL");
                                } else {
                                    logger.debug("Adding REPORTED MS [" + tempMs.getID().toString() + "] type: " + tempMs.getMetric().getMetricType().name() + ", unit: " + tempMs.getMetric().getUnit().getName());

                                }
                            }

                        }

                        resultSet.add(tempMs);
                    }
                } catch (Exception ex) {
                    logger.error("Failed to get report for tail measurements", ex);
                    break;
                }
            } else {
                logger.warn("Failed to retrieve measurement set: MS ID is NULL");
            }
        }
//        } catch (Exception ex) {
//            String msg = "Failed to retrieve tail measurement set data for attribute " + attr.getName() + ": " + ex.getMessage();
//            logger.warn(msg);
//        }

        return resultSet;
    }
}
