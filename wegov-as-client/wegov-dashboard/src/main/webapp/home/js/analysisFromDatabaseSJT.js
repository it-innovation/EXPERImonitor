/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/****************************************************************************
 *  Widget Data type
 ***************************************************************************/
function widgetDataStruct(id, name, term, colour, type, category, dataType, params) {

  this.id = id;
  this.name = name;
  this.term = term;
  this.colour = colour;
  this.type = type;
  this.category = category;
  this.dataType = dataType;
  this.parameters = params;

}

/*
 * Get source widget data
 */
function getSourceWidgetData(sourceWidgetId) {


  // get details for source widget from DB
  var sourceWidgetName = undefined;
  var sourceWidgetTerm = undefined;
  var sourceWidgetColour = undefined;
  var sourceWidgetType = undefined;
  var sourceWidgetCategory = undefined;
  var sourceWidgetDataType = undefined;
  var sourceWidgetParameters = undefined;

  var srcWidgetOut = null;


  if (sourceWidgetId == undefined) {
    console.log ("Source Widget ID is undefined.");
    srcWidgetOut = new widgetDataStruct(
      "undefined",
      "no-source-widget",
      "no-source-widget-term",
      "gray",
      "no-source-widget-type",
      "no-source-widget-catgory",
      "no-source-widget-dataType",
      "no-source-widget-parameters"
    );
    return srcWidgetOut;

  }


  $.ajax({
    url:
      "/home/widgets/getWidget/do.json?wId=" + sourceWidgetId,
    type: 'get',

    success:function(srcWidget){
      if (srcWidget != undefined) {

        sourceWidgetName = srcWidget["name"];
        sourceWidgetType = srcWidget["type"];
        sourceWidgetCategory = srcWidget["category"];
        sourceWidgetDataType = srcWidget["dataType"];

        var srcParameters = jQuery.parseJSON(srcWidget["parametersAsString"]);

        if (srcParameters != undefined) {
          sourceWidgetParameters = srcParameters;
        }
        else {
          sourceWidgetParameters = "none";
        }

        if (srcParameters != undefined && srcParameters.term != undefined) {
          sourceWidgetTerm = srcParameters.term;
        }
        else {
          sourceWidgetTerm = "nothing";
        }
        if (srcParameters != undefined && srcParameters.contentColour != undefined) {
          sourceWidgetColour = srcParameters.contentColour;
        }
        else {
          sourceWidgetColour = "gray";
        }

        console.log("Source widget id = " + sourceWidgetId +
          " widget name = " + sourceWidgetName +
          ", widget type = " + sourceWidgetType +
          ", source term = " + sourceWidgetTerm +
          ", source colour = " + sourceWidgetColour);



      } else {
        console.log ("No data found.");
        return null;
        //sourceWidgets = jQuery.parseJSON('{"result":"No data found."}');
      }
    },
    async: false
  });

  srcWidgetOut = new widgetDataStruct(
    sourceWidgetId,
    sourceWidgetName,
    sourceWidgetTerm,
    sourceWidgetColour,
    sourceWidgetType,
    sourceWidgetCategory,
    sourceWidgetDataType,
    sourceWidgetParameters
  );

  return srcWidgetOut;


}


/****************************************************************************
 * addTopicAnalysisWidget Widget
 ***************************************************************************/
