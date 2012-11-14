/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2011
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
//	Created By :			Ken Meacham
//	Created Date :			2011-12-19
//	Created for Project :	WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.tools.inject;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import eu.wegov.coordinator.dao.data.WegovPostItem;
import eu.wegov.coordinator.dao.data.WegovSnsUserAccount;
import eu.wegov.tools.WegovTool;

public class TwitterInject extends SingleSiteInject {
	
	private DateFormat postDF = null;
	private DateFormat userDF = null;

	public TwitterInject(WegovTool wegovTool) throws Exception {
		super(wegovTool, "twitter");
		
		defineDateFormats();
	}

	private void defineDateFormats() {
        postDF = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZZ", new Locale("en", "GB")); // Example: "Fri, 16 Sep 2011 13:38:03 +0000"
        postDF.setLenient(false);
        
        userDF = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", new Locale("en", "GB")); // Example: "Tue Dec 22 18:25:58 +0000 2009"
        userDF.setLenient(false);
	}

	protected void setAuthMethod() {
		authMethod = "oauth"; // default;
	}
	
	/* No longer required
	protected AuthScope getAuthScope() throws Exception {
		//String authScope = configuration.getValueOfParameter(site + ".authscope");
		String authScope = "stream.twitter.com";
		return new AuthScope(authScope, 80);		
	}
	*/
	
	public void setupInject() throws Exception {
		/*
		if (whatCollect.equals("posts")) {
			injectUrl = "http://inject.twitter.com/inject.json"; //TODO: fix this
			
			// Location
			setLocationParams();
			
			injectParams.put("q", buildInjectQueryString());
			//injectParams.put("result_type", "recent");
			if (! resultsMaxPerPage.equals(""))
				injectParams.put("rpp", resultsMaxPerPage);
		}
		else if (whatCollect.equals("users")) {
			injectUrl = "http://api.twitter.com/1/users/inject.json";
			injectParams.put("q", buildUsersQueryString());
			if (! resultsMaxPerPage.equals(""))
				injectParams.put("per_page", resultsMaxPerPage);
		}
		else if (whatCollect.equals("groups")) {
			System.out.println("WARNING: cannot inject for groups on Twitter");
		}
		else {
			throw new Exception("Unrecognised parameter value for what.collect: " + whatCollect);
		}
		*/
		
		//TODO: set up inject params
		injectUrl = "https://api.twitter.com/1/statuses/update.json";
		injectParams.put("status", postText);
		injectParams.put("include_entities", "true");
	}

	protected void setLocationViaAPI() {
		/*
		//injectParams.put("geocode", location.trim()); // e.g. "50.91667,-1.38333,1mi"
		String location = locationLat.trim() + "," + locationLong.trim() + "," + locationRadius.trim() + locationRadiusUnit.trim();
		System.out.println("Setting location as: " + location);
		injectParams.put("geocode", location); // e.g. "50.91667,-1.38333,1mi"
		*/
	}

	private String buildInjectQueryString() throws Exception {
		String q = "";
		
		/*
		if (! whatWordsAll.equals(""))
			q += whatWordsAll.trim() + " ";
		
		if (! whatWordsExactPhrase.equals(""))
			q += "\"" + whatWordsExactPhrase.trim() + "\" ";
		
		if (! whatWordsAny.equals("")) {
			q += whatWordsAny.trim().replaceAll("[,\\s]+", " OR ") + " ";
		}
		
		if (! whatWordsNone.equals("")) {
			String[] words = whatWordsNone.trim().split("[,\\s]+");
			for (String word : words) {
				q += "-" + word + " ";
			}
		}
		
		if (! whatWordsHashtags.equals("")) {
			q += expandWordsString(whatWordsHashtags, "#");
		}
		
		if (! whatPeopleFromAccounts.equals("")) {
			q += expandWordsString(whatPeopleFromAccounts, "from:");
		}
		
		if (! whatPeopleToAccounts.equals("")) {
			q += expandWordsString(whatPeopleToAccounts, "to:");
		}
		
		if (! whatPeopleMentioningAccounts.equals("")) {
			q += expandWordsString(whatPeopleMentioningAccounts, "@");
		}
		*/
		
		/* Handled elsewhere
		if ( (! isEmpty(location)) && (! location.equals("any")) ) {
			//TODO
			//q += "location:" + location + " ";
		}
		*/

		/*
		if ( (! isEmpty(language)) && (! language.equals("any")) ) {
			q += "lang:" + language + " ";
		}

		if (! whatDatesOption.equals("any")) {
			if (! isEmpty(whatDatesSince)) {
				q += "since:" + formatDate(whatDatesSince) + " ";
			}
			
			if (! isEmpty(whatDatesUntil)) {
				q += "until:" + formatDate(whatDatesUntil) + " ";
			}
		}
		*/
		
		if (q.endsWith(" "))
			q = q.substring(0, q.length() - 1);
		
		if (q.equals(""))
			//throw new Exception("No query parameters defined");
			System.out.println("WARNING: No query parameters defined");
		
		return q;
	}

