package eu.wegov.web.controller;

import eu.experimedia.itinnovation.scc.web.adapters.EMClient;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.wegov.common.model.JSONActivity;
import eu.wegov.common.model.JSONActivityArray;
import eu.wegov.common.model.JSONRunArray;
import eu.wegov.common.model.JSONUserDetails;
import eu.wegov.coordinator.web.PolicymakerSetting;
import eu.wegov.web.security.WegovLoginService;
import eu.wegov.web.vo.scheduler.JobView;
import java.util.UUID;

@Controller
@RequestMapping("/home")
public class UserInfoController {
        
	@Autowired
	@Qualifier("wegovLoginService")
	WegovLoginService loginService;

	@RequestMapping(method = RequestMethod.GET, value = "/getnameandrole/do.json")
	public @ResponseBody
	JSONUserDetails getNameAndRole() throws Throwable {

		return loginService.getLoggedInUserDetails();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/getactivities/do.json")
	public @ResponseBody
	JSONActivityArray getActivities() {

		return loginService.getLoggedInUserActivities();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/getsearches/do.json")
	public @ResponseBody
	JSONActivityArray getSearches() {
		try {
			JSONActivityArray allActivites = loginService.getLoggedInUserActivities();
			ArrayList<JSONActivity> searchesAsArrayList = new ArrayList<JSONActivity>();

			JSONActivity tempActivity;
			for (int i = 0; i < allActivites.getSize(); i++) {
				tempActivity = allActivites.getData()[i];
//				System.out.println(tempActivity.getId() + ", " + tempActivity.getName()
//						 + ", " + tempActivity.getComment() + ", " + tempActivity.getStatus() + ", " + tempActivity.getWhenCreated());
				if (tempActivity.getComment().equals("search")) //TODO: should use a type field, not comment!
					searchesAsArrayList.add(tempActivity);
			}

			JSONActivity[] data = new JSONActivity[searchesAsArrayList.size()];
			for (int i = 0; i < data.length; i++) {
				data[i] = searchesAsArrayList.get(i);
			}
			JSONActivityArray searches = new JSONActivityArray(
					searchesAsArrayList.size(), data);
			return searches;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/getanalysises/do.json")
	public @ResponseBody
	JSONActivityArray getAnalysises() {
		try {
			JSONActivityArray allActivites = loginService
					.getLoggedInUserActivities(); // TODO this should be way more efficient!
			ArrayList<JSONActivity> analysisAsArrayList = new ArrayList<JSONActivity>();
			
			JSONActivity tempActivity;
			for (int i = 0; i < allActivites.getSize(); i++) {
				tempActivity = allActivites.getData()[i];
//				System.out.println(tempActivity.getId() + ", " + tempActivity.getName()
//						 + ", " + tempActivity.getComment() + ", " + tempActivity.getStatus() + ", " + tempActivity.getWhenCreated());
				if (tempActivity.getComment().equals("analysis"))
					analysisAsArrayList.add(tempActivity);
			}
			
			JSONActivity[] data = new JSONActivity[analysisAsArrayList.size()];
			for (int i = 0; i < data.length; i++) {
				data[i] = analysisAsArrayList.get(i);
			}
			JSONActivityArray searches = new JSONActivityArray(
					analysisAsArrayList.size(), data);
			return searches;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	@RequestMapping(method = RequestMethod.GET, value = "/getruns/do.json")
	public @ResponseBody
	JSONRunArray getRuns() {
		return loginService.getLoggedInUserRuns();
	}
	*/
	
	@RequestMapping(method = RequestMethod.GET, value = "/getRunsForActivity/do.json")
	public @ResponseBody
	JSONRunArray getRunsForActivity(@RequestParam("activityId") int activityId) {
		return loginService.getLoggedInUserRuns(activityId);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/getScheduledJobs/do.json")
	public @ResponseBody
	ArrayList<JobView> getScheduledJobs() throws Exception {
		return loginService.getScheduledJobs();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/deleteJobSchedule/do.json")
	public @ResponseBody
	boolean deleteJobSchedule(@RequestParam("scheduleId") String scheduleId) throws Exception {
		System.out.println("deleteJobSchedule: " + scheduleId);
		return loginService.deleteJobSchedule(scheduleId);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/savePolicymakerInfo/do.json")
	public @ResponseBody void savePolicymakerInfo(
			@RequestParam("fullName") String fullName,
			@RequestParam("organisation") String organisation,
			@RequestParam("newPassword") String newPassword,
			@RequestParam("changePassword") String changePassword			
			) {
		try {
//			System.out.println("Changing user details to: " + fullName + ", " + organisation + ", " + newPassword + ", " + changePassword);
			loginService.savePolicymakerInfo(fullName, organisation, newPassword, Integer.parseInt(changePassword));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/getPolicymakerSettings/do.json")
	public @ResponseBody PolicymakerSetting[] getPolicymakerSettings() {
		try {
			return loginService.getPolicymakerSettings();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/getPolicymakerSettingByName/do.json")
	public @ResponseBody PolicymakerSetting getPolicymakerSettingByName(@RequestParam("settingName") String settingName) {
		try {
			return loginService.getPolicymakerSetting(settingName);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}
}
