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
//	Created Date :			2011-09-05
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator;

import eu.wegov.coordinator.dao.Dao;
import eu.wegov.coordinator.dao.mgt.WegovActivity_Run;
import eu.wegov.coordinator.dao.mgt.WegovConfigurationSet;
import eu.wegov.coordinator.dao.mgt.WegovPolicymaker_Run;
import eu.wegov.coordinator.dao.mgt.WegovRun;
import eu.wegov.coordinator.dao.mgt.WegovRun_ConfigurationSet;
import eu.wegov.coordinator.dao.mgt.WegovRun_Error;
import eu.wegov.coordinator.dao.mgt.WegovRun_Log;
import eu.wegov.coordinator.sql.SqlSchema;
import eu.wegov.coordinator.utils.Util;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * Each time an Activity is executed, a Run is created Each Run is linked to policymaker, configuration set that the Activity had at the moment of execution, and data produced by one or more jars from the Configuration set. Example:<br /><br />
<code>
        Coordinator coordinator = new Coordinator("coordinator.properties");<br/>
        coordinator.wipeDatabase(); // optional - removes everything from the database<br/>
        coordinator.setupWegovDatabase();<br/><br/>
        
        String maxFullName = "Maxim Bashevoy";<br/>
        String maxOrganisation = "IT Innovation";<br/>
        String maxUserName = "mbashevoy";<br/>
        String maxPassword = "password";<br/>
        Role maxRole = coordinator.getDefaultAdminRole();<br/><br/>
        
        Policymaker max = coordinator.createPolicyMaker(maxFullName, maxRole, maxOrganisation, maxUserName, maxPassword);  <br/><br/>      
        
        Worksheet worksheet1 = coordinator.createWorksheet(max, "First worksheet", "First worksheet to run Runs!");<br/><br/>
        
        Activity worksheet1Activity = worksheet1.createActivity("First activity", "First activity with runs!");<br/>
        worksheet1Activity.setConfigurationSet(coordinator.getConfigurationSetByID(3));<br/><br/>
        
        worksheet1.startThreaded();<br/><br/>
        
        do {<br/>
            &nbsp;&nbsp;&nbsp;Thread.sleep(1000);<br/>
            &nbsp;&nbsp;&nbsp;System.out.println("Worksheet status: " + worksheet1.getStatus());<br/>
            &nbsp;&nbsp;&nbsp;for (Activity activity : worksheet1.getActivities()) {<br/>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println("\t- Activity name: \'" + activity.getName() + "\', status: \'" + activity.getStatus() + "\'");<br/>
            &nbsp;&nbsp;&nbsp;}<br/><br/>
            
        } while (!worksheet1.isDone());<br/><br/>
  
        Activity activity1 = worksheet1.getActivities().get(0);<br/><br/>
        
        System.out.println("Last run logs:");<br/>
        System.out.println(activity1.getLastRun().getLogs());<br/>
        System.out.println("Last run errors:");<br/>
        System.out.println(activity1.getLastRun().getErrors());<br/>
        System.out.println("Last run data:");<br/><br/>
        
        ArrayList<WegovPostItem> data = activity1.getLastRun().getData(new WegovPostItem());<br/><br/>
        
        for (WegovPostItem postItem : data)<br/>
            &nbsp;&nbsp;&nbsp;System.out.println(postItem.toString()); 
 * </code>
 * 
 * @author Maxim Bashevoy
 */
public class Run {
    private int id;
    private String name;
    private String comment;
    private String status;
    private Timestamp whenStarted;
    private Timestamp whenFinished;
    
    private Coordinator coordinator;
    private WegovRun wegovRun;  // only temporary but useful object!
    
    private SqlSchema mgtSchema;
    private Util util = new Util();
    
    public final static String STATUS_INITIALISING = "initialising";
    public final static String STATUS_PENDING = "pending";
    public final static String STATUS_RUNNING = "running";
    public final static String STATUS_SKIPPING = "skipping";
    public final static String STATUS_FAILED = "failed";
    public final static String STATUS_FINISHED = "finished";    
    
    private final static Logger logger = Logger.getLogger(Run.class.getName());
    
    /**
     * Create new Run. 
     */    
    Run(String name, String comment, String status, Timestamp whenCreated, Coordinator coordinator) throws SQLException {
        this.id = 0;
        this.name = name;
        this.status = status;
        this.comment = comment;
        this.coordinator = coordinator;
        this.mgtSchema = coordinator.getMgtSchema();
        
        this.wegovRun = new WegovRun(name, comment, status, whenCreated, null);
        
        String iDfromDatabase = coordinator.getMgtSchema().insertObject(wegovRun);
        
        logger.debug("Got database ID: \'" + iDfromDatabase + "\' for new run.");
        
        if (iDfromDatabase != null) {
            this.id = Integer.parseInt(iDfromDatabase);
        } else {
            throw new SQLException("Failed to assign database ID to run: \'" + name + "\'.");
        }
        
//        ConfigurationSet configurationSet = new ConfigurationSet("\'" + name + "\' activity\'s configuration set ", "Configuration set for activity " + id, coordinator);
//        int csID = configurationSet.getID();
//        
//        mgtSchema.insertObject(new WegovActivity_ConfigurationSet(id, csID));
        
//        logger.debug("Created new run: " + toString());
    }
    
