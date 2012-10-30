package eu.wegov.web.security;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import eu.wegov.common.model.JSONActivity;
import eu.wegov.common.model.JSONActivityArray;
import eu.wegov.common.model.JSONRun;
import eu.wegov.common.model.JSONRunArray;
import eu.wegov.common.model.JSONUserDetails;
import eu.wegov.converter.SchedulerConverter;
import eu.wegov.coordinator.Activity;
import eu.wegov.coordinator.Configuration;
import eu.wegov.coordinator.ConfigurationSet;
import eu.wegov.coordinator.Parameter;
import eu.wegov.coordinator.Policymaker;
import eu.wegov.coordinator.Run;
import eu.wegov.coordinator.Worksheet;
import eu.wegov.coordinator.web.ConfigParameter;
import eu.wegov.coordinator.web.PolicymakerLocation;
import eu.wegov.coordinator.web.PolicymakerSetting;
import eu.wegov.coordinator.web.Widget;
import eu.wegov.coordinator.web.WidgetDataAsJson;
import eu.wegov.coordinator.web.WidgetSet;
import eu.wegov.helper.CoordinatorHelper;
import eu.wegov.web.service.SchedulerService;
import eu.wegov.web.util.ApplicationConstants;
import eu.wegov.web.util.ApplicationUtils;
import eu.wegov.web.vo.coordinator.SchedulerConfigView;
import eu.wegov.web.vo.scheduler.JobView;

@Service("wegovLoginService")
public class WegovLoginService {

    private static final Logger LOG = Logger.getLogger(WegovLoginService.class);

	private SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	@Autowired(required = false)
	@Qualifier("authenticationManager")
	AuthenticationManager authenticationManager;

	@Autowired
	@Qualifier("coordinatorHelper")
	private transient CoordinatorHelper helper;

    @Autowired
	@Qualifier("schedulerService")
    private transient SchedulerService schedulerService;

    //Used for testing
    public void setHelper(CoordinatorHelper helper) {
    	this.helper = helper;
    }

    //Used for testing
    public void setSchedulerService(SchedulerService schedulerService) {
    	this.schedulerService = schedulerService;
    }

    public PolicymakerSetting getPolicymakerSetting(String settingName) throws Exception {
		return helper.getCoordinator().getPolicymakerSettingByName(getLoggedInUserAsPolicymaker().getID(), settingName);
	}

	public PolicymakerSetting[] getPolicymakerSettings() throws Exception {
		return helper.getCoordinator().getPolicymakerSettings(getLoggedInUserAsPolicymaker().getID());
	}

	public WidgetDataAsJson[] getWidgetData(int wId) throws Exception {
		return helper.getCoordinator().getWidgetData(wId, getLoggedInUserAsPolicymaker().getID());
	}

	public WidgetDataAsJson getResultsMetadataForRun(int runId) throws Exception {
		return helper.getCoordinator().getResultsMetadataForRun(runId, getLoggedInUserAsPolicymaker().getID());
	}

	public WidgetDataAsJson[] getAllResultsMetadataForRun(int runId) throws Exception {
		return helper.getCoordinator().getAllResultsMetadataForRun(runId, getLoggedInUserAsPolicymaker().getID());
	}

	public WidgetDataAsJson getResultsDataForRun(int runId) throws Exception {
		return helper.getCoordinator().getResultsForRun(runId, getLoggedInUserAsPolicymaker().getID());
	}

  public ConfigParameter [] getParametersForRun(int runId) throws Exception {
      return helper.getCoordinator().getParametersForRun(runId, getLoggedInUserAsPolicymaker().getID());
  }


	public WidgetDataAsJson[] getAllResultsForActivity(int activityId) throws Exception {
    return helper.getCoordinator().getAllResultsForActivity(
            activityId, getLoggedInUserAsPolicymaker().getID()
    );
	}

	public WidgetDataAsJson[] getFacebookPostCommentsData(int activityId, String postId) throws Exception {
    return helper.getCoordinator().getFacebookPostCommentsData(
            activityId, getLoggedInUserAsPolicymaker().getID(), postId
    );
	}

      //public WidgetDataAsJson[]  throws SQLException {



	public Widget[] getWidgetsMatchingDataType(String dataType) throws Exception {
		return helper.getCoordinator().getWidgetsMatchingDataType(dataType, getLoggedInUserAsPolicymaker().getID());
	}

