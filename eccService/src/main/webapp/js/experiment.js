var BASE_URL = "/" + window.location.href.split('/')[3];
var clientsDropdownList, entitiesDropdownList, attrDropdownList;
var CLIENT_MODELS_AJAX = [];


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
        var clientContainer = $("<div class='clientContainer'></div>").appendTo("#clients_details");
        clientContainer.append("<p class='details'><strong>" + client.name + "</strong></p>");
        clientContainer.append("<p class='sub_details_mid'>Connected: " + client.connected + "</p>");
        clientContainer.append("<p class='sub_details'>UUID: " + client.uuid + "</p>");
        var clientStatus = client.connected === true ? 'connected' : 'disconnected';
        clientContainer.data("status", clientStatus);
        entitiesDropdownList.append("<option value='" + client.uuid + "'>" + client.name + " (" + clientStatus + ")</option>");
        CLIENT_MODELS_AJAX.push(appendEntitiesFromClient(client.uuid, client, attrDropdownList));
    });

    // trigger first entries when all metrics fetched
    $.when.apply($, CLIENT_MODELS_AJAX).done(function() {
        clientsDropdownList.prop('selectedIndex', 1);
        clientsDropdownList.change();
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
            var entityContainer = $("<div class='entityContainer'></div>").appendTo("#entities_details");
            entityContainer.append("<p class='details'><strong>" + entity.name + "</strong></p>");
            entityContainer.append("<p class='sub_details_mid'>Client: " + client.name + "</p>");
            entityContainer.append("<p class='sub_details_mid'>Desc: " + entity.description + "</p>");
            entityContainer.append("<p class='sub_details'>UUID: " + entity.uuid + "</p>");
            entityContainer.data("clientId", uuid);

            attrDropdownList.append("<option value='" + entity.uuid + "'>" + entity.name + " (" + client.name + " client)" + "</option>");
            $.each(entity.attributes, function(akey, attribute) {
                var attributeContainerWrapper = $("<div class='attributeContainer row fullWidth collapse'></div>").appendTo("#attribute_details");
                var attributeCheckboxContainer = $("<div class='small-1 columns text-right'></div>").appendTo(attributeContainerWrapper);
                var attributeCheckbox = $("<input type='checkbox' name='attributeCheckboxes' id='a_" + attribute.uuid + "_input'/>").appendTo(attributeCheckboxContainer);
                attributeCheckbox.data("attribute", attribute);
                var attributeContainer = $("<div class='small-11 columns'></div>").appendTo(attributeContainerWrapper);
                attributeContainer.append("<p class='details'><strong>" + attribute.name + "</strong></p>");
                attributeContainer.append("<p class='sub_details_mid'>Client: " + client.name + "</p>");
                attributeContainer.append("<p class='sub_details_mid'>Entity: " + entity.name + "</p>");
                attributeContainer.append("<p class='sub_details_mid'>Desc: " + attribute.description + "</p>");
                attributeContainer.append("<p class='sub_details'>UUID: " + attribute.uuid + "</p>");
                attributeContainerWrapper.data("entityId", entity.uuid);

                attributeCheckbox.change(function() {
                    var attribute = $(this).data("attribute");
                    if (this.checked) {
                        addAttributeGraph(attribute);
                    } else {
                        hideAttributeGraph(attribute);
                    }
                });

            });
        });
    });
}

// adds attribute's graph to display
function addAttributeGraph(attribute) {
    console.log("Displaying graph for attribute " + attribute.uuid);

    // remove end class
    $("#live_metrics .attributeGraphDiv").each(function() {
        $(this).removeClass('end');
    });

    var attributeGraphContainer = $("<div class='small-4 columns end attributeGraphDiv' id='a_" + attribute.uuid + "_graph'></div>").appendTo("#live_metrics");
    attributeGraphContainer.append("<p><strong>" + attribute.name + "</strong></p>");

    $.getJSON(BASE_URL + "/data/attribute/" + attribute.uuid, function(data) {
        console.log(data);
    });
    /*
     var graphContainer = $("<div id='agraph_" + attribute.uuid + "'></div>").appendTo(attributeGraphContainer);

     graphContainer.highcharts({
     chart: {
     type: 'spline',
     animation: Highcharts.svg, // don't animate in old IE
     marginRight: 10,
     events: {
     load: function() {

     // set up the updating of the chart each second
     var series = this.series[0];
     setInterval(function() {
     var x = (new Date()).getTime(), // current time
     y = Math.random();
     series.addPoint([x, y], true, true);
     }, 1000);
     }
     }
     },
     title: {
     text: 'Live random data'
     },
     xAxis: {
     type: 'datetime',
     tickPixelInterval: 150
     },
     yAxis: {
     title: {
     text: 'Value'
     },
     plotLines: [{
     value: 0,
     width: 1,
     color: '#808080'
     }]
     },
     tooltip: {
     formatter: function() {
     return '<b>' + this.series.name + '</b><br/>' +
     Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) + '<br/>' +
     Highcharts.numberFormat(this.y, 2);
     }
     },
     legend: {
     enabled: false
     },
     exporting: {
     enabled: false
     },
     series: [{
     name: 'Random data',
     data: (function() {
     // generate an array of random data
     var data = [],
     time = (new Date()).getTime(),
     i;

     for (i = -19; i <= 0; i++) {
     data.push({
     x: time + i * 1000,
     y: Math.random()
     });
     }
     return data;
     })()
     }]
     }); */
}

// hides attribute's graph to display
function hideAttributeGraph(attribute) {
    console.log("Removing graph for attribute " + attribute.uuid);
    $("#a_" + attribute.uuid + "_graph").remove();
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