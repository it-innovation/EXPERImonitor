/////////////////////////////////////////////////////////////////////////
//
// ¬© University of Southampton IT Innovation Centre, 2011
//
// Copyright in this library belongs to the University of Southampton
// University Road, Highfield, Southampton, UK, SO17 1BJ
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
//	Created Date :			2011-08-22
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator;

import eu.wegov.coordinator.dao.mgt.WegovPolicymaker;
import eu.wegov.coordinator.dao.mgt.WegovPolicymakerRole;
import eu.wegov.coordinator.dao.mgt.WegovPolicymaker_PolicymakerRole;
import eu.wegov.coordinator.sql.SqlSchema;
import eu.wegov.coordinator.utils.Util;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 * Policymakers are Dashboard Users with different roles.
 *
 * @author Maxim Bashevoy
 */
public class Policymaker {
    private int id;
    private String name;
    private String organisation;
    private Timestamp startDate;
    private Timestamp endDate;

    private Coordinator coordinator;
    private WegovPolicymaker wegovPolicymaker;  // only temporary but useful object!

    private SqlSchema mgtSchema;

    private Util util = new Util();

    private final static Logger logger = Logger.getLogger(Policymaker.class.getName());

    
    /**
     * Create new policymaker by specifying every parameter.
     * Additional flag for whether to encrypt password or not
     */
    Policymaker(String name, Role role, String organisation, String username, String password, Timestamp startDate, Timestamp endDate, Coordinator coordinator, boolean encryptPassword) throws SQLException {
        this.id = 0;
        this.name = name;
        this.organisation = organisation;
        this.startDate = startDate;
        this.endDate = endDate;

        if (role == null)
            throw new RuntimeException("Attempt to create policymaker with null role!");

        if ( (username == null) | (username.trim().equals("")) )
            throw new RuntimeException("Attempt to create policymaker with null or empty username!");

        logger.debug("Creating new Policymaker: \'" + name + "\' (" + username + "), \'" + role.getName() + " (" + role.getDescription() + ")" + "\', \'" + organisation + "\', \'" + startDate + "\', \'" + endDate + "\'");

        this.coordinator = coordinator;
        this.mgtSchema = coordinator.getMgtSchema();

        if (!mgtSchema.getAllWhere(new WegovPolicymaker(), "Username", username).isEmpty())
            throw new RuntimeException("Policymaker with username \'" + username + "\' already exists! Choose different username");

        String ecryptedPassword = null;
        
        if (encryptPassword) {
        	ecryptedPassword = util.makeShaHash(password);
        }
        else {
        	ecryptedPassword = password; // password is already encrypted
        }
        
        this.wegovPolicymaker = new WegovPolicymaker(name, organisation, username, ecryptedPassword, startDate, endDate);

        String iDfromDatabase = coordinator.getMgtSchema().insertObject(wegovPolicymaker);

        logger.debug("Got database ID: \'" + iDfromDatabase + "\'");

        if (iDfromDatabase != null) {
            this.id = Integer.parseInt(iDfromDatabase);
        } else {
            throw new SQLException("Failed to assign database ID to policymaker: \'" + name + "\'.");
        }

        // Sort out roles:
        this.addRole(role);

        // Create settings
        coordinator.createPolicymakerSetting(id, "language", "en");
        coordinator.createPolicymakerSetting(id, "refreshWidgetsOnPageReload", "yes");

        // Create default widgetset
        int wsId = coordinator.createWidgetSet(id, "Default Widget Set", "Default set of widgets for " + name, 1);

        coordinator.createWidget(wsId, "columnleft", 0, id, "My Locations", "This chart shows your chosen locations", "settings", "location", "gmap", "", "", 0, "");
        //coordinator.createWidget(wsId, "columnmid", 0, id, "My Saved Locations", "This chart shows all your saved locations", "settings", "mylocations", "gmaps", "", "", 0, "");
  /*
  * coordinator.createWidget(wsId, "columnright", 0, id, "My Locations", "This chart shows your chosen locations", "location", "gmap", "", "", 0);
        coordinator.createWidget(wsId, "columnright", 0, id, "Recent Local Posts on", "Recent tweets for current location", "twitterLocal", "posts", "", "{\"term\":\"Klaus Wowereit\"}", 0);
        coordinator.createWidget(wsId, "columnleft", 1, id, "Main Topics on", "This chart shows topic analysis", "topicanalysis", "analysis", "", "{\"term\":\"kernenergie\"}", 0);
        coordinator.createWidget(wsId, "columnright", 1, id, "Main Local Topics on", "This chart shows topic analysis", "topicanalysis", "analysis", "", "{\"term\":\"Klaus Wowereit\", \"location\":\"current\"}", 0);
        coordinator.createWidget(wsId, "columnleft", 2, id, "Main users for", "User roles for search term", "roleforterm", "analysis", "", "{\"term\":\"mindestlohn\", \"role\":\"Rare Poster\"}", 0);
        coordinator.createWidget(wsId, "columnright", 2, id, "User Roles for", "This chart shows user distribution per role", "userroles", "analysis", "", "{\"term\":\"hacking report\"}", 0);
        coordinator.createWidget(wsId, "columnleft", 3, id, "Facebook Posts for", "This chart shows current posts for the selected group", "groupposts", "posts", "", "{\"term\":\"AngelaMerkel\"}", 0);
        coordinator.createWidget(wsId, "columnright", 3, id, "Facebook Topics for Latest Post from", "This chart shows topic analysis for the latest post from this user page", "latestgroupposttopicanalysis", "analysis", "", "{\"term\":\"AngelaMerkel\"}", 0);
        coordinator.createWidget(wsId, "columnleft", 4, id, "Comments on Facebook Post", "This chart shows comments for the selected post", "grouppostcomments", "posts", "", "{\"term\":\"59788447049_290786810982672\"}", 0);
        coordinator.createWidget(wsId, "columnright", 4, id, "Facebook Post Topics", "This chart shows topic analysis for the selected group post", "groupposttopicanalysis", "analysis", "", "{\"term\":\"59788447049_379659095395726\"}", 0);
        coordinator.createWidget(wsId, "columnleft", 5, id, "Trending now in", "Trending topics for location", "trending", "analysis", "", "{\"term\":\"Deutschland\"}", 0);
        coordinator.createWidget(wsId, "columnleft", 5, id, "Peerindex for", "This chart shows peerindex", "peerindex", "main3", "", "{\"term\":\"Atomausstieg\"}", 0);
        //coordinator.createWidget(wsId, "widgets", 0, id, "All My Activities", "This chart shows your all activities", "allactivities", "", "", "", 1);

        coordinator.createWidget(wsId, "columnright", 6, id, "Recent Tweets on", "Recent tweets", "twitterbasic", "posts", "", "{\"term\":\"beatles\"}", 0);

        coordinator.createWidget(wsId, "columnleft", 6, id, "Main Topics on", "This chart shows topic analysis using tweets from wegov DB", "topics_from_database", "analysis", "", "{\"term\":\"anything\"}", 0);
*/

    }

