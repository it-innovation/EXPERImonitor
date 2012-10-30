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
//	Created Date :			2011-08-05
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator.utils;

import eu.wegov.coordinator.Coordinator;
import eu.wegov.coordinator.dao.data.twitter.Tweet;
import eu.wegov.coordinator.dao.data.twitter.User;
import eu.wegov.coordinator.sql.SqlSchema;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.apache.log4j.Logger;

/**
 *
 * @author Maxim Bashevoy
 */
public class GetTopRetweeted {
    private Coordinator coordinator;
    private final static int TASK_ID = 0;
    private Util util = new Util();
    
    private final static Logger logger = Logger.getLogger(GetTopRetweeted.class.getName());

    public GetTopRetweeted(Coordinator coordinator) {

        logger.debug("Get Top Retweeted task initializing");

        this.coordinator = coordinator;

    }
    
    public LinkedHashMap<String, Integer> execute(String schemaName) throws SQLException {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
        
        SqlSchema schema = coordinator.getSchema(schemaName);
        
        logger.debug("Searching schema: " + schema.getName() + " of database: " + schema.getParent().getName());
        
        ArrayList<Tweet> tweets = schema.getAllWhereNot(new Tweet(), "Retweeted", "false");
        logger.debug("Processing " + tweets.size() + " results");
        
        for (Tweet tweet : tweets) {
            String retweeted = tweet.getRetweeted();
            Tweet retweet = (Tweet) schema.getAllWhere(tweet, "ID", retweeted).get(0);
            String userID = retweet.getUserID();
            User user = (User) schema.getAllWhere(new User(), "ID", userID).get(0);
            logger.debug("Retweeted: " + user.getName() + " in tweet: " + tweet.getID());

            if (map.containsKey(userID)) {
                int tempCount = map.get(userID);
                map.remove(userID);
                map.put(userID, tempCount + 1);
            } else {
                map.put(userID, 1);
            }
        }
        
        return map;
        
    }
}
