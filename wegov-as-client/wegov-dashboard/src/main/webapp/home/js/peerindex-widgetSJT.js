/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


function addPeerindexWidget(widget) {
	//var myDiv = initWidget(widget);
  var myDiv = initAnalysisWidget(widget);
  var widgetId = widget["id"];

//	var peerIndexProfile = widget["parametersAsString"];
	var parameters = jQuery.parseJSON(widget["parametersAsString"]);
	var peerIndexProfile = parameters.term;

  var peerIndexData = getPeerindexDataFromDB_Synchronous(widgetId);
  console.log(peerIndexData);
  plotPeerIndex(peerIndexData, myDiv, widget);


}

/****************************************************************************
 * Get Peerindex data From DB - Synchronous Version that returns the JSON
 ***************************************************************************/
	function getPeerindexDataFromDB_Synchronous(wId) {
    var peerindexData;
    var widgetData;

    $.ajax({
      url:
        "/home/widgets/getWidgetData/do.json?wId=" + wId,
      type: 'get',
      //wId: wId,
      success:function(widgetDataUpdate){
        widgetData = widgetDataUpdate;

        console.log(widgetData);
        console.log(widgetData[0]);
        if (widgetData[0] != undefined) {

          var jsonData = jQuery.parseJSON(widgetData[0]["dataAsJson"]);
          console.log(jsonData);

          peerindexData = jsonData["peerindex"];
          console.log(peerindexData);

        } else {
          peerindexData = jQuery.parseJSON('{"result":"No data found."}');
        }
      },
      async: false
    });


    console.log(peerindexData);
    return peerindexData;
  }


/****************************************************************************
 * Refresh Peerindex Widget
 ***************************************************************************/
	function refreshPeerindexWidget(widget, myDiv, peerIndexProfile) {
		myDiv.empty();


		var myContainer = $("<div class=\"widgetContainer\"></div>")
				.appendTo(myDiv);
		myContainer.append("<p>Refreshing Peerindex, please wait...</p>");


    // Peerindex
    $.ajax({
      url:"https://api.peerindex.net/v2/profile/show.json?id=" + peerIndexProfile + "&api_key=09355f288dcba68de7adb0e8c4f0fffd&callback=?",
      dataType: 'json',
      type: 'GET',
      contentType: "application/json; charset=utf-8",
      error: function(){
        myDiv.text("Never met " + peerIndexProfile + " before. Try again?");
      },
      success: function(data) {
        if (data.length < 1) {
          myDiv.text("Never met " + peerIndexProfile + ". Try again?");
        } else {

          var dataToSend = JSON.stringify({
            wId: widget["id"],
            type: "peerindex",
            name: "Search query: " + peerIndexProfile,
            location: "none",
            //data: data});
            data: JSON.stringify({peerindex: data})
          });

          console.log("Sending data to server");
        	console.log(dataToSend);

          $.ajax({
              type: 'POST',
              url: "/home/widgets/saveWidgetData/do.json",
              contentType: "application/json; charset=utf-8",
              data: dataToSend
          })
          // End store results in server database


          plotPeerIndex(data, myDiv, widget);

        }
      }
    });

	}


  function plotPeerIndex(data, myDiv, widget) {

    var widgetId = widget["id"];

    var parameters = jQuery.parseJSON(widget["parametersAsString"]);
    var peerIndexProfile = parameters.term;

    myDiv.empty();

    var widgetHeaderDiv = $("<div class=\"widgetHeaderDiv\"></div>").appendTo(myDiv);
    $("<img class=\"widgetLogo\" src=\"img/peerindex_reasonably_small.png\" alt=\"Source: Peerindex\"/>").appendTo(widgetHeaderDiv);
    var widgetHeaderTextDiv = $("<div class=\"widgetHeaderTextDiv\"></div>").appendTo(widgetHeaderDiv);

    var myHeader = $("<h2 class=\"widgetHeader\">" + "Peerindex " + peerIndexProfile + "</h2>");
    myHeader.appendTo(widgetHeaderTextDiv);

    widgetHeaderDiv.append('<div class="clearfix"></div>');
    var widgetHeaderExtraTextDiv = $("<div class=\"widgetHeaderExtraTextDiv\"></div>").appendTo(widgetHeaderDiv);
    var extraText = $("<p></p>").appendTo(widgetHeaderExtraTextDiv);

    var myContainer = $("<div class=\"widgetContent\"></div>").appendTo(myDiv);

    var myChart = $(
        "<div id=\"plot_" + widgetId
            + "\" class=\"peerindexchart\"></div>").appendTo(myContainer);


    // plot peerindex
    var peerindex = data["peerindex"];
    var authority = data["authority"];
    var activity = data["activity"];
    var audience = data["audience"];

    var header = myHeader;
    header.text(widget["name"] + ": " +  data["name"]);

    widgetHeaderTextDiv.append("<p class=\"peedindexaslabel\">" + peerindex + "</p>");


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

    var widgetFooter = $("<div class=\"widgetFooter\"></div>").appendTo(myDiv);
    var settingsButton = $("<p class=\"widgetSettings\">Settings</p>").appendTo(widgetFooter);
    settingsButton.click(function(e){
      //showWidgetSettingsWindowSJT(myDiv, widget, "Twitter username:");
      showWidgetSettingsWindowSJT(myDiv, widget);
    });
    var refreshButton = $("<p class=\"widgetRefresh\">Refresh Data</p>").appendTo(widgetFooter);
    refreshButton.click(function(e){
    refreshPeerindexWidget(widget, myDiv, peerIndexProfile);
    });
    widgetFooter.append('<div class="clearfix"></div>');


  }