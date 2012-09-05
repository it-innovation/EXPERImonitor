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

$(document).ready(function() {
    
        // Get the Experiment
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
        
        // Get clients
        getClients();
        setInterval("getClients()", 3000);

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

function getClients() {
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
                            
                            var cd = $(".clientdetails");
                            cd.empty();
                            cd.append('<p class="clientHeader">' + clientData.name + '</p>');
                            cd.append('<p class="clientDescription">UUID: ' + clientData.uuid + '</p>');
                            
                        });
                        
                        // If no clients selected, select the first one
                        if ($(".clientlist .active").size() < 1)
                            $(".clientlist .clientitem").first().trigger('click');
                            
                    }
                });
            }
        }
    });    
}