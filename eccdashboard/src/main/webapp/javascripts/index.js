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

var isClientMonitoringOn = true;
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

                getClients();

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

    // Client monitoring OFF
    isClientMonitoringOn = false;

    $(".clientlist .clientitem").first().trigger('click');
    
    // Show experiment details
    displayExperimentInfo();        
    
    // Get list of clients
    getCurrentPhaseClients();
    
    prepareSetupPhase(actionButton);

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

    // Client monitoring OFF
    isClientMonitoringOn = false;
    
    // Get list of clients
    getCurrentPhaseClients();    
    
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

    // Client monitoring OFF
    isClientMonitoringOn = false;
    
    // Get list of clients
    getCurrentPhaseClients();    
    
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

    // Client monitoring OFF
    isClientMonitoringOn = false;
    
    // Get list of clients
    getCurrentPhaseClients();    
    
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

    // Client monitoring OFF
    isClientMonitoringOn = false;
    
    // Get list of clients
    getCurrentPhaseClients();    
    
}

function getMetricGeneratorsForClient(clientUUID) {
        
        if (!isClientMonitoringOn) {
            console.log('Getting metric generators for client with UUID: ' + clientUUID);
        
            $.ajax({
                type: 'POST',
                url: "/em/getmmetricgeneratorsforclient/do.json",
                data: JSON.stringify({clientUUID: clientUUID}),
                contentType: "application/json; charset=utf-8",
                success: function(metricGenerators){
                    console.log(metricGenerators);

                    if (metricGenerators.length < 1) {
                        console.log('No metric generators returned, waiting 2 seconds and trying again');
                        setTimeout(function(){getMetricGeneratorsForClient(clientUUID)}, 2000);
                    }

                    var detailsRow = $('<div class="row"></div>').appendTo(".clientdetails");

                    $.each(metricGenerators, function(indexMG, metricGenerator){
                        var measurementSetDetailsContainer = $('<div class="twelve columns"></div>').appendTo(detailsRow);
                        measurementSetDetailsContainer.data('metricGenerator', metricGenerator);

                        measurementSetDetailsContainer.append('<p class="metricMonitorHeader">Measurement Set ' + (indexMG + 1) + ':</p>');
                        measurementSetDetailsContainer.append('<p class="metricMonitorDescription">Name: ' + metricGenerator.name + '</p>');
                        measurementSetDetailsContainer.append('<p class="metricMonitorDescription">Description: ' + metricGenerator.description + '</p>');
                        measurementSetDetailsContainer.append('<p class="metricMonitorDescription">UUID: ' + metricGenerator.uuid + '</p>');

                        var moreDetailsRow = $('<div class="row moreDetailsRow"></div>').appendTo(measurementSetDetailsContainer);
                        var entitiesDivWrapper = $('<div class="four columns nopaddingright"></div>').appendTo(moreDetailsRow);
                        var entitiesDiv = $('<div class="capabilitieslistbg"></div>').appendTo(entitiesDivWrapper);
                        var attributesDivWrapper = $('<div class="four columns nopaddingright"></div>').appendTo(moreDetailsRow);
                        var attributesDiv = $('<div class="capabilitieslistbg"></div>').appendTo(attributesDivWrapper);
                        var infoDivWrapper = $('<div class="four columns"></div>').appendTo(moreDetailsRow);
                        var infoDiv = $('<div class="capabilitieslistbg"></div>').appendTo(infoDivWrapper);

                        entitiesDiv.append('<p><strong>Entities</strong></p>');
                        attributesDiv.append('<p><strong>Attributes</strong></p>');
                        infoDiv.append('<p><strong>Info</strong></p>');

                        var entitiesList = $('<div class="entitiesList"></div>').appendTo(entitiesDiv);
                        var attributesList = $('<div class="attributesList"></div>').appendTo(attributesDiv);
                        var infoList = $('<div class="infoList"></div>').appendTo(infoDiv);

                        var theEntity;
                        $.each(metricGenerator.listOfEntities, function(indexEntity, entity){
                            theEntity = $('<p class="noextrawhitespace entitiesitem">' + entity.name + '</p>').appendTo(entitiesList);
                            theEntity.data('entity', entity);

                            theEntity.click(function(e){
                                e.preventDefault();

                                $(this).parents('.entitiesList').children('.entitiesitem').removeClass('active');
                                $(this).addClass('active');

                                attributesList.empty();

                                var entity = $(this).data('entity');

                                var theAttribute;
                                $.each(entity.attributes, function(indexAttribute, attribute){
                                    theAttribute = $('<p class="noextrawhitespace attributesitem">' + attribute.name + '</p>').appendTo(attributesList);
                                    theAttribute.data('attribute', attribute);

                                    theAttribute.click(function(ev){
                                        ev.preventDefault();

                                        $(this).parents('.attributesList').children('.attributesitem').removeClass('active');
                                        $(this).addClass('active');

                                        infoList.empty();

                                        var attribute = $(this).data('attribute');

                                        infoList.append('<p class="noextrawhitespace">UUID: ' + attribute.uuid + '</p>');
                                        infoList.append('<p class="noextrawhitespace">Name: ' + attribute.name + '</p>');
                                        infoList.append('<p class="noextrawhitespace">Description: ' + attribute.description + '</p>');
                                    });
                                });

                                if ($(".attributesList .active").size() < 1)
                                    $(".attributesList .attributesitem").first().trigger('click');

                            });
                        });


                    });

    //                $('.entitiesitem').click(function(){
    //                    $(this).parents('.entitiesList').children('.entitiesitem').removeClass('active');
    //                    $(this).addClass('active');
    //                });

                    if ($(".entitiesList .active").size() < 1)
                        $(".entitiesList .entitiesitem").first().trigger('click');


                    // Setup next phase: Set-up phase
//                    prepareSetupPhase(actionButton);

                },
                error: function() {
                    console.error("Failed to get a list of metric generators");
                }
            });
        } else {
//            console.log('NOT getting metric generators for client with UUID: ' + clientUUID);
        }
}

