package eu.wegov.coordinator.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import west.importer.WegovImporter;
import west.wegovdemo.SampleInput;
import west.wegovdemo.TopicOpinionAnalysis;
import west.wegovdemo.TopicOpinionOutput;
import eu.wegov.coordinator.Activity;
import eu.wegov.coordinator.Configuration;
import eu.wegov.coordinator.ConfigurationSet;
import eu.wegov.coordinator.Coordinator;
import eu.wegov.coordinator.KoblenzAnalysisTopicWrapper;
import eu.wegov.coordinator.Parameter;
import eu.wegov.coordinator.Policymaker;
import eu.wegov.coordinator.Role;
import eu.wegov.coordinator.Run;
import eu.wegov.coordinator.Worksheet;
import eu.wegov.coordinator.dao.data.WegovAnalysisKoblenzMessage;
import eu.wegov.coordinator.dao.data.WegovAnalysisKoblenzTopic;
import eu.wegov.coordinator.dao.data.WegovAnalysisKoblenzUser;
import eu.wegov.coordinator.dao.data.WegovAnalysisKoblenzViewpoint;
import eu.wegov.coordinator.dao.data.WegovFollower;
import eu.wegov.coordinator.dao.data.WegovLike;
import eu.wegov.coordinator.dao.data.WegovPostTag;
import eu.wegov.coordinator.dao.data.WegovSNS;
import eu.wegov.coordinator.dao.data.WegovTag;
import eu.wegov.coordinator.dao.data.twitter.FullTweet;
import eu.wegov.coordinator.dao.data.twitter.Hashtag;
import eu.wegov.coordinator.dao.data.twitter.Tweet;
import eu.wegov.coordinator.dao.data.twitter.User;
import eu.wegov.coordinator.sql.SqlSchema;
import eu.wegov.coordinator.utils.GetLastTweetsByContaining;
import eu.wegov.coordinator.utils.GetRetweetedBy;
import eu.wegov.coordinator.utils.GetTopRetweeted;
import eu.wegov.coordinator.utils.GetTwitterPostDetails;
import eu.wegov.coordinator.utils.RunLiveTwitterCollection;
import eu.wegov.coordinator.utils.TwitterHelper;
import eu.wegov.coordinator.utils.Util;

/**
 * 
 * @author max
 */
public class CoordinatorTest {

	public CoordinatorTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testSampleSetup() throws Exception {
		Coordinator coordinator = new Coordinator("../wegov-dashboard/coordinator.properties");
		coordinator.setupWegovDatabase();
		
		Policymaker max = coordinator.getPolicymakerByUsername("mbashevoy");
		ConfigurationSet set = coordinator.getConfigurationSets().get(0);
		coordinator.createActivity(max, "Twitter search for \"Education\"", "Local discussion on education", set).setStatus("running");
		coordinator.createActivity(max, "Topic analysis of \"Education\" search", "Topic analysis of local discussion on education", set).setStatus("initialising");
		coordinator.createActivity(max, "Facebook search for \"Nuclear Power\"", "Local discussion on education", set).setStatus("finished");
		coordinator.createActivity(max, "Socialmention search for \"Nuclear Power\"", "Local discussion on education", set).setStatus("finished");
		coordinator.createActivity(max, "Socialmention search for \"Snow in London\"", "Local discussion on education", set).setStatus("finished");
		coordinator.createActivity(max, "Twitter search for \"Where to buy icecream in Southampton\"", "Local discussion on education", set).setStatus("finished");
	}

	// @Test
	public void testPerformance() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");
		coordinator.setupWegovDatabase();

		long start = System.currentTimeMillis();

		Run run = coordinator.getRunByID(18);

		System.out.println(run.getLogs());
		System.out.println(run.getErrors());

		long end = System.currentTimeMillis();

