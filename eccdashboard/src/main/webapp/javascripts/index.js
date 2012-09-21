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

var counter = 0;
var currentClientsUuids = [];
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

                console.log('Current phase is: ' + currentPhase.description + ' (' + currentPhase.index + ')');

                $("#currentPhaseName").text(currentPhase.description);
                $("#currentPhaseID").text(currentPhase.index);
                $("#currentPhaseDescription").text("Waiting for clients to connect");


                switch(internalPhase) {
                    // CLIENTS CONNECTING PHASE
                    case 0:
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
                        console.log('In LIVE MONITORING PHASE');
                        actionButton.text('Start Post Report Phase');
                        break;

                    // POST REPORT PHASE
                    case 4:
                        console.log('In POST REPORT PHASE');
                        actionButton.text('Start Tear Down Phase');
                        break;

                    // TEAR-DOWN PHASE
                    case 5:
                        console.log('In TEAR-DOWN PHASE');
                        actionButton.text('Experiment finished - restart');
                        $(this).addClass('alert');
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

    // Get clients

    // Get the Experiment - later
    /*
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
                    alert("No experiments found");
                    console.error("No experiments found");
                } else {
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

    */


    // Get current phase
//    getPhase();

    // Get clients
    getClients(null);

    // Action button
    //var actionButton = $("#actionButton");
//    $("#currentPhaseID").text(internalPhase);

/*
    actionButton.click(function(e){
        e.preventDefault();

        // TO DISCOVERY PHASE
        if (internalPhase == 0) {
            $(this).text('Start Set-up Phase');
            $(".experimentInfo").css('display', 'block');
            $("#currentPhaseName").text('EM Metric generator discovery phase');
            $("#currentPhaseDescription").text("Clients reporting metric generators");

            console.log('Starting monitoring process');
            $.ajax({
                type: 'GET',
                url: "/em/startlifecycle/do.json",
                contentType: "application/json; charset=utf-8",
                success: function(currentPhase){
                    console.log(currentPhase);
                    if (currentPhase != null) {
                        console.log('Process started, returned phase is: ' + currentPhase.description + ' (' + currentPhase.index + ')');
                        $("#currentPhaseName").text(currentPhase.description);
                        $("#currentPhaseID").text(currentPhase.index);
                    }

                    // Stop monitoring
                    isClientMonitoringOn = false;




                },
                error: function() {
                    console.error('Failed to start monitoring process');
                }
            });

        // TO SETUP PHASE
        } else if (internalPhase == 1) {
            $(this).text('Start Live Monitoring Phase');
            $("#currentPhaseName").text('EM Metric generator set-up phase');
            $("#currentPhaseDescription").text("Setting-up clients");
            console.log('Starting set-up phase');
            $.ajax({
                type: 'GET',
                url: "/em/gotonextphase/do.json",
                contentType: "application/json; charset=utf-8",
                success: function(currentPhase){
                    console.log(currentPhase);
                    if (currentPhase != null) {
                        console.log('Set-up phase started, returned phase is: ' + currentPhase.description + ' (' + currentPhase.index + ')');
                        $("#currentPhaseName").text(currentPhase.description);
                        $("#currentPhaseID").text(currentPhase.index);
                    }

                    // Get discovered metric generators!
                    var theClientUuid = currentClientsUuids[0];
                    console.log('Getting metric generators for client with UUID: ' + theClientUuid);
                    $.ajax({
                        type: 'POST',
                        url: "/em/getmmetricgeneratorsforclient/do.json",
                        data: JSON.stringify({clientUUID: theClientUuid}),
                        contentType: "application/json; charset=utf-8",
                        success: function(metricGenerators){
                            console.log(metricGenerators);

                            var detailsRow = $('<div class="row"></div>').appendTo(".clientdetails");
                            detailsRow.empty();


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

                                $.each(metricGenerator.listOfEntities, function(indexEntity, entity){
                                    entitiesDiv.append('<p class="noextrawhitespace entitiesitem">' + entity.name + '</p>');
                                });

                                attributesDiv.append('<p><strong>Attributes</strong></p>');
                                attributesDiv.append('<p class="noextrawhitespace entitiesitem">Packet loss</p>');
                                attributesDiv.append('<p class="noextrawhitespace active entitiesitem">Latency</p>');

                                infoDiv.append('<p><strong>Info</strong></p>');
                                infoDiv.append('<p class="noextrawhitespace">From this day forward, F</p>');
                            });

                            $('.entitiesitem').click(function(){
                                $(this).parents('.capabilitieslistbg').children('.entitiesitem').removeClass('active');
                                $(this).addClass('active');
                            });

                        },
                        error: function() {
                            console.error("Failed to get a list of metric generators");
                        }
                    });

                },
                error: function() {
                    console.error('Failed to start set-up phase');
                }
            });

        // TO LIVE MONITORING PHASE
        } else if (internalPhase == 2) {
            $(this).text('Start Post Report Phase');
            $("#currentPhaseName").text('EM Metric live monitoring phase');
            $("#currentPhaseDescription").text("Collecting data from clients");
            console.log('Starting live monitoring phase');
            $.ajax({
                type: 'GET',
                url: "/em/gotonextphase/do.json",
                contentType: "application/json; charset=utf-8",
                success: function(currentPhase){
                    console.log(currentPhase);
                    if (currentPhase != null) {
                        console.log('Live monitoring phase started, returned phase is: ' + currentPhase.description + ' (' + currentPhase.index + ')');
                        $("#currentPhaseName").text(currentPhase.description);
                        $("#currentPhaseID").text(currentPhase.index);
                    }

                },
                error: function() {
                    console.error('Failed to start live monitoring phase');
                }
            });

        // TO POST REPORT PHASE
        } else if (internalPhase == 3) {
            $(this).text('Start Tear Down Phase');
            $("#currentPhaseName").text('EM Post-monitoring reporting phase');
            $("#currentPhaseDescription").text("Receiving summary reports");
            console.log('Starting post report phase');
            $.ajax({
                type: 'GET',
                url: "/em/gotonextphase/do.json",
                contentType: "application/json; charset=utf-8",
                success: function(currentPhase){
                    console.log(currentPhase);
                    if (currentPhase != null) {
                        console.log('Post report phase started, returned phase is: ' + currentPhase.description + ' (' + currentPhase.index + ')');
                        $("#currentPhaseName").text(currentPhase.description);
                        $("#currentPhaseID").text(currentPhase.index);
                    }

                },
                error: function() {
                    console.error('Failed to start post report phase');
                }
            });

        // TO TEAR DOWN PHASE
        } else if (internalPhase == 4) {
            $(this).text('Experiment finished - restart');
            $(this).addClass('alert');
            $("#currentPhaseName").text('EM Monitoring tear-down phase');
            $("#currentPhaseDescription").text("Asking clients to preform tear-down");
            console.log('Starting tear down phase');
            $.ajax({
                type: 'GET',
                url: "/em/gotonextphase/do.json",
                contentType: "application/json; charset=utf-8",
                success: function(currentPhase){
                    console.log(currentPhase);
                    if (currentPhase != null) {
                        console.log('Tear down phase started, returned phase is: ' + currentPhase.description + ' (' + currentPhase.index + ')');
                        $("#currentPhaseName").text(currentPhase.description);
                        $("#currentPhaseID").text(currentPhase.index);
                    }

                },
                error: function() {
                    console.error('Failed to start tear down phase');
                }
            });

        // TO THE BEGINNING
        } else {
            $(this).text('Start Discovery Phase');
            $(this).removeClass('alert');
            $(".experimentInfo").css('display', 'none');
            $("#currentPhaseName").text('Undefined EM phase');
            $("#currentPhaseDescription").text("Waiting for clients to connect");
            internalPhase = -1;
        }

        internalPhase++;
        $("#currentPhaseID").text(internalPhase);

    });
    
*/    

//    $('#beginMonitoringProcess').click(function(){
//        if (currentClientsUuids.length > 0) {
//            console.log('Starting monitoring process');
//            $.ajax({
//                type: 'GET',
//                url: "/em/startlifecycle/do.json",
//                contentType: "application/json; charset=utf-8",
//                success: function(currentPhase){
//                    console.log(currentPhase);
//                    if (currentPhase != null) {
//                        console.log('Process started, returned phase is: ' + currentPhase.description + ' (' + currentPhase.index + ')');
//                        $("#currentPhaseName").text(currentPhase.description);
//                        $("#currentPhaseID").text(currentPhase.index);
//                    }
//
//                },
//                error: function() {
//                    console.error('Failed to start monitoring process');
//                }
//            });
//
//        } else {
//            console.error('Can not start monitoring process because there are no clients');
//            alert('Can not start monitoring process because there are no clients');
//        }
//    });

/*
    $(".clientitem").click(function(){
        $(".clientlist .clientitem").removeClass('active');
        $(this).addClass('active');

        var cd = $(".clientdetails");
        cd.empty();

        cd.append('<h6>Client Summary</h6>');
        cd.append('<a href="#" class="small button radius alert inpanelbutton">Disconnect Client</a>');
        cd.append('<p class="noextrawhitespace">Name: ' + $(this).text() + '</p>');
        cd.append('<p>Connected at: ' + randomDate(new Date(2012, 0, 1), new Date()) + '</p>');
        var detailsRow = $('<div class="row"></div>').appendTo(".clientdetails");

        var entitiesDivWrapper = $('<div class="four columns nopaddingright"></div>').appendTo(detailsRow);
        var entitiesDiv = $('<div class="capabilitieslistbg"></div>').appendTo(entitiesDivWrapper);
        entitiesDiv.append('<p><strong>Entities</strong></p>');
        entitiesDiv.append('<p class="noextrawhitespace active entitiesitem">Local network</p>');
        entitiesDiv.append('<p class="noextrawhitespace entitiesitem">Content database</p>');

        var attributesDivWrapper = $('<div class="four columns nopaddingright"></div>').appendTo(detailsRow);
        var attributesDiv = $('<div class="capabilitieslistbg"></div>').appendTo(attributesDivWrapper);
        attributesDiv.append('<p><strong>Attributes</strong></p>');
        attributesDiv.append('<p class="noextrawhitespace entitiesitem">Packet loss</p>');
        attributesDiv.append('<p class="noextrawhitespace active entitiesitem">Latency</p>');

        var infoDivWrapper = $('<div class="four columns"></div>').appendTo(detailsRow);
        var infoDiv = $('<div class="capabilitieslistbg"></div>').appendTo(infoDivWrapper);
        infoDiv.append('<p><strong>Info</strong></p>');
        infoDiv.append('<p class="noextrawhitespace">From this day forward, Flight Control will be known by two words: Tough means we are forever accountable for what we do or what we fail to do. We will never again compromise our responsibilities. Every time we walk into Mission Control we will know what we stand for. Competent means we will never take anything for granted. </p>');

        $('.entitiesitem').click(function(){
            $(this).parents('.capabilitieslistbg').children('.entitiesitem').removeClass('active');
            $(this).addClass('active');
        });
    });

    $(".clientlist .clientitem").first().trigger('click');
*/
});

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

