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
//	Created By :			Maxim Bashevoy, Simon Crowle
//	Created Date :			2014-04-01
//	Created for Project :           EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.ecc.service.services;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.configRegistry.api.ECCConfigAPIFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.configRegistry.api.IECCProjectConfig;
import uk.ac.soton.itinnovation.ecc.service.configuration.LocalConfiguration;
import uk.ac.soton.itinnovation.ecc.service.domain.DatabaseConfiguration;
import uk.ac.soton.itinnovation.ecc.service.domain.EccConfiguration;
import uk.ac.soton.itinnovation.ecc.service.domain.MiscConfiguration;
import uk.ac.soton.itinnovation.ecc.service.domain.ProjectConfigAccessorConfiguration;
import uk.ac.soton.itinnovation.ecc.service.domain.RabbitConfiguration;
import uk.ac.soton.itinnovation.ecc.service.utils.Validate;

/**
 * Deals with the configuration.
 */
@Service("configurationService")
@Configuration
@EnableConfigurationProperties(LocalConfiguration.class)
public class ConfigurationService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String component = "ECC";
    private final String featureRt = "RabbitMQ";
    private final String featureDb = "Database";
    private final String featureDh = "Dashboard";

    private boolean initialised = false;
    private boolean configurationSet = false;
    private boolean webdavServiceOnline = false; // TODO
    private boolean servicesStarted = false;

    private ArrayList<String> whiteListedOnlineProjects = new ArrayList<String>();

    private EccConfiguration localEccConfiguration, selectedEccConfiguration;

    private String configUsername, configPassword;

    @Autowired
    ExperimentService experimentService;

    @Autowired
    DataService dataService;
    
    @Autowired
    ExplorerService explorerService;

    @Autowired
    LocalConfiguration localConfiguration;

    public ConfigurationService() {
    }

    /**
     * Initialises the service: checks if default configuration is available.
     */
    @PostConstruct
    public void init() {

        logger.debug("Initialising configuration service");

        if (localConfiguration == null) {
            logger.error("Failed to get local configuration, check 'application.properties' file is on classpath");
        } else {
            EccConfiguration eccConfiguration = localConfiguration.getConfiguration();
            if (eccConfiguration == null) {
                logger.error("Failed to get default ECC configuration from local configuration, check 'application.properties' file contains ecc.configuration.* entries");
            } else {
//                logger.debug("Found default ECC configuration:\n" + eccConfiguration.toJson().toString(2));
                ProjectConfigAccessorConfiguration projectConfigAccessorConfiguration = localConfiguration.getProjectconfig();

                if (projectConfigAccessorConfiguration == null) {
                    logger.error("Failed to get default ProjectConfigAccessorConfiguration from local configuration, check 'application.properties' file contains ecc.projectconfig.* entries");
                } else {
                    logger.debug("Found default ProjectConfigAccessorConfiguration configuration:\n" + projectConfigAccessorConfiguration.toJson().toString(2));

                    // TODO: validate eccConfiguration, projectConfigAccessorConfiguration
                    localEccConfiguration = eccConfiguration;
                    configUsername = projectConfigAccessorConfiguration.getUsername();
                    configPassword = projectConfigAccessorConfiguration.getPassword();

                    initialised = true;
                    logger.debug("Configuration service initialised successfully, checking webdav service availability");

                    Sardine sardine = SardineFactory.begin(projectConfigAccessorConfiguration.getUsername(), projectConfigAccessorConfiguration.getPassword());
                    try {
                        List<DavResource> resources = sardine.getResources(projectConfigAccessorConfiguration.getEndpoint());
                        for (DavResource res : resources) {
                            if (projectConfigAccessorConfiguration.getSortedWhiteList().contains(res.getName())) {
                                logger.debug("Adding project '" + res.getName() + "' to the list of online configurations");
                                whiteListedOnlineProjects.add(res.getName());
                            }
                        }
                        if (whiteListedOnlineProjects.size() > 1) {
                            Collections.sort(whiteListedOnlineProjects);
                        }
                        webdavServiceOnline = true;
                    } catch (IOException ex) {
                        logger.error("Failed to connect to webdav service", ex.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Ensures the service is shut down properly.
     */
    @PreDestroy
    public void shutdown() {
        logger.debug("Shutting down configuration service");

        logger.debug("Configuration service shut down");
    }

    /**
     * Starts all services if fully configured and configuration selected.
     *
     * @return true if all service started successfully.
     */
    public boolean startServices() {
        
        boolean result = false;
        
        if (startExperimentService())
            if (startDataService())
                if (startExplorerService())
                    result = true;
        
        servicesStarted = result;
        
        return result;
    }

    /**
     * Attempts to stop both experiment and data services.
     *
     * @return false on fail.
     */
    public boolean stopServices() {
        if (!initialised) {
            logger.error("Failed to stop services: configuration service not initialised");
            return false;
        } else {
            boolean result;
            if (experimentService.isStarted()) {
                result = experimentService.stop();
            } else {
                logger.error("Failed to stop experiment service: experiment service not started");
                result = false;
            }

            if (result) {
                if (dataService.isStarted()) {
                    result = dataService.stop();
                } else {
                    logger.error("Failed to stop experiment service: experiment service not started");
                    result = false;
                }
            }

            return result;
        }
    }

    /**
     * Reset the service to force reconfiguration.
     *
     * @return false on fail.
     */
    public boolean reset() {
        if (stopServices()) {
            logger.debug("Resetting configuration service variables");
            servicesStarted = false;
            configurationSet = false;
            selectedEccConfiguration = null;
            return true;
        } else {
            logger.error("Failed to stop the services");
            return false;
        }

    }

    /**
     * Starts experiment service.
     *
     * @return true if the experiment service started successfully.
     */
    public boolean startExperimentService() {
        if (!initialised) {
            logger.error("Failed to start experiment service: configuration service not initialised");
            return false;
        } else {
            if (!configurationSet) {
                logger.error("Failed to start services: configuration not selected");
                return false;
            } else {
                logger.debug("Starting experiment service");
                if (!experimentService.start(selectedEccConfiguration.getDatabaseConfig(), selectedEccConfiguration.getRabbitConfig())) {
                    logger.error("Failed to start experiment service");
                    return false;
                } else {
                    logger.debug("Successfully started experiment service");
                    return true;
                }
            }
        }
    }

    /**
     * Starts the data service.
     *
     * @return true if the data service started successfully.
     */
    public boolean startDataService() {
        if (!initialised) {
            logger.error("Failed to start data service: configuration service not initialised");
            return false;
        } else {
            if (!configurationSet) {
                logger.error("Failed to start data service: configuration not selected");
                return false;
            } else {
                logger.debug("Starting data service");
                if (!dataService.start(selectedEccConfiguration.getDatabaseConfig())) {
                    logger.error("Failed to start data service");
                    return false;
                } else {
                    logger.debug("Successfully started data service");
                    return true;
                }
            }
        }
    }
    
    public boolean startExplorerService() {
        if (!initialised) {
            logger.error("Failed to start explorer service: configuration service not initialised");
            return false;
        } else {
            if (!configurationSet) {
                logger.error("Failed to start data service: configuration not selected");
                return false;
            } else {
                logger.info("Starting explorer service");
                if (!explorerService.start(selectedEccConfiguration.getDatabaseConfig())) {
                    logger.error("Failed to start explorer service");
                    return false;
                } else {
                    logger.info("Successfully started explorer service");
                    return true;
                }
            }
        }
    }

    public boolean isInitialised() {
        return initialised;
    }

    public boolean isConfigurationSet() {
        return configurationSet;
    }

    public boolean isServicesStarted() {
        return servicesStarted;
    }

    public ArrayList<String> getWhiteListedOnlineProjects() {
        return whiteListedOnlineProjects;
    }

    public void setWhiteListedOnlineProjects(ArrayList<String> whiteListedOnlineProjects) {
        this.whiteListedOnlineProjects = whiteListedOnlineProjects;
    }

    /**
     * @return Configuration read from application.properties if initialised,
     * null otherwise.
     */
    public EccConfiguration getLocalConfiguration() {
        if (initialised) {
            return localEccConfiguration;
        } else {
            return null;
        }
    }

    public EccConfiguration getSelectedEccConfiguration() {
        return selectedEccConfiguration;
    }

    public boolean selectEccConfiguration(EccConfiguration newConfiguration) {

        configurationSet = false;
        selectedEccConfiguration = null;

        if (initialised) {
            try {
                // Validate configuration before setting
                // TODO: get reason back, throwing exceptions didn't work for some reason
                configurationSet = (new Validate()).eccConfiguration(newConfiguration);
                if (configurationSet) {
                    selectedEccConfiguration = newConfiguration;
                }

            } catch (Exception ex) {
                logger.error("Could not select configuration: errors found", ex);
                configurationSet = false;
            }
        } else {
            logger.error("Could not select configuration: configuration service not initialised");
        }

        return configurationSet;
    }

    public EccConfiguration createDefaultConfiguration(String projectName) {
        localEccConfiguration.setProjectName(projectName);
        return localEccConfiguration;
    }

    /**
     * Use this method to retrieve an ECC configuration based on a project name
     * (given by the user) from ECCConfigAPIFactory.
     *
     * @param projectName - Non-null, non-empty string of the project name.
     * @return - Configuration for project (this may be default if input params
     * are invalid or configuration server is inaccessible.
     */
    public EccConfiguration getRemoteConfiguration(String projectName) {

        EccConfiguration config = null;

        // Try retrieving the configuration
        if (initialised && projectName != null && !projectName.isEmpty()) {

            try {

                IECCProjectConfig pc = ECCConfigAPIFactory.getProjectConfigAccessor(projectName, configUsername, configPassword);

                // Build configuration
                String configData;
                RabbitConfiguration rtc;
                DatabaseConfiguration dbc;
                MiscConfiguration mcc;

                // Rabbit data -------------------------------------------------
                if (pc.componentFeatureConfigExists(component, featureRt)) {
                    configData = pc.getConfigData(component, featureRt);
                } else {
                    configData = pc.getDefaultConfigData(component, featureRt);
                }

                rtc = createRabbitConfig(configData);

                // Database config ---------------------------------------------
                if (pc.componentFeatureConfigExists(component, featureDb)) {
                    configData = pc.getConfigData(component, featureDb);
                } else {
                    configData = pc.getDefaultConfigData(component, featureDb);
                }

                dbc = createDatabaseConfig(configData);

                // Misc config -------------------------------------------------
                if (pc.componentFeatureConfigExists(component, featureDh)) {
                    configData = pc.getConfigData(component, featureDh);
                } else {
                    configData = pc.getDefaultConfigData(component, featureDh);
                }

                mcc = createMiscConfig(configData);

                // Create full config & validate
                EccConfiguration ec = new EccConfiguration(projectName, rtc, dbc, mcc);

                if ((new Validate()).eccConfiguration(ec)) {
                    config = ec;
                } else {
                    logger.error("Configuration validation failed");
                }
            } catch (Exception ex) {

                String msg = "Could not retrieve configuration data for project " + projectName + " : " + ex.getMessage();
                logger.error(msg);
            }
        } else {
            logger.warn("Request for configuration that was null or empty;");
        }

//        // Otherwise fallback on default - no!
//        if (config == null) {
//            config = createDefaultConfiguration(projectName);
//        }
        return config;
    }

    /**
     * Use this method to update a project's configuration (usually after
     * modification of some part of this configuration by the user).
     *
     * @param config - Non-null, complete configuration to update the
     * configuration server
     * @throws javax.xml.bind.ValidationException if called when the service was
     * not initialized properly.
     */
    public void updateRemoteConfiguration(EccConfiguration config) throws Exception {

        if (!initialised) {
            throw new IllegalStateException("Failed to update configuration: service not initialised");
        }

        // Try validating the configuration first (throws up if there's a problem)
        if ((new Validate()).eccConfiguration(config)) {

            String projectName = config.getProjectName();

            logger.debug("Updating configuration for project '" + projectName + "'");

            IECCProjectConfig pc = ECCConfigAPIFactory.getProjectConfigAccessor(projectName, configUsername, configPassword);

            logger.debug("Removing old configuration for " + projectName);

            // Rabbit
            if (pc.componentFeatureConfigExists(component, featureRt)) {
                pc.deleteComponentFeatureConfig(component, featureRt);
            } else {
                pc.createComponentFeature(component, featureRt);
            }

            // Database
            if (pc.componentFeatureConfigExists(component, featureDb)) {
                pc.deleteComponentFeatureConfig(component, featureDb);
            } else {
                pc.createComponentFeature(component, featureDb);
            }

            // Dashboard
            if (pc.componentFeatureConfigExists(component, featureDh)) {
                pc.deleteComponentFeatureConfig(component, featureDh);
            } else {
                pc.createComponentFeature(component, featureDh);
            }

            // Write new configuration
            logger.info("Writing new configuration for " + projectName);

            pc.putComponentFeatureConfig(component, featureRt, createRabbitJSON(config.getRabbitConfig()));
            pc.putComponentFeatureConfig(component, featureDb, createDatabaseJSON(config.getDatabaseConfig()));
            pc.putComponentFeatureConfig(component, featureDh, createMiscJSON(config.getMiscConfig()));
        } else {
            logger.error("Configuration validation failed");
        }
    }

    // Private methods ---------------------------------------------------------
    private RabbitConfiguration createRabbitConfig(String configData) throws Exception {

        RabbitConfiguration rc = null;

        if (configData != null && !configData.isEmpty()) {

            rc = new RabbitConfiguration();

            JSONObject jo = (JSONObject) JSONSerializer.toJSON(configData);

            rc.setPort(jo.getString("Rabbit_Port"));
            rc.setUseSsl(jo.getBoolean("Rabbit_Use_SSL"));
            rc.setIp(jo.getString("Rabbit_IP"));
            rc.setMonitorId(UUID.fromString(jo.getString("Monitor_ID")));
            rc.setUserPassword(jo.getString("Rabbit_Password"));
            rc.setUserName(jo.getString("Rabbit_Username"));
        }

        return rc;
    }

    private String createRabbitJSON(RabbitConfiguration rc) throws Exception {

        JSONObject jo = new JSONObject();

        jo.put("Rabbit_Port", rc.getPort());
        jo.put("Rabbit_Use_SSL", rc.isUseSsl());
        jo.put("Rabbit_IP", rc.getIp());
        jo.put("Monitor_ID", rc.getMonitorId().toString());
        jo.put("Rabbit_Password", rc.getUserPassword());
        jo.put("Rabbit_Username", rc.getUserName());

        return jo.toString();
    }

    private DatabaseConfiguration createDatabaseConfig(String configData) throws Exception {

        DatabaseConfiguration dc = null;

        if (configData != null && !configData.isEmpty()) {

            dc = new DatabaseConfiguration();
            JSONObject jo = (JSONObject) JSONSerializer.toJSON(configData);

            dc.setUserPassword(jo.getString("dbPassword"));
            dc.setDatabaseName(jo.getString("dbName"));
            dc.setDatabaseType(jo.getString("dbType"));
            dc.setUrl(jo.getString("dbURL"));
            dc.setUserName(jo.getString("dbUsername"));
        }

        return dc;
    }

    private String createDatabaseJSON(DatabaseConfiguration dc) throws Exception {

        JSONObject jo = new JSONObject();

        jo.put("dbPassword", dc.getUserPassword());
        jo.put("dbName", dc.getDatabaseName());
        jo.put("dbType", dc.getDatabaseType());
        jo.put("dbURL", dc.getUrl());
        jo.put("dbUsername", dc.getUserName());

        return jo.toString();
    }

    private MiscConfiguration createMiscConfig(String configData) throws Exception {

        MiscConfiguration mc = null;

        if (configData != null && !configData.isEmpty()) {

            mc = new MiscConfiguration();

            JSONObject jo = (JSONObject) JSONSerializer.toJSON(configData);

            mc.setSnapshotCount(jo.getInt("livemonitor.defaultSnapshotCountMax"));
            mc.setNagiousUrl(jo.getString("nagios.fullurl"));
        }

        return mc;
    }

    private String createMiscJSON(MiscConfiguration mc) throws Exception {

        JSONObject jo = new JSONObject();

        jo.put("livemonitor.defaultSnapshotCountMax", mc.getSnapshotCount());
        jo.put("nagios.fullurl", mc.getNagiousUrl());

        return jo.toString();
    }
}
