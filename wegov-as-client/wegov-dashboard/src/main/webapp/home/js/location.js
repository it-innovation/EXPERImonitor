/*
var lat = geoplugin_latitude();
var lon = geoplugin_longitude();
var geo_city = geoplugin_city();
var geo_region = geoplugin_region();
var geo_countryName = geoplugin_countryName();
var geo_countryCode = geoplugin_countryCode();
*/

var currLocation;
var gettingLocation = false;
var geocoding = false;

function getCurrentLocation(locationDefinedCallback) {
	console.log("getCurrentLocation:");
	var lat, lon, source;

	if (currLocation) {
		console.log("Location coords already defined:");
		if (! currLocation["locationAddress"]) {
			console.log("No address defined");
			if (geocoding) {
				console.log("Waiting for geocode...");
				waitForGeocode(locationDefinedCallback);
			}
			else {
				console.log("Calling geocode...");
				geocode(currLocation["lat"], currLocation["lon"], locationDefinedCallback);
			}
			return;
		}
		console.log(JSON.stringify(currLocation));
		locationDefinedCallback();
		return;
	}

	if (gettingLocation) {
		console.log("Waiting for location...");
		waitForLocation(function() {waitForGeocode(locationDefinedCallback);});
	}
	else {
		gettingLocation = true;
		if (navigator.geolocation) // check if browser support this feature or not
		{
			console.log("Getting location via browser");
			navigator.geolocation.getCurrentPosition(function(position) {
				source = "browser";
				lat = position.coords.latitude;
				lon = position.coords.longitude;
				currLocation = {source: source, lat:lat, lon: lon};
				console.log(JSON.stringify(currLocation));
				//locationDefinedCallback();
				geocode(lat, lon, locationDefinedCallback);
				gettingLocation = false;
			});
		}
		else {
			source = "geoplugin";
			lat = geoplugin_latitude();
			lon = geoplugin_longitude();
			currLocation = {source: source, lat:lat, lon: lon};
			console.log(JSON.stringify(currLocation));
			//locationDefinedCallback();
			geocode(lat, lon, locationDefinedCallback);
			gettingLocation = false;
		}
	}
}

function waitForLocation(callback) {
	if (! gettingLocation) {
		console.log("waitForLocation: location available");
		callback();
	}
	else {
		console.log("waitForLocation: waiting for location...");
		setTimeout(function() {waitForLocation(callback);}, 100);
	}
}

function geocode(lat, lon, callback) {
	geocoding = true;
	console.log("Geocode coords: " + lat + ", " + lon);
	var latLng = new google.maps.LatLng(lat, lon);
	var geocoder = new google.maps.Geocoder();
	geocoder.geocode({'latLng': latLng}, function(results, status) {
		console.log(results);
		if (status == google.maps.GeocoderStatus.OK) {
			currLocation["locationAddress"] = results[0].formatted_address;
			console.log(JSON.stringify(currLocation));
		}
		else {
			console.log("Geocoder failed due to: " + status);
		}
		geocoding = false;
		callback();
	});
}

function waitForGeocode(callback) {
	if (! geocoding) {
		console.log("waitForGeocode: geocode completed");
		callback();
	}
	else {
		console.log("waitForGeocode: waiting for geocode...");
		setTimeout(function() {waitForGeocode(callback);}, 100);
	}
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

  addNewLocationControl(myDiv);

  var gmapId = "gmap" + widget["id"];
  addLocationMapToDiv(myDiv, gmapId, extraText);
}

function addNewLocationControl(myDiv, updateCallback) {
  //
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
      center: new google.maps.LatLng(currLocation["lat"], currLocation["lon"]),
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
      {
        locationName:locationName,
        locationAddress:locationAddress,
        lat:locationLat,
        lon:locationLon
      }, function(data) {
        $("#add-new-location-dialog-holder, #dialog-overlay").hide();

        $.each($(".hasMapToRefresh"), function(containerCount, container) {
          var containerDom = document.getElementById($(this).attr('id'));
          refreshLocations(containerDom.containerId);
        });

	  	if (updateCallback) {
			updateCallback();
		}
		else {
			loadWidgets(false);
		}

	  
	  });


    });

  });
}

function addLocationMapToDiv(myDiv, gmapId, extraText) {
	//
	var myContainer = $("<div class=\"widgetContent\"></div>").appendTo(myDiv);
	myContainer.append("<div id=\"" + gmapId + "\" class=\"gmap\"></div>");

	var widgetPostContent = $("<div class=\"widgetPostContent\"></div>").appendTo(myDiv);

	if (currLocation) {
		showMarkersOnGoogleMap(gmapId, currLocation.lat, currLocation.lon, widgetPostContent);
		if (currLocation.source == "browser") {
			extraText.text("Your current location (marked with a star) is determined with the help of your internet browser");
		}
		else {
			extraText.text("Your current location was determined based on your IP address");
		}
	}
	else {
		console.log("ERROR: currLocation is not defined");
	}

	var widgetFooter = $("<div class=\"widgetFooter\"></div>").appendTo(myDiv);

	widgetFooter.append('<div class="clearfix"></div>');

	widgetFooter.append('<div class="clearfix"></div>');

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

  geocoder.geocode({
    'latLng': myLatlng
  }, function(results, status) {
    console.log(results);
    if (status == google.maps.GeocoderStatus.OK) {
      locationDescription.html(
        "<b>" + markerTextFromNumber(0) + "</b> - "
          + " your current location (" + results[0].formatted_address
          //+ ") <span class=\"changeCurrentlocation\">change</span>");
          + ")");


	  //search page only
	  $("#radioButtonLocalLabel2").text("Near me 2 (" + results[0].formatted_address + ")");
	  $("#searchLocalRadio2").data("location", results[0].formatted_address);


      //locationDescription.find(".changeCurrentlocation").click(function(e){
      //  console.log("Changing location!");
      //});
    } else {
      console.log("Geocoder failed due to: " + status);
      locationDescription.text(geo_city + ", " + geo_countryName);
    }

    var currentPositionMaker = new StyledMarker({
      styleIcon:new StyledIcon(StyledIconTypes.MARKER,{
        color:"FE796D",
        text:markerTextFromNumber(0),
        starcolor:"FFFF00"
      }),
      position:myLatlng,
      map:map
    });
    bounds.extend(currentPositionMaker.position);
    var infoWindow = new google.maps.InfoWindow({
      maxWidth: 210
    });
    var infoWindowContents = $(
      '<p><b>Your current location</b><br />' + results[0].formatted_address
        + "<br/><span title=\"-1\" class=\"removelocation aslink\">Change</span></p>");

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
        {
          locationId:removeLocationLinkId
        }, function(response) {
          widgetPostContent.empty();
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
          var marker = new StyledMarker({
            styleIcon:new StyledIcon(StyledIconTypes.MARKER,{
              color:"FE796D",
              text:markerText
            }),
            position:myLatlng,
            map:map
          });
          bounds.extend(marker.position);
          map.fitBounds(bounds);

          var infoWindow = new google.maps.InfoWindow({
            maxWidth: 210
          });
          var infoWindowContents = $(
            "<p><b>" + location.locationName + '</b><br />' + location.locationAddress
              + "<br/><span title=\"" + location.id
              + "\" class=\"removelocation aslink\">Remove this location</span></p>");

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
              {
                locationId:removeLocationLinkId
              }, function(response) {
                widgetPostContent.empty();
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

