
nv.addGraph(function() {
  var chart = nv.models.pieChart()
      .x(function(d) { return d.label; })
      .y(function(d) { return d.value; })
      .showLabels(true);

    d3.select("#chart svg")
        .datum(exampleData2())
        .transition().duration(350)
        .call(chart);

  return chart;
});

//Donut chart example
nv.addGraph(function() {
  var chart = nv.models.pieChart()
      .x(function(d) { return d.label; })
      .y(function(d) { return d.value; })
      .showLabels(true)     //Display pie labels
      .labelThreshold(.05)  //Configure the minimum slice size for labels to show up
      .labelType("percent") //Configure what type of data to show in the label. Can be "key", "value" or "percent"
      .donut(true)          //Turn on Donut mode. Makes pie chart look tasty!
      .donutRatio(0.35)     //Configure how big you want the donut hole size to be.
      ;

    d3.select("#chart2 svg")
        .datum(exampleData2())
        .transition().duration(350)
        .call(chart);

  return chart;
});

//Pie chart example data. Note how there is only a single array of key-value pairs.
function exampleData2() {
  return  [ 
        { 
          "label" : "Strongly disagree" ,
          "value" : 5
        } ,  
        { 
          "label" : "Disagree" ,
          "value" : 7
        } , 
        { 
          "label" : "Neutral" ,
          "value" : 21
        } ,
        { 
          "label" : "Agree" ,
          "value" : 27
        } ,
        { 
          "label" : "Strongly Agree", 
          "value" : 10
        }
  ];
}