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
//	Created Date :			2011-07-27
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator.dao.mgt;

import eu.wegov.coordinator.dao.Dao;
import eu.wegov.coordinator.utils.Triplet;

/**
 *
 * @author Maxim Bashevoy
 */
public class WegovWorkflow_Task extends Dao {
    public static final String TABLE_NAME = "Workflows_Tasks";
    
    public WegovWorkflow_Task() {
        this(1, 1);
    }

    public WegovWorkflow_Task(int workflowID, int taskID) {
        super(TABLE_NAME);
        properties.add(new Triplet("WorkflowID", "integer", workflowID));
        properties.add(new Triplet("TaskID", "integer", taskID));
    }

    @Override
    public Dao createNew() {
        return new WegovWorkflow_Task();
    }
    
    
    @Override
    public String returning() {
        return "WorkflowID";
    }
    
    public int getWorkflowID() {
        return getValueForKeyAsInt("WorkflowID");
    }
    
    public int getTaskID() {
        return getValueForKeyAsInt("TaskID");
    }    
}
