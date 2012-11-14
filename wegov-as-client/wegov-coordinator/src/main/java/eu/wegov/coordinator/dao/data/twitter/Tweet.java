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
package eu.wegov.coordinator.dao.data.twitter;

import eu.wegov.coordinator.dao.Dao;
import eu.wegov.coordinator.utils.Triplet;
import java.sql.Timestamp;

/**
 *
 * @author Maxim Bashevoy
 */
public class Tweet extends Dao {
    public static final String TABLE_NAME = "Tweets";
    
    public Tweet() {
        this("", "", "", "", new Timestamp(System.currentTimeMillis()), "", "", false, false, "", "", "", "", "", "", 0, new Timestamp(System.currentTimeMillis()), 0);
    }

    public Tweet(String id,
            String by_user_id,
            String text,
            String coordinates,
            Timestamp created_at,
            String place,
            String source,
            boolean truncated,
            boolean favourited,
            String geo,
            String in_reply_to_screen_name,
            String in_reply_to_status_id,
            String in_reply_to_user_id,
            String contributors,
            String retweeted,
            int retweet_count,
            Timestamp collected_at,
            int outputOfTaskID) {
        super(TABLE_NAME);
        properties.add(new Triplet("count_ID", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("id", "character varying(60) NOT NULL", id));
        properties.add(new Triplet("By_user_id", "character varying(60) NOT NULL", by_user_id));
        properties.add(new Triplet("Text", "text", text));
        properties.add(new Triplet("Coordinates", "character varying(60)", coordinates));
        properties.add(new Triplet("Created_at", "timestamp with time zone", created_at));
        properties.add(new Triplet("Place", "character varying(256)", place));
        properties.add(new Triplet("Source", "character varying(60)", source));
        properties.add(new Triplet("Truncated", "boolean", truncated));
        properties.add(new Triplet("Favourited", "boolean", favourited));
        properties.add(new Triplet("Geo", "character varying(256)", geo));
        properties.add(new Triplet("In_reply_to_screen_name", "character varying(256)", in_reply_to_screen_name));
        properties.add(new Triplet("In_reply_to_status_id", "character varying(60)", in_reply_to_status_id));
        properties.add(new Triplet("In_reply_to_user_id", "character varying(60)", in_reply_to_user_id));
        properties.add(new Triplet("Contributors", "character varying(256)", contributors));
        properties.add(new Triplet("Retweeted", "character varying(60)", retweeted));
        properties.add(new Triplet("Retweet_count", "character varying(60)", retweet_count));
        properties.add(new Triplet("Collected_at", "timestamp with time zone", collected_at));
//        properties.add(new Triplet("OutputOfTaskID", "integer", outputOfTaskID));
    }

    @Override
    public Dao createNew() {
        return new Tweet();
    }
    
    public String getID() {
        return getValueForKeyAsString("id");
    }
    
    public String getUserID() {
        return getValueForKeyAsString("By_user_id");
    }
    
    public String getInReplyToUserID() {
        return getValueForKeyAsString("In_reply_to_user_id");
    }
    
    public String getRetweeted() {
        return getValueForKeyAsString("Retweeted");
    }
    
    public String getRetweetCount() {
        return getValueForKeyAsString("Retweet_count");
    }
    
    public String getTimeCollected() {
        return getValueForKeyAsString("Collected_at");
    }
    
    public String getTimeCreated() {
        return getValueForKeyAsString("Created_at");
    }
    
    public Timestamp getTimeCreatedAsTimestamp() {
        return getValueForKeyAsTimestamp("Created_at");
    }
    
    public String getText() {
        return getValueForKeyAsString("Text");
    }
}
