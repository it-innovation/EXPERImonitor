//window.lang = new jquery_lang_js();
// peerdindex api key: 09355f288dcba68de7adb0e8c4f0fffd

/* old geoplugin
var lat = geoplugin_latitude();
var lon = geoplugin_longitude();
var geo_city = geoplugin_city();
var geo_countryName = geoplugin_countryName();
*/

$(document).ready(

  function() {
    $.ajaxSetup({
      cache: false
    });

	getCurrentLocation(function() {
		console.log("Location defined: ");
		console.log(JSON.stringify(currLocation));
	});

    // Home link
    $("#invislink").click(function() {
      window.location = "./index.html";
    });

    $("#add-new-location-dialog-holder").draggable();

	/* Old location widget, using geoplugin
    // Save current location as new location
    $("#newLocationNameSaveButton").mouseup(function(e){
      var locationAddress = geo_city + ", " + geo_countryName;
      var locationName = $("#newLocationName").val();

      if (locationName.length > 0) {
        console.log(locationName + " | " + locationAddress + " | " + lat + " | " + lon);
        $.getJSON("/home/locations/addNewLocation/do.json",
        {
          locationName:locationName,
          locationAddress:locationAddress,
          lat:lat,
          lon:lon
        }, function(data) {
          $("#name-location-dialog-holder, #dialog-overlay, #add-new-location-dialog-holder").hide();

          $.each($(".hasMapToRefresh"), function(containerCount, container) {
            var containerDom = document.getElementById($(this).attr('id'));
            refreshLocations(containerDom.containerId);
          });
        });
      } else {
        alert("Please enter name for your new location");
      }
    });
	*/

    $("#addNewLocationName").bind('keypress', function(e){
      //				e.preventDefault();
      if (e.keyCode == 13) {
        $("#addNewLocationDialogNameSaveButton").trigger('mouseup');
      }
    });

    // TODO make it wider so that there is space for more widgets?
    // TODO turn into kendo window?
    // TODO just make movable?
    $("#username").click(function(e){
      var userdetails = jQuery.data($(this)[0], "userdetails");
      //				console.log(userdetails);
      var userSettingsPopup = $("<div id=\"userSettings\" class=\"closed-by-escape\"></div>").appendTo("#wrapper");
      userSettingsPopup.draggable();
      userSettingsPopup.append("<h2>Edit my settings</h2>");

      userSettingsPopup.append("<h3>My Details</h3>");

      var mySettingsWrapper = $("<div class=\"userSettingsWrapper\"></div>").appendTo(userSettingsPopup);
      mySettingsWrapper.append("<p class=\"myDetailLabel\">Username:</p>");
      mySettingsWrapper.append("<input type=\"text\" value=\"" + userdetails["username"] + "\" class=\"myDetailInput\" disabled=\"disabled\">");
      mySettingsWrapper.append("<p class=\"myDetailLabel\" id=\"newUserFullnameLabel\">Full name:</p>");
      mySettingsWrapper.append("<input type=\"text\" value=\"" + userdetails["name"] + "\" class=\"myDetailInput\" id=\"newUserFullname\">");
      mySettingsWrapper.append("<p class=\"myDetailLabel\" id=\"newUserOrganisationLabel\">Organisation:</p>");
      mySettingsWrapper.append("<input type=\"text\" value=\"" + userdetails["organisation"] + "\" class=\"myDetailInput\" id=\"newUserOrganisation\">");
      mySettingsWrapper.append("<p class=\"myDetailLabel\">New password:</p>");
      mySettingsWrapper.append("<input type=\"password\" value=\"\" class=\"myDetailInput\" id=\"newUserPassword\">");
      mySettingsWrapper.append("<div class=\"clearfix\"></div>");

      userSettingsPopup.append("<hr>");

      userSettingsPopup.append("<h3>My Widgets</h3>");
      var myWidgetsWrapper = $("<div class=\"userSettingsWrapper\"></div>").appendTo(userSettingsPopup);

      $.get("/home/widgets/getWidgetsForDefaultWidgetSet/do.json", function(widgets){
        var checkedValue;
        var theInput;
        var term = undefined;
        var role = undefined;
        $.each(widgets, function(widgetCounter, widget) {
          console.log(widget);
          if (widget["isVisible"] == 0)
            checkedValue = "checked=\"checked\"";
          else
            checkedValue = "";
          var widgetTitle = "Undefined Widget";
          if (widget["name"] != null || widget["name"] != undefined) {
            widgetTitle = widget["name"];
          }

          if (widget["parametersAsString"] != null) {
            if (widget["parametersAsString"].length > 0) {
              var parameters = null;
              parameters = jQuery.parseJSON(widget["parametersAsString"]);
              if (parameters != null) {
                if (parameters.term != null) {
                  term = parameters.term;
                }
                if  (parameters.role != null) {
                  role = parameters.role;
                }

                //if (role == undefined)
                //myWidgetsWrapper.append("<p class=\"myWidgetsLabel\">" + widget["name"] + ": " + term + "</p>");
                widgetTitle = widget["name"] + ": " + term;
              //else
              //myWidgetsWrapper.append("<p class=\"myWidgetsLabel\">" + role + "s for: " + term + "</p>");
              //widgetTitle = widget["name"] + " " + role + "s for: " + term;
              }
            }
            else {
            //myWidgetsWrapper.append("<p class=\"myWidgetsLabel\">" + widget["name"] + "</p>");
            }

          }
          myWidgetsWrapper.append("<p class=\"myWidgetsLabel\">" + widgetTitle + "</p>");

          theInput = $("<input type=\"checkbox\" value=\"\" class=\"myWidgetsInput\" " + checkedValue  + ">").appendTo(myWidgetsWrapper);
          var inputDom = theInput[0];
          jQuery.data(inputDom, "data", widget["id"]);
          myWidgetsWrapper.append("<p class=\"myWidgetsInputLabel\">show</p>");

        });
      });

      userSettingsPopup.append("<div class=\"clearfix\"></div>");
      var mySettingsControlsWrapper = $("<div class=\"userSettingsControlsWrapper\"></div>").appendTo(userSettingsPopup);
      var mySettingsControlsCancel = $("<p id=\"userSettingsCancelButton\"><span lang=\"en\">Cancel</span></p>").appendTo(mySettingsControlsWrapper);
      var mySettingsControlsSave = $("<p id=\"userSettingsSaveButton\"><span lang=\"en\">Save my settings</span></p>").appendTo(mySettingsControlsWrapper);

      mySettingsControlsCancel.click(function(e){
        $('.closed-by-escape').hide();
        userSettingsPopup.remove();
      });

      mySettingsControlsSave.click(function(e){
        console.log("Saving new user settings");

        // Update user details
        // fullName, organisation, newPassword, Integer.parseInt(changePassword)
        var fullName = $("#newUserFullname").val();
        var organisation = $("#newUserOrganisation").val();
        var newPassword = $("#newUserPassword").val();
        var changePassword = 0;

        if (fullName.length < 1) {
          $("#newUserFullnameLabel").css('color', 'red');
          return false;
        }

        if (organisation.length < 1) {
          $("#newUserOrganisationLabel").css('color', 'red');
          return false;
        }

        if (newPassword.length > 0) {
          changePassword = 1;
        }

        $.get("/home/savePolicymakerInfo/do.json", {
          fullName: fullName,
          organisation : organisation,
          newPassword : newPassword,
          changePassword : changePassword
        }, function(){
          $('.closed-by-escape').hide();
          userSettingsPopup.remove();
          userdetails["name"] = fullName;
          userdetails["organisation"] = organisation;
          jQuery.data($("#username")[0], "userdetails", userdetails);
          $("#username").text(fullName);
          loadWidgets(false);
        } );

        var wId;
        // TODO: make this recursive? Only save changed ones?
        // Have to do it here because of Cancel option
        $.each(myWidgetsWrapper.find("input"), function(inputCounter, theInput){
          wId = jQuery.data(theInput, "data");
          if ($(this).attr('checked') != undefined) {
            //							console.log("Showing widget: [" + wId + "]");
            $.get("/home/widgets/showWidget/do.json", {
              wId: wId
            } );
          } else {
            //							console.log("Hiding widget: [" + wId + "]");
            $.get("/home/widgets/hideWidget/do.json", {
              wId: wId
            } );
          }
        });

      });

      popupBackground(userSettingsPopup.attr('id'));
    });

    // Create new widget - select from a set of templates
    $("#createWidget").click(function(e){


      var createWidgetPopup = jQuery("<div id=\"userSettings\" class=\"closed-by-escape\"></div>").appendTo("#wrapper");

      createWidgetPopup.draggable();

      createWidgetPopup.append("<h3>Available Widget Types</h3>");

      var availableWidgetsWrapper = $("<div class=\"userSettingsWrapper\"></div>").appendTo(createWidgetPopup);


      $.get("/home/widgets/getTemplateWidgets/do.json", function(widgets){
        var checkedValue;
        var theInput;

        availableWidgetsWrapper.append("<p class=\"myWidgetsLabel\"><b>Search Widgets</b></p>");
        $.each(widgets, function(widgetCounter, widget) {

          var parameters = jQuery.parseJSON(widget["parametersAsString"]);
          var widgetCategory =  widget["widgetCategory"];
          if (widgetCategory == "search") {

            checkedValue = "";

            availableWidgetsWrapper.append("<p class=\"myWidgetsLabel\">" + widget["name"] + "</p>");

            // Radio button as you should only be able to create one widget at a time.
            // This is becuase the default behaviour is to pop up the settings window immediately.
            theInput = $(
              "<input name=createWidgetRadio type=\"radio\" value=\"\" class=\"myWidgetsInput\" "
                + checkedValue  + ">").appendTo(availableWidgetsWrapper);

            var inputDom = theInput[0];
            jQuery.data(inputDom, "widgetId", widget["id"]);
            jQuery.data(inputDom, "widgetParameters", parameters);

            availableWidgetsWrapper.append("<p class=\"myWidgetsInputLabel\">Create</p>");
          }

        });

        availableWidgetsWrapper.append("<p class=\"myWidgetsLabel\"><b>WeGov Analysis Widgets</b></p>");
        $.each(widgets, function(widgetCounter, widget) {

          var parameters = jQuery.parseJSON(widget["parametersAsString"]);
          var widgetCategory =  widget["widgetCategory"];
          if (widgetCategory == "wegov_analysis") {

            checkedValue = "";

            availableWidgetsWrapper.append("<p class=\"myWidgetsLabel\">" + widget["name"] + "</p>");

            // Radio button as you should only be able to create one widget at a time.
            // This is becuase the default behaviour is to pop up the settings window immediately.
            theInput = $("<input name=createWidgetRadio type=\"radio\" value=\"\" class=\"myWidgetsInput\" " + checkedValue  + ">").appendTo(availableWidgetsWrapper);

            var inputDom = theInput[0];

            jQuery.data(inputDom, "widgetId", widget["id"]);
            jQuery.data(inputDom, "widgetParameters", parameters);

            availableWidgetsWrapper.append("<p class=\"myWidgetsInputLabel\">Create</p>");
          }

        });
        availableWidgetsWrapper.append("<p class=\"myWidgetsLabel\"><b>External Analysis Widgets</b></p>");
        $.each(widgets, function(widgetCounter, widget) {

          var parameters = jQuery.parseJSON(widget["parametersAsString"]);
          var widgetCategory =  widget["widgetCategory"];
          if (widgetCategory == "external_analysis") {

            checkedValue = "";

            availableWidgetsWrapper.append("<p class=\"myWidgetsLabel\">" + widget["name"] + "</p>");

            // Radio button as you should only be able to create one widget at a time.
            // This is becuase the default behaviour is to pop up the settings window immediately.
            theInput = $("<input name=createWidgetRadio type=\"radio\" value=\"\" class=\"myWidgetsInput\" " + checkedValue  + ">").appendTo(availableWidgetsWrapper);

            var inputDom = theInput[0];
            jQuery.data(inputDom, "widgetId", widget["id"]);
            jQuery.data(inputDom, "widgetParameters", parameters);

            availableWidgetsWrapper.append("<p class=\"myWidgetsInputLabel\">Create</p>");
          }

        });

      });


      createWidgetPopup.append("<div class=\"clearfix\"></div>");

      var createWidgetControlsWrapper = $("<div class=\"userSettingsControlsWrapper\"></div>").appendTo(createWidgetPopup);
      var createWidgetControlsCancel = $("<p id=\"userSettingsCancelButton\"><span lang=\"en\">Cancel</span></p>").appendTo(createWidgetControlsWrapper);
      var createWidgetControlsSave = $("<p id=\"userSettingsSaveButton\"><span lang=\"en\">Save</span></p>").appendTo(createWidgetControlsWrapper);

      createWidgetControlsCancel.click(function(e){
        $('.closed-by-escape').hide();
        createWidgetPopup.remove();
      });

      createWidgetControlsSave.click(function(e){

        var newWidgetId;

        console.log("Saving new user settings");

        $.each(availableWidgetsWrapper.find("input"), function(inputCounter, theInput){
          var widgetId = jQuery.data(theInput, "widgetId");
          var widgetParameters = jQuery.data(theInput, "widgetParameters");
          if ($(this).attr('checked') != undefined) {
            console.log("Duplicating Widget: [" + widgetId + "]");

            $.ajax({
              url:
              "/home/widgets/duplicateWidgetToCallingUserDefaultSet/do.json?wId=" + widgetId
              + "&parametersAsString=" + JSON.stringify(widgetParameters),
              //"/home/widgets/getWidgetData/do.json?wId=" + , { wId: wId },
              type: 'get',

              success:function(newWidgetIdReturn){
                newWidgetId = newWidgetIdReturn;
                console.log("New Widget ID = " + newWidgetId);
                $('.closed-by-escape').hide();
                createWidgetPopup.remove();

                //console.log("New Widget ID = " + newWidgetId);
                var settingsPopup = jQuery("<div id=\"userSettings2\" class=\"closed-by-escape\"></div>").appendTo("#wrapper");
                showWidgetSettingsWindowSJTWidgetId(settingsPopup, newWidgetId);

              }
            });

          }
        });

      });

      popupBackground(createWidgetPopup.attr('id'));

    });


    // Get widgets
    loadWidgets(true);

  });

