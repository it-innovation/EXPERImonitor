/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2012
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
//	Created Date :			2012-02-16
//	Created for Project :	WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.tools.searchandanalysis;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.WegovFacebookClient;
import com.restfb.json.JsonArray;
import com.restfb.json.JsonObject;
import com.restfb.types.CategorizedFacebookType;
import com.restfb.types.Post;
import com.restfb.util.DateUtils;

import eu.wegov.coordinator.dao.data.WegovPostItem;
import eu.wegov.coordinator.dao.data.WegovSnsUserAccount;
import eu.wegov.coordinator.dao.data.WegovWidgetDataAsJson;
import eu.wegov.tools.WegovTool;
import net.sf.json.JSONArray;

public class FacebookGroupPostsSearch extends SingleSiteSearch {

	private FacebookClient facebookClient;
	protected String groupID; //e.g. "24378370318"
	protected String pages; //e.g. "single" or "all"
	private String sinceId;
	//private Date sinceDate;

	private JsonArray resultsJson = new JsonArray();
	private JsonArray usersJson = new JsonArray();
	private JsonArray commentsJson = new JsonArray();
	private ArrayList<String> postIds = new ArrayList<String>();
	private Timestamp sinceTs;
	private String sinceTimestamp;
	private String untilTimestamp;
	private boolean collectCommentsForPosts;
	private int maxPostsToCollectCommentsFor;
	private String minCommentId = null;
	private String maxCommentId = null;
	private String minCommentCreatedTime = null;
	private String maxCommentCreatedTime = null;
	private int nComments;
	private String searchType;
	private Map<String, Timestamp> latestTsForPostsComments;
	private int nPosts = 0;

	public FacebookGroupPostsSearch(WegovTool tool) throws Exception {
		super(tool, "facebook");
		getFacebookClient();
	}

	@Override
	protected void setSNS() throws Exception {
		int outputOfRunID = new Integer(tool.getMyRunId());
		sns = getOrCreateSNS("facebook", "Facebook", "http://www.facebook.com/", "http://www.facebook.com/favicon.ico", outputOfRunID);
	}

	@Override
	protected void setAuthMethod() {
		authMethod = "access_token";
	}

	@Override
	protected void setSearchParams() throws Exception {
		//super.setSearchParams();
		whatCollect = getValueOfParameter("what.collect");
		collectCommentsForPosts = getBooleanValueOfParameter("collectComments");
		String maxPostsToCollectCommentsForStr = getValueOfParameter("maxPostsToCollectCommentsFor");
		if (! isEmpty(maxPostsToCollectCommentsForStr)) {
			maxPostsToCollectCommentsFor = Integer.parseInt(maxPostsToCollectCommentsForStr);
		}

		groupID = getValueOfParameter("group.id");
		if (isEmpty(groupID)) throw new Exception("No group.id parameter defined");

		pages = getValueOfParameter("pages");

		resultsMaxOption = getValueOfParameter("results.max.results.option");
		resultsMax = getValueOfParameter("results.max.results");
		resultsMaxPerPage = getValueOfParameter("results.max.per.page");
		resultsMaxPages = getValueOfParameter("results.max.pages");
		resultsStoreInDB = getValueOfParameter("results.storage.storeindb");
		resultsKeepRawData = getValueOfParameter("results.storage.keeprawdata");

		//collectResultsSinceLastActivityRun = getBooleanValueOfParameter("results.collect.since.last.run");
		collectResultsSinceLastActivityRun = true;

		System.out.println("whatCollect: " + whatCollect);
		System.out.println("collectCommentsForPosts: " + collectCommentsForPosts);
		System.out.println("maxPostsToCollectCommentsFor: " + maxPostsToCollectCommentsFor);
		System.out.println("groupID: " + groupID);
		System.out.println("pages: " + pages);
		System.out.println("accessToken: " + accessToken);

		System.out.println("resultsMaxOption: " + resultsMaxOption);
		System.out.println("resultsMax: " + resultsMax);
		System.out.println("resultsMaxPerPage: " + resultsMaxPerPage);
		System.out.println("resultsMaxPages: " + resultsMaxPages);
		System.out.println("resultsStoreInDB: " + resultsStoreInDB);
		System.out.println("resultsKeepRawData: " + resultsKeepRawData);
		System.out.println("collectResultsSinceLastActivityRun: " + collectResultsSinceLastActivityRun);

		System.out.println();

		setResultsOptions();
	}

