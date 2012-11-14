var canRun = true;
var userRolesDistributionPlot = null;
var discussionActivityPlot = null;
var $tabs;
//var selectedActivity;

$(document).ready(function() {

	console.log("Calling fbLoad()...");
	fbLoad();

	var maxPages;
	var maxResults;
	var maxResultsPerPage;
	var defaultRadius = 5;

	resetOptions();
	setDefaults();

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
			console.log("Saving new user settings");

		});
		
		popupBackground(userSettingsPopup.attr('id'));
	});			
		
    $("#panelbar").kendoPanelBar({
//    	expandMode: "single"
    });

    $("#panelbar2").kendoPanelBar({
//    	expandMode: "single"
    });

	$("#languageSelector").kendoDropDownList({
        dataTextField: "text",
        dataValueField: "value",
        dataSource: [
            { text: "Any", value: "any" },
            { text: "English", value: "en" },
            { text: "German", value: "de" }
        ]
    });

	//$("#languageSelectorWrapper span").show();

	// Make tabs
	$tabs = $("#searchRunResultsWrapper").tabs();
	
	// Redraw graphs when the tab is selected
    $('#searchRunResultsWrapper').bind('tabsshow', function(event, ui) {
		console.log("tab selected: " + ui.index);
    	if ( (ui.index === 0) || (ui.index === 1) ) {
			showSearchHistory();
		}
		else if (ui.index === 2) {
			showAnalysisHistory("topic-opinion");
		}
    	else if (ui.index === 3){
			showAnalysisHistory("behaviour");
    		if (userRolesDistributionPlot != null) {
				console.log("user roles draw count = " + userRolesDistributionPlot._drawCount);
	    		if (userRolesDistributionPlot._drawCount === 0) {
					console.log("replotting user roles");
		        	userRolesDistributionPlot.replot();
	    		}
    		}
			else {
				console.log("userRolesDistributionPlot is null");
			}
    		if (discussionActivityPlot != null) {    	
				console.log("discussion activity draw count = " + discussionActivityPlot._drawCount);
    			if (discussionActivityPlot._drawCount === 0) {
					console.log("replotting discussion activity ");
    				discussionActivityPlot.replot();
    			}
    		}
			else {
				console.log("discussionActivityPlot is null");
			}
    	}
    });	
    
    $("#runButton").mouseup(function(e){
		//if (getValidatedSearchTerms() == false) {
		//	return false;
		//}
		console.log('Schedule button clicked');
		getValidatedSearchTerms(true, showSchedulerOptions);
    });
    
	function showSchedulerOptions() {
		var now = new Date();

		var timePicker3 = $("#timePicker3").data("kendoTimePicker");
		timePicker3.min(now);
		timePicker3.value(now);
		
		var timePicker4 = $("#timePicker4").data("kendoTimePicker");
		timePicker4.min(now);
		timePicker4.value(now);
		
		var timePicker5 = $("#timePicker5").data("kendoTimePicker");
		timePicker5.min(now);
		timePicker5.value(now);
		
		var timePicker6 = $("#timePicker6").data("kendoTimePicker");
		timePicker6.min(now);
		timePicker6.value(now);
		
    	popupBackground('schedulerPopup');
	}

    $("#addNewSchedulerButton").mouseup(function(e){
		addNewSchedule();
    });
	
    // Scheduler
//    $("#numberOfRunsSlider1").kendoSlider({
//        min: 0,
//        max: 20,
//        smallStep: 1,
//        largeStep: 4
//    }).data("kendoSlider");    
    
    $("#timePicker1").kendoTimePicker({
    	value: new Date(),
    	min: new Date(),
    	format: "HH:mm",
    	interval: 1
    });
    $("#timePicker2").kendoTimePicker({
    	value: new Date(),
    	min: new Date(),
    	format: "HH:mm",
    	interval: 1    	
    });

    $("#timePicker3").kendoTimePicker({
    	value: new Date(),
    	min: new Date(),
    	format: "HH:mm",
    	interval: 1    	
    });
    $("#timePicker4").kendoTimePicker({
    	value: new Date(),
    	min: new Date(),
    	format: "HH:mm",
    	interval: 1    	
    });
    $("#timePicker5").kendoTimePicker({
    	value: new Date(),
    	min: new Date(),
    	format: "HH:mm",
    	interval: 1    	
    });
    $("#timePicker6").kendoTimePicker({
    	value: new Date(),
    	min: new Date(),
    	format: "HH:mm",
    	interval: 1    	
    });
    $("#datePicker1").kendoDatePicker({
    	value: new Date(),
    	min: new Date(),
    	format: "dd/MM/yyyy"
    });
    $("#datePicker2").kendoDatePicker({
    	value: new Date(),
    	min: new Date(),
    	format: "dd/MM/yyyy"
    });

	var todaysDate = $("#datePicker2").val();
	console.log('Today = ' + todaysDate);

    $("#datePicker3").kendoDatePicker({
    	value: new Date(),
    	min: new Date(),
    	format: "dd/MM/yyyy"
    });
    $("#datePicker4").kendoDatePicker({
    	value: new Date(),
    	min: new Date(),
    	format: "dd/MM/yyyy"
    });
    
    //$("#numberOfRunsPicker1").kendoNumericTextBox({
    //	value: 3,
    //	format: "# times"
    //});
    //$("#numberOfRunsPicker2").kendoNumericTextBox({
    //	value: 3,
    //	format: "# times"
    //});
    //$("#numberOfRunsPicker3").kendoNumericTextBox({
    //	value: 3,
    //	format: "# times"
   // });
    $("#numeric1").kendoNumericTextBox({
    	value: 1,
		min: 1,
		step: 1,
    	format: "#"
    });

	var timeUnitsDs = new kendo.data.DataSource({
        data: [
            { text: "mins", value: "mins" },
            { text: "hours", value: "hours" },
            { text: "days", value: "days" }
        ]
	});

	$("#numeric1units").kendoDropDownList({
        dataTextField: "text",
        dataValueField: "value",
		dataSource: timeUnitsDs
    });

	$("#numeric3units").kendoDropDownList({
        dataTextField: "text",
        dataValueField: "value",
		dataSource: timeUnitsDs
    });

	$("#numeric4units").kendoDropDownList({
        dataTextField: "text",
        dataValueField: "value",
		dataSource: timeUnitsDs
    });

	$("#numeric7units").kendoDropDownList({
        dataTextField: "text",
        dataValueField: "value",
		dataSource: timeUnitsDs
    });

    $("#numeric2").kendoNumericTextBox({
    	value: 3,
		min: 1,
		step: 1,
    	format: "#"    	
    });
    $("#numeric3").kendoNumericTextBox({
    	value: 1,
		min: 1,
		step: 1,
    	format: "#"
    });
    $("#numeric4").kendoNumericTextBox({
    	value: 1,
		min: 1,
		step: 1,
    	format: "#"
    });
    //$("#numeric5").kendoNumericTextBox({
    //	value: 5,
    //	format: "# minute"
    //});
    //$("#numeric6").kendoNumericTextBox({
    //	value: 5,
    //	format: "# minute"
    //});
    $("#numeric7").kendoNumericTextBox({
    	value: 1,
		min: 1,
		step: 1,
    	format: "#"
    });
    //$("#numeric8").kendoNumericTextBox({
    //	value: 5,
    //	format: "# minute"
    //});    

	//KEM dev
	//var runObject = {runId: 297, results: 'twitter'};
	//getResultsForRun(runObject);


