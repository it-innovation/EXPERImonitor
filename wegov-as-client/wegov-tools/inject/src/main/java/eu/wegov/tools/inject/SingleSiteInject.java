/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2011
//
// Copyright in this library belongs to the University of Southampton
// University Road, Highfield, Southampton, UK, SO17 1BJ
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
//	Created By :			Ken Meacham
//	Created Date :			2011-12-19
//	Created for Project :	WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.tools.inject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
//import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

import eu.wegov.coordinator.Configuration;
import eu.wegov.coordinator.dao.data.WegovPostItem;
import eu.wegov.coordinator.dao.data.WegovSNS;
import eu.wegov.coordinator.utils.Util;
import eu.wegov.tools.WegovTool;

public abstract class SingleSiteInject implements WegovInject {

	protected WegovTool tool = null;
	protected Configuration configuration = null;
	protected WegovSNS sns;
	protected Map<String, WegovSNS> snsMap = new HashMap<String, WegovSNS>();
	
	protected String site = null;
	//protected String aggregatorSources = null;
	protected String injectUrl = null;
	protected Map<String, String> injectParams = new HashMap<String, String>();

	protected String postOption;
	protected String postText;
	protected String postHashtags;
	protected String inReplyToUserId;
	protected String inReplyToPostId;

	//protected String whatCollect;
	//protected String whatWordsAll;
	//protected String whatWordsExactPhrase;
	//protected String whatWordsAny;
	//protected String whatWordsNone;
	//protected String whatWordsHashtags;
	
	//protected String whatPeopleFromAccounts;
	//protected String whatPeopleToAccounts;
	//protected String whatPeopleMentioningAccounts;
	
	//protected String whatPeopleFromGroups;
	//protected String whatPeopleToGroups;
	//protected String whatPeopleMentioningGroups;
	
	//protected String whatWordsNameIDorTag;
	//protected String whatWordsNameContains;

	/*
	protected String whatDatesOption;
	protected String whatDatesSince;
	protected String whatDatesUntil;
	
	//protected String location; //deprecated
	protected String locationOption;
	protected String locationUseapi;
	protected String locationAppendtosq;

	protected String locationCity;
	protected String locationRegion;
	protected String locationCountryName;
	protected String locationCountryCode;
	protected String locationLat;
	protected String locationLong;
	protected String locationRadius;
	protected String locationRadiusUnit;

	protected String language;
	
	protected String resultsType;
	protected String resultsMaxOption;
	protected String resultsMax;
	protected String resultsMaxPerPage;
	protected String resultsStoreInDB;
	protected String resultsKeepRawData;
	*/
	
	protected Util util = new Util();
	
	protected String authMethod = null;
	
	// OAuth tokens
	protected String oauthConsumerKey;
	protected String oauthConsumerSecret;
	protected String oauthConsumerAccessToken;
	protected String oauthConsumerAccessTokenSecret;

	protected String userIdSeparator = "";

	public SingleSiteInject(WegovTool tool, String site) throws Exception {
		this.tool = tool;
		this.configuration = tool.getConfiguration();
		this.site = site;

		System.out.println("\nNew inject (post): " + site);
		setSNS();
		
        setSecurityParams();
		setInjectParams();
		setupInject();
	}

	protected WegovSNS getOrCreateSNS(String snsID, String snsName, String apiUrl, String favicon, int outputOfRunID) throws Exception {
        WegovSNS sns;

        ArrayList<?> snsArray = tool.getCoordinator().getDataSchema().getAllWhere(new WegovSNS(), "ID", snsID);
        
        if (snsArray.isEmpty()) {
        	sns = new WegovSNS(snsID, snsName, apiUrl, favicon, outputOfRunID);
            tool.getCoordinator().getDataSchema().insertObject(sns);
        }
        else {
        	sns = (WegovSNS) snsArray.get(0);
        }
        
        return sns;
	}
	
	// Override in subclass if necessary
	protected void setAuthMethod() {
		authMethod = "none"; // default;
	}
	
