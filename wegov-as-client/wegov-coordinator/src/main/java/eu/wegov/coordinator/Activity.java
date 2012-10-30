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
//	Created Date :			2011-08-01
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator;

import eu.wegov.coordinator.dao.data.WegovWidgetDataAsJson;
import eu.wegov.coordinator.dao.mgt.WegovActivity;
import eu.wegov.coordinator.dao.mgt.WegovActivity_ConfigurationSet;
import eu.wegov.coordinator.dao.mgt.WegovActivity_InputActivity;
import eu.wegov.coordinator.dao.mgt.WegovActivity_Run;
import eu.wegov.coordinator.dao.mgt.WegovConfigurationSet;
import eu.wegov.coordinator.dao.mgt.WegovPolicymaker_Activity;
import eu.wegov.coordinator.dao.mgt.WegovRun_Error;
import eu.wegov.coordinator.dao.mgt.WegovRun_Log;
import eu.wegov.coordinator.dao.mgt.WegovWorksheet_Activity;
import eu.wegov.coordinator.sql.SqlSchema;
import eu.wegov.coordinator.sql.SqlTable;
import eu.wegov.coordinator.utils.Util;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 * Activities are containers for configuration sets, activites themselves are held within worksheets. Activities can startThreaded all configurations within themselves in order.<br/><br/>
 <code>
    Coordinator coordinator = new Coordinator("coordinator.properties");<br/>
    coordinator.wipeDatabase(); // optional - removes everything from the database<br/>
    coordinator.setupWegovDatabase();<br/><br/>
 
    Activity activity = coordinator.createActivity(new Policymaker("Maxim Bashevoy", "IT Innovation", "max", "test", coordinator), "Test activity", "A comment");<br/><br/>

    Configuration configuration = new Configuration("Test configuration", "java -version", "Test description", coordinator);<br/>
    configuration.addParameter("searchFor", "What to search on Twitter for", "bbc", coordinator.getDefaultUserRole());<br/><br/>

    activity.addConfiguration(configuration);<br/><br/>

    System.out.println(activity);
 </code>
 * @author Maxim Bashevoy
 */
public class Activity implements Runnable {
    private int id;
    private String name;
    private String comment;
    private String status;
    private Timestamp whenCreated;
    
    private Thread threadToWaitFor;
    private Coordinator coordinator;
    private WegovActivity wegovActivity;  // only temporary but useful object!
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> timeHandle;    
    
    private SqlSchema mgtSchema;
    private Util util = new Util();
    
    public final static String STATUS_INITIALISING = "initialising";
    public final static String STATUS_PENDING = "pending";
    public final static String STATUS_RUNNING = "running";
    public final static String STATUS_SKIPPING = "skipping";
    public final static String STATUS_FAILED = "failed";
    public final static String STATUS_FINISHED = "finished";
    public final static String STATUS_DELETED = "deleted";
    
    public final static String LAST_RUN = "last";
    public final static String ALL_RUNS = "all";
    
    private final static Logger logger = Logger.getLogger(Activity.class.getName());
    
    /**
     * Create new activity. 
     */    
    Activity(String name, String comment, String status, Timestamp whenCreated, Coordinator coordinator) throws SQLException {
        this.id = 0;
        this.name = name;
        this.status = status;
        this.comment = comment;
        this.coordinator = coordinator;
        this.mgtSchema = coordinator.getMgtSchema();
        this.whenCreated = whenCreated;
        
        this.wegovActivity = new WegovActivity(name, comment, status, whenCreated);
        
        String iDfromDatabase = coordinator.getMgtSchema().insertObject(wegovActivity);
        
        if (iDfromDatabase != null) {
            this.id = Integer.parseInt(iDfromDatabase);
        } else {
            throw new SQLException("Failed to assign database ID to activity: \'" + name + "\'.");
        }
        
//        ConfigurationSet configurationSet = new ConfigurationSet("\'" + name + "\' activity\'s configuration set ", "Configuration set for activity " + id, coordinator);
//        int csID = configurationSet.getID();
//        
//        mgtSchema.insertObject(new WegovActivity_ConfigurationSet(id, csID));
        
        logger.debug("Created new activity [" + id + "].");
    }
    
