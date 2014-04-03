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
//										Simon Crowle
//
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
    
    private boolean serviceInitialised = false;

    @Autowired
    ExperimentService experimentService;
    
    public ConfigurationService() {
    }

    /**
     * Initialises the configuration service. This will involve attempt to connect
     * to the EXPERIMEDIA configuration server - if this fails, this service 
     * will only ever be able to provide a local configuration profile.
     * 
     * @throws - throws if the EXPERIMEDIA configuration server is inaccessible
     */
    @PostConstruct
    public void init() throws Exception {
        
        logger.info("Initialising configuration service");
        serviceInitialised = false;
  
        // Initialisation work here

        serviceInitialised = true;
        logger.info("Successfully initialised configuration service");   
    }

    public boolean isServiceInitialised() {
        return serviceInitialised;
    }

    /**
     * Use this method to retrieve an ECC configuration based on a project name 
     * (given by the user)
     * 
     * @param projectName - Non-null, non-empty string of the project name
     * @return            - Configuration for project (this may be default if input params are invalid
     *                      or configuration server is inaccessible.
     */
    public EccConfiguration getConfiguration(String projectName) {
        
        // Generate a default configuration
        EccConfiguration config = null;
        
        // Try retrieving the configuration
        if ( projectName != null && !projectName.isEmpty() )
        {
            
        }
        
        // Otherwise fallback on default
        if (config == null)
            config = createDefaultConfiguration(projectName); 
        
        return config;
    }

    /**
     * Use this method to update a project's configuration (usually after modification
     * of some part of this configuration by the user).
     * 
     * @param projectName   - Non-null, non-empty string of project name
     * @param config        - Non-null, complete configuration to update the configuration server
     * @throws              - Throws if input parameters are invalid or the configuration server is inaccessible
     */
    public void updateConfiguration(String projectName, EccConfiguration config) throws Exception {
        
        if ( projectName != null && !projectName.isEmpty() && config != null )
        {
            validateConfiguration(config);
            
            // Update configuration on server here
        }
    }

    // Private methods ---------------------------------------------------------
    private EccConfiguration createDefaultConfiguration(String projectName) {
        
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
    
    private void validateConfiguration( EccConfiguration config ) throws Exception
    {
        
    }
}
