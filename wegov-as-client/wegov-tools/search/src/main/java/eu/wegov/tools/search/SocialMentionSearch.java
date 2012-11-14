package eu.wegov.tools.search;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import eu.wegov.coordinator.dao.data.WegovPostItem;
import eu.wegov.coordinator.dao.data.WegovSNS;
import eu.wegov.coordinator.dao.data.WegovSnsUserAccount;
import eu.wegov.tools.WegovTool;

public class SocialMentionSearch extends SingleSiteSearch {
	
	private HashSet<String> sources;

	public SocialMentionSearch(WegovTool wegovTool) throws Exception {
		super(wegovTool, "socialmention");
	}

	private void setSources() {
		sources = new HashSet<String>();
		if (! isEmpty(aggregatorSources)) {
			String[] sourcesArray = aggregatorSources.split(",");
			for (String source : sourcesArray) {
				sources.add(source.trim());
			}
		}
	}

	public void setupSearch() throws Exception {
		searchUrl = "http://api2.socialmention.com/search";
		searchParams.put("f", "json"); // Return type
		setSourceParams();	// Select sources
		
		// search type (blogs, microblogs, bookmarks, comments, events, images, news, videos, audio, questions, networks, all), can use multiple 
		searchParams.put("t[]", "blogs&t[]=microblogs&t[]=comments&t[]=news&t[]=networks");

		if (whatCollect.equals("posts")) {
			// Location
			setLocationParams();

			// Set query
			searchParams.put("q", buildPostsQueryString());
			
			// Date range
			if (! whatDatesOption.equals("any")) {
				if (! isEmpty(whatDatesSince)) {
					// Remove results older than the supplied number of seconds, ex: to remove results older than 1 day &from_ts=86400
					searchParams.put("from_ts", formatTimeSince(whatDatesSince));
				}

				if (! isEmpty(whatDatesUntil)) {
					System.out.println("WARNING: cannot search until specified date: " + whatDatesUntil + " (parameter ignored)");
				}
			}
			
			//Other available parameters (not yet implemented)
			//lang		(optional) 2 letter ISO 639-1 language code (if null - source default language will be returned
			//strict	(default true) disable strict mode, which will not query sources that do not support the language param (true, false)
			//callback	(optional) required for JSONP compatible requests
			//key		(optional) required for commercial requests
			//meta[]	(optional) meta data to include, ex: &meta=top 
		}
		else if (whatCollect.equals("users")) {
			System.out.println("WARNING: search for users not available");
		}
		else if (whatCollect.equals("groups")) {
			System.out.println("WARNING: search for groups not available");
		}
		else {
			throw new Exception("Unrecognised parameter value for what.collect: " + whatCollect);
		}
	
	}

	private void setSourceParams() {
		setSources();
		
		if (! sources.isEmpty()) {
			StringBuffer sb = new StringBuffer();
			for (String source : sources) {
				sb.append("src[]=" + source.trim() + "&");
			}
			String query = sb.substring(6, sb.length()-1);
			searchParams.put("src[]", query);
		}
	}

	protected void setLocationViaAPI() {
		//l			location string (city and state/province). Required if search type is "events"
		String location = "";
		
		if (! isEmpty(locationCity)) {
			locationCity = locationCity.trim();
			location += locationCity;
		}
		
		if (! isEmpty(locationRegion)) {
			locationRegion = locationRegion.trim();
			if (isEmpty(locationCity)) {
				location += locationRegion;
			}
			else {
				if (! locationRegion.equals(locationCity))
					location += "," + locationRegion;
			}
		}
		
		//TODO: use country?
		searchParams.put("l", location);
		System.out.println("Added l=" + location);
	}

	private String buildPostsQueryString() throws Exception {
		String q = "";
		
		if (! isEmpty(whatWordsAll))
			q += whatWordsAll.trim() + " ";
		
		if (q.endsWith(" "))
			q = q.substring(0, q.length() - 1);
		
		if (q.equals(""))
			throw new Exception("No query parameters defined");
		
		return q;
	}