//	$("#searchRunResultsWrapper").kendoTabStrip({
//		animation: false,
//		contentLoad: onContentLoad
//	});
	//setupTabs();
	
	var map, marker;

	//updateLocations();
	getCurrentLocation(updateLocations); // get current location then call callback

	function updateLocations() {
		$("#locationSelectorWrapper").empty();

		$("#radiusUnitSelector").kendoDropDownList({
			dataTextField: "txt",
			dataValueField: "value",
			dataSource: [
				{ txt: "km", value: "km" },
				{ txt: "miles", value: "mi" }
			]
		});

		console.log("Setting default radius: " + defaultRadius);
		$("#radius").val(defaultRadius);

		// Fetch user's locations
		$.getJSON("/home/locations/getLocationsForPM/do.json",
				{}, function(data) {
	//				console.log(data);
					var tempLocation;
					
	 				$("#locationSelectorWrapper").append('<input type="radio" id="searchTheWorldRadio" name="location" value="everywhere" checked="checked"/><span lang="en" class="radioButtonLabels">Everywhere</span><br/>');

					if (data != null) {
						$.each(data, function(locationCounter, location){
							tempLocation = $("<input type=\"radio\" name=\"location\" value=\"" + location["lat"] + "," + location["lon"] + "\"/>").appendTo("#locationSelectorWrapper");
							jQuery.data(tempLocation[0], "location", location);
							$("#locationSelectorWrapper").append("<span lang=\"en\" class=\"radioButtonLabels\">" + location["locationName"] + " (" + location["locationAddress"] + ")" + "</span><br>");
						});
					}
					
					var myLocationName = "Near me";
					//var myLocationAddress = geo_city + ", " + geo_countryName;
					//var myLocation = {locationName: myLocationName, locationAddress: myLocationAddress, lat: lat, lon: lon};
					var myLocation = currLocation; // as defined in getCurrentLocation() - see location.js

					$("#locationSelectorWrapper").append("<input type=\"radio\" id=\"searchLocalRadio\" name=\"location\" value=\"" + myLocation["lat"] + "," + myLocation["lon"] + "\" />" +
														"<span lang=\"en\" class=\"radioButtonLabels\" id=\"radioButtonLocalLabel\"></span><br>");
					$("#radioButtonLocalLabel").text("Near me (" + myLocation["locationAddress"] + ")");
					$("#locationSelectorWrapper").append("<div id=\"whereIsNearMe\"></div>");
					$("#searchLocalRadio").data("location", myLocation);

					var myLatlng = new google.maps.LatLng( myLocation["lat"],  myLocation["lon"]);
	//				locationDecription.text(geo_city + ", " + geo_countryName);
					console.log(myLocation["lat"] + ", " + myLocation["lon"]);
					var myOptions = {
							  center: myLatlng,
							  zoom: 8,
							  mapTypeId: google.maps.MapTypeId.ROADMAP,
							  disableDefaultUI: true
							};
							map = new google.maps.Map(document.getElementById("whereIsNearMe"),
								myOptions);
							marker = new google.maps.Marker({
								position: myLatlng,
								map: map,
								animation: google.maps.Animation.DROP
							});
					
					//TODO: uncomment once this is working
					//addCurrentLocationNew();

					enableLocationPanel();

					$("#locationSelectorWrapper input:radio").click(getSelectedLocation);

		});
	}

	function addCurrentLocationNew() {
				//var myLocationName = "Near me";
				//var myLocationAddress = geo_city + ", " + geo_countryName;
				//var myLocation = {locationName: myLocationName, locationAddress: myLocationAddress, lat: lat, lon: lon};
				$("#locationSelectorWrapper").append("<input type=\"radio\" id=\"searchLocalRadio2\" name=\"location\" value=\"" + lat + "," + lon + "\" />" +
													"<span lang=\"en\" class=\"radioButtonLabels\" id=\"radioButtonLocalLabel2\"></span><br>");
				//$("#radioButtonLocalLabel2").text("Near me 2 (" + myLocationAddress + ")");
				$("#radioButtonLocalLabel2").text("Near me 2 ()");
				
				$("#locationSelectorWrapper").append("<div id=\"whereIsNearMe2\"></div>");

				var locationDiv = $("#whereIsNearMe2");
				//var addNewLocationControl = $("<p id=\"addNewLocationControl\">Add new</p>").appendTo(locationDiv);

				addNewLocationControl(locationDiv, updateLocations);

				var gmapId = "gmap"
				var extraText = $("<p></p>").appendTo("#whereIsNearMe2");

				addLocationMapToDiv(locationDiv, gmapId, extraText);
				
				//$("#searchLocalRadio2").data("location", myLocation);

				/*
				var myLatlng = new google.maps.LatLng(lat, lon);
//				locationDecription.text(geo_city + ", " + geo_countryName);
				console.log(lat + ", " + lon);
				var myOptions = {
				          center: new google.maps.LatLng(lat, lon),
				          zoom: 8,
				          mapTypeId: google.maps.MapTypeId.ROADMAP,
				          disableDefaultUI: true
				        };
				        map = new google.maps.Map(document.getElementById("whereIsNearMe2"),
				            myOptions);
				        marker = new google.maps.Marker({
				            position: myLatlng,
				            map: map,
				            animation: google.maps.Animation.DROP
				        });
				*/
	}

	function enableLocationPanel() {
		//$("#locationSelectorWrapper :input").removeAttr('disabled');

		//if (map) {
		//	map.setOptions({draggable: true});
		//}
		//if (marker) {
		//	marker.setOptions({clickable: true});
		//}
		//$("#location-li").show();
		$("#searchParamsDiv2").show();
	}

	function disableLocationPanel() {
		//$("#locationSelectorWrapper :input").attr('disabled', 'disabled');

		//if (map) {
		//	map.setOptions({draggable: false});
		//}
		//if (marker) {
		//	marker.setOptions({clickable: false});
		//}
		//$("#location-li").hide();
		$("#searchParamsDiv2").hide();
	}

	function enableLanguagePanel() {
		//var langDropDown = $("#languageSelector").data("kendoDropDownList");
		//if (langDropDown) {
		//	langDropDown.enable(true);
		//}
		$("#language-li").show();
	}

	function disableLanguagePanel() {
		//var langDropDown = $("#languageSelector").data("kendoDropDownList");
		//if (langDropDown) {
		//	langDropDown.enable(false);
		//}
		$("#language-li").hide();
	}

	$("#checkFbDetailsButton").mousedown(function(event) {
		$(this).css('background', "#E99520 url('../img/loginbtn_bg_inv.png') repeat-x");
	});

	$("#checkFbDetailsButton").mouseup(function(event) {
		$(this).css('background', "#E99520 url('../img/loginbtn_bg.png') repeat-x");
		console.log("checkFbDetailsButton clicked - calling checkFbGroupDetails()");
		checkFbGroupDetails();
	});

	$("#runButton").mousedown(function(event) {
		$(this).css('background', "#E99520 url('../img/loginbtn_bg_inv.png') repeat-x");
	});
	
	$("#runButton").mouseup(function(event) {
		$(this).css('background', "#E99520 url('../img/loginbtn_bg.png') repeat-x");
	});
	
	$("#resetButton").mousedown(function(event) {
		$(this).css('background', "#333 url('../img/loginbtn_bg_grey_inv.png') repeat-x");
//		$("#resultsHeaderMetadata").text("Results will appear here, search for something!");
//		$("#searchRunResultsWrapper").empty();
//		$("#searchRunResultsWrapper").attr('class', '');
		//$("#searchTerms").focus();
		//setupTabs();
		resetOptions();
		setDefaults();
	});
	
	$("#resetButton").mouseup(function(event) {
		$(this).css('background', "#333 url('../img/loginbtn_bg_grey.png') repeat-x");
	});
	
	$("#testButton").mousedown(function(event) {
		$(this).css('background', "#E99520 url('../img/loginbtn_bg_inv.png') repeat-x");
	});

	$("#analyseButton").mousedown(function(event) {
		$(this).css('background', "#E99520 url('../img/loginbtn_bg_inv.png') repeat-x");
	});

	$("#analyseButton").mouseup(function(event) {
		$(this).css('background', "#E99520 url('../img/loginbtn_bg.png') repeat-x");
		analyseSelectedResults();
	});
	
	// Search input
	$("#searchTerms").bind('keypress', function(e){
//		e.preventDefault();
		var code = (e.keyCode ? e.keyCode : e.which);
		//console.log(code);
		if (code == 13) {
			//$("#testButton").trigger('mousedown');
			//$("#testButton").trigger('mouseup');
			getValidatedSearchTerms(false);
		}
	});	

	//$("#searchTerms").bind('input', function(e){
	//	console.log('input');
	//});	

	$("#searchTerms").bind('paste', function(e){
		console.log('paste');
		validating = false;
		searchTermsValid = false;
		var el = $(this);
        setTimeout(function() {
            //var text = $(el).val();
			//console.log(text);
			getValidatedSearchTerms(false);
        }, 100);

		//$("#searchTerms").trigger('change');
	});	

	//$("#searchTerms").bind('change', function(e){
	//	console.log('change');
	//	console.log($(this).val());
	//	//getValidatedSearchTerms(false);
	//});

	//$("#searchTerms").bind('focusout', function(e){
	//	console.log('focusout');
	//	searchTermsValid = false;
	//	getValidatedSearchTerms(false);
	//});	

	// Search History controls
	$("#scheduledJobsButton").mousedown(function(event) {
		$(this).css('background', "#333 url('../img/loginbtn_bg_grey_inv.png') repeat-x");
	});

	addScheduledJobsWidget();

	// Make dialog windows draggable
	$("#scheduledJobsPopup").draggable();
	$("#schedulerPopup").draggable();

	$("#scheduledJobsButton").mouseup(function(event) {
		$(this).css('background', "#333 url('../img/loginbtn_bg_grey.png') repeat-x");
		popupBackground('scheduledJobsPopup');
		getScheduledJobs();
	});

	$("#refreshButton").mousedown(function(event) {
		$(this).css('background', "#333 url('../img/loginbtn_bg_grey_inv.png') repeat-x");
	});

	$("#refreshButton").mouseup(function(event) {
		$(this).css('background', "#333 url('../img/loginbtn_bg_grey.png') repeat-x");
		refreshSearchHistory();
	});

	$("input:radio:[name='sns']").click(function(){
		clearSearchResults();
		setupTabs();
		$("#searchTerms").val(""); // reset search terms
		setDefaults();
	});
	
	$("input:radio:[name='results.max.results.option']").click(function(){
		setDefaults();
		//validateNumberOfResults();
	});

	$("#max-pages").focusout(setNumberOfResults);
	$("#max-per-page").focusout(setNumberOfResults);
	$("#max-results").focusout(setNumberOfResults);

	function resetOptions() {
		canRun = true;
		$("#searchTerms").val("");
		$("#searchTerms").focus();
		setFbGroupInfo(null);
		$("#posts").attr('checked', 'checked');
		$("#searchTheWorldRadio").attr('checked', 'checked');
		$("#twitter").attr('checked', 'checked');
		$("#collect-fresh-results").attr('checked', 'checked');
		$("#unlimited").attr('checked', 'checked');
		$("#collectComments").attr('checked', 'checked');
		$("#checkFbDetailsButton").removeAttr('disabled');
		$("#testButton").removeAttr('disabled');
		$("#runButton").removeAttr('disabled');
		$("#refreshDelaySecs").val(10);
		$("#numberOfTopics").val(-1);

		setupTabs();
	}

	function setDefaults() {
		var sites = $("input:radio:checked[name='sns']").attr('value');
		console.log("Setting defaults for " + sites);
		var postType;
		var filter;

		if (sites == "twitter") {
			filter = "Twitter";
			postType = "tweets";
			maxPages = 15;
			maxResultsPerPage = 100;
			maxResults = maxPages * maxResultsPerPage;
			$("#posts").removeAttr('disabled');
			//$("#users").attr('disabled', 'disabled');
			//$("#groups").attr('disabled', 'disabled');
			$("#collectCommentsDiv").hide();

			$("#searchTermsLabel").text("Search for:");
			$("#searchTerms").removeClass("facebook").addClass("twitter");
			$("#checkFbDetailsButton").hide();

			enableLocationPanel();
			enableLanguagePanel();

			$("#fbStatus").empty();
			$("#fbGroupInfo").empty();
		}
		else if (sites == "facebook") {
			filter = "Facebook";
			postType = "posts";
			maxPages = 0;
			maxResultsPerPage = 1000;
			maxResults = 5000;
			$("#posts").removeAttr('disabled');
			//$("#users").attr('disabled', 'disabled');
			//$("#groups").attr('disabled', 'disabled');
			$("#collectCommentsDiv").show();
			$("#maxPostsToCollectCommentsFor").val(10);

			$("#searchTermsLabel").text("Event ID:");
			$("#searchTerms").removeClass("twitter").addClass("facebook");
			$("#checkFbDetailsButton").show();

			disableLocationPanel();
			disableLanguagePanel();

			if (getAccessToken() == null) {
				$("#checkFbDetailsButton").attr('disabled', 'disabled');
				$("#testButton").attr('disabled', 'disabled');
				$("#runButton").attr('disabled', 'disabled');
				fbGetLoginStatus();
			}
			//else {
			//	getValidatedSearchTerms(false);
			//}
		}

		if ($("#kendoGridContainer").data("kendoGrid")) {
			$("#kendoGridContainer").data("kendoGrid").dataSource.filter({ field: "name", operator: "startswith", value: filter });
		}

		$("#postType").text(postType);
		$("#asManyAsPossibleLabel").text("As many " + postType + " as possible (max " + maxResults + " per collection)");
		$("#max-pages").val(getStringValue(maxPages));
		$("#max-per-page").val(getStringValue(maxResultsPerPage));
		$("#max-results").val(getStringValue(maxResults));

		var resultsMaxResultsOption = $("input:radio:checked[name='results.max.results.option']").attr('value');
		console.log(resultsMaxResultsOption);
		if (resultsMaxResultsOption == "unlimited") {
			$("#numberOfResults").attr('disabled', 'disabled');
			$("#max-pages").attr('disabled', 'disabled');
			$("#max-per-page").attr('disabled', 'disabled');
			$("#max-results").attr('disabled', 'disabled');
		}
		else {
			$("#numberOfResults").removeAttr('disabled');
			$("#max-pages").removeAttr('disabled');
			$("#max-per-page").removeAttr('disabled');
			$("#max-results").removeAttr('disabled');
		}

		//$("#doTopicAnalysis").attr('checked', 'checked');
		//$("#doBehaviourAnalysis").attr('checked', 'checked');

		$("#collect-since-last-run").attr('checked', 'checked');
	}

	function checkFbGroupDetails() {
		console.log("checkFbGroupDetails(): calling getValidatedSearchTerms(true)");
		getValidatedSearchTerms(true);
	}

	function getStringValue(intVal) {
		if (intVal == 0)
			return "";
		else {
			return intVal;
		}
	}

	// Record this run
	//var myRunId = -1;

	// Create new search activity and run
	function createNewSearchActivityAndRun(clientSearch, config) {
		//myRunId = -1;
                console.log('createNewSearchActivityAndRun');
                console.log(config);
		$.ajax({
			  type: 'POST',
			  url: "/home/search/createNewSearch/do.json",
			  contentType: "application/json; charset=utf-8",
			  data: JSON.stringify(config),
			  error: function() {
				  console.log("WARNING: failed to write new activity to db! This will not be stored in the database");
				  reportSearchError("Failed to create activity for search", config, true);
			  },
			  /*success: function(returnedRunId){
				  if (returnedRunId >= 0) {
					  console.log("New search created with run id: " + returnedRunId);
					  myRunId = returnedRunId;
					  //updateSearchesList();
					  monitorRun(myRunId);
				  } else {
					  console.log("WARNING: failed to write new activity to db! This will not be stored in the database");
				  }
			  }*/
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
					  reportSearchError(actAndRun.error, config, true);
					  updateSearchesList(true);
				  }
				  else {
					  console.log("New search created");
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
							  updateSearchesList(true);
						  }
					  }
				  }
				  else if (actAndRun.activityId > 0) {
					  console.log("Activity created but no run available yet");
					  updateSearchesList(true);
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

	$("#testButton").mouseup(function(event) {
		$(this).css('background', "#E99520 url('../img/loginbtn_bg.png') repeat-x");
		////setupSearch(true, true); // client search, run now
		//setupSearch(false, true); // server search, run now
		console.log('Run now button clicked');
		getValidatedSearchTerms(true, runServerSearchNow);
	});

	function runServerSearchNow() {
		$tabs.tabs('select', '#' + 'resultsTabPanel');
		showSearchHistory();
		setupSearch(false, true); // server search, run now
	}

	function addNewSchedule() {
		var scheduleOption = $("#schedulerPopup input:radio:checked").attr('value');
		//alert(scheduleOption);
		//console.log("scheduleOption: " + scheduleOption);

		var startTime = "";
		var startDate = "";
		var stopTime = "";
		var stopDate = "";

		var startDateTime = "";
		var stopDateTime = "";

		var timeInterval = "";
		var timeIntervalUnits = "";
		var repeatCount = "";

		if (scheduleOption == "runNow") {
			//setupSearch(false, true); // server search, run now
		}
		else if (scheduleOption == "runEveryStartingNowAndRepeat") {
			timeInterval = $("#numeric1").val();
			timeIntervalUnits = $("#numeric1units").val();
			repeatCount = $("#numeric2").val();
		}
		else if (scheduleOption == "runEveryStartingNowAndStopAt") {
			timeInterval = $("#numeric3").val();
			timeIntervalUnits = $("#numeric3units").val();
			stopTime = $("#timePicker3").val();
			stopDate = todaysDate;
		}
		else if (scheduleOption == "runEveryStartingNowAndStopAt2") {
			timeInterval = $("#numeric4").val();
			timeIntervalUnits = $("#numeric4units").val();
			stopTime = $("#timePicker4").val();
			stopDate = $("#datePicker2").val();
		}
		else if (scheduleOption == "runEveryStartAtStopAt") {
			timeInterval = $("#numeric7").val();
			timeIntervalUnits = $("#numeric7units").val();
			startTime = $("#timePicker5").val();
			startDate = $("#datePicker4").val();
			stopTime = $("#timePicker6").val();
			stopDate = $("#datePicker3").val();
		}
		else if (scheduleOption == "runNtimesStartNowWithGap") {
			repeatCount = $("#numberOfRunsPicker1").val();
			timeInterval = $("#numeric5").val();
		}
		else if (scheduleOption == "runNtimesStartAtWithGap") {
			repeatCount = $("#numberOfRunsPicker2").val();
			startTime = $("#timePicker1").val();
			startDate = todaysDate;
			timeInterval = $("#numeric6").val();
		}
		else if (scheduleOption == "runNtimesStartAtOnDateWithGap") {
			repeatCount = $("#numberOfRunsPicker3").val();
			startTime = $("#timePicker2").val();
			startDate = $("#datePicker1").val();
			timeInterval = $("#numeric8").val();
		}
		else {
			alert("Sorry, option not yet implemented");
		}

		startDateTime = startDate + " " + startTime;
		stopDateTime = stopDate + " " + stopTime;

		console.log("Start time: " + startDateTime);
		console.log("Stop time: " + stopDateTime);
		
		scheduleOptions = {"scheduleOption": scheduleOption,
			//"startTime": startTime,
			//"startDate": startDate,
			//"stopTime": stopTime,
			//"stopDate": stopDate,
			"startDateTime": startDateTime,
			"stopDateTime": stopDateTime,
			"timeInterval": timeInterval,
			"timeIntervalUnits": timeIntervalUnits,
			"repeatCount": repeatCount}

		console.log("Scheduling options: " + JSON.stringify(scheduleOptions));
		scheduleSearch(scheduleOptions);

		$('#dialog-overlay, #schedulerPopup').hide();
	}

	function scheduleSearch(scheduleOptions) {
		console.log("\nSchedule options: ");
		console.log("scheduleOption: " + scheduleOptions.scheduleOption);
		console.log("startTime: " + scheduleOptions.startTime);
		console.log("startDate: " + scheduleOptions.startDate);
		console.log("stopTime: " + scheduleOptions.stopTime);
		console.log("stopDate: " + scheduleOptions.stopDate);
		console.log("timeInterval: " + scheduleOptions.timeInterval);
		console.log("timeIntervalUnits: " + scheduleOptions.timeIntervalUnits);
		console.log("repeatCount: " + scheduleOptions.repeatCount);

		var runNow = false;

		if (scheduleOptions.scheduleOption == "runNow") {
			runNow = true;
		}

		setupSearch(false, runNow, scheduleOptions); // server search, schedule
	}

	function setupSearch(clientSearch, runNow, scheduleOptions) {
		var searchTerms = getValidatedSearchTerms();

		if (searchTerms === false) return;
				
		if (clientSearch) {
			alert('Client search no longer supported by this page');
			/*
			console.log("Setting up client search");

			if (canRun) {
				canRun = false;
			}
			else {
				alert("Please wait for current search to finish running.");
				return false;
			}

			clearSearchResults();
			
			var searchConfig = createSearchConfig(searchName, clientSearch, searchTerms, null, null);
			if (searchConfig === false)
				return;

			var searchName = getSearchName(searchTerms);

			// Create new search activity and run (run id returned in myRunId)
			createNewSearchActivityAndRun(clientSearch, searchConfig);

			initialiseSearchResults(searchName);
			
			var locationCoords = getSelectedLocation();

			twitterSearch(searchTerms, locationCoords);
			*/
		}
		else {
			console.log("Setting up server search");

			if (runNow) {
				console.log("Running server search now");

				if (canRun) {
					canRun = false;
				}
				else {
					alert("Please wait for current search to finish running, or schedule the search for later.");
					return false;
				}

				clearSearchResults();

				var selectedLocation = getSelectedLocation();
			
				var searchConfig = createSearchConfig(clientSearch, searchTerms, selectedLocation, runNow);

				if (searchConfig === false) {
					canRun = true;
					return;
				}

				console.log(JSON.stringify(searchConfig));

				// Create new search activity and run (run id returned in myRunId)
				console.log("Starting search: " + searchConfig.name);
				createNewSearchActivityAndRun(clientSearch, searchConfig);

				initialiseSearchResults(searchConfig.name);
			}
			else {
				console.log("Scheduling server search");

				var selectedLocation = getSelectedLocation();
			
				var searchConfig = createSearchConfig(clientSearch, searchTerms, selectedLocation, runNow, scheduleOptions);
				if (searchConfig === false)
					return;

				// Create new search activity and run (run id returned in myRunId)
				createNewSearchActivityAndRun(clientSearch, searchConfig);
			}
		}
		
	}

	function getSearchName(searchTerms, config, selectedLocation, rad, radUnit) {
		console.log("getSearchName()");
		var searchName = "Search for ";
		//var sites = $("input:radio:checked[name='sns']").attr('value');
		var sites = config.sites;

		if (sites == "facebook") {
			searchName = getFbSearchName(searchTerms, config);
		}
		else if (sites == "twitter") {
			//searchName = "Twitter search for " + '"' + searchTerms + '"; location: ' + formatLocation(config);
			var locationStr;
			console.log("selectedLocation: " + JSON.stringify(selectedLocation));
			console.log("rad: " + rad);
			console.log("radUnit: " + radUnit);

			if (selectedLocation.locationName == "Everywhere") {
				locationStr = selectedLocation.locationName;
			}
			else {
				locationStr = selectedLocation.locationAddress;
			}

			searchName = "Twitter search for " + '"' + searchTerms + '"; location: ' + locationStr;

			if (! (selectedLocation.locationName == "Everywhere")) {
				searchName += "; radius: " + rad + radUnit;
			}
		}
		else {
			searchName += searchTerms;
		}

		console.log("searchName: " + searchName);
		return searchName;
	}

	/*
	function formatLocation(config) {
		var formattedLocation;

		console.log("location.option: " + config["location.option"]);
		console.log("location.useapi: " + config["location.useapi"]);
		console.log("location.appendtosq: " + config["location.appendtosq"]);
		console.log("location.city: " + config["location.city"]);
		console.log("location.region: " + config["location.region"]);
		console.log("location.countryName: " + config["location.countryName"]);
		console.log("location.countryCode: " + config["location.countryCode"]);
		console.log("location.lat: " + config["location.lat"]);
		console.log("location.long: " + config["location.long"]);
		console.log("location.radius: " + config["location.radius"]);
		console.log("location.radius.unit: " + config["location.radius.unit"]);

		if (config["location.option"] == "anywhere") {
			formattedLocation = "Everywhere";
		}
		else {
			formattedLocation = config["location.city"] + ", " + config["location.countryName"];
		}

		console.log("Formatted location: " + formattedLocation);
		return formattedLocation;
	}
	*/
	
	function getSelectedLocation() {
		var locationData;

		// search location:
		//var locationCoords = $("#locationSelectorWrapper input:radio:checked").attr('value');
		//var selectedLocation = $("#locationSelectorWrapper input:radio:checked")[0];

		var selectedLocation = $("input:radio:checked[name='location']")[0];
		console.log(selectedLocation);

		var value = $(selectedLocation).attr('value');
		console.log('selected location value: ' + value);

		if (value == "everywhere") {
			locationData = {locationName: "Everywhere"};
		}
		else {
			locationData = jQuery.data(selectedLocation, "location");
		}

		//console.log('locationCoords: ' + locationCoords);
		//return locationCoords;

		console.log('location: ' + JSON.stringify(locationData));

		return locationData;
	}

	function createSearchConfig(clientSearch, searchTerms, selectedLocation, runNow, scheduleOptions) {
		console.log("Creating search config");
		var config;

		if (clientSearch) {
			var locationCoords = selectedLocation.lat + "," + selectedLocation.lon;
			config = {"clientSearch":clientSearch, "searchTerms":searchTerms, "locationCoords":locationCoords};
		}
		else {
			var sites = $("input:radio:checked[name='sns']").attr('value');
			var whatCollect = $("input:radio:checked[name='what.collect']").attr('value');
			var resultsMaxResultsOption = $("input:radio:checked[name='results.max.results.option']").attr('value');
			var resultsMaxPages = $("#max-pages").attr('value');
			var resultsMaxPerPage = $("#max-per-page").attr('value');
			var resultsMaxResults = $("#max-results").attr('value');
			var resultsStorageKeeprawdata = "true";
			var resultsStorageStoreindb = "false";
			var resultsCollectSinceLastRun = $("input:radio:checked[name='results.collect.since.last.run']").attr('value');
			var languageSelector = $("#languageSelector").data("kendoDropDownList");

			if (! validateNumberOfResults())
				return false;

			if (sites == "facebook") {
				var pages = getPagesParam(resultsMaxResultsOption, resultsMaxPages);
				var groupID = searchTerms; // get group id from search terms box
				var collectComments = $("#collectComments").is(":checked");
				var maxPostsToCollectCommentsFor = $("#maxPostsToCollectCommentsFor").val();
				//var searchName = getFbSearchName(groupID);
				//var accessToken = "AAACEdEose0cBANCjZCTuM0ZCGn2l64ZAs1QSETuFfFUW7OBxXNZBTX4Js77Ov5VOZBTGa0acIBrKtQx0ZCiN9fj3zsIIAuHwXd2XHVZBZCdgzWgVkpbyd71r"; //TODO: configure this
				var accessToken = getAccessToken();
				if (accessToken == null) {
					return false;
				}

				config = {"clientSearch":clientSearch,
					"runNow":runNow,
					//"name": searchName,
					"sites": sites,
					"what.collect": whatCollect,
					"collectComments": collectComments,
					"maxPostsToCollectCommentsFor": maxPostsToCollectCommentsFor,
					"pages": pages,
					"group.id": groupID,
					"access.token": accessToken,
					"outputOfType": "eu.wegov.coordinator.dao.data.WegovPostItem",
					"results.max.results.option": resultsMaxResultsOption,
					"results.max.results": resultsMaxResults,
					"results.max.per.page": resultsMaxPerPage,
					"results.max.pages": resultsMaxPages,
					"results.storage.keeprawdata": resultsStorageKeeprawdata,
					"results.storage.storeindb": resultsStorageStoreindb,
					"results.collect.since.last.run": resultsCollectSinceLastRun,
					"schedule":scheduleOptions
				};
			}
			else {
				//var locationOption = $("input:radio:checked[name='location']").attr('value');
				var locationOption;

				var city = "";
				var region = "";
				var countryName = "";
				var countryCode = "";
				var lat = "";
				var lon = "";
				var rad = $("#radius").val();
				var radUnit = $("#radiusUnitSelector").val();

				/*
				console.log("locationOption: " + locationOption);

				if (locationOption == "everywhere") {
					locationOption = "anywhere";
				}
				else {
					if ($("#searchLocalRadio").is(":checked")) {
						city = geo_city;
						region = geo_region;
						countryName = geo_countryName;
						countryCode = geo_countryCode;
					}
					latLon = locationOption;
					locationOption = "local";
					latLonArray = latLon.split(',');
					lat = latLonArray[0];
					lon = latLonArray[1];
				}
				*/

				var locationName = selectedLocation.locationName;
				var locationAddress = selectedLocation.locationAddress;

				if (locationName == "Everywhere") {
					locationOption = "anywhere";
				}
				else {
					locationOption = "local";
					if (locationName == "Near me") {
						var cityAndCountry = locationAddress.split(',');
						city = cityAndCountry[0];
						countryName = cityAndCountry[1];
					}
					else {
						//Following is not ideal, but we can't easily extract city, country, etc
						//as the address might be formatted in different ways
						city = locationAddress;
					}

					lat = selectedLocation.lat;
					lon = selectedLocation.lon;
				}

				config = {"clientSearch":clientSearch,
					"runNow":runNow,
					//"name": searchName,
					"sites": sites,
					"what.collect": whatCollect,
					"what.words.all": searchTerms,
					"what.words.exactphrase": "",
					"what.words.any": "",
					"what.words.none": "",
					"what.words.hashtags": "",
					"what.people.from.accounts": "",
					"what.people.to.accounts": "",
					"what.people.mentioning.accounts": "",
					"what.people.from.groups": "",
					"what.people.to.groups": "",
					"what.people.mentioning.groups": "",
					"what.words.name.idortag": "",
					"what.words.name.contains": "",
					"what.dates.option": "any",
					"what.dates.since": "",
					"what.dates.until": "",
					"concatenate.list_elements": "sources",
					"sources": "",
					"location.option": locationOption,
					"location.useapi": "true",
					"location.appendtosq": "false",
					"location.city": city,
					"location.region": region,
					"location.countryName": countryName,
					"location.countryCode": countryCode,
					"location.lat": lat,
					"location.long": lon,
					"location.radius": rad,
					"location.radius.unit": radUnit,
					"language": languageSelector.value(),
					"outputOfType": "eu.wegov.coordinator.dao.data.WegovPostItem",
					"results.type": "static",
					"results.max.collection.time.option": "",
					"results.max.collection.time": "",
					"results.max.results.option": resultsMaxResultsOption,
					"results.max.results": resultsMaxResults,
					"results.max.per.page": resultsMaxPerPage,
					"results.max.pages": resultsMaxPages,
					"results.storage.keeprawdata": resultsStorageKeeprawdata,
					"results.storage.storeindb": resultsStorageStoreindb,
					"results.collect.since.last.run": resultsCollectSinceLastRun,
					"schedule":scheduleOptions
				};
			}

			var searchName = getSearchName(searchTerms, config, selectedLocation, rad, radUnit);
			config.name = searchName;
		}

		return config;
	}

	function setNumberOfResults(event) {
		console.log("setNumberOfResults(): " + $(this).attr('name') + " = " + $(this).val());
		var resultsMaxPages = $("#max-pages").attr('value').trim();
		var resultsMaxPerPage = $("#max-per-page").attr('value').trim();
		var resultsMaxResults = $("#max-results").attr('value').trim();
		//console.log("resultsMaxPages:" + resultsMaxPages);
		//console.log("resultsMaxPerPage:" + resultsMaxPerPage);
		//console.log("resultsMaxResults:" + resultsMaxResults);

		if (resultsMaxPages == 0) {
			resultsMaxPages = "";
			$("#max-pages").val("");
		}

		if (resultsMaxPerPage == 0) {
			resultsMaxPerPage = maxResultsPerPage;
			$("#max-per-page").val(maxResultsPerPage);
		}
		else if (resultsMaxPerPage == "") {
			resultsMaxPerPage = "";
			$("#max-per-page").val("");
		}
		else if (resultsMaxPerPage > maxResultsPerPage) {
			resultsMaxPerPage = maxResultsPerPage;
			$("#max-per-page").val(maxResultsPerPage);
		}

		if (resultsMaxResults == 0) {
			resultsMaxResults = "";
			$("#max-results").val("");
		}

		if ($(this).attr('id') == "max-results") {
			if (resultsMaxResults != "") {
				if ((maxResults > 0) && (resultsMaxResults > maxResults)) {
					$("#max-results").val(maxResults);
				}
				return;
			}
		}

		if ( (resultsMaxPages == "") || (resultsMaxPerPage == "") ) {
			$("#max-results").val("");
			return;
		}

		resultsMaxResults = resultsMaxPages * resultsMaxPerPage;
		//console.log('resultsMaxResults: ' + resultsMaxResults);
		//console.log('maxResults: ' + maxResults);

		if ((maxResults > 0) && (resultsMaxResults > maxResults)) {
			resultsMaxResults = maxResults;
			$("#max-pages").val(maxPages);
		}

		$("#max-results").val(resultsMaxResults);
	}

	function validateNumberOfResults() {
		var resultsMaxResultsOption = $("input:radio:checked[name='results.max.results.option']").attr('value');
		var resultsMaxPages = $("#max-pages").attr('value').trim();
		var resultsMaxPerPage = $("#max-per-page").attr('value').trim();
		var resultsMaxResults = $("#max-results").attr('value').trim();

		console.log("resultsMaxPages:" + resultsMaxPages);
		console.log("resultsMaxPerPage:" + resultsMaxPerPage);
		console.log("resultsMaxResults:" + resultsMaxResults);

		if (resultsMaxResultsOption == "limited") {
			if ( (resultsMaxPages == "") && (resultsMaxResults == "") ) {
				alert('To limit number results please enter a value for "Max pages" or "Max results"');
				$("#max-pages").val(getStringValue(maxPages));
				$("#max-results").val(getStringValue(maxResults));
				return false;
			}
		}

		return true;
	}

	function getPagesParam(resultsMaxResultsOption, resultsMaxPages) {
		var pagesParam = "all";
		if (resultsMaxResultsOption == "limited") {
			if (resultsMaxPages == "1") {
				pagesParam = "single";
			}
		}
		return pagesParam;
	}

	$("#searchForm").submit(function(e){
		e.preventDefault();
	});

	addSearchesWidget("all");
	//addAnalysisHistoryWidget("all"); // Delay creation of analysis history until analysis tab is selected

	/*
	function twitterSearch(searchTerms, locationCoords) {
		if (locationCoords == "everywhere") {
			locationCoords = "";
		} else {
			locationCoords = "&geocode=" + locationCoords + ",10km";
		}

//		var twitterQuery = "https://search.twitter.com/search.json?q=" + searchTerms + "&include_entities=true&result_type=" + recentOrPopular + "&geocode=" + customLat + "," + customLon + "," + customRad + "mi&page=1&rpp=5&callback=?";
		var parsedTerm = searchTerms.replace("#", "%23");
		var twitterQuery = "https://search.twitter.com/search.json?q=" + parsedTerm + "&show_user=true&include_entities=true&result_type=recent&rpp=99" + locationCoords + "&callback=?";
//		var facebookQuery = "https://graph.facebook.com/search?q=" + searchTerms + "&type=post&limit=99&callback=?";
		console.log(twitterQuery);

		$.ajax({
				url:twitterQuery,
				dataType: 'json',	
				type: 'GET',
				contentType: "application/json; charset=utf-8",
				error: function(){
					$("#resultsPanel").empty();
					$("#resultsPanel").append("<p>Ooops, looks like Twitter misbehaved. You will have to try again, sorry!</p>");
					
					if ($("#doTopicAnalysis").is(":checked")) {
						$("#searchRunResultsWrapper-2").empty();
						$("#searchRunResultsWrapper-2").append("<p>Search returned no results, nothing to analyse. Try again?</p>");
					}
					
					if ($("#doBehaviourAnalysis").is(":checked")) {
						$("#searchRunResultsWrapper-3").empty();
						$("#searchRunResultsWrapper-3").append("<p>Search returned no results, nothing to analyse. Try again?</p>");
					}
					canRun = true;
				},
				success: function(data) {
					console.log(data);
					$("#resultsPanel").empty();
					
					if (myRunId >= 0) {
						// Update activity and give data
						console.log(data);
						$.ajax({
							  type: 'POST',
							  url: "/home/search/updateSearch/do.json",
							  data: JSON.stringify({myRunId: myRunId, data: data}),
							  contentType: "application/json; charset=utf-8",
							  error: function() {
								  console.log("Failed to record search run");
							  },
							  success: function(result){
								  console.log("New search run saved");
								  
								  updateSearchesList();
								  
								  if ($("#doBehaviourAnalysis").is(":checked") || $("#doTopicAnalysis").is(":checked")) {
									  
								  } else {
									  canRun = true;
								  }
							  }
						});
					}
					else {
						console.log("WARNING: myRunId = " + myRunId);
					}
					
					if (data["results"].length == 0) {
						$("#resultsPanel").append("<p>Could not find any posts containing \"" + searchTerms + "\". Try again?</p>");
						if ($("#doTopicAnalysis").is(":checked")) {
							$("#searchRunResultsWrapper-2").empty();
							$("#searchRunResultsWrapper-2").append("<p>Search returned no results, nothing to analyse. Try again?</p>");
						}
						
						if ($("#doBehaviourAnalysis").is(":checked")) {
							$("#searchRunResultsWrapper-3").empty();
							$("#searchRunResultsWrapper-3").append("<p>Search returned no results, nothing to analyse. Try again?</p>");
						}
						canRun = true;
						return false;
					}
					
					// KMI ANALYSIS TAB
					if ($("#doBehaviourAnalysis").is(":checked")) {
						extractTwitterUserIdsAndRunKmiAnalysis(data, searchTerms);
					}
					
					// KOBLENZ ANALYSIS TAB
					if ($("#doTopicAnalysis").is(":checked")) {
						koblenzAnalysis(data);
					}
	//				});
					
					displayResults(data);
					
	//				var tabstrip = $("#searchRunResultsWrapper").kendoTabStrip({animation: false});
	//				tabstrip.select(tabstrip.tabGroup.children("li:first"));
				}
		});
		
	}
*/

});

