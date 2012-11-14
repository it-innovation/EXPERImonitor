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
//	Created Date :			2011-08-18
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator;


import eu.wegov.coordinator.dao.mgt.WegovPolicymaker_Activity;
import eu.wegov.coordinator.dao.mgt.WegovPolicymaker_WegovWorksheet;
import eu.wegov.coordinator.dao.mgt.WegovWorksheet;
import eu.wegov.coordinator.dao.mgt.WegovWorksheet_Activity;
import eu.wegov.coordinator.sql.SqlSchema;
import eu.wegov.coordinator.utils.Util;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import org.apache.log4j.Logger;

/**
 * Worksheets are top logical containers, hold activities.
 * 
 * @author Maxim Bashevoy
 */
public class Worksheet {
    private int id;
    private String name;
    private String comment;
    private String status;
    private Timestamp whenCreated;
    
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> timeHandle;
    private Coordinator coordinator;        // Maybe not needed!
    private WegovWorksheet wegovWorksheet;  // only temporary and useful object!
    
    private SqlSchema mgtSchema;
    
    public final static String STATUS_INITIALISING = "initialising";
    public final static String STATUS_PENDING = "pending";
    public final static String STATUS_RUNNING = "running";
    public final static String STATUS_SKIPPING = "skipping";
    public final static String STATUS_FAILED = "failed";
    public final static String STATUS_FINISHED = "finished";    
    public final static String STATUS_DELETED = "deleted";    
    
    private final static Logger logger = Logger.getLogger(Workflow.class.getName());

    public Worksheet() {
    
    }
    
    
    
    /**
     * Create new worksheet.
     */    
    Worksheet(String name, String comment, String status, Timestamp whenCreated, Coordinator coordinator) throws SQLException {
        this.id = 0;
        this.name = name;
        this.status = status;
        this.comment = comment;
        this.coordinator = coordinator;
        this.mgtSchema = coordinator.getMgtSchema();
        this.whenCreated = whenCreated;
        
        this.wegovWorksheet = new WegovWorksheet(name, comment, status, whenCreated);
        
        String iDfromDatabase = coordinator.getMgtSchema().insertObject(wegovWorksheet);
        
        logger.debug("Got database ID: \'" + iDfromDatabase + "\'");
        
        if (iDfromDatabase != null) {
            this.id = Integer.parseInt(iDfromDatabase);
        } else {
            throw new SQLException("Failed to assign database ID to workflow: \'" + name + "\'.");
        }      
        
//        logger.debug("Created new worksheet: " + toString());
    }
    
    /**
     * Get from database only
     */    
    Worksheet(int id, Coordinator coordinator) throws SQLException {
        this.id = id;
        this.coordinator = coordinator;
        this.mgtSchema = coordinator.getMgtSchema();
        
        ArrayList<WegovWorksheet> wfs = coordinator.getMgtSchema().getAllWhere(new WegovWorksheet(), "ID", id);
        
        if (wfs.isEmpty())
            throw new SQLException("Failed to find workflow with database ID: \'" + id + "\'.");
        
        this.wegovWorksheet = (WegovWorksheet) wfs.get(0);
        
        this.name = wegovWorksheet.getName();
        this.status = wegovWorksheet.getStatus();
        this.whenCreated = wegovWorksheet.getWhenCreated();
        
//        logger.debug("Restored worksheet: " + toString());
    }
    
    /**
     * Creates new activity and adds it to the worksheet.
     */
    public Activity createActivity(String name, String comment) throws SQLException {
        Activity newActivity = new Activity(name, comment, Activity.STATUS_INITIALISING, (new Util()).getTimeNowAsTimestamp(), coordinator);
        mgtSchema.insertObject(new WegovPolicymaker_Activity(getPolicyMaker().getID(), newActivity.getID()));
        addActivity(newActivity);
        return newActivity;
    }
    
    /**
     * Adds new activity to the worksheet.
     */
    public boolean addActivity(Activity activity) throws SQLException {
        
        boolean result = false;
        
        if (activity == null)
            throw new RuntimeException("Attempt to add \'null\' activity to worksheet: [" + id + "]");
        
        int activityID = activity.getID();
        
        // Check if activity has already beed added
        ArrayList<Integer> myActivitiesIDs = mgtSchema.getIDColumnValuesWhere(new WegovWorksheet_Activity(), "ActivityID", "WorksheetID", id);
        
        if (!myActivitiesIDs.contains(activityID)) {
            // Add it to wegovworksheets_activities
            logger.debug("Adding new activity: to the worksheet: [" + id + "]");
            mgtSchema.insertObject(new WegovWorksheet_Activity(id, activityID));
            result = true;
        } else {
            logger.error("Activity: is already in the worksheet: [" + id + "]");
        }
        
        return result;
    }
    
