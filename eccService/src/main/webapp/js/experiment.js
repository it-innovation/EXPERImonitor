var BASE_URL = "/" + window.location.href.split('/')[3];
var clientsDropdownList, entitiesDropdownList, attrDropdownList;
var CLIENT_MODELS_AJAX = [];
var CHART_POLLING_INTERVAL = 3000; // polling delay
var CHART_SHIFT_DATA_THRESHOLD = 10; // how many points to return per graph
var intervals = new Array(); // polling intervals


$(document).ready(function() {
    $(document).foundation();
    $(document).on('open', '#nameExperimentModal', function() {
        $("#newExperimentHeader").text('Select an existing experiment or start a new one');
// check for current experiment
        $.getJSON(BASE_URL + "/experiments/ifinprogress", function(edata) {

            if (edata !== false) { // current experiment in progress, load details
                $(".currentProjectContainer").removeClass("hide");
                $("#currentProjectRadio").prop('checked', true);
                $.getJSON(BASE_URL + "/experiments/current", function(currentExperimentData) {
                    console.log(currentExperimentData);
                    fillWithExperimentMetadata($("#currentProjectDetails"), currentExperimentData);
                    $("#currentProjectDetails").data("currentExperimentData", currentExperimentData);
                });
            }

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
                        // no current experiment, existing projects
                        $(".oldProjectsContainer").removeClass("hide");

                        if ($(".currentProjectContainer").hasClass("hide")) {
                            $("#oldProjectRadio").prop('checked', true);
                        }
                        var experimentsDropdownList = $("<select></select>").appendTo("#oldProjects");
                        $.each(data, function(key, experiment) {
                            var formattedDate;
                            if (experiment.status === 'started') {
                                formattedDate = moment(new Date(experiment.startTime)).format("ddd, MMM Do, HH:mm");
                            } else {
                                formattedDate = moment(new Date(experiment.endTime)).format("ddd, MMM Do, HH:mm");
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
                        if ($(".currentProjectContainer").hasClass("hide")) {
                            // no existing experiments, no current experiments
                            $("#newExperimentHeader").text('Start new experiment');
                            $("#newProjectRadio").prop('checked', true);

                        } else {
                            // no existing experiments, but current experiment
                            $("#newExperimentHeader").text('Start new experiment or connect to current');
                            $("#currentProjectRadio").prop('checked', true);
                        }

                    }
                }
            });
        });
    });
    // modal let's go button
    $("#setProjectNameAndDescription").click(function(e) {
        e.preventDefault();
        $('#nameExperimentModal').foundation('reveal', 'close');
    });
    // stop current experiment
    $("#stop_experiment").click(function(e) {
        e.preventDefault();
        // stop experiment, update metadata
        $.getJSON(BASE_URL + "/experiments/current/stop", function(data) {
            if (data === true) {
                $('#configStatus').attr('class', 'right success-color');
                $('#configStatus').text('experiment stopped');
            } else {
                $('#configStatus').attr('class', 'right alert-color');
                $('#configStatus').text('failed to stop current experiment');
            }
        });

    });

    // reload everything button
    $("#reloadClientsEntitiesAttributes").click(function(e) {
        e.preventDefault();

        // stop all current polling
        for (var i = 0; i < intervals.length; i++) {
            clearInterval(intervals[i]);
        }

        // clear graphs
        $("#live_metrics").empty();

        // clean reload of clients, entities, attributes
        $.getJSON(BASE_URL + "/experiments/clients", function(data) {
            console.log(data);
            showListOfClients(data);
        });
    });

    // new name/description for an experiment
    $(document).on('close', '#nameExperimentModal', function() {

        if ($('#oldProjectRadio').is(':checked')) {
            // show data page for an old experiment
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
        } else if ($('#newProjectRadio').is(':checked')) {
// force start new experiment
            var newProjectName = $("#newProjectName").val();
            var newProjectDescription = $("#newProjectDescription").val();
            console.log("Starting new experiment: '" + newProjectName + "' (" + newProjectDescription + ")");
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
        } else {
// reconnect to current
            console.log("Loading current experiment with uuid " + $("#currentProjectDetails").data("currentExperimentData").uuid);
            startMainMonitor($("#currentProjectDetails").data("currentExperimentData"));
        }

    });
//    $('#nameExperimentModal').foundation('reveal', 'open');
//    return;

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
                                    $('#nameExperimentModal').foundation('reveal', 'open');
                                }
                            });
                        } else {
                        }
                    }});
            } else {
            }
        }
    });
});
// puts experiment metadata into a container
function fillWithExperimentMetadata(container, experiment) {
    container.empty();
    var startTime = experiment.startTime === null ? 'n/a' : moment(new Date(experiment.startTime)).format("ddd, MMM Do, HH:mm");
    var endTime = experiment.endTime === null ? 'n/a' : moment(new Date(experiment.endTime)).format("ddd, MMM Do, HH:mm");
    container.append("<p class='sub_details_mid'>Name: " + experiment.name + "</p>");
    container.append("<p class='sub_details_mid'>Description: " + experiment.description + "</p>");
    container.append("<p class='sub_details_mid'>Status: " + experiment.status + "</p>");
    container.append("<p class='sub_details_mid'>UUID: " + experiment.uuid + "</p>");
    container.append("<p class='sub_details_mid'>Start - end: " + startTime + " - " + endTime + "</p>");
}

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
    console.log(experimentMetadata);
    $("#experiment_details").empty();
    $("#experiment_details").append("<p class='details'>Project: " + experimentMetadata.projectName + "</p>");
    $("#experiment_details").append("<p class='details'>Name: " + experimentMetadata.name + "</p>");
    $("#experiment_details").append("<p class='details'>Description: " + experimentMetadata.description + "</p>");
    $("#experiment_details").append("<p class='details'>Started: " + new Date(experimentMetadata.startTime) + "</p>");
    $("#download_experiment_data").attr('href', BASE_URL + "/data/export/experiment/" + experimentMetadata.uuid);
}

