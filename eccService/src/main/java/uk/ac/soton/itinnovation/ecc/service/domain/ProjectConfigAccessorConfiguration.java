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
//	Created Date :			2014-04-10
//	Created for Project :           EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.ecc.service.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Contains settings to access configuration.experimedia.eu.
 */
public class ProjectConfigAccessorConfiguration {

    private String endpoint;
    private String projectName;
    private String username;
    private String password;
    private String whitelist;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(String whitelist) {
        this.whitelist = whitelist;
    }

    /**
     * @return whitelisted projects as a sorted array list.
     */
    public ArrayList<String> getSortedWhiteList() {
        ArrayList<String> result = new ArrayList<String>();
        if (whitelist != null) {
            result.addAll(Arrays.asList(whitelist.split(",")));
            if (result.size() > 1) {
                Collections.sort(result);
            }
        }
        return result;
    }

    /**
     * @return whitelisted projects as a sorted JSON array.
     */
    public JSONArray getSortedWhiteListAsJsonArray() {
        JSONArray result = new JSONArray();

        for (String e : getSortedWhiteList()) {
            result.add(e);
        }

        return result;
    }

    /**
     * @return simple net.sf.json.JSONObject representation.
     */
    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        result.put("endpoint", endpoint);
        result.put("projectName", projectName);
        result.put("username", username);
        result.put("password", password);
        result.put("whitelist", whitelist);
        result.put("whitelist_json", getSortedWhiteListAsJsonArray());

        return result;
    }

}