		System.out.println("Execution took: " + (end - start) + " msec");
	}

	// @Test
	public void testGetLogs() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");
		coordinator.setupWegovDatabase();

		Run run = coordinator.getRunByID(18);

		System.out.println(run.getLogs());
		System.out.println(run.getErrors());
	}

	// @Test
	public void testAnalysisKoblenzQuick() throws Exception {

		SampleInput input = new SampleInput();

		input.add(
				"Some more details of these are below, and I think we knew about these, so hopefully these are not a surprise",
				"zx c");
		input.add(
				"If the user wants to undelete one of these, it will then be a simple matter of changing its state",
				"unknown@www.metafilter.com");
		input.add(
				"Louis online (Paul Sawers/The Next Web) : Miami Mega Jail -> BBC: Ep1, Ep2. YT: Ep1, Ep2.",
				"unknown@www.me tafilter.com");
		input.add("#education Islamic schools' child abuse risk #news", "asd");

		for (String inputDoc : input.getDocumentContents()) {
			System.out.println("\t-" + inputDoc);
		}

		System.out.println("Running analysis on users: ");

		for (String inputUser : input.getDocumentUsers()) {
			System.out.println("\t-" + inputUser);
		}

		TopicOpinionAnalysis analysis = new WegovImporter();

		// returned results
		TopicOpinionOutput output = analysis.analyzeTopicsOpinions(input);

		int numTopics = output.getNumTopics();

		int myRunID = 0;
		int numTopicsToShow = 3;

		for (int topicID = 0; topicID < numTopics; topicID++) {

			String topicsAsString = "";
			for (String keyTerm : output.getTopicTerms(topicID)) {
				topicsAsString += keyTerm + ", ";
			}

			if (topicsAsString.length() > 3)
				topicsAsString = topicsAsString.substring(0,
						topicsAsString.length() - 2);

			System.out.println("Topic #" + topicID + ", terms: "
					+ topicsAsString + ", num messages: "
					+ output.getTopicRelevantDocIDs(topicID).length
					+ ", num users: " + output.getTopicUsers(topicID).length);

			System.out.println("\t- Key terms:");
			for (String keyTerm : output.getTopicTerms(topicID)) {
				System.out.println("\t\t- " + keyTerm);
			}

			System.out.println("\t- Key users:");
			int count = 0;
			for (String keyUser : output.getTopicUsers(topicID)) {
				System.out.println("\t\t- " + keyUser);
				count++;
			}

			System.out.println("\t- Relevant documents:");
			int[] relevantDocIDs = output.getTopicRelevantDocIDs(topicID);
			double[] relevantDocScores = output
					.getTopicRelevantDocScores(topicID);
			for (int j = 0; j < relevantDocIDs.length; j++) {
				int relevantDocID = relevantDocIDs[j];

				System.out.println("\t\t- [" + relevantDocID + "] "
						+ output.getDocumentBody(relevantDocID) + " ("
						+ relevantDocScores[j] + ")");

			}

			System.out.println("\t- Opinion documents:");
			int[] opinionDocIDs = output.getTopicOpinionDocIDs(topicID);
			String[] opinionDocReasons = output
					.getTopicOpinionDocReasons(topicID);
			double[] opinionDocScores = output
					.getTopicOpinionDocScores(topicID);
			for (int j = 0; j < opinionDocIDs.length; j++) {
				int opinionDocID = opinionDocIDs[j];
				System.out.println("\t\t- [" + opinionDocID + "] "
						+ output.getDocumentBody(opinionDocID) + " ("
						+ opinionDocReasons[j] + ", " + opinionDocScores[j]
						+ ")");

			}
		}

	}

	// @Test
	public void testAnalysisKoblenzDiscovery() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");
		// coordinatorWriter.wipeDatabase();
		coordinator.setupWegovDatabase();

		ArrayList<KoblenzAnalysisTopicWrapper> topics = coordinator
				.getKoblenzAnalysisTopicsForRun(0);

		for (KoblenzAnalysisTopicWrapper topicWrapper : topics) {
			WegovAnalysisKoblenzTopic topic = topicWrapper.getTopic();

			int topicID = topic.getTopicID();
			String topicsAsString = topic.getKeyTerms();
			int numMessages = topic.getNumMessages();
			int numUsers = topic.getNumUsers();
			System.out.println("Topic #" + topicID + ", terms: \""
					+ topicsAsString + "\", num messages: " + numMessages
					+ ", num users: " + numUsers);

			System.out.println("\t- Messages:");
			for (WegovAnalysisKoblenzMessage message : topicWrapper
					.getMessages()) {
				System.out.println("\t\t- [" + message.getDocumentID() + "] "
						+ message.getContents() + " (" + message.getScore()
						+ ")");
			}

			System.out.println("\t- Viewpoints:");
			for (WegovAnalysisKoblenzViewpoint viewpoint : topicWrapper
					.getViewPoints()) {
				System.out.println("\t\t- [" + viewpoint.getDocumentID() + "] "
						+ viewpoint.getContents() + " (" + viewpoint.getScore()
						+ ", " + viewpoint.getReason() + ")");
			}

			System.out.println("\t- Users:");
			for (WegovAnalysisKoblenzUser user : topicWrapper.getUsers()) {
				System.out.println("\t\t- " + user.getScreenName());
			}
		}
	}

	// @Test
	public void testAnalysisKoblenz() throws Exception {
		// OfflineDemoInput input = new OfflineDemoInput();

		Coordinator coordinatorReader = new Coordinator(
				"coordinator.properties");
		coordinatorReader.setDatabase("WeGov - lion");
		coordinatorReader
				.setupTwitterDatabaseWithExactName("Twitter 2011-07-20 12:24:20 lion os");

		Coordinator coordinatorWriter = new Coordinator(
				"coordinator.properties");
		coordinatorWriter.wipeDatabase();
		coordinatorWriter.setupWegovDatabase();

		SampleInput input = new SampleInput();
		CharsetEncoder asciiEncoder = (Charset.forName("US-ASCII"))
				.newEncoder();

		LinkedHashMap<Integer, String> tweetIDsAndDocIDs = new LinkedHashMap<Integer, String>();
		LinkedHashMap<Integer, String> userIDsAndAnalysisUserIDs = new LinkedHashMap<Integer, String>();

		SqlSchema dataSchema = coordinatorReader.getDataSchema();

		int counter = 0;
		for (Tweet tweet : (ArrayList<Tweet>) dataSchema.getAll(new Tweet())) {

			String content = tweet.getText();

			// if (counter > 3)
			// break;

			String userID = tweet.getUserID();

			User user = (User) coordinatorReader.getDataSchema()
					.getAllWhere(new User(), "id", userID).get(0);

			String userScreenName = user.getScreenName();

			// content = content.replaceAll("\\s+", " ").replaceAll("\n",
			// " ").replaceAll("\r", " ").replaceAll("\t",
			// " ").replaceAll("OS X", "osx").replaceAll("_",
			// "").replaceAll(" - ", " ").replaceAll("=", "");

			if (asciiEncoder.canEncode(userScreenName)
					& asciiEncoder.canEncode(content)) {
				content = content.replaceAll("\\s+", " ").replaceAll("\n", " ")
						.replaceAll("\r", " ").replaceAll("\t", " ");
				// System.out.println(content);

				String[] tokens = content.split("\\s");

				String nourlsContent = "";
				for (String token : tokens) {
					token = token.trim();
					// if ( (token.startsWith("http://")) |
					// (token.startsWith("@") | token.startsWith("#"))) {
					if ((token.startsWith("http://"))) {
						// System.out.println("Removing token: " + token);
					} else {
						nourlsContent += token + " ";
					}
				}

				// System.out.println(nourlsContent);

				// remove punctuation and small words
				// tokens = nourlsContent.split("[\\s\\p{Punct}]+");
				//
				// String cleanContent = "";
				//
				// for (String token : tokens) {
				// token = token.trim();
				// if (token.length() > 1)
				// cleanContent += token + " ";
				// }

				// cleanContent = cleanContent.trim();

				// System.out.println(cleanContent);

				// filter by number of words
				// if (cleanContent.split("\\s").length > 5)

				input.add(nourlsContent, userScreenName);
				tweetIDsAndDocIDs.put(counter, tweet.getID());
				userIDsAndAnalysisUserIDs.put(counter, userID);
				counter++;

				// counter++;
				// if (counter > 54) {
				// try {
				// TopicOpinionAnalysis analysis = new WegovImporter();
				// TopicOpinionOutput output =
				// analysis.analyzeTopicsOpinions(input);
				// } catch(Exception ex) {
				// System.out.println("BAD: \"" + userScreenName + "\" - \"" +
				// cleanContent + "\"");
				//
				// System.out.println("Doc contents:");
				// for (String inputDoc : input.getDocumentContents()) {
				// System.out.println(inputDoc);
				// }
				//
				// System.out.println("Users:");
				// for (String inputUser : input.getDocumentUsers()) {
				// System.out.println(inputUser);
				// }
				//
				// break;
				// }
				// }

				// System.out.println("GOOD: \"" + userScreenName + "\" - \"" +
				// nourlsContent + "\"");
				// counter++;

			}

			// if (asciiEncoder.canEncode(userScreenName) &
			// asciiEncoder.canEncode(content) & (content.length() > 50) &
			// (!content.contains("$"))) {
			// input.add(content, userScreenName);
			// counter++;
			//
			// if (counter > 984) {
			// try {
			// TopicOpinionAnalysis analysis = new WegovImporter();
			// TopicOpinionOutput output =
			// analysis.analyzeTopicsOpinions(input);
			//
			// } catch(Exception ex) {
			// System.out.println("BAD: \"" + userScreenName + "\" - \"" +
			// content + "\"");
			// break;
			// }
			// }
			//
			// System.out.println("GOOD: \"" + userScreenName + "\" - \"" +
			// content + "\"");
			//
			// }

			// if (counter < 1865) {
			// if (asciiEncoder.canEncode(userScreenName) &
			// asciiEncoder.canEncode(content) & (content.length() > 40)) {
			// System.out.println(counter + " - " +
			// userScreenName.replaceAll("[^\\p{L}\\p{N} ]", "") + ": " +
			// content.replaceAll("[^\\p{L}\\p{N} ]", ""));
			// input.add(content.replaceAll("[^\\p{L}\\p{N} ]", ""),
			// userScreenName.replaceAll("[^\\p{L}\\p{N} ]", ""));
			// counter++;
			// }
			// } else {
			// break;
			// }
		}

		// for (String user : input.getDocumentContents())
		// System.out.println(user);

		// Analysis class
		TopicOpinionAnalysis analysis = new WegovImporter();

		// returned results
		TopicOpinionOutput output = analysis.analyzeTopicsOpinions(input);

		int numTopics = output.getNumTopics();

		SqlSchema dataSchemaWriter = coordinatorWriter.getDataSchema();

		int myRunID = 0;
		int numTopicsToShow = 3;

		for (int topicID = 0; topicID < numTopicsToShow; topicID++) {

			String topicsAsString = "";
			for (String keyTerm : output.getTopicTerms(topicID)) {
				topicsAsString += keyTerm + ", ";
			}

			if (topicsAsString.length() > 3)
				topicsAsString = topicsAsString.substring(0,
						topicsAsString.length() - 2);

			System.out.println("Topic #" + topicID + ", terms: "
					+ topicsAsString + ", num messages: "
					+ output.getTopicRelevantDocIDs(topicID).length
					+ ", num users: " + output.getTopicUsers(topicID).length);
			dataSchemaWriter.insertObject(new WegovAnalysisKoblenzTopic(
					topicID, topicsAsString, output
							.getTopicRelevantDocIDs(topicID).length, output
							.getTopicUsers(topicID).length, myRunID));

			// Save topics per run
			// Topics have: terms, num messages, num users, run ID, topic ID
			// Linked to: messages, viewpoints, users
			// Message: run, topic number, docid, tweet id, score, message ID
			// Viewpoint: run, topic number, docid, tweet id, score, reason,
			// User: run, topic number, SnsUserAccount, tweet contents

			System.out.println("\t- Key terms:");
			for (String keyTerm : output.getTopicTerms(topicID)) {
				System.out.println("\t\t- " + keyTerm);
			}

			System.out.println("\t- Key users:");
			int count = 0;
			for (String keyUser : output.getTopicUsers(topicID)) {
				System.out.println("\t\t- " + keyUser);

				// public WegovAnalysisKoblenzUser(int analysisUserID, String
				// screenName, String fullName,
				// String profilePictureUrl, String moreLinkUrl, String
				// followLinkUrl, int topicID, int outputOfRunID) {
				// User theUser = (User) dataSchema.getAllWhere(new User(),
				// "Screen_name", keyUser).get(0);
				dataSchemaWriter.insertObject(new WegovAnalysisKoblenzUser(
						count, keyUser, keyUser, "#", "http://twitter.com/#!/"
								+ keyUser, "#", topicID, myRunID));
				count++;
			}

			System.out.println("\t- Relevant documents:");
			int[] relevantDocIDs = output.getTopicRelevantDocIDs(topicID);
			double[] relevantDocScores = output
					.getTopicRelevantDocScores(topicID);
			for (int j = 0; j < relevantDocIDs.length; j++) {
				int relevantDocID = relevantDocIDs[j];
				// public WegovAnalysisKoblenzMessage(int messageID, int
				// documentID, String originalPostID,
				// String contents, String originalPostURL, String userID,
				// String hostSnsID, String score, int topicID, int
				// outputOfRunID) {

				// String tweetID = tweetIDsAndDocIDs.get(relevantDocID);
				// Tweet theTweet = (Tweet) dataSchema.getAllWhere(new Tweet(),
				// "id", tweetID).get(0);
				// String userID = theTweet.getUserID();
				// User theUser = (User) dataSchema.getAllWhere(new User(),
				// "id", userID).get(0);
				//
				// String tweetContents = theTweet.getText();
				// String tweetUrl = "http://twitter.com/#!/" +
				// theUser.getScreenName() + "/status/" + tweetID;

				System.out.println("\t\t- [" + relevantDocID + "] "
						+ output.getDocumentBody(relevantDocID) + " ("
						+ relevantDocScores[j] + ")");
				// System.out.println("\t\t- " + tweetContents);
				dataSchemaWriter.insertObject(new WegovAnalysisKoblenzMessage(
						j, relevantDocID, "321", output
								.getDocumentBody(relevantDocID), "#", "123",
						"twitter", Double.toString(relevantDocScores[j]),
						topicID, myRunID));
			}

			System.out.println("\t- Opinion documents:");
			int[] opinionDocIDs = output.getTopicOpinionDocIDs(topicID);
			String[] opinionDocReasons = output
					.getTopicOpinionDocReasons(topicID);
			double[] opinionDocScores = output
					.getTopicOpinionDocScores(topicID);
			for (int j = 0; j < opinionDocIDs.length; j++) {
				int opinionDocID = opinionDocIDs[j];
				System.out.println("\t\t- [" + opinionDocID + "] "
						+ output.getDocumentBody(opinionDocID) + " ("
						+ opinionDocReasons[j] + ", " + opinionDocScores[j]
						+ ")");

				dataSchemaWriter
						.insertObject(new WegovAnalysisKoblenzViewpoint(j,
								opinionDocID, "321", output
										.getDocumentBody(opinionDocID), "#",
								"123", "twitter", Double
										.toString(opinionDocScores[j]),
								opinionDocReasons[j], topicID, myRunID));

			}
		}

		//
		// WegovRender.showResults(output);

	}

	// @Test
	public void testSampleData() throws Exception {
		Coordinator coordinatorReader = new Coordinator(
				"coordinator.properties");
		Coordinator coordinatorWriter = new Coordinator(
				"coordinator.properties");
		coordinatorReader.setDatabase("WeGov - lion");
		coordinatorReader
				.setupTwitterDatabaseWithExactName("Twitter 2011-07-20 12:24:20 lion os");

		// for (int id :
		// coordinator.getConnector().getIDColumnValuesWhere(coordinator.getDatabase(),
		// coordinator.getMgtSchema(),
		// new WegovParameter(), "ID", "Name", "tweetID", "ParameterID",
		// new WegovConfiguration_Parameter(), "ConfigurationID", 10)) {
		// System.out.println(id);
		// }

		// System.out.println(coordinator.getConfigurationSetByID(1).get(0).getParameterByName("snsToQuery"));

		SqlSchema dataSchema = coordinatorReader.getDataSchema();

		// WegovPostItem myTweet = (WegovPostItem)
		// coordinatorReader.getDataSchema().getAllWhere(new WegovPostItem(),
		// "ID", "27836852555751424").get(0);
		// String userID = myTweet.getAuthor_SnsUserAccount_ID();
		// WegovSnsUserAccount user = (WegovSnsUserAccount)
		// coordinatorReader.getDataSchema().getAllWhere(new
		// WegovSnsUserAccount(), "ID", userID).get(0);
		// user.getFullName();

		coordinatorWriter.wipeDatabase();
		coordinatorWriter.setupWegovDatabase();

		// write Twitter
		coordinatorWriter.getDataSchema()
				.insertObject(
						new WegovSNS("1", "Twitter", "http://api.twitter.com/",
								"#", 0));
		coordinatorWriter.getDataSchema().insertObject(
				new WegovSNS("2", "Facebook",
						"code.google.com/p/facebook-java-api/", "#", 0));
		coordinatorWriter.getDataSchema().insertObject(
				new WegovSNS("3", "Youtube", "code.google.com/apis/youtube/",
						"#", 0));

		// write tweets
		for (Tweet tweet : (ArrayList<Tweet>) dataSchema.getAll(new Tweet())) {
			String tweetID = tweet.getID();
			String userID = tweet.getUserID();
			Timestamp dateCreated = tweet.getTimeCreatedAsTimestamp();
			String content = tweet.getText();
			String retweeted = tweet.getRetweeted();

			String userScreenName = ((User) coordinatorReader.getDataSchema()
					.getAllWhere(new User(), "id", userID).get(0))
					.getScreenName();
			// WegovPostItem postItem = new WegovPostItem(tweetID, userID, "1",
			// dateCreated, content, content, "http://twitter.com/#!/" +
			// userScreenName + "/status/" + tweetID, Short.parseShort("0"),
			// dateCreated, 0);
			//
			// coordinatorWriter.getDataSchema().insertObject(postItem);
			// System.out.println(postItem);
		}

		// write users
		for (User user : (ArrayList<User>) dataSchema.getAll(new User())) {
			String userID = user.getID();
			String name = user.getName();
			String location = user.getLocation();
			String url = user.getUrl();
			String profileImageUrl = user.getProfileImageUrl();
			Timestamp dateCreated = user.getTimeCreatedAsTimestamp();
			String screenName = user.getScreenName();
			String profileUrl = "http://twitter.com/#!/" + screenName;

			// WegovSnsUserAccount snsUser = new WegovSnsUserAccount(userID,
			// name, location, url, profileUrl, profileImageUrl, "1",
			// dateCreated, screenName, 0, 0, 0, 0, dateCreated, 0);
			//
			// coordinatorWriter.getDataSchema().insertObject(snsUser);
			// System.out.println(snsUser);
		}

		// write tags (hashtags)
		for (Hashtag hashtag : (ArrayList<Hashtag>) dataSchema
				.getAll(new Hashtag())) {
			String text = hashtag.getText();
			String postID = hashtag.getIn_tweet_id();

			WegovTag tag = new WegovTag("n/a", text, 0);

			String countid = coordinatorWriter.getDataSchema()
					.insertObject(tag);
			coordinatorWriter.getDataSchema().insertObject(
					new WegovPostTag(countid, postID, 0));
			System.out.println(tag);
		}
	}

	// @Test
	public void testNewSelects() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");
		coordinator.setupWegovDatabase();

		// for (int id :
		// coordinator.getConnector().getIDColumnValuesWhere(coordinator.getDatabase(),
		// coordinator.getMgtSchema(),
		// new WegovParameter(), "ID", "Name", "tweetID", "ParameterID",
		// new WegovConfiguration_Parameter(), "ConfigurationID", 10)) {
		// System.out.println(id);
		// }

		System.out.println(coordinator.getConfigurationSetByID(1).get(0)
				.getParameterByName("snsToQuery"));
	}

	// @Test
	public void testInputs() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");
		coordinator.wipeDatabase(); // optional - removes everything from the
									// database
		coordinator.setupWegovDatabase();

		String maxFullName = "Maxim Bashevoy";
		String maxOrganisation = "IT Innovation";
		String maxUserName = "mbashevoy";
		String maxPassword = "password";
		Role maxRole = coordinator.getDefaultAdminRole();

		Policymaker max = coordinator.createPolicyMaker(maxFullName, maxRole,
				maxOrganisation, maxUserName, maxPassword);

		Worksheet worksheet1 = coordinator.createWorksheet(max,
				"First worksheet", "First worksheet to run Runs!");

		for (ConfigurationSet set : coordinator.getConfigurationSets()) {
			System.out.println(set);
		}

		Activity searchActivity = worksheet1.createActivity("Search activity",
				"Search activity to return some results");
		searchActivity.setConfigurationSet(coordinator
				.getConfigurationSetByID(1));

		Activity analysisActivity = coordinator.createActivity(max,
				"Koblenz analysis activity", "Analysis",
				coordinator.getConfigurationSetByID(3));
		worksheet1.addActivity(analysisActivity);

		ArrayList<String> runs = new ArrayList<String>();
		runs.add("1");
		runs.add("345");
		runs.add("33");

		analysisActivity.addSelectedRunsAsInputFromActivity(searchActivity,
				runs);
		analysisActivity.addAllRunsAsInputFromActivity(worksheet1
				.createActivity("Random activity",
						"Search activity to return some results"));
		analysisActivity.addLastRunAsInputFromActivity(worksheet1
				.createActivity("Random 2 activity",
						"Search activity to return some results"));

		// analysisActivity.removeAllInputs();
		// analysisActivity.removeInputsOfActivity(searchActivity);

		LinkedHashMap<Integer, String> inputs = analysisActivity.getInputs();

		Iterator it = inputs.keySet().iterator();

		while (it.hasNext()) {
			int inputActivityID = (Integer) it.next();
			String whichRuns = inputs.get(inputActivityID);

			System.out.println("Input activity ID: " + inputActivityID
					+ ", which runs: " + whichRuns);
		}

		// for (ConfigurationSet set : coordinator.getTools()) {
		// System.out.println(set.toString());
		// }

		// for (Activity activity : worksheet1.getActivities()) {
		// System.out.println(activity);
		// }
		//

		// SchedulerFactory sf = new StdSchedulerFactory();
		// Scheduler sched = sf.getScheduler();

		// JobDetail job = worksheet1.getJobDetail();
		// Trigger trigger = newTrigger().withIdentity("trigger1",
		// "group1").startNow().build();
		//
		// sched.scheduleJob(job, trigger);
		//

		/*
		 * worksheet1.start(); // do { Thread.sleep(1000);
		 * System.out.println("Worksheet status: " + worksheet1.getStatus());
		 * for (Activity activity : worksheet1.getActivities()) {
		 * System.out.println("\t- Activity name: \'" + activity.getName() +
		 * "\', status: \'" + activity.getStatus() + "\'"); }
		 * 
		 * } while (!worksheet1.isDone()); // // sched.shutdown(true); // for
		 * (Activity activity : worksheet1.getActivities()) {
		 * System.out.println("Activity name: \'" + activity.getName() +
		 * "\', status: \'" + activity.getStatus() + "\'"); Run lastRun =
		 * activity.getLastRun();
		 * 
		 * System.out.println("Logs:"); System.out.println(lastRun.getLogs());
		 * 
		 * System.out.println("Errors:");
		 * System.out.println(lastRun.getErrors());
		 * 
		 * }
		 */

	}

	// @Test
	public void testWegovFollower() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");
		coordinator.wipeDatabase(); // optional - removes everything from the
									// database
		coordinator.setupWegovDatabase();

		Util util = new Util();

		coordinator.getDataSchema().insertObject(
				new WegovFollower("123", "456", util.getTimeNowAsTimestamp(),
						null, 1));

		ArrayList<WegovFollower> followers = coordinator.getDataSchema()
				.getAll(new WegovFollower());

		for (WegovFollower follower : followers) {
			System.out.println(follower.getStartDate());
		}

	}

	// @Test

	public void testEquals() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");
		coordinator.wipeDatabase(); // optional - removes everything from the
									// database
		coordinator.setupWegovDatabase();

		ArrayList<Role> roles = coordinator.getRoles();
		ConfigurationSet searchConfigurationSet = coordinator
				.createConfigurationSet("Search",
						"Twitter search using a keyword", null);
		ConfigurationSet analysisConfigurationSet = coordinator
				.createConfigurationSet("Analysis",
						"Find most influential tweets", null);

		Configuration searchConfiguration = coordinator.createConfiguration(
				"Search Twitter", "java -version", "Test twitter search", null);
		Configuration analysisConfiguration = coordinator.createConfiguration(
				"Analyse Tweets", "java -version", "Test twitter analysis",
				null);

		searchConfiguration.addParameter("searchQuery", "What to search for",
				"London Riots", roles.get(0));
		searchConfiguration.addParameter("snsToQuery", "Which SNS to use",
				"Twitter", roles);
		analysisConfiguration.addParameter("numberOfTweetsToReturn",
				"How many top tweets to return", "10", roles);

		searchConfigurationSet.addConfiguration(searchConfiguration);
		analysisConfigurationSet.addConfiguration(analysisConfiguration);

		ConfigurationSet searchConfigurationSet1 = searchConfigurationSet
				.clone();
		// for (Configuration c : searchConfigurationSet1.getConfigurations())
		// System.out.println(c);

		searchConfigurationSet1.getConfigurationByID(5).getParameterByID(7)
				.addRole(coordinator.createRole("tester", "wanker"));

		System.out.println(coordinator.getConfigurationSetByID(1).equals(
				searchConfigurationSet1));
	}

	// @Test
	public void testRuns() throws Exception {

		Coordinator coordinator = new Coordinator("coordinator.properties");
		coordinator.wipeDatabase(); // optional - removes everything from the
									// database
		coordinator.setupWegovDatabase();

		// coordinator.getConfigurationSetByID(3).getConfigurations().get(0).setCommand("java -jar /Users/max/Desktop/wegov-coordinator-testjar.jar");

		String maxFullName = "Maxim Bashevoy";
		String maxOrganisation = "IT Innovation";
		String maxUserName = "mbashevoy";
		String maxPassword = "password";
		Role maxRole = coordinator.getDefaultAdminRole();

		Policymaker max = coordinator.createPolicyMaker(maxFullName, maxRole,
				maxOrganisation, maxUserName, maxPassword);
		// Policymaker max = coordinator.getPolicymakerByUsername("mbashevoy");
		// Activity a = coordinator.createActivity(max, "Second activity",
		// "Some description", coordinator.getConfigurationSetByID(3));

		Worksheet worksheet1 = coordinator.createWorksheet(max,
				"First worksheet", "First worksheet to run Runs!");

		Activity worksheet1Activity = worksheet1.createActivity(
				"First activity", "First activity with runs!");
		worksheet1Activity.setConfigurationSet(coordinator
				.getConfigurationSetByID(3));

		// SchedulerFactory sf = new StdSchedulerFactory();
		// Scheduler sched = sf.getScheduler();
		//
		// JobDetail job = worksheet1.getJobDetail();
		// Trigger trigger = newTrigger().withIdentity("trigger1",
		// "group1").startNow().build();
		//
		// sched.scheduleJob(job, trigger);
		//
		// sched.start();

		// worksheet1.addActivity(a);

		// worksheet1.startThreaded();
		// // worksheet1.scheduleAtFixedRate(0, 3, TimeUnit.SECONDS);
		//
		// do {
		// Thread.sleep(1000);
		// System.out.println("Worksheet status: " + worksheet1.getStatus());
		// for (Activity activity : worksheet1.getActivities()) {
		// System.out.println("\t- Activity name: \'" + activity.getName() +
		// "\', status: \'" + activity.getStatus() + "\'");
		// }
		//
		// } while (!worksheet1.isDone());

		// // again!
		// worksheet1.startThreaded();
		//
		do {
			Thread.sleep(1000);
			System.out.println("Worksheet status: " + worksheet1.getStatus());
			for (Activity activity : worksheet1.getActivities()) {
				System.out.println("\t- Activity name: \'" + activity.getName()
						+ "\', status: \'" + activity.getStatus() + "\'");
			}

		} while (!worksheet1.isDone());

		// sched.shutdown(true);
		//
		// for (Activity activity : worksheet1.getActivities()) {
		// System.out.println("Activity name: \'" + activity.getName() +
		// "\', status: \'" + activity.getStatus() + "\'");
		// for (Run run : activity.getRuns()) {
		// ArrayList<WegovPostItem> data = run.getData(new WegovPostItem());
		// for (WegovPostItem item : data)
		// System.out.println("\t- " + "Run [" + run.getID() + "], data: " +
		// item.getContent());
		// }
		// }

		// System.out.println("All configuration sets:");
		// for (ConfigurationSet set : coordinator.getConfigurationSets()) {
		// System.out.println(set);
		//
		// for (Configuration config : set.getConfigurations()) {
		// System.out.println("\t- " + config);
		// }
		// }
		//
		// System.out.println("Just Tools (Configuration sets without activities):");
		// for (ConfigurationSet set : coordinator.getTools()) {
		// System.out.println(set);
		//
		// for (Configuration config : set.getConfigurations()) {
		// System.out.println("\t- " + config);
		// }
		// }
		//
		// System.out.println("Tools configurations (Configurations without configuration sets):");
		// for (Configuration config : coordinator.getToolsConfigurations()) {
		// System.out.println(config);
		// }
		//
		// System.out.println("All activities:");
		// for (Activity activity : coordinator.getActivities()) {
		// System.out.println(activity);
		// }
		//
		// a.setConfigurationSet(coordinator.getConfigurationSetByID(1));
		//
		// System.out.println("All configuration sets:");
		// for (ConfigurationSet set : coordinator.getConfigurationSets()) {
		// System.out.println(set);
		//
		// for (Configuration config : set.getConfigurations()) {
		// System.out.println("\t- " + config);
		// }
		// }

		// System.out.println("All activities:");
		// for (Activity activity : coordinator.getActivities()) {
		// System.out.println(activity);
		// }

		// Activity activity1 = coordinator.getActivityByID(1);
		// activity1.startThreaded();
		//
		// do {
		// Thread.sleep(1000);
		// System.out.println("Activity [" + activity1.getID() + "] " +
		// activity1.getName() + ", status: " + activity1.getStatus());
		// // for (Run startThreaded : activity1.getRuns())
		// System.out.println("\t- " + activity1.getLastRun());
		// // System.out.println("\t\t- " + startThreaded);
		//
		// } while(!activity1.isDone());

		// Activity activity1 = worksheet1.getActivities().get(0);
		//
		// System.out.println("Last startThreaded logs:");
		// System.out.println(activity1.getLastRun().getLogs());
		// System.out.println("Last startThreaded errors:");
		// System.out.println(activity1.getLastRun().getErrors());
		// System.out.println("Last startThreaded data:");
		//
		// ArrayList<WegovPostItem> data = activity1.getLastRun().getData(new
		// WegovPostItem());
		//
		// for (WegovPostItem postItem : data)
		// System.out.println(postItem.toString());
		//
		// // again!
		// activity1.startThreaded();
		//
		// do {
		// Thread.sleep(1000);
		// System.out.println("Activity [" + activity1.getID() + "] " +
		// activity1.getName() + ", status: " + activity1.getStatus());
		// //// for (Run startThreaded : activity1.getRuns())
		// System.out.println("\t- " + activity1.getLastRun());
		//
		// } while(!activity1.isDone());
		//
		// data = activity1.getLastRun().getData(new WegovPostItem());
		//
		// for (WegovPostItem postItem : data)
		// System.out.println(postItem.toString());

	}

	// @Test
	public void testForCoordinatorJavadoc() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");
		coordinator.wipeDatabase(); // optional - removes everything from the
									// database
		coordinator.setupWegovDatabase();

		// Create new policymaker Max as administrator
		String maxFullName = "Maxim Bashevoy";
		String maxOrganisation = "IT Innovation";
		String maxUserName = "mbashevoy";
		String maxPassword = "password";
		Role maxRole = coordinator.getDefaultAdminRole();

		Policymaker max = coordinator.createPolicyMaker(maxFullName, maxRole,
				maxOrganisation, maxUserName, maxPassword);

		System.out.println(max.getUserName());

		// Assign Max new custom role
		Role testRole = coordinator.createRole("tester", "Just a test role");
		max.addRole(testRole);

		// Print info about Max
		System.out.println("Information about user max:");
		System.out.println("\t-" + max);

		// Check Max's password
		String randomPassword = "123";
		String correctPassword = maxPassword;
		System.out.println("Is max\'s password \'" + randomPassword + "\'? "
				+ max.isPassword(randomPassword));
		System.out.println("Is max\'s password \'" + correctPassword + "\'? "
				+ max.isPassword(correctPassword));

		// Create new user Tom with default user role:
		Policymaker tom = coordinator.createPolicyMaker("Tom Smith",
				"Microsoft", "tom123", "qwerty");
		System.out.println("Information about user tom:");
		System.out.println("\t-" + tom);

		// Print information about all users:
		System.out.println("Information about all users:");
		for (Policymaker pm : coordinator.getPolicymakers()) {
			System.out.println("\t-" + pm);
		}

		// Max creates new worksheet
		Worksheet worksheet1 = coordinator.createWorksheet(max,
				"Test worksheet", "Description of new worksheet");

		// Max creates new activities:
		Activity activity1 = worksheet1.createActivity("Search activity",
				"We are going to search for something here..."); // Added to
																	// worksheet
																	// straight
																	// away
		Activity activity2 = coordinator.createActivity(max,
				"Analysis activity", "...and then do analysis!");
		worksheet1.addActivity(activity2); // only needed for activity2 as it
											// was created via coordinator, not
											// worksheet

		// Max adds new configuration to activity1
		Configuration configuration1 = coordinator.createConfiguration(
				"Search Twitter tool", "java -version",
				"Configuration for Search Twitter Tool - test only", null);
		configuration1.addParameterAsUser("searchQuery",
				"What goes into the search box", "bbc");
		configuration1.addParameterAsAdmin("returnFormat",
				"Do we want Twitter to return XML or JSON?", "json");
		activity1.addConfiguration(configuration1);

		// Tom creates new worksheet
		Worksheet worksheet2 = coordinator.createWorksheet(tom,
				"Another Test worksheet",
				"Description of another new worksheet");

		// Print information about all worksheets:
		System.out.println("Information about all worksheets:");
		for (Worksheet ws : coordinator.getWorksheets()) {
			System.out.println("\t-" + ws);
			System.out.println("\t\t- Created by: " + ws.getPolicyMaker());
			System.out.println("\t\t- Activities: ");

			for (Activity activity : ws.getActivities()) {
				System.out.println("\t\t\t- "
						+ activity
								.toString()
								.replaceAll("- Configuration",
										"\t\t\t- Configuration")
								.replaceAll("- Parameter",
										"\t\t\t\t- Parameter"));
			}
		}

		System.out.println("Parameters in configuration1 for role admin:");
		for (Parameter p : activity1.getConfigurationSet().get(0)
				.getParametersForRole(coordinator.getDefaultAdminRole())) {
			System.out.println("\t- " + p);
		}

		// for (ConfigurationSet set : coordinator.getConfigurationSets())
		System.out.println(coordinator.getConfigurationSetByID(2));

		assertTrue(true);
	}

	// @Test
	public void testConfigurations() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");
		coordinator.wipeDatabase();
		coordinator.setupWegovDatabase();

		// ConfigurationSet configSet1 = new ConfigurationSet("Test one",
		// "Test configuration set #1", coordinator);
		//
		// System.out.println(configSet1);
		//
		Configuration config1 = new Configuration("Test configuration",
				"java -version", "Test description here", "testJsp",
				coordinator);
		config1.addParameter("searchFor", "What to search on Twitter for",
				"bbc", coordinator.getDefaultUserRole());
		config1.addRoleToParameter("searchFor",
				coordinator.getDefaultAdminRole());

		config1.addParameterAsAdmin("numTweets",
				"Number of tweets to search for", "10");
		config1.addParameterAsUser("inputsFromActivity",
				"Which activity to take input from", "2");

		System.out.println(config1);

		Configuration config2 = new Configuration("Test 2 configuration",
				"java 2 -version", "Test 2 description here", "testNewJsp",
				coordinator);
		config2.addParameter("searchFor", "What to search on Twitter for",
				"bbc", coordinator.getDefaultUserRole());
		config2.addParameterAsAdmin("bunnies",
				"Number of 123 tweets to search for", "12222220");

		// configSet1.addConfiguration(config1);
		// configSet1.addConfiguration(config2);
		//
		// System.out.println(configSet1);
		//
		// for (int i = 0; i < configSet1.size(); i++) {
		// System.out.println(configSet1.get(i));
		// }

		Configuration config3 = config2.clone();

		config2.getParameterByName("bunnies").setDescription(
				"are we there yet?");

		System.out.println(config2);
		System.out.println(config3);

		config3.setParameters(config1.getParameters());
		config3.setRendererJsp("newJsp");
		System.out.println(config3);

		//
		// System.out.println("All parameters:");
		// for (Parameter p : config.getParameters())
		// System.out.println("\t-" + p);
		//
		// System.out.println("All parameters for user:");
		// for (Parameter p :
		// config.getParametersForRole(coordinator.getDefaultUserRole()))
		// System.out.println("\t-" + p);
		//
		// System.out.println("All parameters for admin:");
		// for (Parameter p :
		// config.getParametersForRole(coordinator.getDefaultAdminRole()))
		// System.out.println("\t-" + p);
		//
		// Activity activity1 = coordinator.createActivity(new
		// Policymaker("Maxim Bashevoy", "IT Innovation", "max", "test",
		// coordinator), "Test activity", "A comment");
		//
		// activity1.addConfiguration(config2);
		// activity1.addConfiguration(config1);
		//
		// ConfigurationSet configSet2 = activity1.getConfigurationSet();
		//
		// System.out.println(activity1);
		//
		// System.out.println(configSet2);
		//
		// for (int i = 0; i < configSet2.size(); i++) {
		// System.out.println(configSet2.get(i));
		// }

		assertTrue(true);
	}

	// @Test
	public void testNewPolicymakers() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");
		coordinator.wipeDatabase();
		coordinator.setupWegovDatabase();

		Policymaker max = coordinator.createPolicyMaker("Maxim Bashevoy",
				"IT Innovation", "maximba", "password");
		max.addRole(new Role("admin", "", coordinator));
		max.addRole(new Role("tester", "To test cool things!", coordinator));

		Policymaker bill = coordinator.createPolicyMaker("Bill Gates",
				new Role("user", "", coordinator), "Microsoft", "billy", "123");

		ArrayList<Policymaker> pms = coordinator.getPolicymakers();

		System.out.println("All policymakers:");
		for (Policymaker pm : pms) {
			System.out.println(pm);
		}

		System.out.println("All roles:");
		ArrayList<Role> allRoles = coordinator.getRoles();
		for (Role role : allRoles) {
			System.out.println(role);
		}

		ArrayList<Role> maxsRoles = max.getRoles();
		System.out.println("Max\'s roles:");
		for (Role role : maxsRoles) {
			System.out.println(role);
		}

		System.out.println("Account found by username:");
		System.out.println(coordinator.getPolicymakerByUsername("billy"));

		Worksheet ws = coordinator.createWorksheet(max, "Test worksheet",
				"Pockets are comfy - will do as description");

		System.out.println(ws);
		System.out.println(ws.getPolicyMaker());

		Activity activity = coordinator.createActivity(bill, "Test activity",
				"Some vague description describing something");

		coordinator.createActivity(max, "Test activity by max", "Nothing here");
		coordinator.createActivity(max, "Test activity by max - another one?",
				"Nothing here either");
		coordinator.createActivity(max, "Test activity by max - 3",
				"Nothing here 1 either");
		coordinator.createActivity(max, "Test activity by max - 4",
				"Nothing here 2 either");

		System.out.println(activity);
		System.out.println(activity.getPolicyMaker());

		ArrayList<Activity> allActivities = coordinator.getActivities();

		System.out.println("All activities:");
		for (Activity a : allActivities) {
			System.out.println(a);
			System.out.println("\t- by: " + a.getPolicyMaker());
		}

		System.out.println("All activities by Max:");
		for (Activity a : coordinator.getActivitiesByPolicymaker(max)) {
			System.out.println(a);
			System.out.println("\t- by: " + a.getPolicyMaker());
		}

		assertTrue(true);
	}

	// @Test
	public void testRoles() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");
		coordinator.wipeDatabase();
		coordinator.setupWegovDatabase();

		// Role role1 = coordinator.createRole("random", "what the fuck?");
		//
		// System.out.println(role1);
		// System.out.println(role1.getID());
		//
		// System.out.println(role1.getName());
		// System.out.println(role1.getDescription());
		//
		// role1.setName("new name");
		// role1.setDescription("looks optional");
		//
		// System.out.println(role1.getName());
		// System.out.println(role1.getDescription());
		//
		// System.out.println(coordinator.getDefaultUserRole());
		// System.out.println(coordinator.getDefaultAdminRole());

		assertTrue(true);
	}

	// @Test
	public void testPolicymakers() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");
		coordinator.wipeDatabase();
		coordinator.setupWegovDatabase();

		// System.out.println(coordinator.addRoleIfDoesNotExist("test",
		// "test role"));
		// System.out.println(coordinator.addRoleIfDoesNotExist("admin",
		// "test role"));
		//
		// ArrayList<WegovPolicymakerRole> roles = coordinator.getRoles();
		//
		// for (WegovPolicymakerRole role : roles) {
		// System.out.println(role.getName() + ": " + role.getDescription());
		// }

		Role role1 = coordinator.createRole("random", "what the fuck?");

		assertNull(coordinator.getRoleByName("asd"));
		assertNotNull(coordinator.getRoleByName("user"));

		Policymaker pm1 = coordinator.createPolicyMaker("Maxim Bashevoy",
				coordinator.getDefaultUserRole(), "IT Innovation", "max",
				"test");
		Policymaker pm2 = coordinator.createPolicyMaker("Someone you know",
				role1, "ACME", "someone", "another");

		pm1.addRole(new Role("coke", "diet coke!", coordinator));
		pm1.addRole(new Role("oeas", "blah", coordinator));
		pm1.addRole(new Role("something else", "blah blah", coordinator));

		ArrayList<Role> roles = pm1.getRoles();

		System.out.println("Roles before:");
		for (Role role : roles) {
			System.out.println("\t-" + role.getName() + ": "
					+ role.getDescription());
		}

		pm1.removeRole(new Role("oeas", "blah", coordinator));

		roles = pm1.getRoles();

		pm2.setPassword("1test");

		System.out.println("Roles after:");
		for (Role role : roles) {
			System.out.println("\t-" + role.getName() + ": "
					+ role.getDescription());
		}

		ArrayList<Policymaker> pms = role1.getPolicyMakers();

		System.out.println("Policymakers for role: " + role1);
		for (Policymaker pm : pms) {
			System.out.println(pm);
		}

		System.out.println(pm1.isPassword("test"));
		System.out.println(pm2.isPassword("another"));
		System.out.println(pm2.isPassword("1test"));

		// System.out.println((new Util()).makeShaHash("123"));
		// System.out.println((new Util()).isHashMatch("123",
		// "40bd001563085fc35165329ea1ff5c5ecbdbbeef"));
		assertTrue(true);
	}

	// @Test
	public void testWorksheets() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");
		coordinator.wipeDatabase();
		coordinator.setupWegovDatabase();

		Policymaker pm1 = coordinator.createPolicyMaker("Maxim Bashevoy",
				coordinator.getDefaultUserRole(), "IT Innovation", "max",
				"test");
		Policymaker pm2 = coordinator.createPolicyMaker("Bill Gates", new Role(
				"user", "", coordinator), "Microsoft", "billy", "123");

		Worksheet ws1 = coordinator.createWorksheet(pm1, "First worksheet",
				"Some test comment");
		Worksheet ws2 = coordinator.createWorksheet(pm1, "Second worksheet",
				"2 Some test comment");

		Activity activity1 = coordinator.createActivity(pm1, "Some activity",
				"Dis be da comment mon");
		Activity activity2 = coordinator.createActivity(pm2, "Some 2 activity",
				"2 Dis be da comment mon");

		ConfigurationSet set1 = coordinator.createConfigurationSet(
				"Test set #1", "Test configuration set number 1", null);
		set1.addConfiguration(new Configuration("Configuration 1",
				"java /command1", "Test configuration #1", "render1",
				coordinator));
		set1.addConfiguration(new Configuration("Configuration 2",
				"java /command2", "Test configuration #2", "render2",
				coordinator));

		ConfigurationSet set2 = coordinator.createConfigurationSet(
				"Test set #2", "Test configuration set number 2", null);
		set2.addConfiguration(new Configuration("Configuration 3",
				"java /command3", "Test configuration #3", "render3",
				coordinator));
		set2.addConfiguration(new Configuration("Configuration 4",
				"java /command4", "Test configuration #4", "render4",
				coordinator));
		set2.addConfiguration(new Configuration("Configuration 5",
				"java /command5", "Test configuration #5", "render5",
				coordinator));

		activity1.setConfigurationSet(set1);
		activity2.setConfigurationSet(set2);

		ws1.addActivity(activity1);
		// ws2.addActivity(activity1);
		ws1.addActivity(activity2);
		// ws1.createActivity("new activity", "Created from workflow 1!");

		System.out.println("Activities of worksheet: " + ws1);
		for (Activity activity : ws1.getActivities()) {
			System.out.println("Activity [" + activity.getID() + "]");
			System.out.println("\t- Configuration Set: "
					+ activity.getConfigurationSet().getID());
			// System.out.println("\t-" + activity + " by " +
			// activity.getPolicyMaker());
			// for (Worksheet worksheet : activity.getWorksheets()) {
			// System.out.println("\t\t-belongs to worksheet: " + worksheet);
			// }
		}

		System.out.println("Tools:");
		ArrayList<ConfigurationSet> tools = coordinator.getTools();

		if (tools.isEmpty())
			System.out.println("No tools found");
		else {
			System.out.println("Current tools:");

			for (ConfigurationSet set : tools) {
				System.out.println(set);
			}
		}

		System.out.println("Worksheet 1 as Configuration Set:");
		ConfigurationSet wsAsCS = ws1.toConfigurationSet();
		System.out.println(wsAsCS.toString());

		for (Configuration conf : wsAsCS.getConfigurations()) {
			System.out.println(conf.toString());
		}

		tools = coordinator.getTools();

		if (tools.isEmpty())
			System.out.println("No tools found");
		else {
			System.out.println("Current tools:");

			for (ConfigurationSet set : tools) {
				System.out.println(set);
			}
		}

		// ws1.startThreaded();
		//
		// int counter = 0;
		// String ws1Status = ws1.getStatus();
		//
		// do {
		// Thread.sleep(2000);
		//
		// ws1Status = ws1.getStatus();
		//
		// System.out.println("*********************** #" + counter +
		// " ***************************");
		// System.out.println("Status: " + ws1Status);
		//
		// for (Activity activity : ws1.getActivities()) {
		// System.out.println("\t-" + activity.getName() + ": " +
		// activity.getStatus());
		// }
		//
		// counter++;
		//
		// } while(!ws1.isDone());

		assertTrue(true);
	}

	// @Test
	public void testWorkflow() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");
		coordinator.wipeDatabase();
		coordinator.setupWegovDatabase();

		// WegovPolicymaker policyMaker1 =
		// coordinator.createPolicyMaker("Mystery Guest", "Apple");
		// WegovPolicymaker policyMaker2 =
		// coordinator.createPolicyMaker("Max teh tester", "IT Innovation");
		//
		// Workflow wf1 = coordinator.createWorkflow(policyMaker1,
		// "Test thing one");
		// Workflow wf2 = coordinator.createWorkflow(policyMaker2,
		// "Test thing two");
		//
		// Task task1 = coordinator.createTask(policyMaker1, "Task #1",
		// "Awesome type",
		// "java -jar diet coke");
		//
		// Task task2 = coordinator.createTask(policyMaker2, "Task #2",
		// "Some type",
		// "java -jar /Users/max/Documents/Work/wegov/UpdateTwitterUserDetails/dist/UpdateTwitterUserDetails.jar");
		//
		// Task task3 = coordinator.createTask(policyMaker2, "Task #3",
		// "Some type",
		// "java -jar /Users/max/Documents/Work/wegov/UpdateTwitterUserDetails/dist/UpdateTwitterUserDetails.jar");
		//
		// Task task4 = coordinator.createTask(policyMaker2, "Task #4",
		// "Some type",
		// "java -jar /Users/max/Documents/Work/wegov/UpdateTwitterUserDetails/dist/UpdateTwitterUserDetails.jar");
		//
		// Task task5 = coordinator.createTask(policyMaker2, "Task #5",
		// "Some type",
		// "java -jar /Users/max/Documents/Work/wegov/UpdateTwitterUserDetails/dist/UpdateTwitterUserDetails.jar");
		//
		// wf1.addTask(task1, 2);
		// wf1.addTask(task2, 0);
		// wf1.addTask(task3, 1);
		// wf1.addTask(task4, 0);
		// wf1.addTask(task5, 1);
		//
		// wf1.startThreaded();

		// for (Workflow tempWf : coordinator.getWorkflows()) {
		// System.out.println(tempWf.toString());
		// for (Task tempTask : tempWf.getTasks()) {
		// System.out.println(tempTask.toString());
		// }
		// }

		assertTrue(true);
	}

	// @Test
	public void testTask() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");
		// coordinator.wipeDatabase();
		coordinator.setupWegovDatabase();
		// coordinator.setupTwitterDatabase("test");

		// WegovPolicymaker policyMaker1 =
		// coordinator.createPolicyMaker("Maxim Bashevoy", "IT Innovation");
		//
		// Task task = coordinator.createTask(policyMaker1, "Test task",
		// "Some type",
		// "java -jar /Users/max/Documents/Work/wegov/randomtest/dist/randomtest.jar");
		//
		// String taskStatus = task.getStatus();
		//
		// System.out.println("Running task: " + task.getName());
		//
		// task.startThreaded();
		//
		// do {
		// System.out.println("Task status: " + taskStatus);
		// Thread.sleep(1000);
		// taskStatus = task.getStatus();
		// } while(!taskStatus.equals("finished"));
		//
		// System.out.println("Logs:");
		// System.out.println(task.getLogs());
		//
		// System.out.println("Errors:");
		// System.out.println(task.getErrors());
		//
		// System.out.println("Done");

		// for (Task tempTask : coordinator.getTasks()) {
		// System.out.println(tempTask.toString());
		// }

		// Task task = new Task(1, coordinator);
		// task.setWorkflowOrder(24);
		//
		// System.out.println(task.getWorkflowOrder());
		// System.out.println(task.toString());

		// String name, String type, String status, String command, Coordinator
		// coordinator
		// Task task = new Task("Test task", "Some type", "initialising",
		// "java -version", coordinator);
		// Task task = new Task("Test task", "Some type", "initialising",
		// "java -jar /Users/max/Documents/Work/wegov/RawLiveTwitter/dist/RawLiveTwitter.jar",
		// coordinator);
		// Task task = new Task("Test task", "Some type", "initialising",
		// "java -jar /Users/max/Documents/Work/wegov/UpdateTwitterUserDetails/dist/UpdateTwitterUserDetails.jar",
		// coordinator);

		/*
		 * task.startIn(1000);
		 * 
		 * int counter = 0; String taskStatus = task.getStatus();
		 * 
		 * do{ System.out.println("Status: " + taskStatus); Thread.sleep(1000);
		 * taskStatus = task.getStatus(); counter++; // if (counter > 3) { //
		 * System.out.println("Taking too long! Cancelling: " +
		 * Calendar.getInstance().getTime().toString()); // task.cancel(true);
		 * // do { // System.out.println("Still cancelling: " +
		 * Calendar.getInstance().getTime().toString()); // Thread.sleep(2000);
		 * // } while (!task.isCancelled()); // } } while (!task.isDone());
		 * 
		 * System.out.println("Logs:"); ArrayList<WegovTask_Log> logs =
		 * coordinator.getMgtSchema().getAll(new WegovTask_Log()); for
		 * (WegovTask_Log log : logs) { System.out.println("\t-" +
		 * log.getText()); }
		 * 
		 * if (logs.isEmpty()) System.out.println("\t-None");
		 * 
		 * System.out.println("Errors:"); ArrayList<WegovTask_Error> errors =
		 * coordinator.getMgtSchema().getAll(new WegovTask_Error()); for
		 * (WegovTask_Error error : errors) { System.out.println("\t-" +
		 * error.getText()); }
		 * 
		 * if (errors.isEmpty()) System.out.println("\t-None");
		 * 
		 * System.out.println("Done");
		 */
		assertTrue(true);
	}

	// @Test
	public void testActivity() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");
		// coordinator.wipeDatabase();
		coordinator.setupWegovDatabase();

		// Activity activity = coordinator.createActivity();
		// activity.startIn(5000);
		//
		// int counter = 0;
		//
		// do{
		// System.out.println("Waiting for the task to finish: " +
		// Calendar.getInstance().getTime().toString());
		// Thread.sleep(2000);
		// counter++;
		// if (counter > 3) {
		// System.out.println("Taking too long! Cancelling: " +
		// Calendar.getInstance().getTime().toString());
		// activity.cancel(true);
		// do {
		// System.out.println("Still cancelling: " +
		// Calendar.getInstance().getTime().toString());
		// Thread.sleep(2000);
		// } while (!activity.isCancelled());
		// }
		// } while (!activity.isDone());
		//
		// System.out.println("Done");

		assertTrue(true);
	}

	// @Test
	public void testLiveTwitter() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");
		String keywordToTrack = "google%2B";
		// String keywordToTrack = "Rebekah%20Brooks";

		// coordinator.wipeDatabase();
		coordinator.setupTwitterDatabase(URLDecoder.decode(keywordToTrack,
				"UTF-8"));

		RunLiveTwitterCollection liveTask = new RunLiveTwitterCollection(
				keywordToTrack, coordinator);
		// liveTask.getUsers(100000, "maximbashevoy", "kcG2v6$76/-j(F");
		liveTask.execute(10, "itilabs", "1A}de(z7/6h489%c");

		assertTrue(true);
	}

	// @Test
	public void testJoyCollectionGuardian() throws Exception {

		Coordinator coordinator = new Coordinator("coordinator.properties");
		coordinator.setupTwitterDatabase("guardian test");

		String userName = "maximbashevoy";
		String userPass = "kcG2v6$76/-j(F";

		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(
				"http://search.twitter.com/search.json?q=from%3Atelegraphnews%20until%3A2011-08-16%20since%3A2011-08-15&rpp=99");

		final int CHARS_PER_PAGE = 5000; // counting spaces
		final char[] buffer = new char[CHARS_PER_PAGE];
		StringBuilder output = new StringBuilder(CHARS_PER_PAGE);

		try {
			client.executeMethod(method);

			if (method.getStatusCode() == HttpStatus.SC_OK) {
				InputStream is = method.getResponseBodyAsStream();
				InputStreamReader input = new InputStreamReader(is/* , "UTF-8" */);

				for (int read = input.read(buffer, 0, buffer.length); read != -1; read = input
						.read(buffer, 0, buffer.length)) {
					output.append(buffer, 0, read);
				}
			} else {
				System.out.println(method.getStatusText());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			method.releaseConnection();
		}

		String response = output.toString();
		System.out.println(response);

		JSONObject root = JSONObject.fromObject(response);
		JSONArray results = root.getJSONArray("results");
		ArrayList<String> tweeIDs = new ArrayList<String>();

		for (int i = 0; i < results.size(); i++) {
			JSONObject result = results.getJSONObject(i);
			String tweetId = result.getString("id");
			String tweetText = result.getString("text");
			// String userName = result.getString("from_user");

			System.out.println("------------------ #" + i
					+ " ------------------");
			System.out.println(tweetId + " by: " + userName + ", contents: "
					+ tweetText);

			tweeIDs.add(tweetId);
		}

		if (tweeIDs.isEmpty()) {
			System.out.println("Nothing was found :(");
		} else {
			GetTwitterPostDetails postGetter = new GetTwitterPostDetails(
					coordinator);
			for (String tweetID : tweeIDs) {
				FullTweet tweet = postGetter.getFullTweet(tweetID, userName,
						userPass);
				coordinator.getDataSchema().insertFullTweet(tweet);
				System.out.println("Saved: " + tweet.getTweet().getID());
			}
		}

		assertTrue(true);
	}

	// @Test
	public void testJoyProcessingGuardian() throws Exception {
		Coordinator coordinatorReader = new Coordinator(
				"coordinator.properties");
		coordinatorReader
				.setupTwitterDatabaseWithExactName("Twitter 2011-08-16 15:42:17 guardian test");

		String userName = "maximbashevoy";
		String userPass = "kcG2v6$76/-j(F";

		ArrayList<Tweet> tweets = coordinatorReader.getDataSchema().getAll(
				new Tweet());
		User user = (User) coordinatorReader.getDataSchema()
				.getAllWhere(new User(), "ID", tweets.get(0).getUserID())
				.get(0);

		// for (Tweet tweet : tweets) {
		// System.out.println(tweet.getID() + ": " + tweet.getText());
		// }

		Coordinator coordinatorWriter = new Coordinator(
				"coordinator.properties");
		coordinatorWriter.setupTwitterDatabase("guarding processing test");

		coordinatorWriter.getDataSchema().insertObject(user);

		GetRetweetedBy retweeter = new GetRetweetedBy(coordinatorWriter);

		for (Tweet tweet : tweets) {
			ArrayList<FullTweet> reTweets = retweeter.getFullTweets(
					tweet.getID(), userName, userPass);
			System.out.println(tweet.getID() + ": " + tweet.getText());
			coordinatorWriter.getDataSchema().insertObject(tweet);

			for (FullTweet retweet : reTweets) {
				System.out.println("\t-retweet: " + retweet.getTweet().getID()
						+ ": " + retweet.getTweet().getText());
				coordinatorWriter.getDataSchema().insertFullTweet(retweet);
			}
		}

		assertTrue(true);

	}

	// @Test
	public void testJoyTwitterCollectFollowers() throws Exception {

		String twitterAccountToQuery = "bbcnews";

		assertTrue(true);
	}

	// @Test
	public void testJoyTwitter() throws Exception {

		String twitterAccountToQuery = "bbcnews";
		String startDate = "2011-09-20";
		// 28/09/2011
		String endDate = "2011-09-21";

		Coordinator coordinator = new Coordinator("coordinator.properties");
		coordinator.setupTwitterDatabase(twitterAccountToQuery + " "
				+ startDate);

		TwitterHelper helper = new TwitterHelper(coordinator);

		System.out.println("Remaining hits at start: "
				+ helper.getRemainingHits());

		// ArrayList<String> tweetIDs =
		// helper.getTweetsPublishedByInTimeInterval("telegraphnews",
		// "2011-08-15", "2011-08-16");
		ArrayList<String> tweetIDs = helper.getTweetsPublishedByInTimeInterval(
				twitterAccountToQuery, startDate, endDate);

		/*
		 * for (String tweetID : tweetIDs) { FullTweet fullTweet =
		 * helper.getFullTweet(tweetID);
		 * coordinator.getDataSchema().insertFullTweet(fullTweet); Tweet tweet =
		 * fullTweet.getTweet(); String tweet_retweetCount =
		 * tweet.getRetweetCount(); System.out.println("Tweet " + tweet.getID()
		 * + ": " + tweet.getText()); ArrayList<FullTweet> retweets =
		 * helper.getRetweetedBy(tweetID);
		 * 
		 * for (FullTweet retweet : retweets) { String reTweetID =
		 * retweet.getTweet().getID(); String retweet_retweetCount =
		 * retweet.getTweet().getRetweetCount();
		 * 
		 * System.out.println("\t-" + reTweetID + ": " +
		 * retweet.getTweet().getText());
		 * 
		 * int tweet_retweetCount_int = -1; if
		 * (tweet_retweetCount.contains("+")) { tweet_retweetCount_int = 100; }
		 * else { tweet_retweetCount_int = Integer.parseInt(tweet_retweetCount);
		 * }
		 * 
		 * int retweet_retweetCount_int = -1; if
		 * (retweet_retweetCount.contains("+")) { retweet_retweetCount_int =
		 * 100; } else { retweet_retweetCount_int =
		 * Integer.parseInt(retweet_retweetCount); }
		 * 
		 * int trueRetweetCount = tweet_retweetCount_int -
		 * retweet_retweetCount_int; if (trueRetweetCount < 0) {
		 * trueRetweetCount = 0; }
		 * 
		 * System.out.println("\t-" + tweet_retweetCount_int + " | " +
		 * retweet_retweetCount_int + " | " + trueRetweetCount);
		 * 
		 * retweet.getTweet().updateProperty("Retweet_count",
		 * Integer.toString(trueRetweetCount));
		 * coordinator.getDataSchema().insertFullTweet(retweet); //
		 * ArrayList<FullTweet> reretweets = helper.getRetweetedBy(reTweetID);
		 * // // for (FullTweet reretweet : reretweets) { // String rereTweetID
		 * = reretweet.getTweet().getID(); // System.out.println("\t\t-" +
		 * rereTweetID + ": " + reretweet.getTweet().getText()); // // } } }
		 * 
		 * 
		 * System.out.println("Writing Excel sheet");
		 * 
		 * WritableFont arialBold12font = new WritableFont(WritableFont.ARIAL,
		 * 12, WritableFont.BOLD); WritableFont arial12font = new
		 * WritableFont(WritableFont.ARIAL, 12); WritableCellFormat
		 * arialBold12format = new WritableCellFormat(arialBold12font);
		 * WritableCellFormat arial12format = new
		 * WritableCellFormat(arial12font);
		 * 
		 * WritableWorkbook workbook = Workbook.createWorkbook(new
		 * File(twitterAccountToQuery + " " + startDate +
		 * " with Retweets.xls")); WritableSheet sheet =
		 * workbook.createSheet("First Sheet", 0);
		 * 
		 * String[] columnTitles = new String[]{"Tweet Category",
		 * "Tweet Contents", "Tweet ID", "Tweet Created", "Tweet Recorded",
		 * "Tweet Retweet Count", "Tweet Author ID", "Tweet Author Name",
		 * "Tweet Author Screen Name", "Tweet Author URL",
		 * "Tweet Author Created", "Tweet Author Description",
		 * "Tweet Author Language", "Tweet Author Time Zone",
		 * "Tweet Author Number of Tweets", "Tweet Author Number of Followers",
		 * "Tweet Author Number of Friends",
		 * "Tweet Author Number of Times Listed",
		 * "Tweet Author Number of Favourites", "Retweet Contents",
		 * "Retweet ID", "Retweet Created", "Retweet Recorded",
		 * "Retweet Retweet Count", "Retweet Author ID", "Retweet Author Name",
		 * "Retweet Author Screen Name", "Retweet Author URL",
		 * "Retweet Author Created", "Retweet Author Description",
		 * "Retweet Author Language", "Retweet Author Time Zone",
		 * "Retweet Author Number of Tweets",
		 * "Retweet Author Number of Followers",
		 * "Retweet Author Number of Friends",
		 * "Retweet Author Number of Times Listed",
		 * "Retweet Author Number of Favourites" };
		 * 
		 * // Write header labels int j = 0; for (int i = 0; i <
		 * columnTitles.length; i++) { Label label = new Label(i, j,
		 * columnTitles[i], arialBold12format); sheet.addCell(label); }
		 * 
		 * // Coordinator coordinator = new
		 * Coordinator("coordinator.properties"); //
		 * coordinator.setupTwitterDatabaseWithExactName
		 * ("Twitter 2011-08-17 10:50:26 guarding processing test");
		 * 
		 * ArrayList<Tweet> originalTweets =
		 * coordinator.getDataSchema().getAllWhere(new Tweet(), "Retweeted",
		 * "false");
		 * 
		 * for (Tweet tweet : originalTweets) { User originalUser = (User)
		 * coordinator.getDataSchema().getAllWhere(new User(), "ID",
		 * originalTweets.get(0).getUserID()).get(0); String originalTweetID =
		 * tweet.getID();
		 * 
		 * System.out.println(tweet.getID() + " (" + originalUser.getName() +
		 * "): " + tweet.getText());
		 * 
		 * ArrayList<Tweet> reTweets =
		 * coordinator.getDataSchema().getAllWhere(new Tweet(), "Retweeted",
		 * originalTweetID);
		 * 
		 * for (Tweet reTweet : reTweets) { j++; User reTweetUser = (User)
		 * coordinator.getDataSchema().getAllWhere(new User(), "ID",
		 * reTweet.getUserID()).get(0); System.out.println("\t-" +
		 * reTweet.getID() + " (" + reTweetUser.getName() + "): " +
		 * reTweet.getText());
		 * 
		 * ArrayList<String> valuesAsArray = new ArrayList<String>();
		 * valuesAsArray.add("<category name for tweet " + originalTweetID +
		 * ">");
		 * 
		 * valuesAsArray.add(tweet.getText()); valuesAsArray.add(tweet.getID());
		 * valuesAsArray.add(tweet.getTimeCreated());
		 * valuesAsArray.add(tweet.getTimeCollected());
		 * valuesAsArray.add(tweet.getRetweetCount());
		 * 
		 * valuesAsArray.add(originalUser.getID());
		 * valuesAsArray.add(originalUser.getName());
		 * valuesAsArray.add(originalUser.getScreenName());
		 * valuesAsArray.add(originalUser.getUrl());
		 * valuesAsArray.add(originalUser.getTimeCreated());
		 * valuesAsArray.add(originalUser.getDescription());
		 * valuesAsArray.add(originalUser.getLanguage());
		 * valuesAsArray.add(originalUser.getTimezone());
		 * valuesAsArray.add(originalUser.getNumTweets());
		 * valuesAsArray.add(originalUser.getNumFollowers());
		 * valuesAsArray.add(originalUser.getNumFriends());
		 * valuesAsArray.add(originalUser.getNumListed());
		 * valuesAsArray.add(originalUser.getNumFavorites());
		 * 
		 * valuesAsArray.add(reTweet.getText());
		 * valuesAsArray.add(reTweet.getID());
		 * valuesAsArray.add(reTweet.getTimeCreated());
		 * valuesAsArray.add(reTweet.getTimeCollected());
		 * valuesAsArray.add(reTweet.getRetweetCount());
		 * 
		 * valuesAsArray.add(reTweetUser.getID());
		 * valuesAsArray.add(reTweetUser.getName());
		 * valuesAsArray.add(reTweetUser.getScreenName());
		 * valuesAsArray.add(reTweetUser.getUrl());
		 * valuesAsArray.add(reTweetUser.getTimeCreated());
		 * valuesAsArray.add(reTweetUser.getDescription());
		 * valuesAsArray.add(reTweetUser.getLanguage());
		 * valuesAsArray.add(reTweetUser.getTimezone());
		 * valuesAsArray.add(reTweetUser.getNumTweets());
		 * valuesAsArray.add(reTweetUser.getNumFollowers());
		 * valuesAsArray.add(reTweetUser.getNumFriends());
		 * valuesAsArray.add(reTweetUser.getNumListed());
		 * valuesAsArray.add(reTweetUser.getNumFavorites());
		 * 
		 * for (int k = 0; k < columnTitles.length; k++) { String value =
		 * valuesAsArray.get(k); Label label = new Label(k, j, value,
		 * arial12format); sheet.addCell(label); } } }
		 * 
		 * workbook.write(); workbook.close();
		 * 
		 * System.out.println("Remaining hits at end: " +
		 * helper.getRemainingHits());
		 */
		assertTrue(true);
	}

	// @Test
	public void testJoyExportGuardian() throws Exception {

		WritableFont arialBold12font = new WritableFont(WritableFont.ARIAL, 12,
				WritableFont.BOLD);
		WritableFont arial12font = new WritableFont(WritableFont.ARIAL, 12);
		WritableCellFormat arialBold12format = new WritableCellFormat(
				arialBold12font);
		WritableCellFormat arial12format = new WritableCellFormat(arial12font);

		WritableWorkbook workbook = Workbook.createWorkbook(new File(
				"Telegraph 15 August with Retweets.xls"));
		WritableSheet sheet = workbook.createSheet("First Sheet", 0);

		String[] columnTitles = new String[] { "Tweet Category",
				"Tweet Contents", "Tweet ID", "Tweet Created",
				"Tweet Recorded", "Tweet Retweet Count", "Tweet Author ID",
				"Tweet Author Name", "Tweet Author Screen Name",
				"Tweet Author URL", "Tweet Author Created",
				"Tweet Author Description", "Tweet Author Language",
				"Tweet Author Time Zone", "Tweet Author Number of Tweets",
				"Tweet Author Number of Followers",
				"Tweet Author Number of Friends",
				"Tweet Author Number of Times Listed",
				"Tweet Author Number of Favourites", "Retweet Contents",
				"Retweet ID", "Retweet Created", "Retweet Recorded",
				"Retweet Retweet Count", "Retweet Author ID",
				"Retweet Author Name", "Retweet Author Screen Name",
				"Retweet Author URL", "Retweet Author Created",
				"Retweet Author Description", "Retweet Author Language",
				"Retweet Author Time Zone", "Retweet Author Number of Tweets",
				"Retweet Author Number of Followers",
				"Retweet Author Number of Friends",
				"Retweet Author Number of Times Listed",
				"Retweet Author Number of Favourites" };

		// Write top labels
		int j = 0;
		for (int i = 0; i < columnTitles.length; i++) {
			Label label = new Label(i, j, columnTitles[i], arialBold12format);
			sheet.addCell(label);
		}

		Coordinator coordinatorReader = new Coordinator(
				"coordinator.properties");
		coordinatorReader
				.setupTwitterDatabaseWithExactName("Twitter 2011-08-17 10:50:26 guarding processing test");

		ArrayList<Tweet> originalTweets = coordinatorReader.getDataSchema()
				.getAllWhere(new Tweet(), "Retweeted", "false");

		for (Tweet tweet : originalTweets) {
			User originalUser = (User) coordinatorReader
					.getDataSchema()
					.getAllWhere(new User(), "ID",
							originalTweets.get(0).getUserID()).get(0);
			String originalTweetID = tweet.getID();

			System.out.println(tweet.getID() + " (" + originalUser.getName()
					+ "): " + tweet.getText());

			ArrayList<Tweet> reTweets = coordinatorReader.getDataSchema()
					.getAllWhere(new Tweet(), "Retweeted", originalTweetID);

			for (Tweet reTweet : reTweets) {
				j++;
				User reTweetUser = (User) coordinatorReader.getDataSchema()
						.getAllWhere(new User(), "ID", reTweet.getUserID())
						.get(0);
				System.out.println("\t-" + reTweet.getID() + " ("
						+ reTweetUser.getName() + "): " + reTweet.getText());

				ArrayList<String> valuesAsArray = new ArrayList<String>();
				valuesAsArray.add("<category name for tweet " + originalTweetID
						+ ">");

				valuesAsArray.add(tweet.getText());
				valuesAsArray.add(tweet.getID());
				valuesAsArray.add(tweet.getTimeCreated());
				valuesAsArray.add(tweet.getTimeCollected());
				valuesAsArray.add(tweet.getRetweetCount());

				valuesAsArray.add(originalUser.getID());
				valuesAsArray.add(originalUser.getName());
				valuesAsArray.add(originalUser.getScreenName());
				valuesAsArray.add(originalUser.getUrl());
				valuesAsArray.add(originalUser.getTimeCreated());
				valuesAsArray.add(originalUser.getDescription());
				valuesAsArray.add(originalUser.getLanguage());
				valuesAsArray.add(originalUser.getTimezone());
				valuesAsArray.add(originalUser.getNumTweets());
				valuesAsArray.add(originalUser.getNumFollowers());
				valuesAsArray.add(originalUser.getNumFriends());
				valuesAsArray.add(originalUser.getNumListed());
				valuesAsArray.add(originalUser.getNumFavorites());

				valuesAsArray.add(reTweet.getText());
				valuesAsArray.add(reTweet.getID());
				valuesAsArray.add(reTweet.getTimeCreated());
				valuesAsArray.add(reTweet.getTimeCollected());
				valuesAsArray.add(reTweet.getRetweetCount());

				valuesAsArray.add(reTweetUser.getID());
				valuesAsArray.add(reTweetUser.getName());
				valuesAsArray.add(reTweetUser.getScreenName());
				valuesAsArray.add(reTweetUser.getUrl());
				valuesAsArray.add(reTweetUser.getTimeCreated());
				valuesAsArray.add(reTweetUser.getDescription());
				valuesAsArray.add(reTweetUser.getLanguage());
				valuesAsArray.add(reTweetUser.getTimezone());
				valuesAsArray.add(reTweetUser.getNumTweets());
				valuesAsArray.add(reTweetUser.getNumFollowers());
				valuesAsArray.add(reTweetUser.getNumFriends());
				valuesAsArray.add(reTweetUser.getNumListed());
				valuesAsArray.add(reTweetUser.getNumFavorites());

				for (int k = 0; k < columnTitles.length; k++) {
					String value = valuesAsArray.get(k);
					Label label = new Label(k, j, value, arial12format);
					sheet.addCell(label);
				}
			}
		}

		workbook.write();
		workbook.close();

		assertTrue(true);
	}

	// @Test
	public void testJoyCollection() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");
		String query = "bbc";
		int howManyTweetsToCollect = 100;
		int timesToRecollectTopTweets = 10;
		int timeToSleepBetweetRecollects = 15; // seconds

		String userName = "maximbashevoy";
		String userPass = "kcG2v6$76/-j(F";
		// String databaseName = "WeGov - google 10000";
		// String schemaName = "Twitter 2011-07-29 15:36:23 google";
		// coordinator.wipeDatabase();
		// coordinator.setDatabase(databaseName);
		coordinator.setupTwitterDatabase(query + " " + howManyTweetsToCollect);

		RunLiveTwitterCollection liveTask = new RunLiveTwitterCollection(query,
				coordinator);
		liveTask.execute(howManyTweetsToCollect, userName, userPass);

		GetTopRetweeted topRetweetedTask = new GetTopRetweeted(coordinator);
		LinkedHashMap<String, Integer> map = topRetweetedTask
				.execute(coordinator.getDataSchema().getName());
		map = sortByComparator(map);

		Iterator it = map.keySet().iterator();
		LinkedList<String> keys = new LinkedList<String>();

		while (it.hasNext()) {
			keys.add((String) it.next());
		}

		Collections.reverse(keys);
		ArrayList<String> topAuthors = new ArrayList<String>();

		int iter = 0;
		for (String key : keys) {
			User user = (User) coordinator.getDataSchema()
					.getAllWhere(new User(), "ID", key).get(0);
			topAuthors.add(key);
			int count = map.get(key);
			System.out.println("User: " + key + "(" + user.getName() + ")"
					+ ", retweeted times: " + count);

			iter++;
			if (iter > 2) {
				break;
			}
		}

		GetLastTweetsByContaining tweetsTask = new GetLastTweetsByContaining(
				coordinator);
		LinkedHashMap<String, ArrayList<Tweet>> tweetIDsToTrack = new LinkedHashMap<String, ArrayList<Tweet>>();

		for (String authorID : topAuthors) {
			User user = (User) coordinator.getDataSchema()
					.getAllWhere(new User(), "ID", authorID).get(0);
			System.out
					.println("User: " + authorID + "(" + user.getName() + ")");
			ArrayList<Tweet> lastZtweets = tweetsTask.execute(authorID, query,
					userName, userPass);

			for (Tweet tweet : lastZtweets) {
				coordinator.getDataSchema().insertObject(tweet);
				System.out.println("\t-" + tweet.getID() + " retweet count: "
						+ tweet.getRetweetCount() + " at "
						+ tweet.getTimeCollected());
			}

			tweetIDsToTrack.put(authorID, lastZtweets);
		}

		System.out.println("Tracking tweets for several mins");
		// GetTwitterPostDetails postDetailsTask = new
		// GetTwitterPostDetails(coordinator);
		GetRetweetedBy postRetweetedByTask = new GetRetweetedBy(coordinator);

		for (int i = 0; i < timesToRecollectTopTweets; i++) {
			Thread.sleep(timeToSleepBetweetRecollects * 1000);
			System.out.println("----------------------- Attempt #" + i
					+ " -------------------");
			for (String authorID : topAuthors) {
				User user = (User) coordinator.getDataSchema()
						.getAllWhere(new User(), "ID", authorID).get(0);
				System.out.println("User: " + authorID + "(" + user.getName()
						+ ")");
				ArrayList<Tweet> lastZtweets = tweetIDsToTrack.get(authorID);

				for (Tweet tweet : lastZtweets) {
					String tweetID = tweet.getID();
					// Get number of user retweeted
					// Updated retweet_count in tweet and save!
					ArrayList<User> retweeters = postRetweetedByTask.getUsers(
							tweetID, userName, userPass);
					tweet.updateProperty("Retweet_count",
							Integer.toString(retweeters.size()));
					tweet.updateProperty("Collected_at",
							new Timestamp(System.currentTimeMillis()));
					coordinator.getDataSchema().insertObject(tweet);
					System.out.println("\t-" + tweet.getID()
							+ " retweet count: " + tweet.getRetweetCount()
							+ " at " + tweet.getTimeCollected());
				}
			}
		}

		for (String authorID : topAuthors) {
			User user = (User) coordinator.getDataSchema()
					.getAllWhere(new User(), "ID", authorID).get(0);
			System.out
					.println("User: " + authorID + "(" + user.getName() + ")");
			ArrayList<Tweet> lastZtweets = tweetIDsToTrack.get(authorID);

			for (Tweet tweet : lastZtweets) {
				String tweetID = tweet.getID();
				System.out.println(tweetID + "(" + tweet.getText() + "):");

				ArrayList<Tweet> records = coordinator.getDataSchema()
						.getAllWhere(tweet, "ID", tweetID);
				for (Tweet record : records) {
					System.out.println("\t-" + record.getTimeCollected()
							+ " retweets: " + record.getRetweetCount());
				}
			}

		}

		assertTrue(true);
	}

	private static LinkedHashMap sortByComparator(Map unsortMap) {

		List list = new LinkedList(unsortMap.entrySet());

		// sort list based on comparator
		Collections.sort(list, new Comparator() {

			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue())
						.compareTo(((Map.Entry) (o2)).getValue());
			}
		});

		// put sorted list into map again
		LinkedHashMap sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	// @Test
	public void testTwitterQuery() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");

		// coordinator.wipeDatabase();
		coordinator.setupTwitterDatabase("test");

		// GetTwitterUserDetails taskUserDetails = new
		// GetTwitterUserDetails("49980783,273065473", coordinator);
		// taskUserDetails.getUsers();

		GetTwitterPostDetails taskPostDetails = new GetTwitterPostDetails(
				coordinator);
		System.out.println(taskPostDetails.getRoot("96938038701195264")
				.toString());

		// RunLiveTwitterCollection liveTask = new
		// RunLiveTwitterCollection("google%2B", coordinator);
		// liveTask.getUsers();

		assertTrue(true);
	}

	// @Test
	public void testNewInsert() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");
		coordinator.wipeDatabase();
		coordinator.setupWegovDatabase();

		WegovLike like = new WegovLike("123", "543", 0);
		coordinator.getDataSchema().insertObject(like);
		like = new WegovLike("333", "111", 0);
		coordinator.getDataSchema().insertObject(like);

		ArrayList<WegovLike> likes = coordinator.getDataSchema().getAll(like);

		for (WegovLike l : likes) {
			System.out.println(l.toString());
		}

		// WegovPolicymaker user = new WegovPolicymaker("Maxim Bashevoy",
		// "IT Innovation");
		// coordinator.getMgtSchema().insertObject(user);
		//
		// ArrayList<WegovPolicymaker> people =
		// coordinator.getMgtSchema().getAll(user);
		//
		// for (WegovPolicymaker p : people) {
		// System.out.println(p.toString());
		// }

		assertTrue(true);
	}

	// @Test
	public void testSetup() throws Exception {
		Coordinator coordinator = new Coordinator("coordinator.properties");
		coordinator.wipeDatabase();
		// coordinator.setupWegovDatabase();
		coordinator.setupTwitterDatabase("test");

		// WegovPolicymaker p = new WegovPolicymaker("Some bloke", "Apple", new
		// Date(), new Date());

		// for (String key : p.getKeysWithDescriptions()) {
		// System.out.println(key);
		// }

		// System.out.println(p.getTableSqlSchemaAsString());

		assertTrue(true);
	}
}