function addAnalysisUsingStoredPostData(widget) {
	var myDiv = initAnalysisWidget(widget);
	var widgetId = widget["id"];
	var widgetType = widget["type"];
	var parameters = jQuery.parseJSON(widget["parametersAsString"]);
	var searchTerms = parameters.term;
  var sourceWidgetId = parameters.sourceWidgetId;
  var role = parameters.role;

  console.log("Widget is a " + widgetType + " type");
  console.log (parameters);


  var sourceWidget = getSourceWidgetData(sourceWidgetId);
  var sourceWidgetParameters = sourceWidget["parameters"];


  console.log (sourceWidget);
  console.log (sourceWidgetParameters);


  var contentColour = parameters.contentColour;
  if (contentColour == undefined || contentColour == 'undefined' ) {
    contentColour = "gray";
  }

  console.log(sourceWidgetId);

	var widgetHeaderDiv = $("<div id=\"widgetHeader_" + widgetId + "\" class=\"analysisWidgetHeaderDiv\"></div>").appendTo(myDiv);

  var widgetLogo = $("<img id=\"widgetHeaderLogo_" + widgetId + "\" class=\"widgetLogo\">" + "</img>").appendTo(widgetHeaderDiv);

	var widgetHeaderTextDiv = $("<div id=\"widgetHeaderText_" + widgetId + "\" class=\"widgetHeaderTextDiv\"></div>").appendTo(widgetHeaderDiv);

  //var myHeader = $("<a id=\"myHeader_" + widgetId + "\" class=\"nonClickableWidgetHeader\">" + "</a>").appendTo(widgetHeaderTextDiv);
  var myHeader = $("<p id=\"myHeader_" + widgetId + "\" class=\"nonClickableWidgetHeader\">" + "</p>").appendTo(widgetHeaderTextDiv);

  //widgetHeaderDiv.append('<div class="clearfix"></div>');
	var widgetHeaderExtraTextDiv = $("<div id=\"widgetHeaderExtraText_" + widgetId + "\" class=\"widgetHeaderExtraTextDiv\"></div>").appendTo(widgetHeaderDiv);

  var extraText = $("<p></p>").appendTo(widgetHeaderExtraTextDiv);

	if (parameters.location != undefined) {
		$("<p class=\"locationDecription\">For location: " + geoplugin_city() + ", "
				+ geoplugin_countryName() + "</p>")
		.appendTo(widgetHeaderExtraTextDiv);
	}
  /*
  else {
		$("<p class=\"locationDecription\">Based on no location bound search</p>")
		.appendTo(widgetHeaderExtraTextDiv);
	}
*/
  myDiv.append('<div class="clearfix"></div>');


  var containerDiv =
    $("<div id=\"myContainer_" + widgetId + "\" style=background-color:" + sourceWidget.colour + "; class=\"widgetContent\"></div>")
    .appendTo(myDiv);

  myDiv.append('<div class="clearfix"></div>');


  // Main analysis switch
  if (widgetType == "topics_from_database") {

    // Topic Analysis

    var sourceWidgetPostName = null;

    var sourceWidgetType = sourceWidget.type;



    if (sourceWidgetType == "grouppostcomments") {
      if (sourceWidgetParameters.postName != undefined) {
        sourceWidgetPostName = "\"" + sourceWidgetParameters.postName + "\"";
      }
      else {
        sourceWidgetPostName = sourceWidget.term;
      }
    }
    else {
      sourceWidgetPostName = sourceWidget.term;
    }
/*
    document.getElementById("myHeader_" + widgetId).innerHTML =
      "<a id=\"myHeader_" + widgetId + "\" class=\"nonClickableWidgetHeader\">"
      + "Topics for " + sourceWidget.name + ": " + sourceWidget.term + "</a>";
*/

    document.getElementById("myHeader_" + widgetId).innerHTML =
      "<a id=\"myHeader_" + widgetId + "\" class=\"nonClickableWidgetHeader\">"
      + "Topics for " + sourceWidget.name + ": " + sourceWidgetPostName + "</a>";

  document.getElementById("widgetHeaderLogo_" + widgetId).innerHTML =
      "<img id=\"widgetHeaderLogo_" + widgetId
      + "\" class=\"widgetLogo\" src=\"img/UKOB-logo-v2.png\" alt=\"Source: Uni Koblenz\"/>";

    var listOfTopicsDiv = $("<div id=\"listOfTopicsDiv_" + widgetId + "\" class=\"listOfTopicsDiv\"></div>").appendTo(containerDiv);

    listOfTopicsDiv.empty();
    listOfTopicsDiv.append("<p>Getting search results ...</p>");
    listOfTopicsDiv.append("<p>Running analysis...</p>");

    refreshTopicsForWidget (listOfTopicsDiv, sourceWidget.id, sourceWidget.type);

  }
  else if (widgetType == "userroles") {

    // User Roles

    document.getElementById("myHeader_" + widgetId).innerHTML =
      "<a id=\"myHeader_" + widgetId + "\" class=\"nonClickableWidgetHeader\">"
      + "User Roles for " + sourceWidget.name + ": " + sourceWidget.term + "</a>";

    document.getElementById("widgetHeaderLogo_" + widgetId).innerHTML =
      "<img id=\"widgetHeaderLogo_" + widgetId
      + "\" class=\"widgetLogo\" src=\"img/kmi-logo-square.png\" alt=\"Source: Open University\"/>";

    //var myChart = $("<div id=\"piechart_" + widgetId + "\" class=\"piechart\"></div>").appendTo(containerDiv);
    var myChart = $("<div id=\"piechart_" + widgetId +
      "\" style=background-color:" + sourceWidget.colour +
      "; class=\"piechart\"></div>").appendTo(containerDiv);


    myChart.empty();
    myChart.append("<p>Getting search results ...</p>");
    myChart.append("<p>Running analysis...</p>");

    refreshUserRolesForWidget(myChart, sourceWidgetId);

  }
  else if (widgetType == "roleforterm") {
    // Role for search term
    document.getElementById("myHeader_" + widgetId).innerHTML =
      "<a id=\"myHeader_" + widgetId + "\" class=\"nonClickableWidgetHeader\">"
      + "Users with Role " + role + "\" for: "
      + sourceWidget.name + ": " + sourceWidget.term + "</a>";

    document.getElementById("widgetHeaderLogo_" + widgetId).innerHTML =
      "<img id=\"widgetHeaderLogo_" + widgetId
      + "\" class=\"widgetLogo\" src=\"img/kmi-logo-square.png\" alt=\"Source: Open University\"/>";

    var listOfUserWithRoleDiv = $("<div id=\"listOfUserWithRoleDiv_" + widgetId + "\" class=\"listOfUserWithRoleDiv\"></div>").appendTo(containerDiv);

    listOfUserWithRoleDiv.empty();
    listOfUserWithRoleDiv.append("<p>Getting search results ...</p>");
    listOfUserWithRoleDiv.append("<p>Running analysis...</p>");

    refreshUsersForRoleWidget(listOfUserWithRoleDiv, sourceWidgetId, role);
  }

  else {

    containerDiv.append("<p>Unknown analysis type.</p>");
  }


	var widgetFooter = $("<div class=\"widgetFooter\"></div>").appendTo(myDiv);
	var settingsButton = $("<p class=\"widgetSettings\">Settings</p>").appendTo(widgetFooter);
  settingsButton.click(function(e){
    showSettingsForAnalysisUsingWidgetData(myDiv, widget);
  });
	var refreshButton = $("<p class=\"widgetRefresh\">Refresh Data</p>").appendTo(widgetFooter);
  refreshButton.click(function(e){
    //addAnalysisUsingStoredPostData(widget);


    if (widgetType == "topics_from_database") {

      document.getElementById("myHeader_" + widgetId).innerHTML =
        "<a id=\"myHeader_" + widgetId + "\" class=\"nonClickableWidgetHeader\">"
        + "Topics for " + sourceWidget.name + ": " + sourceWidget.term + "</a>";

      document.getElementById("widgetHeaderLogo_" + widgetId).innerHTML =
        "<img id=\"widgetHeaderLogo_" + widgetId
        + "\" class=\"widgetLogo\" src=\"img/UKOB-logo-v2.png\" alt=\"Source: Uni Koblenz\"/>";

      listOfTopicsDiv.empty();
      listOfTopicsDiv.append("<p>Getting search results ...</p>");
      listOfTopicsDiv.append("<p>Running analysis...</p>");

      refreshTopicsForWidget (listOfTopicsDiv, sourceWidget.id);

    }
    else if (widgetType == "userroles") {

      document.getElementById("myHeader_" + widgetId).innerHTML =
        "<a id=\"myHeader_" + widgetId + "\" class=\"nonClickableWidgetHeader\">"
        + "User Roles for " + sourceWidget.name + ": " + sourceWidget.term + "</a>";

      document.getElementById("widgetHeaderLogo_" + widgetId).innerHTML =
        "<img id=\"widgetHeaderLogo_" + widgetId
        + "\" class=\"widgetLogo\" src=\"img/kmi-logo-square.png\" alt=\"Source: Open University\"/>";

      myChart.empty();
      myChart.append("<p>Getting search results ...</p>");
      myChart.append("<p>Running analysis...</p>");

      refreshUserRolesForWidget(myChart, sourceWidget.id);

    }
    else if (widgetType == "roleforterm") {
      // Role for search term
      document.getElementById("myHeader_" + widgetId).innerHTML =
        "<a id=\"myHeader_" + widgetId + "\" class=\"nonClickableWidgetHeader\">"
        + "Users with Role " + role + "\" for: "
        + sourceWidget.name + ": " + sourceWidget.term + "</a>";

      document.getElementById("widgetHeaderLogo_" + widgetId).innerHTML =
        "<img id=\"widgetHeaderLogo_" + widgetId
        + "\" class=\"widgetLogo\" src=\"img/kmi-logo-square.png\" alt=\"Source: Open University\"/>";

      //listOfUserWithRoleDiv = $("<div id=\"listOfUserWithRoleDiv_" + widgetId + "\" class=\"listOfUserWithRoleDiv\"></div>").appendTo(containerDiv);

      listOfUserWithRoleDiv.empty();
      listOfUserWithRoleDiv.append("<p>Getting search results ...</p>");
      listOfUserWithRoleDiv.append("<p>Running analysis...</p>");

      refreshUsersForRoleWidget(listOfUserWithRoleDiv, sourceWidgetId, role);
    }

    else {

      containerDiv.append("<p>Unknown analysis type.</p>");
    }



  });
  widgetFooter.append('<div class="clearfix"></div>');


}