	public Widget[] getWidgetsMatchingWidgetType(String widgetType) throws Exception {
		return helper.getCoordinator().getWidgetsMatchingWidgetType(widgetType, getLoggedInUserAsPolicymaker().getID());
	}

        public Widget[] getWidgetsMatchingWidgetCategory(String widgetCategory) throws Exception {
		return helper.getCoordinator().getWidgetsMatchingWidgetCategory(widgetCategory, getLoggedInUserAsPolicymaker().getID());
	}


	public int saveWidgetDataAsJson(int wId, String type, String name, String location, String dataAsJson, Timestamp collected_at) throws Exception {
		return helper.getCoordinator().saveWidgetDataAsJson(wId, getLoggedInUserAsPolicymaker().getID(), type, name, location, dataAsJson, collected_at);
	}

	public int saveRunResultsDataAsJson(
          int runId, String type, String name, String location, int nResults,
          String minId, String maxId, Timestamp minTs, Timestamp maxTs,
          String dataAsJson, Timestamp collected_at) throws Exception {

      return helper.getCoordinator().saveRunResultsDataAsJson(
              runId, type, getLoggedInUserAsPolicymaker().getID(), name, location, nResults
              , minId, maxId, minTs, maxTs, dataAsJson, collected_at);
  }

	public Widget getWidget(int wId) throws Exception {
		return helper.getCoordinator().getWidgetWithIdForPM(wId, getLoggedInUserAsPolicymaker().getID());
	}

	public int getTotalNumberOfWidgets() throws Exception {
		return helper.getCoordinator().getTotalNumberOfWidgets();
	}

	public WidgetSet[] getWidgetSetsForPM() throws Exception {
		return helper.getCoordinator().getWidgetSetsForPM(getLoggedInUserAsPolicymaker().getID());
	}

	public WidgetSet getDefaultWidgetSetForPM() throws Exception {
		return helper.getCoordinator().getDefaultWidgetSetForPM(getLoggedInUserAsPolicymaker().getID());
	}

	public Widget[] getWidgetsForDefaultWidgetSet() throws Exception {
		return helper.getCoordinator().getWidgetsForDefaultWidgetSet(getLoggedInUserAsPolicymaker().getID());
	}

	public Widget[] getVisibleWidgetsForDefaultWidgetSet() throws Exception {
		return helper.getCoordinator().getVisibleWidgetsForDefaultWidgetSet(getLoggedInUserAsPolicymaker().getID());
	}

	public Widget[] getHiddenWidgetsForDefaultWidgetSet() throws Exception {
		return helper.getCoordinator().getHiddenWidgetsForDefaultWidgetSet(getLoggedInUserAsPolicymaker().getID());
	}

	public Widget[] getWidgetsForWidgetSet(int wsId) throws Exception {
		return helper.getCoordinator().getWidgetsForWidgetSet(wsId, getLoggedInUserAsPolicymaker().getID());
	}

	public Widget[] getWidgetsForWidgetSetColumn(int wsId, String columnName) throws Exception {
		return helper.getCoordinator().getWidgetsForWidgetSetColumn(wsId, columnName, getLoggedInUserAsPolicymaker().getID());
	}


	public Widget[] getTemplateWidgets() throws Exception {
    // this function is really here to make it consistent with others - it is
    // not necessary to know the logged in user, but it is easier to put it
    // here than to include all the coordinator stuff in the widget controller
		return helper.getCoordinator().getTemplateWidgets();
	}

	public Widget[] getTemplateWidgetsMatchingWidgetType(String widgetType) throws Exception {
    // this function is really here to make it consistent with others - it is
    // not necessary to know the logged in user, but it is easier to put it
    // here than to include all the coordinator stuff in the widget controller
		return helper.getCoordinator().getTemplateWidgetsMatchingWidgetType(widgetType);
	}

	public Widget[] getTemplateWidgetsMatchingWidgetCategory(String widgetCategory) throws Exception {
    // this function is really here to make it consistent with others - it is
    // not necessary to know the logged in user, but it is easier to put it
    // here than to include all the coordinator stuff in the widget controller
		return helper.getCoordinator().getTemplateWidgetsMatchingWidgetCategory(widgetCategory);
	}

  // HeadsUp analyser specific
  public boolean getHeadsUpEnabledFlag() throws Exception {
    return helper.getCoordinator().getHeadsUpEnabledFlag();
  }

  public String getHeadsUpDataFilePath()throws Exception {
    return helper.getCoordinator().getHeadsUpDataFilePath();
  }