    /**
     * Returns all activities on the worksheet.
     */
    public ArrayList<Activity> getActivities() {
        ArrayList<Activity> result = new ArrayList<Activity>();
        
        try {
            for (Integer activityID : mgtSchema.getIDColumnValuesWhere(new WegovWorksheet_Activity(), "ActivityID", "WorksheetID", id)) {
                result.add(new Activity(activityID, coordinator));
            }
        } catch (SQLException ex) {
            logger.error(ex);
        }
        
        return result;
    }
    
    /**
     * Returns all activities on the worksheet that are not deleted.
     */
    public ArrayList<Activity> getNotDeletedActivities() {
        ArrayList<Activity> result = new ArrayList<Activity>();
        
        try {
            for (Integer activityID : mgtSchema.getIDColumnValuesWhere(new WegovWorksheet_Activity(), "ActivityID", "WorksheetID", id)) {
                Activity tempActivity = new Activity(activityID, coordinator);
                
                if (!tempActivity.getStatus().equals(Activity.STATUS_DELETED))
                    result.add(new Activity(activityID, coordinator));
            }
        } catch (SQLException ex) {
            logger.error(ex);
        }
        
        return result;
    }
    
    
    /**
     * Returns one activity with specified ID.
     */
    public Activity getActivityByID(int activityID) {
        int gotID = -1;
        
        try {
            for (Integer aid : mgtSchema.getIDColumnValuesWhere(new WegovWorksheet_Activity(), "ActivityID", "WorksheetID", id)) {
                if (activityID == aid)
                    gotID = aid;
            }
        } catch (SQLException ex) {
            logger.error(ex);
        }
        
        if (gotID == -1) {
            return null;
        } else {
            try {
                return new Activity(gotID, coordinator);
            } catch (SQLException ex) {
                logger.error(ex);
                return null;
            }
        }
    }    
    
    private void updateWegovWorksheetFromDatabase() {
        try {
            wegovWorksheet = (WegovWorksheet) mgtSchema.getAllWhere(wegovWorksheet, "ID", id).get(0);
        } catch (SQLException ex) {
            logger.error("Failed to update from database worksheet's details with ID: " + id);
            logger.error(ex.toString());
        }
    }
    