	private void setSecurityParams() throws Exception {
		setAuthMethod();
		
		System.out.println("Auth method: " + authMethod);
		
		if (authMethod.equals("oauth")) {
	        //WeGov Test application / wegovtest1 Twitter account (should be set in coordinator.properties for now)
			
	        //oauthConsumerKey = "bPE4bvcVbWgyr7h2VK3sw";
	        //oauthConsumerSecret = "LDM87QZ2gPREUQrxQSnznPMzfqxcJe66WD9BbhuX3E";
	        //oauthConsumerAccessToken = "380850453-A5s7OTPTN8cRw6y2Mwpc0MSvKwpgShz1Y0tG9mtQ";
	        //oauthConsumerAccessTokenSecret = "LuwM0p1OAGg64mzvhuDCLR57AC9gpTQR7iUBfULH9ZM";
			
			Properties properties = tool.getCoordinatorProperties();
			
			oauthConsumerKey = properties.getProperty("oauthConsumerKey");
			oauthConsumerSecret = properties.getProperty("oauthConsumerSecret");
			oauthConsumerAccessToken = properties.getProperty("oauthConsumerAccessToken");
			oauthConsumerAccessTokenSecret = properties.getProperty("oauthConsumerAccessTokenSecret");
			
			System.out.println("\nOAuth Consumer Key: " + oauthConsumerKey);
			System.out.println("OAuth Consumer Secret: " + oauthConsumerSecret);
			System.out.println("OAuth Consumer Access Token: " + oauthConsumerAccessToken);
			System.out.println("OAuth Consumer Access Token Secret: " + oauthConsumerAccessTokenSecret + "\n");
		}
	}

