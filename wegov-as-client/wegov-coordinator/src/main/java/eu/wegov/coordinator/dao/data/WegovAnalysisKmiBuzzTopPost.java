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
package eu.wegov.coordinator.dao.data;

import eu.wegov.coordinator.dao.Dao;
import eu.wegov.coordinator.utils.Triplet;

/**
 *
 * @author Maxim Bashevoy
 */
public class WegovAnalysisKmiBuzzTopPost extends Dao {
    public static final String TABLE_NAME = "AnalysisKmiBuzzTopPost";
    
    public WegovAnalysisKmiBuzzTopPost() {
        this(0, "", "", "", "", "", "", 0);
    }

    public WegovAnalysisKmiBuzzTopPost(int messageID, String originalPostID, String contents, String originalPostURL, String userID, String hostSnsID, String score, int outputOfRunID) {
        super(TABLE_NAME);
        properties.add(new Triplet("count_ID", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("MessageID", "integer NOT NULL", messageID));
        properties.add(new Triplet("OriginalPostID", "character varying(60)", originalPostID));
        properties.add(new Triplet("Contents", "text", contents));
        properties.add(new Triplet("OriginalPostURL", "text", originalPostURL));        
        properties.add(new Triplet("Author_SnsUserAccount_ID", "character varying(60) NOT NULL", userID));        
        properties.add(new Triplet("HostSNS_SNS_ID", "character varying(60) NOT NULL", hostSnsID));        
        properties.add(new Triplet("Score", "character varying(60) NOT NULL", score));        
        properties.add(new Triplet("OutputOfRunID", "integer NOT NULL", outputOfRunID));
    }

    @Override
    public Dao createNew() {
        return new WegovAnalysisKmiBuzzTopPost();
    }
    
    public String getCount_ID() {
        return getValueForKeyAsString("count_ID");
    }
    
    public int getMessageID() {
        return getValueForKeyAsInt("MessageID");
    }          
    
    public String getOriginalPostID() {
        return getValueForKeyAsString("OriginalPostID");
    }           
    
    public String getContents() {
        return getValueForKeyAsString("Contents");
    }           
    
    public String getOriginalPostURL() {
        return getValueForKeyAsString("OriginalPostURL");
    }           
    
    public String getUserID() {
        return getValueForKeyAsString("Author_SnsUserAccount_ID");
    }           
    
    public String getHostSnsID() {
        return getValueForKeyAsString("HostSNS_SNS_ID");
    }           
    
    public String getScore() {
        return getValueForKeyAsString("Score");
    }               
    
    public int getOutputOfRunID() {
        return getValueForKeyAsInt("OutputOfRunID");
    }    
}
