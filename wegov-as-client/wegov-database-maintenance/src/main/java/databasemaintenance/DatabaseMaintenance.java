/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package databasemaintenance;

import eu.wegov.coordinator.Configuration;
import eu.wegov.coordinator.ConfigurationSet;
import eu.wegov.coordinator.Coordinator;
import eu.wegov.coordinator.Policymaker;
import eu.wegov.coordinator.Role;
import eu.wegov.coordinator.dao.data.WegovSNS;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author max
 */
public class DatabaseMaintenance {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
//        Coordinator coordinator = new Coordinator("../wegov-dashboard/coordinator.properties");
//        String wegovToolsHome = (new File("../wegov-tools")).getCanonicalPath();
        Coordinator coordinator = new Coordinator("/var/lib/tomcat7/coordinator.properties");
        String wegovToolsHome = (new File("/var/lib/tomcat7/")).getCanonicalPath();

        //coordinator.wipeDatabase();
        coordinator.setupWegovDatabase(); // Creates a new user - omg
        
        if (coordinator.getPolicymakers().size() < 2) {
            Role userRole = coordinator.getDefaultUserRole();

            addTemplateWidgetWegovUser(coordinator);

            coordinator.createPolicyMaker("Test user", userRole, "Experimedia", "user", "test");

            addSearchTool(coordinator, wegovToolsHome + "/wegov-search-tool-2.0-jar-with-dependencies.jar");
            addGroupsSearchTool(coordinator, wegovToolsHome + "/wegov-search-tool-2.0-jar-with-dependencies.jar");
            addGroupsSearchAndAnalysisTool(coordinator, wegovToolsHome + "/wegov-search-analysis-tool-2.0-jar-with-dependencies.jar");
            addInjectTool(coordinator, wegovToolsHome + "/wegov-inject-tool-2.0-jar-with-dependencies.jar");
            addTopicOpinionTool(coordinator, wegovToolsHome + "/wegov-analysis-tool-2.0-jar-with-dependencies.jar");
            addKMITool(coordinator, wegovToolsHome + "/wegov-analysis-tool-2.0-jar-with-dependencies.jar");

    //        addSearchTool(coordinator, wegovToolsHome + "/search/target/wegov-search-tool-2.0-jar-with-dependencies.jar");
    //        addGroupsSearchTool(coordinator, wegovToolsHome + "/search/target/wegov-search-tool-2.0-jar-with-dependencies.jar");
    //        addGroupsSearchAndAnalysisTool(coordinator, wegovToolsHome + "/search-analysis/target/wegov-search-analysis-tool-2.0-jar-with-dependencies.jar");
    //        addInjectTool(coordinator, wegovToolsHome + "/inject/target/wegov-inject-tool-2.0-jar-with-dependencies.jar");
    //        addTopicOpinionTool(coordinator, wegovToolsHome + "/analysis/target/wegov-analysis-tool-2.0-jar-with-dependencies.jar");
    //        addKMITool(coordinator, wegovToolsHome + "/analysis/target/wegov-analysis-tool-2.0-jar-with-dependencies.jar");


            for (Policymaker pm : coordinator.getPolicymakers()) {
                System.out.println(pm);
            }
        }