/*
 * FUNCTIONS
 */

function compareWidgets(a, b) {
  if (a["columnOrderNum"] < b["columnOrderNum"]) return -1;
  if (a["columnOrderNum"] > b["columnOrderNum"]) return 1;
  return 0;
}

function loadWidgets(loadFB) {
	if (loadFB) {
		console.log("Calling fbLoad()...");
		fbLoad(loadWidgetsFbReady);
	}
	else {
		loadWidgetsFbReady();
	}
}

function loadWidgetsFbReady() {
    console.log("Loading widgets");
    $("#widgets").empty();
    $("#widgets").append("<p>Loading widgets, please wait...</p>");

    $.getJSON("/home/widgets/getVisibleWidgetsForDefaultWidgetSet/do.json",
    {}, function(data) {
        console.log(data);
        //				console.log(data.sort(compareWidgets));
        var widgetType;
        $("#widgets").empty();
        $("#widgets").append("<div class=\"column ui-sortable\" id=\"columnleft\">");
        $("#widgets").append("<div class=\"column ui-sortable\" id=\"columnmid\">");
        $("#widgets").append("<div class=\"column ui-sortable\" id=\"columnright\">");
        $("#widgets").append("<div class=\"clearfix\"></div>");

        $.each(data.sort(compareWidgets), function(index, widget) {
            //					console.log(widget);
            widgetType = widget["type"];

            if (widgetType == "location") {
                getCurrentLocation(function() {
                    addLocationMap(widget);
                });
            } else if (widgetType == "userroles") {
                //addUserRolesWidget(widget);
                addAnalysisUsingStoredPostData(widget);
            }
            else if (widgetType == "retweets") {
                addLineAnalysisWidget(widget);
            }
            else if (widgetType == "twitterLocal") {
                //addTwitter3PostsByLocationContainingTerm(widget, lat, lon, 20, "recent");
                getCurrentLocation(function() {
                    addTwitter3PostsContainingTerm(widget, "recent", currLocation["lat"], currLocation["lon"], 5, currLocation["locationAddress"]);
                });
            }
            else if (widgetType == "peerindex") {
                addPeerindexWidget(widget);
            }
            else if (widgetType == "addmorewidgets") {
                addMoreWidgetsWidget(widget);
            }
            else if (widgetType == "allactivities") {
                addActivitiesWidget("all", widget);
            }
            else if (widgetType == "groupposts") {
                addGroupPostsWidget(widget);
            }
            else if (widgetType == "groupposttopicanalysis") {
                addGroupPostTopicAnalysisWidget(widget);
            }
            else if (widgetType == "grouppostcomments") {
                addGroupPostCommentsWidget(widget);
            }
            /*
					else if (widgetType == "latestgroupposttopicanalysis") {
						addLatestGroupPostTopicAnalysisWidget(widget);
					}
*/
            else if (widgetType == "trending") {
                addTrendingWidget(widget);
            }
            else if (widgetType == "roleforterm") {
                //addRoleForTermWidget(widget);
                addAnalysisUsingStoredPostData(widget);
            }
            else if (widgetType == "twitterbasic") {
                addTwitter3PostsContainingTerm(widget, "recent");
            }
            else if (widgetType == "topics_from_database") {
                addAnalysisUsingStoredPostData(widget);
            }

            else {
            //addEmptyWidget(widget);
            }
        });

        // Make widgets
        $( ".column" ).sortable({
            connectWith: ".column",
            cursor: 'move',
            revert: true,
            start: function(event, ui) {
                $(".column").css("padding-bottom", "50px");
            },
            stop: function(event, ui) {
                $(".column").css("padding-bottom", "10px");
            }
        //					update: function(event, ui) {
        //						var columnName = $(this).attr('id');
        //						console.log("Updating db now, column: " + columnName);
        //						var newOrder = $(this).sortable('toArray').toString();
        //						console.log(newOrder);
        //						$.get("/home/widgets/updateWidgetPositions/do.json", {columnName:columnName, newOrder:newOrder});
        //					}
        });

        $( ".column" ).bind("sortupdate", function(event, ui){
            var columnName = $(this).attr('id');
            console.log("Updating db now, column: " + columnName);
            var newOrder = $(this).sortable('toArray').toString();
            console.log(newOrder);
            $.get("/home/widgets/updateWidgetPositions/do.json", {
                columnName:columnName,
                newOrder:newOrder
            });
        });

        $( ".portlet" ).addClass( "ui-widget ui-widget-content ui-helper-clearfix ui-corner-all" )
        .find( ".portlet-header" )
        .addClass( "ui-widget-header ui-corner-all" )
        .prepend( "<span class='ui-icon ui-icon-minusthick'></span>")
        .end()
        .find( ".portlet-content" );

        $( ".portlet-header .ui-icon" ).click(function() {
            $( this ).toggleClass( "ui-icon-minusthick" ).toggleClass( "ui-icon-plusthick" );
            $( this ).parents( ".portlet:first" ).find( ".portlet-content" ).toggle();
        });

        $( ".column" ).disableSelection();

    //				$.each($( ".column" ), function(counter, column){
    //					console.log("Initial order for column " + $(this).attr('id') + ": " + $(this).sortable('toArray').toString());
    //				});

    });
}

