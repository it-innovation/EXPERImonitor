/*
var topicAnalysisResult;

var testglobal;

var testjsonresult;

var topicsDataSource;

var maxTopicPosts;

var currentRunParameters;
*/


$(document).ready(function() {
	//addTopicsTable();

  var runIdIn;


  // Testing Only

    var runIdInput =
      $("<input type=\"text\" value=0 id=\"testGetTopicAnalysisResultButton\">").appendTo("#resultsPanel");

    var testGetTopicAnalysisResultButton =
      $("<a id=\"testGetTopicAnalysisResultButton\" href=# class=\"clickableWidgetHeader\">"
      + "TESTING ONLY: Get Topic Results from DB for run in box" + "</a>")
      .appendTo("#resultsPanel");

      testGetTopicAnalysisResultButton.click(function(e){

       runIdIn = runIdInput.attr('value');

       getRunParameters(runIdIn);

       //topicAnalysisResult = getTopicAnalysisResults(runIdIn);
       getTopicAnalysisResults(runIdIn);

       console.log(topicAnalysisResult);

       addTopicsTable3(runIdIn);


    });
/*
    var newNumTopicsInput =
      $("<input type=\"text\" value=0 id=\"testRerunTopicAnalysisButton\">").appendTo("#resultsPanel");

    var testRerunTopicAnalysisButton =
      $("<a id=\"testRerunTopicAnalysisButton\" href=# class=\"clickableWidgetHeader\">"
      + "Rerun Analysis With Number of Topics" + "</a>")
      .appendTo("#resultsPanel");

      testRerunTopicAnalysisButton.click(function(e){


        // get run parameters

        getRunParameters(runIdIn);

        console.log(currentRunParameters);

        //var runParamsJson = $.parseJSON(currentRunParameters);

        var runParamsJson = currentRunParameters;

        console.log(runParamsJson);


        var analysisType;
        var analysisSubType;
        var inputDataSpec;
        var numberOfTopicsToReturn;
        var outputOfType;

        if (runParamsJson.length > 0) {
          $.each(runParamsJson, function(index, param) {

//[
//{"name":"analysis.type","value":"topic-opinion","description":"Analysis Type","run":278,"id":7853},
//{"name":"analysis.subType","value":"twitter-topics","description":"Analysis Sub-Type","run":278,"id":7854},
//{"name":"analysis.input-data-spec","value":"[{\"activityId\":259,\"runId\":\"-1\"}]","description":"JSON-formatted input data specification containing activities and runs","run":278,"id":7855},
//{"name":"numberOfTopicsToReturn","value":"3","description":"How many topics to return","run":278,"id":7856},
//{"name":"outputOfType","value":"eu.wegov.coordinator.KoblenzAnalysisTopicWrapper","description":"Classname of generated output","run":278,"id":7857}]


            if (param["name"] == "analysis.type")
                analysisType = param["value"];

            if (param["name"] == "analysis.subType")
                analysisSubType = param["value"];

            if (param["name"] == "analysis.input-data-spec")
                inputDataSpec = param["value"];
            //
            //if (param["name"] == "numberOfTopicsToReturn")
            //    numberOfTopicsToReturn = param["value"];

            if (param["name"] == "outputOfType")
                outputOfType = param["value"];


          });
        }

        numberOfTopicsToReturn = newNumTopicsInput.attr('value');

        console.log("New run parameters: "
          + "analysisType = " + analysisType
          + ", analysis.subType = " + analysisSubType
          + ", analysis.input-data-spec = " + inputDataSpec
          + ", numberOfTopicsToReturn = " + numberOfTopicsToReturn
          + ", outputOfType = "  + outputOfType);


        var   config = {
          "runNow":true,
          "analysis.type":analysisType,
          "analysis.subType":analysisSubType,
          "analysis.input-data-spec":inputDataSpec,
          "numberOfTopicsToReturn":numberOfTopicsToReturn
        };

        // need activity ID and post ID here
       // console.log ("activity id = " + activityId);
       // var config = createAnalysisConfig(
       //   "topic-opinion",
       //   "twitter-topics",
       //   true,
       //   activityId
       // );

        createNewAnalysisActivityAndRun(config);
        //var analysisRunId = createNewAnalysisActivityAndRun(config);
        //console.log ("New analysis run ID = " + analysisRunId);

       //topicAnalysisResult = getTopicAnalysisResults(runIdIn);
       getTopicAnalysisResults(runIdIn);

       console.log(topicAnalysisResult);

       addTopicsTable3(runIdIn);


    });

*/
    // End Testing Only


  // Real - uncomment once testing done
	//addTopicsTable2();
});
