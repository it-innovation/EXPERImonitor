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

import eu.wegov.coordinator.dao.mgt.WegovPolicymakerRole;
import eu.wegov.coordinator.dao.mgt.WegovPolicymaker_PolicymakerRole;
import eu.wegov.coordinator.sql.SqlSchema;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 * Roles define Dashboard User's (Policymaker's) privileges: user, administrator etc.
 * 
 * @author Maxim Bashevoy
 */
public class Role {
    private int id;
    private String name;
    private String description;
    
    private Coordinator coordinator;
    private WegovPolicymakerRole wegovPolicymakerRole;
    private SqlSchema mgtSchema;
    
    private final static Logger logger = Logger.getLogger(Role.class.getName());
    
    /**
     * Creates new role or returns existing role with the same name.
     * 
     * @param name role name
     * @param description description of the role
     */
    public Role(String name, String description, Coordinator coordinator) throws SQLException {
        this.id = 0;
        this.name = name;
        this.description = description;
        
        logger.debug("Attempting to create new Role: \'" + name + "\', \'" + description + "\'");
        
        this.coordinator = coordinator;
        this.mgtSchema = coordinator.getMgtSchema(); 
        
        this.wegovPolicymakerRole = new WegovPolicymakerRole(name, description);

        // check if role exists
        ArrayList<WegovPolicymakerRole> existingRoles = mgtSchema.getAllWhere(wegovPolicymakerRole, "Name", name);
        
        if (existingRoles.isEmpty()) {
            // Add new role to database
            logger.debug("Creating new Role: \'" + name + "\', \'" + description + "\'");
            this.wegovPolicymakerRole = new WegovPolicymakerRole(name, description);
            String iDfromDatabase = coordinator.getMgtSchema().insertObject(wegovPolicymakerRole);
            logger.debug("Got database ID: \'" + iDfromDatabase + "\'");
            
            if (iDfromDatabase != null) {
                this.id = Integer.parseInt(iDfromDatabase);
            } else {
                throw new SQLException("Failed to assign database ID to policymaker: \'" + name + "\'.");
            }            
            
        } else {
            // Return existing one
            logger.warn("Role: \'" + name + "\' already exists, returning role from the database");
            this.wegovPolicymakerRole = existingRoles.get(0);
            this.id = wegovPolicymakerRole.getID();
        }
    }
    
    Role(int id, Coordinator coordinator) throws SQLException {
        this.id = id;
        this.coordinator = coordinator;
        this.mgtSchema = coordinator.getMgtSchema();
        
        logger.debug("Getting existing role with ID: " + id);
        
        ArrayList<WegovPolicymakerRole> existingRoles = coordinator.getMgtSchema().getAllWhere(new WegovPolicymakerRole(), "ID", id);
        
        if (existingRoles.isEmpty())
            throw new SQLException("Failed to find role with database ID: \'" + id + "\'.");
        
        this.wegovPolicymakerRole = (WegovPolicymakerRole) existingRoles.get(0);
        
        this.name = wegovPolicymakerRole.getName();
        this.description = wegovPolicymakerRole.getDescription();        
    }
    
    /**
     * Returns all policymakers with that role.
     */
    public ArrayList<Policymaker> getPolicyMakers() throws SQLException {
        ArrayList<Policymaker> result = new ArrayList<Policymaker>();
        
        ArrayList<WegovPolicymaker_PolicymakerRole> pm_prs = mgtSchema.getAllWhere(new WegovPolicymaker_PolicymakerRole(), "RoleID", id);
        
        for (WegovPolicymaker_PolicymakerRole pm_pr : pm_prs) {
            result.add(new Policymaker(pm_pr.getPolicymakerID(), coordinator));
        }
        
        return result;
    }
    
    private void updateWegovPolicymakerRole() {
        try {
            wegovPolicymakerRole = (WegovPolicymakerRole) mgtSchema.getAllWhere(wegovPolicymakerRole, "ID", id).get(0);
        } catch (SQLException ex) {
            logger.error("Failed to update from database role details with ID: " + id);
            logger.error(ex.toString());
        }
    }    
    
    private WegovPolicymakerRole getWegovPolicymakerRole() {
        updateWegovPolicymakerRole();
        return wegovPolicymakerRole;
    }  
    
    private void setValue(String name, Object value) {
        try {
            mgtSchema.updateRow(wegovPolicymakerRole, name, value, "ID", id);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
    
    private Object getValue(String name) {
        Object result = null;
        
        try {        
            result = mgtSchema.getColumnValue(wegovPolicymakerRole, name, "ID", id);
        } catch (Exception ex) {
            logger.error(ex);
        }
        
        return result;
    }    
    
    /**
     * Returns database ID of the role.
     */
    public int getID() {
        return id;
    }

    /**
     * Returns role's name.
     */
    public String getName() {
        this.name = (String) getValue("Name");
        return name;
    }

    /**
     * Sets new name for the role.
     */
    public void setName(String newName) {
        this.name = newName;
        logger.debug("Setting name to: \'" + newName + "\'");
        setValue("Name", newName);
    }    

    /**
     * Returns role's description.
     */    
    public String getDescription() {
        this.description = (String) getValue("Description");
        return description;
    }

    /**
     * Sets new description for the role.
     */    
    public void setDescription(String newDescription) {
        this.description = newDescription;
        logger.debug("Setting description to: \'" + newDescription + "\'");
        setValue("Description", newDescription);
    }
    
    /**
     * Compares the role to a candidate role (only by name as names should be unique).
     */
    public boolean equals(Role candidateRole) {
        boolean result = true;
        
//        logger.debug("Matching role: \'" + toString() + "\' to candidate role: \'" + candidateRole.toString() + "\'.");
        
        if (!getName().equals(candidateRole.getName())) {
            logger.debug("No match: roles have different names.");
            result = false;
        }
        
        if (result)
            logger.debug("Roles match.");        
        
        return result;
    }
    
    /**
     * Returns information about the role.
     */    
    @Override
    public String toString() {
        updateWegovPolicymakerRole();
        return "[" + getID() + "] \'" + wegovPolicymakerRole.getName() + "\' (" + wegovPolicymakerRole.getDescription() + ")";
    }
}
