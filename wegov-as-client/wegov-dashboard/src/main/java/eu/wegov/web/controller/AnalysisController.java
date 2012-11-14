package eu.wegov.web.controller;

import eu.wegov.common.model.BehaviourAnalysisResult;
import eu.wegov.common.model.JSONTwitterPostDetails;
import eu.wegov.common.model.NewActivityAndRun;
import eu.wegov.common.model.BehaviourAnalysisUsersForRole;
import eu.wegov.common.model.JSONTwitterUserDetails;
import eu.wegov.common.model.TopicOpinionAnalysisResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.Vector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


import uk.ac.open.kmi.analysis.Buzz.BuzzPrediction;
import uk.ac.open.kmi.analysis.DiscussionActivity.DiscussionActivity;
import uk.ac.open.kmi.analysis.DiscussionActivity.DiscussionActivityInput;
import uk.ac.open.kmi.analysis.UserRoles.UserFeatures;
import uk.ac.open.kmi.analysis.UserRoles.UserRole;
import uk.ac.open.kmi.analysis.UserRoles.UserRoleAnalysis;
import uk.ac.open.kmi.analysis.core.Post;
import west.importer.WegovImporter;
import west.wegovdemo.SampleInput;
import west.wegovdemo.TopicOpinionAnalysis;
import west.wegovdemo.TopicOpinionDocument;
import west.wegovdemo.TopicOpinionInput;
import west.wegovdemo.TopicOpinionOutput;
import west.wegovdemo.WegovRender;
import eu.wegov.coordinator.Activity;
import eu.wegov.coordinator.Parameter;
import eu.wegov.coordinator.Run;
import eu.wegov.coordinator.web.ConfigParameter;
import eu.wegov.coordinator.web.WidgetDataAsJson;
import eu.wegov.web.security.WegovLoginService;


import eu.wegov.tools.analysis.*;
import net.sf.json.JsonConfig;
import org.apache.log4j.Logger;

@Controller
@RequestMapping("/home/analysis")
public class AnalysisController {

    @Autowired
    @Qualifier("wegovLoginService")
    public WegovLoginService loginService;
    private final static Logger logger = Logger.getLogger(AnalysisController.class.getName());

