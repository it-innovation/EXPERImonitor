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
        Coordinator coordinator = new Coordinator("../wegov-dashboard/coordinator.properties");

        coordinator.wipeDatabase();
        coordinator.setupWegovDatabase();

        
/*        
        // wegov Dev site config
        String wegovToolsHome = "/root/wegov-3.0-trunk/wegov-parent/wegov-tools/";
        

        addTopicOpinionTool(coordinator, wegovToolsHome + "analysis/target/wegov-analysis-tool-2.0-jar-with-dependencies.jar");

        addKMITool(coordinator, wegovToolsHome + "analysis/target/wegov-analysis-tool-2.0-jar-with-dependencies.jar");
*/
        
        //addLanguageParamsToAnalysisTools(coordinator);
        
   

//
        Role userRole = coordinator.getDefaultUserRole();

        addTemplateWidgetWegovUser(coordinator);
        
        coordinator.createPolicyMaker("Sample user", userRole, "Experimedia", "user", "test");

//        setupWegovUsers(coordinator);
      //setupExternalUsers(coordinator);	// uncomment for wegov site
      //setupGesisUsers(coordinator);		// uncomment for wegov site
	  //setupExperimediaUsers(coordinator); // wegov-dev only



        // wegov Dev site config
        //String wegovToolsHome = "/root/wegov-2.6-branch/wegov-parent/wegov-tools/";
        // wegov Dev site config



        // SJT Config
        //String wegovToolsHome = "C:/Users/sjt/Documents/Work/WeGov-Code-External-SVN/trunk/wegov/wegov-parent/wegov-tools/";
        //String wegovToolsHome = "C:/work_code/wegov/trunk/wegov/wegov-parent/wegov-tools/";
        // End SJT Config



        // KEM Config
        String wegovToolsHome = (new File("../wegov-tools")).getCanonicalPath();

        // End KEM Config


        addSearchTool(coordinator, wegovToolsHome + "/search/target/wegov-search-tool-2.0-jar-with-dependencies.jar");
        addGroupsSearchTool(coordinator, wegovToolsHome + "/search/target/wegov-search-tool-2.0-jar-with-dependencies.jar");
        addInjectTool(coordinator, wegovToolsHome + "/inject/target/wegov-inject-tool-2.0-jar-with-dependencies.jar");