function initWidget(widget) {
  return $("<div class=\"portlet\" id=\"" + widget["id"] + "\"></div>").appendTo("#" + widget["columnName"]);
}

function initSearchWidget(widget) {
  return $("<div class=\"portlet portlet_search\" id=\"" + widget["id"] + "\"></div>").appendTo("#" + widget["columnName"]);
}

function initAnalysisWidget(widget) {
  return $("<div class=\"portlet portlet_analysis\" id=\"" + widget["id"] + "\"></div>").appendTo("#" + widget["columnName"]);
}


//addLocationMap moved to location.js



function addLineAnalysisWidget(widget) {
  var myDiv = initWidget(widget);
  $("<h2 class=\"widgetHeader\">" + widget["name"] + "</h2>")
  .appendTo(myDiv);
  var myChart = $("<div id=\"linechart_" + widget["id"] + "\" class=\"piechart\"></div>")
  .appendTo(myDiv);

  $("<p class=\"locationDecription\">Based on your 1332 posts</p>").appendTo(
    myDiv);

  var line1 = [ [ '2011-09-30 4:00PM', 4 ], [ '2011-10-30 4:00PM', 6.5 ],
  [ '2011-11-30 4:00PM', 5.7 ], [ '2011-12-30 4:00PM', 9 ],
  [ '2012-01-30 4:00PM', 8.2 ] ];
  var plot1 = $.jqplot(myChart.attr('id'), [ line1 ], {
    // title:'Default Date Axis',
    axes : {
      xaxis : {
        renderer : $.jqplot.DateAxisRenderer
      }
    },
    series : [ {
      lineWidth : 4,
      markerOptions : {
        style : 'square'
      }
    } ]
  });

}

