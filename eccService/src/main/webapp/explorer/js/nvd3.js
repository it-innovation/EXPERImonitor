d3.json('participants-qoe.json', function(data) {

  nv.addGraph(function() {
    var chart = nv.models.multiBarHorizontalChart()
        .x(function(d) { return d.label; })
        .y(function(d) { return d.value; })
        .margin({top: 30, right: 40, bottom: 25, left: 6})
        .showValues(false)
        .tooltips(true)
        .barColor(d3.scale.category20().range())
        .transitionDuration(250)
        .stacked(true)
        .showControls(true);

    chart.yAxis
        .tickFormat(d3.format(',.f'));

    d3.select('#chart3 svg')
        .datum(data)
        .call(chart);

    nv.utils.windowResize(chart.update);

    chart.dispatch.on('stateChange', function(e) { nv.log('New State:', JSON.stringify(e)); });

    return chart;
  });

});