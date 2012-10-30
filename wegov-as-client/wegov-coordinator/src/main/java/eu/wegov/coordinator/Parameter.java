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
//	Created Date :			2011-08-24
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator;

import eu.wegov.coordinator.dao.mgt.WegovParameter_Role;
import eu.wegov.coordinator.dao.mgt.WegovParameter;
import eu.wegov.coordinator.sql.SqlSchema;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 * Parameters are parts of configurations.
 * 
 * @author Maxim Bashevoy
 */
public class Parameter {
    private int id;
    private String name;
    private String description;
    private String value;
    private Coordinator coordinator;
    
    private SqlSchema mgtSchema;    
    
    private WegovParameter wegovParameter;
    
    private final static Logger logger = Logger.getLogger(Configuration.class.getName());
    
    /**
     * Creates new configuration parameter.
     */
    Parameter(String name, String description, String value, ArrayList<Role> roles, Coordinator coordinator) throws SQLException {
        this.id = 0;
        this.name = name;
        this.value = value;
        this.description = description;
        this.coordinator = coordinator;
        
        this.mgtSchema = coordinator.getMgtSchema();
        
        this.wegovParameter = new WegovParameter(name, value, description);       
        
        String iDfromDatabase = mgtSchema.insertObject(wegovParameter);
        
        if (iDfromDatabase != null) {
            this.id = Integer.parseInt(iDfromDatabase);
        } else {
            throw new SQLException("Failed to assign database ID to parameter: \'" + name + "\'.");
        }      
        
//        logger.debug("Created new parameter: " + toString());
        
        logger.debug("Adding roles to parameter [" + id + "]");
        
        for (Role role : roles) {
            mgtSchema.insertObject(new WegovParameter_Role(id, role.getID()));
//            logger.debug("Added role: " + role.toString());
        }
    }
    
    /**
     * Get from database only
     */    
    Parameter(int id, Coordinator coordinator) throws SQLException {
        this.id = id;
        this.coordinator = coordinator;
        this.mgtSchema = coordinator.getMgtSchema();
        
        ArrayList<WegovParameter> configs = mgtSchema.getAllWhere(new WegovParameter(), "ID", id);
        
        if (configs.isEmpty())
            throw new SQLException("Failed to find parameter with database ID: \'" + id + "\'.");
        
        this.wegovParameter = (WegovParameter) configs.get(0);
        
        this.name = wegovParameter.getName();
        this.description = wegovParameter.getDescription();
        this.value = wegovParameter.getValue();
        
//        logger.debug("Restored parameter: " + toString());
    }

    /**
     * Returns databaseID for the parameter.
     */    
    public int getID() {
        return id;
    }
    
    
    private void updateWegovParameterFromDatabase() {
        try {
            wegovParameter = (WegovParameter) mgtSchema.getAllWhere(wegovParameter, "ID", id).get(0);
        } catch (SQLException ex) {
            logger.error("Failed to update from database parameter's details with ID: " + id);
            logger.error(ex.toString());
        }
    }    
    