/*
 *refresh the whole widget
 */
/*
function refreshWholeTopicAnalysisWidget(widgetId, sourceWidget, topicsDiv) {
//function refreshWholeTopicAnalysisWidget(widgetId, sourceWidget, myDiv) {

    document.getElementById("myHeader_" + widgetId).innerHTML =
      "<a id=\"myHeader_" + widgetId + "\" class=\"nonClickableWidgetHeader\">" + "Topics for " + sourceWidget.name + ": " + sourceWidget.term + "</a>";
    document.getElementById("widgetHeaderLogo_" + widgetId).innerHTML =
      "<img id=\"widgetHeaderLogo_" + widgetId + "\" class=\"widgetLogo\" src=\"img/UKOB-logo-v2.png\" alt=\"Source: Uni Koblenz\"/>";

    //$("<div id=\"myContainer_" + widgetId + "\" style=background-color:" + sourceWidget.colour + "; class=\"widgetContent\"></div>").replaceAll(topicsDiv);

    $(topicsDiv).replaceWith("<div id=\"listOfTopicsDiv_" + widgetId + "\" style=background-color:" + sourceWidget.colour + "; class=\"listOfTopicsDiv\"></div>");

    topicsDiv.empty();
    topicsDiv.append("<p>Getting search results ...</p>");
    topicsDiv.append("<p>Running analysis...</p>");



    //listOfTopicsDiv.append("<p>Getting search results ...</p>");

    //listOfTopicsDiv.append("<p>Running analysis...</p>");

    console.log (topicsDiv);
    //console.log (listOfTopicsDiv);

    //refreshTopicsForWidget (listOfTopicsDiv, sourceWidget.id);
    //refreshTopicsForWidget (containerDiv, sourceWidget.id, sourceWidget.type);
    refreshTopicsForWidget (topicsDiv, sourceWidget.id);

}
*/
/*
 * Refresh the topics for a widget
 */


