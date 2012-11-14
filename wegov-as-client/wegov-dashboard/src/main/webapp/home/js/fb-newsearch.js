/////////////////////////////////////////////////////////////////////////////////////////////
// Facebook search page
var fbDebug = false;
var addHover = false;
var fbLoginStatus;

function fbLoginStatusChanged(status, accessToken, expiresIn) {
	fbLoginStatus = status;
	var statusMessage = getStatusMessage(status);

	if (status == "LOGGED_OUT") {
		//showFbLogin();
		login();
		return;
	}
	else if (status == "LOGGED_IN_EXTRA_PERMISSIONS_REQUIRED") {
		updateGroupPostsStatus('<p>' + statusMessage + '</p>');
		return;
	}
	else if (status == "POSTS_RECEIVED") {
		updateGroupPostsStatus('');
		return;
	}

	if (fbDebug) {
		updateGroupPostsStatus('<p>' + statusMessage + '</p><p>Access token: ' + accessToken + '</p><p>Expires in: ' + expiresIn + '</p>');
	}

	if (status == "LOGGED_IN_CHECKING_PERMISSIONS") {
		if (! fbDebug) {
			//updateGroupPostsStatus('<p>Checking Facebook login status...OK</p><p>Getting group info...</p>');
			updateGroupPostsStatus('<p>Checking Facebook login status...OK</p><p>Checking permissions...</p>');
		}
		//getGroupInfoAllWidgets();
		//getGroupPostInfoAllWidgets();
	}
	else if (status == "LOGGED_IN_TOKEN_EXTENDED") {
		//fbLoginStatusChanged("LOGGED_IN_TOKEN_EXTENDED_GETTING_POSTS", accessToken, expiresIn);
		if (! fbDebug) {
			updateGroupPostsStatus('<p>Checking Facebook login status...OK</p><p>Checking permissions...OK</p>');
		}
		clientAccessToken = accessToken;
		clientAccessTokenExpiryTime = expiresIn;
		console.log('Access token: ' + accessToken);
		console.log('Expires in: ' + expiresIn);

		// Now user is logged in, we might have set the search term, which can now be validated
		//getValidatedSearchTerms(false);
		enableFbRequests();
	}
	else if (status == "LOGGED_IN_PERMISSIONS_FAILED") {
		updateGroupPostsStatus('<p>Checking Facebook login status...OK</p><p>Checking permissions...FAILED!</p>');
	}
	else if (status == "LOGGED_IN_TOKEN_EXTENDED_GETTING_POSTS") {
		if (! fbDebug) {
			updateGroupPostsStatus('<p>Checking Facebook login status...OK</p><p>Getting group info...OK</p><p>Collecting posts...</p>');
		}
		//getGroupPostsAllWidgets();
		//getGroupPostCommentsAllWidgets();
	}
}

function showFbLogin() {
	updateGroupPostsStatus(	'<p class="middleTextWidgetWithHeaderOn">Please login to Facebook first:' +
							'<input type="button" value="Login" id="fb-auth"/></p>');
	$('#fb-auth').click(login);
}

function updateGroupPostsStatus(html) {
	//$('.groupPostsStatus').html(html);
	//$("#resultsPanel").html(html);
	$("#fbStatus").html(html);
}

var gettingGroupInfo = false;
var groupIdValid = false;

function getGroupInfo(groupID, callback) {
	if (gettingGroupInfo) {
		console.log('WARNING: already getting Facebook group info');
		return false;
	}

	gettingGroupInfo = true;
	console.log('Getting Facebook group info for id: ' + groupID);

	var url = groupID + "&metadata=true";
	console.log(url);

	//FB.api(url, setFbGroupInfo);

	FB.api(url, function(response) {
		console.log("getGroupInfo(): calling setFbGroupInfo with callback: " + callback);
		setFbGroupInfo(response, callback);
	});

}

