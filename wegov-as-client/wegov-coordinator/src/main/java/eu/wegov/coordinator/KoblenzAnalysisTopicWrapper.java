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
//	Created Date :			2011-10-11
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator;

import eu.wegov.coordinator.dao.data.WegovAnalysisKoblenzMessage;
import eu.wegov.coordinator.dao.data.WegovAnalysisKoblenzTopic;
import eu.wegov.coordinator.dao.data.WegovAnalysisKoblenzUser;
import eu.wegov.coordinator.dao.data.WegovAnalysisKoblenzViewpoint;
import eu.wegov.coordinator.sql.SqlSchema;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Maxim Bashevoy
 */
public class KoblenzAnalysisTopicWrapper {
    private int id;
    private int runId;
    private Coordinator coordinator;
    private SqlSchema dataSchema;
    private HashMap<String, Object> idsMap = new HashMap<String, Object>();
    
    public KoblenzAnalysisTopicWrapper(int id, int runId, Coordinator coordinator) {
        this.id = id;
        this.runId = runId;
        this.coordinator = coordinator;
        this.dataSchema = coordinator.getDataSchema();
        idsMap.put("TopicID", id);
        idsMap.put("OutputOfRunID", runId);
    }
    
    public WegovAnalysisKoblenzTopic getTopic() throws SQLException {
        WegovAnalysisKoblenzTopic result = null;
        ArrayList<WegovAnalysisKoblenzTopic> results = dataSchema.getAllWhere(new WegovAnalysisKoblenzTopic(), idsMap);
        
        if (!results.isEmpty()) {
            result = results.get(0);
        }
        
        return result;
    }
    
    public ArrayList<WegovAnalysisKoblenzMessage> getMessages() throws SQLException {        
        return (ArrayList<WegovAnalysisKoblenzMessage>) dataSchema.getAllWhere(new WegovAnalysisKoblenzMessage(), idsMap);
    }
    
    public ArrayList<WegovAnalysisKoblenzViewpoint> getViewPoints() throws SQLException {        
        return (ArrayList<WegovAnalysisKoblenzViewpoint>) dataSchema.getAllWhere(new WegovAnalysisKoblenzViewpoint(), idsMap);
    }
    
    public ArrayList<WegovAnalysisKoblenzUser> getUsers() throws SQLException {        
        return (ArrayList<WegovAnalysisKoblenzUser>) dataSchema.getAllWhere(new WegovAnalysisKoblenzUser(), idsMap);
    }
    
}
