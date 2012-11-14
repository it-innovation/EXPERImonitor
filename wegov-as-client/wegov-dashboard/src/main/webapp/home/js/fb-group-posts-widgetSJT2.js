/////////////////////////////////////////////////////////////////////////////////////////////
// Group Posts Widget
var fbDebug = false;
var addHover = false;
var fbLoginStatus;

function addGroupPostsWidget(widget) {
    var widgetId = widget["id"];
    var widgetName = widget["name"];

    var myDiv = initSearchWidget(widget);

    //var groupPostsHeaderDiv = $("<div class=\"searchWidgetHeaderDiv\"></div>").appendTo(myDiv);
    var groupPostsHeaderDiv = $("<div class=\"widgetHeaderDiv\"></div>").appendTo(myDiv);

    $("<img class=\"widgetLogo\" src=\"img/f_logo.png\" alt=\"Source: Facebook\"/>").appendTo(groupPostsHeaderDiv);
    
    jQuery.data(groupPostsHeaderDiv[0], "widgetId", widgetId);

    var parameters = jQuery.parseJSON(widget["parametersAsString"]);
    var groupID = parameters.term;
    console.log("Group ID = " + groupID);

    // determine whether we want to get new data from outside or get it from the DB
    var autoRefreshData = parameters.autoRefreshData;
    var autoRefreshDataFlag = false;
    if (autoRefreshData == "true") {
        autoRefreshDataFlag = true;
    }
    else {
        autoRefreshDataFlag = false;
    }

    

    //var widgetHeaderTextDiv = $("<div class=\"widgetHeaderTextDiv\"></div>").appendTo(groupPostsHeaderDiv);
    /*
		var fbHeader = $('<a target=\"_blank\" href="/home/results.html?w=' + widget["id"] + '" class="clickableWidgetHeader"></a>');
		setGroupPostsHeader(fbHeader, widgetName, groupID);
		fbHeader.appendTo(widgetHeaderTextDiv);
 */

    $("<h2 class=\"widgetHeader\">" + widget["name"]  + ": " + groupID + "</h2>").appendTo(groupPostsHeaderDiv);
    //widgetHeaderDiv.append('<div class="clearfix"></div>');

    /*
		var fbFooter = $("<p class=\"locationDecription\"></p>");
		setGroupPostsFooter(fbFooter, "entity", groupID);
		fbFooter.appendTo(groupPostsHeaderDiv);
*/
    //widgetHeaderTextDiv.append('<div class="clearfix"></div>');
    //groupPostsHeaderDiv.append('<div class="clearfix"></div>');

    myDiv.append('<div class="clearfix"></div>');

    //var detailsButton = $('<a target=\"_blank\" href="/home/results.html?w=' + widget["id"] + '" class="clickableWidgetHeader">Details</a>');


    var detailsButton = $(
        "<a target=\"_blank\"  href=\"/home/results.html?w="
        + widget["id"] + "\" class=\"widgetDetails\">"
        + "Details" + "</a>").appendTo(myDiv);

    myDiv.append('<div class="clearfix"></div>');
    /*
 *

   var widgetHeaderDiv = $("<div class=\"widgetHeaderDiv\"></div>").appendTo(myDiv);

  $("<img class=\"widgetLogo\" src=\"img/twitter_newbird_boxed_whiteonblue.png\" alt=\"Source: Twitter\"/>").appendTo(widgetHeaderDiv);

  var widgetHeaderTextDiv = $("<div class=\"widgetHeaderTextDiv\"></div>").appendTo(widgetHeaderDiv);
  $("<h2 class=\"widgetHeader\">" + widget["name"]  + ": " + term + "</h2>").appendTo(widgetHeaderTextDiv);
  widgetHeaderDiv.append('<div class="clearfix"></div>');

  if (widgetType == "twitterLocal") {
    var widgetHeaderExtraTextDiv = $("<div class=\"widgetHeaderExtraTextDiv\"></div>").appendTo(myDiv);
    var locationDecription = $("<p class=\"locationDecription\"></p>")
    .appendTo(widgetHeaderExtraTextDiv);
    locationDecription.text("For location: " + geoplugin_city() + ", "
      + geoplugin_countryName());
  }

  var detailsButton = $(
    "<a target=\"_blank\"  href=\"/home/results.html?w="
    + widget["id"] + "\" class=\"widgetDetails\">"
    + "Details" + "</a>").appendTo(myDiv);

  myDiv.append('<div class="clearfix"></div>');



 */

    var contentColour = parameters.contentColour;
    if (contentColour == undefined || contentColour == 'undefined' ) {
        contentColour = "gray";
    }
    var myContainer = $("<div style=background-color:" + contentColour + "; class=\"widgetContent\"></div>").appendTo(myDiv);

    $('<input type="hidden" name="groupID" value="' + groupID + '">').appendTo(myContainer);
    $('<input type="hidden" name="widgetName" value="' + widgetName + '">').appendTo(myContainer);
    myContainer.append('<div class="groupPostsStatus"></div>');
    myContainer.append('<div class="groupPostsContainer"></div>');

    /*
    var fbPosts = getFBPostsFromDB_Synchronous(widgetId);
    putGroupPostsIntoContainer(myContainer, fbPosts);
*/

    // either get new posts or get them from DB
    if (autoRefreshDataFlag) {
        
        console.log("Getting posts from Facebook");
        
        // refresh posts
        refreshFacebookPostsWidget(widget, myContainer);

        // now unset auto refresh data flag in widget
        parameters.autoRefreshData = "false";
        
        $.get("/home/widgets/updateWidgetParameters/do.json", {
            wId: widgetId, 
            newParametersValue: JSON.stringify(parameters)            
        });

    } else {
        console.log("Getting posts from the database");
        
        // get posts from DB
        var fbPosts = getFBPostsFromDB_Synchronous(widgetId);
        putGroupPostsIntoContainer(myContainer, fbPosts);

    }


    var widgetFooter = $("<div class=\"widgetFooter\"></div>").appendTo(myDiv);
    var settingsButton = $("<p class=\"widgetSettings\">Settings</p>").appendTo(widgetFooter);
    settingsButton.click(function(e){
        //showWidgetSettingsWindowSJT(myDiv, widget, "Enter Facebook group or page ID, e.g. 24378370318 or AngelaMerkel:")
        showWidgetSettingsWindowSJT(myDiv, widget)
    });
    var refreshButton = $("<p class=\"widgetRefresh\">Refresh Data</p>").appendTo(widgetFooter);
    refreshButton.click(function(e){
        refreshFacebookPostsWidget(widget, myContainer);
    });
    widgetFooter.append('<div class="clearfix"></div>');



}
	
