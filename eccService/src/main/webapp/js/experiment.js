var BASE_URL = "/ECC";
$(document).ready(function() {
    $(document).foundation();

    // check if the service is initialised successfully.
    $.ajax({
        type: 'GET',
        url: BASE_URL + "/configuration/ifinitialised",
        error: function(jqXHR, textStatus, errorThrown) {
            console.log(jqXHR);
            console.log(textStatus);
            console.log(errorThrown);
            showStatus(false, "", "initialisation error (" + errorThrown + ")", "");
        },
        success: function(idata) {
            console.log(idata);
            showStatus(idata, 'initialised', 'not initialised', 'unknown initialisation status');
            if (idata === false) {
            } else if (idata === true) {
                // check if configuration was set
                $.ajax({
                    type: 'GET',
                    url: BASE_URL + "/configuration/ifconfigured",
                    error: function(jqXHR, textStatus, errorThrown) {
                        console.log(jqXHR);
                        console.log(textStatus);
                        console.log(errorThrown);
                        showStatus(false, "", "configuration error (" + errorThrown + ")", "");
                    },
                    success: function(cdata) {
                        console.log(cdata);
                        showStatus(cdata, 'configured', 'not configured', 'unknown configuration status');
                        if (cdata === false) {

                            // go back to main page to select configuration
                            // TODO: show what is wrong with the configuration
                            window.location.replace(BASE_URL + "/index.html");

                        } else if (cdata === true) {
                            // check if services started
                            $.getJSON(BASE_URL + "/configuration/ifservicesstarted", function(sdata) {
                                showStatus(sdata, 'services started', 'services failed', 'unknown status');

                                if (sdata === true) {
                                    // check experiment status
                                    $.getJSON(BASE_URL + "/experiments/ifinprogress", function(edata) {
                                        showStatus(edata, 'experiment in progress', 'no experiment', 'unknown experiment status');

                                        if (edata === false) {
                                            // create new experiment
                                            $('#nameExperimentModal').foundation('reveal', 'open');
                                        } else {
                                            // show details
                                            $.getJSON(BASE_URL + "/experiments", function(aedata) {
                                                console.log(aedata);
                                                showActiveExperimentDetails(aedata);
                                            });
                                        }
                                    });

                                }
                            });

                        } else {
                        }
                    }});

            } else {
            }
        }
    });

    // modal let's go button
    $("#setProjectNameAndDescription").click(function(e) {
        e.preventDefault();
        $('#nameExperimentModal').foundation('reveal', 'close');
    });

    // new name/description for a project
    $(document).on('close', '#nameExperimentModal', function() {
        var newProjectName = $("#newProjectName").val();
        var newProjectDescription = $("#newProjectDescription").val();
        console.log("Starting project: '" + newProjectName + "'/'" + newProjectDescription + "'");

        var newConfiguration = new Object();
        newConfiguration.name = newProjectName;
        newConfiguration.description = newProjectDescription;
        console.log(newConfiguration);

        $.ajax({
            type: 'POST',
            dataType: 'json',
            contentType: 'application/json',
            url: BASE_URL + "/experiments",
            data: JSON.stringify(newConfiguration),
            error: function(jqXHR, textStatus, errorThrown) {
                console.log(jqXHR);
                console.log(textStatus);
                console.log(errorThrown);
            },
            success: function(data) {
                console.log(data);
                if (data.hasOwnProperty('uuid')) {
                    $('#configStatus').attr('class', 'right success-color');
                    $('#configStatus').text('experiment in progress');
                    showActiveExperimentDetails(data);
                } else {
                    $('#configStatus').attr('class', 'right alert-color');
                    $('#configStatus').text('failed to create experiment');
                }
            }
        });
    });
});

// shows active experiment details
function showActiveExperimentDetails(data) {
    console.log("Disaplying the details...");
    $("#experiment_details").empty();
    $("#experiment_details").append("<p class='details'>Project: " + data.experimentID + "</p>");
    $("#experiment_details").append("<p class='details'>Experiment name: " + data.name + "</p>");
    $("#experiment_details").append("<p class='details'>Experiment description: " + data.description + "</p>");
    $("#experiment_details").append("<p class='details'>Experiment started: " + new Date(data.startTime) + "</p>");
}

// displays status in the top right corner
function showStatus(data, ifTrue, ifFalse, ifUnknown) {
    if (data === false) {
        $('#configStatus').attr('class', 'right alert-color');
        $('#configStatus').text(ifFalse);

    } else if (data === true) {
        $('#configStatus').attr('class', 'right success-color');
        $('#configStatus').text(ifTrue);

    } else {
        $('#configStatus').attr('class', 'right alert-color');
        $('#configStatus').text(ifUnknown);
    }
}