//function refreshTopicsForWidget (listOfTopicsDiv, widgetId, sourceWidgetId) {
function refreshTopicsForWidget (topicsDiv, sourceWidgetId) {


  if (sourceWidgetId == undefined | sourceWidgetId == "undefined") {
    topicsDiv.append("<p>Source widget is undefined. Please set a source widget in \"settings\"</p>");
    return;
  }


  var sourceWidget = getSourceWidgetData(sourceWidgetId);


  console.log (topicsDiv);
  console.log (sourceWidget);




  var analysisUrl = undefined;
  if ( (sourceWidget.type == "groupposts") || (sourceWidget.type == "grouppostcomments") ) {
    analysisUrl = "/home/analysis/koblenz/facebook/widget_data/do.json?wId=" + sourceWidget.id;
  }
  else {
    analysisUrl = "/home/analysis/koblenz/twitter/widget_data/do.json?wId=" + sourceWidget.id;
  }

  $.ajax({
      type: 'POST',
     // url: "/home/analysis/koblenz/twitter/widget_data/do.json?wId=" + sourceWidgetId,
      url: analysisUrl,
      contentType: "application/json; charset=utf-8",
      error: function() {
        topicsDiv.append("<p>Error getting data, try again?</p>");
      },
      success: function(result){
        var topicDivId = $(topicsDiv).attr("id");
        topicsDiv.empty();
        //console.log(topicDivId);
        //$(topicsDiv).replaceWith("<div id=" + topicDivId + "\" class=\"listOfTopicsDiv\"></div>");
        //console.log (topicsDiv);
        if (result["topics"]) {
          topicsDiv.append("<p class=\"koblenzResultsHeader\">Found " + result["numTopicsAsString"] + ":</p>");
          $.each(result["topics"], function(counter, topic){
            topicsDiv.append("<p class=\"koblenzTopicKeywords\">" + (counter + 1) + ". " + topic["keywords"] + "</p>");
            var koblenzWidgettopicWrapper = $("<div class=\"koblenzWidgettopicWrapper\"></div>").appendTo(topicsDiv);
            koblenzWidgettopicWrapper.append("<p class=\"koblenzKeyUsersLabel\">Key users: </p>");
            $.each(topic["keyUsers"], function(keyUserCounter, keyUser){
              if (keyUserCounter == topic["keyUsers"].length - 1)
                koblenzWidgettopicWrapper.append(
                  "<a target=\"_blank\" class=\"koblenzTopicuserProfileLink\" href=\"https://twitter.com/#!/"
                  + keyUser["screenName"] + "\">" + keyUser["fullName"] + "</a>");
              else
                koblenzWidgettopicWrapper.append(
                  "<a target=\"_blank\" class=\"koblenzTopicuserProfileLink\" href=\"https://twitter.com/#!/"
                  + keyUser["screenName"] + "\">" + keyUser["fullName"] + "</a>, ");
            });

          });
        } else {
          topicsDiv.append("<p>Nothing was found. Try again?</p>");
        }

      }
  });

}

