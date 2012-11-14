var widgetData = null;
var theWidgetSource = null;

$(document).ready(function() {
	var wId = loadPageVar("w");

	// Get widget metadata
	$.get("/home/widgets/getWidget/do.json", { wId: wId }, function(theWidget){
		console.log(theWidget);
		theWidgetSource = theWidget;
		if (theWidget) {
			$("#titlePanel").append("<h1 class=\"pageHeader\">" + theWidget["name"] + "</h1>");

			// Get widget data TODO: there is a better way by sending query for each set of results
			$.get("/home/widgets/getWidgetData/do.json", { wId: wId }, function(widgetDataUpdate){
				widgetData = widgetDataUpdate;
//				console.log(widgetData);

				if (widgetData) {
					var displayWidget = widgetData[0];
					showResultsWithId(displayWidget["id"], widgetData);

					$("#history").kendoGrid({
						dataSource: {
							type : "json",
							serverPaging: false,
				            serverSorting: false,
							data: widgetData,
							schema: {
					            model: {
					                id: "id"
					            }
					        },
					        sort: {
								field: "collected_at", dir : "desc"
							},
							pageSize: 10
						},

						columns : [ {
							title : "Name",
							field : "name"
						}, {
							title : "Created",
							field : "collected_at",
							width : "200px"
						} ],
						filterable : true,
						sortable : true,
						pageable : true,
						groupable : true,
						selectable : "row",
						change: function(e) {
							var a = this.select();
//							console.log(a.data("id"));
							showResultsWithId(a.data("id"), widgetData);
//							console.log(this.select());
						}
					});

					var theGrid = $("#history").data("kendoGrid");
					theGrid.select(theGrid.table.find('tr[data-id="' + displayWidget["id"] + '"]'));

				} else {
					$("#history").append("<p>No data found.</p>");
				}
			});


		} else {
			$("#titlePanel").append("<h2 class=\"error\">You do not have permission to access that data. You probably got here by mistake...</h2>");
		}
	});


});

function getPosts(displayWidget) {
	var dataAsJson = jQuery.parseJSON(displayWidget["dataAsJson"]);

	/*
	//KEM Facebook tool returns results in a "results" element
	console.log(wdJson.type);
	if (! $.isArray(wdJson)) {
		console.log("results are not an array");
    if (wdJson.results != null) {
      console.log("results");
      //console.log(wdJson.results);
      wdJson = wdJson.results;
    }
    else if (wdJson.postData != null) {
      console.log("postData");
      //console.log(wdJson.postData);
      //console.log(wdJson.postData.results);
      wdJson = wdJson.postData.results;
    }
	}
	*/

	var postData;

	if ($.isArray(dataAsJson)) {
		console.log("WARNING: results are not in correct format. JSON object expected, but JSON array found");
		console.log(dataAsJson);
		return dataAsJson; //assume posts are already in this array
	}
	else {
		postData = dataAsJson.postData;
	}

	var userData = dataAsJson.userData;
	var posts;

	if (displayWidget.type == "posts-twitter") {
		if (postData)
			posts = postData.results;
		else
			posts = dataAsJson.results; // legacy format
	}
	else if ( (displayWidget.type == "posts-facebook") ||
			  (displayWidget.type == "comments-facebook") ){
		if (postData)
			posts = postData.data;
		else
			posts = dataAsJson.results; // legacy format
	}
	else {
		alert("ERROR: cannot display results for type: " + displayWidget.type);
		return null;
	}

	//console.log(posts);
	return posts;
}

