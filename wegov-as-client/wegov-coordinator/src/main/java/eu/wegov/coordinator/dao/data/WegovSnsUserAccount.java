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
package eu.wegov.coordinator.dao.data;

import eu.wegov.coordinator.dao.Dao;
import eu.wegov.coordinator.utils.Triplet;
import java.sql.Timestamp;

/**
 *
 * @author Maxim Bashevoy
 */
public class WegovSnsUserAccount extends Dao {
    public static final String TABLE_NAME = "SnsUserAccount";
    
    public WegovSnsUserAccount() {
        this("", "", "", "", "", "", "", new Timestamp(System.currentTimeMillis()), "", 0, 0, 0, 0, new Timestamp(System.currentTimeMillis()), 0);
    }

    public WegovSnsUserAccount(String userID, String fullName, String location, String url, String profileUrl, String profilePictureUrl, String hostSnsID, Timestamp dateCreated, String screenName,
    		int followersCount, int listedCount, int postsCount, int followingCount, Timestamp dateCollected, int outputOfRunID) {
        super(TABLE_NAME);
        properties.add(new Triplet("count_ID", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("ID", "character varying(60) NOT NULL", userID));
        properties.add(new Triplet("FullName", "character varying(256)", fullName));
        properties.add(new Triplet("Location", "character varying(256)", location));
        properties.add(new Triplet("URL", "text", url));
        properties.add(new Triplet("ProfileURL", "text", profileUrl));
        properties.add(new Triplet("ProfilePictureURL", "text", profilePictureUrl));
        properties.add(new Triplet("HostSNS_SNS_ID", "character varying(60) NOT NULL", hostSnsID));
        properties.add(new Triplet("DateCreated", "timestamp with time zone", dateCreated));
        properties.add(new Triplet("ScreenName", "character varying(256)", screenName));
        properties.add(new Triplet("FollowersCount", "integer", followersCount));
        properties.add(new Triplet("ListedCount", "integer", listedCount));
        properties.add(new Triplet("PostsCount", "integer", postsCount));
        properties.add(new Triplet("FollowingCount", "integer", followingCount));
        properties.add(new Triplet("DateCollected", "timestamp with time zone", dateCollected));
        properties.add(new Triplet("OutputOfRunID", "integer", outputOfRunID));
    }

    @Override
    public Dao createNew() {
        return new WegovSnsUserAccount();
    }
    
    public String getCount_ID() {
        return getValueForKeyAsString("count_ID");
    }
    
    public String getID() {
        return getValueForKeyAsString("ID");
    }
    
    public String getFullName() {
        return getValueForKeyAsString("FullName");
    }    
    
    public String getLocation() {
        return getValueForKeyAsString("Location");
    }    
    
    public String getURL() {
        return getValueForKeyAsString("URL");
    }    
    
    public String getProfileURL() {
        return getValueForKeyAsString("ProfileURL");
    }    
    
    public String getProfilePictureURL() {
        return getValueForKeyAsString("ProfilePictureURL");
    }    
    
    public String getHostSNS_SNS_ID() {
        return getValueForKeyAsString("HostSNS_SNS_ID");
    }    
    
    public Timestamp getDateCreated() {
        return getValueForKeyAsTimestamp("DateCreated");
    }       
    
    public String getScreenName() {
        return getValueForKeyAsString("ScreenName");
    }    
    
    public int getFollowersCount() {
        return getValueForKeyAsInt("FollowersCount");
    }    
    
    public int getListedCount() {
        return getValueForKeyAsInt("ListedCount");
    }    
    
    public int getPostsCount() {
        return getValueForKeyAsInt("PostsCount");
    }    
    
    public int getFollowingCount() {
        return getValueForKeyAsInt("FollowingCount");
    }    
    
    public Timestamp getDateCollected() {
        return getValueForKeyAsTimestamp("DateCollected");
    }       
    
    public int getOutputOfRunID() {
        return getValueForKeyAsInt("OutputOfRunID");
    }    
}