    @RequestMapping(method = RequestMethod.POST, value = "/createNewAnalysis/do.json")
    public @ResponseBody
    NewActivityAndRun recordNewAnalysis(@RequestBody final String paramsString) {
        int activityId = 0;
        int runId = 0;
        String error = null;

        try {
            System.out.println("Creating new analysis with params" + paramsString);


            /*
             * Parameters
             *
             * runNow - boolean
             *
             * schedule - ??
             *
             * Array - sourceDataRuns {activityId = actId, runId = runId, postId
             * = postId}
             *
             * analysis.type analysis.type is either "topic-opinion" or
             * "behaviour" Defined in addTopicOpinionTool and addKMITool in
             * Database maintenance
             *
             * analysis.subType this is either: facebook-post-comments-topics
             * facebook-group-topics twitter-topics twitter-behaviour
             *
             *
             */

            JSONObject params = (JSONObject) JSONSerializer.toJSON(paramsString);
            //boolean clientAnalysis = params.getBoolean("clientAnalysis");

            Activity analysisActivity = loginService.createNewAnalysisActivity(params);
            activityId = analysisActivity.getID();
            System.out.println("Created activity " + analysisActivity.getID() + ": " + analysisActivity.getName());

            boolean runNow = params.getBoolean("runNow");
            if (runNow) {
                loginService.startAnalysisActivity(analysisActivity);
                String activityStatus = analysisActivity.getStatus();
                System.out.println("Activity status: " + activityStatus);

                int i = 0;
                while ((!(activityStatus.equals(Activity.STATUS_RUNNING) || activityStatus.equals(Activity.STATUS_FINISHED) || activityStatus.equals(Activity.STATUS_FAILED))) && (i < 10)) {
                    Thread.sleep(5000);
                    analysisActivity = loginService.getActivityById(activityId); //update activity
                    activityStatus = analysisActivity.getStatus();
                    System.out.println("Activity status: " + activityStatus);
                    i++;
                }

                if (!(activityStatus.equals(Activity.STATUS_RUNNING) || activityStatus.equals(Activity.STATUS_FINISHED) || activityStatus.equals(Activity.STATUS_FAILED))) {
                    //return -1;
                    throw new Exception("ERROR: could not start activity " + analysisActivity.getID());
                }

                System.out.println("Getting last run for activity " + analysisActivity.getID());
                Run analysisRun = analysisActivity.getLastRun();
                //return analysisRun.getID();
                runId = analysisRun.getID();
                System.out.println("Run ID is " + runId);

            } else {
                JSONObject scheduleParams = (JSONObject) params.get("schedule");
                loginService.scheduleAnalysisActivity(analysisActivity, scheduleParams);
                String activityStatus = analysisActivity.getStatus();
                System.out.println("Activity status: " + activityStatus);

                int i = 0;
                while ((activityStatus.equals(Activity.STATUS_INITIALISING)) && (i < 10)) {
                    Thread.sleep(5000);
                    analysisActivity = loginService.getActivityById(activityId); //update activity
                    activityStatus = analysisActivity.getStatus();
                    System.out.println("Activity status: " + activityStatus);
                    i++;
                }

                //if (activityStatus.equals(Activity.STATUS_INITIALISING)) {
                //	throw new Exception("ERROR: could not schedule activity " + analysisActivity.getID());
                //	//return -1;
                //}

                if (activityStatus.equals(Activity.STATUS_RUNNING)) {
                    System.out.println("Getting last run for activity " + analysisActivity.getID());
                    Run analysisRun = analysisActivity.getLastRun();
                    //return analysisRun.getID();
                    runId = analysisRun.getID();
                    System.out.println("Run ID is " + runId);
                }
                //else {
                //	return analysisActivity.getID();
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = e.getMessage();
            //return -1;
        }

        NewActivityAndRun activityAndRun = new NewActivityAndRun(activityId, runId, error);
        return activityAndRun;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/updateAnalysis/do.json")
    public @ResponseBody
    int updateAnalysis(@RequestBody final String inputData) {
        try {
            Timestamp finishedTime = new Timestamp(new Date().getTime());
            JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(inputData);
            String myRunId = inputDataAsJSON.getString("myRunId");
            int runId = Integer.parseInt(myRunId);
            System.out.println("Updating analysis run with ID: " + myRunId);

            Run theRun = loginService.getRunById(runId);
            theRun.getActivity().setStatus(Activity.STATUS_FINISHED, theRun);
            theRun.setWhenFinished(finishedTime);
//			theRun.setStatus(Run.STATUS_FINISHED);
//			Activity analysisActivity = loginService.createNewAnalysisActivity(query);
            return runId;
        } catch (Exception e) {
            e.printStackTrace();
            return -1; //TODO: fix as previous method
        }
    }

    //public Parameter [] getParametersForRun(int runId) throws Exception {
    @RequestMapping(method = RequestMethod.GET, value = "/getRunParameters/do.json")
    public @ResponseBody
    ConfigParameter[] getParametersForRun(@RequestParam("runId") int runId) {

        String status = null;
        try {
            return loginService.getParametersForRun(runId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getAnalysisStatus/do.json")
    public @ResponseBody
    String getAnalysisStatus(@RequestParam("runId") int runId) {

        String status = null;
        try {
            Run run = loginService.getRunById(runId);
            status = run.getStatus();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getAnalysisResults/do.json")
    public @ResponseBody
    WidgetDataAsJson getAnalysisResults(@RequestParam("runId") int runId) {

        WidgetDataAsJson result = null;
        try {
            result = loginService.getResultsDataForRun(runId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getTopicAnalysisResults/do.json")
    public @ResponseBody
    TopicOpinionAnalysisResult getTopicAnalysisResults(@RequestParam("runId") int runId) {
        return getTopicAnalysisOutputFromDb(runId);
    }

    public TopicOpinionAnalysisResult getTopicAnalysisOutputFromDb(int runId) {

        WidgetDataAsJson widget = null;
        TopicOpinionAnalysisResult output = null;

        try {

            widget = loginService.getResultsDataForRun(runId);

            JSONObject widgetAsJSONObject = (JSONObject) JSONSerializer.toJSON(widget);

            String type = widgetAsJSONObject.getString("type");

            if (!type.startsWith("topic-opinion", 0)) {
                throw new Exception("Getting topic analysis result from DB - Incompatible type: " + type);
            }
            JSONObject dataAsJSONObject = (JSONObject) JSONSerializer.toJSON(widgetAsJSONObject.getString("dataAsJson"));

            //System.out.println ("JSON object = " + dataAsJSONObject.toString());
            logger.debug("JSON object = " + dataAsJSONObject.toString());

            output = new TopicOpinionAnalysisResult(dataAsJSONObject);

            return output;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getBehaviourAnalysisResults/do.json")
    public @ResponseBody
    BehaviourAnalysisResult getBehaviourAnalysisResults(@RequestParam("runId") int runId) {
        return getBehaviourAnalysisOutputFromDb(runId);
    }

    public BehaviourAnalysisResult getBehaviourAnalysisOutputFromDb(int runId) {

        WidgetDataAsJson widget = null;
        BehaviourAnalysisResult output = null;

        try {

            widget = loginService.getResultsDataForRun(runId);

            JSONObject widgetAsJSONObject = (JSONObject) JSONSerializer.toJSON(widget);

            String type = widgetAsJSONObject.getString("type");

            if (!type.startsWith("behaviour", 0)) {
                throw new Exception("Getting behaviour analysis result from DB - Incompatible type: " + type);
            }
            JSONObject dataAsJSONObject = (JSONObject) JSONSerializer.toJSON(widgetAsJSONObject.getString("dataAsJson"));

            System.out.println("JSON object = " + dataAsJSONObject.toString());
            logger.debug("JSON object = " + dataAsJSONObject.toString());

            output = new BehaviourAnalysisResult(dataAsJSONObject);

            return output;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/kmi/onlyroles/do.json")
    public @ResponseBody
    BehaviourAnalysisUsersForRole doOnlyRoles(@RequestBody final String inputData) throws Exception {

        JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(inputData);
        String userData = inputDataAsJSON.getString("userData");
        //JSONArray userDataAsJSON = inputDataAsJSON.getJSONArray("userData");
        String searchQuery = inputDataAsJSON.getString("searchQuery");
        String selectedRoleName = inputDataAsJSON.getString("selectedRoleName");

        return doOnlyRolesInternal(userData, selectedRoleName, searchQuery);
    }

    @RequestMapping(method = RequestMethod.POST, value = "kmi/onlyroles/widget_data/do.json")
    public @ResponseBody
    BehaviourAnalysisUsersForRole doOnlyRolesWithWidgetData(
            @RequestParam("wId") int wId,
            @RequestParam("selectedRoleName") String selectedRoleName,
            @RequestParam("searchQuery") String searchQuery) throws Exception {

        WidgetDataAsJson[] result = null;
        try {
            result = loginService.getWidgetData(wId);
            if (result != null) {
                // Data exists for this widget
                String dataAsJson = result[0].getDataAsJson();

                JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(dataAsJson);
                //String postData = inputDataAsJSON.getString("postData");
                String userData = inputDataAsJSON.getString("userData");

                if (userData != null) {
                    return doOnlyRolesInternal(userData, selectedRoleName, searchQuery);
                } else {
                    return null;
                }
            } else {
                // No data for this widget - TODO need to return a sensible message
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @RequestMapping(method = RequestMethod.POST, value = "kmi/onlyroles/run_data/do.json")
    public @ResponseBody
    BehaviourAnalysisUsersForRole doOnlyRolesWithRunData(
            @RequestParam("runId") int runId, // this needs to be search results run
            @RequestParam("selectedRoleName") String selectedRoleName,
            @RequestParam("searchQuery") String searchQuery) throws Exception {

        WidgetDataAsJson result = null;
        try {
            result = loginService.getResultsDataForRun(runId);
            if (result != null) {
                // Data exists for this widget
                String dataAsJson = result.getDataAsJson();

                JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(dataAsJson);
                //String postData = inputDataAsJSON.getString("postData");
                String userData = inputDataAsJSON.getString("userData");

                if (userData != null) {
                    return doOnlyRolesInternal(userData, selectedRoleName, searchQuery);
                } else {
                    return null;
                }
            } else {
                // No data for this widget - TODO need to return a sensible message
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    BehaviourAnalysisUsersForRole doOnlyRolesInternal(String inputData, String selectedRoleName, String searchQuery) throws Exception {


        Activity analysisActivity = loginService.createNewAnalysisActivity("Behaviour user roles for query \"" + searchQuery + "\"");
        Run analysisRun = loginService.createNewAnalysisRun(analysisActivity, "Role: " + selectedRoleName);

        String runId = Integer.toString(analysisRun.getID());

        try {
            System.out.println("Behaviour Analysis Roles only START by " + loginService.getLoggedInUser() + ", role: " + selectedRoleName + ", query: " + searchQuery);

            analysisActivity.setStatus(Activity.STATUS_RUNNING, analysisRun);

            // The Guts
            BehaviourAnalysisUsersForRole analysisResult =
                    WeGovBehaviourAnalysis.doBehaviourRoles(runId, inputData, selectedRoleName, searchQuery);

            analysisActivity.setStatus(Activity.STATUS_FINISHED, analysisRun);
            System.out.println("Behaviour Analysis Roles only SUCCESS by " + loginService.getLoggedInUser());
            return analysisResult;

        } catch (Exception e) {
            System.out.println("Behaviour Analysis Roles only FAILED by " + loginService.getLoggedInUser());
            e.printStackTrace();
            analysisActivity.setStatus(Activity.STATUS_FAILED, analysisRun);
            return null;
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/kmi/twitter/widget_data/do.json")
    public @ResponseBody
    BehaviourAnalysisResult doBehaviourWithWidgetData(@RequestParam("wId") int wId) throws Exception {

        WidgetDataAsJson[] result = null;
        try {
            result = loginService.getWidgetData(wId);
            if (result != null) {
                // Data exists for this widget
                String inputData = result[0].getDataAsJson();
                if (inputData != null) {
                    return doBehaviourInternal(inputData);
                } else {
                    return null;
                }
            } else {
                // No data for this widget - TODO need to return a sensible message
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @RequestMapping(method = RequestMethod.GET, value = "/kmi/run_data/do.json")
    public @ResponseBody
    BehaviourAnalysisResult doBehaviourForRun(@RequestParam("runId") int runId) throws Exception {

        WidgetDataAsJson result = null;
        try {
            result = loginService.getResultsDataForRun(runId);
            if (result != null) {
                // Data exists for this run
                String dataAsJson = result.getDataAsJson();

                if (dataAsJson != null) {
                    String type = result.getType();
                    if (type.equals("posts-twitter")) {
                        return doBehaviourInternal(dataAsJson);
                    } else if (type.equals("posts-facebook")) {
                        return doBehaviourInternal(dataAsJson);
                    } else {
                        throw new Exception("Unsupported results type: " + type);
                    }
                } else {
                    return null;
                }
            } else {
                // No data for this run - TODO need to return a sensible message
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/kmi/do.json")
    public @ResponseBody
    BehaviourAnalysisResult doBehaviour(@RequestBody final String inputData) throws Exception {

        return doBehaviourInternal(inputData);
    }

    BehaviourAnalysisResult doBehaviourInternal(final String inputData) throws Exception {

        JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(inputData);
        JSONObject postDataAsJSON = inputDataAsJSON.getJSONObject("postData");
        JSONArray postDataAsJSONArray = postDataAsJSON.getJSONArray("results");
        int numPostInInput = postDataAsJSONArray.size();

        Activity analysisActivity = loginService.createNewAnalysisActivity("Behaviour analysis for query \"" + postDataAsJSON.getString("query") + "\"");
        Run analysisRun = loginService.createNewAnalysisRun(analysisActivity, numPostInInput + " posts analysed");

        String runId = Integer.toString(analysisRun.getID());

        try {
            // System.out.println("Here is the data for behaviour analysis:");
            // System.out.println(inputData);
            System.out.println("Behaviour Analysis START by " + loginService.getLoggedInUser());

            // The Guts
            BehaviourAnalysisResult result = WeGovBehaviourAnalysis.doBehaviour(runId, inputData);

            analysisActivity.setStatus(Activity.STATUS_FINISHED, analysisRun);
            System.out.println("Behaviour Analysis SUCCESS by " + loginService.getLoggedInUser());
            return result;

        } catch (Exception e) {
            System.out.println("Behaviour Analysis FAILED by " + loginService.getLoggedInUser());
            e.printStackTrace();
            analysisActivity.setStatus(Activity.STATUS_FAILED, analysisRun);
            return null;
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/koblenz/twitter/widget_data/do.json")
    public @ResponseBody
    TopicOpinionAnalysisResult doTwitterTopicsWithWidgetData(@RequestParam("wId") int wId) throws Exception {

        WidgetDataAsJson[] result = null;
        try {
            result = loginService.getWidgetData(wId);
            if (result != null) {
                // Data exists for this widget
                String dataAsJson = result[0].getDataAsJson();

                JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(dataAsJson);
                String postData = inputDataAsJSON.getString("postData");

                if (postData != null) {
                    return doTwitterTopicsInternal(postData);
                } else {
                    return null;
                }
            } else {
                // No data for this widget - TODO need to return a sensible message
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @RequestMapping(method = RequestMethod.GET, value = "/koblenz/run_data/do.json")
    public @ResponseBody
    TopicOpinionAnalysisResult doTopicsForRun(@RequestParam("runId") int runId) throws Exception {

        WidgetDataAsJson result = null;
        try {
            result = loginService.getResultsDataForRun(runId);
            if (result != null) {
                // Data exists for this run
                String dataAsJson = result.getDataAsJson();

                JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(dataAsJson);
                String postData = inputDataAsJSON.getString("postData");
                if (postData != null) {
                    String type = result.getType();
                    if (type.equals("posts-twitter")) {
                        return doTwitterTopicsInternal(postData);
                    } else if (type.equals("posts-facebook")) {
                        return doFacebookTopicsInternal(postData);
                    } else {
                        throw new Exception("Unsupported results type: " + type);
                    }
                } else {
                    return null;
                }
            } else {
                // No data for this run - TODO need to return a sensible message
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/koblenz/do.json")
    public @ResponseBody
    TopicOpinionAnalysisResult doTopics(@RequestBody final String postData) throws Exception {

        return doTwitterTopicsInternal(postData);
    }

    TopicOpinionAnalysisResult doTwitterTopicsInternal(final String postData) throws Exception {
        JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(postData);
        String searchQuery = inputDataAsJSON.getString("query").toLowerCase().trim();
        JSONArray posts = inputDataAsJSON.getJSONArray("results");
        return doTopicsInternal("twitter", searchQuery, posts);
    }

    TopicOpinionAnalysisResult doTopicsInternal(final String sns, final String searchQuery, final JSONArray posts) throws Exception {
        Activity analysisActivity = loginService.createNewAnalysisActivity("Topic analysis for query \"" + searchQuery + "\"");
        Run analysisRun = loginService.createNewAnalysisRun(analysisActivity, posts.size() + " posts analysed");

        String runId = Integer.toString(analysisRun.getID());

        try {
            SampleInput input = new SampleInput();
            System.out.println("Topic Analysis START by " + loginService.getLoggedInUser());

            // The Guts
            TopicOpinionAnalysisResult result = WeGovTopicAnalysis.doTopics(runId, sns, searchQuery, posts);

            // Finish activity
            analysisActivity.setStatus(Activity.STATUS_FINISHED, analysisRun);

            System.out.println("Topic Analysis SUCCESS by " + loginService.getLoggedInUser());

            return result;
        } catch (Exception e) {
            System.out.println("Topic Analysis FAILED by " + loginService.getLoggedInUser());
            e.printStackTrace();
            analysisActivity.setStatus(Activity.STATUS_FAILED, analysisRun);
            return null;
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/koblenz/facebook/widget_data/do.json")
    public @ResponseBody
    TopicOpinionAnalysisResult doFacebookTopicsWithWidgetData(@RequestParam("wId") int wId) throws Exception {

        WidgetDataAsJson[] result = null;
        try {
            result = loginService.getWidgetData(wId);
            if (result != null) {
                // Data exists for this widget
                String dataAsJson = result[0].getDataAsJson();

                JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(dataAsJson);
                String postData = inputDataAsJSON.getString("postData"); // TODO: extract as object to pass into doFacebookTopicsInternal


                if (postData != null) {
                    return doFacebookTopicsInternal(postData);
                } else {
                    return null;
                }
            } else {
                // No data for this widget - TODO need to return a sensible message
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @RequestMapping(method = RequestMethod.POST, value = "/fbkoblenz/do.json")
    public @ResponseBody
    TopicOpinionAnalysisResult doTopicsForFacebook(@RequestBody final String postData) throws Exception {

        return doFacebookTopicsInternal(postData);
    }

    TopicOpinionAnalysisResult doFacebookTopicsInternal(final String postData) throws Exception {
        JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(postData);
        String searchQuery = inputDataAsJSON.getString("query").toLowerCase().trim();
        JSONArray posts = inputDataAsJSON.getJSONArray("data");
        return doFacebookTopicsInternal("facebook", searchQuery, posts);
    }

    //TODO: consolidate this method with doTopicsInternal, to re-use core topic analysis code
    TopicOpinionAnalysisResult doFacebookTopicsInternal(final String sns, final String searchQuery, final JSONArray posts) throws Exception {
        Activity analysisActivity = loginService.createNewAnalysisActivity("Topic analysis for facebook post comments\"" + searchQuery + "\"");
        Run analysisRun = loginService.createNewAnalysisRun(analysisActivity, posts.size() + " post comments analysed");

        String runId = Integer.toString(analysisRun.getID());

        try {
            SampleInput input = new SampleInput();
            System.out.println("Topic Analysis START by " + loginService.getLoggedInUser());

            // The Guts
            TopicOpinionAnalysisResult result = WeGovTopicAnalysis.doTopics(runId, sns, searchQuery, posts);

            // Finish activity
            analysisActivity.setStatus(Activity.STATUS_FINISHED, analysisRun);

            System.out.println("Topic Analysis SUCCESS by " + loginService.getLoggedInUser());

            return result;
            /*
             * SampleInput input = new SampleInput(); System.out.println("Topic
             * Analysis START by " + loginService.getLoggedInUser());
             *
             * //TODO: should really be JSONFacebookUserDetails, but this class
             * does not exist yet HashMap<String, JSONTwitterUserDetails>
             * userIds_jsonTwitterUserDetails = new HashMap<String,
             * JSONTwitterUserDetails>(); HashMap<String,
             * JSONTwitterPostDetails> postIds_jsonTwitterPostDetails = new
             * HashMap<String, JSONTwitterPostDetails>(); String userId; String
             * userName; String postId; String postContents; String
             * postContentsWithoutHttp; String cleanWord; int docId = 0; for
             * (int i = 0; i < posts.size(); i++) { JSONObject postJSON =
             * (JSONObject) posts.get(i); postId = postJSON.getString("id");
             * JSONObject fromJSON = postJSON.getJSONObject("from"); userId =
             * fromJSON.getString("id"); userName = fromJSON.getString("name");
             * String message = postJSON.has("message") ?
             * postJSON.getString("message") : ""; String description =
             * postJSON.has("description") ? postJSON.getString("description") :
             * ""; postContents = message + " " + description;
             *
             * postContentsWithoutHttp = ""; for (String word :
             * postContents.split(" ")) { cleanWord = word.toLowerCase().trim();
             * if ( cleanWord.startsWith("http://") ) { } else {
             * postContentsWithoutHttp = postContentsWithoutHttp + cleanWord + "
             * "; } }
             *
             * postContentsWithoutHttp = postContentsWithoutHttp.trim();
             *
             * if (postContentsWithoutHttp.equals("")) {
             * System.out.println("WARNING: result " + i + " has no message or
             * description content: \n" + postJSON); } else {
             * input.add(postContentsWithoutHttp.trim(), userId);
             * userIds_jsonTwitterUserDetails.put( userId, new
             * JSONTwitterUserDetails(userId, userName, userName,
             * "https://graph.facebook.com/" + userId + "/picture" ));
             * postIds_jsonTwitterPostDetails.put( //Integer.toString(i),
             * Integer.toString(docId), new
             * JSONTwitterPostDetails(postJSON.getString("created_time"),
             * postId, postContents, userId, userName, userName, "")); // }
             *
             * System.out.println(docId + ": " + postContentsWithoutHttp);
             * docId++; } }
             *
             * TopicOpinionAnalysisResult result =
             * WeGovTopicAnalysis.doTopicsCore(input,
             * postIds_jsonTwitterPostDetails, userIds_jsonTwitterUserDetails);
             *
             * analysisActivity.setStatus(Activity.STATUS_FINISHED,
             * analysisRun); System.out.println("Topic Analysis SUCCESS by " +
             * loginService.getLoggedInUser()); return result;
             */
        } catch (Exception e) {
            System.out.println("Topic Analysis FAILED by " + loginService.getLoggedInUser());
            e.printStackTrace();
            analysisActivity.setStatus(Activity.STATUS_FAILED, analysisRun);
            return null;
        }
    }

    private static Vector<String> listFilesInDir(String DirPath,
            String Extension) {

        Vector<String> filenames = new Vector<String>();
        File dir = new File(DirPath);
        String[] children = dir.list();
        if (children == null || children.length == 0) {
//			System.out.println(DirPath + " is empty");
        } else {
            for (int i = 0; i < children.length; i++) {
                String filename = children[i];
                if (filename.endsWith(Extension)) {
                    filenames.add(filename.replaceAll(Extension, ""));
                }
            }
        }
        return filenames;
    }

    public static String readStringFile(String path) {
        String result = "";
        try {
            String str = "";
            BufferedReader in = new BufferedReader(new FileReader(path));
            while ((str = in.readLine()) != null) {
                result += (" " + str);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }
}
