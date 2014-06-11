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
//	Created Date :			2014-04-11
//	Created for Project :           EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.ecc.service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.soton.itinnovation.ecc.service.domain.DatabaseConfiguration;
import uk.ac.soton.itinnovation.ecc.service.domain.EccConfiguration;
import uk.ac.soton.itinnovation.ecc.service.domain.MiscConfiguration;
import uk.ac.soton.itinnovation.ecc.service.domain.RabbitConfiguration;

/**
 * Validates objects.
 */
public class Validate {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Tests all parameters of an {@link EccConfiguration}.
     *
     * @param config configuration to validate.
     * @return
     */
    public boolean eccConfiguration(EccConfiguration config) {

        if (config == null) {
            logger.error("ECC configuration error: provided configuration is NULL");
            return false;
        }

        // Project name tests
        String projectName = config.getProjectName();

        if (projectName == null) {
            logger.error("ECC configuration error: project name is NULL");
            return false;
        }

        if (projectName.isEmpty()) {
            logger.error("ECC configuration error: project name is EMPTY");
            return false;
        }

        // Test essential Rabbit config
        RabbitConfiguration rc = config.getRabbitConfig();

        if (rc == null) {
            logger.error("ECC configuration error: Rabbit configuration is null");
            return false;
        }
        if (rc.getMonitorId() == null) {
            logger.error("ECC configuration error: ECC UUID is invalid");
            return false;
        }
        if (rc.getIp() == null) {
            logger.error("ECC configuration error: Rabbit server IP is null");
            return false;
        }
        if (rc.getPort() == null) {
            logger.error("ECC configuration error: Rabbit port is null");
            return false;
        }
        if (rc.getUserName() == null) {
            logger.error("ECC configuration error: Rabbit username is null");
            return false;
        }
        if (rc.getUserPassword() == null) {
            logger.error("ECC configuration error: Rabbit password is null");
            return false;
        }

        // Test essential database config
        DatabaseConfiguration dc = config.getDatabaseConfig();

        if (dc == null) {
            logger.error("ECC configuration error: Database configuration is null");
            return false;
        }
        if (dc.getDatabaseName() == null) {
            logger.error("ECC configuration error: Database name is null");
            return false;
        }
        if (dc.getDatabaseType() == null) {
            logger.error("ECC configuration error: Database type is null");
            return false;
        }
        if (dc.getUrl() == null) {
            logger.error("ECC configuration error: Database url is null");
            return false;
        }
        if (dc.getUserName() == null) {
            logger.error("ECC configuration error: Database username is null");
            return false;
        }
        if (dc.getUserPassword() == null) {
            logger.error("ECC configuration error: Database password is null");
            return false;
        }

        // Test essential misc config
        MiscConfiguration mc = config.getMiscConfig();

        if (mc == null) {
            logger.error("ECC configuration error: Misc configuration is null");
            return false;
        }
        if (mc.getSnapshotCount() < 1) {
            logger.error("ECC configuration error: Snapshot count is 0 or less");
            return false;
        }
        if (mc.getNagiousUrl() == null) {
            logger.error("ECC configuration error: No NAGIOS configuration found");
            return false;
        }

        return true;
    }
}
