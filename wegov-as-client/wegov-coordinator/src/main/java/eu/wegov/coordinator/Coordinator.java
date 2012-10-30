/////////////////////////////////////////////////////////////////////////
//
// ¬© University of Southampton IT Innovation Centre, 2011
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
//	Created By :			Maxim Bashevoy
//	Created Date :			2011-07-26
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.wegov.coordinator.dao.data.WegovAnalysisKmiBuzzTopPost;
import eu.wegov.coordinator.dao.data.WegovAnalysisKmiBuzzTopUser;
import eu.wegov.coordinator.dao.data.WegovAnalysisKmiDa;
import eu.wegov.coordinator.dao.data.WegovAnalysisKmiDaMostActiveUser;
import eu.wegov.coordinator.dao.data.WegovAnalysisKoblenzMessage;
import eu.wegov.coordinator.dao.data.WegovAnalysisKoblenzTopic;
import eu.wegov.coordinator.dao.data.WegovAnalysisKoblenzUser;
import eu.wegov.coordinator.dao.data.WegovAnalysisKoblenzViewpoint;
import eu.wegov.coordinator.dao.data.WegovFollower;
import eu.wegov.coordinator.dao.data.WegovFriend;
import eu.wegov.coordinator.dao.data.WegovGroupAdmin;
import eu.wegov.coordinator.dao.data.WegovGroupMember;
import eu.wegov.coordinator.dao.data.WegovGroupPost;
import eu.wegov.coordinator.dao.data.WegovLike;
import eu.wegov.coordinator.dao.data.WegovMentionedUser;
import eu.wegov.coordinator.dao.data.WegovPostComment;
import eu.wegov.coordinator.dao.data.WegovPostItem;
import eu.wegov.coordinator.dao.data.WegovPostTag;
import eu.wegov.coordinator.dao.data.WegovSNS;
import eu.wegov.coordinator.dao.data.WegovSnsGroup;
import eu.wegov.coordinator.dao.data.WegovSnsUserAccount;
import eu.wegov.coordinator.dao.data.WegovTag;
import eu.wegov.coordinator.dao.data.WegovWidgetDataAsJson;
import eu.wegov.coordinator.dao.data.twitter.Hashtag;
import eu.wegov.coordinator.dao.data.twitter.Tweet;
import eu.wegov.coordinator.dao.data.twitter.Url;
import eu.wegov.coordinator.dao.data.twitter.User;
import eu.wegov.coordinator.dao.data.twitter.UserMention;
import eu.wegov.coordinator.dao.mgt.WegovActivity;
import eu.wegov.coordinator.dao.mgt.WegovActivity_ConfigurationSet;
import eu.wegov.coordinator.dao.mgt.WegovActivity_InputActivity;
import eu.wegov.coordinator.dao.mgt.WegovActivity_Run;
import eu.wegov.coordinator.dao.mgt.WegovConfiguration;
import eu.wegov.coordinator.dao.mgt.WegovConfigurationSet;
import eu.wegov.coordinator.dao.mgt.WegovConfigurationSet_Configuration;
import eu.wegov.coordinator.dao.mgt.WegovConfiguration_Parameter;
import eu.wegov.coordinator.dao.mgt.WegovParameter;
import eu.wegov.coordinator.dao.mgt.WegovParameter_Role;
import eu.wegov.coordinator.dao.mgt.WegovPolicymaker;
import eu.wegov.coordinator.dao.mgt.WegovPolicymakerLocation;
import eu.wegov.coordinator.dao.mgt.WegovPolicymakerRole;
import eu.wegov.coordinator.dao.mgt.WegovPolicymaker_Activity;
import eu.wegov.coordinator.dao.mgt.WegovPolicymaker_PolicymakerRole;
import eu.wegov.coordinator.dao.mgt.WegovPolicymaker_Run;
import eu.wegov.coordinator.dao.mgt.WegovPolicymaker_Settings;
import eu.wegov.coordinator.dao.mgt.WegovPolicymaker_Task;
import eu.wegov.coordinator.dao.mgt.WegovPolicymaker_TwitterOauthAccount;
import eu.wegov.coordinator.dao.mgt.WegovPolicymaker_WegovWorksheet;
import eu.wegov.coordinator.dao.mgt.WegovPolicymaker_Workflow;
import eu.wegov.coordinator.dao.mgt.WegovRun;
import eu.wegov.coordinator.dao.mgt.WegovRun_ConfigurationSet;
import eu.wegov.coordinator.dao.mgt.WegovRun_Error;
import eu.wegov.coordinator.dao.mgt.WegovRun_Log;
import eu.wegov.coordinator.dao.mgt.WegovTask;
import eu.wegov.coordinator.dao.mgt.WegovTwitterOauthAccount;
import eu.wegov.coordinator.dao.mgt.WegovWidget;
import eu.wegov.coordinator.dao.mgt.WegovWidgetSet;
import eu.wegov.coordinator.dao.mgt.WegovWorkflow;
import eu.wegov.coordinator.dao.mgt.WegovWorksheet;
import eu.wegov.coordinator.dao.mgt.WegovWorksheet_Activity;
import eu.wegov.coordinator.sql.PostgresConnector;
import eu.wegov.coordinator.sql.SqlDatabase;
import eu.wegov.coordinator.sql.SqlSchema;
import eu.wegov.coordinator.sql.SqlTable;
import eu.wegov.coordinator.utils.Util;
import eu.wegov.coordinator.web.ConfigParameter;
import eu.wegov.coordinator.web.PolicymakerLocation;
import eu.wegov.coordinator.web.PolicymakerSetting;
import eu.wegov.coordinator.web.Widget;
import eu.wegov.coordinator.web.WidgetDataAsJson;
import eu.wegov.coordinator.web.WidgetSet;
//import eu.wegov.coordinator.web.Parameter;

/**
 * Coordinator is the main object in Wegov Coordinator library. It only requires correct properties file to initialize, for example:<br /><br />
 <code>
    # POSTGRESQL SERVER CONFIGURATION (admin access)<br />
    postgres.url=jdbc:postgresql://localhost:5432/<br />
    postgres.login=postgres<br />
    postgres.pass=test<br /><br />

    # POSTGRESQL DATABASES AND SCHEMAS CONFIGURATION<br />
    postgres.database.name=WeGov<br />
    postgres.database.mgt.name=WeGovManagement<br />
    postgres.database.data.name=WeGovRawData<br /><br />
 </code>
 *
 * Wegov Coordinator provides a link between the Dashboard, Components (Search, Analysis and Injection), and the Database.<br /><br />
 *
 * Policymakers (users) interact with the whole Wegov system using the Dashboard. Dashboard redirects user’s actions and data requests to Components and the Database through the Coordinator. Coordinator also handles management of all actions and persistence of necessary data.<br /><br />

 * Search, Injection and Analysis Components are the tools that Policymakers (users) use to accomplish their goal within the scope of the project: inject posts into social networks, gather and analyse social network users’ opinions and reactions, etc. Components also make user of Coordinator to store their output and query in the Database.<br /><br />

 * The Database stores all information gathered and needed by the Components along with full management information for Wegov system. The Coordinator provides a comprehensive set of tools to interact with the Database.<br /><br /> Example:<br /><br />
<code>
        Coordinator coordinator = new Coordinator("coordinator.properties");<br />
        coordinator.wipeDatabase(); // optional - removes everything from the database<br />
        coordinator.setupWegovDatabase(); // you HAVE TO call this method in order to proceed<br /><br />

        // Create new policymaker Max as administrator<br />
        String maxFullName = "Maxim Bashevoy";<br />
        String maxOrganisation = "IT Innovation";<br />
        String maxUserName = "mbashevoy";<br />
        String maxPassword = "password";<br />
        Role maxRole = coordinator.getDefaultAdminRole();<br /><br />

        Policymaker max = coordinator.createPolicyMaker(maxFullName, maxRole, maxOrganisation, maxUserName, maxPassword);<br /><br />

        // Assign Max new custom role<br />
        Role testRole = coordinator.createRole("tester", "Just a test role");<br />
        max.addRole(testRole);<br /><br />

        // Print info about Max<br />
        System.out.println("Information about user max:");<br />
        System.out.println("\t-" + max);<br /><br />

        // Check Max's password<br />
        String randomPassword = "123";<br />
        String correctPassword = maxPassword;<br />
        System.out.println("Is max\'s password \'" + randomPassword + "\'? " + max.isPassword(randomPassword));<br />
        System.out.println("Is max\'s password \'" + correctPassword + "\'? " + max.isPassword(correctPassword));<br /><br />

        // Create new user Tom with default user role:<br />
        Policymaker tom = coordinator.createPolicyMaker("Tom Smith", "Microsoft", "tom123", "qwerty");<br />
        System.out.println("Information about user tom:");<br />
        System.out.println("\t-" + tom);<br /><br />

        // Print information about all users:<br />
        System.out.println("Information about all users:");<br />
        for (Policymaker pm : coordinator.getPolicymakers()) {<br />
            &nbsp;&nbsp;&nbsp;System.out.println("\t-" + pm);<br />
        }<br /><br />

        // Max creates new worksheet<br />
        Worksheet worksheet1 = coordinator.createWorksheet(max, "Test worksheet", "Description of new worksheet");<br /><br />

        // Max creates new activities:<br />
        Activity activity1 = worksheet1.createActivity("Search activity", "We are going to search for something here..."); // Added to worksheet straight away<br />
        Activity activity2 = coordinator.createActivity(max, "Analysis activity", "...and then do analysis!");<br />
        worksheet1.addActivity(activity2); // only needed for activity2 as it was created via coordinator, not worksheet<br /><br />

        // Max adds new configuration to activity1<br />
        Configuration configuration1 = coordinator.createConfiguration("Search Twitter tool", "java -version", "Configuration for Search Twitter Tool - test only");<br />
        configuration1.addParameterAsUser("searchQuery", "What goes into the search box", "bbc");<br />
        configuration1.addParameterAsAdmin("returnFormat", "Do we want Twitter to return XML or JSON?", "json");<br />
        activity1.addConfiguration(configuration1);<br /><br />

        // Tom creates new worksheet<br />
        Worksheet worksheet2 = coordinator.createWorksheet(tom, "Another Test worksheet", "Description of another new worksheet");  <br /><br />

        // Print information about all worksheets:<br />
        System.out.println("Information about all worksheets:");<br />
        for (Worksheet ws : coordinator.getWorksheets()) {<br />
            &nbsp;&nbsp;&nbsp;System.out.println("\t-" + ws);<br />
            &nbsp;&nbsp;&nbsp;System.out.println("\t\t- Created by: " + ws.getPolicyMaker());<br />
            &nbsp;&nbsp;&nbsp;System.out.println("\t\t- Activities: ");<br /><br />

            &nbsp;&nbsp;&nbsp;for (Activity activity : ws.getActivities()) {<br />
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println("\t\t\t- " + activity.toString().replaceAll("- Configuration", "\t\t\t- Configuration").replaceAll("- Parameter", "\t\t\t\t- Parameter"));<br />
            &nbsp;&nbsp;&nbsp;}<br />
        }<br /><br />

        System.out.println("Parameters in configuration1 for role admin:");<br />
        for (Parameter p : activity1.getConfigurationSet().get(0).getParametersForRole(coordinator.getDefaultAdminRole()))<br />
            &nbsp;&nbsp;&nbsp;System.out.println("\t- " + p);<br /><br />
 * </code>
 Output:<br /><br />
 <code>
Information about user max:<br />
	&nbsp;&nbsp;&nbsp;-[1] 'Maxim Bashevoy' (username: 'mbashevoy', roles: admin, tester) from 'IT Innovation', startThreaded date: '2011-08-26 14:06:47.225', end date: 'null'<br />
Is max's password '123'? false<br />
Is max's password 'password'? true<br />
Information about user tom:<br />
	&nbsp;&nbsp;&nbsp;-[2] 'Tom Smith' (username: 'tom123', roles: user) from 'Microsoft', startThreaded date: '2011-08-26 14:06:47.959', end date: 'null'<br />
Information about all users:<br />
	&nbsp;&nbsp;&nbsp;-[1] 'Maxim Bashevoy' (username: 'mbashevoy', roles: admin, tester) from 'IT Innovation', startThreaded date: '2011-08-26 14:06:47.225', end date: 'null'<br />
	&nbsp;&nbsp;&nbsp;-[2] 'Tom Smith' (username: 'tom123', roles: user) from 'Microsoft', startThreaded date: '2011-08-26 14:06:47.959', end date: 'null'<br />
Information about all worksheets:<br />
	&nbsp;&nbsp;&nbsp;-[1] 'Test worksheet' (Description of new worksheet), status: 'initialising', created: '2011-08-26 14:06:48.104'<br />
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- Created by: [1] 'Maxim Bashevoy' (username: 'mbashevoy', roles: admin, tester) from 'IT Innovation', startThreaded date: '2011-08-26 14:06:47.225', end date: 'null'<br />
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- Activities: <br />
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- [1] 'Search activity' (We are going to search for something here...), status: 'initialising', created: '2011-08-26 14:06:48.543'.<br />
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- Configuration name: 'Search Twitter tool', description: 'Configuration for Search Twitter Tool - test only', command: 'java -version'.<br />
					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- Parameter name: 'searchQuery' (What goes into the search box), value: 'bbc', roles: 'user'.<br />
					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- Parameter name: 'returnFormat' (Do we want Twitter to return XML or JSON?), value: 'json', roles: 'admin'.<br />

			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- [2] 'Analysis activity' (...and then do analysis!), status: 'initialising', created: '2011-08-26 14:06:49.899'.<br />
	&nbsp;&nbsp;&nbsp;-[2] 'Another Test worksheet' (Description of another new worksheet), status: 'initialising', created: '2011-08-26 14:06:50.631'<br />
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- Created by: [2] 'Tom Smith' (username: 'tom123', roles: user) from 'Microsoft', startThreaded date: '2011-08-26 14:06:47.959', end date: 'null'<br />
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- Activities: <br />
Parameters in configuration1 for role admin:<br />
	&nbsp;&nbsp;&nbsp;- Parameter name: 'returnFormat' (Do we want Twitter to return XML or JSON?), value: 'json', roles: 'admin'. <br /><br />
 </code>
 *
 * @author Maxim Bashevoy
 */