// show list of clients
function showListOfClients(clientMetadataArray) {
    $("#clients_details").empty();
    $("#entities_details").empty();
    $("#attribute_details").empty();
    $("#clients_details").append("<h4>Clients</h4>");
    $("#entities_details").append("<h4>Entities</h4>");
    $("#attribute_details").append("<h4>Attributes</h4>");

    var clientsDropdownLabel = $("<label>Filter by client connection status or show all</label>").appendTo("#clients_details");
    clientsDropdownList = $("<select></select>").appendTo(clientsDropdownLabel);
    clientsDropdownList.append("<option value='all'>All</option>");
    clientsDropdownList.append("<option value='connected'>Connected</option>");
    clientsDropdownList.append("<option value='disconnected'>Disconnected</option>");
    clientsDropdownList.change(function(e) {
        var sel = $("#clients_details option:selected").val();
        if (sel === 'all') {
            $("#clients_details div.clientContainer").each(function(key) {
                $(this).removeClass('hide');
            });
        } else {
            console.log(sel);
            $("#clients_details div.clientContainer").each(function(key) {
                if ($(this).data('status') === sel) {
                    $(this).removeClass('hide');
                } else {
                    $(this).addClass('hide');
                }
            });
        }
    });
    var entitiesDropdownLabel = $("<label>Filter by client or show all</label>").appendTo("#entities_details");
    entitiesDropdownList = $("<select></select>").appendTo(entitiesDropdownLabel);
    entitiesDropdownList.append("<option value='all'>All</option>");
    entitiesDropdownList.change(function(e) {
        var sel = $("#entities_details option:selected").val();
        if (sel === 'all') {
            $("#entities_details div.entityContainer").each(function(key) {
                $(this).removeClass('hide');
            });
        } else {
            console.log(sel);
            $("#entities_details div.entityContainer").each(function(key) {
                if ($(this).data('clientId') === sel) {
                    $(this).removeClass('hide');
                } else {
                    $(this).addClass('hide');
                }
            });
        }
    });
    var attrDropdownLabel = $("<label>Filter by entity or show all</label>").appendTo("#attribute_details");
    attrDropdownList = $("<select></select>").appendTo(attrDropdownLabel);
    attrDropdownList.append("<option value='all'>All</option>");
    attrDropdownList.change(function(e) {
        var sel = $("#attribute_details option:selected").val();
        if (sel === 'all') {
            $("#attribute_details div.attributeContainer").each(function(key) {
                $(this).removeClass('hide');
            });
        } else {
            console.log(sel);
            $("#attribute_details div.attributeContainer").each(function(key) {
                if ($(this).data('entityId') === sel) {
                    $(this).removeClass('hide');
                } else {
                    $(this).addClass('hide');
                }
            });
        }
    });
    $.each(clientMetadataArray, function(key, client) {
        var clientContainerWrapper = $("<div class='clientContainer row fullWidth collapse'></div>").appendTo("#clients_details");
        var clientContainer = $("<div class='small-12 columns'></div>").appendTo(clientContainerWrapper);
        clientContainer.append("<p class='details'><strong>" + client.name + "</strong></p>");
        clientContainer.append("<p class='sub_details_mid'>Connected: " + client.connected + "</p>");
        clientContainer.append("<p class='sub_details'>UUID: " + client.uuid + "</p>");
        var clientStatus = client.connected === true ? 'connected' : 'disconnected';
        clientContainerWrapper.data("status", clientStatus);
        var actionsParagraph = $("<p class='sub_details'></p>").appendTo(clientContainer);
        var clientAddToLiveMetricsLink = $("<a class='clientCheckbox' id='c_" + client.uuid + "_input' href='#'>Add to Live metrics</a>").appendTo(actionsParagraph);
        clientAddToLiveMetricsLink.data("client", client);
        actionsParagraph.append("<a class='downloadLink' href='" + BASE_URL + "/data/export/client/" + client.uuid + "'>Download CSV data</a>");
        entitiesDropdownList.append("<option value='" + client.uuid + "'>" + client.name + " (" + clientStatus + ")</option>");
        CLIENT_MODELS_AJAX.push(appendEntitiesFromClient(client.uuid, client, attrDropdownList));
        clientAddToLiveMetricsLink.click(function(e) {
            e.preventDefault();
            var client = $(this).data("client");
            if (!$(this).hasClass('actionSelected')) {
                $(this).text("Remove from Live Metrics");
                $(this).addClass('actionSelected');
            } else {
                $(this).text("Add to Live Metrics");
                $(this).removeClass('actionSelected');
            }
            $("a.entityCheckbox").each(function() {
                if ($(this).data("clientId") === client.uuid) {
                    $(this).trigger("click");
                }
            });
        });
    });
// trigger first entries when all metrics fetched
    $.when.apply($, CLIENT_MODELS_AJAX).done(function() {
        clientsDropdownList.prop('selectedIndex', 1);
        clientsDropdownList.change();
        // TODO: sort entities list alphabetically
        entitiesDropdownList.prop('selectedIndex', 1);
        entitiesDropdownList.change();
        attrDropdownList.prop('selectedIndex', 1);
        attrDropdownList.change();
    });
}