    /**
     * End date is null.
     */
    Policymaker(String name, Role role, String organisation, String username, String unecryptedPassword, Timestamp startDate, Coordinator coordinator) throws SQLException {
        this(name, role, organisation, username, unecryptedPassword, startDate, null, coordinator, true);
    }

    /**
     * End date is null and start date is now.
     */
    Policymaker(String name, Role role, String organisation, String username, String unecryptedPassword, Coordinator coordinator) throws SQLException {
        this(name, role, organisation, username, unecryptedPassword, (new Util()).getTimeNowAsTimestamp(), null, coordinator, true);
    }

    /**
     * End date is null and start date is now.
     * Flag to encrypt password or not
     */
    Policymaker(String name, Role role, String organisation, String username, String password, Coordinator coordinator, boolean encryptPassword) throws SQLException {
        this(name, role, organisation, username, password, (new Util()).getTimeNowAsTimestamp(), null, coordinator, encryptPassword);
    }

    /**
     * End date is null and start date is now and policymaker is default user.
     */
    Policymaker(String name, String organisation, String username, String unecryptedPassword, Coordinator coordinator) throws SQLException {
        this(name, coordinator.getDefaultUserRole(), organisation, username, unecryptedPassword, (new Util()).getTimeNowAsTimestamp(), null, coordinator, true);
    }

