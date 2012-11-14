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
//	Created Date :			2011-08-02
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator;

import eu.wegov.coordinator.dao.mgt.WegovPolicymaker;
import eu.wegov.coordinator.dao.mgt.WegovPolicymaker_Workflow;
import eu.wegov.coordinator.dao.mgt.WegovWorkflow;
import eu.wegov.coordinator.dao.mgt.WegovWorkflow_Task;
import eu.wegov.coordinator.sql.SqlSchema;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 *
 * @author Maxim Bashevoy
 */
public class Workflow implements Runnable {
    private int id;
    private String name;
    private String status;
    
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> timeHandle;
    private Coordinator coordinator;
    private WegovWorkflow wegovWorkflow; // only temporary and useful object!
    
    private SqlSchema mgtSchema;
    
    private final static Logger logger = Logger.getLogger(Workflow.class.getName());
    
    /**
     * Create new workflow only
     */    
    public Workflow(String name, String status, Coordinator coordinator) throws SQLException {
        this.id = 0;
        this.name = name;
        this.status = status;
        this.coordinator = coordinator;
        this.mgtSchema = coordinator.getMgtSchema();
        
        this.wegovWorkflow = new WegovWorkflow(name, status);
        
        String iDfromDatabase = coordinator.getMgtSchema().insertObject(wegovWorkflow);
        
        logger.debug("Got database ID: \'" + iDfromDatabase + "\'");
        
        if (iDfromDatabase != null) {
            this.id = Integer.parseInt(iDfromDatabase);
        } else {
            throw new SQLException("Failed to assign database ID to workflow: \'" + name + "\'.");
        }        
    }
    
    /**
     * Get from database only
     */    
    public Workflow(int id, Coordinator coordinator) throws SQLException {
        this.id = id;
        this.coordinator = coordinator;
        this.mgtSchema = coordinator.getMgtSchema();
        
        ArrayList<WegovWorkflow> wfs = coordinator.getMgtSchema().getAllWhere(new WegovWorkflow(), "ID", id);
        
        if (wfs.isEmpty())
            throw new SQLException("Failed to find workflow with database ID: \'" + id + "\'.");
        
        this.wegovWorkflow = (WegovWorkflow) wfs.get(0);
        
        this.name = wegovWorkflow.getName();
        this.status = wegovWorkflow.getStatus();
    }    
    
    
    private void updateWegovWorkflowFromDatabase() {
        try {
            wegovWorkflow = (WegovWorkflow) mgtSchema.getAllWhere(wegovWorkflow, "ID", id).get(0);
        } catch (SQLException ex) {
            logger.error("Failed to update from database workflow's details with ID: " + id);
            logger.error(ex.toString());
        }
    }
    
    private void setValue(String name, Object value) throws SQLException {
        mgtSchema.updateRow(wegovWorkflow, name, value, "ID", id);
    }
    
    private Object getValue(String name) throws SQLException {
        return mgtSchema.getColumnValue(wegovWorkflow, name, "ID", id);
    }
    
    public WegovPolicymaker getPolicyMaker() {
        WegovPolicymaker result = new WegovPolicymaker();
        
        try {
            WegovPolicymaker_Workflow pt = (WegovPolicymaker_Workflow) mgtSchema.getAllWhere(new WegovPolicymaker_Workflow(), "WorkflowID", id).get(0);
            int policyMakerId = pt.getPolicymakerID();
            result = (WegovPolicymaker) mgtSchema.getAllWhere(result, "ID", policyMakerId).get(0);
        } catch (SQLException ex) {
            logger.error("Failed to get policymaker from database for workflow with ID: " + id);
            logger.error(ex.toString());
        }
        
        return result;
    }
    
    public void addTask(Task task, int workflowOrder) throws SQLException {
      int taskId = task.getId();
      task.setWorkflowOrder(workflowOrder);
      
      mgtSchema.insertObject(new WegovWorkflow_Task(id, taskId));
    }
    