	public void setupSearch() throws Exception {
		if (collectResultsSinceLastActivityRun) {
			String searchType = getSearchType();

			//String id = getLatestPostIdFromPreviousRun();
			WegovWidgetDataAsJson prevResults = getResultsFromPreviousRun();
			String id = null;
			Timestamp ts = null;

			if (prevResults != null) {
				id = prevResults.getMaxId();
				ts = prevResults.getMaxTimestamp();
			}
			//System.out.println("Last post from previous run: " + id + " for search type: " + searchType);

			if (id != null) {
				if (searchType.equals("comments-facebook")) {
					String[] idFrags = id.split("_",3);
					String postId = idFrags[0] + "_" + idFrags[1];
					if (! postId.equals(groupID)) {
						throw new Exception("post id for previous run " + postId + " does not match current search id " + groupID);
					}
				}
				else {
					String[] idFrags = id.split("_",2);
					if (! idFrags[0].equals(groupID)) {
						throw new Exception("group id for previous run " + idFrags[0] + " does not match current group id " + groupID);
					}
				}

				sinceId = id;
				System.out.println("Since id: " + sinceId);

				//sinceDate = getPostCreatedTime(id);
				//System.out.println("Since date: " + sinceDate);

//				sinceTs = ts;
//				sinceTimestamp = formatTimestamp(ts);
//				System.out.println("Since timestamp: " + sinceTimestamp + " (" + ts.toString() + ")");
//				//searchParams.put("since", sinceTimestamp); // use Parameter instead

				latestTsForPostsComments = tool.getCoordinator().getLatestTimestampsForPostComments(tool.getActivity().getID());
			}
			else {
				latestTsForPostsComments = new HashMap<String, Timestamp>();
				collectResultsSinceLastActivityRun = false;
			}
		}
	}

	@Override
	protected void extractPosts(JSON json) throws Exception {
	}

	@Override
	protected void extractUsers(JSON json) throws Exception {
	}

	@Override
	protected void extractGroups(JSON json) throws Exception {
	}

	@Override
	protected WegovPostItem jsonToPost(JSONObject object) throws Exception {
		return null;
	}

	@Override
	protected WegovSnsUserAccount jsonToUser(JSONObject object) throws Exception {
		return null;
	}

	@Override
	protected String getUserIdFromPost(JSONObject object) {
		return null;
	}

	@Override
	protected String getPostIdFromPost(JSONObject object) {
		return null;
	}

	@Override
	protected void setLocationViaAPI() {
	}

	@Override
	protected void submitRequestAndHandleResponse() throws Exception {
		collectionDate = new Timestamp(new Date().getTime());

		System.out.println("\nSearch site: " + site);
		//tool.reportMessage(sns.getSNS() + ": search started");
		tool.reportMessage("Search started");

		getFacebookGroupPosts();
	}

	protected Timestamp getMinTs() {
		return getTimestamp(minTsStr);
	}

	protected Timestamp getMaxTs() {
		return getTimestamp(maxTsStr);
	}

	/*
	private Date getPostCreatedTime(String id) {
		Post post = getFacebookPost(id);
		Date timestamp = post.getCreatedTime();
		return timestamp;
	}
	*/

	private Post getFacebookPost(String id) {
		Post post = getFacebookClient().fetchObject(id, Post.class);
		return post;
	}

	private FacebookClient getFacebookClient() {
		if (facebookClient == null) {
			System.out.println("Creating new WegovFacebookClient with access token: " + accessToken);
			facebookClient = new WegovFacebookClient(accessToken);
		}
		return facebookClient;
	}

	private void getFacebookGroupPosts() throws Exception {
		getFacebookGroupPosts("full-data");

		if (searchType.equals("posts-facebook")) {
			if (collectCommentsForPosts) {
				nPosts = nResults;
				getLatestPostIds();
				getFacebookPostsComments();
			}
		}
	}

