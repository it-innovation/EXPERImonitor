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
//	Created Date :			2014-04-02
//	Created for Project :           EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.co.soton.itinnovation.ecc.service.services;

import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.soton.itinnovation.ecc.service.domain.DatabaseConfiguration;

/**
 * Provides access to data in the database.
 */
public class DataService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    ConfigurationService configurationService;

    private boolean started = false;
    private DatabaseConfiguration databaseConfig;

    public DataService() {
    }

    @PostConstruct
    public void init() {
        logger.debug("Initialising data service");

        logger.debug("Finished initialising data service");
    }

    /**
     * Starts the service with new settings.
     *
     * @return true if started successfully.
     */
    public boolean start() {
        // Check if configured, then:
        started = true;
        
        return true;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

}
