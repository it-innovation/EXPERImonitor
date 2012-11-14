package eu.wegov.common.model;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TopicOpinionAnalysisResult {
	private int numTopics;
	private String numTopicsAsString;
	private TopicOpinionAnalysisTopic[] topics;
	private String[][] topicDistances;

	public TopicOpinionAnalysisResult() {
		super();
	}

	public TopicOpinionAnalysisResult(int numTopics,
			TopicOpinionAnalysisTopic[] topics) {
		super();
		this.numTopics = numTopics;
		this.topics = topics;
		if (numTopics == 1)
			this.numTopicsAsString = "1 topic";
		else
			this.numTopicsAsString = numTopics + " topics";
	}

  public TopicOpinionAnalysisResult(JSONObject jsonResult) throws Exception{

    this.setNumTopics(jsonResult.getInt("numTopics"));


    this.topicDistances = new String[this.numTopics][this.numTopics];

    JSONArray distancesOuterJSON = jsonResult.getJSONArray("topicDistances");
    if (distancesOuterJSON.size()!= this.numTopics) {
      throw new Exception ("Topic distance matrix is not the same size as the number of topics!");
    }
    for (int i=0; i< distancesOuterJSON.size(); i++) {
      JSONArray distancesInnerJSON = distancesOuterJSON.getJSONArray(i);
      if (distancesInnerJSON.size()!= this.numTopics) {
        throw new Exception ("Topic distance matrix is not the same size as the number of topics!");
      }
      for (int j=0; j < distancesInnerJSON.size(); j++) {
        this.topicDistances[i][j] = distancesInnerJSON.getString(j);
      }
    }


    JSONArray topicsJSON  = jsonResult.getJSONArray("topics");
    //System.out.println("JSON array of topics = " + topicsJSON.toString());
    TopicOpinionAnalysisTopic[] topics = new TopicOpinionAnalysisTopic[topicsJSON.size()];
    for (int i=0; i< topicsJSON.size(); i++) {
      topics[i] = new TopicOpinionAnalysisTopic(topicsJSON.getJSONObject(i));
    }

    this.topics = topics;

  }


	public int getNumTopics() {
		return numTopics;
	}

	public void setNumTopics(int numTopics) {
		if (numTopics == 1)
			this.setNumTopicsAsString("1 topic");
		else
			this.setNumTopicsAsString(numTopics + " topics");
		this.numTopics = numTopics;
	}

	public TopicOpinionAnalysisTopic[] getTopics() {
		return topics;
	}

	public void setTopics(TopicOpinionAnalysisTopic[] topics) {
		this.topics = topics;
	}

	public String getNumTopicsAsString() {
		return numTopicsAsString;
	}

	public void setNumTopicsAsString(String numTopicsAsString) {
		this.numTopicsAsString = numTopicsAsString;
	}

	public void setTopicDistances(String[][] distances) {
		this.topicDistances = distances;
	}

	public String[][] getTopicDistances() {
		return topicDistances;
	}

  @Override
  public String toString() {
    return "TopicOpinionAnalysisResult{" + "numTopics=" + numTopics + ", numTopicsAsString=" + numTopicsAsString + ", topics=" + topics + ", topicDistances=" + topicDistances + '}';
  }

}