	private void getFacebookGroupPosts(String mode) throws Exception {
		System.out.println("getFacebookGroupPosts: mode = " + mode);
		query = groupID;

		String connectionType;

		String searchType = getSearchType();

		if (searchType.equals("posts-facebook")) {
			connectionType = "feed";
		}
		else if (searchType.equals("comments-facebook")) {
			connectionType = "comments";
		}
		else {
			throw new Exception("Unsupported search type: " + searchType);
		}

		//Connection<Post> myFeed = facebookClient.fetchConnection(groupID + "/feed", Post.class);
		Connection<JsonObject> myFeed;
		if (sinceTimestamp != null) {
//			myFeed = facebookClient.fetchConnection(groupID + "/" + connectionType, JsonObject.class, Parameter.with("limit", resultsMaxPerPageInt), Parameter.with("since", sinceTimestamp));
			myFeed = facebookClient.fetchConnection(groupID + "/" + connectionType, JsonObject.class, Parameter.with("limit", resultsMaxPerPageInt));
			pages = "single"; // Just collect a single page of results, to avoid paging back through older posts than since time
		}
		else if (untilTimestamp != null) {
//			myFeed = facebookClient.fetchConnection(groupID + "/" + connectionType, JsonObject.class, Parameter.with("limit", resultsMaxPerPageInt), Parameter.with("until", untilTimestamp));
			myFeed = facebookClient.fetchConnection(groupID + "/" + connectionType, JsonObject.class, Parameter.with("limit", resultsMaxPerPageInt));
		}
		else {
			myFeed = facebookClient.fetchConnection(groupID + "/" + connectionType, JsonObject.class, Parameter.with("limit", resultsMaxPerPageInt));
		}
		System.out.println(myFeed.toString());

		boolean getAllPages = false;
		int maxPages = Integer.MAX_VALUE; // default number of pages
		nResults = 0;

		if (! isEmpty(pages)) {
			if (pages.equals("all")) {
				getAllPages = true;
				maxPages = Integer.MAX_VALUE;
				System.out.println("Getting all pages");
			}
			else if (pages.equals("single")) {
				getAllPages = false;
				maxPages = 1;
				System.out.println("Getting single page");
			}
		}

		if (resultsMaxPagesInt > 0) {
			maxPages = resultsMaxPagesInt;
		}

		if (maxPages == Integer.MAX_VALUE) {
			System.out.println("WARNING: not limiting number of pages to collect\n");
		}
		else {
			System.out.println("Collecting " + maxPages + " pages");
		}

		int nPage = 0;
		boolean getFurtherPages = true;

		if (getAllPages) {
			//for (List<Post> myFeedConnectionPage : myFeed) {
			for (List<JsonObject> myFeedConnectionPage : myFeed) {
				if (nPage >= maxPages)
					break;

				getFurtherPages = extractPageData(myFeedConnectionPage);

				if (! getFurtherPages) {
					System.out.println("Not collecting any further pages");
					break;
				}

	        	if (limitResults && (nResults >= resultsMaxInt)) {
	        		System.out.println("Reached max number of results: " + resultsMaxInt);
	        		break;
	        	}

				System.out.println("Page: " + nPage + ", Posts: " + myFeedConnectionPage.size() + ", Total: " + nResults);

	        	nPage++;
			}
		}
		else { // get this page only
			//List<Post> data = myFeed.getData();
			List<JsonObject> data = myFeed.getData();
			if (mode.equals("ids")) {
				extractPostIds(data);
			}
			else {
				extractPageData(data);
			}
		}

		/*
		// Now get comments for posts (if requested)
		if (searchType.equals("posts-facebook")) {
			if (collectCommentsForPosts) {
				getFacebookPostsComments(mode);
			}
		}
		*/

		return;
	}

	private void getLatestPostIds() throws Exception {
		untilTimestamp = null;
		System.out.println("\nGetting post ids for latest" + maxPostsToCollectCommentsFor + " posts");
		limitResults = true;
		pages = "single";
		resultsMaxPagesInt = 1;
		resultsMaxInt  = maxPostsToCollectCommentsFor;
		resultsMaxPerPageInt = maxPostsToCollectCommentsFor;
		sinceTimestamp = null;

		postIds = new ArrayList<String>(); // reset results
		getFacebookGroupPosts("ids"); // get post ids only
	}