/*
 * refresh whole widget
 */
/*
function refreshWholeUserRolesAnalysisWidget(widgetId, sourceWidget, containerDiv) {

    document.getElementById("myHeader_" + widgetId).innerHTML =
      "<a id=\"myHeader_" + widgetId + "\" class=\"nonClickableWidgetHeader\">" + "User Roles for " + sourceWidget.name + ": " + sourceWidget.term + "</a>";
    document.getElementById("widgetHeaderLogo_" + widgetId).innerHTML =
      "<img id=\"widgetHeaderLogo_" + widgetId + "\" class=\"widgetLogo\" src=\"img/kmi-logo-square.png\" alt=\"Source: Open University\"/>";

    //$('.myContainer_' + widgetId).remove();

//    var containerDiv =
      $("<div id=\"myContainer_" + widgetId + "\" style=background-color:" + sourceWidget.colour + "; class=\"widgetContent\"></div>")
      .replaceAll(containerDiv);
    // User Roles

    $(containerDiv).replaceWith("<div id=\"myContainer_" + widgetId + "\" style=background-color:" + sourceWidget.colour + "; class=\"piechart\"></div>");

    console.log (containerDiv);

    //var myChart = $("<div id=\"piechart_" + widgetId + "\" class=\"piechart\"></div>").appendTo(containerDiv);
    //refreshUserRolesForWidget(myChart, sourceWidget.id);
    refreshUserRolesForWidget(containerDiv, sourceWidget.id);


}
*/

/*
 * Refresh roles for a widget
 */
function refreshUserRolesForWidget(chartDiv, sourceWidgetId) {
  if (sourceWidgetId == undefined | sourceWidgetId == "undefined") {
    chartDiv.append("<p>Source widget is undefined. Please set a source widget in \"settings\"</p>");
    return;
  }

  var sourceWidget = getSourceWidgetData(sourceWidgetId);

  console.log (sourceWidget);

	chartDiv.empty();

	var analysisUrl = undefined;
	if ( (sourceWidget.type == "groupposts") || (sourceWidget.type == "grouppostcomments") ) {
		//N.B. following URL is not yet available on server, so we bomb out
		analysisUrl = "/home/analysis/kmi/facebook/widget_data/do.json?wId=" + sourceWidget.id;
		chartDiv.append("<p>Sorry, user roles not currently available for Facebook</p>");
		return
	}
	else {
		analysisUrl = "/home/analysis/kmi/twitter/widget_data/do.json?wId=" + sourceWidget.id;
	}

	chartDiv.append("<p>Getting search results from WeGov DB...</p>");

    $.ajax({
      type: 'POST',
      url: analysisUrl,
      contentType: "application/json; charset=utf-8",
      error: function() {
        chartDiv.append("<p>Error getting data, try again?</p>");
      },
      success: function(result){
        chartDiv.empty();
        var userRolesPlotData = new Array();
        var discussionActivityPlotData = new Array();
        var tempArray;
        $.each(result["roleDistributionPoints"], function(counter, roleDistributionPoint){
          tempArray = new Array();
          tempArray[0] = roleDistributionPoint["roleName"];
          tempArray[1] = roleDistributionPoint["numberOfUsers"];
          userRolesPlotData[counter] = tempArray;
        });
        var userRolesDistributionPlot = jQuery.jqplot(chartDiv.attr('id'), [ userRolesPlotData ], {
          seriesDefaults : {
            renderer : jQuery.jqplot.PieRenderer,
            rendererOptions : {
              showDataLabels : true
            }
          },
          legend : {
            show : true,
            location : 'e',
            fontSize: '10px',
            showLabels: true
          }
        });
      }
  });

}

//

