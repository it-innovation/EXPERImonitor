package west.wegovdemo;

import west.importer.*;

/**
 * This class demonstrates the functionality of topic-opinion analysis in an offline example.
 * Prepared editorial articles from Yahoo.uk News blog are used as input (hardcoded into source code of the offline input class)
 * are passed to the WeGov topic-opinion analysis as input documents. Next, it makes a printout of 
 * received results. For experiments, any collection of text documents (represented by strings in an array) can be used. 
 */
public class OfflineDemo  
{ 
 
	public static void main(String[] args)
	{
		// The class OfflineDemoInput implements TopicOpinionInput and can be interpreted by topic-opinion analysis
		// additionally, it provides mapping between document IDs and document contents.
		// This is helpful to visualize results of the analysis
		OfflineDemoInput input = new west.wegovdemo.OfflineDemoInput();
		
		// Analysis class 
		TopicOpinionAnalysis analysis = new west.importer.WegovImporter();
		analysis.setNumTopics(5);
		analysis.setLanguage("en");
		analysis.setTermsPerTopic(3);
		
		//returned results
		TopicOpinionOutput output = analysis.analyzeTopicsOpinions(input);
		
		WegovRender.showResults(output);
	}
	
}
