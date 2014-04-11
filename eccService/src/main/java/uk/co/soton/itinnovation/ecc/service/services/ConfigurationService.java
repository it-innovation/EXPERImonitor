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
//                                      Simon Crowle
//
//	Created Date :			2014-04-01
//	Created for Project :           EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.co.soton.itinnovation.ecc.service.services;

import java.util.UUID;
import javax.annotation.PostConstruct;
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
import uk.co.soton.itinnovation.ecc.service.configuration.LocalConfiguration;
import uk.co.soton.itinnovation.ecc.service.domain.DatabaseConfiguration;
import uk.co.soton.itinnovation.ecc.service.domain.EccConfiguration;
import uk.co.soton.itinnovation.ecc.service.domain.MiscConfiguration;
import uk.co.soton.itinnovation.ecc.service.domain.ProjectConfigAccessorConfiguration;
import uk.co.soton.itinnovation.ecc.service.domain.RabbitConfiguration;

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

    private EccConfiguration localEccConfiguration, selectedEccConfiguration;

    private String configUsername, configPassword;

    @Autowired
    ExperimentService experimentService;

    @Autowired
    DataService dataService;

    @Autowired
    LocalConfiguration localConfiguration;

    public ConfigurationService() {
    }

    /**
     * Initialises the configuration service: checks if default configuration is
     * available.
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
                logger.debug("Found default ECC configuration:\n" + eccConfiguration.toJson().toString(2));
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
                    logger.debug("Configuration service initialised successfully");
                }
            }
        }

//        // See if it's possible to connect to the configuration service - not necessary
//        try {
//
//            String defaultProjectname = localConfiguration.getProjectconfig().getProjectName();
//            String defaultUsername = localConfiguration.getProjectconfig().getUsername();
//            String defaultPassword = localConfiguration.getProjectconfig().getPassword();
//
//            logger.debug("Connecting to project '" + defaultProjectname + "' using '" + defaultUsername + "':'" + defaultPassword + "'");
//            IECCProjectConfig pc = ECCConfigAPIFactory.getProjectConfigAccessor(defaultProjectname, defaultUsername, defaultPassword);
//            String url = pc.getProjectUrl();
//
//            configUsername = defaultUsername;
//            configPassword = defaultPassword;
//
//            logger.debug("Successfully retrieved default URL configuration using username and password: " + url);
//
//            serviceInitialised = true;
//            logger.debug("Successfully initialised configuration service");
//
//        } catch (Exception ex) {
//            logger.error("Failed to contact EXPERIMEDIA configuration service: " + ex.getMessage());
//        }
    }

    /**
     * Starts all services if fully configured and configuration selected.
     *
     * @return true if all service started successfully.
     */
    public boolean startServices() {
        if (!initialised) {
            logger.error("Failed to start services: configuration service not initialised");
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
                    logger.debug("Starting data service");
                    if (!dataService.start(selectedEccConfiguration.getDatabaseConfig())) {
                        logger.error("Failed to start data service");
                        return false;
                    } else {
                        logger.debug("Successfully started all services with selected configuration:\n" + selectedEccConfiguration.toJson().toString(2));
                        return true;
                    }
                }
            }
        }
    }

    public static boolean validateConfiguration(EccConfiguration config) throws Exception {

        final String err = "Cannot validate configuration: ";

        if (config == null) {
            throw new Exception(err + "config is null");
        }

        // Test essential Rabbit config
        RabbitConfiguration rc = config.getRabbitConfig();

        if (rc == null) {
            throw new Exception(err + "rabbit configuration is null");
        }
        if (rc.getMonitorId() == null) {
            throw new Exception(err + "ECC UUID is invalid");
        }
        if (rc.getIp() == null) {
            throw new Exception(err + "Rabbit server IP is null");
        }
        if (rc.getPort() == null) {
            throw new Exception(err + "Rabbit port is null");
        }
        if (rc.getUserName() == null) {
            throw new Exception(err + "Rabbit username is null");
        }
        if (rc.getUserPassword() == null) {
            throw new Exception(err + "Rabbit password is null");
        }

        // Test essential database config
        DatabaseConfiguration dc = config.getDatabaseConfig();

        if (dc == null) {
            throw new Exception(err + "database configuration is null");
        }
        if (dc.getDatabaseName() == null) {
            throw new Exception(err + "database name is null");
        }
        if (dc.getDatabaseType() == null) {
            throw new Exception(err + "database type is null");
        }
        if (dc.getUrl() == null) {
            throw new Exception(err + "database url is null");
        }
        if (dc.getUserName() == null) {
            throw new Exception(err + "database username is null");
        }
        if (dc.getUserPassword() == null) {
            throw new Exception(err + "database password is null");
        }

        // Test essential misc config
        MiscConfiguration mc = config.getMiscConfig();

        if (mc == null) {
            throw new Exception(err + "Misc configuration is null");
        }
        if (mc.getSnapshotCount() < 1) {
            throw new Exception(err + "Snapshot count is 0 or less");
        }
        if (mc.getNagiousUrl() == null) {
            throw new Exception(err + "No NAGIOS configuration found");
        }

        return true;
    }

    public boolean isInitialised() {
        return initialised;
    }

    public boolean isConfigurationSet() {
        return configurationSet;
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
        selectedEccConfiguration = newConfiguration;
        configurationSet = true;

        // TODO: check if it's OK to change the configuration;
        return true;
    }

    private EccConfiguration createDefaultConfiguration(String projectName) {
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

        // Generate a default configuration
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

                validateConfiguration(ec);
                config = ec;
            } catch (Exception ex) {

                String msg = "Could not retreive configuration data for project " + projectName + " : " + ex.getMessage();
                logger.error(msg);
            }
        } else {
            logger.warn("Request for configuration that was null or empty;");
        }

        // Otherwise fallback on default
        if (config == null) {
            config = createDefaultConfiguration(projectName);
        }

        return config;
    }

    /**
     * Use this method to update a project's configuration (usually after
     * modification of some part of this configuration by the user).
     *
     * @param projectName - Non-null, non-empty string of project name
     * @param config - Non-null, complete configuration to update the
     * configuration server
     * @throws java.lang.Exception - Throws if input parameters are invalid or
     * the configuration server is inaccessible
     */
    public void updateConfiguration(String projectName, EccConfiguration config) throws Exception {

        if (!initialised) {
            throw new Exception("Could not update configuration: service not initialised");
        }

        if (projectName != null && !projectName.isEmpty() && config != null) {

            validateConfiguration(config);

            // Update configuration on server here
            try {

                IECCProjectConfig pc = ECCConfigAPIFactory.getProjectConfigAccessor(projectName, configUsername, configPassword);

                // Delete old configurations if they exist
                logger.info("Removing old configuration for " + projectName);

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
                logger.info("Updating new configuration for " + projectName);

                pc.putComponentFeatureConfig(component, featureRt, createRabbitJSON(config.getRabbitConfig()));
                pc.putComponentFeatureConfig(component, featureDb, createDatabaseJSON(config.getDatabaseConfig()));
                pc.putComponentFeatureConfig(component, featureDh, createMiscJSON(config.getMiscConfig()));
            } catch (Exception ex) {
                String msg = "Failed to update configuration for project: " + projectName + "; " + ex.getMessage();
                logger.error(msg);

                throw new Exception(msg, ex);
            }
        } else {
            throw new Exception("Could not update configuration: invalid input parameters");
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