//        Building jar: C:\Users\sjt\Documents\Work\WeGov-Code-External-SVN\trunk\wegov\wegov-parent\wegov-tools\analysis\target\wegov-analysis-tool-2.0-jar-with-dependencies.jar
        addTopicOpinionTool(coordinator, wegovToolsHome + "/analysis/target/wegov-analysis-tool-2.0-jar-with-dependencies.jar");

        addKMITool(coordinator, wegovToolsHome + "/analysis/target/wegov-analysis-tool-2.0-jar-with-dependencies.jar");


        for (Policymaker pm : coordinator.getPolicymakers()) {
        	System.out.println(pm);
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
        Policymaker templateWidgetsUser = coordinator.createPolicyMaker(
                "Wegov Template Widgets", adminRole, "WeGov", "template-widgets", "cvbgt543edfr"
        );
*/

        Policymaker templateWidgetsUser = coordinator.getTemplateWidgetsSourceUser();

        int templateWidgetsUserId =    templateWidgetsUser.getID();

        int wsId = coordinator.getDefaultWidgetSetForPM(templateWidgetsUserId).getId();

        /*
        public int createWidget(
        *   int wsId, String columnName, int columnOrderNum, int pmId,
        *   String name, String description,
        *   String type, String datatype,
        *   String dataAsString, String parametersAsString, int isVisible
        * )
        */

        coordinator.createWidget(
                wsId, "columnright", 0, templateWidgetsUserId,
                "Local Tweets", "Recent tweets for current location",
                "search", "twitterLocal", "tweets",
                "", "{\"term\":\"search term\"}", 0,
                "Enter a search term"
        );

        coordinator.createWidget(
                wsId, "columnleft", 3, templateWidgetsUserId,
                "Facebook Group Posts", "This chart shows current posts for the selected group",
                "search", "groupposts", "posts",
                "", "{\"term\":\"unspecified\"}", 0,
                "Enter a Facebook Group ID"
        );

        coordinator.createWidget(
                wsId, "columnleft", 4, templateWidgetsUserId,
                "Comments on Facebook Post", "This chart shows comments for the selected post",
                "search", "grouppostcomments", "posts",
                "", "{\"term\":\"unspecified\"}", 0,
                "Enter Facebook post ID, e.g. 59788447049_141536755968221"
        );

        coordinator.createWidget(
                wsId, "columnright", 6, templateWidgetsUserId,
                "Recent Tweets", "Recent tweets",
                "search", "twitterbasic", "tweets",
                "", "{\"term\":\"search term\"}", 0,
                "Enter a search term"
        );


/*
        coordinator.createWidget(
                wsId, "columnleft", 1, templateWidgetsUserId,
                "Topics Analysis ", "This chart shows topic analysis",
                "topicanalysis", "analysis",
                "", "{\"term\":\"search term\"}", 0
         );
  */
/*
        coordinator.createWidget(
                wsId, "columnright", 1, templateWidgetsUserId,
                "Main Local Topics on", "This chart shows topic analysis",
                "topicanalysis", "analysis",
                "", "{\"term\":\"Klaus Wowereit\", \"location\":\"current\"}",
                0
        );
  */

        coordinator.createWidget(
                wsId, "columnleft", 2, templateWidgetsUserId,
                "Key Users for Role", "User roles for search term",
                "wegov_analysis", "roleforterm", "roles",
                "", "{\"term\":\"search term\", \"role\":\"Rare Poster\"}", 0,
                "Select a Twitter search widget as input"
        );

        coordinator.createWidget(
                wsId, "columnright", 2, templateWidgetsUserId,
                "User Roles", "This chart shows user distribution per role",
                "wegov_analysis", "userroles", "roles",
                "", "{\"term\":\"unspecified\"}", 0,
                "Select a Twitter search widget as input"
        );


        coordinator.createWidget(
                wsId, "columnleft", 6, templateWidgetsUserId,
                "Topic Analysis", "This chart shows topic analysis using tweets from wegov DB",
                "wegov_analysis", "topics_from_database", "topics",
                "", "{\"term\":\"unspecified\"}", 0,
                "Select a search widget as input"
        );
/*
        coordinator.createWidget(
                wsId, "columnleft", 5, templateWidgetsUserId,
                "Facebook Post Topics", "This chart shows topic analysis for the selected group post",
                "wegov_analysis", "groupposttopicanalysis", "topics",
                "", "{\"term\":\"unspecified\"}", 0,
                "Enter a Facebook Post ID"
        );
*/

        coordinator.createWidget(
                wsId, "columnleft", 5, templateWidgetsUserId,
                "Trending Now", "Trending topics for location",
                "external_analysis", "trending", "trends",
                "", "{\"term\":\"unspecified\"}", 0,
                "Enter the name of a country or major city"
        );

        coordinator.createWidget(
                wsId, "columnleft", 5, templateWidgetsUserId,
                "Peerindex for Twitter User", "This chart shows peerindex",
                "external_analysis", "peerindex", "peerindex",
                "", "{\"term\":\"unspecified\"}", 0,
                "Enter a Twitter user ID"
        );


/*

        coordinator.createWidget(wsId, "columnleft", 3, id, "Facebook Posts for", "This chart shows current posts for the selected group", "groupposts", "posts", "", "{\"term\":\"AngelaMerkel\"}", 0);
        coordinator.createWidget(wsId, "columnright", 3, id, "Facebook Topics for Latest Post from", "This chart shows topic analysis for the latest post from this user page", "latestgroupposttopicanalysis", "analysis", "", "{\"term\":\"AngelaMerkel\"}", 0);
        coordinator.createWidget(wsId, "columnleft", 4, id, "Comments on Facebook Post", "This chart shows comments for the selected post", "grouppostcomments", "posts", "", "{\"term\":\"59788447049_290786810982672\"}", 0);
        coordinator.createWidget(wsId, "columnright", 4, id, "Facebook Post Topics", "This chart shows topic analysis for the selected group post", "groupposttopicanalysis", "analysis", "", "{\"term\":\"59788447049_379659095395726\"}", 0);

        //coordinator.createWidget(wsId, "widgets", 0, id, "All My Activities", "This chart shows your all activities", "allactivities", "", "", "", 1);
*/
    }