	private void setInjectParams() throws Exception {
		postText = configuration.getValueOfParameter("post.text");
		postOption = configuration.getValueOfParameter("post.option");
		inReplyToUserId = configuration.getValueOfParameter("post.replyto.userid");
		inReplyToPostId = configuration.getValueOfParameter("post.replyto.postid");
		postHashtags = configuration.getValueOfParameter("post.hashtags");

		System.out.println("postOption: " + postOption);
		System.out.println("postText: " + postText);
		System.out.println("postHashtags: " + postHashtags);
		System.out.println("inReplyToUserId: " + inReplyToUserId);
		System.out.println("inReplyToPostId: " + inReplyToPostId);

		/*
		aggregatorSources = configuration.getValueOfParameter("sources");
		whatCollect = configuration.getValueOfParameter("what.collect");
		whatWordsAll = configuration.getValueOfParameter("what.words.all");
		whatWordsExactPhrase = configuration.getValueOfParameter("what.words.exactphrase");
		whatWordsAny = configuration.getValueOfParameter("what.words.any");
		whatWordsNone = configuration.getValueOfParameter("what.words.none");
		whatWordsHashtags = configuration.getValueOfParameter("what.words.hashtags");
		
		whatPeopleFromAccounts = configuration.getValueOfParameter("what.people.from.accounts");
		whatPeopleToAccounts = configuration.getValueOfParameter("what.people.to.accounts");
		whatPeopleMentioningAccounts = configuration.getValueOfParameter("what.people.mentioning.accounts");

		whatPeopleFromGroups = configuration.getValueOfParameter("what.people.from.groups");
		whatPeopleToGroups = configuration.getValueOfParameter("what.people.to.groups");
		whatPeopleMentioningGroups = configuration.getValueOfParameter("what.people.mentioning.groups");

		whatWordsNameIDorTag = configuration.getValueOfParameter("what.words.name.idortag");
		whatWordsNameContains = configuration.getValueOfParameter("what.words.name.contains");
		*/
		
		/*
		whatDatesOption = configuration.getValueOfParameter("what.dates.option");
		whatDatesSince = configuration.getValueOfParameter("what.dates.since");
		whatDatesUntil = configuration.getValueOfParameter("what.dates.until");

		//location = configuration.getValueOfParameter("location"); //deprecated
		locationOption = configuration.getValueOfParameter("location.option");
		locationUseapi = configuration.getValueOfParameter("location.useapi");
		locationAppendtosq = configuration.getValueOfParameter("location.appendtosq");

		locationCity = configuration.getValueOfParameter("location.city");
		locationRegion = configuration.getValueOfParameter("location.region");
		locationCountryName = configuration.getValueOfParameter("location.countryName");
		locationCountryCode = configuration.getValueOfParameter("location.countryCode");
		locationLat = configuration.getValueOfParameter("location.lat");
		locationLong = configuration.getValueOfParameter("location.long");
		locationRadius = configuration.getValueOfParameter("location.radius");
		locationRadiusUnit = configuration.getValueOfParameter("location.radius.unit");

		language = configuration.getValueOfParameter("language");

		resultsType = configuration.getValueOfParameter("results.type");
		resultsMaxOption = configuration.getValueOfParameter("results.max.results.option");
		resultsMax = configuration.getValueOfParameter("results.max.results");
		resultsMaxPerPage = configuration.getValueOfParameter("results.max.per.page");
		resultsStoreInDB = configuration.getValueOfParameter("results.storage.storeindb");
		resultsKeepRawData = configuration.getValueOfParameter("results.storage.keeprawdata");
		*/

		/*
		System.out.println("sources: " + aggregatorSources + "\n");
		
		System.out.println("whatCollect: " + whatCollect);
		System.out.println("whatWordsAll: " + whatWordsAll);
		System.out.println("whatWordsExactPhrase: " + whatWordsExactPhrase);
		System.out.println("whatWordsAny: " + whatWordsAny);
		System.out.println("whatWordsNone: " + whatWordsNone);
		System.out.println("whatWordsHashtags: " + whatWordsHashtags);
		
		System.out.println("whatPeopleFromAccounts: " + whatPeopleFromAccounts);
		System.out.println("whatPeopleToAccounts: " + whatPeopleToAccounts);
		System.out.println("whatPeopleMentioningAccounts: " + whatPeopleMentioningAccounts);

		System.out.println("whatPeopleFromGroups: " + whatPeopleFromGroups);
		System.out.println("whatPeopleToGroups: " + whatPeopleToGroups);
		System.out.println("whatPeopleMentioningGroups: " + whatPeopleMentioningGroups);

		System.out.println("whatWordsNameIDorTag: " + whatWordsNameIDorTag);
		System.out.println("whatWordsNameContains: " + whatWordsNameContains);
		*/

		/*
		System.out.println("whatDatesOption: " + whatDatesOption);
		System.out.println("whatDatesSince: " + whatDatesSince);
		System.out.println("whatDatesUntil: " + whatDatesUntil);

		System.out.println("locationOption: " + locationOption);

		//System.out.println("location: " + location);
		System.out.println("locationCity: " + locationCity);
		System.out.println("locationRegion: " + locationRegion);
		System.out.println("locationCountryName: " + locationCountryName);
		System.out.println("locationCountryCode: " + locationCountryCode);
		System.out.println("locationLat: " + locationLat);
		System.out.println("locationLong: " + locationLong);
		System.out.println("locationRadius: " + locationRadius);
		System.out.println("locationRadiusUnit: " + locationRadiusUnit);

		System.out.println("locationUseapi: " + locationUseapi);
		System.out.println("locationAppendtosq: " + locationAppendtosq);
		
		System.out.println("language: " + language);

		System.out.println("resultsType: " + resultsType);
		System.out.println("resultsMaxOption: " + resultsMaxOption);
		System.out.println("resultsMax: " + resultsMax);
		System.out.println("resultsMaxPerPage: " + resultsMaxPerPage);
		System.out.println("resultsStoreInDB: " + resultsStoreInDB);
		System.out.println("resultsKeepRawData: " + resultsKeepRawData);
		*/

		System.out.println();
	}
	
