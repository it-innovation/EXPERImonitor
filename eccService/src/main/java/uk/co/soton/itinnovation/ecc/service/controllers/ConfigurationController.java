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
import java.util.ArrayList;
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
import uk.co.soton.itinnovation.ecc.service.utils.Validate;

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
     * @return configuration of this service, null if not yet configured.
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public EccConfiguration getSelectedConfiguration() {
        logger.debug("Returning selected service configuration");

        return configurationService.getSelectedEccConfiguration();
    }

    /**
     * @return true if the service is initialised (i.e., is ready to retrieve
     * configurations based on project names)
     */
    @RequestMapping(method = RequestMethod.GET, value = "/ifinitialised")
    @ResponseBody
    public boolean ifInitialised() {
        boolean result = configurationService.isInitialised();
        logger.debug("Returning service initialisation status: " + result);
        return result;
    }

    /**
     * @return true if the service is configured (i.e., configuration was
     * selected)
     */
    @RequestMapping(method = RequestMethod.GET, value = "/ifconfigured")
    @ResponseBody
    public boolean ifConfigured() {
        boolean result = configurationService.isConfigurationSet();
        logger.debug("Returning service configuration status: " + result);

        if (result) {
            logger.debug(configurationService.getSelectedEccConfiguration().toJson().toString(2));
        }
        return result;
    }

    /**
     * @return true if the service is configured (i.e., configuration was
     * selected)
     */
    @RequestMapping(method = RequestMethod.GET, value = "/ifservicesstarted")
    @ResponseBody
    public boolean ifServicesStarted() {
        boolean result = configurationService.isServicesStarted();
        logger.debug("Returning services started status: " + result);
        return result;
    }

    /**
     * @return local configuration (from application.properties).
     */
    @RequestMapping(method = RequestMethod.GET, value = "/local")
    @ResponseBody
    public EccConfiguration getLocalConfiguration() {
        logger.debug("Returning local service configuration");

        return configurationService.getLocalConfiguration();
    }

    /**
     * @return names for whitelisted online projects.
     */
    @RequestMapping(method = RequestMethod.GET, value = "/projects")
    @ResponseBody
    public ArrayList<String> getWhitelistedOnlinesConfigurations() {
        ArrayList<String> result = configurationService.getWhiteListedOnlineProjects();
        logger.debug("Returning names of whitelisted online projects");
        return result;
    }

    /**
     * @param projectName name of the project.
     * @return configuration for that project.
     */
    @RequestMapping(method = RequestMethod.GET, value = "/projects/{projectName}")
    @ResponseBody
    public EccConfiguration getConfigurationForProject(@PathVariable String projectName) {
        logger.debug("Returning service configuration for project '" + projectName + "'");

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

            // TODO: make safe
            ObjectMapper mapper = new ObjectMapper();
            logger.debug(JSONObject.fromObject(mapper.writeValueAsString(newEccConfiguration)).toString(2));

            // Validate configuration
            if ((new Validate()).eccConfiguration(newEccConfiguration)) {
                if (configurationService.selectEccConfiguration(newEccConfiguration)) {
                    if (newEccConfiguration.isRemote()) {
                        logger.debug("Set and save new configuration on WebDAV");
                        configurationService.updateRemoteConfiguration(newEccConfiguration);
                    } else {
                        logger.debug("Just set new configuration");
                    }

                    return configurationService.startServices();
                } else {
                    return false;
                }
            } else {
                logger.error("Submitted configuration is not valid!");
                return false;
            }
        } catch (Throwable ex) {
            // TODO improve this
            logger.error("Failed to set service configuration", ex);
            return false;
        }
    }

}
