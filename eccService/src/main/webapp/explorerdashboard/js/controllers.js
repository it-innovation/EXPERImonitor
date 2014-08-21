//var BASE_URL = "http://" + $(location).attr('host') + "/" + $(location).attr('href').split('/')[3];
var BASE_URL = "http://zts14:8080/EccService-2.1";
var EXP_ID;
var CHART_HEIGHT = 500;

var appControllers = angular.module('appControllers', ['angular-loading-bar', 'ngTable']);

appControllers.controller('MainController', ['$scope', function($scope) {
        $scope.experiments = [
            {
                "uuid": "c91c05ed-c6ba-4880-82af-79eb5d4a58cd",
                "name": "Test Experiment",
                "description": "New EXPERIMEDIA Experiment",
                "phase": "unknown",
                "status": "Finished",
                "projectName": "My Local EXPERIMEDIA Project",
                "startTime": 1408378746438,
                "endTime": 1408378877686
            }
        ];
}]);

appControllers.controller('ExperimentController', ['$scope', '$http', '$routeParams', function($scope, $http, $routeParams) {
    EXP_ID = $routeParams.uuid;
    $http.get(BASE_URL + "/explorer/" + EXP_ID + "/summary").success(function(data) {
        $scope.summary = data;
    });
}]);

appControllers.controller('ParticipantController', ['$scope', '$http', function($scope, $http) {
    $http.get(BASE_URL + "/explorer/" + EXP_ID + "/participants").success(function(data) {
        $scope.participants = data.participants;
    });
    $http.get(BASE_URL + "/explorer/" + EXP_ID + "/participants/groupAttributes").success(function(data) {
        $scope.attributes = data.qoEAttributes;
    });
    // sets viz options to default i.e. 'All'
    $scope.participantSelection = null;
    $scope.attributeSelection = null;
    var customColors = ["#d90000", "#ff9326", "#f5dd01", "#01ff51", "#00b200"];
    d3.scale.customColors = function() {
        return d3.scale.ordinal().range(customColors);
    };     
    $scope.viz = function(){
        if($scope.participantSelection === null && $scope.attributeSelection === null){
        // -- this shows a viz of all participants and all attributes
            $('#selectedParticipants').hide();
            d3.json(BASE_URL + "/explorer/" + EXP_ID + "/participants/distribution/stratified", function(data) {
                $('#attribBarChart svg, #attribDonutChart svg').hide();
                $('#allQoEChart svg').show().height(CHART_HEIGHT);
                nv.addGraph(function() {
                    var chart = nv.models.multiBarHorizontalChart()
                        .x(function(d) { return d.label; })
                        .y(function(d) { return d.count; })
                        .margin({top: 50, right: 40, bottom: 50, left: 100})
                        .showValues(false)
                        .tooltipContent(function(key, label, count, e) { return '<p><strong>' + count + ' participants</strong> selected ' + e.point.labelValue + ' for ' + label + '</p>' ; })
                        .color(d3.scale.customColors().range())
                        .transitionDuration(350)
                        .stacked(true)
                        .showControls(true);
                    chart.yAxis
                        .tickFormat(d3.format('0 10 , .f'));
                    d3.select('#allQoEChart svg')
                        .datum(data)
                        .call(chart);
                    nv.utils.windowResize(chart.update);
                    chart.multibar.dispatch.on("elementClick", function(e) {
                        $('#selectedParticipants').show();
                        $('#selectedParticipants h6').html("<strong>" + e.point.size + " participants</strong>");
                        $('#selectedAttributes h6').html("QoE of <strong>" + e.point.labelValue + "</strong> for <strong>" + e.point.label + "</strong>");
                        $http.get(BASE_URL + "/explorer/" + EXP_ID + "/participants/attributes/select?attrName=" + encodeURIComponent(e.point.label) + "&nomOrdLabel=" + encodeURIComponent(e.point.labelValue)).success(function(data) {
                            $scope.selectedParticipants = data.participants;
                        });
                        //-- build service to support this
//                        $http.get(BASE_URL + "/explorer/" + EXP_ID + "/participants/groupAttributes").success(function(data) {
//                            $scope.selectedAttributes = data.qoEAttributes[0];
//                        });
                    });
                    return chart;
                });
            });
        } else if($scope.participantSelection === null && $scope.attributeSelection !== null){
        // -- this shows a viz of a single attribute for all participants
            $('#selectedParticipants, #selectedAttributes h6').hide();
            d3.json(BASE_URL + "/explorer/" + EXP_ID + "/attributes/distribution/qoe?attrName=" + encodeURIComponent($scope.attributeSelection), function(data) {
                $('#allQoEChart svg').hide();
                $('#attribBarChart svg, #attribDonutChart svg').show().height(CHART_HEIGHT);
                nv.addGraph(function() {
                    var chart = nv.models.multiBarHorizontalChart()
                        .x(function(d) { return d.label; })
                        .y(function(d) { return d.count; })
                        .tooltipContent(function(key, label, count) { return '<p><strong>' + count + ' participants</strong> selected ' + label + '</p>' ; })
                        .showYAxis(false)
                        .margin({top: 30, right: 20, bottom: 50, left: 130})
                        .barColor(d3.scale.customColors().range())
                        .showValues(true)
                        .showControls(false)
                        .transitionDuration(350)
                        .valueFormat(d3.format(',f'));
                    chart.yAxis
                        .tickFormat(d3.format(',f'));
                    d3.select('#attribBarChart svg')
                        .datum(data)
                        .call(chart);
                    nv.utils.windowResize(chart.update);
                    chart.multibar.dispatch.on("elementClick", function(e) {
                        chartClick(e);
                    });
                    return chart;
                });
                // donut chart
                nv.addGraph(function() {
                    var chart = nv.models.pieChart()
                        .x(function(d) { return d.label; })
                        .y(function(d) { return d.count; })
                        .height(500)
                        .tooltipContent(function(label, count) { return '<p><strong>' + count + ' participants</strong> selected ' + label + '</p>' ; })
                        .showLabels(true)
                        .labelThreshold(.05)    // Configure the minimum slice size for labels to show up
                        .color(d3.scale.customColors().range())
                        .valueFormat(d3.format(',f'))
                        .labelType("percent")   // Configure what type of data to show in the label. Can be "key", "value" or "percent"
                        .donut(true)            // Turn on Donut mode.
                        .donutRatio(0.325);     // Configure how big you want the donut hole size to be.
                    d3.select('#attribDonutChart svg')
                        .datum(data[0].values)
                        .transition().duration(350)
                        .call(chart);
                    chart.pie.dispatch.on("elementClick", function(e) {
                        chartClick(e);
                    });
                    return chart;
                });
                function chartClick(e){
                    $('#selectedParticipants, #selectedAttributes, #selectedAttributes h6').show();
                    $('#selectedParticipants h6').html("<strong>" + e.point.count + " participants</strong>");
                    $("#selectedAttributes h6").html("QoE value <strong>" + e.point.label + "</strong>");
                    $http.get(BASE_URL + "/explorer/" + EXP_ID + "/participants/attributes/select?attrName=" + encodeURIComponent($scope.attributeSelection) + "&nomOrdLabel=" + encodeURIComponent(e.point.label)).success(function(data) {
                        $scope.selectedParticipants = data.participants;
                    });
                    //-- build service to support this
//                    $http.get(BASE_URL + "/explorer/" + EXP_ID + "/participants/groupAttributes").success(function(data) {
//                        $scope.selectedAttributes = data.qoEAttributes[0];
//                    });
                };
            });
        } else if($scope.participantSelection !== null && $scope.attributeSelection === null){
        // -- this shows info on all attributes for a single participant
            $('#selectedParticipants, #attribBarChart svg, #attribDonutChart svg, #allQoEChart svg').hide();
            $('#selectedAttributes, #selectedAttributes h6').show();
            $('#selectedAttributes table').hide();
            $('#selectedAttributes h6').html("Should return info on all attributes for a single participant. Service not fully implemented.");

        } else if($scope.participantSelection !== null && $scope.attributeSelection !== null){
        // -- this shows info on a single attribute for a single participant
            $('#selectedParticipants, #attribBarChart svg, #attribDonutChart svg, #allQoEChart svg').hide();
            $('#selectedAttributes, #selectedAttributes h6').show();
            $('#selectedAttributes table').hide();
            $('#selectedAttributes h6').html("Should return info on the selecetd attribute for a single participant. Service not available yet.");
        }
    };    
}]);

