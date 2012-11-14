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
//	Created Date :			2011-12-19
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator;

import eu.wegov.coordinator.dao.data.WegovAnalysisKmiBuzzTopPost;
import eu.wegov.coordinator.dao.data.WegovAnalysisKmiBuzzTopUser;
import eu.wegov.coordinator.dao.data.WegovAnalysisKmiDa;
import eu.wegov.coordinator.dao.data.WegovAnalysisKmiDaMostActiveUser;
import eu.wegov.coordinator.sql.SqlSchema;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Maxim Bashevoy
 */
public class KmiAnalysisResultsWrapper {
    private int runId;
    private Coordinator coordinator;
    private SqlSchema dataSchema;    
    
    public KmiAnalysisResultsWrapper(int runId, Coordinator coordinator) {
        this.runId = runId;
        this.coordinator = coordinator;
        this.dataSchema = coordinator.getDataSchema();
    }
    
    public WegovAnalysisKmiDa getDiscussionActivity() throws SQLException {        
        WegovAnalysisKmiDa result = null;
        ArrayList<WegovAnalysisKmiDa> results = dataSchema.getAllWhere(new WegovAnalysisKmiDa(), "OutputOfRunID", runId);
        
        if (!results.isEmpty()) {
            result = results.get(0);
        }
        
        return result;        
    }
    
    public ArrayList<WegovAnalysisKmiDaMostActiveUser> getMostActiveUsers() throws SQLException {        
        return (ArrayList<WegovAnalysisKmiDaMostActiveUser>) dataSchema.getAllWhere(new WegovAnalysisKmiDaMostActiveUser(), "OutputOfRunID", runId);
    }
    
    public ArrayList<WegovAnalysisKmiBuzzTopUser> getTopBuzzUsers() throws SQLException {        
        return (ArrayList<WegovAnalysisKmiBuzzTopUser>) dataSchema.getAllWhere(new WegovAnalysisKmiBuzzTopUser(), "OutputOfRunID", runId);
    }    
    
    public ArrayList<WegovAnalysisKmiBuzzTopPost> getTopBuzzPosts() throws SQLException {        
        return (ArrayList<WegovAnalysisKmiBuzzTopPost>) dataSchema.getAllWhere(new WegovAnalysisKmiBuzzTopPost(), "OutputOfRunID", runId);
    }    
    
}
