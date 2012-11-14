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

import eu.wegov.coordinator.dao.mgt.WegovActivity_ConfigurationSet;
import eu.wegov.coordinator.dao.mgt.WegovConfigurationSet;
import eu.wegov.coordinator.dao.mgt.WegovConfigurationSet_Configuration;
import eu.wegov.coordinator.dao.mgt.WegovRun_ConfigurationSet;
import eu.wegov.coordinator.sql.SqlSchema;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.log4j.Logger;

/**
 * Configuration set describes a Dashboard Tool, contains one or several configurations.
 * 
 * <br/><br/>
 
 <code>
    Coordinator coordinator = new Coordinator("coordinator.properties");<br/>
    coordinator.wipeDatabase(); // optional - removes everything from the database<br/>
    coordinator.setupWegovDatabase();<br/><br/>
 
    ConfigurationSet configSet = new ConfigurationSet("Test name", "Test description", coordinator);<br/><br/>
    
    Configuration configuration = new Configuration("Test configuration", "java -version", "Test description", coordinator);<br/>
    configuration.addParameter("searchFor", "What to search on Twitter for", "bbc", coordinator.getDefaultUserRole());<br/><br/>
  
  configSet.addConfiguration(configuration);<br/><br/>
  
  System.out.println(configSet);<br/><br/>
  
   for (int i = 0; i < configSet.size(); i++) {<br/> &nbsp;&nbsp;&nbsp;
            System.out.println(configSet.get(i));<br/>
        }
 
 </code>
  
 * @author Maxim Bashevoy
 */
public class ConfigurationSet {
    private int id;
    private String name, description, rendererJsp;
    private Coordinator coordinator;
    
    private SqlSchema mgtSchema;    
    
    private WegovConfigurationSet wegovConfigurationSet;
    private ArrayList<Parameter> tempParameters; // do not use
    
    private final static Logger logger = Logger.getLogger(ConfigurationSet.class.getName());  
    
    public final static String STATUS_DELETED = "deleted";
    
    /**
     * Creates new configuration set.
     */
    ConfigurationSet(String name, String description, String rendererJsp, Coordinator coordinator) throws SQLException {
        this.id = 0;
        this.name = name;
        this.description = description;
        this.rendererJsp = rendererJsp;
        
        this.coordinator = coordinator;
        
        this.mgtSchema = coordinator.getMgtSchema();
        
        this.wegovConfigurationSet = new WegovConfigurationSet(name, description, rendererJsp);        
        
        String iDfromDatabase = mgtSchema.insertObject(wegovConfigurationSet);
        
        if (iDfromDatabase != null) {
            this.id = Integer.parseInt(iDfromDatabase);
        } else {
            throw new SQLException("Failed to assign database ID to configuration set: \'" + name + "\'.");
        }      
        
//        logger.debug("Created new configuration: " + toString());        
    }
    
    
    /**
     * Get from database only
     */    
    ConfigurationSet(int id, Coordinator coordinator) throws SQLException {
        this.id = id;
        this.coordinator = coordinator;
        this.mgtSchema = coordinator.getMgtSchema();
        
        ArrayList<WegovConfigurationSet> configs = mgtSchema.getAllWhere(new WegovConfigurationSet(), "ID", id);
        
        if (configs.isEmpty())
            throw new SQLException("Failed to find configuration set with database ID: \'" + id + "\'.");
        
        this.wegovConfigurationSet = (WegovConfigurationSet) configs.get(0);
        
        this.name = wegovConfigurationSet.getName();
        this.description = wegovConfigurationSet.getDescription();
        this.rendererJsp = wegovConfigurationSet.getRendererJsp();
        
//        logger.debug("Restored configuration set: " + toString());
    }
    
    /**
     * Creates new Configuration object with exactly the same properties and parameters as current configuration.
     */    
    @Override
    public ConfigurationSet clone() {
        updateWegovConfigurationSetFromDatabase();
        try{
            ConfigurationSet clone = new ConfigurationSet(wegovConfigurationSet.getName(), wegovConfigurationSet.getDescription(), wegovConfigurationSet.getRendererJsp(), coordinator);
            for (Integer p : getConfigurationIDs()) {
                clone.addConfiguration(new Configuration(p, coordinator));
            }
            return clone;
        } catch(SQLException ex) {
            logger.error(ex);
            return null;
        }
        
    }    