var validating = false;
var searchTermsValid = false;
function getValidatedSearchTerms(showAlert, callback) {
	if (validating) {
		if (callback) {
			console.log('getValidatedSearchTerms in progress - waiting for completion, then calling ' + callback);
		}
		else {
			console.log('getValidatedSearchTerms in progress - waiting for completion (no callback)');
		}
		waitForValidation(showAlert, callback);
		return;
	}

	validating = true;

	var searchTerms = $("#searchTerms").val().trim();
	console.log(searchTerms);
	
	if (searchTerms.length < 1) {
		if (showAlert) {
			alert("Please enter keywords to search for");
		}
		validating = false;
		return false;
	}

	var sites = $("input:radio:checked[name='sns']").attr('value');

	if (sites == "facebook") {
		validateGroupId(searchTerms, callback);
	}
	else {
		searchTermsValid = true;
		validating = false;

		if (callback) {
			callback();
		}
	}

	return searchTerms;
}

function waitForValidation(showAlert, callback) {
	if (! validating) {
		validationComplete(callback);
		return;
	}

	console.log('Waiting for validation to complete');

	setTimeout(function() {
		if (! validating) {
			//getValidatedSearchTerms(showAlert, callback);
			validationComplete(callback);
		}
		else {
			waitForValidation(showAlert, callback);
		}
	}, 100);
}

