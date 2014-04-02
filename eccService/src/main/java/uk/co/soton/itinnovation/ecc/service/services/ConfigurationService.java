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
package uk.co.soton.itinnovation.ecc.service.services;

import java.util.UUID;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.soton.itinnovation.ecc.service.domain.DatabaseConfiguration;
import uk.co.soton.itinnovation.ecc.service.domain.EccConfiguration;
import uk.co.soton.itinnovation.ecc.service.domain.MiscConfiguration;
import uk.co.soton.itinnovation.ecc.service.domain.RabbitConfiguration;

/**
 * Deals with the configuration.
 */
@Service
public class ConfigurationService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private EccConfiguration configuration;
    private boolean serviceConfigured = false;

    @Autowired
    ExperimentService experimentService;

    public ConfigurationService() {
    }

    @PostConstruct
    public void init() {
        logger.debug("Initialising configuration service");

        logger.debug("Finished initialising configuration service");
    }

    public boolean isServiceConfigured() {
        return serviceConfigured;
    }

    public void setServiceConfigured(boolean serviceConfigured) {
        this.serviceConfigured = serviceConfigured;

    }

    public EccConfiguration getConfiguration() {
        return configuration;
    }

    public String getProjectName() {
        if (configuration == null) {
            return null;
        } else {
            return configuration.getProjectName();
        }
    }

    public RabbitConfiguration getRabbitConfiguration() {
        if (configuration == null) {
            return null;
        } else {
            return configuration.getRabbitConfig();
        }
    }

    public DatabaseConfiguration getDatabaseConfiguration() {
        if (configuration == null) {
            return null;
        } else {
            return configuration.getDatabaseConfig();
        }
    }

    public MiscConfiguration getMiscConfiguration() {
        if (configuration == null) {
            return null;
        } else {
            return configuration.getMiscConfig();
        }
    }

    public void setConfiguration(EccConfiguration configuration) {
        this.configuration = configuration;
        serviceConfigured = true;
    }

    public EccConfiguration lookUpConfiguration(String projectName) {

        // TODO: fetch data from Experimedia config service.
        EccConfiguration result = new EccConfiguration();

        RabbitConfiguration rabbitConfig = new RabbitConfiguration(
                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                "127.0.0.1",
                "5672",
                "guest",
                "guest",
                "",
                false);

        DatabaseConfiguration databaseConfig = new DatabaseConfiguration(
                "localhost:5432",
                "edm-metrics",
                "postgres",
                "postgres",
                "postgresql");

        MiscConfiguration miscConfig = new MiscConfiguration(
                50,
                "http://username:password@host/nagios");

        result.setProjectName(projectName);
        result.setRabbitConfig(rabbitConfig);
        result.setDatabaseConfig(databaseConfig);
        result.setMiscConfig(miscConfig);

        return result;
    }

}