function displayTwitterPosts(displayWidget) {
	//var wdJson = jQuery.parseJSON(displayWidget["dataAsJson"]);
	var wdJson = getPosts(displayWidget);
	if (wdJson == null) return;
	var postWrapperDiv, postContentsDiv, createdAt;

  var dataAsJson = jQuery.parseJSON(displayWidget["dataAsJson"]);
  console.log(dataAsJson);

	$("#otherPanel").append('<h2 class=\"settingsHeading\">Search query:</h2><p class=\"settingsValue\">' + dataAsJson.postData["query"] + '</p>');

	//$("#otherPanel").append('<h2 class=\"settingsHeading\">Search query:</h2><p class=\"settingsValue\">' + wdJson.query + '</p>');
	$("#otherPanel").append('<h2 class=\"settingsHeading\">Near to:</h2><p class=\"settingsValue\">' + displayWidget["location"] + '</p>');
	$("#otherPanel").append('<h2 class=\"settingsHeading\">Posts found:</h2><p class=\"settingsValue\">' + wdJson.length + '</p>');
	$("#otherPanel").append('<h2 class=\"settingsHeading\">Collected at:</h2><p class=\"settingsValue\">' + displayWidget["collected_at"] + '</p>');

	$("#titlePanel").find("h1").text("Recent Posts");

  var activityId = -1;
  if (displayWidget["activityid"]) {
    activityId = displayWidget["activityid"];
    console.log ("Activity ID = " + activityId);
  }

	var hashTags = new Array();
	var urls = new Array();
	if (wdJson.length > 0) {

/*
    $("<div class=\"clearfix\"></div>").appendTo("#resultsPanel");


    var numTopicsInput =
      $("<input type=\"text\" value=-1 id=\"numTopics\">").appendTo("#resultsPanel");

    var topicAnalyseAllTweetsButton =
      $("<a id=\"topicAnalyseAllTweetsButton\" href=# class=\"clickableWidgetHeader\">"
      + "Analyse Topics from all Tweets from this Search" + "</a>")
      .appendTo("#resultsPanel");

      topicAnalyseAllTweetsButton.click(function(e){

        var numTopics = numTopicsInput.attr('value');
        // need activity ID and post ID here
        console.log ("activity id = " + activityId);
        var config = createAnalysisConfig(
          "topic-opinion",
          "twitter-topics",
          true,
          activityId,
          -1,
          numTopics
        );
        createNewAnalysisActivityAndRun(config);
        //var analysisRunId = createNewAnalysisActivityAndRun(config);
        //console.log ("New analysis run ID = " + analysisRunId);

    });

    $("<div class=\"clearfix\"></div>").appendTo("#resultsPanel");

    var behaviourAnalyseAllTweetsButton =
      $("<a id=\"behaviourAnalyseAllTweetsButton\" href=# class=\"clickableWidgetHeader\">"
      + "Analyse Behaviour from all Tweets from this Search" + "</a>")
      .appendTo("#resultsPanel");

      behaviourAnalyseAllTweetsButton.click(function(e){

        // need activity ID and post ID here
        console.log ("activity id = " + activityId);
        var config = createAnalysisConfig(
          "behaviour",
          "twitter-behaviour",
          true,
          activityId
        );
        createNewAnalysisActivityAndRun(config);
        //var analysisRunId = createNewAnalysisActivityAndRun(config);
        //console.log ("New analysis run ID = " + analysisRunId);

    });

    $("<div class=\"clearfix\"></div>").appendTo("#resultsPanel");
*/

/*
	// FOR TESTING ONLY

    var runIdInput =
      $("<input type=\"text\" value=0 id=\"testGetTopicAnalysisResultButton\">").appendTo("#resultsPanel");

    var testGetTopicAnalysisResultButton =
      $("<a id=\"testGetTopicAnalysisResultButton\" href=# class=\"clickableWidgetHeader\">"
      + "TESTING ONLY: Get Topic Results from DB for run in box" + "</a>")
      .appendTo("#resultsPanel");

      testGetTopicAnalysisResultButton.click(function(e){

       var runIdIn = runIdInput.attr('value');


        $.get("analysis/getTopicAnalysisResults/do.json", {
          runId: runIdIn
        } );

    });

    $("<div class=\"clearfix\"></div>").appendTo("#resultsPanel");

*/

/*
    var runIdInput =
      $("<input type=\"text\" value=0 id=\"testGetBehaviourAnalysisResultButton\">").appendTo("#resultsPanel");
*/

/*
    var testGetBehaviourAnalysisResultButton =
      $("<a id=\"testGetBehaviourAnalysisResultButton\" href=# class=\"clickableWidgetHeader\">"
      + "TESTING ONLY: Get Behaviour Results from DB for run in box" + "</a>")
      .appendTo("#resultsPanel");

      testGetBehaviourAnalysisResultButton.click(function(e){

       var runIdIn = runIdInput.attr('value');


        $.get("analysis/getBehaviourAnalysisResults/do.json", {
          runId: runIdIn
        } );

    });


    var topicAnalyseAllTweetsFromDifferentActivitiesButton =
      $("<a id=\"topicAnalyseAllTweetsFromDifferentActivitiesButton\" href=# class=\"clickableWidgetHeader\">"
      + "TESTING ONLY: Analyse Topics from all Tweets from the search activity IDS in the box - separate activity IDs by commas" + "</a>")
      .appendTo("#resultsPanel");

      topicAnalyseAllTweetsFromDifferentActivitiesButton.click(function(e){

        var activityIDArray = runIdInput.attr('value').split(',');

        // need activity ID and post ID here
        console.log ("activity id = " + activityId);
        var config = createAnalysisConfig(
          "topic-opinion",
          "twitter-topics",
          true,
          activityIDArray
        );
        createNewAnalysisActivityAndRun(config);
        //var analysisRunId = createNewAnalysisActivityAndRun(config);
        //console.log ("New analysis run ID = " + analysisRunId);

    });


    var behaviourAnalyseAllTweetsFromDifferentActivitiesButton =
      $("<a id=\"behaviourAnalyseAllTweetsFromDifferentActivitiesButton\" href=# class=\"clickableWidgetHeader\">"
      + "TESTING ONLY: Analyse Behaviour from all Tweets from the search activity IDS in the box - separate activity IDs by commas" + "</a>")
      .appendTo("#resultsPanel");

      behaviourAnalyseAllTweetsFromDifferentActivitiesButton.click(function(e){

        var activityIDArray = runIdInput.attr('value').split(',');

        // need activity ID and post ID here
        console.log ("activity id = " + activityId);
        var config = createAnalysisConfig(
          "behaviour",
          "twitter-behaviour",
          true,
          activityIDArray
        );
        createNewAnalysisActivityAndRun(config);
        //var analysisRunId = createNewAnalysisActivityAndRun(config);
        //console.log ("New analysis run ID = " + analysisRunId);

    });


    $("<div class=\"clearfix\"></div>").appendTo("#resultsPanel");


   // END FOR TESTING ONLY
*/

		var postContents;
		var geocoder = new google.maps.Geocoder();

		$.each(wdJson, function(index, post) {
			var id = post["id_str"];
			postWrapperDiv = $("<div class=\"singlePostWrapper\"></div>").appendTo("#resultsPanel");

			if (index >= maxResultsToDisplay) {
				console.log("Reached limit of results to display: " + maxResultsToDisplay);
				postWrapperDiv.append('<p style="font-weight: bold;">WARNING: results display limited to ' + maxResultsToDisplay + '</p>');
				return false;
			}

			postContents = post["text"];
			//console.log(postContents);
			createdAt = post["created_at"];

			var geo = post["geo"];
			var coords;
			var locationHtml = '';

			if (geo) {
				//console.log(JSON.stringify(geo));
				coords = geo["coordinates"];
				if (coords) {
					//console.log(JSON.stringify(coords));
					//geocode2(coords[0], coords[1], function(results, status) {
					//	console.log(results);
					//});
					var locationSpanId = "geo-" + post["id_str"];
					var formattedCoords = 'Lat: ' + coords[0] + ', Lon: ' + coords[1];
					locationHtml = '<p><span id="' + locationSpanId + '">' + formattedCoords + '</span></p>';

					var latLng = new google.maps.LatLng(coords[0], coords[1]);
					geocoder.geocode({'latLng': latLng}, function(results, status) {
						if (results) {
							console.log($("#" + locationSpanId));
							console.log(results[0].formatted_address);
							$("#" + locationSpanId).text(results[0].formatted_address);
						}
					});
				}
			}

			postWrapperDiv.append('<img src="' + post["profile_image_url_https"] + '">');
			postContentsDiv = $('<div class="postContentsWrapper"></div>').appendTo(postWrapperDiv);
			postContentsDiv.append('<div class="postHeader">' +
				'<span class="dateCreated">' + formatTwitterDate(createdAt) + '</span>' +
				'<a class="userName" target="_blank" href="https://twitter.com/' + post["from_user"]	+ '">' + $.trim(post["from_user_name"]) + '</a>' +
				locationHtml +
								'</div>');
			postContentsDiv.append('<p class="postContents">' + twttr.txt.autoLink(postContents) + '</p>');
			//postContentsDiv.append('<p class="dateCreated">' + formatTwitterDate(createdAt) + '</p>');
			postContentsDiv.append('<div class="postFooter"><ul class="actions">' +
									'<li><a class="with-icn" href="https://twitter.com/intent/tweet?in_reply_to=' + id + '"><i class="sm-reply"></i><b>Reply</b></a></li>' +
									'<li><a class="with-icn" href="https://twitter.com/intent/retweet?tweet_id=' + id + '"><i class="sm-retweet"></i><b>Retweet</b></a></li>' +
									'<li><a class="with-icn" href="https://twitter.com/intent/favorite?tweet_id=' + id + '"><i class="sm-favorite"></i><b>Favorite</b></a></li>' +
									'</ul></div>');
			postWrapperDiv.append("<div class=\"clearfix\"></div>");

			$.each(twttr.txt.extractHashtags(postContents), function(index, theTag){
				if ($.inArray(theTag, hashTags) < 0) {
					hashTags.push(theTag);
				}
			});

			$.each(twttr.txt.extractUrls(postContents), function(index, theUrl){
				if ($.inArray(theUrl, urls) < 0) {
					urls.push(theUrl);
				}
			});

		});

		if (hashTags.length > 0) {
			$("#otherPanel").append('<h2 class=\"settingsHeadingextraSpaceTop\">Hashtags:</h2>');
			$.each(hashTags, function(index, el){
				$("#otherPanel").append('<a class=\"hashtagItem\" target=\"_blank\" href="http://twitter.com/search/%23' + el + "\">#" + el + '</a>');

			});
		}

		if (urls.length > 0) {
			$("#otherPanel").append('<h2 class=\"settingsHeadingextraSpaceTop\">Links:</h2>');
			$.each(urls, function(index, el){
				$("#otherPanel").append('<a class=\"hashtagItem\" target=\"_blank\" href="' + el + "\">" + el + '</a>');

			});
		}
	} else {
		$("#resultsPanel").append("<p>Nothing was found.</p>");
	}
}

