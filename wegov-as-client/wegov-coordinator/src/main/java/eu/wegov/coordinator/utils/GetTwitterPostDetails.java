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
package eu.wegov.coordinator.utils;

import eu.wegov.coordinator.Coordinator;
import eu.wegov.coordinator.dao.data.twitter.FullTweet;
import eu.wegov.coordinator.dao.data.twitter.Tweet;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
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

/**
 *
 * @author Maxim Bashevoy
 */
public class GetTwitterPostDetails {

    private Coordinator coordinator;
    private final static int TASK_ID = 0;
    private Util util = new Util();
    
    private final static Logger logger = Logger.getLogger(GetTwitterPostDetails.class.getName());

    public GetTwitterPostDetails(Coordinator coordinator) {

        logger.debug("Get Twitter Post Details task initializing");

        this.coordinator = coordinator;

    }
    
    public Tweet getTweet(String idToQuery, String userName, String password) throws Exception {
        Tweet tweet = null;
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

        HttpGet get = new HttpGet("http://api.twitter.com/1/statuses/show.json?id=" + idToQuery + "&include_entities=true");

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

                    JSONObject root = JSONObject.fromObject(line);
                    tweet = util.jsonToTweet(root, timeCollected, TASK_ID);

                } else {
                    logger.debug("Got empty response because of timeout");
                }
            }
        }  
        return tweet;
    }

    public FullTweet getFullTweet(String idToQuery, String userName, String password) throws Exception {

        FullTweet fullTweet = null;
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

        HttpGet get = new HttpGet("http://api.twitter.com/1/statuses/show.json?id=" + idToQuery + "&include_entities=true");

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

                    JSONObject root = JSONObject.fromObject(line);
                    fullTweet = util.getFullTweet(root, timeCollected, TASK_ID);

                } else {
                    logger.debug("Got empty response because of timeout");
                }
            }
        }
        
//        if (fullTweet != null)
//            logger.info(fullTweet.toString());
        
        return fullTweet;
    }
    
    public JSONObject getRoot(String idToQuery) throws Exception {

        JSONObject root = null;
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "utf-8");

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params,
                registry);
        DefaultHttpClient client = new DefaultHttpClient(manager, params);

        client.getCredentialsProvider().setCredentials(new AuthScope("stream.twitter.com", 80),
                new UsernamePasswordCredentials("maximbashevoy", "kcG2v6$76/-j(F"));

        HttpGet get = new HttpGet("http://api.twitter.com/1/statuses/show.json?id=" + idToQuery + "&include_entities=true");

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

                    root = JSONObject.fromObject(line);

                } else {
                    logger.debug("Got empty response because of timeout");
                }
            }
        }
        
//        if (fullTweet != null)
//            logger.info(fullTweet.toString());
        
        return root;
    }    
    
}