function getMetricGenerators() {
        console.log(currentClientsUuids);
        $.ajax({
            type: 'POST',
            url: "/em/getmmetricgeneratorsforclient/do.json",
            data: JSON.stringify({clientUUID: currentClientsUuids[0]}),
            contentType: "application/json; charset=utf-8",
            success: function(metricGenerators){
                console.log(metricGenerators);
                
                if (metricGenerators.length < 1) {
                    console.log('No metric generators returned, waiting 2 seconds and trying again');
                    setTimeout('getMetricGenerators()', 2000);
                }

                var detailsRow = $('<div class="row"></div>').appendTo(".clientdetails");
                detailsRow.empty();


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
                prepareSetupPhase(actionButton);

            },
            error: function() {
                console.error("Failed to get a list of metric generators");
            }
        });    
}

function doDiscoveryPhase(actionButton, currentPhase) {
    console.log('In DISCOVERY PHASE');

    $("#currentPhaseName").text(currentPhase.description);
    $("#currentPhaseID").text(currentPhase.index);
    $("#currentPhaseDescription").text("Clients reporting metric generators");

    // Stop client monitoring
    isClientMonitoringOn = false;
    
    // Get metric generators (it takes clients some time to report generators)
//    var theClientUuid = currentClientsUuids[0]; // TODO: setup multiple ajax calls to all clients
//    console.log('Getting metric generators for client with UUID: ' + theClientUuid);
    
    getClients(getMetricGenerators);

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
}