	public void updateOrderAndColumnOfWidgetWithId(int widgetId, String newColumnName, int newOrder) throws Exception {
		helper.getCoordinator().updateOrderAndColumnOfWidgetWithId(widgetId, newColumnName, newOrder);
	}

	public void updateWidgetParameters(int widgetId, String newParametersValue) throws Exception {
		helper.getCoordinator().updateWidgetParameters(widgetId, newParametersValue);
	}

	public void hideWidget(int widgetId) throws Exception {
		helper.getCoordinator().hideWidget(widgetId);
	}

	public void showWidget(int widgetId) throws Exception {
		helper.getCoordinator().showWidget(widgetId);
	}

	public void duplicateWidget(int widgetId, String parametersAsString) throws Exception {
		helper.getCoordinator().duplicateWidget(widgetId, parametersAsString);
	}

	public int duplicateWidgetToCallingUserDefaultSet(
          int widgetId, String parametersAsString) throws Exception {
		//helper.getCoordinator().duplicateWidget(widgetId, parametersAsString);
    int targetPmId = getLoggedInUserAsPolicymaker().getID();
    int pmDefaultWidgetSetId = this.getDefaultWidgetSetForPM().getId();
    int newWidgetId = helper.getCoordinator().duplicateWidgetToNewUserOrWidgetSet(
            widgetId,
            parametersAsString,
            targetPmId,
            pmDefaultWidgetSetId
    );
    return newWidgetId;
	}



	public void deleteWidget(int widgetId) throws Exception {
		helper.getCoordinator().deleteWidget(widgetId);
	}

	public void addNewLocation(String locationName, String locationAddress, String lat, String lon) throws Exception {
		helper.getCoordinator().addNewLocationForPolicymaker(getLoggedInUserAsPolicymaker().getID(), locationName, locationAddress, lat, lon);
	}

	public Activity createNewSearchActivity(String name) throws Exception {
		return helper.getCoordinator().createActivity(getLoggedInUserAsPolicymaker(), name, "search");
	}

	public ArrayList<ConfigurationSet> getTools() throws Exception {
		return helper.getCoordinator().getTools();
	}

	public Activity createNewSearchActivity(JSONObject jsonParams) throws Exception {
		boolean clientSearch = jsonParams.getBoolean("clientSearch");

		System.out.println("clientSearch: " + clientSearch);

		Activity searchActivity;

		if (clientSearch) {
			String searchTerms = jsonParams.getString("searchTerms");
			System.out.println("searchTerms: " + searchTerms);
			searchActivity = createNewSearchActivity(searchTerms);
		}
		else {
			Map<String, String> params = getParamsFromJson(jsonParams);
			String sites = params.get("sites");

			String toolName = "Adv Search";

			if (sites.equals("facebook")) {
				toolName = "Groups Search";
			}

			System.out.println("Selecting tool: " + toolName);

			ConfigurationSet searchConfSet = null;
			ArrayList<ConfigurationSet> tools = getTools();
			for (ConfigurationSet configurationSet : tools) {
				if (configurationSet.getName().equals(toolName)) {
					searchConfSet = configurationSet;
				}
			}

			String activityName = searchConfSet.getName();
			String userActivityName = params.get("name");

			if (userActivityName != null) {
				/*
				if (sites.equals("facebook")) {
					activityName += " for " + userActivityName;
				}
				else {
					activityName += " for \"" + userActivityName + "\"";
				}
				*/
				activityName = userActivityName; // Client can now provide full name for search
			}

			String activityComment = "search";

			searchActivity = addActivity2DefaultWorksheet(activityName, activityComment, searchConfSet, params);
		}

		return searchActivity;
	}

	public Map <String, String> getParamsFromJson(JSONObject jsonParams) {
		HashMap <String, String> params = new HashMap <String, String>();


		Set keys = jsonParams.keySet();
		System.out.println("\nParams:");
		for (Object keyObj : keys) {
			String key = (String)keyObj;
			String value = jsonParams.getString(key);
			params.put(key, value);
			System.out.println(key + ": " + value);
		}

		System.out.println("\n");

		return params;
	}

	private Worksheet getDefaultWorksheet() throws SQLException, Exception {
		ArrayList<Worksheet> worksheets = helper.getCoordinator().getWorksheetsByPolicymaker(getLoggedInUserAsPolicymaker());
		if (worksheets.size() > 0)
			return worksheets.get(0);
		else {
			return helper.getCoordinator().createWorksheet(getLoggedInUserAsPolicymaker(), "default", "default worksheet");
		}
	}

