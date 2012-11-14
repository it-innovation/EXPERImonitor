function addScheduledJobsWidget() {
	console.log("addScheduledJobsWidget");
	var myDiv = $("#scheduledJobs");
	myDiv.empty();
	
	var myContainer = $("<div class=\"widgetContainer\" id=\"kendoScheduledJobsGridContainer\"></div>").appendTo(myDiv);
	
                        var dataSource = new kendo.data.DataSource({
                            transport: {
                                read:  {
                                    url: "/home/getScheduledJobs/do.json",
                                    dataType: "json"
                                },
                                destroy: {
                                    url: "/home/deleteJobSchedule/do.json",
                                    dataType: "json"
                                },
                                parameterMap: function(options, operation) {
									//console.log(options);
									console.log(kendo.stringify(options));
									console.log(operation);
                                    if (operation !== "read" && options.key) {
										console.log(kendo.stringify(options.key));
                                        return {scheduleId: options.key.name};
                                    }
                                }
                            },
                            pageSize: 30,
                            schema: {
                                model: {
                                    id: "id",
                                    fields: {
										objectName: {editable: false, nullable: true},
										id: {editable: false, nullable: true},
										startTime: {editable: false, nullable: true},
										previousFireTime: {editable: false, nullable: true},
										nextFireTime: {editable: false, nullable: true},
										endTime: {editable: false, nullable: true},
										status: {editable: false, nullable: true},
                                    }
                                }
                            }
                        });

	myContainer.kendoGrid({
		/*
        dataSource: {
            //type: "json",
            serverPaging: false,
            serverSorting: false,
            transport: {
                read: {
					url: "/home/getScheduledJobs/do.json",
					dataType: "json"
				},
				destroy: {
					url: "/home/getScheduledJobs/do.json",
					dataType: "json"
				},
            },
            schema: {
				model: {
					id: "id",
					fields: {
						objectName: {editable: false, nullable: true},
						id: {editable: false, nullable: true},
					}
				},
            	data: function(result) {
					return result;
				},
				total: function(result) {
					return result.length;
				}            	
            },
            sort: {
            	field: "whenCreated", dir : "desc"
            },
            pageSize: 10
        },*/
        dataSource: dataSource,
        columns: [
                  //{title: "ID", field: "key.name", width: "160px"},
                  {title: "Name", field: "objectName", width: "100px"},
                  //{title: "Description", field: "description", width: "80px"},
                  //{title: "Job Type", field: "jobType", width: "80px"},
                  {title: "Activity ID", field: "id", width: "60px"},
                  {title: "Start Time", template: '#= startTime? kendo.toString(new Date(startTime),"yyyy-MM-dd HH:mm") : "" #', width: "100px"},
                  {title: "Prev Start Time", template: '#= previousFireTime ? kendo.toString(new Date(previousFireTime),"yyyy-MM-dd HH:mm") : "" #', width: "100px"},
                  {title: "Next Start Time", template: '#= nextFireTime? kendo.toString(new Date(nextFireTime),"yyyy-MM-dd HH:mm") : "" #', width: "100px"},
                  {title: "End Time", template: '#= endTime? kendo.toString(new Date(endTime),"yyyy-MM-dd HH:mm") : "" #', width: "100px"},
                  {title: "Status", field: "status", width: "60px"},
                  {title: "Action", command: "destroy", width: "80px"}

        ],
		//editable: {
		//	update: true, // puts the row in edit mode when it is clicked
		//	destroy: true, // does not remove the row when it is deleted, but marks it for deletion
		//	confirmation: "Cancel this schedule?"
		//},
		editable: "inline",
			/*
//        height: auto,
        //detailInit: detailInit,
        dataBound: function() {
            this.expandRow(this.tbody.find("tr.k-master-row").first());
        },               
        //change: onSearchResultsGridChange,
        filterable: true,
        sortable: true,
        pageable: true,
        groupable: true,
        //selectable: "row" */
    });

	/* myContainer.delegate(".cancel-button", "click", function(e) {
                        e.preventDefault();

                        //var dataItem = grid.dataItem($(this).closest("tr"));
                        //wnd.content(detailsTemplate(dataItem));
                        //wnd.center().open();
						alert('Cancelling schedule');
                    }); */
}
