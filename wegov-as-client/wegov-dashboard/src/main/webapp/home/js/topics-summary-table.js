var topicAnalysisResult;

var testglobal;

var testjsonresult;

var topicsDataSource;

var maxTopicPosts;

var currentRunParameters;

/*
function addTopicsTable() {
	console.log("addTopicsTable");
	var myDiv = $("#topicsTable");
	myDiv.empty();

	var myContainer = $("<div class=\"widgetContainer\" id=\"kendoTopicsTable\"></div>").appendTo(myDiv);

	myContainer.kendoGrid({
		dataSource: {
			data: [
				//{id: 1, keywords: "test, hello, wegov", numPosts: 50000, sentiment: 5.4, controversy: 2.0},
				//{id: 2, keywords: "bad, news", numPosts: 40000, sentiment: -8.0, controversy: 1.0},
				//{id: 3, keywords: "interesting, controversial, subject", numPosts: 10000, sentiment: 0.0, controversy: 8.0},
				//{id: 4, keywords: "really, boring, subject", numPosts: 1000, sentiment: 2.3, controversy: 2.0},
				],
			schema: {
				model: {
					fields: {
						id: { type: "number" },
						keywords: { type: "string" },
						numPosts: { type: "number" },
						sentiment: { type: "number" },
						controversy: { type: "number" }
					}
				}
			},
			pageSize: 10
		},
		sortable: true,
		reorderable: true,
		resizable: true,
		pageable: true,
		columns: [
			{title: "ID", field: "id", width: 60},
			{title: "Keywords", field: "keywords"},
			{title: "Num Posts", field: "numPosts", width: 100},
			{title: "Sentiment", field: "sentiment", width: 100},
			{title: "Controversy", field: "controversy", width: 100},
		]
	});
}

function addTopicsTable2() {
	console.log("addTopicsTable");

	var runId = loadPageVar("runId");
	var runIdOption = "";

	if (runId) {
		runIdOption = "?runId=" + runId;
	}


//


	var myDiv = $("#topicsTable2");
	myDiv.empty();

	var myContainer = $("<div class=\"widgetContainer\" id=\"kendoTopicsTable\"></div>").appendTo(myDiv);

	myContainer.kendoGrid({
		dataSource: {
			type: "json",
			//data: [
			//	{id: 1, keywords: "test, hello, wegov", numPosts: 50000, sentiment: 5.4, controversy: 2.0},
			//	{id: 2, keywords: "bad, news", numPosts: 40000, sentiment: -8.0, controversy: 1.0},
			//	{id: 3, keywords: "interesting, controversial, subject", numPosts: 10000, sentiment: 0.0, controversy: 8.0},
			//	{id: 4, keywords: "really, boring, subject", numPosts: 1000, sentiment: 2.3, controversy: 2.0},
			//	],
            transport: {
                //read: "/home/analysis/koblenz/run_data/do.json" + runIdOption,
                read: "/home/analysis/getTopicAnalysisResults/do.json" + runIdOption
            },
			schema: {
				data: function(data) {
                    return data.topics;
	            },
				model: {
					fields: {
						id: { type: "number" },
						keywords: { type: "string" },
						numPosts: { type: "number" },
						valence: { type: "number" },
						controversy: { type: "number" }
					}
				}
			},
			pageSize: 10
		},
		sortable: true,
		reorderable: true,
		resizable: true,
		pageable: true,
		columns: [
			{title: "ID", field: "id", width: 30},
			{title: "Keywords", field: "keywords"},
			{title: "Num Posts", field: "numPosts", width: 180},
			{title: "Sentiment", field: "valence", width: 160},
			{title: "Controversy", field: "controversy", width: 160},
		],
		rowTemplate: kendo.template(topicRowTemplate()),
		detailInit: detailInitTopicPosts,
		dataBound: function() {
			createSliders();
            //this.expandRow(this.tbody.find("tr.k-master-row").first());
		}
        //change: onSearchResultsGridChange,
        //filterable: true,
        //groupable: true,
        //selectable: "row"
	});
}
*/


