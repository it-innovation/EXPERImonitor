package eu.wegov.tools.searchandanalysis;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import eu.wegov.coordinator.dao.data.WegovPostItem;
import eu.wegov.coordinator.dao.data.WegovSnsUserAccount;
import eu.wegov.tools.WegovTool;

public class TwitterStreamSearch extends TwitterSearch {

	public TwitterStreamSearch(WegovTool wegovTool) throws Exception {
		super(wegovTool);
	}
	
	private int nResults;
	//private int maxResults;
	private long maxCollectionTime;
	private boolean limitExecutionTime;
	
	private Date streamStartTime;
	private String[] keywords;

	@Override
	public void setupSearch() throws Exception {
		System.out.println("Setting up new Twitter stream search");

		limitExecutionTime = false;
		
		if (! isEmpty(resultsMaxCollectionTimeOption) && (resultsMaxCollectionTimeOption.equals("limited"))) {
			if (! isEmpty(resultsMaxCollectionTime)) {
				maxCollectionTime = Integer.parseInt(resultsMaxCollectionTime) * 1000;
				limitExecutionTime = true;
			}
		}
		
		System.out.println("Max results: " + resultsMaxInt);
		System.out.println("Max collection time: " + resultsMaxCollectionTime + " s (" + maxCollectionTime + " ms)");
		
		if (whatCollect.equals("posts")) {
			/*
			searchUrl = "http://search.twitter.com/search.json";
			
			// Location
			setLocationParams();
			
			searchParams.put("q", buildPostsQueryString());
			//searchParams.put("result_type", "recent");
			if (! resultsMaxPerPage.equals(""))
				searchParams.put("rpp", resultsMaxPerPage);
			*/
			setKeywords();
		}
		else if (whatCollect.equals("users")) {
			System.out.println("WARNING: users search not available for Twitter strem");
		}
		else if (whatCollect.equals("groups")) {
			System.out.println("WARNING: cannot search for groups on Twitter");
		}
		else {
			throw new Exception("Unrecognised parameter value for what.collect: " + whatCollect);
		}
	}

	private void setKeywords() {
		ArrayList<String> keywordsList = new ArrayList<String>();
		if (! isEmpty(whatWordsAll)) {
			keywordsList.add(new String(whatWordsAll));
		}
		keywords = keywordsList.toArray(new String[]{});
	}

	protected void submitRequestAndHandleResponse() throws Exception {
		collectionDate = new Timestamp(new Date().getTime());
		//String requestUrl = generateRequestUrl();
		System.out.println("\nSearch site: " + site);
		//System.out.println(  "Search URL : " + requestUrl + "\n");		
		
		if (keywords.length == 0) {
			throw new Exception("No keywords defined for stream");
		}

		tool.reportMessage(sns.getSNS() + ": streaming search started");
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		
		cb.setDebugEnabled(true);
		
		cb.setOAuthConsumerKey(oauthConsumerKey)
		  .setOAuthConsumerSecret(oauthConsumerSecret)
		  .setOAuthAccessToken(oauthConsumerAccessToken)
		  .setOAuthAccessTokenSecret(oauthConsumerAccessTokenSecret);
		
		Configuration conf = cb.build();

		TwitterStream twitterStream = new TwitterStreamFactory(conf).getInstance();
    	
        StatusListener listener = new StatusListener() {
            public void onStatus(Status status) {
            	statusReceived(status);
            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
        twitterStream.addListener(listener);
        
        FilterQuery filterQuery = new FilterQuery();
		filterQuery.track(keywords);
		
		System.out.println("Starting filter");
		streamStartTime = new Date(); // set start time
		twitterStream.filter(filterQuery);

		collectResults();
		
		//twitterStream.shutdown();
	}

	
	private synchronized void collectResults() {
		System.out.println("Waiting for results...");
		while(continueCollection()) {
			try {
				wait(1000);
			} catch (InterruptedException e) {
				System.out.println("Stream interrupted");
			}
		}
		
	}
	
	private synchronized boolean continueCollection() {
		//System.out.println("Number of results: " + nResults);
		
		Date now = new Date();
		long executionTime = now.getTime() - streamStartTime.getTime();
		//System.out.println("Total execution time: " + executionTime/1000);
		
		if (limitExecutionTime) {
			if ( (executionTime) > maxCollectionTime) {
				System.out.println("Exceeded max collection time: " + maxCollectionTime);
				System.out.println("Number of results: " + nResults);
				return false;
			}
		}
		
		if (limitResults) {
			if (nResults < resultsMaxInt) {
				return true;
			}
			else {
				System.out.println("Collected enough results");
				return false;
			}
		}
		
		return true;
	}

	protected synchronized void statusReceived(Status status) {
        try {
			System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
			storePost(status);
			nResults++;
			System.out.println("Number of results: " + nResults);
		} catch (Exception e) {
			e.printStackTrace();
			//TODO: report error and check for this in continueCollection?
		}
		finally {
			notifyAll();			
		}
	}

	private void storePost(Status status) throws Exception {
    	WegovPostItem postItem = statusToPost(status);
		System.out.println("Adding post: " + postItem.toString());
    	tool.getCoordinator().getDataSchema().insertObject(postItem);
    	getOrCreateUserAccount(postItem.getAuthor_SnsUserAccount_ID(), status.getUser());
	}

	public WegovPostItem statusToPost(Status status) {
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

}
