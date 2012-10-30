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
//	Created By :			Steve Taylor, modifying a file by Ken Meacham
//	Created Date :			2012-07-05
//	Created for Project :	WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.tools.analysis;


import eu.wegov.common.model.RoleDistributionPoint;
import eu.wegov.common.model.BehaviourAnalysisResult;
import eu.wegov.common.model.BehaviourAnalysisUsersForRole;
import eu.wegov.common.model.KmiPost;
import eu.wegov.common.model.KmiDiscussionActivityPoint;
import eu.wegov.common.model.KmiUser;
import eu.wegov.common.model.TopicOpinionAnalysisResult;
import net.sf.json.JSONArray;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.Vector;

import uk.ac.open.kmi.analysis.Buzz.BuzzPrediction;
import uk.ac.open.kmi.analysis.DiscussionActivity.DiscussionActivity;
import uk.ac.open.kmi.analysis.DiscussionActivity.DiscussionActivityInput;
import uk.ac.open.kmi.analysis.UserRoles.UserFeatures;
import uk.ac.open.kmi.analysis.UserRoles.UserRole;
import uk.ac.open.kmi.analysis.UserRoles.UserRoleAnalysis;
import uk.ac.open.kmi.analysis.core.Post;
import uk.ac.open.kmi.analysis.core.Language;


import eu.wegov.coordinator.Activity;
import eu.wegov.coordinator.Run;
//import eu.wegov.coordinator.web.WidgetDataAsJson;
//import eu.wegov.web.security.WegovLoginService;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class WeGovBehaviourAnalysis extends WeGovAnalysis {

	private long sinceId;
  
  
  // Default to English
  private static String language = Language.ENGLISH;

  //public WegovLoginService loginService;

	public WeGovBehaviourAnalysis(WegovAnalysisTool wegovTool, String subType, JSONArray sourceRunIds, String language) throws Exception {
		super(wegovTool, "behaviour", subType, sourceRunIds);

            if(language.equals("en")){ // should be "en"
              this.language = Language.ENGLISH;
            }
            else if (language.equals("de")){ // should be "de"
              this.language = Language.GERMAN;
            }
    
	}


  @Override
  	public void execute() throws Exception {

    super.execute();
      /*
       * analysis.type
       *  analysis.type is either "topic-opinion" or "behaviour"
       * Defined in addTopicOpinionTool and addKMITool in
       * Database maintenance
       *
       * analysis.subType
       * this is either:
       *  facebook-post-comments-topics
       *  facebook-group-topics
       *  twitter-topics
       *  twitter-behaviour
       *
       */

    BehaviourAnalysisResult result = null;
    // run appropriate analysis
    if (this.subType.equals("twitter-behaviour")) {
 /*
      JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(this.inputDataAsJsonString);
      inputDataAsJSON = inputDataAsJSON.getJSONObject("postData");
      String searchQuery = inputDataAsJSON.getString("query").toLowerCase().trim();
      JSONArray posts = inputDataAsJSON.getJSONArray("results");
*/
      // The Guts
      System.out.println ("Running " + this.language + " LANGUAGE behaviour analysis");
      result = WeGovBehaviourAnalysis.doBehaviour(tool.getMyRunId(), this.inputDataAsJsonString, this.language);

    }
    else {
      throw new Exception ("Incorrect analysis subType : " + this.subType);

    }




    // store results in DB


    JSONObject resultJson = JSONObject.fromObject(result);

    String resultJsonString = resultJson.toString();

    System.out.println("FROM BEHAVIOUR ANALYSIS result JSON = " + resultJsonString);

   // store results in DB

    String type = this.type + "." + this.subType;

    Date now = new Date();
    Timestamp runTime = new Timestamp(now.getTime());

    int runId = Integer.parseInt(tool.getMyRunId());
    /*
     public int saveRunResultsDataAsJson(
     * int wsId,
     * int runId,
     * String type,
     * String name,
     * String location,
     * int nResults,
     * String minId,
     * String maxId,
     * Timestamp minTs,
     * Timestamp maxTs,
     * String dataAsJson,
     * Timestamp collected_at
     * */

    tool.getCoordinator().saveRunResultsDataAsJson(
            0, runId, type, "behaviour-analysis-output", "", 0,
            "", "", runTime, runTime, resultJsonString, runTime);

  }