function addTopicsTable3(runId) {
	console.log("addTopicsTable3");

	/*
	var runIdOption = "";

	if (runId) {
		runIdOption = "?runId=" + runId;
	}
	*/


  var myDiv = $("#topicsTable2");
  myDiv.empty();

  var myContainer = $("<div class=\"widgetContainer\" id=\"kendoTopicsTable\"></div>").appendTo(myDiv);

  myContainer.kendoGrid({
    dataSource: {
      type: "json",
      //data: result.topics,
      data: topicsDataSource.transport.data.topics,
      schema: {
        model: {
          fields: {
            id: { type: "number" },
            keywords: { type: "string" },
            numPosts: { type: "number" },
            sentiment: { type: "number" },
            controversy: { type: "number" }
          }
        }
      },
      //pageSize: 10
      pageSize: 1000
    },
    sortable: true,
    reorderable: true,
    resizable: true,
    pageable: false,
    columns: [
      {title: "ID", field: "id", width: 30},
      {title: "Keywords", field: "keywords"},
      {title: "Num Posts", field: "numPosts", width: 180},
      {title: "Sentiment", field: "valence", width: 160},
      {title: "Controversy", field: "controversy", width: 160},
    ],
    rowTemplate: kendo.template(topicRowTemplate()),
    //detailTemplate: kendo.template($("#template").html()),
    detailTemplate: kendo.template(topicDetailTemplate()),
    detailInit: detailInitTopicPosts,
    dataBound: function() {
      createSliders();
            //this.expandRow(this.tbody.find("tr.k-master-row").first());
    }
        //change: onSearchResultsGridChange,
        //filterable: true,
        //groupable: true,
        //selectable: "row"
  });



}

/*
function detailInitTopicPosts(e) {
    var detailRow = e.detailRow;

    detailRow.find(".tabstrip").kendoTabStrip({
        animation: {
            open: { effects: "fadeIn" }
        }
    });

    detailRow.find(".orders").kendoGrid({
        dataSource: {
            type: "odata",
            transport: {
                read: "http://demos.kendoui.com/service/Northwind.svc/Orders"
            },
            serverPaging: true,
            serverSorting: true,
            serverFiltering: true,
            pageSize:6,
            filter: { field: "EmployeeID", operator: "eq", value: e.data.EmployeeID }
        },
        scrollable: false,
        sortable: true,
        pageable: true,
        columns: [
            { field: "OrderID", width: 70 },
            { field: "ShipCountry", title:"Ship Country", width: 100 },
            { field: "ShipAddress", title:"Ship Address" },
            { field: "ShipName", title: "Ship Name", width: 200 }
        ]
    });
}


function topicDetailTemplate() {
//<script type="text/x-kendo-template" id="template">
  html =
    '<div class="tabstrip">' +
         '<ul>' +
             '<li class="k-state-active">' +
                'Orders' +
             '</li>' +
             '<li>' +
                 'Contact Information' +
             '</li>' +
         '</ul>' +
         '<div>' +
             '<div class="orders"></div>' +
         '</div>' +
         '<div>' +
             '<div class=\'employee-details\'>' +
                 '<ul>' +
                     //'<li><label>Country:</label>#= Country #</li>' +
                     '<li><label>City:</label>#= keywords #</li>' +
                     '<li><label>Address:</label>#= numPosts #</li>' +
                     '<li><label>Home Phone:</label>#= valence #</li>' +
                 '</ul>' +
             '</div>' +
         '</div>' +
     '</div>'
//   </script>

 return html;
}
*/

function topicDetailTemplate() {
//<script type="text/x-kendo-template" id="template">
  html =
    '<div class="tabstrip">' +
         '<ul>' +
             '<li class="k-state-active">' +
                'Post Details' +
             '</li>' +
//             '<li>' +
//                 'User Details' +
//             '</li>' +
         '</ul>' +
         '<div>' +
             '<div class="postDetails"></div>' +
         '</div>' +
//         '<div>' +
//             '<div class="userDetails"></div>' +
//         '</div>' +
     '</div>'
//   </script>

 return html;
}


