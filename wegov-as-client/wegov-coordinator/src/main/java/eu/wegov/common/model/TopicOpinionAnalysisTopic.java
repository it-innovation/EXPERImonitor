package eu.wegov.common.model;

import java.util.ArrayList;
import java.util.Arrays;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TopicOpinionAnalysisTopic {
	private String id;
	private String keywords;
	private JSONTwitterUserDetails[] keyUsers;
	private JSONTwitterPostDetails[] keyPosts;
	private String numPosts;

  // need keyword & probability pairs in an array

  // need controversy histograms - binned values going from -10 to +10
  // in sergej's version each bin has an array of document IDs, so we can
  // get the actual post if we want it. Initially, a count of documents in each
  // bin will be a good start, so just go with the array length of each bin.

	private static String[] metricNames = {"valence", "controversy"};

	private String valence;
	private String controversy;

  // contains a more comprehensive set of documents for this topic
  // the number is determined by the relevance score lower limit
  // all posts greater than the limit are stored in this
  private TopicDocument [] topicDocuments;
  private double relevanceLowerLimit;
  private int numTopicDocuments;

	public TopicOpinionAnalysisTopic(String id, String keywords,
			JSONTwitterUserDetails[] keyUsers, JSONTwitterPostDetails[] keyPosts, String numPosts) {
		super();
		this.id = id;
		this.keywords = keywords;
		this.keyUsers = keyUsers;
		this.keyPosts = keyPosts;
		this.numPosts = numPosts;
	}

  // version with topic document array
	public TopicOpinionAnalysisTopic(String id, String keywords,
			JSONTwitterUserDetails[] keyUsers, JSONTwitterPostDetails[] keyPosts,
      ArrayList <TopicDocument> topicPosts, double relevanceLowerLimit) {
		super();
		this.id = id;
		this.keywords = keywords;
		this.keyUsers = keyUsers;
		this.keyPosts = keyPosts;
    this.numPosts = String.valueOf(topicPosts.size());

    

    // fuller set of documents for this topic
    this.topicDocuments = new TopicDocument [topicPosts.size()];
    topicPosts.toArray(this.topicDocuments);

    this.relevanceLowerLimit = relevanceLowerLimit;

    this.numTopicDocuments = topicPosts.size();

	}


  // constructor that reconstructs from database stored version
  public TopicOpinionAnalysisTopic(JSONObject jsonTopic) {

    this.controversy = jsonTopic.getString("controversy");

    this.id = jsonTopic.getString("id");

    JSONArray posts  = jsonTopic.getJSONArray("keyPosts");
    JSONTwitterPostDetails[] keyPosts = new JSONTwitterPostDetails[posts.size()];
    for (int i=0; i< posts.size(); i++) {
      keyPosts[i] = new JSONTwitterPostDetails();
      keyPosts[i].recoverValuesFromJsonObject(posts.getJSONObject(i));
    }
    this.keyPosts = keyPosts;

    JSONArray users  = jsonTopic.getJSONArray("keyUsers");
    JSONTwitterUserDetails[] keyUsers = new JSONTwitterUserDetails[users.size()];
    for (int i=0; i< users.size(); i++) {
      keyUsers[i] = new JSONTwitterUserDetails();
      keyUsers[i].recoverValuesFromJsonObject(users.getJSONObject(i));
    }
    this.keyUsers = keyUsers;

    this.keywords = jsonTopic.getString("keywords");

    this.numPosts = jsonTopic.getString("numPosts");

    this.valence = jsonTopic.getString("valence");

    // optional set of topic documents
    if (jsonTopic.containsKey("topicDocuments")) {
      JSONArray docsJson  = jsonTopic.getJSONArray("topicDocuments");
      this.topicDocuments = new TopicDocument[docsJson.size()];
      for (int i=0; i< docsJson.size(); i++) {
        this.topicDocuments[i] = new TopicDocument(docsJson.getJSONObject(i));
      }
    }
    // optional set of topic documents
    if (jsonTopic.containsKey("numTopicDocuments")) {
      this.numTopicDocuments = jsonTopic.getInt("numTopicDocuments");
    }
  
    // optional lower score limit that determines topicDocuments
    if (jsonTopic.containsKey("relevanceLowerLimit")) {
      this.relevanceLowerLimit = jsonTopic.getDouble("relevanceLowerLimit");
    }



  }


	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public JSONTwitterUserDetails[] getKeyUsers() {
		return keyUsers;
	}

	public void setKeyUsers(JSONTwitterUserDetails[] keyUsers) {
		this.keyUsers = keyUsers;
	}

	public JSONTwitterPostDetails[] getKeyPosts() {
		return keyPosts;
	}

	public void setKeyPosts(JSONTwitterPostDetails[] keyPosts) {
		this.keyPosts = keyPosts;
	}

	public void setValence(String valence) {
		this.valence = valence;
	}

	public String getValence() {
		return valence;
	}

	public void setControversy(String controversy) {
		this.controversy = controversy;
	}

	public String getControversy() {
		return controversy;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setNumPosts(String numPosts) {
		this.numPosts = numPosts;
	}

	public String getNumPosts() {
		return numPosts;
	}

  public static String[] getMetricNames() {
    return metricNames;
  }

  public static void setMetricNames(String[] metricNames) {
    TopicOpinionAnalysisTopic.metricNames = metricNames;
  }

  public TopicDocument[] getTopicDocuments() {
    return topicDocuments;
  }

  public void setTopicDocuments(TopicDocument[] topicDocuments) {
    this.topicDocuments = topicDocuments;
  }

  public double getRelevanceLowerLimit() {
    return relevanceLowerLimit;
  }

  public void setRelevanceLowerLimit(double relevanceLowerLimit) {
    this.relevanceLowerLimit = relevanceLowerLimit;
  }




  @Override
  public String toString() {
    return "TopicOpinionAnalysisTopic{" + "id=" + id + ", keywords=" + keywords + ", keyUsers=" + keyUsers + ", keyPosts=" + keyPosts + ", numPosts=" + numPosts + ", valence=" + valence + ", controversy=" + controversy + '}';
  }


}
