/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/*
 * RECENT TWITTER SEARCH WIDGET
 */
/****************************************************************************
 * addTwitter3PostsContainingTerm Widget
 ***************************************************************************/
function addTwitter3PostsContainingTerm(widget, recentOrPopular, customLat, customLon, customRad, customAddress) {
  var myDiv = initSearchWidget(widget);
  //	console.log(widget);
  var parameters = jQuery.parseJSON(widget["parametersAsString"]);
  var term = parameters.term;

  // determine whether we want to get new data from outside or get it from the DB
  var autoRefreshData = parameters.autoRefreshData;
  var autoRefreshDataFlag = false;
  if (autoRefreshData == "true") {
    autoRefreshDataFlag = true;
  }
  else {
    autoRefreshDataFlag = false;
  }


  var contentColour = parameters.contentColour;
  if (contentColour == undefined || contentColour == 'undefined' ) {
    contentColour = "gray";
  }

  var widgetType = widget["type"];

  // check we have lat, long and radius if this is a local search
  if (widgetType == "twitterLocal") {
    if (customLat == null | customLon == null |	customRad == null) {
      myDiv.append("<p>This is a local search widget but lat, long and radius need to be specified...</p>");
      return;
    }
  }

  // wegov blue
  //myDiv.style.backgroundColor = "rgb(144, 200, 219)";

  // Header Display

  var widgetHeaderDiv = $("<div class=\"widgetHeaderDiv\"></div>").appendTo(myDiv);

  $("<img class=\"widgetLogo\" src=\"img/twitter_newbird_boxed_whiteonblue.png\" alt=\"Source: Twitter\"/>").appendTo(widgetHeaderDiv);

  var widgetHeaderTextDiv = $("<div class=\"widgetHeaderTextDiv\"></div>").appendTo(widgetHeaderDiv);
  $("<h2 class=\"widgetHeader\">" + widget["name"]  + ": " + term + "</h2>").appendTo(widgetHeaderTextDiv);
  widgetHeaderDiv.append('<div class="clearfix"></div>');

  if (widgetType == "twitterLocal") {
    var widgetHeaderExtraTextDiv = $("<div class=\"widgetHeaderExtraTextDiv\"></div>").appendTo(myDiv);
    var locationDecription = $("<p class=\"locationDecription\"></p>")
    .appendTo(widgetHeaderExtraTextDiv);
    //locationDecription.text("For location: " + geoplugin_city() + ", "
    // + geoplugin_countryName());
	console.log("customAddress: " + customAddress);
	if (customAddress !== undefined) {
		locationDecription.text("For location: " + customAddress);
	}
	else {
		locationDecription.text("For location: [" + customLat + ", " + customLon + "]");
	}
  }

  var detailsButton = $(
    "<a target=\"_blank\"  href=\"/home/results.html?w="
    + widget["id"] + "\" class=\"widgetDetails\">"
    + "Details" + "</a>").appendTo(myDiv);

  myDiv.append('<div class="clearfix"></div>');


  // Container Display

  var myContainer = $("<div style=background-color:" + contentColour + "; class=\"widgetContent\"></div>").appendTo(myDiv);

  myContainer.append("<p>Getting results from WeGov database, please wait...</p>");

  var wId = widget["id"];


  if (autoRefreshDataFlag) {
    // refresh tweets
    if (widgetType == "twitterLocal") {
      refreshTwitterPostsWidget(widget, recentOrPopular, myContainer, parameters.term, customLat, customLon, customRad, customAddress);
    }
    else {
      refreshTwitterPostsWidget(widget, recentOrPopular, myContainer, parameters.term, null, null, null, null);
    }

    parameters.autoRefreshData = "false";

    $.get("/home/widgets/updateWidgetParameters/do.json", {
      wId: wId,
      newParametersValue: JSON.stringify(parameters)
      });

  }
  else {
    // get tweets from DB
    var jsonTweets = getTwitterPostsFromDB_Synchronous(wId);
    putTweetIntoContainer2(myContainer, jsonTweets);

    // two alternatives for getting tweets
    // - synchronous one that waits to get results
    // - async one that displays as well

    //getTwitterPostsFromDB_Display_Aynchronous(myContainer, wId);

  }

  var widgetFooter = $("<div class=\"widgetFooter\"></div>").appendTo(myDiv);
  var settingsButton = $("<p class=\"widgetSettings\">Settings</p>").appendTo(widgetFooter);
  settingsButton.click(function(e){
    //showWidgetSettingsWindowSJT(myDiv, widget, "Settings")
    showWidgetSettingsWindowSJT(myDiv, widget)
  });
  var refreshButton = $("<p class=\"widgetRefresh\">Refresh Data</p>").appendTo(widgetFooter);
  refreshButton.click(function(e){
    if (widgetType == "twitterLocal") {
      refreshTwitterPostsWidget(widget, recentOrPopular, myContainer, parameters.term, customLat, customLon, customRad, customAddress);
    }
    else {
      refreshTwitterPostsWidget(widget, recentOrPopular, myContainer, parameters.term, null, null, null, null);
    }
  });
  widgetFooter.append('<div class="clearfix"></div>');

}

