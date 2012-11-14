// settings for wegov.it-innovation.soton.ac.uk
//var appId = '380488908645887';
//var clientSecret = 'fb035b32a62b18f1da8830b6c92f5fe5';

// settings for wegov-dev.it-innovation.soton.ac.uk
//var appId = '434907376542228';
//var clientSecret = '0b011a189219a025db7967eaab2325fb';

// settings for localhost
//var appId = '275750059166570';
//var clientSecret = '50567d250f129665e3e5c875c623689f';
var appId = '197483306942353';
var clientSecret = 'aaac5731761ddfd2c7dd0fa5335ebd1e';

function fbLoad(callback) {
    console.log("Loading Facebook functions");
    window.fbAsyncInit = function() {
            console.log("Facebook functions loaded");
            fbInit(callback);
    };

    (function() {
            var e = document.createElement('script');
            e.async = true;
            e.src = document.location.protocol
            + '//connect.facebook.net/en_US/all.js';
            document.getElementById('fb-root').appendChild(e);
    }());
}

function fbInit(callback) {
	console.log("Calling FB.init");
	FB.init({
			appId : appId,
			status : true,
			cookie : true,
			xfbml : true,
			oauth : true
	});
	
	if (callback) {
            callback();
	}
	else {
            console.log("WARNING: no callback for fbLoad/fbInit defined");
	}
}

var clientAccessToken = null;
var clientAccessTokenExpiryTime = null;

function getAccessToken() {
	console.log("Client access token = " + clientAccessToken);
	return clientAccessToken;
}

var accessTokenCallbacksWithArgs = new Array();
var gettingAccessToken = false;

function getAccessTokenThenExecCallback(callback, args) {
	accessTokenCallbacksWithArgs.push({callback: callback, args: args})
	console.log("Added access token callback " + accessTokenCallbacksWithArgs.length);
	if (! gettingAccessToken) {
		gettingAccessToken = true;
		console.log("Getting access token...");
		fbGetLoginStatus();
	}
	else {
		console.log("Already getting access token");
	}
}

function executeAccessTokenCallbacks() {
	console.log("executeAccessTokenCallbacks: " + accessTokenCallbacksWithArgs.length);
	for (var i=0; i<accessTokenCallbacksWithArgs.length; i++) {
		var obj = accessTokenCallbacksWithArgs[i];
		obj.callback.apply(undefined, obj.args);
	}

	clearAccessTokenCallbacks();
}

function clearAccessTokenCallbacks() {
	accessTokenCallbacksWithArgs = new Array(); //reset callbacks array
	gettingAccessToken = false;
}

function updateWidgetStatus(html) {
	console.log("updateWidgetStatus: " + html);
	if (accessTokenCallbacksWithArgs.length > 0) {
		var obj = accessTokenCallbacksWithArgs[0];
		var args = obj.args;
		var myContainer = args[0];
		var widget = args[1];
		var statusDiv = myContainer.find(".groupPostsStatus, .groupPostCommentsStatus");
		console.log(statusDiv);
		statusDiv.html(html);
	}
	else {
		console.log("updateWidgetStatus: no widgets currently registered for update");
	}
}

var gettingFbLoginStatus = false;

function fbGetLoginStatus() {
	if (gettingFbLoginStatus) {
		console.log("WARNING: fbGetLoginStatus: already getting login status");
		return;
	}
	gettingFbLoginStatus = true;
	fbLoginStatusChanged("CHECKING_LOGIN_STATUS");
	
	console.log("Calling FB.getLoginStatus");
	FB.getLoginStatus(fbLoginStatusCallback, true);
	//FB.Event.subscribe('auth.statusChange', fbLoginStatusCallback);


	//function getLoginStatus() {
	//	var authResponse;
	//	FB.getLoginStatus(function (response) {
	//		authResponse = response.authResponse;
	//	});
	//	return authResponse;
	//}
}

function fbLoginStatusCallback(response) {
	console.log("response.status: " + response.status)
	if (response.authResponse) {
		console.log("authResponse: " + response.authResponse);
		userLoggedIn(response.authResponse);
	} else {
		//user is not connected to your app or logged out
		userLoggedOut();
	}
}

function login() {
	FB.login(function(response) {
		if (response.authResponse) {
			userLoggedIn(response.authResponse);
		} else {
			//user cancelled login or did not grant authorization
			console.log("User cancelled login");
			gettingFbLoginStatus = false;
			clearAccessTokenCallbacks();
		}
	}, {
		scope : 'read_stream'
	});
}

function logout() {
	FB.logout(function(response) {
		userLoggedOut();
	});
}

function userLoggedIn(authResponse) {
	var accessToken = authResponse.accessToken;
	var expiresIn = authResponse.expiresIn;

	fbLoginStatusChanged("LOGGED_IN_CHECKING_PERMISSIONS", accessToken, expiresIn);

	FB.api('/me/permissions',
			function(response) {
				if (! response.data) {
					alert("Facebook server failure!");
					fbLoginStatusChanged("LOGGED_IN_PERMISSIONS_FAILED");
					gettingFbLoginStatus = false;
					return;
				}
				if (! response.data[0].read_stream) {
					fbLoginStatusChanged("LOGGED_IN_EXTRA_PERMISSIONS_REQUIRED");
					login();
					gettingFbLoginStatus = false;
				}
				else {
					extendTokenExpiry(accessToken);
				}
			});
}

function userLoggedOut() {
	console.log("userLoggedOut");
	fbLoginStatusChanged("LOGGED_OUT", null, null);
}

function extendTokenExpiry(accessToken) {
	var expiresIn = '';

	var extend_token_url = 'https://graph.facebook.com/oauth/access_token?client_id=' + appId
		+ '&client_secret=' + clientSecret
		+ '&grant_type=fb_exchange_token&fb_exchange_token=' + accessToken;

	$.get(extend_token_url, function(data){
		var fullAccessTokenArray = data.split('&');
		for (var i=0; i<fullAccessTokenArray.length; i++) {
			var frag = fullAccessTokenArray[i];
			var nameAndValue = getNameValueArray(frag);

			var name = nameAndValue[0];
			var val = nameAndValue[1];

			if (name == 'access_token') {
				accessToken = val;
			}
			else if (name == 'expires') {
				expiresIn = val;
			}
		}

		gettingFbLoginStatus = false;
		fbLoginStatusChanged("LOGGED_IN_TOKEN_EXTENDED", accessToken, expiresIn);
	});
}

function getNameValueArray(nameAndVal) {
	return nameAndVal.split('=');
}

function getStatusMessage(status) {
	var message = "Unknown";

	if (status == "LOGGED_IN_CHECKING_PERMISSIONS") {
		message = "Logged In. Checking permissions...";
	}
	else if (status == "LOGGED_IN_EXTRA_PERMISSIONS_REQUIRED") {
		message = "Logged In. Please add requested permission to continue";
	}
	else if (status == "LOGGED_IN_TOKEN_EXTENDED") {
		message = "Logged In. Token extended.";
	}
	else if (status == "LOGGED_IN_TOKEN_EXTENDED_GETTING_POSTS") {
		message = "Logged In. Token extended. Getting posts...";
	}
	else {
		message = status;
	}

	return message;
}
