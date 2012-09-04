$(document).ready(function() {

    // Get the Experiment
    $.ajax({
        type: 'GET',
        url: "/da/getexperiments/do.json",
        contentType: "application/json; charset=utf-8",
        dataType: 'json',
        success: function(experiments){
            console.log(experiments);

            if (experiments == null) {
                alert("Server error retrieving experiments");
                console.error("Server error retrieving experiments");
                return;
            } else {
                if (experiments.length < 1) {
                    alert("No experiments found");
                    console.error("No experiments found");
                } else {
                    theExperiment = experiments[0]; // we are only going to have one per dashboard for now
                    $("#experimentName").text(theExperiment.name);
                    $("#experimentUUID").text(theExperiment.uuid);
                    $("#experimentDescription").text(theExperiment.description);
                    $("#experimentExperimentID").text(theExperiment.experimentID);
                    $("#experimentStartTime").text(longToDate(theExperiment.startTime));
                    $("#experimentEndTime").text(longToDate(theExperiment.endTime));

                    // Get metric generators
                    $.ajax({
                        type: 'POST',
                        url: "/da/getmetricgenerators/do.json",
                        data: JSON.stringify({experimentUUID: theExperiment.uuid}),
                        contentType: "application/json; charset=utf-8",
                        dataType: 'json',
                        success: function(metricGenerators){
                            console.log(metricGenerators);

                            var mgObj;
                            $.each(metricGenerators, function(index, mg){
                                mgObj = $('<p class="metricgenitem">' + mg.name + '<span>' + mg.listOfEntities + '</span></p>').appendTo(".metricgenlist");

                                mgObj.data('mg', mg);

                                mgObj.click(function(){
                                    var mgdata = $(this).data().mg;
                                    console.log(mgdata);

                                    $(".metricgenlist .metricgenitem").removeClass('active');
                                    $(this).addClass('active');

                                    var md = $(".metricgendetails");
                                    md.empty();

                                    md.append('<p class="metricGeneratorHeader">' + mgdata.name + ' (' + mgdata.description + ')</p>');
                                    md.append('<p class="metricGeneratorDescription">UUID: ' + mgdata.uuid + '</p>');
                                    md.append('<p class="metricGeneratorDescription">Entities: ' + mgdata.listOfEntities + '</p>');
    //                                        md.append('<a href="#" class="small button radius normal inpanelbutton">Add to Dashboard</a>');

                                    // Get metric groups
                                    $.ajax({
                                        type: 'POST',
                                        url: "/da/getmetricgroups/do.json",
                                        data: JSON.stringify({metricGeneratorUUID: mgdata.uuid}),
                                        contentType: "application/json; charset=utf-8",
                                        dataType: 'json',
                                        success: function(metricGroups){
                                            console.log(metricGroups);

                                            var counter = 0;
                                            $.each(metricGroups, function(indexMetricGroup, metricGroup){
                                                md.append('<p class="metricGroupHeader">Metric Group ' + (indexMetricGroup + 1) + ': ' + metricGroup.name + ' (' + metricGroup.description + ')</p>');
                                                md.append('<p class="metricGroupSubheader">' + metricGroup.uuid + '</p>');
                                                
                                                var measurementSetContainer = $('<div class="twelve columns"></div>').appendTo($('<div class="row"></div>').appendTo(md));
                                                $.each(metricGroup.measurementSets, function(indexMeasurementSet, measurementSet){
//                                                    measurementSetContainer.append('<p class="measurementSetHeader">Measurement Set ' + (indexMeasurementSet + 1) + ': ' + measurementSet.attribute + ' (' + measurementSet.metricUnit + ', ' + measurementSet.metricType + ')</p>');
//                                                    measurementSetContainer.append('<p class="measurementSetSubheader">' + measurementSet.uuid + '</p>');
                                                    makeFakeUsageDataForContainer(measurementSetContainer, counter, indexMeasurementSet, measurementSet);
                                                    counter++;
                                                });

                                            });
                                        }
                                    });
                                });
                            });
                            $(".metricgenlist .metricgenitem").first().trigger('click');
                        }
                    });
                }
            }
        },
        error: function(xhr, ajaxOptions, thrownError){
            alert('Failed to get a list of experiments');
            console.error('Failed to get a list of experiments');
            console.error(thrownError);
            console.error(xhr.status);
        }
    });

});

