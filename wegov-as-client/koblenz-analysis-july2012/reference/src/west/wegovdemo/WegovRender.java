package west.wegovdemo;

import java.text.NumberFormat;
import java.util.*;
import java.text.*;

/**
 * Small helper class that contains functions for rendering results of topic-opinion analysis
 * It makes an (almost) complete printout of the results returned by topic-opinion analysis.
 * @author sizov
 */
public class WegovRender 
{
	
	/**
	 * Makes the printout of topic-opinion results
	 * @param output Results returned by topic-opinion analysis
	 * @param input Input documents that have been passed to topic-opinion analysis. The output 
	 * of topic-opinion analysis contains only document IDs (not document bodies). To display some sample documents, 
	 * we need the original input array as well.
	 */
	public static void showResults(TopicOpinionOutput output)
	{
		NumberFormat formatter =  new DecimalFormat("0.00"); 	
		
		line();
		
		int numTopics = output.getNumTopics();
		System.out.println("Results of topic-opinion analysis with " + numTopics + " topics");
		
		line();
		
		System.out.println("Key TERMS per topic:");
		System.out.println();
		for (int i=0; i<numTopics; i++)
		{
			String[] termlist = output.getTopicTerms(i);
			double[] termscores = output.getTopicTermProbs(i);
			System.out.println("Topic " + i + ": ");
			for (int j=0;j<termlist.length;j++)
				System.out.print (termlist[j] + " (" + termscores[j] + ") ");
			System.out.println();
		}
		
		System.out.println("Key USERS per topic:");
		System.out.println();
		for (int i=0; i<numTopics; i++)
		{
			String[] termlist = output.getTopicUsers(i);
			System.out.println("Topic " + i + ": ");
			for (int j=0;j<termlist.length;j++)
				System.out.print (termlist[j] + " ");
			System.out.println();
		}
		
		line();
		
		System.out.println("Topic positiveness/negativeness (valence):");
		System.out.println();
		for (int i=0; i<numTopics; i++)
		{
			System.out.println("Topic " + i + ": " + formatter.format(output.getValence(i)));
		}
		
		line();

		System.out.println("Topic controversity:");
		System.out.println();
		for (int i=0; i<numTopics; i++)
		{
			System.out.println("Topic " + i + ": " + formatter.format(output.getControversity(i)));
		}
		
		line();
		
		System.out.println("Topic controversity HISTOGRAMS:");
		System.out.println();
		String[] labels = output.getOpinionHistogramLabels();
		for (int i=0; i<numTopics; i++)
		{
			System.out.println("Topic " + i + ": ");
			int[][] histogram = output.getOpinionHistogram(i);
			for (int x = 0; x < histogram.length; x++)
			{
				System.out.print(labels[x] + " : ");
				for (int y = 0; y < histogram[x].length; y++)
					System.out.print (histogram[x][y] + " ");
				System.out.println();
			}
			System.out.println();
		}
		
		
		line();
		
		System.out.println("Relevant documents per topic (compact view):");
		System.out.println();
		for (int i=0; i<numTopics; i++)
		{
			int[] docids = output.getTopicRelevantDocIDs(i);
			//String[] reasons = output.getTopicDocReasons(i);
			double[] scores = output.getTopicRelevantDocScores(i);
			System.out.println("Topic " + i + ": ");
			for (int j=0;j<docids.length;j++)
				//System.out.print ("     " + docids[j] + " - " + reasons[j] + " (" + scores[j] + ")");
				System.out.print ("     " + docids[j] + " (" + scores[j] + ")");
			System.out.println();
		}
		
		line();

		System.out.println("Opinion documents per topic (compact view):");
		System.out.println();
		for (int i=0; i<numTopics; i++)
		{
			int[] docids = output.getTopicOpinionDocIDs(i);
			String[] reasons = output.getTopicOpinionDocReasons(i);
			double[] scores = output.getTopicOpinionDocScores(i);
			System.out.println("Topic " + i + ": ");
			for (int j=0;j<docids.length;j++)
				System.out.print ("     " + docids[j] + " - " + reasons[j] + " (" + formatter.format(scores[j]) + ")");
				
			System.out.println();
		}		
		
		line();
		
		int[] topics = {0,1};
		
		for (int x=0; x<topics.length;x++)
		{
		int topic = topics[x];
		System.out.println("Key documents per topic (long view for topic " + topic + ")");
		System.out.println();
		int[] docids = output.getTopicRelevantDocIDs(topic);
		//String[] reasons = output.getTopicDocReasons(topic);
		double[] scores = output.getTopicRelevantDocScores(topic);
		for (int j=0;j<docids.length;j++)
		{
		  System.out.println(output.getUser(docids[j]) + " said: ");
		  System.out.println(output.getDocumentBody(docids[j]));
		 // System.out.println("Reason: " + reasons[j] + ", score " + scores[j]);
		  System.out.println("Score: " + scores[j]);
		System.out.println();
		}
		}
		
		line();
		
		System.out.println("Function getAllDocuments() returns the entire result set. For each document, its ID, the array of topic scores, and a list of opinions with names and weights is provided, in form of a small container object TopicOpinionDocument. The function TopicOpininOutput.getAllDocuments() simply returns a Collection of such mini-containers. Sample output (first document in the list) as follows:");

		Collection tmp = output.getAllDocuments();
		Iterator iter = tmp.iterator();
		for (int q=0; q<3; q++)
		{
		TopicOpinionDocument d = (TopicOpinionDocument) iter.next();
		System.out.println();
		System.out.println("ID: " + d.getID());
		double[] sc = d.getTopicScores();
		System.out.println("Topic Scores for all " + sc.length + " topics:");
		for (int i=0; i<sc.length;i++)
			System.out.print(formatter.format(sc[i]) + "  ");
		System.out.println();
		System.out.println("Opinions:");
		Map ops = d.getOpinions();
		Iterator it = ops.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry entry = (Map.Entry)it.next();
			String op = (String) entry.getKey();
			Double val = (Double) entry.getValue();
			System.out.println("   - " + op + " : " + formatter.format(val.doubleValue()));
		}
		System.out.println();
		System.out.println("Document positiveness/negativeness [-10..+10]: " + formatter.format(d.getValence()));
		System.out.println();
		System.out.println("REMARK: Since the docID is known, it is easy to get the (additional) text source and user name of the document as well:");
		System.out.println();
		System.out.println("Username: " + output.getUser(d.getID()));
		System.out.println();
		System.out.println("Doc source:");
		System.out.println(output.getDocumentBody(d.getID()));
		}
		iter = null;
		
		line();
		
		/*
		double[] terms1 = output.getTopicTermScores(1);
		double[] terms2 = output.getTopicTermScores(3);
		
		System.out.println("List of term scores for topic 1: ");
		for (int q=0; q<terms1.length; q++)
			System.out.print(terms1[q] + " ");
		
		System.out.println("List of term scores for topic 3: ");
    	for (int q=0; q<terms2.length; q++)
			System.out.print(terms2[q] + " ");
		*/
		
		System.out.println();
		System.out.println("Pairwise distances between topics:");
		System.out.println();   
		
		for (int i=0; i<output.getNumTopics(); i++)
		{
			for (int j=0; j<output.getNumTopics(); j++)
				System.out.print(formatter.format(output.topicDist(i,j)) + " ");
			System.out.println();
		}
		
	}
	
	public static void line()
	{
		System.out.println();
		System.out.println("=========================================================");
		System.out.println();
	}
}
