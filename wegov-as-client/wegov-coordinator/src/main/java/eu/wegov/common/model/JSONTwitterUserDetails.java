package eu.wegov.common.model;

import net.sf.json.JSONObject;

public class JSONTwitterUserDetails {
	private String id  = "not available";
	private String screenName = "not available";
	private String fullName = "not available";
	private String profileImageUrl = "not available";
	private String location = "not available";
	private String createdAt = "not available";
	private String favouritesCount = "not available";
	private String url = "not available";
	private String followersCount = "not available";
	private String description = "not available";
	private String timeZone = "not available";
	private String friendsCount = "not available";
	private String statusesCount = "not available";
	private String userUrl = "not available";

	public JSONTwitterUserDetails(String id, String screenName, String fullName, String profileImageUrl) {
		super();
		this.id = id;
		this.screenName = screenName;
		this.fullName = fullName;
		this.profileImageUrl = profileImageUrl;
	}


	public JSONTwitterUserDetails(JSONObject postJSON) {
		super();
    
		//this.id = postJSON.getString("from_user_id");
    if (postJSON.containsKey("from_user_id")) {
      this.id = postJSON.getString("from_user_id");
    }
    else if (postJSON.containsKey("id")) {
      this.id = postJSON.getString("id");
    }
    // else leave as default
		
    //this.screenName = postJSON.getString("from_user");
    if (postJSON.containsKey("from_user")) {
      this.screenName = postJSON.getString("from_user");
    }
    else if (postJSON.containsKey("screenName")) {
      this.screenName = postJSON.getString("screenName");
    }
    // else leave as default
    
    
    //this.fullName = postJSON.getString("from_user_name");
    if (postJSON.containsKey("from_user_name")) {
      this.fullName = postJSON.getString("from_user_name");
    }
    else if (postJSON.containsKey("fullName")) {
      this.fullName = postJSON.getString("fullName");
    }
    // else leave as default

    //this.profileImageUrl = postJSON.getString("profile_image_url_https");
    if (postJSON.containsKey("profile_image_url_https")) {
      this.profileImageUrl = postJSON.getString("profile_image_url_https");
    }
    else if (postJSON.containsKey("profileImageUrl")) {
      this.profileImageUrl = postJSON.getString("profileImageUrl");
    }
    // else leave as default
    
    if (postJSON.containsKey("userUrl")) {
      this.userUrl = postJSON.getString("userUrl");
    }
    // else leave as default
    
	}

 	public JSONTwitterUserDetails() {
   // void constructor for use with recover values from JSON function below
  }

	public void recoverValuesFromJsonObject(JSONObject userJSON) {
    /*
		super();
		this.id = userJSON.getString("from_user_id");
		this.screenName = postJSON.getString("from_user");
		this.fullName = postJSON.getString("from_user_name");
		this.profileImageUrl = postJSON.getString("profile_image_url_https");
   */

    this.createdAt = userJSON.getString("createdAt");
    this.description = userJSON.getString("description");
    this.favouritesCount = userJSON.getString("favouritesCount");
    this.followersCount = userJSON.getString("followersCount");
    this.friendsCount = userJSON.getString("friendsCount");
    this.fullName = userJSON.getString("fullName");
    this.id = userJSON.getString("id");
    this.location = userJSON.getString("location");
    this.profileImageUrl = userJSON.getString("profileImageUrl");
    this.screenName = userJSON.getString("screenName");
    this.statusesCount = userJSON.getString("statusesCount");
    this.timeZone = userJSON.getString("timeZone");
    this.url = userJSON.getString("url");
	}

 
 	public JSONTwitterUserDetails(String voidUser) {

    // object with "none" in all fields
    if (voidUser.equals("none")) {
      this.createdAt = "none";
      this.description = "none";
      this.favouritesCount = "none";
      this.followersCount = "none";
      this.friendsCount = "none";
      this.fullName = "none";
      this.id = "none";
      this.location = "none";
      this.profileImageUrl = "none";
      this.screenName = "none";
      this.statusesCount = "none";
      this.timeZone = "none";
    }
  }
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getProfileImageUrl() {
		return profileImageUrl;
	}

	public void setProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getFavouritesCount() {
		return favouritesCount;
	}

	public void setFavouritesCount(String favouritesCount) {
		this.favouritesCount = favouritesCount;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFollowersCount() {
		return followersCount;
	}

	public void setFollowersCount(String followersCount) {
		this.followersCount = followersCount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getFriendsCount() {
		return friendsCount;
	}

	public void setFriendsCount(String friendsCount) {
		this.friendsCount = friendsCount;
	}

	public String getStatusesCount() {
		return statusesCount;
	}

	public void setStatusesCount(String statusesCount) {
		this.statusesCount = statusesCount;
	}

  public String getUserUrl() {
    return userUrl;
  }

  public void setUserUrl(String userUrl) {
    this.userUrl = userUrl;
  }

  
  
  
  @Override
  public String toString() {
    return "JSONTwitterUserDetails{" + "id=" + id + ", screenName=" + screenName + ", fullName=" + fullName + ", profileImageUrl=" + profileImageUrl + ", location=" + location + ", createdAt=" + createdAt + ", favouritesCount=" + favouritesCount + ", url=" + url + ", followersCount=" + followersCount + ", description=" + description + ", timeZone=" + timeZone + ", friendsCount=" + friendsCount + ", statusesCount=" + statusesCount + '}';
  }

}