function refreshFacebookPostsWidget(widget, myContainer, groupID) {
    /* console.log("refreshFacebookPostsWidget: fbLoginStatus = " + fbLoginStatus);
        if (fbLoginStatus != undefined) {
                getGroupInfo(myContainer);
                getGroupPosts(myContainer, widget);
        }*/

    var accessToken = getAccessToken();
    console.log("refreshFacebookPostsWidget: accessToken = " + accessToken);

    if (accessToken == null) {
        console.log("Calling getAccessTokenThenExecCallback() with callback getGroupPosts(" + myContainer + ", " + widget + ")");
        getAccessTokenThenExecCallback(getGroupPosts, [myContainer, widget]);
    }
    else {
        getGroupPosts(myContainer, widget);
    }
}


function setGroupPostsWidgetHeader(groupInfoWidget, name) {
    var groupID = getGroupID(groupInfoWidget);
    var widgetName = getWidgetName(groupInfoWidget);

    var groupName = groupID;

    if (name) {
        groupName = name;
    }

    var fbHeader = groupInfoWidget.find('.clickableWidgetHeader');
    setGroupPostsHeader(fbHeader, widgetName, groupID, groupName);
}

function setGroupPostsHeader(fbHeader, widgetName, groupID, groupName) {
    var displayName = groupID;

    if (groupName) {
        displayName = groupName;
    }

    //		var text = widgetName + ': <a target="_blank" href="http://www.facebook.com/' + groupID + '">' + displayName + '</a>';
    var text = widgetName + ': ' + displayName;

    fbHeader.html(text);
}

function setGroupPostsWidgetFooter(groupInfoWidget, type) {
    var groupID = getGroupID(groupInfoWidget);

    var fbFooter = groupInfoWidget.find('.locationDecription');
    setGroupPostsFooter(fbFooter, type, groupID);
}

function setGroupPostsFooter(fbFooter, type, groupID) {
    var displayType = "entity";

    if (type) {
        displayType = type;
    }

    fbFooter.html("For Facebook " + displayType + ': <a target="_blank" href="http://www.facebook.com/' + groupID + '">' + groupID + '</a>');
}

