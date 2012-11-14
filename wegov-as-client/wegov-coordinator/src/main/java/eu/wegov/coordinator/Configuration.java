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

import eu.wegov.coordinator.dao.mgt.WegovConfiguration;
import eu.wegov.coordinator.dao.mgt.WegovConfigurationSet_Configuration;
import eu.wegov.coordinator.dao.mgt.WegovConfiguration_Parameter;
import eu.wegov.coordinator.dao.mgt.WegovParameter;
import eu.wegov.coordinator.sql.SqlSchema;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 * Configuration defines a java executable, goes into configuration sets. Contains all infomation about an executable, along with all parameters.<br/><br/>
 
 <code>
    Coordinator coordinator = new Coordinator("coordinator.properties");<br/>
    coordinator.wipeDatabase(); // optional - removes everything from the database<br/>
    coordinator.setupWegovDatabase();<br/><br/>
 
    Configuration configuration = new Configuration("Test configuration", "java -version", "Test description", coordinator);<br/>
    configuration.addParameter("searchFor", "What to search on Twitter for", "bbc", coordinator.getDefaultUserRole());<br/><br/>
 
    System.out.println(configuration);
 </code>
  
 * @author Maxim Bashevoy
 */
public class Configuration {
    private int id;
    private String name;
    private String description;
    private String command, rendererJsp;
    private Coordinator coordinator;
    
    private SqlSchema mgtSchema;
    
    private WegovConfiguration wegovConfiguration;
    private ArrayList<Parameter> myParameters; // do not use
    
    private final static Logger logger = Logger.getLogger(Configuration.class.getName());
    public final static String STATUS_DELETED = "deleted";

    /**
     * Creates new configuration.
     */
    public Configuration(String name, String command, String description, String rendererJsp, Coordinator coordinator) throws SQLException {
        this.id = 0;
        this.name = name;
        this.command = command;
        this.description = description;
        this.rendererJsp = rendererJsp;
        this.coordinator = coordinator;
        
        this.mgtSchema = coordinator.getMgtSchema();
        
        this.wegovConfiguration = new WegovConfiguration(name, command, description, rendererJsp);
        
        String iDfromDatabase = mgtSchema.insertObject(wegovConfiguration);
        
        if (iDfromDatabase != null) {
            this.id = Integer.parseInt(iDfromDatabase);
        } else {
            throw new SQLException("Failed to assign database ID to configuration: \'" + name + "\'.");
        }      
        
        myParameters = getParametersFromDatabase();
        
//        logger.debug("Created new configuration: " + toString());        
    }
    
