$(document).ready(function() {
	// Home link
	$("#invislink").click(function() {
		window.location = "./index.html";
	});

	// username
	$.ajax({
		url:'/home/getnameandrole/do.json',
		type: 'GET',
		success: function(userdetails) {
//			console.log(userdetails);
			var mySettingsPopupControl = $("#username");
			mySettingsPopupControl.text(userdetails["name"] + ": Settings");
//			if (userdetails["role"] == "admin") {
//				mySettingsPopupControl.text(userdetails["name"] + " (" + userdetails["role"] + ")");
//			} else {
//				mySettingsPopupControl.text(userdetails["name"]);
//			}
			jQuery.data(mySettingsPopupControl[0], "userdetails", userdetails);
		}
	});

	// Close dialogs on escape
	$(document).keyup(function(e) {
        if (e.which == 27) {
            $('.closed-by-escape').hide();
        }
    });

	// TODO clean this up
	$("#newLocationNameCancelButton, #addNewLocationDialogNameCancelButton, #cancelSchedulerButton, #closeScheduledJobsButton").mouseup(function(e){
		$("#name-location-dialog-holder, #dialog-overlay, #add-new-location-dialog-holder, #schedulerPopup, #scheduledJobsPopup").hide();
	});

	$("#dialog-overlay").click(function(e){
		$('.closed-by-escape').hide();
	});
});


function trimMessage(message, maxLength) {
	var trimmedMessage = message;

	if (message && (message.length > maxLength)) {
		trimmedMessage = message.substring(0, maxLength) + '...';
	}

	return trimmedMessage;
}

function createCookie(name,value,days) {
	var expires = "";

	if (days) {
	        var date = new Date();
	        date.setTime(date.getTime()+(days*24*60*60*1000));
	        expires = "; expires="+date.toGMTString();
	}

	document.cookie = name + "=" + value + expires + "; path=/";
}

function readCookie(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
	        var c = ca[i];
	        while (c.charAt(0)==' ') c = c.substring(1,c.length);
	        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}

function eraseCookie(name) {
	createCookie(name,"",-1);
}

function popupBackground(boxId){

	var viewportwidth;
	var viewportheight;

	 if (typeof window.innerWidth != 'undefined')
	 {
	      viewportwidth = window.innerWidth,
	      viewportheight = window.innerHeight
	 }
	 else if (typeof document.documentElement != 'undefined'
	     && typeof document.documentElement.clientWidth !=
	     'undefined' && document.documentElement.clientWidth != 0)
	 {
	       viewportwidth = document.documentElement.clientWidth,
	       viewportheight = document.documentElement.clientHeight
	 }
	 else
	 {
	       viewportwidth = document.getElementsByTagName('body')[0].clientWidth,
	       viewportheight = document.getElementsByTagName('body')[0].clientHeight
	 }

	var formBox = $('#' + boxId);
	var formBoxHeight = formBox.height();
	var formBoxWidth = formBox.width();

	var formBoxX = (viewportwidth / 2) - (formBoxWidth / 2);
/* 	var formBoxY = (viewportheight / 2) - (formBoxHeight / 2) - 200; */
	var formBoxY = 50;


    $('#dialog-overlay').show();
    $('#' + boxId).css({top:formBoxY, left:formBoxX}).show();
}

function loadPageVar(sVar) {
	return unescape(window.location.search.replace(new RegExp("^(?:.*[&\\?]"
			+ escape(sVar).replace(/[\.\+\*]/g, "\\$&")
			+ "(?:\\=([^&]*))?)?.*$", "i"), "$1"));
}