function refreshLocations(id) {
  var myContainer = $("#mylocationsContainer_" + id);
  //	console.log(myContainer);
  myContainer.empty();
  myContainer.parent().find("p.locationDecription").text("");
  $.getJSON("/home/locations/getLocationsForPM/do.json",
  {}, function(data) {
    //				console.log("Here are my locations:");
    //				console.log(data);
    if (data != null) {

      myContainer.append("<div id=\"gmaps_" + id + "\" class=\"gmap\"></div>");

      var myOptions = {
        mapTypeId : google.maps.MapTypeId.ROADMAP,
        disableDefaultUI : true,
        draggable: false,
        scrollwheel: false
      };

      var map = new google.maps.Map(document.getElementById("gmaps_" + id), myOptions);
      var bounds = new google.maps.LatLngBounds();

      var myLatLng;
      var locationId;
      var locationAddress;
      var locationName;
      var locationLat;
      var locationLon;
      $.each(data, function(locationCounter, location){

        locationId = location["id"];
        locationAddress = location["locationAddress"];
        locationName = location["locationName"];
        locationLat = location["lat"];
        locationLon = location["lon"];

        console.log("Processing location: " + locationName + " | " + locationAddress + " | " + locationLat + " | " + locationLon);

        myLatLng = new google.maps.LatLng(locationLat, locationLon);
        var marker = new google.maps.Marker({
          position : myLatLng,
          map : map,
          animation : google.maps.Animation.DROP
        });

        var infoWindow = new google.maps.InfoWindow({
          maxWidth: 250
        });
        var infoWindowContents = $(
          "<p><b>" + locationName + '</b><br />' + locationAddress
            + "<br/><span title=\"" + locationId
            + "\" class=\"removelocation aslink\">Remove this location</span></p>");

        var html = infoWindowContents.html();
        console.log(html);
        google.maps.event.addListener(marker, 'click', function() {
          infoWindow.setContent(html);
          infoWindow.open(map, marker);
          var removeLocationLink = $("span.removelocation:visible");
          var removeLocationLinkId = removeLocationLink.attr('title');

          removeLocationLink.mouseup(function(e){
            console.log("Removing location with id: " + removeLocationLinkId);
            $.getJSON("/home/locations/removeLocation/do.json",
            {
              locationId:removeLocationLinkId
            }, function(response) {
              refreshLocations(id);
            });
          });
        });

        bounds.extend(marker.position);
        map.fitBounds(bounds);

        google.maps.event.addListener(map, 'click', function() {
          infoWindow.close();
          map.fitBounds(bounds);
          if (data.length == 1) {
            map.setZoom(8);
          }
        });

        google.maps.event.addListener(infoWindow, 'closeclick', function() {
          map.fitBounds(bounds);
          if (data.length == 1) {
            map.setZoom(8);
          }
        });

      });

      if (data.length == 1) {
        console.log("Setting zoom level to 8");
        map.setZoom(8);
        myContainer.parent().find("p.locationDecription").text("You have 1 location saved");
      } else {
        myContainer.parent().find("p.locationDecription").text("You have " + data.length + " locations saved");
      }

    } else {
      myContainer.append("<p class=\"middleTextWidgetWithHeaderOn\">You haven't saved any locations yet. <span class=\"aslink\">Add one now?</span></p>");
      myContainer.find("span.aslink").mouseup(function(e){
        $("#addNewLocationControl").trigger('mouseup');
      });
    }
  });
}