	private void getFacebookPostsComments() throws Exception {

		System.out.println("\nCollecting latest comments for " + postIds.size() + " posts\n");
		
		int totalComments = 0;

		for (String postId : postIds) {
			try {
				JsonObject comments = getFacebookPostComments(postId);
				int nComments = comments.getInt("nComments");
				if (nComments == 0)
					continue;

				totalComments += nComments;
				
				//tool.reportMessage(sns.getSNS() + ": collected " + nPosts + " posts, " + totalComments + " comments");
				tool.reportMessage("Collected " + nPosts + " posts, " + totalComments + " comments");
				
				JsonObject commentsForPost = new JsonObject();
				commentsForPost.put("postId", postId);
				commentsForPost.put("comments", comments);

				commentsJson.put(commentsForPost);
			} catch (Exception e) {
				System.out.println("ERROR: could not get comments for post: " + postId);
				e.printStackTrace(System.out);
			}
		}

		/*
		if (mode.equals("ids"))
			// Here we are already in process of getting older group posts, i.e. we only wanted the comments above
			return;

		//System.out.println("\nDetermining older posts to collect comments for...");
		//System.out.println("New posts collected = " + nResults);
		//System.out.println("Max posts for comments collection = " + maxPostsToCollectCommentsFor);

		//if (nResults >= maxPostsToCollectCommentsFor) {
		//	System.out.println("Not collecting comments for any older posts");
		//}
		//else {
			untilTimestamp = null;
			//int numAdditionalPosts = maxPostsToCollectCommentsFor - nResults;
			int numAdditionalPosts = maxPostsToCollectCommentsFor;
			//if (nResults > 0) {
			//	JsonObject oldestPost = resultsJson.getJsonObject(nResults - 1);
			//	String postId = oldestPost.getString("id");
			//	String createdTime = oldestPost.getString("created_time");
			//	untilTimestamp = createdTime;
			//	System.out.println("\nCollecting comments for " + numAdditionalPosts + " older posts before " + untilTimestamp + " (postid " + postId + ")");
			//}
			//else {
				System.out.println("\nCollecting comments for " + numAdditionalPosts + " latest posts");
			//}
			limitResults = true;
			pages = "single";
			resultsMaxPagesInt = 1;
			resultsMaxInt  = numAdditionalPosts;
			resultsMaxPerPageInt = numAdditionalPosts;
			sinceTimestamp = null;

			postIds = new ArrayList<String>(); // reset results
			getFacebookGroupPosts("ids"); // get post ids only

			System.out.println();
		//}
		*/
	}

	private JsonObject getFacebookPostComments(String postID) throws Exception {
		//query = groupID;

		String connectionType = "comments";
		int commentsMaxPerPageInt = 1000;

		Connection<JsonObject> myFeed;
		if (latestTsForPostsComments.containsKey(postID)) {
			Timestamp ts = latestTsForPostsComments.get(postID);
			String commentsSinceTimestamp = formatTimestamp(ts);
			System.out.println("\nCollecting comments for post " + postID + " since " + commentsSinceTimestamp + " (" + ts.toString() + ")\n");
			myFeed = facebookClient.fetchConnection(postID + "/" + connectionType, JsonObject.class, Parameter.with("limit", commentsMaxPerPageInt), Parameter.with("since", commentsSinceTimestamp));
			pages = "single"; // Just collect a single page of results, to avoid paging back through older posts than since time
		}
		else {
			System.out.println("\nCollecting all comments for post " + postID + "\n");
			myFeed = facebookClient.fetchConnection(postID + "/" + connectionType, JsonObject.class, Parameter.with("limit", commentsMaxPerPageInt));
		}
		System.out.println(myFeed.toString());

		boolean getAllPages = false;
		int maxPages = Integer.MAX_VALUE; // default number of pages
		nComments = 0;

		JsonArray commentsJson = new JsonArray();

		minCommentId = null;
		maxCommentId = null;
		minCommentCreatedTime = null;
		maxCommentCreatedTime = null;

		if (! isEmpty(pages)) {
			if (pages.equals("all")) {
				getAllPages = true;
				maxPages = Integer.MAX_VALUE;
				System.out.println("Getting all pages");
			}
			else if (pages.equals("single")) {
				getAllPages = false;
				maxPages = 1;
				System.out.println("Getting single page");
			}
		}

		//if (resultsMaxPagesInt > 0) {
		//	maxPages = resultsMaxPagesInt;
		//}

		if (maxPages == Integer.MAX_VALUE) {
			System.out.println("WARNING: not limiting number of pages of comments to collect\n");
		}
		else {
			System.out.println("Collecting " + maxPages + " pages");
		}

		int nPage = 0;

		if (getAllPages) {
			//for (List<Post> myFeedConnectionPage : myFeed) {
			for (List<JsonObject> myFeedConnectionPage : myFeed) {
				if (nPage >= maxPages)
					break;

				extractCommentsForPageData(postID, myFeedConnectionPage, commentsJson);

				/*
	        	if (limitResults && (nComments >= resultsMaxInt)) {
	        		System.out.println("Reached max number of results: " + resultsMaxInt);
	        		break;
	        	}
	        	*/

				System.out.println("Page: " + nPage + ", Comments: " + myFeedConnectionPage.size() + ", Total: " + nComments);

	        	nPage++;
			}
		}
		else { // get this page only
			//List<Post> data = myFeed.getData();
			List<JsonObject> data = myFeed.getData();
			extractCommentsForPageData(postID, data, commentsJson);
		}

		JsonObject commentsObject = new JsonObject();
		commentsObject.put("nComments", commentsJson.length());
		commentsObject.put("minId", minCommentId);
		commentsObject.put("maxId", maxCommentId);
		commentsObject.put("minTs", minCommentCreatedTime);
		commentsObject.put("maxTs", maxCommentCreatedTime);
		commentsObject.put("data", commentsJson);

		return commentsObject;
	}