function refreshUsersForRoleWidget(listOfUserWithRoleDiv, sourceWidgetId, role) {

  if (sourceWidgetId == undefined | sourceWidgetId == "undefined") {
    listOfUserWithRoleDiv.append("<p>Source widget is undefined. Please set a source widget in \"settings\"</p>");
    return;
  }

  var sourceWidget = getSourceWidgetData(sourceWidgetId);


  var term = sourceWidget.term;

  console.log (sourceWidget);

  listOfUserWithRoleDiv.empty();
	listOfUserWithRoleDiv.append("<p>Getting search results from WeGov DB...</p>");

  var analysisUrl =
    "/home/analysis/kmi/onlyroles/widget_data/do.json"
    //"/home/analysis/kmi/twitter/widget_data/do.json"
    + "?wId=" + sourceWidget.id
    + "&selectedRoleName=" + role
    + "&searchQuery=" + term;

  $.ajax({
    type: 'POST',
    url: analysisUrl,
    contentType: "application/json; charset=utf-8",
    error: function() {
      listOfUserWithRoleDiv.append("<p>Error getting data, try again?</p>");
    },
    success:function(userdata) {
      console.log(userdata);
      listOfUserWithRoleDiv.append("<p>Running analysis</p>");
        listOfUserWithRoleDiv.empty();
        var users = userdata["users"];
        if (users.length < 1) {
          listOfUserWithRoleDiv.append("<p>No users found with this role. Try a different role?</p>");
        }
        else {
          var usersAsString = "";
          $.each(users, function(counter, kmiuser){
            usersAsString = usersAsString + "<a target=\"_blank\" href=\"https://twitter.com/#!/" + kmiuser["screenName"] + "\">" + kmiuser["name"] + "</a>";

            if (counter != users.length - 1)
              usersAsString = usersAsString + ", ";
          });

          if (users.length == 1)
            listOfUserWithRoleDiv.append("<p>Found 1 user</p>");
          else
            listOfUserWithRoleDiv.append("<p>Found " + users.length + " users</p>");

          listOfUserWithRoleDiv.append("<p>" + usersAsString + "</p>");
        }
      }
    });
}
//

/*
 * WIDGET SETTINGS POPUP
 */
