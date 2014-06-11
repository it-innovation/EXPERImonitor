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
//	Created Date :			2014-04-16
//	Created for Project :           EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.ecc.service.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricHelper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.ac.soton.itinnovation.ecc.service.domain.EccAttribute;
import uk.ac.soton.itinnovation.ecc.service.domain.EccClient;
import uk.ac.soton.itinnovation.ecc.service.domain.EccEntity;
import uk.ac.soton.itinnovation.ecc.service.domain.EccExperiment;
import uk.ac.soton.itinnovation.ecc.service.domain.ExperimentNameDescription;
import uk.ac.soton.itinnovation.ecc.service.services.ConfigurationService;
import uk.ac.soton.itinnovation.ecc.service.services.DataService;
import uk.ac.soton.itinnovation.ecc.service.services.ExperimentService;
import uk.ac.soton.itinnovation.ecc.service.utils.Convert;
import uk.ac.soton.itinnovation.ecc.service.utils.EccAttributesComparator;
import uk.ac.soton.itinnovation.ecc.service.utils.EccClientsComparator;
import uk.ac.soton.itinnovation.ecc.service.utils.EccEntitiesComparator;
import uk.ac.soton.itinnovation.ecc.service.utils.EccExperimentsComparator;

/**
 * Exposes ECC experiment service endpoints.
 */
