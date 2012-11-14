package eu.wegov.web.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

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

import eu.wegov.common.model.NewActivityAndRun;
import eu.wegov.coordinator.Activity;
import eu.wegov.coordinator.ConfigurationSet;
import eu.wegov.coordinator.Run;
import eu.wegov.coordinator.web.WidgetDataAsJson;
import eu.wegov.web.security.WegovLoginService;
import eu.wegov.web.service.SchedulerService;

@Controller
@RequestMapping("/home/search")
public class SearchController {

    @Autowired
    @Qualifier("wegovLoginService")
    public WegovLoginService loginService;

    @RequestMapping(method = RequestMethod.POST, value = "/createNewSearch/do.json")
    //public @ResponseBody int recordNewSearch(@RequestParam("searchTerms") final String searchTerms) {
    public @ResponseBody
    NewActivityAndRun recordNewSearch(@RequestBody final String paramsString) {
        int activityId = 0;
        int runId = 0;
        String error = null;

        try {
            System.out.println("Creating new search");

            JSONObject params = (JSONObject) JSONSerializer.toJSON(paramsString);
            boolean clientSearch = params.getBoolean("clientSearch");

            Activity searchActivity = loginService.createNewSearchActivity(params);
            activityId = searchActivity.getID();
            System.out.println("Created activity " + searchActivity.getID() + ": " + searchActivity.getName());

            if (clientSearch) {
                //String runName = "At some location";
                String runName = null; //name will be set to default name
                Run searchRun = loginService.createNewSearchRun(searchActivity, runName);
                runId = searchRun.getID();
            } else {
                boolean runNow = params.getBoolean("runNow");
                if (runNow) {
                    loginService.startSearchActivity(searchActivity);
                    String activityStatus = searchActivity.getStatus();
                    System.out.println("Activity status: " + activityStatus);

                    int i = 0;
                    while ((!(activityStatus.equals(Activity.STATUS_RUNNING) || activityStatus.equals(Activity.STATUS_FINISHED) || activityStatus.equals(Activity.STATUS_FAILED))) && (i < 10)) {
                        Thread.sleep(5000);
                        searchActivity = loginService.getActivityById(activityId); //update activity
                        activityStatus = searchActivity.getStatus();
                        System.out.println("Activity status: " + activityStatus);
                        i++;
                    }

                    if (!(activityStatus.equals(Activity.STATUS_RUNNING) || activityStatus.equals(Activity.STATUS_FINISHED) || activityStatus.equals(Activity.STATUS_FAILED))) {
                        //return -1;
                        throw new Exception("ERROR: could not start activity " + searchActivity.getID());
                    }

                    System.out.println("Getting last run for activity " + searchActivity.getID());
                    Run searchRun = searchActivity.getLastRun();
                    //return searchRun.getID();
                    runId = searchRun.getID();
                } else {
                    JSONObject scheduleParams = (JSONObject) params.get("schedule");
                    loginService.scheduleSearchActivity(searchActivity, scheduleParams);
                    String activityStatus = searchActivity.getStatus();
                    System.out.println("Activity status: " + activityStatus);

                    int i = 0;
                    while ((activityStatus.equals(Activity.STATUS_INITIALISING)) && (i < 10)) {
                        Thread.sleep(5000);
                        searchActivity = loginService.getActivityById(activityId); //update activity
                        activityStatus = searchActivity.getStatus();
                        System.out.println("Activity status: " + activityStatus);
                        i++;
                    }

                    //if (activityStatus.equals(Activity.STATUS_INITIALISING)) {
                    //	throw new Exception("ERROR: could not schedule activity " + searchActivity.getID());
                    //	//return -1;
                    //}

                    if (activityStatus.equals(Activity.STATUS_RUNNING)) {
                        System.out.println("Getting last run for activity " + searchActivity.getID());
                        Run searchRun = searchActivity.getLastRun();
                        //return searchRun.getID();
                        runId = searchRun.getID();
                    }
                    //else {
                    //	return searchActivity.getID();
                    //}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = e.getMessage();
            //return -1;
        }

        NewActivityAndRun activityAndRun = new NewActivityAndRun(activityId, runId, error);
        return activityAndRun;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/updateSearch/do.json")
    public @ResponseBody
    int updateSearch(@RequestBody final String inputData) {
        try {
            Timestamp finishedTime = new Timestamp(new Date().getTime());
            JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(inputData);
            String myRunId = inputDataAsJSON.getString("myRunId");
            int runId = Integer.parseInt(myRunId);
            System.out.println("Updating search run with ID: " + myRunId);

            Run theRun = loginService.getRunById(runId);
            theRun.getActivity().setStatus(Activity.STATUS_FINISHED, theRun);
            theRun.setWhenFinished(finishedTime);
//			theRun.setStatus(Run.STATUS_FINISHED);
//			Activity searchActivity = loginService.createNewSearchActivity(query);
            return runId;
        } catch (Exception e) {
            e.printStackTrace();
            return -1; //TODO: fix as previous method
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getSearchStatus/do.json")
    public @ResponseBody
    String getSearchStatus(@RequestParam("runId") int runId) {

        String status = null;
        try {
            Run run = loginService.getRunById(runId);
            status = run.getStatus();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getSearchResults/do.json")
    public @ResponseBody
    WidgetDataAsJson getSearchResults(@RequestParam("runId") int runId) {

        WidgetDataAsJson result = null;
        try {
            result = loginService.getResultsDataForRun(runId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }
}