var timezone = "BST";
function formatTwitterDate(dateStr) {
	//return dateStr.substring(0, dateStr.length - 6);
	var d = Date.parse(dateStr);
	d.setTimezone(timezone);
	return (d.toString("ddd, d MMM yyyy HH:mm:ss ") + timezone);
}

function displayFacebookPosts(displayWidget) {
	//var wdJson = jQuery.parseJSON(displayWidget["dataAsJson"]);
	var wdJson = getPosts(displayWidget);
	var postWrapperDiv, postContentsDiv, createdAt;

	$("#otherPanel").append('<h2 class=\"settingsHeading\">Posts found:</h2><p class=\"settingsValue\">' + wdJson.length + '</p>');
	$("#otherPanel").append('<h2 class=\"settingsHeading\">Collected at:</h2><p class=\"settingsValue\">' + displayWidget["collected_at"] + '</p>');

	if (theWidgetSource != null) {
		$("#titlePanel").find("h1").text(theWidgetSource["name"] + ": " + jQuery.parseJSON(theWidgetSource["parametersAsString"]).term);
	}

  var activityId = -1;
  if (displayWidget["activityid"]) {
    activityId = displayWidget["activityid"];
    console.log ("Activity ID = " + activityId);
    }

	if (wdJson.length > 0) {
		console.log(wdJson);

    $("<div class=\"clearfix\"></div>").appendTo("#resultsPanel");

/*
    var analyseAllGroupButton =
      $("<a id=\"analyseAllGroupButton\" href=# class=\"clickableWidgetHeader\">"
      + "Analyse Topics from All Posts & Comments Collected So Far" + "</a>")
      .appendTo("#resultsPanel");

      analyseAllGroupButton.click(function(e){

        // need activity ID and post ID here
        console.log ("activity id = " + activityId);
        var config = createAnalysisConfig(
          "topic-opinion",
          "facebook-group-topics",
          true,
          activityId
        );
        createNewAnalysisActivityAndRun(config);

    });

    $("<div class=\"clearfix\"></div>").appendTo("#resultsPanel");
*/

/*
    // TESTING ONLY

    var testIdInput =
      $("<input type=\"text\" value=0 id=\"analyseMultipleGroupsButton\">").appendTo("#resultsPanel");

    var analyseMultipleGroupsButton =
      $("<a id=\"analyseMultipleGroupsButton\" href=# class=\"clickableWidgetHeader\">"
      + "TESTING ONLY: Analyse Topics from Multiple Groups (activity IDs for group searches in the box separated by commas)" + "</a>")
      .appendTo("#resultsPanel");

      analyseMultipleGroupsButton.click(function(e){

        var activityIDArray = testIdInput.attr('value').split(',');

        // need activity ID and post ID here
        console.log ("activity id = " + activityId);
        var config = createAnalysisConfig(
          "topic-opinion",
          "facebook-group-topics",
          true,
          activityIDArray
        );
        createNewAnalysisActivityAndRun(config);

    });

    // END TESTING ONLY
*/

    $("<div class=\"clearfix\"></div>").appendTo("#resultsPanel");



		$.each(wdJson, function(index, post) {
			postWrapperDiv = $("<div class=\"singlePostWrapper\"></div>").appendTo("#resultsPanel");

			if (index >= maxResultsToDisplay) {
				console.log("Reached limit of results to display: " + maxResultsToDisplay);
				postWrapperDiv.append('<p style="font-weight: bold;">WARNING: results display limited to ' + maxResultsToDisplay + '</p>');
				return false;
			}

			if (post["picture"])
				postWrapperDiv.append('<img src="' + post["picture"] + '">');
			else
				postWrapperDiv.append('<img src="https://graph.facebook.com/' + post.from.id + '/picture">');

			postContentsDiv = $('<div class="postContentsWrapper"></div>').appendTo(postWrapperDiv);
			postContentsDiv.append("<a class=\"userName\" target=\"_blank\" href=\"http://facebook.com/" + post.from.id	+ "\">" + $.trim(post.from.name) + "</a>");

			if (post["name"])
				postContentsDiv.append('<p class="postTitle">' + post["name"] + '</p>');

			if (post["message"])
				postContentsDiv.append('<p class="postContents">' + post["message"] + '</p>');

			if (post["source"])
				postContentsDiv.append('<p class="postContents">' + post["source"] + '</p>');

			if (post["story"])
				postContentsDiv.append('<p class="postContents">' + post["story"] + '</p>');

			if (post["likes"])
				postContentsDiv.append('<p class="postLikes">' + post.likes.count + ' likes, ' + post.comments.count + ' comments</p>');

      var commentCount = -1;
			if (post["comments"]) {
        commentCount = post["comments"]["count"];
				postContentsDiv.append('<p class="postContents">Number of Comments: ' + commentCount + '</p>');
      }


			postContentsDiv.append('<p class="postID">ID: ' + post.id + '</p>');


      var commentCount = -1;
			if (post["comments"]) {
        commentCount = post["comments"]["count"];
				postContentsDiv.append('<p class="postContents">Number of Comments: ' + commentCount + '</p>');
      }


/*
      	var commentsLinkButton = $("<p class=\"widgetSettings\">Get comments in new Widget</p>")
          .appendTo(postContentsDiv);
  */
      // only display comments and analyse comments button if the number of comments > 0!
      if (commentCount > 0) {
        var commentsLinkButton =
          $("<a id=\"getComments\" href=# class=\"clickableWidgetHeader\">"
          + "Get Comments in a new Widget" + "</a>")
          .appendTo(postContentsDiv);

          commentsLinkButton.click(function(e){
          var groupCommentsPopup = jQuery("<div id=\"userSettings\" class=\"closed-by-escape\"></div>")
            .appendTo("#wrapper");

          groupCommentsPopup.draggable();

          groupCommentsPopup.append("<h3>Create Widget for Comments</h3>");

          var availableWidgetsWrapper = $("<div class=\"userSettingsWrapper\"></div>")
            .appendTo(groupCommentsPopup);

          var parameters;
          var sourceWidgetId;
          var postId = post.id;
          var postName = null;
          var postMessage = null;

          if (post["name"]) postName = post["name"];
          if (post["message"]) postMessage = post["message"];

          // get the template widget for group post comments
          $.get("/home/widgets/getTemplateWidgetsMatchingWidgetType/do.json",
            {widgetType: "grouppostcomments"},
            function(widgets)
          {
            //var checkedValue;
            //var theInput;
            //var parameters;
            //var sourceWidgetId;

            if (widgets.length < 1) {
              console.log("Error - no group post comments widget in template set.");
              availableWidgetsWrapper.append(
                "<p class=\"myWidgetsLabel\">"
                + "Error - no group post comments widget in template set."
                + "</p>");

            }
            else {
              // get the first (should be only) widget
              // we only need a template so any of the
              // right type will do
              var templateGroupPostCommentWidget = widgets[0];

              parameters = jQuery.parseJSON(templateGroupPostCommentWidget["parametersAsString"]);
              sourceWidgetId = templateGroupPostCommentWidget["id"]
  /*
              theInput =
                $("<input type=\"checkbox\" value=\"\" class=\"myWidgetsInput\" "
                + checkedValue  + ">")
                .appendTo(availableWidgetsWrapper);

              var inputDom = theInput[0];
              jQuery.data(inputDom, "widgetId", widget["id"]);
              jQuery.data(inputDom, "widgetParameters", parameters);
  */

              availableWidgetsWrapper.append(
                "<p class=\"myWidgetsLabel\">"
                + "Press \"Save\" to create a new widget to get the comments for post: "
                + postId + "</p>");

              availableWidgetsWrapper.append(
                "<p class=\"myWidgetsLabel\">"
                + templateGroupPostCommentWidget["name"] + "</p>");


              var createWidgetControlsSave =
                $("<p id=\"userSettingsSaveButton\"><span lang=\"en\">Save</span></p>")
                .appendTo(createWidgetControlsWrapper);

              createWidgetControlsSave.click(function(e){
                console.log("Saving new user settings");
                parameters.term = postId;
                if (postName != null) parameters.postName = postName;
                if (postMessage != null) parameters.postMessage = postMessage;

                // auto refresh data on next reload
                parameters.autoRefreshData = "true";


                console.log("Duplicating Widget: [" + sourceWidgetId + "]");
                $.get("/home/widgets/duplicateWidgetToCallingUserDefaultSet/do.json",
                  {wId: sourceWidgetId, parametersAsString: JSON.stringify(parameters)} );


                $('.closed-by-escape').hide();
                groupCommentsPopup.remove();
                //loadWidgets();

              });

            }
          });

          groupCommentsPopup.append("<div class=\"clearfix\"></div>");

          var createWidgetControlsWrapper =
            $("<div class=\"userSettingsControlsWrapper\"></div>")
            .appendTo(groupCommentsPopup);

          var createWidgetControlsCancel =
            $("<p id=\"userSettingsCancelButton\"><span lang=\"en\">Cancel</span></p>")
            .appendTo(createWidgetControlsWrapper);


          createWidgetControlsCancel.click(function(e){
            $('.closed-by-escape').hide();
            groupCommentsPopup.remove();
          });


          popupBackground(groupCommentsPopup.attr('id'));

        });


        postContentsDiv.append("<div class=\"clearfix\"></div>");

        var analyseCommentsButton =
          $("<a id=\"analyseComments\" href=# class=\"clickableWidgetHeader\">"
          + "Analyse Comments" + "</a>")
          .appendTo(postContentsDiv);

          analyseCommentsButton.click(function(e){

			var numTopics = getNumTopics();
			var language = getSelectedLanguage();
			var runId = -1;

			// need activity ID and post ID here
            console.log ("activity id = " + activityId + ", post id = " + post.id);
            var config = createAnalysisConfig(
              "topic-opinion",
              "facebook-post-comments-topics",
              true,
              activityId,
              post.id,
			  numTopics,
			  language,
			  runId
            );
            createNewAnalysisActivityAndRun(config);

        });
      }

			createdAt = post["created_time"];
			postContentsDiv.append('<p class="dateCreated">' + createdAt.substring(0, createdAt.length - 5).replace("T", " ") + '</p>');
			postWrapperDiv.append("<div class=\"clearfix\"></div>");

//			$.each(twttr.txt.extractUrls(postContents), function(index, theUrl){
//				if ($.inArray(theUrl, urls) < 0) {
//					urls.push(theUrl);
//				}
//			});

		});
	} else {
		$("#resultsPanel").append("<p>Nothing was found.</p>");
	}
}