/*
    public static void setupExperimediaUsers(Coordinator coordinator) throws Exception {
        Role userRole = coordinator.getDefaultUserRole();
        coordinator.createPolicyMaker("Stephen Phillips", userRole, "IT Innovation", "scp", "scplmko");
        coordinator.createPolicyMaker("Simon Crowle", userRole, "IT Innovation", "sgc", "sgcokmjhn");

        coordinator.createPolicyMaker("Experimedia 01", userRole, "Experimedia", "experimedia01", "wegov01exp");
        coordinator.createPolicyMaker("Experimedia 02", userRole, "Experimedia", "experimedia02", "wegov02exp");
        coordinator.createPolicyMaker("Experimedia 03", userRole, "Experimedia", "experimedia03", "wegov03exp");
        coordinator.createPolicyMaker("Experimedia 04", userRole, "Experimedia", "experimedia04", "wegov04exp");
        coordinator.createPolicyMaker("Experimedia 05", userRole, "Experimedia", "experimedia05", "wegov05exp");
        coordinator.createPolicyMaker("Experimedia 06", userRole, "Experimedia", "experimedia06", "wegov06exp");
        coordinator.createPolicyMaker("Experimedia 07", userRole, "Experimedia", "experimedia07", "wegov07exp");
        coordinator.createPolicyMaker("Experimedia 08", userRole, "Experimedia", "experimedia08", "wegov08exp");
        coordinator.createPolicyMaker("Experimedia 09", userRole, "Experimedia", "experimedia09", "wegov09exp");
        coordinator.createPolicyMaker("Experimedia 10", userRole, "Experimedia", "experimedia10", "wegov10exp");

    }
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
		String userName;
		String userFullName;
		String userPassword;
		for (int i = 1; i <= 60; i++) {
			userName = "wegov" + i;
			userFullName = "Wegov User " + i;
			userPassword = "we" + i + "gov";
			System.out.println(userName + " : " + userPassword);
			coordinator.createPolicyMaker(userFullName, userRole, "European Parliament", userName, userPassword);
		}
		*/
    }

    public static void setupExternalUsers(Coordinator coordinator) throws Exception {
    	Role userRole = coordinator.getDefaultUserRole();

    	coordinator.createPolicyMaker("Gerold Reichenbach", userRole, "Deutscher Bundestag","wegov1","0af58e2c047f4131a74b7fd10827c4fde1e7f8b0", false);
    	coordinator.createPolicyMaker("Wegov User 2", userRole, "European Parliament","wegov2","dd7f606130b75559c5ae0c697234b12d8643502b", false);
    	coordinator.createPolicyMaker("Wegov User 3", userRole, "European Parliament","wegov3","17c4fd708a7901afae5b7a9b1d9209ffdb74c172", false);
    	coordinator.createPolicyMaker("Wegov User 4", userRole, "European Parliament","wegov4","7ea8afc53e63f18842cbb91d6081971de465a355", false);
    	coordinator.createPolicyMaker("Wegov User 5", userRole, "European Parliament","wegov5","321399ea12f8e8ba56da73acb55a4d5178faede5", false);
    	coordinator.createPolicyMaker("Wegov User 6", userRole, "European Parliament","wegov6","e07de1ddeb20bf5369c655bd48e64d40b5f415bd", false);
    	coordinator.createPolicyMaker("Wegov User 7", userRole, "European Parliament","wegov7","8dbe155af3feda0a116a56479341ac840a481fa4", false);
    	coordinator.createPolicyMaker("Wegov User 8", userRole, "European Parliament","wegov8","db2fb15ccdd6bd0fac52abf66eafb908f254252f", false);
    	coordinator.createPolicyMaker("Wegov User 9", userRole, "European Parliament","wegov9","c2f504c72275e2c2e2af7eef308b4e4197b7884e", false);
    	coordinator.createPolicyMaker("Wegov User 10", userRole, "European Parliament","wegov10","bc6d293cf3d5aba60b37835bc6222c2e7cd467b4", false);
    	coordinator.createPolicyMaker("Wegov User 11", userRole, "European Parliament","wegov11","a22fe45e8d5c591ae215c19d5abb7c41b3bc0ebb", false);
    	coordinator.createPolicyMaker("Wegov User 12", userRole, "European Parliament","wegov12","9729c76b56c2fc991202828c74244b84d71b3420", false);
    	coordinator.createPolicyMaker("Wegov User 13", userRole, "European Parliament","wegov13","c0dea3d8860b0eb80d998fb007f516ec4457f43e", false);
    	coordinator.createPolicyMaker("Wegov User 14", userRole, "European Parliament","wegov14","f140bf6e9f36e31b187f8761bdf82636237e2b00", false);
    	coordinator.createPolicyMaker("Stadt Bielefeld", userRole, "Stadt Bielefeld","wegov15","fb0c4317d9ad4e466c53925dee2eefd97eea2b79", false);
    	coordinator.createPolicyMaker("Wegov User 16", userRole, "European Parliament","wegov16","089b446c9c553b67d8ff6a644df7d346b6a96e84", false);
    	coordinator.createPolicyMaker("Wegov User 17", userRole, "European Parliament","wegov17","1b168c8fe5d2a3f52e8685016ef294d5c0be84c3", false);
    	coordinator.createPolicyMaker("Wegov User 18", userRole, "European Parliament","wegov18","c52b9ca1b1b3cdf133aa05a01d6fa08f30a9e76a", false);
    	coordinator.createPolicyMaker("Wegov User 19", userRole, "European Parliament","wegov19","faa4a6f74581cc090971fb39b0abc627fc3afc49", false);
    	coordinator.createPolicyMaker("Wegov User 20", userRole, "European Parliament","wegov20","d5999af302adea73e0bf2b6314a3d7beffc156a9", false);
    	coordinator.createPolicyMaker("Wegov User 21", userRole, "European Parliament","wegov21","e1d2a4ed57ed77f7e2d7a80c1465daea33cef765", false);
    	coordinator.createPolicyMaker("Stadt D\u00fcsseldorf", userRole, "Stadt D\u00fcsseldorf","wegov22","3748bfe6de9861cfc5aad9e2f55825fb1c709c74", false);
    	coordinator.createPolicyMaker("Stadt K\u00f6ln", userRole, "Stadt K\u00f6ln","wegov23","4314b74ad49d9a427e7de5df9f88aedca070f3a1", false);
    	coordinator.createPolicyMaker("Wegov User 24", userRole, "European Parliament","wegov24","c3ecb3923d562332304382334a5240e2c139f268", false);
    	coordinator.createPolicyMaker("Deutschland Radio", userRole, "European Parliament","wegov25","3ff1499ef0efa9085e675a7f09e7747cb90b2ae6", false);
    	coordinator.createPolicyMaker("Wegov User 26", userRole, "European Parliament","wegov26","723c2951655d9e9e246a46a3a01a683e3f7076c2", false);
    	coordinator.createPolicyMaker("Wegov User 27", userRole, "European Parliament","wegov27","22b6072b0bc84d4db8a61fbca741a08cc5e8809c", false);
    	coordinator.createPolicyMaker("Manuel H\u00f6ferlin", userRole, "Bundestag","wegov28","c70b9ad07e12f5149b85a6a20c9cfb8276028834", false);
    	coordinator.createPolicyMaker("Halina Wawzyniak", userRole, "Bundestag","wegov29","e457dec4f3eea9b3430e1ab532d42d42546f8239", false);
    	coordinator.createPolicyMaker("Markus Tressel", userRole, "Bundestag","wegov30","1d06c90ea4a8148110e89399e3d5aeedacd6644a", false);
    	coordinator.createPolicyMaker("Matthi Bolte", userRole, "Landtag NRW","wegov31","11c0a70727515595f15618dfa2feee74b9b0a0dc", false);
    	coordinator.createPolicyMaker("Andrea Verpoorten", userRole, "Landtag NRW","wegov32","ad92f4f2a43a32ae5a08a949a7d90063348c3246", false);
    	coordinator.createPolicyMaker("Josef Rickfelder", userRole, "Landtag NRW","wegov33","43f28fcef6fb098db156878f4faec11a885e551f", false);
    	coordinator.createPolicyMaker("Stefan Wiedon", userRole, "Landtag NRW","wegov34","c089b794ec9e4d048c4026021094c3d12eb3bbdb", false);
    	coordinator.createPolicyMaker("Dr. Martin Schoser", userRole, "Landtag NRW","wegov35","00633acc1a14dceae891b0a6fa280e6637c97912", false);
    	coordinator.createPolicyMaker("Jens Kamieth", userRole, "Landtag NRW","wegov36","8957426fbaf34cb3ed1a30401c13a2405ecc5631", false);
    	coordinator.createPolicyMaker("Stefan Engstfeld ", userRole, "Landtag NRW","wegov37","ae95e9d65296d64861af693e83715dd0d0c6c8bf", false);
    	coordinator.createPolicyMaker("Oliver Keymis ", userRole, "Landtag NRW","wegov38","fbec1332ad9ec19511850b94fbed33f9e82c8f1c", false);
    	coordinator.createPolicyMaker("B\u00e4rbel Beuermann", userRole, "Landtag NRW","wegov39","06d48688feb087d632a37d2e99e79293cde2b181", false);
    	coordinator.createPolicyMaker("Matthias Miersch ", userRole, "Bundestag","wegov40","f6719f86f619c4c35ed1bb0a19d0dfefe537261f", false);
    	coordinator.createPolicyMaker("Wegov User 41", userRole, "European Parliament","wegov41","dc1205f49fef961d8a5d930f9b4a4e3dcdf08c7b", false);
    	coordinator.createPolicyMaker("Wegov User 42", userRole, "European Parliament","wegov42","2ebe67738beb9ec16b18c34c46d50bef96206a5e", false);
    	coordinator.createPolicyMaker("Wegov User 43", userRole, "European Parliament","wegov43","0500af784fe275fac61a365793b808faf9251689", false);
    	coordinator.createPolicyMaker("Wegov User 44", userRole, "European Parliament","wegov44","6193a33adbceeb145fc64b80be5f89f13796a46e", false);
    	coordinator.createPolicyMaker("Advisory Board", userRole, "Advisory Board","wegov45","7673787b0d4ef369fe685fab79e0b876ffedda4a", false);
    	coordinator.createPolicyMaker("Wegov User 46", userRole, "European Parliament","wegov46","c584a058e02d0801d2697925cbfcc35574f71286", false);
    	coordinator.createPolicyMaker("Wegov User 47", userRole, "European Parliament","wegov47","479e57ee6d1389297344e8d6c3f80cb99211d925", false);
    	coordinator.createPolicyMaker("Wegov User 48", userRole, "European Parliament","wegov48","fc0229b8e9bb51cfb66bd3a76344e727c8f32683", false);
    	coordinator.createPolicyMaker("Wegov User 49", userRole, "European Parliament","wegov49","228081926890d6210b5337bfb9c1f55fade46298", false);
    	coordinator.createPolicyMaker("Wegov User 50", userRole, "European Parliament","wegov50","3e6f910dfaad12aa53a57f6aaea36047b25fca7a", false);
    	coordinator.createPolicyMaker("Wegov User 51", userRole, "European Parliament","wegov51","66fcab50e944b38553b1bf1e2b71e2afae121a45", false);
    	coordinator.createPolicyMaker("Wegov User 52", userRole, "European Parliament","wegov52","f7d152816d0cfe5e0ec0377d900c692dbbcdba3e", false);
    	coordinator.createPolicyMaker("Wegov User 53", userRole, "European Parliament","wegov53","3d883312ad2049c4afdedac0042c2a817b10d9b2", false);
    	coordinator.createPolicyMaker("Wegov User 54", userRole, "European Parliament","wegov54","ef76c39636a97f9e47e6ae978c964d0b5e842fca", false);
    	coordinator.createPolicyMaker("Wegov User 55", userRole, "European Parliament","wegov55","a990685cc1506f119701faec7f88e20d24c43ab0", false);
    	coordinator.createPolicyMaker("Cambridgeshire County Council", userRole, "Cambridgeshire County Council","wegov56","63b36e59d826736ccf8848daa4cbc3be3a2e36da", false);
    	coordinator.createPolicyMaker("Wegov User 57", userRole, "European Parliament","wegov57","19ce6a6ab8d53ef5c312764690304b829755f459", false);
    	coordinator.createPolicyMaker("Wegov User 58", userRole, "European Parliament","wegov58","2709933dec4e41bbae0bbee6963eaae19a3cfe6a", false);
    	coordinator.createPolicyMaker("Wegov User 59", userRole, "European Parliament","wegov59","e2605a7d6593380ca2ad7caa39ee5b0a19947ec4", false);
    	coordinator.createPolicyMaker("Wegov User 60", userRole, "European Parliament","wegov60","47a89c3d816e344787464ce057b1dec48247ed21", false);
    	coordinator.createPolicyMaker("Keri Facer", userRole, "University of Bristol","keri","ea0fc2616778a66db40ce1f7f8ef7a024e7e3252", false);
    	coordinator.createPolicyMaker("Robert Link", userRole, "University of Graz","rlink","75c585ac36d13c9727c41d92f527897fdd06cb7a", false);
    	coordinator.createPolicyMaker("Robert Woitsch", userRole, "BOC","rwoitsch","e64a4394dfa5593034f13efeb99a3963abbf3b73", false);
    	coordinator.createPolicyMaker("Peter Winstanley", userRole, "Advisory board","pwinstanley","41332207fcfbf02d52e3d352c9ec7e822343d656", false);
    	coordinator.createPolicyMaker("Athanasios Kountzeris", userRole, "Advisory board","akountzeris","8e739ecc3518610590924ae65467720bb547b3a3", false);
    	coordinator.createPolicyMaker("Michael Dauderstadt", userRole, "Advisory board","mdauderstadt","55a5e0f81615906fb6b4aa6da9fd8d7f704fcef7", false);
    	coordinator.createPolicyMaker("Jan Linhart", userRole, "Advisory board","jlinhart","d3bb8fdbab3039becf956d478c24b07d0a9f9166", false);
    	coordinator.createPolicyMaker("Kostas Rossoglou", userRole, "Advisory board","krossoglou","5b02115ebad16aa176d5e964f58ccadc0cc93a82", false);
    	coordinator.createPolicyMaker("Test User", userRole, "WeGov Project","wegovtest","a8a4cd5ed4586dd0f3e8897c233495f07100881f", false);
    	coordinator.createPolicyMaker("Bassem Nasser", userRole, "IT Innovation","bmn","1fe7a3d758b4ef910e30adab75452dbef7367792", false);
    	coordinator.createPolicyMaker("Peter Sonntagbauer", userRole, "Cellent (FUPOL)","ps","da15fd0508e05c5fd1e6cba3dce6aead6c52f158", false);
    	coordinator.createPolicyMaker("Nikolaus Rumm", userRole, "Cellent (FUPOL)","nr","bfd22b947eddc7a0cfc6740f8a6521ba2df77685", false);
    	coordinator.createPolicyMaker("Wegov PO", userRole, "EU", "wegov-po","3be803573bf56690e1c7b7b457dc45a08047eec5", false);
    }

    public static void setupGesisUsers(Coordinator coordinator) throws Exception {
    	Role userRole = coordinator.getDefaultUserRole();

        coordinator.createPolicyMaker("GESIS-1", userRole, "GESIS", "GESIS-1", "plokij099");
        coordinator.createPolicyMaker("GESIS-2", userRole, "GESIS", "GESIS-2", "oonojir0");
        coordinator.createPolicyMaker("GESIS-3", userRole, "GESIS", "GESIS-3", "8u7y6t5rf");
        coordinator.createPolicyMaker("GESIS-4", userRole, "GESIS", "GESIS-4", "8hg7ujh75");
        coordinator.createPolicyMaker("GESIS-5", userRole, "GESIS", "GESIS-5", "jiu7ytgfg");
        coordinator.createPolicyMaker("GESIS-6", userRole, "GESIS", "GESIS-6", "95it7ehfi87");
        coordinator.createPolicyMaker("GESIS-7", userRole, "GESIS", "GESIS-7", "hdyre74ur");
        coordinator.createPolicyMaker("GESIS-8", userRole, "GESIS", "GESIS-8", "kgiti58gj");
        coordinator.createPolicyMaker("GESIS-9", userRole, "GESIS", "GESIS-9", "khiyjfu8jg");
        coordinator.createPolicyMaker("K\u00f6rper", userRole, "Bundestag", "K\u00f6rper", "korp098");
        coordinator.createPolicyMaker("Klingbeil", userRole, "Bundestag", "Klingbeil", "kling7654");
        coordinator.createPolicyMaker("Schnieder", userRole, "Bundestag", "Schnieder", "schn8765");
        coordinator.createPolicyMaker("Grund", userRole, "Bundestag", "Grund", "grun4637r");
        coordinator.createPolicyMaker("Wawzyniak", userRole, "Bundestag", "Wawzyniak", "wawught");
        coordinator.createPolicyMaker("Vogt", userRole, "Bundestag", "Vogt", "vogt7584u");
        coordinator.createPolicyMaker("Reichenbach", userRole, "Bundestag", "Reichenbach", "reichen64739");
        coordinator.createPolicyMaker("Engstfeld", userRole, "Landtag NRW", "Engstfeld", "engst876yt");
        coordinator.createPolicyMaker("Bolte", userRole, "Landtag NRW", "Bolte", "bolte7u473y");
        coordinator.createPolicyMaker("Rickfelder", userRole, "Landtag NRW", "Rickfelder", "rick986uhyt");
        coordinator.createPolicyMaker("Beuermann", userRole, "Landtag NRW", "Beuermann", "beu74yegy7");
        coordinator.createPolicyMaker("Baden-W\u00fcrttemberg", userRole, "Land Baden-W\u00fcrttemberg", "Baden-W\u00fcrttemberg", "baden84736");
        coordinator.createPolicyMaker("Bielefeld", userRole, "Stadt Bielefeld", "Bielefeld", "biele7363g");
        coordinator.createPolicyMaker("Hamburg", userRole, "Stadt Hamburg", "Hamburg", "ham73yfr6");
        coordinator.createPolicyMaker("K\u00f6ln", userRole, "Stadt K\u00f6ln", "K\u00f6ln", "kol8373he7f");
        coordinator.createPolicyMaker("Experiment-1", userRole, "GESIS", "Experiment-1", "exp1mvnfh");
        coordinator.createPolicyMaker("Experiment-2", userRole, "GESIS", "Experiment-2", "exp2jfureh");
        coordinator.createPolicyMaker("Experiment-3", userRole, "GESIS", "Experiment-3", "exp3igjr863");
        coordinator.createPolicyMaker("Experiment-4", userRole, "GESIS", "Experiment-4", "exp4jfuru7");
        coordinator.createPolicyMaker("Experiment-5", userRole, "GESIS", "Experiment-5", "exp5irejfu8");    
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

 
    public static void updateConfigurationAddParameter (
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
