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
});