function validationComplete(callback) {
	console.log('Validation complete: searchTermsValid: ' + searchTermsValid);
	if (callback) {
		if (searchTermsValid) {
			callback();
		}
	}
}

function validateGroupId(searchTerms, callback) {
	var groupId = null;

	console.log('Checking for facebook group in: ' + searchTerms);

	if (searchTerms.indexOf("/") == -1) {
		groupId = searchTerms;
	}
	else {
		var match = searchTerms.match(/([\d_]+)/g);
		if (match) {
			console.log("Matched number");
			for (var i=0; i<match.length; i++) {
				console.log(i + ": " + match[i]);
				groupId = match[i];
			}
		}
		else {
			/*match = searchTerms.match(/([\w]+)/);
			if (match) {
				groupId = match[0];
			}*/
			if ( (searchTerms.indexOf("http") != -1) || (searchTerms.indexOf("www") != -1) ) {
				console.log("Matched URL");
				var urlFrags = searchTerms.split('/');
				if (urlFrags.length > 1) {
					groupId = urlFrags[urlFrags.length - 1];
				}
			}
		}
	}

	console.log("groupID = " + groupId);

	if (groupId != null) {
		console.log("Calling getGroupInfo with callback: " + callback);
		getGroupInfo(groupId, callback)
	}
	else {
		alert('Invalid Facebook event/group id or URL: ' + searchTerms);
	}
	
	//return groupId;
}

function enableFbRequests() {
	$("#checkFbDetailsButton").removeAttr('disabled');
	$("#testButton").removeAttr('disabled');
	$("#runButton").removeAttr('disabled');
}

function clearSearchResults() {
	currentSearchRun = null;
	currentTopicsRun = null;
	currentBehaviourRun = null;

	$("#resultsPanel").empty();		
	$("#otherPanel").empty();		
	$("#searchRunResultsWrapper-2").empty();
	$("#searchRunResultsWrapper-3").empty();
	
	/*
	if (!$("#doTopicAnalysis").is(":checked")) 
		$("#searchRunResultsWrapper-2").append("<p>Topics opinion analysis can be turned on in <b>On-the-fly analysis</b> panel on the left.</p>");
	
	if (!$("#doBehaviourAnalysis").is(":checked"))
		$("#searchRunResultsWrapper-3").append("<p>Behaviour analysis can be turned on in <b>On-the-fly analysis</b> panel on the left.</p>");
	*/
}

function clearAnalysisResults(analysisType) {
	if (analysisType) {
		if (analysisType == "topic-opinion") {
			currentTopicsRun = null;
			$("#searchRunResultsWrapper-2").empty();
		}
		else {
			currentBehaviourRun = null;
			$("#searchRunResultsWrapper-3").empty();
		}
	}
	else {
		currentTopicsRun = null;
		currentBehaviourRun = null;
		$("#searchRunResultsWrapper-2").empty();
		$("#searchRunResultsWrapper-3").empty();
	}
}

function initialiseSearchResults(searchName) {
	var sites = $("input:radio:checked[name='sns']").attr('value');

	if (sites == "facebook") {
		//$("#resultsPanel").append("<p>Getting posts for \"" + searchName + "\".</p>");
		$("#resultsPanel").append("<p>Starting " + searchName + ".</p>");
	}
	else {
		//$("#resultsPanel").append("<p>Looking for posts containing \"" + searchName + "\".</p>");
		$("#resultsPanel").append("<p>Starting " + searchName + ".</p>");
	}

	
	if ($("#doTopicAnalysis").is(":checked")) {
		$("#searchRunResultsWrapper-2").append("<p>Waiting for the search to complete.</p>");
	}
	
	if ($("#doBehaviourAnalysis").is(":checked")) {
		$("#searchRunResultsWrapper-3").append("<p>Waiting for the search to complete.</p>");
	}
}

function initialiseAnalysisResults(config) {
	var analysisType = config["analysis.type"];

	clearAnalysisResults(analysisType);

	$("#historyTitle").text("Analysis History");
	showAnalysisHistory(analysisType);

	if (analysisType == "topic-opinion") {
		$tabs.tabs('select', '#' + 'searchRunResultsWrapper-2');
		$("#searchRunResultsWrapper-2").append("<p>Starting topic-opinion analysis.</p>");
	}
	else {
		$tabs.tabs('select', '#' + 'searchRunResultsWrapper-3');
		$("#searchRunResultsWrapper-3").append("<p>Starting behaviour analysis.</p>");
	}
}

function showSearchHistory() {
	$("#historyTitle").text("Search History");
	$("#historyOfSearches").show();
	$("#historyOfAnalysis").hide();
}

function showAnalysisHistory(type) {
	$("#historyTitle").text("Analysis History");
	$("#historyOfSearches").hide();
	$("#historyOfAnalysis").show();
	if (! $("#kendoGridContainer2").data("kendoGrid")) {
		addAnalysisHistoryWidget("all");
	}
	$("#kendoGridContainer2").data("kendoGrid").dataSource.filter({ field: "name", operator: "startswith", value: type, ignoreCase: false });
}

function displayResultsForRun(runId, data) {
	console.log('\nResults for run ' + runId);
	console.log('id = ' + data.id);
	console.log('wId = ' + data.wId);
	console.log('pmid = ' + data.pmid);
	console.log('activityid = ' + data.activityid);
	console.log('runid = ' + data.runid);
	console.log('type = ' + data.type);
	console.log('name = ' + data.name);
	console.log('location = ' + data.location);
	console.log('nResults = ' + data.nResults);
	console.log('minId = ' + data.minId);
	console.log('maxId = ' + data.maxId);
	console.log('collected_at = ' + data.collected_at);
	//console.log('dataAsJson = ' + data.dataAsJson);

	//resultsData = $.parseJSON(data.dataAsJson);
	//console.log('resultsData = ' + resultsData);

	//displayResults(resultsData);

	// Re-use following method from results.js
	showResultsWithId(runId, data);
}

function displayResults(data) {
	$("#resultsPanel").empty();

	$.each(data["results"], function(num, contents){

		var location = "";
		var createdAt = "";
		
		if (contents["location"] != null)
			location = " from " + contents["location"];
		
		if (contents["created_at"] != null) {
			var parsedDate = Date.parse(contents["created_at"]);
			createdAt = " created on " + parsedDate.toString('dddd, MMMM d, yyyy');
		}
		
		$("#resultsPanel").append("<div class=\"widgetTweet\"><img src=\"" + contents["profile_image_url_https"] + "\"><p>" +
				contents["text"] + " by <a href=\"https://twitter.com/" + contents["from_user"] + "\">"  + contents["from_user_name"] + "</a>" +
				location + createdAt + "</p><div class=\"clearfix\"></div></div>");
		
//					console.log(num);
		
//					if (num >= showPosts - 1) {
//						return false;
//					}
	});
}

/*
function extractTwitterUserIdsAndRunKmiAnalysis(data, searchTerms) {
	if (data["results"].length == 1)
		$("#resultsPanel").append("<p>Found 1 post containing \"" + searchTerms + "\":</p>");
	else
		$("#resultsPanel").append("<p>Found " + data["results"].length + " posts containing \"" + searchTerms + "\":</p>");

	var twitterIdsToLookup = "";
	console.log("Number of results: " + data["results"].length);
	
	$.each(data["results"], function(resultCounter, result){
		if (resultCounter < data["results"].length - 1)
			twitterIdsToLookup = twitterIdsToLookup + result["from_user_id"] + ",";
		else
			twitterIdsToLookup = twitterIdsToLookup + result["from_user_id"];
	});
	
	console.log(twitterIdsToLookup);
	lookupTwitterUsers(twitterIdsToLookup, data);
}

function lookupTwitterUsers(twitterIdsToLookup, data) {
	var usersLookupQuery = "https://api.twitter.com/1/users/lookup.json?user_id=" + twitterIdsToLookup + "&include_entities=true&callback=?";

	$("#searchRunResultsWrapper-3").empty();
	$("#searchRunResultsWrapper-3").append("<p>Requesting additional information about users from Twitter.</p>");
	$.ajax({
			url:usersLookupQuery,
			dataType: 'json',	
			type: 'GET',
			contentType: "application/json; charset=utf-8",
			error: function(){
				$("#searchRunResultsWrapper-3").empty();
				$("#searchRunResultsWrapper-3").append("<p>Ooops, looks like Twitter misbehaved. You will have to try again, sorry!</p>");
				canRun = true;
			},
			success:function(userdata) {
				kmiAnalysis(data, userdata);
			}
	});
}
*/

function kmiAnalysis(data, userdata) {
//	console.log(userdata);
	$("#searchRunResultsWrapper-3").empty();
	$("#searchRunResultsWrapper-3").append("<p>Received additional information about users from Twitter, starting analysis.</p>");
	$.ajax({
		  type: 'POST',
		  url: "/home/analysis/kmi/do.json",
		  data: JSON.stringify({postData: data, userData: userdata}),
		  contentType: "application/json; charset=utf-8",
		  success: function(results) {
				displayKmiAnalysisResults(results);
		  },
		  dataType: 'json',
		  mimeType: 'application/json',
		  contentType: 'application/json; charset=utf-8'
	});
}