	public Activity createNewAnalysisActivity(String name) throws Exception {
		return helper.getCoordinator().createActivity(getLoggedInUserAsPolicymaker(), name, "analysis");
	}

	public Activity createNewAnalysisActivity(JSONObject jsonParams) throws Exception {

		Activity analysisActivity;

    Map<String, String> params = getParamsFromJson(jsonParams);

    String analysisType = params.get("analysis.type");
    // analysisType is either "topic-opinion" or "behaviour"
    // Defined in addTopicOpinionTool and addKMITool in
    // Database maintenance
    
    String analysisLanguage = params.get("analysisLanguage");


    System.out.println("Selecting analysis type: " + analysisType);

    ConfigurationSet analysisConfSet = null;
    ArrayList<ConfigurationSet> tools = getTools();
    for (ConfigurationSet configurationSet : tools) {
      if (configurationSet.getName().equals(analysisType)) {
        analysisConfSet = configurationSet;
      }
    }

    String activityName = analysisConfSet.getName();
    String userActivityName = params.get("name");

    if (userActivityName != null) {
      activityName = userActivityName; // Client can now provide full name for analysis
    }

    String activityComment = "analysis";

    analysisActivity = addActivity2DefaultWorksheet(activityName, activityComment, analysisConfSet, params);


		return analysisActivity;
	}


    /**
     * Add a new Activity to the default Worksheet.
     * @return
     */
    public Activity addActivity2DefaultWorksheet(String activityName, String activityComment, ConfigurationSet confSet, Map<String,String> params) throws Exception {

        final Worksheet wk = getDefaultWorksheet();
        final Integer worksheetId = wk.getID();
        LOG.info("Adding new Activity for Worksheet [" + worksheetId + "]");

        final Activity act = wk.createActivity(activityName, activityComment);
        LOG.info("New Activity created with id [" + act.getID() + "]");
        LOG.info("Add and clone the Configuration Set retrieved via its ID");
        //Add and clone the Configuration Set retrieved via it's ID
        LOG.info("act.setConfigurationSet");
        act.setConfigurationSet(confSet);
        LOG.info("act.getConfigurationSet");
        final ConfigurationSet cSet = act.getConfigurationSet();
        LOG.info("cSet.getConfigurations");
        final ArrayList < Configuration > cConf = cSet.getConfigurations();
        LOG.info("Add and clone the Configuration Set retrieved via its ID (done)");

        /*
        //start dev code
        LOG.info("Getting activity 49");
        final Activity act = helper.getCoordinator().getActivityByID(49);
        final ConfigurationSet cSet = act.getConfigurationSet();
        LOG.info("cSet.getConfigurations");
        final ArrayList < Configuration > cConf = cSet.getConfigurations();
        LOG.info("Add and clone the Configuration Set retrieved via its ID (done)");
        //end dev code
        */

        /*
         * Now retrieve the parameters from the inner Configuration and replace
         * the value if the value has been retrieved via the interface.
         */

        LOG.info("Retrieve the parameters from the inner Configuration");
        for (final Configuration c : cConf) {
            for (final Parameter p : c.getParameters()) {
                if (params.containsKey(p.getName())) {
                    p.setValue(params.get(p.getName()));
                    LOG.debug("Replaced Configuration parameter ["
                            + p.getName()
                            + "] with value ["
                            + params.get(p.getName())
                            + "]");
                }
            }
        }

        LOG.info("new Activity for Worksheet [" + worksheetId + "] added successfully.");

        return act;
    }

	public void startActivity(Activity activity) throws Exception {
		Worksheet wk = getDefaultWorksheet();
		String activityID = Integer.toString(activity.getID());
		schedulerService.executeActivitiesSubset(wk.getID(), new String[] { activityID });
	}

	public void scheduleActivity(Activity activity, JSONObject scheduleParams) throws Exception {
		Worksheet wk = getDefaultWorksheet();
		String activityID = Integer.toString(activity.getID());
		SchedulerConfigView schedulerConfigView = SchedulerConverter.createSchedulerConfigView(scheduleParams);
		System.out.println("Scheduling activity " + activity.getID() + " for:\n" + schedulerConfigView);
		schedulerService.scheduleActivitiesSubset(wk.getID(), schedulerConfigView, new String[] { activityID });
	}

	public void startSearchActivity(Activity activity) throws Exception {
    startActivity(activity);
	}