function detailInitTopicPosts(e) {
    var detailRow = e.detailRow;

    detailRow.find(".tabstrip").kendoTabStrip({
        animation: {
            open: { effects: "fadeIn" }
        }
    });

    detailRow.find(".postDetails").kendoGrid({
		dataSource: {
      type: "json",
      data: topicsDataSource.transport.data,
			//pageSize:6,
      pageSize:1000,
      schema: {
        data: function(data) {
          console.log(e.data.id);
          for (i = 0; i< data.numTopics; i++) {
            if (data.topics[i].id == e.data.id) {
              console.log (data.topics[i]);
              return data.topics[i].topicDocuments;
            }
          }
        }
      }
		},
/*    
				'<td>${ postDetails.id }</td>' +
				'<td>${ postDetails.text }</td>' +
				'<td>${ userDetails.fullName }</td>' +    
    */
		scrollable: false,
		sortable: true,
		pageable: false,
		columns: [
			//{title: "ID", field: "postDetails.id", width: 30},
			//{title: "Post Text", field: "postDetails.text" + " (ID = " + "postDetails.id" + ")"},
      {title: "Post Text", field: "postDetails.text"},
			//{title: "Author", field: "<a href=\"" + "userDetails.userUrl" + "\">" + "userDetails.fullName" + "</a>", width: 120},
      {title: "Author", field: "userDetails.fullName", width: 160},
			{title: "Relevance", field: "topicScore", width: 80},
			{title: "Sentiment", field: "valence", width: 80},
		],
		rowTemplate: kendo.template(postRowTemplate()),
		dataBound: function() {
			console.log("dataBound");
      //console.log(topicsDataSource);
			//createPostRowSliders();
		}
  });
/*
  detailRow.find(".userDetails").kendoGrid({
		dataSource: {
      type: "json",
      data: topicsDataSource.transport.data,
			pageSize:6,
      schema: {
        data: function(data) {
          console.log(e.data.id);
          for (i = 0; i< data.numTopics; i++) {
            if (data.topics[i].id == e.data.id) {
              console.log (data.topics[i]);
              return data.topics[i].keyUsers;
            }
          }
        }
      }
		},
		scrollable: false,
		sortable: true,
		pageable: true,
		columns: [
			{title: "ID", field: "id", width: 60},
			{title: "Full Name", field: "fullName"},
			{title: "Screen Name", field: "screenName", width: 180}//,
			//{title: "Relevance", field: "score", width: 160}//,
			//{title: "Sentiment", field: "sentiment", width: 160},
		],
*/
/*
 *
 *  User details example
 *
    createdAt ""
    description ""
    favouritesCount ""
    followersCount ""
    friendsCount ""
    fullName "Y v e t t e"
    id "61547666"
    location ""
    profileImageUrl "https://si0.twimg.com/p...0E2-6E7493348E03_normal"
    screenName "evet965"
    statusesCount ""
    timeZone ""
    url ""
 */
/*
		rowTemplate: kendo.template(userRowTemplate()),
		dataBound: function() {
			console.log("dataBound");
      //console.log(topicsDataSource);
			//createPostRowSliders();
		}
  });
  */
}


/*
function detailInitTopicPostsOld(e) {
    var detailRow = e.detailRow;

    detailRow.find(".tabstrip").kendoTabStrip({
        animation: {
            open: { effects: "fadeIn" }
        }
    });

    detailRow.find(".postDetails").kendoGrid({
		dataSource: {
      type: "json",
      data: topicsDataSource.transport.data,
			pageSize:6,
      schema: {
        data: function(data) {
          console.log(e.data.id);
          for (i = 0; i< data.numTopics; i++) {
            if (data.topics[i].id == e.data.id) {
              console.log (data.topics[i]);
              return data.topics[i].keyPosts;
            }
          }
        }
      }
		},
		scrollable: false,
		sortable: true,
		pageable: true,
		columns: [
			{title: "ID", field: "id", width: 160},
			{title: "Post Text", field: "text"},
			{title: "Author", field: "byUserScreenName", width: 120},
			{title: "Relevance", field: "score", width: 160}//,
			//{title: "Sentiment", field: "sentiment", width: 160},
		],
		rowTemplate: kendo.template(postRowTemplateOld()),
		dataBound: function() {
			console.log("dataBound");
      //console.log(topicsDataSource);
			createPostRowSliders();
		}
  });

  detailRow.find(".userDetails").kendoGrid({
		dataSource: {
      type: "json",
      data: topicsDataSource.transport.data,
			pageSize:6,
      schema: {
        data: function(data) {
          console.log(e.data.id);
          for (i = 0; i< data.numTopics; i++) {
            if (data.topics[i].id == e.data.id) {
              console.log (data.topics[i]);
              return data.topics[i].keyUsers;
            }
          }
        }
      }
		},
		scrollable: false,
		sortable: true,
		pageable: true,
		columns: [
			{title: "ID", field: "id", width: 60},
			{title: "Full Name", field: "fullName"},
			{title: "Screen Name", field: "screenName", width: 180}//,
			//{title: "Relevance", field: "score", width: 160}//,
			//{title: "Sentiment", field: "sentiment", width: 160},
		],


		rowTemplate: kendo.template(userRowTemplate()),
		dataBound: function() {
			console.log("dataBound");
      //console.log(topicsDataSource);
			//createPostRowSliders();
		}
  });
}
*/