/****************************************************************************
 * Get Twitter Posts From DB - Synchronous Version that returns the JSON
 ***************************************************************************/
function getTwitterPostsFromDB_Synchronous(wId) {
  var tweets;

  $.ajax({
    url:
    "/home/widgets/getWidgetData/do.json?wId=" + wId,
    type: 'get',
    //wId: wId,
    success:function(widgetDataUpdate){
      widgetData = widgetDataUpdate;

      console.log(widgetData);
      if (widgetData[0] != undefined) {
        //tweets = jQuery.parseJSON(widgetData[0]["dataAsJson"])["results"];
        var postData = jQuery.parseJSON(widgetData[0]["dataAsJson"])["postData"];
        //console.log(postData);
        tweets = postData["results"];
      } else {
        tweets = jQuery.parseJSON('{"result":"No data found."}');
      }
    },
    async: false
  });


  //console.log(tweets);
  return tweets;
}


/****************************************************************************
 * Get Twitter Posts From DB - Asynchronous Version that Does inline Display
 ***************************************************************************/
function getTwitterPostsFromDB_Display_Aynchronous(container, wId) {
  jQuery.get("/home/widgets/getWidgetData/do.json", {
    wId: wId
  }, function(widgetDataUpdate){
    //jQuery.get("/home/widgets/getWidgetData/do.json?wId=" + wId, function(widgetDataUpdate){
    widgetData = widgetDataUpdate;
    //console.log(widgetData);
    if (widgetData[0] != undefined) {
      putTweetIntoContainer2(container, jQuery.parseJSON(widgetData[0]["dataAsJson"])["results"]);
    } else {
      $("#history").append("<p>No data found.</p>");
    }
  });

}


/****************************************************************************
 * Refresh Twitter Posts Widget
 ***************************************************************************/
