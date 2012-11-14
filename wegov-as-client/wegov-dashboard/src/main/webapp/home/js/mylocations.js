function LocationWidget(widget) {
	this.widget = widget;
	this.myDiv = null;
	
	this.init = function(widget) {
		myDiv = $("<div class=\"portlet\" id=\"" + widget.id + "\"></div>").appendTo("#" + widget.columnName);
		
		var widgetHeaderDiv = $("<div class=\"widgetHeaderDiv\"></div>").appendTo(myDiv);
		$("<img class=\"widgetLogo\" src=\"img/google_logo.jpg\" alt=\"Source: Google Maps\"/>").appendTo(widgetHeaderDiv);
		var widgetHeaderTextDiv = $("<div class=\"widgetHeaderTextDiv\"></div>").appendTo(widgetHeaderDiv);
		$("<h2 class=\"widgetHeader\">" + widget["name"] + "</h2>").appendTo(widgetHeaderTextDiv);
		widgetHeaderDiv.append('<div class="clearfix"></div>');
		var widgetHeaderExtraTextDiv = $("<div class=\"widgetHeaderExtraTextDiv\"></div>").appendTo(widgetHeaderDiv);
		var extraText = $("<p></p>").appendTo(widgetHeaderExtraTextDiv);

		var myContainer = $("<div class=\"widgetContent\"></div>").appendTo(myDiv);	
			var gmapId = "gmap" + widget["id"];
			myContainer.append("<div id=\"" + gmapId + "\" class=\"gmap\"></div>");
		
		var widgetPostContent = $("<div class=\"widgetPostContent\"></div>").appendTo(myDiv);
		
	};
	this.refresh = function(widget) {
		
	};
}