    private void setValue(String name, Object value) {
        try {
            mgtSchema.updateRow(wegovWorksheet, name, value, "ID", id);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
    
    private Object getValue(String name) {
        Object result = null;
        
        try {        
            result = mgtSchema.getColumnValue(wegovWorksheet, name, "ID", id);
        } catch (Exception ex) {
            logger.error(ex);
        }
        
        return result;
    }
    
    /**
     * Returns the policymaker who created the worksheet.
     */    
    public Policymaker getPolicyMaker() {
        Policymaker result = null;
        
        try {
            WegovPolicymaker_WegovWorksheet pt = (WegovPolicymaker_WegovWorksheet) mgtSchema.getAllWhere(new WegovPolicymaker_WegovWorksheet(), "WorksheetID", id).get(0);
            int policyMakerId = pt.getPolicymakerID();
            result = new Policymaker(policyMakerId, coordinator);
        } catch (SQLException ex) {
            logger.error("Failed to get policymaker from database for worksheet with ID: " + id);
            logger.error(ex.toString());
        }
        
        return result;
    }

    /**
     * Returns databaseID for the worksheet.
     */    
    public int getID() {
        return id;
    }

    /**
     * Returns name of the worksheet.
     */ 
    public String getName() {
        this.name = (String) getValue("Name");
        return name;
    }

    /**
     * Sets new name for the worksheet.
     */     
    public void setName(String newName) {
        this.name = newName;
        logger.debug("Setting name to: \'" + newName + "\'");
        setValue("Name", newName);
    }
    
    /**
     * Returns status for the worksheet.
     */    
    public String getStatus() {
        this.status = (String) getValue("Status");
        return status;
    }

    /**
     * Sets new status for the worksheet.
     */    
    public void setStatus(String newStatus) {
        this.status = newStatus;
        logger.debug("Setting status: \'" + newStatus + "\'");
        setValue("Status", newStatus);
    }
    
    /**
     * Returns comments for the worksheet.
     */
    public String getComment() {
        this.comment = (String) getValue("Comment");
        return comment;
    }

    /**
     * Sets new comments for the worksheet.
     */
    public void setComment(String newComment) {
        this.comment = newComment;
        logger.debug("Setting comment: \'" + newComment + "\'");
        setValue("Comment", newComment);
    }
    
    /**
     * Returns timestamp when the worksheet was created.
     */
    public Timestamp getWhenCreated() {
        this.whenCreated = (Timestamp) getValue("Created");
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
    
    /**
     * Schedules the worksheet to be startThreaded in the future after delay in milliseconds.
     */    
//    public ScheduledFuture startIn(long delay) {
//        timeHandle = startIn(delay, TimeUnit.MILLISECONDS);
//        return timeHandle;
//    }
//    
//    /**
//     * Schedules the worksheet to be startThreaded in the future after delay in custom units.
//     */
//    public ScheduledFuture startIn(long delay, TimeUnit unit) {
//        canRunCheck();
//        timeHandle = scheduler.schedule(this, delay, unit);
//        return timeHandle;
//    }
//    
//    /**
//     * Creates and executes a periodic action that becomes enabled first after the given initial delay, and subsequently with the given period; that is executions will commence after initialDelay then initialDelay+period, then initialDelay + 2 * period, and so on.
//     */
//    public ScheduledFuture scheduleAtFixedRate(long initialDelay, long period, TimeUnit unit) {
//        canRunCheck();
//        timeHandle = scheduler.scheduleAtFixedRate(this, initialDelay, period, unit);
//        return timeHandle;
//    }
//    
//    /**
//     *  Creates and executes a periodic action that becomes enabled first after the given initial delay, and subsequently with the given delay between the termination of one execution and the commencement of the next.
//     */
//    public ScheduledFuture scheduleWithFixedDelay(long initialDelay, long delay, TimeUnit unit) {
//        canRunCheck();
//        timeHandle = scheduler.scheduleWithFixedDelay(this, initialDelay, delay, unit);
//        return timeHandle;
//    }
//    
//    /**
//     *  Same as scheduleWithFixedDelay with unit = TimeUnit.MILLISECONDS.
//     */
//    public ScheduledFuture scheduleWithFixedDelay(long initialDelay, long delay) {
//        timeHandle = scheduleWithFixedDelay(initialDelay, delay, TimeUnit.MILLISECONDS);
//        return timeHandle;
//    }
//    
//    /**
//     * Same as scheduleAtFixedRate with unit = TimeUnit.MILLISECONDS.
//     */
//    public ScheduledFuture scheduleAtFixedRate(long initialDelay, long period) {
//        timeHandle = scheduleAtFixedRate(initialDelay, period, TimeUnit.MILLISECONDS);
//        return timeHandle;
//    }
    
    /**
     * Cancels the worksheet's execution.
     */
//    public void cancel(boolean nowOrWhenFinished) {
//        timeHandle.cancel(nowOrWhenFinished);
//    }
    
    /**
     * Checks if the worksheet's execution is finished.
     */
    public boolean isDone() {
        if (timeHandle != null)
            return timeHandle.isDone();
        else {
            String currentStatus = getStatus();
            if (currentStatus.equals(STATUS_FINISHED) | currentStatus.equals(STATUS_FAILED) | currentStatus.equals(STATUS_INITIALISING))
                return true;
            else
                return false;
        }
    }
    
    /**
     * Checks if the worksheet's execution has been canceled.
     */
//    public boolean isCancelled() {
//        return timeHandle.isCancelled();
//    }    

    /**
     * Runs the worksheet now.
     */
//    public void startThreaded() {
//        startIn(0);
//    }
//    
    /**
     * Checks if the worksheet is not already running.
     */
    private void canRunCheck() {
        if (isDone()) {
            logger.debug("Starting worksheet [" + getID() + "]");
        } else {
            logger.error("Unable to start worksheet as it is already running!");
//            throw new RuntimeException("Unable to startThreaded worksheet as it is already running!");
        } 
    }
    
    /**
     * Use startThreaded() instead.
     */
    public void start() {
        logger.debug("Running worksheet [" + getID() + "]");

        // Run all activities in order
        setStatus(STATUS_RUNNING);
        logger.debug("Running worksheet: [" + id + "]");

        if (execute())
            setStatus(STATUS_FINISHED);
        else
            setStatus(STATUS_FAILED);

        logger.debug("Finished running worksheet: [" + id + "] with result: \'" + getStatus() + "\'");
    }
    
    
    private boolean execute() {
        
        boolean success = true;

//        ArrayList<Activity> allActivities = getActivities();
        ArrayList<Activity> allActivities = getNotDeletedActivities();
        ArrayList<Thread> threads = new ArrayList<Thread>();
        
        for (int i = 0; i < allActivities.size(); i++) {
            Activity activity = allActivities.get(i);
            
            // TODO: check status not deleted
                if (i == 0) {
                    threads.add(new Thread(activity));
                } else {
                    activity.setThreadToWaitFor(threads.get(i - 1));
                    threads.add(new Thread(activity));
                }
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                logger.error(ex);
                success = false;
            }
        }
        
        return success;
    }
    
    /**
     * Convert the worksheet to an unlinked ConfigurationSet (WeGov Tool).
     */
    public ConfigurationSet toConfigurationSet() throws SQLException {
        ConfigurationSet newSet = coordinator.createConfigurationSet(getName(), getComment(), null);
        
        for (Activity activity : getNotDeletedActivities()) {
            ConfigurationSet activityConfigurationSet = activity.getConfigurationSet();
            
            for (Configuration activityConfiguration : activityConfigurationSet.getConfigurations()) {
                newSet.addConfiguration(activityConfiguration);
            }
        }
        
        return newSet;
    }
    
//    public Activity toActivity() throws SQLException {
//        Activity result = coordinator.createActivity(getPolicyMaker(), getName() + " as activity", "Workflow " + getName() + " saved as activity");
//        
//        ConfigurationSet allConfigurations = new ConfigurationSet();
//        for (Activity activity : getActivities()) {
//            for (Configuration c : activity.getConfigurationSet()) {
//                allConfigurations.add(c);
//            }
//        }
//        
//        result.setConfigurationSet(allConfigurations);
//        
//        return result;
//    }
//    
//    public ConfigurationSet toConfigurationSet() {
//        ConfigurationSet result = new ConfigurationSet();
//        
//        for (Activity activity : getActivities()) {
//            for (Configuration c : activity.getConfigurationSet()) {
//                result.add(c);
//            }
//        }
//        
//        return result;
//    }
    
    /**
     * Returns information about the worksheet.
     */     
    @Override
    public final String toString() {
        updateWegovWorksheetFromDatabase();
        return "[" + getID() + "] \'" + wegovWorksheet.getName() + "\' (" + wegovWorksheet.getComment() + "), status: \'" + 
                wegovWorksheet.getStatus() + "\', created: \'" + wegovWorksheet.getWhenCreated() + "\'";
    }

//    @Override
//    public void execute(JobExecutionContext context) throws JobExecutionException {
//        
//        this.id = context.getJobDetail().getJobDataMap().getIntValue("ID");
//        this.coordinator = (Coordinator) context.getJobDetail().getJobDataMap().get("Coordinator");
//        this.mgtSchema = coordinator.getMgtSchema();
//        
//        try {
//            ArrayList<WegovWorksheet> wfs = coordinator.getMgtSchema().getAllWhere(new WegovWorksheet(), "ID", id);
//
//            if (wfs.isEmpty())
//                throw new SQLException("Failed to find workflow with database ID: \'" + id + "\'.");
//
//            this.wegovWorksheet = (WegovWorksheet) wfs.get(0);
//
//            this.name = wegovWorksheet.getName();
//            this.status = wegovWorksheet.getStatus();        
//
//            start();
//        } catch (Exception ex) {
//            logger.error(ex);
//        }
//    }
    
//    public JobDetail getJobDetail() {
//        JobDetail tempDetail = newJob(Worksheet.class).withIdentity(Integer.toString(getID()), "group1").build();
//        
//        tempDetail.getJobDataMap().put("ID", getID());
//        tempDetail.getJobDataMap().put("Coordinator", coordinator);
//        
//        return tempDetail;
//    }
}