    /**
     * Get from database only
     */    
    Run(int id, Coordinator coordinator) throws SQLException {
        this.id = id;
        this.coordinator = coordinator;
        this.mgtSchema = coordinator.getMgtSchema();
        
        ArrayList<WegovRun> runs = coordinator.getMgtSchema().getAllWhere(new WegovRun(), "ID", id);
        
        if (runs.isEmpty())
            throw new SQLException("Failed to find run with database ID: \'" + id + "\'.");
        
        this.wegovRun = (WegovRun) runs.get(0);
        
        this.name = wegovRun.getName();
        this.status = wegovRun.getStatus();
        this.comment = wegovRun.getComment();
        
        this.whenStarted = wegovRun.getWhenStarted();
        this.whenFinished = wegovRun.getWhenFinished();
        
//        logger.debug("Successfully restored run from the database: " + toString());
    }

    /**
     * Returns configuration set for the run.
     */
    public ConfigurationSet getConfigurationSet() {
        int csID = getConfigurationSetID();
        if (csID > 0) {
            try {
                return new ConfigurationSet(csID, coordinator);
            } catch (SQLException ex) {
                logger.error("Failed to return configuration set for run: " + getID());
                logger.error(ex);
                return null;
            }
        } else 
            return null;
    }

    /**
     * Replace current configuration set for the run with a new one. Only a clone of ConfigurationSet given as the argument will be added. 
     */
    public boolean setConfigurationSet(ConfigurationSet configurationSet) {
        try {
            ConfigurationSet oldConfigurationSet = getConfigurationSet();
            
            if (oldConfigurationSet != null) {
                int oldConfigurationSetID = oldConfigurationSet.getID();

                // delete existing links to configuration set and the configuration set itself
                mgtSchema.deleteAllWhere(new WegovRun_ConfigurationSet(), "RunID", id);
                mgtSchema.deleteAllWhere(new WegovConfigurationSet(), "ID", oldConfigurationSetID);
            }

            ConfigurationSet tempSet = configurationSet.clone();

            mgtSchema.insertObject(new WegovRun_ConfigurationSet(id, tempSet.getID()));
            return true;
        } catch(SQLException ex) {
            logger.error("Failed to return set configuration set for run: " + getID());
            logger.error(ex);            
            return false;
        }
    }
    
    private int getConfigurationSetID() {
        try {
            ArrayList<Integer> ids = mgtSchema.getIDColumnValuesWhere(new WegovRun_ConfigurationSet(), "ConfigurationSetID", "RunID", id);
            if (ids.isEmpty())
                return -1;
            else
                return ids.get(0);        
        } catch (SQLException ex) {
            logger.error("Failed to get configuration set ID for run: " + getID());
            logger.error(ex);
            return -1;
        }
    }
    
    /**
     * Add new configuration to the configuration set of the activity.
     */
    public void addConfiguration(Configuration c) throws SQLException {
        if (c != null)
            getConfigurationSet().addConfiguration(c);
        else
            throw new RuntimeException("Null configuration can not be added to run: [" + id + "]");
    }
    
    private void updateWegovRunFromDatabase() {
        try {
            wegovRun = (WegovRun) mgtSchema.getAllWhere(wegovRun, "ID", id).get(0);
        } catch (SQLException ex) {
            logger.error("Failed to update from database run's details with ID: " + id);
            logger.error(ex.toString());
        }
    }
    
