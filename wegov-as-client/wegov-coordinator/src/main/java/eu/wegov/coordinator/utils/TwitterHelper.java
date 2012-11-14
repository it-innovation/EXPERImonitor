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
//	Created Date :			2011-08-17
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator.utils;

import eu.wegov.coordinator.Coordinator;
import eu.wegov.coordinator.dao.data.twitter.FullTweet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.log4j.Logger;

/**
 *
 * @author Maxim Bashevoy
 */
public class TwitterHelper {
    private Coordinator coordinator;
    private final static int TASK_ID = 0;
    private Util util = new Util();
    private String oauthConsumerKey = "";
    private String oauthConsumerSecret = "";
    private String oauthConsumerAccessToken = "";
    private String oauthConsumerAccessTokenSecret = "";
    
    private final static Logger logger = Logger.getLogger(TwitterHelper.class.getName());
    
    public TwitterHelper(Coordinator coordinator) {
        this.coordinator = coordinator;
        this.oauthConsumerKey = "bPE4bvcVbWgyr7h2VK3sw";
        this.oauthConsumerSecret = "LDM87QZ2gPREUQrxQSnznPMzfqxcJe66WD9BbhuX3E";
        this.oauthConsumerAccessToken = "20392673-3MWLamdbcj19BU8jeBbyBNuuuMQG8j8pJkoZdUIN8";
        this.oauthConsumerAccessTokenSecret = "UynHwahnyRkXOxbbVWrqKy55P2qE88RUgz3OtNPx4";
    }
    
    
    public HttpURLConnection getResponse(String urlAsString) throws MalformedURLException, IOException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
        URL url = new URL(urlAsString);
        logger.debug("Running query: " + urlAsString);
        OAuthConsumer consumer = new DefaultOAuthConsumer(oauthConsumerKey, oauthConsumerSecret);

//        OAuthProvider provider = new DefaultOAuthProvider(
//                "https://api.twitter.com/oauth/request_token",
//                "https://api.twitter.com/oauth/access_token",
//                "https://api.twitter.com/oauth/authorize");

        consumer.setTokenWithSecret(oauthConsumerAccessToken, oauthConsumerAccessTokenSecret);
        
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        
        consumer.sign(request);
        
        request.connect();
        
        return request;
    }    
    
    public int getRemainingHits() throws Exception {
        int result = -99;
        String theQuery = "http://api.twitter.com/1/account/rate_limit_status.json";
        
        logger.debug("Requesting remaining hits on Twitter with query: " + theQuery);
        
        HttpURLConnection response = getResponse(theQuery);
        
        int statusCode = response.getResponseCode();
        String line;
        
        if (statusCode == 200) {
            InputStream stream = response.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

            while ((line = reader.readLine()) != null) {
                logger.debug(line);
                if (line.length() > 2) {
                   JSONObject root = JSONObject.fromObject(line);
                   
                   result = Integer.parseInt(root.getString("remaining_hits"));

                } else {
                    logger.debug("Got empty response because of timeout");
                }
            }            
        } else {
            logger.error("Connection error with status code: " + statusCode);
        }   
        
        response.disconnect();
        
        return result;
    }
    
    public ArrayList<String> getTweetsPublishedByInTimeInterval(String twitterName, String startDate, String endDate) throws Exception {
        ArrayList<String> result = new ArrayList<String>();
        
        String theQuery = "http://search.twitter.com/search.json?q=from%3A" + twitterName + "%20since%3A" + startDate + "%20until%3A" + endDate + "&rpp=99";
        
        logger.debug("Getting tweet IDs using query: " + theQuery);
        
        HttpURLConnection response = getResponse(theQuery);
        
        int statusCode = response.getResponseCode();
        String line;
        
        if (statusCode == 200) {
            InputStream stream = response.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

            while ((line = reader.readLine()) != null) {
                logger.debug("Response: " + line);
                if (line.length() > 2) {
                    JSONObject root = JSONObject.fromObject(line);
                   
                    JSONArray results = root.getJSONArray("results");
                    
                    for (int i = 0; i < results.size(); i++) {
                        JSONObject tweet = results.getJSONObject(i);
                        String tweetId = tweet.getString("id");
                        String tweetText = tweet.getString("text");
                        String userName = tweet.getString("from_user");

                        logger.debug("------------------ #" + i + " ------------------");
                        logger.debug(tweetId + " by: " + userName  + ", contents: " + tweetText);

                        result.add(tweetId);
                    }                    

                } else {
                    logger.debug("Got empty response because of timeout");
                }
            }            
        } else {
            logger.error("Connection error with status code: " + statusCode);
        }   
        
        response.disconnect();        
        
        return result;
    }
    
    public FullTweet getFullTweet(String idToQuery) throws Exception {
        FullTweet fullTweet = null;
        
        String theQuery = "http://api.twitter.com/1/statuses/show.json?id=" + idToQuery + "&include_entities=true";
        
        HttpURLConnection response = getResponse(theQuery);
        int statusCode = response.getResponseCode();
        String line;
        
        if (statusCode == 200) {
            InputStream stream = response.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

            while ((line = reader.readLine()) != null) {
                logger.debug("Response: " + line);
                if (line.length() > 2) {
                    Timestamp timeCollected = (new Util()).getTimeNowAsTimestamp();

                    JSONObject root = JSONObject.fromObject(line);
                    fullTweet = util.getFullTweet(root, timeCollected, TASK_ID);                    

                } else {
                    logger.debug("Got empty response because of timeout");
                }
            }            
        } else {
            logger.error("Connection error with status code: " + statusCode);
        }   
        
        response.disconnect();        
        
        return fullTweet;
    }
    
    public ArrayList<FullTweet> getRetweetedBy(String id) throws Exception {
        ArrayList<FullTweet> tweets = new ArrayList<FullTweet>();
        String theQuery = "http://api.twitter.com/1/statuses/retweets/" + id + ".json?count=100&include_entities=true";

        HttpURLConnection response = getResponse(theQuery);
        
        String line;
        int statusCode = response.getResponseCode();

        if (statusCode == 200) {
            InputStream stream = response.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

            while ((line = reader.readLine()) != null) {
                logger.debug("Response: " + line);

                if (line.length() > 2) {
                    Timestamp timeCollected = (new Util()).getTimeNowAsTimestamp();

                    JSONArray root = JSONArray.fromObject(line);
                    
                    for (int i = 0; i < root.size(); i++) {
                        JSONObject item = root.getJSONObject(i);
                        FullTweet tweet = util.getFullTweet(item, timeCollected, TASK_ID);
                        tweet.getTweet().updateProperty("Retweeted", id);
                        tweets.add(tweet);
                    }

                } else {
                    logger.debug("Got empty response because of timeout");
                }
            }
        } else {
            System.out.println("Connection error with status code: " + statusCode);
        }
        
        response.disconnect();
                
        return tweets;
        
    }     
}