function kmiAnalysisForRun(runId) {
	console.log("Running behaviour analysis for search run " + runId);

	$("#searchRunResultsWrapper-3").empty();
	$("#searchRunResultsWrapper-3").append("<p>Running analysis...</p>");
	$.get("/home/analysis/kmi/run_data/do.json", { runId: runId }, function(results){
		displayKmiAnalysisResults(runId, results);
	});
}

function displayKmiAnalysisResults(runId, result){
			  console.log(result);
			  canRun = true;
			  $("#searchRunResultsWrapper-3").empty();
			  $("#searchRunResultsWrapper-3").append("<p class=\"userRolesDistributionHeader\">Discussion activity (number of posts in time):</p>");
			  $("#searchRunResultsWrapper-3").append("<div id=\"discussionActivityChart\"></div>");
			  $("#searchRunResultsWrapper-3").append("<div id=\"discussionActivityChartDescription\"></div>");
			  $("#searchRunResultsWrapper-3").append("<hr>");
			  $("#searchRunResultsWrapper-3").append("<p class=\"userRolesDistributionHeader\">User roles distribution:</p>");
			  $("#searchRunResultsWrapper-3").append("<div id=\"userRolesDistributionChart\"></div>");
			  $("#searchRunResultsWrapper-3").append("<div id=\"userRolesDescriptions\"></div>");
			  $("#searchRunResultsWrapper-3").append("<hr>");
			  $("#searchRunResultsWrapper-3").append("<p class=\"buzzHeader\">Top 5 Users to Watch:</p>");
			  $("#searchRunResultsWrapper-3").append("<div id=\"buzzUsersWrapper\"></div>");
			  $("#searchRunResultsWrapper-3").append("<hr>");
			  $("#searchRunResultsWrapper-3").append("<p class=\"buzzHeader\">Top 5 Posts to Watch:</p>");
			  $("#searchRunResultsWrapper-3").append("<div id=\"buzzPostsWrapper\"></div>");
			  
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
			  
//										  console.log(discussionActivityPlotData);
			 userRolesDistributionPlot = jQuery.jqplot('userRolesDistributionChart', [ userRolesPlotData ], {
					seriesDefaults : {
						renderer : jQuery.jqplot.PieRenderer,
						rendererOptions : {
							showDataLabels : true
							
						}
					},
					legend : {
						show : true,
						location : 'e',
						fontSize: '14px',
						showLabels: true
					}
			 });
       
       
       var role = "Broadcaster"; 
       $("#userRolesDescriptions").append("<p><b>Role: " + role +"</b></p>");

			 $("#userRolesDescriptions").append(
       "<p>Broadcaster is someone who posts with high daily rate and has a very high following." + 
         " However he follows very few people, if any at all.</p>");

       var broadcasters = result["broadcasters"];
       if (broadcasters != null) {
        var users = broadcasters["users"];
        if (users.length < 1) {
          $("#userRolesDescriptions").append("<p>No users found with this role.</p>");
        }
        else {
          var usersAsString = "";
          $.each(users, function(counter, kmiuser){
            usersAsString = usersAsString 
              + "<a target=\"_blank\" href=\"https://twitter.com/#!/" 
              + kmiuser["screenName"] + "\">" + kmiuser["name"] + "</a>";

            if (counter != users.length - 1)
              usersAsString = usersAsString + ", ";
          });

          if (users.length == 1)
            $("#userRolesDescriptions").append("<p>Found 1 user</p>");
          else
            $("#userRolesDescriptions").append("<p>Found " + users.length + " users</p>");

          $("#userRolesDescriptions").append("<p>" + usersAsString + "</p>");
        }
       }
       
       
       role = "Daily User"; 
       $("#userRolesDescriptions").append("<p><b>Role: " + role +"</b></p>");
       
			 $("#userRolesDescriptions").append(
       "<p>Daily User is someone with middle of the ground stats.</p>");
      // getUsersForRole("#userRolesDescriptions", result, "Daily User");
      
      var dailyUsers = result["dailyUsers"];
      if (dailyUsers != null) {
        var users = dailyUsers["users"];
        if (users.length < 1) {
          $("#userRolesDescriptions").append("<p>No users found with this role.</p>");
        }
        else {
          var usersAsString = "";
          $.each(users, function(counter, kmiuser){
            usersAsString = usersAsString 
              + "<a target=\"_blank\" href=\"https://twitter.com/#!/" 
              + kmiuser["screenName"] + "\">" + kmiuser["name"] + "</a>";

            if (counter != users.length - 1)
              usersAsString = usersAsString + ", ";
          });

          if (users.length == 1)
            $("#userRolesDescriptions").append("<p>Found 1 user</p>");
          else
            $("#userRolesDescriptions").append("<p>Found " + users.length + " users</p>");

          $("#userRolesDescriptions").append("<p>" + usersAsString + "</p>");
        }
       }      

       role = "Information Seeker"; 
       $("#userRolesDescriptions").append("<p><b>Role: " + role +"</b></p>");

			 $("#userRolesDescriptions").append(
       "<p>Information Seeker is someone who posts very rarely but follows a lot of people.</p>");

      var informationSeekers = result["informationSeekers"];
      if (informationSeekers != null) {
        var users = informationSeekers["users"];
        if (users.length < 1) {
          $("#userRolesDescriptions").append("<p>No users found with this role.</p>");
        }
        else {
          var usersAsString = "";
          $.each(users, function(counter, kmiuser){
            usersAsString = usersAsString 
              + "<a target=\"_blank\" href=\"https://twitter.com/#!/" 
              + kmiuser["screenName"] + "\">" + kmiuser["name"] + "</a>";

            if (counter != users.length - 1)
              usersAsString = usersAsString + ", ";
          });

          if (users.length == 1)
            $("#userRolesDescriptions").append("<p>Found 1 user</p>");
          else
            $("#userRolesDescriptions").append("<p>Found " + users.length + " users</p>");

          $("#userRolesDescriptions").append("<p>" + usersAsString + "</p>");
        }
       }      



       
       role = "Information Source"; 
       $("#userRolesDescriptions").append("<p><b>Role: " + role +"</b></p>");
       
			 $("#userRolesDescriptions").append("<p>Information Source is someone who posts a lot, is followed a lot but follows more people than the Broadcaster.</p>");

      var informationSources = result["informationSources"];
      if (informationSources != null) {
        var users = informationSources["users"];
        if (users.length < 1) {
          $("#userRolesDescriptions").append("<p>No users found with this role.</p>");
        }
        else {
          var usersAsString = "";
          $.each(users, function(counter, kmiuser){
            usersAsString = usersAsString 
              + "<a target=\"_blank\" href=\"https://twitter.com/#!/" 
              + kmiuser["screenName"] + "\">" + kmiuser["name"] + "</a>";

            if (counter != users.length - 1)
              usersAsString = usersAsString + ", ";
          });

          if (users.length == 1)
            $("#userRolesDescriptions").append("<p>Found 1 user</p>");
          else
            $("#userRolesDescriptions").append("<p>Found " + users.length + " users</p>");

          $("#userRolesDescriptions").append("<p>" + usersAsString + "</p>");
        }
       }      




       role = "Rare Poster"; 
       $("#userRolesDescriptions").append("<p><b>Role: " + role +"</b></p>");
              
			 $("#userRolesDescriptions").append("<p>RarePoster is somebody who hardly ever posts.</p>");
      
      var rarePosters = result["rarePosters"];
      if (rarePosters != null) {
        var users = rarePosters["users"];
        if (users.length < 1) {
          $("#userRolesDescriptions").append("<p>No users found with this role.</p>");
        }
        else {
          var usersAsString = "";
          $.each(users, function(counter, kmiuser){
            usersAsString = usersAsString 
              + "<a target=\"_blank\" href=\"https://twitter.com/#!/" 
              + kmiuser["screenName"] + "\">" + kmiuser["name"] + "</a>";

            if (counter != users.length - 1)
              usersAsString = usersAsString + ", ";
          });

          if (users.length == 1)
            $("#userRolesDescriptions").append("<p>Found 1 user</p>");
          else
            $("#userRolesDescriptions").append("<p>Found " + users.length + " users</p>");

          $("#userRolesDescriptions").append("<p>" + usersAsString + "</p>");
        }
       }      
      
      
      
      
			 
			 discussionActivityPlot = $.jqplot('discussionActivityChart', [discussionActivityPlotData],{
				 axes:{xaxis:{renderer:$.jqplot.DateAxisRenderer, tickOptions:{formatString:'%b %#d<br/> %T'}},
					 yaxis:{
						 tickOptions: { 
								min: 0,
								formatString: '%d' 
							}
					 }
				 },
				 highlighter: {
						show: true,
						sizeAdjust: 7.5
					  },
					  cursor: {
						show: true,
						zoom: true,
						showTooltip: false
					  }
			 });
			 
			 $("#discussionActivityChartDescription").append("<p>Discussion activity shows the number of posts published over time.</p>");
			 
			 // Process buzz users
			 var buzzUserContainer;
			 var score = "";
			 var role = "";
			 $.each(result["buzzUsers"], function(userCount, buzzUser){
				 if (buzzUser != null) {
					 
					 // Sort out missing scores and roles
					 if (buzzUser["role"].length > 0)
						 role = ", role: " + buzzUser["role"];
					 
					 if (buzzUser["buzzScore"] > 0.001)
						 score = "Score: " + buzzUser["buzzScore"];
					 else {
						 if (buzzUser["role"].length > 0)
							 role = "Role: " + buzzUser["role"];
						 else
							 role = "Unable to determine score or role";
					 }
					 
					 buzzUserContainer = $("<div class=\"buzzUserContainer\"></div>").appendTo("#buzzUsersWrapper");
					 buzzUserContainer.append("<img src=\"" + buzzUser["profileImageUrl"] + "\">");
					 buzzUserContainer.append("<p><b>" + buzzUser["name"] + "</b> (" + buzzUser["screenName"] + ")</p>");
					 buzzUserContainer.append("<p>" + score + role + "</p>");
					 buzzUserContainer.append("<p class=\"buzzUserDescription\">Description: " + buzzUser["description"] + "</p>");
					 buzzUserContainer.append("<div class=\"clearfix\"></div>");
				 }
			 });
			 
			 // Process buzz posts
			 var buzzPostContainer;
			 score = "";
			 $.each(result["buzzPosts"], function(postCount, buzzPost){
				 if (buzzPost != null) {
					 var parsedDate = Date.parse(buzzPost["createdAt"]);
					 createdAt = " created on " + parsedDate.toString('dddd, MMMM d, yyyy');
					 
					 if (buzzPost["buzzScore"] > 0.001) {
						 score = " with score " + buzzPost["buzzScore"];
					 }
					 buzzUserContainer = $("<div class=\"buzzPostContainer\"></div>").appendTo("#buzzPostsWrapper");
					 buzzUserContainer.append("<p>" + buzzPost["textContent"] +
							 " by <u>" + buzzPost["userFullName"] + "</u>" + createdAt + score + "</p>");
				 }
			 });
}

function getUsersForRole(listOfUserWithRoleDiv, result, role) {



  var term = "none";
  var roleId = 0;
  
  var userdata = undefined;
  
  if ( role.toLowerCase() == "Broadcaster".toLowerCase() ) {
    userdata = result["broadcasters"]; 
  }
  else if ( role.toLowerCase() == "Daily User".toLowerCase() ) {
    userdata = result["dailyUsers"]; 
  }
  else if ( role.toLowerCase() == "Information Seeker".toLowerCase() ) {
    userdata = result["informationSeekers"]; 
  }
  else if ( role.toLowerCase() == "Information Source".toLowerCase() ) {
    userdata = result["informationSources"]; 
  }
  else if ( role.toLowerCase() == "Rare Poster".toLowerCase() ) {
    userdata = result["rarePosters"]; 
  }
  else { // defualt to daily user
   userdata = result["dailyUsers"]; 
  }

  console.log(userdata);
  //listOfUserWithRoleDiv.append("<p>Running analysis</p>");
    //listOfUserWithRoleDiv.empty();
    var users = userdata["users"];
    if (users.length < 1) {
      $("#listOfUserWithRoleDiv").append("<p>No users found with this role.</p>");
    }
    else {
      var usersAsString = "";
      $.each(users, function(counter, kmiuser){
        usersAsString = usersAsString + "<a target=\"_blank\" href=\"https://twitter.com/#!/" + kmiuser["screenName"] + "\">" + kmiuser["name"] + "</a>";

        if (counter != users.length - 1)
          usersAsString = usersAsString + ", ";
      });

      $("#listOfUserWithRoleDiv").append("<p><b>Role: " + role +"</b></p>");

      if (users.length == 1)
        $("#listOfUserWithRoleDiv").append("<p>Found 1 user</p>");
      else
        $("#listOfUserWithRoleDiv").append("<p>Found " + users.length + " users</p>");

      $("#listOfUserWithRoleDiv").append("<p>" + usersAsString + "</p>");
    }

  
}



function getUsersForRoleOld(listOfUserWithRoleDiv, runId, role) {

  // this version has the wrong run ID passed in - this version needs the search results - not the run id of the behaviour analysis

  var term = "none";
  var roleId = 0;
  
  if ( role.toLowerCase() == "Broadcaster".toLowerCase() ) { roleId = 1; }
  else if ( role.toLowerCase() == "Daily User".toLowerCase() ) { roleId = 2; }
  else if ( role.toLowerCase() == "Information Seeker".toLowerCase() ) { roleId = 3; }
  else if ( role.toLowerCase() == "Information Source".toLowerCase() ) { roleId = 4; }
  else if ( role.toLowerCase() == "Rare Poster".toLowerCase() ) { roleId = 5; }
  else { // defualt to daily user
    role = "Daily User";
    roleId = 2;
  }
/*
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

  */    

	//listOfUserWithRoleDiv.append("<p>Getting search results from WeGov DB...</p>");

  var analysisUrl =
    "/home/analysis/kmi/onlyroles/run_data/do.json"
    //"/home/analysis/kmi/twitter/widget_data/do.json"
    + "?runId=" + runId
    + "&selectedRoleName=" + roleId
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
      //listOfUserWithRoleDiv.append("<p>Running analysis</p>");
        //listOfUserWithRoleDiv.empty();
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

          listOfUserWithRoleDiv.append("<p><b>Role: " + role +"</b></p>");

          if (users.length == 1)
            listOfUserWithRoleDiv.append("<p>Found 1 user</p>");
          else
            listOfUserWithRoleDiv.append("<p>Found " + users.length + " users</p>");

          listOfUserWithRoleDiv.append("<p>" + usersAsString + "</p>");
        }
      }
    });
}