function activityRunPostId(activityId, runId, postId) {
  this.activityId = activityId;
  this.runId = runId;
  this.postId = postId;
}

//function createAnalysisConfig(analysisType, analysisSubType, runNow) {
//  return createAnalysisConfig(analysisType, analysisSubType, runNow, -1, undefined);
//  }

//function createAnalysisConfig(analysisType, analysisSubType, runNow, activityId) {
//  return createAnalysisConfig(analysisType, analysisSubType, runNow, activityId, undefined);
//  }


function createAnalysisConfig(analysisType, analysisSubType, runNow, activityId, postId, numTopics, language, runId) {
  console.log("Creating analysis config");
  var config;

//       * analysisType is either "topic-opinion" or "behaviour"

  var inputRuns=new Array();

  if (activityId instanceof Array) {

    console.log ("Input activityId is an array - therefore multiple activities");

    // loop through activities
	// each activity may be either an object, e.g. {activityId: activityId, runId: runId}, or activity id
    for(var i = 0; i < activityId.length; i++){
		var activity = activityId[i];
		if (activity instanceof Object) {
			inputRuns[i] = activity;
		}
		else {
			inputRuns[i] = (new activityRunPostId(activityId[i], "-1"));
		}
    }

  }
  else {

    // we have one activity and can specify other things

	console.log("Activity id = " + activityId);
	console.log("Run id = " + runId);
	console.log("Post id = " + postId);

	if (activityId > 0 && (postId != undefined) && (postId != -1)) {
      // case where we want to monitor the results for a post id
      // - activityId is always needed
      inputRuns[0] = (new activityRunPostId(activityId, "-1", postId));
    }
    else if (activityId > 0) {
		if (runId) {
			inputRuns[0] = (new activityRunPostId(activityId, runId));
		}
		else {
			// case where we want all runs for an activity
			inputRuns[0] = (new activityRunPostId(activityId, "-1"));
		}
    }
    else {
      // default case - hard coded
     inputRuns[0] = (new activityRunPostId("32", "26"));
     inputRuns[1] = (new activityRunPostId("32", "28"));
     inputRuns[2] = (new activityRunPostId("32", "31"));
     inputRuns[3] = (new activityRunPostId("32", "32"));
   }

  }

	// don't need to stringify here
	//var jsonInputRuns = JSON.stringify(inputRuns);

	var activityName = analysisType;
	activityName += " (" + analysisSubType + ")";
	activityName += "; lang:" + language;
	if (numTopics ) {
		activityName += "; numTopics:" + numTopics;
	}
	activityName += " for " + JSON.stringify(inputRuns);

//if (numTopics > 0) {
  config = {
    "runNow":runNow,
    "analysis.type":analysisType,
    "analysis.subType":analysisSubType,
    "analysis.input-data-spec":inputRuns,
    "numberOfTopicsToReturn":numTopics,
    "analysisLanguage":language,
	"name":activityName
  };

//}
//else {
//    config = {
//    "runNow":runNow,
//    "analysis.type":analysisType,
//    "analysis.subType":analysisSubType,
//    "analysis.input-data-spec":jsonInputRuns
//  };
//}

  console.log(JSON.stringify(config));

  return config;

}


