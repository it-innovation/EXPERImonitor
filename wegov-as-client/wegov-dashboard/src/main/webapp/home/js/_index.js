//window.lang = new jquery_lang_js();
// peerdindex api key: 09355f288dcba68de7adb0e8c4f0fffd

var lat = geoplugin_latitude();
var lon = geoplugin_longitude();
var geo_city = geoplugin_city();
var geo_countryName = geoplugin_countryName();

$(document).ready(
		function() {
			
			$.ajaxSetup({ cache: false });
			
			// Home link
			$("#invislink").click(function() {
				window.location = "./index.html";
			});
			
			$("#add-new-location-dialog-holder").draggable();
			
			// Save current location as new location
			$("#newLocationNameSaveButton").mouseup(function(e){
				var locationAddress = geo_city + ", " + geo_countryName; 
				var locationName = $("#newLocationName").val();
				
				if (locationName.length > 0) {
					console.log(locationName + " | " + locationAddress + " | " + lat + " | " + lon);
					$.getJSON("/home/locations/addNewLocation/do.json",
							{locationName:locationName, locationAddress:locationAddress, lat:lat, lon:lon}, function(data) {
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
					$.each(widgets, function(widgetCounter, widget) {
						
						if (widget["isVisible"] == 0)
							checkedValue = "checked=\"checked\"";
						else
							checkedValue = "";
						
						if (widget["parametersAsString"].length < 1)
							myWidgetsWrapper.append("<p class=\"myWidgetsLabel\">" + widget["name"] + "</p>");
						else {
							var parameters = jQuery.parseJSON(widget["parametersAsString"]);
							var term = parameters.term;
							var role = parameters.role;
							
							if (role == undefined)
								myWidgetsWrapper.append("<p class=\"myWidgetsLabel\">" + widget["name"] + ": " + term + "</p>");
							else
								myWidgetsWrapper.append("<p class=\"myWidgetsLabel\">" + role + "s for: " + term + "</p>");
						}
						
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
					
					$.get("/home/savePolicymakerInfo/do.json", { fullName: fullName, organisation : organisation, newPassword : newPassword, changePassword : changePassword }, function(){
						$('.closed-by-escape').hide();
						userSettingsPopup.remove();
						userdetails["name"] = fullName;
						userdetails["organisation"] = organisation;
						jQuery.data($("#username")[0], "userdetails", userdetails);
						$("#username").text(fullName);
						loadWidgets();						
					} );					
					
					var wId;
					// TODO: make this recursive? Only save changed ones?
					// Have to do it here because of Cancel option
					$.each(myWidgetsWrapper.find("input"), function(inputCounter, theInput){
						wId = jQuery.data(theInput, "data");
						if ($(this).attr('checked') != undefined) {
//							console.log("Showing widget: [" + wId + "]");
							$.get("/home/widgets/showWidget/do.json", { wId: wId } );							
						} else {
//							console.log("Hiding widget: [" + wId + "]");
							$.get("/home/widgets/hideWidget/do.json", { wId: wId } );
						}
					});
					
				});
				
				popupBackground(userSettingsPopup.attr('id'));
			});			
			

			// Get widgets
			loadWidgets();

		});

/*
 * FUNCTIONS
 */

function compareWidgets(a, b) {
	if (a["columnOrderNum"] < b["columnOrderNum"]) return -1;
	if (a["columnOrderNum"] > b["columnOrderNum"]) return 1;
	return 0;
}

function loadWidgets() {
	$("#widgets").empty();
	$("#widgets").append("<p>Loading widgets, please wait...</p>");
	
	$.getJSON("/home/widgets/getVisibleWidgetsForDefaultWidgetSet/do.json",
			{}, function(data) {
//				console.log(data);
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
						addLocationMap(widget);
					} else if (widgetType == "userroles") {
						addUserRolesWidget(widget);
					}
					else if (widgetType == "retweets") {
						addLineAnalysisWidget(widget);
					}
					else if (widgetType == "twitterLocal") {
						addTwitter3PostsByLocationContainingTerm(widget, lat, lon, 20, "recent");
					}
					else if (widgetType == "mylocations") {
						addMyLocationsWidget(widget);
					}
					else if (widgetType == "peerindex") {
						addPeerindexWidget(widget);
					}
					else if (widgetType == "topicanalysis") {
						addTopicAnalysisWidget(widget);
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
					else if (widgetType == "latestgroupposttopicanalysis") {
						addLatestGroupPostTopicAnalysisWidget(widget);
					}
					else if (widgetType == "trending") {
						addTrendingWidget(widget);
					}
					else if (widgetType == "roleforterm") {
						addRoleForTermWidget(widget);
					}
					else {
						addEmptyWidget(widget);
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
					$.get("/home/widgets/updateWidgetPositions/do.json", {columnName:columnName, newOrder:newOrder});							
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

function addLocationMap(widget) {
//	var myDiv = $("<div class=\"portlet\" id=\"" + widget["id"] + "\"></div>").appendTo("#" + widget["columnName"]);
	var myDiv = initWidget(widget);
	var widgetHeaderDiv = $("<div class=\"widgetHeaderDiv\"></div>").appendTo(myDiv);
	$("<img class=\"widgetLogo\" src=\"img/google_logo.jpg\" alt=\"Source: Google Maps\"/>").appendTo(widgetHeaderDiv);
	var widgetHeaderTextDiv = $("<div class=\"widgetHeaderTextDiv\"></div>").appendTo(widgetHeaderDiv);
	$("<h2 class=\"widgetHeader\">" + widget["name"] + "</h2>").appendTo(widgetHeaderTextDiv);
	widgetHeaderDiv.append('<div class="clearfix"></div>');
	var widgetHeaderExtraTextDiv = $("<div class=\"widgetHeaderExtraTextDiv\"></div>").appendTo(widgetHeaderDiv);
	var extraText = $("<p></p>").appendTo(widgetHeaderExtraTextDiv);

	var myContainer = $("<div class=\"widgetContent\"></div>").appendTo(myDiv);	
		var gmapId = "gmap" + widget["id"];
		myContainer.append("<div id=\"" + gmapId + "\" class=\"gmap\"></div>");
	
	var widgetPostContent = $("<div class=\"widgetPostContent\"></div>").appendTo(myDiv);

//	var locationDecription = $("<p class=\"locationDecription\"></p>").appendTo(widgetPostContent);
//	var saveLocation = $("<p class=\"saveLocation\" id=\"saveLocation" + widget["name"] + "\">Save as</p>")
//			.appendTo(myDiv);
	
//	saveLocation.mouseup(function(e){
//		popupBackground('name-location-dialog-holder');		
//	});
	
	if (navigator.geolocation) // check if browser support this feature or not
	{
	    navigator.geolocation.getCurrentPosition(function(position) {
//	    		  console.log("Current latitude:"+lat+", longitude:"+lon);
	              betterlat = position.coords.latitude;
	              betterlon = position.coords.longitude;
//	              console.log("Better latitude:"+betterlat+", longitude:"+betterlon);
	              showMarkersOnGoogleMap(gmapId, betterlat, betterlon, widgetPostContent);
	              extraText.text("Your current location was determined with the help of your internet browser");
	         }
	    );
	} else {
		showMarkersOnGoogleMap(gmapId, lat, lon, widgetPostContent);
		extraText.text("Your current location was determined based on your IP address");
	}
	
	var widgetFooter = $("<div class=\"widgetFooter\"></div>").appendTo(myDiv);
	widgetFooter.append("<p class=\"widgetSettings\">Settings</p>");
	widgetFooter.append("<p class=\"widgetRefresh\">Refresh</p>");
	widgetFooter.append('<div class="clearfix"></div>');

//	locationDecription.text(geo_city + ", " + geo_countryName);

}

function showMarkersOnGoogleMap(gmapId, newLat, newLon, widgetPostContent) {
	var myLatlng = new google.maps.LatLng(newLat, newLon);
	var geocoder = new google.maps.Geocoder();
	var myOptions = {
			center : myLatlng,
			zoom : 10,
			mapTypeId : google.maps.MapTypeId.ROADMAP,
			disableDefaultUI : true,
			draggable: false,	
			scrollwheel: false
		};
	var map = new google.maps.Map(document.getElementById(gmapId), myOptions);	
	var bounds = new google.maps.LatLngBounds();
	
	var locationDescription = $("<p class=\"locationEntry\"></p>").appendTo(widgetPostContent);
    
	geocoder.geocode({'latLng': myLatlng}, function(results, status) {
    	console.log(results);
        if (status == google.maps.GeocoderStatus.OK) {
       		locationDescription.html("<b>" + markerTextFromNumber(0) + "</b> - " + " you current location (" + results[0].formatted_address + ") <span class=\"changeCurrentlocation\">change</span>");
       		locationDescription.find(".changeCurrentlocation").click(function(e){
       			console.log("Changing location!");
       		});
        } else {
          console.log("Geocoder failed due to: " + status);
          locationDescription.text(geo_city + ", " + geo_countryName);
        }

		var currentPositionMaker = new StyledMarker({styleIcon:new StyledIcon(StyledIconTypes.MARKER,{color:"FE796D",text:markerTextFromNumber(0),starcolor:"FFFF00"}),position:myLatlng,map:map});
		bounds.extend(currentPositionMaker.position);
		var infoWindow = new google.maps.InfoWindow({maxWidth: 210});
		var infoWindowContents = $('<p><b>Your current location</b><br />' + results[0].formatted_address + "<br/><span title=\"-1\" class=\"removelocation aslink\">Change</span></p>");
	    var html = infoWindowContents.html();
//	    console.log(html);
		google.maps.event.addListener(currentPositionMaker, 'click', function() {
	        infoWindow.setContent(html);
	        infoWindow.open(map, currentPositionMaker);
			var removeLocationLink = $("span.removelocation:visible");
			var removeLocationLinkId = removeLocationLink.attr('title');
			
			removeLocationLink.mouseup(function(e){
				console.log("Removing location with id: " + removeLocationLinkId);
				$.getJSON("/home/locations/removeLocation/do.json",
						{locationId:removeLocationLinkId}, function(response) {
							showMarkersOnGoogleMap(gmapId, newLat, newLon, widgetPostContent);
						});
			});						        
	    });		
		google.maps.event.addListener(map, 'click', function() {
			infoWindow.close();
			map.fitBounds(bounds);
//			if (data.length == 1) {
//				map.setZoom(8);
//			}
		});		
		
		google.maps.event.addListener(infoWindow, 'closeclick', function() { 
			map.fitBounds(bounds);
//			if (data.length == 1) {
//				map.setZoom(8);
//			}								
	    });		
		
		$.getJSON("/home/locations/getLocationsForPM/do.json",
				{}, function(data) {
					console.log(data);	
					if (data != null) {
						var markerText = "";
						$.each(data, function(counter, location){
							markerText = "" + markerTextFromNumber(counter + 1);
							myLatlng = new google.maps.LatLng(location.lat, location.lon);
							locationDecription = $("<p class=\"locationEntry\"></p>").appendTo(widgetPostContent);
							locationDecription.html("<b>" + markerText +"</b> - " + location.locationName + " (" + location.locationAddress + ")");
							var marker = new StyledMarker({styleIcon:new StyledIcon(StyledIconTypes.MARKER,{color:"FE796D",text:markerText}),position:myLatlng,map:map});
							bounds.extend(marker.position);
							map.fitBounds(bounds);
							
							var infoWindow = new google.maps.InfoWindow({maxWidth: 210});
							var infoWindowContents = $("<p><b>" + location.locationName + '</b><br />' + location.locationAddress + "<br/><span title=\"" + location.id + "\" class=\"removelocation aslink\">Remove this location</span></p>");
						    var html = infoWindowContents.html();
//						    console.log(html);
							google.maps.event.addListener(marker, 'click', function() {
						        infoWindow.setContent(html);
						        infoWindow.open(map, marker);
								var removeLocationLink = $("span.removelocation:visible");
								var removeLocationLinkId = removeLocationLink.attr('title');
								
								removeLocationLink.mouseup(function(e){
									console.log("Removing location with id: " + removeLocationLinkId);
									$.getJSON("/home/locations/removeLocation/do.json",
											{locationId:removeLocationLinkId}, function(response) {
												showMarkersOnGoogleMap(gmapId, newLat, newLon, widgetPostContent);
											});
								});						        
						    });	
							
							google.maps.event.addListener(map, 'click', function() {
								infoWindow.close();
								map.fitBounds(bounds);
//								if (data.length == 1) {
//									map.setZoom(8);
//								}
							});		
							
							google.maps.event.addListener(infoWindow, 'closeclick', function() { 
								map.fitBounds(bounds);
//								if (data.length == 1) {
//									map.setZoom(8);
//								}								
						    });							
						});
					}
				}
		);
	});	
}

function markerTextFromNumber(n) {
    var s = "";
    while(n >= 0) {
        s = String.fromCharCode(n % 26 + 97) + s;
        n = Math.floor(n / 26) - 1;
    }
    return s.toUpperCase();
}

function addUserRolesWidget(widget) {
	var myDiv = initWidget(widget);
	var widgetId = widget["id"];
//	var searchTerms = widget["parametersAsString"];
	var parameters = jQuery.parseJSON(widget["parametersAsString"]);
	var searchTerms = parameters.term;	
		
	$("<h2 class=\"widgetHeader\">" + widget["name"] + ": " + searchTerms + "</h2>")
			.appendTo(myDiv);
	var myChart = $("<div id=\"piechart_" + widgetId + "\" class=\"piechart\"></div>")
			.appendTo(myDiv);
	
	showWidgetSettingsWindow(myDiv, widget, "Keywords:");
	
//	var propertiesControl = $("<img src=\"./img/SvcIconInfo16.png\" class=\"propertiesControlImg\" id=\"propertiesControlImg_" + widgetId + "\">").appendTo(myDiv);
//	propertiesControl.click(function(e){
//		alert("Not implelemented yet. Click on the same button on \"Recent Local Posts\" widget for a setting window that will be used here");
//	});	
	$("<p class=\"locationDecription\">Based on no location bound Twitter search</p>")
			.appendTo(myDiv);

//	var data = [ [ 'Broadcaster', 12 ], [ 'Daily User', 28 ],
//			[ 'Information Seeker', 14 ], [ 'Information Source', 16 ],
//			[ 'Rare Poster', 7 ] ];

	myChart.empty();
	myChart.append("<p>Getting search results from Twitter...</p>");
	var parsedTerm = searchTerms.replace("#", "%23");
	var twitterQuery = "https://search.twitter.com/search.json?q=" + parsedTerm + "&show_user=true&include_entities=true&result_type=recent&rpp=99&callback=?";
	$.ajax({
		url:twitterQuery,
		dataType: 'json',	
		type: 'GET',
		contentType: "application/json; charset=utf-8",
		error: function(){
			myChart.append("<p>Twitter misbehaved, try again?</p>");
		},
		success: function(data) {
//			console.log(data);
			var twitterIdsToLookup = "";
			$.each(data["results"], function(resultCounter, result){
				if (resultCounter < data["results"].length - 1)
					twitterIdsToLookup = twitterIdsToLookup + result["from_user_id"] + ",";
				else
					twitterIdsToLookup = twitterIdsToLookup + result["from_user_id"];
			});
			var usersLookupQuery = "https://api.twitter.com/1/users/lookup.json?user_id=" + twitterIdsToLookup + "&include_entities=true&callback=?";
			myChart.append("<p>Looking up users on Twitter...</p>");
			$.ajax({
				url:usersLookupQuery,
				dataType: 'json',	
				type: 'GET',
				contentType: "application/json; charset=utf-8",
				error: function(){
					myChart.append("<p>Twitter misbehaved, try again?</p>");;
				},
				success:function(userdata) {
					myChart.append("<p>Running analysis...</p>");
					$.ajax({
						  type: 'POST',
						  url: "/home/analysis/kmi/do.json",
						  contentType: "application/json; charset=utf-8",
						  data: JSON.stringify({postData: data, userData: userdata}),
						  success: function(result){
							  myChart.empty();
							  var userRolesPlotData = new Array();
							  var discussionActivityPlotData = new Array();
							  var tempArray;
							  $.each(result["roleDistributionPoints"], function(counter, roleDistributionPoint){
								  tempArray = new Array();
								  tempArray[0] = roleDistributionPoint["roleName"];
								  tempArray[1] = roleDistributionPoint["numberOfUsers"];
								  userRolesPlotData[counter] = tempArray;
							  });
							  $.each(result["discussionActivityPoints"], function(counter, discussionActivityPoint){
								  tempArray = new Array();
								  tempArray[0] = discussionActivityPoint["time"];
								  tempArray[1] = discussionActivityPoint["value"];
								  discussionActivityPlotData[counter] = tempArray;
							  });
							  
							 userRolesDistributionPlot = jQuery.jqplot(myChart.attr('id'), [ userRolesPlotData ], {
							 		seriesDefaults : {
							 			renderer : jQuery.jqplot.PieRenderer,
							 			rendererOptions : {
							 				showDataLabels : true,
							 				
							 			}
							 		},
							 		legend : {
							 			show : true,
							 			location : 'e',
							 			fontSize: '12px',
							 			showLabels: true
							 		}
							 });							  
							  
//								jQuery.jqplot(myChart.attr('id'), [ data ], {
//									console.log(result);
//									seriesDefaults : {
//										renderer : jQuery.jqplot.PieRenderer,
//										rendererOptions : {
//											showDataLabels : true,
//							
//										}
//									},
//									legend : {
//										show : true,
//										location : 'e'
//									}
//								});

						  }
					});
				}
			});
		}
	});

}

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
		} ],
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
				
							var infoWindow = new google.maps.InfoWindow({maxWidth: 250});
							var infoWindowContents = $("<p><b>" + locationName + '</b><br />' + locationAddress + "<br/><span title=\"" + locationId + "\" class=\"removelocation aslink\">Remove this location</span></p>");
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
											{locationId:removeLocationLinkId}, function(response) {
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

function addMyLocationsWidget(widget) {
	var myDiv = initWidget(widget);
	var widgetHeaderDiv = $("<div class=\"widgetHeaderDiv\"></div>").appendTo(myDiv);
	$("<img class=\"widgetLogo\" src=\"img/google_logo.jpg\" alt=\"Source: Google Maps\"/>").appendTo(widgetHeaderDiv);
	var widgetHeaderTextDiv = $("<div class=\"widgetHeaderTextDiv\"></div>").appendTo(widgetHeaderDiv);
	$("<h2 class=\"widgetHeader\">" + widget["name"] + "</h2>").appendTo(widgetHeaderTextDiv);
	widgetHeaderDiv.append('<div class="clearfix"></div>');
	
//	myDiv.append("<h2 class=\"widgetHeader\">" + widget["name"] + "</h2>");
	$("<div class=\"widgetContainer hasMapToRefresh\" id=\"mylocationsContainer_" + widget["id"] + "\"></div>")
			.appendTo(myDiv);
	var myContainerDom = document.getElementById("mylocationsContainer_" + widget["id"]);
	myContainerDom.containerId = widget["id"];
	
//	$("<p class=\"saveLocation\">Configure</p>").appendTo(myDiv);			
	var addNewLocationControl = $("<p id=\"addNewLocationControl\">Add new</p>").appendTo(myDiv);
	$("<p class=\"locationDecription\"></p>").appendTo(myDiv);
	
	addNewLocationControl.mouseup(function(e){
		popupBackground('add-new-location-dialog-holder');
		$("#searchTextField").val('');
		$("#searchTextField").focus();
		$("#addNewLocationName").val('');
		$("#addNewLocationDialogNameSaveButton").unbind('mouseup');

			var mapOptions = {
	          center: new google.maps.LatLng(lat, lon),
	          zoom: 8,
	          mapTypeId: google.maps.MapTypeId.ROADMAP
	        };
	        var map = new google.maps.Map(document.getElementById('map_canvas'),
	          mapOptions);

	        var input = document.getElementById('searchTextField');
	        var autocomplete = new google.maps.places.Autocomplete(input);

	        autocomplete.bindTo('bounds', map);

	        var infowindow = new google.maps.InfoWindow();
	        var marker = new google.maps.Marker({
	          map: map
	        });

	        google.maps.event.addListener(autocomplete, 'place_changed', function() {
	          infowindow.close();
	          var place = autocomplete.getPlace();
	          console.log(place);
	          if (place.geometry.viewport) {
	            map.fitBounds(place.geometry.viewport);
	          } else {
	            map.setCenter(place.geometry.location);
	            map.setZoom(17);  // Why 17? Because it looks good.
	          }

	          var image = new google.maps.MarkerImage(
	              place.icon,
	              new google.maps.Size(71, 71),
	              new google.maps.Point(0, 0),
	              new google.maps.Point(17, 34),
	              new google.maps.Size(35, 35));
	          marker.setIcon(image);
	          marker.setPosition(place.geometry.location);

	          var address = '';
	          if (place.address_components) {
	            address = [(place.address_components[0] &&
	                        place.address_components[0].short_name || ''),
	                       (place.address_components[1] &&
	                        place.address_components[1].short_name || ''),
	                       (place.address_components[2] &&
	                        place.address_components[2].short_name || '')
	                      ].join(', ');
	          
		          console.log(place.geometry.location);
		          console.log("Latitude: " + place.geometry.location.lat());
		          console.log("Longitude: " + place.geometry.location.lng());
	

	          }

	          infowindow.setContent('<div><strong>' + place.name + '</strong><br>' + address);
	          infowindow.open(map, marker);
	          
	        });

	        // Sets a listener on a radio button to change the filter type on Places
	        // Autocomplete.
	        function setupClickListener(id, types) {
	          var radioButton = document.getElementById(id);
	          google.maps.event.addDomListener(radioButton, 'click', function() {
	            autocomplete.setTypes(types);
	          });
	        }

	        setupClickListener('changetype-all', []);
	        setupClickListener('changetype-establishment', ['establishment']);
	        setupClickListener('changetype-geocode', ['geocode']);
	          
	        // Save new location
			  $("#addNewLocationDialogNameSaveButton").mouseup(function(e){
				  var place = autocomplete.getPlace();
				  console.log("Saving location: ");
				  console.log(place);
				  var locationAddress = [(place.address_components[0] &&
	                        place.address_components[0].short_name || ''),
		                       (place.address_components[1] &&
		                        place.address_components[1].short_name || ''),
		                       (place.address_components[2] &&
		                        place.address_components[2].short_name || '')
		                      ].join(', '); 
			//					var locationAddress = address; 
					var locationName = $("#addNewLocationName").val();
					var locationLat = place.geometry.location.lat();
					var locationLon = place.geometry.location.lng();
					
					if (locationName.length < 1)
						locationName = locationAddress;
					
					console.log("Writing location to db: " + locationName + " | " + locationAddress + " | " + locationLat + " | " + locationLon);
					
					$.getJSON("/home/locations/addNewLocation/do.json",
							{locationName:locationName, locationAddress:locationAddress, lat:locationLat, lon:locationLon}, function(data) {
								$("#add-new-location-dialog-holder, #dialog-overlay").hide();
								
								$.each($(".hasMapToRefresh"), function(containerCount, container) {
									var containerDom = document.getElementById($(this).attr('id'));
									refreshLocations(containerDom.containerId);
								});
					});
				
			  });	        
	        
//	      }
//	      google.maps.event.addDomListener(document.getElementById('add-new-location-dialog-holder'), 'load', initialize);		
	});
	
	refreshLocations(widget["id"]);

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
				field: "whenCreated", dir : "desc"
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
					field: "whenStarted", dir : "desc"
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

function addTopicAnalysisWidget(widget) {
	var myDiv = initWidget(widget);
	var widgetId = widget["id"];
//	var searchTerms = widget["parametersAsString"];
	var parameters = jQuery.parseJSON(widget["parametersAsString"]);
	var searchTerms = parameters.term;	
	$("<h2 class=\"widgetHeader\">" + widget["name"] + ": " + searchTerms + "</h2>")
			.appendTo(myDiv);
//	console.log(widget);
	if (parameters.location != undefined) {
		$("<p class=\"locationDecription\">For location: " + geoplugin_city() + ", "
				+ geoplugin_countryName() + "</p>")
		.appendTo(myDiv);
	} else {
		$("<p class=\"locationDecription\">Based on no location bound Twitter search</p>")
		.appendTo(myDiv);
	}
	
	showWidgetSettingsWindow(myDiv, widget, "Keywords:");
	
//	var propertiesControl = $("<img src=\"./img/SvcIconInfo16.png\" class=\"propertiesControlImg\" id=\"propertiesControlImg_" + widgetId + "\">").appendTo(myDiv);
//	propertiesControl.click(function(e){
//		alert("Not implelemented yet. Click on the same button on \"Recent Local Posts\" widget for a setting window that will be used here");
//	});	
	var listOfTopicsDiv = $("<div id=\"listOfTopicsDiv_" + widgetId + "\" class=\"listOfTopicsDiv\"></div>").appendTo(myDiv);
	
	listOfTopicsDiv.empty();
	listOfTopicsDiv.append("<p>Getting search results from Twitter...</p>");
	var parsedTerm = searchTerms.replace("#", "%23");
	var twitterQuery = "https://search.twitter.com/search.json?q=" + parsedTerm + "&show_user=true&include_entities=true&result_type=recent&rpp=99&callback=?";
	
	if (parameters.location != undefined) {
		var locationCoords = "&geocode=" + lat + "," + lon + ",10km";
		parsedTerm = searchTerms.replace("#", "%23");
		twitterQuery = "https://search.twitter.com/search.json?q=" + parsedTerm + "&show_user=true&include_entities=true&result_type=recent&rpp=99" + locationCoords + "&callback=?";
	}
		
//	console.log("Topic analysis query: " + twitterQuery);
	
	$.ajax({
		url:twitterQuery,
		dataType: 'json',	
		type: 'GET',
		contentType: "application/json; charset=utf-8",
		error: function(){
			listOfTopicsDiv.append("<p>Twitter misbehaved, try again?</p>");
		},
		success: function(data) {
//			console.log(data);
			listOfTopicsDiv.append("<p>Running analysis...</p>");
			$.ajax({
				  type: 'POST',
				  url: "/home/analysis/koblenz/do.json",
//							  data: JSON.stringify({postData: data, userData: userdata}),
				  data: JSON.stringify(data),
				  contentType: "application/json; charset=utf-8",
//							  data: {postData: data, userData: userdata},
				  error: function() {
					  listOfTopicsDiv.append("<p>Twitter misbehaved, try again?</p>");
				  },
				  success: function(result){
//					  console.log(result);
					  listOfTopicsDiv.empty();
					  
					  if (result["topics"]) {
						  listOfTopicsDiv.append("<p class=\"koblenzResultsHeader\">Found " + result["numTopicsAsString"] + ":</p>");
	//					  var topicsList = $("<ul class=\"topicsList\"></ul>").appendTo(listOfTopicsDiv);
						  $.each(result["topics"], function(counter, topic){
							  
							  listOfTopicsDiv.append("<p class=\"koblenzTopicKeywords\">" + (counter + 1) + ". " + topic["keywords"] + "</p>");
							  var koblenzWidgettopicWrapper = $("<div class=\"koblenzWidgettopicWrapper\"></div>").appendTo(listOfTopicsDiv);
							  koblenzWidgettopicWrapper.append("<p class=\"koblenzKeyUsersLabel\">Key users: </p>");
							  $.each(topic["keyUsers"], function(keyUserCounter, keyUser){
								  if (keyUserCounter == topic["keyUsers"].length - 1)
									  koblenzWidgettopicWrapper.append("<a target=\"_blank\" class=\"koblenzTopicuserProfileLink\" href=\"https://twitter.com/#!/" + keyUser["screenName"] + "\">" + keyUser["fullName"] + "</a>");
								  else
									  koblenzWidgettopicWrapper.append("<a target=\"_blank\" class=\"koblenzTopicuserProfileLink\" href=\"https://twitter.com/#!/" + keyUser["screenName"] + "\">" + keyUser["fullName"] + "</a>, ");
							  });						  
							  
						  });
					  } else {
						  listOfTopicsDiv.append("<p>Nothing was found. Try again?</p>");
					  }
					  
				  }
			});
		}
	});
	
}

function addTrendingWidget(widget) {
	var myDiv = initWidget(widget);
	var widgetId = widget["id"];
	var parameters = jQuery.parseJSON(widget["parametersAsString"]);
	var searchTerms = parameters.term;	
//	var searchTerms = widget["parametersAsString"];
	var myHeader = $("<h2 class=\"widgetHeader\">" + widget["name"] + ": " + searchTerms + "</h2>").appendTo(myDiv);
	var footerText = $("<p class=\"locationDecription\"></p>").appendTo(myDiv);	
	
	showWidgetSettingsWindow(myDiv, widget, "Town, country or 'world':");
	
	var listOfTrendsDiv = $("<div id=\"listOfTrendsDiv_" + widgetId + "\" class=\"listOfTrendsDiv\"></div>").appendTo(myDiv);
	listOfTrendsDiv.append("<p>Getting location information for " + searchTerms + "</p>");
	
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
//				console.log(thePlace);
				listOfTrendsDiv.append("<p>Found " + thePlace["name"] + ", " + thePlace["placeTypeName"] + "</p>");
				listOfTrendsDiv.append("<p>Getting trends from Twitter</p>");
//				myHeader.text(widget["name"] + ": " + thePlace["name"]);
				myHeader.text(widget["name"] + ": " + searchTerms);
				
				if (thePlace["name"] != thePlace["country"])
					footerText.text("Location: " + thePlace["name"] + ", " + thePlace["country"] + " (" + thePlace["placeTypeName"] + ")");
				else
					footerText.text("Location: " + thePlace["name"] + " (" + thePlace["placeTypeName"] + ")");
				
				// get trending: https://api.twitter.com/1/trends/968019.json
				var woeid = thePlace["woeid"];
				var twitterQuery = "https://api.twitter.com/1/trends/" + woeid + ".json?callback=?"
				$.ajax({
					url:twitterQuery,
					dataType: 'json',	
					type: 'GET',
					contentType: "application/json; charset=utf-8",
					error: function(){
						listOfTrendsDiv.append("<p>Twitter misbehaved. Try again?</p>");
					},	
					success: function(twitterData) {
//						console.log(twitterData);
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
}

function addRoleForTermWidget(widget) {
	var myDiv = initWidget(widget);
	var widgetId = widget["id"];
	var parameters = jQuery.parseJSON(widget["parametersAsString"]);
	var searchTerms = parameters.term;
	var role = parameters.role;
	var myHeader = $("<h2 class=\"widgetHeader\">Users with the role \"" + role + "\" for: " + searchTerms + "</h2>").appendTo(myDiv);
	var footerText = $("<p class=\"locationDecription\">Based on no location bound Twitter search</p>").appendTo(myDiv);	
	
	showWidgetSettingsWindow(myDiv, widget, "Search term on Twitter:");
	
	var listOfUserWithRoleDiv = $("<div id=\"listOfUserWithRoleDiv_" + widgetId + "\" class=\"listOfUserWithRoleDiv\"></div>").appendTo(myDiv);
	listOfUserWithRoleDiv.append("<p>Getting search results from Twitter...</p>");
	
	var parsedTerm = searchTerms.replace("#", "%23");
	var twitterQuery = "https://search.twitter.com/search.json?q=" + parsedTerm + "&include_entities=true&result_type=recent&rpp=100&callback=?";
	$.ajax({
		url:twitterQuery,
		dataType: 'json',	
		type: 'GET',
		contentType: "application/json; charset=utf-8",
		error: function(){
			listOfUserWithRoleDiv.append("<p>Twitter misbehaved. Try again?</p>");
		},	
		success: function(postData) {
			var twitterIdsToLookup = "";
			$.each(postData["results"], function(resultCounter, result){
				if (resultCounter < postData["results"].length - 1)
					twitterIdsToLookup = twitterIdsToLookup + result["from_user_id"] + ",";
				else
					twitterIdsToLookup = twitterIdsToLookup + result["from_user_id"];
			});	
			var usersLookupQuery = "https://api.twitter.com/1/users/lookup.json?user_id=" + twitterIdsToLookup + "&include_entities=true&callback=?";
			listOfUserWithRoleDiv.append("<p>Looking up users on Twitter...</p>");
			$.ajax({
				url:usersLookupQuery,
				dataType: 'json',	
				type: 'GET',
				contentType: "application/json; charset=utf-8",
				error: function(){
					listOfUserWithRoleDiv.append("<p>Twitter misbehaved, try again?</p>");;
				},
				success:function(userdata) {
					listOfUserWithRoleDiv.append("<p>Running analysis</p>");
					$.ajax({
						  type: 'POST',
						  url: "/home/analysis/kmionlyroles/do.json",
						  contentType: "application/json; charset=utf-8",
						  data: JSON.stringify({userData: userdata, selectedRoleName: role, searchQuery: searchTerms}),
						  success: function(result){
//							  console.log(result);
							  listOfUserWithRoleDiv.empty();
							  var users = result["users"]; 
							  if (users.length < 1) {
								  listOfUserWithRoleDiv.append("<p>No users found with this role. Try a different role?</p>");
							  } else {
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
			});

		}
	});
	
	/*
	// get woeid: http://where.yahooapis.com/v1/places.q%28%27world%27%29?appid=GYknAX_V34HWU0aG9RFlvm.WTPBB7QlC9VNwYpo_JQc3MFJnNpmA2l17tut3pR02r6gCig-&format=json
	// places - place - woeid
	// Yahoo doesn't support https!
	var yahooQuery = "http://where.yahooapis.com/v1/places.q(" + searchTerms + ")?appid=GYknAX_V34HWU0aG9RFlvm.WTPBB7QlC9VNwYpo_JQc3MFJnNpmA2l17tut3pR02r6gCig-&format=json&callback=?"
	$.ajax({
		url:yahooQuery,
		dataType: 'json',	
		type: 'GET',
		error: function(){
			listOfTrendsDiv.append("<p>Yahoo never heard of '" + searchTerms + "'. Try again?</p>");
		},	
		success: function(data) {
//			console.log(data);
			if (data["places"]["count"] > 0) {
				var thePlace = data["places"]["place"][0];
//				console.log(thePlace);
				listOfTrendsDiv.append("<p>Found " + thePlace["name"] + ", " + thePlace["placeTypeName"] + "</p>");
				listOfTrendsDiv.append("<p>Getting trends from Twitter</p>");
				myHeader.text(widget["name"] + ": " + thePlace["name"]);
				
				if (thePlace["name"] != thePlace["country"])
					footerText.text("Location: " + thePlace["name"] + ", " + thePlace["country"] + " (" + thePlace["placeTypeName"] + ")");
				else
					footerText.text("Location: " + thePlace["name"] + " (" + thePlace["placeTypeName"] + ")");
				
				// get trending: https://api.twitter.com/1/trends/968019.json
				var woeid = thePlace["woeid"];
				var twitterQuery = "https://api.twitter.com/1/trends/" + woeid + ".json?callback=?"
				$.ajax({
					url:twitterQuery,
					dataType: 'json',	
					type: 'GET',
					error: function(){
						listOfTrendsDiv.append("<p>Twitter misbehaved. Try again?</p>");
					},	
					success: function(twitterData) {
//						console.log(twitterData);
						var trends = twitterData[0]["trends"];
						if (trends.length > 0) {
							listOfTrendsDiv.empty();
							var url;
							var trendName;
							$.each(trends, function(index, trend){
								trendName = trend["name"];
								url = trend["url"];
								listOfTrendsDiv.append("<p class=\"trend\"><a href=\"" + url + "\">" + trendName + "</a></p>");
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
	*/
}

function addPeerindexWidget(widget) {
	var myDiv = initWidget(widget);
	
//	var peerIndexProfile = widget["parametersAsString"];
	var parameters = jQuery.parseJSON(widget["parametersAsString"]);
	var peerIndexProfile = parameters.term;	
	var myHeader = $("<h2 class=\"widgetHeader\">" + "Never met " + peerIndexProfile + " before. Try again?" + "</h2>").appendTo(
			myDiv);
	var myChart = $(
			"<div id=\"plot_" + widget["id"]
					+ "\" class=\"peerindexchart\"></div>").appendTo(myDiv);
	
	showWidgetSettingsWindow(myDiv, widget, "Twitter username:");
	
	// Peerindex
	$.ajax({
		url:"https://api.peerindex.net/v2/profile/show.json?id=" + peerIndexProfile + "&api_key=09355f288dcba68de7adb0e8c4f0fffd&callback=?",
		dataType: 'json',	
		type: 'GET',
		contentType: "application/json; charset=utf-8",
		error: function(){
			myHeader.text("Never met " + peerIndexProfile + " before. Try again?");
		},
		success: function(data) {
			if (data.length < 1) {
				myHeader.text("Never met " + peerIndexProfile + ". Try again?");
			} else {
				var peerindex = data["peerindex"];
				var authority = data["authority"];
				var activity = data["activity"];
				var audience = data["audience"];
//				var url = data["url"];

				var header = myHeader;
//				var headerText = header.text();
				// header.text(headerText + " of " + data["name"] + " (" +
				// peerIndexProfile + ")");
				header.text(widget["name"] + ": " +  data["name"]);
				myDiv.append("<p class=\"peedindexaslabel\">" + peerindex
						+ "</p>");

				// if (data["topics"].length > 0) {
				// widget.append("<p>Topics: </p>");
				// var listOfTopics = $("<ul></ul>").appendTo(widget);
				// $.each(data["topics"], function(topicNum, topicName){
				// listOfTopics.append("<li>" + topicName + "</li>");
				// });
				// }

				$.jqplot(myChart.attr('id'), [ [ [ authority, "Authority" ] ],
						[ [ activity, "Activity" ] ],
						[ [ audience, "Audience" ] ] ], {
					seriesDefaults : {
						renderer : $.jqplot.BarRenderer,
						pointLabels : {
							show : true,
							location : 'e',
							edgeTolerance : -15
						},
						shadowAngle : 135,
						rendererOptions : {
							// barMargin: 50,
							barPadding : -25,
							barWidth : 25,
							barDirection : 'horizontal'
						}
					},
					axes : {
						yaxis : {
							renderer : $.jqplot.CategoryAxisRenderer,
							tickRenderer : $.jqplot.CanvasAxisTickRenderer,
							tickOptions : {
								angle : -30,
								fontSize : '10pt'
							}
						},
						xaxis : {
							pad : 1.5,

						}

					}
				});
			}			
		}
	});
	
	
//	$.getJSON("https://api.peerindex.net/v2/profile/show.json?id="
//			+ peerIndexProfile
//			+ "&api_key=09355f288dcba68de7adb0e8c4f0fffd&callback=?", {},
//			function(data) {
//
//			});

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
 * GENERIC WIDGET SETTINGS POPUP
 */
function showWidgetSettingsWindow(myDiv, widget, inputLabelText) {
	var widgetId = widget["id"];
	var parameters = jQuery.parseJSON(widget["parametersAsString"]);
	var term = parameters.term;
	var role = parameters.role;

	// i button
	var propertiesControl = $("<img src=\"./img/SvcIconInfo16.png\" class=\"propertiesControlImg\" id=\"propertiesControlImg_" + widgetId + "\">").appendTo(myDiv);
	
	// i button clicked
	propertiesControl.mouseup(function(e){
		var propertiesPopup = $("<div class=\"configureTwitterPostsByLocation closed-by-escape\" id=\"configureTwitterPostsByLocation_" + widgetId + "\"></div>").appendTo("#wrapper");
		$("<h2>Configure widget</h2>").appendTo(propertiesPopup);
		var deleteButton = $("<p id=\"deleteTwitterPostsByLocationConfiguration_" + widgetId + "\" class=\"deleteWidgetButton\">Delete Widget</p>").appendTo(propertiesPopup);
		deleteButton.click(function(e){
			$.get("/home/widgets/deleteWidget/do.json", { wId: widgetId }, function(result){
				$('.closed-by-escape').hide();
				propertiesPopup.remove();
				myDiv.remove();
			} );				
		});
		var optionsDiv = $("<div class=\"widgetPropertiesOptions\"></div>").appendTo(propertiesPopup);
		optionsDiv.append("<p>" + inputLabelText + "</p>");
		var newTermInput = $("<input type=\"text\" value=\"" + term + "\" id=\"newTermTwitterPostsByLocationConfiguration_" + widgetId + "\">").appendTo(optionsDiv);
		optionsDiv.append("<br>");
		
		
		
		// Role selection
		if (role != undefined) {
			var data = [
                        { text: "Broadcaster", value: "1", description: "Broadcaster is someone who posts with high daily rate and has a very high following. However he follows very few people, if any at all." },
                        { text: "Daily User", value: "2", description: "Daily User is someone with middle of the ground stats." },
                        { text: "Information Seeker", value: "3", description: "Information Seeker is someone who posts very rarely but follows a lot of people." },
                        { text: "Information Source", value: "4", description: "Information Source is someone who posts a lot, is followed a lot but follows more people than the Broadcaster."},
                        { text: "Rare Poster", value: "5", description: "RarePoster is somebody who hardly ever posts."}
                    ];

			var roleSelectionRoleSelector = $("<div class=\"roleSelectionRoleSelector\"></div>").appendTo(optionsDiv);
			var roleSelectionDropDownDiv = $("<div class=\"roleSelectionDropDownDiv\"></div>").appendTo(roleSelectionRoleSelector);
			roleSelectionDropDownDiv.append("<p>User role to search for:</p><br>");
			
			// Get value + description out of data!
			var roleSelectionDropDown = $("<input id=\"roleSelectionDropDownList_" + widgetId + "\" value =\"" + findValueAndDescriptionByRole(data, role).value + "\"/>").appendTo(roleSelectionDropDownDiv);
			
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
		var duplicateButton = $("<p id=\"duplicateTwitterPostsByLocationConfiguration_" + widgetId + "\" class=\"duplicateWidgetButton\">Duplicate</p>").appendTo(buttonsDiv);
		
		duplicateButton.click(function(e){
			var newTerm = newTermInput.attr('value');
			if (newTerm.length < 1) {
				alert('Please enter a keyword to search for');
				return false;
			} else {
				parameters.term = newTerm;
				if (role != undefined) {
					var newRoleId = $("#roleSelectionDropDownList_" + widgetId).val();
					parameters.role = findRoleAndDescriptionByValue(data, newRoleId).text;
				}
				
				$.get("/home/widgets/duplicateWidget/do.json", { wId: widgetId, parametersAsString: JSON.stringify(parameters) }, function(result){
					$('.closed-by-escape').hide();
					propertiesPopup.remove();
					loadWidgets();
				} );
			}
		});
		var cancelButton = $("<p id=\"cancelTwitterPostsByLocationConfiguration_" + widgetId + "\" class=\"cancelWidgetButton\">Cancel</p>").appendTo(buttonsDiv);
		cancelButton.mouseup(function(e){
			$('.closed-by-escape').hide();
		});
		var updateButton = $("<p id=\"updateTwitterPostsByLocationConfiguration_" + widgetId + "\" class=\"updateWidgetButton\">Update Widget</p>").appendTo(buttonsDiv);
		updateButton.click(function(e){
			var newTerm = newTermInput.attr('value');
			// check if the terms are ok
			if (newTerm.length < 1) {
				alert("Please enter keywords to search for");
				return false;
			}
			
			$('.closed-by-escape').hide();				
			
			// refresh or remove widget
			if (!showOrHideInput.is(":checked")) {
				
				parameters.term = newTerm;
				if (role != undefined) {
					var newRoleId = $("#roleSelectionDropDownList_" + widgetId).val();
					parameters.role = findRoleAndDescriptionByValue(data, newRoleId).text;
				}
				
				$.get("/home/widgets/updateWidgetParameters/do.json", { wId: widgetId, newParametersValue: JSON.stringify(parameters) }, function(a){
					loadWidgets();
				} );
			} else {
//				console.log("Hiding widget [" + widgetId + "]");
				$.get("/home/widgets/hideWidget/do.json", { wId: widgetId } );
				// refresh widgets - just the column?
				myDiv.remove();
//				$( ".column" ).trigger("sortupdate");
				propertiesPopup.remove();
//				loadWidgets();
			}
		});
		propertiesPopup.draggable();
		popupBackground(propertiesPopup.attr('id'));
	});
	
	return propertiesControl;
}

/*
 * LOCAL RECENT TWITTER SEARCH WIDGET
 */
function addTwitter3PostsByLocationContainingTerm(widget, customLat, customLon,	customRad, recentOrPopular) {
	var myDiv = initWidget(widget);
//	console.log(widget);
	var parameters = jQuery.parseJSON(widget["parametersAsString"]);
	refreshTwitterPostsByLocationWidget(widget, customLat, customLon, customRad, recentOrPopular, myDiv, parameters.term);
}

	function refreshTwitterPostsByLocationWidget(widget, customLat, customLon, customRad, recentOrPopular, myDiv, term) {
		myDiv.empty();
		var myHeader = $("<a target=\"_blank\"  href=\"/home/results.html?w=" + widget["id"] + "\" class=\"clickableWidgetHeader\">" + widget["name"] + ": " + term
					+ "</a>").appendTo(myDiv);
		
//		myHeader.click(function(e){
//			window.location.replace("/home/results.html?w=" + widget["id"]);
//		});
	
		showWidgetSettingsWindow(myDiv, widget, "Keywords:");
		
		var myContainer = $("<div class=\"widgetContainer\"></div>")
				.appendTo(myDiv);
		myContainer.append("<p>Searching Twitter, please wait...</p>");
		var locationDecription = $("<p class=\"locationDecription\"></p>")
				.appendTo(myDiv);
		locationDecription.text("For location: " + geoplugin_city() + ", "
				+ geoplugin_countryName());
	
		var parsedTerm = term.replace("#", "%23");
		var twitterQuery = "https://search.twitter.com/search.json?q=" + parsedTerm
				+ "&include_entities=true&result_type=" + recentOrPopular
				+ "&geocode=" + customLat + "," + customLon + "," + customRad
				+ "mi&rpp=100&callback=?";
	
		$.getJSON(twitterQuery, {}, function(data) {
//			console.log(data);
			putTweetIntoContainer(myContainer, data["results"]);
//			if (data["results"].length > 99) {
//				myHeader.text(widget["name"] + ": " + term + " (found " + data["results"].length + "+)");
//			} else {
				myHeader.text(widget["name"] + ": " + term + " (found " + data["results"].length + ")");
//			}
				
			var dataToSend = JSON.stringify({wId: widget["id"], type: "posts-twitter", name: "Search query: " + term + "; location: " + geoplugin_city() + ", "
				+ geoplugin_countryName() + "; posts: " + data["results"].length, location: geoplugin_city() + ", " + geoplugin_countryName(), data: data});
//			console.log(dataToSend);
			$.ajax({
				  type: 'POST',
				  url: "/home/widgets/saveWidgetData/do.json",
				  contentType: "application/json; charset=utf-8",
				  data: dataToSend
			});
		});	
	}
	
	// TODO display more tweets? Better tweetData - only get 3 tweets instead of 100?
	function putTweetIntoContainer(container, tweetData) {
		// TODO check location because it can be undefined!
//		console.log(tweetData);
		container.empty();
		if (tweetData.length > 0) {
			container.append("<p class=\"numResultsTitle\">First three, click on the link above to see all:</p>");
			var locationDetails;
			$.each(tweetData, function(num, contents) {
				if (contents["location"].length > 0) {
					locationDetails = ", " + $.trim(contents["location"]);
				} else {
					locationDetails = "";
				}
					
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
			container.append("<p>Nothing was found. Try again?</p>");
		}
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