	private boolean extractPageData(List<JsonObject> posts) throws Exception {
		boolean getFurtherPages = true;
		//for (Post post : posts) {
		//	storePost(post);
		//}

		String searchType = getSearchType();

		for (JsonObject jsonPost : posts) {
			String postId = jsonPost.getString("id");
			String createdTime = jsonPost.getString("created_time");
			Timestamp createdTs = getTimestamp(createdTime);

			if ( (sinceTs != null) && (createdTs.getTime() <= sinceTs.getTime()) ) {
				System.out.println("WARNING: post timestamp earlier than since timestamp - finishing collection");
				System.out.println("Since timestamp = " + formatTimestamp(sinceTs));
				System.out.println("Post timestamp = " + formatTimestamp(createdTs));
				getFurtherPages = false;
				break;
			}

			postIds.add(postId); // add to list of post ids

			if (searchType.equals("comments-facebook")) {
	        	if (minId == null) { //i.e. first result should have earliest timestamp
	        		minId = postId;
	        		minTsStr = createdTime;
	        	}

	        	maxId = postId;
	        	maxTsStr = createdTime; // latest result should have latest timestamp
			}
			else {
	        	if (maxId == null) { //i.e. first result should have latest timestamp
	        		maxId = postId;
	        		maxTsStr = createdTime;
	        	}

	        	minId = postId; // latest result should have smallest id
	        	minTsStr = createdTime;  // latest result should have earliest timestamp
			}

        	/*
        	if (collectResultsSinceLastActivityRun && (! postIdInRange(postId)) ) {
        		System.out.println("WARNING: post id: " + postId + " <= since id: " + sinceId + " (discarding post)");
        		System.out.println("Terminating collection");
        		break;
        	}
        	*/

			//System.out.println("postId: " + postId + " minId: " + minId + " maxId: " + maxId);

			storePost(jsonPost);

        	nResults++; // increment total number of results

        	if (limitResults && (nResults >= resultsMaxInt)) {
        		System.out.println("Reached max number of results: " + resultsMaxInt);
        		break;
        	}
		}

		//tool.reportMessage(sns.getSNS() + ": collected " + nResults + " posts");
		tool.reportMessage("Collected " + nResults + " posts");

		return getFurtherPages;
	}

	private void extractPostIds(List<JsonObject> posts) throws Exception {
		for (JsonObject jsonPost : posts) {
			String postId = jsonPost.getString("id");
			System.out.println("PostId: " + postId);
			postIds.add(postId); // add to list of post ids
		}
	}