    /**
     * Get from database only
     */    
    Configuration(int id, Coordinator coordinator) throws SQLException {
        this.id = id;
        this.coordinator = coordinator;
        this.mgtSchema = coordinator.getMgtSchema();
        
        ArrayList<WegovConfiguration> configs = mgtSchema.getAllWhere(new WegovConfiguration(), "ID", id);
        
        if (configs.isEmpty())
            throw new SQLException("Failed to find configuration with database ID: \'" + id + "\'.");
        
        this.wegovConfiguration = (WegovConfiguration) configs.get(0);
        
        this.name = wegovConfiguration.getName();
        this.description = wegovConfiguration.getDescription();
        this.command = wegovConfiguration.getCommand();
        this.rendererJsp = wegovConfiguration.getRendererJsp();
        
        myParameters = getParametersFromDatabase();
        
//        logger.debug("Restored configuration: " + toString());
    } 
    
    
    private void setValue(String name, Object value) {
        try {
            mgtSchema.updateRow(wegovConfiguration, name, value, "ID", id);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
    
    private Object getValue(String name) {
        Object result = null;
        
        try {        
            result = mgtSchema.getColumnValue(wegovConfiguration, name, "ID", id);
        } catch (Exception ex) {
            logger.error(ex);
        }
        
        return result;
    }

    /**
     * Returns unique database ID for the configuration.
     */    
    public int getID() {
        return id;
    }
    
    /**
     * Returns name of the configuration.
     */ 
    public String getName() {
//        this.name = (String) getValue("Name");
        return name;
    }

    /**
     * Sets new name for the configuration.
     */     
    public void setName(String newName) {
        this.name = newName;
        logger.debug("Setting name to: \'" + newName + "\'");
        setValue("Name", newName);
    }
    
    /**
     * Returns command of the configuration.
     */ 
    public String getCommand() {
//        this.command = (String) getValue("Command");
        return command;
    }

    /**
     * Sets new command for the configuration.
     */     
    public void setCommand(String newCommand) {
        this.command = newCommand;
        logger.debug("Setting command to: \'" + newCommand + "\'");
        setValue("Command", newCommand);
    }    
    
    /**
     * Returns description of the configuration.
     */ 
    public String getDescription() {
//        this.description = (String) getValue("Description");
        return description;
    }

    /**
     * Sets new description for the configuration.
     */     
    public void setDescription(String newDescription) {
        this.description = newDescription;
        logger.debug("Setting description to: \'" + newDescription + "\'");
        setValue("Description", newDescription);
    }    
    
    /**
     * Returns rendererJsp of the configuration.
     */ 
    public String getRendererJsp() {
//        this.rendererJsp = (String) getValue("RendererJsp");
        return rendererJsp;
    }

    /**
     * Sets new rendererJsp for the configuration.
     */     
    public void setRendererJsp(String newRendererJsp) {
        this.rendererJsp = newRendererJsp;
        logger.debug("Setting rendererJsp to: \'" + newRendererJsp + "\'");
        setValue("RendererJsp", newRendererJsp);
    }
    
    private void updateWegovConfigurationFromDatabase() {
        try {
            wegovConfiguration = (WegovConfiguration) mgtSchema.getAllWhere(wegovConfiguration, "ID", id).get(0);
            myParameters = getParametersFromDatabase();
        } catch (SQLException ex) {
            logger.error("Failed to update from database configuration's details with ID: " + id);
            logger.error(ex.toString());
        }
    }
    
    /**
     * Adds new parameter to the configuration with the selected role.
     */
    public void addParameter(String name, String description, String value, Role role) throws SQLException {
        ArrayList<Role> roles = new ArrayList<Role>();
        roles.add(role);
        addParameter(name, description, value, roles);
    }

    /**
     * Adds new parameter to the configuration with default user role.
     */
    public void addParameterAsUser(String name, String description, String value) throws SQLException {
        ArrayList<Role> roles = new ArrayList<Role>();
        roles.add(coordinator.getDefaultUserRole());
        addParameter(name, description, value, roles);
    }

    /**
     * Adds new parameter to the configuration with default admin role.
     */    
    public void addParameterAsAdmin(String name, String description, String value) throws SQLException {
        ArrayList<Role> roles = new ArrayList<Role>();
        roles.add(coordinator.getDefaultAdminRole());
        addParameter(name, description, value, roles);
    }
    
    /**
     * Adds new parameter to the configuration with default set of user role.
     */
    public void addParameter(String name, String description, String value, ArrayList<Role> roles) throws SQLException {
        
        if (name == null)
            throw new RuntimeException("Null parameter names not allowed");
        
        if (roles.isEmpty())
            throw new RuntimeException("No roles defined for parameter: \'" + name + "\'");
        
        ArrayList<Parameter> parameters = getParameters();
        ArrayList<String> parameterNames = new ArrayList<String>();
        
        for (Parameter p : parameters) {
            parameterNames.add(p.getName());
        }        
        
        if (!parameterNames.contains(name)) {
            Parameter p = new Parameter(name, description, value, roles, coordinator);
            int pID = p.getID();
            mgtSchema.insertObject(new WegovConfiguration_Parameter(id, pID));
        } else {
            throw new RuntimeException("Parameter \'" + name + "\' already defined for configuration " + getName());
        }
    }
    
    /**
     * Adds new parameter to the configuration (parameter in the argument will be cloned).
     */
    public void addParameter(Parameter newParameter) throws SQLException {
        
        if (newParameter == null)
            throw new RuntimeException("Null parameters not allowed");
        
        Parameter parameter = newParameter.clone();
        
        ArrayList<Parameter> parameters = getParameters();
        ArrayList<String> parameterNames = new ArrayList<String>();
        
        for (Parameter p : parameters) {
            parameterNames.add(p.getName());
        }        
        
        String parameterName = parameter.getName();
        
        if (!parameterNames.contains(parameterName)) {
            Parameter p = parameter.clone();
            int pID = p.getID();
            mgtSchema.insertObject(new WegovConfiguration_Parameter(id, pID));
        } else {
            throw new RuntimeException("Parameter \'" + parameterName + "\' already defined for configuration: \'" + getName() + "\'");
        }
    }
    
    /**
     * Returns parameter by name.
     */
    public Parameter getParameterByName(String name) throws SQLException {
        
        if (name == null)
            throw new RuntimeException("Null parameters not allowed");
        
        ArrayList<Integer> results = mgtSchema.getIDColumnValuesWhere(
                new WegovParameter(), "ID", "Name", name, "ParameterID",
                new WegovConfiguration_Parameter(), "ConfigurationID", getID());
        
        if (results.isEmpty())
            return null;
        else
            return new Parameter(results.get(0), coordinator);
        
//        ArrayList<Parameter> parameters = getParameters();
//        ArrayList<String> parameterNames = new ArrayList<String>();
//        
//        for (Parameter p : parameters) {
//            parameterNames.add(p.getName());
//        }
//        
//        if (!parameterNames.contains(name))
//            throw new RuntimeException("Parameter \'" + name + "\' does not exist in configuration: " + name);        
//        
//        Parameter result = null;
//        
//        for (Parameter parameter : parameters) {
//            if (parameter.getName().equals(name)) {
//                result = parameter;
//                break;
//            }
//        }
        
//        return result;
    }
    
    public ArrayList<Integer> getParametersDatabaseIDs() throws SQLException {
        return mgtSchema.getIDColumnValuesWhere(new WegovConfiguration_Parameter(), "ParameterID", "ConfigurationID", id);
    }
    
    /**
     * Returns parameter by database ID.
     */
    public Parameter getParameterByID(int parameterID) throws SQLException {
        if (getParametersDatabaseIDs().contains(parameterID))
            return new Parameter(parameterID, coordinator);
        else
            return null;
    }
    
    

    /**
     * Returns all parameters for the configuration.
     */    
    public ArrayList<Parameter> getParameters() throws SQLException {      
        
        return myParameters;
    }

    /**
     * Returns all parameters for the configuration from the database.
     */    
    public final ArrayList<Parameter> getParametersFromDatabase() throws SQLException {
        ArrayList<Parameter> result = new ArrayList<Parameter>();
        
        for (Integer pID : getParametersDatabaseIDs()) {
            result.add(new Parameter(pID, coordinator));
        }        
        
        return result;
    }
    
    /**
     * Returns all parameter names for specified role for the configuration.
     */    
    public ArrayList<String> getParameterNamesForRole(Role role) throws SQLException {
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<Parameter> parameters = getParameters();
        
        for (Parameter parameter : parameters) {
            boolean hasRole = false;
            
            for (Role tempRole : parameter.getRoles()) {
                if (role.getName().equals(tempRole.getName()))
                    hasRole = true;
            }
                
            if (hasRole)
                result.add(parameter.getName());
        }        
        
        return result;
    }
    
    /**
     * Returns all parameters for specified role for the configuration.
     */     
    public ArrayList<Parameter> getParametersForRole(Role role) throws SQLException {
        ArrayList<Parameter> result = new ArrayList<Parameter>();
        ArrayList<Parameter> parameters = getParameters();
        
        for (Parameter parameter : parameters) {
            boolean hasRole = false;
            
            for (Role tempRole : parameter.getRoles()) {
                if (role.getName().equals(tempRole.getName()))
                    hasRole = true;
            }
                
            if (hasRole)
                result.add(parameter);
        }        
        
        return result;
    }
    
    /**
     * Adds new role to parameter by name (unless the role was assigned already).
     */     
    public void addRoleToParameter(String name, Role newRole) throws SQLException {
        
        if (name == null)
            throw new RuntimeException("Null parameters not allowed");
        
        if (newRole == null)
            throw new RuntimeException("Null roles not allowed");
        
        Parameter p = getParameterByName(name);
        p.addRole(newRole);
    }
    
    /**
     * Returns all roles names for a parameter.
     */ 
    public ArrayList<Role> getRolesForParameter(String name) throws SQLException {
        
        return getParameterByName(name).getRoles();
    }
    
    /**
     * Returns description for a parameter.
     */    
    public String getDescriptionOfParameter(String name) throws SQLException {
        
        return getParameterByName(name).getDescription();
    }
    
    /**
     * Returns value for a parameter.
     */    
    public String getValueOfParameter(String name) throws SQLException {
        
        return getParameterByName(name).getValue();
    }

    /**
     * Replaces configuration parameters with new ones.
     */
    public void setParameters(ArrayList<Parameter> parameters) throws SQLException {
        // delete existing parameters
        mgtSchema.deleteAllWhere(new WegovConfiguration_Parameter(), "ConfigurationID", id);
        
        // careful now! create new parameters
        for (Parameter p : parameters) {
            addParameter(p.getName(), p.getDescription(), p.getValue(), p.getRoles());
        }
        
    }
    
    /**
     * Get configuration set this configuration belongs to.
     */
    public ConfigurationSet getConfigurationSet() throws SQLException {
        ArrayList<Integer> temp = mgtSchema.getIDColumnValuesWhere(new WegovConfigurationSet_Configuration(), "ConfigurationSetID", "ConfigurationID", id);
        
        if (temp.isEmpty())
            return null;
        else {
            return new ConfigurationSet(temp.get(0), coordinator);
        }
    }
    
//    /**
//     * Get rendererJsp of the configuration set this configuration belongs to.
//     */
//    public String getRendererJsp() throws SQLException {
//        ConfigurationSet temp = getConfigurationSet();
//        
//        if (temp == null)
//            return null;
//        else {
//            return temp.getRendererJsp();
//        }
//    }
    
    /**
     * Creates new Configuration object with exactly the same properties and parameters as current configuration.
     */    
    @Override
    public Configuration clone() {
        updateWegovConfigurationFromDatabase();
        try{
            Configuration clone = new Configuration(wegovConfiguration.getName(), wegovConfiguration.getCommand(), wegovConfiguration.getDescription(), wegovConfiguration.getRendererJsp(), coordinator);
            for (Parameter p : getParameters()) {
                clone.addParameter(p.getName(), p.getDescription(), p.getValue(), p.getRoles());
            }
            return clone;
        } catch(SQLException ex) {
            logger.error(ex);
            return null;
        }
        
    }
    
    /**
     * Compares the configuration to candidate configuration (with parameters, but excluding renderedJsp).
     */
    public boolean equals(Configuration candidateConfiguration) throws SQLException {
        boolean result = true;
        int matchingParametersCount = 0;
        
        logger.debug("Matching configuration: \'" + toString() + "\' to candidate configuration: \'" + candidateConfiguration.toString() + "\'.");
        
        ArrayList<Parameter> candidateParameters = candidateConfiguration.getParameters();
        ArrayList<Parameter> myParameters = getParameters();

        if (getName().equals(candidateConfiguration.getName())) {
            if (getDescription().equals(candidateConfiguration.getDescription())) {
                if (getCommand().equals(candidateConfiguration.getCommand())) {
                    if (myParameters.size() == candidateParameters.size()) {
                        for (Parameter myParameter : myParameters) {
                            for (Parameter candidateParameter : candidateParameters) {
                                if (myParameter.equals(candidateParameter)) {
                                    matchingParametersCount++;
                                    break;
                                }
                            }
                        }
                        
                        if (matchingParametersCount != myParameters.size()) {
                            logger.debug("No match: configurations have different parameters.");
                            result = false;
                        }
                        
                    } else {
                        logger.debug("No match: configurations have different number of parameters.");
                        result = false;
                    }
                } else {
                    logger.debug("No match: configurations have different commands.");
                    result = false;                    
                }
            } else {
                logger.debug("No match: configurations have different descriptions.");
                result = false;                
            }
        } else {
            logger.debug("No match: configurations have different names.");
            result = false;            
        }
        
        if (result)
            logger.debug("Configurations match.");        
        
        return result;
    }
        
    /**
     * Returns information about the configuration.
     */    
    @Override
    public final String toString() {
        updateWegovConfigurationFromDatabase();
        String result = "Configuration [" + getID() + "], name: \'" + wegovConfiguration.getName() + "', description: \'" + wegovConfiguration.getDescription() + "\', command: \'" + wegovConfiguration.getCommand() + "\', rendererJsp: \'" + wegovConfiguration.getRendererJsp() + "\'.\n";
        
        for (Parameter parameter : myParameters) {
            result += "\t- " + parameter.toString() + "\n";
        }
        
        return result;
    }
}