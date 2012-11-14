package eu.wegov.prototype.web.resources.headsup;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import uk.ac.itinnovation.soton.wegov.hansard.Factory;
import uk.ac.itinnovation.soton.wegov.hansard.Forum;
import uk.ac.itinnovation.soton.wegov.hansard.Post;
import uk.ac.itinnovation.soton.wegov.hansard.Thread;
import uk.ac.itinnovation.soton.wegov.hansard.User;
import west.importer.WegovImporter;
import west.wegovdemo.SampleInput;
import west.wegovdemo.TopicOpinionAnalysis;
import west.wegovdemo.TopicOpinionOutput;

/**
 * Runs Koblenz Analysis on selected posts and writes results into an Excel file.
 *
 */
public class KoblenzPerformAnalysisResource extends ServerResource {
	@Get("json")
	public Representation retrieveAnalysisResults() throws Exception {

		System.out.println("Running analysis");
		Factory f = DataConnector.getFactory();

		if (f == null)
			throw new RuntimeException("Failed to init factory");

		System.out.println("Finished collecting data");

		String type = getQuery().getFirstValue("type");
		System.out.println("Topic analysis for: " + type);

		if (! ( type.equals("posts") || (type.equals("threads")) ) ) {
			throw new Exception("Unknown type: " + type);
		}

		String ids = getQuery().getFirstValue("input");
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
        TopicOpinionAnalysis analysis = new WegovImporter();
        TopicOpinionOutput output = analysis.analyzeTopicsOpinions(input);

        int numTopics = output.getNumTopics();
        JSONArray koblenzTopics = new JSONArray();

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
        	JSONObject koblenzTopic = new JSONObject();
        	StringBuilder koblenzTopicKeyTerms = new StringBuilder();
        	StringBuilder koblenzTopicJustKeyTermsSeparatedBySpace = new StringBuilder();
        	topicLabelPrefix = "Topic " + counter + " keywords: ";
        	koblenzTopicKeyTerms.append(topicLabelPrefix);
        	counter++;

        	JSONArray koblenzTopicKeyUsers = new JSONArray();
        	JSONArray koblenzRelevantDocsSubjects = new JSONArray();
        	JSONArray koblenzRelevantDocsMessages = new JSONArray();
        	JSONArray koblenzRelevantDocsUsers = new JSONArray();
        	JSONArray koblenzRelevantDocsDates = new JSONArray();
        	JSONArray koblenzRelevantDocsContext = new JSONArray();
        	JSONArray koblenzRelevantDocsScores = new JSONArray();

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

            String koblenzTopicKeyTermsAsString = koblenzTopicKeyTerms.toString();
            if (koblenzTopicKeyTermsAsString.length() > 3)
            	koblenzTopicKeyTermsAsString = koblenzTopicKeyTermsAsString.substring(0, koblenzTopicKeyTermsAsString.length() - 2);

            label = new Label(xLabel, yLabel, koblenzTopicKeyTermsAsString.split(":")[1].trim(), arialBold12format);
            sheet.addCell(label);
            xLabel = 0;
            yLabel++;

            System.out.println("\t- Key users:");
            label = new Label(xLabel, yLabel, "Key users:", arial12format); sheet.addCell(label);
            xLabel = 1;
            for (String keyUser : output.getTopicUsers(topicID)) {
                System.out.println("\t\t- " + f.getUserWithId(Integer.parseInt(keyUser)));
                User theUser = f.getUserWithId(Integer.parseInt(keyUser));
                koblenzTopicKeyUsers.put(theUser.getName() + " (" + theUser.getType() + ")");
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


                koblenzRelevantDocsSubjects.put(highlightedSubjectStringBuilder.toString().trim());
//                koblenzRelevantDocsSubjects.put(originalPost.getSubject());
//                koblenzRelevantDocsMessages.put(originalPost.getMessageClean());
                koblenzRelevantDocsMessages.put(highlightedMessagesStringBuilder.toString().trim());
                koblenzRelevantDocsUsers.put(originalUser.getName() + " (" + originalUser.getType() + ")");
                koblenzRelevantDocsDates.put(datePublishedAsString);
                koblenzRelevantDocsContext.put(forum.getName() + " - " + thread.getName());
                koblenzRelevantDocsScores.put(Double.toString((double) Math.round(relevantDocScores[j] * 10000) / 10000));

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

            koblenzTopic.put("keyterms", koblenzTopicKeyTermsAsString);
            koblenzTopic.put("keyusers", koblenzTopicKeyUsers);
            koblenzTopic.put("relevantdocssubjects", koblenzRelevantDocsSubjects);
            koblenzTopic.put("relevantdocsmessages", koblenzRelevantDocsMessages);
            koblenzTopic.put("relevantdocsusers", koblenzRelevantDocsUsers);
            koblenzTopic.put("relevantdocsdates", koblenzRelevantDocsDates);
            koblenzTopic.put("relevantdocscontext", koblenzRelevantDocsContext);
            koblenzTopic.put("relevantdocsscores", koblenzRelevantDocsScores);

            koblenzTopics.put(koblenzTopic);
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

		JSONObject result = new JSONObject();

		result.put("summary", numTopics + " topics in " + numPostsToAnalyse + " posts");
		result.put("result", koblenzTopics);
		result.put("filePath", filePath);

		return new JsonRepresentation(result);
	}
}