function makeFakeUsageDataForContainer(container, counter, indexMeasurementSet, measurementSet) {
//    var attributeDivWrapper = $('<div class="row"></div>').appendTo(container);
//    var ad = $('<div class="twelve columns attributediv"></div>').appendTo(attributeDivWrapper);
    var ad = $('<div class="attributediv"></div>').appendTo(container);
    ad.append('<p class="header">Measurement Set ' + (indexMeasurementSet + 1) + ': ' + measurementSet.attribute + ' (' + measurementSet.metricUnit + ', ' + measurementSet.metricType + ')<span>collapse</span></p>');

    ad.append('<p class="parameters">UUID: ' + measurementSet.uuid + '</p>');
    ad.append('<p class="parameters">Last metric report: ' + randomDate(new Date(2012, 0, 1), new Date()) + '</p>');
    ad.append('<p class="parameters">Total number of reports: ' + (Math.floor(Math.random()*11)) + '</p>');

    var jqplotContainerID = "representationContainer" + counter;
    var graphSelectorID = "graphselector" + counter;
    var tableSelectorID = "tableselector" + counter;

    var graphDataSwitcher = $('<div class="graphDataSwitcher"></div>').appendTo(ad);
    var graphSelectorButton = $('<div id="' + graphSelectorID + '" class="switchbutton"><p>Graph</p></div>').appendTo(graphDataSwitcher);
    var tableSelectorButton = $('<div id="' + tableSelectorID + '" class="switchbutton"><p>History data</p></div>').appendTo(graphDataSwitcher);

    graphSelectorButton.data('counter', counter);
    graphSelectorButton.data('jqplotContainerID', jqplotContainerID);
    tableSelectorButton.data('counter', counter);
    tableSelectorButton.data('jqplotContainerID', jqplotContainerID);

    $('<div id="'+ jqplotContainerID + '" class="row"></div>').appendTo(ad);

    $(".graphDataSwitcher .switchbutton").click(function(){
        $(this).parent().find('.switchbutton').removeClass('active');
        $(this).addClass('active');

        var counter = $(this).data().counter;
        var jqplotContainerID = $(this).data().jqplotContainerID;

        $('#' + jqplotContainerID).empty();
        var dataDivGraphsAndHistory = $('<div class="eleven columns centered"></div>').appendTo($('#' + jqplotContainerID));
        var plotdata = [['2008-09-30 4:00PM',4], ['2008-10-30 4:00PM',6.5], ['2008-11-30 4:00PM',5.7], ['2008-12-30 4:00PM',9]];

        if ($(this).attr('id').indexOf('g') === 0) {
            dataDivGraphsAndHistory.append('<div id="dataplot' + counter + '" class="extraspacebottom"></div>');

            $.jqplot ('dataplot' + counter, [plotdata], {
                title:'Latency (ms)',
                axes:{
                    xaxis:{
                        label:'Timestamp (mmm dd, yy)',
                        renderer:$.jqplot.DateAxisRenderer,
                        tickOptions:{formatString:'%b %#d, %y'}
                    }

                }
            });
        } else {
            var dataTable = $('<table class="metricstable"><tbody></tbody></table>').appendTo($('<div id="datatablecontainer' + counter + '" class="extraspacebottom"></div>').appendTo(dataDivGraphsAndHistory));
            dataTable.append('<tr><th>Timestamp, yyyy-mm-dd HH:MM</th><th>Value, ms</th></tr>');
            $.each(plotdata, function(key, value){
                dataTable.append('<tr><td>' + value[0] + '</td><td>' + value[1] + '</td></tr>');
            });

        }
    });


    $(".graphDataSwitcher .switchbutton:first").trigger('click');
}