import java.util.ArrayList;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import west.importer.WegovImporter;
import west.wegovdemo.SampleInput;
import west.wegovdemo.TopicOpinionAnalysis;
import west.wegovdemo.TopicOpinionOutput;

import eu.wegov.common.model.JSONActivityArray;
import eu.wegov.common.model.NewActivityAndRun;
import eu.wegov.common.model.TopicOpinionAnalysisResult;
import eu.wegov.coordinator.Coordinator;
import eu.wegov.coordinator.Policymaker;
import eu.wegov.coordinator.web.WidgetDataAsJson;
import eu.wegov.helper.CoordinatorHelper;
import eu.wegov.web.controller.AnalysisController;
import eu.wegov.web.controller.SearchController;
import eu.wegov.web.security.TestWegovLoginService;
import eu.wegov.web.security.WegovLoginService;
import eu.wegov.web.service.SchedulerService;
import eu.wegov.web.service.impl.SchedulerServiceImpl;
import eu.wegov.web.vo.scheduler.JobView;


public class DashboardTest {

    public static void main(String[] args) throws Exception {
    	test2();
    }

    public static void test() throws Exception {
    	CoordinatorHelper helper = new CoordinatorHelper();
    	
    	Coordinator coordinator = helper.getCoordinator();
    	Policymaker policyMaker = coordinator.getPolicymakerByUsername("kem");
    	
    	//SchedulerServiceImpl schedulerService = new SchedulerServiceImpl();
    	//schedulerService.setHelper(helper);
    	
    	//SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
    	//String quartzPropsFile = "C:/Users/kem/Projects/WeGov/workspace/wegov-parent/wegov-dashboard/src/main/resources/quartz";
		////schedulerFactoryBean.setConfigLocation(configLocation);
    	//schedulerService.setSchedulerFactory(schedulerFactoryBean);
    	
    	TestWegovLoginService loginService = new TestWegovLoginService();
    	loginService.setHelper(helper);
		//loginService.setSchedulerService(schedulerService);
		
    	//while (true) {
			//WidgetDataAsJson resultsMetadata = coordinator.getResultsMetadataForRun(184);
//    		coordinator.getRunsByPolicymaker(policyMaker);
  //  		loginService.getLoggedInUserRuns();
        	//Thread.sleep(1000);
		//}
    	
    	//System.out.println();
    	
    }
    