	/*
	private String formatDate(String timeStr) {
		String formattedDate = "";
        Date date;
		
        SimpleDateFormat incomingDateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("en", "GB"));
		SimpleDateFormat twitterDateFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("en", "GB"));
		
        incomingDateFormat.setLenient(false);
        
		try {
			date = incomingDateFormat.parse(timeStr);
			formattedDate = twitterDateFormat.format(date);
		} catch (ParseException e) {
			e.printStackTrace(System.out);
		}
		
		System.out.println("Date: " + timeStr + " formatted as " + formattedDate);
		return formattedDate;
	}
	*/

	/*
	private String buildUsersQueryString() throws Exception {
		String q = "";
		
		if (! whatWordsNameContains.equals(""))
			q += whatWordsNameContains.trim() + " ";
		
		if (q.endsWith(" "))
			q = q.substring(0, q.length() - 1);
		
		if (q.equals(""))
			throw new Exception("No query parameters defined");
		
		return q;
	}
	*/

	/*
	private String expandWordsString(String wordsString, String prefix) throws Exception {
		String q = "";
		boolean andOr = false;
		boolean firstWord = true;

		String[] words = wordsString.trim().split("[\\W]+");
		
		for (String word : words) {
			if (!word.equals("")) {
				//System.out.println("\""+word+"\"");
				if ( (word.equals("AND")) || (word.equals("OR")) ) {
					q += word + " ";
					andOr = true;
					if (firstWord)
						throw new Exception("Badly formed query: " + wordsString);
				}
				else {
					if ( (! firstWord) && (! andOr) )
						q += "OR ";
					q += prefix + word + " ";
					andOr = false;
				}
				
				firstWord = false;
			}
		}
		
		if (andOr)
			throw new Exception("Badly formed query: " + wordsString);
		
		return q;
	}
	*/

	protected void submitRequestAndHandleResponse() throws Exception {
		tool.reportMessage(sns.getSNS() + ": inject started");

    	OAuthConsumer consumer = new DefaultOAuthConsumer(oauthConsumerKey, oauthConsumerSecret);
        consumer.setTokenWithSecret(oauthConsumerAccessToken, oauthConsumerAccessTokenSecret);

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey(oauthConsumerKey)
		  .setOAuthConsumerSecret(oauthConsumerSecret)
		  .setOAuthAccessToken(oauthConsumerAccessToken)
		  .setOAuthAccessTokenSecret(oauthConsumerAccessTokenSecret);

		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();

		if (isEmpty(postOption)) {
			System.out.println("No post option set");
			return;
		}

		if (isEmpty(postText)) {
			throw new Exception("No post text set");
		}
		
		// append any hashtags to post text
		appendHashtags();
		
		// Ensure post text is unique for testing purposes
		//postText += " " + UUID.randomUUID();
		
		if (postOption.equals("post")) {
			System.out.println("Submitting post: " + postText + "\n");
			Status status = twitter.updateStatus(postText);
			System.out.println("\nSuccessfully updated the status to [" + status.getText() + "].");
			storePost(status);
		}
		else if (postOption.equals("reply")) {
			String replyText;
			
			if (isEmpty(inReplyToPostId)) {
				throw new Exception("No inReplyToPostId specified for reply");
			}
			
			long inReplyToStatusId = Long.parseLong(inReplyToPostId); //e.g. "149154034853490688"

			if (isEmpty(inReplyToUserId)) {
				System.out.println("WARNING: no user specified for reply");
				replyText = "";
			}
			else {
				replyText = "@" + inReplyToUserId + " ";
			}
			
			replyText += postText;
			
			System.out.println("Replying to post " + inReplyToPostId + " by user " + inReplyToUserId);
			Status status = twitter.updateStatus(new StatusUpdate(replyText).inReplyToStatusId(inReplyToStatusId));
			System.out.println("\nSuccessfully updated the status to [" + status.getText() + "].");
			storePost(status);
		}
		else {
			throw new Exception("Post option not supported: " + postOption);
		}
		
	}

