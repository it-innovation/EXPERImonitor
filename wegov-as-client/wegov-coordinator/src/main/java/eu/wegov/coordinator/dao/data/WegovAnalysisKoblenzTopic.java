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
package eu.wegov.coordinator.dao.data;

import eu.wegov.coordinator.dao.Dao;
import eu.wegov.coordinator.utils.Triplet;

/**
 *
 * @author Maxim Bashevoy
 */
public class WegovAnalysisKoblenzTopic extends Dao {
    public static final String TABLE_NAME = "AnalysisKoblenzTopic";
    
    public WegovAnalysisKoblenzTopic() {
        this(0, "", 0, 0, 0);
    }

    public WegovAnalysisKoblenzTopic(int topicID, String terms, int numMessages, int numUsers, int outputOfRunID) {
        super(TABLE_NAME);
        properties.add(new Triplet("count_ID", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("TopicID", "integer NOT NULL", topicID));
        properties.add(new Triplet("KeyTerms", "text", terms));
        properties.add(new Triplet("NumMessages", "integer", numMessages));
        properties.add(new Triplet("NumUsers", "integer", numUsers));
        properties.add(new Triplet("OutputOfRunID", "integer NOT NULL", outputOfRunID));
    }

    @Override
    public Dao createNew() {
        return new WegovAnalysisKoblenzTopic();
    }
    
    public String getCount_ID() {
        return getValueForKeyAsString("count_ID");
    }
    
    public int getTopicID() {
        return getValueForKeyAsInt("TopicID");
    }    
    
    public String getKeyTerms() {
        return getValueForKeyAsString("KeyTerms");
    }           
        
    public int getNumMessages() {
        return getValueForKeyAsInt("NumMessages");
    }
    
    public int getNumUsers() {
        return getValueForKeyAsInt("NumUsers");
    }    
    
    public int getOutputOfRunID() {
        return getValueForKeyAsInt("OutputOfRunID");
    }    
}