function koblenzAnalysis(data) {
	$("#searchRunResultsWrapper-2").empty();
	$("#searchRunResultsWrapper-2").append("<p>Running analysis, please wait.</p>");
	$.ajax({
		  type: 'POST',
		  url: "/home/analysis/koblenz/do.json",
//							  data: JSON.stringify({postData: data, userData: userdata}),
		  data: JSON.stringify(data),
		  contentType: "application/json; charset=utf-8",
//							  data: {postData: data, userData: userdata},
		  error: function() {
				$("#searchRunResultsWrapper-2").empty();
				$("#searchRunResultsWrapper-2").append("<p>Ooops, something went wrong with the analysis. You will have to try again, sorry!</p>");
				canRun = true;
		  },
		  success: function(results){
			  displayKoblenzAnalysisResults(results);
		  },
		  dataType: 'json',
		  mimeType: 'application/json',
		  contentType: 'application/json; charset=utf-8'
		});
}

function koblenzAnalysisForRun(runId) {
	console.log("Running topic analysis for search run " + runId);
	$("#searchRunResultsWrapper-2").empty();
	$("#searchRunResultsWrapper-2").append("<p>Running analysis...</p>");
	$.get("/home/analysis/koblenz/run_data/do.json", { runId: runId }, function(results){
		//displayKoblenzAnalysisResults(runId, results);
		setTopicAnalysisResults(results, displayTopicsTable);
	});
}

function displayKoblenzAnalysisResultsOld(runId, result) {
			  console.log(result);
			  canRun = true;
			  $("#searchRunResultsWrapper-2").empty();
			  if (! result["numTopicsAsString"]) {
				  $("#searchRunResultsWrapper-2").append("<p>Ooops, something went wrong with the analysis. You will have to try again, sorry!</p>");
				  return false;
			  }

			  var furtherAnalysisLink = '<a href="topics-opinions.html?runId=' + runId + '">Further analysis</a>';
			  
			  $("#searchRunResultsWrapper-2").append("<p class=\"koblenzResultsHeader\">Found " + result["numTopicsAsString"] + ": " + furtherAnalysisLink + "</p>");
			  $.each(result["topics"], function(counter, topic){
				  
				  $("#searchRunResultsWrapper-2").append("<p class=\"koblenzTopicKeywords\">" + (counter + 1) + ". " + topic["keywords"] + "</p>");
				  $("#searchRunResultsWrapper-2").append("<p class=\"koblenzTopicMetrics\">" + "Valence: " + topic["valence"] + " Controversy: " + topic["controversy"]+ "</p>");
				  $("#searchRunResultsWrapper-2").append("<p class=\"koblenzTopicKeyusersHeader\">Key Users:</p>");
				  
				  $.each(topic["keyUsers"], function(keyUserCounter, keyUser){
					  var keyUserEntry = $("<div class=\"koblenzUserWrapper\"></div>").appendTo("#searchRunResultsWrapper-2");
					  keyUserEntry.append("<img src=\"" + keyUser["profileImageUrl"] + "\">");
					  keyUserEntry.append("<p>" + keyUser["fullName"] + " (" + keyUser["screenName"] + ")</p>");
					  keyUserEntry.append("<div class=\"clearfix\"></div>");
				  });
				  $("#searchRunResultsWrapper-2").append("<p class=\"koblenzTopicKeyPostHeader\">Key Posts:</p>");
				  $.each(topic["keyPosts"], function(keypostCounter, keyPost){
					  var keyPostEntry = $("<div class=\"koblenzPostWrapper\"></div>").appendTo("#searchRunResultsWrapper-2");
					  
					  var parsedDate = Date.parse(keyPost["createdAt"]);
					  createdAt = "" + parsedDate.toString('dddd, MMMM d, yyyy');
//								  topic["keyPostScores"].get(keypostCounter) + 
					  keyPostEntry.append("<p><i>" + createdAt + " | Score " + keyPost["score"] + "</i><br>" + keyPost["text"] + "</p>");
					  keyPostEntry.append("<div class=\"clearfix\"></div>");
				  });
			  });

			  // Display topic distances (prototype)
			  displayTopicDistances(result);
}

function displayTopicDistances(result) {
	var distances = result["topicDistances"];
	console.log("Topic distances:");

	$("#searchRunResultsWrapper-2").append("<h3>Topic Distances</h3>");
	$.each(distances, function(i, distanceRow) {
		console.log(distanceRow);
		var formattedRow = "";
		$.each(distanceRow, function(j, distanceCol) {
			formattedRow += distanceCol + " ";
		});
		$("#searchRunResultsWrapper-2").append("<p>" + formattedRow + "</p>");
	});
}

//function displayKoblenzAnalysisResults(runId, result) {
//	$("#searchRunResultsWrapper-2").append('<label id="topicAnalysisLabel" class="searchForLabel"><span lang="en">Topics Summary</span></label>');
//	addTopicsTable3(runId);
//}

function displayTopicsTable() {
	console.log("displayTopicsTable");
	console.log(topicAnalysisResult);

	$("#searchRunResultsWrapper-2").empty();
	$("#searchRunResultsWrapper-2").append('<h2>Topics Summary</h2>');
	$("#searchRunResultsWrapper-2").append('<div id="topicsTable2"></div>');
	addTopicsTable3();
}

function createTestGetTopicAnalysisResultButton() {
	var runIdInput =
		$("<input type=\"text\" value=0 id=\"testGetTopicAnalysisResultButton\">").appendTo("#searchRunResultsWrapper-2");

	var testGetTopicAnalysisResultButton =
		$("<a id=\"testGetTopicAnalysisResultButton\" href=# class=\"clickableWidgetHeader\">"
		+ "TESTING ONLY: Get Topic Results from DB for run in box" + "</a>")
		.appendTo("#searchRunResultsWrapper-2");

		testGetTopicAnalysisResultButton.click(function(e){

			runIdIn = runIdInput.attr('value');

			//getRunParameters(runIdIn);

			//topicAnalysisResult = getTopicAnalysisResults(runIdIn);
			getTopicAnalysisResults(runIdIn, displayTopicsTable);
		});
}

function createAnalyseCurrentRunButton(analysisOption, analysisSNS, activityId, runId) {
	var analysisType;
	var postType;
	var panel = "#resultsPanel";
	var label;

	if (analysisOption == "topics") {
		analysisType = "Topics";
		panel = "#searchRunResultsWrapper-2";
	}
	else {
		analysisType = "Behaviour";
		panel = "#searchRunResultsWrapper-3";
	}

	if (analysisSNS == "twitter") {
		postType = "Tweets";
	}
	else {
		postType = "Posts & Comments";
	}

	if (runId == -1) {
		label = "Analyse " + analysisType + " for all " + postType + " from this Search (all runs)";
	}
	else {
		label = "Analyse " + analysisType + " for all " + postType + " from this Search run (" + runId + ")";
	}

    var button = $("<p><a href=# class=\"clickableWidgetHeader\"></p>" + label + "</a>").appendTo(panel);

	button.click(function(e){
		analyseActivitiesAndRuns(analysisOption, analysisSNS, activityId, runId);
    });

}

function setupTabs() {
	$("#resultsPanel").empty();
	$("#searchRunResultsWrapper-2").empty();
	$("#searchRunResultsWrapper-3").empty();
	
	$("#resultsPanel").append("<p>Search results will appear here...</p><p>Type in a keyword in the Search Params tab and click <b>Run now</b> button to start search.</p>")
		.append("<p>Alternatively, click on a previous search run below to see the results.</p>")
		.append('<div id="fbStatus"></div>');

	$("#searchRunResultsWrapper-2").append("<p>Topics opinion analysis results will appear here automatically once you have searched for something.</p>")
		.append("<p>(You can turn this feature off in <b>On-the-fly analysis</b> panel in the Search Params tab)</p>")
		.append("<p>Alternatively, click on a previous analysis run below to see the results.</p>");
	//createTestGetTopicAnalysisResultButton(); // FOR DEV ONLY

	$("#searchRunResultsWrapper-3").append("<p>Behaviour analysis results will appear here automatically once you have searched for something.</p>")
		.append("<p>(You can turn this feature off in <b>On-the-fly analysis</b> panel in the Search Params tab)</p>")
		.append("<p>Alternatively, click on a previous analysis run below to see the results.</p>");
	
	$("#searchResultsTabSNS").text("SNS");

}

function addSearchesWidget(whichActivities) {
	console.log("addSearchesWidget");
	var myDiv = $("#historyOfSearches");
	
//	var myHeader;
//	if (whichActivities == "all") {
//		myHeader = $("<h2 class=\"widgetHeader\">All My Activities</h2>").appendTo(myDiv);
//	} else {
//		myHeader = $("<h2 class=\"widgetHeader\">My Activities</h2>").appendTo(myDiv);
//	}
	
	var myContainer = $("<div class=\"widgetContainer\" id=\"kendoGridContainer\"></div>").appendTo(myDiv);
	
	myContainer.kendoGrid({
        dataSource: {
            type: "json",
            serverPaging: false,
            serverSorting: false,
            transport: {
                read: "/home/getsearches/do.json" 
            },
            schema: {
            	data: function(result) {
                    return result.data || result;
	              },
	              total: function(result) {
	                    return result.total || result.length || 0;
	              }            	
//                data: "data",
//                total: "total"
            },
			filter: { field: "name", operator: "startswith", value: "Twitter" }, // filter Twitter results initially
            sort: {
            	field: "whenCreated", dir : "desc"
            }
            //pageSize: 10
        },
        columns: [
                  {title: " ", field: "id", width: "40px", template: '#= formatCheckbox("activity", id, name) #'},
                  {title: "ID", field: "id", width: "40px"},
                  {title: "Name", field: "name"},
                  {title: "Status", field: "status", width: "80px"},
                  {title: "Created on", field: "whenCreated", width: "160px"},
                  {title: "Next Start Time", field: "nextStartTime", width: "160px"},
        ],
//        height: auto,
        //detailInit: detailInit,
		detailInit: function(e) {
			detailInit(e, "search");
		},
        dataBound: function() {
            //this.expandRow(this.tbody.find("tr.k-master-row").first());
			var checkboxes = this.tbody.find("input:checkbox[name='activity']");
			console.log(checkboxes);
			checkboxes.click(activityCheckboxClicked);
			expandActivityRowForCurrentRun(this);
			console.log("Activity metadata:");
			console.log(JSON.stringify(activityMetadata));
        },               
        change: onSearchResultsGridChange,
        filterable: true,
        sortable: true,
        pageable: false,
        groupable: true,
        //selectable: "multiple row" // TODO: use this for selecting multiple rows for analysis
        //selectable: "row"
        selectable: false
    });

	$("#autoRefreshSearchHistory").click(function(e){
		console.log("autoRefreshSearchHistory clicked");
		if ($(this).is(":checked")) {
			startMonitoring();
		}
		else {
			stopMonitoring();
		}
	});
}

function addAnalysisHistoryWidget(whichActivities) {
	console.log("addAnalysisHistoryWidget");
	var myDiv = $("#historyOfAnalysis");
	
	var myContainer = $("<div class=\"widgetContainer\" id=\"kendoGridContainer2\"></div>").appendTo(myDiv);
	
	myContainer.kendoGrid({
        dataSource: {
            type: "json",
            serverPaging: false,
            serverSorting: false,
            transport: {
                read: "/home/getanalysises/do.json" 
            },
            schema: {
            	data: function(result) {
                    return result.data || result;
	              },
	              total: function(result) {
	                    return result.total || result.length || 0;
	              }            	
//                data: "data",
//                total: "total"
            },
            sort: {
            	field: "whenCreated", dir : "desc"
            }
            //pageSize: 10
        },
        columns: [
                  //{title: " ", field: "id", width: "40px", template: '#= formatCheckbox("activity", id, name) #'},
                  {title: "ID", field: "id", width: "40px"},
                  {title: "Name", field: "name"},
                  {title: "Status", field: "status", width: "80px"},
                  {title: "Created on", field: "whenCreated", width: "160px"},
                  {title: "Next Start Time", field: "nextStartTime", width: "160px"},
        ],
//        height: auto,
        detailInit: detailInit,
        dataBound: function() {
            //this.expandRow(this.tbody.find("tr.k-master-row").first());
			expandActivityRowForCurrentRun(this);

			//console.log("Activity metadata:");
			//console.log(JSON.stringify(activityMetadata));
        },               
        change: onSearchResultsGridChange,
        filterable: true,
        sortable: true,
        pageable: false,
        groupable: true,
        //selectable: "multiple row" // TODO: use this for selecting multiple rows for analysis
        //selectable: "row"
        selectable: false
    });

	/*
	$("#autoRefreshSearchHistory").click(function(e){
		console.log("autoRefreshSearchHistory clicked");
		if ($(this).is(":checked")) {
			startMonitoring();
		}
		else {
			stopMonitoring();
		}
	});
	*/
}

function expandActivityRowForCurrentRun(grid) {
	var currRun;
	var historyType = getCurrentHistoryType();

	if (monitoredRun) {
		currRun = monitoredRun;
	}
	if (historyType == "search") {
		currRun = currentSearchRun;
	}
	else if (historyType == "topics") {
		currRun = currentTopicsRun;
	}
	else if (historyType == "behaviour") {
		currRun = currentBehaviourRun;
	}

	console.log("Current selected " + historyType + " run: " + currRun);

	if (currRun) {
		console.log("Getting activity for run " + currRun);

		var metadata = runMetadata[currRun];

		if (metadata) {
			console.log(JSON.stringify(metadata));
			var activityId = metadata.activity;
			console.log("Run " + currRun + " for Activity " + activityId);
			var row = getRowForActivity(grid, activityId);
			console.log("Expanding row for activity " + activityId);
			if (row) {
				grid.expandRow(row);
			}
			else {
				console.log("WARNING: could not locate row for activity " + activityId);
			}
		}
		else {
			console.log("WARNING: no metadata available for run " + currRun);
		}
	}
	else {
		console.log("Expanding first row");
		grid.expandRow(grid.tbody.find("tr.k-master-row").first());
	}

}