    /**
     * Get from database, only for internal use.
     */
    Policymaker(int id, Coordinator coordinator) throws SQLException {
        this.id = id;
        this.coordinator = coordinator;
        this.mgtSchema = coordinator.getMgtSchema();

        logger.debug("Getting existing policymaker with ID: " + id);

        ArrayList<WegovPolicymaker> pms = coordinator.getMgtSchema().getAllWhere(new WegovPolicymaker(), "ID", id);

        if (pms.isEmpty())
            throw new SQLException("Failed to find policymaker with database ID: \'" + id + "\'.");

        this.wegovPolicymaker = (WegovPolicymaker) pms.get(0);

        this.name = wegovPolicymaker.getName();
        this.organisation = wegovPolicymaker.getOrganisation();
        this.startDate = wegovPolicymaker.getStartDate();
        this.endDate = wegovPolicymaker.getEndDate();
    }

    private void updateWegovPolicymaker() {
        try {
            wegovPolicymaker = (WegovPolicymaker) mgtSchema.getAllWhere(wegovPolicymaker, "ID", id).get(0);
        } catch (SQLException ex) {
            logger.error("Failed to update from database policymaker\'s details with ID: " + id);
            logger.error(ex.toString());
        }
    }

    private void setValue(String name, Object value) throws SQLException {
        mgtSchema.updateRow(wegovPolicymaker, name, value, "ID", id);
    }

    private Object getValue(String name) throws SQLException {
        return mgtSchema.getColumnValue(wegovPolicymaker, name, "ID", id);
    }

    /**
     * Returns policymaker's database ID.
     */
    public int getID() {
        return id;
    }

    /**
     * Returns policymaker's name.
     */
    public String getName() throws SQLException {
        this.name = (String) getValue("Name");
        return name;
    }

    /**
     * Sets new name for the policymaker.
     */
    public void setName(String newName) throws SQLException {
        this.name = newName;
        logger.debug("Setting name to: \'" + newName + "\'");
        setValue("Name", newName);
    }

    /**
     * Returns policymaker's username.
     */
    public String getUserName() throws SQLException {
        return (String) getValue("Username");
    }

    /**
     * Sets new username for the policymaker.
     */
    public void setUserName(String newUserName) throws SQLException {
        logger.debug("Setting UserName to: \'" + newUserName + "\'");
        setValue("UserName", newUserName);
    }

    /**
     * Returns policymaker's organisation.
     */
    public String getOrganisation() throws SQLException {
        this.organisation = (String) getValue("Organisation");
        return organisation;
    }

    /**
     * Sets new organisation for the policymaker.
     */
    public void setOrganisation(String newOrganisation) throws SQLException {
        this.organisation = newOrganisation;
        logger.debug("Setting organisation to: \'" + newOrganisation + "\'");
        setValue("Organisation", newOrganisation);
    }

    /**
     * Returns start date for the policymaker.
     */
    public Timestamp getStartDate() throws SQLException {
        this.startDate = (Timestamp) getValue("StartDate");
        return startDate;
    }

    /**
     * Sets new start date for the policymaker.
     */
    public void setStartDate(Timestamp newStartDate) throws SQLException {
        this.startDate = newStartDate;
        logger.debug("Setting new startDate: \'" + newStartDate + "\'");
        setValue("StartDate", newStartDate);
    }

    /**
     * Returns end date for the policymaker.
     */
    public Timestamp getEndDate() throws SQLException {
        this.endDate = (Timestamp) getValue("EndDate");
        return endDate;
    }

    /**
     * Sets new end date for the policymaker.
     */
    public void setEndDate(Timestamp newEndDate) throws SQLException {
        this.endDate = newEndDate;
        logger.debug("Setting new endDate: \'" + newEndDate + "\'");
        setValue("EndDate", newEndDate);
    }

