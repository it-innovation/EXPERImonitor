package eu.wegov.common.model;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class BehaviourAnalysisUsersForRole {
	private KmiUser[] users;

	public KmiUser[] getUsers() {
		return users;
	}

	public void setUsers(KmiUser[] users) {
		this.users = users;
	}

	public BehaviourAnalysisUsersForRole() {
		super();
	}

	public BehaviourAnalysisUsersForRole(KmiUser[] users) {
		super();
		this.users = users;
	}
  
	public BehaviourAnalysisUsersForRole(JSONObject jsonUsersForRole) {
  
    JSONArray jsonUsers  = jsonUsersForRole.getJSONArray("users");
    KmiUser[] users = new KmiUser[jsonUsers.size()];
    for (int i=0; i< jsonUsers.size(); i++) {
      users[i] = new KmiUser(jsonUsers.getJSONObject(i));
     // keyPosts[i].recoverValuesFromJsonObject(posts.getJSONObject(i));
    }
    this.users = users;
	}

}
