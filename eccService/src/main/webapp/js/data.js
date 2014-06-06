var BASE_URL = "/" + window.location.href.split('/')[3];
var DISPLAY_TIME_FORMAT = "ddd, MMM Do, HH:mm [(]Z[)]";

$(document).ready(function() {
    $(document).foundation();

    var experimentId = getParameter('experimentId');

    $("#change_experiment").click(function(e) {
        e.preventDefault();
        $('#nameExperimentModal').foundation('reveal', 'open');
    });

    $("#to_monitor").attr('href', BASE_URL + "/experiment.html");

    // check if the service is initialised successfully.
    $.ajax({
        type: 'GET',
        url: BASE_URL + "/configuration/ifinitialised",
        error: function(jqXHR, textStatus, errorThrown) {
            console.log(jqXHR);
            console.log(textStatus);
            console.log(errorThrown);
            $('#configStatus').attr('class', 'right alert-color');
            $('#configStatus').text("initialisation error (" + errorThrown + ")");
        },
        success: function(idata) {
            console.log(idata);
            if (idata === false) {
                $('#configStatus').attr('class', 'right alert-color');
                $('#configStatus').text('not initialised');
            } else if (idata === true) {
                $('#configStatus').attr('class', 'right success-color');
                $('#configStatus').text('initialised');

                // check if configuration was set
                $.ajax({
                    type: 'GET',
                    url: BASE_URL + "/configuration/ifconfigured",
                    error: function(jqXHR, textStatus, errorThrown) {
                        console.log(jqXHR);
                        console.log(textStatus);
                        console.log(errorThrown);
                        $('#configStatus').attr('class', 'right alert-color');
                        $('#configStatus').text("configuration error (" + errorThrown + ")");
                    },
                    success: function(cdata) {
                        if (cdata === true) {
                            $('#configStatus').attr('class', 'right success-color');
                            $('#configStatus').text('configured');

                            // fetch configuration
                            $.getJSON(BASE_URL + "/configuration", function(data) {
                                console.log(data);
                                showConfigurationDetails(data);
                            });

                            // fetch requested experiment info
                            $.getJSON(BASE_URL + "/experiments/id/" + experimentId, function(currentExperimentData) {
                                console.log(currentExperimentData);
                                showActiveExperimentDetails(currentExperimentData);
                            });

                            // fetch data
                            $.getJSON(BASE_URL + "/data/entities/" + experimentId, function(data) {
//                                console.log(data);
                                $("#entities_details").empty();
                                $("#entities_details").append("<h4>Entities</h4>");
                                $("#attribute_details").empty();
                                $("#attribute_details").append("<h4>Attributes</h4>");
                                $.each(data, function(ekey, entity) {
                                    var entityContainerWrapper = $("<div class='entityContainer row fullWidth collapse'></div>").appendTo("#entities_details");

                                    var entityContainer = $("<div class='small-12 columns'></div>").appendTo(entityContainerWrapper);
                                    entityContainer.append("<p class='details'><strong>" + entity.name + "</strong></p>");
                                    entityContainer.append("<p class='sub_details_mid'>Description: " + entity.description + "</p>");
                                    entityContainer.append("<p class='sub_details'>UUID: " + entity.uuid + "</p>");

                                    var actionsParagraph = $("<p class='sub_details'></p>").appendTo(entityContainer);
                                    actionsParagraph.append("<a class='downloadLink' href='" + BASE_URL + "/data/export/experiment/" + experimentId + "/entity/" + entity.uuid + "'>Download CSV data</a>");


//                                    attrDropdownList.append("<option value='" + entity.uuid + "'>" + entity.name + " (" + client.name + " client)" + "</option>");
                                    $.each(entity.attributes, function(akey, attribute) {
                                        var attributeContainerWrapper = $("<div class='attributeContainer row fullWidth collapse'></div>").appendTo("#attribute_details");
                                        var attributeContainer = $("<div class='small-12 columns'></div>").appendTo(attributeContainerWrapper);
                                        attributeContainer.append("<p class='details'><strong>" + attribute.name + "</strong></p>");
                                        attributeContainer.append("<p class='sub_details_mid'>Entity: " + entity.name + "</p>");
                                        attributeContainer.append("<p class='sub_details_mid'>Description: " + attribute.description + "</p>");
                                        attributeContainer.append("<p class='sub_details_mid'>UUID: " + attribute.uuid + "</p>");
                                        attributeContainer.append("<p class='sub_details_mid'>Type: " + attribute.type + "</p>");
                                        attributeContainer.append("<p class='sub_details_mid'>Unit: " + attribute.unit + "</p>");
                                        var actionsParagraph = $("<p class='sub_details'></p>").appendTo(attributeContainer);
                                        actionsParagraph.append("<a class='downloadLink' href='" + BASE_URL + "/data/export/experiment/" + experimentId + "/attribute/" + attribute.uuid + "'>Download CSV data</a>");

                                    });
                                });
                            });


                        } else {
                            $('#configStatus').attr('class', 'right alert-color');
                            $('#configStatus').text('unknown configured status');
                        }
                    }});

            } else {
                $('#configStatus').attr('class', 'right alert-color');
                $('#configStatus').text('unknown initialisation status');
            }
        }
    });


    $(document).on('open', '#nameExperimentModal', function() {
        $.ajax({
            type: 'GET',
            dataType: 'json',
            contentType: 'application/json',
            url: BASE_URL + "/experiments/latest",
            error: function(jqXHR, textStatus, errorThrown) {
                console.log(jqXHR);
                console.log(textStatus);
                console.log(errorThrown);
            },
            success: function(data) {
                console.log(data);
                if (data.length > 0) {

                    var experimentsDropdownList = $("<select></select>").appendTo("#oldProjects");

                    $.each(data, function(key, experiment) {
                        var formattedDate;
                        if (experiment.status === 'started') {
                            formattedDate = moment(new Date(experiment.startTime)).format(DISPLAY_TIME_FORMAT);
                        } else {
                            formattedDate = moment(new Date(experiment.endTime)).format(DISPLAY_TIME_FORMAT);
                        }

                        var experimentEntry = $("<option value='" + experiment.uuid + "'>" + experiment.name + " (" + experiment.status + " at " + formattedDate + ")</option>").appendTo(experimentsDropdownList);
                        experimentEntry.data("experiment", experiment);

                        if (key === 0) {
                            fillWithExperimentMetadata($("#oldProjectDetails"), experiment);
                        }
                    });
                    experimentsDropdownList.change(function(e) {
                        var sel = $("#oldProjects option:selected"); //.val();
                        var experiment = sel.data("experiment");
                        fillWithExperimentMetadata($("#oldProjectDetails"), experiment);
                    });
                } else {
                    $("#newProjectRadio").prop('checked', true);
                }
            }
        });
    });

    // modal let's go button
    $("#setProjectNameAndDescription").click(function(e) {
        e.preventDefault();
        $('#nameExperimentModal').foundation('reveal', 'close');
    });

    $(document).on('close', '#nameExperimentModal', function() {
        var oldProjectUuid = $("#oldProjects option:selected").val();
        console.log("Loading existing experiment with UUID: '" + oldProjectUuid + "'");

        $.ajax({
            type: 'GET',
            dataType: 'json',
            contentType: 'application/json',
            url: BASE_URL + "/experiments/id/" + oldProjectUuid,
            error: function(jqXHR, textStatus, errorThrown) {
                console.log(jqXHR);
                console.log(textStatus);
                console.log(errorThrown);
            },
            success: function(data) {
                console.log(data);
                if (data.hasOwnProperty('uuid')) {
                    window.location.replace(BASE_URL + "/data.html?experimentId=" + data.uuid);
                } else {
                    $('#configStatus').attr('class', 'right alert-color');
                    $('#configStatus').text('failed to load experiment [' + oldProjectUuid + ']');
                }
            }
        });
    });
});