public class Coordinator {
    //private PostgresConnector connector;
	private HashMap<String, PostgresConnector> connectors;
    private SqlDatabase database;
    private SqlSchema mgtSchema;
    private SqlSchema dataSchema;
    private String databaseName;
    private String mgtSchemaName;
    private String dataSchemaName;
    private String absolutePathToConfigurationFile;
    private String pathToKmiDataFolder;
    private String templateWidgetsSourceUser;
    private String templateWidgetsSourceUserPassword;
    private Policymaker templateWidgetsUser = null;
    private boolean headsUpEnabled = false;
    private String headsUpDataFilePath = null;

    private Util util = new Util();
	private final SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
	private Properties properties;

    private final static Logger logger = Logger.getLogger(Coordinator.class.getName());

    /**
     * Default administrator's role name: "admin".
     */
    public final static String DEFAULT_ADMIN_ROLE_NAME = "admin";

    /**
     * Default administrator's role description: "Administrator role for cool things".
     */
    public final static String DEFAULT_ADMIN_ROLE_DESCRIPTION = "Administrator role for cool things";

    /**
     * Default user's role name: "user".
     */
    public final static String DEFAULT_USER_ROLE_NAME = "user";

    /**
     * Default user's role description: "User role for useful things".
     */
    public final static String DEFAULT_USER_ROLE_DESCRIPTION = "User role for useful things";

    /**
     * Create coordinator using path to the configuration file. For the example of one see top of the page.
     */
    public Coordinator(String pathToConfigurationFile) throws FileNotFoundException, IOException, MalformedURLException, ClassNotFoundException, SQLException {



        // counting methods on the database

        // DONE links to original posts (tweets, facebook updates etc.)
        // DONE links to original post user - all in a postitem?

        // DONE add profile picture URL to the user's table!


        logger.debug("New Coordinator initialising with the following path: " + pathToConfigurationFile);

        // Get properties from configuration file:
        properties = new Properties();
        File tempFile = new File(pathToConfigurationFile);
        FileInputStream in = new FileInputStream(tempFile.getAbsolutePath());
        properties.load(in);
        in.close();

        logger.debug("Resolved path to: " + tempFile.getAbsolutePath());

        // Check all properties are there and set:
        String error = verifyProperties(properties);
        if (error != null)
            throw new RuntimeException(error);

        // Create connector and set database names
        //this.connector = new PostgresConnector(properties.getProperty("postgres.url"), properties.getProperty("postgres.login"), properties.getProperty("postgres.pass"));
        this.databaseName = properties.getProperty("postgres.database.name");
        this.mgtSchemaName = properties.getProperty("postgres.database.mgt.name");
        this.dataSchemaName = properties.getProperty("postgres.database.data.name");
        this.absolutePathToConfigurationFile = tempFile.getAbsolutePath();
        this.pathToKmiDataFolder = properties.getProperty("kmi.pathto.datafolder");

        // Psuedo user that holds template widgets -
        // when a real user creates a widget it is
        // copied from the set of templates held
        // by this template user
        this.templateWidgetsSourceUser = properties.getProperty("wegov.widget.template.source.user");
        this.templateWidgetsSourceUserPassword = properties.getProperty("wegov.widget.template.source.user.password");


        //optional headsup analysis
        if (properties.getProperty("wegov.headsup.enabled").equalsIgnoreCase("true") != true) {
          this.headsUpEnabled = false;
        }
        else {
          this.headsUpEnabled = true;

          // only set file path if config is true
          this.headsUpDataFilePath = properties.getProperty("wegov.headsup.source.file");
        }


        //this.templateWidgetsUser = getPolicymakerByUsername(this.templateWidgetsSourceUser);
/*
        Role adminRole = this.getDefaultAdminRole();
        this.templateWidgetsUser = this.createPolicyMaker(
                "Wegov Template Widgets", adminRole, "WeGov",
                this.templateWidgetsSourceUser,
                this.templateWidgetsSourceUserPassword
        );
*/
        /*
        //KEM create a connector for each schema
        PostgresConnector mgtConnector = new PostgresConnector(
                properties.getProperty("postgres.url"),
                properties.getProperty("postgres.login"),
                properties.getProperty("postgres.pass")
        );

        mgtConnector.useDatabase(databaseName);
        mgtConnector.getSchema(mgtConnector.getDatabase(), mgtSchemaName);
        mgtConnector.useSchema(mgtSchemaName);

        PostgresConnector dataConnector = new PostgresConnector(
                properties.getProperty("postgres.url"),
                properties.getProperty("postgres.login"),
                properties.getProperty("postgres.pass")
        );

        dataConnector.useDatabase(databaseName);
        dataConnector.getSchema(dataConnector.getDatabase(), dataSchemaName);
        dataConnector.useSchema(dataSchemaName);

        //KEM add connectors to HashMap
        connectors = new HashMap<String, PostgresConnector>();
        connectors.put(mgtSchemaName, mgtConnector);
        connectors.put(dataSchemaName, dataConnector);

        this.database = getMgtConnector().getDatabase();
        */

        if (!pathToKmiDataFolder.endsWith("/"))
            pathToKmiDataFolder += "/";

        logger.debug("Finished initialising Coordinator. Use setupWegovDatabase() method to proceed.");
    }

    public boolean getHeadsUpEnabledFlag() {
    		return this.headsUpEnabled;
    }

    public String getHeadsUpDataFilePath() {
    		if (this.headsUpEnabled == true) {
          return this.headsUpDataFilePath;
        }
        else {
          return null;
        }
    }


    public PostgresConnector getMgtConnector() {
    	if (connectors == null)
    		return null;
    	return connectors.get(mgtSchemaName);
    }

    public PostgresConnector getDataConnector() {
    	if (connectors == null)
    		return null;
    	return connectors.get(dataSchemaName);
    }

    /*
     * Web methods
     */
    public PolicymakerSetting getPolicymakerSettingByName(int pmId, String settingName) throws Exception {
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	map.put("PolicymakerID", pmId);
    	map.put("SettingName", settingName);

    	ArrayList<WegovPolicymaker_Settings> fromDb = getMgtSchema().getAllWhere(new WegovPolicymaker_Settings(), map);

    	if (fromDb.isEmpty())
    		return null;
    	else {
    		WegovPolicymaker_Settings settingFromDb = fromDb.get(0);
    		return new PolicymakerSetting(settingFromDb.getSettingName(), settingFromDb.getSettingValue());
    	}
    }

    public PolicymakerSetting[] getPolicymakerSettings(int pmId) throws Exception {
    	ArrayList<WegovPolicymaker_Settings> fromDb = getMgtSchema().getAllWhere(new WegovPolicymaker_Settings(), "PolicymakerID", pmId);
    	PolicymakerSetting[] result = new PolicymakerSetting[fromDb.size()];

    	for (int i = 0; i < fromDb.size(); i++) {
    		WegovPolicymaker_Settings settingFromDb = fromDb.get(i);
    		result[i] = new PolicymakerSetting(settingFromDb.getSettingName(), settingFromDb.getSettingValue());
    	}

    	return result;
    }

    public int createPolicymakerSetting(int pmId, String settingName, String settingValue) throws SQLException {
    	String newid = getMgtSchema().insertObject(new WegovPolicymaker_Settings(pmId, settingName, settingValue));
    	return Integer.parseInt(newid);
    }


    /**
     * Returns special user that holds template widgets
     */

    public Policymaker getTemplateWidgetsSourceUser() throws Exception {
      if (this.templateWidgetsUser != null) {
        return this.templateWidgetsUser;
      }
      else {
        throw new Exception ("Null template widgets user");
      }

    }


	public WidgetDataAsJson[] getAllResultsMetadataForRun(int runId, int pmId) throws Exception {
		//System.out.println("getAllResultsMetadataForRun: " + runId);
		WegovWidgetDataAsJson[] resultsForRun = getAllResultsForRun(runId, pmId, false);
		if (resultsForRun == null) {
			//System.out.println("getAllResultsMetadataForRun: " + runId + " returned null data");
			return null;
		}

		WidgetDataAsJson[] results = new WidgetDataAsJson[resultsForRun.length];

		for (int i = 0; i < resultsForRun.length; i++) {
			WegovWidgetDataAsJson data = resultsForRun[i];
			WidgetDataAsJson result = new WidgetDataAsJson(data.getId(), data.getWidgetId(), data.getPmId(), data.getActivityId(), data.getRunId(), data.getType(),
					data.getName(), data.getLocation(), data.getNumResults(), data.getMinId(), data.getMaxId(), null, df.format(data.getTimeCollected()));
			results[i] = result;
		}

		return results;
	}

	public WidgetDataAsJson getResultsMetadataForRun(int runId, int pmId) throws Exception {
		//System.out.println("getResultsMetadataForRun: " + runId);
		WegovWidgetDataAsJson data = getResultsForRun(runId, pmId, false);
		if (data == null) {
			//System.out.println("getResultsMetadataForRun: " + runId + " returned null data");
			return null;
		}
		WidgetDataAsJson result = new WidgetDataAsJson(data.getId(), data.getWidgetId(), data.getPmId(), data.getActivityId(), data.getRunId(), data.getType(),
				data.getName(), data.getLocation(), data.getNumResults(), data.getMinId(), data.getMaxId(), null, df.format(data.getTimeCollected()));
		return result;
	}

	public WidgetDataAsJson getResultsForRun(int runId, int pmId) throws Exception {
		WegovWidgetDataAsJson data = getResultsForRun(runId, pmId, true);
		WidgetDataAsJson result = new WidgetDataAsJson(
            data.getId(),
            data.getWidgetId(),
            data.getPmId(),
            data.getActivityId(),
            data.getRunId(),
            data.getType(),
            data.getName(),
            data.getLocation(),
            data.getNumResults(),
            data.getMinId(),
            data.getMaxId(),
            data.getDataAsJson(),
            df.format(data.getTimeCollected()));
		return result;
	}

