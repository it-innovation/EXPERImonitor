/*

$(document).ready(function() {
    var plot1 = $.jqplot('test', [new Array(1)], {
        title: 'Live Random Data',
        series: [
            {
            yaxis: 'y2axis',
            label: '',
            showMarker: false,
            fill: false,
            neighborThreshold: 3,
            lineWidth: 2.2,
            color: '#0571B6',
            fillAndStroke: true}
        ],
        axes: {
            xaxis: {
                renderer: $.jqplot.DateAxisRenderer,
                tickOptions: {
                    formatString: '%H:%M:%S'
                },
                numberTicks: 10
            },
            y2axis: {
                min: 100,
                max: 150,
                tickOptions: {
                    formatString: '%.2f'
                },
                numberTicks: 15
            }
        },
        cursor: {
            zoom: false,
            showTooltip: false,
            show: false
        },
        highlighter: {
            useAxesFormatters: false,
            showMarker: false,
            show: false
        },
        grid: {
            gridLineColor: '#333333',
            background: 'transparent',
            borderWidth: 3
        }
    });

    var myData = [];
    var x = (new Date()).getTime() - 101000;
    var y;
    var i;
    for ( i = 0; i < 100; i++) {
        x += 1000;
        y = Math.floor(Math.random() * 100);
        myData.push([x, y]);
    }

    plot1.series[0].data = myData;
    plot1.resetAxesScale();
    plot1.axes.xaxis.numberTicks = 10;
    plot1.axes.y2axis.numberTicks = 15;
    plot1.replot();

    function updateSeries() {
        myData.splice(0, 1);
        x = (new Date()).getTime();
        y = Math.floor(Math.random() * 100);
        
        myData.push([x, y]);

        plot1.series[0].data = myData;
        plot1.resetAxesScale();
        plot1.axes.xaxis.numberTicks = 10;
        plot1.axes.y2axis.numberTicks = 15;
        plot1.replot();
    }

    window.setInterval(updateSeries, 1000);
});

*/


$(document).ready(function() {
    
    
    var theAttributeDescription = "Very simple measurement of total bytes used http://static.ddmcdn.com/gif/ram-ch.jpg";
    
    $.each(theAttributeDescription.split(" "), function(index, value){
        console.log(value);
        
        if ( value.indexOf('http') == 0 && ( ( value.match('.jpg$') == '.jpg') || ( value.match('.png$') == '.png' ) ) ) {
            console.log('Image url: ' + value);
            $("#test").append("<img src='" + value + "'>");
        }
        
    });
    
    return;
    
    var test = new Date(1350397100431);
    
    console.log(test.toUTCString());
    
    return;
    
//  var line1=[['2008-08-12 4:00PM',4], ['2008-09-12 4:00PM',6.5], ['2008-10-12 4:00PM',5.7], ['2008-11-12 4:00PM',9], ['2008-12-12 4:00PM',8.2]];
//  var plot1 = $.jqplot('test', [line1], {
//    title:'Default Date Axis',
//    axes:{
//        xaxis:{
//            renderer:$.jqplot.DateAxisRenderer
//        }
//    },
//    series:[{lineWidth:4, markerOptions:{style:'square'}}]
//  });  
    
//    var plotdata = [[['2008-09-30 4:00PM',4], ['2008-10-30 4:00PM',6.5], ['2008-11-30 4:00PM',5.7]]];
    var plotdata = [[[1350393940976,5020128], [1350393942479,5379472], [1350393947496,5561216]]];
//    var plotdata = [[[1,4], [2,6.5], [3,5.7]]];
    var jqplotGraph = $.jqplot ('test', plotdata, {
        axes:{
            xaxis:{
                renderer:$.jqplot.DateAxisRenderer,
                tickOptions:{formatString:'%b %#d<br/> %T'}
            }
        }
    }); 
    
//    console.log(jqplotGraph);

//    var newPlotdata = [[['2008-09-30 4:00PM',4], ['2008-10-30 4:00PM',6.5], ['2008-11-30 4:00PM',5.7], ['2008-12-30 4:00PM',9]]];
//    var newPlotdata = [[1,4], [2,6.5], [3,5.7], [4,9]];
    var newPlotdata = [[1350393940976,5020128], [1350393942479,5379472], [1350393947496,5561216], [1350393948196,7561216]];
    
    console.log(plotdata);
    console.log(newPlotdata);
    
    jqplotGraph.series[0].data = newPlotdata;
    
    console.log(jqplotGraph);
    
    jqplotGraph.resetAxesScale();
    
    jqplotGraph.replot();

    
});