/*
function detailInitWorks(e) {
	$("<div/>").appendTo(e.detailCell).kendoGrid({
		dataSource: {
      type: "json",
      //data: result.topics,
      data: topicsDataSource.transport.data,
			pageSize:6,
      schema: {
        data: function(data) {
          console.log(e.data.id);
          for (i = 0; i< data.numTopics; i++) {
            if (data.topics[i].id == e.data.id) {
              console.log (data.topics[i]);
              return data.topics[i].keyPosts;
            }
          //return data.keyPosts;
          }
        }
      }
      //,
			//filter: { field: "id", operator: "eq", value: e.data.id }
		},
		scrollable: false,
		sortable: true,
		pageable: true,
		columns: [
			{title: "ID", field: "id", width: 60},
			{title: "Post Text", field: "text"},
			{title: "Author", field: "byUserScreenName", width: 180},
			{title: "Relevance", field: "score", width: 160}//,
			//{title: "Sentiment", field: "sentiment", width: 160},
		],
		rowTemplate: kendo.template(postRowTemplate()),
		dataBound: function() {
			console.log("dataBound");

     console.log(topicsDataSource);

     console.log (testglobal);

     console.log (testjsonresult);


			createPostRowSliders();
		}
	});
}
*/

function createSliders() {
	$(".sliderWrapper").each(function(i) {
		if ( $(this).find('div.wegovRangeSlider').size() > 0) {
			createRangeSlider($(this));
		}
		else {
			createSlider($(this));
		}
	});
}

function createPostRowSliders() {
	$(".postRow.sliderWrapper").each(function(i) {
		if ( $(this).find('div.wegovRangeSlider').size() > 0) {
			createRangeSlider($(this));
		}
		else {
			createSlider($(this));
		}
	});
}

function createRangeSlider(sliderWrapper) {
	var value = sliderWrapper.find('input[name="sliderValue"]').val();

	var slider = sliderWrapper.find(".wegovRangeSlider").kendoRangeSlider({
		min: -10,
		max: 10,
		orientation: "horizontal",
		smallStep: 2,
		largeStep: 10,
		selectionStart: 0,
		selectionEnd: value,
		showButtons: false
	}).data("kendoRangeSlider");

	sliderWrapper.find(".k-draghandle").each(function(i) {
		if (i == 0) {
			if (value >= 0) {
				$(this).hide(); // don't show first drag handle for +ve values
			}
			else {
				$(this).attr('title', value); // put value into tooltip
				$(this).unbind('mousedown');
			}
		}
		else {
			if (value < 0) {
				$(this).hide(); // don't show second drag handle for -ve values
			}
			else {
				$(this).attr('title', value); // put value into tooltip
				$(this).unbind('mousedown');
			}
		}
	});

	// Set colour of slider according to +ve or -ve
	if (value < 0) {
		setSliderColour(sliderWrapper, "negative");
	}
	else {
		setSliderColour(sliderWrapper, "positive");
	}

	sliderWrapper.find(".k-tick").unbind("mousedown");
	sliderWrapper.find(".k-slider-track").unbind("mousedown");
}

function createSlider(sliderWrapper) {
	var value = sliderWrapper.find('input[name="sliderValue"]').val();
	var maxValue = 10;

	if (sliderWrapper.hasClass("numPosts")) {
		maxValue = maxTopicPosts;
	}
  else if (sliderWrapper.hasClass("relevance")) {
		maxValue = 1;
	}
  else {
    maxValue = 10;
  }


	var slider = sliderWrapper.find(".wegovSlider").kendoSlider({
		min: 0,
		max: maxValue,
		orientation: "horizontal",
		smallStep: 1,
		largeStep: 10,
		value: value,
		showButtons: false
	}).data("kendoSlider");

	sliderWrapper.find(".k-draghandle").each(function(i) {
		$(this).attr('title', value); // put value into tooltip
		$(this).unbind('mousedown');
	});

	if (sliderWrapper.hasClass("controversy")) {
		setSliderColour(sliderWrapper, "controversy");
	}
	else if (sliderWrapper.hasClass("numPosts")) {
		setSliderColour(sliderWrapper, "numPosts");
	}
	else if (sliderWrapper.hasClass("relevance")) {
		setSliderColour(sliderWrapper, "relevance");
	}

	sliderWrapper.find(".k-tick").unbind("mousedown");
	sliderWrapper.find(".k-slider-track").unbind("mousedown");
}

function draggableOnDragStart() {
	console.log("Drag start");
}

function setSliderColour(sliderWrapper, colourClass) {
	sliderWrapper.find(".k-slider-selection").addClass(colourClass);
	sliderWrapper.find(".k-draghandle").addClass(colourClass);
}

