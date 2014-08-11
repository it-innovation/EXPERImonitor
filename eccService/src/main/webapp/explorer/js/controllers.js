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
                $('#chart2 svg').remove();
                d3.select('#chart3 svg').remove();
                $('#chart1').append('<svg class="large-6 text-center columns"></svg>');
                $('#chart2').append('<svg class="large-6 text-center columns"></svg>');
                $('#chart3').append('<svg class="large-6 text-center columns"></svg>');
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
    $http.get(BASE_URL + EXP_ID + "/participants").success(function(data) {
        $scope.participants = data.participants;
    });
    $http.get(BASE_URL + EXP_ID + "/participants/iri?IRI=" + partIRI).success(function(data) {
        $scope.whichPeople = data.name;
        // TODO: need to fix routeparams to show current selction
        $scope.participantSelection = data.name;
    });
//    // get activities
    $http.get(BASE_URL + EXP_ID + "/participants/iri/activities/summary?IRI=" + partIRI).success(function(data) {
        $scope.activities = data.activities;
    });
    // get activity instances
    $http.get(BASE_URL + EXP_ID + "/participants/iri/activities?IRI=" + partIRI).success(function(data) {
        $scope.activityInstances = data.activities;
        $scope.numActivities = data.activityTotal;
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
    // qos line-area chart   
    d3.json("json/ts.json", function(error, data) {
        nv.addGraph(function() {
            var chart = nv.models.lineChart()
                .x(function(d) { return d.date; })
                .y(function(d) { return d.value; })
                .useInteractiveGuideline(true)
                .isArea(true)
                .forceY([0]);
            chart.xAxis
                .showMaxMin(true)
                .tickFormat(function(d) { return d3.time.format('%x')(new Date(d)); });
            chart.yAxis
                .axisLabel('Response Time(ms)')
                .tickFormat(d3.format(',.2f'));
            d3.select('#qosChart1 svg')
                .datum(data)
                .transition().duration(500)
                .call(chart);
            nv.utils.windowResize(chart.update);
            return chart;
        });
    });
    
    // BEGIN parallel sets viz
    var chart = d3.parsets()
        .dimensions(["QoE", "Activities", "Applications", "Services"]);

    var vis = d3.select('#qosChart2 svg')
        .attr("height", chart.height());

    var partition = d3.layout.partition()
        .sort(null)
        .size([chart.width(), chart.height() * 5 / 4])
        .children(function(d) { return d.children ? d3.values(d.children) : null; })
        .value(function(d) { return d.count; });

    var ice = false;

    function curves() {
      var t = vis.transition().duration(500);
      if (ice) {
        t.delay(1000);
        icicle();
      }
      t.call(chart.tension(this.checked ? .5 : 1));
    }

    d3.json("json/ps.json", function(error, data) {

        vis.datum(data).call(chart);

    });

    function iceTransition(g) {
      return g.transition().duration(1000);
    }

    function ribbonPath(s, t, tension) {
      var sx = s.node.x0 + s.x0,
          tx = t.node.x0 + t.x0,
          sy = s.dimension.y0,
          ty = t.dimension.y0;
      return (tension === 1 ? [
          "M", [sx, sy],
          "L", [tx, ty],
          "h", t.dx,
          "L", [sx + s.dx, sy],
          "Z"]
       : ["M", [sx, sy],
          "C", [sx, m0 = tension * sy + (1 - tension) * ty], " ",
               [tx, m1 = tension * ty + (1 - tension) * sy], " ", [tx, ty],
          "h", t.dx,
          "C", [tx + t.dx, m1], " ", [sx + s.dx, m0], " ", [sx + s.dx, sy],
          "Z"]).join("");
    }

    function stopClick() { d3.event.stopPropagation(); }

    // Given a text function and width function, truncates the text if necessary to
    // fit within the given width.
    function truncateText(text, width) {
      return function(d, i) {
        var t = this.textContent = text(d, i),
            w = width(d, i);
        if (this.getComputedTextLength() < w) return t;
        this.textContent = "…" + t;
        var lo = 0,
            hi = t.length + 1,
            x;
        while (lo < hi) {
          var mid = lo + hi >> 1;
          if ((x = this.getSubStringLength(0, mid)) < w) lo = mid + 1;
          else hi = mid;
        }
        return lo > 1 ? t.substr(0, lo - 2) + "…" : "";
      };
    }

    d3.select("#file").on("change", function() {
      var file = this.files[0],
          reader = new FileReader;
      reader.onloadend = function() {
        var csv = d3.csv.parse(reader.result);
        vis.datum(csv).call(chart
            .value(csv[0].hasOwnProperty("Number") ? function(d) { return +d.Number; } : 1)
            .dimensions(function(d) { return d3.keys(d[0]).filter(function(d) { return d !== "Number"; }).sort(); }));
      };
      reader.readAsText(file);
    });

    // END parallel sets viz
    
}]);