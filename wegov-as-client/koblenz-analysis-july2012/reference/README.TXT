This package contains an extended prototype of WeGov topic-opinion analysis.

Code status: 28 July 2012

- SOURCE CODE can be found in 'src' folder

- DOCUMENTATION (javadoc) for key interfaces and classes can be found in 'javadoc' folder

- SUPPLEMENTAL LIBRARIES can be found in 'lib' folder

- RUNNABLE example is wegov-demo.jar 

 java -jar wegov-demo.jar

- DISTRIBUTION JAR (e.g. for integration) is wegow-topics-opinions.jar

STRUCTURE

The key classes and interfaces to look for are contained in packages west.wegovdemo and west.importer. Classes that serve inputs for 
topic-opinion analysis are expected to implement the interface TopicOpinionInput. Outputs of the analysis implement the interface 
TopicOpinionOutput. The analysis module itself implements the interface TopicOpinionAnalysis

Implementing CLASSES:

 - TopicOpinionInput: west.wegovdemo.OfflineDemoInput
 - TopicOpinionOutput: west.importer.importer.WegovOutput
 - TopicOpinionAnalysis: west.importer.WegovImporter

Idea: Input of the topic-opinion analysis is a collection of text documents (array of strings). 
The output contains 
  a) most characteristic terms for each topic,
  b) most significant documents for each topic, characterized by document IDs, scores, and reasons (e.g. 'relevance')
The output does NOT contain the bodies of input document, just document IDs. For simplicity, document IDs are just their 
position in the input array.

Example: Document with docid=5 is the second-best in the third topic ith score 0.89 for reason 'relevance'.
The numbering in Java arrays starts at 0, so we would have following entries:

   topicDocScores[2,1] = 0.89
   topicDocReasons[2,1] = 'relevance'
   topicDocIDs [2,1] = 5
 
 The original document is located at inputDocuments[5].
 
 ================================================================
 
INCREMENTAL UPDATE July 2012: 
 
1) The result of topic-opinion analysis now returns top terms and associated 
relevance scores for each topic. Relevant terms are returned as characteristic words,
not word stems. The number of terms per topic can also be customized:

TopicOpinionAnalysis analysis = new west.importer.WegovImporter();
analysis.setTermsPerTopic(3);

String[] termlist = output.getTopicTerms(i);
double[] termscores = output.getTopicTermProbs(i);
 
2) The language of the document collection can be chosen between english ("en" = default)
and German ("de"):

TopicOpinionAnalysis analysis = new west.importer.WegovImporter();
analysis.setLanguage("en");

3) The number of topics for analysis can be chosen individually:

TopicOpinionAnalysis analysis = new west.importer.WegovImporter();
analysis.setNumTopics(5);

4) For each topic we produce an opinion histogram that shows how 
most relevant documents are distributed across pos/neg scores:

String[] labels = output.getOpinionHistogramLabels();
int[][] histogram = output.getOpinionHistogram(i);

for each label, the histogram contains a list of topic-relevant documentt IDs that 
have a certain degree of positiveness/negativeness. 
     
5) The use of all new functions is demonstrated in west.wegovdemo.OfflineDemo. All functions are also documented in the new javadoc. 
  