appControllers.controller('DetailsController', ['$scope', '$http', '$routeParams', 'ngTableParams', function($scope, $http, $routeParams, ngTableParams) {
    $http.get(BASE_URL + "/explorer/" + EXP_ID + "/participants").success(function(data) {
        $scope.participants = data.participants;
    });
    $http.get(BASE_URL + "/explorer/" + EXP_ID + "/participants/iri?IRI=" + encodeURIComponent($routeParams.iri)).success(function(data) {
        $scope.participantSelection = data.iri;
    });
    $scope.getActivities = function(){
        $('#applications, #services, #serviceMetrics, #qosChart').hide();
        $.get(BASE_URL + "/explorer/" + EXP_ID + "/participants/iri/activities/summary?IRI=" + encodeURIComponent($scope.participantSelection), function(data) {
            $scope.activities = data.activities;
        });
    };
    $scope.getActivityInstancesNApps = function(){
        $.get(BASE_URL + "/explorer/" + EXP_ID + "/participants/iri/activities/select?IRI=" + encodeURIComponent($scope.participantSelection)+ "&actLabel=" + encodeURIComponent($scope.activitySelection.label)).success(function(data) {
            $scope.activityInstances = data.activities;
            $scope.numActivities = data.activityTotal;
            var actIRI = data.activities[0].iri;
            $scope.tableParams = new ngTableParams({
                page: 1,            // show first page
                count: 5           // count per page
            }, {
                total: data.activities.length, // length of data
                getData: function($defer, params) {
                    $defer.resolve(data.activities.slice((params.page() - 1) * params.count(), params.page() * params.count()));
                }
            });
            $.get( BASE_URL + "/explorer/" + EXP_ID + "/activities/iri/applications?IRI=" + encodeURIComponent(actIRI), function( data ) {
                $scope.applications = data.applications;
            });
        }); 
        $('#applications').show();
    };
    $scope.getServices = function(){
        $.get( BASE_URL + "/explorer/" + EXP_ID + "/applications/iri/services?IRI=" + encodeURIComponent($scope.applicationSelection.iri), function( data ) {
            $scope.services = data.services;
        });
        $('#services').show();
    };
    $scope.getServiceMetrics = function(){
        $.get( BASE_URL + "/explorer/" + EXP_ID + "/services/iri/attributes?IRI=" + encodeURIComponent($scope.serviceSelection.iri), function( data ) {
            $scope.serviceMetrics = data.attributes;
        });
        $('#serviceMetrics').show();
    };    
    $scope.timeSeries = function(){
        $('#qosChart').show();
        $('#qosChart svg').height(CHART_HEIGHT);
        d3.json(BASE_URL + "/explorer/" + EXP_ID + "/attributes/series/qos/highlight/activities?attrID=" + $scope.serviceMetricSelection.metricID + "&IRI=" + encodeURIComponent($scope.participantSelection) + "&actLabel=" + encodeURIComponent($scope.activitySelection.label), function(data) { 
            nv.addGraph(function() {
                var chart = nv.models.lineChart()
                    .x(function(d) { return d.timestamp; })
                    .y(function(d) { return d.value/10; })
                    .useInteractiveGuideline(true)
                    .forceY([0])
                    .color(d3.scale.category10().range())
                    .isArea(true);
                chart.xAxis
                    .showMaxMin(true)
                    .tickFormat(function(d) { return d3.time.format('%X')(new Date(d)); });
                chart.yAxis
                    .axisLabel('Response Time(ms)')
                    .tickFormat(d3.format(',.2f'));
                d3.select('#qosChart svg')
                    .datum(data.seriesSet)
                    .transition().duration(500)
                    .call(chart);
                nv.utils.windowResize(chart.update);
                return chart;
            });
        });
    };
}]);