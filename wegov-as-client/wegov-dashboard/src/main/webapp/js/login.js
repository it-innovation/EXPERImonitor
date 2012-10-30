window.lang = new jquery_lang_js();

$(document).ready(function(){
//	createCookie("wegovlanguage","de");
//	console.log(window.location);
	
//	console.log($.sha256('asd'));
	
	// Check for failed login
	var loginResult = getUrlVars()["result"];
	if (loginResult == "error") {
//		console.log("Authentication failed");
		$("#loginerror p").text("Something went wrong... Try again?");
	}
	
	// Username in login box
	var usernameFromCookie = readCookie("wegovusername");
	if (usernameFromCookie != null) {
		$("#login").val(usernameFromCookie);
		$("#password").focus();
	} else {
		$("#login").focus();		
	}
	
	// Language
	var languageFromCookie = readCookie("wegovlanguage");
	if (readCookie("wegovlanguage") != null) {
		window.lang.change(languageFromCookie);
		$("#langChanger p").text("I speak English");
	}
	$("#langChanger p").click(function(event){
		event.preventDefault();
		var text = $(this).text();
		
		if (text == "Ich spreche Deutsch") {
			$(this).text("I speak English");
			window.lang.change('de');
			createCookie("wegovlanguage","de");
//			window.location.replace(window.location.href + "?lang=de")
		} else {
			$(this).text("Ich spreche Deutsch");
			window.lang.change('en');
			eraseCookie("wegovlanguage");
		}
		
	});	
	
	// Login inputs
	$("#loginForm input").bind('keypress', function(e){
		if (e.keyCode == 13) {
			$("#loginButton").trigger('mousedown');
			$("#loginButton").trigger('mouseup');
		}
	});
		
	//	Login button
	$("#loginButton").mouseup(function(event) {
//		event.preventDefault();
		$("#loginerror p").text("");
		var username = $("#login").val();
		
		if (username.length < 1) {
			$("#loginerror p").text("Please enter your username");
			$(this).css('background', "#E99520 url('./img/loginbtn_bg.png') repeat-x");
			return false;
		}
		
		eraseCookie("wegovusername");
		createCookie("wegovusername", username);
		
		var password = $("#password").val();
		if (password.length < 1) {
			$("#loginerror p").text("Please enter your password");
			$(this).css('background', "#E99520 url('./img/loginbtn_bg.png') repeat-x");
			return false;
		}
		
//		var passwordHash = $.sha256(password);
//		var passwordHash = $.encoding.digests.hexSha1Str(password).toLowerCase();
//		console.log(passwordHash);
		
//		$.get(
//				'/usercheck/quick.html',
//				{username: username, passwordhash: passwordHash},
//				function(data){
////					console.log(data);
//					if (data == "success") {
////						console.log("submitting form");
//						$(this).css('background', "#E99520 url('./img/loginbtn_bg.png') repeat-x");
//						$("#loginForm").submit();
//					} else {
////						console.log("try again");
//						$("#loginerror p").text("Something went wrong... Try again?");
//					}
//					
//				});
		
		$("#loginForm").submit();
		$(this).css('background', "#E99520 url('./img/loginbtn_bg.png') repeat-x");
		
	});
	$("#loginButton").mousedown(function(event) {
//		event.preventDefault();
//		console.log("Mouse down");
		$(this).css('background', "#E99520 url('./img/loginbtn_bg_inv.png') repeat-x");
		
	});
		
	// Translate
	window.lang.run();

});

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