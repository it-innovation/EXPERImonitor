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

import eu.wegov.coordinator.dao.mgt.WegovPolicymaker;
import eu.wegov.coordinator.dao.mgt.WegovPolicymaker_Task;
import eu.wegov.coordinator.dao.mgt.WegovTask;
import eu.wegov.coordinator.dao.mgt.WegovRun_Error;
import eu.wegov.coordinator.dao.mgt.WegovRun_Log;
import eu.wegov.coordinator.dao.mgt.WegovTask_Threads;
import eu.wegov.coordinator.sql.SqlSchema;
import eu.wegov.coordinator.sql.SqlTable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 *
 * @author Maxim Bashevoy
 */
public class Task<T> implements Runnable {
    private int id;
    private String name;
    private String type;
    private String status;
    private String command;
    private int workflowOrder;
    
    private Thread[] threadsToWaitFor;
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> timeHandle;
    private Coordinator coordinator;
    private WegovTask wegovTask; // only temporary and useful object!
    
    private SqlSchema mgtSchema;
    
    
    private final static Logger logger = Logger.getLogger(Task.class.getName());
    
    /**
     * Create new task only
     */
    public Task(String name, String type, String status, String command, Coordinator coordinator) throws SQLException {
        this.id = 0;
        this.workflowOrder = 0;
        this.name = name;
        this.type = type;
        this.status = status;
        this.command = command;
        this.coordinator = coordinator;
        this.wegovTask = new WegovTask(name, status, type, command, workflowOrder);
        this.mgtSchema = coordinator.getMgtSchema();
        
        String iDfromDatabase = coordinator.getMgtSchema().insertObject(wegovTask);
        
        logger.debug("Got database ID: \'" + iDfromDatabase + "\'");
        
        if (iDfromDatabase != null) {
            this.id = Integer.parseInt(iDfromDatabase);
        } else {
            throw new SQLException("Failed to assign database ID to task: \'" + name + "\'.");
        }
//        WegovTask(String name, String status, String type, String command, int workflowOrder)
    }
    
    /**
     * Get from database only
     */
    public Task(int id, Coordinator coordinator) throws SQLException {
        this.id = id;
        this.coordinator = coordinator;
        this.mgtSchema = coordinator.getMgtSchema();
        
        ArrayList<WegovTask> tasks = coordinator.getMgtSchema().getAllWhere(new WegovTask(), "ID", id);
        
        if (tasks.isEmpty())
            throw new SQLException("Failed to find task with database ID: \'" + id + "\'.");
        
        this.wegovTask = (WegovTask) tasks.get(0);
        
        this.name = wegovTask.getName();
        this.type = wegovTask.getType();
        this.status = wegovTask.getStatus();
        this.command = wegovTask.getCommand();
        this.workflowOrder = wegovTask.getWorkflowOrder();
    }
    
    private void updateWegovTaskFromDatabase() {
        try {
            wegovTask = (WegovTask) mgtSchema.getAllWhere(wegovTask, "ID", id).get(0);
        } catch (SQLException ex) {
            logger.error("Failed to update from database task's details with ID: " + id);
            logger.error(ex.toString());
        }
    }
    
    private void setValue(String name, Object value) throws SQLException {
        mgtSchema.updateRow(wegovTask, name, value, "ID", id);
    }
    
    private Object getValue(String name) throws SQLException {
        return mgtSchema.getColumnValue(wegovTask, name, "ID", id);
    }
    
    public WegovPolicymaker getPolicyMaker() {
        WegovPolicymaker result = new WegovPolicymaker();
        
        try {
            WegovPolicymaker_Task pt = (WegovPolicymaker_Task) mgtSchema.getAllWhere(new WegovPolicymaker_Task(), "TaskID", id).get(0);
            int policyMakerId = pt.getPolicymakerID();
            result = (WegovPolicymaker) mgtSchema.getAllWhere(result, "ID", policyMakerId).get(0);
        } catch (SQLException ex) {
            logger.error("Failed to get policymaker from database for task with ID: " + id);
            logger.error(ex.toString());
        }
        
        return result;
    }

    public int getId() {
        return id;
    }

    public String getName() throws SQLException {
        this.name = (String) getValue("Name");
        return name;
    }

    public void setName(String newName) throws SQLException {
        this.name = newName;
        logger.debug("Setting name to: \'" + newName + "\'");
        setValue("Name", newName);
    }
    
    public String getType() throws SQLException {
        this.type = (String) getValue("Type");
        return type;
    }

    public void setType(String newType) throws SQLException {
        this.type = newType;
        logger.debug("Setting type to: \'" + newType + "\'");
        setValue("Type", newType);
    }
    
    public String getStatus() throws SQLException {
        this.status = (String) getValue("Status");
        return status;
    }

    public void setStatus(String newStatus) throws SQLException {
        this.status = newStatus;
        logger.debug("Setting status: \'" + newStatus + "\'");
        setValue("Status", newStatus);
    }    

