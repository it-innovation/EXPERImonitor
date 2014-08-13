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
package uk.ac.soton.itinnovation.ecc.service.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Metric;
import uk.ac.soton.itinnovation.ecc.service.utils.MetricCalculator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricHelper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.NoDataException;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IEntityDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMeasurementSetDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IReportDAO;
import uk.ac.soton.itinnovation.ecc.service.domain.DatabaseConfiguration;
import uk.ac.soton.itinnovation.ecc.service.domain.EccAttribute;
import uk.ac.soton.itinnovation.ecc.service.domain.EccClient;
import uk.ac.soton.itinnovation.ecc.service.domain.EccCounterMeasurement;
import uk.ac.soton.itinnovation.ecc.service.domain.EccCounterMeasurementSet;
import uk.ac.soton.itinnovation.ecc.service.domain.EccEntity;
import uk.ac.soton.itinnovation.ecc.service.domain.EccMeasurement;
import uk.ac.soton.itinnovation.ecc.service.domain.EccMeasurementSet;
import uk.ac.soton.itinnovation.ecc.service.utils.EccAttributesComparator;
import uk.ac.soton.itinnovation.ecc.service.utils.EccEntitiesComparator;
import uk.ac.soton.itinnovation.ecc.service.utils.EccMeasurementsComparator;

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
    private IMeasurementSetDAO msetDAO;
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

        boolean stopResult = stop();

        logger.debug("Data service shut down: " + stopResult);
    }

    /**
     * Attempts to stop service.
     *
     * @return false on fail.
     */
    public boolean stop() {
        if (started) {
            logger.debug("Stopping data service");
            try {
                expReportDAO = null;
                msetDAO = null;
                entityDAO = null;
                metricGenDAO = null;
                experimentDAO = null;
                expDataManager = null;

                return true;
            } catch (Throwable e) {
                logger.error("Failed to stop data service", e);
                return false;
            }
        } else {
            logger.error("Failed to stop data service: not started");
            return false;
        }
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
            msetDAO = null;
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
                msetDAO = expDataManager.getMeasurementSetDAO();
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
     */
    public Experiment getExperimentWithMetricModels(UUID expID) {

        // Safety first
        if (!started) {
            logger.error("Failed to get experiment: service not started");
            return null;
        }
        if (expID == null) {
            logger.error("Failed to get experiment: ID is null");
            return null;
        }

        Experiment experiment = null;

        try {
            experiment = experimentDAO.getExperiment(expID, true);
        } catch (Exception ex) {
            logger.error("Could not retrieve experiment: " + ex.getMessage());
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

    public Attribute getAttribute(String uuid) {
        if (uuid == null) {
            logger.error("Failed to return attribute, requested id is NULL");
            return null;
        }

        if (!started) {
            logger.error("Failed to return attribute with id [" + uuid + "]: data service not yet started");
            return null;
        } else {
            if (experimentService.getActiveExperiment() == null) {
                logger.error("Failed to return attribute with id [" + uuid + "]: no active experiment");
                return null;
            } else {
                try {
                    return entityDAO.getAttribute(UUID.fromString(uuid));
                } catch (Exception e) {
                    logger.error("Failed to return attribute with id [" + uuid + "]", e);
                    return null;
                }
            }
        }
    }

    /**
     *
     * @param experimentUuid
     * @param uuid
     * @return attribute by UUID.
     */
    public Attribute getAttribute(String experimentUuid, String uuid) {
        if (uuid == null) {
            logger.error("Failed to return attribute, requested id is NULL");
            return null;
        }

        if (!started) {
            logger.error("Failed to return attribute with id [" + uuid + "]: data service not yet started");
            return null;
        } else {
            try {
                Attribute result = null;
                for (Entity e : entityDAO.getEntitiesForExperiment(UUID.fromString(experimentUuid), true)) {
                    for (Attribute a : e.getAttributes()) {
                        if (a.getUUID().equals(UUID.fromString(uuid))) {
                            result = a;
                            break;
                        }
                    }
                }

                return result;
            } catch (Exception e) {
                logger.error("Failed to return attribute with id [" + uuid + "]", e);
                return null;
            }
        }
    }

    /**
     *
     * @param uuid
     * @param withAttributes
     * @return attribute by UUID.
     */
    public Entity getEntity(String uuid, boolean withAttributes) {
        if (uuid == null) {
            logger.error("Failed to return entity, requested id is NULL");
            return null;
        }

        if (!started) {
            logger.error("Failed to return entity with id [" + uuid + "]: data service not yet started");
            return null;
        } else {
            if (experimentService.getActiveExperiment() == null) {
                logger.error("Failed to return entity with id [" + uuid + "]: no active experiment");
                return null;
            } else {
                try {
                    return entityDAO.getEntity(UUID.fromString(uuid), withAttributes);
                } catch (Exception e) {
                    logger.error("Failed to return entity with id [" + uuid + "]", e);
                    return null;
                }
            }
        }
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
            String msg = "Had problems retrieving measurement set data for attribute " + attr.getName() + ": " + ex.getMessage();
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
    public EccMeasurementSet getAllMeasurementsForAttribute(String attributeId) {
        return getAllMeasurementsForAttribute(experimentService.getActiveExperiment().getUUID().toString(), attributeId);
    }

    public EccMeasurementSet getAllMeasurementsForAttribute(String experimentId, String attributeId) {

        // TODO: make safe + convert to stream
        Attribute a = getAttribute(experimentId, attributeId);

        EccMeasurementSet result = new EccMeasurementSet();

        if (a == null) {
            return result;
        }

        ArrayList<EccMeasurement> data = new ArrayList<EccMeasurement>();
        result.setData(data);

        try {
            Set<MeasurementSet> msetInfo = getAllEmptyMeasurementSetsForAttribute(UUID.fromString(experimentId), a);
            MeasurementSet ms = msetInfo.iterator().next();
            for (Measurement m : expReportDAO.getReportForAllMeasurements(ms.getID(), true).getMeasurementSet().getMeasurements()) {
                data.add(new EccMeasurement(m.getTimeStamp(), m.getValue()));
            }
        } catch (Exception e) {
            if (e instanceof NoDataException) {
                logger.debug("No data found for attribute [" + attributeId + "] in experiment [" + experimentId + "]");
            } else {
                logger.error("Failed to get data for attribute [" + attributeId + "] in experiment [" + experimentId + "]", e);
            }
        }

        if (data.size() > 1) {
            Collections.sort(data, new EccMeasurementsComparator());
        }

        return result;
    }

    /**
     *
     * @param attributeId
     * @param limit
     * @return
     */
    public EccMeasurementSet getLatestMeasurementsForAttribute(String attributeId, int limit) {
        return getTailMeasurementsForAttribute(attributeId, (new Date().getTime()), limit);
    }

    /**
     *
     * @param attributeId the attribute.
     * @param since
     * @return last 10 measurements for the attribute.
     */
    public EccMeasurementSet getTailMeasurementsForAttribute(String attributeId, Long since, int limit) {
        EccMeasurementSet result = new EccMeasurementSet();
        ArrayList<EccMeasurement> data = new ArrayList<EccMeasurement>();
        result.setData(data);

        Experiment currentExperiment = experimentService.getActiveExperiment();

        if (currentExperiment != null) {
            try {
                Attribute attr = MetricHelper.getAttributeFromID(UUID.fromString(attributeId), metricGenDAO.getMetricGeneratorsForExperiment(currentExperiment.getUUID(), true));
                Set<MeasurementSet> measurementSets = getTailMeasurementSetsForAttribute(currentExperiment.getUUID(), attr, new Date(since), limit);
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
                if (e instanceof NoDataException) {
                    logger.debug("No measurements for attribute [" + attributeId + "] before " + since);
                } else {
                    logger.error("Failed to retrieve data for attribute [" + attributeId + "]", e);
                }
            }

        } else {
            logger.warn("Data requested on current experiment which is NULL");
        }

        // Sort by time stamps, add timestamp
        if (result.getData().size() > 1) {
            Collections.sort(result.getData(), new EccMeasurementsComparator());

        }

        if (result.getData().size() > 0) {
            result.setTimestamp(ISODateTimeFormat.dateTime().print(result.getData().get(result.getData().size() - 1).getTimestamp().getTime()));
        }

        return result;
    }

    /**
     *
     * @param attributeId the attribute.
     * @param since
     * @param limit
     * @return latest 10 measurements for the attribute since a moment in time.
     */
    public EccMeasurementSet getLatestSinceMeasurementsForAttribute(String attributeId, Long since, int limit) {
        EccMeasurementSet result = new EccMeasurementSet();
        ArrayList<EccMeasurement> data = new ArrayList<EccMeasurement>();
        result.setData(data);

        Experiment currentExperiment = experimentService.getActiveExperiment();

        if (currentExperiment != null) {
            try {
                Attribute attr = MetricHelper.getAttributeFromID(UUID.fromString(attributeId), metricGenDAO.getMetricGeneratorsForExperiment(currentExperiment.getUUID(), true));
                Set<MeasurementSet> measurementSets = getSinceMeasurementSetsForAttribute(currentExperiment.getUUID(), attr, new Date(since), limit);
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
                                    if (!m.getTimeStamp().equals(new Date(since))) {
                                        data.add(new EccMeasurement(m.getTimeStamp(), m.getValue()));
                                    }
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

        // TODO: make this a database operation!
        // Sort by time stamps
        int resultSize = result.getData().size();
        if (resultSize > 1) {
            // reverse sort
            Collections.sort(result.getData(), Collections.reverseOrder(new EccMeasurementsComparator()));

            // select latest 'limit' measurements
            ArrayList<EccMeasurement> tempData = new ArrayList<EccMeasurement>(result.getData().subList(0, limit > resultSize ? resultSize : limit));

            // sort again
            Collections.sort(tempData, new EccMeasurementsComparator());

            // reset to new data
            result.setData(tempData);
        }

        if (resultSize > 0) {
            // set timestamp
            result.setTimestamp(ISODateTimeFormat.dateTime().print(result.getData().get(result.getData().size() - 1).getTimestamp().getTime()));
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
     * @param beforeThisDate - Non-null time stamp from which to work backwards
     * from
     * @param count - Greater than zero maximum number of measurements per
     * measurement set
     * @return - Returns a collection of Measurement Sets
     * @throws Exception - Throws if parameters are invalid or there were
     * problems retrieving data from the database
     */
    public Set<MeasurementSet> getTailMeasurementSetsForAttribute(UUID expID, Attribute attr, Date beforeThisDate, int count) {

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
        if (beforeThisDate == null) {
            throw new IllegalArgumentException("Could not get tail Measurement Sets for Attribute: Date is null");
        }
        if (count < 1) {
            throw new IllegalArgumentException("Could not get tail Measurement Sets for Attribute: date(s) is null");
        }

        logger.debug("Returning " + count + " data points BACK since '" + beforeThisDate.toString() + "' for attribute [" + attr.getUUID().toString() + "] of experiment [" + expID.toString() + "]");

        HashSet<MeasurementSet> resultSet = new HashSet<MeasurementSet>();

        // Get the MeasurementSet model first
        Set<MeasurementSet> msetInfo;
        try {
            msetInfo = getAllEmptyMeasurementSetsForAttribute(expID, attr);
        } catch (Exception e) {
            logger.error("Failed to return All Empty MeasurementSets For Attribute [" + attr.getUUID().toString() + "]", e);
            return resultSet;
        }

        for (MeasurementSet ms : msetInfo) {
            // Then populate with data
            if (ms != null) {
                try {
                    Report report = expReportDAO.getReportForTailMeasurements(ms.getID(), beforeThisDate, count, true);
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
                } catch (Exception e) {
                    if (e instanceof NoDataException) {
                        logger.debug("No measurements for attribute [" + attr.getUUID().toString() + "] before " + beforeThisDate);
                    } else {
                        logger.error("Failed to retrieve data for attribute [" + attr.getUUID().toString() + "]", e);
                    }
                    break;
                }
            } else {
                logger.warn("Failed to retrieve measurement set: MS ID is NULL");
            }
        }
        return resultSet;
    }

    public Set<MeasurementSet> getSinceMeasurementSetsForAttribute(UUID expID, Attribute attr, Date sinceThisDate, int count) {

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
        if (sinceThisDate == null) {
            throw new IllegalArgumentException("Could not get since Measurement Sets for Attribute: Date is null");
        }
        if (count < 1) {
            throw new IllegalArgumentException("Could not get since Measurement Sets for Attribute: date(s) is null");
        }

        logger.debug("Returning " + count + " data points FORWARD since '" + sinceThisDate.toString() + "' for attribute [" + attr.getUUID().toString() + "] of experiment [" + expID.toString() + "]");

        HashSet<MeasurementSet> resultSet = new HashSet<MeasurementSet>();

        // Get the MeasurementSet model first
        Set<MeasurementSet> msetInfo;
        try {
            msetInfo = getAllEmptyMeasurementSetsForAttribute(expID, attr);
        } catch (Exception e) {
            logger.error("Failed to return All Empty MeasurementSets For Attribute [" + attr.getUUID().toString() + "]", e);
            return resultSet;
        }

        for (MeasurementSet ms : msetInfo) {
            // Then populate with data
            if (ms != null) {
                try {
                    Report report = expReportDAO.getReportForMeasurementsFromDate(ms.getID(), sinceThisDate, true);
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
                            logger.warn("Metric for measurement set [" + tempMs.getID().toString() + "] is NULL, fixing");
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
                } catch (Exception e) {
                    if (e instanceof NoDataException) {
                        logger.debug("No measurements for attribute [" + attr.getUUID().toString() + "] since " + sinceThisDate);
                    } else {
                        logger.error("Failed to retrieve data for attribute [" + attr.getUUID().toString() + "]", e);
                    }
                    break;
                }
            } else {
                logger.warn("Failed to retrieve measurement set: MS ID is NULL");
            }
        }
        return resultSet;
    }

    public ArrayList<Experiment> getAllExperiments(boolean sortedByDateCreated, boolean withMetricModels) {

        ArrayList<Experiment> resultSet = new ArrayList<Experiment>();

        // Safety first
        if (started) {
            try {
                Collection<Experiment> experiments = experimentDAO.getExperiments(withMetricModels);

                // Sort these by creation date, if required
                if (sortedByDateCreated) {

                    // Sort experiments
                    TreeMap<Date, Experiment> sortedExps = new TreeMap<Date, Experiment>();
                    for (Experiment exp : experiments) {
                        sortedExps.put(exp.getStartTime(), exp);
                    }

                    // Add experiments linearly, most recent first
                    Iterator<Date> dateIt = sortedExps.descendingKeySet().descendingIterator();
                    while (dateIt.hasNext()) {
                        resultSet.add(sortedExps.get(dateIt.next()));
                    }
                } else {
                    resultSet.addAll(experiments);
                }

            } catch (Exception ex) {
                logger.warn("Could not retrieve experiments: " + ex.getMessage());
            }
        } else {
            logger.warn("Cannot get experiments: service not started");
        }

        return resultSet;

    }

    public Experiment getExperiment(String experimentUuid, boolean withMetricModels) {

        // Safety first
        if (!started) {
            logger.error("Failed to get experiment: service not started");
            return null;
        }
        if (experimentUuid == null) {
            logger.error("Failed to get experiment: ID is null");
            return null;
        }

        Experiment experiment = null;

        try {
            experiment = experimentDAO.getExperiment(UUID.fromString(experimentUuid), withMetricModels);
        } catch (Exception ex) {
            logger.error("Could not retrieve experiment: " + ex.getMessage());
        }

        return experiment;

    }

    public Experiment getCurrentExperiment(boolean withMetricModels) {

        Experiment targetExperiment = null;

        if (started && experimentService != null) {
            if (experimentService.isExperimentInProgress()) {

                // Get high-level experiment meta-data
                targetExperiment = experimentService.getActiveExperiment();

                // If we want more details, query the database
                if (withMetricModels) {
                    try {
                        targetExperiment = experimentDAO.getExperiment(targetExperiment.getUUID(),
                                true);
                    } catch (Exception ex) {
                        logger.error("Could not retrieve experiment from database: " + ex.getMessage());
                    }
                }
            } else {
                logger.error("Could not return current experiment: no experiment in progress");
            }
        } else {
            logger.error("Could not return current experiment: service(s) not started");
        }

        return targetExperiment;
    }

    public ArrayList<EccClient> getEccClientsForCurrentExperiment() {

        ArrayList<EccClient> currentClients = new ArrayList<EccClient>();

        if (started && experimentService != null) {
            if (experimentService.isExperimentInProgress()) {

                Set<EMClient> currClients = experimentService.getKnownClients();

                for (EMClient client : currClients) {
                    EccClient ec = new EccClient(client.getID().toString(),
                            client.getName(),
                            client.isConnected());
                    currentClients.add(ec);
                }
            } else {
                logger.error("Could not return clients for current experiment: no experiment in progress");
            }
        } else {
            logger.error("Could not return clients for current experiment: service(s) not started");
        }

        return currentClients;
    }

    public EccEntity getEccEntity(String uuid, boolean withAttributes) {

        // Safety
        if (uuid == null) {
            logger.error("Failed to return entity, requested id is NULL");
            return null;
        }

        if (!started) {
            logger.error("Failed to return entity with id [" + uuid + "]: data service not yet started");
            return null;
        } else {
            try {
                Entity entity = entityDAO.getEntity(UUID.fromString(uuid), withAttributes);

                // Convert to domain class
                return toEccEntity(entity, withAttributes);

            } catch (Exception e) {
                logger.error("Failed to return entity with id [" + uuid + "]", e);
                return null;
            }
        }
    }

    public ArrayList<EccEntity> getEntitiesForExperiment(String experimentUuid, boolean withAttributes) {

        ArrayList<EccEntity> eccEntities = new ArrayList<EccEntity>();

        if (started) {
            try {
                Set<Entity> entities = entityDAO.getEntitiesForExperiment(UUID.fromString(experimentUuid),
                        withAttributes);
                for (Entity entity : entities) {
                    eccEntities.add(toEccEntity(entity, withAttributes));
                }

            } catch (Exception ex) {
                logger.warn("Could not get entities for experiment (" + experimentUuid + "): " + ex.getMessage());
            }
        } else {
            logger.error("Could not get entities for experiment: Data Service not started");
        }

        if (eccEntities.size() > 1) {
            Collections.sort(eccEntities, new EccEntitiesComparator());
        }

        return eccEntities;
    }

    public ArrayList<EccEntity> getEntitiesForClient(String clientUuid, boolean withAttributes) {

        ArrayList<EccEntity> clientEntities = new ArrayList<EccEntity>();

        if (started && experimentService != null) {
            if (experimentService.isExperimentInProgress()) {

                UUID targetID = UUID.fromString(clientUuid);

                if (targetID != null) {

                    Set<EMClient> currClients = experimentService.getKnownClients();

                    for (EMClient client : currClients) {

                        if (client.getID().equals(targetID)) {

                            for (MetricGenerator mg : client.getCopyOfMetricGenerators()) {
                                for (Entity entity : mg.getEntities()) {

                                    EccEntity ent = toEccEntity(entity, withAttributes);

                                    if (ent != null) {
                                        clientEntities.add(ent);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    logger.error("Could not get client entities for current experiment: client ID is invalid");
                }
            } else {
                logger.error("Could not get client entities for current experiment: no experiment in progress");
            }
        } else {
            logger.error("Could not get client entities for current experiment: service(s) not started");
        }

        if (clientEntities.size() > 1) {
            Collections.sort(clientEntities, new EccEntitiesComparator());
        }

        return clientEntities;
    }

    public EccAttribute getEccAttribute(String uuid) {

        EccAttribute eccAttr = null;

        if (uuid == null) {
            logger.error("Failed to return attribute, requested id is NULL");
            return null;
        }

        if (!started) {
            logger.error("Failed to return attribute with id [" + uuid + "]: data service not yet started");
            return null;
        }

        try {
            Attribute attr = entityDAO.getAttribute(UUID.fromString(uuid));

            if (attr != null) {
                eccAttr = toEccAttribute(attr);
            } else {
                logger.error("Failed to return attribute with id[" + uuid + "] - it does not exist");
            }

        } catch (Exception ex) {
            logger.error("Failed to return attribute with id [" + uuid + "]", ex.getMessage());
        }

        return eccAttr;
    }

    public ArrayList<EccAttribute> getAttributesForExperiment(String experimentUuid) {

        // Safety
        if (!started) {
            logger.error("Could not retrieve attributes for experiment: service not started");
            return null;
        }

        if (experimentUuid == null) {
            logger.error("Could not retrieve attributes for experiment: UUID value is null");
            return null;
        }

        ArrayList<EccAttribute> resultSet = new ArrayList<EccAttribute>();

        // Get all entities for this experiment
        try {
            Collection<Entity> entities = entityDAO.getEntitiesForExperiment(UUID.fromString(experimentUuid), true);

            // And add their attributes (none should be shared by Entities)
            EccAttribute eccAttr;
            for (Entity entity : entities) {
                for (Attribute attr : entity.getAttributes()) {

                    eccAttr = toEccAttribute(attr);

                    if (eccAttr != null) {
                        resultSet.add(eccAttr);
                    }
                }
            }
        } catch (Exception ex) {
            String msg = "Could not retrieve attributes for experiment " + experimentUuid + " " + ex.getMessage();
            logger.error(msg);
        }

        return resultSet;
    }

    // Returns 'limit' measurements starting from the 'dateInMsec' into the past
    public Set<EccMeasurementSet> getMeasurementsForAttributeAfter(String experimentUuid, String attributeUuid, long dateInMsec, int limit) {

        // Safety
        if (!attrSearchParamsValid(experimentUuid, attributeUuid, dateInMsec, limit)) {
            logger.error("Could not get measurements for attribute: input parameter(s) invalid");
            return null;
        }

        UUID expID = UUID.fromString(experimentUuid);
        UUID attrID = UUID.fromString(attributeUuid);
        HashSet<EccMeasurementSet> resultSet = null;

        try {
            Set<MeasurementSet> mSets = msetDAO.getMeasurementSetsForAttribute(attrID, expID, true);

            resultSet = new HashSet<EccMeasurementSet>();

            for (MeasurementSet ms : mSets) {

                // Get measurements within time frame
                Date start = new Date(dateInMsec);
                Date end = new Date();
                Report report = expReportDAO.getReportForMeasurementsForTimePeriod(ms.getID(),
                        start,
                        end,
                        true);

                Set<Measurement> mSet = report.getMeasurementSet().getMeasurements();

                if (!mSet.isEmpty()) {

                    // Sort measurements
                    List<Measurement> measurements = MetricHelper.sortMeasurementsByDateLinear(mSet);

                    // Truncate measuements and create domain object if we have data
                    if (measurements.size() > 0) {
                        measurements = MetricHelper.truncateMeasurements(measurements, limit, true);

                        // Create domain class
                        EccMeasurementSet ems = createMSDomainObject(ms, measurements);
                        resultSet.add(ems);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Could not retrieve measurements for Attribute after date: " + ex.getMessage());
        }

        return resultSet;
    }

    // Returns 'limit' measurements starting from now until 'dateInMsec' in the past excluding dateInMsec
    public Set<EccMeasurementSet> getMeasurementsForAttributeBefore(String experimentUuid, String attributeUuid, long dateInMsec, int limit) {

        // Safety
        if (experimentUuid == null || attributeUuid == null || dateInMsec < 0 || limit < 1) {
            logger.error("Could not get measurements for attribute before date: input parameter(s) invalid");
            return null;
        }

        UUID expID = UUID.fromString(experimentUuid);
        UUID attrID = UUID.fromString(attributeUuid);
        HashSet<EccMeasurementSet> resultSet = null;

        try {
            Set<MeasurementSet> mSets = msetDAO.getMeasurementSetsForAttribute(attrID, expID, true);

            resultSet = new HashSet<EccMeasurementSet>();

            for (MeasurementSet ms : mSets) {

                // Get measurements...
                Report report = expReportDAO.getReportForTailMeasurements(ms.getID(), new Date(dateInMsec), limit, true);

                Set<Measurement> mSet = report.getMeasurementSet().getMeasurements();

                if (!mSet.isEmpty()) {

                    // Sort measurements
                    List<Measurement> measurements = MetricHelper.sortMeasurementsByDateLinear(mSet);

                    // Don't need to truncate; report already truncated
                    // Create domain class
                    resultSet.add(createMSDomainObject(ms, measurements));
                }
            }
        } catch (Exception ex) {
            logger.error("Could not retrieve measurements for Attribute after date: " + ex.getMessage());
        }

        return resultSet;
    }

    // Returns 'limit' measurements starting from the 'dateInMsec' into the past in <value, number of times the value has occurred>
    public EccCounterMeasurementSet getCounterMeasurementsForAttributeAfter(String experimentUuid, String attributeUuid, long dateInMsec, int limit) {

        // Safety
        if (experimentUuid == null || attributeUuid == null || dateInMsec < 0 || limit < 1) {
            logger.error("Could not get measurements for attribute before date: input parameter(s) invalid");
            return null;
        }

        UUID expID = UUID.fromString(experimentUuid);
        UUID attrID = UUID.fromString(attributeUuid);
        EccCounterMeasurementSet result = new EccCounterMeasurementSet();
        ArrayList<EccCounterMeasurement> data = new ArrayList<EccCounterMeasurement>();
        result.setData(data);

        // TODO: get from measurement sets below (not sure if Metric is NULL below)
        result.setType("NOMINAL");
        result.setUnit("");

        try {
            Set<MeasurementSet> mSets = msetDAO.getMeasurementSetsForAttribute(attrID, expID, true);

            Set<Measurement> allMeasurements = new HashSet<Measurement>();
            for (MeasurementSet ms : mSets) {

                // Get measurements from 0 to dateInMsec
                Report report = expReportDAO.getReportForMeasurementsForTimePeriod(ms.getID(),
                        new Date(0),
                        new Date(dateInMsec),
                        true);

                allMeasurements.addAll(report.getMeasurementSet().getMeasurements());
            }

            if (!allMeasurements.isEmpty()) {

                // find most recent
                Date mostRecent = allMeasurements.iterator().next().getTimeStamp(), temp;
                for (Measurement m : allMeasurements) {
                    temp = m.getTimeStamp();
                    if (temp.after(mostRecent)) {
                        mostRecent = temp;
                    }
                }
                result.setTimestamp(ISODateTimeFormat.dateTime().print(mostRecent.getTime()));

                Map<String, Integer> freqMap = MetricCalculator.countValueFrequencies(allMeasurements);

                for (String key : freqMap.keySet()) {
                    data.add(new EccCounterMeasurement(key, freqMap.get(key)));
                }

            }
        } catch (Exception ex) {
            logger.error("Could not retrieve measurements for Attribute after date: " + ex.getMessage());
        }

        return result;
    }

    // Returns 'limit' measurements starting from now until 'dateInMsec' in the past in <value, number of times the value has occurred> excluding dateInMsec
    public EccCounterMeasurementSet getCounterMeasurementsForAttributeBeforeAndExcluding(String experimentUuid, String attributeUuid, long dateInMsec, int limit) {

        // Safety
        if (experimentUuid == null || attributeUuid == null || dateInMsec < 0 || limit < 1) {
            logger.error("Could not get counter measurements for attribute after date: input parameter(s) invalid");
            return null;
        }

        UUID expID = UUID.fromString(experimentUuid);
        UUID attrID = UUID.fromString(attributeUuid);
        EccCounterMeasurementSet result = new EccCounterMeasurementSet();
        Date start = new Date(dateInMsec);
        Date end = new Date();

        try {
            Set<MeasurementSet> mSets = msetDAO.getMeasurementSetsForAttribute(attrID, expID, true);

            Set<Measurement> allMeasurements = new HashSet<Measurement>();
            ArrayList<EccCounterMeasurement> data = new ArrayList<EccCounterMeasurement>();
            result.setData(data);

            // TODO: get from measurement sets below (not sure if Metric is NULL below)
            result.setType("NOMINAL");
            result.setUnit("");

            for (MeasurementSet ms : mSets) {
                // Get measurements within time frame
                Report report = expReportDAO.getReportForMeasurementsForTimePeriod(ms.getID(),
                        start,
                        end,
                        true);

                // TODO: optimise
                for (Measurement m : report.getMeasurementSet().getMeasurements()) {
                    if (!m.getTimeStamp().equals(start)) {
                        allMeasurements.add(m);
                    }
                }
            }

            if (!allMeasurements.isEmpty()) {

                // find most recent
                Date mostRecent = allMeasurements.iterator().next().getTimeStamp(), temp;
                for (Measurement m : allMeasurements) {
                    temp = m.getTimeStamp();
                    if (temp.after(mostRecent)) {
                        mostRecent = temp;
                    }
                }
                result.setTimestamp(ISODateTimeFormat.dateTime().print(mostRecent.getTime()));

                Map<String, Integer> freqMap = MetricCalculator.countValueFrequencies(allMeasurements);

                for (String key : freqMap.keySet()) {
                    data.add(new EccCounterMeasurement(key, freqMap.get(key)));
                }

            }

        } catch (Exception ex) {
            logger.error("Could not retrieve measurements for Attribute after date: " + ex.getMessage());
        }

        return result;
    }

    // Private methods ---------------------------------------------------------
    /**
     * This method provides a best guess at the attribute's measurement metrics
     * - these are actually separately stored in measurement sets. Most ECC
     * clients independently declare their entities so in the majority of cases
     * this will return the metric type that is expected.
     *
     * @param attr - Attribute of interest
     * @return - Measurement Set representing observations for the attribute
     */
    private MeasurementSet getBestGuessMeasurementSet(Attribute attr) {

        MeasurementSet bestMS = null;
        Set<MeasurementSet> mSets = null;

        if (attr != null && msetDAO != null) {

            try {
                mSets = msetDAO.getMeasurementSetsForAttribute(attr.getUUID(), true);
            } catch (Exception ex) {
                logger.warn("Failed to retrieve measurement sets for attribute " + attr.getName(), ex);
            }
        }

        if (mSets != null) {
            if (mSets.isEmpty()) {
                logger.warn("Could not find any measurement sets for attribute " + attr.getName());
            } else {
//                logger.warn("Measurement set retrieval: Attribute " + attr.getName() + " has more than one measurement set");
            }

            // Take the first measurement set
            bestMS = mSets.iterator().next();
        }

        return bestMS;
    }

    private EccAttribute toEccAttribute(Attribute attr) {

        EccAttribute eccAttr = null;

        if (attr != null) {

            MeasurementSet ms = getBestGuessMeasurementSet(attr);

            if (ms != null) {

                Metric met = ms.getMetric();
                if (met != null) {
                    eccAttr = new EccAttribute(attr.getName(),
                            attr.getDescription(),
                            attr.getUUID(),
                            attr.getEntityUUID(),
                            met.getMetricType().name(),
                            met.getUnit().getName());
                }
            }
        }

        return eccAttr;
    }

    private EccEntity toEccEntity(Entity entity, boolean withAttrs) {

        ArrayList<EccAttribute> domAttrs = new ArrayList<EccAttribute>();

        EccEntity eccEnt = new EccEntity(entity.getName(),
                entity.getDescription(),
                entity.getUUID(),
                domAttrs);

        // Add attributes, if required
        if (withAttrs) {
            for (Attribute attr : entity.getAttributes()) {

                EccAttribute ea = toEccAttribute(attr);

                if (ea != null) {
                    domAttrs.add(toEccAttribute(attr));
                }
            }
        }

        if (domAttrs.size() > 1) {
            Collections.sort(domAttrs, new EccAttributesComparator());
        }

        return eccEnt;
    }

    private boolean attrSearchParamsValid(String experimentUuid, String attributeUuid, long dateInMsec, int limit) {

        if (experimentUuid == null || attributeUuid == null || dateInMsec < 1 || limit < 1) {
            return false;
        }

        UUID expID = UUID.fromString(experimentUuid);
        UUID attrID = UUID.fromString(attributeUuid);

        if (expID == null || attrID == null) {
            return false;
        }

        return true;
    }

    private EccMeasurementSet createMSDomainObject(MeasurementSet srcMS, List<Measurement> measures) {

        EccMeasurementSet result = null;

        // Re-create data
        ArrayList<EccMeasurement> targetMeasures = new ArrayList<EccMeasurement>();
        for (Measurement m : measures) {
            targetMeasures.add(new EccMeasurement(m.getTimeStamp(), m.getValue()));
        }

        // Attach metric meta-dat
        Metric met = srcMS.getMetric();

        result = new EccMeasurementSet(met.getUnit().getName(),
                met.getMetricType().name(),
                targetMeasures);

        return result;
    }

    private EccCounterMeasurementSet createCounterMSDomainObject(MeasurementSet srcMs, Map<String, Integer> freqMap) {

        Metric met = srcMs.getMetric();

        // Create counter measurement set
        EccCounterMeasurementSet ecms = new EccCounterMeasurementSet();
        ecms.setUnit(met.getUnit().getName());
        ecms.setType(met.getMetricType().name());

        // Populate the set
        ArrayList<EccCounterMeasurement> freqCounts = new ArrayList<EccCounterMeasurement>();
        ecms.setData(freqCounts);

        for (String key : freqMap.keySet()) {
            freqCounts.add(new EccCounterMeasurement(key, freqMap.get(key)));
        }

        return ecms;
    }
}
