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
public class Hashtag extends Dao {
    public static final String TABLE_NAME = "Hashtags";
    
    public Hashtag() {
        this("", "", "", new Timestamp(System.currentTimeMillis()), 0);
    }

    public Hashtag(String by_user_id,
            String in_tweet_id,
            String text,
            Timestamp collected_at,
            int outputOfTaskID) {
        super(TABLE_NAME);
        properties.add(new Triplet("count_ID", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("By_user_id", "character varying(60) NOT NULL", by_user_id));
        properties.add(new Triplet("In_tweet_id", "character varying(60) NOT NULL", in_tweet_id));
        properties.add(new Triplet("Text", "text", text));

        properties.add(new Triplet("Collected_at", "timestamp with time zone", collected_at));
//        properties.add(new Triplet("OutputOfTaskID", "integer", outputOfTaskID));
    }

    @Override
    public Dao createNew() {
        return new Hashtag();
    }
    
    public String getBy_user_id() {
        return getValueForKeyAsString("By_user_id");
    }    
    
    public String getIn_tweet_id() {
        return getValueForKeyAsString("In_tweet_id");
    }    
    
    public String getText() {
        return getValueForKeyAsString("Text");
    }    
}
