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
var DATE_FORMAT = "d/M/yyyy H:m";

$(document).ready(function() {
        $(document).foundationButtons();
        

});

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