    /**
     * Returns unique database ID for the configuration set.
     */
    public int getID() {
        return id;
    }
    
    /**
     * Adds new configuration to the configuration set (configuration given as argument will be cloned).
     */
    public void addConfiguration(Configuration newConfiguration) throws SQLException {
        Configuration configuration = newConfiguration.clone();        
        int newConfigurationID = configuration.getID();        
        mgtSchema.insertObject(new WegovConfigurationSet_Configuration(id, newConfigurationID));
    }
    
    /**
     * Returns database IDs of configurations in the set.
     */
    private ArrayList<Integer> getConfigurationIDs() {
        try {
            ArrayList<Integer> temp = mgtSchema.getIDColumnValuesWhere(new WegovConfigurationSet_Configuration(), "ConfigurationID", "ConfigurationSetID", id);
            return temp;
        } catch (Exception ex) {
            logger.error(ex);
            return new ArrayList<Integer>();
        }
    }
    
    /**
     * Returns configurations in the set.
     */
    public ArrayList<Configuration> getConfigurations() throws SQLException {
        ArrayList<Configuration> configurations = new ArrayList<Configuration>();
        
        for (int configurationID : getConfigurationIDs())
            configurations.add(new Configuration(configurationID, coordinator));
            
        return configurations;
    }
    
    /**
     * Returns configuration with specified database ID.
     */
    public Configuration getConfigurationByID(int configurationID) throws SQLException {
        
        if (getConfigurationIDs().contains(configurationID))
            return new Configuration(configurationID, coordinator);
        else
            return null;
    }
    
    /**
     * Returns size of the configuration set.
     */
    public Integer size() throws SQLException {
        return getConfigurationIDs().size();
    }
    
    /**
     * Returns configuration number i from the set.
     */
    public Configuration get(Integer i) throws SQLException {
        ArrayList<Configuration> tempArray = new ArrayList<Configuration>();
        
        for (Integer configurationID : getConfigurationIDs()) {
            tempArray.add(new Configuration(configurationID, coordinator));
        }
        
        return tempArray.get(i);
    }
    
    /**
     * Returns iterator over configurations in the set.
     */
    public Iterator<Configuration> iterator() throws SQLException {
        ArrayList<Configuration> tempArray = new ArrayList<Configuration>();

        for (Integer configurationID : getConfigurationIDs()) {
            tempArray.add(new Configuration(configurationID, coordinator));
        }

        return tempArray.iterator(); 
    }
    
    /**
     * Returns true if there are no configurations in the set, false otherwise.
     */
    public boolean isEmpty() {
        if (getConfigurationIDs().isEmpty())
            return true;
        else
            return false;
    }
    