    public WidgetDataAsJson[] getWidgetData(int wId, int pmId) throws Exception {
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	map.put("widgetid", wId);
    	map.put("pmid", pmId);

    	ArrayList<WegovWidgetDataAsJson> widgetDataFromDb = getDataSchema().getAllWhereSortBy(new WegovWidgetDataAsJson(), map, "collected_at");

    	if (widgetDataFromDb.isEmpty()) {
    		return null;
    	} else {
    		WidgetDataAsJson[] result = new WidgetDataAsJson[widgetDataFromDb.size()];
    		int counter = 0;
    		for (WegovWidgetDataAsJson data : widgetDataFromDb) {
    			result[counter] = new WidgetDataAsJson(data.getId(), data.getWidgetId(), data.getType(), data.getName(), data.getLocation(), data.getDataAsJson(), df.format(data.getTimeCollected()));
    			counter++;
    		}
    		return result;
    	}
    }

    public Widget[] getWidgetsMatchingDataType(String datatype, int pmId) throws Exception {
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	map.put("datatype", datatype);
    	map.put("policymakerId", pmId);
    	DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");

    	ArrayList<WegovWidget> widgetDataFromDb = getMgtSchema().getAllWhereSortBy(new WegovWidget(), map, "id");

    	if (widgetDataFromDb.isEmpty()) {
    		return null;
    	} else {
    		Widget[] result = new Widget[widgetDataFromDb.size()];
    		int counter = 0;
    		for (WegovWidget data : widgetDataFromDb) {
    			//result[counter] = new Widget(data.getId(), data.getWidgetId(), data.getType(), data.getName(), data.getLocation(), data.getDataAsJson(), df.format(data.getTimeCollected()));
        result[counter] = new Widget(
                data.getId(), data.getWidgetsetId(), data.getColumnName(),
                data.getColumnOrderNumber(), data.getPolicymakerId(),
                data.getName(),
                data.getDescription(),
                data.getWidgetCategory(), data.getType(),
                data.getDatatype(), data.getDataAsString(),
                data.getParametersAsString(), data.isVisible(),
                data.getLabelText());
    			counter++;
    		}
    		return result;
    	}
    }

    public Widget[] getWidgetsMatchingWidgetType(String widgetType, int pmId) throws Exception {
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	map.put("type", widgetType);
    	map.put("policymakerId", pmId);
    	DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");

    	ArrayList<WegovWidget> widgetDataFromDb = getMgtSchema().getAllWhereSortBy(new WegovWidget(), map, "id");

    	if (widgetDataFromDb.isEmpty()) {
    		return null;
    	} else {
    		Widget[] result = new Widget[widgetDataFromDb.size()];
    		int counter = 0;
    		for (WegovWidget data : widgetDataFromDb) {
    			//result[counter] = new Widget(data.getId(), data.getWidgetId(), data.getType(), data.getName(), data.getLocation(), data.getDataAsJson(), df.format(data.getTimeCollected()));
        result[counter] = new Widget(
                data.getId(), data.getWidgetsetId(), data.getColumnName(),
                data.getColumnOrderNumber(), data.getPolicymakerId(),
                data.getName(),
                data.getDescription(),
                data.getWidgetCategory(), data.getType(),
                data.getDatatype(), data.getDataAsString(),
                data.getParametersAsString(), data.isVisible(),
                data.getLabelText());
    			counter++;
    		}
    		return result;
    	}
    }

    public Widget[] getWidgetsMatchingWidgetCategory(String widgetCategory, int pmId) throws Exception {
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	map.put("widgetCategory", widgetCategory);
    	map.put("policymakerId", pmId);
    	DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");

    	ArrayList<WegovWidget> widgetDataFromDb = getMgtSchema().getAllWhereSortBy(new WegovWidget(), map, "id");

    	if (widgetDataFromDb.isEmpty()) {
    		return null;
    	} else {
    		Widget[] result = new Widget[widgetDataFromDb.size()];
    		int counter = 0;
    		for (WegovWidget data : widgetDataFromDb) {
    			//result[counter] = new Widget(data.getId(), data.getWidgetId(), data.getType(), data.getName(), data.getLocation(), data.getDataAsJson(), df.format(data.getTimeCollected()));
        result[counter] = new Widget(
                data.getId(), data.getWidgetsetId(), data.getColumnName(),
                data.getColumnOrderNumber(), data.getPolicymakerId(),
                data.getName(),
                data.getDescription(),
                data.getWidgetCategory(), data.getType(),
                data.getDatatype(), data.getDataAsString(),
                data.getParametersAsString(), data.isVisible(),
                data.getLabelText());
    			counter++;
    		}
    		return result;
    	}
    }

    public int saveWidgetDataAsJson(int wsId, int pmId, String type, String name, String location, String dataAsJson, Timestamp collected_at) throws SQLException {
    	int activityId = 0;
		int runId = 0;

		//TODO pass these as input parameters?
		int nResults = 0;
		String minId = "";
		String maxId = "";
		Timestamp minTs = null;
		Timestamp maxTs = null;

		String newWDataid = getDataSchema().insertObject(new WegovWidgetDataAsJson(wsId, pmId, activityId, runId, type, name, location, nResults, minId, maxId, minTs, maxTs, dataAsJson, collected_at));
    	return Integer.parseInt(newWDataid);
    }

    public int saveRunResultsDataAsJson(int wsId, int runId, String type, String name, String location, int nResults, String minId, String maxId, Timestamp minTs, Timestamp maxTs, String dataAsJson, Timestamp collected_at) throws SQLException {
    	Run run = getRunByID(runId);
    	Activity activity = run.getActivity();
    	int activityId = activity.getID();
    	Policymaker pm = activity.getPolicyMaker();
    	int pmId = pm.getID();
    	String newWDataid = getDataSchema().insertObject(new WegovWidgetDataAsJson(wsId, pmId, activityId, runId, type, name, location, nResults, minId, maxId, minTs, maxTs, dataAsJson, collected_at));
    	return Integer.parseInt(newWDataid);
    }

    public int saveRunResultsDataAsJson(int runId, String type, int pmId, String name, String location, int nResults, String minId, String maxId, Timestamp minTs, Timestamp maxTs, String dataAsJson, Timestamp collected_at) throws SQLException {
    	Run run = getRunByID(runId);
    	Activity activity = run.getActivity();
    	int activityId = activity.getID();
//    	Policymaker pm = activity.getPolicyMaker();
  //  	int pmId = pm.getID();
    	String newWDataid = getDataSchema().insertObject(new WegovWidgetDataAsJson(0, pmId, activityId, runId, type, name, location, nResults, minId, maxId, minTs, maxTs, dataAsJson, collected_at));
    	return Integer.parseInt(newWDataid);
    }





    public WidgetDataAsJson[] getAllResultsForActivity(int activityId, int pmId) throws SQLException {
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	map.put("activityid", activityId);
      map.put("pmid", pmId);

    	ArrayList<WegovWidgetDataAsJson> dbResults = getDataSchema().getAllWhere(new WegovWidgetDataAsJson(), map);

      WidgetDataAsJson[] resultsData = new WidgetDataAsJson[dbResults.size()];
    	for (int i = 0; i < dbResults.size(); i++) {
        resultsData[i] = new WidgetDataAsJson(
                dbResults.get(i).getId(),
                dbResults.get(i).getWidgetId(),
                dbResults.get(i).getPmId(),
                dbResults.get(i).getActivityId(),
                dbResults.get(i).getRunId(),
                dbResults.get(i).getType(),
                dbResults.get(i).getName(),
                dbResults.get(i).getLocation(),
                dbResults.get(i).getNumResults(),
                dbResults.get(i).getMinId(),
                dbResults.get(i).getMaxId(),
                dbResults.get(i).getDataAsJson(),
                df.format(dbResults.get(i).getTimeCollected())
        );

      }

      System.out.println(resultsData);
    	return resultsData;

    }


    public ConfigParameter [] getParametersForRun(int runId, int pmId) throws SQLException {

      int runOwner = this.getPolicymakerOwnerIdFromRunId(Integer.toString(runId));

      if (runOwner != pmId) {
        throw new SQLException (
                "User requesting parameters for run " + runId
                + " is not the run's owner. Requester = " + pmId);
      }

      //public ArrayList<Integer> getIDColumnValuesWhere(
      //Dao object, String columnNameToReturn, String columnName, Object value

      // get conf set id from run
      ArrayList<Integer> confSetIds = getMgtSchema().getIDColumnValuesWhere(
              new WegovRun_ConfigurationSet(), "ConfigurationSetID", "RunID", runId);

      if (confSetIds.isEmpty()) {
          throw new SQLException ("No config set for run ID " + runId);
      }
      if (confSetIds.size() > 1) {
        throw new SQLException ("More than one config set for run ID " + runId);
      }

      int confSetId = confSetIds.get(0).intValue();
      System.out.println("Conf set ID = " + confSetId);

      // get conf id from conf set id
      ArrayList<Integer> confIds = getMgtSchema().getIDColumnValuesWhere(
              new WegovConfigurationSet_Configuration(), "ConfigurationID", "ConfigurationSetID", confSetId);

      if (confIds.isEmpty()) {
          throw new SQLException ("No config for conf set ID " + confSetId);
      }
      if (confIds.size() > 1) {
        throw new SQLException ("More than one config for conf set ID " + confSetId);
      }

      int confId = confIds.get(0).intValue();
      System.out.println("Conf ID = " + confId);


      // get param IDs for conf
      ArrayList<Integer> paramIds = getMgtSchema().getIDColumnValuesWhere(
              new WegovConfiguration_Parameter(), "ParameterID", "ConfigurationID", confId);

      if (paramIds.isEmpty()) {
          throw new SQLException ("No config for conf set ID " + confSetId);
      }

      //int confId = confIds.get(0).intValue();

      ArrayList<WegovParameter> dbResults = new ArrayList<WegovParameter>();


      for (int i = 0; i < paramIds.size(); i++) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("ID", paramIds.get(i).intValue());
        dbResults.addAll(getMgtSchema().getAllWhere(new WegovParameter(), map));
      }



      ConfigParameter[] resultsData = new ConfigParameter[dbResults.size()];
      for (int i = 0; i < dbResults.size(); i++) {
        resultsData[i] = new ConfigParameter(
                dbResults.get(i).getID(),
                dbResults.get(i).getName(),
                dbResults.get(i).getValue(),
                dbResults.get(i).getDescription(),
                runId);
      }

      //System.out.println(resultsData.toString());
    	return resultsData;

}


    public WidgetDataAsJson[] getAllResultsForActivityAndRun(int activityId, int pmId, int runId) throws SQLException {
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	map.put("activityid", activityId);
      map.put("pmid", pmId);
      map.put("runid", runId);

    	ArrayList<WegovWidgetDataAsJson> dbResults = getDataSchema().getAllWhere(new WegovWidgetDataAsJson(), map);

      WidgetDataAsJson[] resultsData = new WidgetDataAsJson[dbResults.size()];
    	for (int i = 0; i < dbResults.size(); i++) {
        resultsData[i] = new WidgetDataAsJson(
                dbResults.get(i).getId(),
                dbResults.get(i).getWidgetId(),
                dbResults.get(i).getPmId(),
                dbResults.get(i).getActivityId(),
                dbResults.get(i).getRunId(),
                dbResults.get(i).getType(),
                dbResults.get(i).getName(),
                dbResults.get(i).getLocation(),
                dbResults.get(i).getNumResults(),
                dbResults.get(i).getMinId(),
                dbResults.get(i).getMaxId(),
                dbResults.get(i).getDataAsJson(),
                df.format(dbResults.get(i).getTimeCollected())
        );

      }

      System.out.println(resultsData);
    	return resultsData;

    }


    public WidgetDataAsJson[] getFacebookPostCommentsData(int activityId, int pmId, String postId) throws SQLException {
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	map.put("activityid", activityId);
      map.put("pmid", pmId);

      // additional filter on type, which is "post-comments-facebook"
      map.put("type", "post-comments-facebook");
      // for FB comments, the postId is stored in the "name" field
      map.put("name", postId);

    	ArrayList<WegovWidgetDataAsJson> dbResults = getDataSchema().getAllWhere(new WegovWidgetDataAsJson(), map);

      WidgetDataAsJson[] resultsData = new WidgetDataAsJson[dbResults.size()];
    	for (int i = 0; i < dbResults.size(); i++) {
        resultsData[i] = new WidgetDataAsJson(
                dbResults.get(i).getId(),
                dbResults.get(i).getWidgetId(),
                dbResults.get(i).getPmId(),
                dbResults.get(i).getActivityId(),
                dbResults.get(i).getRunId(),
                dbResults.get(i).getType(),
                dbResults.get(i).getName(),
                dbResults.get(i).getLocation(),
                dbResults.get(i).getNumResults(),
                dbResults.get(i).getMinId(),
                dbResults.get(i).getMaxId(),
                dbResults.get(i).getDataAsJson(),
                df.format(dbResults.get(i).getTimeCollected())
        );

      }

      System.out.println(resultsData);
    	return resultsData;

    }