        System.out.println("Done");
    }

    public static void addSNS(Coordinator coordinator) throws Exception {
        final WegovSNS sns = new WegovSNS("twitter", "Twitter", "http://twitter.com/", "http://twitter.com/favicon.ico", 0);
        coordinator.getDataSchema().insertObject(sns);
    }

    public static void addTemplateWidgetWegovUser(Coordinator coordinator) throws Exception {
        Role userRole = coordinator.getDefaultUserRole();
        Role adminRole = coordinator.getDefaultAdminRole();

        /*
         * Policymaker templateWidgetsUser = coordinator.createPolicyMaker(
         * "Wegov Template Widgets", adminRole, "WeGov", "template-widgets",
         * "cvbgt543edfr" );
         */

        Policymaker templateWidgetsUser = coordinator.getTemplateWidgetsSourceUser();

        int templateWidgetsUserId = templateWidgetsUser.getID();

        int wsId = coordinator.getDefaultWidgetSetForPM(templateWidgetsUserId).getId();

        /*
         * public int createWidget( int wsId, String columnName, int
         * columnOrderNum, int pmId, String name, String description, String
         * type, String datatype, String dataAsString, String
         * parametersAsString, int isVisible )
         */

        coordinator.createWidget(
                wsId, "columnright", 0, templateWidgetsUserId,
                "Local Tweets", "Recent tweets for current location",
                "search", "twitterLocal", "tweets",
                "", "{\"term\":\"search term\"}", 0,
                "Enter a search term");

        coordinator.createWidget(
                wsId, "columnleft", 3, templateWidgetsUserId,
                "Facebook Group Posts", "This chart shows current posts for the selected group",
                "search", "groupposts", "posts",
                "", "{\"term\":\"unspecified\"}", 0,
                "Enter a Facebook Group ID");

        coordinator.createWidget(
                wsId, "columnleft", 4, templateWidgetsUserId,
                "Comments on Facebook Post", "This chart shows comments for the selected post",
                "search", "grouppostcomments", "posts",
                "", "{\"term\":\"unspecified\"}", 0,
                "Enter Facebook post ID, e.g. 59788447049_141536755968221");

        coordinator.createWidget(
                wsId, "columnright", 6, templateWidgetsUserId,
                "Recent Tweets", "Recent tweets",
                "search", "twitterbasic", "tweets",
                "", "{\"term\":\"search term\"}", 0,
                "Enter a search term");


        /*
         * coordinator.createWidget( wsId, "columnleft", 1,
         * templateWidgetsUserId, "Topics Analysis ", "This chart shows topic
         * analysis", "topicanalysis", "analysis", "", "{\"term\":\"search
         * term\"}", 0 );
         */
        /*
         * coordinator.createWidget( wsId, "columnright", 1,
         * templateWidgetsUserId, "Main Local Topics on", "This chart shows
         * topic analysis", "topicanalysis", "analysis", "", "{\"term\":\"Klaus
         * Wowereit\", \"location\":\"current\"}", 0 );
         */

        coordinator.createWidget(
                wsId, "columnleft", 2, templateWidgetsUserId,
                "Key Users for Role", "User roles for search term",
                "wegov_analysis", "roleforterm", "roles",
                "", "{\"term\":\"search term\", \"role\":\"Rare Poster\"}", 0,
                "Select a Twitter search widget as input");

        coordinator.createWidget(
                wsId, "columnright", 2, templateWidgetsUserId,
                "User Roles", "This chart shows user distribution per role",
                "wegov_analysis", "userroles", "roles",
                "", "{\"term\":\"unspecified\"}", 0,
                "Select a Twitter search widget as input");


        coordinator.createWidget(
                wsId, "columnleft", 6, templateWidgetsUserId,
                "Topic Analysis", "This chart shows topic analysis using tweets from wegov DB",
                "wegov_analysis", "topics_from_database", "topics",
                "", "{\"term\":\"unspecified\"}", 0,
                "Select a search widget as input");
        /*
         * coordinator.createWidget( wsId, "columnleft", 5,
         * templateWidgetsUserId, "Facebook Post Topics", "This chart shows
         * topic analysis for the selected group post", "wegov_analysis",
         * "groupposttopicanalysis", "topics", "", "{\"term\":\"unspecified\"}",
         * 0, "Enter a Facebook Post ID" );
         */

        coordinator.createWidget(
                wsId, "columnleft", 5, templateWidgetsUserId,
                "Trending Now", "Trending topics for location",
                "external_analysis", "trending", "trends",
                "", "{\"term\":\"unspecified\"}", 0,
                "Enter the name of a country or major city");

        coordinator.createWidget(
                wsId, "columnleft", 5, templateWidgetsUserId,
                "Peerindex for Twitter User", "This chart shows peerindex",
                "external_analysis", "peerindex", "peerindex",
                "", "{\"term\":\"unspecified\"}", 0,
                "Enter a Twitter user ID");


        /*
         *
         * coordinator.createWidget(wsId, "columnleft", 3, id, "Facebook Posts
         * for", "This chart shows current posts for the selected group",
         * "groupposts", "posts", "", "{\"term\":\"AngelaMerkel\"}", 0);
         * coordinator.createWidget(wsId, "columnright", 3, id, "Facebook Topics
         * for Latest Post from", "This chart shows topic analysis for the
         * latest post from this user page", "latestgroupposttopicanalysis",
         * "analysis", "", "{\"term\":\"AngelaMerkel\"}", 0);
         * coordinator.createWidget(wsId, "columnleft", 4, id, "Comments on
         * Facebook Post", "This chart shows comments for the selected post",
         * "grouppostcomments", "posts", "",
         * "{\"term\":\"59788447049_290786810982672\"}", 0);
         * coordinator.createWidget(wsId, "columnright", 4, id, "Facebook Post
         * Topics", "This chart shows topic analysis for the selected group
         * post", "groupposttopicanalysis", "analysis", "",
         * "{\"term\":\"59788447049_379659095395726\"}", 0);
         *
         * //coordinator.createWidget(wsId, "widgets", 0, id, "All My
         * Activities", "This chart shows your all activities", "allactivities",
         * "", "", "", 1);
         */
    }

    /*
     * public static void setupExperimediaUsers(Coordinator coordinator) throws
     * Exception { Role userRole = coordinator.getDefaultUserRole();
     * coordinator.createPolicyMaker("Stephen Phillips", userRole, "IT
     * Innovation", "scp", "scplmko"); coordinator.createPolicyMaker("Simon
     * Crowle", userRole, "IT Innovation", "sgc", "sgcokmjhn");
     *
     * coordinator.createPolicyMaker("Experimedia 01", userRole, "Experimedia",
     * "experimedia01", "wegov01exp");
     * coordinator.createPolicyMaker("Experimedia 02", userRole, "Experimedia",
     * "experimedia02", "wegov02exp");
     * coordinator.createPolicyMaker("Experimedia 03", userRole, "Experimedia",
     * "experimedia03", "wegov03exp");
     * coordinator.createPolicyMaker("Experimedia 04", userRole, "Experimedia",
     * "experimedia04", "wegov04exp");
     * coordinator.createPolicyMaker("Experimedia 05", userRole, "Experimedia",
     * "experimedia05", "wegov05exp");
     * coordinator.createPolicyMaker("Experimedia 06", userRole, "Experimedia",
     * "experimedia06", "wegov06exp");
     * coordinator.createPolicyMaker("Experimedia 07", userRole, "Experimedia",
     * "experimedia07", "wegov07exp");
     * coordinator.createPolicyMaker("Experimedia 08", userRole, "Experimedia",
     * "experimedia08", "wegov08exp");
     * coordinator.createPolicyMaker("Experimedia 09", userRole, "Experimedia",
     * "experimedia09", "wegov09exp");
     * coordinator.createPolicyMaker("Experimedia 10", userRole, "Experimedia",
     * "experimedia10", "wegov10exp");
     *
     * }
     */
    public static void setupWegovUsers(Coordinator coordinator) throws Exception {
        Role userRole = coordinator.getDefaultUserRole();
        Role adminRole = coordinator.getDefaultAdminRole();

//        Policymaker max = coordinator.createPolicyMaker("Maxim Bashevoy", adminRole, "IT Innovation", "mbashevoy", "a2l3j45lk35b6l3jh4b6jhl35");
        Policymaker francesco = coordinator.createPolicyMaker("Francesco Timperi Tiberi", adminRole, "GFI", "francesco", "JkX6K47gv23LezFanC8rQ");
        Policymaker ken = coordinator.createPolicyMaker("Ken Meacham", adminRole, "IT Innovation", "ken", "h7U68soE3Nm2YJ4cCAxuq");
        Policymaker steve = coordinator.createPolicyMaker("Steve Taylor", adminRole, "IT Innovation", "steve", "sjt098");
        Policymaker max = coordinator.createPolicyMaker("Maxim Bashevoy", adminRole, "IT Innovation", "mbashevoy", "admin");
        Policymaker kem = coordinator.createPolicyMaker("Ken Meacham", adminRole, "IT Innovation", "kem", "kem098");

        coordinator.createPolicyMaker("Paul Walland", userRole, "IT Innovation", "paul", "6jhl35");

        coordinator.createPolicyMaker("Sofia Angeletou", userRole, "KMi", "kmiuser", "nji90okm");
        coordinator.createPolicyMaker("Eric Bernard", userRole, "GFI", "ericbernard", "cft67ygv");
        coordinator.createPolicyMaker("Somya Joshi", userRole, "Gov2u", "SomyaJoshi", "gfde345r");
        coordinator.createPolicyMaker("Timo Wandh\u00f6fer", userRole, "GESIS", "Timo", "t67ygvcf");
        coordinator.createPolicyMaker("Mark Thamm", userRole, "GESIS", "Mark", "mko09ijn");
        coordinator.createPolicyMaker("Peter Mutschke", userRole, "GESIS", "Peter", "p09okm");
        coordinator.createPolicyMaker("Robert Weichselbaum", userRole, "GESIS", "Robert", "bgt67yhn");
        coordinator.createPolicyMaker("Christoph Ringelstein", userRole, "University of Koblenz-Landau", "cringel", "cde34rfv");
        coordinator.createPolicyMaker("Harith Alani", userRole, "KMi", "halani", "hu89ijnb");
        coordinator.createPolicyMaker("Sergej Sizov", userRole, "University of Koblenz-Landau", "Sergej", "se45rdcx");
        coordinator.createPolicyMaker("Freddy Fallon", userRole, "Hansard Society", "Freddy", "fr45tgbv");
        coordinator.createPolicyMaker("Test User", userRole, "IT Innovation", "test", "test098");
        coordinator.createPolicyMaker("Miriam Fernandez", userRole, "KMi", "miriam", "DCZjzBU");
        coordinator.createPolicyMaker("Bernard de Dorlodot", userRole, "GFI", "bernard", "gv23Lez");
        coordinator.createPolicyMaker("Beccy Allen", userRole, "Hansard Society", "beccy", "soE3Nm");
        coordinator.createPolicyMaker("Catherine Van Eeckhaute", userRole, "Gov2u", "catherine", "cde34rfv");
        /*
         * String userName; String userFullName; String userPassword; for (int i
         * = 1; i <= 60; i++) { userName = "wegov" + i; userFullName = "Wegov
         * User " + i; userPassword = "we" + i + "gov";
         * System.out.println(userName + " : " + userPassword);
         * coordinator.createPolicyMaker(userFullName, userRole, "European
         * Parliament", userName, userPassword); }
         */
    }

    public static void addTopicOpinionTool(Coordinator c, String jarPath) throws Exception {
        final ConfigurationSet s4 = c.createConfigurationSet("topic-opinion", "Topic opinion Analysis",
                "topic_opinion_parameters.jsp");
        final Configuration c4 = c.createConfiguration("Topic opinion Analysis Tool Configuration",
                "java -Duser.language=en -Duser.region=GB -jar " + jarPath, "Topic opinion Configuration", "topic_opinion_parameters.jsp");
        c4.addParameterAsAdmin("analysis.type", "Analysis Type", "topic-opinion");
        c4.addParameterAsAdmin("analysis.subType", "Analysis Sub-Type", "");
        c4.addParameterAsAdmin("analysis.input-data-spec", "JSON-formatted input data specification containing activities and runs", "");
        c4.addParameterAsAdmin("numberOfTopicsToReturn", "How many topics to return", "3");
        c4.addParameterAsAdmin("outputOfType", "Classname of generated output", "eu.wegov.coordinator.KoblenzAnalysisTopicWrapper");
        c4.addParameterAsAdmin("analysisLanguage", "analysis language", "en");
        s4.addConfiguration(c4);

        System.out.println("addTopicOpinionTool done");
    }

    public static void addKMITool(Coordinator c, String jarPath) throws Exception {
        final ConfigurationSet s4 = c.createConfigurationSet("behaviour", "KMI Analysis",
                "kmi_parameters.jsp");
        final Configuration c4 = c.createConfiguration("KMI Analysis Tool Configuration",
                "java -Duser.language=en -Duser.region=GB -jar " + jarPath, "KMI Configuration", "kmi_parameters.jsp");
        c4.addParameterAsAdmin("analysis.type", "Analysis Type", "behaviour");
        c4.addParameterAsAdmin("analysis.subType", "Analysis Sub-Type", "");
        c4.addParameterAsAdmin("analysis.input-data-spec", "JSON-formatted input data specification containing activities and runs", "");
        //c4.addParameterAsAdmin("numberOfTopicsToReturn", "How many topics to return", "3");
        c4.addParameterAsAdmin("outputOfType", "Classname of generated output", "eu.wegov.coordinator.KmiAnalysisTopicWrapper");
        c4.addParameterAsAdmin("analysisLanguage", "analysis language", "en");
        s4.addConfiguration(c4);

        System.out.println("addKmiTool done");
    }

    public static void updateConfigurationAddParameter(
            Coordinator c, int confId,
            String name,
            String comment,
            String defaultValue) throws Exception {
        // look up conf ID for admin 
        Configuration c4 = c.getConfigurationByID(confId);
        c4.addParameterAsAdmin(name, comment, defaultValue);
        //c4.addParameterAsAdmin("analysisLanguage", "analysis language", "en"); 
    }

    public static void addSearchTool(Coordinator c, String jarPath) throws Exception {

        // Create initial table of social network sites
//        final WegovSNS sns = new WegovSNS("twitter", "Twitter", "http://twitter.com/", "http://twitter.com/favicon.ico",0);
//        c.getDataSchema().insertObject(sns);

        // Create Advanced Search Tool
        final ConfigurationSet configurationSet = c.createConfigurationSet("Adv Search", "Advanced search",
                "search_parameters.jsp");

        final Configuration configuration = c.createConfiguration("Adv Search Configuration",
                "java -Duser.language=en -Duser.region=GB -jar " + jarPath,
                "Perform advanced search", "search_parameters.jsp");

        configuration.addParameterAsAdmin(
                "concatenate.list_elements",
                "Specifies a comma separated list of parameters need to be populated concatenating values backed by checkbox selection",
                "sources");
        configuration.addParameterAsUser("what.collect", "What to collect", "posts");

        configuration.addParameterAsUser("what.words.all", "", "");
        configuration.addParameterAsUser("what.words.exactphrase", "", "");
        configuration.addParameterAsUser("what.words.any", "", "");
        configuration.addParameterAsUser("what.words.none", "", "");
        configuration.addParameterAsUser("what.words.hashtags", "", "");

        configuration.addParameterAsUser("what.people.from.accounts", "", "");
        configuration.addParameterAsUser("what.people.to.accounts", "", "");
        configuration.addParameterAsUser("what.people.mentioning.accounts", "", "");

        configuration.addParameterAsUser("what.people.from.groups", "", "");
        configuration.addParameterAsUser("what.people.to.groups", "", "");
        configuration.addParameterAsUser("what.people.mentioning.groups", "", "");

        configuration.addParameterAsUser("what.words.name.idortag", "", "");
        configuration.addParameterAsUser("what.words.name.contains", "", "");

        configuration.addParameterAsUser("what.dates.option", "", "any");
        configuration.addParameterAsUser("what.dates.since", "", "");
        configuration.addParameterAsUser("what.dates.until", "", "");

        configuration.addParameterAsUser("sites", "Site(s) to search", "socialmention");
        configuration.addParameterAsUser("sources", "Aggregator sources", ""); // default is all sources

        //configuration.addParameterAsUser("location", "", "any"); //deprecated

        //New location params
        configuration.addParameterAsUser("location.option", "", "anywhere");
        configuration.addParameterAsUser("location.useapi", "", "true");
        configuration.addParameterAsUser("location.appendtosq", "", "false");

        configuration.addParameterAsUser("location.city", "", "");
        configuration.addParameterAsUser("location.region", "", "");
        configuration.addParameterAsUser("location.countryName", "", "");
        configuration.addParameterAsUser("location.countryCode", "", "");
        configuration.addParameterAsUser("location.lat", "", "");
        configuration.addParameterAsUser("location.long", "", "");
        configuration.addParameterAsUser("location.radius", "", "2");
        configuration.addParameterAsUser("location.radius.unit", "", "mi");
        //End of location params

        configuration.addParameterAsUser("language", "", "any");

        configuration.addParameterAsUser("outputOfType", "Full class name for the output of this configuration", "eu.wegov.coordinator.dao.data.WegovPostItem");
        configuration.addParameterAsUser("results.type", "", "static");
        configuration.addParameterAsUser("results.max.results.option", "", "unlimited");
        configuration.addParameterAsUser("results.max.results", "", "");
        configuration.addParameterAsUser("results.max.per.page", "", "");
        configuration.addParameterAsUser("results.max.pages", "", "");
        configuration.addParameterAsUser("results.max.collection.time.option", "Limit collection time", "");
        configuration.addParameterAsUser("results.max.collection.time", "Maximum collection time in secs", "");
        configuration.addParameterAsUser("results.storage.keeprawdata", "Store as raw JSON", "true");
        configuration.addParameterAsUser("results.storage.storeindb", "Store in PostItem table, etc", "false");
        configuration.addParameterAsUser("results.collect.since.last.run", "Collect results since latest post in previous run", "false");

        configurationSet.addConfiguration(configuration);

        System.out.println("addSearchTool done");
    }     

    public static void addInjectTool(Coordinator c, String jarPath) throws Exception {
        final ConfigurationSet s4 = c.createConfigurationSet("Injection", "Injection tool",
                "inject_parameters.jsp");
        final Configuration c4 = c.createConfiguration("Inject Tool Configuration",
                "java -Duser.language=en -Duser.region=GB -jar " + jarPath, "Inject Configuration", "inject_parameters.jsp");
        c4.addParameterAsUser("sites", "Site(s) to post to", "twitter");
        c4.addParameterAsUser("post.option", "Post option", "post");
        c4.addParameterAsUser("post.text", "Post text", "");
        c4.addParameterAsUser("post.replyto.userid", "User ID to reply to", "");
        c4.addParameterAsUser("post.replyto.postid", "Post ID to reply to", "");
        c4.addParameterAsUser("post.hashtags", "Hashtags to append to post", "");
        c4.addParameterAsUser("outputOfType", "Full class name for the output of this configuration", "eu.wegov.coordinator.dao.data.WegovPostItem");

        s4.addConfiguration(c4);

        System.out.println("addInjectTool done");
    }

    public static void addGroupsSearchTool(Coordinator c, String jarPath) throws Exception {
        final ConfigurationSet s4 = c.createConfigurationSet("Groups Search", "Groups search", "group_search_parameters.jsp");
        final Configuration c4 = c.createConfiguration("Groups Search Tool Configuration",
                "java -Duser.language=en -Duser.region=GB -jar " + jarPath, "Groups Search Configuration", "group_search_parameters.jsp");

        c4.addParameterAsUser("sites", "Site(s) to search", "facebook");
        c4.addParameterAsUser("what.collect", "What to collect", "posts");
        c4.addParameterAsUser("collectComments", "Collect comments for posts?", "true");
        c4.addParameterAsUser("maxPostsToCollectCommentsFor", "How many latest posts to collect comments for", "10");
        c4.addParameterAsUser("pages", "How many pages to collect", "single"); // single/all
        c4.addParameterAsUser("group.id", "ID of group to search", "");
        c4.addParameterAsUser("access.token", "Access token", "");
        c4.addParameterAsUser("outputOfType", "Full class name for the output of this configuration", "eu.wegov.coordinator.dao.data.WegovPostItem");
        c4.addParameterAsUser("results.max.results.option", "", "unlimited");
        c4.addParameterAsUser("results.max.results", "", "");
        c4.addParameterAsUser("results.max.per.page", "", "");
        c4.addParameterAsUser("results.max.pages", "", "");
        c4.addParameterAsUser("results.storage.keeprawdata", "Store as raw JSON", "true");
        c4.addParameterAsUser("results.storage.storeindb", "Store in PostItem table, etc", "false");
        c4.addParameterAsUser("results.collect.since.last.run", "Collect results since latest post in previous run", "false");

        s4.addConfiguration(c4);

        System.out.println("addGroupsSearchTool done");
    }

    public static void addGroupsSearchAndAnalysisTool(Coordinator c, String jarPath) throws Exception {
        final ConfigurationSet s4 = c.createConfigurationSet("Groups Search and Analysis", "Groups search", "group_search_parameters.jsp");
        final Configuration c4 = c.createConfiguration("Groups Search Tool Configuration",
                "java -Duser.language=en -Duser.region=GB -jar " + jarPath, "Groups Search Configuration", "group_search_parameters.jsp");

        c4.addParameterAsUser("sites", "Site(s) to search", "facebook");
        c4.addParameterAsUser("what.collect", "What to collect", "posts");
        c4.addParameterAsUser("collectComments", "Collect comments for posts?", "true");
        c4.addParameterAsUser("maxPostsToCollectCommentsFor", "How many latest posts to collect comments for", "10");
        c4.addParameterAsUser("pages", "How many pages to collect", "single"); // single/all
        c4.addParameterAsUser("group.id", "ID of group to search", "");
        c4.addParameterAsUser("access.token", "Access token", "");
        c4.addParameterAsUser("outputOfType", "Full class name for the output of this configuration", "eu.wegov.coordinator.dao.data.WegovPostItem");
        c4.addParameterAsUser("results.max.results.option", "", "unlimited");
        c4.addParameterAsUser("results.max.results", "", "");
        c4.addParameterAsUser("results.max.per.page", "", "");
        c4.addParameterAsUser("results.max.pages", "", "");
        c4.addParameterAsUser("results.storage.keeprawdata", "Store as raw JSON", "true");
        c4.addParameterAsUser("results.storage.storeindb", "Store in PostItem table, etc", "false");
        c4.addParameterAsUser("results.collect.since.last.run", "Collect results since latest post in previous run", "false");

        s4.addConfiguration(c4);

        System.out.println("addGroupsSearchTool done");
    }

    public static void addLanguageParamsToAnalysisTools(Coordinator c) throws Exception {
        //updateConfigurationAddParameter (coordinator, 10, "analysisLanguage", "analysis language", "en");                 
        //updateConfigurationAddParameter (coordinator, 8, "analysisLanguage", "analysis language", "en");                 
        addLanguageParamsToTool(c, "topic-opinion");
        addLanguageParamsToTool(c, "behaviour");
    }

    public static void addLanguageParamsToTool(Coordinator c, String toolName) throws Exception {
        System.out.println("Adding language parameter to tool: " + toolName);
        Configuration toolConf = getToolConfiguration(c, toolName);
        System.out.println("\nCurrent configuration:");
        System.out.println(toolConf);

        updateConfigurationAddParameter(c, toolConf.getID(), "analysisLanguage", "analysis language", "en");

        Configuration toolConfNew = getToolConfiguration(c, toolName);
        System.out.println("\nNew configuration:");
        System.out.println(toolConfNew);
    }

    public static Configuration getToolConfiguration(Coordinator c, String toolName) throws Exception {
        ConfigurationSet toolConfSet = null;
        ArrayList<ConfigurationSet> tools = c.getTools();
        for (ConfigurationSet configurationSet : tools) {
            if (configurationSet.getName().equals(toolName)) {
                toolConfSet = configurationSet;
            }
        }

        ArrayList<Configuration> toolConfigurations = toolConfSet.getConfigurations();

        Configuration toolConfiguration = toolConfigurations.get(0); //assuming one conf per confSet
        return toolConfiguration;
    }
}
