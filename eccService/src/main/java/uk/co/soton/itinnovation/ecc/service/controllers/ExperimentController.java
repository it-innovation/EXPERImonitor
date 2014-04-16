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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.co.soton.itinnovation.ecc.service.domain.ExperimentNameDescription;
import uk.co.soton.itinnovation.ecc.service.services.ConfigurationService;
import uk.co.soton.itinnovation.ecc.service.services.ExperimentService;

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
     * Starts new experiment.
     *
     * @param experimentNameDescription new experiment name and description.
     * @return true if experiment was created successfully.
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Experiment startExperiment(@RequestBody ExperimentNameDescription experimentNameDescription) {
        logger.debug("Starting new experiment: '" + experimentNameDescription.getName() + "'" + experimentNameDescription.getDescription());

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
