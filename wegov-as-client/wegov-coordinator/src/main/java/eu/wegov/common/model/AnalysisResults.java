/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.wegov.common.model;

import java.util.ArrayList;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;



/**
 *
 * @author sjt
 */

public class AnalysisResults {

  	String summary;
		AnalysisTopic [] result;
		String filePath;

    int [] forumIds; // for headsup

    int runId; //crude retrieval mechanism

  public AnalysisResults(String summary, AnalysisTopic[] result, String filePath) {
    this.summary = summary;
    this.result = result;
    this.filePath = filePath;
  }


  public AnalysisResults(String summary, ArrayList<AnalysisTopic> result, String filePath) {
    this.summary = summary;
    this.result = result.toArray(new AnalysisTopic[result.size()]);
    this.filePath = filePath;
  }

  public AnalysisResults(String summary, String filePath) {
    this.summary = summary;
    //this.result = result.toArray(new AnalysisTopic[result.size()]);
    this.filePath = filePath;
  }


  public AnalysisResults(JSONObject resultJson) {



    this.summary = resultJson.getString("summary");
    //this.result = result.toArray(new AnalysisTopic[result.size()]);

    JSONArray analysisTopicsJson = resultJson.getJSONArray("result");

    this.result = new AnalysisTopic [analysisTopicsJson.size()];
    for (int i=0; i < analysisTopicsJson.size(); i++) {
      this.result[i] = new AnalysisTopic(analysisTopicsJson.getJSONObject(i));
    }

    
    JSONArray forumIdsJson = resultJson.getJSONArray("forumIds");

    this.forumIds = new int [forumIdsJson.size()];
    for (int i=0; i < forumIdsJson.size(); i++) {
      this.forumIds[i] = forumIdsJson.getInt(i);
    }


    this.runId = resultJson.getInt("runId");

    this.filePath = resultJson.getString("filePath");
  }


  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public AnalysisTopic[] getResult() {
    return result;
  }

  public void setResult(AnalysisTopic[] result) {
    this.result = result;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public int[] getForumIds() {
    return forumIds;
  }

  public void setForumIds(int[] forumIds) {
    this.forumIds = forumIds;
  }

  public int getRunId() {
    return runId;
  }

  public void setRunId(int runId) {
    this.runId = runId;
  }



  @Override
  public String toString() {

    String resultStr = "result: ";
    for (int i = 0; i< result.length; i++) {
      resultStr = resultStr + result[i].toString();
    }
    return "AnalysisResults{" + "summary=" + summary + ", result=" + resultStr + ", filePath=" + filePath + '}';
  }


}
