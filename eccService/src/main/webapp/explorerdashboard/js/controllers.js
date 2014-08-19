var BASE_URL = "http://" + $(location).attr('host') + "/" + $(location).attr('href').split('/')[3];
//var BASE_URL = "http://zts14:8080/EccService-2.1";
var EXP_ID;
var CHART_HEIGHT = 500;

var appControllers = angular.module('appControllers', ['angular-loading-bar', 'ngTable']);

appControllers.controller('MainController', ['$scope', '$http', function($scope, $http) {
        //$http.get(BASE_URL + "/experiments").success(function(data) {
        //$http.get("json2/a0.1.json").success(function(data) {
        $scope.experiments = [
            {
                "uuid": "c91c05ed-c6ba-4880-82af-79eb5d4a58cd",
                "name": "Test Experiment",
                "description": "New EXPERIMEDIA Experiment",
                "phase": "Teardown",
                "status": "Finished",
                "projectName": "My Local EXPERIMEDIA Project",
                "startTime": 1406786400000,
                "endTime": 1406836800000
            }
        ];
        //});
    }]);

appControllers.controller('ExperimentController', ['$scope', '$http', '$routeParams', function($scope, $http, $routeParams) {
        EXP_ID = $routeParams.uuid;
        $http.get(BASE_URL + "/explorer/" + EXP_ID + "/summary").success(function(data) {
//            console.log(BASE_URL + "/explorer/" + EXP_ID + "/summary");
            //$http.get("json2/a1.1.json").success(function(data) {
            $scope.summary = data;
        });
    }]);

