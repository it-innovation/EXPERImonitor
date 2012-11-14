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
var theExperiment;
var internalPhase = -1;
var DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";

//$(document).ready(function() {
//        $(document).foundationButtons();
//        
//
//});

// Converts date as long to string format
function longToDate(longTime) {
    return (new Date(longTime)).toString(DATE_FORMAT);
}

// Polls something to death
function poll(){
    if (counter < 10) {
        var dataContainer = $('#appendhere');
        $.ajax({
            url: 'sample/getdata/do.json',
            dataType: 'json',	
            type: 'GET',
            contentType: "application/json; charset=utf-8",
            error: function(){
                dataContainer.append("<p>Something went wrong, try again?</p>");
            },
            success: function(data) {
                counter++;
                $("#reportsCounter").text(counter);
                console.log(data);
                if (data !== null) {
                    dataContainer.append('<p class="datarow">measurement: <strong>' + data.value + '</strong>, timestamp: <strong>' + data.timeStamp + '<strong></p>');
                } else {
                    dataContainer.append('<p class="datarow dataerror">Server error, check logs.</p>');
                }
            },
            complete: poll,
            timeout: 30000
        });    
    }
    

}

// Creates random dates within start - end period
function randomDate(start, end) {
    return new Date(start.getTime() + Math.random() * (end.getTime() - start.getTime()))
}

function executePhase(currentPhase) {
    internalPhase = currentPhase.index;
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

        // END
        case 6:
            doTearDownPhase(actionButton, currentPhase)
            break;

        // UNKNOWN PHASE
        default:
            console.log('ERROR: UNKNOWN PHASE');
            actionButton.text('ERROR');
            actionButton.click(function(e){
                e.preventDefault();
            })
            console.error('Current phase is ' + currentPhase + ', not sure what to do with it, will stop now.');
            $("#currentPhaseName").text("ERROR - current phase is unknown (" + currentPhase + ")");
            $("#currentPhaseID").text(currentPhase);
            break;
    }    
}

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