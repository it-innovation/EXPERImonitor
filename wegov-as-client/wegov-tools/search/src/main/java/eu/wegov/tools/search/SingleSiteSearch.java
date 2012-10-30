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
//	Created Date :			2011-09-29
//	Created for Project :	WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.tools.search;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import eu.wegov.coordinator.Configuration;
import eu.wegov.coordinator.Run;
import eu.wegov.coordinator.dao.data.WegovPostItem;
import eu.wegov.coordinator.dao.data.WegovSNS;
import eu.wegov.coordinator.dao.data.WegovSnsUserAccount;
import eu.wegov.coordinator.dao.data.WegovWidgetDataAsJson;
import eu.wegov.coordinator.utils.Util;
import eu.wegov.tools.WegovTool;

public abstract class SingleSiteSearch implements WegovSearch {

	protected WegovTool tool = null;
	protected Configuration configuration = null;
	protected WegovSNS sns;
	protected Map<String, WegovSNS> snsMap = new HashMap<String, WegovSNS>();
	
	protected boolean storeResultsAsRawJson = true;
	protected boolean storeResultsAsStructuredData = false;
	
	private JSONArray resultsJson = new JSONArray();
	private JSONArray usersJson = new JSONArray();
	
	protected String site = null;
	protected String aggregatorSources = null;
	protected String searchUrl = null;
	protected String query = null;
	protected int currentPage;
	protected String nextPageQuery = null;
	protected Map<String, String> searchParams = new HashMap<String, String>();
	protected int nResults = 0;
	protected HashSet<String> userIds = new HashSet<String>();
	protected String[] userIdsArray;

	protected boolean limitResults = false;
	protected int resultsMaxInt = 0;
	protected int resultsMaxPerPageInt = 0;
	protected int resultsMaxPagesInt = 0;
	
	protected boolean collectResultsSinceLastActivityRun = false;

	protected String minId = null;
	protected String maxId = null;
	protected String minTsStr = null;
	protected String maxTsStr = null;

	protected String whatCollect;
	protected String whatWordsAll;
	protected String whatWordsExactPhrase;
	protected String whatWordsAny;
	protected String whatWordsNone;
	protected String whatWordsHashtags;
	
	protected String whatPeopleFromAccounts;
	protected String whatPeopleToAccounts;
	protected String whatPeopleMentioningAccounts;
	
	protected String whatPeopleFromGroups;
	protected String whatPeopleToGroups;
	protected String whatPeopleMentioningGroups;
	
	protected String whatWordsNameIDorTag;
	protected String whatWordsNameContains;

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
	protected String resultsMaxPages;
	protected String resultsMaxCollectionTimeOption;
	protected String resultsMaxCollectionTime;
	protected String resultsStoreInDB;
	protected String resultsKeepRawData;
	
	protected Util util = new Util();
	
	protected String authMethod = null;
	
	// OAuth tokens
	protected String oauthConsumerKey;
	protected String oauthConsumerSecret;
	protected String oauthConsumerAccessToken;
	protected String oauthConsumerAccessTokenSecret;
	
	protected String accessToken; // used by Facebook

	protected String userIdSeparator = "";
	protected Timestamp collectionDate;
	
	// Debugging options
	protected boolean printHeaders = false;
	protected boolean printResponses = false;
	protected boolean printDataObjects = false;
	
	protected boolean retryRequest = false;

	public SingleSiteSearch(WegovTool tool, String site) throws Exception {
		this.tool = tool;
		this.configuration = tool.getConfiguration();
		this.site = site;

		System.out.println("\nNew search: " + site);
		setSNS();
		
        setSecurityParams();
		setSearchParams();
		setupSearch();
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
		else if (authMethod.equals("access_token")) {
			//Properties properties = tool.getCoordinatorProperties();
			//accessToken = properties.getProperty("accessToken");
			
			accessToken = getValueOfParameter("access.token");
		}
	}

	protected String getValueOfParameter(String param) throws Exception {
		String val = null;
		try {
			val = configuration.getValueOfParameter(param);
		}
		catch (Exception e) {
			throw new Exception("Parameter is undefined: " + param);
		}
		return val;
	}