// Get current phase info
function getPhase() {
    $.ajax({
        type: 'GET',
        url: "/em/getcurrentphase/do.json",
        contentType: "application/json; charset=utf-8",
        success: function(currentPhase){
            console.log(currentPhase);

            if (currentPhase != null) {
                console.log('Current phase is: ' + currentPhase.description + ' (' + currentPhase.index + ')');
                $("#currentPhaseName").text(currentPhase.description);
                $("#currentPhaseID").text(currentPhase.index);
                if (currentPhase.index == 0) {
                    $("#currentPhaseDescription").text("Waiting for clients to connect");
                } else {
                    $("#currentPhaseDescription").text("This phase needs a description");

                }

            }

        },
        error: function() {
            console.error('Failed to fetch current phase');
        }
    });
}

function getClients(callOnSuccess) {
    console.log("Getting clients, " + (callOnSuccess === null ? "on success calling myself" : "on success calling someone else"));
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
                var theClient;
                $.each(clients, function(indexClient, client){
                    if (jQuery.inArray(client.uuid, currentClientsUuids) < 0) {

                        console.log("New client: " + client.uuid);

                        currentClientsUuids.push(client.uuid);

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

                    }
                });
            }

            //wait for 3 seconds then run again if it is OK
            if (callOnSuccess === null) {
                if (isClientMonitoringOn) {
                    setTimeout('getClients(null)', 3000);
                }
            } else {
                console.log(currentClientsUuids);
                callOnSuccess();
            }
        },
        error: function() {
            // TODO
            console.error("Error getting clients list, monitoring stopped");
            isClientMonitoringOn = false;
        }
    });
}