	private void extractCommentsForPageData(String postID, List<JsonObject> comments, JsonArray commentsJson) throws Exception {
		//for (Post post : posts) {
		//	storePost(post);
		//}
		for (JsonObject jsonComment : comments) {
			String commentId = jsonComment.getString("id");
			String createdTime = jsonComment.getString("created_time");

        	if (minCommentId == null) { //i.e. first result should have earliest timestamp
        		minCommentId = commentId;
        		minCommentCreatedTime = createdTime;
        	}

        	maxCommentId = commentId; // latest result should have largest timestamp
    		maxCommentCreatedTime = createdTime;

        	/*
        	if (collectResultsSinceLastActivityRun && (! postIdInRange(postId)) ) {
        		System.out.println("WARNING: post id: " + postId + " <= since id: " + sinceId + " (discarding post)");
        		System.out.println("Terminating collection");
        		break;
        	}
        	*/

			//System.out.println("commentId: " + commentId + " minId: " + minCommentId + " maxId: " + maxCommentId);

			//storeCommentForPost(postID, jsonComment);
			commentsJson.put(jsonComment);

			nComments++;

			/*
        	nResults++; // increment total number of results

        	if (limitResults && (nResults >= resultsMaxInt)) {
        		System.out.println("Reached max number of results: " + resultsMaxInt);
        		break;
        	}
        	*/
		}
	}

	/*
	private boolean postIdInRange(String id) {
		String[] idFrags = id.split("_",2);
		long postId = Long.parseLong(idFrags[1]);
		System.out.println("Comparing " + postId + " to " + sinceId);
		return (postId > sinceId);
	}
	*/

	protected int getNumResults() {
		return resultsJson.length();
	}
        
    public JsonArray getResultsJson() {
        return resultsJson;
    }        

