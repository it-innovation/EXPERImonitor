var counter = 0;

$(document).ready(function() {
        
//    poll();


    $(".clientitem").click(function(){
        $(".clientlist .clientitem").removeClass('active');
        $(this).addClass('active');
        
        $(".clientdetails").empty();

        $(".clientdetails").append('<h6>Client Summary</h6>');
        $(".clientdetails").append('<p class="noextrawhitespace">Name: ' + $(this).text() + '</p>');
        $(".clientdetails").append('<p>Connected at: ' + randomDate(new Date(2012, 0, 1), new Date()) + '</p>');
//        $(".clientdetails").append('<h6>Observation capabilities</h6>');
        var detailsRow = $('<div class="row"></div>').appendTo(".clientdetails");
        
        var entitiesDivWrapper = $('<div class="four columns nopaddingright"></div>').appendTo(detailsRow);
        var entitiesDiv = $('<div class="capabilitieslistbg"></div>').appendTo(entitiesDivWrapper);
        entitiesDiv.append('<p><strong>Entities</strong></p>');
        entitiesDiv.append('<p class="noextrawhitespace active entitiesitem">Local network</p>');
        entitiesDiv.append('<p class="noextrawhitespace entitiesitem">Content database</p>');
        
        var attributesDivWrapper = $('<div class="four columns nopaddingright"></div>').appendTo(detailsRow);
        var attributesDiv = $('<div class="capabilitieslistbg"></div>').appendTo(attributesDivWrapper);
        attributesDiv.append('<p><strong>Attributes</strong></p>');
        attributesDiv.append('<p class="noextrawhitespace entitiesitem">Packet loss</p>');
        attributesDiv.append('<p class="noextrawhitespace active entitiesitem">Latency</p>');
        
        var infoDivWrapper = $('<div class="four columns"></div>').appendTo(detailsRow);
        var infoDiv = $('<div class="capabilitieslistbg"></div>').appendTo(infoDivWrapper);
        infoDiv.append('<p><strong>Info</strong></p>');
        infoDiv.append('<p class="noextrawhitespace">From this day forward, Flight Control will be known by two words: Tough means we are forever accountable for what we do or what we fail to do. We will never again compromise our responsibilities. Every time we walk into Mission Control we will know what we stand for. Competent means we will never take anything for granted. </p>');

        $('.entitiesitem').click(function(){
            $(this).parents('.capabilitieslistbg').children('.entitiesitem').removeClass('active');
            $(this).addClass('active');
        });
    });
    
    $(".metricgenitem").click(function(){
        $(".metricgenlist .metricgenitem").removeClass('active');
        $(this).addClass('active');
        
        var md = $(".metricgendetails");
        md.empty();
        md.append('<div id="testjqplot"></div>');
        $.jqplot ('testjqplot', [[3,7,9,1,4,6,8,2,5]]);        
        md.append('<h6>Metrics Summary</h6>');
        md.append('<p>For: ' + $(this).html().replace(' <span>', ', entity: ').replace('</span>', '.') + '</p>');
        
        var attributeDivWrapper = $('<div class="row"></div>').appendTo(md);
        var ad = $('<div class="twelve columns attributediv"></div>').appendTo(attributeDivWrapper);
        ad.append('<p class="header">Latency</p>');
        var dataDivWrapper = $('<div class="row"></div>').appendTo(ad);
        var dataDivText = $('<div class="four columns"></div>').appendTo(dataDivWrapper);
        var dataDivGraphsAndHistory = $('<div class="eight columns"></div>').appendTo(dataDivWrapper);
        dataDivText.append('<p>Last metric report: ' + randomDate(new Date(2012, 0, 1), new Date()) + '</p>');
        dataDivText.append('<p>Total number of reports: ' + (Math.floor(Math.random()*11)) + '</p>');
        dataDivGraphsAndHistory.append('<dl class="tabs"><dd class="active"><a href="#simple1">Graph view</a></dd><dd><a href="#simple2">Historical data</a></dd></dl>');
        var tabsWrapper = $('<ul class="tabs-content"></ul>').appendTo(dataDivGraphsAndHistory);
        var graphTab = $('<li class="active" id="simple1Tab"></li>').appendTo(tabsWrapper);
//        graphTab.append('<div id="testjqplot"></div>');
        var historyTab = $('<li id="simple2Tab"></li>').appendTo(tabsWrapper);
        historyTab.append('<p>Data here!</p>');
//        $.jqplot ('testjqplot', [[3,7,9,1,4,6,8,2,5]]);


    });    
    
    $(".clientlist .clientitem").first().trigger('click');
    $(".metricgenlist .metricgenitem").first().trigger('click');
    
    
});

function poll(){
    if (counter < 10) {
        var dataContainer = $('#appendhere');
        $.ajax({
            url: 'sample/getdata/do.json',
            dataType: 'json',	
            type: 'GET',
            contentType: "application/json; charset=utf-8",
            error: function(){
                dataContainer.append("<p>Something went wrong, try again?</p>");
            },
            success: function(data) {
                counter++;
                $("#reportsCounter").text(counter);
                console.log(data);
                if (data !== null) {
                    dataContainer.append('<p class="datarow">measurement: <strong>' + data.value + '</strong>, timestamp: <strong>' + data.timeStamp + '<strong></p>');
                } else {
                    dataContainer.append('<p class="datarow dataerror">Server error, check logs.</p>');
                }
            },
            complete: poll,
            timeout: 30000
        });    
    }
    

}

function randomDate(start, end) {
    return new Date(start.getTime() + Math.random() * (end.getTime() - start.getTime()))
}

(function($){  
    $(function(){
        $(document).foundationMediaQueryViewer();
    
        $(document).foundationAlerts();
        $(document).foundationAccordion();
        $(document).tooltips();
        $('input, textarea').placeholder();
    
    
    
        $(document).foundationButtons();
    
    
    
        $(document).foundationNavigation();
    
    
    
        $(document).foundationCustomForms();
    
    
    
      
        $(document).foundationTabs({
            callback:$.foundation.customForms.appendCustomMarkup
        });
      
    
    
    
//        $("#featured").orbit();
    
    
    // UNCOMMENT THE LINE YOU WANT BELOW IF YOU WANT IE8 SUPPORT AND ARE USING .block-grids
    // $('.block-grid.two-up>li:nth-child(2n+1)').css({clear: 'left'});
    // $('.block-grid.three-up>li:nth-child(3n+1)').css({clear: 'left'});
    // $('.block-grid.four-up>li:nth-child(4n+1)').css({clear: 'left'});
    // $('.block-grid.five-up>li:nth-child(5n+1)').css({clear: 'left'});
    });
  
})(jQuery);
