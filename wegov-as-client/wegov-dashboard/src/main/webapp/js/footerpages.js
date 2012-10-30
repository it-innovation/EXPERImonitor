window.lang = new jquery_lang_js();

$(document).ready(function() {
	
	$("#invislink").click(function() {
		window.location = "./login.html";
	});
	
	// Language
	var languageFromCookie = readCookie("wegovlanguage");
	if (readCookie("wegovlanguage") != null) {
		window.lang.change(languageFromCookie);
		$("#langChanger p").text("I speak English");
	}
	$("#langChanger p").click(function(event) {
		event.preventDefault();
		var text = $(this).text();

		if (text == "Ich spreche Deutsch") {
			$(this).text("I speak English");
			window.lang.change('de');
			createCookie("wegovlanguage", "de");
		} else {
			$(this).text("Ich spreche Deutsch");
			window.lang.change('en');
			eraseCookie("wegovlanguage");
		}

	});
	
	window.lang.run();
	
});