// append entities from client
function appendEntitiesFromClient(uuid, client, attrDropdownList) {
    return $.getJSON(BASE_URL + "/experiments/entities/" + uuid, function(data) {
        console.log(data);
        $.each(data, function(ekey, entity) {
            var entityContainerWrapper = $("<div class='entityContainer row fullWidth collapse'></div>").appendTo("#entities_details");
            var entityContainer = $("<div class='small-12 columns'></div>").appendTo(entityContainerWrapper);
            entityContainer.append("<p class='details'><strong>" + entity.name + "</strong></p>");
            entityContainer.append("<p class='sub_details_mid'>Client: " + client.name + "</p>");
            entityContainer.append("<p class='sub_details_mid'>Description: " + entity.description + "</p>");
            entityContainer.append("<p class='sub_details'>UUID: " + entity.uuid + "</p>");
            var actionsParagraph = $("<p class='sub_details'></p>").appendTo(entityContainer);
            var entityAddToLiveMetricsLink = $("<a class='entityCheckbox' id='e_" + entity.uuid + "_input' href='#'>Add to Live metrics</a>").appendTo(actionsParagraph);
            entityAddToLiveMetricsLink.data("entity", entity);
            entityAddToLiveMetricsLink.data("clientId", uuid);
            actionsParagraph.append("<a class='downloadLink' href='" + BASE_URL + "/data/export/entity/" + entity.uuid + "'>Download CSV data</a>");
            entityContainerWrapper.data("clientId", uuid);
            entityAddToLiveMetricsLink.click(function(e) {
                e.preventDefault();
                var entity = $(this).data("entity");
                if (!$(this).hasClass('actionSelected')) {
                    $(this).text("Remove from Live Metrics");
                    $(this).addClass('actionSelected');
                } else {
                    $(this).text("Add to Live Metrics");
                    $(this).removeClass('actionSelected');
                }
                $.each(entity.attributes, function(akey, attribute) {
                    $("#a_" + attribute.uuid + "_input").trigger("click");
                });
            });
            attrDropdownList.append("<option value='" + entity.uuid + "'>" + entity.name + " (" + client.name + " client)" + "</option>");
            $.each(entity.attributes, function(akey, attribute) {
                var attributeContainerWrapper = $("<div class='attributeContainer row fullWidth collapse'></div>").appendTo("#attribute_details");
                var attributeContainer = $("<div class='small-12 columns'></div>").appendTo(attributeContainerWrapper);
                attributeContainer.append("<p class='details'><strong>" + attribute.name + "</strong></p>");
                attributeContainer.append("<p class='sub_details_mid'>Client: " + client.name + "</p>");
                attributeContainer.append("<p class='sub_details_mid'>Entity: " + entity.name + "</p>");
                attributeContainer.append("<p class='sub_details_mid'>Description: " + attribute.description + "</p>");
                attributeContainer.append("<p class='sub_details_mid'>UUID: " + attribute.uuid + "</p>");
                attributeContainer.append("<p class='sub_details_mid'>Type: " + attribute.type + "</p>");
                attributeContainer.append("<p class='sub_details_mid'>Unit: " + attribute.unit + "</p>");
                var actionsParagraph = $("<p class='sub_details'></p>").appendTo(attributeContainer);
                var attributeAddToLiveMetricsLink = $("<a id='a_" + attribute.uuid + "_input' href='#'>Add to Live metrics</a>").appendTo(actionsParagraph);
                attributeAddToLiveMetricsLink.data("attribute", attribute);
                attributeAddToLiveMetricsLink.data("entityName", entity.name);
                actionsParagraph.append("<a class='downloadLink' href='" + BASE_URL + "/data/export/attribute/" + attribute.uuid + "'>Download CSV data</a>");
                attributeContainerWrapper.data("entityId", entity.uuid);
                attributeAddToLiveMetricsLink.click(function(e) {
                    e.preventDefault();
                    var attribute = $(this).data("attribute");
                    var entityName = $(this).data("entityName");
                    if (!$(this).hasClass('actionSelected')) {
                        $(this).text("Remove from Live Metrics");
                        $(this).addClass('actionSelected');
                        addAttributeGraph(attribute, entityName);
                    } else {
                        $(this).text("Add to Live Metrics");
                        $(this).removeClass('actionSelected');
                        hideAttributeGraph(attribute);
                    }
                });
            });
        });
    });
}

