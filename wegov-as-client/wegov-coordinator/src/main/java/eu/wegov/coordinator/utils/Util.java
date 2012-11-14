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
//	Created Date :			2011-07-26
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator.utils;

import eu.wegov.coordinator.dao.Dao;
import eu.wegov.coordinator.dao.data.twitter.FullTweet;
import eu.wegov.coordinator.dao.data.twitter.Hashtag;
import eu.wegov.coordinator.dao.data.twitter.Tweet;
import eu.wegov.coordinator.dao.data.twitter.Url;
import eu.wegov.coordinator.dao.data.twitter.User;
import eu.wegov.coordinator.dao.data.twitter.UserMention;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Maxim Bashevoy
 */
public class Util {

    private final static Logger logger = Logger.getLogger(Util.class.getName());

    public Util() {
    
    }

    public Integer[] sortArrayList(ArrayList<Integer> arrayToProcess) {
        if (arrayToProcess == null)
            throw new RuntimeException("Can not process null arraylist");
        
        if (arrayToProcess.isEmpty())
            throw new RuntimeException("Can not process empty arraylist");
        
        Integer[] tempArray = new Integer[arrayToProcess.size()];
        arrayToProcess.toArray(tempArray);
        Arrays.sort(tempArray);
        
        return tempArray;
    }

    public Integer maxIntFromArrayList(ArrayList<Integer> arrayToProcess) {
        
        if (arrayToProcess == null)
            throw new RuntimeException("Can not process null arraylist");
        
        if (arrayToProcess.isEmpty())
            throw new RuntimeException("Can not process empty arraylist");
        
        Integer[] tempArray = new Integer[arrayToProcess.size()];
        arrayToProcess.toArray(tempArray);
        Arrays.sort(tempArray);

        return tempArray[tempArray.length - 1];
    }
    
    public Integer minIntFromArrayList(ArrayList<Integer> arrayToProcess) {
        
        if (arrayToProcess == null)
            throw new RuntimeException("Can not process null arraylist");
        
        if (arrayToProcess.isEmpty())
            throw new RuntimeException("Can not process empty arraylist");
        
        Integer[] tempArray = new Integer[arrayToProcess.size()];
        arrayToProcess.toArray(tempArray);
        Arrays.sort(tempArray);

        return tempArray[0];
    }
    
    public String makeShaHash(String passwordToEncrypt) {
        if (passwordToEncrypt != null)
            return DigestUtils.shaHex(passwordToEncrypt);
        else
            throw new RuntimeException("You are trying to encrypt \'null\' password!");
    }
    
    public boolean isHashMatch(String unencryptedCandidate, String existingHash) {
        if ( (unencryptedCandidate == null) | (existingHash == null) )
            throw new RuntimeException("Can not match \'null\' values!");
        
        if (makeShaHash(unencryptedCandidate).equals(existingHash))
            return true;
        else
            return false;
    }

    public static String ensureSlash(String string) {
        if (string != null) {
            String newString = string;
            if (!string.endsWith("/")) {
                newString = newString + "/";
            }
            return newString;
        } else {
            return null;
        }
    }

