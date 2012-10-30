package eu.wegov.web.controller;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.wegov.prototype.web.resources.headsup.DataConnector;
import eu.wegov.web.security.WegovLoginService;


import eu.wegov.coordinator.dao.data.HeadsUpForumAndThreadData;
import eu.wegov.coordinator.dao.data.HeadsUpPostData;
import eu.wegov.coordinator.dao.data.HeadsUpPost;
import java.io.File;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

//import org.json.JSONException;
import net.sf.json.JSONException;
//import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import eu.wegov.common.model.AnalysisResults;
import eu.wegov.common.model.AnalysisTopic;
import eu.wegov.common.model.TopicDocument;
import eu.wegov.coordinator.Activity;
import eu.wegov.coordinator.Run;
import eu.wegov.coordinator.web.WidgetDataAsJson;
//import net.sf.json.JSONSerializer;

import uk.ac.itinnovation.soton.wegov.hansard.Factory;
import uk.ac.itinnovation.soton.wegov.hansard.Forum;
import uk.ac.itinnovation.soton.wegov.hansard.Post;
import uk.ac.itinnovation.soton.wegov.hansard.Thread;
import uk.ac.itinnovation.soton.wegov.hansard.User;
import west.importer.WegovImporter;
import west.wegovdemo.SampleInput;
import west.wegovdemo.TopicOpinionAnalysis;
import west.wegovdemo.TopicOpinionDocument;
import west.wegovdemo.TopicOpinionOutput;

/*
 *
 * Original application context
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN 2.0//EN" "http://www.springframework.org/dtd/spring-beans-2.0.dtd">
<beans>
	<bean id="wegovComponent" class="org.restlet.ext.spring.SpringComponent">
		<property name="defaultTarget" ref="wegovApplication" />
	</bean>

	<bean id="wegovApplication" class="eu.wegov.prototype.web.application.WegovApplication">
		<property name="root" ref="router" />
	</bean>

	<!-- Define the router -->
	<bean name="router" class="org.restlet.ext.spring.SpringBeanRouter" />

	<!-- Define all the routes -->
	<bean name="/" class="eu.wegov.prototype.web.application.WegovApplication"
		scope="prototype" autowire="byName" />

	<!-- Define all HeadsUp routes -->
	<bean name="/data" class="eu.wegov.prototype.web.resources.headsup.DataResource" scope="prototype" autowire="byName" />
	<bean name="/koblenzanalysis" class="eu.wegov.prototype.web.resources.headsup.KoblenzPerformAnalysisResource" scope="prototype" autowire="byName" />
	<bean name="/download" class="eu.wegov.prototype.web.resources.headsup.KoblenzDownloadResultsAsExcelFileResource" scope="prototype" autowire="byName" />
	<bean name="/search" class="eu.wegov.prototype.web.resources.headsup.HeadsUpSearch" scope="prototype" autowire="byName" />
	<bean name="/viewposts" class="eu.wegov.prototype.web.resources.headsup.ViewPosts" scope="prototype" autowire="byName" />

</beans>

 *
 */

@Controller
@RequestMapping("/home/headsup")
public class HeadsupController {
	@Autowired
	@Qualifier("wegovLoginService")
	WegovLoginService loginService;

	@RequestMapping(method = RequestMethod.GET, value = "/viewposts/do.json")
	public @ResponseBody
	HeadsUpPostData retrieve(@RequestParam("input") String input) throws MalformedURLException, JSONException, Exception {

 		org.json.JSONObject results = new org.json.JSONObject();

		System.out.println("Getting posts data");

    Factory f = DataConnector.getFactory();

		if (f == null)
			throw new RuntimeException("Failed to init factory");

		System.out.println("Finished collecting data");

    String inputThreadsIds = input;
		System.out.println("Ids to view: " + inputThreadsIds);

		ArrayList<String> threadIds = new ArrayList<String>();
		Collections.addAll(threadIds, inputThreadsIds.split(","));

    ArrayList posts = new ArrayList();
		int postsCounter = 0;

		for (uk.ac.itinnovation.soton.wegov.hansard.Thread thread : f.getthreadsWithIds(threadIds)) {
			System.out.println(thread);
			for (Post post : f.getPostsForThread(thread.getId())) {

        HeadsUpPost huPost = new HeadsUpPost (
          post.getId(),
          post.getTimePublished(),
          thread.getName(),
          f.getUserWithId(post.getUserId()).getName(),
          post.getSubject(),
          post.getMessage()
        );
				posts.add(huPost);
				postsCounter++;

			}
		}

		System.out.println("Returning " + postsCounter + " posts" );

		return new HeadsUpPostData(posts);
	}