// adds attribute's graph to display
function addAttributeGraph(attribute, entityName) {
    console.log("Adding graph for attribute " + attribute.uuid);
    // remove end class
    $("#live_metrics .attributeGraphDiv").each(function() {
        $(this).removeClass('end');
    });
    var attributeGraphContainer = $("<div class='small-4 columns end attributeGraphDiv' id='a_" + attribute.uuid + "_graph'></div>").appendTo("#live_metrics");
    var graphContainer = $("<div id='agraph_" + attribute.uuid + "'></div>").appendTo(attributeGraphContainer);
    var removeButton = $("<a href='#' class='removeSelfGraphButton' id='removeButton_" + attribute.uuid + "'>Remove</a>").appendTo($("<div class='removeSelfGraphButtonWrapper text-center'></div>").appendTo(attributeGraphContainer));
    removeButton.data("attributeiuud", attribute.uuid);
    removeButton.click(function(e) {
        e.preventDefault();
        $("#a_" + $(this).data("attributeiuud") + "_input").trigger("click");
    });
    var chart;
    if (attribute.type === 'NOMINAL') {
        console.log("Adding pie chart");
        chart = createEmptyPieChart(graphContainer, attribute, entityName);
    } else {
        console.log("Adding line chart");
        chart = createEmptyLineChart(graphContainer, attribute, entityName);
    }

    var lastTimestamp, shift;
    $.getJSON(BASE_URL + "/data/attribute/" + attribute.uuid, function(data) {
        console.log(data);
        if (data.data.length > 0) {
            lastTimestamp = data.data[data.data.length - 1].timestamp;
            var pieData = {}, pieArray;
            $.each(data.data, function(key, datapoint) {
                console.log(datapoint);
                if (attribute.type === 'NOMINAL') {
                    if (pieData.hasOwnProperty(datapoint.value)) {
                        console.log("Old property: " + datapoint.value);
                        pieData[datapoint.value] = pieData[datapoint.value] + 1;
                    } else {
                        console.log("New property: " + datapoint.value);
                        pieData[datapoint.value] = 1;
                    }
                    pieArray = new Array();
                    $.each(pieData, function(key, value) {
                        pieArray.push({name: key, y: value});
                    });
                    chart.series[0].setData(pieArray);
                } else {
                    shift = chart.series[0].data.length > CHART_SHIFT_DATA_THRESHOLD;
                    chart.series[0].addPoint([datapoint.timestamp, parseFloat(datapoint.value)], true, shift);
                }
            });
        } else {
            lastTimestamp = 0;
        }

        // set polling
        var series = chart.series[0];
        var theInterval = setInterval(function() {
            // TODO: stop polling on error
            $.getJSON(BASE_URL + "/data/attribute/" + attribute.uuid + "/since/" + lastTimestamp, function(dataSince) {
                console.log(dataSince);
                if (dataSince.data.length > 0) {
                    lastTimestamp = dataSince.data[dataSince.data.length - 1].timestamp;
                    console.log("Updated timestamp: " + lastTimestamp);
                    var pieArray, shift;
                    $.each(dataSince.data, function(key, datapoint) {
                        console.log(datapoint);
                        if (attribute.type === 'NOMINAL') {
                            if (pieData.hasOwnProperty(datapoint.value)) {
                                pieData[datapoint.value] = pieData[datapoint.value] + 1;
                            } else {
                                pieData[datapoint.value] = 1;
                            }
                            pieArray = new Array();
                            $.each(pieData, function(key, value) {
                                pieArray.push({name: key, y: value});
                            });
                            series.setData(pieArray);
                        } else {
                            shift = chart.series[0].data.length > CHART_SHIFT_DATA_THRESHOLD;
                            series.addPoint([datapoint.timestamp, parseFloat(datapoint.value)], true, shift);
                        }
                    });
                }
            });
        }, CHART_POLLING_INTERVAL);
        $("#a_" + attribute.uuid + "_input").data("interval", theInterval);
        intervals.push(theInterval);
    });
}