    /**
     * Get from database only
     */    
    Activity(int id, Coordinator coordinator) throws SQLException {
        this.id = id;
        this.coordinator = coordinator;
        this.mgtSchema = coordinator.getMgtSchema();
        
        ArrayList<WegovActivity> activities = coordinator.getMgtSchema().getAllWhere(new WegovActivity(), "ID", id);
        
        if (activities.isEmpty())
            throw new SQLException("Failed to find activity with database ID: \'" + id + "\'.");
        
        this.wegovActivity = (WegovActivity) activities.get(0);
        
        this.name = wegovActivity.getName();
        this.comment = wegovActivity.getComment();
        this.status = wegovActivity.getStatus();
        this.whenCreated = wegovActivity.getWhenCreated();
        
        logger.debug("Restored activity [" + id + "].");
    }

    /**
     * Returns configuration set for the activity.
     */
    public ConfigurationSet getConfigurationSet() {
        int csID = getConfigurationSetID();
        
        if (csID > 0) {
            try {
                return new ConfigurationSet(csID, coordinator);
            } catch (SQLException ex) {
                logger.error("Failed to return configuration set for activity: " + getID());
                logger.error(ex);
                return null;
            }
        } else 
            return null;
    }

    /**
     * Replace current configuration set for the activity with a new one. Only a clone of ConfigurationSet given as the argument will be added. 
     */
    public void setConfigurationSet(ConfigurationSet configurationSet) throws SQLException {
        
        ConfigurationSet oldConfigurationSet = getConfigurationSet();
        if (oldConfigurationSet != null) {
            int oldConfigurationSetID = oldConfigurationSet.getID();
            
            // delete existing links to configuration set and the configuration set itself
            mgtSchema.deleteAllWhere(new WegovActivity_ConfigurationSet(), "ActivityID", id);
            mgtSchema.deleteAllWhere(new WegovConfigurationSet(), "ID", oldConfigurationSetID);
        }
        
        ConfigurationSet tempSet = configurationSet.clone();
        
        mgtSchema.insertObject(new WegovActivity_ConfigurationSet(id, tempSet.getID()));
    }
    