	private String generateRequestUrl() throws Exception {
		String paramsString = "";
		
		int i=0;
		for (String name : injectParams.keySet()) {
			//paramsString += (i == 0) ? "?" : "&";
			paramsString += name + "=" + injectParams.get(name) + "&";
			i++;
		}
		
		if (paramsString.endsWith("&"))
			paramsString = paramsString.substring(0, paramsString.length() - 1);
		
		//String requestUrl = injectUrl + paramsString;
		
		URI uri = new URI(injectUrl);
		URI fullURI = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), paramsString, null);
		
		return fullURI.toASCIIString();
	}
	
	//Not currently required
	//protected abstract AuthScope getAuthScope() throws Exception;
	
	/* No longer used
	private UsernamePasswordCredentials getUsernamePasswordCredentials() throws Exception {
		String usernameProp = "credentials." + site + ".username";
		String passwordProp = "credentials." + site + ".password";
		
		String username = configuration.getValueOfParameter(usernameProp);
		String password = configuration.getValueOfParameter(passwordProp);
		
		if (username == null)
			throw new Exception("Missing inject property: " + usernameProp);
		
		if (password == null)
			throw new Exception("Missing inject property: " + passwordProp);
		
		return new UsernamePasswordCredentials(username, password);
	}
	*/
	
	protected void submitRequestAndHandleResponse() throws Exception {
		String requestUrl = generateRequestUrl();
		//System.out.println("\nInject site: " + site);
		//System.out.println(  "Inject URL : " + requestUrl + "\n");		

		tool.reportMessage(sns.getSNS() + ": inject started");
		//Uncomment these lines for testing
        //String responseString = loadTestResponseFromFile();
        //handleResponse(responseString);

		/*
		if (authMethod.equals("oauth")) {
			// OAuth request
	        HttpURLConnection response = getOAuthResponse(requestUrl);
	        handleResponse(response);			
		}
		else if (authMethod.equals("none")) {
			// Anonymous request
			HttpResponse resp = submitRequest(requestUrl);
	        handleResponse(resp);
		}
		*/
		
	}

	private void saveTestResponseToFile(JSON json) throws IOException {
		FileOutputStream fos = new FileOutputStream("C:/Users/kem/Projects/WeGov/temp/inject-results.txt");
		OutputStreamWriter os = new OutputStreamWriter(fos, "UTF8");
		try {
			os.write(json.toString(2));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			os.close();
		}
	}
	
	private String loadTestResponseFromFile() throws IOException {
		System.out.println("Loading test response from file...");
		FileInputStream fis = new FileInputStream("C:/Users/kem/Projects/WeGov/temp/inject-results.txt");
		StringBuilder text = new StringBuilder();
		String NL = System.getProperty("line.separator");
		Scanner scanner = new Scanner(fis);
		try {
			while (scanner.hasNextLine()){
				text.append(scanner.nextLine() + NL);
			}
		}
		finally{
			scanner.close();
		}
		
		return text.toString();
	}

	private HttpResponse submitRequest(String requestUrl) throws Exception {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "utf-8");

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, registry);
        DefaultHttpClient client = new DefaultHttpClient(manager, params);

        //No longer used - should use OAuth instead, for authenticated requests
        //client.getCredentialsProvider().setCredentials(getAuthScope(), getUsernamePasswordCredentials());

		HttpGet get = new HttpGet(requestUrl);

        System.out.println("Request sent");
        
        HttpResponse resp = client.execute(get);
        
        return resp;
	}

    public HttpURLConnection getOAuthResponse(String urlAsString) throws MalformedURLException, IOException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
    	URL url = new URL(urlAsString);

    	OAuthConsumer consumer = new DefaultOAuthConsumer(oauthConsumerKey, oauthConsumerSecret);
        consumer.setTokenWithSecret(oauthConsumerAccessToken, oauthConsumerAccessTokenSecret);
        
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        
        consumer.sign(request);
        
        request.connect();
        
        return request;
    }    

    public HttpParams getParams() {
        // Tweak further as needed for your app
        HttpParams params = new BasicHttpParams();
        // set this to false, or else you'll get an
        // Expectation Failed: error
        HttpProtocolParams.setUseExpectContinue(params, false);
        return params;
    }

    /*
    // Your actual method might look like this:
    //public void updateProfileBackground(User user, File file) {
    public void updateProfileBackground(File file) {
        try {
            // Create a new consumer using the commons implementation
            //OAuthConsumer consumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
            OAuthConsumer consumer = new CommonsHttpOAuthConsumer(oauthConsumerKey, oauthConsumerSecret);
            //consumer.setTokenWithSecret(getUserAccessToken(user), getUserTokenSecret(user));
            consumer.setTokenWithSecret(oauthConsumerAccessToken, oauthConsumerAccessTokenSecret);
            
            HttpPost uploadBackgroundPost = new HttpPost(UPDATE_PROFILE_BACKGROUND_IMAGE_URL);

            // The body of a multi-part post isn't needed
            // for the generation of the signature
            consumer.sign(uploadBackgroundPost);

            // only works in strict mode
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.STRICT);

            // Twitter checks against supported file types
            FileBody imageBody = new FileBody(file, "image/png");

            entity.addPart("image", imageBody);
            uploadBackgroundPost.setEntity(entity);
            DefaultHttpClient httpClient = new DefaultHttpClient(getParams());

            // If you're interested in the headers,
            // implement and add a request interceptor that prints them
            httpClient.addRequestInterceptor(new PrintRequestInterceptor());

            System.out.println(httpClient.execute(uploadBackgroundPost, new BasicResponseHandler()));
        } catch (Exception e) {
            // do some proper exception handling here
            e.printStackTrace();
        }
    }
    */
    
    
    

    
    
    
	private void handleResponse(Object resp) throws Exception {
        String line;
        int statusCode;

        statusCode = getResponseCode(resp);
        System.out.println("Response code: " + statusCode + "\n");
        
        handleResponseHeaders(resp);
        
        if (statusCode == 200) {
            InputStream stream = getResponseStream(resp);
            System.out.println("\nGot stream");

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

            while ((line = reader.readLine()) != null) {
            	handleResponse(line);
            }
        }
        else
        	throw new Exception("Response code: " + statusCode);
        
        disconnect(resp);
        
	}

	private int getResponseCode(Object resp) throws Exception {
		int statusCode = 0;
		
		if (resp instanceof HttpResponse)
			statusCode = ((HttpResponse) resp).getStatusLine().getStatusCode();
		else if (resp instanceof HttpURLConnection) {
			statusCode = ((HttpURLConnection) resp).getResponseCode();
		}
		else
			throw new Exception("Unknown response type: " + resp.getClass());
		
		return statusCode;
	}

	private InputStream getResponseStream(Object resp) throws Exception {
		InputStream is = null;
		
		if (resp instanceof HttpResponse)
			is = ((HttpResponse) resp).getEntity().getContent();
		else if (resp instanceof HttpURLConnection) {
			is = ((HttpURLConnection) resp).getInputStream();
		}
		else
			throw new Exception("Unknown response type: " + resp.getClass());
		
		return is;
	}
	
	private void handleResponseHeaders(Object resp) throws Exception {
        HashMap<String, String> headersMap = new HashMap<String,String>();

        System.out.println("Headers:");

		if (resp instanceof HttpResponse) {
			Header[] headers = ((HttpResponse) resp).getAllHeaders();
			
	        for (Header header : headers) {
				System.out.println(header.toString() + ": " + header.getValue());
				headersMap.put(header.getName(), header.getValue());
			}
		}
		else if (resp instanceof HttpURLConnection) {
			Map<String, List<String>> headers = ((HttpURLConnection) resp).getHeaderFields();
			
	        for (String header : headers.keySet()) {
	        	if (header != null) {
					System.out.println(header.toString() + ": " + headers.get(header).toString());
					headersMap.put(header, headers.get(header).toString());
	        	}
			}
		}
		else
			throw new Exception("Unknown response type: " + resp.getClass());
        
        handleResponseHeaders(headersMap);
	}
	
	protected void handleResponseHeaders(Map<String, String> headersMap) {
	}

	private void handleResponse(String line) throws Exception {
		Object jsonResponse = null;
        //System.out.println("Response: " + line);

        if (line.length() > 2) {            
            if (line.startsWith("[")) {
            	jsonResponse = JSONArray.fromObject(line);
            }
            else if (line.startsWith("{")) {
            	jsonResponse = JSONObject.fromObject(line);
            }
            else {
            	throw new Exception("Could not parse response: " + line);
            }
            
            handleResponse((JSON)jsonResponse);
        } else {
           throw new Exception("Got empty response because of timeout");
        }
	}

	protected void handleResponse(JSON json) throws Exception {
		System.out.println("\nHandling response:");
        //OutputStreamWriter os = new OutputStreamWriter(System.out, "UTF8");
        //PrintWriter ps = new PrintWriter(os);
        //ps.println("\n" + json.toString(2));
		
		//Uncomment this line to temporarily store the result (for development testing)
		//saveTestResponseToFile(json);
		
		System.out.println(json.toString(2));

		/*
		if (whatCollect.equals("posts")) {
			extractPosts(json);
		}
		else if (whatCollect.equals("users")) {
			extractUsers(json);
		}
		else if (whatCollect.equals("groups")) {
			extractGroups(json);
		}
		*/
		
		//TODO: handle response

	}

	protected abstract void setSNS() throws Exception;
	protected abstract void extractPosts(JSON json) throws Exception;
	protected abstract void extractUsers(JSON json) throws Exception;
	protected abstract void extractGroups(JSON json) throws Exception;
	protected abstract WegovPostItem jsonToPost(JSONObject object) throws Exception;
	
	private void disconnect(Object resp) throws Exception {
		if (resp instanceof HttpResponse)
			return;
		else if (resp instanceof HttpURLConnection) {
			((HttpURLConnection) resp).disconnect();
		}
		else
			throw new Exception("Unknown response type: " + resp.getClass());
	}

	public void execute() throws Exception {
		submitRequestAndHandleResponse();
		tool.reportMessage(sns.getSNS() + ": inject completed");
	}

	protected boolean isEmpty(String string) {
		boolean isEmpty = false;

		if (string == null)
			isEmpty = true;
		else if (string.trim().equals("")) {
			isEmpty = true;
		}
		else if (string.trim().equals("null")) {
			isEmpty = true;
		}

		return isEmpty;
	}
	
	protected boolean isTrue(String string) {
		if (isEmpty(string))
			return false;
		
		if (string.trim().toLowerCase().equals("true"))
			return true;
		
		return false;
	}

	protected String getAccountIdFromPost(JSONObject object) {
		String accountID = null;
		String userID = getUserIdFromPost(object);
		String domain = getUserDomainFromPost(object);
		String postID = getPostIdFromPost(object);
		
		accountID = generateAccountID(userID, domain, postID);
		
		return accountID;
	}
	
	protected abstract String getUserIdFromPost(JSONObject object);
	protected abstract String getPostIdFromPost(JSONObject object);
	
	protected String getUserDomainFromPost(JSONObject object) {
		return this.site;
	}

	protected String generateAccountID(String userID, String postID) {
		String domain = this.site;
		return generateAccountID(userID, domain, postID);
	}
	
	protected String generateAccountID(String userID, String domain, String postID) {
		String accountID = null;
		
		if (isEmpty(userID)) {
			System.out.println("WARNING: null user id - creating dummy user");
			accountID = "unknown";
		}
		else {
			accountID = userID;
		}
		
		accountID += userIdSeparator + domain + userIdSeparator + postID;
		
		return accountID;
	}

	protected void setLocationParams() {
		/*
		if ( isEmpty(locationOption) || locationOption.equals("anywhere")) {
			System.out.println("No location specified (inject anywhere)");
			return;
		}
		
		//if (isEmpty(location)) {
		//	System.out.println("WARNING: no location defined! Will default to anywhere");
		//	return;
		//}
		
		if ( isEmpty(locationUseapi) && isEmpty(locationAppendtosq)) {
			System.out.println("WARNING: location selected, but no location options selected. Will use API by default");
			locationUseapi = "true";
		}
		
		if ( isTrue(locationUseapi)) {
			System.out.println("Setting location via API");
			setLocationViaAPI();
		}

		if ( isTrue(locationAppendtosq)) {
			System.out.println("Setting location via inject query");
			setLocationViaInjectQuery();
		}
		*/
	}
	
	protected void setLocationViaInjectQuery() {
		//TODO: is this method required?

		/*
		if (isEmpty(whatWordsAll)) {
			whatWordsAll = "";
		}
		
		//whatWordsAll += " " + location.trim();
		
		if (! isEmpty(locationCity)) {
			locationCity = locationCity.trim();
			whatWordsAll += " " + locationCity;
		}
		
		if (! isEmpty(locationRegion)) {
			locationRegion = locationRegion.trim();
			if ( (isEmpty(locationCity)) || (! locationRegion.equals(locationCity)) )
				whatWordsAll += " " + locationRegion;
		}

		//TODO: should we use country name?
		//if (! isEmpty(locationCountryName))
		//	whatWordsAll += " " + locationCountryName;

		System.out.println("Appended location to whatWordsAll: " + whatWordsAll);
		*/
		
	}

	protected abstract void setLocationViaAPI();
}