	public void scheduleSearchActivity(Activity activity, JSONObject scheduleParams) throws Exception {
    scheduleActivity(activity, scheduleParams);
  }

	public void startAnalysisActivity(Activity activity) throws Exception {
    startActivity(activity);
	}

	public void scheduleAnalysisActivity(Activity activity, JSONObject scheduleParams) throws Exception {
    scheduleActivity(activity, scheduleParams);
  }

	public Run createNewSearchRun(Activity activity, String runName) throws Exception {
		return helper.getCoordinator().createRun(activity, runName, "search");
	}

	public Run createNewAnalysisRun(Activity activity, String runName) throws Exception {
		return helper.getCoordinator().createRun(activity, runName, "analysis");
	}

	public Run getRunById(int runId) throws Exception {
		return helper.getCoordinator().getRunByID(runId);
	}

	public Activity getActivityById(int activityId) throws Exception {
		return helper.getCoordinator().getActivityByID(activityId);
	}

	public void removeLocation(String locationId) throws Exception {
		boolean candelete = false;
		int locationIdAsInt = Integer.parseInt(locationId);

		for (PolicymakerLocation location : getLocationsForPM()) {
			if (location.getId() == locationIdAsInt) {
				candelete = true;
				break;
			}
		}

		if (candelete)
			helper.getCoordinator().removeLocationWithId(locationIdAsInt);
	}

	public PolicymakerLocation[] getLocationsForPM() throws Exception {
		return helper.getCoordinator().getLocationsForPolicymaker(getLoggedInUserAsPolicymaker().getID());
	}

	public LoginStatus getStatus() {
		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		if (auth != null && !auth.getName().equals("anonymousUser")
				&& auth.isAuthenticated()) {
			return new LoginStatus(true, auth.getName());
		} else {
			return new LoginStatus(false, null);
		}
	}

	public String getLoggedInUser() {
		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		if (auth != null && !auth.getName().equals("anonymousUser")
				&& auth.isAuthenticated()) {
			return auth.getName();
		} else {
			return null;
		}
	}

	private Policymaker getLoggedInUserAsPolicymaker() throws Exception {
		return helper.getCoordinator().getPolicymakerByUsername(getLoggedInUser());
	}

	public void savePolicymakerInfo(String fullName, String organisation, String newPassword, int changePassword) throws Exception {
		helper.getCoordinator().savePolicymakerInfo(getLoggedInUserAsPolicymaker().getID(), fullName, organisation, newPassword, changePassword);
	}