    public static void test2() throws Exception {
    	CoordinatorHelper helper = new CoordinatorHelper();
    	SchedulerServiceImpl schedulerService = new SchedulerServiceImpl();
    	schedulerService.setHelper(helper);
    	
    	SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
    	String quartzPropsFile = "C:/Users/kem/Projects/WeGov/workspace/wegov-parent/wegov-dashboard/src/main/resources/quartz/quartz.properties";
		Resource configLocation = new FileSystemResource(quartzPropsFile);
		schedulerFactoryBean.setConfigLocation(configLocation);
		schedulerFactoryBean.afterPropertiesSet();
    	schedulerService.setSchedulerFactory(schedulerFactoryBean);
    	
    	TestWegovLoginService loginService = new TestWegovLoginService();
    	loginService.setHelper(helper);
		loginService.setSchedulerService(schedulerService);
    	
    	SearchController searchController = new SearchController();
    	searchController.loginService = loginService;
    	
    	AnalysisController analysisController = new AnalysisController();
    	analysisController.loginService = loginService;
    	
    	String scheduleOptions = "";
    	
		//String scheduleOption = "runEveryStartingNowAndRepeat";
		String scheduleOption = "runEveryStartingNowAndStopAt";
		
		//String startTime = "";
		//String startDate = "";
		//String stopTime = "13:25";
		//String stopDate = "";
		
		String startDateTime = "";
		String stopDateTime = "01/06/2012 15:10";
		
		String timeInterval = "1";
		String repeatCount = "";
		
		scheduleOptions = "{\"scheduleOption\": \"" + scheduleOption + "\"," +
				//"\"startTime\": \"" + startTime + "\"," +
				//"\"startDate\": \"" + startDate + "\"," +
				//"\"stopTime\": \"" + stopTime + "\"," +
				//"\"stopDate\": \"" + stopDate + "\"," +

				"\"startDateTime\": \"" + startDateTime + "\"," +
				"\"stopDateTime\": \"" + stopDateTime + "\"," +
				
				"\"timeInterval\": \"" + timeInterval + "\"," +
				"\"repeatCount\": \"" + repeatCount + "\"}";
    	
		String searchTerms = "politics";
		
		String paramsString = "{" +
    			"\"clientSearch\":false," +
    			"\"runNow\":true," +
    			"\"name\":\"" + searchTerms + "\"," +
    			"\"concatenate.list_elements\":\"sources\"" +
    			",\"what.collect\":\"posts\"," +
    			"\"what.words.all\":\"politics\"," +
    			"\"what.words.exactphrase\":\"\"," +
    			"\"what.words.any\":\"\"," +
    			"\"what.words.none\":\"\"," +
    			"\"what.words.hashtags\":\"\"," +
    			"\"what.people.from.accounts\":\"\"," +
    			"\"what.people.to.accounts\":\"\"," +
    			"\"what.people.mentioning.accounts\":\"\"," +
    			"\"what.people.from.groups\":\"\"," +
    			"\"what.people.to.groups\":\"\"," +
    			"\"what.people.mentioning.groups\":\"\"," +
    			"\"what.words.name.idortag\":\"\"," +
    			"\"what.words.name.contains\":\"\"," +
    			"\"what.dates.option\":\"any\"," +
    			"\"what.dates.since\":\"\"," +
    			"\"what.dates.until\":\"\"," +
    			"\"sites\":\"twitter\"," +
    			"\"sources\":\"\"," +
    			"\"location.option\":\"anywhere\"," +
    			"\"location.useapi\":\"true\"," +
    			"\"location.appendtosq\":\"false\"," +
    			"\"location.city\":\"\"," +
    			"\"location.region\":\"\"," +
    			"\"location.countryName\":\"\"," +
    			"\"location.countryCode\":\"\"," +
    			"\"location.lat\":\"\"," +
    			"\"location.long\":\"\"," +
    			"\"location.radius\":\"2\"," +
    			"\"location.radius.unit\":\"mi\"," +
    			"\"language\":\"any\"," +
    			"\"outputOfType\":\"eu.wegov.coordinator.dao.data.WegovPostItem\"," +
    			"\"results.type\":\"static\"," +
    			"\"results.max.results.option\":\"limited\"," +
    			"\"results.max.results\":\"100\"," +
    			"\"results.max.per.page\":\"100\"," +
    			"\"results.max.pages\":\"1\"," +
    			"\"results.max.collection.time.option\":\"\"," +
    			"\"results.max.collection.time\":\"\"," +
    			"\"results.storage.keeprawdata\":\"true\"," +
    			"\"results.storage.storeindb\":\"false\"," +
    			"\"results.collect.since.last.run\":\"false\"," +
				"\"schedule\":" + scheduleOptions
    			+ "}";
    	
    	//NewActivityAndRun activityAndRun = searchController.recordNewSearch(paramsString);
    	//int runId = activityAndRun.getRunId();
    	
    	//schedulerService.getCurrentSchedulerView4Worksheet(1);
		//JSONActivityArray activities = loginService.getLoggedInUserActivities();
		
		//analysisController.doTopicsForRun(266); // passes
		//analysisController.doTopicsForRun(189); // fails
		//analysisController.doTopicsForRun(636); // facebook test
		//TopicOpinionAnalysisResult result = analysisController.doTopicsForRun(638); // twitter test
		//TopicOpinionAnalysisResult result = analysisController.doTopicsForRun(796); // twitter test
		//TopicOpinionAnalysisResult result = analysisController.doTopicsForRun(74); // facebook test
		
		
		//String status = searchController.getSearchStatus(runId);
		//System.out.println("Status of run " + runId + ": " + status);
		
		ArrayList<JobView> scheduledJobs = loginService.getScheduledJobs();
		
		boolean deleted = loginService.deleteJobSchedule("activity_123_1340378670564");
		
		System.out.println("Deleted = " + deleted);
		
    }
    
    public static void test3() {
    	SampleInput input = new SampleInput();
		input.add("Sample post", "hugo");
        input.add("", "ben");

		TopicOpinionAnalysis analysis = new WegovImporter();
		TopicOpinionOutput output = analysis.analyzeTopicsOpinions(input);

    }
}