// removes interval from intervals - more reliable, removes duplicates
function removeInterval(interval) {
    for (var i = 0; i < intervals.length; i++) {
        if (intervals[i] === interval)
            intervals.splice(i, 1);
    }

}

// adds empty pie chart
function createEmptyPieChart(container, attribute, entityName) {
    return new Highcharts.Chart({
        chart: {
            renderTo: container.attr('id'),
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: false
        },
        title: {
            text: entityName + ': ' + attribute.name
        },
        tooltip: {
            pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b> ({point.y})'
        },
        credits: {
            enabled: false
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: true,
                    format: '<b>{point.name}</b>: {point.percentage:.1f} %',
                    style: {
                        color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                    }
                }
            }
        },
        series: [{
                type: 'pie',
                name: 'NOMINAL data',
                data: []
            }]
    });
}

// adds empty line chart
function createEmptyLineChart(container, attribute, entityName) {
    return new Highcharts.Chart({
        chart: {
            renderTo: container.attr('id'),
//            type: 'spline',
            animation: Highcharts.svg
        },
        title: {
            text: entityName + ': ' + attribute.name
        },
        xAxis: {
            type: 'datetime',
            title: {
                text: 'Time'
            }
        },
        yAxis: {
            title: {
                text: attribute.unit
            }
        },
        legend: {
            enabled: false
        },
        credits: {
            enabled: false
        },
        exporting: {
            enabled: false
        },
        series: [{
                name: 'Measurement data',
                data: []
            }]
    });
}

// hides attribute's graph to display
function hideAttributeGraph(attribute) {

    $("#a_" + attribute.uuid + "_graph").remove();
    var theInterval = $("#a_" + attribute.uuid + "_input").data("interval");
    console.log("Removing graph for attribute " + attribute.uuid + ", interval " + theInterval);
    clearInterval(theInterval);
    removeInterval(theInterval);
    // set last container class to end
    $("#live_metrics .attributeGraphDiv").each(function() {
        $(this).removeClass('end');
    });
    $("#live_metrics .attributeGraphDiv:last-child").addClass('end');
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