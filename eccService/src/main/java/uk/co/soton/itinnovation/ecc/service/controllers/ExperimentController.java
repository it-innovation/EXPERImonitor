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
package uk.co.soton.itinnovation.ecc.service.controllers;

import java.util.ArrayList;
import java.util.Collections;
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
import uk.co.soton.itinnovation.ecc.service.domain.EccAttribute;
import uk.co.soton.itinnovation.ecc.service.domain.EccClient;
import uk.co.soton.itinnovation.ecc.service.domain.EccEntity;
import uk.co.soton.itinnovation.ecc.service.domain.ExperimentNameDescription;
import uk.co.soton.itinnovation.ecc.service.services.ConfigurationService;
import uk.co.soton.itinnovation.ecc.service.services.ExperimentService;
import uk.co.soton.itinnovation.ecc.service.utils.EccAttributeComparator;
import uk.co.soton.itinnovation.ecc.service.utils.EccClientComparator;
import uk.co.soton.itinnovation.ecc.service.utils.EccEntityComparator;

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

    /**
     * @return configuration of this service, null if not yet configured.
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Experiment getActiveExperiment() {
        logger.debug("Returning current experiment details");

        return experimentService.getActiveExperiment();
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
            Collections.sort(result, new EccClientComparator());
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
                    Collections.sort(attributes, new EccAttributeComparator());
                    tempEntity.setAttributes(attributes);
                    result.add(tempEntity);
                }
            }

            if (result.size() > 1) {
                Collections.sort(result, new EccEntityComparator());
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
    public Experiment startExperiment(@RequestBody ExperimentNameDescription experimentNameDescription) {
        logger.debug("Starting new experiment: '" + experimentNameDescription.getName() + "' with description '" + experimentNameDescription.getDescription() + "'");

        if (!experimentService.isStarted()) {
            logger.error("Failed to create new expiment: the service is not yet started");
            return null;
        } else {
            try {
                Experiment result = experimentService.startExperiment(
                        configurationService.getSelectedEccConfiguration().getProjectName(),
                        experimentNameDescription.getName(), experimentNameDescription.getDescription());
                return result;
            } catch (Exception e) {
                logger.error("Failed to create new experiment", e);
                return null;
            }
        }
    }
}