function addActivitiesWidget(whichActivities, widget) {
  var myDiv = $("<div class=\"widgetFull\" id=\"" + widget["id"] + "\"></div>").appendTo("#" + widget["columnName"]);

  var myHeader;
  if (whichActivities == "all") {
    myHeader = $("<h2 class=\"widgetHeader\">" + widget["name"] + "</h2>")
    .appendTo(myDiv);
  } else {
    myHeader = $("<h2 class=\"widgetHeader\">My Activities</h2>").appendTo(
      myDiv);
  }

  var myContainer = $("<div class=\"widgetContainer\"></div>")
  .appendTo(myDiv);

  myContainer.kendoGrid({
    dataSource : {
      type : "json",
      serverPaging: false,
      serverSorting: false,
      transport : {
        read : "/home/getactivities/do.json"
      },
      schema : {
        data: function(result) {
          return result.data || result;
        },
        total: function(result) {
          return result.total || result.length || 0;
        }
      },
      sort: {
        field: "whenCreated",
        dir : "desc"
      },
      pageSize: 10
    },
    columns : [ {
      title : "Name",
      field : "name"
    }, {
      title : "Status",
      field : "status",
      width : "80px"
    }, {
      title : "Created on",
      field : "whenCreated",
      width : "160px"
    }, ],
    // height: auto,
    //		detailInit : detailInit,
    //		dataBound : function() {
    //			this.expandRow(this.tbody.find("tr.k-master-row").first());
    //		},
    filterable : true,
    sortable : true,
    pageable : true,
    groupable : true,
    selectable : "row"
  });
}
// TODO: get runs only for selected activity
function detailInit(e) {
  $("<div/>").appendTo(e.detailCell).kendoGrid({
    dataSource : {
      type : "json",
      serverPaging: false,
      serverSorting: false,
      transport : {
        read : "/home/getruns/do.json"
      },
      schema : {
        data: function(result) {
          return result.data || result;
        },
        total: function(result) {
          return result.total || result.length || 0;
        }
      },
      filter : {
        field : "activityid",
        operator : "eq",
        value : e.data.id
      },
      sort: {
        field: "whenStarted",
        dir : "desc"
      },
      pageSize: 10
    },
    columns : [ {
      title : "Name",
      field : "name"
    }, {
      title : "Status",
      field : "status",
      width : "80px"
    }, {
      title : "Started on",
      field : "whenStarted",
      width : "160px"
    }, {
      title : "Finished on",
      field : "whenFinished",
      width : "160px"
    } ],
    scrollable : false,
    sortable : true,
    pageable : true
  // columns: [ "OrderID", "ShipCountry", "ShipAddress", "ShipName" ]
  });
}