public static BehaviourAnalysisUsersForRole doBehaviourRoles(String runId, String inputData, String selectedRoleName, String searchQuery) throws Exception {
          return doBehaviourRoles(runId, inputData, selectedRoleName, searchQuery, "en");
}
  
	public static BehaviourAnalysisUsersForRole doBehaviourRoles(String runId, String inputData, String selectedRoleName, String searchQuery, String language) throws Exception {

		//JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(inputData);
		//String searchQuery = inputDataAsJSON.getString("searchQuery");
		//String selectedRoleName = inputDataAsJSON.getString("selectedRoleName");

//		Activity analysisActivity = loginService.createNewAnalysisActivity("Behaviour user roles for query \"" + searchQuery + "\"");
//		Run analysisRun = loginService.createNewAnalysisRun(analysisActivity, "Role: " + selectedRoleName);

		try {
			 System.out.println("Behaviour Analysis Roles only START. Role: " + selectedRoleName + ", query: " + searchQuery + ", language = " + language);

			BehaviourAnalysisUsersForRole analysisResult = new BehaviourAnalysisUsersForRole();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"EEE MMM dd HH:mm:ss Z yyyy");

      JSONArray userDataAsJSON = (JSONArray) JSONSerializer.toJSON(inputData);

			//JSONArray userDataAsJSON = inputDataAsJSON.getJSONArray("userData");
			//analysisActivity.setStatus(Activity.STATUS_RUNNING, analysisRun);

			// USER ROLE ANALYSIS
			HashMap<String, KmiUser> userId_KmiUser = new HashMap<String, KmiUser>();
			int numUsers = userDataAsJSON.size();

			JSONObject twitterUserAsJSON;
			String createdAt;
			String description;
			Double numFavorities;
			Double numFollowers;
			Double numFriends;
			String id;
			Double numListed;
			String location;
			String name;
			String profileImageUrl;
			String screenName;
			Double numStatuses;
			String timeZone;
			String url;
			UserFeatures userFeatures;

			Vector<UserFeatures> usIn = new Vector<UserFeatures>();
			Double userPostRate;
			Date createdAtAsDate;
			Double userAccountAgeInDays;
			for (int i = 0; i < numUsers; i++) {
				userFeatures = new UserFeatures();

				twitterUserAsJSON = (JSONObject) userDataAsJSON.get(i);

				createdAt = twitterUserAsJSON.getString("created_at");
				createdAtAsDate = dateFormat.parse(createdAt);
				description = twitterUserAsJSON.getString("description");
				numFavorities = twitterUserAsJSON.getDouble("favourites_count");
				numFollowers = twitterUserAsJSON.getDouble("followers_count");
				numFriends = twitterUserAsJSON.getDouble("friends_count");
				id = twitterUserAsJSON.getString("id");
				numListed = twitterUserAsJSON.getDouble("listed_count");
				location = twitterUserAsJSON.getString("location");
				name = twitterUserAsJSON.getString("name");
				profileImageUrl = twitterUserAsJSON
						.getString("profile_image_url_https");
				screenName = twitterUserAsJSON.getString("screen_name");
				numStatuses = twitterUserAsJSON.getDouble("statuses_count");
				timeZone = twitterUserAsJSON.getString("time_zone");
				url = twitterUserAsJSON.getString("url");
				userId_KmiUser.put(id, new KmiUser(createdAt, description,
						numFavorities, numFollowers, numFriends, id, numListed,
						location, name, profileImageUrl, screenName,
						numStatuses, url, timeZone, "", ""));

				userFeatures.setUserID(id);

				userAccountAgeInDays = ((new Date()).getTime() - createdAtAsDate
						.getTime()) / (86400000.0);
				userFeatures.setAge(userAccountAgeInDays);
				userFeatures.setIndegree(numFollowers);
				userFeatures.setNumOfLists(numListed);
				userFeatures.setNumOfPosts(numStatuses);

				userPostRate = numStatuses / userAccountAgeInDays;
				userFeatures.setPostRate(userPostRate);

				userFeatures.setOutdegree(numFriends);

				Double outInRatio = numFriends / numFollowers;
				userFeatures.setOutInRatio(outInRatio);

				// System.out.println("\t- " + userFeatures.getUserID() +
				// ", age: " + userFeatures.getAge() +
				// ", indegree (num followers): " + userFeatures.getIndegree()
				// + ", numOfLists: " + userFeatures.getNumOfLists() +
				// ", numOfPosts: " + userFeatures.getNumOfPosts() +
				// ", postRate: " + userFeatures.getPostRate()
				// + ", outdegree: " + userFeatures.getOutdegree() +
				// ", outInRatio: " + userFeatures.getOutInRatio());

				usIn.add(userFeatures);
			}

			UserRoleAnalysis ura = new UserRoleAnalysis(usIn, language);

			String userRole;
			String userId;
			KmiUser userWithRole;
			ArrayList<KmiUser> usersAsArray = new ArrayList<KmiUser>();
			for (UserRole ur : ura.getUserRoles()) {
				userRole = ur.getRoleLabel();
				userId = ur.getUserID();

				userWithRole = userId_KmiUser.get(userId);
				userWithRole.setRole(userRole);

				String date = ur.getDate().toString();
//				System.out.println(ur.getUserID() + " is a " + userRole + " on " + date);

				if (userRole.equals(selectedRoleName.replaceAll(" ", "")))
					usersAsArray.add(userWithRole);
			}

			KmiUser[] users = new KmiUser[usersAsArray.size()];
			for (int i = 0; i < users.length; i++) {
				users[i] = usersAsArray.get(i);
			}
			analysisResult.setUsers(users);
//			analysisActivity.setStatus(Activity.STATUS_FINISHED, analysisRun);
			System.out.println("Behaviour Analysis Roles only SUCCESS");
			return analysisResult;
		} catch (Exception e) {
			System.out.println("Behaviour Analysis Roles only FAILED");
			e.printStackTrace();
			//analysisActivity.setStatus(Activity.STATUS_FAILED, analysisRun);
			return null;
		}
	}


	public static BehaviourAnalysisResult doBehaviour(String runId, final String inputData) throws Exception {
    return doBehaviour(runId, inputData, "en");
  }
    
  
	public static BehaviourAnalysisResult doBehaviour(String runId, final String inputData, String language) throws Exception {

//	@RequestMapping(method = RequestMethod.POST, value = "/kmi/do.json")
//	public @ResponseBody
//	BehaviourAnalysisResult doBehaviour(@RequestBody final String inputData) throws Exception {

		JSONObject inputDataAsJSON = (JSONObject) JSONSerializer
				.toJSON(inputData);
		JSONObject postDataAsJSON = inputDataAsJSON
				.getJSONObject("postData");
		JSONArray postDataAsJSONArray = postDataAsJSON
				.getJSONArray("results");
		int numPostInInput = postDataAsJSONArray.size();
		//Activity analysisActivity = loginService.createNewAnalysisActivity("Behaviour analysis for query \"" + postDataAsJSON.getString("query") + "\"");
		//Run analysisRun = loginService.createNewAnalysisRun(analysisActivity, numPostInInput + " posts analysed");

    System.out.println ("Running behaviour analysis for run ID " + runId + " in language " + language);


		try {
			// System.out.println("Here is the data for behaviour analysis:");
			// System.out.println(inputData);
			 //System.out.println("Behaviour Analysis START by " + loginService.getLoggedInUser());

			// Sun Jul 25 16:37:18 +0000 2010 - users
			// Fri, 17 Feb 2012 10:04:09 +0000 - posts
			// why you no have same date format?

			BehaviourAnalysisResult analysisResult = new BehaviourAnalysisResult();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"EEE MMM dd HH:mm:ss Z yyyy");
			SimpleDateFormat dateFormatForPosts = new SimpleDateFormat(
					"EEE, dd MMM yyyy HH:mm:ss Z"); // Why, Twitter?
			SimpleDateFormat dateFormatForJqplotGraphs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			JSONArray userDataAsJSON = inputDataAsJSON.getJSONArray("userData");
			//analysisActivity.setStatus(Activity.STATUS_RUNNING, analysisRun);

			// USER ROLE ANALYSIS
			HashMap<String, KmiUser> userId_KmiUser = new HashMap<String, KmiUser>();
			int numUsers = userDataAsJSON.size();

			JSONObject twitterUserAsJSON;
			String createdAt;
			String description;
			Double numFavorities;
			Double numFollowers;
			Double numFriends;
			String id;
			Double numListed;
			String location;
			String name;
			String profileImageUrl;
			String screenName;
			Double numStatuses;
			String timeZone;
			String url;
			UserFeatures userFeatures;

			Vector<UserFeatures> usIn = new Vector<UserFeatures>();
			Double userPostRate;
			Date createdAtAsDate;
			Double userAccountAgeInDays;
			for (int i = 0; i < numUsers; i++) {
				userFeatures = new UserFeatures();

				twitterUserAsJSON = (JSONObject) userDataAsJSON.get(i);

				createdAt = twitterUserAsJSON.getString("created_at");
				createdAtAsDate = dateFormat.parse(createdAt);
				description = twitterUserAsJSON.getString("description");
				numFavorities = twitterUserAsJSON.getDouble("favourites_count");
				numFollowers = twitterUserAsJSON.getDouble("followers_count");
				numFriends = twitterUserAsJSON.getDouble("friends_count");
				id = twitterUserAsJSON.getString("id");
				numListed = twitterUserAsJSON.getDouble("listed_count");
				location = twitterUserAsJSON.getString("location");
				name = twitterUserAsJSON.getString("name");
				profileImageUrl = twitterUserAsJSON
						.getString("profile_image_url_https");
				screenName = twitterUserAsJSON.getString("screen_name");
				numStatuses = twitterUserAsJSON.getDouble("statuses_count");
				timeZone = twitterUserAsJSON.getString("time_zone");
				url = twitterUserAsJSON.getString("url");
				userId_KmiUser.put(id, new KmiUser(createdAt, description,
						numFavorities, numFollowers, numFriends, id, numListed,
						location, name, profileImageUrl, screenName,
						numStatuses, url, timeZone, "", ""));

				userFeatures.setUserID(id);

				userAccountAgeInDays = ((new Date()).getTime() - createdAtAsDate
						.getTime()) / (86400000.0);
				userFeatures.setAge(userAccountAgeInDays);
				userFeatures.setIndegree(numFollowers);
				userFeatures.setNumOfLists(numListed);
				userFeatures.setNumOfPosts(numStatuses);

				userPostRate = numStatuses / userAccountAgeInDays;
				userFeatures.setPostRate(userPostRate);

				userFeatures.setOutdegree(numFriends);

				Double outInRatio = numFriends / numFollowers;
				userFeatures.setOutInRatio(outInRatio);

				// System.out.println("\t- " + userFeatures.getUserID() +
				// ", age: " + userFeatures.getAge() +
				// ", indegree (num followers): " + userFeatures.getIndegree()
				// + ", numOfLists: " + userFeatures.getNumOfLists() +
				// ", numOfPosts: " + userFeatures.getNumOfPosts() +
				// ", postRate: " + userFeatures.getPostRate()
				// + ", outdegree: " + userFeatures.getOutdegree() +
				// ", outInRatio: " + userFeatures.getOutInRatio());

				usIn.add(userFeatures);
			}

			UserRoleAnalysis ura = new UserRoleAnalysis(usIn, language);
			TreeMap<String, Integer> roleDistribution = new TreeMap<String, Integer>();
			Vector<String> availableRoles = listFilesInDir(
					"./data/roleClassifiers/", ".role"); // load existing role
															// labels from dir

			// Print the role descriptions
			Vector<String> rolesDescriptions = listFilesInDir(
					"./data/roleClassifiers/", ".description");
//			for (String role : rolesDescriptions) {
//				System.out.println(readStringFile("./data/roleClassifiers/"
//						+ role + ".description"));
//			}

			for (String rolelabel : availableRoles) {
				roleDistribution.put(rolelabel, 0); // initialise the map that
													// counts how many users
													// have this role
			}

			String userRole;
			String userId;
			KmiUser userWithRole;
			for (UserRole ur : ura.getUserRoles()) {
				userRole = ur.getRoleLabel();
				userId = ur.getUserID();

				userWithRole = userId_KmiUser.get(userId);
				userWithRole.setRole(userRole);

				String date = ur.getDate().toString();
//				System.out.println(ur.getUserID() + " is a " + userRole + " on " + date);

				for (String rolelabel : availableRoles) {
					if (rolelabel.equalsIgnoreCase(userRole)) {
						int current = roleDistribution.get(rolelabel);
						roleDistribution.put(rolelabel, ++current);
					}
				}
			}

//			System.out.println("\n" + roleDistribution);

			Iterator<String> it = roleDistribution.keySet().iterator();
			int numEntries = roleDistribution.keySet().size();
			String roleName;
			int roleValue;
			RoleDistributionPoint roleDistributionPoint;
			RoleDistributionPoint[] roleDistributionPoints = new RoleDistributionPoint[numEntries];
			int count = 0;
			while (it.hasNext()) {
				roleName = (String) it.next();
				roleValue = roleDistribution.get(roleName);
				roleDistributionPoint = new RoleDistributionPoint(roleName,
						roleValue);
				roleDistributionPoints[count] = roleDistributionPoint;
				count++;
			}

			// DISCUSSION ACTIVITY
			Vector<Post> poIn = new Vector<Post>();
//			System.out.println("Discussion activity");


			JSONObject twitterPostAsJSON;
			String twitterPostId, twitterPostUserId, userScreenName, userFullName, userProfileImageUrl, twitterPostCreatedAt, inReplyToID, textContent;
			double authorOutDegree, authorInDegree, authorNumLists;
			KmiUser twitterPostKmiUser;
			KmiPost twitterPostKmiPost;
			Post postForAnalysis;
			HashMap<String, KmiPost> postId_KmiPost = new HashMap<String, KmiPost>();
			for (int i = 0; i < numPostInInput; i++) {
				twitterPostAsJSON = (JSONObject) postDataAsJSONArray.get(i);
				twitterPostId = twitterPostAsJSON.getString("id");
				twitterPostUserId = twitterPostAsJSON.getString("from_user_id");
				twitterPostKmiUser = userId_KmiUser.get(twitterPostUserId);
				userScreenName = twitterPostKmiUser.getScreenName();
				userFullName = twitterPostKmiUser.getName();
				userProfileImageUrl = twitterPostAsJSON
						.getString("profile_image_url_https");
				twitterPostCreatedAt = twitterPostAsJSON
						.getString("created_at");

				inReplyToID = null;

				textContent = twitterPostAsJSON.getString("text");
				authorOutDegree = twitterPostKmiUser.getNumFriends();
				authorInDegree = twitterPostKmiUser.getNumFollowers();
				authorNumLists = twitterPostKmiUser.getNumListed();
				twitterPostKmiPost = new KmiPost(twitterPostId,
						twitterPostUserId, userScreenName, userFullName,
						userProfileImageUrl, twitterPostCreatedAt, inReplyToID,
						textContent, authorOutDegree, authorInDegree,
						authorNumLists, "");
				postId_KmiPost.put(twitterPostId, twitterPostKmiPost);

				postForAnalysis = new Post();
				postForAnalysis.setPostID(twitterPostId);
				postForAnalysis.setTextContent(textContent);
				// postForAnalysis.setDateCreated(new
				// Timestamp(dateFormat.parse(twitterPostCreatedAt).getTime()));
				// System.out.println("Processing post with createdAtAsDate: "
				// + twitterPostCreatedAt);

				createdAtAsDate = dateFormatForPosts
						.parse(twitterPostCreatedAt);

				postForAnalysis.setDateCreated(new Timestamp(createdAtAsDate
						.getTime()));
				// System.out.println("Processing post with id past timestamp: "
				// + createdAtAsDate);
				postForAnalysis.setAuthorID(twitterPostUserId);
				postForAnalysis.setAuthorInDegree(authorInDegree);
				postForAnalysis.setAuthorNumLists(authorNumLists);
				postForAnalysis.setAuthorOutDegree(authorOutDegree);

				// postForAnalysis.setInReplyToID(inReplyToID);

				poIn.add(postForAnalysis);
//				System.out.println("[" + postForAnalysis.getPostID() + "] "
//						+ postForAnalysis.getTextContent() + ", by "
//						+ postForAnalysis.getAuthorID() + ", created on "
//						+ postForAnalysis.getDateCreated().toString()
//						+ ", in reply to: " + postForAnalysis.getInReplyToID());
			}

			DiscussionActivity d = new DiscussionActivity();
			DiscussionActivityInput dInput = new DiscussionActivityInput();
			dInput.setInputPosts(poIn);
			d.setDiscussionActivityInput(dInput);

			double[] val = d.getDiscussionRate();
			long startInMsec = dInput.getStart().getTime();
			long endInMsec = dInput.getEnd().getTime();
			int drEntriesNum = val.length - 1;
            double drStep = (endInMsec - startInMsec) / drEntriesNum;
            int drStepCounter = 0;
            String timestampAsString;
            KmiDiscussionActivityPoint kmiDiscussionActivityPoint;
            KmiDiscussionActivityPoint[] kmiDiscussionActivityPoints = new KmiDiscussionActivityPoint[val.length];
			for (double du : val) {
				timestampAsString = dateFormatForJqplotGraphs.format(new Timestamp(Math.round( (startInMsec + drStep * drStepCounter ) / 1000 ) * 1000));
//				System.out.println("Discussion rate: " + du);
//				System.out.println("Discussion rate: " + du + ", time: " + timestampAsString);
				kmiDiscussionActivityPoint = new KmiDiscussionActivityPoint(timestampAsString, du);
				kmiDiscussionActivityPoints[drStepCounter] = kmiDiscussionActivityPoint;
				drStepCounter++;
			}
//			System.out.println("\n");
//			System.out.println("Most active users:");

//			for (Entry<String, Integer> entry : d.getTopKMostActiveUsr(5)) {
//				System.out.println(entry.getKey() + ": " + entry.getValue());
//			}

//			System.out.println("Most replied posts:");
//			for (Entry<String, Integer> entry : d.getTopKMostRepliedPosts(5)) {
//				System.out.println(entry.getKey() + ": " + entry.getValue());
//			}

			// Buzz!
			//BuzzPrediction buzzPrediction = new BuzzPrediction("./data/");
      BuzzPrediction buzzPrediction = new BuzzPrediction(language);
			buzzPrediction.setInputPosts(poIn);

            count = 0;
//            System.out.println("Top buzz users:");
            KmiUser[] buzzUsers = new KmiUser[5];
            for (Entry<String, Double> entry : buzzPrediction.getTopKMostBuzzUsers(5)) {
                Double score = entry.getValue();
//                System.out.println("\t- key: " + entry.getKey() + ", value: " + score);
                String scoreAsAtring = Double.toString((double) Math.round(score * 100) / 100);

                KmiUser user = userId_KmiUser.get(entry.getKey());
                user.setBuzzScore(scoreAsAtring);
                buzzUsers[count] = user;
                count++;
            }

            count = 0;
//            System.out.println("Top buzz posts:");
            KmiPost[] buzzPosts = new KmiPost[5];
            for (Entry<String, Double> entry : buzzPrediction.getTopKMostBuzzPosts(5)) {
                Double score = entry.getValue();
//                System.out.println("\t- key: " + entry.getKey() + ", value: " + score);
                String scoreAsAtring = Double.toString((double) Math.round(score * 10000) / 10000);

                KmiPost post = postId_KmiPost.get(entry.getKey());
                post.setBuzzScore(scoreAsAtring);
                buzzPosts[count] = post;
                count++;

            }

// Experimental users for role
     String userData = inputDataAsJSON.getString("userData");
            
      BehaviourAnalysisUsersForRole broadcasters =
              WeGovBehaviourAnalysis.doBehaviourRoles(runId, userData, "Broadcaster", "none");
      analysisResult.setBroadcasters(broadcasters);

      BehaviourAnalysisUsersForRole dailyUsers =
              WeGovBehaviourAnalysis.doBehaviourRoles(runId, userData, "Daily User", "none");
      analysisResult.setDailyUsers(dailyUsers);
      
      BehaviourAnalysisUsersForRole informationSeekers =
              WeGovBehaviourAnalysis.doBehaviourRoles(runId, userData, "Information Seeker", "none");
      analysisResult.setInformationSeekers(informationSeekers);

      BehaviourAnalysisUsersForRole informationSources =
              WeGovBehaviourAnalysis.doBehaviourRoles(runId, userData, "Information Source", "none");
      analysisResult.setInformationSources(informationSources);

      BehaviourAnalysisUsersForRole rarePosters =
              WeGovBehaviourAnalysis.doBehaviourRoles(runId, userData, "Rare Poster", "none");
      analysisResult.setRarePosters(rarePosters);
    
            
			// Add user role analysis to the output
			analysisResult.setRoleDistributionPoints(roleDistributionPoints);
			// Add discussion activity to the output
			analysisResult.setDiscussionActivityPoints(kmiDiscussionActivityPoints);
			// Add Buzz users (top 5 to watch) to the output
			analysisResult.setBuzzUsers(buzzUsers);
			// Add Buzz posts (top 5 to watch) to the output
			analysisResult.setBuzzPosts(buzzPosts);
			//analysisActivity.setStatus(Activity.STATUS_FINISHED, analysisRun);
			System.out.println("Behaviour Analysis SUCCESS");
			return analysisResult;
		} catch (Exception e) {
			System.out.println("Behaviour Analysis FAILED");
			e.printStackTrace();
			//analysisActivity.setStatus(Activity.STATUS_FAILED, analysisRun);
			return null;
		}
	}


	private static Vector<String> listFilesInDir(String DirPath,
			String Extension) {

		Vector<String> filenames = new Vector<String>();
		File dir = new File(DirPath);
		String[] children = dir.list();
		if (children == null || children.length == 0) {
//			System.out.println(DirPath + " is empty");
		} else {
			for (int i = 0; i < children.length; i++) {
				String filename = children[i];
				if (filename.endsWith(Extension)) {
					filenames.add(filename.replaceAll(Extension, ""));
				}
			}
		}
		return filenames;
	}


}