/*
function createAnalysisConfig(analysisType, analysisSubType, runNow, activityId, postId) {
  console.log("Creating analysis config");
  var config;

//       * analysisType is either "topic-opinion" or "behaviour"


  var inputRuns=new Array();

  if (activityId > 0 && postId != undefined) {
    // case where we want to monitor the results for a post id
    // - activityId is always needed
    inputRuns[0] = (new activityRunPostId(activityId, "-1", postId));
  }
  else if (activityId > 0) {
    // case where we want all runs for an activity
    inputRuns[0] = (new activityRunPostId(activityId, "-1"));
  }
  else {
    // default case - hard coded
   inputRuns[0] = (new activityRunPostId("32", "26"));
   inputRuns[1] = (new activityRunPostId("32", "28"));
   inputRuns[2] = (new activityRunPostId("32", "31"));
   inputRuns[3] = (new activityRunPostId("32", "32"));
 }


  var jsonInputRuns = JSON.stringify(inputRuns);


  config = {
    "runNow":runNow,
    "analysis.type":analysisType,
    "analysis.subType":analysisSubType,
    "analysis.input-data-spec":jsonInputRuns

  };

  console.log(config);

  return config;

}
*/

// Create new analysis activity and run
function createNewAnalysisActivityAndRun(config) {
  //myRunId = -1;

  initialiseAnalysisResults(config);

  $.ajax({
      type: 'POST',
      url: "/home/analysis/createNewAnalysis/do.json",
      contentType: "application/json; charset=utf-8",
      data: JSON.stringify(config),
      error: function() {
        console.log("WARNING: failed to write new activity to db! This will not be stored in the database");
        reportAnalysisError("Failed to create activity for analysis", config, true);
      },
      success: function(actAndRun){
        console.log('actAndRun type: ' + (typeof actAndRun));

        if (typeof actAndRun === 'object') {
          console.log('actAndRun: ' + JSON.stringify(actAndRun));
        }
        else {
          console.log('actAndRun: ' + actAndRun);
          if (actAndRun.indexOf('<html>') != -1) {
            location.reload();
          }
        }

        if (actAndRun.error) {
          //reportSearchError("ERROR: failed to create new search: " + actAndRun.error, config, true);
          reportAnalysisError(actAndRun.error, config, true);
          updateAnalysesList(true);
        }
        else {
          console.log("New analysis created");
        }

        console.log("Activity: " + actAndRun.activityId);
        console.log("Run: " + actAndRun.runId);

        if (actAndRun.runId > 0) {
			console.log("Setting metadata for run " + actAndRun.runId);
			runMetadata[actAndRun.runId] = {activity: actAndRun.activityId};
			console.log(JSON.stringify(runMetadata[actAndRun.runId]));
          if (config.runNow) {
            monitorRun(actAndRun.runId, config);
          }
          else {
            if (! monitoring) {
              startMonitoring();
            }
            else {
              updateAnalysesList(true);
            }
          }
        }
        else if (actAndRun.activityId > 0) {
          console.log("Activity created but no run available yet");
          updateAnalysesList(true);
        }
        else {
          console.log("WARNING: no activity or run returned");
        }
      },
      complete: function(jqXHR, textStatus) {
        console.log(textStatus);
      }

  });
}

