package west.importer;

import west.wegovdemo.*;
import java.util.*;

public class WegovOutput implements TopicOpinionOutput
{ 
	
	public HashMap termDictionary;
	
	public String[][] topicTerms;
	
	public int[][] topicRelevantDocIds;
	
	public String[][] topicUsers;
	
	public int[][] topicOpinionDocIds;
	
	public double[][] topicRelevantDocScores;
	
	public double[][] topicOpinionDocScores;
	
	public String[][] topicOpinionDocReasons;
	
	public String[] docs;
	
	public String[] users;
	
	public Collection allDocs;
	
	public double[] topicValence;
	
	public double[] topicControversity;
	
	public double[][] docTopicValence;
	
	public double[][] topicTermScores;
	
	public double[][] topicTermProbs;
	
	public static String[] opinionHistogramLabels = {"[-10..-9)", "[-9..-8)", "[-8..-7)", "[-7..-6)", "[-6..-5)", "[-5..-4)", "[-4..-3)", "[-3..-2)", "[-2..-1)", "[-1..0)", 
			                                  "[0..+1)", "[+1..+2)", "[+2..+3)", "[+3..+4)", "[+4..+5)", "[+5..+6)", "[+6..+7)", "[+7..+8)", "[+8..+9)", "[+9..+10]" };
		
	public static int[] opinionHistogramBorders = {-9,-8, -7, -6, -5, -4, -3, -2, -1, 0, 1,2,3,4,5,6,7,8,9, 10};
	
	public ArrayList opinionHistograms;
	
	   /**
	    * Returns all analyzed documents along with estimated topic scores for each topic, all opinion weights together with opinion names. Can be used to construct more comprehensive result sets 
	    * of your choice (than top-X as supported by other functions).
	    * @return A collection of simple object containers (wegov.TopicOpinionDocument) that contain desired information per document.
	    * @see TopicOpinionDocument
	    */
	public Collection getAllDocuments()
	{
		return allDocs;
	}
	
	/**
	 * Returns the list of opinion histogram labels, i.e. labels of intervals used for histogram construction (number of highly relevant documents that show the desired degree 
	 * of positiveness / negativeness)
	 * @return Labels of the opinion histogram
	 */
	public String[] getOpinionHistogramLabels()
	{
		return opinionHistogramLabels;
	}
	
	/**
	 * Returns the opinion histogram for a given topic. The opinion histogram contains - for each label / interval - a list of
	 * most relevant documents that have the desired degree of positiveness / negativeness. The corresponding
	 * labels for histogram intervals are returned by function getOpinionHistogramLabels.
	 * @param topicID - topic for histogram computation
	 * @return Array of arrays, for each histogram element an array of document ids that belong to the topic with
	 * high probability and show the desired degree of positiveness / negativeness.
	 */
	public int[][] getOpinionHistogram(int topicID)
	{
      return (int[][])opinionHistograms.get(topicID);
	}
	
	 /**
		 * Returns Returns a vector of term (word) probabilities for a given topic. This output is used to estimate similarity between topics. Returned values 
		 * for two topics are always exactly in the same order of terms. The actual terms (strings) are not part of the output. 
		 * @return The array of values that are interpreted as term probabilities for a given topic. Notably, the values form a multinomial distribution and thus sum up to 1. 
		 * For any two topics, values
		 * in corresponding arrays are for exactly the same sequence of terms.
		 * @param topicID - the ID of the desired topic, value between 0 and TopicOpinionOutput.getNumTopics()-1.
		 */
	public double[] getTopicTermScores(int topicID)
	{
		if (topicID >= 0 && topicID < this.getNumTopics() && topicTermScores != null)
		    return this.topicTermScores[topicID];
		else
			return new double[0];
	}
	
	/**
	 * The number of latent topics used in the analysis model. This parameter is automatically set by topic-opinion analysis
	 * based on corpus size and corpus properties. 
	 * @return The number K of topics produced by topic-opinion analysis.
	 */
   public int getNumTopics()
   {
	   return topicTerms.length;
   }
   
   /**
    * The collection of most relevant terms for a given topic. Returns a limited number of most relevant terms 
    * for the given topic. The number of characteristic terms is automatically determined by topic-opinion analysis.
    * @param topicID The number of the particular topic of interest, should be between 0..getNumTopics()-1. 
    * Otherwise, an empty array will be returned.
    * @return The array of most relevant terms for a given topic, sorted by topic relevance in descending order. 
    */
   public String[] getTopicTerms(int topicID)
   {
	   return topicTerms[topicID];
   }
   
