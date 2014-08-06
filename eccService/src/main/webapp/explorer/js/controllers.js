var BASE_URL = "http://zts14:8080/EccService-2.1/explorer/";
var EXP_ID = "c91c05ed-c6ba-4880-82af-79eb5d4a58cd"; 
var appControllers = angular.module('appControllers', []);

appControllers.filter('encodeURIComponent', function() {
    return window.encodeURIComponent;
});

appControllers.controller('MainController', ['$scope', '$http', function($scope, $http) {
    $http.get(BASE_URL + EXP_ID + "/summary").success(function(data) {
        $scope.summary = data;
    });
}]);

appControllers.controller('ParticipantController', ['$scope', '$http', function($scope, $http) {        
    $http.get(BASE_URL + EXP_ID + "/participants").success(function(data) {
        $scope.participants = data.participants;
    });
    $http.get(BASE_URL + EXP_ID + "/participants/groupAttributes").success(function(data) {
        $scope.attributes = data.qoEAttributes;
    });
    // need to change---too coupled
    $scope.attributeSelection = "All";
    $scope.visualize = function(participantSelection, attributeSelection){
        // draw chart
        if(attributeSelection === "All"){
            d3.json(BASE_URL + EXP_ID + "/participants/distribution/stratified", function(data) {
                // clear the canvas before redrawing !!!! find a better way to allow for interactivity
                d3.select('#chart1 svg').remove();
                d3.select('#chart2 svg').remove();
                d3.select('#chart3 svg').remove();
                $('#chart3').append('<svg class="large-12 text-center columns"></svg>');
                // stacked bar chart
                nv.addGraph(function() {
                    var chart = nv.models.multiBarHorizontalChart()
                        .x(function(d) { return d.label; })
                        .y(function(d) { return d.count; })
                        .margin({top: 50, right: 40, bottom: 50, left: 100})
                        .height(500)
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
        } else {
            d3.json(BASE_URL + EXP_ID + "/attributes/distribution/qoe?attrName=" + encodeURIComponent(attributeSelection), function(data) {
                // clear the canvas before redrawing !!!! find a better way to allow for interactivity
                d3.select('#chart1 svg').remove();
                d3.select('#chart2 svg').remove();
                d3.select('#chart3 svg').remove();
                $('#chart1').append('<svg class="large-6 text-center columns"></svg>');
                $('#chart2').append('<svg class="large-6 text-center columns"></svg>');
                // histogram
                nv.addGraph(function() {
                    var chart = nv.models.discreteBarChart()
                        .x(function(d) { return d.label; })    //Specify the data accessors.
                        .y(function(d) { return d.count; })
                        .height(500)
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
                    $http.get(BASE_URL + EXP_ID + "/participants/attributes/select?attrName" + encodeURIComponent(attributeSelection) + "&nomOrdLabel=" + encodeURIComponent(e.point.label)).success(function(data) {
                        $scope.participants = data.participants;
                    });
                }
            });
        }       
        return true;    // to enable angular ng-show
    };    
}]);

appControllers.controller('DetailsController', ['$scope', '$http', '$routeParams', function($scope, $http, $routeParams) {
    var partIRI = encodeURIComponent($routeParams.iri);
    var actIRI = "http%3A%2F%2Fit-innovation.soton.ac.uk%2Fontologies%2Fexperimedia%23activity_c108742d-d41d-40ee-b532-7f8fd6508baf";
    var serIRI = "http%3A%2F%2Fit-innovation.soton.ac.uk%2Fontologies%2Fexperimedia%23application_3af4c091-2019-4f1c-a867-89c44970a509";
    $http.get(BASE_URL + EXP_ID + "/participants").success(function(data) {
        $scope.participants = data.participants;
    });
    $http.get(BASE_URL + EXP_ID + "/participants/iri?IRI=" + partIRI).success(function(data) {
        $scope.whichPeople = data.name;
        // TODO: need to fix routeparams to show current selction
        $scope.participantSelection = data.name;
    });
    // get activities
    $http.get(BASE_URL + EXP_ID + "/participants/iri/activities?IRI=" + partIRI).success(function(data) {
        $scope.activities = data.activities;
        $scope.numActivities = data.activityTotal;
        //TODO: Pick from selection
        actIRI = encodeURIComponent(data.activities[0].iri);
    });
    $scope.activitySelection = "";
    $scope.getApplications = function(activitySelection){
        //using jqery get since angular http.get looping
        $.get( BASE_URL + EXP_ID + "/activities/iri/applications?IRI=" + encodeURIComponent(activitySelection.iri), function( data ) {
            $scope.applications = data.applications;
        });
        return true;    // to enable angular ng-show
    };
    $scope.applicationSelection = "";
    $scope.getServices = function(applicationSelection){
        $.get( BASE_URL + EXP_ID + "/applications/iri/services?IRI=" + encodeURIComponent(applicationSelection.iri), function( data ) {
            $scope.services = data.services;
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
    d3.json("qoe.json", function(data) {
        nv.addGraph(function() {
            var chart = nv.models.multiBarHorizontalChart()
                .x(function(d) { return d.label })
                .y(function(d) { return d.value })
                .margin({top: 30, right: 20, bottom: 50, left: 175})
                .showValues(true)
                .tooltips(false)
                .showControls(false);
            chart.yAxis
                .tickFormat(d3.format(',.1f'));
            d3.select('#chart4 svg')
                .datum(data)
              .transition().duration(500)
                .call(chart);
            nv.utils.windowResize(chart.update);
            return chart;
        });
    });
    // line chart
    d3.json('cumulativeLineData.json', function(data) {
        nv.addGraph(function() {
            var chart = nv.models.cumulativeLineChart()
                .x(function(d) { return d[0]; })
                .y(function(d) { return d[1]/100; }) //adjusting, 100% is 1.00, not 100 as it is in the data
                .color(d3.scale.category10().range())
                .useInteractiveGuideline(true);
            chart.xAxis
              .tickValues([1078030800000,1122782400000,1167541200000,1251691200000])
              .tickFormat(function(d) {
                  return d3.time.format('%x')(new Date(d));
                });
          chart.yAxis
              .tickFormat(d3.format(',.1%'));
          d3.select('#chart6 svg')
              .datum(data)
              .call(chart);
          nv.utils.windowResize(chart.update);
          return chart;
        });
    });
}]);