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
//    $("#nagiosconnector").click(function(e){
//        e.preventDefault();
//        $("#nagioscontainer").empty();
//        
//        if ($(this).hasClass('alert')) {
//            $(this).text('Connect to Nagios');
//            $(this).removeClass('alert');
//            
//        } else {
//            $(this).text('Disconnect');
//            $(this).addClass('alert');
//            $("#nagioscontainer").append('<iframe src="http://buronga/nagios3/" frameborder="0" name="mainframe" vspace="0" hspace="0" marginwidth="0" marginheight="0" width="100%" height="800px" scrolling="no" noresize id="mainframe"></iframe>');
//        }
//        
//    });

    actionButton = $("#actionButton");
    actionButton.click(function(e){        
        e.preventDefault();
    });

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

                // Get Nagios details
                $.ajax({
                    type: 'GET',
                    url: "/em/geteccproperties/do.json",
                    contentType: "application/json; charset=utf-8",
                    success: function(properties){
                        console.log(properties);

                        if (properties == null) {
                            alert("Server error retrieving nagios details");
                            console.error("Server error retrieving nagios details");
                            return;
                        } else {
                            if (properties.length < 1) {
                                alert("No nagios details found");
                                console.error("No nagios details found");                        
                            } else {
                                // Display info
                                $("#nagiosinfo").append("<p>Nagios URL: " + properties.nagios_fullurl + "</p>");

                                // Load iframe
                                $("#nagioscontainer").append('<iframe src="' + properties.nagios_fullurl + '" frameborder="0" name="mainframe" vspace="0" hspace="0" marginwidth="0" marginheight="0" width="100%" height="800px" scrolling="no" noresize id="mainframe"></iframe>');
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