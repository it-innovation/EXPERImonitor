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
//	Created Date :			2011-07-28
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
public class WegovTask_Inputs extends Dao {
    public static final String TABLE_NAME = "Tasks_Inputs";
    
    public WegovTask_Inputs() {
        this(1, 0, 1, "", "", "", "", "", "");
    }

    // outputOfTaskID = 0 should be default (no task) I guess
    public WegovTask_Inputs(int taskID, int inputID, int outputOfTaskID, String databaseName, String schemaName, String tableName, String entryStartID, String entryEndID, String datatype) {
        super(TABLE_NAME);
        properties.add(new Triplet("TaskID", "integer", taskID));
        properties.add(new Triplet("InputID", "integer", inputID));
        properties.add(new Triplet("OutputOfTaskID", "integer", outputOfTaskID));
        properties.add(new Triplet("DatabaseName", "character varying(60)", databaseName));
        properties.add(new Triplet("SchemaName", "character varying(60)", schemaName));
        properties.add(new Triplet("TableName", "character varying(60)", tableName));
        properties.add(new Triplet("EntryStartID", "integer", entryStartID));
        properties.add(new Triplet("EntryEndID", "character varying(60)", entryEndID));
        properties.add(new Triplet("DataType", "character varying(60)", datatype));
    }

    @Override
    public Dao createNew() {
        return new WegovTask_Inputs();
    }
}