   /**
    * The collection of numeric scores (probabilities) for most relevant terms for a given topic. Returns a limited number of most relevant terms 
    * for the given topic. The number of characteristic terms is automatically determined by topic-opinion analysis.
    * @param topicID The number of the particular topic of interest, should be between 0..getNumTopics()-1. 
    * Otherwise, an empty array will be returned.
    * @return The array of most relevant terms for a given topic, sorted by topic relevance in descending order. 
    */
   public double[] getTopicTermProbs(int topicID)
   {
	   return topicTermProbs[topicID];
   }
   
   
   /**
    * The collection of labels for most relevant (characteristic) documents in a given topic. 
    * In this output, each of the documents is represented by its identifier (label) that was provided  
    * to the topic-opinion analysis as part of the input data.  
    * The number of characteristic documents for the given topic is automatically determined by topic-opinion analysis.
    * @param topicID The number of the particular topic of interest, should be between 0..getNumTopics()-1. 
    * Otherwise, an empty array will be returned.
    * @return The array of IDs (labels) for documents that appear to be most relevant with respect to the given topic. 
    */
   public int[] getTopicRelevantDocIDs(int topicID)
   {
	   return topicRelevantDocIds[topicID];
   }
   
   /**
    * The collection of scores for most relevant (characteristic) documents in a given topic. 
    * In this output, each of the documents is characterized by its relevance score. The 
    * scores appear exactly in the same order and quantity as topic-specific document IDs returned by getTopicTerms().
    * @param topicID The number of the particular topic of interest, should be between 0..getNumTopics()-1. 
    * Otherwise, an empty array will be returned.
    * @return The array of document scores for documents that appear to be most relevant with respect to the given topic.
    * The scores appear exactly in same order as document IDs returned for same topic by getTopicDocIDs().   
    */
   public double[] getTopicRelevantDocScores(int topicID)
   {
	   return topicRelevantDocScores[topicID];
   }

   /**
    * The collection of labels for most relevant (characteristic) documents in a given topic. 
    * In this output, each of the documents is represented by its identifier (label) that was provided  
    * to the topic-opinion analysis as part of the input data.  
    * The number of characteristic documents for the given topic is automatically determined by topic-opinion analysis.
    * @param topicID The number of the particular topic of interest, should be between 0..getNumTopics()-1. 
    * Otherwise, an empty array will be returned.
    * @return The array of IDs (labels) for documents that appear to be most relevant with respect to the given topic. 
    */
   public int[] getTopicOpinionDocIDs(int topicID)
   {
	   return topicOpinionDocIds[topicID];
   }
   
   /**
    * The collection of scores for most relevant (characteristic) documents in a given topic. 
    * In this output, each of the documents is characterized by its relevance score. The 
    * scores appear exactly in the same order and quantity as topic-specific document IDs returned by getTopicTerms().
    * @param topicID The number of the particular topic of interest, should be between 0..getNumTopics()-1. 
    * Otherwise, an empty array will be returned.
    * @return The array of document scores for documents that appear to be most relevant with respect to the given topic.
    * The scores appear exactly in same order as document IDs returned for same topic by getTopicDocIDs().   
    */
   public double[] getTopicOpinionDocScores(int topicID)
   {
	   return topicOpinionDocScores[topicID];
   }  
   
   /**
    * The collection of reasons for most relevant (characteristic) documents in a given topic. 
    * In this output, each of the documents is characterized by its reason being in the result set.
    * The set of reasons includes relevance and strongness of several affective aspects (agreement, positiveness, etc.).
    * Reasons appear exactly in the same order and quantity as topic-specific document IDs returned by getTopicTerms().
    * @param topicID The number of the particular topic of interest, should be between 0..getNumTopics()-1. 
    * Otherwise, an empty array will be returned.
    * @return The array of explanations why particular documents appear to be most relevant with respect to the given topic.
    * The reasons appear exactly in same order as document IDs returned for same topic by getTopicDocIDs().   
    */
   public String[] getTopicOpinionDocReasons(int topicID)
   {
	   return topicOpinionDocReasons[topicID];
   }
   
