// Parameters for clicks
var DELAY = 700,
	clicks = 0,
	timer = null;

// used in sorting results
var sortCriteria;
var sortDirection = "desc";
var topicsCollapsed = false;

// used to set num topics for analysis
var numTopicsWanted;


$(document).ready(function(){

	// iframe object for downloadz
	var iframe = document.createElement('iframe');
    iframe.id = "hiddenDownloader";
    iframe.style.visibility = 'hidden';
    document.body.appendChild(iframe);

    //$("#header").click(function(){
    //	window.location.href = "/";
    //});

	// Settings
	$("#username").click(function(e){
		var userdetails = jQuery.data($(this)[0], "userdetails");
//		console.log(userdetails);
		var userSettingsPopup = $("<div id=\"userSettings\" class=\"closed-by-escape\"></div>").appendTo("#wrapper");
		userSettingsPopup.append("<h2>Edit my settings</h2>");

		userSettingsPopup.append("<h3>My Details</h3>");

		var mySettingsWrapper = $("<div class=\"userSettingsWrapper\"></div>").appendTo(userSettingsPopup);
		mySettingsWrapper.append("<p class=\"myDetailLabel\">Username:</p>");
		mySettingsWrapper.append("<input type=\"text\" value=\"" + userdetails["username"] + "\" class=\"myDetailInput\" disabled=\"disabled\">");
		mySettingsWrapper.append("<p class=\"myDetailLabel\">Full name:</p>");
		mySettingsWrapper.append("<input type=\"text\" value=\"" + userdetails["name"] + "\" class=\"myDetailInput\">");
		mySettingsWrapper.append("<p class=\"myDetailLabel\">Organisation:</p>");
		mySettingsWrapper.append("<input type=\"text\" value=\"" + userdetails["organisation"] + "\" class=\"myDetailInput\">");
		mySettingsWrapper.append("<div class=\"clearfix\"></div>");

		userSettingsPopup.append("<div class=\"clearfix\"></div>");
		var mySettingsControlsWrapper = $("<div class=\"userSettingsControlsWrapper\"></div>").appendTo(userSettingsPopup);
		var mySettingsControlsCancel = $("<p id=\"userSettingsCancelButton\"><span lang=\"en\">Cancel</span></p>").appendTo(mySettingsControlsWrapper);
		var mySettingsControlsSave = $("<p id=\"userSettingsSaveButton\"><span lang=\"en\">Save my settings</span></p>").appendTo(mySettingsControlsWrapper);

		mySettingsControlsCancel.mouseup(function(e){
			$('.closed-by-escape').hide();
		});

		mySettingsControlsSave.mouseup(function(e){
			console.log("WARNING: save settings not implemented");
			//TODO implement this!
			alert('Sorry, save option not yet implemented');
			$('.closed-by-escape').hide();
		});

		popupBackground(userSettingsPopup.attr('id'));
	});

	//createTestTopics();	// for development of topics
	$.getJSON("/home/headsup/data/do.json", {}, updateTopics);

	function forumsSelectionChanged() {
		var numAllCheckedCheckboxesInForums = $("div.topicsDiv").find(":checked").size();

		if (numAllCheckedCheckboxesInForums == 0)
			$("#forumsTitle span").text("");
		else if (numAllCheckedCheckboxesInForums < 2)
			$("#forumsTitle span").text(" 1 thread selected");
		else
			$("#forumsTitle span").text(" " + numAllCheckedCheckboxesInForums + " threads selected");

	}

	function updateTopics(data) {
		var forumIds = null;
		var forumNames = null;
		var topics = null;
		var topicIdsOnly = null;

//		console.log("Response from data server resource: " + data["forumdata"]);
		forumIds = data["forumIds"];
		forumNames = data["forumNames"];
		numTopicsAndPostsInForum = data["numthreadsAndPostsInForum"];
		topics = data["threads"];
		topicsstats = data["threadsStats"];
		topicIdsOnly = data["threadIdsOnly"];

		if (forumIds == null) {
			alert("Failed to get data");

		} else {
//			console.log("Done getting data from server");
			$('#forums').empty();

			var selectionControlBar = $("<div class=\"selectionControlBar\" id=\"forumsAndTopicsControlBar\"></div>").appendTo('#forums');
			var listOfControls = $("<ul></ul>").appendTo(selectionControlBar);

			var showOrHideTopicsControl = $("<li>Hide<br>THREADS</li>").appendTo(listOfControls);
			showOrHideTopicsControl.click(function(event) {
				event.preventDefault();
				var text = $(this).html();
				if (text == "Hide<br>THREADS") {
					$(this).html("Show<br>THREADS");
					// Hide threads
					$("div.topicsDiv").hide();
					$("div.singleForumThreadsDisplayControl").removeClass('singleForumThreadsDisplayControlShowThreads').addClass('singleForumThreadsDisplayControlHideThreads');
				} else {
					$(this).html("Hide<br>THREADS");
					// Show threads
					$("div.topicsDiv").show();
					$("div.singleForumThreadsDisplayControl").removeClass('singleForumThreadsDisplayControlHideThreads').addClass('singleForumThreadsDisplayControlShowThreads');
				}

			});
			var showOrHideStatsControl = $("<li>Hide<br>STATS</li>").appendTo(listOfControls);
			showOrHideStatsControl.click(function(event) {
				event.preventDefault();
//				$(".stats").toggle();
				var text = $(this).html();
//				$(this).text(text == "Show stats" ? "Hide stats" : "Show stats");
				if (text == "Hide<br>STATS") {
					$(this).html("Show<br>STATS");
					// Hide stats
					$(".stats").hide();
				} else {
					$(this).html("Hide<br>STATS");
					// Show stats
					$(".stats").show();
				}
			});
			var clearSelectionControl = $("<li>Clear<br>SELECTION</li>").appendTo(listOfControls);
			clearSelectionControl.click(function(event) {
				event.preventDefault();
				$("#mainForm").find(":checked").attr('checked', false);
				forumsSelectionChanged();
			});
			var selectAllControl = $("<li>Select<br>ALL THREADS</li>").appendTo(listOfControls);
			selectAllControl.click(function(event) {
				event.preventDefault();
				$("#mainForm").find(":checkbox").attr('checked', true);
				forumsSelectionChanged();
			});

			// Search link
			var searchControl = $("<li class=\"highlighted\"><br>SEARCH</li>").appendTo(listOfControls);
			searchControl.click(function(event) {
				event.preventDefault();
				var text = $(this).html();
				if (text == "<br>SEARCH") {
					$(this).html("Hide<br>SEARCH");
					// Show search
					$("#searchForm").show();
					$("#mainForm").find(":checked").attr('checked', false); // clear selection
					forumsSelectionChanged();
					$("#mainForm").hide();
					$('#resultsTitle').empty();
					$('#resultsControlBar').empty();
				} else {
					$(this).html("<br>SEARCH");
					// Hide search
					$("#searchForm").hide();
					$("#mainForm").show();
					clearResults();
				}
			});

			var showOrHideInputThreadsControl = $('<li id="showOrHideInputThreadsControl" style="display:none">Only show<br>THREADS USED</li>').appendTo(listOfControls);
			showOrHideInputThreadsControl.click(function(event) {
				event.preventDefault();

				// Hide/show forums with nothing selected:
				$("#forums div.forumDiv").each(function() {
	//							var numberInputs = $(this).find("input").size();
					var numberInputsChecked = $(this).find("input:checked").size();
	//							console.log("Inputs: " + numberInputs + ", checked: " + numberInputsChecked + ", they match: " + (numberInputs == numberInputsChecked));
					if (numberInputsChecked < 1) {
						$(this).toggle();
					} else {
						$(this).find("div.topicsDiv div input:not(:checked)").parent().toggle();
					}
				});

				// Hide/show threads with nothing in them
				var text = $(this).html();
				$(this).html(text == "Only show<br>THREADS USED" ? "Show<br>ALL THREADS" : "Only show<br>THREADS USED");
			});

			// Search form
			//var searchForm = $("<form id=\"searchForm\" action=\"\">").appendTo('#forums');
			var searchForm = $('<div id="searchForm" action="" style="display:none">').appendTo('#forums');

			searchForm.append('<div id="searchParameters">');
			searchForm.append(	'<label for="searchTerms" class="searchForLabel"><span lang="en">Search for: </span></label><input type="text" id="searchTerms" name="" value="" />');
			searchForm.append(	'<span lang="en" id="searchButton">Go</span>');
			searchForm.append('</div>');

			// Search input
			$("#searchTerms").bind('keypress', function(e){
		//		e.preventDefault();
				if (e.keyCode == 13) {
					$("#searchButton").trigger('mousedown');
					$("#searchButton").trigger('mouseup');
				}
			});

			$("#searchButton").mouseup(function(event) {
				var searchTerms = $("#searchTerms").val();

				if (searchTerms.length < 1) {
					alert("Please enter keywords to search for");
					return false;
				}

				search(searchTerms);
			});

			clearResults();

			var mainForm = $("<form id=\"mainForm\" action=\"\">").appendTo('#forums');
//			mainForm.append("<input id=\"submitForAnalysis\" type=\"submit\" value=\"Analyse selected topics\" />");
			var i;
			for (i = 0; i < forumIds.length; ++i) {

				var forumDiv = $("<div id=\"forumDiv" + forumIds[i] + "\" class=\"forumDiv\"></div>").appendTo(mainForm);

				var forumCheckboxName = "checkForum" + forumIds[i];
				var forumCheckboxAndLabelDiv = $("<div class=\"forumCheckboxAndLabelDiv\" id=\"" + "checkForumDiv" + forumIds[i] + "\"></div>").appendTo(forumDiv);
				forumCheckboxAndLabelDiv.append("<input type=\"checkbox\" id=\"" + forumCheckboxName + "\" name=\"togglebox\" value=\"0\"/>");
				forumCheckboxAndLabelDiv.append("<label for=\"" + forumCheckboxName + "\">" + forumNames[i] + "<br/><span class=\"stats\">" + numTopicsAndPostsInForum[i] + "</span></label>");

				// Hover controls
				var forumHoverControl = $("<div class=\"singleForumThreadsDisplayControlShowThreads singleForumThreadsDisplayControl\"></div>").appendTo(forumDiv);
//				forumCheckboxAndLabelDiv.hoverIntent(showForumHoverControls, hideForumHoverControls);
				forumHoverControl.click(function() {
					if ($(this).hasClass("singleForumThreadsDisplayControlShowThreads")) {
						// Hide
						$(this).removeClass("singleForumThreadsDisplayControlShowThreads").addClass("singleForumThreadsDisplayControlHideThreads");
						$(this).parent().find("div.topicsDiv").hide();
					} else {
						// Show
						$(this).removeClass("singleForumThreadsDisplayControlHideThreads").addClass("singleForumThreadsDisplayControlShowThreads");
						$(this).parent().find("div.topicsDiv").show();
					}
//					console.log("Hover click");
//					$(this).parent().find("div.topicsDiv").toggle();
//					var myClass = $(this).attr('class');
//					$(this).attr('class', myClass == "singleForumThreadsDisplayControlShowThreads singleForumThreadsDisplayControl" ? "singleForumThreadsDisplayControlHideThreads singleForumThreadsDisplayControl" : "singleForumThreadsDisplayControlShowThreads singleForumThreadsDisplayControl");
				});

				var topicsDiv = $("<div id=\"topicsDiv" + forumIds[i] + "\" class=\"topicsDiv\"></div>").appendTo(forumDiv);

				$('#' + forumCheckboxName).click(function(event) {
					//event.preventDefault();
					$(this).parent().parent().find('div.topicsDiv :checkbox').attr('checked', this.checked);
					forumsSelectionChanged();
				});

				var j;
				for (j = 0; j < topics[forumIds[i]].length; ++j) {
					var listOfTopics = $("<div class=\"checkTopicDiv\"></div>").appendTo(topicsDiv);
					var topicCheckboxName = "checkTopic" + forumIds[i] + "_" + j;

					var topicCheckbox = $("<input type=\"checkbox\" id=\"" + topicCheckboxName + "\" name=\"" + topicIdsOnly[forumIds[i]][j] + "\" value=\"\">").appendTo(listOfTopics);
					listOfTopics.append("<label for=\"" + topicCheckboxName + "\">" + topics[forumIds[i]][j] + "<span class=\"stats\">" + topicsstats[forumIds[i]][j] + "</span>" + "</label>");

					topicCheckbox.click(function() {
//						!this.attr('checked')
						var numAllCheckboxesInTopics = $(this).parent().parent().find(':checkbox').size();
						var numAllCheckedCheckboxesInTopics = $(this).parent().parent().find(':checked').size();

//						console.log("All checkboxes: " + allCheckboxesInTopicsDiv.size());
//						console.log("Just checked: " + allCheckedCheckboxesInTopicsDiv.size());

						if (numAllCheckboxesInTopics != numAllCheckedCheckboxesInTopics)
							$(this).parent().parent().parent().find('input:first').attr('checked', false);
						else
							$(this).parent().parent().parent().find('input:first').attr('checked', true);

						forumsSelectionChanged();
					});

//					listOfTopics.append("<li>" + topics[forumIds[i]][j] + "</li>");
				}

			}

		}

	}

	function clearResults() {
		$("#showOrHideInputThreadsControl").hide();

		// Show initial analyser options
		$('#controls').empty();
    $('#results').empty();
		$('#resultsTitle').html('Analysis Options:');

		var resultsControlBar = $("<div class=\"selectionControlBar\" id=\"resultsControlBar\"></div>").appendTo('#controls');
		var listOfControls = $("<ul></ul>").appendTo(resultsControlBar);

		var viewPostsControl = $("<li class=\"highlighted\">VIEW<br>POSTS</li>").appendTo(listOfControls);
		viewPostsControl.click(function(event) {
			event.preventDefault();
			if (threadsSelected()) {
				$('#resultsTitle').html('Posts:<span></span>');
				viewPosts();
			}
			else {
				alert("Please select one or more forum threads to view");
			}
		});

    var numTopicsInput;

		var submitFormControl = $("<li class=\"highlighted\">RUN<br>ANALYSIS</li>").appendTo(listOfControls);
		submitFormControl.click(function(event) {
			event.preventDefault();
      numTopicsWanted = numTopicsInput.attr('value');

			if (threadsSelected()) {
				$('#resultsTitle').html('Topic analysis results:<span></span>');
				topicAnalysis();
			}
			else {
				alert("Please select one or more forum threads before running analysis");
			}
		});

    $("<label for=\"numTopicsInput\" class=\"boxLabelRight\">Number of Topics: </label>").appendTo(listOfControls);
     numTopicsInput =
       $("<input type=\"text\" value=-1 id=\"numTopicsInput\" size=\"5\" >").appendTo(listOfControls);

    $("<label for=\"numTopicsInput\" class=\"boxLabelLeft\"> (-1 for auto)</label>").appendTo(listOfControls);

    $("<div class=\"clearfix\"></div>").appendTo(listOfControls);


    $("<hr />").appendTo('#controls')
    //$("<div class=\"clearfix\"></div>").appendTo('#controls');
   // load previous results

		var loadPreviousResultsControlBar = $("<div class=\"selectionControlBar\" id=\"loadPreviousResultsControlBar\"></div>").appendTo('#controls');
		var loadPreviousResultsListOfControls = $("<ul></ul>").appendTo(loadPreviousResultsControlBar);

    var runIdInput;

		var loadPreviousResultsControl = $("<li class=\"highlighted\">Load Previous Results</li>").appendTo(loadPreviousResultsListOfControls);
		loadPreviousResultsControl.click(function(event) {
			event.preventDefault();
      var runId = runIdInput.attr('value');


      $('#results').append("<p class=\"waitMessage\">Getting data from server, please wait...</p>");

      $.getJSON("/home/headsup/getPreviousAnalysisResults/do.json", {runId: runId}, function(analysisData) {
        console.log(analysisData);
        $("#resultsTitle span").text(" " + analysisData["summary"]);
        $('#results').empty();
        displayTopicAnalysisControlBar(analysisData);
        displayTopics(analysisData, "averageTopicSentiment", "desc");
      });

/*
			if (threadsSelected()) {
				$('#resultsTitle').html('Topic analysis results:<span></span>');
				topicAnalysis();
			}
			else {
				alert("Please select one or more forum threads before running analysis");
			}
*/
		});

   $("<label for=\"runIdInput\" class=\"boxLabelRight\">Previous Run ID: </label>").appendTo(loadPreviousResultsListOfControls);
    runIdInput =
      //$("<input type=\"text\" value=-1 id=\"runIdInput\" size=\"5\" >").appendTo(loadPreviousResultsListOfControls);
      $("<input type=\"text\" id=\"runIdInput\" size=\"5\" >").appendTo(loadPreviousResultsListOfControls);

   $("<div class=\"clearfix\"></div>").appendTo(loadPreviousResultsListOfControls);

   $("<hr />").appendTo('#controls')

/*
    var numTopics = $("INPUT.spinbox").spinbox({
      min:-1,    // Set lower limit or null for no limit.
      max: null,  // Set upper limit or null for no limit.
      step: 1 // Set increment size.
    }).appendTo(listOfControls);
 */

	}

	function threadsSelected() {
		return (getSelectedThreads().length > 0);
	}

	function getSelectedThreads() {
		return $('.topicsDiv input:checked');
	}

	function getSelectedThreadIDs() {
		console.log("Threads to analyse:");
		var topicsToAnalyseArray = new Array();

		$('.topicsDiv input:checked').each(function() {
			console.log($(this).attr('name'));
			topicsToAnalyseArray.push($(this).attr('name'));
		});

		var selectedIdsStr = topicsToAnalyseArray.join(",");
		console.log(topicsToAnalyseArray);
		console.log(selectedIdsStr);

		return selectedIdsStr;
	}

	function viewPosts() {
		$('#results').empty();

		var numSelectedThreads = getSelectedThreads().length;

		if (numSelectedThreads < 2)
			$("#resultsTitle span").text(" viewing 1 thread");
		else
			$("#resultsTitle span").text(" viewing " + numSelectedThreads + " threads");

		$('#results').append("<p class=\"waitMessage\">Loading posts, please wait...</p>");

		var selectedIdsStr = getSelectedThreadIDs();

		$.getJSON("/home/headsup/viewposts/do.json", {input: selectedIdsStr}, function(postsData) {
			console.log(postsData);
			$('#results').empty();
			displayPostsControlBar();
			displayPosts(postsData);
		});

		return false;
	}

	function displayPostsControlBar() {
		$("#showOrHideInputThreadsControl").hide();

    $('#controls').empty();

		var resultsControlBar = $("<div class=\"selectionControlBar\" id=\"resultsControlBar\"></div>").appendTo('#controls');
		var listOfControls = $("<ul></ul>").appendTo(resultsControlBar);

		var expandCollapsePostsControl = $("<li>Collapse<br>POSTS</li>").appendTo(listOfControls);
		expandCollapsePostsControl.click(function(event) {
			event.preventDefault();
			var text = $(this).html();
			if (text == "Collapse<br>POSTS") {
				$(this).html("Expand<br>POSTS");
				// Collapse
				$("div.topicContents").hide();
				$("div.topicContentsDisplayControl").removeClass('topicContentsDisplayControlShowContent').addClass('topicContentsDisplayControlHideContent');
			} else {
				$(this).html("Collapse<br>POSTS");
				// Expand
				$("div.topicContents").show();
				$("div.topicContentsDisplayControl").removeClass('topicContentsDisplayControlHideContent').addClass('topicContentsDisplayControlShowContent');
			}
		});

		var submitFormControl = $("<li class=\"highlighted\">RUN<br>ANALYSIS</li>").appendTo(listOfControls);
		submitFormControl.click(function(event) {
			event.preventDefault();
			topicAnalysisForPosts();
		});

		var clearResultsControl = $("<li>CLEAR<br>RESULTS</li>").appendTo(listOfControls);
		clearResultsControl.click(function(event) {
			event.preventDefault();
			clearResults();
		});
	}

	function displayPosts(postsData) {
		var postsListDiv = $('<div id="postsList"></div>').appendTo('#results');

		$.each(postsData["posts"], function(i, post) {
			var postDiv = $("<div id=post" + i + " class=\"koblenzTopicDiv ui-state-default\"></div>").appendTo(postsListDiv);
			var postId = $('<input type="hidden" name="postID" value="' + post["id"] + '"></input>').appendTo(postDiv);
			var postSubjectDiv = $('<div class="koblenzTopicKeytermsDiv"></div>').appendTo(postDiv);
			postSubjectDiv.append("<h3>" + post["subject"] + "</h3>");

			// Hover controls
			var topicHoverControl = $("<div class=\"topicContentsDisplayControlShowContent topicContentsDisplayControl\"></div>").appendTo(postDiv);
			topicHoverControl.click(function() {
				if ($(this).hasClass("topicContentsDisplayControlShowContent")) {
					// Hide
					$(this).removeClass("topicContentsDisplayControlShowContent").addClass("topicContentsDisplayControlHideContent");
					$(this).parent().find("div.topicContents").hide();
				} else {
					// Show
					$(this).removeClass("topicContentsDisplayControlHideContent").addClass("topicContentsDisplayControlShowContent");
					$(this).parent().find("div.topicContents").show();
				}
			});

			var postContentsDiv = $("<div class=\"topicContents\"></div>").appendTo(postDiv);
			postContentsDiv.append("<p class=\"koblenzRelevantDocTimeAndUser\">at " + post["date"] + " by " + post["user"] + "</p>");
			postContentsDiv.append("<p class=\"koblenzRelevantDocTimeAndUser\">in " + post["thread"] + "</p>");
			postContentsDiv.append("<p class=\"koblenzRelevantDocMessageAndCount\">" + post["message"] + "</p>");

		});

		$('#postsList').sortable({ placeholder: 'ui-state-highlight', forcePlaceholderSize: true });
	}

	function getSelectedPosts() {
		console.log("Posts to analyse:");
		var postsToAnalyseArray = new Array();

		$('#postsList input[name=postID]').each(function() {
			console.log($(this).attr('value'));
			postsToAnalyseArray.push($(this).attr('value'));
		});

		return postsToAnalyseArray;
	}

	function topicAnalysisForPosts() {
		var selectedPosts = getSelectedPosts();
		topicAnalysis(selectedPosts);
	}

	function topicAnalysis(selectedPosts) {
		$('#results').empty();
		var selectedIdsStr;
		var type;

		if (selectedPosts) {
			type = "posts";
			console.log('Topic analysis for selected posts');
			console.log(selectedPosts);

			var numSelectedPosts = selectedPosts.length;

			if (numSelectedPosts < 2)
				$("#resultsTitle span").text(" analysing 1 post");
			else
				$("#resultsTitle span").text(" analysing " + numSelectedPosts + " posts");

			selectedIdsStr = selectedPosts.join(",");
		}
		else {
			type = "threads";
			console.log('Topic analysis for selected threads');
			var numSelectedThreads = getSelectedThreads().length;

			if (numSelectedThreads < 2)
				$("#resultsTitle span").text(" analysing 1 thread");
			else
				$("#resultsTitle span").text(" analysing " + numSelectedThreads + " threads");

			selectedIdsStr = getSelectedThreadIDs();
		}



		console.log(selectedIdsStr);

		$('#results').append("<p class=\"waitMessage\">Analysis is running, please wait...</p>");

		$.getJSON("/home/headsup/koblenzanalysis/do.json", {type: type, input: selectedIdsStr, numTopicsWanted: numTopicsWanted}, function(analysisData) {
			console.log(analysisData);
			$("#resultsTitle span").text(" " + analysisData["summary"]);
			$('#results').empty();
			displayTopicAnalysisControlBar(analysisData);
			displayTopics(analysisData, "averageTopicSentiment", "desc");
		});

		return false;
	}

	function displayTopicAnalysisControlBar(analysisData) {
		$("#showOrHideInputThreadsControl").show();

		$('#controls').empty();

		var resultsControlBar = $("<div class=\"selectionControlBar\" id=\"resultsControlBar\"></div>").appendTo('#controls');
		var listOfControls = $("<ul></ul>").appendTo(resultsControlBar);

		var expandCollapseTopicsControl = $("<li id=\"expandCollapseTopicsControl\">Expand/Collapse<br>TOPICS</li>").appendTo(listOfControls);
		expandCollapseTopicsControl.click(function(event) {
			event.preventDefault();
			var text = $(this).html();

			if (topicsCollapsed == false) {
				//$(this).html("Expand<br>TOPICS");
				// Collapse
				$("div.topicContents").hide();
				//$("div.topicContentsDisplayControl").removeClass('topicContentsDisplayControlShowContent').addClass('topicContentsDisplayControlHideContent');
        topicsCollapsed = true;

			} else {
				//$(this).html("Collapse<br>TOPICS");
				// Expand
				$("div.topicContents").show();
				//$("div.topicContentsDisplayControl").removeClass('topicContentsDisplayControlHideContent').addClass('topicContentsDisplayControlShowContent');
        topicsCollapsed = false;
			}

/*
			if (text == "Collapse<br>TOPICS") {
				$(this).html("Expand<br>TOPICS");
				// Collapse
				$("div.topicContents").hide();
				$("div.topicContentsDisplayControl").removeClass('topicContentsDisplayControlShowContent').addClass('topicContentsDisplayControlHideContent');
        topicsCollapsed = true;

			} else {
				$(this).html("Collapse<br>TOPICS");
				// Expand
				$("div.topicContents").show();
				$("div.topicContentsDisplayControl").removeClass('topicContentsDisplayControlHideContent').addClass('topicContentsDisplayControlShowContent');
        topicsCollapsed = false;
			}
      */
		});

		var showOrHideUsersControl = $("<li>Hide<br>KEY USERS</li>").appendTo(listOfControls);
		showOrHideUsersControl.click(function(event) {
			event.preventDefault();
			var text = $(this).html();
			if (text == "Hide<br>KEY USERS") {
				$(this).html("Show<br>KEY USERS");
				// Hide
				$("div.koblenzUsers").hide();
			} else {
				$(this).html("Hide<br>KEY USERS");
				// Show
				$("div.koblenzUsers").show();
			}
		});

		var showOrHidePostsControl = $("<li id=\"listpostscontrolab\">Hide<br>KEY POSTS</li>").appendTo(listOfControls);
		showOrHidePostsControl.click(function(event) {
			event.preventDefault();
			var text = $(this).html();
			if (text == "Hide<br>KEY POSTS") {
				$(this).html("Show<br>KEY POSTS");
				// Hide
				$("div.koblenzRelevantDocs").hide();
			} else {
				$(this).html("Hide<br>KEY POSTS");
				// Show
				$("div.koblenzRelevantDocs").show();
			}
		});

		var showOrHideMessagesControl = $("<li>Hide<br>MESSAGES</li>").appendTo(listOfControls);
		showOrHideMessagesControl.click(function(event) {
			event.preventDefault();
			var text = $(this).html();
			if (text == "Hide<br>MESSAGES") {
				$(this).html("Show<br>MESSAGES");
				// Hide
				$("p.koblenzRelevantDocMessageAndCount").hide();
			} else {
				$(this).html("Hide<br>MESSAGES");
				// Show
				$("p.koblenzRelevantDocMessageAndCount").show();
				$("div.koblenzRelevantDocs").show();
				$("#listpostscontrolab").html("Hide<br>KEY POSTS");
			}
		});
/*
		var exportResultsControl = $("<li class=\"highlighted\">DOWNLOAD<br>SPREADSHEET</li>").appendTo(listOfControls);
		exportResultsControl.click(function(event) {
			event.preventDefault();
			console.log(analysisData["filePath"]);
			iframe.src = "/wegov/download?filePath=" + analysisData["filePath"];
		});
*/
		var clearResultsControl = $("<li>CLEAR<br>RESULTS</li>").appendTo(listOfControls);
		clearResultsControl.click(function(event) {
			event.preventDefault();
			clearResults();
		});

    $("<hr />").appendTo('#controls')

    $("<div class=\"clearfix\"></div>").appendTo('#controls');


    var sortControlBar = $("<div class=\"selectionControlBar\" id=\"sortControlBar\"></div>").appendTo('#controls');
		var listOfSortControls = $("<ul></ul>").appendTo(sortControlBar);

    $("<br><li>Sort Topics By: </li>").appendTo(listOfSortControls);


  	var sortNumPostsControl = $("<li><b>NUM POSTS</b></li>").appendTo(listOfSortControls);
		sortNumPostsControl.click(function(event) {
			event.preventDefault();

      // toggle sort direction
      if (sortDirection == "desc") { sortDirection = "asc"; }
      else if (sortDirection == "asc") { sortDirection = "desc"; }

      sortCriteria = "numTopicPosts";
			//clearResults();
      displayTopics(analysisData, sortCriteria, sortDirection);
      $("div.topicContents").hide();
      topicsCollapsed = true;

		});

  	var sortSentimentControl = $("<li><b>SENTIMENT</b></li>").appendTo(listOfSortControls);
		sortSentimentControl.click(function(event) {
			event.preventDefault();

      // toggle sort direction
      if (sortDirection == "desc") { sortDirection = "asc"; }
      else if (sortDirection == "asc") { sortDirection = "desc"; }

      sortCriteria = "averageTopicSentiment";
			//clearResults();
      displayTopics(analysisData, sortCriteria, sortDirection);
      $("div.topicContents").hide();
      topicsCollapsed = true;

		});

  	var sortControversyControl = $("<li><b>CONTROVERSY</b></li>").appendTo(listOfSortControls);
		sortControversyControl.click(function(event) {
			event.preventDefault();

      // toggle sort direction
      if (sortDirection == "desc") { sortDirection = "asc"; }
      else if (sortDirection == "asc") { sortDirection = "desc"; }

      sortCriteria = "topicControversy";
			//clearResults();
      displayTopics(analysisData, sortCriteria, sortDirection);

      $("div.topicContents").hide();
      topicsCollapsed = true;

/*
      if (topicsCollapsed == false) {
       //$expandCollapseTopicsControl.trigger('click');
       $("#expandCollapseTopicsControl").trigger("click");
      }
      */
		});


    $("<hr />").appendTo('#controls')
	}

	function createTestTopics() {
		var topic1 = {'keyterms': 'Topic 1 keywords: cruelti, post, debat, primat, research', 'keyusers':[], 'relevantdocssubjects':[]};
		var topic2 = {'keyterms': 'Topic 2 keywords: pet, anim, farm, batteri, meat', 'keyusers':[], 'relevantdocssubjects':[]};
		var topic3 = {'keyterms': 'Topic 3 keywords: rabbit, anim, member, badger, welfar', 'keyusers':[], 'relevantdocssubjects':[]};
		var topic4 = {'keyterms': 'Topic 4 keywords: anim, test, fur, human, think', 'keyusers':[], 'relevantdocssubjects':[]};
		var topic5 = {'keyterms': 'Topic 5 keywords: fish, cow, final, kill, time', 'keyusers':[], 'relevantdocssubjects':[]};
		var topic6 = {'keyterms': 'Topic 6 keywords: anim, circus, think, wild, zoo', 'keyusers':[], 'relevantdocssubjects':[]};

		var data = {'result': [topic1, topic2, topic3, topic4, topic5, topic6]};

		displayTopicAnalysisControlBar(data);
		displayTopics(data);
	}

	function formatKeyterms(keytermsString) {
		var frags = keytermsString.split(":");
		var keywordsStr = $.trim(frags[1]);
		var prefix = frags[0];
		var keywords = keywordsStr.split(", ");
		var formattedTerms = "";

		$.each(keywords, function(i, keyword) {
			if (i != 0) formattedTerms += ", ";
			formattedTerms += "<span>" + keyword + "</span>";
		});

		//var html = '<h3 class="highlight">' + prefix + ': ' + formattedTerms + '</h3>';
    var html = '<h3 class="highlight">' + prefix + formattedTerms + '</h3>';
		//console.log(html);

		return html;
	}

	function displayTopics(analysisDataIn, sortCriteria, sortDirection) {

    $('#results').empty();

    var runId = analysisDataIn["runId"];

    var runIdDiv = $("<div id=runId class=\"koblenzTopicDiv ui-state-default\"></div>").appendTo('#results');
    runIdDiv.append("<td class=\"koblenzTopicDiv\">Run ID = <b>" + runId + "</b> - write this down if you want to retrieve these results!</td>");
			//listOfScores.append("<td class=\"scoresTableCellValue\">" + koblenzTopic["numTopicPosts"].toFixed(0) + "</td>");

    var topicsListDiv = $('<div id="topicsList"></div>').appendTo('#results');

    var analysisData = analysisDataIn["result"];

    // sort array by num posts, sentiment or controversy
    if (sortCriteria == "numTopicPosts"){
      if (sortDirection == "desc") {
        console.log ("sorting by " + sortCriteria + " in the " + sortDirection  + "direction");
        analysisData.sort(function(a, b) {
            return parseInt(b.numTopicPosts) - parseInt(a.numTopicPosts)
          });
       }
       else {
        console.log ("sorting by " + sortCriteria + " in the " + sortDirection  + "direction");
        analysisData.sort(function(a, b) {
            return parseInt(a.numTopicPosts) - parseInt(b.numTopicPosts)
          });
       }
    }
    else if (sortCriteria == "averageTopicSentiment"){
      if (sortDirection == "desc") {
        console.log ("sorting by " + sortCriteria + " in the " + sortDirection  + " direction");
        analysisData.sort(function(a, b) {
            return parseFloat(b.averageTopicSentiment) - parseFloat(a.averageTopicSentiment)
          });
       }
       else {
        console.log ("sorting by " + sortCriteria + " in the " + sortDirection  + " direction");
        analysisData.sort(function(a, b) {
            return parseFloat(a.averageTopicSentiment) - parseFloat(b.averageTopicSentiment)
          });
       }
    }
    else if (sortCriteria == "topicControversy"){
      if (sortDirection == "desc") {
        console.log ("sorting by " + sortCriteria + " in the " + sortDirection  + " direction");
        analysisData.sort(function(a, b) {
            return parseFloat(b.topicControversy) - parseFloat(a.topicControversy)
          });
       }
       else {
        console.log ("sorting by " + sortCriteria + " in the " + sortDirection  + " direction");
        analysisData.sort(function(a, b) {
            return parseFloat(a.topicControversy) - parseFloat(b.topicControversy)
          });
       }
    }


		//$.each(analysisData["result"], function(koblenzTopicId, koblenzTopic) {
    $.each(analysisData, function(koblenzTopicId, koblenzTopic) {

			var koblenzTopicDiv = $("<div id=koblenzTopic" + koblenzTopicId + " class=\"koblenzTopicDiv ui-state-default\"></div>").appendTo(topicsListDiv);
//						console.log(koblenzTopic);
			var koblenzTopicKeytermsDiv = $('<div class="koblenzTopicKeytermsDiv"></div>').appendTo(koblenzTopicDiv);
			//koblenzTopicDiv.append("<h3>" + koblenzTopic["keyterms"] + "</h3>");
			//koblenzTopicKeytermsDiv.append('<h3>' + koblenzTopic["keyterms"] + "</h3>");
			koblenzTopicKeytermsDiv.append(formatKeyterms(koblenzTopic["keyterms"]));


			var koblenzTopicScoresDiv = $(
      '<div class=\"scoresTable\"></div>').appendTo(koblenzTopicKeytermsDiv);


      var listOfScores = $(
      "<table id=\"scoresTable\"><tr>").appendTo(koblenzTopicScoresDiv);

      listOfScores.append("<td class=\"scoresTableCellText\">Num<br>Posts" + "</td>");
			listOfScores.append("<td class=\"scoresTableCellValue\">" + koblenzTopic["numTopicPosts"].toFixed(0) + "</td>");
			listOfScores.append("<td class=\"scoresTableCellText\">Average<br>Sentiment" + "</td>");
			listOfScores.append("<td class=\"scoresTableCellValue\">" + koblenzTopic["averageTopicSentiment"].toFixed(2) + "</td>");
      listOfScores.append("<td class=\"scoresTableCellText\">Controversy" + "</td>");
      listOfScores.append("<td class=\"scoresTableCellValue\">" + koblenzTopic["topicControversy"].toFixed(2) + "</td>");

      listOfScores.append("</tr></table>");


			// Hover controls
			var topicHoverControl = $("<div class=\"topicContentsDisplayControlShowContent topicContentsDisplayControl\"></div>").appendTo(koblenzTopicDiv);
			topicHoverControl.click(function() {
				if ($(this).hasClass("topicContentsDisplayControlShowContent")) {
					// Hide
					$(this).removeClass("topicContentsDisplayControlShowContent").addClass("topicContentsDisplayControlHideContent");
					$(this).parent().find("div.topicContents").hide();
				} else {
					// Show
					$(this).removeClass("topicContentsDisplayControlHideContent").addClass("topicContentsDisplayControlShowContent");
					$(this).parent().find("div.topicContents").show();
				}
			});



      var topicPosts = koblenzTopic["topicPostsArray"];

      // sort?

      var postSortCriteria = "relevance"; // can be either "relevance" or "sentiment"
      var postSortDirection = "desc"; // can be either "asc" or "desc"

      // sort control bar

    var postSortControlBar = $("<div class=\"selectionControlBar\" id=\"postSortControlBar\"></div>").appendTo(koblenzTopicKeytermsDiv);
		var listOfPostSortControls = $("<ul></ul>").appendTo(postSortControlBar);

    $("<li>Sort Posts By: </li>").appendTo(listOfPostSortControls);


  	var postSortRelevanceControl = $("<li><b>RELEVANCE</b></li>").appendTo(listOfPostSortControls);
		postSortRelevanceControl.click(function(event) {
			event.preventDefault();

      // toggle sort direction
      if (postSortDirection == "desc") { postSortDirection = "asc"; }
      else if (postSortDirection == "asc") { postSortDirection = "desc"; }

      postSortCriteria = "relevance";
      displayPostsFromAnalysis(topicPosts, koblenzRelevantDocsDiv, postSortCriteria, postSortDirection);

		});

  	var postSortSentimentControl = $("<li><b>SENTIMENT</b></li>").appendTo(listOfPostSortControls);
		postSortSentimentControl.click(function(event) {
			event.preventDefault();

      // toggle sort direction
      if (postSortDirection == "desc") { postSortDirection = "asc"; }
      else if (postSortDirection == "asc") { postSortDirection = "desc"; }

      postSortCriteria = "sentiment";
      displayPostsFromAnalysis(topicPosts, koblenzRelevantDocsDiv, postSortCriteria, postSortDirection);

		});


			var koblenzTopicContentsDiv = $("<div class=\"topicContents\"></div>").appendTo(koblenzTopicDiv);
			var koblenzUsersDiv = $("<div class=\"koblenzUsers\"></div>").appendTo(koblenzTopicContentsDiv);
			koblenzUsersDiv.append('<p class="koblenzKeyUsersLabel">Key Users:</p>');
			$.each(koblenzTopic["keyusers"], function(count, userName) {
				koblenzUsersDiv.append("<p class=\"koblenzUser\">" + userName + "</p>");
			});




      // where the docs are displayed

			var koblenzRelevantDocsDiv = $("<div class=\"koblenzRelevantDocs\"></div>").appendTo(koblenzTopicContentsDiv);
			koblenzRelevantDocsDiv.append('<p class="koblenzRelevantDocsDivLabel">Key Posts:</p>');
//			koblenzRelevantDocsDiv.append('<p class="koblenzKeyUsersLabel">Key Posts:</p>');

/*
      console.log ("sorting posts by " + postSortCriteria + " in the " + postSortDirection  + " direction");

      topicPosts.sort(function(a, b) {
            return parseFloat(b.topicScore) - parseFloat(a.topicScore)
      });
*/
      displayPostsFromAnalysis(topicPosts, koblenzRelevantDocsDiv, postSortCriteria, postSortDirection);
/*

      // display
 			$.each(topicPosts, function(count, post) {

				koblenzRelevantDocsDiv.append(
          "<p class=\"koblenzRelevantDocSubject\">"
            + "<b>" + post["docSubject"] + "</b>" + "</p>");

				koblenzRelevantDocsDiv.append(
          "<p class=\"koblenzRelevantDocTimeAndUser\">at "
            + post["datePublishedAsString"]
            + " by " + post["userName"]  + "(" + post["userName"] + ")" + "</p>");


				koblenzRelevantDocsDiv.append(
        "<p class=\"koblenzRelevantDocTimeAndUser\">in "
          + post["forumName"] + "</p>");

				koblenzRelevantDocsDiv.append(
        "<p class=\"koblenzRelevantDocMessageAndCount\">"
          + post["docMessage"]
          + "<br/> Score: " + post["topicScore"].toFixed(2)
          + "<br/> Sentiment: " + post["valence"].toFixed(2)
          + "</p>");
			});

*/

      /*
      // Original version using top 3
			$.each(koblenzTopic["relevantdocssubjects"], function(count, relevantDocSubject) {

				koblenzRelevantDocsDiv.append(
          "<p class=\"koblenzRelevantDocSubject\">"
            + relevantDocSubject + "</p>");

				koblenzRelevantDocsDiv.append(
          "<p class=\"koblenzRelevantDocTimeAndUser\">at "
            + koblenzTopic["relevantdocsdates"][count]
            + " by " + koblenzTopic["relevantdocsusers"][count] + "</p>");

				koblenzRelevantDocsDiv.append(
        "<p class=\"koblenzRelevantDocTimeAndUser\">in "
          + koblenzTopic["relevantdocscontext"][count] + "</p>");

				koblenzRelevantDocsDiv.append(
        "<p class=\"koblenzRelevantDocMessageAndCount\">"
          + koblenzTopic["relevantdocsmessages"][count]
          + "<br/> Score: " + koblenzTopic["relevantdocsscores"][count] + "</p>");
			});
      */

		});

		// Add hover event
		$("span").hover(function() {highlightKeywords($(this).text(), false, true);},
			function() {highlightKeywords($(this).text(), false, false);});

		// Add click event
		$("span").click(function() {
			highlightKeywords($(this).text(), true);
		});

		$('#topicsList').sortable({ placeholder: 'ui-state-highlight', forcePlaceholderSize: true });
	}

function displayPostsFromAnalysis (topicPosts, koblenzRelevantDocsDiv, postSortCriteria, postSortDirection) {

  koblenzRelevantDocsDiv.empty();
	koblenzRelevantDocsDiv.append('<p class="koblenzRelevantDocsDivLabel">Key Posts:</p>');


  // sort array
  // can be either "relevance" or "sentiment"
  if (postSortCriteria == "relevance") {
    if (postSortDirection == "desc") {
      console.log ("sorting posts by " + postSortCriteria + " in the " + postSortDirection  + " direction");
      topicPosts.sort(function(a, b) {
            return parseFloat(b.topicScore) - parseFloat(a.topicScore)
      });
    }
    else  {
      console.log ("sorting posts by " + postSortCriteria + " in the " + postSortDirection  + " direction");
      topicPosts.sort(function(a, b) {
            return parseFloat(a.topicScore) - parseFloat(b.topicScore)
      });
    }
  }
  else { // "sentiment"
    if (postSortDirection == "desc") {
      console.log ("sorting posts by " + postSortCriteria + " in the " + postSortDirection  + " direction");
      topicPosts.sort(function(a, b) {
            return parseFloat(b.valence) - parseFloat(a.valence)
      });
    }
    else  {
      console.log ("sorting posts by " + postSortCriteria + " in the " + postSortDirection  + " direction");
      topicPosts.sort(function(a, b) {
            return parseFloat(a.valence) - parseFloat(b.valence)
      });
    }

  }


  // display
  $.each(topicPosts, function(count, post) {

    koblenzRelevantDocsDiv.append("<hr />");
    koblenzRelevantDocsDiv.append(
      "<p class=\"koblenzRelevantDocSubject\">"
        + "<b>" + post["docSubject"] + "</b>" + "</p>");

    koblenzRelevantDocsDiv.append(
    "<p class=\"koblenzRelevantDocMessageAndCount\">"
      + "<br/> Relevance: " + "<b>" + post["topicScore"].toFixed(2) + "</b>"
      + "<br/> Sentiment: " + "<b>" + post["valence"].toFixed(2) + "</b>"
      + "</p>");


    koblenzRelevantDocsDiv.append(
      "<p class=\"koblenzRelevantDocTimeAndUser\">at "
        + post["datePublishedAsString"]
        + " by " + post["userName"]  + "(" + post["userName"] + ")" + "</p>");


    koblenzRelevantDocsDiv.append(
    "<p class=\"koblenzRelevantDocTimeAndUser\">in "
      + post["forumName"] + "</p>");

    koblenzRelevantDocsDiv.append(
    "<p class=\"koblenzRelevantDocMessageAndCount\">"
      + post["docMessage"]
      + "</p>");

  });
}


	var highlightedColor = '#0000B8';
	var selectedColor = '#0000FF';

	function highlightKeywords(keyword, set, highlight) {
		var keywordElements = $(".koblenzTopicKeytermsDiv span").each(function(){
			var keywordElement = $(this);
			var isSelected = keywordElement.data('selected');

			if (keywordElement.text() == keyword) {
				if (set) {
					if (isSelected) {
						keywordElement.data('selected', false); // deselect if already selected
						keywordElement.css('color', '');
					}
					else {
						keywordElement.data('selected', true); // select
						keywordElement.css('color', selectedColor);
					}
				}
				else {
					if (! isSelected) {
						if (highlight) {
							keywordElement.css('color', highlightedColor);
						}
						else {
							keywordElement.css('color', '');
						}
					}
				}
			}
			else {
				if (set) {
					keywordElement.data('selected', false);
					keywordElement.css('color', '');
				}
				else {
					if (! isSelected) {
						keywordElement.css('color', '');
					}
				}
			}
		});

	}

	function search(searchTerms) {
		//$.getJSON("/wegov/search", {q: searchTerms}, updateTopics);

		$('#resultsTitle').html('Posts Search Results:<span></span>');
		$('#results').empty();
		$('#results').append("<p class=\"waitMessage\">Searching posts, please wait...</p>");

		$.getJSON("/home/headsup/search/do.json", {query: searchTerms}, function(postsData) {
			console.log(postsData);
			$('#results').empty();
			displayPostsControlBar();
			updateTopics(postsData);
			$("#mainForm").find(":checkbox").attr('checked', true);
			forumsSelectionChanged();
			$('#resultsTitle').html('Posts Search Results:<span></span>');
			$('#results').empty();
			displayPostsControlBar();
			displayPosts(postsData);
		});

		return false;
	}

});