function addTrendingWidget(widget) {
  var myDiv = initSearchWidget(widget);
  var widgetId = widget["id"];
  var parameters = jQuery.parseJSON(widget["parametersAsString"]);
  var searchTerms = parameters.term;

  var widgetHeaderDiv = $("<div class=\"searchWidgetHeaderDiv\"></div>").appendTo(myDiv);
  $("<img class=\"widgetLogo\" src=\"img/twitter_newbird_boxed_whiteonblue.png\" alt=\"Source: Twitter\"/>").appendTo(widgetHeaderDiv);
  var widgetHeaderTextDiv = $("<div class=\"widgetHeaderTextDiv\"></div>").appendTo(widgetHeaderDiv);

  var myHeader = $("<a id=\"myHeader_" + widgetId + "\" class=\"nonClickableWidgetHeader\">" + widget["name"] + ": " + searchTerms +  "</a>");

  myHeader.appendTo(widgetHeaderTextDiv);

  widgetHeaderDiv.append('<div class="clearfix"></div>');


  var contentColour = parameters.contentColour;
  if (contentColour == undefined || contentColour == 'undefined' ) {
    contentColour = "gray";
  }

  var myContainer = $("<div style=background-color:" + contentColour + "; class=\"widgetContent\"></div>").appendTo(myDiv);

  var listOfTrendsDiv = $("<div id=\"listOfTrendsDiv_" + widgetId +
    "\" class=\"listOfTrendsDiv\"></div>").appendTo(myContainer);

  listOfTrendsDiv.append("<p>Getting location information for " + searchTerms + "</p>");

  var footerText = $("<p class=\"locationDecription\"></p>").appendTo(myDiv);

  //
  // get woeid: http://where.yahooapis.com/v1/places.q%28%27world%27%29?appid=GYknAX_V34HWU0aG9RFlvm.WTPBB7QlC9VNwYpo_JQc3MFJnNpmA2l17tut3pR02r6gCig-&format=json
  // places - place - woeid
  // Yahoo doesn't support https!
  var yahooQuery = "http://where.yahooapis.com/v1/places.q(" + searchTerms + ")?appid=GYknAX_V34HWU0aG9RFlvm.WTPBB7QlC9VNwYpo_JQc3MFJnNpmA2l17tut3pR02r6gCig-&format=json&callback=?";
  $.ajax({
    url:yahooQuery,
    dataType: 'json',
    type: 'GET',
    contentType: "application/json; charset=utf-8",
    error: function(){
      listOfTrendsDiv.append("<p>Yahoo never heard of '" + searchTerms + "'. Try again?</p>");
    },
    success: function(data) {
      //			console.log(data);
      if (data["places"]["count"] > 0) {
        var thePlace = data["places"]["place"][0];
        console.log(thePlace);

        listOfTrendsDiv.append("<p>Found " + thePlace["name"] + ", " + thePlace["placeTypeName"] + "</p>");
        listOfTrendsDiv.append("<p>Getting trends from Twitter</p>");

        myHeader.text(widget["name"] + ": " + searchTerms);

        if (thePlace["name"] != thePlace["country"])
          footerText.text("Location: " + thePlace["name"] + ", " + thePlace["country"] + " (" + thePlace["placeTypeName"] + ")");
        else
          footerText.text("Location: " + thePlace["name"] + " (" + thePlace["placeTypeName"] + ")");

        // get trending: https://api.twitter.com/1/trends/968019.json
        var woeid = thePlace["woeid"];
        var twitterQuery = "https://api.twitter.com/1/trends/" + woeid + ".json?callback=?";

        $.ajax({
          url:twitterQuery,
          dataType: 'json',
          type: 'GET',
          timeout: 5000,
          contentType: "application/json; charset=utf-8",
          error: function(jqXHR, textStatus, errorThrown){
            console.log ("Error");
            listOfTrendsDiv.append("<p>Twitter did not return any trends. Most likely this is because only major cities and countries are supported.</p>");
          },
          success: function(twitterData) {
            console.log(twitterData);
            var trends = twitterData[0]["trends"];
            if (trends.length > 0) {
              listOfTrendsDiv.empty();
              var url;
              var trendName;
              $.each(trends, function(index, trend){
                trendName = trend["name"];
                url = trend["url"];
                listOfTrendsDiv.append("<p class=\"trend\"><a target=\"_blank\" href=\"" + url + "\">" + trendName + "</a></p>");
              });
            } else {
              listOfTrendsDiv.append("<p>No trends found.</p>");
            }
          }

        });

      } else {
        listOfTrendsDiv.append("<p>Yahoo never heard of '" + searchTerms + "'. Try again?</p>");
      }
    }
  });



  var widgetFooter = $("<div class=\"widgetFooter\"></div>").appendTo(myDiv);
  var settingsButton = $("<p class=\"widgetSettings\">Settings</p>").appendTo(widgetFooter);
  settingsButton.click(function(e){
    showWidgetSettingsWindowSJT(myDiv, widget);
  });
  /*
	var refreshButton = $("<p class=\"widgetRefresh\">Refresh Data</p>").appendTo(widgetFooter);
  refreshButton.click(function(e){
    addTrendingWidget(widget)
  });
  */
  widgetFooter.append('<div class="clearfix"></div>');

}

