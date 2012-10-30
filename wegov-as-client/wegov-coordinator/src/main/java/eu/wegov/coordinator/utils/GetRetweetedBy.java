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
import eu.wegov.coordinator.dao.data.twitter.FullTweet;
import eu.wegov.coordinator.dao.data.twitter.User;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
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

/**
 *
 * @author Maxim Bashevoy
 */
public class GetRetweetedBy{
    private Coordinator coordinator;
    private final static int TASK_ID = 0;
    private Util util = new Util();
    
    private final static Logger logger = Logger.getLogger(GetRetweetedBy.class.getName());

    public GetRetweetedBy(Coordinator coordinator) {

        logger.debug("Get Top Retweeted task initializing");

        this.coordinator = coordinator;

    }
    
    private HttpURLConnection getResponse(URL url) throws Exception {
        OAuthConsumer consumer = new DefaultOAuthConsumer(
                "bPE4bvcVbWgyr7h2VK3sw",
                "LDM87QZ2gPREUQrxQSnznPMzfqxcJe66WD9BbhuX3E");

        OAuthProvider provider = new DefaultOAuthProvider(
                "https://api.twitter.com/oauth/request_token",
                "https://api.twitter.com/oauth/access_token",
                "https://api.twitter.com/oauth/authorize");

        System.out.println("Fetching request token from Twitter...");

        consumer.setTokenWithSecret("20392673-3MWLamdbcj19BU8jeBbyBNuuuMQG8j8pJkoZdUIN8", "UynHwahnyRkXOxbbVWrqKy55P2qE88RUgz3OtNPx4");
        
        HttpURLConnection request = (HttpURLConnection) url.openConnection();

        consumer.sign(request);
        
        request.connect();
        
        return request;
    }
    
    public ArrayList<User> getUsers(String id, String userName, String password) throws Exception {
        ArrayList<User> users = new ArrayList<User>();
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

        HttpGet get = new HttpGet("http://api.twitter.com/1/statuses/" + id + "/retweeted_by.json?count=100");

        logger.debug("Request sent");
        String line;

        HttpResponse resp = client.execute(get);
        int statusCode = resp.getStatusLine().getStatusCode();

        logger.debug("Response code: " + statusCode);

        if (statusCode == 200) {
            InputStream stream = resp.getEntity().getContent();

            logger.debug("Got stream");

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

            while ((line = reader.readLine()) != null) {
                logger.debug("Response: " + line);

                if (line.length() > 2) {
                    Timestamp timeCollected = (new Util()).getTimeNowAsTimestamp();

                    JSONArray root = JSONArray.fromObject(line);
                    
                        users = util.jsonToUsers(root, timeCollected, TASK_ID);
//                        tweets.add(tweet);

                } else {
                    logger.debug("Got empty response because of timeout");
                }
            }
        }
        
//        if (fullTweet != null)
//            logger.info(fullTweet.toString());
        
        return users;
        
    }
    
    public ArrayList<FullTweet> getFullTweets(String id, String userName, String password) throws Exception {
        ArrayList<FullTweet> tweets = new ArrayList<FullTweet>();
        URL url = new URL("http://api.twitter.com/1/statuses/retweets/" + id + ".json?count=100&include_entities=true");

        HttpURLConnection request = getResponse(url);
        
        logger.debug("Request sent");
        String line;

        int statusCode = request.getResponseCode();

        logger.debug("Response code: " + statusCode);

        if (statusCode == 200) {
            InputStream stream = request.getInputStream();

            logger.debug("Got stream");

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
        
        request.disconnect();
                
        return tweets;
        
    }    
}