// shows active experiment details
function showConfigurationDetails(configurationMetadata) {
    console.log(configurationMetadata);
    var d = configurationMetadata.databaseConfig;
    var r = configurationMetadata.rabbitConfig;
    $("#configuration_details").empty();
    $("#configuration_details").append("<p class='details'>Project: " + configurationMetadata.projectName + "</p>");
    $("#configuration_details").append("<p class='details'>Database: " + d.url + " / " + d.databaseName + "</p>");
    $("#configuration_details").append("<p class='details'>Rabbit: " + r.ip + ":" + r.port + " / " + r.monitorId + "</p>");
}

// shows active experiment details
function showActiveExperimentDetails(experimentMetadata) {
    console.log(experimentMetadata);
    $("#experiment_details").empty();
    $("#experiment_details").append("<p class='details'>Project: " + experimentMetadata.projectName + "</p>");
    $("#experiment_details").append("<p class='details'>Name: " + experimentMetadata.name + "</p>");
    $("#experiment_details").append("<p class='details'>Description: " + experimentMetadata.description + "</p>");
    $("#experiment_details").append("<p class='details'>Started: " + moment(new Date(experimentMetadata.startTime)).format(DISPLAY_TIME_FORMAT) + "</p>");
    $("#download_experiment_data").attr('href', BASE_URL + "/data/export/experiment/" + experimentMetadata.uuid);
}

// puts experiment metadata into a container
function fillWithExperimentMetadata(container, experiment) {
    container.empty();
    var startTime = experiment.startTime === null ? 'n/a' : moment(new Date(experiment.startTime)).format(DISPLAY_TIME_FORMAT);
    var endTime = experiment.endTime === null ? 'n/a' : moment(new Date(experiment.endTime)).format(DISPLAY_TIME_FORMAT);
    container.append("<p class='sub_details_mid'>Name: " + experiment.name + "</p>");
    container.append("<p class='sub_details_mid'>Description: " + experiment.description + "</p>");
    container.append("<p class='sub_details_mid'>Status: " + experiment.status + "</p>");
    container.append("<p class='sub_details_mid'>UUID: " + experiment.uuid + "</p>");
    container.append("<p class='sub_details_mid'>Start - end: " + startTime + " - " + endTime + "</p>");
}

function getParameter(name) {
    var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href);
    if (results === null) {
        return null;
    }
    else {
        return results[1] || 0;
    }
}