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
                                                startMainMonitor(aedata);
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
                    startMainMonitor(data);
                } else {
                    $('#configStatus').attr('class', 'right alert-color');
                    $('#configStatus').text('failed to create experiment');
                }
            }
        });
    });
});

// handles the display of all data
function startMainMonitor(experimentData) {
    // refresh experiment details
    showActiveExperimentDetails(experimentData);

    // show list of clients
    $.getJSON(BASE_URL + "/experiments/clients", function(data) {
        console.log(data);
        showListOfClients(data);
    });
}

// shows active experiment details
function showActiveExperimentDetails(experimentMetadata) {
    $("#experiment_details").empty();
    $("#experiment_details").append("<p class='details'>Project: " + experimentMetadata.experimentID + "</p>");
    $("#experiment_details").append("<p class='details'>Experiment name: " + experimentMetadata.name + "</p>");
    $("#experiment_details").append("<p class='details'>Experiment description: " + experimentMetadata.description + "</p>");
    $("#experiment_details").append("<p class='details'>Experiment started: " + new Date(experimentMetadata.startTime) + "</p>");
}

// show list of clients
function showListOfClients(clientMetadataArray) {
    $("#clients_details").empty();
    $("#entities_details").empty();
    $("#attribute_details").empty();
    $.each(clientMetadataArray, function(key, client) {
        $("#clients_details").append("<p class='details'><strong>" + client.name + "</strong></p>");
        $("#clients_details").append("<p class='sub_details'>" + client.uuid + "</p>");
        appendEntitiesFromClient(client.uuid);
    });
}

// append entities from client
function appendEntitiesFromClient(uuid) {
    $.getJSON(BASE_URL + "/experiments/entities/" + uuid, function(data) {
        console.log(data);
        $.each(data, function(ekey, entity) {
            $("#entities_details").append("<p class='details'><strong>" + entity.name + "</strong></p>");
            $("#entities_details").append("<p class='sub_details_mid'>" + entity.description + "</p>");
            $("#entities_details").append("<p class='sub_details'>" + entity.uuid + "</p>");
            $.each(entity.attributes, function(akey, attribute) {
                $("#attribute_details").append("<p class='details'><strong>" + attribute.name + "</strong></p>");
                $("#attribute_details").append("<p class='sub_details_mid'>" + attribute.description + "</p>");
                $("#attribute_details").append("<p class='sub_details'>" + attribute.uuid + "</p>");
            });
        });
    });
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