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
package uk.co.soton.itinnovation.ecc.service.utils;

import javax.xml.bind.ValidationException;
import uk.co.soton.itinnovation.ecc.service.domain.DatabaseConfiguration;
import uk.co.soton.itinnovation.ecc.service.domain.EccConfiguration;
import uk.co.soton.itinnovation.ecc.service.domain.MiscConfiguration;
import uk.co.soton.itinnovation.ecc.service.domain.RabbitConfiguration;

/**
 * Validates objects.
 */
public class Validate {

    /**
     * Tests all parameters of an {@link EccConfiguration}.
     *
     * @param config configuration to validate.
     * @throws ValidationException in case of an error.
     */
    public static void eccConfiguration(EccConfiguration config) throws ValidationException {

        if (config == null) {
            throw new ValidationException("ECC configuration error: provided configuration is NULL");
        }
        
        // Project name tests
        String projectName = config.getProjectName();

        if (projectName == null) {
            throw new IllegalArgumentException("ECC configuration error: project name is NULL");
        }

        if (projectName.isEmpty()) {
            throw new IllegalArgumentException("ECC configuration error: project name is EMPTY");
        }

        // Test essential Rabbit config
        RabbitConfiguration rc = config.getRabbitConfig();

        if (rc == null) {
            throw new ValidationException("ECC configuration error: Rabbit configuration is null");
        }
        if (rc.getMonitorId() == null) {
            throw new ValidationException("ECC configuration error: ECC UUID is invalid");
        }
        if (rc.getIp() == null) {
            throw new ValidationException("ECC configuration error: Rabbit server IP is null");
        }
        if (rc.getPort() == null) {
            throw new ValidationException("ECC configuration error: Rabbit port is null");
        }
        if (rc.getUserName() == null) {
            throw new ValidationException("ECC configuration error: Rabbit username is null");
        }
        if (rc.getUserPassword() == null) {
            throw new ValidationException("ECC configuration error: Rabbit password is null");
        }

        // Test essential database config
        DatabaseConfiguration dc = config.getDatabaseConfig();

        if (dc == null) {
            throw new ValidationException("ECC configuration error: Database configuration is null");
        }
        if (dc.getDatabaseName() == null) {
            throw new ValidationException("ECC configuration error: Database name is null");
        }
        if (dc.getDatabaseType() == null) {
            throw new ValidationException("ECC configuration error: Database type is null");
        }
        if (dc.getUrl() == null) {
            throw new ValidationException("ECC configuration error: Database url is null");
        }
        if (dc.getUserName() == null) {
            throw new ValidationException("ECC configuration error: Database username is null");
        }
        if (dc.getUserPassword() == null) {
            throw new ValidationException("ECC configuration error: Database password is null");
        }

        // Test essential misc config
        MiscConfiguration mc = config.getMiscConfig();

        if (mc == null) {
            throw new ValidationException("ECC configuration error: Misc configuration is null");
        }
        if (mc.getSnapshotCount() < 1) {
            throw new ValidationException("ECC configuration error: Snapshot count is 0 or less");
        }
        if (mc.getNagiousUrl() == null) {
            throw new ValidationException("ECC configuration error: No NAGIOS configuration found");
        }
    }
}