    public static Date stringToDate(String dateAsString) throws ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.parse(dateAsString);
    }

    public String getTimeNow() {
        Date now = new Date();
        return now.toString();
    }

    public Timestamp getTimeNowAsTimestamp() {
        Date now = new Date();
        return new Timestamp(now.getTime());
    }

    public ArrayList<User> jsonToUsers(JSONArray rootArray, Timestamp timeCollected, int outputOfTaskID) throws ParseException {
        ArrayList<User> users = new ArrayList<User>();

        for (int i = 0; i < rootArray.size(); i++) {
            JSONObject root = JSONObject.fromObject(rootArray.get(i));
            users.add(jsonToUser(root, timeCollected, outputOfTaskID));
        }

        return users;
    }


    public User jsonToUser(JSONObject root, Timestamp timeCollected, int outputOfTaskID) throws ParseException {
        User newUser = new User();

        logger.debug("New User:");

        updateObject(newUser, root);

        newUser.updateProperty("Collected_at", timeCollected);
        newUser.updateProperty("OutputOfTaskID", outputOfTaskID);

        return newUser;
    }
    

    public ArrayList<Tweet> jsonToTweets(JSONArray rootArray, Timestamp timeCollected, int outputOfTaskID) throws ParseException {
        ArrayList<Tweet> tweets = new ArrayList<Tweet>();

        for (int i = 0; i < rootArray.size(); i++) {
            JSONObject root = JSONObject.fromObject(rootArray.get(i));
            tweets.add(jsonToTweet(root, timeCollected, outputOfTaskID));
        }

        return tweets;
    }    

    public Tweet jsonToTweet(JSONObject root, Timestamp timeCollected, int outputOfTaskID) throws ParseException {
        Tweet newTweet = new Tweet();
//        ArrayList<Triplet<String, String, Object>> newProperties = newTweet.getProperties();
//        newProperties.clear();

        logger.debug("New Tweet:");

        for (Triplet<String, String, ?> entry : (new Tweet()).getProperties()) {

            String key = entry.getKey();
            String description = entry.getDescription();

            if ((!description.startsWith("SERIAL")) & (root.keySet().contains(key.toLowerCase()))) {

                String acquiredValue = root.getString(key.toLowerCase());

                logger.debug(key + ": " + acquiredValue);

                if (description.startsWith("character") | description.startsWith("text")) {

                    if (key.toLowerCase().equals("place")) {
                        JSONObject place = JSONObject.fromObject(root.get("place"));

                        if (place.containsKey("country")) {
                            String country_code = place.getString("country_code").trim();
                            String country = place.getString("country").trim();
                            String full_name = place.getString("full_name").trim();
                            String name = place.getString("name").trim();

                            acquiredValue = name + " (" + full_name + ") " + country + " (" + country_code + ")";
                        }

                    } else if (key.toLowerCase().equals("coordinates")) {
                        JSONObject coords = JSONObject.fromObject(root.get("coordinates"));

                        if (coords.containsKey("coordinates")) {
                            acquiredValue = coords.getString("coordinates");
                        }

                    } else if (key.toLowerCase().equals("geo")) {
                        JSONObject geo = JSONObject.fromObject(root.get("geo"));

                        if (geo.containsKey("coordinates")) {
                            acquiredValue = geo.getString("coordinates");
                        }

                    } else if (key.toLowerCase().equals("source")) {

                        acquiredValue = acquiredValue.replaceAll("\\<.*?>", "");

                    }

//                    newProperties.add(new Triplet(key, description, acquiredValue.trim()));
                    newTweet.updateProperty(key, acquiredValue.trim());

                } else if (description.startsWith("time")) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
                    dateFormat.setLenient(false);
                    long msecs = dateFormat.parse(acquiredValue).getTime();
                    newTweet.updateProperty(key, new Timestamp(msecs));
                } else if (description.startsWith("int")) {
                    newTweet.updateProperty(key,  Integer.parseInt(acquiredValue));
                } else if (description.startsWith("smallint")) {
                    newTweet.updateProperty(key, Short.valueOf(acquiredValue));
                } else if (description.startsWith("boolean")) {
                    newTweet.updateProperty(key, Boolean.parseBoolean(acquiredValue));
                } else {
                    logger.error("Description not recognised: " + description);
                }
            }
        }

        newTweet.updateProperty("Collected_at", timeCollected);
        newTweet.updateProperty("OutputOfTaskID", outputOfTaskID);