	protected boolean getBooleanValueOfParameter(String param) throws Exception {
		String strval = getValueOfParameter(param);
		if (isEmpty(strval))
			return false;
		boolean val = Boolean.parseBoolean(strval);
		return val;
	}

	protected void setSearchParams() throws Exception {
		aggregatorSources = getValueOfParameter("sources");
		whatCollect = getValueOfParameter("what.collect");
		whatWordsAll = getValueOfParameter("what.words.all");
		whatWordsExactPhrase = getValueOfParameter("what.words.exactphrase");
		whatWordsAny = getValueOfParameter("what.words.any");
		whatWordsNone = getValueOfParameter("what.words.none");
		whatWordsHashtags = getValueOfParameter("what.words.hashtags");
		
		whatPeopleFromAccounts = getValueOfParameter("what.people.from.accounts");
		whatPeopleToAccounts = getValueOfParameter("what.people.to.accounts");
		whatPeopleMentioningAccounts = getValueOfParameter("what.people.mentioning.accounts");

		whatPeopleFromGroups = getValueOfParameter("what.people.from.groups");
		whatPeopleToGroups = getValueOfParameter("what.people.to.groups");
		whatPeopleMentioningGroups = getValueOfParameter("what.people.mentioning.groups");

		whatWordsNameIDorTag = getValueOfParameter("what.words.name.idortag");
		whatWordsNameContains = getValueOfParameter("what.words.name.contains");
		
		whatDatesOption = getValueOfParameter("what.dates.option");
		whatDatesSince = getValueOfParameter("what.dates.since");
		whatDatesUntil = getValueOfParameter("what.dates.until");

		//location = getValueOfParameter("location"); //deprecated
		locationOption = getValueOfParameter("location.option");
		locationUseapi = getValueOfParameter("location.useapi");
		locationAppendtosq = getValueOfParameter("location.appendtosq");

		locationCity = getValueOfParameter("location.city");
		locationRegion = getValueOfParameter("location.region");
		locationCountryName = getValueOfParameter("location.countryName");
		locationCountryCode = getValueOfParameter("location.countryCode");
		locationLat = getValueOfParameter("location.lat");
		locationLong = getValueOfParameter("location.long");
		locationRadius = getValueOfParameter("location.radius");
		locationRadiusUnit = getValueOfParameter("location.radius.unit");

		language = getValueOfParameter("language");

		resultsType = getValueOfParameter("results.type");
		resultsMaxOption = getValueOfParameter("results.max.results.option");
		resultsMax = getValueOfParameter("results.max.results");
		resultsMaxPerPage = getValueOfParameter("results.max.per.page");
		resultsMaxPages = getValueOfParameter("results.max.pages");
		resultsMaxCollectionTime = getValueOfParameter("results.max.collection.time");
		resultsMaxCollectionTimeOption = getValueOfParameter("results.max.collection.time.option");
		resultsStoreInDB = getValueOfParameter("results.storage.storeindb");
		resultsKeepRawData = getValueOfParameter("results.storage.keeprawdata");
		
		collectResultsSinceLastActivityRun = getBooleanValueOfParameter("results.collect.since.last.run");
		
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
		System.out.println("resultsMaxPages: " + resultsMaxPages);
		System.out.println("resultsMaxCollectionTimeOption: " + resultsMaxCollectionTimeOption);
		System.out.println("resultsMaxCollectionTime: " + resultsMaxCollectionTime);
		System.out.println("resultsStoreInDB: " + resultsStoreInDB);
		System.out.println("resultsKeepRawData: " + resultsKeepRawData);
		System.out.println("collectResultsSinceLastActivityRun: " + collectResultsSinceLastActivityRun);

		System.out.println();

		setResultsOptions();
	}

