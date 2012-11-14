function getTopicAnalysisResults(runId, callback) {
  $.ajax({
    url:
      "/home/analysis/getTopicAnalysisResults/do.json?runId=" + runId,
    type: 'get',

    success:function(result){
		setTopicAnalysisResults(result, callback);
    },
    async: false
  });
}

function setTopicAnalysisResults(result, callback) {
      topicAnalysisResult = result;
      //return result;

      //topicAnalysisResult = $.parseJSON(result);

      console.log(result);

      testglobal = "IT WORKS";

      testjsonresult = result;

      topicsDataSource = new kendo.data.DataSource({ data: result });

      var numTopics = topicsDataSource.transport.data.numTopics;

      console.log ("num topics = " + numTopics);

       var currentMaxPostsPerTopic = 0;
       for (var  i = 0; i < numTopics; i++) {
          var newNumPosts = parseInt(topicsDataSource.transport.data.topics[i].numPosts);
         //console.log ("i = " + i + ", new num posts = " + newNumPosts);
         if (newNumPosts > currentMaxPostsPerTopic) {
            currentMaxPostsPerTopic = topicsDataSource.transport.data.topics[i].numPosts;
           // console.log("i = " + i + ", new max = " + currentMaxPostsPerTopic);

        }
      }
      maxTopicPosts = currentMaxPostsPerTopic;

      console.log("Max posts in any topic = " + maxTopicPosts);

	  if (callback) {
		  console.log("getTopicAnalysisResults: calling " + callback);
		  callback();
	  }
	  else {
		  console.log("getTopicAnalysisResults: no display callback defined");
	  }
}

function getRunParameters(runId) {
  $.ajax({
    url:
      "/home/analysis/getRunParameters/do.json?runId=" + runId,
    type: 'get',

    success:function(result){
      currentRunParameters = result;
      console.log(result);
    },
    async: false
  });

}

function koblenzAnalysisForRun(runId) {
	//$("#searchRunResultsWrapper-2").empty();
	//$("#searchRunResultsWrapper-2").append("<p>Running analysis...</p>");
	$.get("/home/analysis/koblenz/run_data/do.json", { runId: runId }, function(results){
		//displayKoblenzAnalysisResults(runId, results);
	});
}
