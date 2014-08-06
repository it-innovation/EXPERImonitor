d3.json(BASE_URL + EXP_ID + "/attributes/Ease%20of%20use%3A%20Ski%20lift%20app/histogram", function(data) {

    nv.addGraph(function() {
      var chart = nv.models.discreteBarChart()
          .x(function(d) { return d.label; })    //Specify the data accessors.
          .y(function(d) { return d.count; })
          .staggerLabels(false)    //Too many bars and not enough room? Try staggering labels.
          .tooltips(false)        //Don't show tooltips
          .showValues(true)       //...instead, show the bar value right on top of each bar.
          .transitionDuration(350)
          ;
      chart.yAxis
            .tickFormat(d3.format(',.f'));

      d3.select('#chart1 svg')
          .datum( data )
          .call(chart);

      nv.utils.windowResize(chart.update);

      return chart;

    });

});