    public String getCommand() throws SQLException {
        this.command = (String) getValue("Command");
        return command;
    }
    
    public void setCommand(String newCommand) throws SQLException {
        this.command = newCommand;
        logger.debug("Setting command to: \'" + newCommand + "\'");
        setValue("Command", newCommand);
    }    

    public int getWorkflowOrder() throws SQLException {
        this.workflowOrder = (Integer) getValue("WorkflowOrder");
        return workflowOrder;
    }
    
    public void setWorkflowOrder(int newWorkflowOrder) throws SQLException {
        this.workflowOrder = newWorkflowOrder;
        logger.debug("Setting workflow order to: \'" + newWorkflowOrder + "\'");
        setValue("WorkflowOrder", newWorkflowOrder);
    }

    public Thread[] getThreadsToWaitFor() throws SQLException {
        ArrayList<WegovTask_Threads> taskThreads = mgtSchema.getAllWhere(new WegovTask_Threads(), "TaskID", id);
        threadsToWaitFor = new Thread[taskThreads.size()];
        
        for (int i = 0; i < taskThreads.size(); i++) {
            int threadID = taskThreads.get(i).getThreadID();
//            threadsToWaitFor[i] = threadID;
        }
        
        return threadsToWaitFor;
    }

    public void setThreadsToWaitFor(Thread[] threadsToWaitFor) throws SQLException {
        this.threadsToWaitFor = threadsToWaitFor;
        
        if (threadsToWaitFor != null) {
            for (Thread thread : threadsToWaitFor) {
                long threadId = thread.getId();
                mgtSchema.insertObject(new WegovTask_Threads(id, (int) threadId));
            }
        }        
    }
    
    

    public ScheduledFuture scheduleIn(long delay) {
        timeHandle = scheduler.schedule(this, delay, TimeUnit.MILLISECONDS);
        return timeHandle;
    }
    
    public ScheduledFuture scheduleIn(long delay, TimeUnit unit) {
        timeHandle = scheduler.schedule(this, delay, unit);
        return timeHandle;
    }
    
    public void cancel(boolean nowOrWhenFinished) {
        timeHandle.cancel(nowOrWhenFinished);
    }
    
    public boolean isDone() {
        return timeHandle.isDone();
    }
    
    public boolean isCancelled() {
        return timeHandle.isCancelled();
    }
    
    public void start() {
        scheduleIn(0);
    }
    
    public String getLogs() throws SQLException {
        String result = "";
        
        ArrayList<WegovRun_Log> logs = mgtSchema.getAllWhere(new WegovRun_Log(), "TaskID", id);
        
        for (WegovRun_Log line : logs) {
            result = result + line.getText() + "\n";
        }
        
        return result;
    }
    
    public String getErrors() throws SQLException {
        String result = "";
        
        ArrayList<WegovRun_Error> errors = mgtSchema.getAllWhere(new WegovRun_Error(), "TaskID", id);
        
        for (WegovRun_Error line : errors) {
            result = result + line.getText() + "\n";
        }
        
        return result;
    }

    public void run() {
        if (threadsToWaitFor != null) {
            try {
                this.setStatus("pending");

                for (Thread thread : threadsToWaitFor) {
                    thread.join();
                }

                try {
                    this.executeProcess();
                } catch (Exception ex) {
                    logger.error("Oops, this happened:");
                    logger.error(ex.toString());
                    this.setStatus("failed");
                }
                
            } catch (Exception ex) {
                try {
                    this.setStatus("skipping");
                } catch (Exception e) {
                    logger.error(ex.getStackTrace());
                }
                logger.error("Something went wrong with the previous thread, I will skip my turn");
                logger.error(ex.toString());
            }
        } else {
            try {
                this.executeProcess();
            } catch (Exception ex) {
                logger.error("Oops, this happened:");
                logger.error(ex.toString());
                status = "failed";
                try {
                    this.setStatus("failed");
                } catch (Exception e) {
                    logger.error(ex.toString());
                }
            }
        }
    }
    
    private void executeProcess() throws InterruptedException, SQLException, IOException {
        this.setStatus("running");
        SqlTable logs = coordinator.getTasksLogsTable();
        SqlTable errors = coordinator.getTasksErrorsTable();

        String stdOut = null;
        String errOut = null;
        logger.debug("Running command: \'" + command + "\'");
        Process p = Runtime.getRuntime().exec(command);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        while ((stdOut = stdInput.readLine()) != null) {
            logs.insertRow(new WegovRun_Log(id, stdOut));            
        }

        while ((errOut = stdError.readLine()) != null) {
            errors.insertRow(new WegovRun_Error(id, errOut));
        }        
        
        p.waitFor();
        this.setStatus("finished");
    }    
    
    @Override
    public String toString() {
        updateWegovTaskFromDatabase();
        return getPolicyMaker().toString() + "\n" + wegovTask.toString();
    }
}