function addMoreWidgetsWidget(widget) {
  var myDiv = initWidget(widget);

  $("<p class=\"middleTextWidget\">Add <span class=\"aslink\">more widgets</span></p>")
  .appendTo(myDiv);

}

function addEmptyWidget(widget) {
  var myDiv = initWidget(widget);
  $("<h2 class=\"widgetHeader\">Empty post with header: " + widget["name"] + "</h2>").appendTo(myDiv);

}

function closeClosedByEscape() {
  $('.closed-by-escape').hide();
}


/*
 *
 *
 * FUNCTIONS
 *
 *
 */


function clearDisplayArea() {
  $("#displayArea").empty();
}

function showHeader(headerContents) {
  var header = $("<h1 id=\"displayAreaHeader\"></h1>").appendTo(
    "#displayArea");
  header.html(headerContents);
  header.show();
}

function showStatsHeader() {
  showHeader("21 <span class=\"underline\">activities</span> with 15 <span class=\"underline\">searches</span> from 6 <span class=\"underline\">analysis</span>");
}

function findRoleAndDescriptionByValue(data, valueId) {
  var result = null;
  $.each(data, function(counter, dataEntry){
    if (dataEntry.value == valueId) {
      result = dataEntry;
    }
  });
  return result;
}
function findValueAndDescriptionByRole(data, roleValue) {
  var result = null;
  $.each(data, function(counter, dataEntry){
    if (dataEntry.text == roleValue) {
      result = dataEntry;
    }
  });
  return result;
}

/*
 * GENERIC WIDGET SETTINGS POPUP
 */

function showWidgetSettingsWindowSJTWidgetId(myDiv, widgetId) {

  var widget;

  $.ajax({
    url:
    "/home/widgets/getWidget/do.json?wId=" + widgetId,
    type: 'get',

    success:function(widgetResponse){
      if (widgetResponse != undefined) {
        console.log (widgetResponse);
        widget = widgetResponse;
        showWidgetSettingsWindowSJT(myDiv, widget);

      } else {
        console.log ("No data found.");
        return null;
      }
    },
    async: false
  });
}

function showWidgetSettingsWindowSJT(myDiv, widget) {

  var widgetCategory = widget["widgetCategory"];

  if (widgetCategory == "wegov_analysis") {
    showSettingsForAnalysisUsingWidgetData(myDiv, widget);
  }
  else {
    showWidgetDefaultSettingsWindowSJT(myDiv, widget);
  }
}

