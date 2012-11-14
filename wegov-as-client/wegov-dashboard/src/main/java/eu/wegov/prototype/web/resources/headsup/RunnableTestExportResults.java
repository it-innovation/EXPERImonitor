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

import uk.ac.itinnovation.soton.wegov.hansard.Factory;
import uk.ac.itinnovation.soton.wegov.hansard.Post;
import uk.ac.itinnovation.soton.wegov.hansard.Thread;
import uk.ac.itinnovation.soton.wegov.hansard.User;
import west.importer.WegovImporter;
import west.wegovdemo.SampleInput;
import west.wegovdemo.TopicOpinionAnalysis;
import west.wegovdemo.TopicOpinionOutput;


public class RunnableTestExportResults {
	public static void main(String[] args) throws Exception {
		
		Factory f = DataConnector.getFactory();
		
		if (f == null)
			throw new RuntimeException("Failed to init factory");
		
//		System.out.println("Finished collecting data");
		
//		Array list = getQuery().getFirst("input");
		String inputTopicsIds = "0,1,2,3,4";
//		System.out.println("Ids to analyse: " + inputTopicsIds);
		
		ArrayList<String> topicIds = new ArrayList<String>(); 
		Collections.addAll(topicIds, inputTopicsIds.split(","));
		
		SampleInput input = new SampleInput();
		
        WritableFont arialBold12font = new WritableFont(WritableFont.ARIAL, 12, WritableFont.BOLD); 
        WritableFont arial12font = new WritableFont(WritableFont.ARIAL, 12); 
        WritableCellFormat arialBold12format = new WritableCellFormat (arialBold12font); 
        WritableCellFormat arial12format = new WritableCellFormat (arial12font);
                
        String outputFileName = (new SimpleDateFormat("MMM dd, yyyy - HH_mm_ss")).format(new Date()) + ", " + topicIds.size() + " threads.xls";
//        System.out.println(outputFileName);
//        System.exit(0);
        
        File outputFile = new File(outputFileName);
        System.out.println("Writing to file: " + outputFile.getAbsolutePath());
        
        WritableWorkbook workbook = Workbook.createWorkbook(outputFile);
        WritableSheet sheet = workbook.createSheet("First Sheet", 0); 
		
		int inputDocsCounter = 0;
		LinkedHashMap<Integer, Integer> postIDsAndDocIDs = new LinkedHashMap<Integer, Integer>();
		for (Thread topic : f.getthreadsWithIds(topicIds)) {
			System.out.println(topic);
			for (Post post : f.getPostsForThread(topic.getId())) {
//				System.out.println("\t-" + post + " " + f.getUserWithId(post.getUserId()));
				input.add(post.getContents(), Integer.toString(post.getUserId()));
				postIDsAndDocIDs.put(inputDocsCounter, post.getId());
				inputDocsCounter++;
			}
		}
		
		int numDocs = input.getDocumentContents().length;
		System.out.println("Using " + numDocs + " documents for analysis" );
        TopicOpinionAnalysis analysis = new WegovImporter();
        TopicOpinionOutput output = analysis.analyzeTopicsOpinions(input);
        
        int numTopics = output.getNumTopics();
        
        int counter = 1;
        int xLabel = 0;
        int yLabel = 0;
        String topicLabelPrefix;
        Label label;
        for (int topicID = 0; topicID < numTopics; topicID++) {
        	StringBuilder koblenzTopicKeyTerms = new StringBuilder();
        	topicLabelPrefix = "Topic " + counter + " keywords: ";
        	koblenzTopicKeyTerms.append(topicLabelPrefix);        	
        	counter++;
        	
        	JSONArray koblenzTopicKeyUsers = new JSONArray();
        	JSONArray koblenzRelevantDocsSubjects = new JSONArray();
        	JSONArray koblenzRelevantDocsMessages = new JSONArray();
        	JSONArray koblenzRelevantDocsUsers = new JSONArray();
        	JSONArray koblenzRelevantDocsDates = new JSONArray();
        	JSONArray koblenzRelevantDocsScores = new JSONArray();
        	
        	System.out.println("Topic number " + topicID);
            System.out.println("\t- Key terms:");
            
            label = new Label(xLabel, yLabel, topicLabelPrefix.trim(), arialBold12format); sheet.addCell(label);
            xLabel++;

            for (String keyTerm : output.getTopicTerms(topicID)) {
                System.out.println("\t\t- " + keyTerm);
                koblenzTopicKeyTerms.append(keyTerm);
                koblenzTopicKeyTerms.append(", ");
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
            for (int j = 0; j < relevantDocIDs.length; j++) {
                int relevantDocID = relevantDocIDs[j];
                
//                System.out.println("\t\t- [" + relevantDocID + "] " + output.getDocumentBody(relevantDocID) + " (" + relevantDocScores[j] + ")");
                
                Post originalPost = f.getPostWithId(postIDsAndDocIDs.get(relevantDocID));
                User originalUser = f.getUserWithId(originalPost.getUserId());
                Timestamp datePublished = originalPost.getTimePublished();
                String datePublishedAsString = new SimpleDateFormat("HH:mm MM/dd/yyyy").format(datePublished);
                
//                System.out.println("\t\t- Found post: " + );
                koblenzRelevantDocsSubjects.put(originalPost.getSubject());
                koblenzRelevantDocsMessages.put(originalPost.getMessageClean());
                koblenzRelevantDocsUsers.put(originalUser.getName() + " (" + originalUser.getType() + ")");
                koblenzRelevantDocsDates.put(datePublishedAsString);
                koblenzRelevantDocsScores.put( Double.toString((double) Math.round(relevantDocScores[j] * 10000) / 10000) );
                
                label = new Label(xLabel, yLabel, originalPost.getSubject(), arial12format); sheet.addCell(label);
                xLabel++;
                label = new Label(xLabel, yLabel, originalUser.getName() + " (" + originalUser.getType() + ")", arial12format); sheet.addCell(label);
                xLabel++;
                label = new Label(xLabel, yLabel, datePublishedAsString, arial12format); sheet.addCell(label);
                xLabel++;
                label = new Label(xLabel, yLabel, Double.toString((double) Math.round(relevantDocScores[j] * 10000) / 10000) , arial12format); sheet.addCell(label);
                xLabel++;
                label = new Label(xLabel, yLabel, originalPost.getMessageClean(), arial12format); sheet.addCell(label);
                xLabel = 1;
                yLabel++;

            }            
            
            xLabel = 0;
            yLabel++;
            

        }
        
        workbook.write(); 
        workbook.close();
	}
}