function getRowForActivity(grid, activityId) {
	console.log("getRowForActivity " + activityId);

	var activityRow;

	var rows = grid.tbody.find(">tr");
	console.log(rows);

	rows.each(function(i) {
		var data = grid.dataItem($(this));
		if (data.id == activityId) {
			console.log(data);
			activityRow = $(this);
			return false;
		}
	});

	console.log(activityRow);
	return activityRow;
}

var activityMetadata = {};

function formatCheckbox(type, id, name) {
	//console.log("Name: " + name);
	var nameArray = name.split(" ");
	var sns = nameArray[0].toLowerCase();
	//console.log("SNS: " + sns);
	var checkboxId = type + "-" + id;

	activityMetadata[checkboxId] = {sns: sns};

	return '<input type="checkbox" id="' + checkboxId + '" name="' + type + '" value="' + id + '"/>';
}

function getScheduledJobs() {
	console.log("Getting scheduled jobs...");
	$("#kendoScheduledJobsGridContainer").data("kendoGrid").dataSource.read();
}

function refreshSearchHistory() {
	monitoring = $("#autoRefreshSearchHistory").is(":checked");
	//checkRunStatus();
	updateSearchesList(true); // force update
}

var monitoring = false;
var monitoredRun = null;
var monitoredRunConfig = null;
var monitorDelay = 10000;
var monitorCancelled = false;

function monitorRun(runId, config) {
	if (monitoring) {
		console.log("Cannot monitor run " + runId + ". Already monitoring run " + monitoredRun);
		return;
	}

	if (runId != null) {
		monitoredRun = runId;
		monitoredRunConfig = config;
		console.log("Monitoring run: " + runId);
	}
	$("#autoRefreshSearchHistory").attr('checked', 'checked');
	startMonitoring();
}

function startMonitoring() {
	console.log("startMonitoring");
	$("#autoRefreshSearchHistory").attr('checked', 'checked');
	monitorCancelled = false;
	if (monitoring) return;
	monitoring = true;
	updateSearchesList(true);
}

function stopMonitoring() {
	console.log("stopMonitoring");
	monitoring = false;
	monitorCancelled = true;
	$("#autoRefreshSearchHistory").removeAttr('checked');
}

function checkRunStatus() {
	if ( (!monitoring) || (monitoredRun == null) ){
		console.log("checkRunStatus: not currently monitoring a run");
		return;
	}
	console.log("Checking status for run: " + monitoredRun);
	$.get("/home/search/getSearchStatus/do.json", { runId: monitoredRun }, function(response){
		console.log("Status of run " + monitoredRun + ": " + response);
		if ( (response == "finished") || (response == "failed") ) {
			runFinished(response);
		}
	});
}

function runFinished(response) {
	var runId = monitoredRun;
	var config = monitoredRunConfig;
	var sns = config.sites;

	monitoredRun = null;
	monitoredRunConfig = null;

	console.log("Run finished: " + runId);

	if (monitoring) {
		console.log("Stop monitoring run " + runId);
		stopMonitoring();
		canRun = true;
		//alert("Run " + runId + " finished!");
		var runObject = {runId: runId, results: sns, status: response};
		getResultsForRun(runObject, config);
	}
	else {
		console.log("Not updating results for run " + runId + " (no longer monitoring)");
	}

}

function reportSearchError(error, config, showAlert) {
	console.log(error);
	if (config && config.runNow) {
		clearSearchResults();
		$("#resultsPanel").append("<p>" + error + "</p>");
		canRun = true;
	}
	if (showAlert) {
		alert(error);
	}
}

var timerSet = false;

function updateSearchesList(forceUpdate) {
	console.log("updateSearchesList: forceUpdate = " + forceUpdate);
	console.log("updateSearchesList: monitorCancelled = " + monitorCancelled);
	if (monitorCancelled) {
		monitorCancelled = false;
		if (! forceUpdate)
			return;
	}

	checkRunStatus();

	var config = monitoredRunConfig;

	var selectedTab = $tabs.tabs('option', 'selected');
	console.log("Selected tab: " + selectedTab);

	if ( (selectedTab === 0) || (selectedTab === 1) ) {
		// Update list of searches
		console.log("Updating searches list");
		console.log("monitoring: " + monitoring);
		$("#kendoGridContainer").data("kendoGrid").dataSource.read();
	}
	else {
		// Update list of analyses
		console.log("Updating analyses list");
		console.log("monitoring: " + monitoring);
		if ($("#kendoGridContainer2").data("kendoGrid")) {
			$("#kendoGridContainer2").data("kendoGrid").dataSource.read();
		}
		else {
			addAnalysisHistoryWidget("all");
		}
	}

	if (monitoring && (!timerSet)) {
		var refreshDelaySecs = $("#refreshDelaySecs").val();
		if (refreshDelaySecs == "") {
			console.log("WARNING: refreshDelaySecs is empty");
		}
		else {
			if (isNaN(refreshDelaySecs)) {
				console.log("WARNING: refreshDelaySecs is not a number: " + refreshDelaySecs);
				console.log($("#refreshDelaySecs"));
				console.log("Setting back to 10 secs");
				$("#refreshDelaySecs").val(10);
				refreshDelaySecs = 10;
			}
			monitorDelay = refreshDelaySecs * 1000;
			console.log("waiting for " + monitorDelay + " ms");
			setTimeout(timerExpired, monitorDelay);
			timerSet = true;
		}
	}
}

function timerExpired() {
	timerSet = false;
	updateSearchesList();
}

var selectedGrid = {};
var selectedRow = {};
var selecting = false;

var currentSearchRun;
var currentTopicsRun;
var currentBehaviourRun;

function getCurrentHistoryType() {
	var selectedTab = $tabs.tabs('option', 'selected');
	console.log("Selected tab: " + selectedTab);

	if ( (selectedTab == 0) || (selectedTab == 1) ) {
		historyType = "search";
	}
	else if (selectedTab == 2) {
		historyType = "topics";
	}
	else if (selectedTab == 3) {
		historyType = "behaviour";
	}
	console.log("History type: " + historyType);

	return historyType;
}

function onSearchResultsGridChangeDelayed() {
	console.log("onSearchResultsGridChangeDelayed");
	var context = this;
	setTimeout(function() {
		onSearchResultsGridChange(context);
	}, 100);
}

var reselectingRow = false;
var clearSelectionCalled = false;

function onSearchResultsGridChange(context) {
	console.log("onSearchResultsGridChange");

	if (reselectingRow) {
		console.log("Reselecting same row - ignoring request");
		reselectingRow = false;
		return;
	}

	if (clearSelectionCalled) {
		console.log("Clear selection called - ignoring request");
		clearSelectionCalled = false;
		return;
	}

	var ths = context;

	if (! ths) {
		ths = this;
	}

	console.log("ths:");
	console.log(ths.tbody);

	console.log("disableRowSelection: " + disableRowSelection);

	if (disableRowSelection) {
		disableRowSelection = false;
		//selectRowIfCurrentRun(gridId);
		var historyType = getCurrentHistoryType();
		if (selectedGrid[historyType] && (selectedGrid[historyType] == ths)) {
			ths = selectedGrid[historyType];
			reselectingRow = true;
			ths.select(selectedRow[historyType]);
			return;
		}
		else {
			clearSelectionCalled = true;
			ths.clearSelection();
			return;
		}
	}

	//console.log(JSON.stringify(context));
	//console.log(JSON.stringify(this));
	//console.log("onSearchResultsGridChange: " + arg);
	//console.log(arg);

	//var selected = $.map(this.select(), function(item) {
    //    return $(item);
    //});
	console.log("onSearchResultsGridChange: selecting = " + selecting);
	if (selecting) {
		console.log("onSearchResultsGridChange: already selecting a row - ignoring request");
		return;
	}

	console.log("onSearchResultsGridChange: setting selecting to true");
	selecting = true;

	var historyType = getCurrentHistoryType();	
	
	if (selectedGrid[historyType]) {
		console.log("Current selected grid: ");
		console.log(selectedGrid[historyType].tbody);
		console.log("Current selected row: ");
		console.log(selectedRow[historyType]);
		if (ths != selectedGrid[historyType]) {
			console.log("Selected a different grid - clearing previous selection...");
			clearSelectionCalled = true;
			selectedGrid[historyType].clearSelection();
			console.log("New selected grid: ");
			console.log(ths.tbody);
		}
		else {
			console.log("Selected same grid");
		}
	}
	else {
		console.log("(no rows currently selected)");
	}

	var rows = ths.select();
	console.log("Number of selected rows: " + rows.length);

	if (rows.length == 0) {
		selecting = false;
		return;
	}

	var row = rows[0];
	console.log("New selected row: ");
	console.log(row);
	selectedGrid[historyType] = ths;
	selectedRow[historyType] = row;

	var clas = $(row).attr('class');
	//console.log("Class: " + clas);

	if (clas.indexOf("k-master-row") >= 0) {
		//var idTd = $(row).children('td')[2];
		//var activityId = $(idTd).text();
		//selectedActivity = activityId;
		//console.log("Selected activity: " + selectedActivity);
	}
	else {
		// First, clear all tabs, as we don't want to display analysis results for a different search that might already be displayed
		clearSearchResults();

		var idTd = $(row).children('td')[1];
		var statusTd = $(row).children('td')[2];
		var commentTd = $(row).children('td')[3];
		var startedTd = $(row).children('td')[4];
		var finishedTd = $(row).children('td')[5];
		var resultsTd = $(row).children('td')[6];

		var runId = $(idTd).text();
		var status = $(statusTd).text();
		var comment = $(commentTd).text();
		var started = $(startedTd).text();
		var finished = $(finishedTd).text();
		var results = $(resultsTd).text();

		console.log("Selected run id: " + runId);
		console.log("runId="+runId);
		//console.log("name="+name);
		console.log("status="+status);
		console.log("comment="+comment);
		console.log("started="+started);
		console.log("finished="+finished);
		console.log("results="+results);

		runObject = {
			runId: runId,
			name: name,
			status: status,
			comment: comment,
			started: started,
			finished: finished,
			results: results
		};

		if ( (status == "finished") || (status == "failed") ) {
			/*
			if (results != "") {
				//getResultsForRun(runId);
				getResultsForRun(runObject);
			}
			else {
				console.log("WARNING: run finished, but no results available");
			}
			*/
			//console.log("getResultsForRun:");
			//console.log(JSON.stringify(runObject));
			getResultsForRun(runObject);
		}
	}

	//var selectedData = $("#kendoGridContainer").data("kendoGrid"); //.dataItem(row);
	//console.log(selectedData);
	//console.log("Selected activity or run with id:" + selectedData.id + ", created on: " + selectedData.whenCreated);
	console.log("onSearchResultsGridChange: setting selecting to false");
	selecting = false;

}

/* If we have a config here, then results are for a monitored run.
   Otherwise we have just clicked on a search or analysis run in the table
*/
function getResultsForRun(runObject, config) {
	console.log(JSON.stringify(runObject));
	var runId = runObject.runId;
	if (runId == undefined) return;
	if (runId == "") return;

	var activityId;

	console.log("getResultsForRun: " + runId);
	if ( (currentSearchRun == runId) || (currentTopicsRun == runId) || (currentBehaviourRun == runId)) {
		console.log("WARNING: already getting results for run " + runId + " - ignoring request");
		return;
	}

	var metadata = runMetadata[runId];

	if (metadata) {
		console.log(JSON.stringify(metadata));
		activityId = metadata.activity;
	}
	else {
		console.log("WARNING: no metadata available for run " + runId);
	}

	console.log("Run " + runId + " for Activity " + activityId);

	var status = runObject.status;

	var runType;

	var updateHistory = false;

	if (! config) {
		var selectedTab = $tabs.tabs('option', 'selected');
		if ( (selectedTab === 0) || (selectedTab === 1) ) {
			runType = "search";
		}
		else if (selectedTab === 2) {
			runType = "topic-opinion";
		}
		else if (selectedTab === 3) {
			runType = "behaviour";
		}

		updateHistory = false;
	}
	else {
		if (config.sites) { // run is a search (Twitter or Facebook)
			runType = "search";
		}
		else {
			runType = config["analysis.type"];
		}

		updateHistory = true;
	}

	console.log("runType: " + runType);
	console.log("updateHistory: " + updateHistory);

	if (runType == "search") {
		var resultsSummary = runObject.results;
		var facebook = (resultsSummary.indexOf("facebook") != -1);

		//$("#resultsPanel").empty();
		//$("#searchRunResultsWrapper-2").empty();
		//$("#searchRunResultsWrapper-3").empty();
		$tabs.tabs('select', '#' + 'resultsTabPanel');
		clearSearchResults();
		if (updateHistory) showSearchHistory();

		//Set SNS value in results tab
		var resultsTabSNS = "Twitter";
		var analysisSNS = "twitter";

		if (facebook) {
			resultsTabSNS = "Facebook";
			analysisSNS = "facebook";
		}

		$("#searchResultsTabSNS").text(resultsTabSNS);

		if (status && (status == "failed")) {
			reportSearchError("Run " + runId + " failed.", config);
			return;
		}

		if (resultsSummary == "") {
			$("#resultsPanel").append("<p>Nothing was found.</p>");
			return;
		}
		else if (resultsSummary.indexOf("0") == 0) {
			$("#resultsPanel").append("<p>Nothing was found.</p>");
			return;
		}

		if (config) { // do analysis on-the-fly for a monitored search, if checkboxes are selected
			// KMI ANALYSIS TAB
			if ($("#doBehaviourAnalysis").is(":checked")) {
				console.log("Behaviour Analysis is checked");
				//extractTwitterUserIdsAndRunKmiAnalysis(data, searchTerms);
				if (facebook) {
					$("#searchRunResultsWrapper-3").empty();
					$("#searchRunResultsWrapper-3").append("<p>Sorry, Behaviour Analysis not available for Facebook.</p>");
				}
				else {
					kmiAnalysisForRun(runId);
				}
			}
			else {
				console.log("Behaviour Analysis is not checked (not launching analysis)");
				createAnalyseCurrentRunButton("behaviour", analysisSNS, activityId, runId);
				createAnalyseCurrentRunButton("behaviour", analysisSNS, activityId, -1);
			}
			
			// KOBLENZ ANALYSIS TAB
			if ($("#doTopicAnalysis").is(":checked")) {
				console.log("Topic Analysis is checked");
				//koblenzAnalysis(data);
				koblenzAnalysisForRun(runId);
			}
			else {
				console.log("Topic Analysis is not checked (not launching analysis)");
				createAnalyseCurrentRunButton("topics", analysisSNS, activityId, runId);
				createAnalyseCurrentRunButton("topics", analysisSNS, activityId, -1);
			}
		}
		else {
			createAnalyseCurrentRunButton("topics", analysisSNS, activityId, runId);
			createAnalyseCurrentRunButton("topics", analysisSNS, activityId, -1);
			createAnalyseCurrentRunButton("behaviour", analysisSNS, activityId, runId);
			createAnalyseCurrentRunButton("behaviour", analysisSNS, activityId, -1);
		}

		$("#resultsPanel").append("<p>Getting results for run " + runId + "...</p>");
		console.log("Getting results for run " + runId);
		currentSearchRun = runId;

		$.get("/home/search/getSearchResults/do.json", { runId: runId }, function(data){
			// Display results
			//alert('Got results');
			displayResultsForRun(runId, data);
			//alert('Finished display');
		});

	}
	else { // analysis run
		if (status && (status == "failed")) {
			reportAnalysisError("Run " + runId + " failed.", config);
			return;
		}

		clearAnalysisResults(runType);
		if (updateHistory) showAnalysisHistory(runType);

		if (runType == "topic-opinion") {
			$tabs.tabs('select', '#' + 'searchRunResultsWrapper-2');

			$("#searchRunResultsWrapper-2").append("<p>Getting results for analysis run " + runId + "...</p>");

			//$.get("analysis/getTopicAnalysisResults/do.json", { runId: runId}, function(data){
			//	displayKoblenzAnalysisResults(runId, data);
			//});
			currentTopicsRun = runId;

			getTopicAnalysisResults(runId, displayTopicsTable);
		}
		else {
			$tabs.tabs('select', '#' + 'searchRunResultsWrapper-3');

			$("#searchRunResultsWrapper-3").append("<p>Getting results for analysis run " + runId + "...</p>");

			currentBehaviourRun = runId;

			$.get("analysis/getBehaviourAnalysisResults/do.json", { runId: runId}, function(data){
				displayKmiAnalysisResults(runId, data);
			});
		}
	}
}