    private int getConfigurationSetID() {
        try {
            ArrayList<Integer> ids = mgtSchema.getIDColumnValuesWhere(new WegovActivity_ConfigurationSet(), "ConfigurationSetID", "ActivityID", id);
            if (ids.isEmpty())
                return -1;
            else
                return ids.get(0);        
        } catch (SQLException ex) {
            logger.error("Failed to get configuration set ID for activity: " + getID());
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
            throw new RuntimeException("Null configuration can not be added to activity: [" + id + "]");
    }
    
    private void updateWegovActivityFromDatabase() {
        try {
            wegovActivity = (WegovActivity) mgtSchema.getAllWhere(wegovActivity, "ID", id).get(0);
        } catch (SQLException ex) {
            logger.error("Failed to update from database activity's details with ID: " + id);
            logger.error(ex.toString());
        }
    }
    
    private void setValue(String name, Object value) {
        try {
            mgtSchema.updateRow(wegovActivity, name, value, "ID", id);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
    
    private Object getValue(String name) {
        Object result = null;
        
        try {        
            result = mgtSchema.getColumnValue(wegovActivity, name, "ID", id);
        } catch (Exception ex) {
            logger.error(ex);
        }
        
        return result;
    }    
    
    /**
     * Returns all worksheets the activity belongs to.
     */
    public ArrayList<Worksheet> getWorksheets() throws SQLException {
        ArrayList<Worksheet> result = new ArrayList<Worksheet>();
        
        for (Integer activityID : mgtSchema.getIDColumnValuesWhere(new WegovWorksheet_Activity(), "WorksheetID", "ActivityID", id)) {
            result.add(new Worksheet(activityID, coordinator));
        }        
        
        return result;
    }
    
    /**
     * Returns the policymaker who created the activity.
     */    
    public Policymaker getPolicyMaker() {
        Policymaker result = null;
        
        try {
            WegovPolicymaker_Activity pt = (WegovPolicymaker_Activity) mgtSchema.getAllWhere(new WegovPolicymaker_Activity(), "ActivityID", id).get(0);
            int policyMakerId = pt.getPolicymakerID();
            result = new Policymaker(policyMakerId, coordinator);
        } catch (SQLException ex) {
            logger.error("Failed to get policymaker from database for activity with ID: " + id);
            logger.error(ex);
        }
        
        return result;
    }

    /**
     * Returns databaseID for the activity.
     */    
    public int getID() {
        return id;
    }

    /**
     * Returns name of the activity.
     */ 
    public String getName() {
//        this.name = (String) getValue("Name");
        return name;
    }

    /**
     * Sets new name for the activity.
     */     
    public void setName(String newName) {
        this.name = newName;
        logger.debug("Setting name to: \'" + newName + "\'");
        setValue("Name", newName);
    }
    
    /**
     * Returns status for the activity.
     */    
    public String getStatus() {
//        this.status = (String) getValue("Status");
        return status;
    }

    /**
     * Sets new status for the activity.
     */    
    public void setStatus(String newStatus) {
        this.status = newStatus;
        logger.debug("Setting status: \'" + newStatus + "\'");
        setValue("Status", newStatus);
    }

    /**
     * Sets new status for the activity and selected startThreaded.
     */    
    public void setStatus(String newStatus, Run thisRun) {
        this.status = newStatus;
        logger.debug("Setting status: \'" + newStatus + "\'");
        setValue("Status", newStatus);
        thisRun.setStatus(newStatus);
    }
    
    /**
     * Returns comments for the activity.
     */
    public String getComment() {
//        this.comment = (String) getValue("Comment");
        return comment;
    }

    /**
     * Sets new comments for the activity.
     */
    public void setComment(String newComment) {
        this.comment = newComment;
        logger.debug("Setting comment: \'" + newComment + "\'");
        setValue("Comment", newComment);
    }
    
    /**
     * Returns timestamp when the activity was created.
     */
    public Timestamp getWhenCreated() {
//        this.whenCreated = (Timestamp) getValue("Created");
        return whenCreated;
    }

    /**
     * Sets new when created time.
     */
    public void setWhenCreated(Timestamp newCreated) {
        this.whenCreated = newCreated;
        logger.debug("Setting when created: \'" + newCreated + "\'");
        setValue("Created", newCreated);
    }

    public void setThreadToWaitFor(Thread threadToWaitFor) {
        this.threadToWaitFor = threadToWaitFor;
    }
    
    /**
     * Run the activity now using java.concurrent.
     */
    public void startThreaded() {
        startIn(0);
    }
    
    /**
     * Run the activity now.
     */
    public void start() {
        run();
    }
    
    /**
     * Schedules the activity to be startThreaded in the future after delay in milliseconds.
     */    
    private ScheduledFuture startIn(long delay) {
        
        return timeHandle = startIn(delay, TimeUnit.MILLISECONDS);
        
    }
    
    /**
     * Schedules the activity to be startThreaded in the future after delay in custom units.
     */
    private ScheduledFuture startIn(long delay, TimeUnit unit) {
        
        if (getConfigurationSet().isEmpty())
            throw new RuntimeException("No configurations to run in activity: " + getID());
        
        if (getConfigurationSet() == null)
            throw new RuntimeException("Configuration set is null for activity: " + getID());
        
        return timeHandle = scheduler.schedule(this, delay, unit);
    }
    
    /**
     * Checks if the activity's execution is finished.
     */
    public boolean isDone() {
        return timeHandle.isDone();
    }
    
    /**
     * Returns all runs for the activity.
     */
    public ArrayList<Run> getRuns() {
        try {
            ArrayList<Run> result = new ArrayList<Run>();
            for (int runID : mgtSchema.getIDColumnValuesWhere(new WegovActivity_Run(), "RunID", "ActivityID", getID())) {
                result.add(new Run(runID, coordinator));
            }
            return result;
        } catch (Exception ex) {
            logger.error("Failed to get all runs from database for activity with ID: " + id);
            logger.error(ex);
            return null;
        }
    }
    
    /**
     * Tell the Activity where to look for inputs.
     */
    private void addInputsFromActivity(Activity inputsActivity, String whichRuns) throws SQLException {
        
        if (inputsActivity == null)
            throw new RuntimeException("Unable to link inputs from a null Activity!");
        
        int inputActivityID = inputsActivity.getID();
        
        if (inputActivityID == getID())
            throw new RuntimeException("Unable to link own inputs for Activity [" + getID() + "]!");
        
        if (whichRuns.trim().equals(LAST_RUN)) {
            logger.debug("Linking last run of Activity [" + inputActivityID + "] to Activity [" + getID() + "].");
        } else if(whichRuns.trim().equals(ALL_RUNS)) {
            logger.debug("Linking all runs of Activity [" + inputActivityID + "] to Activity [" + getID() + "].");
        } else if ( (whichRuns.trim().startsWith("[")) & (whichRuns.trim().endsWith("]")) ) {
            logger.debug("Linking runs " + whichRuns + " of Activity [" + inputActivityID + "] to Activity [" + getID() + "].");
        } else {
            logger.error("Unrecognised runs format: \"" + whichRuns + "\" whilst linking runs of Activity [" + inputActivityID + "] to Activity [" + getID() + "].");
            throw new RuntimeException("Unrecognised runs format: \"" + whichRuns + "\" whilst linking runs of Activity [" + inputActivityID + "] to Activity [" + getID() + "].");
        }
        
        if (getInputActivitiesIDs().contains(inputActivityID)) {
            logger.error("Inputs from Activity [" + inputActivityID + "] already linked to Activity [" + getID() + "].");
        } else {
//            logger.debug("Linking inputs of Activity [" + inputActivityID + "] to Activity [" + getID() + "].");
            mgtSchema.insertObject(new WegovActivity_InputActivity(getID(), inputActivityID, whichRuns));
        }
    }
    
    public void addLastRunAsInputFromActivity(Activity inputsActivity) throws SQLException {
        addInputsFromActivity(inputsActivity, LAST_RUN);
    }
    
    public void addAllRunsAsInputFromActivity(Activity inputsActivity) throws SQLException {
        addInputsFromActivity(inputsActivity, ALL_RUNS);
    }
    
    public void addSelectedRunsAsInputFromActivity(Activity inputsActivity, ArrayList<String> runsIDs) throws SQLException {
        
        if (runsIDs == null) {
            logger.debug("Can not accept null as list of run IDs");
            throw new RuntimeException("Can not accept null as list of run IDs");
            
        }
        
        if (runsIDs.isEmpty()) {
            logger.debug("List of run IDs is empty, inputs will not be added!");
            throw new RuntimeException("List of run IDs is empty, inputs will not be added!");
        }
        
        String whichRuns = "[";
        
        for (String runID : runsIDs) {
            whichRuns += runID + ", ";
        }
        
        whichRuns = whichRuns.substring(0, whichRuns.length() - 2) + "]";
        
        addInputsFromActivity(inputsActivity, whichRuns);
        
    }
    
    public void removeAllInputs() throws SQLException {
        mgtSchema.deleteAllWhere(new WegovActivity_InputActivity(), "ActivityID", id);
    }
    
    public void removeInputsOfActivity(Activity inputsActivity) throws SQLException {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("ActivityID", id);
        map.put("InputActivityID", inputsActivity.getID());
        
        mgtSchema.deleteAllWhere(new WegovActivity_InputActivity(), map);
    }
    
    /**
     * Returns information on where the Activity will look for inputs.
     */
    public ArrayList<Activity> getInputActivities() throws SQLException {
        ArrayList<Activity> tempList = new ArrayList<Activity>();
        
        for (int inputActivityID : getInputActivitiesIDs()) {
            tempList.add(new Activity(inputActivityID, coordinator));
        }
        
        return tempList;
    }
    
    /**
     * Returns IDs of Activities from which the Activity will get its inputs.
     */
    public ArrayList<Integer> getInputActivitiesIDs() throws SQLException {
        return mgtSchema.getIDColumnValuesWhere(new WegovActivity_InputActivity(), "InputActivityID", "ActivityID", getID());
    }
    
    /**
     * Returns a map of Activity IDs and runs to use as input
     */
    public LinkedHashMap<Integer, String> getInputs() throws SQLException {
        LinkedHashMap<Integer, String> inputs = new LinkedHashMap<Integer, String>();
        
        ArrayList<WegovActivity_InputActivity> aias = mgtSchema.getAllWhere(new WegovActivity_InputActivity(), "ActivityID", getID());
        
        for (WegovActivity_InputActivity aia : aias) {
            int inputActivityID = aia.getInputActivityID();
            String whichRuns = aia.getWhichRuns();
            inputs.put(inputActivityID, whichRuns);
        }        
        
        return inputs;
    }
    
    /**
     * Returns startThreaded with required database ID.
     */
    public Run getRunByID(int runID) throws SQLException {
        int gotID = -1;
        
        for (int rID : mgtSchema.getIDColumnValuesWhere(new WegovActivity_Run(), "RunID", "ActivityID", getID())) {
            if (rID == runID)
                gotID = rID;
        }
        
        if (gotID == -1)
            return null;
        else
            return new Run(gotID, coordinator);
    }    
    
    /**
     * Returns last startThreaded for the activity.
     */
    public Run getLastRun() {
        try {
            ArrayList<Integer> allIDs = mgtSchema.getIDColumnValuesWhere(new WegovActivity_Run(), "RunID", "ActivityID", getID());
            int maxID = util.maxIntFromArrayList(allIDs);
            return new Run(maxID, coordinator);
        } catch (Exception ex) {
            logger.error("Failed to get last run from database for activity with ID: " + id);
            logger.error(ex);
            return null;
        }
    }
   
    /**
     * Returns run previous to the given run, for this activity.
     */
    public Run getPreviousRun(int runID) {
        try {
            ArrayList<Integer> allIDs = mgtSchema.getIDColumnValuesWhere(new WegovActivity_Run(), "RunID", "ActivityID", getID());
            Integer[] sortedIds = util.sortArrayList(allIDs);
            for (int i = 0; i < sortedIds.length; i++) {
				int id = sortedIds[i];
				if (id == runID) {
					if (i>0) {
						int prevRunID = sortedIds[i-1];
						return new Run(prevRunID, coordinator);
					}
				}
			}
            return null;
        } catch (Exception ex) {
            logger.error("Failed to get last run from database for activity with ID: " + id);
            logger.error(ex);
            return null;
        }
    }
   
    /**
     * Returns last run (for this activity), prior to this one, which has results.
     */
    public Run getPreviousRunWithResults(int runID) {
        try {
        	System.out.println("Getting run prior to " + runID);
            ArrayList<Integer> allIDs = mgtSchema.getIDColumnValuesWhere(new WegovActivity_Run(), "RunID", "ActivityID", getID());
            Integer[] sortedIds = util.sortArrayList(allIDs);
            //TODO: loop from end which is more efficient
            for (int i = 0; i < sortedIds.length; i++) {
				int id = sortedIds[i];
				if (id == runID) {
					if (i>0) {
						int prevRunID = sortedIds[i-1];
						System.out.println("Run id = " + prevRunID);
						Run run = new Run(prevRunID, coordinator);
						WegovWidgetDataAsJson results = coordinator.getResultsForRun(prevRunID, getPolicyMaker().getID(), false);
						
						if (results != null) {
							int nResults = results.getNumResults();
							System.out.println("nResults = " + nResults);
							if (nResults > 0) {
								System.out.println("Previous run with results = " + prevRunID);
								return run;
							}
						}
						
						System.out.println("No results for run - getting previous run...");
						return getPreviousRunWithResults(prevRunID);
					}
				}
			}
            return null;
        } catch (Exception ex) {
            logger.error("Failed to get last run from database for activity with ID: " + id);
            logger.error(ex);
            return null;
        }
    }
    /**
     * USE "start()" METHOD INSTEAD.
     */
    
    
    public void run() {
        logger.debug("Activity [" + getID() + "] is queued for execution.");
        
        // Create new Configuration by cloning existing one
        ConfigurationSet currentConfigurationSet = getConfigurationSet();
        
        // Create new Run object and link it to the configuration
        Run thisRun = coordinator.createRun(this, "Run name with number", "Run comment");
        
        setStatus(STATUS_INITIALISING, thisRun);
        
        int thisRunID = thisRun.getID();
        thisRun.setName("Run [" + thisRunID + "] for activity [" + getID() + "]");
        thisRun.setConfigurationSet(currentConfigurationSet);
        thisRun.setComment("Created using configuration set [" + thisRun.getConfigurationSet().getID() + "]");
        
        if (threadToWaitFor == null) {
            // I'm the first one! Just startThreaded
            execute(thisRun);
        } else {
            logger.debug("Activity [" + getID() + "] is pending.");
            setStatus(STATUS_PENDING, thisRun);
            try {
                threadToWaitFor.join();
                execute(thisRun);
            } catch (InterruptedException ex) {
                logger.debug("Activity [" + getID() + "] is being skipped.");
                setStatus(STATUS_SKIPPING, thisRun);
                logger.error(ex);
            }
        }
               
        logger.debug("Finished running activity [" + getID() + "] with status: \'" + getStatus() + "\'");
    }
    
    /**
     * Executes all configurations in the activity.
     */
    private void execute(Run thisRun) {
        boolean success = true;

        // split startThreaded into processes and follow each process
        
        logger.debug("Activity [" + getID() + "] is about to run.");
        setStatus(STATUS_RUNNING, thisRun);
        
        try {
            
            SqlTable logs = coordinator.getTasksLogsTable();
            SqlTable errors = coordinator.getTasksErrorsTable();

            int runID = thisRun.getID();
            
            for (Configuration configuration : thisRun.getConfigurationSet().getConfigurations()) {
            
                String stdOut = null;
                String errOut = null;
                
                StringBuilder fullLog = new StringBuilder("");
                StringBuilder fullErrors = new StringBuilder("");
                
                String command = configuration.getCommand();
                int configurationID = configuration.getID();
                
                // Modify command for the jar
                command = command + " " + runID + " " + configurationID + " " + coordinator.getAbsolutePathToConfigurationFile();
                
                logger.debug("Running command: \'" + command + "\'");
                Process p = Runtime.getRuntime().exec(command);

                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                while ((stdOut = stdInput.readLine()) != null) {
                    try {
						logs.insertRow(new WegovRun_Log(runID, stdOut));
					} catch (Exception e) {
						e.printStackTrace(System.out);
					}
//                    fullLog.append(stdOut);
//                    fullLog.append("\n");

                }

                while ((errOut = stdError.readLine()) != null) {
//                    fullErrors.append(errOut);
//                    fullErrors.append("\n");

                    try {
						errors.insertRow(new WegovRun_Error(runID, errOut));
					} catch (Exception e) {
						e.printStackTrace(System.out);
					}
                }        

                p.waitFor();
                
//                logs.insertRow(new WegovRun_Log(runID, fullLog.toString()));
//                errors.insertRow(new WegovRun_Error(runID, fullErrors.toString()));
                
                if (p.exitValue() != 0)
                    success = false;
            }
            
            
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace(System.out);
            success = false;

        }
        
        if(success) {
            logger.debug("Activity [" + getID() + "] finished successfully.");
            setStatus(STATUS_FINISHED, thisRun);
        } else {
            logger.debug("Activity [" + getID() + "] failed.");
            setStatus(STATUS_FAILED, thisRun);
        }
        
        thisRun.setWhenFinished(util.getTimeNowAsTimestamp());
    }
    
    /**
     * Returns information about the activity.
     */     
    @Override
    public final String toString() {
        updateWegovActivityFromDatabase();
        String result = "Activity [" + getID() + "], name: \'" + wegovActivity.getName() + "\', comment: \'" + wegovActivity.getComment() + "\', status: \'" + 
                wegovActivity.getStatus() + "\', created: \'" + wegovActivity.getWhenCreated() + "\'.\n";
        
        try {
            ConfigurationSet tempSet = getConfigurationSet();
            if (tempSet != null) {
                result += "\t-" + tempSet.toString() + "\n";
                for (int i = 0; i < tempSet.size(); i++) {
                    Configuration c = tempSet.get(i);
                    result += "\t\t- " + c.toString() + "\n";
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        
        return result.substring(0, result.length() - 1);
    }

//    @Override
//    public void execute(JobExecutionContext jec) throws JobExecutionException {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
}
