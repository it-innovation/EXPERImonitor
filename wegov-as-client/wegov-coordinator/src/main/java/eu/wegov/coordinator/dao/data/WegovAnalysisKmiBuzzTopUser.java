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
public class WegovAnalysisKmiBuzzTopUser extends Dao {
    public static final String TABLE_NAME = "AnalysisKmiBuzzTopUser";
    
    public WegovAnalysisKmiBuzzTopUser() {
        this(0, "", "", "0.0", "", "", "", "", 0);
    }

    public WegovAnalysisKmiBuzzTopUser(int analysisUserID, String screenName, String fullName, String score,
            String profilePictureUrl, String moreLinkUrl, String followLinkUrl, String hostSNS_SNS_ID, int outputOfRunID) {
        super(TABLE_NAME);
        properties.add(new Triplet("count_ID", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("AnalysisUserID", "integer NOT NULL", analysisUserID));
        properties.add(new Triplet("ScreenName", "character varying(256)", screenName));
        properties.add(new Triplet("FullName", "character varying(256)", fullName));
        properties.add(new Triplet("Score", "character varying(256)", score));
        properties.add(new Triplet("ProfilePictureURL", "text", profilePictureUrl));        
        properties.add(new Triplet("MoreLinkURL", "text", moreLinkUrl));        
        properties.add(new Triplet("FollowLinkURL", "text", followLinkUrl));        
        properties.add(new Triplet("HostSNS_SNS_ID", "text", hostSNS_SNS_ID));        
        properties.add(new Triplet("OutputOfRunID", "integer NOT NULL", outputOfRunID));
    }

    @Override
    public Dao createNew() {
        return new WegovAnalysisKmiBuzzTopUser();
    }
    
    public String getCount_ID() {
        return getValueForKeyAsString("count_ID");
    }
    
    public int getAnalysisUserID() {
        return getValueForKeyAsInt("AnalysisUserID");
    }     
    
    public String getScreenName() {
        return getValueForKeyAsString("ScreenName");
    }           
    
    public String getFullName() {
        return getValueForKeyAsString("FullName");
    }           
    
    public String getScore() {
        return getValueForKeyAsString("Score");
    }    
    
    public String getProfilePictureURL() {
        return getValueForKeyAsString("ProfilePictureURL");
    }           
    
    public String getMoreLinkURL() {
        return getValueForKeyAsString("MoreLinkURL");
    }           
    
    public String getFollowLinkURL() {
        return getValueForKeyAsString("FollowLinkURL");
    }              
    
    public String getHostSNS_SNS_ID() {
        return getValueForKeyAsString("HostSNS_SNS_ID");
    }              
    
    public int getOutputOfRunID() {
        return getValueForKeyAsInt("OutputOfRunID");
    }    
}