/*
    //TODO: create similar method that only includes the results metadata (not the raw JSON itself)
    public WegovWidgetDataAsJson[] getAllResultsForRun(int runId, boolean getFullResultsData, int pmId) throws SQLException {
    	//System.out.println("getResultsForRun: " + runId);
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	map.put("runid", runId);
      map.put("pmid", pmId);

    	//TODO: check getFullResultsData. If true, populate resultsData with raw JSON result
    	ArrayList<WegovWidgetDataAsJson> dbResults = getDataSchema().getAllWhere(new WegovWidgetDataAsJson(), map);
    	WegovWidgetDataAsJson[] resultsData = new WegovWidgetDataAsJson[dbResults.size()];
    	for (int i = 0; i < dbResults.size(); i++) {
    		resultsData[i] = dbResults.get(i);
		}
    	return resultsData;
    }
*/





/*
    //TODO: create similar method that only includes the results metadata (not the raw JSON itself)
    public WegovWidgetDataAsJson getResultsForRun(int runId, boolean getFullResultsData, int pmId) throws SQLException {
    	//System.out.println("getResultsForRun: " + runId);
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	map.put("runid", runId);
      map.put("pmid", pmId);

      //TODO: check getFullResultsData. If true, populate resultsData with raw JSON result
    	WegovWidgetDataAsJson resultsData = (WegovWidgetDataAsJson) getDataSchema().getFirstWhere(new WegovWidgetDataAsJson(), map);
    	return resultsData;
    }
*/

    public WegovWidgetDataAsJson[] getAllResultsForRun(int runId, int pmId, boolean getFullResultsData) throws SQLException {
    	WegovWidgetDataAsJson object = new WegovWidgetDataAsJson();
    	PostgresConnector pc = getDataConnector();

    	String select;

    	if (getFullResultsData) {
    		select = "SELECT *";
    	}
    	else {
    		select = "SELECT id, widgetid, pmid, activityid, runid, type, name, location, \"nResults\", \"minId\", \"maxId\", \"minTs\", \"maxTs\", collected_at";
    	}
    	String query = select +
		" FROM \"" + object.getTableName() + "\"" +
		//" WHERE runid=" + runId + " AND type != 'post-comments-facebook'" +
		" WHERE runid=" + runId + " AND pmid=" + pmId +
		";";

    	//System.out.println(query);

    	final ResultSet rs = pc.executeQuery(query);
    	ArrayList<WegovWidgetDataAsJson> daoArray;

    	if (getFullResultsData) {
    		daoArray = pc.resultSetAsDao(rs, object);
    	}
    	else {
    		daoArray = pc.resultSetAsDaoExcludeColumn(rs, object, "dataAsJson");
    	}

    	/*
    	if (daoArray.size() > 0) {
    		return (WegovWidgetDataAsJson) daoArray.get(0);
    	}
    	else {
    		return null;
    	}
    	*/
    	WegovWidgetDataAsJson[] results = daoArray.toArray(new WegovWidgetDataAsJson[daoArray.size()]);

    	return results;
    }

    public WegovWidgetDataAsJson getResultsForRun(int runId, int pmId, boolean getFullResultsData) throws SQLException {
    	WegovWidgetDataAsJson object = new WegovWidgetDataAsJson();

    	PostgresConnector pc = getDataConnector();

    	String select;

    	if (getFullResultsData) {
    		select = "SELECT *";
    	}
    	else {
    		select = "SELECT id, widgetid, pmid, activityid, runid, type, name, location, \"nResults\", \"minId\", \"maxId\", \"minTs\", \"maxTs\", collected_at";
    	}
    	String query = select +
		" FROM \"" + object.getTableName() + "\"" +
		" WHERE runid=" + runId + " AND pmid=" + pmId + " AND type != 'post-comments-facebook'" +
		";";

    	System.out.println(query);

    	final ResultSet rs = pc.executeQuery(query);
    	ArrayList daoArray;

    	if (getFullResultsData) {
    		daoArray = pc.resultSetAsDao(rs, object);
    	}
    	else {
    		daoArray = pc.resultSetAsDaoExcludeColumn(rs, object, "dataAsJson");
    	}

    	if (daoArray.size() > 0) {
    		return (WegovWidgetDataAsJson) daoArray.get(0);
    	}
    	else {
    		return null;
    	}

    }


    public Map<String,Timestamp> getLatestTimestampsForPostComments(final Integer activityId) throws Exception{
    	HashMap<String, Timestamp> timestampsForPosts = new HashMap<String, Timestamp>();
    	WegovWidgetDataAsJson object = new WegovWidgetDataAsJson();
    	PostgresConnector pc = getDataConnector();

    	String query = "SELECT name, max(\"maxTs\") AS maxTs" +
    		" FROM \"" + object.getTableName() + "\"" +
    		" WHERE activityid=" + activityId + " AND type='post-comments-facebook'" +
    		" GROUP BY name" +
    		";";

    	//System.out.println(query);

    	final ResultSet rs = pc.executeQuery(query);

    	while (rs.next()){
    		String name = rs.getString("name");
    		Timestamp maxTs = rs.getTimestamp("maxTs");
    		System.out.println(name + "\t" + maxTs);
    		timestampsForPosts.put(name, maxTs);
    	}

    	return timestampsForPosts;
    }

    public Widget getWidgetWithIdForPM(int wId, int pmId) throws SQLException {
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	map.put("id", wId);
    	map.put("policymakerId", pmId);

    	ArrayList<WegovWidget> widgetsFromDb = getMgtSchema().getAllWhere(new WegovWidget(), map);

    	if (widgetsFromDb.isEmpty())
    		return null;
    	else {
    		WegovWidget data = widgetsFromDb.get(0);
    		return new Widget(
            data.getId(), data.getWidgetsetId(), data.getColumnName(),
                data.getColumnOrderNumber(), data.getPolicymakerId(),
    				data.getName(), data.getDescription(),
    				data.getWidgetCategory(), data.getType(), data.getDatatype(),
    				data.getDataAsString(), data.getParametersAsString(), data.isVisible(),
            data.getLabelText());
    	}
    }

    public int addNewLocationForPolicymaker(int pmId, String locationName, String locationAddress, String lat, String lon) throws SQLException {
    	String locationDbId = getMgtSchema().insertObject(new WegovPolicymakerLocation(pmId, locationName, locationAddress, lat, lon));
    	return Integer.parseInt(locationDbId);
    }

    public PolicymakerLocation[] getLocationsForPolicymaker(int pmId) throws SQLException {
    	ArrayList<WegovPolicymakerLocation> locationsFromDb = getMgtSchema().getAllWhere(new WegovPolicymakerLocation(), "pmId", pmId);
    	int numLocationsFound = locationsFromDb.size();
    	if (numLocationsFound > 0) {
    		PolicymakerLocation[] locations = new PolicymakerLocation[numLocationsFound];

    		WegovPolicymakerLocation locationDb;
    		for (int i = 0; i < numLocationsFound; i++) {
    			locationDb = locationsFromDb.get(i);
    			locations[i] = new PolicymakerLocation(locationDb.getId(), locationDb.getPolicymakerId(), locationDb.getLocationName(), locationDb.getLocationAddress(), locationDb.getLat(), locationDb.getLon());
    		}

    		return locations;
    	} else {
    		return null;
    	}
    }

    public void removeLocationWithId(int locationId) throws SQLException {

    	getMgtSchema().deleteAllWhere(new WegovPolicymakerLocation(), "id", locationId);
    }

    public int createWidgetSet(int pmId, String name, String description, int isDefault) throws SQLException {
    	String newWSid = getMgtSchema().insertObject(new WegovWidgetSet(pmId, name, description, isDefault));
    	return Integer.parseInt(newWSid);
    }

    public int createWidget(
            int wsId, String columnName, int columnOrderNum,
            int pmId, String name, String description,
            String category, String type, String datatype, String dataAsString,
            String parametersAsString, int isVisible,
            String labelText) throws SQLException {
    	String newWid = getMgtSchema().insertObject(
              new WegovWidget(
                wsId, columnName, columnOrderNum, pmId, name, description,
              category, type, datatype, dataAsString, parametersAsString, isVisible, labelText));
    	return Integer.parseInt(newWid);
    }

    public void duplicateWidget(int wId, String parametersAsString) throws SQLException {
    	ArrayList<WegovWidget> wegovWidgetsToDuplicateInDb = getMgtSchema().getAllWhere(new WegovWidget(), "id", wId);
    	if (wegovWidgetsToDuplicateInDb.isEmpty()) {
    		throw new SQLException("Widget with id=[" + wId + "] does not exist!");
    	} else {
    		WegovWidget wegovWidgetToDuplicate = wegovWidgetsToDuplicateInDb.get(0);
    		this.createWidget(wegovWidgetToDuplicate.getWidgetsetId(),
    				wegovWidgetToDuplicate.getColumnName(),
    				wegovWidgetToDuplicate.getColumnOrderNumber(),
    				wegovWidgetToDuplicate.getPolicymakerId(),
    				wegovWidgetToDuplicate.getName(),
    				wegovWidgetToDuplicate.getDescription(),
            wegovWidgetToDuplicate.getWidgetCategory(),
    				wegovWidgetToDuplicate.getType(),
    				wegovWidgetToDuplicate.getDatatype(),
    				wegovWidgetToDuplicate.getDataAsString(),
            parametersAsString, 0,
    				wegovWidgetToDuplicate.getLabelText()
        );
    	}
    }