    public ArrayList<Task> getTasks() throws SQLException {
        ArrayList<Task> allTasks = new ArrayList<Task>();
        
        ArrayList<WegovWorkflow_Task> wf_ts = mgtSchema.getAllWhere(new WegovWorkflow_Task(), "WorkflowID", id);
        
        for (WegovWorkflow_Task wf_t : wf_ts) {
            int tempTaskId = wf_t.getTaskID();
            allTasks.add(new Task(tempTaskId, coordinator));
        }
        
        return allTasks;
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
    
    public String getStatus() throws SQLException {
        this.status = (String) getValue("Status");
        return status;
    }

    public void setStatus(String newStatus) throws SQLException {
        this.status = newStatus;
        logger.debug("Setting status: \'" + newStatus + "\'");
        setValue("Status", newStatus);
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

    public void run() {
        // Get tasks
        ArrayList<Task> allTasks = new ArrayList<Task>();
        
        try {
            allTasks = getTasks();
        } catch (SQLException ex) {
            logger.error("Failed to load tasks for workflow: " + id);
            logger.error(ex.toString());
        }
        
        if (!allTasks.isEmpty()) {
            try {
                if (validate(allTasks)) {
                    LinkedHashMap<Task, Thread> tasksAndThreadsMap = new LinkedHashMap<Task, Thread>();
                    int maxOrder = getMaxTaskOrder(allTasks);
                    
                    for (int i = 0; i <= maxOrder; i++) {
                        ArrayList<Task> levelItasks = getTasksWithWorklowOrder(allTasks, i);
                        String line = "| ";
                        
                        for (int k = 0; k < levelItasks.size(); k++) {
                            Task task = levelItasks.get(k);
                            line = line + task.getName() + " | ";
                            
                            if (i > 0) {
                                // Not first level in the workflow
                                ArrayList<Task> levelIminusOnetasks = getTasksWithWorklowOrder(allTasks, i - 1);
                                int size = levelIminusOnetasks.size();
                                Thread[] threadsToWaitFor = new Thread[size];
                                
                                for (int j = 0; j < size; j++) {
                                    Task taskMinusOne = levelIminusOnetasks.get(j);
                                    Thread threadMinusOne = tasksAndThreadsMap.get(taskMinusOne);

                                    threadsToWaitFor[j] = threadMinusOne;
                                }

                                task.setThreadsToWaitFor(threadsToWaitFor);
                                Thread thread = new Thread(task);
                                tasksAndThreadsMap.put(task, thread);                                
                                
                            } else {
                                // First level in the workflow - nothing to wait for
                                Thread thread = new Thread(task);
                                tasksAndThreadsMap.put(task, thread);                                
                            }
                        }
                        
                        logger.debug(Integer.toString(i) + ": " + line);
                    }
                    
                    
                } else {
                    logger.error("Level without a task in workflow: " + id);
                }
                
            } catch (SQLException ex) {
                logger.error(ex.toString());
            }            
        } else {
            logger.error("No tasks to run in workflow: " + id);
        }
    }
    
    /*
     * Useful
     */
    
    private int getMaxTaskOrder(ArrayList<Task> allTasks) throws SQLException {
        int maxOrder = 0;

        for (int i = 0; i < allTasks.size(); i++) {
            int currentLevel = allTasks.get(i).getWorkflowOrder();
            if (maxOrder < currentLevel)
                maxOrder = currentLevel;
        }

        return maxOrder;
    }
    
    private boolean validate(ArrayList<Task> allTasks) throws SQLException {
        boolean valid = true;
        int maxOrder = getMaxTaskOrder(allTasks);

        for (int i = 0; i <= maxOrder; i++) {
            if (this.getTasksWithWorklowOrder(allTasks, i).size() < 1) {
                valid = false;
            } 
        }

        return valid;
    }

    private ArrayList<Task> getTasksWithWorklowOrder(ArrayList<Task> allTasks, int workflowOrder) throws SQLException {

        ArrayList<Task> group = new ArrayList<Task>();

        for (int i = 0; i < allTasks.size(); i++) {
            Task task = allTasks.get(i);
            if (task.getWorkflowOrder() == workflowOrder)
                group.add(task);
        }

        return group;
    }    
    
    @Override
    public String toString() {
        updateWegovWorkflowFromDatabase();
        return  wegovWorkflow.toString() + "\n" + getPolicyMaker().toString() + "\n";
    }
}
