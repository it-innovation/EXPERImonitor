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
//	Created Date :			2011-09-29
//	Created for Project :	WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.tools.search;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import eu.wegov.coordinator.Run;
import eu.wegov.coordinator.dao.Dao;
import eu.wegov.coordinator.dao.data.WegovPostItem;
import eu.wegov.coordinator.dao.data.WegovSnsUserAccount;
import eu.wegov.tools.WegovTool;

public class TwitterSearch extends SingleSiteSearch {
	
	private DateFormat postDF = null;
	private DateFormat userDF = null;
	private long sinceId;
	private int nTweets = 0;

	public TwitterSearch(WegovTool wegovTool) throws Exception {
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
	
	public void setupSearch() throws Exception {
		if (whatCollect.equals("posts")) {
			searchUrl = "http://search.twitter.com/search.json";
			
			// Location
			setLocationParams();
			
			searchParams.put("q", buildPostsQueryString());
			searchParams.put("include_entities", "true");
			searchParams.put("result_type", "recent");
			
			if (collectResultsSinceLastActivityRun) {
				String id = getLatestPostIdFromPreviousRun();
				System.out.println("Last post from previous run: " + id);
				if (id != null) {
					sinceId = Long.parseLong(id);
					searchParams.put("since_id", id);
				}
				else {
					collectResultsSinceLastActivityRun = false;
				}
			}
			
			searchParams.put("rpp", Integer.toString(resultsMaxPerPageInt));
		}
		else if (whatCollect.equals("users")) {
			searchUrl = "http://api.twitter.com/1/users/search.json";
			searchParams.put("q", buildUsersQueryString());
			searchParams.put("include_entities", "true");
			//if (! resultsMaxPerPage.equals(""))
			//	searchParams.put("per_page", resultsMaxPerPage);
			searchParams.put("rpp", Integer.toString(resultsMaxPerPageInt));
		}
		else if (whatCollect.equals("groups")) {
			System.out.println("WARNING: cannot search for groups on Twitter");
		}
		else if (whatCollect.equals("userdetails")) {
			searchUrl = "http://api.twitter.com/1/users/lookup.json";
			
			searchParams.clear(); // first clear the current params
			String userIds = getUserIdsAsCSV();
			if (userIds != null) {
				searchParams.put("user_id", userIds);
				searchParams.put("include_entities", "true");
			}
		}
		else {
			throw new Exception("Unrecognised parameter value for what.collect: " + whatCollect);
		}
	}

	@Override
	protected int getDefaultMaxResultsPerPage() {
		return 100;
	}

	@Override
	protected int getDefaultMaxPages() {
		return 15;
	}

	@Override
	protected int getDefaultMaxResults() {
		return 1500;
	}

	protected void setLocationViaAPI() {
		//searchParams.put("geocode", location.trim()); // e.g. "50.91667,-1.38333,1mi"
		String location = locationLat.trim() + "," + locationLong.trim() + "," + locationRadius.trim() + locationRadiusUnit.trim();
		System.out.println("Setting location as: " + location);
		searchParams.put("geocode", location); // e.g. "50.91667,-1.38333,1mi"
		
		//TODO: try geo searches like this:
		//searchUrl = "https://api.twitter.com/1/geo/search.json?lat=50.91667&long=-1.38333";
	}

	private String buildPostsQueryString() throws Exception {
		String q = "";
		
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
		
		/* Handled elsewhere
		if ( (! isEmpty(location)) && (! location.equals("any")) ) {
			//TODO
			//q += "location:" + location + " ";
		}
		*/

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
		
		if (q.endsWith(" "))
			q = q.substring(0, q.length() - 1);
		
		if (q.equals(""))
			//throw new Exception("No query parameters defined");
			System.out.println("WARNING: No query parameters defined");
		
		return q;
	}

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

	private String stripBrackets(String string) {
		int beginIndex = 0;
		int endIndex = string.length();
		
    	if (string.startsWith("["))
    		beginIndex = 1;
    	
    	if (string.endsWith("]"))
    		endIndex--;
    	
    	return string.substring(beginIndex, endIndex);
	}


	protected void extractPosts(JSON json) throws Exception {
		boolean collectionTerminated = false;
		
        if (! (json instanceof JSONObject))
        	throw new Exception("Unexpected JSON type for response: " + json.getClass());
        
        JSONObject jsonObject = (JSONObject)json;
        if (query == null) {
        	query = jsonObject.getString("query");
        }
        
        if (printDataObjects) System.out.println("\nParsing JSONObject");
        JSONArray results = (JSONArray) jsonObject.get("results");
        System.out.println("Number of results returned: " + results.size());
        
        for (int i = 0; i < results.size(); i++) {
        	JSONObject object = JSONObject.fromObject(results.get(i));
        	WegovPostItem postItem = jsonToPost(object);
        	String postId = object.getString("id_str");
        	
        	if (maxId == null) { //i.e. first result should have largest id
        		maxId = postId;
        	}
        	
        	minId = postId; // latest result should have smallest id
        	
        	if (collectResultsSinceLastActivityRun && (! postIdInRange(postId)) ) {
        		System.out.println("WARNING: post id: " + postId + " <= since id: " + sinceId + " (discarding post)");
        		System.out.println("Terminating collection");
        		collectionTerminated = true;
        		break;
        	}
        	
        	if (printDataObjects) System.out.println("Adding post: " + postItem.toString());
        	
        	storePost(object, postItem);
        	
        	getOrCreateUserAccount(postItem.getAuthor_SnsUserAccount_ID(), object);

        	nResults++; // increment total number of results
        	
        	if (limitResults && (nResults >= resultsMaxInt)) {
        		System.out.println("Reached max number of results: " + resultsMaxInt);
        		break;
        	}
        }

        System.out.println("Total number of results stored: " + nResults);
        
		//tool.reportMessage(sns.getSNS() + ": collected " + nResults + " tweets");
		tool.reportMessage("Collected " + nResults + " tweets");
		
		if (results.size() == 0) {
			System.out.println("No results returned - aborting collection");
			collectionTerminated = true;
		}

        nextPageQuery = null;
        
        if (collectionTerminated) return;
        
        if ((!limitResults) || (nResults < resultsMaxInt)) {
        	if (jsonObject.has("next_page")) {
        		nextPageQuery = jsonObject.getString("next_page");
        		System.out.println("\nNext page: " + nextPageQuery);
        	}
        }
	}

	private boolean postIdInRange(String id) {
		long postId = Long.parseLong(id);
		//System.out.println("Comparing " + postId + " to " + sinceId);
		return (postId > sinceId);
	}

	protected void extractUsers(JSON json) throws Exception {
        if (! (json instanceof JSONArray))
        	throw new Exception("Unexpected JSON type for response: " + json.getClass());
        
        if (printDataObjects) System.out.println("\nParsing JSONArray");
        JSONArray results = (JSONArray) ((JSONArray)json);
        System.out.println("Number of results: " + results.size());
        for (int i = 0; i < results.size(); i++) {
        	JSONObject object = JSONObject.fromObject(results.get(i));
        	
        	storeUser(object);
        	
        	nResults++; // increment total number of results
        }
        
		//tool.reportMessage(sns.getSNS() + ": collected " + nTweets + " tweets, " + nResults + " users");
		tool.reportMessage("Collected " + nTweets + " tweets, " + nResults + " users");
	}

	protected void extractGroups(JSON json) throws Exception {
		System.out.println("WARNING: cannot search for groups on Twitter");
	}

	protected WegovPostItem jsonToPost(JSONObject object) {
		
		/* Example
		"created_at": "Fri, 16 Sep 2011 13:38:03 +0000",
		"from_user": "PhilTaylor_",
		"from_user_id": 352257260,
		"from_user_id_str": "352257260",
		"geo": null,
		"id": 114694536260878337,
		"id_str": "114694536260878337",
		"iso_language_code": "en",
		"metadata": {"result_type": "recent"},
		"profile_image_url": "http://a2.twimg.com/profile_images/1522131234/image_normal.jpg",
		"source": "&lt;a href=&quot;http://twitterrific.com&quot; rel=&quot;nofollow&quot;&gt;Twitterrific&lt;/a&gt;",
		"text": "RT @PeterWatt123: My take on the excellent new book 'What next for Labour' on @LabourUncut http://t.co/aSQ2RdAb #wnfl",
		"to_user_id": null,
		"to_user_id_str": null
		*/
		
		String postID = object.getString("id_str");
		String accountID = getAccountIdFromPost(object);
		String userScreenName = object.getString("from_user");
		String hostSnsID = sns.getID();
		Timestamp dateCreated = getTimestamp(object.getString("created_at"), postDF); // e.g. "Fri, 16 Sep 2011 13:38:03 +0000"
		String title = null;
		String content = object.getString("text");
		String originalPostURL = "http://twitter.com/#!/" + userScreenName + "/status/" + postID;
		short isRetweet = -1; //indicates unknown
		String toUserID = (object.containsKey("to_user_id_str")) ? object.getString("to_user_id_str") : null;
		String replyToPostID = (object.containsKey("in_reply_to_status_id_str")) ? object.getString("in_reply_to_status_id_str") : null;
		int outputOfRunID = new Integer(tool.getMyRunId());

		int likes = -1; // not available for Twitter

		return new WegovPostItem(postID, accountID, hostSnsID, dateCreated, title, content, originalPostURL, isRetweet, toUserID, replyToPostID, likes, collectionDate, outputOfRunID);
	}

	protected String getPostIdFromPost(JSONObject object) {
		// N.B. For Twitter search, we don't need to append the post id to the user account, as we are not getting
		// distinct user records per post (cf stream). Instead, the subsequent users search gets all users for these posts in one go.
		//return object.getString("id_str");
		return "";
	}

	protected String getUserIdFromPost(JSONObject object) {
		return object.getString("from_user_id_str");
	}

	protected WegovSnsUserAccount jsonToUser(JSONObject object) {
		/* Example
	    {
	    "profile_background_color": "f4f4f1",
	    "protected": false,
	    "profile_image_url": "http://a0.twimg.com/profile_images/1556433620/FiggleChatTwitter_normal.jpg",
	    "profile_background_tile": false,
	    "profile_image_url_https": "https://si0.twimg.com/profile_images/1556433620/FiggleChatTwitter_normal.jpg",
	    "name": "Fred",
	    "show_all_inline_media": false,
	    "contributors_enabled": false,
	    "favourites_count": 0,
	    "profile_sidebar_fill_color": "f3dd37",
	    "listed_count": 1275,
	    "lang": "en",
	    "utc_offset": -28800,
	    "created_at": "Tue Dec 22 18:25:58 +0000 2009",
	    "description": "Hey online friends! I'm Fred Figglehorn and I'm not trying to brag or anything, but I can talk to animals",
	    "time_zone": "Pacific Time (US & Canada)",
	    "profile_sidebar_border_color": "edd734",
	    "followers_count": 87808,
	    "verified": true,
	    "status":     {
	      "retweeted": false,
	      "in_reply_to_status_id_str": null,
	      "geo": null,
	      "in_reply_to_user_id_str": null,
	      "coordinates": null,
	      "created_at": "Mon Oct 03 21:42:47 +0000 2011",
	      "contributors": null,
	      "place": null,
	      "favorited": false,
	      "in_reply_to_status_id": null,
	      "source": "<a href=\"http://www.whosay.com\" rel=\"nofollow\">WhoSay<\/a>",
	      "id_str": "120977117730189312",
	      "retweet_count": 2,
	      "in_reply_to_screen_name": null,
	      "in_reply_to_user_id": null,
	      "id": 120977117730189312,
	      "possibly_sensitive": false,
	      "truncated": false,
	      "text": "i hackin love your comments!!!! keep em coming!! http://t.co/6YzP78uq"
	    },
	    "statuses_count": 679,
	    "geo_enabled": false,
	    "profile_use_background_image": true,
	    "default_profile_image": false,
	    "notifications": false,
	    "profile_text_color": "000000",
	    "default_profile": false,
	    "following": false,
	    "profile_background_image_url": "http://a1.twimg.com/profile_background_images/330109148/Fred_twitterskin_Version2_final2.jpg",
	    "location": "sorry, not allowed to tell",
	    "id_str": "98691222",
	    "is_translator": false,
	    "profile_background_image_url_https": "https://si0.twimg.com/profile_background_images/330109148/Fred_twitterskin_Version2_final2.jpg",
	    "profile_link_color": "ed1f22",
	    "id": 98691222,
	    "follow_request_sent": false,
	    "friends_count": 6,
	    "url": "http://www.youtube.com/fred",
	    "screen_name": "FredFigglehorn"
	  },		
		 */
		
		//String userID = object.getString("id_str");
		String userID = generateAccountID(object.getString("id_str"), this.site, "");
		String fullName = object.getString("name");
		String location = object.getString("location");
		String url = object.getString("url");
		String profilePictureUrl = object.getString("profile_image_url");
		String hostSnsID = sns.getID();
		Timestamp dateCreated = getTimestamp(object.getString("created_at"), userDF); // e.g. "Tue Dec 22 18:25:58 +0000 2009"
		String screenName = object.getString("screen_name");
		String profileUrl = "http://twitter.com/#!/" + screenName;
		int outputOfRunID = new Integer(tool.getMyRunId());

		int followersCount = new Integer(object.getString("followers_count"));
		int listedCount = new Integer(object.getString("listed_count"));
		int postsCount = new Integer(object.getString("statuses_count"));
		int followingCount = new Integer(object.getString("friends_count"));

		return new WegovSnsUserAccount(userID, fullName, location, url, profileUrl, profilePictureUrl, hostSnsID, dateCreated, screenName, followersCount, listedCount, postsCount, followingCount, collectionDate, outputOfRunID);
	}

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

	private void getOrCreateUserAccount(String accountID, JSONObject object) throws Exception {
		/*
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
		*/
		
		// Create unique user account for this run
    	// Get Twitter user id from post and add to set of users to look up later
    	String userId = getUserIdFromPost(object);
    	//System.out.println("Adding userid to list: " + userId);
    	userIds.add(userId);
	}

	@Override
	protected void setSNS() throws Exception {
		int outputOfRunID = new Integer(tool.getMyRunId());
		sns = getOrCreateSNS("twitter", "Twitter", "http://twitter.com/", "http://twitter.com/favicon.ico", outputOfRunID);
	}

	@Override
	public void execute() throws Exception {
		// First, run standard execution for search
		super.execute();
		
		if (resultsType.equals("dynamic"))
			return;
		
		// Perform subsequent search/query, as required
		if (whatCollect.equals("posts")) {
			executeUserDetailsLookup();
		}
	}

	@Override
	protected void handleResponse(JSON json) throws Exception {
		if (whatCollect.equals("userdetails")) {
			if (printResponses) System.out.println("\nHandling response:");
			if (printResponses) System.out.println(json.toString(2));
			extractUsers(json);
		}
		else {
			super.handleResponse(json);
		}
	}

	private void executeUserDetailsLookup() throws Exception {
		resultsMaxInt = userIds.size();

		if (resultsMaxInt == 0) {
			System.out.println("No users to look up");
			return;
		}

		nTweets = nResults;

		limitResults = true;
		System.out.println("Number of users to collect = " + resultsMaxInt);

		userIdsArray = userIds.toArray(new String[0]);

		whatCollect = "userdetails";
		
		try {
			submitRequestAndHandleResponse();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new Exception("User lookup failed: " + e.getMessage());
		}
	}

	private String getUserIdsAsCSV() {
		StringBuffer sb = new StringBuffer();
		
		//for (Iterator<String> iterator = userIds.iterator(); iterator.hasNext();) {
		//	String userId = (String) iterator.next();
		//	sb.append(userId + ",");
		//}
		
		int pageOffset = (currentPage * 100);
		int numUsers = userIds.size();
		int numUsersRequested = 0;
		
		for (int i=0; i<100; i++) {
			int userOffset = pageOffset + i;
			if (userOffset >= numUsers)
				break;
			String userId = userIdsArray[userOffset];
			sb.append(userId + ",");
			numUsersRequested++;
		}
		
		String userIdsCSV = sb.toString();
		if (userIdsCSV.endsWith(",")) {
			userIdsCSV = userIdsCSV.substring(0, userIdsCSV.length()-1);
		}
		
		if ( (numUsersRequested == 0) || (userIdsCSV.equals("")) )
			return null;
		
		System.out.println("Requesting " + numUsersRequested + " user ids");
		System.out.println("User ids: " + userIdsCSV);
		return userIdsCSV;
	}

	@Override
	protected String getSearchType() {
		return "posts-twitter";
	}

	@Override
	protected void handleStatusCode(int statusCode) throws Exception {
		System.out.println("Status code: " + statusCode);
		
		if ( (statusCode == 502) || (statusCode == 503) || (statusCode == 504) ) {
			System.out.println("Setting retryRequest = true");
			retryRequest = true;
		}
		else {
			System.out.println("Status code not handled by TwitterSearch: " + statusCode);
			super.handleStatusCode(statusCode);
		}
	}

}