function getCurrentPhaseClients() {
    $.ajax({
        type: 'GET',
        url: "/em/getcurrentphaseclients/do.json",
        contentType: "application/json; charset=utf-8",
        success: function(clients){
            console.log(clients);
        }   
    });
}

function getClients() {
    if (isClientMonitoringOn) {
        $.ajax({
            type: 'GET',
            url: "/em/getclients/do.json",
            contentType: "application/json; charset=utf-8",
            success: function(clients){
    //            console.log(clients);
                if (clients.length == 0) {
                    $(".clientdetails").empty();
                    $(".clientdetails").append('<h6>No clients detected</h6>');

                } else {
                    $(".clientlist").empty();
                    var theClient;
                    $.each(clients, function(indexClient, client){
    //                    if (jQuery.inArray(client.uuid, currentClientsUuids) < 0) {

                            console.log("Client: " + client.uuid);
                            console.log(client);

    //                        currentClientsUuids.push(client.uuid);

                            theClient = $('<p class="clientitem">' + client.name + '<span>' + client.uuid + '</span></p>').appendTo($('.clientlist'));
                            theClient.data('client', client);

                            theClient.click(function(){
                                $(".clientlist .clientitem").removeClass('active');
                                $(this).addClass('active');

                                var clientData = $(this).data().client;

                                var cd = $('.clientdetails');
                                cd.empty();
                                cd.append('<p class="clientHeader">' + clientData.name + '</p>');
                                cd.append('<p class="clientDescription">UUID: ' + clientData.uuid + '</p>');

                                getMetricGeneratorsForClient(clientData.uuid);

    //                            $.ajax({
    //                                type: 'POST',
    //                                url: "/em/getmeasurementsetsforclient/do.json",
    //                                data: JSON.stringify({clientUUID: clientData.uuid}),
    //                                contentType: "application/json; charset=utf-8",
    //                                success: function(measurementSets){
    //                                    if (measurementSets == null) {
    //                                        console.error('Failed to retrieve measurement sets for client ' + clientData.uuid + ' because the client is no longer connected.');
    //                                    } else {
    //                                        console.log(measurementSets);
    //                                    }
    //
    //                                    $.each(measurementSets, function(indexMeasurementSet, measurementSet){
    //                                        var detailsRow = $('<div class="row"></div>').appendTo(".clientdetails");
    //                                        var measurementSetDetailsContainer = $('<div class="twelve columns"></div>').appendTo(detailsRow);
    //                                        measurementSetDetailsContainer.data('measurementSet', measurementSet);
    //
    //                                        measurementSetDetailsContainer.append('<p class="metricMonitorHeader">Measurement Set ' + (indexMeasurementSet + 1) + ':</p>');
    //                                        measurementSetDetailsContainer.append('<p class="metricMonitorDescription">UUID: ' + measurementSet.uuid + '</p>');
    //                                        measurementSetDetailsContainer.append('<p class="metricMonitorDescription">Metric Unit: ' + measurementSet.metricUnit + '</p>');
    //                                        measurementSetDetailsContainer.append('<p class="metricMonitorDescription">Metric UUID: ' + measurementSet.metricUUID + '</p>');
    //                                        measurementSetDetailsContainer.append('<p class="metricMonitorDescription">Metric Type: ' + measurementSet.metricType + '</p>');
    //
    //                                    });
    //                                },
    //                                error: function() {
    //                                    console.error('Failed to retrieve measurement sets for client ' + clientData.uuid);
    //                                }
    //                            });

                            });

                            // If no clients selected, select the first one
                            if ($(".clientlist .active").size() < 1)
                                $(".clientlist .clientitem").first().trigger('click');

    //                    }
                    });
                }

                if (isClientMonitoringOn) {
                    setTimeout(function(){getClients()}, 3000);
                }
            },
            error: function() {
                // TODO
                console.error("Error getting clients list, monitoring stopped");
                isClientMonitoringOn = false;
            }
        });
    }
}