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
package uk.co.soton.itinnovation.ecc.service.domain;

import net.sf.json.JSONObject;

/**
 * Service configuration object.
 */
public class EccConfiguration {

    private String projectName;
    private RabbitConfiguration rabbitConfig;
    private DatabaseConfiguration databaseConfig;
    private MiscConfiguration miscConfig;

    public EccConfiguration() {
    }

    public EccConfiguration(String projectName, RabbitConfiguration rabbitConfig, DatabaseConfiguration databaseConfig, MiscConfiguration miscConfig) {
        this.projectName = projectName;
        this.rabbitConfig = rabbitConfig;
        this.databaseConfig = databaseConfig;
        this.miscConfig = miscConfig;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public RabbitConfiguration getRabbitConfig() {
        return rabbitConfig;
    }

    public void setRabbitConfig(RabbitConfiguration rabbitConfig) {
        this.rabbitConfig = rabbitConfig;
    }

    public DatabaseConfiguration getDatabaseConfig() {
        return databaseConfig;
    }

    public void setDatabaseConfig(DatabaseConfiguration databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public MiscConfiguration getMiscConfig() {
        return miscConfig;
    }

    public void setMiscConfig(MiscConfiguration miscConfig) {
        this.miscConfig = miscConfig;
    }

    /**
     * @return simple net.sf.json.JSONObject representation.
     */
    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        result.put("projectName", projectName);
        if (rabbitConfig == null) {
            result.put("rabbitConfig", null);
        } else {
            result.put("rabbitConfig", rabbitConfig.toJson());
        }
        if (databaseConfig == null) {
            result.put("databaseConfig", null);
        } else {
            result.put("databaseConfig", databaseConfig.toJson());
        }
        if (miscConfig == null) {
            result.put("miscConfig", null);
        } else {
            result.put("miscConfig", miscConfig.toJson());
        }

        return result;
    }

}