	private void appendHashtags() {
		if (isEmpty(postHashtags))
			return;
		
		String hashtagsString = "";
		
		String[] hashtags = postHashtags.trim().split("[\\W]+");
		
		for (String hashtag : hashtags) {
			if (!isEmpty(hashtag)) {
				System.out.println("\""+hashtag+"\"");
				hashtagsString += " #" + hashtag;
			}
		}
		
		System.out.println("Appending hashtags: " + hashtagsString);
		postText += hashtagsString;
	}

	private void storePost(Status status) throws Exception {
    	WegovPostItem postItem = statusToPost(status);
		System.out.println("Adding post: " + postItem.toString());
    	tool.getCoordinator().getDataSchema().insertObject(postItem);
    	getOrCreateUserAccount(postItem.getAuthor_SnsUserAccount_ID(), status.getUser());
	}

	/*
	protected void handleResponseHeaders(Map<String, String> headersMap) {
        if (headersMap.containsKey("X-RateLimit-Remaining")) {
        	String xRateLimitRemainingStr = stripBrackets(headersMap.get("X-RateLimit-Remaining"));
        	int xRateLimitRemaining = Integer.parseInt(xRateLimitRemainingStr);
        	if (xRateLimitRemaining < 145) {
                System.err.println("WARNING: X-RateLimit-Remaining: " + xRateLimitRemaining);
        	}
        	else {
                System.out.println("\nX-RateLimit-Remaining: " + xRateLimitRemaining);
        	}
        }
	}
	*/

	/*
	private String stripBrackets(String string) {
		int beginIndex = 0;
		int endIndex = string.length();
		
    	if (string.startsWith("["))
    		beginIndex = 1;
    	
    	if (string.endsWith("]"))
    		endIndex--;
    	
    	return string.substring(beginIndex, endIndex);
	}
	*/

	/*
	protected void extractPosts(JSON json) throws Exception {
        if (! (json instanceof JSONObject))
        	throw new Exception("Unexpected JSON type for response: " + json.getClass());
        
        System.out.println("\nParsing JSONObject");
        JSONArray results = (JSONArray) ((JSONObject)json).get("results");
        System.out.println("Number of results: " + results.size());
        for (int i = 0; i < results.size(); i++) {
        	JSONObject object = JSONObject.fromObject(results.get(i));
        	WegovPostItem postItem = jsonToPost(object);
    		System.out.println("Adding post: " + postItem.toString());
        	tool.getCoordinator().getDataSchema().insertObject(postItem);
        	getOrCreateUserAccount(postItem.getAuthor_SnsUserAccount_ID(), object);
        }
	}
	*/