	public JSONUserDetails getLoggedInUserDetails() {
		try {
			Policymaker pm = helper.getCoordinator().getPolicymakerByUsername(
					getLoggedInUser());

			return new JSONUserDetails(pm.getName(), pm.getRoles().get(0)
					.getName().trim(), pm.getUserName(), pm.getOrganisation(), pm.getID());


		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public JSONActivityArray getLoggedInUserActivities() {
		try {
			Policymaker pm = helper.getCoordinator().getPolicymakerByUsername(getLoggedInUser());
			Worksheet ws = this.getDefaultWorksheet();

			ArrayList<Activity> activities = helper.getCoordinator().getActivitiesByPolicymaker(pm);
			int numActivities = activities.size();

			JSONActivity[] jsonActivities = new JSONActivity[numActivities];

			Activity activity;
			Timestamp createdDate;
			Timestamp nextStartDate;

			for (int i = 0; i < numActivities; i++) {
				activity = activities.get(i);
				createdDate = activity.getWhenCreated();
				nextStartDate = schedulerService.getNextStartDateForActivity(ws.getID(), activity.getID());

				jsonActivities[i] = new JSONActivity(activity.getID(),
						activity.getName(), activity.getComment(),
						activity.getStatus(),
						createdDate == null ? "" : dateFormat.format(new Date(createdDate.getTime())),
						nextStartDate == null ? "" : dateFormat.format(new Date(nextStartDate.getTime()))
						);
			}

			return new JSONActivityArray(jsonActivities.length, jsonActivities);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public JSONRunArray getLoggedInUserRuns(int activityId) {
		try {
			Policymaker pm = helper.getCoordinator().getPolicymakerByUsername(
					getLoggedInUser());
			ArrayList<Run> runs = helper.getCoordinator().getRunsByPolicymaker(activityId, pm);
			int numRuns = runs.size();

			//JSONRun[] jsonRuns = new JSONRun[numRuns];
			ArrayList<JSONRun> jsonRunsArray = new ArrayList<JSONRun>();

			Run run;
			Timestamp timestampStarted;
			Timestamp timestampFinished;

			for (int i = 0; i < numRuns; i++) {
				run = runs.get(i);
				String comment = run.getComment();
				if (comment.contains("analysis")) {
					continue; // skip any analysis runs (would be better to do in original query)
				}

				String timestampStartedStr = "";
				timestampStarted = run.getWhenStarted();
				if (timestampStarted != null) {
					timestampStartedStr = dateFormat.format(new Date(timestampStarted.getTime()));
				}

				String timestampFinishedStr = "";
				timestampFinished = run.getWhenFinished();
				if (timestampFinished != null) {
					timestampFinishedStr = dateFormat.format(new Date(timestampFinished.getTime()));
				}

				String resultsSummary = "";

				if (run.getStatus().equals(Run.STATUS_FINISHED)) {
					LOG.debug("Run " + run.getID() + " finished. Getting results metadata");
					WidgetDataAsJson[] resultsMetadata = getAllResultsMetadataForRun(run.getID());
					if (resultsMetadata == null) {
						LOG.debug("WARNING: no results metadata available for run " + run.getID());
						resultsSummary = "No results available";
					}
					else {
						/*
						int nResults = resultsMetadata.getnResults();
						String type = resultsMetadata.getType();
						resultsSummary = nResults + " " + type;
						*/

						int countPostsWithComments = 0;
						int totalComments = 0;

						for (WidgetDataAsJson resultMetadata : resultsMetadata) {
							int nResults = resultMetadata.getnResults();
							String type = resultMetadata.getType();
							String summary = nResults + " " + type;
							//System.out.println(run.getID() + " " + summary);

							if ( type.startsWith("posts-") || type.startsWith("comments-") ) {
								String[] typeFrags = type.split("-");
								String postType = typeFrags[0];
								String sns = typeFrags[1];
								//resultsSummary = summary; //TODO add summary of available comments for post
								resultsSummary = nResults + " " + sns + " " + postType;
							}
							else if (type.startsWith("post-comments-facebook")) {
								countPostsWithComments++;
								totalComments += nResults;
							}
						}

						if (countPostsWithComments > 0) {
							//resultsSummary += ", " + countPostsWithComments + " post-comments-facebook";
							resultsSummary += ",</br> " + totalComments + " facebook comments for latest " + countPostsWithComments + " posts";
						}

						//System.out.println(run.getID() + " " + resultsSummary);
					}
				}

				//jsonRuns[i] = new JSONRun(run.getID(), run.getActivityId(),
				JSONRun jsonRun = new JSONRun(run.getID(), run.getActivityId(),
						run.getName(), run.getComment(),
						run.getStatus(), timestampStartedStr, timestampFinishedStr, resultsSummary);

				jsonRunsArray.add(jsonRun);
			}

			JSONRun[] jsonRuns = jsonRunsArray.toArray(new JSONRun[jsonRunsArray.size()]);

			return new JSONRunArray(jsonRuns.length, jsonRuns);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public ArrayList<JobView> getScheduledJobs() throws Exception {
		Worksheet ws = getDefaultWorksheet();
		ArrayList<JobView> scheduledJobs = schedulerService.getCurrentSchedulerView4Worksheet(ws.getID());
		return scheduledJobs;
	}

	public boolean deleteJobSchedule(String name) throws SQLException, Exception {
		Worksheet ws = getDefaultWorksheet();
		String group = schedulerService.getJobGroup(ws.getID());
		boolean deleted = schedulerService.removeJob(name, group);
		if (deleted) {
			System.out.println("Deleted schedule " + name + " in group " + group);
		}
		else {
			System.out.println("Failed to delete schedule " + name + " in group " + group);
		}
		return deleted;
	}

	public boolean ifCredentialsMatch(String username, String password) {
		Policymaker userTryingToLogin;
		boolean pwdCheck = false;

		// System.out.println("Authenticating user: " + username +
		// " with password: " + password);

		try {

			userTryingToLogin = helper.getCoordinator()
					.getPolicymakerByUsername(username);

			if (userTryingToLogin != null) {
				// System.out.println("User was found, checking password");
				pwdCheck = userTryingToLogin.isPassword(password);
				System.out.println("Password check: " + pwdCheck + " for user "
						+ username);
			} else {
				System.out.println("User not found: " + username);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new BadCredentialsException("Database or file error");
		}

		return pwdCheck;
	}


}