@Controller
@RequestMapping("/experiments")
public class ExperimentController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    ConfigurationService configurationService;

    @Autowired
    ExperimentService experimentService;

    @Autowired
    DataService dataService;

    /**
     * @return list of all known experiments.
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ArrayList<EccExperiment> getAllExperiments() {
        logger.debug("Returning details of all known experiments");

        ArrayList<EccExperiment> result = new ArrayList<EccExperiment>();

        for (Experiment e : dataService.getAllKnownExperiments()) {
            if (e != null) {
                if (e.getStartTime() != null) {
                    result.add(Convert.experimentToEccExperiment(e));
                } else {
                    logger.error("Invalid experiment [" + e.getUUID().toString() + "]: no start time specified. Will not return in getAllExperiments()");
                }
            } else {
                logger.error("Encountered NULL experiment!");
            }
        }

        if (result.size() > 1) {
            Collections.sort(result, new EccExperimentsComparator());
        }

        return result;
    }

    /**
     * @return list of latest 10 experiments.
     */
    @RequestMapping(method = RequestMethod.GET, value = "/latest")
    @ResponseBody
    public ArrayList<EccExperiment> getLatestExperiments() {
        logger.debug("Returning details of latest 10 experiments");

        ArrayList<EccExperiment> result = new ArrayList<EccExperiment>();

        // check for current experiment
        UUID currentExperimentUuid = null;
        if (experimentService.isExperimentInProgress()) {
            currentExperimentUuid = experimentService.getActiveExperiment().getUUID();
        }

        for (Experiment e : dataService.getAllKnownExperiments()) {
            if (e != null) {
                if (e.getStartTime() != null) {
                    if (currentExperimentUuid == null) {
                        result.add(Convert.experimentToEccExperiment(e));
                    } else {
                        if (!e.getUUID().equals(currentExperimentUuid)) {
                            result.add(Convert.experimentToEccExperiment(e));
                        }
                    }
                } else {
                    logger.error("Invalid experiment [" + e.getUUID().toString() + "]: no start time specified. Will not return in getLatestExperiments()");
                }
            } else {
                logger.error("Encountered NULL experiment!");
            }
        }

        if (result.size() > 1) {
            Collections.sort(result, new EccExperimentsComparator());
        }

        // TODO: move to database
        if (result.size() > 10) {
            return new ArrayList<EccExperiment>(result.subList(0, 9));
        } else {
            return result;
        }
    }

    /**
     * @return current experiment.
     */
    @RequestMapping(method = RequestMethod.GET, value = "/current")
    @ResponseBody
    public EccExperiment getCurrentExperiment() {
        logger.debug("Returning current experiment details");

        return Convert.experimentToEccExperiment(experimentService.getActiveExperiment());
    }

    /**
     * @param experimentUuid
     * @return list of entities for the client.
     */
    @RequestMapping(method = RequestMethod.GET, value = "/id/{experimentUuid}")
    @ResponseBody
    public EccExperiment getExperimentByUuid(@PathVariable String experimentUuid) {

        logger.debug("Returning experiment with UUID [" + experimentUuid + "]");
        if (!experimentService.isStarted()) {
            logger.error("Failed to fetch experiment [" + experimentUuid + "] the service is not yet started");
            return null;
        } else {
            try {
                return Convert.experimentToEccExperiment(experimentService.getExperiment(experimentUuid));
            } catch (Exception e) {
                logger.error("Failed to fetch experiment [" + experimentUuid + "]", e);
                return null;
            }
        }
    }

    /**
     * @param experimentUuid
     * @return list of entities for the client.
     */
    @RequestMapping(method = RequestMethod.POST, value = "/id/{experimentUuid}")
    @ResponseBody
    public EccExperiment relaunchExperimentByUuid(@PathVariable String experimentUuid) {

        logger.debug("Restarting experiment with UUID [" + experimentUuid + "]");
        if (!experimentService.isStarted()) {
            logger.error("Failed to restart experiment [" + experimentUuid + "] the service is not yet started");
            return null;
        } else {
            try {
                return Convert.experimentToEccExperiment(experimentService.reStartExperiment(experimentUuid));
            } catch (Exception e) {
                logger.error("Failed to restart experiment [" + experimentUuid + "]", e);
                return null;
            }
        }
    }

    /**
     * @return true if the service is initialised (i.e., is ready to retrieve
     * configurations based on project names)
     */
    @RequestMapping(method = RequestMethod.GET, value = "/ifinprogress")
    @ResponseBody
    public boolean ifExperimentExists() {
        boolean result = experimentService.isExperimentInProgress();
        logger.debug("Returning experiment in progress status: " + result);
        return result;
    }

    /**
     * @return list of connected clients.
     */
    @RequestMapping(method = RequestMethod.GET, value = "/clients")
    @ResponseBody
    public ArrayList<EccClient> getClients() {
        ArrayList<EccClient> result = new ArrayList<EccClient>();
        for (EMClient c : experimentService.getCurrentlyConnectedClients()) {
            result.add(new EccClient(c.getID().toString(), c.getName(), c.isConnected()));
        }
        logger.debug("Returning currently connected clients (" + result.size() + ")");
        if (result.size() > 0) {
            Collections.sort(result, new EccClientsComparator());
        }
        return result;
    }

    /**
     * @param clientUuid client UUID.
     * @return list of entities for the client.
     */
    @RequestMapping(method = RequestMethod.GET, value = "/entities/{clientUuid}")
    @ResponseBody
    public ArrayList<EccEntity> getEntitiesForClient(@PathVariable String clientUuid) {

        logger.debug("Returning the list of entities for client [" + clientUuid + "]");
        ArrayList<EccEntity> result = new ArrayList<EccEntity>();

        // TODO: make safe
        try {
            EMClient theClient = experimentService.getClientByID(UUID.fromString(clientUuid));
            Iterator<MetricGenerator> it = theClient.getCopyOfMetricGenerators().iterator();
            MetricGenerator mg;
            EccEntity tempEntity;
            ArrayList<EccAttribute> attributes;
            MeasurementSet ms;
            while (it.hasNext()) {
                mg = it.next();
                for (Entity e : mg.getEntities()) {
                    tempEntity = new EccEntity();
                    attributes = new ArrayList<EccAttribute>();
                    tempEntity.setName(e.getName());
                    tempEntity.setDescription(e.getDescription());
                    tempEntity.setUuid(e.getUUID());
                    for (Attribute a : e.getAttributes()) {
                        ms = MetricHelper.getMeasurementSetForAttribute(a, mg);
                        attributes.add(new EccAttribute(a.getName(), a.getDescription(), a.getUUID(), a.getEntityUUID(), ms.getMetric().getMetricType().name(), ms.getMetric().getUnit().getName()));
                    }
                    Collections.sort(attributes, new EccAttributesComparator());
                    tempEntity.setAttributes(attributes);
                    result.add(tempEntity);
                }
            }

            if (result.size() > 1) {
                Collections.sort(result, new EccEntitiesComparator());
            }

            logger.debug("Found " + result.size() + " entities for client [" + clientUuid + "]");

        } catch (Exception e) {
            logger.error("Failed to return the list of entities for client [" + clientUuid + "]", e);
        }
        return result;
    }

    /**
     * Starts new experiment.
     *
     * @param experimentNameDescription new experiment name and description.
     * @return true if experiment was created successfully.
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public EccExperiment startExperiment(@RequestBody ExperimentNameDescription experimentNameDescription) {
        logger.debug("Starting new experiment: '" + experimentNameDescription.getName() + "' with description '" + experimentNameDescription.getDescription() + "'");

        if (!experimentService.isStarted()) {
            logger.error("Failed to create new experiment: the service is not yet started");
            return null;
        } else {
            try {
                Experiment result = experimentService.startExperiment(
                        configurationService.getSelectedEccConfiguration().getProjectName(),
                        experimentNameDescription.getName(), experimentNameDescription.getDescription());
                return Convert.experimentToEccExperiment(result);
            } catch (Exception e) {
                logger.error("Failed to create new experiment", e);
                return null;
            }
        }
    }

    /**
     * @return current experiment.
     */
    @RequestMapping(method = RequestMethod.GET, value = "/current/stop")
    @ResponseBody
    public boolean stopCurrentExperiment() {
        logger.debug("Stopping current experiment");

        return experimentService.stopExperiment();
    }
}