//        newProperties.add(new Triplet("OutputOfTaskID", (new Tweet()).getDescriptionForKey("OutputOfTaskID"), outputOfTaskID));
//        logger.debug(newTweet.toString());

        return newTweet;
    }

    public ArrayList<UserMention> jsonToUserMentions(JSONArray rootArray, Timestamp timeCollected, int outputOfTaskID) throws ParseException {
        ArrayList<UserMention> userMentions = new ArrayList<UserMention>();

        for (int i = 0; i < rootArray.size(); i++) {
            JSONObject root = JSONObject.fromObject(rootArray.get(i));
            userMentions.add(jsonToUserMention(root, timeCollected, outputOfTaskID));
        }

        return userMentions;
    }

    public UserMention jsonToUserMention(JSONObject root, Timestamp timeCollected, int outputOfTaskID) throws ParseException {
        UserMention newUserMention = new UserMention();

        logger.debug("New User Mention:");

        updateObject(newUserMention, root);

        newUserMention.updateProperty("Collected_at", timeCollected);
        newUserMention.updateProperty("OutputOfTaskID", outputOfTaskID);

        return newUserMention;
    }
    
    public void updateObject(Dao object, JSONObject root) throws ParseException {
        for (Triplet<String, String, Object> entry : object.getProperties()) {

            String key = entry.getKey();
            String description = entry.getDescription();

            if ((!description.startsWith("SERIAL")) & (root.keySet().contains(key.toLowerCase()))) {

                String acquiredValue = root.getString(key.toLowerCase());
                Object value = null;

                logger.debug(key + ": " + acquiredValue);

                if (description.startsWith("character") | description.startsWith("text")) {
                    value = acquiredValue.trim();
                } else if (description.startsWith("time")) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
                    dateFormat.setLenient(false);
                    long msecs = dateFormat.parse(acquiredValue).getTime();
                    value = new Timestamp(msecs);
                } else if (description.startsWith("int")) {
                    value = Integer.parseInt(acquiredValue);
                } else if (description.startsWith("smallint")) {
                    value = Short.valueOf(acquiredValue);
                } else if (description.startsWith("boolean")) {
                    value = Boolean.parseBoolean(acquiredValue);
                } else {
                    logger.error("Description not recognised: " + description);
                }
                
                object.updateProperty(key, value);
            }
        }        
    }

    public ArrayList<Url> jsonToUrls(JSONArray rootArray, Timestamp timeCollected, int outputOfTaskID) throws ParseException {
        ArrayList<Url> urls = new ArrayList<Url>();

        logger.debug("Urls array size: " + rootArray.size());

        for (int i = 0; i < rootArray.size(); i++) {
            JSONObject root = JSONObject.fromObject(rootArray.get(i));
            urls.add(jsonToUrl(root, timeCollected, outputOfTaskID));
        }

        return urls;
    }

    public Url jsonToUrl(JSONObject root, Timestamp timeCollected, int outputOfTaskID) throws ParseException {
        Url newUrl = new Url();

        logger.debug("New Url:");

        updateObject(newUrl, root);

        newUrl.updateProperty("Collected_at", timeCollected);
        newUrl.updateProperty("OutputOfTaskID", outputOfTaskID);

        return newUrl;
    }

    public ArrayList<Hashtag> jsonToHashtags(JSONArray rootArray, Timestamp timeCollected, int outputOfTaskID) throws ParseException {
        ArrayList<Hashtag> hashtags = new ArrayList<Hashtag>();

        for (int i = 0; i < rootArray.size(); i++) {
            JSONObject root = JSONObject.fromObject(rootArray.get(i));
            hashtags.add(jsonToHashtag(root, timeCollected, outputOfTaskID));
        }

        return hashtags;
    }

    public Hashtag jsonToHashtag(JSONObject root, Timestamp timeCollected, int outputOfTaskID) throws ParseException {
        Hashtag newHashtag = new Hashtag();

        logger.debug("New Hashtag:");

        updateObject(newHashtag, root);

        newHashtag.updateProperty("Collected_at", timeCollected);
        newHashtag.updateProperty("OutputOfTaskID", outputOfTaskID);

        return newHashtag;
    }

    public FullTweet getFullTweet(JSONObject root, Timestamp timeCollected, int outputOfTaskID) throws ParseException {
        JSONObject userRoot = JSONObject.fromObject(root.get("user"));
        JSONObject entitiesRoot = JSONObject.fromObject(root.get("entities"));
        JSONArray mentionsArray = JSONArray.fromObject(entitiesRoot.get("user_mentions"));
        JSONArray urlsArray = JSONArray.fromObject(entitiesRoot.get("urls"));
        JSONArray hashtagsArray = JSONArray.fromObject(entitiesRoot.get("hashtags"));

        Tweet tweet = jsonToTweet(root, timeCollected, outputOfTaskID);
        String tweetId = tweet.getID();

        User user = jsonToUser(userRoot, timeCollected, outputOfTaskID);
        String userId = user.getID();

        ArrayList<UserMention> mentions = jsonToUserMentions(mentionsArray, timeCollected, outputOfTaskID);
        ArrayList<Url> urls = jsonToUrls(urlsArray, timeCollected, outputOfTaskID);
        ArrayList<Hashtag> hashtags = jsonToHashtags(hashtagsArray, timeCollected, outputOfTaskID);

        tweet.updateProperty("By_user_id", userId);

        for (UserMention mention : mentions) {
            mention.updateProperty("Mentioned_in_tweet_id", tweetId);
            mention.updateProperty("Mentioned_by_user_id", userId);
        }

        for (Url url : urls) {
            url.updateProperty("In_tweet_id", tweetId);
            url.updateProperty("By_user_id", userId);
        }

        for (Hashtag hashtag : hashtags) {
            hashtag.updateProperty("In_tweet_id", tweetId);
            hashtag.updateProperty("By_user_id", userId);
        }

        return new FullTweet(tweet, user, mentions, urls, hashtags);
    }
}