	protected void setResultsOptions() throws Exception {
		storeResultsAsRawJson = getBooleanValueOfParameter("results.storage.keeprawdata");
		storeResultsAsStructuredData = getBooleanValueOfParameter("results.storage.storeindb");
		System.out.println("storeResultsAsRawJson: " + storeResultsAsRawJson);
		System.out.println("storeResultsAsStructuredData: " + storeResultsAsStructuredData);
		
		//N.B. SNS specific search should override this method, if paging is used
		resultsMaxPerPageInt = getDefaultMaxResultsPerPage();
		System.out.println("Setting default max per page: " + resultsMaxPerPageInt);

		if (! isEmpty(resultsMaxPerPage)) {
			int userResultsMaxPerPageInt = Integer.parseInt(resultsMaxPerPage);
			if (userResultsMaxPerPageInt > 0) {
				System.out.println("Setting max per page: " + userResultsMaxPerPageInt);
				resultsMaxPerPageInt = userResultsMaxPerPageInt;
			}
		}

		//N.B. SNS specific search should override this method, if paging is used
		resultsMaxPagesInt = getDefaultMaxPages();
		System.out.println("Setting default max pages: " + resultsMaxPagesInt);
		
		resultsMaxInt = getDefaultMaxResults();
		System.out.println("Setting default max results: " + resultsMaxInt);
		
		if (! isEmpty(resultsMaxOption) && (resultsMaxOption.equals("limited"))) {
			if (! isEmpty(resultsMax)) {
				int userResultsMaxInt = Integer.parseInt(resultsMax);
				if ( (userResultsMaxInt > 0) && ( (resultsMaxInt == 0) || (userResultsMaxInt < resultsMaxInt) ) ) {
					System.out.println("Setting max results: " + userResultsMaxInt);
					resultsMaxInt = userResultsMaxInt;
					limitResults = true; 
				}
			}
			
			if ((resultsMaxInt > 0) && resultsMaxPerPageInt > resultsMaxInt) {
				System.out.println("Reducing max per page to max results: " + resultsMaxInt);
				resultsMaxPerPageInt = resultsMaxInt;
			}

			if (! isEmpty(resultsMaxPages)) {
				int userResultsMaxPagesInt = Integer.parseInt(resultsMaxPages);
				if (userResultsMaxPagesInt > 0) {
					System.out.println("Setting max pages: " + userResultsMaxPagesInt);
					resultsMaxPagesInt = userResultsMaxPagesInt;
					limitResults = true;
				}
			}
			
			if ((resultsMaxPerPageInt > 0) && (resultsMaxPagesInt > 0)) {
				int nPagedResults = resultsMaxPerPageInt * resultsMaxPagesInt;
				if (resultsMaxInt > 0) {
					if (nPagedResults < resultsMaxInt) {
						System.out.println("Setting max results: " + nPagedResults);
						resultsMaxInt = nPagedResults;
					}
					else {
						resultsMaxPagesInt = resultsMaxInt / resultsMaxPerPageInt;
						System.out.println("Limiting max pages to: " + resultsMaxPagesInt);
					}
				}
			}

			if (! limitResults) {
				System.out.println("WARNING: resultsMaxOption is set to limited, but neither resultsMax nor resultsMaxPages are set!");
			}
		}
		else {
			System.out.println();
			
			if (resultsMaxInt > 0) {
				System.out.println("Results limited by resultsMaxInt: " + resultsMaxInt);
				limitResults = true;
			}
			
			if (resultsMaxPagesInt > 0) {
				System.out.println("Results limited by resultsMaxPagesInt: " + resultsMaxPagesInt);
				limitResults = true;
			}
		}

		System.out.println("\nLimit results: " + limitResults);

		System.out.println();
	}
	
	protected int getDefaultMaxResultsPerPage() {
		return 0;
	}
	
	protected int getDefaultMaxPages() {
		return 0;
	}
	
	protected int getDefaultMaxResults() {
		return 0;
	}

	private String generateRequestUrl(String query) throws Exception {
		String paramsString = "";
		String requestURI = null;
		
		if (query != null) {
			System.out.println("generateRequestUrl: query = " + query);
			//paramsString = query.substring(1, query.length()); // knock off initial "?"
			requestURI = searchUrl + query;
		}
		else {
			int i=0;
			for (String name : searchParams.keySet()) {
				//paramsString += (i == 0) ? "?" : "&";
				paramsString += name + "=" + searchParams.get(name) + "&";
				i++;
			}
			
			if (paramsString.endsWith("&"))
				paramsString = paramsString.substring(0, paramsString.length() - 1);
			
			//String requestUrl = searchUrl + paramsString;
			
			URI uri = new URI(searchUrl);
			URI fullURI = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), paramsString, null);
			
