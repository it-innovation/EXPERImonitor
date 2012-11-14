package eu.wegov.common.model;

import java.util.ArrayList;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class BehaviourAnalysisResult {
	private RoleDistributionPoint[] roleDistributionPoints;
	private KmiDiscussionActivityPoint[] discussionActivityPoints;
	private KmiUser[] buzzUsers;
	private KmiPost[] buzzPosts;
  
  private BehaviourAnalysisUsersForRole broadcasters;
  private BehaviourAnalysisUsersForRole dailyUsers;
  private BehaviourAnalysisUsersForRole informationSeekers;
  private BehaviourAnalysisUsersForRole informationSources;
  private BehaviourAnalysisUsersForRole rarePosters;



	public BehaviourAnalysisResult() {
		super();
	}


  public BehaviourAnalysisResult(JSONObject jsonResult) {


    System.out.println();

    System.out.println("Reconstructing behaviour analysis from results");

    JSONArray roleDistributionPointsJSON  = jsonResult.getJSONArray("roleDistributionPoints");
    System.out.println("JSON array of roleDistributionPoints = " + roleDistributionPointsJSON.toString());
    RoleDistributionPoint[] roleDistributionPoints = new RoleDistributionPoint[roleDistributionPointsJSON.size()];
    for (int i=0; i< roleDistributionPointsJSON.size(); i++) {
      roleDistributionPoints[i] = new RoleDistributionPoint(roleDistributionPointsJSON.getJSONObject(i));
    }
    this.roleDistributionPoints = roleDistributionPoints;

    JSONArray discussionActivityPointsJSON  = jsonResult.getJSONArray("discussionActivityPoints");
    System.out.println("JSON array of discussionActivityPoints = " + discussionActivityPointsJSON.toString());
    KmiDiscussionActivityPoint[] discussionActivityPoints = new KmiDiscussionActivityPoint[discussionActivityPointsJSON.size()];
    for (int i=0; i< discussionActivityPointsJSON.size(); i++) {
      discussionActivityPoints[i] = new KmiDiscussionActivityPoint(discussionActivityPointsJSON.getJSONObject(i));
    }
    this.discussionActivityPoints = discussionActivityPoints;

    JSONArray buzzUsersJSON  = jsonResult.getJSONArray("buzzUsers");
    System.out.println("JSON array of buzzUsers = " + buzzUsersJSON.toString());


    // sometime we get null values in buzz users and posts so we need to get the size right
/*
    int buzzUsersSize = 0;
    for (int i=0; i< buzzUsersJSON.size(); i++) {
      try (
              buzzUsersJSON.getJSONObject(i) != null) {
        buzzUsersSize = buzzUsersSize + 1;
      }
    }

    System.out.println("buzz users array size = " + buzzUsersSize);
    */


    //KmiUser[] buzzUsers = new KmiUser[buzzUsersSize];

    ArrayList<KmiUser> buzzUsersTemp = new ArrayList<KmiUser>();
    for (int i=0; i< buzzUsersJSON.size(); i++) {
      try {
        buzzUsersTemp.add (new KmiUser(buzzUsersJSON.getJSONObject(i)));
      }
      catch (net.sf.json.JSONException ex) {
        System.out.println(ex.toString());
      }
    }
    //KmiUser[] buzzUsers = new KmiUser[buzzUsersTemp.size()];

    //KmiUser[] buzzUsers = (KmiUser[]) buzzUsersTemp.toArray();

    KmiUser[] buzzUsers = buzzUsersTemp.toArray(new KmiUser[buzzUsersTemp.size()]);

    //buzzUsers

    this.buzzUsers = buzzUsers;




    JSONArray buzzPostsJSON  = jsonResult.getJSONArray("buzzPosts");
    System.out.println("JSON array of buzzPosts = " + buzzPostsJSON.toString());

    ArrayList<KmiPost> buzzPostsTemp = new ArrayList<KmiPost>();
    for (int i=0; i< buzzPostsJSON.size(); i++) {
      try {
        buzzPostsTemp.add(new KmiPost(buzzPostsJSON.getJSONObject(i)));
      }
      catch (net.sf.json.JSONException ex) {
        System.out.println(ex.toString());
      }
    }
    //KmiPost[] buzzPosts = new KmiPost[buzzPostsTemp.size()];

    //KmiPost[] buzzPosts = (KmiPost[]) buzzPostsTemp.toArray();

    KmiPost[] buzzPosts = buzzPostsTemp.toArray(new KmiPost[buzzPostsTemp.size()]);

/*
    int buzzPostsSize = 0;
    for (int i=0; i< buzzPostsJSON.size(); i++) {
      if (buzzPostsJSON.getJSONObject(i) != null) {
        buzzPostsSize = buzzPostsSize + 1;
      }
    }

    System.out.println("buzz posts array size = " + buzzPostsSize);

    KmiPost[] buzzPosts = new KmiPost[buzzPostsSize];
    for (int i=0; i< buzzPostsSize; i++) {
      if (buzzPostsJSON.getJSONObject(i) != null) {
        buzzPosts[i] = new KmiPost(buzzPostsJSON.getJSONObject(i));
      }
    }
    *
    */


    this.buzzPosts = buzzPosts;

    try {
      JSONObject broadcasters = jsonResult.getJSONObject("broadcasters");
      this.broadcasters = new BehaviourAnalysisUsersForRole(broadcasters);
    }
    catch (net.sf.json.JSONException ex) {
      System.out.println(ex.toString());
    }
    
    try {
      JSONObject dailyUsers = jsonResult.getJSONObject("dailyUsers");
      this.dailyUsers = new BehaviourAnalysisUsersForRole(dailyUsers);
    }
    catch (net.sf.json.JSONException ex) {
      System.out.println(ex.toString());
    }
    


    try {
      JSONObject informationSeekers = jsonResult.getJSONObject("informationSeekers");
      this.informationSeekers = new BehaviourAnalysisUsersForRole(informationSeekers);
    }
    catch (net.sf.json.JSONException ex) {
      System.out.println(ex.toString());
    }


    try {
      JSONObject informationSources = jsonResult.getJSONObject("informationSources");
      this.informationSources = new BehaviourAnalysisUsersForRole(informationSources);
    }
    catch (net.sf.json.JSONException ex) {
      System.out.println(ex.toString());
    }

    try {
      JSONObject rarePosters = jsonResult.getJSONObject("rarePosters");
      this.rarePosters = new BehaviourAnalysisUsersForRole(rarePosters);
    }
    catch (net.sf.json.JSONException ex) {
      System.out.println(ex.toString());
    }
    

  }

	public KmiPost[] getBuzzPosts() {
		return buzzPosts;
	}

	public void setBuzzPosts(KmiPost[] buzzPosts) {
		this.buzzPosts = buzzPosts;
	}

	public RoleDistributionPoint[] getRoleDistributionPoints() {
		return roleDistributionPoints;
	}

	public void setRoleDistributionPoints(
			RoleDistributionPoint[] roleDistributionPoints) {
		this.roleDistributionPoints = roleDistributionPoints;
	}

	public KmiDiscussionActivityPoint[] getDiscussionActivityPoints() {
		return discussionActivityPoints;
	}

	public void setDiscussionActivityPoints(
			KmiDiscussionActivityPoint[] discussionActivityPoints) {
		this.discussionActivityPoints = discussionActivityPoints;
	}

	public KmiUser[] getBuzzUsers() {
		return buzzUsers;
	}

	public void setBuzzUsers(KmiUser[] buzzUsers) {
		this.buzzUsers = buzzUsers;
	}

  public BehaviourAnalysisUsersForRole getBroadcasters() {
    return broadcasters;
  }

  public void setBroadcasters(BehaviourAnalysisUsersForRole broadcasters) {
    this.broadcasters = broadcasters;
  }

  public BehaviourAnalysisUsersForRole getDailyUsers() {
    return dailyUsers;
  }

  public void setDailyUsers(BehaviourAnalysisUsersForRole dailyUsers) {
    this.dailyUsers = dailyUsers;
  }

  public BehaviourAnalysisUsersForRole getInformationSeekers() {
    return informationSeekers;
  }

  public void setInformationSeekers(BehaviourAnalysisUsersForRole informationSeekers) {
    this.informationSeekers = informationSeekers;
  }

  public BehaviourAnalysisUsersForRole getInformationSources() {
    return informationSources;
  }

  public void setInformationSources(BehaviourAnalysisUsersForRole informationSources) {
    this.informationSources = informationSources;
  }

  public BehaviourAnalysisUsersForRole getRarePosters() {
    return rarePosters;
  }

  public void setRarePosters(BehaviourAnalysisUsersForRole rarePosters) {
    this.rarePosters = rarePosters;
  }

  
  
}