    /**
     * Returns password hash for the policymaker. Passwords themselves are not stored in the database
     */
    private String getPasswordHash() throws SQLException {
        return (String) getValue("PasswordHash");
    }

    /**
     * Sets new password for the policymaker.
     */
    public void setPassword(String unecryptedPassword) throws SQLException {
        setValue("PasswordHash", util.makeShaHash(unecryptedPassword));
    }

    /**
     * Checks if the candidate password and policymaker's password match by comparing hashes.
     */
    public boolean isPassword(String unecryptedCandidate) throws SQLException {
        return util.isHashMatch(unecryptedCandidate, getPasswordHash());
    }

    /**
     * Checks if the candidate password and policymaker's password match by comparing hashes.
     */
    public boolean isPasswordHash(String passwordHash) throws SQLException {
    	if (passwordHash.equals(getPasswordHash()))
	    	return true;
    	else
    		return false;
    }

    /**
     * Returns all roles held by the policymaker.
     */
    public ArrayList<Role> getRoles() throws SQLException {

        ArrayList<Role> result = new ArrayList<Role>();

        ArrayList<WegovPolicymaker_PolicymakerRole> pms_roles = mgtSchema.getAllWhere(new WegovPolicymaker_PolicymakerRole(), "PolicymakerID", id);

        if (!pms_roles.isEmpty()) {
            for (WegovPolicymaker_PolicymakerRole pms_role : pms_roles) {
                WegovPolicymakerRole gotRole = (WegovPolicymakerRole) mgtSchema.getAllWhere(new WegovPolicymakerRole(), "ID", pms_role.getRoleID()).get(0);
                result.add(new Role(gotRole.getID(), coordinator));
            }
        }
        return result;
    }

    private String getRolesAsString() {
        String result = "";

        try{
            ArrayList<Role> roles = getRoles();

            for (Role role : roles) {
                result = result + role.getName().trim() + ", ";
            }
        } catch (Exception ex) {
            logger.error(ex);
        }

        if (!result.isEmpty()) {
            result = result.substring(0, result.length() - 2);
        }

        return result;
    }

    /**
     * Adds new role to the policymaker. If the role has already been assigned, nothing is added.
     */
    public final void addRole(Role newRole) throws SQLException {

        if (newRole == null)
            throw new RuntimeException("Attempt to add null role!");

        if (!getRoles().contains(newRole)) {
            logger.debug("Adding role " + newRole + " to policymaker: [" + id + "]");
            mgtSchema.insertObject(new WegovPolicymaker_PolicymakerRole(id, newRole.getID()));
        } else {
            logger.error("Role " + newRole + " is already assigned to policymaker:  [" + id + "]");
        }

    }

    /**
     * Removes a role from the policymaker. If the role doesn't exist, nothing is removed.
     */
    public void removeRole(Role roleToRemove) throws SQLException {

        if (roleToRemove == null)
            throw new RuntimeException("Attempt to remove null role!");

        ArrayList<Role> currentRoles = getRoles();

        for (Role role : currentRoles) {
            if (role.getName().equals(roleToRemove.getName())) {
                logger.debug("Removing role " + roleToRemove + " from policymaker:  [" + id + "]");
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("PolicymakerID", id);
                map.put("RoleID", roleToRemove.getID());
                mgtSchema.deleteAllWhere(new WegovPolicymaker_PolicymakerRole(), map);
                break;
            }
        }
    }

    /**
     * Returns information about the policymaker.
     */
    @Override
    public String toString() {
        updateWegovPolicymaker();
        return "[" + getID() + "] \'" + wegovPolicymaker.getName() + "\' (username: \'" + wegovPolicymaker.getUsername() + "\', roles: " + getRolesAsString() + ") from \'" + wegovPolicymaker.getOrganisation() + "\', start date: \'" +
                wegovPolicymaker.getStartDate() + "\', end date: \'" + wegovPolicymaker.getEndDate() + "\'";
    }
}