	/*
	protected void extractUsers(JSON json) throws Exception {
        if (! (json instanceof JSONArray))
        	throw new Exception("Unexpected JSON type for response: " + json.getClass());
        
        System.out.println("\nParsing JSONArray");
        JSONArray results = (JSONArray) ((JSONArray)json);
        System.out.println("Number of results: " + results.size());
        for (int i = 0; i < results.size(); i++) {
        	JSONObject object = JSONObject.fromObject(results.get(i));
        	WegovSnsUserAccount userItem = jsonToUser(object);
    		System.out.println("Adding user: " + userItem.toString());
        	// Should we always create new user account entry in db, or create/update if necessary?
        	tool.getCoordinator().getDataSchema().insertObject(userItem);
        	//getOrCreateUserAccount(userItem.getCount_ID(), object);
        }
	}
	*/

	/*
	protected void extractGroups(JSON json) throws Exception {
		System.out.println("WARNING: cannot inject for groups on Twitter");
	}
	*/

	/*
	protected WegovPostItem jsonToPost(JSONObject object) {
		String postID = object.getString("id_str");
		String userID = getAccountID(object);
		String userScreenName = object.getString("from_user");
		String hostSnsID = sns.getID();
		Timestamp dateCreated = getTimestamp(object.getString("created_at"), postDF); // e.g. "Fri, 16 Sep 2011 13:38:03 +0000"
		String title = null;
		String content = object.getString("text");
		String originalPostURL = "http://twitter.com/#!/" + userScreenName + "/status/" + postID;
		short isRetweet = -1; //indicates unknown
		int outputOfRunID = new Integer(tool.getMyRunId());
		
		return new WegovPostItem(postID, userID, hostSnsID, dateCreated, title, content, originalPostURL, isRetweet, outputOfRunID);
	}
	*/

	/*
	protected String getUserIdFromPost(JSONObject object) {
		return object.getString("from_user_id_str");
	}
	*/

	private WegovPostItem statusToPost(Status status) {
		String postID = new Long(status.getId()).toString();
		User user = status.getUser();
		String userID = new Long(user.getId()).toString();
		String accountID = generateAccountID(userID, postID);
		String userScreenName = user.getScreenName();
		String hostSnsID = sns.getID();
		//Timestamp dateCreated = getTimestamp(object.getString("created_at"), postDF); // e.g. "Fri, 16 Sep 2011 13:38:03 +0000"
		Date date = status.getCreatedAt();
		Timestamp dateCreated = new Timestamp(date.getTime());
		String title = null;
		String content = status.getText();
		String originalPostURL = "http://twitter.com/#!/" + userScreenName + "/status/" + postID; //TODO: check this
		//Status retweetedStatus = status.getRetweetedStatus();
		short isRetweet = (short) status.getRetweetCount();
		
		String toUserID = null;
		long toUserIDint = status.getInReplyToUserId();
		if (toUserIDint > 0) toUserID = new Long(toUserIDint).toString();
		
		String replyToPostID = null;
		long replyToPostIDint = status.getInReplyToStatusId();
		if (replyToPostIDint > 0) replyToPostID = new Long(replyToPostIDint).toString();

		Timestamp collectionDate = new Timestamp(new Date().getTime());
		int outputOfRunID = new Integer(tool.getMyRunId());
		
		int likes = -1; // not available for Twitter

		return new WegovPostItem(postID, accountID, hostSnsID, dateCreated, title, content, originalPostURL, isRetweet, toUserID, replyToPostID, likes, collectionDate, outputOfRunID);
	}

	private String getAccountID(User user) {
		String accountID = null;
		String userID = new Long(user.getId()).toString();
		String domain = this.site;
		
		accountID = generateAccountID(userID, domain);
		
		return accountID;
	}

	/*
	private WegovSnsUserAccount jsonToUser(JSONObject object) {
		String userID = object.getString("id_str");
		String fullName = object.getString("name");
		String location = object.getString("location");
		String url = object.getString("url");
		String profilePictureUrl = object.getString("profile_image_url");
		String hostSnsID = sns.getID();
		Timestamp dateCreated = getTimestamp(object.getString("created_at"), userDF); // e.g. "Tue Dec 22 18:25:58 +0000 2009"
		String screenName = object.getString("screen_name");
		String profileUrl = "http://twitter.com/#!/" + screenName;
		int outputOfRunID = new Integer(tool.getMyRunId());

		return new WegovSnsUserAccount(userID, fullName, location, url, profileUrl, profilePictureUrl, hostSnsID, dateCreated, screenName, outputOfRunID);
	}
	*/

