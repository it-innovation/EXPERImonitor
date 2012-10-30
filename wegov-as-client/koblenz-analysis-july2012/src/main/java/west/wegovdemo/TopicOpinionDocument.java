package west.wegovdemo;

import java.util.*;
/**
 * This class serves a small container for per-document data in results returned by topic-opinion analysis: topic-specific scores and opinion weights.
 * @author skyhorse
 *
 */
public class TopicOpinionDocument
{
	public int ID;
	
	public double valence;
	
	public Map opinions;
	
	public double[] topicScores;
		
	/**
	 * Returns the ID of the represented document, consistent with other parts of the analysis package.
	 * @return ID of the represented document.
	 */
	public int getID()
	{
		return ID;
	}

	/**
	 * Returns the document opinions together with estimated weights. The Map has the form: key = opinion name, e.g. "valence" as String, and value = the weight of this opinion in the document, as estimatd by topic-opinion analysis as Double, e.g. "0.35"
	 * @return Map of opinion-name:opinion-weight pairs for the current document.
	 */
	public Map getOpinions()
	{
		return opinions;
	}
	
	/**
	 * Returns the document scores in particular topics. The outcome is an array of double values for scores; the order of topics is consistent with other parts of the Topic-Opinion outcome.
	 * @return The array of topic weights for the current document.
	 */
	public double[] getTopicScores()
	{
		return topicScores;
	}
	
	/**
	 * Returns The overall positiveness/negativeness of the given document, on the scale between 
	 * -10 (very negative) and +10 (very positive).
	 */
	public double getValence()
	{
		return valence;
	}
}