var runMetadata = {};

function detailInit(e, type) {
	console.log("type: " + type);
	console.log(e);
	console.log(e.data);
	console.log(e.detailCell);

	var currActivity = e.data.id;
	console.log("Getting runs for activity: " + currActivity);

	var gridId = "activity-detail-" + currActivity;
	console.log("Creating detail grid: " + gridId);

	var columns;
	if (type && (type == "search")) {
		columns = [
				  {title: " ", field: "id", width: "40px", template: '#= formatCheckbox("run", id, name) #'},
                  {title: "ID", field: "id", width: "50px"},
                  //{title: "Name", field: "name", width: "180px"},
                  {title: "Status", field: "status", width: "65px"},
                  {title: "Comment", field: "comment"},
                  {title: "Started on", field: "whenStarted", width: "160px"},
                  {title: "Finished on", field: "whenFinished", width: "160px"},
                  {title: "Results", field: "results", width: "180px",
					  template: '#= formatResults(results, id, activityid) #'}
        ];
	}
	else {
		columns = [
				  {title: " ", field: "id", width: "40px", hidden: true},
                  {title: "ID", field: "id", width: "50px"},
                  //{title: "Name", field: "name", width: "180px"},
                  {title: "Status", field: "status", width: "65px"},
                  {title: "Comment", field: "comment"},
                  {title: "Started on", field: "whenStarted", width: "160px"},
                  {title: "Finished on", field: "whenFinished", width: "160px"},
                  {title: "Results", field: "results", width: "180px",
					  template: '#= formatResults(results, id, activityid) #'}
        ];
	}

    var runCell = $('<div id="' + gridId + '" />').appendTo(e.detailCell).kendoGrid({
        dataSource: {
            type: "json",
            serverPaging: false,
            serverSorting: false,
            transport: {
                read: "/home/getRunsForActivity/do.json?activityId=" + currActivity
            },
            schema: {
            	data: function(result) {
                    return result.data || result;
	              },
	              total: function(result) {
	                    return result.total || result.length || 0;
	              } 
            },
            filter: { field: "activityid", operator: "eq", value: e.data.id },
            sort: {
            	field: "whenStarted", dir : "desc"
            }
            //pageSize: 10
        },
		/*
        columns: [
				  {title: " ", field: "id", width: "40px", template: '#= formatCheckbox("run", id, name) #'},
                  {title: "ID", field: "id", width: "50px"},
                  //{title: "Name", field: "name", width: "180px"},
                  {title: "Status", field: "status", width: "65px"},
                  {title: "Comment", field: "comment"},
                  {title: "Started on", field: "whenStarted", width: "160px"},
                  {title: "Finished on", field: "whenFinished", width: "160px"},
                  {title: "Results", field: "results", width: "180px",
					  template: '#= formatResults(results, id, activityid) #'}
        ],
		*/
		columns: columns,
        dataBound: function() {
			var checkboxes = e.detailCell.find("input:checkbox");
			checkboxes.click(runCheckboxClicked);
			console.log("Run metadata:");
			console.log(JSON.stringify(runMetadata));
			selectRowIfCurrentRun(gridId);
        },               
		change: onSearchResultsGridChangeDelayed,
		resizable: true,
        scrollable: false,
        sortable: true,
        pageable: false,
        selectable: "row"
//        columns: [ "OrderID", "ShipCountry", "ShipAddress", "ShipName" ]
    });

    //console.log(runCell);
}

var disableRowSelection = false;
function runCheckboxClicked(e) {
	console.log("runCheckboxClicked:");
	console.log($(this));
	var runId = $(this).val();
	if ($(this).is(":checked")) {
		console.log("Selected run " + runId);
		var masterRow = $(this).parent().parent().parent().parent().parent().parent().parent().prev(".k-master-row");
		console.log(masterRow);
		var activityCheckbox = masterRow.find("input:checkbox[name='activity']");;
		console.log(activityCheckbox);
		console.log("Activity " + activityCheckbox.val());
		activityCheckbox.attr('checked', 'checked'); // Select activity if one of its runs has been selected
	}
	else {
		console.log("Deselected run " + runId);
		console.log("Unchecked");
	}
	disableRowSelection = true;
}

function activityCheckboxClicked(e) {
	console.log("activityCheckboxClicked:");
	console.log($(this));
	var activityId = $(this).val();
	if ($(this).is(":checked")) {
		console.log("Selected activity " + activityId);
		// Select all runs in this activity
		$(this).parent().parent().next(".k-detail-row").find("input:checkbox[name='run']").attr('checked', 'checked');
	}
	else {
		console.log("Deselected activity " + activityId);
		// Deselect all runs in this activity
		$(this).parent().parent().next(".k-detail-row").find("input:checkbox[name='run']").removeAttr('checked');
	}
}

function selectRowIfCurrentRun(gridId) {
	console.log("selectRowIfCurrentRun for " + gridId);
	console.log("monitoredRun: " + monitoredRun);
	console.log("currentSearchRun: " + currentSearchRun);
	console.log("currentTopicsRun: " + currentTopicsRun);
	console.log("currentBehaviourRun: " + currentBehaviourRun);
 
	if ( (monitoredRun == null) && (! currentSearchRun) && (! currentTopicsRun) && (! currentBehaviourRun)) {
		console.log("Not selecting a row, as no monitored or currently displayed run");
		return;
	}

	var historyType = getCurrentHistoryType();	

	var grid = $("#"+gridId).data("kendoGrid");

	var rows = grid.tbody.find(">tr");
	console.log(rows);

	var matchedRun = false;

	rows.each(function(i) {
		var data = grid.dataItem($(this));
		if (data.id == monitoredRun) {
			console.log("Select row for monitored run " + monitoredRun);
			console.log(data);
			grid.select($(this));
			selectedGrid[historyType] = grid;
			selectedRow[historyType] = $(this);
			matchedRun = true;
		}
		else if (data.id == currentSearchRun) {
			console.log("Select row for currently displayed search run " + currentSearchRun);
			console.log(data);
			selecting = true;
			grid.select($(this));
			selectedGrid[historyType] = grid;
			selectedRow[historyType] = $(this);
			selecting = false;
			matchedRun = true;
		}
		else if (data.id == currentTopicsRun) {
			console.log("Select row for currently displayed topics run " + currentTopicsRun);
			console.log(data);
			selecting = true;
			grid.select($(this));
			selectedGrid[historyType] = grid;
			selectedRow[historyType] = $(this);
			selecting = false;
			matchedRun = true;
		}
		else if (data.id == currentBehaviourRun) {
			console.log("Select row for currently displayed behaviour run " + currentBehaviourRun);
			console.log(data);
			selecting = true;
			grid.select($(this));
			selectedGrid[historyType] = grid;
			selectedRow[historyType] = $(this);
			selecting = false;
			matchedRun = true;
		}
	});

	if (! matchedRun) {
		console.log("Not selecting a row, as no runs match monitored run " + monitoredRun);
	}
}

function formatResults(results, id, activityid) {
	console.log("activity: " + activityid + ", run: " + id);
	runMetadata[id] = {activity: activityid};
	return '<span class="results">' + results + '</span>';
}

function analyseSelectedResults() {
	//if (! selectedActivity) {
	//	alert("Please select a search to analyse");
	//	return;
	//}

	var activities = $("input:checkbox:checked[name='activity']");
	console.log(activities);

	var activityArray = new Array();

	var analysisSNS, activitySNS;

	var error = false;

	activities.each(function(i) {
		var id = $(this).attr('id');
		activitySNS = activityMetadata[id].sns;
		console.log(id + ": " + activitySNS);
		var activityId = $(this).val();

		if (i == 0) {
			analysisSNS = activitySNS;
			console.log("Analysis SNS: " + analysisSNS);
		}
		else {
			if (activitySNS != analysisSNS) {
				alert("Sorry, cannot analyse Twitter and Facebook search results together. Please select searches of one type only");
				error = true;
				return false;
			}
		}
		//console.log($(this).val());


		var detailRow = $(this).parent().parent().next(".k-detail-row");
		console.log(detailRow);
		var activityRuns = detailRow.find("input:checkbox:checked[name='run']");
		console.log(activityRuns);

		//var runIDArray = new Array();

		if (activityRuns.length > 0) {
			activityRuns.each(function(i) {
				var runId = $(this).val();
				var activityObject = {activityId: activityId, runId: runId};
				console.log(activityObject);
				activityArray.push(activityObject);
				//runIDArray.push(runId);
			});
			//console.log("Selecting runs for " + id + ": " + runIDArray);
		}
		else {
			//console.log("Selecting all runs for activity " + id);
			var activityObject = {activityId: activityId, runId: -1};
			activityArray.push(activityObject);
			console.log(activityObject);
		}

	});

	if (error) {
		return false;
	}

	console.log("Selected activities/runs: " + JSON.stringify(activityArray));

	if (activityArray.length == 0) {
		alert("Please select search(es) to analyse");
		return;
	}

	//console.log("Analysing activities: " + activityArray);

	var analysisOption = $("input:radio:checked[name='analysisOption']").val();
	console.log("Analysis option: " + analysisOption);

	analyseActivitiesAndRuns(analysisOption, analysisSNS, activityArray);
}

function getSelectedLanguage() {
	var languageSelector = $("#languageSelector").data("kendoDropDownList");
	return languageSelector.value();
}

function getNumTopics() {
	console.log($("#numberOfTopics"));
	var numTopics = $("#numberOfTopics").val();
	console.log("getNumTopics: value = " + numTopics);

	if (numTopics == "") {
		numTopics = -1;
	}
	else {
		if (isNaN(numTopics)) {
			console.log("WARNING: numTopics is not a number: " + numTopics);
			console.log($("#numberOfTopics"));
			console.log("Setting back to -1");
			$("#numberOfTopics").val(-1);
			numTopics = -1;
		}
	}
	console.log("getNumTopics: returning numTopics = " + numTopics);

	return numTopics;
}

function analyseActivitiesAndRuns(analysisOption, analysisSNS, activities, runId) {
	console.log("analyseActivitiesAndRuns");
	console.log("analysisOption: " + analysisOption);
	console.log("analysisSNS: " + analysisSNS);
	console.log("activities: " + JSON.stringify(activities));
	console.log("runId: " + runId);

	var analysisType;
	var analysisSubType;
	var numTopics;
	var language;

	if (analysisOption == "topics") {
		//var languageSelector = $("#languageSelector").data("kendoDropDownList");
		//language = languageSelector.value();
		language = getSelectedLanguage();
		numTopics = getNumTopics();

		analysisType = "topic-opinion";

		if (analysisSNS == "twitter") {
			analysisSubType = "twitter-topics";
		}
		else if (analysisSNS == "facebook") {
			analysisSubType = "facebook-group-topics";
		}
	}
	else {
		analysisType = "behaviour";
		//var languageSelector = $("#languageSelector").data("kendoDropDownList");
		//language = languageSelector.value();
		language = getSelectedLanguage();
		if (analysisSNS == "twitter") {
			analysisSubType = "twitter-behaviour";
		}
		else if (analysisSNS == "facebook") {
			alert("Sorry, Behaviour Analysis not available for Facebook");
			return;
		}
	}
	
	var runNow = true;
	var postId = -1; // only used when analysisSubType = "facebook-post-comments-topics"

	var analysisConfig = createAnalysisConfig(
		analysisType,
		analysisSubType,
		true,
		activities,
		postId,
		numTopics,
		language,
		runId
	);

	createNewAnalysisActivityAndRun(analysisConfig);
}