	/*
	private Timestamp getTimestamp(String timeStr, DateFormat df) {
		Timestamp timestamp = null;
		long msecs;

		try {
			msecs = df.parse(timeStr).getTime();
	        timestamp = new Timestamp(msecs);
		} catch (ParseException e) {
			e.printStackTrace(System.out);
		}

		return timestamp;
	}
	*/

	/*
	private void getOrCreateUserAccount(String accountID, JSONObject object) throws Exception {
		// If user account does not already exist in the database, create it			
		if (tool.getCoordinator().getDataSchema().getAllWhere(new WegovSnsUserAccount(), "ID", accountID).isEmpty()) {
			System.out.println("Creating user account: " + accountID);
			String userID = accountID;
			String fullName = ""; 
			String location = object.getString("geo");
			String url = "";
			String profilePictureUrl = object.getString("profile_image_url");
			String hostSnsID = sns.getID();
			Timestamp dateCreated = null;
			String screenName = object.getString("from_user");
			String profileUrl = "http://twitter.com/#!/" + screenName;
			int outputOfRunID = new Integer(tool.getMyRunId());

			WegovSnsUserAccount userAccount = new WegovSnsUserAccount(userID, fullName, location, url, profileUrl, profilePictureUrl, hostSnsID, dateCreated, screenName, outputOfRunID);
			System.out.println("Adding account: " + userAccount.toString());
			tool.getCoordinator().getDataSchema().insertObject(userAccount);
		}
		else {
			System.out.println("User account: " + accountID + " already exists");
		}
	}
	*/

	private void getOrCreateUserAccount(String accountID, User user) throws Exception {
		// If user account does not already exist in the database, create it			
		//if (tool.getCoordinator().getDataSchema().getAllWhere(new WegovSnsUserAccount(), "ID", accountID).isEmpty()) {
			System.out.println("Creating user account: " + accountID);
			String userID = accountID;
			String fullName = user.getName();
			String location = user.getLocation();
			String url = "";
			String profilePictureUrl = user.getProfileBackgroundImageUrl();
			String hostSnsID = sns.getID();
			Date date = user.getCreatedAt();
			Timestamp dateCreated = new Timestamp(date.getTime());
			String screenName = user.getScreenName();
			String profileUrl = "http://twitter.com/#!/" + screenName;
			int outputOfRunID = new Integer(tool.getMyRunId());

			int followersCount = user.getFollowersCount();
			int listedCount = user.getListedCount();
			int postsCount = user.getStatusesCount();
			int followingCount = user.getFriendsCount();
			
			Timestamp collectionDate = new Timestamp(new Date().getTime());

			WegovSnsUserAccount userAccount = new WegovSnsUserAccount(userID, fullName, location, url, profileUrl, profilePictureUrl, hostSnsID, dateCreated, screenName, followersCount, listedCount, postsCount, followingCount, collectionDate, outputOfRunID);
			System.out.println("Adding account: " + userAccount.toString());
			tool.getCoordinator().getDataSchema().insertObject(userAccount);
		//}
		//else {
		//	System.out.println("User account: " + accountID + " already exists");
		//}
	}

	@Override
	protected void setSNS() throws Exception {
		int outputOfRunID = new Integer(tool.getMyRunId());
		sns = getOrCreateSNS("twitter", "Twitter", "http://twitter.com/", "http://twitter.com/favicon.ico", outputOfRunID);
	}

	@Override
	protected void extractPosts(JSON json) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	protected void extractUsers(JSON json) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	protected void extractGroups(JSON json) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	protected WegovPostItem jsonToPost(JSONObject object) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getUserIdFromPost(JSONObject object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getPostIdFromPost(JSONObject object) {
		// TODO Auto-generated method stub
		return null;
	}

}