	@RequestMapping(method = RequestMethod.GET, value = "/data/do.json")
	public @ResponseBody
  HeadsUpForumAndThreadData retrieveData() throws MalformedURLException, JSONException, Exception {
    try {
      org.json.JSONObject result = new org.json.JSONObject();

      System.out.println("Collecting data");
      Factory f = DataConnector.getFactory();

      if (f == null)
        throw new RuntimeException("Failed to init factory");

      System.out.println("Finished collecting data");

      ArrayList forumIds = new ArrayList();
      ArrayList forumNames = new ArrayList();
      ArrayList numthreadsAndPostsInForumArray = new ArrayList();
      HashMap threads = new HashMap();
      HashMap threadsStats = new HashMap();
      HashMap threadIdsOnly = new HashMap();

      int numMessagesInForum;
      for (Forum forum : f.getForums()) {
        ArrayList<Thread> threadsData = f.getthreadsForForum(forum.getId());
        String numthreadsInForum = Integer.toString(threadsData.size());
        numMessagesInForum = 0;

        forumIds.add(Integer.toString(forum.getId()));
        System.out.println(forum);

        ArrayList threadNames = new ArrayList();
        ArrayList threadStats = new ArrayList();
        ArrayList threadIds = new ArrayList();

        for (Thread thread : threadsData) {
          ArrayList<Post> postsData = f.getPostsForThread(thread.getId());
          int numMessagesInthread = postsData.size();
          numMessagesInForum += numMessagesInthread;

          threadNames.add(thread.getName().toString());
          threadStats.add(" (" + numMessagesInthread + " posts)");
          threadIds.add(thread.getId());

  //				System.out.println("\t-" + thread);
        }

        forumNames.add(forum.getName().toString());
        if (threadsData.size() > 1)
          numthreadsAndPostsInForumArray.add(numthreadsInForum + " threads, " + numMessagesInForum + " posts");
        else
          numthreadsAndPostsInForumArray.add(numthreadsInForum + " thread, " + numMessagesInForum + " posts");

        threads.put(Integer.toString(forum.getId()), threadNames);
        threadsStats.put(Integer.toString(forum.getId()), threadStats);
        threadIdsOnly.put(Integer.toString(forum.getId()), threadIds);
      }

      return new HeadsUpForumAndThreadData(
              forumIds, forumNames, numthreadsAndPostsInForumArray,
              threads, threadsStats, threadIdsOnly);

    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
	}


	private Pattern createSearchPattern(String query) {
		String lcQuery = query.toLowerCase();
		Pattern pattern = Pattern.compile(".*" + lcQuery + ".*");
		return pattern;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/search/do.json")
	public @ResponseBody
  HeadsUpForumAndThreadData searchPosts(@RequestParam("query") String query)
          throws MalformedURLException, JSONException, Exception {

		Pattern searchPattern = createSearchPattern(query);

		Factory f = DataConnector.getFactory();

		if (f == null)
			throw new RuntimeException("Failed to init factory");

		System.out.println("Finished collecting data");

		System.out.println("\nHeadsUp search posts containing: " + query);

    ArrayList forumIds = new ArrayList();
    ArrayList forumNames = new ArrayList();
    ArrayList numthreadsAndPostsInForumArray = new ArrayList();
    HashMap threads = new HashMap();
    HashMap threadsStats = new HashMap();
    HashMap threadIdsOnly = new HashMap();

    ArrayList posts = new ArrayList();

		int postsCounter = 0;

		ArrayList<Post> selectedPosts = f.getPosts(searchPattern);

		TreeSet<Integer> threadIdsSet = new TreeSet<Integer>();
		HashMap<Integer, Thread> threadsMap = new HashMap<Integer, Thread>();

		// Get list of threads for returned posts
		System.out.println("\nGetting thread ids for posts");
		for (Post post : selectedPosts) {
			int threadId = post.getThreadId();
			System.out.println("Thread id: " + threadId);
			threadIdsSet.add(threadId);
		}

		// Initialise lists of posts for each thread
		HashMap<Integer,ArrayList<Post>> threadPosts = new HashMap<Integer,ArrayList<Post>>();
		for (Integer threadId : threadIdsSet) {
			ArrayList<Post> postsList = new ArrayList<Post>();
			threadPosts.put(threadId, postsList);
		}

		// Allocate posts to each thread
		for (Post post : selectedPosts) {
			int threadId = post.getThreadId();
			ArrayList<Post> postsList = threadPosts.get(threadId);
			postsList.add(post);
		}

		TreeSet<Integer> forumIdsSet = new TreeSet<Integer>(); // naturally ordered

		// Get unordered list of forums for set of thread ids
		System.out.println("\nGetting forums ids for unique threads");
		for (Integer threadId : threadIdsSet) {
			Thread thread = f.getThreadWithId(threadId);
			threadsMap.put(threadId, thread); // store in map for later
			int forumId = thread.getForumId();
			System.out.println("Thread id: " + threadId + ", forum id: " + forumId);
			forumIdsSet.add(forumId);
		}

		// Initialise lists of threads for each forum
		HashMap<Integer,ArrayList<Thread>> forumThreads = new HashMap<Integer,ArrayList<Thread>>();
		for (Integer forumId : forumIdsSet) {
			ArrayList<Thread> threadsList = new ArrayList<Thread>();
			forumThreads.put(forumId, threadsList);
		}

		// Allocate threads to each forum
		for (Integer threadId : threadIdsSet) {
			Thread thread = threadsMap.get(threadId);
			int forumId = thread.getForumId();
			ArrayList<Thread> threadsList = forumThreads.get(forumId);
			threadsList.add(thread);
		}

		System.out.println("\nForums:");
		for (Integer forumId : forumIdsSet) {
			int numMessagesInForum;

			ArrayList<Thread> threadsList = forumThreads.get(forumId);
			int numthreadsInForum = threadsList.size();
			numMessagesInForum = 0;

      ArrayList threadNames = new ArrayList();
      ArrayList threadStats = new ArrayList();
      ArrayList threadIds = new ArrayList();

      forumIds.add(Integer.toString(forumId));
			Forum forum = f.getForumWithId(forumId);
			System.out.println(forumId + ": " + forum.getName());
      forumNames.add(forum.getName());

			for (Thread thread : threadsList) {
				ArrayList<Post> postsList = threadPosts.get(thread.getId());
				int numMessagesInthread = postsList.size();
				numMessagesInForum += numMessagesInthread;

        threadNames.add(thread.getName().toString());
				threadStats.add(" (" + numMessagesInthread + " posts)");
				threadIds.add(thread.getId());

			}

			if (threadsList.size() > 1)
        numthreadsAndPostsInForumArray.add(numthreadsInForum + " threads, " + numMessagesInForum + " posts");
			else
        numthreadsAndPostsInForumArray.add(numthreadsInForum + " thread, " + numMessagesInForum + " posts");

			threads.put(Integer.toString(forumId), threadNames);
			threadsStats.put(Integer.toString(forumId), threadStats);
			threadIdsOnly.put(Integer.toString(forumId), threadIds);
		}

		for (Post post : selectedPosts) {
			//System.out.println(post);
			Thread thread = threadsMap.get(post.getThreadId());
      HeadsUpPost huPost = new HeadsUpPost (
        post.getId(),
        post.getTimePublished(),
        thread.getName(),
        f.getUserWithId(post.getUserId()).getName(),
        post.getSubject(),
        post.getMessage()
      );
      posts.add(huPost);
      postsCounter++;
		}

		System.out.println("\nReturning " + postsCounter + " posts" );

    return new HeadsUpForumAndThreadData(
          forumIds, forumNames, numthreadsAndPostsInForumArray,
          threads, threadsStats, threadIdsOnly,
          posts);


	}


/*
  public static ArrayList<TopicDocument> getTopicPostsWithScoreOver(
          TopicOpinionOutput output, int topicId, double lowerLimit) throws Exception {

    // lower limit goes from 0.0 to 1.0
    if (lowerLimit <0.0 || lowerLimit > 1.0) {
      throw new Exception ("lowerLimit argument must be in the range 0.0->1.0");
    }


    int[] docIds = output.getTopicOpinionDocIDs(topicId);
    double[] topicScores = output.getTopicOpinionDocScores(topicId);

    // make sure both arrays are same length!

    if (docIds.length != topicScores.length) {
      throw new Exception ("topic scores and doc Id arrays are different lengths!");
    }

    ArrayList<TopicDocument> resultTopics;
    resultTopics = new ArrayList<TopicDocument>();

    for (int i=0; i < docIds.length; i++) {
      if (topicScores[i] > lowerLimit) {
        String docBody = output.getDocumentBody(i);
        String docUser = output.getUser(i);
        resultTopics.add(
                new TopicDocument(i, topicId, topicScores[i], docBody, docUser));
      }
    }
    return resultTopics;
}
*/

  public static ArrayList<TopicDocument> getTopicPostsWithScoreOver(
          TopicOpinionOutput output,
          ArrayList<TopicOpinionDocument> allDocs,
          LinkedHashMap<Integer, Integer> postIDsAndDocIDs,
          int topicId, double lowerLimit, ArrayList<Integer> forumIDs) throws Exception {

    // lower limit goes from 0.0 to 1.0
    if (lowerLimit <0.0 || lowerLimit > 1.0) {
      throw new Exception ("lowerLimit argument must be in the range 0.0->1.0");
    }

		Factory f = DataConnector.getFactory();

		if (f == null) {
			throw new RuntimeException("Failed to init factory");
    }

  /*
    int[] docIds = output.getTopicOpinionDocIDs(topicId);
    double[] topicScores = output.getTopicOpinionDocScores(topicId);

    // make sure both arrays are same length!

    if (docIds.length != topicScores.length) {
      throw new Exception ("topic scores and doc Id arrays are different lengths!");
    }
  */

    ArrayList<TopicDocument> resultTopics;
    resultTopics = new ArrayList<TopicDocument>();

/*
		LinkedHashMap<Integer, Integer> postIDsAndDocIDs = new LinkedHashMap<Integer, Integer>();

		if (type.equals("threads")) {
			for (Thread thread : f.getthreadsWithIds(idsArray)) {
				System.out.println(thread);
				for (Post post : f.getPostsForThread(thread.getId())) {
	//				System.out.println("\t-" + post + " " + f.getUserWithId(post.getUserId()));
					input.add(post.getContents(), Integer.toString(post.getUserId()));
					postIDsAndDocIDs.put(inputDocsCounter, post.getId());
					inputDocsCounter++;
				}
			}
		}
		else {
			for (Post post : f.getPostsWithIds(idsArray)) {
				input.add(post.getContents(), Integer.toString(post.getUserId()));
				postIDsAndDocIDs.put(inputDocsCounter, post.getId());
				inputDocsCounter++;
			}
		}
  */
    for (int i=0; i < allDocs.size(); i++) {
      TopicOpinionDocument thisDoc = allDocs.get(i);
      double[] thisDocScores = thisDoc.getTopicScores();
      double thisDocScoreInTopic = thisDocScores[topicId];
      
      int docId = thisDoc.getID();
      
//      System.out.println("Topic ID  = " + topicId + " Doc Scores = " + thisDocScores + " i = " + i + ", doc ID = " + thisDoc.getID());

      if (thisDocScoreInTopic > lowerLimit) {
        //String docBody = output.getDocumentBody(i);
        //String docUser = output.getUser(i);
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

        // user name & type
        System.out.println("\t\t- " + f.getUserWithId(Integer.parseInt(docUser)));
        User theUser = f.getUserWithId(Integer.parseInt(docUser));

        //Post originalPost = f.getPostWithId(postIDsAndDocIDs.get(i));
        Post originalPost = f.getPostWithId(postIDsAndDocIDs.get(docId));

        int threadId = originalPost.getThreadId();
        Thread thread = f.getThreadWithId(threadId);
        int  forumId = thread.getForumId();

        forumIDs.add(new Integer(forumId));

        Forum  forum = f.getForumWithId(forumId);

        User originalUser = f.getUserWithId(originalPost.getUserId());
        Timestamp datePublished = originalPost.getTimePublished();
        String datePublishedAsString = new SimpleDateFormat("HH:mm MM/dd/yyyy").format(datePublished);

        /*
        System.out.println("Document " + i
                + ": User = " + theUser.toString()
                + ", Original post = " + originalPost.toString()
                + ", thread = " + thread.toString()
                + ", forum = " + forum.toString());
        */
        //koblenzTopicKeyUsers.add(theUser.getName() + " (" + theUser.getType() + ")");

        //relevantdocscontext


        //relevantdocssubjects


        // relevantdocsdates



        resultTopics.add(
                new TopicDocument(
//                i, topicId, thisDocScoreInTopic,
                docId, topicId, thisDocScoreInTopic,                
                originalPost.getSubject(), originalPost.getMessage(), docBody,
                docUser, theUser.getName(), theUser.getType(),
                thread.getId(), thread.getName(),
                forum.getId(), forum.getName(),
                datePublishedAsString,
                valence,
                dominance,
                arousal,
                deNormalisedValence)
        );
      }
    }



    return resultTopics;
}

  @RequestMapping(method = RequestMethod.GET, value = "/koblenzanalysis/do.json")
	public @ResponseBody
  AnalysisResults retrieveAnalysisResults(
          @RequestParam("type") String type,
          @RequestParam("input") String ids,
          @RequestParam("numTopicsWanted") int numTopicsWanted) throws Exception {
		System.out.println("Running analysis");
		Factory f = DataConnector.getFactory();

		if (f == null)
			throw new RuntimeException("Failed to init factory");

		Activity analysisActivity = loginService.createNewAnalysisActivity("Topic analysis for headsup");
		Run analysisRun = loginService.createNewAnalysisRun(analysisActivity, "Topic Analysis run for headsup");

    int runId = analysisRun.getID();

		System.out.println("Finished collecting data");

		System.out.println("Topic analysis for: " + type);

		if (! ( type.equals("posts") || (type.equals("threads")) ) ) {
			throw new Exception("Unknown type: " + type);
		}

		System.out.println("Ids to analyse: " + ids);

		ArrayList<String> idsArray = new ArrayList<String>();
		Collections.addAll(idsArray, ids.split(","));

		SampleInput input = new SampleInput();

    int inputDocsCounter = 0;

		LinkedHashMap<Integer, Integer> postIDsAndDocIDs = new LinkedHashMap<Integer, Integer>();

		if (type.equals("threads")) {
			for (Thread thread : f.getthreadsWithIds(idsArray)) {
				System.out.println(thread);
				for (Post post : f.getPostsForThread(thread.getId())) {
	//				System.out.println("\t-" + post + " " + f.getUserWithId(post.getUserId()));
					input.add(post.getContents(), Integer.toString(post.getUserId()));
					postIDsAndDocIDs.put(inputDocsCounter, post.getId());
					inputDocsCounter++;
				}
			}
		}
		else {
			for (Post post : f.getPostsWithIds(idsArray)) {
				input.add(post.getContents(), Integer.toString(post.getUserId()));
				postIDsAndDocIDs.put(inputDocsCounter, post.getId());
				inputDocsCounter++;
			}
		}

		int numPostsToAnalyse = input.getDocumentContents().length;
		System.out.println("Using " + numPostsToAnalyse + " documents for analysis" );


    // The Guts
        TopicOpinionAnalysis analysis = new WegovImporter();

        if (numTopicsWanted > 0) {
          analysis.setNumTopics(numTopicsWanted);
        }
        //otherwise let it determine automatically

        TopicOpinionOutput output = analysis.analyzeTopicsOpinions(input);
    //

        // Could be very large!!!
        ArrayList<TopicOpinionDocument> allDocuments = new ArrayList<TopicOpinionDocument>(output.getAllDocuments());


        ArrayList<Integer> masterForumIdList = new ArrayList<Integer>();

        int numTopics = output.getNumTopics();

        //JSONArray koblenzTopics = new JSONArray();
        ArrayList<AnalysisTopic> koblenzTopics = new ArrayList<AnalysisTopic>();

        for (int topicID = 0; topicID < numTopics; topicID++) {
        	//JSONObject koblenzTopic = new JSONObject();

          System.out.println("Topic ID = " + topicID);
        	StringBuilder koblenzTopicKeyTerms = new StringBuilder();
        	StringBuilder koblenzTopicJustKeyTermsSeparatedBySpace = new StringBuilder();

          int[] topicRelevantDocIds = output.getTopicRelevantDocIDs(topicID);

          int numRelevantDocsInTopic = topicRelevantDocIds.length;

          System.out.println("Num relevant docs in topic " + topicID + " = " + numRelevantDocsInTopic);

          double docScoreLowerLimit = 0.5;

          ArrayList<TopicDocument> topicPosts =
                this.getTopicPostsWithScoreOver(
                  output, allDocuments, postIDsAndDocIDs,
                  topicID, docScoreLowerLimit, masterForumIdList);

          String [] reasons = output.getTopicOpinionDocReasons(topicID);

          System.out.println ("reasons for topic ID " + topicID + " = " + Arrays.toString(reasons));

          int numTopicPosts = topicPosts.size();
          /*
          TopicDocument [] topicPostsArray = new TopicDocument [topicPosts.size()];
          topicPosts.toArray(topicPostsArray);
          */
          System.out.println
                  ("number of topicPosts is " + numTopicPosts
                  + " when lower score limit = " + docScoreLowerLimit);



          /*
        	JSONArray koblenzTopicKeyUsers = new JSONArray();
        	JSONArray koblenzRelevantDocsSubjects = new JSONArray();
        	JSONArray koblenzRelevantDocsMessages = new JSONArray();
        	JSONArray koblenzRelevantDocsUsers = new JSONArray();
        	JSONArray koblenzRelevantDocsDates = new JSONArray();
        	JSONArray koblenzRelevantDocsContext = new JSONArray();
        	JSONArray koblenzRelevantDocsScores = new JSONArray();
          */

        	ArrayList<String> koblenzTopicKeyUsers = new ArrayList<String>();
        	ArrayList<String> koblenzRelevantDocsSubjects = new ArrayList<String>();
        	ArrayList<String> koblenzRelevantDocsMessages = new ArrayList<String>();
        	ArrayList<String> koblenzRelevantDocsUsers = new ArrayList<String>();
        	ArrayList<String> koblenzRelevantDocsDates = new ArrayList<String>();
        	ArrayList<String> koblenzRelevantDocsContext = new ArrayList<String>();
        	ArrayList<String> koblenzRelevantDocsScores = new ArrayList<String>();

        	System.out.println("Topic number " + topicID);
          System.out.println("\t- Key terms:");

            for (String keyTerm : output.getTopicTerms(topicID)) {
                System.out.println("\t\t- " + keyTerm);
                koblenzTopicKeyTerms.append(keyTerm);
                koblenzTopicJustKeyTermsSeparatedBySpace.append(keyTerm);
                koblenzTopicKeyTerms.append(", ");
                koblenzTopicJustKeyTermsSeparatedBySpace.append(" ");
            }
            //output

            String koblenzTopicKeyTermsAsString = koblenzTopicKeyTerms.toString();
            if (koblenzTopicKeyTermsAsString.length() > 3) {
            	koblenzTopicKeyTermsAsString =
                      koblenzTopicKeyTermsAsString.substring(0, koblenzTopicKeyTermsAsString.length() - 2);
            }
            // /output


            System.out.println("\t- Key users:");


            for (String keyUser : output.getTopicUsers(topicID)) {
            // output

                System.out.println("\t\t- " + f.getUserWithId(Integer.parseInt(keyUser)));
                User theUser = f.getUserWithId(Integer.parseInt(keyUser));
                koblenzTopicKeyUsers.add(theUser.getName() + " (" + theUser.getType() + ")");
            // /output

            }

            System.out.println("\t- Relevant documents:");

            int[] relevantDocIDs = output.getTopicRelevantDocIDs(topicID);
            double[] relevantDocScores = output.getTopicRelevantDocScores(topicID);
            int threadId, forumId;
            Thread thread;
            Forum forum;
            String[] koblenzTopicKeyTermsAsArray
                    = koblenzTopicJustKeyTermsSeparatedBySpace.toString().split(" ");
            String[] originalMessageAsArray;
            String[] highlightedMessagesAsArray;
            String[] originalSubjectAsArray;
            String[] highlightedSubjectAsArray;
            StringBuilder highlightedMessagesStringBuilder;
            StringBuilder highlightedSubjectStringBuilder;
            String messageWord;

            for (int j = 0; j < relevantDocIDs.length; j++) {
                int relevantDocID = relevantDocIDs[j];

                System.out.println(
                        "\t\t- [" + relevantDocID + "] "
                        + output.getDocumentBody(relevantDocID)
                        + " (" + relevantDocScores[j] + ")");

                Post originalPost = f.getPostWithId(postIDsAndDocIDs.get(relevantDocID));

                threadId = originalPost.getThreadId();
                thread = f.getThreadWithId(threadId);
                forumId = thread.getForumId();
                forum = f.getForumWithId(forumId);

                User originalUser = f.getUserWithId(originalPost.getUserId());
                Timestamp datePublished = originalPost.getTimePublished();
                String datePublishedAsString = new SimpleDateFormat("HH:mm MM/dd/yyyy").format(datePublished);

//                System.out.println("\t\t- Found post: " + );
                originalMessageAsArray = originalPost.getMessageClean().split(" ");
                highlightedMessagesAsArray = new String[originalMessageAsArray.length];
                originalSubjectAsArray = originalPost.getSubject().split(" ");
                highlightedSubjectAsArray = new String[originalSubjectAsArray.length];


                for (int i = 0; i < originalMessageAsArray.length; i++) {
                	messageWord = originalMessageAsArray[i];
                	for (String termWord : koblenzTopicKeyTermsAsArray) {
                		if (messageWord.trim().toLowerCase().startsWith(termWord.trim().toLowerCase())) {
//                			System.out.println("Replacing " + messageWord + " because of " + termWord);
                			highlightedMessagesAsArray[i] = "<b>" + messageWord + "</b>";
                			break;
                		} else {
//                			System.out.println("NOT replacing " + messageWord + " because of " + termWord);
                			highlightedMessagesAsArray[i] = messageWord;
                		}
                	}
                }

                for (int i = 0; i < originalSubjectAsArray.length; i++) {
                	messageWord = originalSubjectAsArray[i];
                	for (String termWord : koblenzTopicKeyTermsAsArray) {
                		if (messageWord.trim().toLowerCase().startsWith(termWord.trim().toLowerCase())) {
//                			System.out.println("Replacing " + messageWord + " because of " + termWord);
                			highlightedSubjectAsArray[i] = "<b>" + messageWord + "</b>";
                			break;
                		} else {
//                			System.out.println("NOT replacing " + messageWord + " because of " + termWord);
                			highlightedSubjectAsArray[i] = messageWord;
                		}
                	}
                }

                highlightedMessagesStringBuilder = new StringBuilder();
                for (String highlightedMessage : highlightedMessagesAsArray) {
                	highlightedMessagesStringBuilder.append(highlightedMessage);
                	highlightedMessagesStringBuilder.append(" ");
                }

                highlightedSubjectStringBuilder = new StringBuilder();
                for (String highlightedSubject : highlightedSubjectAsArray) {
                	highlightedSubjectStringBuilder.append(highlightedSubject);
                	highlightedSubjectStringBuilder.append(" ");
                }


                // output
                koblenzRelevantDocsSubjects.add(highlightedSubjectStringBuilder.toString().trim());
//                koblenzRelevantDocsSubjects.put(originalPost.getSubject());
//                koblenzRelevantDocsMessages.put(originalPost.getMessageClean());
                koblenzRelevantDocsMessages.add(highlightedMessagesStringBuilder.toString().trim());
                koblenzRelevantDocsUsers.add(originalUser.getName() + " (" + originalUser.getType() + ")");
                koblenzRelevantDocsDates.add(datePublishedAsString);
                koblenzRelevantDocsContext.add(forum.getName() + " - " + thread.getName());
                koblenzRelevantDocsScores.add(
                        Double.toString((double) Math.round(relevantDocScores[j] * 10000) / 10000));

                // / output

            }

            //output
/*
            koblenzTopic.put("keyterms", koblenzTopicKeyTermsAsString);
            koblenzTopic.put("keyusers", koblenzTopicKeyUsers);
            koblenzTopic.put("relevantdocssubjects", koblenzRelevantDocsSubjects);
            koblenzTopic.put("relevantdocsmessages", koblenzRelevantDocsMessages);
            koblenzTopic.put("relevantdocsusers", koblenzRelevantDocsUsers);
            koblenzTopic.put("relevantdocsdates", koblenzRelevantDocsDates);
            koblenzTopic.put("relevantdocscontext", koblenzRelevantDocsContext);
            koblenzTopic.put("relevantdocsscores", koblenzRelevantDocsScores);
*/

            /*
  public AnalysisTopic(
            String keyterms,
            ArrayList keyusers,
            ArrayList relevantdocssubjects,
            ArrayList relevantdocsmessages,
            ArrayList relevantdocsusers,
            ArrayList relevantdocsdates,
            ArrayList relevantdocscontext,
            ArrayList relevantdocsscores) {*/

            double valence = output.getValence(topicID);

            double controversy = output.getControversity(topicID);

            System.out.println("topic " + topicID + " sentiment = " + valence + ", controversy = " + controversy);


            AnalysisTopic koblenzTopic = new AnalysisTopic(
                    numTopicPosts,
                    docScoreLowerLimit,
                    valence,
                    controversy,
                    koblenzTopicKeyTermsAsString,
                    koblenzTopicKeyUsers,
                    koblenzRelevantDocsSubjects,
                    koblenzRelevantDocsMessages,
                    koblenzRelevantDocsUsers,
                    koblenzRelevantDocsDates,
                    koblenzRelevantDocsContext,
                    koblenzRelevantDocsScores,
                    topicPosts);

            koblenzTopics.add(koblenzTopic);

            // /output
        }


        // compute set of unique forum IDs
        HashSet hs = new HashSet();
        hs.addAll(masterForumIdList);
        ArrayList<Integer> uniqueForumIDs = new ArrayList<Integer>();
        uniqueForumIDs.addAll(hs);

        int [] uniqueForumIDsInt;
        uniqueForumIDsInt = new int [uniqueForumIDs.size()];

        for (int i = 0; i < uniqueForumIDs.size(); i++) {
          uniqueForumIDsInt[i] = uniqueForumIDs.get(i).intValue();
        }


       //JSONArray resultJson = (JSONArray) JSONSerializer.toJSON(koblenzTopics);

        //JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( bean );

        //String resultJsonString = resultJson.toString();

        //System.out.println("result json string = " + resultJsonString);

    AnalysisResults result = new AnalysisResults(
            numTopics + " topics in " + numPostsToAnalyse + " posts",
            koblenzTopics,
            "no file");

    result.setRunId(runId);
    result.setForumIds(uniqueForumIDsInt);

    String forumIdsString =  Arrays.toString(uniqueForumIDsInt);

    /*
		result.put("summary", numTopics + " topics in " + numPostsToAnalyse + " posts");
		result.put("result", koblenzTopics);
		result.put("filePath", filePath);

		return new JsonRepresentation(result);
    */

    JSONObject resultJson = (JSONObject) JSONSerializer.toJSON( result );

    // using the forum ids string as the name in the database
    saveAnalysisResult (runId, resultJson.toString(), forumIdsString);

    //System.out.println("result to string = " + result.toString());

    return result;
	}



  	void saveAnalysisResult(int runId, final String inputData, String name) throws Exception {
		try {

			JSONObject inputDataAsJSON = (JSONObject) JSONSerializer
					.toJSON(inputData);
			Timestamp collected_at = new Timestamp((new Date()).getTime());

      loginService.saveRunResultsDataAsJson(
          runId, "headsup-topic-analysis",
          name, "", -1,
          "", "", collected_at, collected_at,
          inputData, collected_at);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@RequestMapping(method = RequestMethod.GET, value = "/getPreviousAnalysisResults/do.json")
	public @ResponseBody
	AnalysisResults getPreviousAnalysisResults(@RequestParam("runId") int runId) {

		AnalysisResults result = null;
		try {
			WidgetDataAsJson widgetData = loginService.getResultsDataForRun(runId);

      String jsonDataString = widgetData.getDataAsJson();
      JSONObject jsonData = (JSONObject)JSONSerializer.toJSON(jsonDataString);

      result = new AnalysisResults(jsonData);
      //result.setRunId(widgetData.getRunid());

      //return result;
		}
    catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return result;
	}

  AnalysisResults retrieveAnalysisResultsOld(@RequestParam("type") String type, @RequestParam("input") String ids) throws Exception {
		System.out.println("Running analysis");
		Factory f = DataConnector.getFactory();

		if (f == null)
			throw new RuntimeException("Failed to init factory");

		System.out.println("Finished collecting data");

		//String type = getQuery().getFirstValue("type");
		System.out.println("Topic analysis for: " + type);

		if (! ( type.equals("posts") || (type.equals("threads")) ) ) {
			throw new Exception("Unknown type: " + type);
		}

		//String ids = getQuery().getFirstValue("input");
		System.out.println("Ids to analyse: " + ids);

		ArrayList<String> idsArray = new ArrayList<String>();
		Collections.addAll(idsArray, ids.split(","));

		SampleInput input = new SampleInput();

        WritableFont arialBold12font = new WritableFont(WritableFont.ARIAL, 12, WritableFont.BOLD);
        WritableFont arialBold14font = new WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD);
        WritableFont arial12font = new WritableFont(WritableFont.ARIAL, 12);
        WritableCellFormat arialBold12format = new WritableCellFormat (arialBold12font);
        WritableCellFormat arialBold14format = new WritableCellFormat (arialBold14font);
        WritableCellFormat arial12format = new WritableCellFormat (arial12font);

        Date timeNow = new Date();
        SimpleDateFormat dateFormatterForFileName = new SimpleDateFormat("MMM dd, yyyy - HH_mm_ss");
        SimpleDateFormat dateFormatterForSpreadsheetDate = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat dateFormatterForSpreadsheetTime = new SimpleDateFormat("HH:mm:ss");

        String timeNowAsStringFormattedForFileName = dateFormatterForFileName.format(timeNow);
        String timeNowAsStringFormattedForSpreadsheetDate = dateFormatterForSpreadsheetDate.format(timeNow);
        String timeNowAsStringFormattedForSpreadsheetTime = dateFormatterForSpreadsheetTime.format(timeNow);
        String outputFileName;

        String fileType;

        if (type.equals("threads")) {
        	fileType = "thread";
        }
        else {
        	fileType = "post";
        }

        if (idsArray.size() > 1)
        	fileType += "s";

        //if (idsArray.size() < 2)
        //	outputFileName = timeNowAsStringFormattedForFileName + ", " + idsArray.size() + " thread.xls";
        //else
        //	outputFileName = timeNowAsStringFormattedForFileName + ", " + idsArray.size() + " threads.xls";

        outputFileName = timeNowAsStringFormattedForFileName + ", " + idsArray.size() + " " + fileType + ".xls";

        File outputFile = new File(outputFileName);
        String filePath = outputFile.getAbsolutePath();
//        System.out.println("Stuff: " + getContext().getClientDispatcher().getApplication().);
        System.out.println("Writing to file: " + filePath);

        WritableWorkbook workbook = Workbook.createWorkbook(outputFile);
        WritableSheet sheet = workbook.createSheet("Summary", 0);

		int inputDocsCounter = 0;

		LinkedHashMap<Integer, Integer> postIDsAndDocIDs = new LinkedHashMap<Integer, Integer>();

		if (type.equals("threads")) {
			for (Thread thread : f.getthreadsWithIds(idsArray)) {
				System.out.println(thread);
				for (Post post : f.getPostsForThread(thread.getId())) {
	//				System.out.println("\t-" + post + " " + f.getUserWithId(post.getUserId()));
					input.add(post.getContents(), Integer.toString(post.getUserId()));
					postIDsAndDocIDs.put(inputDocsCounter, post.getId());
					inputDocsCounter++;
				}
			}
		}
		else {
			for (Post post : f.getPostsWithIds(idsArray)) {
				input.add(post.getContents(), Integer.toString(post.getUserId()));
				postIDsAndDocIDs.put(inputDocsCounter, post.getId());
				inputDocsCounter++;
			}
		}

		int numPostsToAnalyse = input.getDocumentContents().length;
		System.out.println("Using " + numPostsToAnalyse + " documents for analysis" );


    // The Guts
        TopicOpinionAnalysis analysis = new WegovImporter();
        TopicOpinionOutput output = analysis.analyzeTopicsOpinions(input);
    //

        int numTopics = output.getNumTopics();

        //JSONArray koblenzTopics = new JSONArray();
        ArrayList<AnalysisTopic> koblenzTopics = new ArrayList<AnalysisTopic>();

        int counter = 1;
        int xLabel = 0;
        int yLabel = 0;
        String topicLabelPrefix;
        Label label;
        label = new Label(xLabel, yLabel, "Topic analysis of " + numPostsToAnalyse + " posts performed on " + timeNowAsStringFormattedForSpreadsheetDate
        		+ " at " + timeNowAsStringFormattedForSpreadsheetTime, arialBold12format); sheet.addCell(label);

        yLabel++;
        label = new Label(xLabel, yLabel, "(List of threads analyzed is after the results)", arial12format); sheet.addCell(label);

        yLabel++; yLabel++;

        label = new Label(xLabel, yLabel, "Topic analysis results:", arialBold14format); sheet.addCell(label);
        yLabel++;

        for (int topicID = 0; topicID < numTopics; topicID++) {
        	//JSONObject koblenzTopic = new JSONObject();
        	StringBuilder koblenzTopicKeyTerms = new StringBuilder();
        	StringBuilder koblenzTopicJustKeyTermsSeparatedBySpace = new StringBuilder();
        	topicLabelPrefix = "Topic " + counter + " keywords: ";
        	koblenzTopicKeyTerms.append(topicLabelPrefix);
        	counter++;

          /*
        	JSONArray koblenzTopicKeyUsers = new JSONArray();
        	JSONArray koblenzRelevantDocsSubjects = new JSONArray();
        	JSONArray koblenzRelevantDocsMessages = new JSONArray();
        	JSONArray koblenzRelevantDocsUsers = new JSONArray();
        	JSONArray koblenzRelevantDocsDates = new JSONArray();
        	JSONArray koblenzRelevantDocsContext = new JSONArray();
        	JSONArray koblenzRelevantDocsScores = new JSONArray();
          */

        	ArrayList<String> koblenzTopicKeyUsers = new ArrayList<String>();
        	ArrayList<String> koblenzRelevantDocsSubjects = new ArrayList<String>();
        	ArrayList<String> koblenzRelevantDocsMessages = new ArrayList<String>();
        	ArrayList<String> koblenzRelevantDocsUsers = new ArrayList<String>();
        	ArrayList<String> koblenzRelevantDocsDates = new ArrayList<String>();
        	ArrayList<String> koblenzRelevantDocsContext = new ArrayList<String>();
        	ArrayList<String> koblenzRelevantDocsScores = new ArrayList<String>();

        	System.out.println("Topic number " + topicID);
            System.out.println("\t- Key terms:");

            label = new Label(xLabel, yLabel, topicLabelPrefix.trim(), arialBold12format); sheet.addCell(label);
            xLabel++;

            for (String keyTerm : output.getTopicTerms(topicID)) {
                System.out.println("\t\t- " + keyTerm);
                koblenzTopicKeyTerms.append(keyTerm);
                koblenzTopicJustKeyTermsSeparatedBySpace.append(keyTerm);
                koblenzTopicKeyTerms.append(", ");
                koblenzTopicJustKeyTermsSeparatedBySpace.append(" ");
            }
            //output

            String koblenzTopicKeyTermsAsString = koblenzTopicKeyTerms.toString();
            if (koblenzTopicKeyTermsAsString.length() > 3)
            	koblenzTopicKeyTermsAsString = koblenzTopicKeyTermsAsString.substring(0, koblenzTopicKeyTermsAsString.length() - 2);
            // /output



            label = new Label(xLabel, yLabel, koblenzTopicKeyTermsAsString.split(":")[1].trim(), arialBold12format);
            sheet.addCell(label);
            xLabel = 0;
            yLabel++;

            System.out.println("\t- Key users:");
            label = new Label(xLabel, yLabel, "Key users:", arial12format); sheet.addCell(label);
            xLabel = 1;
            for (String keyUser : output.getTopicUsers(topicID)) {
            // output

                System.out.println("\t\t- " + f.getUserWithId(Integer.parseInt(keyUser)));
                User theUser = f.getUserWithId(Integer.parseInt(keyUser));
                koblenzTopicKeyUsers.add(theUser.getName() + " (" + theUser.getType() + ")");
            // /output

                label = new Label(xLabel, yLabel, theUser.getName() + " (" + theUser.getType() + ")", arial12format); sheet.addCell(label);
                yLabel++;
            }

            xLabel = 0;
            yLabel++;

            System.out.println("\t- Relevant documents:");
            label = new Label(xLabel, yLabel, "Key posts:", arial12format); sheet.addCell(label);
            xLabel++;

            int[] relevantDocIDs = output.getTopicRelevantDocIDs(topicID);
            double[] relevantDocScores = output.getTopicRelevantDocScores(topicID);
            int threadId, forumId;
            Thread thread;
            Forum forum;
            String[] koblenzTopicKeyTermsAsArray = koblenzTopicJustKeyTermsSeparatedBySpace.toString().split(" ");
            String[] originalMessageAsArray;
            String[] highlightedMessagesAsArray;
            String[] originalSubjectAsArray;
            String[] highlightedSubjectAsArray;
            StringBuilder highlightedMessagesStringBuilder;
            StringBuilder highlightedSubjectStringBuilder;
            String messageWord;

            for (int j = 0; j < relevantDocIDs.length; j++) {
                int relevantDocID = relevantDocIDs[j];

//                System.out.println("\t\t- [" + relevantDocID + "] " + output.getDocumentBody(relevantDocID) + " (" + relevantDocScores[j] + ")");

                Post originalPost = f.getPostWithId(postIDsAndDocIDs.get(relevantDocID));

                threadId = originalPost.getThreadId();
                thread = f.getThreadWithId(threadId);
                forumId = thread.getForumId();
                forum = f.getForumWithId(forumId);

                User originalUser = f.getUserWithId(originalPost.getUserId());
                Timestamp datePublished = originalPost.getTimePublished();
                String datePublishedAsString = new SimpleDateFormat("HH:mm MM/dd/yyyy").format(datePublished);

//                System.out.println("\t\t- Found post: " + );
                originalMessageAsArray = originalPost.getMessageClean().split(" ");
                highlightedMessagesAsArray = new String[originalMessageAsArray.length];
                originalSubjectAsArray = originalPost.getSubject().split(" ");
                highlightedSubjectAsArray = new String[originalSubjectAsArray.length];


                for (int i = 0; i < originalMessageAsArray.length; i++) {
                	messageWord = originalMessageAsArray[i];
                	for (String termWord : koblenzTopicKeyTermsAsArray) {
                		if (messageWord.trim().toLowerCase().startsWith(termWord.trim().toLowerCase())) {
//                			System.out.println("Replacing " + messageWord + " because of " + termWord);
                			highlightedMessagesAsArray[i] = "<b>" + messageWord + "</b>";
                			break;
                		} else {
//                			System.out.println("NOT replacing " + messageWord + " because of " + termWord);
                			highlightedMessagesAsArray[i] = messageWord;
                		}
                	}
                }

                for (int i = 0; i < originalSubjectAsArray.length; i++) {
                	messageWord = originalSubjectAsArray[i];
                	for (String termWord : koblenzTopicKeyTermsAsArray) {
                		if (messageWord.trim().toLowerCase().startsWith(termWord.trim().toLowerCase())) {
//                			System.out.println("Replacing " + messageWord + " because of " + termWord);
                			highlightedSubjectAsArray[i] = "<b>" + messageWord + "</b>";
                			break;
                		} else {
//                			System.out.println("NOT replacing " + messageWord + " because of " + termWord);
                			highlightedSubjectAsArray[i] = messageWord;
                		}
                	}
                }

                highlightedMessagesStringBuilder = new StringBuilder();
                for (String highlightedMessage : highlightedMessagesAsArray) {
                	highlightedMessagesStringBuilder.append(highlightedMessage);
                	highlightedMessagesStringBuilder.append(" ");
                }

                highlightedSubjectStringBuilder = new StringBuilder();
                for (String highlightedSubject : highlightedSubjectAsArray) {
                	highlightedSubjectStringBuilder.append(highlightedSubject);
                	highlightedSubjectStringBuilder.append(" ");
                }


                // output
                koblenzRelevantDocsSubjects.add(highlightedSubjectStringBuilder.toString().trim());
//                koblenzRelevantDocsSubjects.put(originalPost.getSubject());
//                koblenzRelevantDocsMessages.put(originalPost.getMessageClean());
                koblenzRelevantDocsMessages.add(highlightedMessagesStringBuilder.toString().trim());
                koblenzRelevantDocsUsers.add(originalUser.getName() + " (" + originalUser.getType() + ")");
                koblenzRelevantDocsDates.add(datePublishedAsString);
                koblenzRelevantDocsContext.add(forum.getName() + " - " + thread.getName());
                koblenzRelevantDocsScores.add(Double.toString((double) Math.round(relevantDocScores[j] * 10000) / 10000));

                // / output

                label = new Label(xLabel, yLabel, forum.getName(), arial12format); sheet.addCell(label);
                xLabel++;
                label = new Label(xLabel, yLabel, thread.getName(), arial12format); sheet.addCell(label);
                xLabel++;
                label = new Label(xLabel, yLabel, originalUser.getName() + " (" + originalUser.getType() + ")", arial12format); sheet.addCell(label);
                xLabel++;
                label = new Label(xLabel, yLabel, datePublishedAsString, arial12format); sheet.addCell(label);
                xLabel++;
                label = new Label(xLabel, yLabel, originalPost.getSubject(), arial12format); sheet.addCell(label);
                xLabel++;
                label = new Label(xLabel, yLabel, Double.toString((double) Math.round(relevantDocScores[j] * 10000) / 10000) , arial12format); sheet.addCell(label);
                xLabel++;
                label = new Label(xLabel, yLabel, originalPost.getMessageClean(), arial12format); sheet.addCell(label);
                xLabel = 1;
                yLabel++;

            }

            xLabel = 0;
            yLabel++;

            //output
/*
            koblenzTopic.put("keyterms", koblenzTopicKeyTermsAsString);
            koblenzTopic.put("keyusers", koblenzTopicKeyUsers);
            koblenzTopic.put("relevantdocssubjects", koblenzRelevantDocsSubjects);
            koblenzTopic.put("relevantdocsmessages", koblenzRelevantDocsMessages);
            koblenzTopic.put("relevantdocsusers", koblenzRelevantDocsUsers);
            koblenzTopic.put("relevantdocsdates", koblenzRelevantDocsDates);
            koblenzTopic.put("relevantdocscontext", koblenzRelevantDocsContext);
            koblenzTopic.put("relevantdocsscores", koblenzRelevantDocsScores);
*/

            /*
  public AnalysisTopic(
            String keyterms,
            ArrayList keyusers,
            ArrayList relevantdocssubjects,
            ArrayList relevantdocsmessages,
            ArrayList relevantdocsusers,
            ArrayList relevantdocsdates,
            ArrayList relevantdocscontext,
            ArrayList relevantdocsscores) {*/

            AnalysisTopic koblenzTopic = new AnalysisTopic(
                    koblenzTopicKeyTermsAsString,
                    koblenzTopicKeyUsers,
                    koblenzRelevantDocsSubjects,
                    koblenzRelevantDocsMessages,
                    koblenzRelevantDocsUsers,
                    koblenzRelevantDocsDates,
                    koblenzRelevantDocsContext,
                    koblenzRelevantDocsScores);

            koblenzTopics.add(koblenzTopic);

            // /output
        }

        xLabel = 0;
        yLabel++;

		if (type.equals("threads")) {
	        label = new Label(xLabel, yLabel, "Forum threads used in the analysis:", arialBold14format); sheet.addCell(label);
	        yLabel++;
	        label = new Label(xLabel, yLabel, "Forum name", arialBold12format); sheet.addCell(label);
	        xLabel++;
	        label = new Label(xLabel, yLabel, "Thread name", arialBold12format); sheet.addCell(label);
	        xLabel++;
	        label = new Label(xLabel, yLabel, "Number of posts in the thread", arialBold12format); sheet.addCell(label);
	        xLabel = 0;
	        yLabel++;

	        String forumName;
	        String threadName;
	        String numPostsInThread;
			for (Thread thread : f.getthreadsWithIds(idsArray)) {
				int forumId = thread.getForumId();
				int threadId = thread.getId();
				Forum forum = f.getForumWithId(forumId);
				forumName = forum.getName();
				threadName = thread.getName();
				numPostsInThread = Integer.toString(f.getNumPostsInThread(threadId));

				label = new Label(xLabel, yLabel, forumName, arial12format); sheet.addCell(label);
		        xLabel++;
		        label = new Label(xLabel, yLabel, threadName, arial12format); sheet.addCell(label);
		        xLabel++;
		        label = new Label(xLabel, yLabel, numPostsInThread, arial12format); sheet.addCell(label);
		        xLabel = 0;
		        yLabel++;

			}
		}
		else {
			//TODO: summarise forums and threads for the analysed posts
		}

        workbook.write();
        workbook.close();

		//JSONObject result = new JSONObject();

    AnalysisResults result = new AnalysisResults(
            numTopics + " topics in " + numPostsToAnalyse + " posts",
            koblenzTopics,
            filePath);
    /*
		result.put("summary", numTopics + " topics in " + numPostsToAnalyse + " posts");
		result.put("result", koblenzTopics);
		result.put("filePath", filePath);

		return new JsonRepresentation(result);
    */

    return result;
	}


}