    private void setValue(String name, Object value) {
        try {
            mgtSchema.updateRow(wegovRun, name, value, "ID", id);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
    
    private Object getValue(String name) {
        Object result = null;
        
        try {        
            result = mgtSchema.getColumnValue(wegovRun, name, "ID", id);
        } catch (Exception ex) {
            logger.error(ex);
        }
        
        return result;
    }    


    /**
     * Returns databaseID for the run.
     */    
    public int getID() {
        return id;
    }

    /**
     * Returns name of the run.
     */ 
    public String getName() {
//        this.name = (String) getValue("Name");
        return name;
    }

    /**
     * Sets new name for the run.
     */     
    public void setName(String newName) {
        this.name = newName;
        logger.debug("Setting name to: \'" + newName + "\'");
        setValue("Name", newName);
    }
    
    /**
     * Returns status for the run.
     */    
    public String getStatus() {
//        this.status = (String) getValue("Status");
        return status;
    }

    /**
     * Sets new status for the run.
     */    
    public void setStatus(String newStatus) {
        this.status = newStatus;
        logger.debug("Setting status: \'" + newStatus + "\'");
        setValue("Status", newStatus);
    }
    
    /**
     * Returns comments for the run.
     */
    public String getComment() {
//        this.comment = (String) getValue("Comment");
        return comment;
    }

    /**
     * Sets new comments for the run.
     */
    public void setComment(String newComment) {
        this.comment = newComment;
        logger.debug("Setting comment: \'" + newComment + "\'");
        setValue("Comment", newComment);
    }
    
    /**
     * Returns timestamp when the run was started.
     */
    public Timestamp getWhenStarted() {
//        this.whenCreated = (Timestamp) getValue("Started");
        return whenStarted;
    } 
    
    /**
     * Sets new when started time.
     */
    public void setWhenStarted(Timestamp newStarted) {
        this.whenStarted = newStarted;
        logger.debug("Setting when started: \'" + newStarted + "\'");
        setValue("Started", newStarted);
    }    
    
    /**
     * Returns timestamp when the run was finished.
     */
    public Timestamp getWhenFinished() {
//        this.whenCreated = (Timestamp) getValue("Finished");
        return whenFinished;
    } 
    
    /**
     * Sets new when finished time.
     */
    public void setWhenFinished(Timestamp newFinished) {
        this.whenFinished = newFinished;
        logger.debug("Setting when finished: \'" + newFinished + "\'");
        setValue("Finished", newFinished);
    }
    
    
    /**
     * Returns parent activity.
     */    
    public Activity getActivity() {
        try {
            WegovActivity_Run activity_run = (WegovActivity_Run) mgtSchema.getAllWhere(new WegovActivity_Run(), "RunID", id).get(0);
            int activityId = activity_run.getActivityID();
            return new Activity(activityId, coordinator);
        } catch (SQLException ex) {
            logger.error("Failed to get activity from database for run with ID: " + id);
            logger.error(ex);
            return null;
        }
    }    
    
    /**
     * Returns parent activity id.
     */    
    public int getActivityId() {
    	try {
    		WegovActivity_Run activity_run = (WegovActivity_Run) mgtSchema.getAllWhere(new WegovActivity_Run(), "RunID", id).get(0);
    		return activity_run.getActivityID();
    	} catch (SQLException ex) {
    		logger.error("Failed to get activity from database for run with ID: " + id);
    		logger.error(ex);
    		return 0;
    	}
    }    
    
    /**
     * Returns the policymaker who created the run.
     */    
    public Policymaker getPolicyMaker() {
        try {
            WegovPolicymaker_Run pm_run = (WegovPolicymaker_Run) mgtSchema.getAllWhere(new WegovPolicymaker_Run(), "RunID", id).get(0);
            int policyMakerId = pm_run.getPolicymakerID();
            return new Policymaker(policyMakerId, coordinator);
        } catch (SQLException ex) {
            logger.error("Failed to get policymaker from database for run with ID: " + id);
            logger.error(ex);
            return null;
        }
    }
    
    /**
     * Returns logs for the run.
     */
    public String getLogs() throws SQLException {
        String result = "";
        
        ArrayList<WegovRun_Log> logs = mgtSchema.getAllWhere(new WegovRun_Log(), "RunID", id);
        
        for (WegovRun_Log line : logs) {
            result = result + line.getText() + "\n";
        }
        
        return result;
    }
    
    /**
     * Returns error logs for the run.
     */
    public String getErrors() throws SQLException {
        String result = "";
        
        ArrayList<WegovRun_Error> errors = mgtSchema.getAllWhere(new WegovRun_Error(), "RunID", id);
        
        for (WegovRun_Error line : errors) {
            result = result + line.getText() + "\n";
        }
        
        return result;
    } 
    
    /**
     * Returns data in the form of an ArrayList of data objects, see dao.data package.
     */
    public <T extends Dao> ArrayList<T> getData(T daoToReturn) throws SQLException {
        ArrayList<T> result = new ArrayList<T>();
        
        result = coordinator.getDataSchema().getAllWhere(daoToReturn, "OutputOfRunID", getID());
        
        return result;
    }

    /**
     * Returns first results data item, from results sorted by the given field
     */
    public <T> Dao getFirstResult(T daoToReturn, String sortBy) throws SQLException {
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	map.put("OutputOfRunID", getID());

        Dao result = coordinator.getDataSchema().getFirstWhereSortBy((Dao) daoToReturn, map, sortBy);
        
        return result;
    }

    
    /**
     * Returns information about the run.
     */     
    @Override
    public final String toString() {
        updateWegovRunFromDatabase();
        String result = "Run [" + getID() + "], name: \'" + wegovRun.getName() + "\', comment: \'" + wegovRun.getComment() + "\', status: \'" + 
                wegovRun.getStatus() + "\', created: \'" + wegovRun.getWhenStarted() + "\', finished: \'" + wegovRun.getWhenFinished() + "\'.\n";
        
//        ConfigurationSet tempSet = getConfigurationSet();
//        
//        if (tempSet != null)
//            result += "\t- " + tempSet.toString();
        
        return result.substring(0, result.length() - 1);
    }    
}