			requestURI = fullURI.toASCIIString();
			
		}
		
		System.out.println("generateRequestUrl: requestURI = " + requestURI);
		
		return requestURI;
	}
	
	//Not currently required
	//protected abstract AuthScope getAuthScope() throws Exception;
	
	/* No longer used
	private UsernamePasswordCredentials getUsernamePasswordCredentials() throws Exception {
		String usernameProp = "credentials." + site + ".username";
		String passwordProp = "credentials." + site + ".password";
		
		String username = getValueOfParameter(usernameProp);
		String password = getValueOfParameter(passwordProp);
		
		if (username == null)
			throw new Exception("Missing search property: " + usernameProp);
		
		if (password == null)
			throw new Exception("Missing search property: " + passwordProp);
		
		return new UsernamePasswordCredentials(username, password);
	}
	*/
	
	protected void submitRequestAndHandleResponse() throws Exception {
		collectionDate = new Timestamp(new Date().getTime());
		String requestUrl = null;
		
		if (! whatCollect.equals("userdetails")) {
			requestUrl = generateRequestUrl(null);
			System.out.println("\nSearch site: " + site);
			System.out.println(  "Search URL : " + requestUrl + "\n");		
	
			//tool.reportMessage(sns.getSNS() + ": search started");
			tool.reportMessage("Search started");
		}
		//Uncomment these lines for testing
        //String responseString = loadTestResponseFromFile();
        //handleResponse(responseString);

		int maxPages = Integer.MAX_VALUE; // default number of pages
		nResults = 0;
		
		if ( whatCollect.equals("posts") && (resultsMaxPagesInt > 0) ) {
			maxPages = resultsMaxPagesInt;
			System.out.println("Collecting " + maxPages + " pages");
		}
		else if (whatCollect.equals("userdetails")) {
			maxPages = Integer.MAX_VALUE; // no specific limit of pages. Use resultsMaxInt instead
		}
		
		if (maxPages == Integer.MAX_VALUE)
			System.out.println("WARNING: not limiting number of pages to collect\n");
		
		if (authMethod.equals("oauth")) {
			String pageUrl = requestUrl;
			
			for (int i=0; i < maxPages; i++) {
				currentPage = i;

				if (whatCollect.equals("userdetails")) {
					setupSearch();
					if (searchParams.isEmpty()) //If there are no more users to collect
						break;
					pageUrl = generateRequestUrl(null);
				}

				System.out.println("Page: " + (currentPage+1) + ", URL: " + pageUrl);
				
				retryRequest = false;
				
				int attempts = 0;
				
				do {
					// OAuth request (e.g. Twitter user search)
					HttpURLConnection response;
					try {
						response = getOAuthResponse(pageUrl);
						handleResponse(response);
					}
					catch (IOException e) {
						e.printStackTrace();
						retryRequest = true;
					}
					
					if (retryRequest) {
						int delay = (attempts * 500) + 500; 
						System.out.println("Retrying request in " + delay + " secs");
						Thread.sleep(delay);
					}
					
					attempts++;
				} while (retryRequest && (attempts < 5));
				
				if (whatCollect.equals("posts")) {
					if ( (nextPageQuery == null) || (limitResults && (nResults >= resultsMaxInt)) ) {
						break;
					}
					pageUrl = generateRequestUrl(nextPageQuery);
				}
				else if (whatCollect.equals("userdetails")) {
					if ( (limitResults && (nResults >= resultsMaxInt)) ) {
						break;
					}
				}
			}
		}
		else if (authMethod.equals("none")) {
			// Anonymous request
			HttpResponse resp = submitRequest(requestUrl);
	        handleResponse(resp);
		}
	}

	private void saveTestResponseToFile(JSON json) throws IOException {
		FileOutputStream fos = new FileOutputStream("C:/Users/kem/Projects/WeGov/temp/search-results.txt");
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
		FileInputStream fis = new FileInputStream("C:/Users/kem/Projects/WeGov/temp/search-results.txt");
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
        registry.register(new Scheme("https", PlainSocketFactory.getSocketFactory(), 80));

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

        	System.out.println("handleResponse: start");
            while ((line = reader.readLine()) != null) {
            	//System.out.println("handleResponse: line: " + line);
            	System.out.println("handleResponse: line length: " + line.length());
            	handleResponse(line);
            }
        	System.out.println("handleResponse: end");
            
            retryRequest = false;
        }
        else {
        	handleStatusCode(statusCode);
        }
        
        disconnect(resp);
	}

	// Default behaviour for unexpected status codes
	protected void handleStatusCode(int statusCode) throws Exception {
    	throw new Exception("Response code: " + statusCode);
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

        if (printHeaders) System.out.println("Headers:");

		if (resp instanceof HttpResponse) {
			Header[] headers = ((HttpResponse) resp).getAllHeaders();
			
	        for (Header header : headers) {
	        	if (printHeaders) System.out.println(header.toString() + ": " + header.getValue());
				headersMap.put(header.getName(), header.getValue());
			}
		}
		else if (resp instanceof HttpURLConnection) {
			Map<String, List<String>> headers = ((HttpURLConnection) resp).getHeaderFields();
			
	        for (String header : headers.keySet()) {
	        	if (header != null) {
	        		if (printHeaders) System.out.println(header.toString() + ": " + headers.get(header).toString());
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
            	try {
					jsonResponse = JSONArray.fromObject(line);
				}
            	catch (Exception e) {
            		System.out.println("Could not parse response as JSONArray: " + line);
            		System.out.println("Line length: " + line.length());
					throw new IOException("Could not parse response as JSONArray", e);
				}
            }
            else if (line.startsWith("{")) {
            	try {
					jsonResponse = JSONObject.fromObject(line);
				}
            	catch (Exception e) {
            		System.out.println("Could not parse response as JSONObject: " + line);
            		System.out.println("Line length: " + line.length());
					throw new IOException("Could not parse response as JSONObject", e);
				}
            }
            else {
            	System.out.println("Could not parse response: " + line);
            	throw new IOException("Could not parse response");
            }
            
            handleResponse((JSON)jsonResponse);
        } else {
           throw new Exception("Got empty response because of timeout");
        }
	}

	protected void handleResponse(JSON json) throws Exception {
		if (printResponses) System.out.println("\nHandling response:");
        //OutputStreamWriter os = new OutputStreamWriter(System.out, "UTF8");
        //PrintWriter ps = new PrintWriter(os);
        //ps.println("\n" + json.toString(2));
		
		//Uncomment this line to temporarily store the result (for development testing)
		//saveTestResponseToFile(json);
		
		if (printResponses) System.out.println(json.toString(2));

		if (whatCollect.equals("posts")) {
			extractPosts(json);
		}
		else if (whatCollect.equals("users")) {
			extractUsers(json);
		}
		else if (whatCollect.equals("groups")) {
			extractGroups(json);
		}

	}

	protected abstract void setSNS() throws Exception;
	protected abstract void extractPosts(JSON json) throws Exception;
	protected abstract void extractUsers(JSON json) throws Exception;
	protected abstract void extractGroups(JSON json) throws Exception;
	protected abstract WegovPostItem jsonToPost(JSONObject object) throws Exception;
	protected abstract WegovSnsUserAccount jsonToUser(JSONObject jsonObject) throws Exception;
	
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
		//tool.reportMessage(sns.getSNS() + ": search completed");
		System.out.println("\n" + sns.getSNS() + ": search completed\n");
	}

	public void storeResults() throws Exception {
		if (storeResultsAsRawJson) {
			System.out.println("\nStoring results as JSON object");
			int wsId = 0; // N/A
			int runId = Integer.parseInt(tool.getMyRunId());
			String type = getSearchType();
			String name = getSearchName();
			String location = getLocation();
			int nResults = getNumResults();
			System.out.println("Total number of results: " + nResults);
			String dataAsJson = getResultsDataAsJson();
			Timestamp collected_at = collectionDate;
			String minId = getMinId();
			String maxId = getMaxId();
			Timestamp minTs = getMinTs();
			Timestamp maxTs = getMaxTs();
			tool.getCoordinator().saveRunResultsDataAsJson(wsId, runId, type, name, location, nResults, minId, maxId, minTs, maxTs, dataAsJson, collected_at);
		}
	}

	protected int getNumResults() {
		return resultsJson.size();
	}

	protected String getResultsDataAsJson() {
		JSONObject resultsData = new JSONObject();
		
		JSONObject postData = new JSONObject();
		postData.put("query", query);
		postData.put("results", resultsJson);
		
		resultsData.put("postData", postData);
		resultsData.put("userData", usersJson);
		
		return resultsData.toString();
	}

	protected String getLocation() {
		String location = "";
		if (! isEmpty(locationCity)) {
			location += locationCity;
			if (! isEmpty(locationCountryName)) {
				location += ", " + locationCountryName;
			}
		}
		return location;
	}

	protected String getMinId() {
		return minId;
	}

	protected String getMaxId() {
		return maxId;
	}
	
	protected Timestamp getMinTs() {
		return null;
	}

	protected Timestamp getMaxTs() {
		return null;
	}

	protected abstract String getSearchType() throws Exception;
	
	protected String getSearchName() {
		return "Search query: " + query +
				"; location: " + getLocation() +
				"; posts: " + getNumResults();
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

		int outputOfRunID = new Integer(tool.getMyRunId());

		accountID += userIdSeparator + domain + userIdSeparator + postID + "_" + outputOfRunID;
		
		return accountID;
	}

	protected void setLocationParams() {		
		if ( isEmpty(locationOption) || locationOption.equals("anywhere")) {
			System.out.println("No location specified (search anywhere)");
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
			System.out.println("Setting location via search query");
			setLocationViaSearchQuery();
		}
	}
	
	protected void setLocationViaSearchQuery() {
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
	}

	protected abstract void setLocationViaAPI();

	protected String getLatestPostIdFromPreviousRun() throws Exception {
		String id = null;
		
		WegovWidgetDataAsJson results = getResultsFromPreviousRun();
		
		if (results != null)
			id = results.getMaxId();
		
		return id;
	}

	protected Timestamp getLatestPostTsFromPreviousRun() throws Exception {
		String id = null;
		Timestamp ts = null;
		
		WegovWidgetDataAsJson results = getResultsFromPreviousRun();
		
		if (results != null) {
			id = results.getMaxId();
			ts = results.getMaxTimestamp();
			System.out.println("Last post from previous run: " + id + ", timestamp = " + ts);
		}
		
		return ts;
	}

	protected WegovWidgetDataAsJson getResultsFromPreviousRun() throws Exception {
		Run previousRun = tool.getPreviousRun();
		
		if (previousRun == null) {
			System.out.println("No previous run for this activity");
			return null;
		}
		
		//Previously we took this from the PostItem table... 

		//WegovPostItem latestPostItem = (WegovPostItem) previousRun.getFirstResult(new WegovPostItem(), "\"ID\"");
		//return latestPostItem.getID();
		
		//Now we assume that this is not available, so we get it from WidgetDataAsJson table
		WegovWidgetDataAsJson results = tool.getCoordinator().getResultsForRun(previousRun.getID(), tool.getPolicyMaker().getID(), false);
		
		return results;
	}

	protected void storePost(JSONObject jsonObject, WegovPostItem postItem) throws SQLException {
		if (storeResultsAsRawJson) {
			resultsJson.add(jsonObject);
		}
			
		if (storeResultsAsStructuredData)
			tool.getCoordinator().getDataSchema().insertObject(postItem);
	}
	
	protected void storeUser(JSONObject jsonObject) throws Exception {
		if (storeResultsAsRawJson) {
			usersJson.add(jsonObject);
		}
			
		if (storeResultsAsStructuredData) {
        	WegovSnsUserAccount userItem = jsonToUser(jsonObject);
        	if (printDataObjects) System.out.println("Adding user: " + userItem.toString());
        	// Should we always create new user account entry in db, or create/update if necessary?
        	tool.getCoordinator().getDataSchema().insertObject(userItem);
        	//getOrCreateUserAccount(userItem.getCount_ID(), object);
		}
	}

}