function reportAnalysisError(error, config, showAlert) {
	console.log(error);
	if (config && config.runNow) {
		//clearSearchResults();
		//$("#resultsPanel").append("<p>" + error + "</p>");
		//canRun = true;
		var analysisType = config["analysis.type"];
		clearAnalysisResults(analysisType);
	}
	if (showAlert) {
		alert(error);
	}
}

function updateAnalysesList(forceUpdate) {
	console.log("updateSearchesList not yet implemented");
	//TODO
}

function displayFacebookComments(displayWidget) {
	//var wdJson = jQuery.parseJSON(displayWidget["dataAsJson"]);
	var wdJson = getPosts(displayWidget);
	var postWrapperDiv, postContentsDiv, createdAt;

	$("#otherPanel").append('<h2 class=\"settingsHeading\">Comments found:</h2><p class=\"settingsValue\">' + wdJson.length + '</p>');
	$("#otherPanel").append('<h2 class=\"settingsHeading\">Collected at:</h2><p class=\"settingsValue\">' + displayWidget["collected_at"] + '</p>');

	if (theWidgetSource != null) {

    var parameters = jQuery.parseJSON(theWidgetSource["parametersAsString"]);

    var groupPostID = null;
    var postName = null;
    var postMessage = null;
    if (parameters.term != null) groupPostID = parameters.term;
    if (parameters.postName != null) postName = parameters.postName;
    if (parameters.postMessage != null) postMessage = parameters.postMessage;

    var headerText = null;
    if (groupPostID != null) {
      headerText = "\"" + postName + "\"" + " (ID = " + groupPostID + ")";
    }
    else {
      headerText = groupPostID;
    }

    //if (postName != null) $("#titlePanel").append('<p class="postContents">' + postName + "</p>");
    //if (postMessage != null) $("#titlePanel").append("<p>" + postMessage + "</p>");


    //$("#titlePanel").find("h1").text(theWidgetSource["name"] + ": " + jQuery.parseJSON(theWidgetSource["parametersAsString"]).term);
    $("#titlePanel").find("h1").text(theWidgetSource["name"] + ": " + headerText);
    //	$("#titlePanel").find("h1").text(theWidgetSource["name"]);

	}

	if (wdJson.length > 0) {
		console.log(wdJson);
		$.each(wdJson, function(index, post) {
			postWrapperDiv = $("<div class=\"singlePostWrapper\"></div>").appendTo("#resultsPanel");

			if (index >= maxResultsToDisplay) {
				console.log("Reached limit of results to display: " + maxResultsToDisplay);
				postWrapperDiv.append('<p style="font-weight: bold;">WARNING: results display limited to ' + maxResultsToDisplay + '</p>');
				return false;
			}

			if (post["picture"])
				postWrapperDiv.append('<img src="' + post["picture"] + '">');
			else
				postWrapperDiv.append('<img src="https://graph.facebook.com/' + post.from.id + '/picture">');

			postContentsDiv = $('<div class="postContentsWrapper"></div>').appendTo(postWrapperDiv);
			postContentsDiv.append("<a class=\"userName\" target=\"_blank\" href=\"http://facebook.com/" + post.from.id	+ "\">" + $.trim(post.from.name) + "</a>");

//			if (post["name"])
//				postContentsDiv.append('<p class="postTitle">' + post["name"] + '</p>');

			if (post["message"])
				postContentsDiv.append('<p class="postContents">' + post["message"] + '</p>');

			if (post["source"])
				postContentsDiv.append('<p class="postContents">' + post["source"] + '</p>');
//
//			if (post["story"])
//				postContentsDiv.append('<p class="postContents">' + post["story"] + '</p>');

			if (post["likes"])
				postContentsDiv.append('<p class="postLikes">' + post.likes + ' likes</p>');

			createdAt = post["created_time"];
			postContentsDiv.append('<p class="dateCreated">' + createdAt.substring(0, createdAt.length - 5).replace("T", " ") + '</p>');
			postWrapperDiv.append("<div class=\"clearfix\"></div>");

		});
	} else {
		$("#resultsPanel").append("<p>Nothing was found.</p>");
	}
}