   /**
    * The collection of usernames for most relevant users regarding a given topic. 
    * @param topicID The number of the particular topic of interest, should be between 0..getNumTopics()-1. 
    * Otherwise, an empty array will be returned.
    * @return The array of names (labels) for users that appear to be most important with respect to the given topic. 
    */
   public String[] getTopicUsers(int topicID)
   {
     return topicUsers[topicID];
   }
   
   /**
    * The source of one document from the input collection. The order of postings is exactly the same in  
    * input array, provided by TopicOpinionInput.
    * @param docID - document ID as position of the document in the array.
    * @return the body of requested document.
    */
   public String getDocumentBody(int docID)
   {
	   if (docs != null)
	       return docs[docID];
	   else
		   return null;
	   
   }
   
   /**
    * Username for a given document ID.
    * @param docID - document ID as position of the document in the array.
    * @return the username for requested document.
    */
   public String getUser(int docID)
   {
	   if (users != null)
	       return users[docID];
	   else
		   return null;
	   
   }
   
   /**
	 * Returns The overall positiveness/negativeness of a topic.
	 * @return The valence value on the scale between 
	 * -10 (very negative) and +10 (very positive).
	 * @param topicID - the ID of the desired topic, value between 0 and TopicOpinionOutput.getNumTopics()-1.
	 */
   public double getValence(int topicID)
   {
	   if (topicID >= 0 && topicID < this.getNumTopics())
	       return this.topicValence[topicID];
	   else
		   return 0.0;
   }
   
	/**
	 * Returns The overall controversity of a topic.
	 * @return The controversity value, computed as normalized variance over individual document specific valence scores (which all are on the scale between -10..+10).
	 * @param topicID - the ID of the desired topic, value between 0 and TopicOpinionOutput.getNumTopics()-1.
	 */
    public double getControversity (int topicID)
    {
	   if (topicID >= 0 && topicID < this.getNumTopics())
	       return this.topicControversity[topicID];
	   else
		   return 0.0;
    }
    
    /**
     * Estimates the distance between two topics in the space of term distribution vectors, using Jensen-Shannon divergence as a distance measure. 
     * From the conceptual perspective, the JS divergence can be seen as a smoothed and symmetrized relative entropy between two distributions.
     * This means, two identical distributions have zero divergence, and two distributions that have no terms in common have max divergence.
     * The implementation uses the log2 variation of JS divergence and returns values in the interval [0..1]: 0 corresponds to zero self-distance, and 1 
     * is the max possible distance. The JS divergence is symmetric (i.e. dist(a,b) = dist(b,a)).
     * @param topicID1: the ID of the first topic, value between 0 and TopicOpinionOutput.getNumTopics()-1.
     * @param topicID2: the ID of the second topic, value between 0 and TopicOpinionOutput.getNumTopics()-1.
     * @return Distribution-based distance between two topics in the range [0..1], with 0 corresponding to zero self-distance and 1 the max possible
     * distance between two "orthogonal" topics.
     */
    public double topicDist (int topicID1, int topicID2)
    {
    	if (topicID1 >= 0 && topicID1 < this.getNumTopics() && topicID2 >= 0 && topicID2 < this.getNumTopics())
    	{
    		return jensenShannonDivergence(this.topicTermScores[topicID1], this.topicTermScores[topicID2]);
    	}
    	else
    		return 1.0;
    }
    
    /**
     * Returns the Jensen-Shannon divergence.
     */
    public static double jensenShannonDivergence(double[] p1, double[] p2) {
      assert(p1.length == p2.length);
      double[] average = new double[p1.length];
      for (int i = 0; i < p1.length; ++i) {
        average[i] += (p1[i] + p2[i])/2;
      }
      return (klDivergence(p1, average) + klDivergence(p2, average))/2;
    }

    
   public static final double log2 = Math.log(2);
    /**
     * Returns the KL divergence, K(p1 || p2).
     *
     * The log is w.r.t. base 2. <p>
     *
     * *Note*: If any value in <tt>p2</tt> is <tt>0.0</tt> then the KL-divergence
     * is <tt>infinite</tt>. Limin changes it to zero instead of infinite. 
     * 
     */
    public static double klDivergence(double[] p1, double[] p2) {


      double klDiv = 0.0;

      for (int i = 0; i < p1.length; ++i) {
        if (p1[i] == 0) { continue; }
        if (p2[i] == 0.0) { continue; } // Limin

      klDiv += p1[i] * Math.log( p1[i] / p2[i] );
      }

      return klDiv / log2; // moved this division out of the loop -DM
    }
}

