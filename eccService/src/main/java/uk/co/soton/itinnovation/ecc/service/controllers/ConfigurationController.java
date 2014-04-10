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
//	Created Date :			2014-04-01
//	Created for Project :           EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.co.soton.itinnovation.ecc.service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.co.soton.itinnovation.ecc.service.domain.EccConfiguration;
import uk.co.soton.itinnovation.ecc.service.services.ConfigurationService;

/**
 * Exposes ECC configuration endpoints.
 */
@Controller
@RequestMapping("/configuration")
public class ConfigurationController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    ConfigurationService configurationService;

    /**
     * @return configuration of this service.
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public EccConfiguration getConfiguration() {
        logger.debug("Returning service configuration");

        // TO DO: Get project name from user
        String userSpecifiedProjectName = "EX_ID";

        return configurationService.getRemoteConfiguration(userSpecifiedProjectName);
    }

    /**
     * @return true if the service is configured (i.e., is ready to retrieve
     * configurations based on project names)
     */
    @RequestMapping(method = RequestMethod.GET, value = "/ifconfigured")
    @ResponseBody
    public boolean ifConfigured() {
        logger.debug("Returning service configuration status");
        return configurationService.isInitialised();
    }

    /**
     * @param projectName name of the project.
     * @return configuration for that project.
     */
    @RequestMapping(method = RequestMethod.GET, value = "/project/{projectName}")
    @ResponseBody
    public EccConfiguration getConfigurationForProject(@PathVariable String projectName) {
        logger.debug("Returning service configuration for project '" + projectName + "'");

        // TODO: not sure what the intention for this is; have updated as I think fits
        return configurationService.getRemoteConfiguration(projectName);
    }

    /**
     * Updates the configuration of the service.
     *
     * @param newEccConfiguration new configuration.
     * @return true if configuration was updated successfully and the service is
     * now configured.
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public boolean setConfiguration(@RequestBody EccConfiguration newEccConfiguration) {
        logger.debug("Setting new configuration");
        try {
            // TODO make safe
            ObjectMapper mapper = new ObjectMapper();
            logger.debug(JSONObject.fromObject(mapper.writeValueAsString(newEccConfiguration)).toString(2));

            // TODO: Need project name associated with configuration
            String userSpecifiedProjectName = "EX_ID";
            configurationService.updateConfiguration(userSpecifiedProjectName, newEccConfiguration);

            return true;
        } catch (Throwable ex) {
            // TODO improve this
            logger.error("Failed to set service configuration", ex);
            return false;
        }
    }

}
