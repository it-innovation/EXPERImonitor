package west.wegovdemo;

/**
 * This is the basic functionality provided by topic-opinion analysis to other WeGov components.
 * @author sizov
 *
 */
public interface TopicOpinionAnalysis 
{
  /**
   * 
   * @param in Inputs (document contents) to be analyzed
   * @return Structured results of topic-opinion analysis,including most relevant terms per topic, 
   * most significant documents per topic, document scores and reasons for inclusion into result set.
   * @see TopicOpinionOutput
   */
  TopicOpinionOutput analyzeTopicsOpinions(TopicOpinionInput in);
  
  /**
   * Set the desired number of topics in the topic-opinion analysis. If the number is not explicitly set before starting analysis, an internal estimate
   * for a suitable number of topics will be applied.
   * @param numTopics The number of topics that topic-opinion module should use for analysis.
   */
  void setNumTopics(int numTopics);
  
  /**
   * Set the desired number of most relevant terms in the topic-opinion analysis. If the number is not explicitly set before starting analysis, an internal estimate
   * for a suitable number of most relevant terms will be applied.
   * @param numTerms The number of terms that topic-opinion module should return for each topic.
   */
  void setTermsPerTopic(int numTerms);
  
  /**
   * Set the language of the analyzed document collection. This setting activates  
   * custom, language-specific morphological reduction of words to word stems for constructing document features. In parallel,
   * it activates the corresponding stopword list for term filtering (removal of language-specific irrelevant, very frequent terms).
   * @param language The language of the dataset. possible values of this parameter are currently "en" (English) and "de" (German). Default value is "en". 
   * Any other strings supplied to this function will be ignored and interpreted as "en".  
   */
  void setLanguage(String language);
}
