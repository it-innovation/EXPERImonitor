var BASE_URL = "/" + window.location.href.split('/')[3];
var DISPLAY_TIME_FORMAT = "ddd, MMM Do, HH:mm [(]Z[)]";
var DISPLAY_TIME_FORMAT_SECONDS = "ddd, MMM Do, HH:mm:ss [(]Z[)]";
var CHART_HEIGHT = 500;
var experimentId;
var widgetsCounter = 0;
var graphsCounter = 0;
var tablesCounter = 0;
var participants = [];
var services = [];
var groupAttributes = [];

$(document).ready(function () {
    $(document).foundation();

    // D3 setup
    var customColors = ["#d90000", "#ff9326", "#f5dd01", "#01ff51", "#00b200"];
    d3.scale.customColors = function () {
        return d3.scale.ordinal().range(customColors);
    };

    // disable js cache
    $.ajaxSetup({cache: false});

    experimentId = getParameter('experimentId');

    $("#metricDataLink").attr('href', BASE_URL + '/dataview.html?experimentId=' + experimentId);

    $("#change_experiment").click(function (e) {
        e.preventDefault();
        $('#nameExperimentModal').foundation('reveal', 'open');
    });

    $("#to_monitor").attr('href', BASE_URL + "/experiment.html?experimentId=" + experimentId);

    // Remove all widgets link
    $("#removeAllWidgets").click(function (e) {
        e.preventDefault();
        $('.widgetContainer').remove();
    });

    // check if the service is initialised successfully.
    $.ajax({
        type: 'GET',
        url: BASE_URL + "/configuration/ifinitialised",
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(jqXHR);
            console.log(textStatus);
            console.log(errorThrown);
            $('#configStatus').attr('class', 'right alert-color');
            $('#configStatus').text("initialisation error (" + errorThrown + ")");
        },
        success: function (idata) {
//            console.log(idata);
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
                    error: function (jqXHR, textStatus, errorThrown) {
                        console.log(jqXHR);
                        console.log(textStatus);
                        console.log(errorThrown);
                        $('#configStatus').attr('class', 'right alert-color');
                        $('#configStatus').text("configuration error (" + errorThrown + ")");
                    },
                    success: function (cdata) {
                        if (cdata === true) {
                            $('#configStatus').attr('class', 'right success-color');
                            $('#configStatus').text('configured');

                            // fetch configuration
                            $.getJSON(BASE_URL + "/configuration", function (data) {
//                                console.log(data);
                                showConfigurationDetails(data);
                            });

                            // fetch requested experiment info
                            $.getJSON(BASE_URL + "/experiments/id/" + experimentId, function (currentExperimentData) {
//                                console.log(currentExperimentData);
                                showActiveExperimentDetails(currentExperimentData);
                            });

                            // fetch prov data
                            $.getJSON(BASE_URL + "/explorer/" + experimentId + "/summary", function (provData) {
                                console.log(provData);
                                showProvDetails(provData);
                            });

                            // create participants + attributes widget
                            $.getJSON(BASE_URL + "/explorer/" + experimentId + "/participants", function (participantsResponse) {
//                                console.log(participantsResponse);
//                                addParticipantAttributesWidget(participantsResponse.participants);
                                participants = participantsResponse.participants;
                                $.getJSON(BASE_URL + "/explorer/" + experimentId + "/participants/groupAttributes", function (attributes) {
                                    $.getJSON(BASE_URL + "/explorer/" + experimentId + "/services", function (servicesResponse) {
                                        console.log(servicesResponse);
                                        services = servicesResponse.services;
                                        groupAttributes = attributes;
                                        addParticipantQoeAttributesWidget("");
//                                        addQosServicesExplorerWidget("", "", "");
                                        $("#addNewQoe").click(function (e) {
                                            e.preventDefault();
                                            addParticipantQoeAttributesWidget("");
                                        });
                                        $("#addNewParticipant").click(function (e) {
                                            e.preventDefault();
                                            addParticipantExplorerWidget("");
                                        });
                                        $("#addNewQoS").click(function (e) {
                                            e.preventDefault();
                                            addQosServicesExplorerWidget("", "", "");
                                        });
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


    $(document).on('open', '#nameExperimentModal', function () {
        $.ajax({
            type: 'GET',
            dataType: 'json',
            contentType: 'application/json',
            url: BASE_URL + "/experiments/latest",
            error: function (jqXHR, textStatus, errorThrown) {
                console.log(jqXHR);
                console.log(textStatus);
                console.log(errorThrown);
            },
            success: function (data) {
                console.log(data);
                if (data.length > 0) {

                    var experimentsDropdownList = $("<select></select>").appendTo("#oldProjects");

                    $.each(data, function (key, experiment) {
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
                    experimentsDropdownList.change(function (e) {
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
    $("#setProjectNameAndDescription").click(function (e) {
        e.preventDefault();
        $('#nameExperimentModal').foundation('reveal', 'close');
    });

    $(document).on('close', '#nameExperimentModal', function () {
        var oldProjectUuid = $("#oldProjects option:selected").val();
        console.log("Loading existing experiment with UUID: '" + oldProjectUuid + "'");

        $.ajax({
            type: 'GET',
            dataType: 'json',
            contentType: 'application/json',
            url: BASE_URL + "/experiments/id/" + oldProjectUuid,
            error: function (jqXHR, textStatus, errorThrown) {
                console.log(jqXHR);
                console.log(textStatus);
                console.log(errorThrown);
            },
            success: function (data) {
                console.log(data);
                if (data.hasOwnProperty('uuid')) {
                    window.location.replace(BASE_URL + "/provview.html?experimentId=" + data.uuid);
                } else {
                    $('#configStatus').attr('class', 'right alert-color');
                    $('#configStatus').text('failed to load experiment [' + oldProjectUuid + ']');
                }
            }
        });
    });
});

/*
 * FUNCTIONS
 */

function addQosServicesExplorerWidget(service, participantName, activity) {
    widgetsCounter++;
    var widgetContainerId = "wc" + widgetsCounter;
    var widgetContainer = $('<div id ="' + widgetContainerId + '" class="widgetContainer small-12 columns"></div>').appendTo("#prov_widgets");
//    widgetContainer.append('<hr>');
    widgetContainer.append('<h5>' + 'Service QoS explorer' + '</h5>');
    var removeWidget = $('<a href="#" class="removeWidget">remove widget</a>').appendTo(widgetContainer);
    removeWidget.click(function (e) {
        e.preventDefault();
        $("#" + widgetContainerId).remove();
    });

    var widgetSelectorsContainer = $('<div class="row"></div>').appendTo(widgetContainer);
    var widgetSelectorsContainerLeft = $('<div class="small-6 columns"></div>').appendTo(widgetSelectorsContainer);

    var widgetSelectorsContainerRight = $('<div class="small-6 columns"></div>').appendTo(widgetSelectorsContainer);
    var widgetGraphsContainer = $('<div class="row"></div>').appendTo(widgetContainer);
    widgetGraphsContainer.css('height', 1.2 * CHART_HEIGHT);
    var widgetGraphsSelectedDetailsContainer = $('<div class="row"></div>').appendTo(widgetContainer);
    var widgetGraphsSelectedDetailsContainerMain = $('<div class="small-12 columns"></div>').appendTo(widgetGraphsSelectedDetailsContainer);

    // participants list
    var participantsDropdownLabel = $("<label>Filter by participant</label>").appendTo(widgetSelectorsContainerRight);
    var participantsDropdownList = $('<select id="part' + widgetsCounter + '"></select>').appendTo(participantsDropdownLabel);
    participantsDropdownList.append("<option value='__any'>Any</option>");
    var widgetSelectorsContainerRightForm = $('<form></form>').appendTo(widgetSelectorsContainerRight);
//    var theParticipant;
    $.each(participants, function (participantCounter, participant) {
        var participantsDropdownListOption = $("<option value='" + participant.name + "'>" + participant.name + "</option>").appendTo(participantsDropdownList);
        participantsDropdownListOption.data("participant", participant);
//        if (participant.name === participantName) {
//            theParticipant = participant;
//        }
    });
    participantsDropdownList.change(function (e) {
        runQosWidgetSelection(widgetsCounter, widgetGraphsContainer, widgetSelectorsContainerLeftForm, widgetSelectorsContainerRightForm, widgetGraphsSelectedDetailsContainerMain);
    });

    // services list
    var servicesDropdownLabel = $("<label>Filter by service</label>").appendTo(widgetSelectorsContainerLeft);
    var servicesDropdownList = $('<select id="serv' + widgetsCounter + '"></select>').appendTo(servicesDropdownLabel);
    servicesDropdownList.append("<option value='__any'>Any</option>");
    var widgetSelectorsContainerLeftForm = $('<form></form>').appendTo(widgetSelectorsContainerLeft);
    $.each(services, function (sCounter, tempService) {
        var serviceOption = $("<option value='" + tempService.name + "'>" + tempService.name + "</option>").appendTo(servicesDropdownList);
        serviceOption.data('service', tempService);
    });
    servicesDropdownList.change(function (e) {
        runQosWidgetSelection(widgetsCounter, widgetGraphsContainer, widgetSelectorsContainerLeftForm, widgetSelectorsContainerRightForm, widgetGraphsSelectedDetailsContainerMain);
    });

    if (service === "") {
        servicesDropdownList.val('__any');
    } else {
        servicesDropdownList.val(service.name);
    }

    if (participantName === "") {
        participantsDropdownList.val('__any');
    } else {
        participantsDropdownList.val(participantName);
//        participantsDropdownList.change();
    }
    servicesDropdownList.change();

}

/**
 * Finds participant object by name.
 *
 * @param {type} participantName
 * @returns {participant}
 */
function getParticipantByName(participantName) {
    var result;
    $.each(participants, function (participantCounter, participant) {
        if (participant.name === participantName) {
            result = participant;
        }
    });

    return result;
}

/**
 * Renders QoS for a service metric with one user's attribute on top
 *
 * @param {type} addToContainer
 * @param {type} attributeId
 * @param {type} participantIri
 * @param {type} activityLabel
 * @returns {undefined}
 */
function renderQosGraphWithUser(addToContainer, attributeId, participantIri, activityLabel) {
    console.log('Plotting ' + attributeId + ' and activity ' + activityLabel);
    graphsCounter++;
    var newChartId = 'wg' + graphsCounter;
    d3.json(BASE_URL + "/explorer/" + experimentId + "/attributes/series/qos/highlight/activities?attrID=" + attributeId +
            "&IRI=" + encodeURIComponent(participantIri) +
            "&actLabel=" + encodeURIComponent(activityLabel), function (data) {
        console.log(data);
        addToContainer.empty();
        addToContainer.append('<div id="' + newChartId + '" class="widgetGraph"><svg class="large-12 text-center columns"></svg></div>');
        $('#' + newChartId + ' svg').show().height(CHART_HEIGHT);
        nv.addGraph(function () {
            var chart = nv.models.lineChart()
                    .x(function (d) {
                        return d.timestamp;
                    })
                    .y(function (d) {
                        return d.value / 10;
                    })      // TODO -- fix scalling problems, dividing by 10 is a hack to get round scalling issues
                    .margin({top: 30, right: 50, bottom: 20, left: 100})
                    .useInteractiveGuideline(true)
                    .forceY([0])
                    .color(d3.scale.category10().range())
                    .isArea(true);
            chart.xAxis
                    .axisLabel('Time')
                    .showMaxMin(true)
                    .tickFormat(function (d) {
                        return d3.time.format('%X')(new Date(d));
                    });
            chart.yAxis
                    .axisLabel(units(data.seriesSet[0].key))
                    .tickFormat(d3.format(',.2f'));
            d3.select('#' + newChartId + ' svg')
                    .datum(data.seriesSet)
                    .transition().duration(500)
                    .call(chart);

            nv.utils.windowResize(chart.update);

            return chart;
        });
        // TODO -- get service metric units from service once implemented
        function units(key) {
            if (key === 'Average response time') {
                return 'Response Time (s)';
            } else if (key === 'CPU Usage') {
                return 'CPU Usage (%)';
            } else if (key === 'Memory Usage') {
                return 'Memory Usage (%)';
            }
        }
    });
}

/**
 * Renders QoS for a service metric of all users on top.
 *
 * @param {type} addToContainer
 * @param {type} attributeId
 * @returns {undefined}
 */
function renderQosGraph(addToContainer, attributeId) {
    console.log('Plotting ' + attributeId);
    graphsCounter++;
    var newChartId = 'wg' + graphsCounter;
    d3.json(BASE_URL + "/explorer/" + experimentId + "/attributes/series/qos/highlight/participants?attrID=" + attributeId, function (data) {
        console.log(data);
        addToContainer.empty();
        addToContainer.append('<div id="' + newChartId + '" class="widgetGraph"><svg class="large-12 text-center columns"></svg></div>');
        $('#' + newChartId + ' svg').show().height(CHART_HEIGHT);
        nv.addGraph(function () {
            var chart = nv.models.lineChart()
                    .x(function (d) {
                        return d.timestamp;
                    })
                    .y(function (d) {
                        return d.value / 10;
                    })      // TODO -- fix scalling problems, dividing by 10 is a hack to get round scalling issues
                    .margin({top: 30, right: 50, bottom: 20, left: 100})
                    .useInteractiveGuideline(true)
                    .forceY([0])
                    .color(d3.scale.category10().range())
                    .isArea(true);
            chart.xAxis
                    .axisLabel('Time')
                    .showMaxMin(true)
                    .tickFormat(function (d) {
                        return d3.time.format('%X')(new Date(d));
                    });
            chart.yAxis
                    .axisLabel(units(data.seriesSet[0].key))
                    .tickFormat(d3.format(',.2f'));
            d3.select('#' + newChartId + ' svg')
                    .datum(data.seriesSet)
                    .transition().duration(500)
                    .call(chart);

            nv.utils.windowResize(chart.update);

            return chart;
        });
        // TODO -- get service metric units from service once implemented
        function units(key) {
            if (key === 'Average response time') {
                return 'Response Time (s)';
            } else if (key === 'CPU Usage') {
                return 'CPU Usage (%)';
            } else if (key === 'Memory Usage') {
                return 'Memory Usage (%)';
            }
        }
    });
}

function addParticipantQoeAttributesWidget(selectedParticipant) {
    console.log(selectedParticipant);
    widgetsCounter++;
    var widgetContainerId = "wc" + widgetsCounter;
    var widgetContainer = $('<div id ="' + widgetContainerId + '" class="widgetContainer small-12 columns"></div>').appendTo("#prov_widgets");
    var headerTitle = $('<h5></h5>').appendTo(widgetContainer);
    var removeWidget = $('<a href="#" class="removeWidget">remove widget</a>').appendTo(widgetContainer);
    removeWidget.click(function (e) {
        e.preventDefault();
        $("#" + widgetContainerId).remove();
    });

    var widgetSelectorsContainer = $('<div class="row"></div>').appendTo(widgetContainer);
    var widgetSelectorsContainerLeft = $('<div class="small-6 columns"></div>').appendTo(widgetSelectorsContainer);
    var widgetSelectorsContainerRight = $('<div class="small-6 columns"></div>').appendTo(widgetSelectorsContainer);
    var widgetGraphsContainer = $('<div class="row"></div>').appendTo(widgetContainer);
    widgetGraphsContainer.css('height', CHART_HEIGHT);
    var widgetGraphsSelectedDetailsContainer = $('<div class="row"></div>').appendTo(widgetContainer);
    var widgetGraphsSelectedDetailsContainerMain = $('<div class="small-12 columns"></div>').appendTo(widgetGraphsSelectedDetailsContainer);

    // participants list
    var participantsDropdownLabel = $("<label>Filter by participant</label>").appendTo(widgetSelectorsContainerLeft);
    var participantsDropdownList = $('<select id="part' + widgetsCounter + '"></select>').appendTo(participantsDropdownLabel);
    var attributesDropdownLabel = $("<label>Filter by attribute</label>").appendTo(widgetSelectorsContainerRight);
    var attributesDropdownList = $('<select id="attr' + widgetsCounter + '"></select>').appendTo(attributesDropdownLabel);

    // participants list fill
    participantsDropdownList.append("<option value='__any'>Any</option>");
    $.each(participants, function (participantCounter, participant) {
        var participantOption = $('<option id="part' + widgetsCounter + "-" + participantCounter + '" value="p' + participant.metricEntityID + '">' + participant.name + '</option>').appendTo(participantsDropdownList);
        participantOption.data("participant", participant);
    });
    participantsDropdownList.change(function (e) {
        runQoeWidgetSelection(widgetsCounter, widgetGraphsContainer, widgetGraphsSelectedDetailsContainerMain, headerTitle);
    });

    // attributes list fill
    attributesDropdownList.append("<option value='__any'>Any</option>");
    $.each(groupAttributes.qoEAttributes, function (participantCounter, attribute) {
        attributesDropdownList.append("<option value='" + attribute.name + "'>" + attribute.name + "</option>");
    });
    attributesDropdownList.change(function (e) {
        runQoeWidgetSelection(widgetsCounter, widgetGraphsContainer, widgetGraphsSelectedDetailsContainerMain, headerTitle);
    });

    // add strat graph by default
//    attributesDropdownList.change();
    if (selectedParticipant === "") {
        participantsDropdownList.val('__any');
    } else {
        participantsDropdownList.val('p' + selectedParticipant.metricEntityID);
    }
    participantsDropdownList.change();

}

function runQoeWidgetSelection(widgetsCounter, widgetGraphsContainer, widgetGraphsSelectedDetailsContainerMain, headerTitle) {
    var participantSelection = $("#part" + widgetsCounter).find('option:selected');
    console.log(participantSelection.data("participant"));
    var participantSelectionVal = participantSelection.val();
    var attributeSelection = $("#attr" + widgetsCounter).find('option:selected');
    var attributeSelectionVal = attributeSelection.val();

    if (participantSelectionVal === '__any') {
        if (attributeSelectionVal === '__any') {
            console.log('Plotting all paritipants, all attributes');
            headerTitle.text('Participant QoE');
            renderStratParticipansGraphToContainer(widgetGraphsContainer, widgetGraphsSelectedDetailsContainerMain);
        } else {
            console.log('Plotting all participants, attribute: ' + attributeSelectionVal);
            renderAttributeGraphsToContainer(attributeSelectionVal, widgetGraphsContainer, widgetGraphsSelectedDetailsContainerMain);
            headerTitle.text('Participant QoE: ' + attributeSelectionVal);
        }
    } else {
        if (attributeSelectionVal === '__any') {
            console.log('Plotting participant ' + participantSelectionVal + ' (' + participantSelection.data("participant").name + '), all attributes');
            headerTitle.text('Participant: ' + participantSelection.data("participant").name + ', QoE');
            widgetGraphsContainer.empty();
            widgetGraphsContainer.append('<div class="small-12 columns text-center"><h3>Not implemented</h3></div>');
//            renderParticipantQoeGraphsToContainer(participantSelection.data("participant").iri, widgetGraphsContainer, widgetGraphsSelectedDetailsContainerMain);
        } else {
            console.log('Plotting participant ' + participantSelectionVal + ' (' + participantSelection.data("participant").name + '), attribute: ' + attributeSelectionVal);
            headerTitle.text('Participant: ' + participantSelection.data("participant").name + ', QoE: ' + attributeSelectionVal);
            widgetGraphsContainer.empty();
            widgetGraphsContainer.append('<div class="small-12 columns text-center"><h3>Not implemented</h3></div>');
        }
    }
}

/**
 * Deals with service/user combinations in QoS widget.
 *
 * @param {type} widgetsCounter
 * @param {type} widgetGraphsContainer
 * @param {type} widgetSelectorsContainerLeftForm
 * @param {type} widgetSelectorsContainerRightForm
 * @returns {undefined}
 */
function runQosWidgetSelection(widgetsCounter, widgetGraphsContainer, widgetSelectorsContainerLeftForm, widgetSelectorsContainerRightForm, widgetGraphsSelectedDetailsContainerMain) {
    var serviceSelection = $("#serv" + widgetsCounter).find('option:selected');
    var serviceSelectionVal = serviceSelection.val();
    var participantSelection = $("#part" + widgetsCounter).find('option:selected');
    var participantSelectionVal = participantSelection.val();

    if (participantSelectionVal === '__any') {
        if (serviceSelectionVal === '__any') {
            console.log('Plotting all participants, all services');
            widgetGraphsContainer.empty();
            widgetSelectorsContainerLeftForm.empty();
            widgetSelectorsContainerRightForm.empty();
            widgetGraphsSelectedDetailsContainerMain.empty();
            widgetGraphsContainer.append('<div class="small-12 columns text-center"><h3>Please select a service first</h3></div>');
        } else {
            console.log('Plotting all participants, service: ' + serviceSelectionVal);
            widgetGraphsContainer.empty();
            widgetSelectorsContainerLeftForm.empty();
            widgetSelectorsContainerRightForm.empty();
            widgetGraphsSelectedDetailsContainerMain.empty();
            widgetGraphsContainer.append('<div class="small-12 columns text-center"><h3>Loading...</h3></div>');

            $.getJSON(BASE_URL + "/explorer/" + experimentId + "/services/iri/attributes?IRI=" + encodeURIComponent(serviceSelection.data('service').iri), function (metrics) {
                console.log(metrics);
                $.each(metrics.attributes, function (mC, m) {
                    var radio = $('<input type="radio" name="metric" value="' + m.metricID + '" id="ri' + m.metricID + '">').appendTo(widgetSelectorsContainerLeftForm);
                    widgetSelectorsContainerLeftForm.append('<label for="ri' + m.metricID + '">' + m.name + '</label><br>');
                    radio.click(function (e) {
                        renderQosGraph(widgetGraphsContainer, m.metricID);
                    });
                    if (mC === 0) {
                        radio.attr('checked', 'checked');
                        radio.click();
                    }
                });
            });
        }
    } else {
        if (serviceSelectionVal === '__any') {
            console.log('Plotting participant ' + participantSelectionVal + ' (' + participantSelection.data("participant").name + '), all services');
            widgetGraphsContainer.empty();
            widgetSelectorsContainerLeftForm.empty();
            widgetSelectorsContainerRightForm.empty();
            widgetGraphsSelectedDetailsContainerMain.empty();
            widgetGraphsContainer.append('<div class="small-12 columns text-center"><h3>Please select a service first</h3></div>');
        } else {
            console.log('Plotting participant ' + participantSelectionVal + ' (' + participantSelection.data("participant").name + '), service: ' + serviceSelectionVal);
            widgetGraphsContainer.empty();
            widgetSelectorsContainerLeftForm.empty();
            widgetSelectorsContainerRightForm.empty();
            widgetGraphsSelectedDetailsContainerMain.empty();
            widgetGraphsContainer.append('<div class="small-12 columns text-center"><h3>Loading...</h3></div>');

            // get services first
            $.getJSON(BASE_URL + "/explorer/" + experimentId + "/services/iri/attributes?IRI=" + encodeURIComponent(serviceSelection.data('service').iri), function (metrics) {

                $.get(BASE_URL + "/explorer/" + experimentId + "/participants/iri/activities/summary?IRI=" + encodeURIComponent(getParticipantByName(participantSelectionVal).iri), function (data) {
                    var rbCounter = 0;

                    $.each(metrics.attributes, function (mC, m) {
                        var radio = $('<input type="radio" name="metric" value="' + m.metricID + '" id="ri' + m.metricID + '">').appendTo(widgetSelectorsContainerLeftForm);
                        widgetSelectorsContainerLeftForm.append('<label for="ri' + m.metricID + '">' + m.name + '</label><br>');
                        radio.click(function (e) {
                            var selectedAttribute = widgetSelectorsContainerRightForm.find('input:checked').val();
                            if (typeof selectedAttribute !== "undefined") {
                                widgetGraphsContainer.empty();
                                widgetGraphsContainer.append('<div class="small-12 columns text-center"><h3>Loading...</h3></div>');
                                renderQosGraphWithUser(widgetGraphsContainer, m.metricID, getParticipantByName(participantSelectionVal).iri, selectedAttribute);
                            }
                        });
                        if (mC === 0) {
                            radio.attr('checked', 'checked');
                            radio.click();
                        }
                    });

                    $.each(data.activities, function (aC, a) {
                        var rbId = "rb" + widgetsCounter + "_" + rbCounter;
                        var radio = $('<input type="radio" name="activity" value="' + a.label + '" id="' + rbId + '">').appendTo(widgetSelectorsContainerRightForm);
                        widgetSelectorsContainerRightForm.append('<label for="' + rbId + '">' + a.label + '</label><br>');
                        radio.click(function (e) {
                            var selectedMetric = widgetSelectorsContainerLeftForm.find('input:checked').val();
                            if (typeof selectedMetric !== "undefined") {
                                widgetGraphsContainer.empty();
                                widgetGraphsSelectedDetailsContainerMain.empty();
                                widgetGraphsContainer.append('<div class="small-12 columns text-center"><h3>Loading...</h3></div>');
                                renderQosGraphWithUser(widgetGraphsContainer, selectedMetric, getParticipantByName(participantSelectionVal).iri, a.label);
                                var linksContainer = $('<p></p>').appendTo(widgetGraphsSelectedDetailsContainerMain);
                                var createQoeWidgetLink = $('<a href="#">View QoE for participant ' + participantSelectionVal + '</a>').appendTo(linksContainer);
                                createQoeWidgetLink.click(function (e) {
                                    e.preventDefault();
                                    addParticipantQoeAttributesWidget(participantSelection.data("participant"));
                                });
                                var createActivitiesWidgetLink = $('<a href="#" class="tableft">View activities for participant ' + participantSelectionVal + '</a>').appendTo(linksContainer);
                                createActivitiesWidgetLink.click(function (e) {
                                    e.preventDefault();
                                    addParticipantExplorerWidget(participantSelection.data("participant"));
                                });
                            }
                        });
                        rbCounter++;
                        if (aC === 0) {
                            radio.attr('checked', 'checked');
                            radio.click();
                        }
                    });
                });

            });
        }
    }
}

function addParticipantExplorerWidget(selectedParticipant) {
    console.log(selectedParticipant);
    widgetsCounter++;
    var widgetContainerId = "wc" + widgetsCounter;
    var widgetContainer = $('<div id ="' + widgetContainerId + '" class="widgetContainer small-12 columns"></div>').appendTo("#prov_widgets");
//    widgetContainer.append('<hr>');
    var removeWidget = $('<a href="#" class="removeWidget">remove widget</a>').appendTo(widgetContainer);
    removeWidget.click(function (e) {
        e.preventDefault();
        $("#" + widgetContainerId).remove();
    });
    var thisHeader = $('<h4></h4>').appendTo(widgetContainer);

    var widgetSelectorsContainer = $('<div class="row"></div>').appendTo(widgetContainer);
    var widgetSelectorsContainerLeft = $('<div class="small-6 columns"></div>').appendTo(widgetSelectorsContainer);
    var widgetSelectorsContainerRight = $('<div class="small-6 columns"></div>').appendTo(widgetSelectorsContainer);
//    var widgetTableContainer = $('<div class="small-12 columns"></div>').appendTo($('<div class="row"></div>').appendTo(widgetContainer));
    var widgetTableContainer = $('<div class="small-12 columns"></div>').appendTo(widgetSelectorsContainer);

    // participants list
    var participantsDropdownLabel = $("<label>Filter by participant</label>").appendTo(widgetSelectorsContainerLeft);
    var participantsDropdownList = $("<select></select>").appendTo(participantsDropdownLabel);
    participantsDropdownList.append("<option value='__any'>Any</option>");
    $.each(participants, function (participantCounter, participant) {
        var option = $("<option value='" + participant.name + "'>" + participant.name + "</option>").appendTo(participantsDropdownList);
        option.data('participant', participant);
    });
    participantsDropdownList.change(function (e) {
        var selOption = $(this).find('option:selected');
        var sel = selOption.val();
        widgetTableContainer.empty();
        if (sel === '__any') {
            thisHeader.html('Exploring all participants\' activities');
        } else {
            thisHeader.html('Exploring participant ' + sel + "'s activities");
            tablesCounter++;
            var tableId = "aTable" + tablesCounter;
            var theTable = $('<table id="' + tableId + '"></table>').appendTo(widgetTableContainer);
            theTable.append('<thead><tr><th>Name</th><th>Applications</th><th>Services</th><th>Started</th><th>Finished</th><th>Duration, sec</th></tr></thead>');
            var theTableBody = $('<tbody id="' + "aTableBody" + tablesCounter + '"></tbody>').appendTo(theTable);
            $.getJSON(BASE_URL + "/explorer/" + experimentId + "/participants/iri/activities?IRI=" + encodeURIComponent(selOption.data('participant').iri), function (as) {
                console.log(as);
//                widgetTableContainer.append("<p><strong>" + sel + " has " + as.activityTotal + " activities:" + "</strong></p>");
                $.each(as.activities, function (aC, a) {
                    var theRow = $('<tr></tr>').appendTo(theTableBody);
                    var nameCell = $('<td></td>').appendTo(theRow);
                    var applicationsCell = $('<td></td>').appendTo(theRow);
                    var servicesCell = $('<td></td>').appendTo(theRow);
                    var startedCell = $('<td></td>').appendTo(theRow);
                    var finishedCell = $('<td></td>').appendTo(theRow);
                    var durationCell = $('<td></td>').appendTo(theRow);

                    nameCell.html(a.name);
                    startedCell.html(moment(new Date(a.startTime)).format(DISPLAY_TIME_FORMAT_SECONDS));
                    finishedCell.html(moment(new Date(a.endTime)).format(DISPLAY_TIME_FORMAT_SECONDS));
                    durationCell.html(Math.floor(moment.duration(a.endTime - a.startTime, 'milliseconds').asSeconds()));
//                    widgetTableContainer.append("<p>" + a.name + " (" +  + " sec)</p>");

                    // get applications for activity
                    $.getJSON(BASE_URL + "/explorer/" + experimentId + "/activities/iri/applications?IRI=" + encodeURIComponent(a.iri), function (appsForActivity) {
                        console.log(appsForActivity);

                        // get services for activity
                        $.each(appsForActivity.applications, function (appC, app) {
                            var aText = applicationsCell.html() + app.name;
                            applicationsCell.html(aText);

                            $.getJSON(BASE_URL + "/explorer/" + experimentId + "/activities/iri/services?IRI=" + encodeURIComponent(a.iri), function (servicesForApp) {
                                console.log(servicesForApp);
                                $.each(servicesForApp.services, function (serviceC, service) {
                                    var serviceLink = $('<a href="#">' + service.name + '</a>');
                                    servicesCell.append(serviceLink);
                                    if (serviceC < servicesForApp.services.length - 1) {
                                        servicesCell.append(", ");
                                    }
                                    serviceLink.click(function (e) {
                                        e.preventDefault();
                                        console.log('Adding service "' + service.name + '", user "' + sel + '", activity "' + a.name + '"');
                                        addQosServicesExplorerWidget(service, sel, a);
                                    });

                                });
                                theTable.tablesorter();

                            });
                        });
                    });

                });
            });
        }
    });
    if (selectedParticipant === "") {
        participantsDropdownList.val('__any');
    } else {
        participantsDropdownList.val(selectedParticipant.name);
    }
    participantsDropdownList.change();

}

function chartClickAttr(e, detailsContainer, selectedAttributeName) {
    console.log("QoE of '" + e.point.label.trim() + "' for attribute '" + selectedAttributeName + "', #participants: " + e.point.count);
    detailsContainer.empty();
    detailsContainer.append('<h6>' + e.point.count + ' participant' + (e.point.count === 1 ? "" : "s") + ' selected option "' + e.point.label.trim() + '" for attribute "' + selectedAttributeName + '":</h6>');
    $.getJSON(BASE_URL + "/explorer/" + experimentId + "/participants/attributes/select?attrName=" + encodeURIComponent(selectedAttributeName) + "&nomOrdLabel=" + encodeURIComponent(e.point.label), function (selectedParticipants) {
        console.log(selectedParticipants);
        var pList = $('<ul class="circle"></ul>').appendTo(detailsContainer);
        $.each(selectedParticipants.participants, function (pC, p) {
            var participantWidgetLink = $('<a href="#">' + p.name + ' (' + p.description + ')</a>').appendTo($('<li></li>').appendTo(pList));
            participantWidgetLink.click(function (e) {
                e.preventDefault();
                addParticipantExplorerWidget(p);
            });
        });

        // group separately
//        if (selectedParticipants.participants.length > 1) {
//            var participantWidgetLink = $('<a href="#">View as a group</a>').appendTo($('<li></li>').appendTo(pList));
//            participantWidgetLink.click(function(e) {
//                e.preventDefault();
//            });
//        }
    });
}

function chartClickStrat(e, detailsContainer) {
    console.log("QoE of '" + e.point.labelValue + "' for attribute '" + e.point.label + "', #participants: " + e.point.size);
    detailsContainer.empty();
    detailsContainer.append('<h6>' + e.point.size + ' participant' + (e.point.size === 1 ? "" : "s") + ' selected option "' + e.point.labelValue + '" for attribute "' + e.point.label + '":</h6>');
    $.getJSON(BASE_URL + "/explorer/" + experimentId + "/participants/attributes/select?attrName=" + encodeURIComponent(e.point.label) + "&nomOrdLabel=" + encodeURIComponent(e.point.labelValue), function (selectedParticipants) {
        console.log(selectedParticipants);
        var pList = $('<ul class="circle"></ul>').appendTo(detailsContainer);
        $.each(selectedParticipants.participants, function (pC, p) {
            var participantWidgetLink = $('<a href="#">' + p.name + ' (' + p.description + ')</a>').appendTo($('<li></li>').appendTo(pList));
            participantWidgetLink.click(function (e) {
                e.preventDefault();
                addParticipantExplorerWidget(p);
            });
        });

        // group separately
//        if (selectedParticipants.participants.length > 1) {
//            var participantWidgetLink = $('<a href="#">View as a group</a>').appendTo($('<li></li>').appendTo(pList));
//            participantWidgetLink.click(function(e) {
//                e.preventDefault();
//            });
//        }
    });
}

function renderParticipantQoeGraphsToContainer(selectedParticipantId, addToContainer, detailsContainer) {
    addToContainer.empty();
    graphsCounter++;
    var newChartId = 'wg' + graphsCounter;
    addToContainer.append('<div id="l' + newChartId + '" class="widgetGraph"><svg class="large-6 text-center columns"></svg></div>');
    addToContainer.append('<div id="r' + newChartId + '" class="widgetGraph"><svg class="large-6 text-center columns"></svg></div>');

    d3.json(BASE_URL + "/explorer/" + experimentId + "/participants/iri/distribution/qoe?IRI=" + encodeURIComponent(selectedParticipantId), function (data) {
        $('#l' + newChartId + ' svg').show().height(CHART_HEIGHT);
        $('#r' + newChartId + ' svg').show().height(CHART_HEIGHT);
        console.log(data);
        nv.addGraph(function () {
            var chart = nv.models.multiBarHorizontalChart()
                    .x(function (d) {
                        return d.label;
                    })
                    .y(function (d) {
                        return d.count;
                    })
                    .tooltipContent(function (key, label, count) {
                        return '<p><strong>' + count + ' participants</strong> selected ' + label + '</p>';
                    })
                    .showYAxis(false)
                    .margin({top: 30, right: 20, bottom: 50, left: 130})
                    .barColor(d3.scale.customColors().range())
                    .showValues(true)
                    .showControls(false)
                    .transitionDuration(350)
                    .valueFormat(d3.format(',f'));
            chart.yAxis
                    .tickFormat(d3.format(',f'));
            d3.select('#l' + newChartId + ' svg')
                    .datum(data)
                    .call(chart);
            nv.utils.windowResize(chart.update);
            chart.multibar.dispatch.on("elementClick", function (e) {
//                chartClickAttr(e, detailsContainer, selectedAttributeName);
            });
            return chart;
        });
        // donut chart
        nv.addGraph(function () {
            var chart = nv.models.pieChart()
                    .x(function (d) {
                        return d.label;
                    })
                    .y(function (d) {
                        return d.count;
                    })
                    .height(500)
                    .tooltipContent(function (label, count) {
                        return '<p><strong>' + count + ' participants</strong> selected ' + label + '</p>';
                    })
                    .showLabels(true)
                    .labelThreshold(.05)    // Configure the minimum slice size for labels to show up
                    .color(d3.scale.customColors().range())
                    .valueFormat(d3.format(',f'))
                    .labelType("percent")   // Configure what type of data to show in the label. Can be "key", "value" or "percent"
                    .donut(true)            // Turn on Donut mode.
                    .donutRatio(0.325);     // Configure how big you want the donut hole size to be.
            d3.select('#r' + newChartId + ' svg')
                    .datum(data[0].values)
                    .transition().duration(350)
                    .call(chart);
            chart.pie.dispatch.on("elementClick", function (e) {
//                chartClickAttr(e, detailsContainer, selectedAttributeName);
            });
            return chart;
        });
    });
}

function renderAttributeGraphsToContainer(selectedAttributeName, addToContainer, detailsContainer) {
    addToContainer.empty();
    graphsCounter++;
    var newChartId = 'wg' + graphsCounter;
    addToContainer.append('<div id="l' + newChartId + '" class="widgetGraph"><svg class="large-6 text-center columns"></svg></div>');
    addToContainer.append('<div id="r' + newChartId + '" class="widgetGraph"><svg class="large-6 text-center columns"></svg></div>');

    d3.json(BASE_URL + "/explorer/" + experimentId + "/attributes/distribution/qoe?attrName=" + encodeURIComponent(selectedAttributeName), function (data) {
        $('#l' + newChartId + ' svg').show().height(CHART_HEIGHT);
        $('#r' + newChartId + ' svg').show().height(CHART_HEIGHT);
        console.log(data);
        nv.addGraph(function () {
            var chart = nv.models.multiBarHorizontalChart()
                    .x(function (d) {
                        return d.label;
                    })
                    .y(function (d) {
                        return d.count;
                    })
                    .tooltipContent(function (key, label, count) {
                        return '<p><strong>' + count + ' participants</strong> selected ' + label + '</p>';
                    })
                    .showYAxis(false)
                    .margin({top: 30, right: 20, bottom: 50, left: 130})
                    .barColor(d3.scale.customColors().range())
                    .showValues(true)
                    .showControls(false)
                    .transitionDuration(350)
                    .valueFormat(d3.format(',f'));
            chart.yAxis
                    .tickFormat(d3.format(',f'));
            d3.select('#l' + newChartId + ' svg')
                    .datum(data)
                    .call(chart);
            nv.utils.windowResize(chart.update);
            chart.multibar.dispatch.on("elementClick", function (e) {
                chartClickAttr(e, detailsContainer, selectedAttributeName);
            });
            return chart;
        });
        // donut chart
        nv.addGraph(function () {
            var chart = nv.models.pieChart()
                    .x(function (d) {
                        return d.label;
                    })
                    .y(function (d) {
                        return d.count;
                    })
                    .height(500)
                    .tooltipContent(function (label, count) {
                        return '<p><strong>' + count + ' participants</strong> selected ' + label + '</p>';
                    })
                    .showLabels(true)
                    .labelThreshold(.05)    // Configure the minimum slice size for labels to show up
                    .color(d3.scale.customColors().range())
                    .valueFormat(d3.format(',f'))
                    .labelType("percent")   // Configure what type of data to show in the label. Can be "key", "value" or "percent"
                    .donut(true)            // Turn on Donut mode.
                    .donutRatio(0.325);     // Configure how big you want the donut hole size to be.
            d3.select('#r' + newChartId + ' svg')
                    .datum(data[0].values)
                    .transition().duration(350)
                    .call(chart);
            chart.pie.dispatch.on("elementClick", function (e) {
                chartClickAttr(e, detailsContainer, selectedAttributeName);
            });
            return chart;
        });
    });
}

function renderStratParticipansGraphToContainer(addToContainer, detailsContainer) {
    addToContainer.empty();
    graphsCounter++;
    var newChartId = 'wg' + graphsCounter;
    addToContainer.append('<div id="' + newChartId + '" class="widgetGraph"><svg class="large-12 text-center columns"></svg></div>');
    d3.json(BASE_URL + "/explorer/" + experimentId + "/participants/distribution/stratified", function (data) {
        console.log(data);
        $('#' + newChartId + ' svg').show().height(CHART_HEIGHT);
        nv.addGraph(function () {
            var chart = nv.models.multiBarHorizontalChart()
                    .x(function (d) {
                        return d.label;
                    })
                    .y(function (d) {
                        return d.count;
                    })
                    .margin({top: 50, right: 40, bottom: 50, left: 120})
                    .showValues(false)
                    .tooltipContent(function (key, label, count, e) {
                        return '<p><strong>' + count + ' participants</strong> selected ' + e.point.labelValue + ' for ' + label + '</p>';
                    })
                    .color(d3.scale.customColors().range())
                    .transitionDuration(350)
                    .stacked(true)
                    .showControls(true);
            chart.yAxis
                    .axisLabel('Number of participants')
                    .tickFormat(d3.format('0 10 , .f'));
            d3.select('#' + newChartId + ' svg')
                    .datum(data)
                    .call(chart);
            nv.utils.windowResize(chart.update);
            chart.multibar.dispatch.on("elementClick", function (e) {
                chartClickStrat(e, detailsContainer);
            });
            return chart;
        });
    });
}

// shows active experiment details
function showConfigurationDetails(configurationMetadata) {
//    console.log(configurationMetadata);
    var d = configurationMetadata.databaseConfig;
    var r = configurationMetadata.rabbitConfig;
    $("#configuration_details").empty();
    $("#configuration_details").append("<div class='clearfix'><p class='details'><span class='left'>Project: </span><span class='right'>" + configurationMetadata.projectName + "</span></p></div>");
    $("#configuration_details").append("<div class='clearfix'><p class='details'><span class='left'>Database: </span><span class='right'>" + d.url + " / " + d.databaseName + "</span></p></div>");
    $("#configuration_details").append("<div class='clearfix'><p class='details'><span class='left'>Rabbit: </span><span class='right'>" + r.ip + ":" + r.port + " / " + r.monitorId + "</span></p></div>");
}

// shows active experiment details
function showActiveExperimentDetails(experimentMetadata) {
//    console.log(experimentMetadata);
    $("#experiment_details").empty();
    $("#experiment_details").append("<div class='clearfix'><p class='details'><span class='left'>Project: </span><span class='right'>" + experimentMetadata.projectName + "</span></p></div>");
    $("#experiment_details").append("<div class='clearfix'><p class='details'><span class='left'>Name: </span><span class='right'>" + experimentMetadata.name + "</span></p></div>");
    $("#experiment_details").append("<div class='clearfix'><p class='details'><span class='left'>Description: </span><span class='right'>" + experimentMetadata.description + "</span></p></div>");
    $("#experiment_details").append("<div class='clearfix'><p class='details'><span class='left'>Started: </span><span class='right'>" + moment(new Date(experimentMetadata.startTime)).format(DISPLAY_TIME_FORMAT) + "</span></p></div>");

}

function showProvDetails(provDetails) {
    $("#prov_details").empty();
    $("#prov_details").append("<div class='clearfix'><p class='details'><span class='left'>Participants: </span><span class='right'>" + provDetails.participantCount + "</span></p></div>");
    $("#prov_details").append("<div class='clearfix'><p class='details'><span class='left'>Activities: </span><span class='right'>" + provDetails.activitiesPerformedCount + "</span></p></div>");
    $("#prov_details").append("<div class='clearfix'><p class='details'><span class='left'>Applications: </span><span class='right'>" + provDetails.applicationsUsedCount + "</span></p></div>");
    $("#prov_details").append("<div class='clearfix'><p class='details'><span class='left'>Services: </span><span class='right'>" + provDetails.servicesUsedCount + "</span></p></div>");

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