    private void setValue(String name, Object value) {
        try {
            mgtSchema.updateRow(wegovConfigurationSet, name, value, "ID", id);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
    
    private Object getValue(String name) {
        Object result = null;
        
        try {        
            result = mgtSchema.getColumnValue(wegovConfigurationSet, name, "ID", id);
        } catch (Exception ex) {
            logger.error(ex);
        }
        
        return result;
    } 
    
    /**
     * Returns name of the configuration set.
     */ 
    public String getName() {
//        this.name = (String) getValue("Name");
        return name;
    }

    /**
     * Sets new name for the configuration set.
     */     
    public void setName(String newName) {
        this.name = newName;
        logger.debug("Setting name to: \'" + newName + "\'");
        setValue("Name", newName);
    }
    
    
    /**
     * Returns description of the configuration set.
     */ 
    public String getDescription() {
//        this.description = (String) getValue("Description");
        return description;
    }

    /**
     * Sets new description for the configuration set.
     */     
    public void setDescription(String newDescription) {
        this.description = newDescription;
        logger.debug("Setting description to: \'" + newDescription + "\'");
        setValue("Description", newDescription);
    }   
    
    /**
     * Returns rendererJsp of the configuration set.
     */ 
    public String getRendererJsp() {
//        this.rendererJsp = (String) getValue("RendererJsp");
        return rendererJsp;
    }

    /**
     * Sets new rendererJsp for the configuration set.
     */     
    public void setRendererJsp(String newRendererJsp) {
        this.rendererJsp = newRendererJsp;
        logger.debug("Setting rendererJsp to: \'" + newRendererJsp + "\'");
        setValue("RendererJsp", newRendererJsp);
    }   
    
    /**
     * Returns Activity this Configuration Set belongs to.
     */
    public Activity getActivity() throws SQLException {
        ArrayList<Integer> temp = mgtSchema.getIDColumnValuesWhere(new WegovActivity_ConfigurationSet(), "ActivityID", "ConfigurationSetID", id);
        
        if (temp.isEmpty())
            return null;
        else {
            return new Activity(temp.get(0), coordinator);
        }        
    }
    
    /**
     * Returns Activity this Configuration Set belongs to.
     */
    public Run getRun() throws SQLException {
        ArrayList<Integer> temp = mgtSchema.getIDColumnValuesWhere(new WegovRun_ConfigurationSet(), "RunID", "ConfigurationSetID", id);
        
        if (temp.isEmpty())
            return null;
        else {
            return new Run(temp.get(0), coordinator);
        }        
    }
    
    private void updateWegovConfigurationSetFromDatabase() {
        try {
            wegovConfigurationSet = (WegovConfigurationSet) mgtSchema.getAllWhere(wegovConfigurationSet, "ID", id).get(0);
        } catch (SQLException ex) {
            logger.error("Failed to update from database configuration set's details with ID: " + id);
            logger.error(ex.toString());
        }
    }
    
    /**
     * Compares contents of the configuration set to contents of the candidate configuration set (excluding rendererJsp).
     */
    public boolean equals(ConfigurationSet candidateSet) throws SQLException {
        boolean result = true;
        
        logger.debug("Matching configuration set: \'" + toString() + "\' to candidate configuration set: \'" + candidateSet.toString() + "\'.");
        
        if (getName().equals(candidateSet.getName())) {
            if (getDescription().equals(candidateSet.getDescription())) {
                ArrayList<Configuration> myConfigurations = getConfigurations();
                ArrayList<Configuration> candidateConfigurations = candidateSet.getConfigurations();

                if (myConfigurations.size() == candidateConfigurations.size()) {
                    int matchCounter = 0;
                    for (Configuration myConfiguration : myConfigurations) {
                        for (Configuration candidateConfiguration : candidateConfigurations) {
                            if (myConfiguration.equals(candidateConfiguration)) {
                                matchCounter++;
                                break;
                            }
                        }
                    }

                    if (matchCounter != myConfigurations.size()) {
                        logger.debug("No match: configuration sets have different configurations.");
                        result = false;                
                    }
                } else {
                    logger.debug("No match: configuration sets have different size.");
                    result = false;
                }
            } else {
                logger.debug("No match: configuration sets have different descriptions.");
                result = false;                
            }
        } else {
            logger.debug("No match: configuration sets have different names.");
            result = false;            
        }
        
        if (result)
            logger.debug("Configuration sets match.");
        
        return result;
    }
    
    /**
     * Returns information about the configuration set.
     */     
    @Override
    public final String toString() {
        updateWegovConfigurationSetFromDatabase();
        
        int size = -1;
        
        try {
            size = size();
        } catch (SQLException ex) {
            logger.error(ex);
        }
        
        String result = "Configuration Set [" + getID() + "], name: \'" + wegovConfigurationSet.getName() + "\', description: \'" + wegovConfigurationSet.getDescription() + "\', size: \'" + size + "\', renderedJsp: \'" + wegovConfigurationSet.getRendererJsp() + "\'";
        
//        Activity myActivity = null;
//        try {
//            myActivity = getActivity();
//        } catch (SQLException ex) {
//            logger.error("Failed to get activity for configuration set " + getID());
//        }
//        
//        if (myActivity != null)
//            result += ", belongs to Activity [" + myActivity.getID() + "].";
//        else
            result += ".";
        
        return result;
    }    
    
}