	protected String getResultsDataAsJson() {
		JsonObject resultsData = new JsonObject();

		JsonObject postData = new JsonObject();
		postData.put("query", query);
		postData.put("data", resultsJson);

		resultsData.put("postData", postData);
		resultsData.put("userData", usersJson); //TODO: need to set usersJson

		String searchType = "";
		try {
			searchType = getSearchType();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Comments now stored separately (see storePostComments)
		//if (searchType.equals("posts-facebook") && collectCommentsForPosts) {
		//	resultsData.put("commentsData", commentsJson);
		//}

		return resultsData.toString();
	}

	private void storePost(JsonObject jsonPost) throws Exception {
		resultsJson.put(jsonPost);
	}

	private void storePost(Post post) throws Exception {
		if (storeResultsAsRawJson) {
			//TODO
		}

		if (storeResultsAsStructuredData) {
	    	WegovPostItem postItem = fbGroupPostToWegovPost(post);
			System.out.println("Adding post: " + postItem.toString());
	    	tool.getCoordinator().getDataSchema().insertObject(postItem);
	    	getOrCreateUserAccount(postItem.getAuthor_SnsUserAccount_ID(), post.getFrom());
		}
	}

	private WegovPostItem fbGroupPostToWegovPost(Post post) {
		String postID = post.getId();
		CategorizedFacebookType from = post.getFrom();
		String fromName = from.getName();
		String fromID = from.getId();
		String message = post.getMessage();
		//String picture = post.getPicture();
		//String link = post.getLink();
		//String name = post.getName();
		//String caption = post.getCaption();
		//String description = post.getDescription();
		//String icon = post.getIcon();
		//String type = post.getType();
		Date created_time = post.getCreatedTime();
		//Date updated_time = post.getUpdatedTime();
		//Comments comments = post.getComments();

		String accountID = generateAccountID(fromID, postID);
		String hostSnsID = sns.getID();
		Timestamp dateCreated = new Timestamp(created_time.getTime());
		String title = null;
		String content = message;
		String originalPostURL = "http://www.facebook.com/groups/" + groupID; // N.B. We can't access the individual group post
		short isRetweet = -1; // unavailable
		String toUserID = null;
		String replyToPostID = null;
		int outputOfRunID = new Integer(tool.getMyRunId());

		System.out.println(fromName + " (" + fromID + "): " + message);

		int likes = 0;
		Long likesCount = post.getLikesCount();
		if (likesCount != null) likes = likesCount.intValue();

		return new WegovPostItem(postID, accountID, hostSnsID, dateCreated, title, content, originalPostURL, isRetweet, toUserID, replyToPostID, likes, collectionDate, outputOfRunID);
	}

	private void getOrCreateUserAccount(String accountID, CategorizedFacebookType user) throws Exception {
		System.out.println("Creating user account: " + accountID);
		String userID = accountID;
		String fullName = user.getName();
		String location = null;
		String url = "";
		String profilePictureUrl = "https://graph.facebook.com/" + user.getId() + "/picture";
		String hostSnsID = sns.getID();
		Timestamp dateCreated = null;
		String screenName = user.getName();
		String profileUrl = "http://www.facebook.com/profile.php?id=" + user.getId();
		int outputOfRunID = new Integer(tool.getMyRunId());

		int followersCount = -1; // not available
		int listedCount = -1; // not available
		int postsCount = -1; // not available
		int followingCount = -1; // not available

		Timestamp collectionDate = new Timestamp(new Date().getTime());

		WegovSnsUserAccount userAccount = new WegovSnsUserAccount(userID, fullName, location, url, profileUrl, profilePictureUrl, hostSnsID, dateCreated, screenName, followersCount, listedCount, postsCount, followingCount, collectionDate, outputOfRunID);
		System.out.println("Adding account: " + userAccount.toString());
		tool.getCoordinator().getDataSchema().insertObject(userAccount);
	}

	@Override
	protected String getSearchType() throws Exception {
		if (searchType != null)
			return searchType;

		System.out.println("groupID = " + groupID);
		String[] idParts = groupID.split("_");

		if (idParts.length == 1) { // id is for a group or page
			searchType = "posts-facebook"; // collect posts
		}
		else if (idParts.length == 2) { // id is for a post
			searchType = "comments-facebook"; // collect comments
		}
		else {
			throw new Exception("Bad group id: " + groupID);
		}

		return searchType;
	}

	@Override
	protected int getDefaultMaxResultsPerPage() {
		return 1000;
	}

	public void storeResults() throws Exception {
		// First store posts as normal
		super.storeResults();

		String type = getSearchType();

		if (type.equals("posts-facebook")) {
			if (collectCommentsForPosts)
				storePostComments();
		}

	}

	private void storePostComments() throws Exception {
		if (storeResultsAsRawJson) {
			System.out.println("\nStoring post comments as JSON objects");

			int wsId = 0; // N/A
			int runId = Integer.parseInt(tool.getMyRunId());
			String type = "post-comments-facebook";
			String location = getLocation();
			Timestamp collected_at = collectionDate;

			for (int i=0; i<commentsJson.length(); i++) {
				JsonObject commentObj = commentsJson.getJsonObject(i);
				String postId = commentObj.getString("postId");
				JsonObject comments = commentObj.getJsonObject("comments");

				String name = postId;
				int nComments = comments.getInt("nComments");

				String minId = comments.getString("minId");
				String maxId = comments.getString("maxId");

				String minTsStr = comments.getString("minTs");
				String maxTsStr = comments.getString("maxTs");

				Timestamp minTs = getTimestamp(minTsStr);
				Timestamp maxTs = getTimestamp(maxTsStr);

				JsonArray commentsJsonArray = comments.getJsonArray("data");

				// SJT addition to put the comments into the same format as other SNS data
				JsonObject resultsData = new JsonObject();

				JsonObject postData = new JsonObject();
				postData.put("query", postId);
				postData.put("data", commentsJsonArray);

				resultsData.put("postData", postData);
				resultsData.put("userData", usersJson);

				String dataAsJson = resultsData.toString();

				System.out.println("Post: " + postId + ", " + nComments + " comments");

				tool.getCoordinator().saveRunResultsDataAsJson(wsId, runId, type, name, location, nComments, minId, maxId, minTs, maxTs, dataAsJson, collected_at);
			}
		}
	}

	private Timestamp getTimestamp(String timeStr) {
		if (timeStr == null)
			return null;

		Date date = DateUtils.toDateFromLongFormat(timeStr);
		Timestamp ts = new Timestamp(date.getTime());
		return ts;
	}

	private String formatTimestamp(Timestamp ts) {
		return Long.toString(ts.getTime()/1000);
	}
}