function showWidgetDefaultSettingsWindowSJT(myDiv, widget) {
  var widgetId = widget["id"];
  var parameters = jQuery.parseJSON(widget["parametersAsString"]);
  var inputLabelText = widget["labelText"];
  var term = null;
  var role = null;
  var sourceWidgetId = null;
  var contentColour = undefined;
  var termRequired = false;

  if (parameters != null | parameters != undefined) {
    term = parameters.term;
    role = parameters.role;
    sourceWidgetId = parameters.sourceWidgetId;
    contentColour = parameters.contentColour;
  }
  else {
    // initialise parameters with some default values
    parameters = jQuery.parseJSON("{\"term\":\"none\",\"contentColour\":\"#FAEBD7\"}");
  }

  // Only enable term input if the label is specified in the arguments
  if (inputLabelText != null) {
    termRequired = true;
  }


  if (contentColour == undefined || contentColour == 'undefined' ) {
    contentColour = "gray";
  }


  var propertiesPopup = $("<div class=\"configureTwitterPostsByLocation closed-by-escape\" id=\"configureTwitterPostsByLocation_" + widgetId + "\"></div>").appendTo("#wrapper");
  $("<h2>Configure widget</h2>").appendTo(propertiesPopup);
  var deleteButton = $("<p id=\"deleteTwitterPostsByLocationConfiguration_" + widgetId + "\" class=\"deleteWidgetButton\">Delete Widget</p>").appendTo(propertiesPopup);
  deleteButton.click(function(e){
    $.get("/home/widgets/deleteWidget/do.json", {
      wId: widgetId
    }, function(result){
      $('.closed-by-escape').hide();
      propertiesPopup.remove();
      myDiv.remove();
    } );
  });

  var optionsDiv = $("<div class=\"widgetPropertiesOptions\"></div>").appendTo(propertiesPopup);

  // Only enable term input if the label is specified in the arguments
  if (termRequired == true) {
    optionsDiv.append("<p>" + inputLabelText + "</p>");
    var newTermInput = $("<input type=\"text\" value=\"" + term + "\" id=\"newTermTwitterPostsByLocationConfiguration_" + widgetId + "\">").appendTo(optionsDiv);
    optionsDiv.append("<br>");

  }

  // Role selection
  if (role != undefined) {
    var data = [
    {
      text: "Broadcaster",
      value: "1",
      description: "Broadcaster is someone who posts with high daily rate and has a very high following. However he follows very few people, if any at all."
    },

    {
      text: "Daily User",
      value: "2",
      description: "Daily User is someone with middle of the ground stats."
    },

    {
      text: "Information Seeker",
      value: "3",
      description: "Information Seeker is someone who posts very rarely but follows a lot of people."
    },

    {
      text: "Information Source",
      value: "4",
      description: "Information Source is someone who posts a lot, is followed a lot but follows more people than the Broadcaster."
    },

    {
      text: "Rare Poster",
      value: "5",
      description: "RarePoster is somebody who hardly ever posts."
    }
    ];

    var roleSelectionRoleSelector = $("<div class=\"roleSelectionRoleSelector\"></div>").appendTo(optionsDiv);
    var roleSelectionDropDownDiv = $("<div class=\"roleSelectionDropDownDiv\"></div>").appendTo(roleSelectionRoleSelector);
    roleSelectionDropDownDiv.append("<p>User role to search for:</p><br>");

    // Get value + description out of data!
    var roleSelectionDropDown = $(
      "<input id=\"roleSelectionDropDownList_" + widgetId
      + "\" value =\"" + findValueAndDescriptionByRole(data, role).value + "\"/>"
      ).appendTo(roleSelectionDropDownDiv);

    var roleDescriptionDropDownDiv = $(
      "<div class=\"roleDescriptionDropDownDiv\" id=\"roleDescriptionDropDownDiv_" + widgetId
      + "\"></div>").appendTo(roleSelectionRoleSelector);

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
  } // end role section

  // TODO bind visibility update here instead of duplicateButton.mouseup
  var showOrHideInput = $(
    "<input type=\"checkbox\" id=\"ifHiddenTwitterPostsByLocationConfiguration_" + widgetId + "\">"
    ).appendTo(optionsDiv);

  optionsDiv.append("<p>Hide this widget</p>");

  var colourSelectDiv = $(
    "<div class=\"widgetColourSelectionDropDownDiv\"></div>"
    ).appendTo(optionsDiv);

  colourSelectDiv.append("<p>Widget Colour</p>");

  var colourData = [
  {text: "AntiqueWhite", value: "#FAEBD7"},
  {text: "Aquamarine",  value: "#7FFFD4"},
  {text: "Azure", value: "#F0FFFF"},
  {text: "Beige", value: "#F5F5DC"},
  {text: "BlanchedAlmond", value: "#FFEBCD"},
  {text: "CornflowerBlue", value: "#6495ED"},
  {text: "DarkTurquoise", value: "#00CED1"},
  {text: "FloralWhite", value: "#FFFAF0"},
  {text: "Gold", value: "#FFD700"},
  {text: "GoldenRod", value: "#DAA520"},
  {text: "GreenYellow", value: "#ADFF2F"},
  {text: "Khaki", value: "#F0E68C"},
  {text: "LightSalmon", value: "#FFA07A"},
  {text: "LightSteelBlue", value: "#B0C4DE"},
  {text: "MediumAquaMarine", value: "#66CDAA"},
  {text: "MediumTurquoise", value: "#48D1CC"},
  {text: "NavajoWhite", value: "#FFDEAD"},
  {text: "Orange", value: "#FFA500"},
  {text: "Orchid", value: "#DA70D6"},
  {text: "PaleGreen", value: "#98FB98"},
  {text: "PaleTurquoise", value: "#AFEEEE"},
  {text: "Pink", value: "#FFC0CB"},
  {text: "Yellow", value: "#FFFF00"}
  ];
  var colourSelectionDropDown = $(
    "<input id=\"colourChooser_"  + widgetId + "\" value = \"red\"/>"
    ).appendTo(colourSelectDiv);

  var colourValue = colourSelectionDropDown.data("kendoDropDownList");

  colourSelectionDropDown.kendoDropDownList({
    dataSource: colourData,
    dataTextField: "text",
    dataValueField: "value",
    change: function(e) {
      colourValue = $("#colourChooser_"  + widgetId).val();
      console.log("Changing widget id " + widgetId + " to " + colourValue);
    }
  });


  var buttonsDiv = $("<div class=\"widgetPropertiesControls\"></div>").appendTo(propertiesPopup);

  var cancelButton = $("<p id=\"cancelTwitterPostsByLocationConfiguration_" + widgetId + "\" class=\"cancelWidgetButton\">Cancel</p>").appendTo(buttonsDiv);
  cancelButton.mouseup(function(e){
    $('.closed-by-escape').hide();
  });

  var updateButton = $("<p id=\"updateTwitterPostsByLocationConfiguration_" + widgetId + "\" class=\"updateWidgetButton\">Update Widget</p>").appendTo(buttonsDiv);
  updateButton.click(function(e){

    var widgetColour = $("#colourChooser_"  + widgetId).data("kendoDropDownList").value();
    console.log(widgetColour);
    parameters.contentColour = widgetColour;

    parameters.autoRefreshData = "true";

    if (termRequired == true) {
      var newTerm = newTermInput.attr('value');
      // check if the terms are ok
      if (newTerm.length < 1) {
        alert("Please enter keywords to search for");
        return false;
      }
      parameters.term = newTerm;
    }


    $('.closed-by-escape').hide();

    // refresh or remove widget
    if (!showOrHideInput.is(":checked")) {

      if (role != undefined) {
        var newRoleId = $("#roleSelectionDropDownList_" + widgetId).val();
        parameters.role = findRoleAndDescriptionByValue(data, newRoleId).text;

      }
      $.get("/home/widgets/updateWidgetParameters/do.json", {
        wId: widgetId,
        newParametersValue: JSON.stringify(parameters)
        }, function(a){
        propertiesPopup.remove();
        loadWidgets(false);
      } );
    } else {
      $.get("/home/widgets/hideWidget/do.json", {
        wId: widgetId
      } );
      myDiv.remove();
      propertiesPopup.remove();
    //				loadWidgets();
    }
  });
  propertiesPopup.draggable();
  popupBackground(propertiesPopup.attr('id'));


//	return propertiesControl;
}

