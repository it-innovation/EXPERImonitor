/////////////////////////////////////////////////////////////////////////
//
// ¬© University of Southampton IT Innovation Centre, 2012
//
// Copyright in this library belongs to the University of Southampton
// University Road, Highfield, Southampton, UK, SO17 1BJ
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
//	Created By :			Maxim Bashevoy
//	Created Date :			2012-08-21
//	Created for Project :           Experimedia
//
/////////////////////////////////////////////////////////////////////////


var internalPhase = -1;
var actionButton;

$(document).ready(function() {
    actionButton = $("#actionButton");

    // Get current phase so that we know where we are with the experiment
    $.ajax({
        type: 'GET',
        url: "/em/getcurrentphase/do.json",
        contentType: "application/json; charset=utf-8",
        success: function(currentPhase){
            console.log(currentPhase);

            if (currentPhase != null) {
                internalPhase = currentPhase.index;

                console.log('Current phase is: ' + currentPhase.description + ' (' + currentPhase.index + ')');

                $("#currentPhaseName").text(currentPhase.description);
                $("#currentPhaseID").text(currentPhase.index);
                $("#currentPhaseDescription").text("Waiting for clients to connect");

                displayExperimentInfo();

                // RELOAD CLIENTS LIST FOR EVERY PHASE AS
                // NOT ALL CLIENTS SUPPORT ALL PHASES
                switch(internalPhase) {
                    // CLIENTS CONNECTING PHASE
                    case 0:
                        // TEST DISCONNECTING CLIENTS - does not work for some reason, works on Simon's computer
                        doClientsConnectingPhase(actionButton, currentPhase);
                        break;
                    // DISCOVERY PHASE
                    case 1:
                        doDiscoveryPhase(actionButton, currentPhase);
                        break;

                    // SET-UP PHASE
                    case 2:
                        doSetupPhase(actionButton, currentPhase);
                        break;

                    // LIVE MONITORING PHASE
                    case 3:
                        doLiveMonitoringPhase(actionButton, currentPhase)
                        break;

                    // POST REPORT PHASE
                    case 4:
                        doPostReportPhase(actionButton, currentPhase)
                        break;

                    // TEAR-DOWN PHASE
                    case 5:
                        doTearDownPhase(actionButton, currentPhase)
                        break;

                    // UNKNOWN PHASE
                    default:
                        console.log('ERROR: UNKNOWN PHASE');
                        actionButton.text('ERROR');
                        actionButton.click(function(e){
                            e.preventDefault();
                        })
                        console.error('Current phase is ' + internalPhase + ', not sure what to do with it, will stop now.');
                        $("#currentPhaseName").text("ERROR - current phase is unknown (" + internalPhase + ")");
                        $("#currentPhaseID").text(internalPhase);
                        break;
                }

            } else {
                console.error("Current phase is NULL, will stop now.");
                $("#currentPhaseName").text("ERROR - current phase is NULL");
                $("#currentPhaseID").text(internalPhase);
            }

        },
        error: function() {
            console.error('Failed to fetch current phase, will stop now.');
            $("#currentPhaseName").text("ERROR - failed to get current phase");
            $("#currentPhaseID").text(internalPhase);
        }
    });

});

