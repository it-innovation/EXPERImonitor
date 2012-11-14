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
//	Created Date :			2011-07-29
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator.utils;

/**
 *
 * @author Maxim Bashevoy
 */
import eu.wegov.coordinator.Coordinator;
import eu.wegov.coordinator.dao.data.twitter.FullTweet;
import eu.wegov.coordinator.dao.data.twitter.Hashtag;
import eu.wegov.coordinator.dao.data.twitter.Tweet;
import eu.wegov.coordinator.dao.data.twitter.Url;
import eu.wegov.coordinator.dao.data.twitter.User;
import eu.wegov.coordinator.dao.data.twitter.UserMention;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.log4j.Logger;

public class RunLiveTwitterCollection {
    
    private Coordinator coordinator;
    private String keywordToTrack;
    private final static int TASK_ID = 0;
    private Util util = new Util();
    
    private final static Logger logger = Logger.getLogger(GetTwitterPostDetails.class.getName());

    public RunLiveTwitterCollection(String keywordToTrack, Coordinator coordinator) {

        logger.info("Get Twitter Post Details task initializing with post ID: " + keywordToTrack);

        this.keywordToTrack = keywordToTrack;
        this.coordinator = coordinator;

    }

    public void execute(int stopAtCount, String userName, String password) throws Exception {

//        GetTwitterPostDetails postGetter = new GetTwitterPostDetails(coordinator);
        
        logger.info("Request sent");

        int currentCount = 0;
        int retryCount = 0;
        
        do {
            
            logger.info("Connection attempt: " + retryCount);
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, "utf-8");

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

            ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params,
                    registry);
            DefaultHttpClient client = new DefaultHttpClient(manager, params);

            client.getCredentialsProvider().setCredentials(new AuthScope("stream.twitter.com", 80),
                    new UsernamePasswordCredentials(userName, password));

            HttpGet get = new HttpGet("http://stream.twitter.com/1/statuses/filter.json?track=" + keywordToTrack);            
            HttpResponse resp = client.execute(get);
            currentCount = runStream(resp, currentCount, stopAtCount);
            retryCount++;
            
            if (retryCount > 3)
                break;
            
        } while (currentCount < stopAtCount);

        logger.info("Finished with current count: " + currentCount);
        logger.info("Finished with retry count: " + retryCount);
        
    }
    
    private int runStream(HttpResponse resp, int count, int stopAtCount) throws Exception {

        String line;
        int statusCode = resp.getStatusLine().getStatusCode();
        String statusPhrase = resp.getStatusLine().getReasonPhrase();
        
        logger.info("Starting with count: " + count);

        logger.debug("Response code: " + statusCode);
        logger.debug("Response phrase: " + statusPhrase);
        
        if (statusCode == 200) {
            InputStream stream = resp.getEntity().getContent();

            logger.debug("Got stream");

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

            while (((line = reader.readLine()) != null) & (count < stopAtCount)) {
//                logger.debug("Response: " + line);
                logger.info("------------------------ #" + count + " ---------------------------------------");
                logger.info("Response: " + line);

                if (line.length() > 2) {
                    Timestamp timeCollected = util.getTimeNowAsTimestamp();

                    JSONObject root = JSONObject.fromObject(line);
                    FullTweet fullTweet = util.getFullTweet(root, timeCollected, TASK_ID);
                    
                    Tweet tweet = fullTweet.getTweet();
                    User user = fullTweet.getUser();
                    
                    ArrayList<UserMention> mentions = fullTweet.getUserMentions();
//                    ArrayList<Url> urls = fullTweet.getUrls();
//                    ArrayList<Hashtag> hashtags = fullTweet.getHashtags();
                    
                    String inReplyToUserID = tweet.getInReplyToUserID();
                    
                    for (UserMention mention : mentions) {
                        if (mention.getID().equals(inReplyToUserID)) {
                            mention.updateProperty("Comment", "Reply");
                        } else {
                            mention.updateProperty("Comment", "Mention");
                        }
                    }                    
                    
                    // Is retweet?
                    if (root.containsKey("retweeted_status")) {
                        root = JSONObject.fromObject(root.get("retweeted_status"));
                        logger.info("Retweet: " + root.toString());
                        
                        FullTweet fullReTweet = util.getFullTweet(root, timeCollected, TASK_ID);
                        
                        Tweet re_tweet = fullReTweet.getTweet();
                        String re_tweetID = re_tweet.getID();
                        
                        // Send query and find out if it's a retweet as well?
//                        root = postGetter.getRoot(re_tweetID);
//                        if (root.containsKey("retweeted_status")) {
//                            root = JSONObject.fromObject(root.get("retweeted_status"));
//                            logger.info("Re-Retweet: " + root.toString());
//                            FullTweet rerefullTweet = util.getFullTweet(root, timeCollected, TASK_ID);
//                            re_tweet.updateProperty("Retweeted", rerefullTweet.getTweet().getID());
//                        } else {
//                            logger.info("Not a re-retweet: " + root.toString());
//                        }
                        
                        User re_user = fullReTweet.getUser();
                        String re_userID = re_user.getID();

                        ArrayList<UserMention> re_mentions = fullReTweet.getUserMentions();
//                        ArrayList<Url> re_urls = fullReTweet.getUrls();
//                        ArrayList<Hashtag> re_hashtags = fullReTweet.getHashtags(); 
                        
                        tweet.updateProperty("Retweeted", re_tweetID);
                        
                        inReplyToUserID = re_tweet.getInReplyToUserID();
                        
                        for (UserMention re_mention : re_mentions) {
                            if (re_mention.getID().equals(inReplyToUserID)) {
                                re_mention.updateProperty("Comment", "re-Reply");
                            } else {
                                re_mention.updateProperty("Comment", "re-Mention");
                            }
                        }                        
                        
                        // Add retweet comment to the mention of the tweet
                        for (UserMention mention : mentions) {
//                            logger.info("mentioned: " + mention.getID() + " retweet author: " + re_userID);
                            if (mention.getID().equals(re_userID)) {
                                mention.updateProperty("Comment", "Retweet");
                            }
                        }
                        
                        coordinator.getDataSchema().insertFullTweet(fullReTweet);
                    }
                    
                    coordinator.getDataSchema().insertFullTweet(fullTweet);
                    
                    count++;

                } else {
                    logger.debug("Got empty response because of timeout");
                }
            }
            logger.info("Finished at count: " + count);
        } else {
            logger.error("Response code: " + statusCode);
            logger.error("Response phrase: " + statusPhrase);
        }
        
        return count;
    }
}