    private void setParameterValue(String name, Object value) {
        try {
            mgtSchema.updateRow(wegovParameter, name, value, "ID", id);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
    
    private Object getParameterValue(String name) {
        Object result = null;
        
        try {        
            result = mgtSchema.getColumnValue(wegovParameter, name, "ID", id);
        } catch (Exception ex) {
            logger.error(ex);
        }
        
        return result;
    }    
    
    /**
     * Returns name of the parameter.
     */    
    public String getName() {
//        this.name = (String) getParameterValue("Name");
        return name;
    }

    /**
     * Sets new name for the parameter.
     */     
    public void setName(String newName) {
        this.name = newName;
        logger.debug("Setting name to: \'" + newName + "\'");
        setParameterValue("Name", newName);
    }    
    
    /**
     * Returns description of the parameter.
     */    
    public String getDescription() {
//        this.description = (String) getParameterValue("Description");
        return description;
    }

    /**
     * Sets new description for the parameter.
     */    
    public void setDescription(String newDescription) {
        this.description = newDescription;
        logger.debug("Setting description to: \'" + newDescription + "\'");
        setParameterValue("Description", newDescription);
    }    
    
    /**
     * Returns value of the parameter.
     */    
    public String getValue() {
//        this.value = (String) getParameterValue("Value");
        return value;
    }

    /**
     * Sets new value for the parameter.
     */    
    public void setValue(String newValue) {
        this.value = newValue;
        logger.debug("Setting value to: \'" + newValue + "\'");
        setParameterValue("Value", newValue);
    }  
    
    /**
     * Returns roles for the parameter.
     */    
    public ArrayList<Role> getRoles() {
        ArrayList<Role> result = new ArrayList<Role>();
        
        try {
            for (Integer roleID : mgtSchema.getIDColumnValuesWhere(new WegovParameter_Role(), "RoleID", "ParameterID", id)) {
                result.add(new Role(roleID, coordinator));
            }
        } catch (SQLException ex) {
            logger.error(ex);
        }        
        
        return result;
    }
    
    /**
     * Adds new role to the parameter (unless the role has already been assigned to the parameter).
     */    
    public void addRole(Role newRole) throws SQLException {
        ArrayList<Role> currentRoles = getRoles();
        
        boolean roleExists = false;
        
        for (Role role : currentRoles) {
            if (role.getName().equals(newRole.getName()))
                roleExists = true;
        }
        
        if (roleExists)
            logger.error("Role " + newRole + " already exists for parameter [" + id + "]");
        else {
            logger.debug("Adding role " + newRole + " to parameter [" + id + "]");
            mgtSchema.insertObject(new WegovParameter_Role(id, newRole.getID()));
        }
    }
    
    
    /**
     * Creates new parameter with the same properties.
     */    
    @Override
    public Parameter clone() {
        updateWegovParameterFromDatabase();
        try{
            return new Parameter("Copy of " + wegovParameter.getName(), wegovParameter.getDescription(), wegovParameter.getValue(), getRoles(), coordinator);
        } catch(SQLException ex) {
            logger.error(ex);
            return null;
        }
    }
    
    public boolean equals(Parameter candidateParameter) {
        boolean result = true;
        
//        logger.debug("Matching parameter: \'" + toString() + "\' to candidate parameter: \'" + candidateParameter.toString() + "\'.");
        
        if (getName().equals(candidateParameter.getName())) {
            if (getDescription().equals(candidateParameter.getDescription())) {
                if (getValue().equals(candidateParameter.getValue())) {
                    ArrayList<Role> candidateRoles = candidateParameter.getRoles();
                    ArrayList<Role> myRoles = getRoles();
                    int matchingRolesCount = 0;

                    if (myRoles.size() == candidateRoles.size()) {
                        for (Role myRole : myRoles) {
                            for (Role candidateRole : candidateRoles) {
                                if (myRole.equals(candidateRole)) {
                                    matchingRolesCount++;
                                    break;
                                }
                            }
                        }
                        
                        if (matchingRolesCount != myRoles.size()) {
                            logger.debug("No match: parameters have different roles.");
                            result = false;
                        }
                    } else {
                        logger.debug("No match: parameters have different number of roles.");
                        result = false;
                    }
                } else {
                    logger.debug("No match: parameters have different values.");
                    result = false;
                }
            } else {
                logger.debug("No match: parameters have different descriptions.");
                result = false;
            }
            
        } else {
            logger.debug("No match: parameters have different names.");
            result = false;
        }
        
        if (result)
            logger.debug("Parameters match.");
        
        return result;
    }
    
    /**
     * Returns information about the parameter.
     */     
    @Override
    public final String toString() {
        String roles = "";
        for (Role role : getRoles()) {
            roles += "\'" + role.getName().trim() + "\', ";
        }

        if (!roles.isEmpty())
            roles = roles.substring(0, roles.length() - 2);
        
        return "Parameter [" + getID() + "], name: \'" + getName() + "\' (" + getDescription() + "), value: \'" + getValue() + "\', roles: " + roles + ".";
    }    
}
