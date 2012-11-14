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
public class WegovPostItem extends Dao {
    public static final String TABLE_NAME = "PostItem";
    
    public WegovPostItem() {
        this("", "", "", new Timestamp(System.currentTimeMillis()), "", "", "",(short) 0, "", "", 0, new Timestamp(System.currentTimeMillis()), 0);
    }

    public WegovPostItem(String postID, String userID, String hostSnsID, Timestamp dateCreated, String title, String content, String originalPostURL, short isRetweet, String toUserID, String replyToPostID, int likes, Timestamp dateCollected, int outputOfRunID) {
        super(TABLE_NAME);
        properties.add(new Triplet("count_ID", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("ID", "character varying(60) NOT NULL", postID));
        properties.add(new Triplet("Author_SnsUserAccount_ID", "character varying(60) NOT NULL", userID));
        properties.add(new Triplet("HostSNS_SNS_ID", "character varying(60) NOT NULL", hostSnsID));
        properties.add(new Triplet("DateCreated", "timestamp with time zone", dateCreated));
        properties.add(new Triplet("Title", "text", title));
        properties.add(new Triplet("Content", "text", content));
        properties.add(new Triplet("OriginalPostURL", "text", originalPostURL));
        properties.add(new Triplet("IsRetweet", "smallint", isRetweet));
        properties.add(new Triplet("ToUserID", "character varying(60)", toUserID));
        properties.add(new Triplet("ReplyToPostID", "character varying(60)", replyToPostID));
        properties.add(new Triplet("Likes", "integer", likes));
        properties.add(new Triplet("DateCollected", "timestamp with time zone", dateCollected));
        properties.add(new Triplet("OutputOfRunID", "integer", outputOfRunID));
    }

    @Override
    public Dao createNew() {
        return new WegovPostItem();
    }
    
    public String getID() {
        return getValueForKeyAsString("ID");
    }    
    
    public String getCount_ID() {
        return getValueForKeyAsString("count_ID");
    }    
    
    public String getAuthor_SnsUserAccount_ID() {
        return getValueForKeyAsString("Author_SnsUserAccount_ID");
    }    
    
    public String getHostSNS_SNS_ID() {
        return getValueForKeyAsString("HostSNS_SNS_ID");
    }    
    
    public Timestamp getDateCreated() {
        return getValueForKeyAsTimestamp("DateCreated");
    }    
    
    public String getTitle() {
        return getValueForKeyAsString("Title");
    }    
    
    public String getContent() {
        return getValueForKeyAsString("Content");
    }    
    
    public String getOriginalPostURL() {
        return getValueForKeyAsString("OriginalPostURL");
    }    
    
    public short isRetweet() {
        return (short) getValueForKeyAsInt("IsRetweet");
    }    
    
    public String getToUserID() {
        return getValueForKeyAsString("ToUserID");
    }    
    
    public String getReplyToPostID() {
        return getValueForKeyAsString("ReplyToPostID");
    }    

    public int getLikes() {
        return getValueForKeyAsInt("Likes");
    }

    public Timestamp getDateCollected() {
        return getValueForKeyAsTimestamp("DateCollected");
    }       
    
    public int getOutputOfRunID() {
        return getValueForKeyAsInt("OutputOfRunID");
    }
    
    public void setOutputOfRunID(int newRunID) {
        updateProperty("OutputOfRunID", newRunID);
    }
}
