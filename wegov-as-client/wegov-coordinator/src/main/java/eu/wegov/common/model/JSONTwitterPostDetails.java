package eu.wegov.common.model;

import net.sf.json.JSONObject;

public class JSONTwitterPostDetails {
	private String createdAt = "not available";
	private String id = "not available";
	private String text = "not available";
	private String byUserId = "not available";
	private String byUserScreenName = "not available";
	private String byUserFullName = "not available";
	private String score = "not available";



	public JSONTwitterPostDetails(String createdAt, String id, String text,
			String byUserId, String byUserScreenName, String byUserFullName,
			String score) {
		super();
		this.createdAt = createdAt;
		this.id = id;
		this.text = text;
		this.byUserId = byUserId;
		this.byUserScreenName = byUserScreenName;
		this.byUserFullName = byUserFullName;
		this.score = score;
	}

	public JSONTwitterPostDetails(JSONObject postJSON) {
		super();
    
		//this.createdAt = postJSON.getString("created_at");
    if (postJSON.containsKey("created_at")) {
      this.createdAt = postJSON.getString("created_at");
    }
    else if (postJSON.containsKey("createdAt")) {
      this.createdAt = postJSON.getString("createdAt");
    }
  
		//this.id = postJSON.getString("id");
    if (postJSON.containsKey("id")) {
      this.id = postJSON.getString("id");
    }

    //this.text = postJSON.getString("text");
    if (postJSON.containsKey("text")) {
      this.text = postJSON.getString("text");
    }

    //this.byUserId = postJSON.getString("from_user_id");
    if (postJSON.containsKey("from_user_id")) {
      this.byUserId = postJSON.getString("from_user_id");
    }
    else if (postJSON.containsKey("byUserId")) {
      this.byUserId = postJSON.getString("byUserId");
    }

    //this.byUserScreenName = postJSON.getString("from_user");
    if (postJSON.containsKey("from_user")) {
      this.byUserScreenName = postJSON.getString("from_user");
    }
    else if (postJSON.containsKey("byUserScreenName")) {
      this.byUserScreenName = postJSON.getString("byUserScreenName");
    }
    
    
		//this.byUserFullName = postJSON.getString("from_user_name");
    if (postJSON.containsKey("from_user_name")) {
      this.byUserFullName = postJSON.getString("from_user_name");
    }
    else if (postJSON.containsKey("byUserFullName")) {
      this.byUserFullName = postJSON.getString("byUserFullName");
    }
    
		this.score = "";
	}

  	public JSONTwitterPostDetails() {
     // void constructor for use with recover values function
    }

	public JSONTwitterPostDetails(String voidPost) {
    
        // object with "none" in all fields
    if (voidPost.equals("none")) {

      this.createdAt = "none";
      this.id = "none";
      this.text = "none";
      this.byUserId = "none";
      this.byUserScreenName = "none";
      this.byUserFullName = "none";
      this.score = "none";
    }
    
	}
    
    
	public void recoverValuesFromJsonObject(JSONObject postJSON) {
		//super();
		this.byUserFullName = postJSON.getString("byUserFullName");
		this.byUserId = postJSON.getString("byUserId");
		this.byUserScreenName = postJSON.getString("byUserScreenName");
		this.createdAt = postJSON.getString("createdAt");
		this.id = postJSON.getString("id");
		this.score = postJSON.getString("score");;
		this.text = postJSON.getString("text");
	}


	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getByUserId() {
		return byUserId;
	}

	public void setByUserId(String byUserId) {
		this.byUserId = byUserId;
	}

	public String getByUserScreenName() {
		return byUserScreenName;
	}

	public void setByUserScreenName(String byUserScreenName) {
		this.byUserScreenName = byUserScreenName;
	}

	public String getByUserFullName() {
		return byUserFullName;
	}

	public void setByUserFullName(String byUserFullName) {
		this.byUserFullName = byUserFullName;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

  @Override
  public String toString() {
    return "JSONTwitterPostDetails{" + "createdAt=" + createdAt + ", id=" + id + ", text=" + text + ", byUserId=" + byUserId + ", byUserScreenName=" + byUserScreenName + ", byUserFullName=" + byUserFullName + ", score=" + score + '}';
  }

}
