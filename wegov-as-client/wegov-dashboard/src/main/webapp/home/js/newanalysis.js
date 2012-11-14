$(document).ready(function() {
	
	$.ajax({
		url:"/home/getsearches/do.json",
		dataType: 'json',	
		type: 'GET',
		error: function(){

		},
		success: function(data) {
			console.log(data);
			var singleSearchWrapper;
			$.each(data["data"], function(searchCounter, singleSearch){
				singleSearchWrapper = $("<div class=\"singleSearchWrapper\"></div>").appendTo("#listOfSearchesForInput");
				singleSearchWrapper.append("<input type=\"checkbox\" id=\"singleSearchCheckbox_" + searchCounter + "\"/>");
				singleSearchWrapper.append("<p>" + singleSearch["name"] + "</p><br>");
				singleSearchWrapper.append("<input type=\"radio\" name=\"whichRuns_" + searchCounter + "\" checked=\"checked\"/><p>Last Run</p>");
				singleSearchWrapper.append("<input type=\"radio\" name=\"whichRuns_" + searchCounter + "\"/><p>All Runs</p>");
				singleSearchWrapper.append("<input type=\"radio\" name=\"whichRuns_" + searchCounter + "\"/><p>Selected Runs</p>");
			});
		}
	});
	
	addSearchesWidget("all");
});

function addSearchesWidget(whichActivities) {
	var myDiv = $("#historyOfAnalysis");
	
	var myContainer = $("<div class=\"widgetContainer\" id=\"kendoGridContainer\"></div>").appendTo(myDiv);
	
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
            },
            sort: {
            	field: "whenCreated", dir : "desc"
            },
            pageSize: 10
        },
        columns: [
                  {title: "Name", field: "name"},
                  {title: "Status", field: "status", width: "80px"},
                  {title: "Created on", field: "whenCreated", width: "160px"},
        ],
        detailInit: detailInit,
        dataBound: function() {
            this.expandRow(this.tbody.find("tr.k-master-row").first());
        },               
        filterable: true,
        sortable: true,
        pageable: true,
        groupable: true,
        selectable: "row"
    });	
}

function detailInit(e) {
    $("<div/>").appendTo(e.detailCell).kendoGrid({
        dataSource: {
            type: "json",
            serverPaging: false,
            serverSorting: false,
            transport: {
                read: "/home/getruns/do.json" // TODO: only load runs from the activity
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
            },
            pageSize: 10
        },
        columns: [
                  {title: "Name", field: "name"},
                  {title: "Status", field: "status", width: "80px"},
                  {title: "Started on", field: "whenStarted", width: "160px"},
                  {title: "Finished on", field: "whenFinished", width: "160px"}
        ],        
        scrollable: false,
        sortable: true,
        pageable: true,
        selectable: "row"
    });
}