function displayFacebookTopics(displayWidget) {
	//var wdJson = jQuery.parseJSON(displayWidget["dataAsJson"]);
	var wdJson = getPosts(displayWidget);

	var topicWrapperDiv, topicContentsDiv;

	$("#otherPanel").append('<h2 class=\"settingsHeading\">Topics found:</h2><p class=\"settingsValue\">' + wdJson.numTopics + '</p>');
	$("#otherPanel").append('<h2 class=\"settingsHeading\">Collected at:</h2><p class=\"settingsValue\">' + displayWidget["collected_at"] + '</p>');

	if (theWidgetSource != null) {
		$("#titlePanel").find("h1").text(theWidgetSource["name"] + ": " + jQuery.parseJSON(theWidgetSource["parametersAsString"]).term);
	//	$("#titlePanel").find("h1").text(theWidgetSource["name"]);
	}

//	console.log(wdJson);

	if (wdJson.topics.length > 0) {
		console.log(wdJson.topics);
		$.each(wdJson.topics, function(index, topic) {

			topicWrapperDiv = $("<div class=\"singleTopicWrapper\"></div>").appendTo("#resultsPanel");

			topicWrapperDiv.append("<p class=\"koblenzTopicKeywords\">" + (index + 1) + ". " + topic["keywords"] + "</p>");
			topicWrapperDiv.append("<p class=\"koblenzTopicKeyusersHeader\">Key Users:</p>");

			  $.each(topic["keyUsers"], function(keyUserCounter, keyUser){
				  var keyUserEntry = $("<div class=\"koblenzUserWrapper\"></div>").appendTo(topicWrapperDiv);
				  keyUserEntry.append("<img src=\"" + keyUser["profileImageUrl"] + "\">");
				  keyUserEntry.append("<p>" + keyUser["fullName"] + " (" + keyUser["screenName"] + ")</p>");
				  keyUserEntry.append("<div class=\"clearfix\"></div>");
			  });
			  topicWrapperDiv.append("<p class=\"koblenzTopicKeyPostHeader\">Key Posts:</p>");
			  $.each(topic["keyPosts"], function(keypostCounter, keyPost){
				  var keyPostEntry = $("<div class=\"koblenzPostWrapper\"></div>").appendTo(topicWrapperDiv);

//				  var parsedDate = Date.parse(keyPost["createdAt"]);
				  createdAt = "" + keyPost["createdAt"].replace("T", " ");
//				  createdAt = "" + parsedDate.toString('dddd, MMMM d, yyyy');
//								  topic["keyPostScores"].get(keypostCounter) +
				  keyPostEntry.append("<p><i>" + createdAt.substring(0, createdAt.length - 5) + " | Score " + keyPost["score"] + "</i><br>" + keyPost["text"] + "</p>");
				  keyPostEntry.append("<div class=\"clearfix\"></div>");
			  });



//			topicContentsDiv = $('<div class="postContentsWrapper"></div>').appendTo(topicWrapperDiv);
//
//			topicContentsDiv.append('<p class="postContents">' + index + ". " + topic.keywords + '</p>');

//			topicContentsDiv.append('<p class="dateCreated">' + createdAt.substring(0, createdAt.length - 5).replace("T", " ") + '</p>');
//			topicWrapperDiv.append("<div class=\"clearfix\"></div>");

		});
	} else {
		$("#resultsPanel").append("<p>Nothing was found.</p>");
	}
}