function fbLoginStatusChanged(status, accessToken, expiresIn) {
    console.log("fbLoginStatusChanged: " + status + ", " + accessToken + ", " + expiresIn);
    fbLoginStatus = status;
    var statusMessage = getStatusMessage(status);

    if (status == "LOGGED_OUT") {
        //showFbLogin();
        login();
        return;
    }
    else if (status == "LOGGED_IN_EXTRA_PERMISSIONS_REQUIRED") {
        updateGroupPostsStatus('<p>' + statusMessage + '</p>');
        return;
    }
    else if (status == "POSTS_RECEIVED") {
        updateGroupPostsStatus('');
        return;
    }

    if (fbDebug) {
        updateGroupPostsStatus('<p>' + statusMessage + '</p><p>Access token: ' + accessToken + '</p><p>Expires in: ' + expiresIn + '</p>');
    }

    if (status == "CHECKING_LOGIN_STATUS") {
        updateGroupPostsStatus('<p>Checking Facebook login status...</p>');
    }
    else if (status == "LOGGED_IN_CHECKING_PERMISSIONS") {
        if (! fbDebug) {
            updateGroupPostsStatus('<p>Checking Facebook login status...OK</p><p>Getting group info...</p>');
        }
        getGroupInfoAllWidgets();
        getGroupPostInfoAllWidgets();
    }
    else if (status == "LOGGED_IN_TOKEN_EXTENDED") {
        clientAccessToken = accessToken;
        clientAccessTokenExpiryTime = expiresIn;
        console.log('Access token: ' + accessToken);
        console.log('Expires in: ' + expiresIn);
        fbLoginStatusChanged("LOGGED_IN_TOKEN_EXTENDED_GETTING_POSTS", accessToken, expiresIn);
    }
    else if (status == "LOGGED_IN_TOKEN_EXTENDED_GETTING_POSTS") {
        if (! fbDebug) {
            updateGroupPostsStatus('<p>Checking Facebook login status...OK</p><p>Getting group info...OK</p><p>Collecting posts...</p>');
        }

        //getGroupPostsAllWidgets();
        //getGroupPostCommentsAllWidgets();
        console.log("Calling registered callbacks");

        /*
                console.log(fbLoginCallback);
                console.log(fbLoginCallbackArgs);

                fbLoginCallback.apply(undefined, fbLoginCallbackArgs);
                */

        executeAccessTokenCallbacks();
    }
}

function showFbLogin() {
        updateGroupPostsStatus(	'<p class="middleTextWidgetWithHeaderOn">Please login to Facebook first:' +
                                                        '<input type="button" value="Login" id="fb-auth"/></p>');
        $('#fb-auth').click(login);
}

function updateGroupPostsStatus(html) {
        /*
        $('.groupPostsStatus').html(html);
        $('.groupPostCommentsStatus').html(html);
        */
        updateWidgetStatus(html);
}

function getGroupInfoAllWidgets() {
        var groupPostsWidgets = $('.groupPostsWidget');

        groupPostsWidgets.each(function(index) {
                getGroupInfo($(this));
        });
}

//	function getData(widget) {
//		var groupID = getGroupID(widget);
//
//		if (! groupID) {
//			var errorMessage = 'no groupID defined';
//			updateGroupPostsStatus('<p>Error: ' + errorMessage + '</p>'); //TODO: fix this method (add widget to params)
//			return;
//		}
//
//		var url = groupID + "&metadata=true";
//
//		FB.api(url,
//				function(metadata) {
//					console.log(metadata);
//					setGroupPostsWidgetHeader(widget, metadata.name);
//					setGroupPostsWidgetFooter(widget, metadata.type);
//
//					url = groupID + "/feed";
//
//					FB.api(url,
//							function(posts) {
//								console.log(posts);
//								fbLoginStatusChanged("POSTS_RECEIVED");
//								setGroupPostsWidgetHeader(widget, metadata.name + "(found " + posts.data.length + ")");
//								putGroupPostsIntoContainer(widget, posts.data);
//							});
//				});
//
//	}

function getGroupInfo(myDiv) {
    var groupID = getGroupID(myDiv);
    console.log(groupID);

    if (! groupID) {
        var errorMessage = 'no groupID defined';
        console.log(errorMessage);
        updateGroupPostsStatus('<p>Error: ' + errorMessage + '</p>'); //TODO: fix this method (add myDiv to params)
        return;
    }

    var url = groupID + "&metadata=true";
    console.log(url);

    FB.api(url,
        function(response) {
            console.log(response);
            setGroupPostsWidgetHeader(myDiv, response.name);
        //setGroupPostsWidgetFooter(myDiv, response.type);
        });
}
//
function getGroupPosts(myDiv, widget) {
    var groupID = getGroupID(myDiv);
    console.log(groupID);

    var widgetId = widget["id"];
    var widgetName = widget["name"];

    var status = myDiv.find(".groupPostsStatus");
    status.empty();

    status.append("<p>Getting search results from Facebook Group ID: " + groupID + "</p>");

    var container = myDiv.find(".groupPostsContainer");
    container.empty();

    //$('<input type="hidden" name="groupID" value="' + groupID + '">').appendTo(myContainer);
    //$('<input type="hidden" name="widgetName" value="' + widgetName + '">').appendTo(myContainer);
    //myContainer.append('<div class="groupPostsStatus"></div>');


    if (! groupID) {
        var errorMessage = 'no groupID defined';
        updateGroupPostsStatus('<p>Error: ' + errorMessage + '</p>'); //TODO: fix this method (add myDiv to params)
        return;
    }

    var url = groupID + "/feed";
    console.log(url);

    FB.api(url,
        function(response) {
            //console.log(JSON.stringify(response));
            var fbHeader = myDiv.find('.clickableWidgetHeader');
            var text = fbHeader.text();
            fbHeader.text(text + " (found " + response.data.length + ")");
            fbLoginStatusChanged("POSTS_RECEIVED");
            //					console.log("My ID: " + jQuery.data(myDiv[0], "widgetId"));

            var dataToSend = JSON.stringify({
                //wId: jQuery.data(myDiv[0], "widgetId"),
                wId: widgetId,
                type: "posts-facebook",
                name: "Posts by: " + response.data[0].from.name + ", found: " + response.data.length,
                location: "unknown",
                //data: response.data
                data: {
                    postData: {
                        query: url, 
                        data: response.data
                        }, 
                    userData: {}
            }
            });
        //console.log(dataToSend);
        $.ajax({
            type: 'POST',
            url: "/home/widgets/saveWidgetData/do.json",
            contentType: "application/json; charset=utf-8",
            data: dataToSend
        });
        putGroupPostsIntoContainer(myDiv, response.data);
    });
}

