	function addGroupPostCommentsWidget(widget) {
		var myMode = "grouppostcomments"; //default mode

		var widgetId = widget["id"];
		var widgetName = widget["name"];

		var myDiv = initSearchWidget(widget);

    var groupPostsHeaderDiv = $("<div class=\"widgetHeaderDiv\"></div>").appendTo(myDiv);

    $("<img class=\"widgetLogo\" src=\"img/f_logo.png\" alt=\"Source: Facebook\"/>").appendTo(groupPostsHeaderDiv);
		jQuery.data(groupPostsHeaderDiv[0], "widgetId", widgetId);

		var parameters = jQuery.parseJSON(widget["parametersAsString"]);
		//var groupID = parameters.term;

    var groupPostID = parameters.term;
    console.log("Group Post ID = " + groupPostID);

    // determine whether we want to get new data from outside or get it from the DB
    var autoRefreshData = parameters.autoRefreshData;
    var autoRefreshDataFlag = false;
    if (autoRefreshData == "true") {
      autoRefreshDataFlag = true;
    }
    else {
      autoRefreshDataFlag = false;
    }

    var postName = null;
    var postMessage = null;
    if (parameters.postName != null) postName = parameters.postName;
    if (parameters.postMessage != null) postMessage = parameters.postMessage;


    var widgetHeaderTextDiv = $("<div class=\"widgetHeaderTextDiv\"></div>").appendTo(groupPostsHeaderDiv);

		//var fbHeader = $('<a target=\"_blank\" href="/home/results.html?w=' + widget["id"] + '" class="clickableWidgetHeader"></a>');

    var headerText = null;
    //var headerTextLink = '<a target="_blank" href="http://www.facebook.com/' + groupPostID + '">' + groupPostID + '</a>';
    if (postName != null) {
      //headerText = "\"" + postName + "\"" + " (ID = " + headerTextLink + ")";
      //headerText = "\"" + postName + "\"" + " (ID = " + groupPostID + ")";
      //headerText = "\"" + postName + "\"" + " (ID = " + groupPostID + ")";
      headerText = "\"" + postName + "\"";
    }
    else {
      headerText = groupPostID;
      //headerText = headerTextLink;
      //headerTextLink = '<a target="_blank" href="http://www.facebook.com/' + groupPostID + '">' + groupPostID + '</a>';
    }

    //("For Facebook " + displayType + ': <a target="_blank" href="http://www.facebook.com/' + groupPostID + '">' + groupPostID + '</a>');

    $("<h2 class=\"widgetHeader\">" + widget["name"]  + ": " + headerText + "</h2>").appendTo(groupPostsHeaderDiv);

		//setGroupPostsHeader(fbHeader, widgetName, headerText);
		//fbHeader.appendTo(widgetHeaderTextDiv);


    myDiv.append('<div class="clearfix"></div>');

    var detailsButton = $(
      "<a target=\"_blank\"  href=\"/home/results.html?w="
      + widget["id"] + "\" class=\"widgetDetails\">"
      + "Details" + "</a>").appendTo(myDiv);


    //if (postName != null) var extraTextPostName = $("<p>" + postName + "</p>").appendTo(fbHeader);
    //if (postMessage != null) var extraTextpostMessage = $("<p>" + postMessage + "</p>").appendTo(fbHeader);

/*
		var fbFooter = $("<p class=\"locationDecription\"></p>");
		setGroupPostsFooter(fbFooter, "entity", groupPostID);
		fbFooter.appendTo(groupPostsHeaderDiv);
*/
    //widgetHeaderTextDiv.append('<div class="clearfix"></div>');

    myDiv.append('<div class="clearfix"></div>');

    var contentColour = parameters.contentColour;
    if (contentColour == undefined || contentColour == 'undefined' ) {
      contentColour = "gray";
    }
    var myContainer = $("<div style=background-color:" + contentColour + "; class=\"widgetContent\"></div>").appendTo(myDiv);

		//$('<input type="hidden" name="groupID" value="' + groupID + '">').appendTo(myContainer);
		//$('<input type="hidden" name="widgetName" value="' + widgetName + '">').appendTo(myContainer);

		$('<input type="hidden" name="mode" value="' + myMode + '">').appendTo(myContainer);
		$('<input type="hidden" name="fbType" value="">').appendTo(myContainer);
		$('<input type="hidden" name="groupPostID" value="' + groupPostID + '">').appendTo(myContainer);
		$('<input type="hidden" name="widgetName" value="' + widgetName + '">').appendTo(myContainer);
		$('<input type="hidden" name="comments" value="undefined">').appendTo(myContainer);

    myContainer.append('<div class="groupPostCommentsStatus"></div>');
		myContainer.append('<div class="groupPostCommentsContainer"></div>');


    // either get new posts or get them from DB
    if (autoRefreshDataFlag) {

      console.log ("Refreshing posts for widget: " + widgetId);
      // refresh posts
      refreshFacebookCommentsWidget(widget, myContainer);

      // now unset auto refresh data flag in widget
      parameters.autoRefreshData = "false";
      $.get("/home/widgets/updateWidgetParameters/do.json",
        {wId: widgetId, newParametersValue: JSON.stringify(parameters)});

    }
    else {
      // get posts from DB
      var fbComments = getFBPostsFromDB_Synchronous(widgetId);

      //console.log(fbComments);
      putGroupPostCommentsIntoContainer(myContainer, fbComments);

    }

/*
    var fbComments = getFBPostsFromDB_Synchronous(widgetId);

    console.log(fbComments);

    putGroupPostCommentsIntoContainer(myContainer, fbComments);
*/
/*
		if (fbLoginStatus) {
			getGroupPostInfo(myContainer, true);
		}
*/


/*
    var fbPosts = getFBPostsFromDB_Synchronous(widgetId);
    //console.log(fbPosts);
    //console.log("widget follows");
    //console.log(widget);
    putGroupPostsIntoContainer(myContainer, fbPosts);
*/
    var widgetFooter = $("<div class=\"widgetFooter\"></div>").appendTo(myDiv);
    var settingsButton = $("<p class=\"widgetSettings\">Settings</p>").appendTo(widgetFooter);
    settingsButton.click(function(e){
      //showWidgetSettingsWindowSJT(myDiv, widget, "Enter Facebook post ID, e.g. 59788447049_141536755968221")
      showWidgetSettingsWindowSJT(myDiv, widget)
    });
    var refreshButton = $("<p class=\"widgetRefresh\">Refresh Data</p>").appendTo(widgetFooter);
    refreshButton.click(function(e){
      //refreshFacebookPostsWidget(widget, myContainer);
      refreshFacebookCommentsWidget (widget, myContainer);
    });
    widgetFooter.append('<div class="clearfix"></div>');


	}

	function refreshFacebookCommentsWidget (widget, myContainer) {
		//console.log("refreshFacebookPostsWidget: fbLoginStatus = " + fbLoginStatus);
		//if (fbLoginStatus != undefined) {
		//	//getGroupPostInfo(myContainer, true);
		//	getGroupPostComments(myContainer, widget);
		//	/*
		//	getGroupInfo(myContainer);
		//	getGroupPosts(myContainer, widget);
		//	*/
		//}

		var accessToken = getAccessToken();
		console.log("refreshFacebookCommentsWidget: accessToken = " + accessToken);

		if (accessToken == null) {
			console.log("Calling getAccessTokenThenExecCallback() with callback getGroupPostComments(" + myContainer + ", " + widget + ")");
			getAccessTokenThenExecCallback(getGroupPostComments, [myContainer, widget]);
		}
		else {
			getGroupPostComments(myContainer, widget);
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
					console.log(response);
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

	function getGroupPostComments(myDiv, widget, groupPostIDparam) {
		updateGroupPostsStatus('');

    var widgetId = widget["id"];
		console.log('getGroupPostComments for widget ' + widgetId);


		var groupPostID;

		var parameters = jQuery.parseJSON(widget["parametersAsString"]);

		if (groupPostIDparam) {
			groupPostID = groupPostIDparam;
		}
		else {
			groupPostID = parameters.term;
		}

    console.log(groupPostID);

		var fbType = myDiv.find('input[name=fbType]').val();
/*
		if ((fbType != "link") && (fbType != "status")) {
			getLatestPost(myDiv, groupPostID);
			return;
		}
*/
		if (! groupPostID) {
			var errorMessage = 'no groupPostID defined';
			updateGroupPostsStatus('<p>Error: ' + errorMessage + '</p>'); //TODO: fix this method (add myDiv to params)
			return;
		}

		//var listOfTopicsDiv = myDiv.find('.widgetContainer');
		//listOfTopicsDiv.empty();


    var status = myDiv.find(".groupPostCommentsStatus");
    status.empty();

    status.append("<p>Getting comments for Facebook post ID: " + groupPostID + "</p>");


		var container = myDiv.find(".groupPostCommentsContainer");
		container.empty();

/*
		var comments_count = myDiv.find('input[name=comments]').val();
		//alert('read comments: ' + comments_count);
 		//myDiv.find('.listOfTopicsDiv').append('<p>read comments: ' + comments_count + '</p>');

		if (comments_count == 0) {
			listOfTopicsDiv.append("<p>No comments available.</p>");
			return;
		}
*/
		//var url = groupPostID + "/comments?limit=" + comments_count;

    var url = groupPostID + "/comments";
    console.log(url);

		container.append("<p>Downloading comments from Facebook ...</p>");

		FB.api(url,
				function(response) {
        //fbLoginStatusChanged("POSTS_RECEIVED");
//					console.log(response);
          var fbHeader = myDiv.find('.clickableWidgetHeader');
          var text = fbHeader.text();
          fbHeader.text(text + " (found " + response.data.length + ")");

          var dataToSend = JSON.stringify({
            wId: widgetId,
            type: "comments-facebook",
            name: "Comments for post: " + groupPostID + ", found: " + response.data.length,
            location: "unknown",
			//data: response.data
			data: {postData: {query: url, data: response.data}, userData: {}}
          });

          $.ajax({
              type: 'POST',
              url: "/home/widgets/saveWidgetData/do.json",
              contentType: "application/json; charset=utf-8",
              data: dataToSend
          });
          putGroupPostCommentsIntoContainer(myDiv, response.data);
				});
	}

	function getLatestPost(myDiv, groupID) {
		var listOfTopicsDiv = myDiv.find('.widgetContainer');
		listOfTopicsDiv.empty();
		listOfTopicsDiv.append('<p>Getting latest post for ' + groupID + '...</p>');

		var url = groupID + "/feed?limit=1";

		FB.api(url,
				function(response) {
					//fbLoginStatusChanged("POSTS_RECEIVED");
//					console.log(response);
					var post = response.data[0];
					setGroupPostInfo(myDiv, post);
					getGroupPostComments(myDiv, post.id);
				});
	}

	function putGroupPostCommentsIntoContainer(myDiv, posts) {
    console.log(posts);
    //var container = myDiv;

    var status = myDiv.find(".groupPostCommentsStatus");
    status.empty();

		var container = myDiv.find(".groupPostCommentsContainer");
		container.empty();

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
				  listOfTopicsDiv.append("<p>Error getting analysis results. Try refreshing the data or specifying a different search term.</p>");
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
					  listOfTopicsDiv.append("<p>Nothing was found getting analysis results. Try refreshing the data or specifying a different search term.</p>");
				  }

			  }
		});
	}