var maxResultsToDisplay = 100; // TODO: should be configurable

function showResultsWithId(id, widgetData) {
	console.log('showResultsWithId: widgetData:');
  console.log(widgetData);
	$("#otherPanel").empty();
	$("#resultsPanel").empty();

	var displayWidget = null;

	//KEM widgetData may be single object or array
	if ($.isArray(widgetData)) {
		$.each(widgetData, function(index, el){
			if (el["id"] == id) {
				displayWidget = el;
				return false;
			}
		});
	}
	else {
		displayWidget = widgetData;
	}

	if (displayWidget != null) {
		console.log(displayWidget["type"]);
		if ( (displayWidget["type"] == "posts-twitter") || (displayWidget["type"] == "search")) {
			console.log(displayWidget);
			displayTwitterPosts(displayWidget);
		}
		else if (displayWidget["type"] == "posts-facebook") {
			console.log(displayWidget);
			displayFacebookPosts(displayWidget);
		}
		else if (displayWidget["type"] == "comments-facebook") {
			console.log(displayWidget);
			displayFacebookComments(displayWidget);
		}
		else if (displayWidget["type"] == "topics-facebook") {
			console.log(displayWidget);
			displayFacebookTopics(displayWidget);
		}
		else {
			$("#resultsPanel").append("<p>Unknown data type.</p>");
			console.log(displayWidget);
		}

	} else {
		$("#resultsPanel").append("<p>Result was found.</p>");
	}
}