function refreshTwitterPostsWidget(widget, recentOrPopular, myDiv, term, customLat, customLon, customRad, customAddress) {
  myDiv.empty();
  var widgetType = widget["type"];

  //var myContainer = $("<div class=\"widgetContainer\"></div>").appendTo(myDiv);
  var myContainer = $("<div class=\"tweetContainer\"></div>").appendTo(myDiv);
  myContainer.append("<p>Searching Twitter, please wait...</p>");

  var parsedTerm = term.replace("#", "%23");

  var twitterQuery;

  if (widgetType == "twitterLocal") {
    twitterQuery = "https://search.twitter.com/search.json?q=" + parsedTerm
    + "&include_entities=true&result_type=" + recentOrPopular
    + "&geocode=" + customLat + "," + customLon + "," + customRad
    + "mi&rpp=100&callback=?";
  }
  else {
    twitterQuery = "https://search.twitter.com/search.json?q=" + parsedTerm
    + "&include_entities=true&result_type=" + recentOrPopular
    + "&rpp=100&callback=?";
  }


  // get posts
  $.getJSON(twitterQuery, {}, function(data) {

    //var twitterResults = JSON.stringify(data["results"]);

    //			console.log(data);

    // now get users for posts
    var twitterIdsToLookup = "";
    $.each(data["results"], function(resultCounter, result){
      if (resultCounter < data["results"].length - 1)
        twitterIdsToLookup = twitterIdsToLookup + result["from_user_id"] + ",";
      else
        twitterIdsToLookup = twitterIdsToLookup + result["from_user_id"];
    });
    var usersLookupQuery = "https://api.twitter.com/1/users/lookup.json?user_id=" + twitterIdsToLookup + "&include_entities=true&callback=?";
    //myChart.append("<p>Looking up users on Twitter...</p>");
    $.ajax({
      url:usersLookupQuery,
      dataType: 'json',
      type: 'GET',
      contentType: "application/json; charset=utf-8",
      error: function(){
        myChart.append("<p>Error getting Twitter Users. Try refreshing the data or specifying a different search term.</p>");;
      },
      success:function(userdata) {
        /*
					myChart.append("<p>Running analysis...</p>");
					$.ajax({
						  type: 'POST',
						  url: "/home/analysis/kmi/do.json",
						  contentType: "application/json; charset=utf-8",
						  data: JSON.stringify({postData: data, userData: userdata}),
*/

        var dataToSend;
        if (widgetType == "twitterLocal") {
          dataToSend = JSON.stringify({
            wId: widget["id"],
            type: "posts-twitter",
            name: "Search query: " + term
            //+ "; location: " + geoplugin_city() + ", " + geoplugin_countryName()
            + "; location: " + customAddress
            + "; posts: "
            + data["results"].length,
            //location: geoplugin_city() + ", " + geoplugin_countryName(),
            location: customAddress,
            //data: data
            data: JSON.stringify({
              postData: data,
              userData: userdata
            })
          });
        }
        else {
          dataToSend = JSON.stringify({
            wId: widget["id"],
            type: "posts-twitter",
            name: "Search query: " + term + "; posts: " + data["results"].length,
            location: "none",
            //data: data});
            data: JSON.stringify({
              postData: data,
              userData: userdata
            })
          });
        }


        //console.log("Sending data to server");
        //console.log(dataToSend);

        $.ajax({
          type: 'POST',
          url: "/home/widgets/saveWidgetData/do.json",
          contentType: "application/json; charset=utf-8",
          data: dataToSend
        })
        // End store results in server database

        putTweetIntoContainer2(myContainer, data["results"]);
      }

    });

  });
}



/****************************************************************************
 * putTweetIntoContainer2
 ***************************************************************************/
function putTweetIntoContainer2(container, tweetData) {
  //console.log(tweetData);
  container.empty();
  if (tweetData.length > 0) {
    //container.append("<p class=\"numResultsTitle\">First three, click on the link above to see all:</p>");
    var locationDetails;
    $.each(tweetData, function(num, contents) {
      locationDetails = "";

      container.append("<p class=\"widgetTweet\"><img src=\""
        + contents["profile_image_url_https"] + "\">"
        + "<a class=\"capitalisedLink\" target=\"_blank\" href=\"https://twitter.com/" + contents["from_user"]
        + "\">" + $.trim(contents["from_user_name"]) + "</a>"
        + locationDetails + "<br>" + twttr.txt.autoLink(trimMessage(contents["text"], 150))  + "</p>");

      // console.log(num);

      if (num > 1) {
        return false;
      }
    });
  } else {
    container.append("<p>Nothing was found. Try refreshing the data or specifying a different search term.</p>");
  }
}

