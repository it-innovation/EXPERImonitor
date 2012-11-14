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
public class UserMention extends Dao {
    public static final String TABLE_NAME = "User_mentions";
    
    public UserMention() {
        this("", "", "", "", "", "", new Timestamp(System.currentTimeMillis()), 0);
    }

    public UserMention(String user_id, 
            String mentioned_by_user_id,
            String mentioned_in_tweet_id,
            String screen_name,
            String name,
            String comment,
            Timestamp collected_at,
            int outputOfTaskID) {
        super(TABLE_NAME);
        properties.add(new Triplet("count_ID", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("ID", "character varying(60) NOT NULL", user_id));
        properties.add(new Triplet("Mentioned_by_user_id", "character varying(60) NOT NULL", mentioned_by_user_id));
        properties.add(new Triplet("Mentioned_in_tweet_id", "character varying(60) NOT NULL", mentioned_in_tweet_id));
        properties.add(new Triplet("Screen_name", "character varying(256)", screen_name));
        properties.add(new Triplet("Name", "character varying(256)", name));
        properties.add(new Triplet("Comment", "character varying(256)", comment));

        properties.add(new Triplet("Collected_at", "timestamp with time zone", collected_at));
        properties.add(new Triplet("OutputOfTaskID", "integer", outputOfTaskID));
    }

    @Override
    public Dao createNew() {
        return new UserMention();
    }
    
    public String getID() {
        return getValueForKeyAsString("ID");
    }    
}