appControllers.controller('ParticipantController', ['$scope', '$http', function($scope, $http) {
        $http.get(BASE_URL + "/explorer/" + EXP_ID + "/participants").success(function(data) {
            //$http.get("json2/a2.1.json").success(function(data) {
            $scope.participants = data.participants;
        });
        $http.get(BASE_URL + "/explorer/" + EXP_ID + "/participants/groupAttributes").success(function(data) {
            //$http.get("json2/a3.1.json").success(function(data) {
            $scope.attributes = data.qoEAttributes;
        });
        // sets viz options to default i.e. 'All'
        $scope.participantSelection = null;
        $scope.attributeSelection = null;
        // updates viz depending on chosen options
        $scope.viz = function() {
            if ($scope.participantSelection === null && $scope.attributeSelection === null) {
                var customColors = ["#d90000", "#ff9326", "#f5dd01", "#01ff51", "#00b200"];
                d3.scale.customColors = function() {
                    return d3.scale.ordinal().range(customColors);
                };
                $http.get(BASE_URL + "/explorer/" + EXP_ID + "/participants/distribution/stratified").success(function(data) {
                    //d3.json("json2/a4.3.json", function(data) {
                    $('#chart1 svg').hide();
                    $('#chart2 svg').hide();
                    $('#chart3 svg').show().height(CHART_HEIGHT);
                    nv.addGraph(function() {
                        var chart = nv.models.multiBarHorizontalChart()
                                .x(function(d) {
                                    return d.label;
                                })
                                .y(function(d) {
                                    return d.count;
                                })
                                .margin({top: 50, right: 40, bottom: 50, left: 100})
                                .showValues(false)
                                .tooltipContent(function(key, label, count) {
                                    return '<p><strong>' + count + ' participants</strong> selected ' + key + ' for ' + label + '</p>';
                                })
                                .color(d3.scale.customColors().range())
                                .transitionDuration(350)
                                .stacked(true)
                                .showControls(true);
                        chart.yAxis
                                .tickFormat(d3.format(',f'));
                        d3.select('#chart3 svg')
                                .datum(data)
                                .call(chart);
                        nv.utils.windowResize(chart.update);
                        chart.multibar.dispatch.on("elementClick", function(e) {
                            $("#heroStat").html("<strong>" + e.point.size + " participants</strong> selected a QoE of <strong>" + e.series.key + "</strong> for <strong>" + e.point.label + "</strong>");
                        });
                        return chart;
                    });
                });
            } else if ($scope.participantSelection === null && $scope.attributeSelection !== null) {
                d3.json(BASE_URL + "/explorer/" + EXP_ID + "/attributes/distribution/qoe?attrName=" + encodeURIComponent($scope.attributeSelection), function(data) {
                    //$.get("json2/a4.1.json").success(function(data) {
                    // clear the canvas before redrawing
                    $('#chart3 svg').hide();
                    $('#chart1 svg').show().height(CHART_HEIGHT);
                    $('#chart2 svg').show().height(CHART_HEIGHT);
                    nv.addGraph(function() {
                        var chart = nv.models.multiBarHorizontalChart()
                                .x(function(d) {
                                    return d.label;
                                })
                                .y(function(d) {
                                    return d.count;
                                })
                                .tooltipContent(function(key, label, count) {
                                    return '<p><strong>' + count + ' participants</strong> selected ' + label + '</p>';
                                })
                                .showYAxis(false)
                                .margin({top: 30, right: 20, bottom: 50, left: 75})
                                .barColor(d3.scale.customColors().range())
                                .showValues(true)
                                .showControls(false)
                                .transitionDuration(350)
                                .valueFormat(d3.format(',f'));
                        chart.yAxis
                                .tickFormat(d3.format(',f'));
                        d3.select('#chart1 svg')
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
                                .x(function(d) {
                                    return d.label;
                                })
                                .y(function(d) {
                                    return d.count;
                                })
                                .height(500)
                                .tooltipContent(function(label, count) {
                                    return '<p><strong>' + count + ' participants</strong> selected ' + label + '</p>';
                                })
                                .showLabels(true)
                                .labelThreshold(.05)    // Configure the minimum slice size for labels to show up
                                .color(d3.scale.customColors().range())
                                .valueFormat(d3.format(',f'))
                                .labelType("percent")   // Configure what type of data to show in the label. Can be "key", "value" or "percent"
                                .donut(true)            // Turn on Donut mode.
                                .donutRatio(0.325);     // Configure how big you want the donut hole size to be.
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
                        $http.get(BASE_URL + "/explorer/" + EXP_ID + "/participants/attributes/select?attrName=" + encodeURIComponent($scope.attributeSelection) + "&nomOrdLabel=" + encodeURIComponent(e.point.label)).success(function(data) {
                            $scope.participants = data.participants;
                        });
                    }
                });
            } else if (participantSelection !== null && attributeSelection === null) {

            } else if (participantSelection !== null && attributeSelection !== null) {
                //console.log(BASE_URL + "/explorer/" + EXP_ID + "/participants/iri/attributes?IRI=" + encodeURIComponent(participantSelection));
                //$.get(BASE_URL + "explorer/" + EXP_ID + "/participants", function(data){});
            }
            return true;    // to enable angular ng-show
        };
    }]);

appControllers.controller('DetailsController', ['$scope', '$http', '$routeParams', function($scope, $http, $routeParams) {
        var partIRI = encodeURIComponent($routeParams.iri);
        $http.get(BASE_URL + "/explorer/" + EXP_ID + "/participants").success(function(data) {
            $scope.participants = data.participants;
        });
        $http.get(BASE_URL + "/explorer/" + EXP_ID + "/participants/iri?IRI=" + partIRI).success(function(data) {
            $scope.whichPeople = data.name;
            // TODO: need to fix routeparams to show current selction
            $scope.participantSelection = data.name;
        });
        // get activities
        $http.get(BASE_URL + "/explorer/" + EXP_ID + "/participants/iri/activities/summary?IRI=" + partIRI).success(function(data) {
            $scope.activities = data.activities;
        });
        // All participant activities
        $.get(BASE_URL + "/explorer/" + EXP_ID + "/participants/iri/activities?IRI=" + partIRI).success(function(data) {
            $scope.activityInstances = data.activities;
            $scope.numActivities = data.activityTotal;
        });
        $scope.activitySelection = "";
        $scope.getApplications = function(activitySelection) {
            // beacause an activuty label has no IRI we need to use it to return an instance and get th IRI
            // use jquery $.get rather than angular $http.get to avoid continous loop
            // get filtered activities
            $.get(BASE_URL + "/explorer/" + EXP_ID + "/participants/iri/activities/select?IRI=" + partIRI + "&actLabel=" + encodeURIComponent(activitySelection.label)).success(function(data) {
                $scope.activityInstances = data.activities;
                $scope.numActivities = data.activityTotal;
                var actIRI = data.activities[0].iri;
                $.get(BASE_URL + "/explorer/" + EXP_ID + "/activities/iri/applications?IRI=" + encodeURIComponent(actIRI), function(data) {
                    $scope.applications = data.applications;
                });
            });
            return true;    // to enable angular ng-show
        };
        $scope.applicationSelection = "";
        $scope.getServices = function(applicationSelection) {
            $.get(BASE_URL + "/explorer/" + EXP_ID + "/applications/iri/services?IRI=" + encodeURIComponent(applicationSelection.iri), function(data) {
                $scope.services = data.services;
            });
            return true;    // to enable angular ng-show
        };
        $scope.serviceSelection = "";
        $scope.getServiceMetrics = function(serviceSelection) {
            $.get(BASE_URL + "/explorer/" + EXP_ID + "/services/iri/attributes?IRI=" + encodeURIComponent(serviceSelection.iri), function(data) {
                $scope.serviceMetrics = data.attributes;
            });
            return true;    // to enable angular ng-show
        };
        // clear the canvas before redrawing !!!! find a better way to allow for interactivity
        d3.select('#chart4 svg').remove();
        d3.select('#chart5 svg').remove();
        d3.select('#chart6 svg').remove();

        $scope.timeSeries = function(serviceMetricSelection, serviceSelection, activitySelection) {
            $('#qosChart1 svg').height(CHART_HEIGHT);
            //d3.json("json/tst.json", function(error, data) {
            d3.json(BASE_URL + "/explorer/" + EXP_ID + "/attributes/series/qos/highlight/activities?attrID=" + serviceMetricSelection.metricID + "&IRI=" + partIRI + "&actLabel=" + encodeURIComponent(activitySelection.label), function(error, data) {
//                console.log(data);
                nv.addGraph(function() {
                    var chart = nv.models.lineChart()
                            .x(function(d) {
                                return d.timestamp;
                            })
                            .y(function(d) {
                                return d.value / 100;
                            })
                            .useInteractiveGuideline(true)
                            .isArea(true);
                    //.forceY([0]);
                    chart.xAxis
                            .showMaxMin(true)
                            .tickFormat(function(d) {
                                return d3.time.format('%X')(new Date(d));
                            });
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