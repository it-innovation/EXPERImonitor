/*
function addGroupPostCommentsWidget(widget) {
		addGroupPostTopicAnalysisWidget(widget, "grouppostcomments");
	}
*/

/*
	function addLatestGroupPostTopicAnalysisWidget(widget) {
		addGroupPostTopicAnalysisWidget(widget, "latestgroupposttopicanalysis");
	}
*/
	function addGroupPostTopicAnalysisWidget(widget, mode) {
		var myMode = "groupposttopicanalysis"; //default mode

		if (mode) {
			myMode = mode;
		}

		widgetId = widget["id"];
		var widgetName = widget["name"];

		var myDiv = initWidget(widget);
		var groupPostsWidgetDiv = $('<div class="groupPostTopicAnalysisWidget"></div>').appendTo(myDiv);

		jQuery.data(groupPostsWidgetDiv[0], "widgetId", widgetId);

		var parameters = jQuery.parseJSON(widget["parametersAsString"]);
		//var searchTerms = parameters.term;
		var groupPostID = parameters.term;

		$('<input type="hidden" name="mode" value="' + myMode + '">').appendTo(groupPostsWidgetDiv);
		$('<input type="hidden" name="fbType" value="">').appendTo(groupPostsWidgetDiv);
		$('<input type="hidden" name="groupPostID" value="' + groupPostID + '">').appendTo(groupPostsWidgetDiv);
		$('<input type="hidden" name="widgetName" value="' + widgetName + '">').appendTo(groupPostsWidgetDiv);
		$('<input type="hidden" name="comments" value="undefined">').appendTo(groupPostsWidgetDiv);

		//$("<h2 class=\"widgetHeader\">" + widget["name"] + ": " + searchTerms + "</h2>").appendTo(myDiv);
//		var fbHeader = $('<h2 class="widgetHeader"></h2>');
		var fbHeader = $('<a target=\"_blank\" href="/home/results.html?w=' + widget["id"] + '" class="clickableWidgetHeader"></a>');
		setGroupPostInfoHeader(fbHeader, widgetName, groupPostID);
		fbHeader.appendTo(groupPostsWidgetDiv);

/*
		if (myMode == "latestgroupposttopicanalysis") {
			showWidgetSettingsWindowSJT(myDiv, widget, "Enter Facebook group or page ID, e.g. 24378370318 or AngelaMerkel:");
		}
		else {
			showWidgetSettingsWindowSJT(myDiv, widget, "Enter Facebook post ID, e.g. 59788447049_141536755968221");
		}
*/

    showWidgetSettingsWindowSJT(myDiv, widget);

		var myContainer = $("<div class=\"widgetContainer\"></div>").appendTo(groupPostsWidgetDiv);

		myContainer.append('<div class="groupPostsStatus"></div>');
		myContainer.append('<div class="groupPostsContainer"></div>');
//		myContainer.append('<div class="listOfTopicsDiv"></div>');

		var fbFooter = $("<p class=\"locationDecription\"></p>");
		setGroupPostInfoFooter(fbFooter, "entity", groupPostID);
		fbFooter.appendTo(groupPostsWidgetDiv);

		updateGroupPostsStatus('<p>Checking Facebook login status...</p>');

		// if fbLoginStatus is already defined, we can get group info and posts immediately
		// if fbLoginStatus is undefined, we must wait for fbLoginStatusCallback to be called (see fb-auth.js)
		if (fbLoginStatus) {
			//alert(fbLoginStatus);
			getGroupPostInfo(groupPostsWidgetDiv, true); //getGroupPostComments called by this
			//getGroupPostComments(groupPostsWidgetDiv);
		}

	}

	function setGroupPostInfoWidgetHeader(groupInfoWidget, name) {
		var groupPostID = getGroupPostID(groupInfoWidget);
		var widgetName = getWidgetName(groupInfoWidget);

		var groupName = groupPostID;

		if (name) {
			groupName = name;
		}

		var fbHeader = groupInfoWidget.find('.clickableWidgetHeader');
		setGroupPostInfoHeader(fbHeader, widgetName, groupPostID, groupName);
	}

	function setGroupPostInfoHeader(fbHeader, widgetName, groupPostID, groupName) {
		var displayName = groupPostID;

		if (groupName) {
			displayName = groupName;
		}

//		var text = widgetName + ': <a target="_blank" href="http://www.facebook.com/' + groupPostID + '">' + trimMessage(displayName, 40) + '</a>';
		var text = widgetName + ': ' + trimMessage(displayName, 30);

		fbHeader.html(text);
	}

	function setGroupPostInfoWidgetFooter(groupInfoWidget, type) {
		var groupPostID = getGroupPostID(groupInfoWidget);

		var fbFooter = groupInfoWidget.find('.locationDecription');
		setGroupPostInfoFooter(fbFooter, type, groupPostID);
	}

	function setGroupPostInfoFooter(fbFooter, type, groupPostID) {
		var displayType = "entity";

		if (type) {
			if (type == "link") {
				displayType = "post";
			}
			else {
				displayType = type;
			}
		}

		fbFooter.html("For Facebook " + displayType + " with ID:<br>" + groupPostID);
	}

	function getGroupPostInfoAllWidgets() {
		var groupPostInfoWidgets = $('.groupPostTopicAnalysisWidget');

		groupPostInfoWidgets.each(function(index) {
			getGroupPostInfo($(this));
		});
	}

	function getGroupPostInfo(groupPostInfoWidget, getCommentsNow) {
		var groupPostID = getGroupPostID(groupPostInfoWidget);

		if (! groupPostID) {
			var errorMessage = 'no groupPostID defined';
			updateGroupPostsStatus('<p>Error: ' + errorMessage + '</p>'); //TODO: fix this method (add widget to params)
			return;
		}

		var url = groupPostID + "&metadata=true";

		FB.api(url,
				function(response) {
//					console.log(response);
					setGroupPostInfo(groupPostInfoWidget, response);
					setGroupPostInfoWidgetHeader(groupPostInfoWidget, response.name);
					setGroupPostInfoWidgetFooter(groupPostInfoWidget, response.type);
					if (getCommentsNow) {
						getGroupPostComments(groupPostInfoWidget);
					}
				});
	}

	function getGroupPostID(widget) {
		return widget.find('input[name=groupPostID]').val();
	}

	function setGroupPostInfo(widget, response) {
		//alert('type: ' + response.type);
		console.log(response);
		widget.find('input[name=fbType]').val(response.type);

		var comments_count = 0;
		if (response.comments) {
			comments_count = response.comments.count;
			//alert('comments: ' + comments_count);
		}
		else {
			//alert('no comments');
		}

		widget.find('input[name=comments]').val(comments_count);
		//alert('set comments: ' + comments_count);
		//widget.find('.listOfTopicsDiv').append('<p>set comments: ' + comments_count + '</p>');
	}

	function getGroupPostCommentsAllWidgets() {
		var groupInfoWidgets = $('.groupPostTopicAnalysisWidget');

		groupInfoWidgets.each(function(index) {
			getGroupPostComments($(this));
		});
	}

	function getGroupPostComments(widget, groupPostIDparam) {
		updateGroupPostsStatus('');

		var groupPostID;

		if (groupPostIDparam) {
			groupPostID = groupPostIDparam;
		}
		else {
			groupPostID = getGroupPostID(widget);;
		}

		var fbType = widget.find('input[name=fbType]').val();

		if ((fbType != "link") && (fbType != "status")) {
			getLatestPost(widget, groupPostID);
			return;
		}

		if (! groupPostID) {
			var errorMessage = 'no groupPostID defined';
			updateGroupPostsStatus('<p>Error: ' + errorMessage + '</p>'); //TODO: fix this method (add widget to params)
			return;
		}

		var listOfTopicsDiv = widget.find('.widgetContainer');
		listOfTopicsDiv.empty();

		var comments_count = widget.find('input[name=comments]').val();
		//alert('read comments: ' + comments_count);
 		//widget.find('.listOfTopicsDiv').append('<p>read comments: ' + comments_count + '</p>');

		if (comments_count == 0) {
			listOfTopicsDiv.append("<p>No comments available.</p>");
			return;
		}

		var url = groupPostID + "/comments?limit=" + comments_count;

		listOfTopicsDiv.append("<p>Downloading " + comments_count + " comments...</p>");

		FB.api(url,
				function(response) {
					//fbLoginStatusChanged("POSTS_RECEIVED");
//					console.log(response);
					var mode = widget.find('input[name=mode]').val();
					if (mode == "grouppostcomments") {
						var fbHeader = widget.find('.clickableWidgetHeader');
						var text = fbHeader.text();
						fbHeader.text(text + " (found " + response.data.length + ")");
						var dataToSend = JSON.stringify({wId: jQuery.data(widget[0], "widgetId"), type: "comments-facebook", name: "Comments for post: " + groupPostID + ", found: " + response.data.length, location: "unknown",
							//data: response.data});
							data: {postData: {query: url, data: response.data}, userData: {}}
							});
						$.ajax({
							  type: 'POST',
							  url: "/home/widgets/saveWidgetData/do.json",
							  contentType: "application/json; charset=utf-8",
							  data: dataToSend
						});
						putGroupPostCommentsIntoContainer(widget, response.data);
					}
					else {
						topicAnalysis(response.data, listOfTopicsDiv, widget, groupPostID);
					}
				});
	}

	function getLatestPost(widget, groupID) {
		var listOfTopicsDiv = widget.find('.widgetContainer');
		listOfTopicsDiv.empty();
		listOfTopicsDiv.append('<p>Getting latest post for ' + groupID + '...</p>');

		var url = groupID + "/feed?limit=1";

		FB.api(url,
				function(response) {
					//fbLoginStatusChanged("POSTS_RECEIVED");
//					console.log(response);
					var post = response.data[0];
					setGroupPostInfo(widget, post);
					getGroupPostComments(widget, post.id);
				});
	}

	function putGroupPostCommentsIntoContainer(widget, posts) {
		widget.find('.widgetContainer').empty();
		var container = widget.find(".widgetContainer");
		container.empty();
		if (posts.length > 0) {
			//container.append("<p class=\"numResultsTitle\">First three, click on the link above to see all:</p>");
			$.each(posts, function(num, post) {
				var postEntry = $('<p class="widgetTweet"></p>').appendTo(container);

				if (post.picture)
					postEntry.append('<img src="' + post.picture + '">');
				else
					postEntry.append('<img src="https://graph.facebook.com/' + post.from.id + '/picture">');

				postEntry.append('<a target="_blank" href="http://www.facebook.com/profile.php?id=' + post.from.id + '">' + post.from.name + '</a><br>');

				if (post.message) {
					postEntry.append(trimMessage(post.message, 90));
					if (post.likes) {
						postEntry.append(" (" + post.likes + " likes)");
					}
				} else {
					if (post.name)
						postEntry.append(trimMessage(post.name, 90));

					if (post.source)
						postEntry.append(post.source);

					if (post.story)
						postEntry.append(post.story);
				}

				/*
				var message = contents.message;
				if (! message) message = "";
				var trimmedMessage = trimMessage(message);
				//trimmedMessage = addLinks(trimmedMessage);

				container.append('<p class="widgetTweet">'
//					+ '<a target="_blank" href="http://www.facebook.com/profile.php?id=' + contents.from.id + '">'
					+	'<img width="48" height="48" src="https://graph.facebook.com/' + contents.from.id + '/picture">'
//					+ '</a>' +
//					+ '<div class="groupPostMessage">'
//					+	'<span class="groupPostMessageTrimmed">' + trimmedMessage + '</span>'
					+	trimmedMessage
//					+	'<span class="groupPostMessageFull" style="display:none;">' + message + '</span>'
//					+	'</br>'
//					+ '</div>'
					+ ' by <a target="_blank" href="http://www.facebook.com/profile.php?id=' + contents.from.id + '">' + contents.from.name + '</a>'
					+ '</p>');
*/
				if (num > 1) {
					return false;
				}
			});

			if (addHover) {
				$(".groupPostMessage").hover(
					function() {
						$(this).find("span.groupPostMessageTrimmed").hide();
						$(this).find("span.groupPostMessageFull").show();
					},
					function() {
						$(this).find("span.groupPostMessageFull").hide();
						$(this).find("span.groupPostMessageTrimmed").show();
					}
				);
 			}

		} else {
			container.append("<p>Nothing was found. Try refreshing the data or specifying a different search term.</p>");
		}
	}

	function topicAnalysis(data, listOfTopicsDiv, widget, groupPostID) {
//				console.log(data);
		listOfTopicsDiv.append("<p>Running analysis...</p>");
		$.ajax({
			  type: 'POST',
			  url: "/home/analysis/fbkoblenz/do.json",
//								  data: JSON.stringify({postData: data, userData: userdata}),
			  data: JSON.stringify(data),
			  contentType: "application/json; charset=utf-8",
//								  data: {postData: data, userData: userdata},
			  error: function() {
				  listOfTopicsDiv.append("<p>Error getting analysis results. Try refreshing the data or specifying a different search term.?</p>");
			  },
			  success: function(result){
//				  console.log(result);
				  listOfTopicsDiv.empty();

				  if (result["topics"]) {
					  //listOfTopicsDiv.append("<p class=\"numResultsTitle\">First three, click on the link above to see all:</p>");
//							  listOfTopicsDiv.append("<p class=\"koblenzResultsHeader\">Found " + result["numTopicsAsString"] + ":</p>");
						var fbHeader = widget.find('.clickableWidgetHeader');
						var text = fbHeader.text();
						fbHeader.text(text + " (found " + result["numTopics"] + ")");

						var dataToSend = JSON.stringify({wId: jQuery.data(widget[0], "widgetId"), type: "topics-facebook", name: "Topics for post: " + groupPostID + ", found: " + result["numTopics"], location: "unknown", data: result});
						$.ajax({
							  type: 'POST',
							  url: "/home/widgets/saveWidgetData/do.json",
							  contentType: "application/json; charset=utf-8",
							  data: dataToSend
						});

//					  var topicsList = $("<ul class=\"topicsList\"></ul>").appendTo(listOfTopicsDiv);
					  $.each(result["topics"], function(counter, topic){

						  listOfTopicsDiv.append("<p class=\"koblenzTopicKeywords\">" + (counter + 1) + ". " + topic["keywords"] + "</p>");
						  var koblenzWidgettopicWrapper = $("<div class=\"koblenzWidgettopicWrapper\"></div>").appendTo(listOfTopicsDiv);
						  koblenzWidgettopicWrapper.append("<p class=\"koblenzKeyUsersLabel\">Key users: </p>");
						  $.each(topic["keyUsers"], function(keyUserCounter, keyUser){
							  if (keyUserCounter == topic["keyUsers"].length - 1)
								  koblenzWidgettopicWrapper.append("<a class=\"koblenzTopicuserProfileLink\" href=\"https://twitter.com/#!/" + keyUser["screenName"] + "\">" + keyUser["fullName"] + "</a>");
							  else
								  koblenzWidgettopicWrapper.append("<a class=\"koblenzTopicuserProfileLink\" href=\"https://twitter.com/#!/" + keyUser["screenName"] + "\">" + keyUser["fullName"] + "</a>, ");
						  });

							if (counter > 1) {
								return false;
							}

					  });
				  } else {
					  listOfTopicsDiv.append("<p>Nothing was found. Try refreshing the data or specifying a different search term.</p>");
				  }

			  }
		});
	}