	private String formatTimeSince(String datesSince) throws Exception {
		String timeSince = "";
		
        SimpleDateFormat incomingDateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("en", "GB"));
        incomingDateFormat.setLenient(false);
        
		try {
			Date now = new Date();
			Date dateSince = incomingDateFormat.parse(datesSince);
			
			long sinceSecs = (now.getTime() - dateSince.getTime()) / 1000;
			
			if (sinceSecs <= 0)
				throw new Exception("Illegal since date: " + datesSince);

			timeSince += sinceSecs;
			
			
		} catch (ParseException e) {
			e.printStackTrace(System.out);
		}
		
		System.out.println("Dates since: " + datesSince + " formatted as " + timeSince + " secs");

		return timeSince;
	}

	@Override
	protected void extractPosts(JSON json) throws Exception {
        if (! (json instanceof JSONObject))
        	throw new Exception("Unexpected JSON type for response: " + json.getClass());

        
        System.out.println("\nParsing JSONObject");
        String countStr = ((JSONObject)json).getString("count");
        JSONArray results = (JSONArray) ((JSONObject)json).get("items");
        System.out.println("Count: " + countStr);
        System.out.println("Number of results: " + results.size());
        
        int maxResults = results.size();
        
        if (! isEmpty(resultsMax)) {
        	int resultsMaxInt = Integer.parseInt(resultsMax);
        	if (resultsMaxInt < results.size()) {
        		maxResults = resultsMaxInt;
                System.out.println("Setting max results: " + resultsMaxInt);
        	}
        }
        
        nResults = 0;
        
        for (int i = 0; (i < results.size() && (nResults < maxResults)); i++) {
        	JSONObject object = JSONObject.fromObject(results.get(i));
        	WegovPostItem postItem = jsonToPost(object);
        	if (postItem != null) {
        		if (! isEmpty(postItem.getContent())) {
        			System.out.println("Adding post: " + postItem.toString());
        			tool.getCoordinator().getDataSchema().insertObject(postItem);
        			getOrCreateSNS(object);
        			nResults++;
        		}
        		else {
        			System.out.println("Skipping post (content is empty): " + postItem.toString());
        		}
        	}
        }
	}

	private boolean isSelectedSource(String source) {
		String normalizedSourceID = source.replaceAll(" ", "_");
		
		if (sources.contains(normalizedSourceID)) {
			return true;
		}
		else {
			System.out.println("WARNING: post has unexpected source: " + source + " (ignoring post)");
			return false;
		}
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
		
		/* Example
        {
      "title": "Greek govt to submit public sector pay cut bill",
      "description": "The Greek government will submit a bill on Thursday suspending thousands of civil servants and cutting public sector salaries as it pushes ahead with harsher austerity measures to stave off a disastrous default.",
      "link": "http://biz.yahoo.com/ap/111006/eu_greece_financial_crisis.html?.v=2",
      "timestamp": 1317898320,
      "image": null,
      "embed": null,
      "language": null,
      "user": null,
      "user_image": null,
      "user_link": null,
      "user_id": null,
      "geo": null,
      "source": "Yahoo News",
      "favicon": "http://yahoo.com/favicon.ico",
      "type": "news",
      "domain": "news.search.yahoo.com",
      "id": "15752264245552736086"
    },
		*/
		
		String postID = getPostIdFromPost(object);
		String hostSnsID = normalizeSourceID(object.getString("source"), false); // e.g. "Yahoo News" -> yahoo news
		if (! isSelectedSource(hostSnsID)) {
			return null;
		}
		
		String userID = createPostUser(object);
		//String userScreenName = object.getString("user");
		Timestamp dateCreated = getTimestamp(object.getString("timestamp")); // e.g. "1317898320"
		String originalPostURL = object.getString("link");
		short isRetweet = -1; //indicates unknown
		int outputOfRunID = new Integer(tool.getMyRunId());

		String title = null;
		String content = null;
		
		String description = object.getString("description");
		
		if (isEmpty(description)) {
			title = null;
			content = object.getString("title"); // N.B. This is required for Twitter (and other microblog) posts, whose content is actually in the title!
		}
		else {
			title = object.getString("title");
			content = description;
		}

		String toUserID = null; // This info is not available
		String replyToPostID = null; // This info is not available
		int likes = -1; // This info is not available

		return new WegovPostItem(postID, userID, hostSnsID, dateCreated, title, content, originalPostURL, isRetweet, toUserID, replyToPostID, likes, collectionDate, outputOfRunID);
	}

	private String createPostUser(JSONObject object) throws Exception {
		String userID = getAccountIdFromPost(object);
		System.out.println("userID: " + userID);
		getOrCreateUserAccount(userID, object);

		return userID;
	}
	
	@Override
	protected String getPostIdFromPost(JSONObject object) {
		return object.getString("id");
	}

	protected String getUserIdFromPost(JSONObject object) {
		return object.getString("user_id");
	}

	protected String getUserDomainFromPost(JSONObject object) {
		String domain = null;
		String userID = getUserIdFromPost(object);

		if (isEmpty(userID)) {
			String originalPostURL = object.getString("link");
			domain = getDomainFromLink(originalPostURL);
		}
		else {
			domain = normalizeSourceID(object.getString("source"), true); // remove spaces, etc
		}

		return domain;
	}

	private String getDomainFromLink(String originalPostURL) {
		String domain = null;
		String[] split1 = originalPostURL.split("//", 2);
		
		System.out.println("originalPostURL: " + originalPostURL);
		
		if (split1.length == 2) {
			domain = split1[1];
			
			String[] split2 = domain.split("/");
			domain = split2[0];
		}
		else {
			domain = split1[0];
		}

		System.out.println("domain: " + domain);
		return domain;
	}

	private void getOrCreateUserAccount(String accountID, JSONObject object) throws Exception {
		int userIDmaxLength = 60;
		String userID = accountID;

		if (userID.length() > userIDmaxLength) {
			System.out.println("WARNING: userID > " + userIDmaxLength + " chars: " + userID);
			userID = userID.substring(0, userIDmaxLength);
			System.out.println("Truncated to: " + userID);
		}

		JSONObject userObject = object;
		userObject.put("user_id", accountID);
		storeUser(userObject);
	}
	
	protected void storeUser(JSONObject jsonObject) throws Exception {
		// Not required, as JSON is already stored in post result
		//if (storeResultsAsRawJson) {
		//	usersJson.add(jsonObject);
		//}
			
		if (storeResultsAsStructuredData) {
        	WegovSnsUserAccount userItem = jsonToUser(jsonObject);
        	if (printDataObjects) System.out.println("Adding user: " + userItem.toString());
        	// Should we always create new user account entry in db, or create/update if necessary?
        	tool.getCoordinator().getDataSchema().insertObject(userItem);
        	//getOrCreateUserAccount(userItem.getCount_ID(), object);
		}
	}

	@Override
	protected WegovSnsUserAccount jsonToUser(JSONObject object) throws Exception {
		WegovSnsUserAccount userAccount = null;
		String userID = getUserIdFromPost(object);
		
		// If user account does not already exist in the database, create it			
		if (tool.getCoordinator().getDataSchema().getAllWhere(new WegovSnsUserAccount(), "ID", userID).isEmpty()) {
			System.out.println("Creating user account: " + userID);
			
			String fullName = ""; 
			String location = object.getString("geo");
			String url = "";
			String originalPostURL = object.getString("link");
			String domain = getDomainFromLink(originalPostURL);

			String profileUrl = object.getString("user_link");
			if (isEmpty(profileUrl)) {
				profileUrl = "http://" + domain + "/";
			}
			
			String profilePictureUrl = object.getString("user_image");
			if (isEmpty(profilePictureUrl)) {
				profilePictureUrl = "http://" + domain + "/favicon.ico";
			}

			String screenName = object.getString("user");
			if (isEmpty(screenName)) {
				screenName = "unknown";
			}
			else if (screenName.length() > 256) {
				System.out.println("WARNING: screen name > 256 chars: " + screenName);
				screenName = screenName.substring(0, 256);
				System.out.println("Truncated to: " + screenName);
			}

			String hostSnsID = normalizeSourceID(object.getString("source"), false);
			Timestamp dateCreated = null;
			int outputOfRunID = new Integer(tool.getMyRunId());

			int followersCount = -1; // not available
			int listedCount = -1; // not available
			int postsCount = -1; // not available
			int followingCount = -1; // not available
			
			userAccount = new WegovSnsUserAccount(userID, fullName, location, url, profileUrl, profilePictureUrl, hostSnsID, dateCreated, screenName, followersCount, listedCount, postsCount, followingCount, collectionDate, outputOfRunID);
			System.out.println("Adding account: " + userAccount.toString());
			tool.getCoordinator().getDataSchema().insertObject(userAccount);
		}
		else {
			System.out.println("User account: " + userID + " already exists");
		}
		
		return userAccount;
	}

	private String normalizeSourceID(String sourceID, boolean removeSpaces) {
		String normalizedSourceID = sourceID.toLowerCase();
		if (removeSpaces) {
			normalizedSourceID.replaceAll(" ", "");
		}
		return normalizedSourceID; // Leave in any spaces
	}

	private Timestamp getTimestamp(String timeStr) {
		Timestamp timestamp = null;
		long msecs;

		try {
			if (isEmpty(timeStr)) {
				System.out.println("WARNING: timestamp is empty");
			}
			else if (timeStr.equals("false")) {
				System.out.println("WARNING: timestamp = false");
			}
			else {
				msecs = Long.parseLong(timeStr) * 1000;
				timestamp = new Timestamp(msecs);
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}

		return timestamp;
	}

	private void getOrCreateSNS(JSONObject object) throws Exception {
		String snsID = normalizeSourceID(object.getString("source"), false); // e.g. "Yahoo News" -> yahoo news
		
		// First, check if SNS is in the current cache
		if (snsMap.containsKey(snsID)) {
			System.out.println("SNS: " + snsID + " already cached");
			return;
		}
		
		// If user SNS does not already exist in the database, create it
		ArrayList dbEntries = tool.getCoordinator().getDataSchema().getAllWhere(new WegovSNS(), "ID", snsID);
		if (dbEntries.isEmpty()) {
			System.out.println("Creating SNS: " + snsID);
			
			String snsName = object.getString("source");
			String favicon = object.getString("favicon");
			String apiUrl = favicon.replace("favicon.ico", "");
			int outputOfRunID = new Integer(tool.getMyRunId());
			
			WegovSNS newSNS = new WegovSNS(snsID, snsName, apiUrl, favicon, outputOfRunID);			
			System.out.println("Adding SNS: " + newSNS.toString());
			tool.getCoordinator().getDataSchema().insertObject(newSNS);
			
			snsMap.put(snsID, newSNS);
		}
		else {
			//System.out.println("SNS: " + snsID + " already exists in database");
			snsMap.put(snsID, (WegovSNS) dbEntries.get(0));
		}
	}

	@Override
	protected void setSNS() throws Exception {
		int outputOfRunID = new Integer(tool.getMyRunId());
		sns = getOrCreateSNS("socialmention", "SocialMention", "http://www.socialmention.com/", "http://www.socialmention.com/favicon.ico", outputOfRunID);
	}

	@Override
	protected String getSearchType() {
		return "posts-socialmention";
	}

}