/*
    public int duplicateWidgetReturnId(int wId, String parametersAsString) throws SQLException {
    	ArrayList<WegovWidget> wegovWidgetsToDuplicateInDb = getMgtSchema().getAllWhere(new WegovWidget(), "id", wId);
    	if (wegovWidgetsToDuplicateInDb.isEmpty()) {
    		throw new SQLException("Widget with id=[" + wId + "] does not exist!");
    	} else {
    		WegovWidget wegovWidgetToDuplicate = wegovWidgetsToDuplicateInDb.get(0);
    		int newWidgetId = this.createWidget(wegovWidgetToDuplicate.getWidgetsetId(),
    				wegovWidgetToDuplicate.getColumnName(),
    				wegovWidgetToDuplicate.getColumnOrderNumber(),
    				wegovWidgetToDuplicate.getPolicymakerId(),
    				wegovWidgetToDuplicate.getName(),
    				wegovWidgetToDuplicate.getDescription(),
    				wegovWidgetToDuplicate.getType(),
    				wegovWidgetToDuplicate.getDatatype(),
    				wegovWidgetToDuplicate.getDataAsString(), parametersAsString, 0);
        return newWidgetId;
    	}
    }
  */

    public int duplicateWidgetToNewUserOrWidgetSet(
            int wId,
            String parametersAsString,
            int targetPolicymakerId,
            int targetWsId
    ) throws SQLException {
    	ArrayList<WegovWidget> wegovWidgetsToDuplicateInDb = getMgtSchema().getAllWhere(new WegovWidget(), "id", wId);
    	if (wegovWidgetsToDuplicateInDb.isEmpty()) {
    		throw new SQLException("Widget with id=[" + wId + "] does not exist!");
    	} else {
    		WegovWidget wegovWidgetToDuplicate = wegovWidgetsToDuplicateInDb.get(0);

        // first need to check that the only source widget
        // is the template widget source user
        try {
          Policymaker templateSource = this.getTemplateWidgetsSourceUser();
          if (wegovWidgetToDuplicate.getPolicymakerId() != templateSource.getID()) {
            throw new Exception ("Duplicate Widget: Source widget does not belong to template set");
          }
        }
        catch (Exception ex) {
          throw new SQLException(ex.getMessage());
        }

        // find all widgets for targetPolicymakerId and columnleft
        // increase columnNumOrder for all of them

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("policymakerId", targetPolicymakerId);
        map.put("columnName", "columnleft");
        ArrayList<WegovWidget> widgetsFromLeftColumn = getMgtSchema().getAllWhere(new WegovWidget(), map);
        for (WegovWidget widgetFromLeftColumn : widgetsFromLeftColumn) {
          getMgtSchema().updateRow(new WegovWidget(), "columnordernum", widgetFromLeftColumn.getColumnOrderNumber() + 1, "id", widgetFromLeftColumn.getId());

        }

        int newWidgetId = this.createWidget(targetWsId,
//    				wegovWidgetToDuplicate.getColumnName(),
//    				wegovWidgetToDuplicate.getColumnOrderNumber(),
            "columnleft",
            0,
    				targetPolicymakerId,
    				wegovWidgetToDuplicate.getName(),
    				wegovWidgetToDuplicate.getDescription(),
            wegovWidgetToDuplicate.getWidgetCategory(),
    				wegovWidgetToDuplicate.getType(),
    				wegovWidgetToDuplicate.getDatatype(),
    				wegovWidgetToDuplicate.getDataAsString(),
            parametersAsString,
            0, // default isVisible true - when people duplicate a widget they usually want to see it immediately!
    				wegovWidgetToDuplicate.getLabelText()

        );
        return newWidgetId;
    	}
    }

    public void deleteWidget(int wId) throws SQLException {
    	getMgtSchema().deleteAllWhere(new WegovWidget(), "id", wId);
    }

    // changePassword 0 - don't change, 1 - change
    public void savePolicymakerInfo(int pmId, String fullName, String organisation, String newPassword, int changePassword) throws SQLException {
    	Policymaker pm = new Policymaker(pmId, this);
		pm.setName(fullName);
		pm.setOrganisation(organisation);

		if (changePassword == 1)
			pm.setPassword(newPassword);

    }

    public WidgetSet getDefaultWidgetSetForPM(int pmId) throws SQLException {
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	map.put("policymakerId", pmId);
    	map.put("isDefault", 1);

    	ArrayList<WegovWidgetSet> widgetSetsFromDb = getMgtSchema().getAllWhere(new WegovWidgetSet(), map);

    	if (widgetSetsFromDb.size() > 0) {
    		WegovWidgetSet data = widgetSetsFromDb.get(0);
    		return new WidgetSet(data.getId(), data.getPolicymakerId(), data.getName(), data.getDescription(), data.isDefault());
    	} else {
    		return null;
    	}

    }
    
    public int getTotalNumberOfWidgets() throws SQLException {
        ArrayList<WegovWidget> allWidgets = getMgtSchema().getAll(new WegovWidget());
        
        if (allWidgets.isEmpty())
            return 0;
        else
            return allWidgets.size();
    }

    public Widget[] getWidgetsForDefaultWidgetSet(int pmId) throws SQLException {
    	return getWidgetsForWidgetSet(getDefaultWidgetSetForPM(pmId).getId(), pmId);
    }

    public Widget[] getVisibleWidgetsForDefaultWidgetSet(int pmId) throws SQLException {
    	return getVisibleWidgetsForWidgetSet(getDefaultWidgetSetForPM(pmId).getId(), pmId);
    }

    public Widget[] getHiddenWidgetsForDefaultWidgetSet(int pmId) throws SQLException {
    	return getHiddenWidgetsForWidgetSet(getDefaultWidgetSetForPM(pmId).getId(), pmId);
    }

    public Widget[] getWidgetsForWidgetSet(int wsid, int pmId) throws SQLException {
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	map.put("widgetsetid", wsid);
    	map.put("policymakerId", pmId);

    	ArrayList<WegovWidget> widgetsFromDb = getMgtSchema().getAllWhereSortBy(new WegovWidget(), map, "columnordernum");
    	Widget[] result = new Widget[widgetsFromDb.size()];

    	WegovWidget data;
    	for (int i = 0; i < widgetsFromDb.size(); i++) {
    		data = widgetsFromDb.get(i);
    		result[i] = new Widget(
            data.getId(), data.getWidgetsetId(), data.getColumnName(),
                data.getColumnOrderNumber(), data.getPolicymakerId(),
    				data.getName(), data.getDescription(),
      			data.getWidgetCategory(), data.getType(), data.getDatatype(),
    				data.getDataAsString(), data.getParametersAsString(), data.isVisible(),
            data.getLabelText());
    	}

    	return result;
    }

    public Widget[] getVisibleWidgetsForWidgetSet(int wsid, int pmId) throws SQLException {
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	map.put("widgetsetid", wsid);
    	map.put("policymakerId", pmId);
    	map.put("isVisible", 0);

    	ArrayList<WegovWidget> widgetsFromDb = getMgtSchema().getAllWhere(new WegovWidget(), map);
    	Widget[] result = new Widget[widgetsFromDb.size()];

    	WegovWidget data;
    	for (int i = 0; i < widgetsFromDb.size(); i++) {
    		data = widgetsFromDb.get(i);
    		result[i] = new Widget(
            data.getId(), data.getWidgetsetId(),
            data.getColumnName(), data.getColumnOrderNumber(), data.getPolicymakerId(),
    				data.getName(), data.getDescription(),
    				data.getWidgetCategory(), data.getType(), data.getDatatype(),
    				data.getDataAsString(), data.getParametersAsString(), data.isVisible(),
            data.getLabelText());
    	}

    	return result;
    }

    public Widget[] getHiddenWidgetsForWidgetSet(int wsid, int pmId) throws SQLException {
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	map.put("widgetsetid", wsid);
    	map.put("policymakerId", pmId);
    	map.put("isVisible", 1);

    	ArrayList<WegovWidget> widgetsFromDb = getMgtSchema().getAllWhere(new WegovWidget(), map);
    	Widget[] result = new Widget[widgetsFromDb.size()];

    	WegovWidget data;
    	for (int i = 0; i < widgetsFromDb.size(); i++) {
    		data = widgetsFromDb.get(i);
    		result[i] = new Widget(
            data.getId(), data.getWidgetsetId(),
            data.getColumnName(), data.getColumnOrderNumber(), data.getPolicymakerId(),
    				data.getName(), data.getDescription(),
    				data.getWidgetCategory(), data.getType(), data.getDatatype(),
    				data.getDataAsString(), data.getParametersAsString(), data.isVisible(), data.getLabelText());
    	}

    	return result;
    }

    public Widget[] getWidgetsForWidgetSetColumn(int wsid, String columnName, int pmId) throws SQLException {
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	map.put("widgetsetid", wsid);
    	map.put("policymakerId", pmId);
    	map.put("columnName", columnName);
    	map.put("isVisible", 0);

    	ArrayList<WegovWidget> widgetsFromDb = getMgtSchema().getAllWhere(new WegovWidget(), map);
    	Widget[] result = new Widget[widgetsFromDb.size()];

    	WegovWidget data;
    	for (int i = 0; i < widgetsFromDb.size(); i++) {
    		data = widgetsFromDb.get(i);
    		result[i] = new Widget(
            data.getId(), data.getWidgetsetId(),
            data.getColumnName(), data.getColumnOrderNumber(), data.getPolicymakerId(),
    				data.getName(), data.getDescription(),
    				data.getWidgetCategory(), data.getType(), data.getDatatype(),
    				data.getDataAsString(), data.getParametersAsString(), data.isVisible(), data.getLabelText());
    	}

    	return result;
    }

    public WidgetSet[] getWidgetSetsForPM(int pmId) throws SQLException {

    	ArrayList<WegovWidgetSet> widgetSetsFromDb = getMgtSchema().getAllWhere(new WegovWidgetSet(), "policymakerId", pmId);
    	WidgetSet[] result = new WidgetSet[widgetSetsFromDb.size()];

    	for (int i = 0; i < widgetSetsFromDb.size(); i++) {
    		WegovWidgetSet widgetSetFromDb = widgetSetsFromDb.get(i);
    		result[i] = new WidgetSet(widgetSetFromDb.getId(), widgetSetFromDb.getPolicymakerId(), widgetSetFromDb.getName(), widgetSetFromDb.getDescription(), widgetSetFromDb.isDefault());
    	}

    	return result;

    }


    public Widget[] getTemplateWidgets() throws Exception {
      if (this.templateWidgetsUser != null) {
        int templateUserPmId = this.templateWidgetsUser.getID();
        // All template widgets must be stored in default set!
        return getWidgetsForWidgetSet(
                getDefaultWidgetSetForPM(templateUserPmId).getId(),
                templateUserPmId
        );
      }
      else {
        throw new Exception ("Null template widgets user - need to call setupWeGovDatabase");
      }
    }

    public Widget[] getTemplateWidgetsMatchingWidgetType(String widgetType) throws Exception {
      if (this.templateWidgetsUser != null) {
        int templateUserPmId = this.templateWidgetsUser.getID();
        // All template widgets must be stored in default set!
        return getWidgetsMatchingWidgetType(widgetType, templateUserPmId);
      }
      else {
        throw new Exception ("Null template widgets user - need to call setupWeGovDatabase");
      }
    }

    public Widget[] getTemplateWidgetsMatchingWidgetCategory(String widgetCategory) throws Exception {
      if (this.templateWidgetsUser != null) {
        int templateUserPmId = this.templateWidgetsUser.getID();
        // All template widgets must be stored in default set!
        return getWidgetsMatchingWidgetCategory(widgetCategory, templateUserPmId);
      }
      else {
        throw new Exception ("Null template widgets user - need to call setupWeGovDatabase");
      }
    }


    public void updateOrderAndColumnOfWidgetWithId(
            int widgetId, String newColumnName, int newOrder) throws SQLException {
    	getMgtSchema().updateRow(new WegovWidget(), "columnName", newColumnName, "id", widgetId);
    	getMgtSchema().updateRow(new WegovWidget(), "columnordernum", newOrder, "id", widgetId);
    }

    // (Dao object, String fieldToEdit, Object newValue, String identifyingField, Object identifyingValue)
    public void updateWidgetParameters(int widgetId, String newParametersValue) throws SQLException {
    	getMgtSchema().updateRow(new WegovWidget(), "parametersAsString", newParametersValue, "id", widgetId);
    }

    public void hideWidget(int widgetId) throws SQLException {
    	getMgtSchema().updateRow(new WegovWidget(), "isVisible", 1, "id", widgetId);
    }

    public void showWidget(int widgetId) throws SQLException {
    	getMgtSchema().updateRow(new WegovWidget(), "isVisible", 0, "id", widgetId);
    }

    /*
     * Database methods
     */

    /**
     * Override configuration file setting for the database.
     */
    public void setDatabase(String newDatabaseName) throws SQLException {
        this.databaseName = newDatabaseName;
        getMgtConnector().useDatabase(newDatabaseName);
        getDataConnector().useDatabase(newDatabaseName);
        this.database = getMgtConnector().getDatabase();
    }

    /**
     * Set Wegov data and management database schemas.
     */
    public void setupWegovDatabase() throws Exception {
        //this.database = getMgtConnector().getDatabase(this.databaseName);
        //this.mgtSchema = getMgtConnector().getSchema(this.database, this.mgtSchemaName);
        //this.dataSchema = getDataConnector().getSchema(this.database, this.dataSchemaName);

        //KEM create a connector for each schema
    	PostgresConnector mgtConnector = getOrCreateMgtConnector();
        this.database = mgtConnector.getDatabase(databaseName);
        this.mgtSchema = mgtConnector.getSchema();

        //mgtConnector.useDatabase(databaseName);
        //mgtConnector.useSchema(mgtSchemaName);

        //PostgresConnector dataConnector = new PostgresConnector(properties.getProperty("postgres.url"), properties.getProperty("postgres.login"), properties.getProperty("postgres.pass"));
        PostgresConnector dataConnector = getOrCreateDataConnector();
        dataConnector.getDatabase(databaseName);
        this.dataSchema = dataConnector.getSchema();
        //dataConnector.useDatabase(databaseName);
        //dataConnector.useSchema(dataSchemaName);

        //KEM add connectors to HashMap
        connectors = new HashMap<String, PostgresConnector>();
        connectors.put(mgtSchemaName, mgtConnector);
        connectors.put(dataSchemaName, dataConnector);

        //this.mgtSchema = getMgtConnector().getSchema();
        //this.dataSchema = getDataConnector().getSchema();

        // Fill management schema - much cleaner now!
        setupWegovManagementSchema();

        // Fill data schema
        setupWegovDataSchema();
    }

    private PostgresConnector getOrCreateMgtConnector() throws Exception {
    	PostgresConnector mgtConnector = getMgtConnector();
    	if (mgtConnector != null) {
    		return mgtConnector;
    	}
    	else {
    		mgtConnector = new PostgresConnector(properties.getProperty("postgres.url"),
    				properties.getProperty("postgres.login"),
    				properties.getProperty("postgres.pass"),
    				mgtSchemaName);
    		return mgtConnector;
    	}
    }

    private PostgresConnector getOrCreateDataConnector() throws Exception {
    	PostgresConnector dataConnector = getDataConnector();
    	if (dataConnector != null) {
    		return dataConnector;
    	}
    	else {
    		dataConnector = new PostgresConnector(properties.getProperty("postgres.url"),
    				properties.getProperty("postgres.login"),
    				properties.getProperty("postgres.pass"),
    				dataSchemaName);
    		return dataConnector;
    	}
    }

    /**
     * Set experimental Twitter data schema with default name and Wegov management schema.
     *
     */
    public void setupTwitterDatabase(String twitterSchemaName) throws SQLException {
        //this.database = getMgtConnector().getDatabase(this.databaseName);
        //this.mgtSchema = getMgtConnector().getSchema(this.database, this.mgtSchemaName);
        this.mgtSchema = getMgtConnector().getSchema();

        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //this.dataSchema = getDataConnector().getSchema(this.database, "Twitter " + formatter.format(now) + " " + twitterSchemaName);
        getDataConnector().useSchema("Twitter " + formatter.format(now) + " " + twitterSchemaName);
        this.dataSchema = getDataConnector().getSchema();

        // Fill management schema - much cleaner now!
        setupWegovManagementSchema();

        // Fill data schema
        setupTwitterDataSchema();
    }

    /**
     * Return absolute path to data folder containing files necessary for KMI's analysis.
     * @return
     */
    public String getPathToKmiDataFolder() {
        return pathToKmiDataFolder;
    }

    /**
     * Return absolute path to configuration file for the coordinator.
     * @return
     */
    public String getAbsolutePathToConfigurationFile() {
        return absolutePathToConfigurationFile;
    }

    /**
     * Set experimental Twitter data schema with exact name and Wegov management schema.
     *
     */
    public void setupTwitterDatabaseWithExactName(String twitterSchemaName) throws SQLException {
        //this.database = getMgtConnector().getDatabase(this.databaseName);
        //this.mgtSchema = getMgtConnector().getSchema(this.database, this.mgtSchemaName);
        this.mgtSchema = getMgtConnector().getSchema();

        //this.dataSchema = getDataConnector().getSchema(this.database, twitterSchemaName);
        getDataConnector().useSchema(twitterSchemaName);
        this.dataSchema = getDataConnector().getSchema();

        // Fill management schema - much cleaner now!
        setupWegovManagementSchema();

        // Fill data schema
        setupTwitterDataSchema();
    }

    private void setupWegovManagementSchema() throws SQLException {

        this.mgtSchema = getMgtConnector().getSchema();

        if (mgtSchema == null)
            throw new SQLException("Wegov management database not initialised. Use setupWegovDatabase()");

        logger.debug("Creating database management tables.");
        mgtSchema.getTable(new WegovPolicymaker());

//        mgtSchema.getTable(new WegovWorkflow());
//        mgtSchema.getTable(new WegovTask());
//        mgtSchema.getTable(new WegovPolicymaker_Workflow());
//        mgtSchema.getTable(new WegovPolicymaker_Task());
//        mgtSchema.getTable(new WegovWorkflow_Task());

        mgtSchema.getTable(new WegovRun_Log());
        mgtSchema.getTable(new WegovRun_Error());

//        mgtSchema.getTable(new WegovTask_Inputs());
//        mgtSchema.getTable(new WegovTask_Threads());

        mgtSchema.getTable(new WegovWorksheet());
        mgtSchema.getTable(new WegovPolicymaker_WegovWorksheet());

        mgtSchema.getTable(new WegovActivity());
        mgtSchema.getTable(new WegovActivity_ConfigurationSet());
        mgtSchema.getTable(new WegovActivity_Run());

        mgtSchema.getTable(new WegovPolicymaker_Activity());
        mgtSchema.getTable(new WegovPolicymaker_Run());

        mgtSchema.getTable(new WegovWorksheet_Activity());

        mgtSchema.getTable(new WegovPolicymakerRole());
        mgtSchema.getTable(new WegovPolicymaker_PolicymakerRole());

        mgtSchema.getTable(new WegovConfiguration());
        mgtSchema.getTable(new WegovConfiguration_Parameter());

        mgtSchema.getTable(new WegovParameter());
        mgtSchema.getTable(new WegovParameter_Role());

        mgtSchema.getTable(new WegovConfigurationSet());
        mgtSchema.getTable(new WegovConfigurationSet_Configuration());

        mgtSchema.getTable(new WegovRun());
        mgtSchema.getTable(new WegovRun_ConfigurationSet());

        mgtSchema.getTable(new WegovActivity_InputActivity());

        mgtSchema.getTable(new WegovTwitterOauthAccount());
        mgtSchema.getTable(new WegovPolicymaker_TwitterOauthAccount());

        mgtSchema.getTable(new WegovWidgetSet());
        mgtSchema.getTable(new WegovWidget());

        mgtSchema.getTable(new WegovPolicymakerLocation());
        mgtSchema.getTable(new WegovPolicymaker_Settings());

        // Create Default roles:
        logger.debug("Creating default coordinator roles.");
        createRole(DEFAULT_USER_ROLE_NAME, DEFAULT_USER_ROLE_DESCRIPTION);
        createRole(DEFAULT_ADMIN_ROLE_NAME, DEFAULT_ADMIN_ROLE_DESCRIPTION);


        logger.debug("Default tools will not be created");
//        logger.debug("Checking existing tools.");

//        if (getTools().isEmpty()) {
//            logger.debug("Creating new default configuration sets (tools).");
//            // Create Default configuration sets (tools):
//            ArrayList<Role> roles = getRoles();
//
//            ConfigurationSet searchConfigurationSet = createConfigurationSet("Search", "Twitter search using a keyword", null);
//            ConfigurationSet analysisConfigurationSet = createConfigurationSet("Analysis", "Find most influential tweets", null);
//            ConfigurationSet testConfigurationSet = createConfigurationSet("Test", "Test component", null);
//
//            Configuration searchConfiguration = createConfiguration("Search Twitter", "java -version", "Test twitter search", null);
//            Configuration analysisConfiguration = createConfiguration("Topic opinion analysis", "java -jar /Users/max/Documents/Work/wegov/analysis-koblenz/dist/analysis-koblenz.jar", "Koblenz topic opinion analysis of tweets", null);
//            Configuration testConfiguration = createConfiguration("Test Configuration", "java -jar /Users/max/Documents/Work/wegov/wegov-coordinator-testjar/dist/wegov-coordinator-testjar.jar", "Runs test jar", null);
//
//            searchConfiguration.addParameter("searchQuery", "What to search for", "London Riots", roles);
//            searchConfiguration.addParameter("snsToQuery", "Which SNS to use", "Twitter", roles);
//            analysisConfiguration.addParameter("numberOfTopicsToReturn", "How many topics to return", "3", roles);
//            testConfiguration.addParameter("tweetID", "Tweet ID to insert into the database", "989898981112121", roles);
//            testConfiguration.addParameter("tweetContents", "Tweet contents to insert into the database", "Smells like success!", roles);
//            testConfiguration.addParameter("outputOfType", "Full class name for the output of this configuration", "eu.wegov.coordinator.dao.data.WegovPostItem", roles);
//
//            searchConfigurationSet.addConfiguration(searchConfiguration);
//            analysisConfigurationSet.addConfiguration(analysisConfiguration);
//            testConfigurationSet.addConfiguration(testConfiguration);
//
//        } else {
//            logger.debug("Looks like default configuration sets (tools) already exist.");
//        }


        // Psuedo user that holds template widgets -
        // when a real user creates a widget it is
        // copied from the set of templates held
        // by this template user

        if (this.templateWidgetsUser == null) {

          if (this.getPolicymakerByUsername(this.templateWidgetsSourceUser) != null) {
            // get from database
            this.templateWidgetsUser = this.getPolicymakerByUsername(this.templateWidgetsSourceUser);
          }
          else {
            // if not in db create it
            Role adminRole = this.getDefaultAdminRole();
            this.templateWidgetsUser = this.createPolicyMaker(
                    "Wegov Template Widgets", adminRole, "WeGov",
                    this.templateWidgetsSourceUser,
                    this.templateWidgetsSourceUserPassword
            );

          }
        }


        logger.debug("Database management tables setup complete.");
    }

    private void setupWegovDataSchema() throws SQLException {
        if (dataSchema == null)
            throw new SQLException("Wegov data database not initialised. Use setupWegovDatabase()");

        logger.debug("Creating database data tables.");

        dataSchema.getTable(new WegovFollower());
        dataSchema.getTable(new WegovFriend());
        dataSchema.getTable(new WegovGroupAdmin());
        dataSchema.getTable(new WegovGroupMember());
        dataSchema.getTable(new WegovGroupPost());
        dataSchema.getTable(new WegovLike());
        dataSchema.getTable(new WegovMentionedUser());
        dataSchema.getTable(new WegovPostComment());
        dataSchema.getTable(new WegovPostItem());
        dataSchema.getTable(new WegovPostTag());
        dataSchema.getTable(new WegovSNS());
        dataSchema.getTable(new WegovSnsGroup());
        dataSchema.getTable(new WegovSnsUserAccount());
        dataSchema.getTable(new WegovTag());

        dataSchema.getTable(new WegovAnalysisKoblenzTopic());
        dataSchema.getTable(new WegovAnalysisKoblenzUser());
        dataSchema.getTable(new WegovAnalysisKoblenzMessage());
        dataSchema.getTable(new WegovAnalysisKoblenzViewpoint());

        dataSchema.getTable(new WegovAnalysisKmiDa());
        dataSchema.getTable(new WegovAnalysisKmiDaMostActiveUser());
        dataSchema.getTable(new WegovAnalysisKmiBuzzTopUser());
        dataSchema.getTable(new WegovAnalysisKmiBuzzTopPost());

        dataSchema.getTable(new WegovWidgetDataAsJson());

        logger.debug("Database data tables setup complete.");

    }

    private void setupTwitterDataSchema() throws SQLException {
        if (dataSchema == null)
            throw new SQLException("Twitter data database not initialised. Use setupTwitterDatabase(String twitterSchemaName)");

        dataSchema.getTable(new Tweet());
        dataSchema.getTable(new User());
        dataSchema.getTable(new Url());
        dataSchema.getTable(new UserMention());
        dataSchema.getTable(new Hashtag());

    }

    /**
     * Deletes main database (ALL USERS HAVE TO BE DISCONNECTED FROM THE DATABASE FOR THIS TO WORK).
     */
    public void wipeDatabase() throws Exception {
    	PostgresConnector mgtConnector = getOrCreateMgtConnector();
    	mgtConnector.deleteDatabase(databaseName);
    }

    /*
     * Create things
     */


    /**
     * Creates new role or returns existing one with the same name. If role doesn't exist, creates it. If role does exist, returns it from the database.
     *
     * @param roleName name of the role, case-sensitive, for example "test"
     * @param roleDescription role description, for example "test role description"
     * @return new or existing role
     */
    public Role createRole(String roleName, String roleDescription) throws SQLException {
        return new Role(roleName, roleDescription, this);
    }

    /**
     * Returns existing role by name, null if the role wasn't found.
     *
     * @param roleName name of the role, case-sensitive
     */
    public Role getRoleByName(String roleName) throws SQLException {
        ArrayList<Integer> roleIDs = mgtSchema.getIDColumnValuesWhere(new WegovPolicymakerRole(), "ID", "Name", roleName);

        if (roleIDs.isEmpty()) {
            return null;
        } else {
            return new Role(roleIDs.get(0), this);
        }
    }

    /**
     * Returns default user role.
     */
    public Role getDefaultUserRole() throws SQLException {
        return getRoleByName(DEFAULT_USER_ROLE_NAME);
    }

    /**
     * Returns default administrator.
     */
    public Role getDefaultAdminRole() throws SQLException {
        return getRoleByName(DEFAULT_ADMIN_ROLE_NAME);
    }

    /**
     * Creates new startThreaded.
     */
    public Run createRun(Activity activity, String runName, String runComment) {

        if (activity == null)
            throw new RuntimeException("Null activity not allowed for Run with name: \'" + runName + "\', comment: \'" + runComment + "\'.");

        if (runName == null)
            //throw new RuntimeException("Null names are not allowed for Runs!");
        	runName = "";

        logger.debug("Creating new Run with name: \'" + runName + "\', comment: \'" + runComment + "\'.");

        try {

            Run run = new Run(runName, runComment, Run.STATUS_INITIALISING, util.getTimeNowAsTimestamp(), this);

            // Set default name if none is supplied
            if (runName.equals(""))
            	run.setName("Run [" + run.getID() + "] for activity [" + activity.getID() + "]");

            Policymaker policyMaker = activity.getPolicyMaker();

            int newRunID = run.getID();
            int policyMakerID = policyMaker.getID();
            int activityID = activity.getID();

            mgtSchema.insertObject(new WegovPolicymaker_Run(policyMakerID, newRunID));
            mgtSchema.insertObject(new WegovActivity_Run(activityID, newRunID));

            logger.debug("New Run created with ID: " + newRunID);

            return run;
        } catch(Exception ex) {
            logger.error("Failed to create run with name: \'" + runName + "\', comment: \'" + runComment + "\'.");
            logger.error(ex);
            return null;
        }
    }

    /**
     * Creates new empty activity.
     */
    public Activity createActivity(Policymaker policyMaker, String activityName, String activityComment) {

        if (policyMaker == null)
            throw new RuntimeException("Null policy maker not allowed for activity with name: \'" + activityName + "\', comment: \'" + activityComment + "\'.");

        if (activityName == null)
            throw new RuntimeException("Null names are not allowed for Activities!");

        try{
            Activity activity = new Activity(activityName, activityComment, Activity.STATUS_INITIALISING, util.getTimeNowAsTimestamp(), this);

            int newActivityID = activity.getID();
            int policyMakerID = policyMaker.getID();

            mgtSchema.insertObject(new WegovPolicymaker_Activity(policyMakerID, newActivityID));
            logger.debug("New Activity created with ID: " + newActivityID);

            return activity;
        } catch(Exception ex) {
            logger.error("Failed to create activity with name: \'" + activityName + "\', comment: \'" + activityComment + "\'.");
            logger.error(ex);
            return null;
        }
    }

    /**
     * Creates new activity with an existing configuration set (that set will be only be used for cloning).
     */
    public Activity createActivity(Policymaker policyMaker, String activityName, String activityComment, ConfigurationSet configurationSet) {

        if (policyMaker == null)
            throw new RuntimeException("Null policy maker not allowed for activity with name: \'" + activityName + "\', comment: \'" + activityComment + "\'.");

        if (activityName == null)
            throw new RuntimeException("Null names not allowed for Activities!");

        if (configurationSet == null)
            throw new RuntimeException("Null configuration sets are not allowed for activity with name: \'" + activityName + "\', comment: \'" + activityComment + "\'.");

        try {
            Activity activity = createActivity(policyMaker, activityName, activityComment);

            ConfigurationSet newConfigurationSet = configurationSet.clone();

            int newActivityID = activity.getID();
            int cSID = newConfigurationSet.getID();
            mgtSchema.insertObject(new WegovActivity_ConfigurationSet(newActivityID, cSID));

//            logger.debug("New Activity with configuration set [" + configurationSet.getID() + "] created with ID: " + newActivityID);

            return activity;
        } catch (SQLException ex) {
            logger.error("Failed to create activity with configuration set [" + configurationSet.getID() + "], name: \'" + activityName + "\', comment: \'" + activityComment + "\'.");
            logger.error(ex);
            return null;
        }

    }

    /**
     * Creates new configuration set.
     */
    public ConfigurationSet createConfigurationSet(String name, String description, String rendererJsp) throws SQLException {
        return new ConfigurationSet(name, description, rendererJsp, this);
    }

    /**
     * Creates new configuration.
     */
    public Configuration createConfiguration(String name, String command, String description, String rendererJsp) throws SQLException {
        return new Configuration(name, command, description, rendererJsp, this);
    }

    /**
     * Creates new parameter.
     */
    public Parameter createParameter(String name, String description, String value, ArrayList<Role> roles) throws SQLException {
        return new Parameter(name, description, value, roles, this);
    }

    /**
     * Creates new parameter with one role.
     */
    public Parameter createParameter(String name, String description, String value, Role role) throws SQLException {
        ArrayList<Role> roles = new ArrayList<Role>();
        roles.add(role);
        return new Parameter(name, description, value, roles, this);
    }

    /**
     * Creates new Policymaker with default user role, startThreaded date: now, end date: null.
     *
     * @param name person's name
     * @param organisation person's organisation
     * @param username person's username
     * @param unencryptedPassword unencrypted password as typed in (only sha1 hash is stored)
     */
    public Policymaker createPolicyMaker(String name, String organisation, String username, String unencryptedPassword) throws SQLException {
        return new Policymaker(name, getDefaultUserRole(), organisation, username, unencryptedPassword, this);
    }

    /**
     * Creates new Policymaker with startThreaded date: now, end date: null.
     *
     * @param name person's name
     * @param role person's role
     * @param organisation person's organisation
     * @param username person's username
     * @param unencryptedPassword unencrypted password as typed in (only sha1 hash is stored)
     */
    public Policymaker createPolicyMaker(String name, Role role, String organisation, String username, String unencryptedPassword) throws SQLException {
        return new Policymaker(name, role, organisation, username, unencryptedPassword, this);
    }

    /**
     * Creates new Policymaker with startThreaded date: now, end date: null, with flag to indicate if password should be encrypted or not
     *
     * @param name person's name
     * @param role person's role
     * @param organisation person's organisation
     * @param username person's username
     * @param password encrypted or unencrypted password (depends on encryptPassword)
     * @param encryptPassword flag to request if password should be encrypted or not
     */
    public Policymaker createPolicyMaker(String name, Role role, String organisation, String username, String password, boolean encryptPassword) throws SQLException {
        return new Policymaker(name, role, organisation, username, password, this, encryptPassword);
    }

    /**
     * Creates new Policymaker with end date: null.
     *
     * @param name person's name
     * @param role person's role
     * @param organisation person's organisation
     * @param username person's username
     * @param unencryptedPassword unencrypted password as typed in (only sha1 hash is stored)
     * @param startDate when person has created an account with WeGov system
     */
    public Policymaker createPolicyMaker(String name, Role role, String organisation, String username, String unencryptedPassword, Timestamp startDate) throws SQLException {
        return new Policymaker(name, role, organisation, username, unencryptedPassword, startDate, this);
    }

    /**
     * Creates new Policymaker with all available parameters.
     *
     * @param name person's name
     * @param role person's role
     * @param organisation person's organisation
     * @param username person's username
     * @param unencryptedPassword unencrypted password as typed in (only sha1 hash is stored)
     * @param startDate when the person has created an account with WeGov system
     * @param endDate when the person closed his or her account with WeGov system
     * @throws SQLException
     */
    public Policymaker createPolicyMaker(String name, Role role, String organisation, String username, String unencryptedPassword, Timestamp startDate, Timestamp endDate) throws SQLException {
        return new Policymaker(name, role, organisation, username, unencryptedPassword, startDate, endDate, this, true);
    }

    /**
     * Creates new task.
     */
    public Task createTask(Policymaker policyMaker, String taskName, String taskType, String command) throws SQLException {
        Task newTask = new Task(taskName, taskType, "initializing", command, this);
        int newTaskID = newTask.getId();
        int policyMakerID = policyMaker.getID();

        String commandLineWithId = newTask.getCommand() + " " + newTaskID + " " + absolutePathToConfigurationFile;
        newTask.setCommand(commandLineWithId);

        mgtSchema.insertObject(new WegovPolicymaker_Task(policyMakerID, newTaskID));

        return newTask;
    }

    /**
     * Creates new workflow.
     */
    public Workflow createWorkflow(Policymaker policyMaker, String workflowName) throws SQLException {
        Workflow newWorkflow = new Workflow(workflowName, "initializing", this);
        int newWorkflowID = newWorkflow.getId();
        int policyMakerID = policyMaker.getID();

        mgtSchema.insertObject(new WegovPolicymaker_Workflow(policyMakerID, newWorkflowID));

        return newWorkflow;
    }

    /**
     * Creates new worksheet.
     */
    public Worksheet createWorksheet(Policymaker policyMaker, String worksheetName, String worksheetComment) throws SQLException {
        Worksheet worksheet = new Worksheet(worksheetName, worksheetComment, Worksheet.STATUS_INITIALISING, util.getTimeNowAsTimestamp(), this);

        int newWorksheetID = worksheet.getID();
        int policyMakerID = policyMaker.getID();

        mgtSchema.insertObject(new WegovPolicymaker_WegovWorksheet(policyMakerID, newWorksheetID));

        return worksheet;
    }


    /*
     * Return things
     */

    /**
     * Returns data schema.
     */
    public SqlSchema getDataSchema() {
        return dataSchema;
    }

    /**
     * Returns management schema.
     */
    public SqlSchema getMgtSchema() {
        return mgtSchema;
    }

    /**
     * Returns specified database schema.
     */
    public SqlSchema getSchema(String schemaName) throws SQLException {
        return database.getSchema(schemaName);
    }

    /**
     * Returns tasks and logs database table.
     */
    public SqlTable getTasksLogsTable() throws SQLException {
        return getMgtSchema().getTable(new WegovRun_Log());
    }

    /**
     * Returns tasks and errors database table.
     */
    public SqlTable getTasksErrorsTable() throws SQLException {
        return getMgtSchema().getTable(new WegovRun_Error());
    }

    /**
     * Returns tasks' database table.
     */
    public SqlTable getTasksTable() throws SQLException {
        return getMgtSchema().getTable(new WegovTask());
    }

    /**
     * Returns all tasks.
     */
    public ArrayList<Task> getTasks() throws SQLException {
        ArrayList<Task> tasks = new ArrayList<Task>();
        for (int taskId : getMgtSchema().getIDColumnValues(new WegovTask(), "ID")) {
            tasks.add(new Task(taskId, this));
        }
        return tasks;
    }

    /**
     * Returns task with specific database ID.
     */
    public Task getTask(int id) {

        try {
            Task task = new Task(id, this);
            return task;
        } catch(Exception ex) {
            return null;
        }

    }

    /**
     * Returns all workflows.
     */
    public ArrayList<Workflow> getWorkflows() throws SQLException {
        ArrayList<Workflow> wfs = new ArrayList<Workflow>();
        for (int wfId : getMgtSchema().getIDColumnValues(new WegovWorkflow(), "ID")) {
            wfs.add(new Workflow(wfId, this));
        }
        return wfs;
    }

    /**
     * Returns all configuration sets.
     */
    public ArrayList<ConfigurationSet> getConfigurationSets() throws SQLException {
        ArrayList<ConfigurationSet> css = new ArrayList<ConfigurationSet>();
        for (int wfId : getMgtSchema().getIDColumnValues(new WegovConfigurationSet(), "ID")) {
            css.add(new ConfigurationSet(wfId, this));
        }
        return css;
    }

    /**
     * Returns Tools = configuration sets that are not linked to any activity or startThreaded.
     */
    public ArrayList<ConfigurationSet> getTools() throws SQLException {
        ArrayList<ConfigurationSet> css = new ArrayList<ConfigurationSet>();

        // TODO: There is a more efficient way to do this through the database!
        ArrayList<Integer> toolConfSetIDs = getToolConfigurationSetIDs();
        //ArrayList<Integer> toolConfSetIDs = getToolConfigurationSetIDsForWegovServer(); // uncomment for Wegov server
        
        for (int csId : toolConfSetIDs) {
            ConfigurationSet tempConfigurationSet = new ConfigurationSet(csId, this);

            if (tempConfigurationSet.getActivity() == null) {
                if (tempConfigurationSet.getRun() == null) {
                	if (tempConfigurationSet.getConfigurations().size() > 0) {
                		css.add(tempConfigurationSet);
                	}
                }
            }
        }

        return css;
    }

    private ArrayList<Integer> getToolConfigurationSetIDs() throws SQLException {
    	return getMgtSchema().getIDColumnValues(new WegovConfigurationSet(), "ID");
    }

    private ArrayList<Integer> getToolConfigurationSetIDsForWegovServer() {
    	ArrayList<Integer> toolConfSetIDs = new ArrayList<Integer>();
    	
    	toolConfSetIDs.add(1);		//"Adv Search"
    	toolConfSetIDs.add(2);		//"Groups Search"
    	toolConfSetIDs.add(3);		//"Injection"
    	toolConfSetIDs.add(47764);	//"topic-opinion"
    	toolConfSetIDs.add(47765);	//"behaviour"
		
    	return toolConfSetIDs;
	}

	/**
     * Returns configurations that are not linked to any configuration set.
     */
    public ArrayList<Configuration> getToolsConfigurations() throws SQLException {
        ArrayList<Configuration> cs = new ArrayList<Configuration>();

        // TODO: There is a more efficient way to do this through the database!
        for (int cId : getMgtSchema().getIDColumnValues(new WegovConfiguration(), "ID")) {
            Configuration tempConfiguration = new Configuration(cId, this);
            if (tempConfiguration.getConfigurationSet() == null)
                cs.add(tempConfiguration);
        }

        return cs;
    }

    /**
     * Returns all worksheets.
     */
    public ArrayList<Worksheet> getWorksheets() throws SQLException {
        ArrayList<Worksheet> wfs = new ArrayList<Worksheet>();
        for (int wfId : getMgtSchema().getIDColumnValues(new WegovWorksheet(), "ID")) {
            wfs.add(new Worksheet(wfId, this));
        }
        return wfs;
    }

    /**
     * Returns configuration set with required database ID.
     */
    public ConfigurationSet getConfigurationSetByID(int configurationSetID) throws SQLException {
        int gotID = -1;
        for (int csId : getMgtSchema().getIDColumnValues(new WegovConfigurationSet(), "ID")) {
            if (csId == configurationSetID)
                gotID = csId;
        }

        if (gotID == -1)
            return null;
        else
            return new ConfigurationSet(gotID, this);
    }

    /**
     * Returns configuration set with required database ID.
     */
    public Configuration getConfigurationByID(int configurationID) throws SQLException {
        int gotID = -1;
        for (int cId : getMgtSchema().getIDColumnValues(new WegovConfiguration(), "ID")) {
            if (cId == configurationID)
                gotID = cId;
        }

        if (gotID == -1)
            return null;
        else
            return new Configuration(gotID, this);
    }
    
    
    /**
     * Returns worksheet with required database ID.
     */
    public Worksheet getWorksheetByID(int worksheetID) throws SQLException {
        int gotID = -1;
        for (int wfId : getMgtSchema().getIDColumnValues(new WegovWorksheet(), "ID")) {
            if (wfId == worksheetID)
                gotID = wfId;
        }

        if (gotID == -1)
            return null;
        else
            return new Worksheet(gotID, this);
    }

    /**
     * Returns activity with required database ID.
     */
    public Activity getActivityByID(int activityID) throws SQLException {
        int gotID = -1;
        for (int wfId : getMgtSchema().getIDColumnValues(new WegovActivity(), "ID")) {
            if (wfId == activityID)
                gotID = wfId;
        }

        if (gotID == -1)
            return null;
        else
            return new Activity(gotID, this);
    }

    /**
     * Returns Run with required database ID.
     */
    public Run getRunByID(int runID) throws SQLException {
        int gotID = -1;
        for (int wfId : getMgtSchema().getIDColumnValues(new WegovRun(), "ID")) {
            if (wfId == runID)
                gotID = wfId;
        }

        if (gotID == -1)
            return null;
        else
            return new Run(gotID, this);
    }

    /**
     * Returns all activities.
     */
    public ArrayList<Activity> getActivities() throws SQLException {
        ArrayList<Activity> activities = new ArrayList<Activity>();
        for (int activityId : getMgtSchema().getIDColumnValues(new WegovActivity(), "ID")) {
            activities.add(new Activity(activityId, this));
        }
        return activities;
    }

    /**
     * Returns all worksheets created by the policymaker.
     */
    public ArrayList<Worksheet> getWorksheetsByPolicymaker(Policymaker policyMaker) throws SQLException {
        ArrayList<Worksheet> wfs = new ArrayList<Worksheet>();
        int pmID = policyMaker.getID();

        for (int wfId : getMgtSchema().getIDColumnValuesWhere(new WegovPolicymaker_WegovWorksheet(), "WorksheetID", "PolicymakerID", pmID)) {
            wfs.add(new Worksheet(wfId, this));
        }
        return wfs;
    }

    /**
     * Returns all activities created by the policymaker.
     */
    public ArrayList<Activity> getActivitiesByPolicymaker(Policymaker policyMaker) throws SQLException {
        ArrayList<Activity> activities = new ArrayList<Activity>();
        int pmID = policyMaker.getID();

        for (int activityId : getMgtSchema().getIDColumnValuesWhere(new WegovPolicymaker_Activity(), "ActivityID", "PolicymakerID", pmID)) {
            activities.add(new Activity(activityId, this));
        }
        return activities;
    }

    /**
     * Returns all runs created by the policymaker.
     * @param activityId 
     */
    public ArrayList<Run> getRunsByPolicymaker(int activityId, Policymaker policyMaker) throws SQLException {
    	int pmID = policyMaker.getID();

    	/*
    	ArrayList<Run> runs = new ArrayList<Run>();

    	for (int runId : getMgtSchema().getIDColumnValuesWhere(new WegovPolicymaker_Run(), "RunID", "PolicymakerID", pmID)) {
    		runs.add(new Run(runId, this));
    	}
    	*/
    	Activity activity = getActivityByID(activityId);
    	if (activity.getPolicyMaker().getID() != pmID) {
    		throw new SQLException("Policymaker " + pmID + " does not own activity " + activityId);
    	}
    	ArrayList<Run> runs = activity.getRuns();
    	return runs;
    }

    /**
     * Returns all policymakers.
     */
    public ArrayList<Policymaker> getPolicymakers() throws SQLException {
        ArrayList<Policymaker> result = new ArrayList<Policymaker>();
        ArrayList<WegovPolicymaker> wegovpms = getMgtSchema().getAll(new WegovPolicymaker());

        for (WegovPolicymaker wegovpm : wegovpms) {
            result.add(new Policymaker(wegovpm.getID(), this));
        }

        return result;
    }

    /**
     * Returns policymaker with matching username.
     */
    public Policymaker getPolicymakerByUsername(String userName) throws SQLException {
        ArrayList<WegovPolicymaker> wegovpms = getMgtSchema().getAllWhere(new WegovPolicymaker(), "Username", userName);

        if (wegovpms.isEmpty())
            return null;
        else {
            return new Policymaker(wegovpms.get(0).getID(), this);
        }
    }

    /**
     * Returns all roles.
     */
    public ArrayList<Role> getRoles() throws SQLException {
        ArrayList<Role> result = new ArrayList<Role>();
        ArrayList<WegovPolicymakerRole> roles = getMgtSchema().getAll(new WegovPolicymakerRole());

        for (WegovPolicymakerRole role : roles) {
            result.add(new Role(role.getID(), this));
        }

        return result;
    }


    public int getPolicymakerOwnerIdFromRunId(String runId) throws SQLException {

        //String runIdStr = String.valueOf(runId);
        int runIdInt = Integer.parseInt(runId);

        System.out.println ("Run id = " + runIdInt);

        ArrayList<WegovPolicymaker_Run> wegovpms =
                getMgtSchema().getAllUniqueWhere(new WegovPolicymaker_Run(), "RunID", runIdInt);
                //getMgtSchema().getAllUniqueWhere(new WegovPolicymaker_Run(), "RunID", runId);
                //getMgtSchema().getAllWhere(new WegovPolicymaker_Run(), "RunID", runIdInt);

        if (wegovpms.size() > 1) {
          throw new SQLException ("Error! Run ID " + runId + "has more than one ownwer!");
        }

        if (wegovpms.isEmpty())
            return -1;
        else {
          System.out.println("Owner of Run ID " + runId + " is Policymaker ID = " + wegovpms.get(0).getPolicymakerID());
          return wegovpms.get(0).getPolicymakerID();
        }
    }


    public ArrayList<KoblenzAnalysisTopicWrapper> getKoblenzAnalysisTopicsForRun(int runID) throws SQLException {
        ArrayList<KoblenzAnalysisTopicWrapper> results = new ArrayList<KoblenzAnalysisTopicWrapper>();

        for (int topicId : getDataSchema().getIDColumnValuesWhere(new WegovAnalysisKoblenzTopic(), "TopicID", "OutputOfRunID", runID)) {
            results.add(new KoblenzAnalysisTopicWrapper(topicId, runID, this));
        }


        return results;
    }


    public KmiAnalysisResultsWrapper getKmiResultsForRun(int runID) throws SQLException {

        return new KmiAnalysisResultsWrapper(runID, this);
    }


    /*
     * Useful methods
     */
    private String verifyProperties(Properties properties) {
        String result = null;

        String[] mustHaveProperties = new String[]{
          "postgres.url", "postgres.login", "postgres.pass",
          "postgres.database.name",
          "postgres.database.mgt.name",
          "postgres.database.data.name",
          "kmi.pathto.datafolder",
          "wegov.widget.template.source.user",
          "wegov.widget.template.source.user.password"};

        for (String mustHaveproperty : mustHaveProperties) {
            if (!properties.containsKey(mustHaveproperty)) {
                result = "Missing key: " + mustHaveproperty;
                break;
            } else {
                if (properties.getProperty(mustHaveproperty).equals("")) {
                    result = "Missing property for key: " + mustHaveproperty;
                    break;
                }
            }
        }

        return result;

    }

    /**
     * Returns database connector.
     */
    public PostgresConnector getConnector(String schemaName) {
        return connectors.get(schemaName);
    }

    /**
     * Returns the database.
     */
    public SqlDatabase getDatabase() {
        return database;
    }
}