function testFbInfo() {
	var response = {id: "7303343452", name: "David Cameron", type: "page",
		link: "http://www.facebook.com/DavidCameron",
		picture: "http://profile.ak.fbcdn.net/hprofile-ak-ash2/174697_7303343452_153901066_s.jpg"};
	setFbGroupInfo(response);
}

function setFbGroupInfo(response, callback) {
	console.log("setFbGroupInfo starting");
	//setGroupPostsWidgetHeader(myDiv, response.name);
	//setGroupPostsWidgetFooter(myDiv, response.type);
	$("#fbGroupInfo").empty();
	console.log("response: " + JSON.stringify(response));

	if (response == null) {
		gettingGroupInfo = false;
		validating = false;
		return;
	}

	if (!response) {
		//alert("ERROR: could not locate Facebook group id: " + $("#searchTerms").val());
		console.log("ERROR: could not locate Facebook group id: " + $("#searchTerms").val());
		alert("ERROR: could not locate Facebook group id: " + $("#searchTerms").val());
		gettingGroupInfo = false;
		validating = false;
		return;
	}

	if (response.error) {
		alert(response.error.message);
		gettingGroupInfo = false;
		validating = false;
		return;
	}

	//console.log(JSON.stringify(response));

	var fbGroupDetails = $('<div id="fbGroupDetails"></div>').appendTo("#fbGroupInfo");
	fbGroupDetails.append('<p id="fbGroupInfoName" class="fbGroupInfoName">' + response.name + '</p>');
	//fbGroupDetails.append('<p>' + response.type + '</p>');
        var pageLink = "https://www.facebook.com/events/" + response.id;
	fbGroupDetails.append('<a href=' + pageLink + ' target="_blank">' + decodeURI(pageLink)+ '</a>');

	var fbGroupImage = $('<div id="fbGroupImage"></div>').appendTo("#fbGroupInfo");

	if (response.metadata.connections.picture) {
		fbGroupImage.append('<img src="' + response.metadata.connections.picture + '" />');
	}
//	else {
//		console.log("WARNING: response.picture is undefined. Trying new API call...");
//		var url = response.id + "/picture";
//		console.log(url);
//		FB.api(url, function(resp) {
//			console.log(resp);
//			if (resp) {
//				if (resp.indexOf("http") == 0) {
//					fbGroupImage.append('<img src="' +resp + '" />');
//				}
//				else {
//					console.log("Cannot get picture from response");
//				}
//			}
//		});
//	}

	$('<div class="clearfix"></div>').appendTo("#fbGroupInfo");

	setSearchTerms(response.id);
	setSearchLabel(response.type);

	searchTermsValid = true;
	gettingGroupInfo = false;
	validating = false;

	console.log("setFbGroupInfo finished");

	if (callback) {
		console.log('Calling callback: ' + callback);
		callback();
	}
}

var fbType;

function setSearchLabel(type) {
	fbType = type;
	var label = "Group";
	if (type == "page") {
		label = "Page";
	}
	else if ((type == "post") || (type == "link")) {
		label = "Post";
	}
	label += " ID/URL:";
	$("#searchTermsLabel").text(label);
}

function setSearchTerms(id) {
	$("#searchTerms").val(id);
}

function getFbSearchName(groupID, config) {
	console.log('Getting search name for id: ' + groupID );
	var searchName = "Facebook";

	if (config) {
		var whatToCollect = config["what.collect"];
		if (whatToCollect) {
			searchName += " " + whatToCollect;
			if (config.collectComments) {
				searchName += ", comments"; 
			}
		}
	}

	searchName += " collection from ";

	if (fbType) {
		searchName += fbType + " ";
	}
	else {
		searchName += "group ";
	}

	searchName += groupID;

	var groupName = $("#fbGroupInfoName").text();
	if (groupName != "") {
		searchName += " (" + groupName + ")";
	}
	console.log('Search name: ' + searchName);
	return searchName;
}