function topicRowTemplate() {
	html = '<tr class="k-master-row">' +
				'<td class="k-hierarchy-cell">' +
					'<a class="k-icon k-plus" href="\\#"></a>' +
				'</td>' +
				'<td>${ id }</td>' +
				'<td><span class="ui-widget" style="font-weight: bold;">${ keywords }</span></td>' +

				'<td>' +
					'<div class="sliderWrapper numPosts">' +
						'<div class="wegovSlider"><input /></div>' +
						'<div class="wegovSliderValue"><p>${ numPosts }</p></div>' +
						'<input name="sliderValue" type="hidden" value="${ numPosts }" />' +
					'</div>' +
				'</td>' +

				'<td>' +
					'<div class="sliderWrapper">' +
						'<div class="wegovRangeSlider"><input /><input /></div>' +
						'<div class="wegovSliderValue"><p>${ valence }</p></div>' +
						'<input name="sliderValue" type="hidden" value="${ valence }" />' +
					'</div>' +
				'</td>' +

				'<td>' +
					'<div class="sliderWrapper controversy">' +
						'<div class="wegovSlider"><input /></div>' +
						'<div class="wegovSliderValue"><p>${ controversy }</p></div>' +
						'<input name="sliderValue" type="hidden" value="${ controversy }" />' +
					'</div>' +
				'</td>' +

			'</tr>';
	return html;
}


function postRowTemplate() {
	html = '<tr>' +
				//'<td>${ postDetails.id }</td>' +
//				'<td>${ postDetails.id + postDetails.text }</td>' +
        '<td><p>${ postDetails.text } <br>Posted at: ${ postDetails.createdAt }<br><i>(ID = ${ postDetails.id })</i></p></td>' +
				'<td><a href="${ userDetails.userUrl } ">${ userDetails.fullName }</a></td>' +
				'<td>${ topicScore.toFixed(2) }</td>' +
				'<td>${ valence.toFixed(2) }</td>'
			'</tr>';
	return html;
}

/*
function postRowTemplateOld2() {
	html = '<tr>' +
				'<td>${ docId }</td>' +
				'<td>${ body }</td>' +
				'<td>${ userId }</td>' +

				'<td>' +
					'<div class="postRow sliderWrapper relevance">' +
						'<div class="wegovSlider"><input /></div>' +
						'<div class="wegovSliderValue"><p>${ topicScore }</p></div>' +
						'<input name="sliderValue" type="hidden" value="${ topicScore }" />' +
					'</div>' +
				'</td>' +

				'<td>' +
					'<div class="postRow sliderWrapper">' +
						'<div class="wegovRangeSlider"><input /><input /></div>' +
						'<div class="wegovSliderValue"><p>${ valence }</p></div>' +
						'<input name="sliderValue" type="hidden" value="${ valence }" />' +
					'</div>' +
				'</td>' +

			'</tr>';
	return html;
}
*/

/*
function postRowTemplateOld() {
	html = '<tr>' +
				'<td>${ id }</td>' +
				'<td>${ text }</td>' +
				'<td>${ byUserScreenName }</td>' +

				'<td>' +
					'<div class="postRow sliderWrapper relevance">' +
						'<div class="wegovSlider"><input /></div>' +
						'<div class="wegovSliderValue"><p>${ score }</p></div>' +
						'<input name="sliderValue" type="hidden" value="${ score }" />' +
					'</div>' +
				'</td>' +

				'<td>' +
					'<div class="postRow sliderWrapper">' +
						'<div class="wegovRangeSlider"><input /><input /></div>' +
						'<div class="wegovSliderValue"><p>${ sentiment }</p></div>' +
						'<input name="sliderValue" type="hidden" value="${ sentiment }" />' +
					'</div>' +
				'</td>' +

			'</tr>';
	return html;
}
*/
/*

			{title: "ID", field: "id", width: 60},
			{title: "Full Name", field: "fullName"},
			{title: "Screen Name", field: "screenName", width: 180}//,

*/

function userRowTemplate() {
	html = '<tr>' +
				'<td>${ id }</td>' +
				'<td>${ fullName }</td>' +
				'<td>${ screenName }</td>' +
/*
				'<td>' +
					'<div class="postRow sliderWrapper relevance">' +
						'<div class="wegovSlider"><input /></div>' +
						'<div class="wegovSliderValue"><p>${ score }</p></div>' +
						'<input name="sliderValue" type="hidden" value="${ score }" />' +
					'</div>' +
				'</td>' +
  */
/*
				'<td>' +
					'<div class="postRow sliderWrapper">' +
						'<div class="wegovRangeSlider"><input /><input /></div>' +
						'<div class="wegovSliderValue"><p>${ sentiment }</p></div>' +
						'<input name="sliderValue" type="hidden" value="${ sentiment }" />' +
					'</div>' +
				'</td>' +
*/
			'</tr>';
	return html;
}