function displayExperimentInfo() {
    // Get experiment info
    if (internalPhase > 0) {
        $.ajax({
            type: 'GET',
            url: "/da/getexperiments/do.json",
            contentType: "application/json; charset=utf-8",
            success: function(experiments){
                console.log(experiments);

                if (experiments == null) {
                    alert("Server error retrieving experiments");
                    console.error("Server error retrieving experiments");
                    return;
                } else {
                    if (experiments.length < 1) {
                        console.debug("No experiments found, retrying in 2 seconds");
                        setTimeout(function(){displayExperimentInfo()}, 2000);
                        // alert("No experiments found");
                    } else {

                        $(".experimentInfo").css('display', 'block');
                        theExperiment = experiments[0]; // we are only going to have one per dashboard for now
                        $("#experimentName").text(theExperiment.name);
                        $("#experimentUUID").text(theExperiment.uuid);
                        $("#experimentDescription").text(theExperiment.description);
                        $("#experimentExperimentID").text(theExperiment.experimentID);
                        $("#experimentStartTime").text(longToDate(theExperiment.startTime));
                        if (theExperiment.endTime == 0)
                            $("#experimentEndTime").text('In progress');
                        else
                            $("#experimentEndTime").text(longToDate(theExperiment.endTime));

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
    }
}

function doClientsConnectingPhase(actionButton, currentPhase) {
    console.log('In CLIENTS CONNECTING PHASE');

    actionButton.removeClass('alert');

    // Setup next phase: Discovery phase
    prepareDiscoveryPhase(actionButton);
}

function prepareDiscoveryPhase(actionButton) {
    actionButton.text('Start Discovery Phase');
    actionButton.unbind('click');
    actionButton.click(function(e){
        e.preventDefault();
        console.log('Starting the experiment');
        $.ajax({
            type: 'GET',
            url: "/em/startlifecycle/do.json",
            contentType: "application/json; charset=utf-8",
            success: function(currentPhase){
                console.log(currentPhase);
                if (currentPhase != null) {
                    console.log('Experiment started, returned phase is: ' + currentPhase.description + ' (' + currentPhase.index + ')');
                    internalPhase = currentPhase.index;
                    doDiscoveryPhase(actionButton, currentPhase);
                } else {
                    console.error('Failed to start the experiment: next phase is NULL. Stopped');
                }

            },
            error: function() {
                console.error('Failed to start the experiment. Stopped');
            }
        });

    });
}


function doDiscoveryPhase(actionButton, currentPhase) {
    console.log('In DISCOVERY PHASE');

    $("#currentPhaseName").text(currentPhase.description);
    $("#currentPhaseID").text(currentPhase.index);
    $("#currentPhaseDescription").text("Clients reporting metric generators");

//    $(".clientlist .clientitem").first().trigger('click');

    // Show experiment details
    displayExperimentInfo();

    // Show metric generators
    getMetricGenerators();

    prepareSetupPhase(actionButton);

}

// Populates metrics generators' list
function getMetricGenerators() {
    $.ajax({
        type: 'GET',
        url: "/em/getmetricgenerators/do.json",
        contentType: "application/json; charset=utf-8",
        success: function(metricGenerators){
            
            console.log(metricGenerators);
            
            if (metricGenerators.length < 1) {
                console.debug("No metric generators found, retrying in 2 seconds");
                setTimeout(function(){getMetricGenerators()}, 2000);
            } else {
                var mgObj;
                $.each(metricGenerators, function(index, mg){
                    mgObj = $('<p class="metricgenitem">' + mg.name + '<span>' + mg.listOfEntities[0].name + '</span></p>').appendTo(".metricgenlist");

                    mgObj.data('mg', mg);
                    
                    mgObj.click(function(){
                        var mgdata = $(this).data().mg;
//                        console.log(mgdata);

                        $(".metricgenlist .metricgenitem").removeClass('active');
                        $(this).addClass('active');

                        var md = $(".metricgendetails");
                        md.empty();

                        md.append('<p class="metricGeneratorHeader">' + mgdata.name + ' (' + mgdata.description + ')</p>');
                        md.append('<p class="metricGeneratorDescription">UUID: ' + mgdata.uuid + '</p>');
                        md.append('<p class="metricGeneratorDescription">Entities: ' + mgdata.listOfEntities[0].name + ' (' + mgdata.listOfEntities[0].description + ')</p>');
    
                        $.each(mgdata.listOfMetricGroups, function(indexMetricGroup, metricGroup){
                            md.append('<p class="metricGroupHeader">Metric Group ' + (indexMetricGroup + 1) + ': ' + metricGroup.name + ' (' + metricGroup.description + ')</p>');
                            md.append('<p class="metricGroupSubheader">' + metricGroup.uuid + '</p>');
                            
                            var counter = 0; // for unique div IDs required by jqPlot
                            var measurementSetContainer = $('<div class="twelve columns"></div>').appendTo($('<div class="row"></div>').appendTo(md));
                            $.each(metricGroup.measurementSets, function(indexMeasurementSet, measurementSet){
                                var ad = $('<div class="attributediv"></div>').appendTo(measurementSetContainer);
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
                                
                                counter++;
                            });
                            
                        });

    //                                        md.append('<a href="#" class="small button radius normal inpanelbutton">Add to Dashboard</a>');
                        /*
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
                        }); */
                    });                    
                });
                
                $(".metricgenlist .metricgenitem").first().trigger('click');
            }
        }
    });
}

function prepareSetupPhase(actionButton) {
    actionButton.text('Start Set-up Phase');
    actionButton.unbind('click');
    actionButton.click(function(e){
        e.preventDefault();
        console.log('Starting set-up phase');
        $.ajax({
            type: 'GET',
            url: "/em/gotonextphase/do.json",
            contentType: "application/json; charset=utf-8",
            success: function(currentPhase){
                console.log(currentPhase);
                if (currentPhase != null) {
                    console.log('Set-up phase started, returned phase is: ' + currentPhase.description + ' (' + currentPhase.index + ')');
                    internalPhase = currentPhase.index;
                    $("#currentPhaseName").text(currentPhase.description);
                    $("#currentPhaseID").text(currentPhase.index);
                    doSetupPhase(actionButton, currentPhase);
                } else {
                    console.error('Failed to start the set-up phase: next phase is NULL. Stopped');
                }
            },
            error: function() {
                console.error('Failed to start the set-up phase. Stopped');
            }
        });
    });
}

function doSetupPhase(actionButton, currentPhase) {
    console.log('In SET-UP PHASE');

    $("#currentPhaseName").text(currentPhase.description);
    $("#currentPhaseID").text(currentPhase.index);
    $("#currentPhaseDescription").text("Setting-up clients");

    prepareLiveMonitoringPhase(actionButton);
}

function prepareLiveMonitoringPhase(actionButton) {
    actionButton.text('Start Live Monitoring Phase');
    actionButton.unbind('click');
    actionButton.click(function(e){
        e.preventDefault();
        console.log('Starting live monitoring phase');
        $.ajax({
            type: 'GET',
            url: "/em/gotonextphase/do.json",
            contentType: "application/json; charset=utf-8",
            success: function(currentPhase){
                console.log(currentPhase);
                if (currentPhase != null) {
                    console.log('Live monitoring phase started, returned phase is: ' + currentPhase.description + ' (' + currentPhase.index + ')');
                    internalPhase = currentPhase.index;
                    $("#currentPhaseName").text(currentPhase.description);
                    $("#currentPhaseID").text(currentPhase.index);
                    doLiveMonitoringPhase(actionButton, currentPhase);
                } else {
                    console.error('Failed to start live monitoring phase: next phase is NULL. Stopped');
                }
            },
            error: function() {
                console.error('Failed to start live monitoring phase. Stopped');
            }
        });
    });
}

function doLiveMonitoringPhase(actionButton, currentPhase){
    console.log('In LIVE MONITORING PHASE');

    $("#currentPhaseName").text(currentPhase.description);
    $("#currentPhaseID").text(currentPhase.index);
    $("#currentPhaseDescription").text("Live monitoring of clients");

    preparePostReportPhase(actionButton);
}

function preparePostReportPhase(actionButton) {
    actionButton.text('Start Post Report Phase');
    actionButton.unbind('click');
    actionButton.click(function(e){
        e.preventDefault();
        console.log('Starting post report phase');
        $.ajax({
            type: 'GET',
            url: "/em/gotonextphase/do.json",
            contentType: "application/json; charset=utf-8",
            success: function(currentPhase){
                console.log(currentPhase);
                if (currentPhase != null) {
                    console.log('Post report phase started, returned phase is: ' + currentPhase.description + ' (' + currentPhase.index + ')');
                    internalPhase = currentPhase.index;
                    $("#currentPhaseName").text(currentPhase.description);
                    $("#currentPhaseID").text(currentPhase.index);
                    doPostReportPhase(actionButton, currentPhase);
                } else {
                    console.error('Failed to start post report phase: next phase is NULL. Stopped');
                }
            },
            error: function() {
                console.error('Failed to start post report phase. Stopped');
            }
        });
    });
}


function doPostReportPhase(actionButton, currentPhase){
    console.log('In POST REPORT PHASE');

    $("#currentPhaseName").text(currentPhase.description);
    $("#currentPhaseID").text(currentPhase.index);
    $("#currentPhaseDescription").text("Collecting post reports from clients");

    prepareTearDownPhase(actionButton);
}


function prepareTearDownPhase(actionButton) {
    actionButton.text('Start Tear Down Phase');
    actionButton.unbind('click');
    actionButton.click(function(e){
        e.preventDefault();
        console.log('Starting tear down phase');
        $.ajax({
            type: 'GET',
            url: "/em/gotonextphase/do.json",
            contentType: "application/json; charset=utf-8",
            success: function(currentPhase){
                console.log(currentPhase);
                if (currentPhase != null) {
                    console.log('Tear down phase started, returned phase is: ' + currentPhase.description + ' (' + currentPhase.index + ')');
                    internalPhase = currentPhase.index;
                    $("#currentPhaseName").text(currentPhase.description);
                    $("#currentPhaseID").text(currentPhase.index);
                    doTearDownPhase(actionButton, currentPhase);
                } else {
                    console.error('Failed to start tear down phase: next phase is NULL. Stopped');
                }
            },
            error: function() {
                console.error('Failed to start tear down phase. Stopped');
            }
        });
    });
}


function doTearDownPhase(actionButton, currentPhase){
    console.log('In TEAR-DOWN PHASE');
    actionButton.text('Experiment finished');
    actionButton.addClass('alert');
    actionButton.unbind('click');

    actionButton.click(function(e){
        e.preventDefault();
        alert('Experiment is complete, no phases left');
    });

    $("#currentPhaseName").text(currentPhase.description);
    $("#currentPhaseID").text(currentPhase.index);
    $("#currentPhaseDescription").text("Experiment is complete!");

}

/*
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

*/