function showSettingsForAnalysisUsingWidgetData(myDiv, widget) {
	var widgetId = widget["id"];
	var widgetType = widget["type"];
  var inputLabelText = widget["labelText"];
	var parameters = jQuery.parseJSON(widget["parametersAsString"]);
	var term = parameters.term;
  var role = parameters.role;
  var sourceWidgetId = null;

  sourceWidgetId = parameters.sourceWidgetId;

  console.log(sourceWidgetId);

		var propertiesPopup = $("<div class=\"configureTwitterPostsByLocation closed-by-escape\" id=\"configureTwitterPostsByLocation_" + widgetId + "\"></div>").appendTo("#wrapper");
		$("<h2>Configure widget</h2>").appendTo(propertiesPopup);

    propertiesPopup.append('<div class="clearfix"></div>');

    var deleteButton = $("<p id=\"deleteTwitterPostsByLocationConfiguration_" + widgetId + "\" class=\"deleteWidgetButton\">Delete Widget</p>").appendTo(propertiesPopup);
		deleteButton.click(function(e){
			$.get("/home/widgets/deleteWidget/do.json", {wId: widgetId}, function(result){
				$('.closed-by-escape').hide();
				propertiesPopup.remove();
				myDiv.remove();
			} );
		});

    var optionsDiv = $("<div class=\"widgetPropertiesOptions\"></div>").appendTo(propertiesPopup);
		optionsDiv.append("<p>" + inputLabelText + "</p>");

    // Source Widget selection
    // get search widgets

    var sourceWidgets = [];

    var data = [];

    var sourceWidgetSetRequestUrl = null;
    if (widgetType == "roleforterm" || widgetType == "userroles") {

      //restrict input set to Twitter only
      sourceWidgetSetRequestUrl = "/home/widgets/getWidgetsMatchingDataType/do.json?dataType=" + "tweets";

    }
    else {
      // all search widgets are OK
      sourceWidgetSetRequestUrl = "/home/widgets/getWidgetsMatchingWidgetCategory/do.json?widgetCategory=" + "search";

    }

    $.ajax({
      url:
        //"/home/widgets/getWidgetsMatchingDataType/do.json?dataType=" + "posts",
        //"/home/widgets/getWidgetsMatchingWidgetCategory/do.json?widgetCategory=" + "search",
        sourceWidgetSetRequestUrl,
      type: 'get',

      success:function(widgetDataUpdate){
        var widgetData = widgetDataUpdate;

        if (widgetData != undefined) {

          sourceWidgets = widgetData;
          if (sourceWidgets.length > 0) {
            $.each(sourceWidgets, function(widgetNum, srcWidget) {

              var sourceTerm = null;
              var sourceColour = null;
              var widgetName = srcWidget["name"];
              var srcParameters = jQuery.parseJSON(srcWidget["parametersAsString"]);
              if (srcParameters != undefined && srcParameters.term != undefined) {
                widgetName = widgetName + " " + srcParameters.term;
                sourceTerm = srcParameters.term;
              }
              else {
                sourceTerm = "nothing";
              }
              if (srcParameters != undefined && srcParameters.contentColour != undefined) {
                sourceColour = srcParameters.contentColour;
                console.log("widget id = " + srcWidget["id"] + " widget name = " + widgetName + " source colour = " + sourceColour);
              }
              else {
                sourceColour = "gray";
              }
              data.push({
                text:  widgetName,
                value:  srcWidget["id"],
                description:  widget["description"],
                term: sourceTerm,
                contentColour: sourceColour
              });
            });
          }
        } else {
          console.log ("No data found.");
          //sourceWidgets = jQuery.parseJSON('{"result":"No data found."}');
        }
      },
      async: false
    });

    console.log(data);
    console.log(sourceWidgetId);

    var sourceWidgetSelector = $("<div class=\"sourceWidgetSelector\"></div>").appendTo(optionsDiv);
    var sourceWidgetSelectionDropDownDiv = $("<div style=width:300px; class=\"sourceWidgetSelectionDropDownDiv\"></div>").appendTo(sourceWidgetSelector);

    //sourceWidgetSelectionDropDownDiv.append("<p>Search widgets to use as source:</p><br>");

    // Get value + description out of data!

    var sourceWidgetSelectionDropDown;

    if (sourceWidgetId != 'undefined' && sourceWidgetId != undefined) {
      sourceWidgetSelectionDropDown =
        $("<input id=\"sourceWidgetSelectionDropDownList_"
        + widgetId
        + "\" value =\""
        + sourceWidgetId
        + "\"/>")
      .appendTo(sourceWidgetSelectionDropDownDiv);
    }
    else {
      sourceWidgetSelectionDropDown =
        $("<input id=\"sourceWidgetSelectionDropDownList_"
        + widgetId
        + "\" value =\""
        + "not set"
        + "\"/>")
      .appendTo(sourceWidgetSelectionDropDownDiv);
    }
/*
    var sourceWidgetDescriptionDropDownDiv;
    sourceWidgetDescriptionDropDownDiv =
      $("<div style=width:inherit; class=\"sourceWidgetDescriptionDropDownDiv\" id=\"sourceWidgetDescriptionDropDownDiv_"
      + widgetId
      + "\"></div>").appendTo(sourceWidgetSelector);

    if (sourceWidgetId != 'undefined' && sourceWidgetId != undefined && sourceWidgetId != null && data.length > 0) {
      sourceWidgetDescriptionDropDownDiv.append(
        "<p>"
        + findTextAndDescriptionBySourceWidgetId(data, sourceWidgetId).description
        + "</p>");
    }
    else {
      sourceWidgetDescriptionDropDownDiv.append(
        "<p>"
        + "no description - please select a search widget"
        + "</p>");
    }
    */

    sourceWidgetSelector.append("<div class=\"clearfix\"></div>");

    sourceWidgetSelectionDropDown.kendoDropDownList({
      dataSource: data,
      dataTextField: "text",
      dataValueField: "value",
      change: function(e) {
        var value = $("#sourceWidgetSelectionDropDownList_" + widgetId).val();
        var descriptionBox = $("#sourceWidgetDescriptionDropDownDiv_" + widgetId);
        console.log(value);
        descriptionBox.empty();
        descriptionBox.append("<p>" + findSourceWidgetAndDescriptionByValue(data, value).description + "</p>");
      }
    });


		// Role selection
    if (widgetType == "roleforterm") {
			var data = [
        {text: "Broadcaster", value: "1",
          description: "Broadcaster is someone who posts with high daily rate and has a very high following. However he follows very few people, if any at all."},
        {text: "Daily User", value: "2",
          description: "Daily User is someone with middle of the ground stats."},
        {text: "Information Seeker", value: "3",
          description: "Information Seeker is someone who posts very rarely but follows a lot of people."},
        {text: "Information Source", value: "4",
          description: "Information Source is someone who posts a lot, is followed a lot but follows more people than the Broadcaster."},
        {text: "Rare Poster", value: "5",
          description: "RarePoster is somebody who hardly ever posts."}
      ];

			var roleSelectionRoleSelector = $("<div class=\"roleSelectionRoleSelector\"></div>").appendTo(optionsDiv);
			var roleSelectionDropDownDiv = $("<div class=\"roleSelectionDropDownDiv\"></div>").appendTo(roleSelectionRoleSelector);
			roleSelectionDropDownDiv.append("<p>User role to search for:</p><br>");

			// Get value + description out of data!
			var roleSelectionDropDown = $("<input id=\"roleSelectionDropDownList_" + widgetId + "\" value =\""
        + findValueAndDescriptionByRole(data, role).value + "\"/>").appendTo(roleSelectionDropDownDiv);

			var roleDescriptionDropDownDiv = $("<div class=\"roleDescriptionDropDownDiv\" id=\"roleDescriptionDropDownDiv_" + widgetId + "\"></div>").appendTo(roleSelectionRoleSelector);
			roleDescriptionDropDownDiv.append("<p>" + findValueAndDescriptionByRole(data, role).description + "</p>");

			roleSelectionRoleSelector.append("<div class=\"clearfix\"></div>");

			roleSelectionDropDown.kendoDropDownList({
				dataSource: data,
				dataTextField: "text",
				dataValueField: "value",
				change: function(e) {
					var value = $("#roleSelectionDropDownList_" + widgetId).val();
					var descriptionBox = $("#roleDescriptionDropDownDiv_" + widgetId);
					descriptionBox.empty();
					descriptionBox.append("<p>" + findRoleAndDescriptionByValue(data, value).description + "</p>");
				}
		    });
		}



		// TODO bind visibility update here instead of duplicateButton.mouseup
		var showOrHideInput = $("<input type=\"checkbox\" id=\"ifHiddenTwitterPostsByLocationConfiguration_" + widgetId + "\">").appendTo(optionsDiv);

		optionsDiv.append("<p>Hide this widget</p>");

    var buttonsDiv = $("<div class=\"widgetPropertiesControls\"></div>").appendTo(propertiesPopup);

    // Duplicate Button

    var duplicateButton = $("<p id=\"duplicateTwitterPostsByLocationConfiguration_" + widgetId + "\" class=\"duplicateWidgetButton\">Duplicate</p>").appendTo(buttonsDiv);
		duplicateButton.click(function(e){
      var newSourceWidgetId = sourceWidgetSelectionDropDown.val();
      console.log(newSourceWidgetId);
			if (newSourceWidgetId.length < 1) {
				alert('Please enter a widget ID to get input from');
				return false;
			} else {
				parameters.sourceWidgetId = newSourceWidgetId;
        console.log(parameters.sourceWidgetId);

        $.get("/home/widgets/duplicateWidget/do.json", {wId: widgetId, parametersAsString: JSON.stringify(parameters)}, function(result){
					$('.closed-by-escape').hide();
					propertiesPopup.remove();
					loadWidgets();
				} );
			}
		});

    // Cancel Button

    var cancelButton = $("<p id=\"cancelTwitterPostsByLocationConfiguration_" + widgetId + "\" class=\"cancelWidgetButton\">Cancel</p>").appendTo(buttonsDiv);
		cancelButton.mouseup(function(e){
			$('.closed-by-escape').hide();
      propertiesPopup.remove();
			//loadWidgets();
		});


    // Update Button

    var updateButton = $("<p id=\"updateTwitterPostsByLocationConfiguration_" + widgetId + "\" class=\"updateWidgetButton\">Update Widget</p>").appendTo(buttonsDiv);
		updateButton.click(function(e){
      var newSourceWidgetId = sourceWidgetSelectionDropDown.val();
      console.log(newSourceWidgetId);
			if (newSourceWidgetId.length < 1) {
				alert('Please enter a widget ID to get input from');
				return false;
			}
			parameters.sourceWidgetId = newSourceWidgetId;
      console.log("new source widget = " + parameters.sourceWidgetId);


      if (widgetType == "roleforterm") {
        var newRoleId = $("#roleSelectionDropDownList_" + widgetId).val();
        parameters.role = findRoleAndDescriptionByValue(data, newRoleId).text;
      }

			$('.closed-by-escape').hide();

      // update widget parameters
      $.get("/home/widgets/updateWidgetParameters/do.json", {wId: widgetId, newParametersValue: JSON.stringify(parameters)}, function(a){
        propertiesPopup.remove();
        loadWidgets();
      });

			if (showOrHideInput.is(":checked")) {
				$.get("/home/widgets/hideWidget/do.json", {wId: widgetId} );
				myDiv.remove();
				propertiesPopup.remove();
			}


		});
		propertiesPopup.draggable();
		popupBackground(propertiesPopup.attr('id'));


	//return propertiesControl;
}

function findSourceWidgetAndDescriptionByValue(data, valueId) {
	var result = null;
	$.each(data, function(counter, dataEntry){
		if (dataEntry.value == valueId) {
			result = dataEntry;
		}
	});
	return result;
}
function findTextAndDescriptionBySourceWidgetId(data, sourceWidgetId) {
	var result = null;
	$.each(data, function(counter, dataEntry){
		if (dataEntry.value == sourceWidgetId) {
			result = dataEntry;
		}
	});
	return result;
}

function findValueAndDescriptionBySourceWidgetId(data, sourceWidgetId) {
	var result = null;
	$.each(data, function(counter, dataEntry){
		if (dataEntry.text == sourceWidgetId) {
			result = dataEntry;
		}
	});
	return result;
}