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

import eu.wegov.common.model.JSONTwitterPostDetails;
import eu.wegov.common.model.JSONTwitterUserDetails;
import eu.wegov.common.model.TopicDocument;
import eu.wegov.common.model.TopicOpinionAnalysisTopic;
import eu.wegov.common.model.TopicOpinionAnalysisResult;

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

import eu.wegov.coordinator.Activity;
import eu.wegov.coordinator.Run;
import eu.wegov.coordinator.web.WidgetDataAsJson;

import eu.wegov.tools.analysis.WegovAnalysisTool;
import java.util.HashMap;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import west.importer.WegovImporter;
import west.wegovdemo.SampleInput;
import west.wegovdemo.TopicOpinionAnalysis;
import west.wegovdemo.TopicOpinionDocument;
import west.wegovdemo.TopicOpinionInput;
import west.wegovdemo.TopicOpinionOutput;
import west.wegovdemo.WegovRender;


public class WeGovTopicAnalysis extends WeGovAnalysis {

  private long sinceId;
  private int numTopicsToReturn = -1;
  private int numTermsPerTopic = -1;
  private String analysisLanguage = "en";

  public WeGovTopicAnalysis(
          WegovAnalysisTool wegovTool,
          String subType,
          JSONArray sourceRunIds,
          int numTopicsToReturn,
          int numTermsPerTopic,
          String analysisLanguage
          ) throws Exception {
    super(wegovTool, "topic-opinion", subType, sourceRunIds);

    this.numTopicsToReturn = numTopicsToReturn;
    this.numTermsPerTopic = numTermsPerTopic;
    this.analysisLanguage = analysisLanguage;

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

    TopicOpinionAnalysisResult result = null;
    // run appropriate analysis
    if (this.subType.equals("twitter-topics")) {

      JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(this.inputDataAsJsonString);
      inputDataAsJSON = inputDataAsJSON.getJSONObject("postData");
      String searchQuery = inputDataAsJSON.getString("query").toLowerCase().trim();
      JSONArray posts = inputDataAsJSON.getJSONArray("results");

      // The Guts
      result = doTopics(
              tool.getMyRunId(),
              "twitter",
              searchQuery,
              posts,
              this.numTopicsToReturn,
              this.numTermsPerTopic,
              this.analysisLanguage);

    }
    else if (this.subType.equals("facebook-group-topics")) {
       JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(this.inputDataAsJsonString);
      inputDataAsJSON = inputDataAsJSON.getJSONObject("postData");
      String searchQuery = inputDataAsJSON.getString("query").toLowerCase().trim();
      JSONArray posts = inputDataAsJSON.getJSONArray("data");

      // The Guts
      result = doTopics(
              tool.getMyRunId(),
              "facebook",
              searchQuery,
              posts,
              this.numTopicsToReturn,
              this.numTermsPerTopic,
              this.analysisLanguage);

    }
    else if (this.subType.equals("facebook-post-comments-topics")) {
      JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(this.inputDataAsJsonString);
      inputDataAsJSON = inputDataAsJSON.getJSONObject("postData");
      String searchQuery = inputDataAsJSON.getString("query").toLowerCase().trim();
      JSONArray posts = inputDataAsJSON.getJSONArray("data");

      // The Guts
      result = doTopics(
              tool.getMyRunId(),
              "facebook",
              searchQuery,
              posts,
              this.numTopicsToReturn,
              this.numTermsPerTopic,
              this.analysisLanguage);


    }
    else {
      throw new Exception ("Incorrect analysis subType : " + this.subType);

    }


    JSONObject resultJson = JSONObject.fromObject(result);

    String resultJsonString = resultJson.toString();

    System.out.println("result JSON = " + resultJsonString);

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
            0, runId, type, "topic-analysis-output", "", 0,
            "", "", runTime, runTime, resultJsonString, runTime);




  }

  public static TopicOpinionAnalysisResult doTopics(
          String runId,
          String sns,
          String searchQuery,
          JSONArray posts)  throws Exception {

    return doTopics(
          runId,
          sns,
          searchQuery,
          posts,
          -1, // numTopics: -1 means auto
          -1, // numTermsPerTopics: -1 means auto
          "en" // values are "en" (English) and "de" (German). Default value is "en".
    );
  }



  public static TopicOpinionAnalysisResult doTopics(
          final String runId,
          final String sns,
          final String searchQuery,
          final JSONArray posts,
          final int setNumTopics,
          final int setNumTermsPerTopic,
          final String language) throws Exception {
	//	Activity analysisActivity = loginService.createNewAnalysisActivity("Topic analysis for query \"" + searchQuery + "\"");
	//	Run analysisRun = loginService.createNewAnalysisRun(analysisActivity, posts.size() + " posts analysed");

    System.out.println (
            "Running topic analysis for run ID " + runId +
            " for SNS " + sns + " and search query + " + searchQuery);
		try {

      TopicOpinionAnalysisResult result = null;

      if (sns.equals("twitter") ) {
        SampleInput input = new SampleInput();
      //	System.out.println("Topic Analysis START by " + loginService.getLoggedInUser());

        HashMap<String, JSONTwitterUserDetails> userIds_jsonTwitterUserDetails
                = new HashMap<String, JSONTwitterUserDetails>();
        HashMap<String, JSONTwitterPostDetails> postIds_jsonTwitterPostDetails
                = new HashMap<String, JSONTwitterPostDetails>();

        String postContentsWithoutHttp;
        String cleanWord;
        int docId = 0;
        for (int i = 0; i < posts.size(); i++) {
          JSONObject postJSON = (JSONObject) posts.get(i);
          JSONTwitterPostDetails postDetails = new JSONTwitterPostDetails(postJSON);
          String postContents = postDetails.getText();

          postContentsWithoutHttp = "";
          for (String word : postContents.split(" ")) {
            cleanWord = word.toLowerCase().trim();
            if ( cleanWord.startsWith("http://") || cleanWord.startsWith(searchQuery.trim()) ) {
            } else {
              postContentsWithoutHttp = postContentsWithoutHttp + cleanWord + " ";
            }
          }

          postContentsWithoutHttp = postContentsWithoutHttp.trim();

          if (postContentsWithoutHttp.equals("")) {
            System.out.println("WARNING: result " + i + " is empty");
          }
          //TODO: should be able add this blank content, once Koblenz code can handle these (waiting for a fix)
          else {
            JSONTwitterUserDetails userDetails = new JSONTwitterUserDetails(postJSON);
            String userId = userDetails.getId();
            String userUrl = "https://twitter.com/" + userDetails.getScreenName();
            userDetails.setUserUrl(userUrl);
            input.add(postContentsWithoutHttp, userId);
            userIds_jsonTwitterUserDetails.put(userId, userDetails);
            //postIds_jsonTwitterPostDetails.put(Integer.toString(i), postDetails);
            postIds_jsonTwitterPostDetails.put(Integer.toString(docId), postDetails);
           // System.out.println("doc ID = " + docId + ": post ID = " + postDetails.getId() + "  contents = " + postDetails.getText());
            docId++;
          }
        }

        result = doTopicsCore(
                input,
                postIds_jsonTwitterPostDetails,
                userIds_jsonTwitterUserDetails,
                setNumTopics,
                setNumTermsPerTopic,
                language
        );

        // Finish activity
        //analysisActivity.setStatus(Activity.STATUS_FINISHED);

        System.out.println("Topic Analysis SUCCESS");

        //return result;
      }
      else if (sns.equals("facebook") ){
        SampleInput input = new SampleInput();

        //TODO: should really be JSONFacebookUserDetails, but this class does not exist yet
        HashMap<String, JSONTwitterUserDetails> userIds_jsonTwitterUserDetails
                = new HashMap<String, JSONTwitterUserDetails>();
        HashMap<String, JSONTwitterPostDetails> postIds_jsonTwitterPostDetails
                = new HashMap<String, JSONTwitterPostDetails>();
        String userId;
        String userName;
        String postId;
        String postContents;
        String postContentsWithoutHttp;
        String cleanWord;
        int docId = 0;
        for (int i = 0; i < posts.size(); i++) {
          JSONObject postJSON = (JSONObject) posts.get(i);
          postId = postJSON.getString("id");
          JSONObject fromJSON = postJSON.getJSONObject("from");
          userId = fromJSON.getString("id");
          userName = fromJSON.getString("name");
          String message = postJSON.has("message") ? postJSON.getString("message") : "";
          String description = postJSON.has("description") ? postJSON.getString("description") : "";
          postContents = message + " " + description;

          postContentsWithoutHttp = "";
          for (String word : postContents.split(" ")) {
            cleanWord = word.toLowerCase().trim();
            if ( cleanWord.startsWith("http://") ) {
            } else {
              postContentsWithoutHttp = postContentsWithoutHttp + cleanWord + " ";
            }
          }

          postContentsWithoutHttp = postContentsWithoutHttp.trim();

          if (postContentsWithoutHttp.equals("")) {
            System.out.println("WARNING: result " + i + " has no message or description content: \n" + postJSON);
          }
          else {
            
           JSONTwitterUserDetails userDetails = new JSONTwitterUserDetails(userId,
                    userName,
                    userName,
                    "https://graph.facebook.com/" + userId + "/picture"
                    );
           String userUrl = "http://www.facebook.com/" + userId;
           userDetails.setUserUrl(userUrl);
            
            input.add(postContentsWithoutHttp.trim(), userId);
            userIds_jsonTwitterUserDetails.put(
                userId,
                userDetails
/*                    
                new JSONTwitterUserDetails(userId,
                    userName,
                    userName,
                    "https://graph.facebook.com/" + userId + "/picture"
                    )*/
            );
            postIds_jsonTwitterPostDetails.put(
                //Integer.toString(i),
                Integer.toString(docId),
                new JSONTwitterPostDetails(postJSON.getString("created_time"),
                    postId,
                    postContents,
                    userId,
                    userName,
                    userName, ""));
            // }

            //System.out.println(docId + ": " + postContentsWithoutHttp);
            docId++;
          }
        }
/*
        result = WeGovTopicAnalysis.doTopicsCore(
                input, postIds_jsonTwitterPostDetails, userIds_jsonTwitterUserDetails);
*/
        result = doTopicsCore(
                input,
                postIds_jsonTwitterPostDetails,
                userIds_jsonTwitterUserDetails,
                setNumTopics,
                setNumTermsPerTopic,
                language
        );




        System.out.println("Topic Analysis SUCCESS");
        //return result;

      }
      else {
        throw new Exception ("Invalid SNS for analyis: " + sns);
      }


        return result;

		}
    catch (Exception e) {
			System.out.println("Topic Analysis FAILED");
			e.printStackTrace();
			//analysisActivity.setStatus(Activity.STATUS_FAILED, analysisRun);
			return null;
		}
	}


	public static TopicOpinionAnalysisResult doTopicsCore(
          TopicOpinionInput input,
          HashMap<String, JSONTwitterPostDetails>  postIds_jsonTwitterPostDetails,
          HashMap<String, JSONTwitterUserDetails> userIds_jsonTwitterUserDetails) {

    return doTopicsCore(
          input,
          postIds_jsonTwitterPostDetails,
          userIds_jsonTwitterUserDetails,
          -1, // -1 means auto
          -1, // -1 means auto
          "en" // values are "en" (English) and "de" (German). Default value is "en".
    );
  }


	public static TopicOpinionAnalysisResult doTopicsCore(
          TopicOpinionInput input,
          HashMap<String, JSONTwitterPostDetails>  postIds_jsonTwitterPostDetails,
          HashMap<String, JSONTwitterUserDetails> userIds_jsonTwitterUserDetails,
          int setNumTopics,
          int setNumTermsPerTopic,
          String language
  ) {

    try {

      TopicOpinionAnalysisResult result = new TopicOpinionAnalysisResult();

      // Create Topic Opinion Analysis object
      TopicOpinionAnalysis analysis = new WegovImporter();

      // Analysis parameters - how many topics
      if (setNumTopics > 0) {
        // Set number of topics to return
        analysis.setNumTopics(setNumTopics);
      }

        // Analysis parameters  - terms per topic
      if (setNumTermsPerTopic > 0) {
        // Set number of topics to return
        analysis.setTermsPerTopic(setNumTermsPerTopic);
      }

      System.out.println("Setting language to " + language);
      // Set language of analysis
      analysis.setLanguage(language);
      // language - The language of the dataset.
      // possible values of this parameter are currently "en" (English) and "de" (German).
      // Default value is "en". Any other strings supplied to this function will be ignored and interpreted as "en".


      // Run Topic Opinion Analysis and create output result
      TopicOpinionOutput output = analysis.analyzeTopicsOpinions(input);

      // Display results to stdout (for debugging)
      WegovRender.showResults(output);

      // Get number of topics
      int numTopics = output.getNumTopics();
      result.setNumTopics(numTopics);
      System.out.println("\nNumber of topics: " + numTopics);

      // Create topic results for returning to client
      TopicOpinionAnalysisTopic[] topics = new TopicOpinionAnalysisTopic[numTopics];
      TopicOpinionAnalysisTopic topic;
      StringBuilder sb;
      String[] topicTerms;
      String keyTerm;
      NumberFormat formatter =  new DecimalFormat("0.0");

      Random random = new Random();


      // This could be very big! Do this only once!!
      //Collection<TopicOpinionDocument> documents = output.getAllDocuments();
      ArrayList<TopicOpinionDocument> allDocuments = new ArrayList<TopicOpinionDocument>(output.getAllDocuments());


      // Loop through topics
      for (int topicID = 0; topicID < numTopics; topicID++) {
        String topicIdStr = String.valueOf(topicID + 1);

        // Get topic terms for this topic
        topicTerms = output.getTopicTerms(topicID);
        sb = new StringBuilder();

        for (int i = 0; i < topicTerms.length; i++) {
          keyTerm = topicTerms[i];
  //				System.out.println("\t\t- " + keyTerm);
          sb.append(keyTerm);
          if (i < topicTerms.length - 1)
            sb.append(", ");
        }

        String topicTermsString = sb.toString();
        System.out.println("\nTopic " + topicID + ": " + topicTermsString);

        // Get topic users for this topic
        String[] keyTopicUsers = output.getTopicUsers(topicID);
        int numKeyUsers = keyTopicUsers.length;

        JSONTwitterUserDetails jsonUser;
        JSONTwitterUserDetails[] keyJsonUsers = new JSONTwitterUserDetails[numKeyUsers];
        String keyTopicUserId;

        for (int i = 0; i < numKeyUsers; i++) {
          keyTopicUserId = keyTopicUsers[i];
          jsonUser = userIds_jsonTwitterUserDetails.get(keyTopicUserId);
          keyJsonUsers[i] = jsonUser;
        }

        // Get relevant doc ids for this topic
        int[] relevantDocIDs = output.getTopicRelevantDocIDs(topicID);

        // Get relevant doc scores for this topic
        double[] relevantDocScores = output.getTopicRelevantDocScores(topicID);

        JSONTwitterPostDetails[] topTopicPosts = new JSONTwitterPostDetails[relevantDocIDs.length];

        //System.out.println("Checking relevant document contents");
        for (int j = 0; j < relevantDocIDs.length; j++) {
          int relevantDocID = relevantDocIDs[j];
          String docBody = output.getDocumentBody(relevantDocID);
          String relevantDocScore = Double.toString((double) Math.round(relevantDocScores[j] * 10000) / 10000);
          JSONTwitterPostDetails thePost = postIds_jsonTwitterPostDetails.get(Integer.toString(relevantDocID));
          if (thePost == null) {
            System.out.println("WARNING: could not find post details for relevantDocID: " + relevantDocID);
          }
          else {
            //System.out.println(relevantDocID + ": " + thePost.getText().toLowerCase());
            thePost.setScore(relevantDocScore);
          }
          //System.out.println();
          //System.out.println(j + " " + relevantDocID);
          //System.out.println(relevantDocID + ": " + docBody.toLowerCase());
          topTopicPosts[j] = thePost;
        }


        // Get number of posts for topic
        // TODO: need to get this from topic analysis!!
        //int numPosts = random.nextInt(output.getAllDocuments().size());
        //String numPostsStr = String.valueOf(numPosts);


        // Get the topic posts that have a score above 0.5 for this group
        double docScoreLowerLimit = 0.5;
        ArrayList<TopicDocument> topicDocs = getTopicPostsWithScoreOver(
            output,
            allDocuments,
            topicID, docScoreLowerLimit);
        
        
        // add the source post details to each of the topicDocs
        for (int i = 0; i < topicDocs.size(); i++ ) {
          int docId = topicDocs.get(i).getDocId();
          //System.out.println("DocID = " + docId);
          JSONTwitterPostDetails thePost = postIds_jsonTwitterPostDetails.get(Integer.toString(docId));
          if (thePost != null) {
            //System.out.println ("Got post for doc ID " + docId + " - its ID is " + thePost.getId() + " and its text is: " + thePost.getText());
            topicDocs.get(i).setPostDetails(thePost);
            JSONTwitterUserDetails theUser = userIds_jsonTwitterUserDetails.get(thePost.getByUserId());
            if (theUser != null) {
              //System.out.println ("Got user for doc ID " + docId + " - the user ID is " + thePost.getByUserId() );
              topicDocs.get(i).setUserDetails(theUser);
            }
          }
        }

        
        
/*
        int numPosts = topicDocs.size();
        String numPostsStr = String.valueOf(numPosts);
*/

        topic = new TopicOpinionAnalysisTopic(
                topicIdStr,
                topicTermsString,
                keyJsonUsers,
                topTopicPosts,
                topicDocs,
                docScoreLowerLimit
              );

        // Set metrics in topic result
        topic.setValence(formatter.format(output.getValence(topicID)));
        topic.setControversy(formatter.format(output.getControversity(topicID)));

        topics[topicID] = topic;
      }

      System.out.println();

      // Set topics in result
      result.setTopics(topics);

      // Check all documents


  /*
      for (int i=0; i < allDocuments.size(); i++) {
        String documentDetails = "";
        int id = d.getID();
        documentDetails += id + ":";
        double[] sc = d.getTopicScores();
        for (int i=0; i<sc.length;i++) {
          documentDetails += " " + formatter.format(sc[i]);
        }
        documentDetails += ": " + output.getDocumentBody(id);
        System.out.println(documentDetails);


      }
    */
  /*
      for (TopicOpinionDocument d : documents) {
        String documentDetails = "";
        int id = d.getID();
        documentDetails += id + ":";
        double[] sc = d.getTopicScores();
        for (int i=0; i<sc.length;i++) {
          documentDetails += " " + formatter.format(sc[i]);
        }
        documentDetails += ": " + output.getDocumentBody(id);
        System.out.println(documentDetails);
      }
  */
    // Get pairwise distances between topics
    System.out.println("\nPairwise distances between topics:");

    String [][] distances = new String[numTopics][numTopics];
    for (int i=0; i<numTopics; i++) {
      for (int j=0; j<numTopics; j++) {
        double dist = output.topicDist(i,j);
        String distStr = formatter.format(dist);
        distances[i][j] = distStr;
        System.out.print(distStr + " ");
      }
      System.out.println();
    }
    System.out.println();




    // Set topic distances in result
    result.setTopicDistances(distances);

    return result;
}
  catch (Exception ex) {
    ex.printStackTrace();
    return null;
  }

 }
  public static ArrayList<TopicDocument> getTopicPostsWithScoreOver(
          TopicOpinionOutput output,
          ArrayList<TopicOpinionDocument> allDocs,
          int topicId, double lowerLimit) throws Exception {

    // lower limit goes from 0.0 to 1.0
    if (lowerLimit <0.0 || lowerLimit > 1.0) {
      throw new Exception ("lowerLimit argument must be in the range 0.0->1.0");
    }


    ArrayList<TopicDocument> resultTopics;
    resultTopics = new ArrayList<TopicDocument>();

    for (int i=0; i < allDocs.size(); i++) {
      TopicOpinionDocument thisDoc = allDocs.get(i);
      
      int docId = thisDoc.getID();
      
      double[] thisDocScores = thisDoc.getTopicScores();
      double thisDocScoreInTopic = thisDocScores[topicId];

      if (thisDocScoreInTopic > lowerLimit) {
        //int docId = thisDoc.getID();
//        int docId = i;
//        String docBody = output.getDocumentBody(i);
//        String docUser = output.getUser(i);
        
        String docBody = output.getDocumentBody(docId);
        String docUser = output.getUser(docId);
        
        HashMap thisDocOpinions = (HashMap)thisDoc.getOpinions();

        double dominance = 0.0;
        if (thisDocOpinions.containsKey("dominance")) {
         dominance = new Double(thisDocOpinions.get("dominance").toString());
        }
        double arousal = 0.0;
        if (thisDocOpinions.containsKey("arousal")) {
         arousal = new Double(thisDocOpinions.get("arousal").toString());
        }
        double deNormalisedValence = 0.0;
        if (thisDocOpinions.containsKey("valence")) {
         deNormalisedValence = new Double(thisDocOpinions.get("valence").toString());
        }

        double valence = thisDoc.getValence();


        resultTopics.add(
                new TopicDocument(
                docId,
                topicId,
                thisDocScoreInTopic,
                docBody,
                docUser,
                valence,
                dominance,
                arousal,
                deNormalisedValence)
        );

      }

    }



    return resultTopics;
  }



}
