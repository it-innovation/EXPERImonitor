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
//	Created By :			Steve Taylor, modifying a file by Ken Meacham
//	Created Date :			2012-07-05
//	Created for Project :	WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.tools.analysis;




import eu.wegov.coordinator.Configuration;
import eu.wegov.coordinator.Run;
import eu.wegov.coordinator.dao.data.WegovWidgetDataAsJson;
import eu.wegov.coordinator.web.WidgetDataAsJson;
import eu.wegov.tools.WegovTool;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import org.apache.log4j.Logger;

public abstract class WeGovAnalysis {

	protected WegovAnalysisTool tool = null;
	protected Configuration configuration = null;
  protected String type = null;
  protected String subType = null;
  protected JSONArray sourceDataSpecifications = null;
  protected String inputDataAsJsonString = null;
  protected ArrayList<WidgetDataAsJson> rawInputData = null;

	protected String resultsType;
	protected String resultsStoreInDB;
	protected String resultsKeepRawData;


  //protected int ownerPmId;

  private final static Logger logger = Logger.getLogger(WeGovAnalysis.class.getName());

  private final SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");


	public WeGovAnalysis(WegovAnalysisTool tool, String type, String subType, JSONArray sourceDataSpecs) throws Exception {
		this.tool = tool;
		this.configuration = tool.getConfiguration();
		this.type = type;
		this.subType = subType;
    this.sourceDataSpecifications = sourceDataSpecs;
    //this.ownerPmId = tool.getOwnerPmId();
    this.rawInputData = new ArrayList<WidgetDataAsJson>();

		//System.out.println("\nNew search: " + site);

    System.out.println("New WeGovAnalysis of type: " + this.type);
    System.out.println("Input data spec JSON : " + this.sourceDataSpecifications);

    logger.debug("Owner pmId = " + tool.getOwnerPmId());

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

	protected void setAnalysisParams() throws Exception {
		//aggregatorSources = getValueOfParameter("sources");

		resultsType = getValueOfParameter("results.type");
		resultsKeepRawData = getValueOfParameter("results.storage.keeprawdata");

		//setResultsOptions();
	}
/*
	public void storeResults() throws Exception {

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
    String minId = "0";
    String maxId = "0";
    Timestamp minTs = "0";
    Timestamp maxTs = getMaxTs();
    tool.getCoordinator().saveRunResultsDataAsJson(wsId, runId, type, name, location, nResults, minId, maxId, minTs, maxTs, dataAsJson, collected_at);
	}
*/

  protected boolean isDataCompatible(String analysisSubType, String dataType) {

       //analysis.subType                 Data Type
       // facebook-post-comments-topics   "post-comments-facebook"
       // facebook-group-topics           "posts-facebook"
       // facebook-group-topics           "post-comments-facebook"
       // twitter-topics                  "posts-twitter"
       // twitter-behaviour               "posts-twitter"

       // DATA Types =
       // "posts-facebook"
       // "post-comments-facebook"
       // "posts-twitter"

        HashMap<String, String> analysisDataOK = new HashMap<String, String>();

        analysisDataOK.put("facebook-post-comments-topics", "post-comments-facebook");
        analysisDataOK.put("facebook-group-topics", "posts-facebook");
        analysisDataOK.put("facebook-group-topics", "post-comments-facebook");
        analysisDataOK.put("twitter-topics", "posts-twitter");
        analysisDataOK.put("twitter-behaviour", "posts-twitter");


        String allowedDataForAnalysisType = (String)analysisDataOK.get(analysisSubType);

        if (allowedDataForAnalysisType == null) {
          return false;
        }

        if (allowedDataForAnalysisType.equals(dataType) ) {
          return true;
        }
        else {
          return false;
        }
  }


  protected void getInputDataSets(String analysisSubType) throws Exception {



      /* Parameters
       *
       * runNow - boolean
       *
       * schedule - ??
       *
       * Array - sourceDataRuns {activityId = actId, runId = runId, postId = postId}
       *
       * analysis.type
       *  analysis.type is either "topic-opinion" or "behaviour"
       * Defined in addTopicOpinionTool and addKMITool in
       * Database maintenance
       *
       * analysis.subType
       * this is either:
       *  facebook-post-comments-topics
       *  facebook-group-topics
       *  twitter-topics
       *  twitter-behaviour
       *
       *
       *  DATA Types =
       "posts-facebook"
       "post-comments-facebook"
       "posts-twitter"

        "topic-opinion.twitter-topics"
        "behaviour.twitter-behaviour"
        "topic-opinion.facebook-post-comments-topics"

       *
       */

    for (int i = 0; i < this.sourceDataSpecifications.size(); i++) {
      JSONObject sourceDataSpecJson = (JSONObject) this.sourceDataSpecifications.get(i);

      // get data for run

      // check data for run is compatible with analysis type
      // if yes, add to inputDataAsJson

      // activity ID necessary for all cases of input spec
      int activityId = 0;
      if (sourceDataSpecJson.containsKey("activityId")) {
        activityId = sourceDataSpecJson.getInt("activityId");
      }
      else {
       throw new Exception ("No activity ID in source data specification for tool run: " +  tool.getMyRunId());
      }

      // other elements are optional

      String postId = null;
      if (sourceDataSpecJson.containsKey("postId")) {
        postId = sourceDataSpecJson.getString("postId");
      }

      int runId = -1;
      if (sourceDataSpecJson.containsKey("runId")) {
        runId = sourceDataSpecJson.getInt("runId");
      }


      WidgetDataAsJson[] tempData = null;
      if ("facebook-post-comments-topics".equals(analysisSubType)) {

        if (postId == null) {
          throw new Exception ("Analysis sub type : " + analysisSubType +
                  " that needs a post ID and this is not specified. Run: " +  tool.getMyRunId());
        }
        logger.debug("getting facebook comments data for activity " + activityId + " and pmId " + tool.getOwnerPmId() );
        tempData = tool.getCoordinator().getFacebookPostCommentsData(activityId, tool.getOwnerPmId(), postId);

      }
      else {

        if (runId < 0) {
          // case where we want all runs for an activity
          logger.debug("getting activity results data for activity " + activityId + " and pmId " + tool.getOwnerPmId() );
          tempData =  tool.getCoordinator().getAllResultsForActivity(activityId, tool.getOwnerPmId());
        }
        else {
          // case where we want specifed runs for an activity
          logger.debug("getting activity results data for activity " + activityId + ", run " + runId + " and pmId " + tool.getOwnerPmId() );
          tempData =  tool.getCoordinator().getAllResultsForActivityAndRun(activityId, tool.getOwnerPmId(), runId);
        }
      }

/*
      if (tempData != null) {
        this.rawInputData.addAll(Arrays.asList(tempData));
      }
*/

      // only add data compatible with analysis
      if (tempData != null) {
        for (int j = 0; j < tempData.length; j++) {
          String dataType = tempData[j].getType();
          if (isDataCompatible(analysisSubType, dataType)) {
            System.out.println ("Adding element " + j + " of input data because its type of "
                    +  dataType + " is compatible with the analysis subtype of " + analysisSubType);
            System.out.println ("input data activity ID = " + tempData[j].getActivityid()
                    + " , runID = " + tempData[j].getRunid()
                    + ", name = " + tempData[j].getName() + ".");
            this.rawInputData.add(tempData[j]);
          }
          else {
            System.out.println ("THROWING AWAY element " + j + " of input data because its type of "
                    +  dataType + " is incompatible with the analysis subtype of " + analysisSubType);
            System.out.println ("THROWN AWAY data activity ID = " + tempData[j].getActivityid()
                    + " , runID = " + tempData[j].getRunid()
                    + ", name = " + tempData[j].getName() + ".");
          }
        }
      }


    }


    //JSONArray inputDataAsJSON = new JSONArray();

    //System.out.println("Input data = ");

    /*
     * JSON inputData Structure
     *
     * inputDataAsJSON
     *
     *  FacebookPosts:
     *    postData
     *      query
     *      data []
     *    userData []
     *
     *  FacebookComments:
     *    postData
     *      query
     *      data []
     *    userData []
     *
     *  Tweets:
     *    postData
     *      query
     *      results []
     *    userData []
     *
     */


				JSONObject inputDataJSON = new JSONObject();
/*
				JSONObject postData = new JSONObject();
				postData.put("query", postId);
				postData.put("data", commentsJsonArray);

				resultsData.put("postData", postData);
				resultsData.put("userData", usersJson);
  */
    for (int k = 0; k < this.rawInputData.size(); k++) {
      //inputDataAsJSON.add (JSONSerializer.toJSON(this.inputData.get(k).getDataAsJson()));
/*
      // make sure search results are all the same, i.e. the same as the first one
      String searchResultFirstType = this.inputData.get(0).getType();
*/
      String searchResultType = this.rawInputData.get(k).getType();
/*
      if (!searchResultType.equals(searchResultFirstType) {
        throw new Exception ("Results for activity " + " are not the same!");

*/

      // add this in case needed externally
      inputDataJSON.put("resultType", searchResultType);
      JSONObject tempDataAsJSONObject = (JSONObject)JSONSerializer.toJSON(this.rawInputData.get(k).getDataAsJson());

      if (searchResultType.equals("posts-twitter")) {

        // postData
        JSONObject tempPostData = tempDataAsJSONObject.getJSONObject("postData");
        if (! inputDataJSON.containsKey("postData")) {
          // probably the first call - no element yet
          inputDataJSON.put("postData", tempPostData);
        }
        else {
          // subsequent calls will add to the element
          JSONArray tempResults = tempPostData.getJSONArray("results");
          //inputDataJSON.getJSONObject("postData").accumulate("results", tempResults);
          inputDataJSON.getJSONObject("postData").getJSONArray("results").addAll(tempResults);
        }

        // userData
        JSONArray tempUserDataArray = tempDataAsJSONObject.getJSONArray("userData");
        if (! inputDataJSON.containsKey("userData")) {
          // probably the first call - no element yet
          inputDataJSON.put("userData", tempUserDataArray);
        }
        else {
          // subsequent calls will add to the element
          //inputDataJSON.accumulate("userData", tempUserDataArray);
          inputDataJSON.getJSONArray("userData").addAll(tempUserDataArray);
        }

      }
      else { // should be facebook posts / comments
        // postData
        JSONObject tempPostData = tempDataAsJSONObject.getJSONObject("postData");
        if (! inputDataJSON.containsKey("postData")) {
          // probably the first call - no element yet
          inputDataJSON.put("postData", tempPostData);
        }
        else {
          // subsequent calls will add to the element
          JSONArray tempResults = tempPostData.getJSONArray("data");
          //inputDataJSON.getJSONObject("postData").accumulate("results", tempResults);
          inputDataJSON.getJSONObject("postData").getJSONArray("data").addAll(tempResults);
        }

        // userData
        JSONArray tempUserDataArray = tempDataAsJSONObject.getJSONArray("userData");
        if (! inputDataJSON.containsKey("userData")) {
          // probably the first call - no element yet
          inputDataJSON.put("userData", tempUserDataArray);
        }
        else {
          // subsequent calls will add to the element
          //inputDataJSON.accumulate("userData", tempUserDataArray);
          inputDataJSON.getJSONArray("userData").addAll(tempUserDataArray);
        }

      }

      // At this point we dont know if the dataAsJson is an array or an object
      //Object tempDataAsJSON = JSONSerializer.toJSON(this.inputData.get(k).getDataAsJson());

      /*
      if (tempDataAsJSON instanceof JSONArray) {

          JSONArray tempDataAsJSONArray = (JSONArray)tempDataAsJSON;

          // add each element of the current result set
          for (int m = 0; m < tempDataAsJSONArray.size(); m++) {
            inputDataAsJSON.add (tempDataAsJSONArray.get(m));
          }
      }
      else if (tempDataAsJSON instanceof JSONObject) {

          JSONObject tempDataAsJSONObject = (JSONObject)tempDataAsJSON;

          inputDataAsJSON.add (tempDataAsJSONObject);
      }
      else {
          throw new Exception ("Data as JSON returned is neither a JSONArray nor a JSONObject");
      }
      */

      //System.out.println(this.inputData.get(k).toString());

    }

    this.inputDataAsJsonString = inputDataJSON.toString();

    //System.out.println(inputDataAsJsonString);

  }

	public void execute() throws Exception {

    // get configuration

    // get data from DB
    getInputDataSets(this.subType);

    // run appropriate analysis

    // store results in DB

  }

  /*
	protected WidgetDataAsJson [] getResultDataFromRuns() throws Exception {

    int runId = 1;
    // get list of runs from configuration
    // or make list of runs from spec in configuration

    // Make array list

    ArrayList<WegovWidgetDataAsJson> runData = null;

		runData.add(tool.getCoordinator().getResultsForRun(runId, false));

    WidgetDataAsJson[] result = new WidgetDataAsJson[runData.size()];

    	for (int i = 0; i < runData.size(); i++) {
    		result[i] = new WidgetDataAsJson (
                runData.get(i).getId(),
                runData.get(i).getWidgetId(),
                runData.get(i).getPmId(),
                runData.get(i).getActivityId(),
                runData.get(i).getRunId(),
                runData.get(i).getType(),
                runData.get(i).getName(),
                runData.get(i).getLocation(),
                runData.get(i).getNumResults(),
                runData.get(i).getMinId(),
                runData.get(i).getMaxId(),
                runData.get(i).getDataAsJson(),
                df.format(runData.get(i).getTimeCollected()));

    	}

    //ArrayList<WegovWidgetDataAsJson> widgetDataFromDb = getDataSchema().getAllWhereSortBy(new WegovWidgetDataAsJson(), map, "collected_at");

		return result;
	}
*/

/*
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
		WegovWidgetDataAsJson results = tool.getCoordinator().getResultsForRun(previousRun.getID(), false);

		return results;
	}
*/




}