function getGroupID(widget) {
        return widget.find('input[name=groupID]').val();
}

function getWidgetName(widget) {
        return widget.find('input[name=widgetName]').val();
}

function getGroupPostsAllWidgets() {
        var groupPostsWidgets = $('.groupPostsWidget');

        groupPostsWidgets.each(function(index) {
                getGroupPosts($(this));
        });
}

function putGroupPostsIntoContainer(myDiv, posts) {
    //console.log("myDiv follows");
    //console.log(myDiv);

    var status = myDiv.find(".groupPostsStatus");
    status.empty();

    var container = myDiv.find(".groupPostsContainer");
    container.empty();

    if (posts.length > 0) {
        //container.append("<p class=\"numResultsTitle\">First three, click on the link above to see all:</p>");
        $.each(posts, function(num, post) {
            //console.log(post);
            var postEntry = $('<p class="widgetTweet"></p>').appendTo(container);

            if (post.picture)
                postEntry.append('<img src="' + post.picture + '">');
            else
                postEntry.append('<img src="https://graph.facebook.com/' + post.from.id + '/picture">');

            postEntry.append('<a target="_blank" href="http://www.facebook.com/profile.php?id=' + post.from.id + '">' + post.from.name + '</a><br>');

            if (post.message) {
                postEntry.append(trimMessage(post.message, 90));
                if (post.likes) {
                    postEntry.append(" (" + post.likes.count + " likes, " + post.comments.count + " comments)");
                }
            } else {
                if (post.name)
                    postEntry.append(trimMessage(post.name, 90));

                if (post.source)
                    postEntry.append(post.source);

                if (post.story)
                    postEntry.append(post.story);
            }

            //				a.append(
            //					+ '<img width="48" height="48" src="https://graph.facebook.com/' + contents.from.id + '/picture">'
            //					+ '<a target="_blank" href="http://www.facebook.com/profile.php?id=' + contents.from.id + '">' + contents.from.name + '</a><br>'
            //					+ trimmedMessage + " (" + contents.likes.count + " likes, " + contents.comments.count + " comments)"
            //					+ '</p>');

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
        container.append("<p>No posts to display. Try either refreshing data or selecting a Facebook Group to monitor.</p>");
    }
}

/*
	function addLinks(message) {
		//TODO convert link text to hyperlinks
	}
*/


/****************************************************************************
 * Get FB Posts From DB - Synchronous Version that returns the JSON
 ***************************************************************************/
	function getFBPostsFromDB_Synchronous(wId) {
    var posts;

    $.ajax({
      url:
        "/home/widgets/getWidgetData/do.json?wId=" + wId,
      type: 'get',
      //wId: wId,
      success:function(widgetDataUpdate){
        widgetData = widgetDataUpdate;

        //console.log(widgetData);
        if (widgetData[0] != undefined) {
          //posts = jQuery.parseJSON(widgetData[0]["dataAsJson"])["results"];
          //posts = widgetData[0]["dataAsJson"];
          //posts = jQuery.parseJSON(widgetData[0]["dataAsJson"]);
          var dataAsJson = jQuery.parseJSON(widgetData[0]["dataAsJson"]);
		  var postData = dataAsJson.postData;
		  if (! postData) {
			  posts = dataAsJson; // old format
		  }
		  else {
			posts = dataAsJson.postData.data;
		  }
          //putTweetIntoContainer2(myContainer, jQuery.parseJSON(widgetData[0]["dataAsJson"])["results"]);
        } else {
          posts = jQuery.parseJSON('{"result":"No data found."}');
        }
      },
      async: false
    });


    //console.log(tweets);
    return posts;
  }

