// TODO: get url from window
var BASE_URL = "http://zts14:8080/EccService-2.1/";
var EXP_ID;
var CHART_HEIGHT = 500;

var appControllers = angular.module('appControllers', ['angular-loading-bar']);

appControllers.filter('encodeURIComponent', function() {
    return window.encodeURIComponent;
});

appControllers.controller('MainController', ['$scope', '$http', function($scope, $http) {
    $http.get(BASE_URL + "/experiments").success(function(data) {
        $scope.experiments = data;
    });
}]);

appControllers.controller('ExperimentController', ['$scope', '$http', '$routeParams', function($scope, $http, $routeParams) {
    EXP_ID = $routeParams.uuid;
    $http.get(BASE_URL + "explorer/" + EXP_ID + "/summary").success(function(data) {
        $scope.summary = data;
    });
}]);

appControllers.controller('ParticipantController', ['$scope', '$http', function($scope, $http) {        
    $http.get(BASE_URL + "explorer/" + EXP_ID + "/participants").success(function(data) {
        $scope.participants = data.participants;
    });
    $http.get(BASE_URL + "explorer/" + EXP_ID + "/participants/groupAttributes").success(function(data) {
        $scope.attributes = data.qoEAttributes;
    });
    // sets viz options to default i.e. 'All'
    $scope.participantSelection = null;
    $scope.attributeSelection = null;
    // updates viz depending on chosen options
    $scope.viz = function(){
        if($scope.participantSelection === null && $scope.attributeSelection === null){
            d3.select('svg').style("height", CHART_HEIGHT);
            d3.json(BASE_URL + "explorer/" + EXP_ID + "/participants/distribution/stratified", function(data) {
                // clear the canvas before redrawing
                $('#chart1 svg').hide();
                $('#chart2 svg').hide();
                $('#chart3 svg').show();
                // stacked bar chart
                nv.addGraph(function() {
                    var chart = nv.models.multiBarHorizontalChart()
                        .x(function(d) { return d.label; })
                        .y(function(d) { return d.count; })
                        .margin({top: 50, right: 40, bottom: 50, left: 100})
                        .height(CHART_HEIGHT)
                        .showValues(false)
                        .tooltips(true)
                        .barColor(d3.scale.category20().range())
                        .transitionDuration(350)
                        .stacked(true)
                        .showControls(true);
                    chart.yAxis
                        .tickFormat(d3.format(',.1f'));     // TODO: change to no decimal point
                    d3.select('#chart3 svg')
                        .datum(data)
                        .call(chart);
                    nv.utils.windowResize(chart.update);
                    /// TODO: !!!!! need to check what this does
                    chart.dispatch.on('stateChange', function(e) { nv.log('New State:', JSON.stringify(e)); });
                    return chart;
                });
            });
        } else if($scope.participantSelection === null && $scope.attributeSelection !== null){
            d3.json(BASE_URL + "explorer/" + EXP_ID + "/attributes/distribution/qoe?attrName=" + encodeURIComponent($scope.attributeSelection), function(data) {
                // clear the canvas before redrawing
                $('#chart3 svg').hide();
                $('#chart1 svg').show();
                $('#chart2 svg').show();
                // histogram
                nv.addGraph(function() {
                    var chart = nv.models.discreteBarChart()
                        .x(function(d) { return d.label; })    //Specify the data accessors.
                        .y(function(d) { return d.count; })
                        .height(CHART_HEIGHT)
                        .staggerLabels(false)    //Too many bars and not enough room? Try staggering labels.
                        .tooltips(false)        //Don't show tooltips
                        .showValues(true)       //...instead, show the bar value right on top of each bar.
                        .transitionDuration(350)
                        ;
                    chart.yAxis
                        .tickFormat(d3.format(',.f'));
                    d3.select('#chart1 svg')
                        .datum(data)
                        .call(chart);
                    nv.utils.windowResize(chart.update);
                    chart.discretebar.dispatch.on("elementClick", function(e) {
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
                        .showLabels(true)     //Display pie labels
                        .labelThreshold(.05)  //Configure the minimum slice size for labels to show up
                        .labelType("percent") //Configure what type of data to show in the label. Can be "key", "value" or "percent"
                        .donut(true)          //Turn on Donut mode. Makes pie chart look tasty!
                        .donutRatio(0.35);     //Configure how big you want the donut hole size to be.
                    d3.select('#chart2 svg')
                        .datum(data[0].values)
                        .transition().duration(350)
                        .call(chart);
                    chart.pie.dispatch.on("elementClick", function(e) {
                        chartClick(e);
                    });
                    return chart;
                });
                // click event function
                function chartClick(e) {
                    $http.get(BASE_URL + "explorer/" + EXP_ID + "/participants/attributes/select?attrName" + encodeURIComponent($scope.attributeSelection) + "&nomOrdLabel=" + encodeURIComponent(e.point.label)).success(function(data) {
                        $scope.participants = data.participants;
                    });
                }
            });
        } else if(participantSelection !== null && attributeSelection === null){
            
        } else if(participantSelection !== null && attributeSelection !== null){
            console.log(BASE_URL + "explorer/" + EXP_ID + "/participants/iri/attributes?IRI=" + encodeURIComponent(participantSelection));
            //$.get(BASE_URL + "explorer/" + EXP_ID + "/participants", function(data){});
        }       
        return true;    // to enable angular ng-show
    };    
}]);

appControllers.controller('DetailsController', ['$scope', '$http', '$routeParams', function($scope, $http, $routeParams) {
    var partIRI = encodeURIComponent($routeParams.iri);
    $http.get(BASE_URL + "explorer/" + EXP_ID + "/participants").success(function(data) {
        $scope.participants = data.participants;
    });
    $http.get(BASE_URL + "explorer/" + EXP_ID + "/participants/iri?IRI=" + partIRI).success(function(data) {
        $scope.whichPeople = data.name;
        // TODO: need to fix routeparams to show current selction
        $scope.participantSelection = data.name;
    });
    // get activities
    $http.get(BASE_URL + "explorer/" + EXP_ID + "/participants/iri/activities/summary?IRI=" + partIRI).success(function(data) {
        $scope.activities = data.activities;
    });
    // All participant activities
    $.get(BASE_URL + "explorer/" + EXP_ID + "/participants/iri/activities?IRI=" + partIRI).success(function(data) {
        $scope.activityInstances = data.activities;
        $scope.numActivities = data.activityTotal;
    });
    $scope.activitySelection = "";
    $scope.getApplications = function(activitySelection){
        // beacause an activuty label has no IRI we need to use it to return an instance and get th IRI
        // use jquery $.get rather than angular $http.get to avoid continous loop
        // get filtered activities
        $.get(BASE_URL + "explorer/" + EXP_ID + "/participants/iri/activities/select?IRI=" + partIRI + "&actLabel=" + encodeURIComponent(activitySelection.label)).success(function(data) {
            $scope.activityInstances = data.activities;
            $scope.numActivities = data.activityTotal;
            var actIRI = data.activities[0].iri;
            $.get( BASE_URL + "explorer/" + EXP_ID + "/activities/iri/applications?IRI=" + encodeURIComponent(actIRI), function( data ) {
                $scope.applications = data.applications;
            });
        });
        return true;    // to enable angular ng-show
    };
    $scope.applicationSelection = "";
    $scope.getServices = function(applicationSelection){
        $.get( BASE_URL + "explorer/" + EXP_ID + "/applications/iri/services?IRI=" + encodeURIComponent(applicationSelection.iri), function( data ) {
            $scope.services = data.services;
        });
        return true;    // to enable angular ng-show
    };
    $scope.serviceSelection = "";
    $scope.getServiceMetrics = function(serviceSelection){
        $.get( BASE_URL + "explorer/" + EXP_ID + "/services/iri/attributes?IRI=" + encodeURIComponent(serviceSelection.iri), function( data ) {
            $scope.serviceMetrics = data.attributes;
        });
        return true;    // to enable angular ng-show
    };
    // clear the canvas before redrawing !!!! find a better way to allow for interactivity
    d3.select('#chart4 svg').remove();
    d3.select('#chart5 svg').remove();
    d3.select('#chart6 svg').remove();
    //$('#chart4').append('<svg class="large-6 text-center columns"></svg>');
    //$('#chart5').append('<svg class="large-6 text-center columns"></svg>');
    //$('#chart6').append('<svg class="large-12 text-center columns"></svg>');
    // bar chart
    d3.json("json/qoe.json", function(data) {
        nv.addGraph(function() {
            var chart = nv.models.multiBarHorizontalChart()
                .x(function(d) { return d.label; })
                .y(function(d) { return d.value; })
                //.margin({top: 30, right: 20, bottom: 50, left: 175})
                .showValues(true)
                .tooltips(false)
                .showControls(false);
            chart.yAxis
                .tickFormat(d3.format(',.1f'));
            d3.select('#qosChart svg')
                .datum(data)
              .transition().duration(500)
                .call(chart);
            nv.utils.windowResize(chart.update);
            return chart;
        });
    });
    
    $scope.timeSeries = function(serviceMetricSelection, serviceSelection, activitySelection){
        //d3.json("json/tst.json", function(error, data) {
        d3.json(BASE_URL + "explorer/" + EXP_ID + "/attributes/series/qos/highlight/activities?attrID=" + serviceMetricSelection.metricID + "&IRI=" + partIRI + "&actLabel=" + encodeURIComponent(activitySelection.label), function(error, data) {
            nv.addGraph(function() {
                var chart = nv.models.lineChart()
                    .x(function(d) { return d.timestamp; })
                    .y(function(d) { return d.value/100; })
                    .useInteractiveGuideline(true)
                    .isArea(true)
                    //.forceY([0]);
                chart.xAxis
                    .showMaxMin(true)
                    .tickFormat(function(d) { return d3.time.format('%X')(new Date(d)); });
                chart.yAxis
                    .axisLabel('Response Time(ms)')
                    .tickFormat(d3.format(',.2f'));
                d3.select('#qosChart1 svg')
                    .datum(data.seriesSet)
                    .transition().duration(500)
                    .call(chart);
                nv.utils.windowResize(chart.update);
                return chart;
            });
        });
    };
}]);