/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.wegov.common.model;


import java.util.HashMap;
import net.sf.json.JSONObject;
//import java.util.Map;
//import west.wegovdemo.TopicOpinionDocument;

/**
 *
 * @author Steve Taylor
 */
public class TopicDocument {
  int docId = 0;
  int topicId = 0;
  double topicScore = 0.0;
  String docSubject = "not available";
  String docMessage = "not available";
  String body = "not available";
  String userId = "not available";
  String userName = "not available";
  String userType = "not available";
  int threadId = 0;
  String threadName = "not available";
  int forumId = 0;
  String forumName = "not available";
  String datePublishedAsString = "not available";

  //String opinions;
  //String [] reasons;
  double valence = 0.0;

  double dominance = 0.0;
  double arousal = 0.0;
  double deNormalisedValence = 0.0;

  JSONTwitterUserDetails userDetails = null;
  JSONTwitterPostDetails postDetails = null;
 
  
/*
  public TopicDocument(int docId, int topicId, double topicScore, String body, String userId) {
    this.docId = docId;
    this.topicId = topicId;
    this.topicScore = topicScore;
    this.body = body;
    this.userId = userId;
  }
*/
  public TopicDocument(
          int docId, int topicId, double topicScore,
          String docSubject, String docMessage, String body,
          String userId, String userName, String userType,
          int threadId, String threadName,
          int forumId, String forumName,
          String datePublishedAsString,
          double valence,
          double dominance,
          double arousal,
          double deNormalisedValence) {
    this.docId = docId;
    this.topicId = topicId;
    this.topicScore = topicScore;

    this.docSubject = docSubject;
    this.docMessage = docMessage;
    this.body = body;
    this.userId = userId;

    this.threadId = threadId;
    this.threadName = threadName;
    this.forumId = forumId;
    this.forumName = forumName;
    this.datePublishedAsString = datePublishedAsString;


    this.userName = userName;
    this.userType = userType;
    //this.opinions = opinions;
    this.valence = valence;

    this.dominance = dominance;
    this.arousal = arousal;
    this.deNormalisedValence = deNormalisedValence;
  }


  public TopicDocument(
          int docId, int topicId, double topicScore,
          String body,
          String userId,
          double valence,
          double dominance,
          double arousal,
          double deNormalisedValence) {

    this.docId = docId;
    this.topicId = topicId;
    this.topicScore = topicScore;

    this.body = body;
    this.userId = userId;
    this.valence = valence;

    this.dominance = dominance;
    this.arousal = arousal;
    this.deNormalisedValence = deNormalisedValence;
  }


  	public TopicDocument(JSONObject docJSON) {

    this.docId = docJSON.getInt("docId");
    this.topicId = docJSON.getInt("topicId");
    this.topicScore = docJSON.getDouble("topicScore");

    // optional
    if (docJSON.containsKey("docSubject")) {
      this.docSubject = docJSON.getString("docSubject");
    }

    // optional
    if (docJSON.containsKey("docMessage")) {
      this.docMessage = docJSON.getString("docMessage");
    }

    this.body = docJSON.getString("body");
    this.userId = docJSON.getString("userId");

    // optional
    if (docJSON.containsKey("threadId")) {
      this.threadId = docJSON.getInt("threadId");
    }

    // optional
    if (docJSON.containsKey("threadName")) {
      this.threadName = docJSON.getString("threadName");
    }

    // optional
    if (docJSON.containsKey("forumId")) {
      this.forumId = docJSON.getInt("forumId");
    }

    // optional
    if (docJSON.containsKey("forumName")) {
      this.forumName = docJSON.getString("forumName");
    }

    // optional
    if (docJSON.containsKey("datePublishedAsString")) {
      this.datePublishedAsString = docJSON.getString("datePublishedAsString");
    }

    // optional
    if (docJSON.containsKey("userName")) {
      this.userName = docJSON.getString("userName");
    }

    if (docJSON.containsKey("userType")) {
      this.userType = docJSON.getString("userType");
    }

    // optional
    this.valence = docJSON.getDouble("valence");

    this.dominance = docJSON.getDouble("dominance");
    this.arousal = docJSON.getDouble("arousal");
    this.deNormalisedValence = docJSON.getDouble("deNormalisedValence");
	

    // optional userDetails
    if (docJSON.containsKey("userDetails")) {
      this.userDetails = new JSONTwitterUserDetails(docJSON.getJSONObject("userDetails"));
    }

    // optional postDetails
    if (docJSON.containsKey("postDetails")) {
      this.postDetails = new JSONTwitterPostDetails(docJSON.getJSONObject("postDetails"));
    }
  
    
  }
    

  public JSONTwitterUserDetails getUserDetails() {
    if (userDetails != null) {
      return userDetails;
    }
    else {
      return new JSONTwitterUserDetails("none");
    }
  }

  public void setUserDetails(JSONTwitterUserDetails userDetails) {
    this.userDetails = userDetails;
  }

  public JSONTwitterPostDetails getPostDetails() {
    if (postDetails != null) {
      return postDetails;
    }
    else {
      return new JSONTwitterPostDetails("none");
    }
  }

  public void setPostDetails(JSONTwitterPostDetails postDetails) {
    this.postDetails = postDetails;
  }

    
    
    
  public double getDominance() {
    return dominance;
  }

  public void setDominance(double dominance) {
    this.dominance = dominance;
  }

  public double getArousal() {
    return arousal;
  }

  public void setArousal(double arousal) {
    this.arousal = arousal;
  }

  public double getDeNormalisedValence() {
    return deNormalisedValence;
  }

  public void setDeNormalisedValence(double deNormalisedValence) {
    this.deNormalisedValence = deNormalisedValence;
  }



/*
  public String[] getReasons() {
    return reasons;
  }

  public void setReasons(String[] reasons) {
    this.reasons = reasons;
  }
*/

  public String getDocSubject() {
    return docSubject;
  }

  public void setDocSubject(String docSubject) {
    this.docSubject = docSubject;
  }

  public String getDocMessage() {
    return docMessage;
  }

  public void setDocMessage(String docMessage) {
    this.docMessage = docMessage;
  }



  public int getThreadId() {
    return threadId;
  }

  public void setThreadId(int threadId) {
    this.threadId = threadId;
  }

  public String getThreadName() {
    return threadName;
  }

  public void setThreadName(String threadName) {
    this.threadName = threadName;
  }

  public int getForumId() {
    return forumId;
  }

  public void setForumId(int forumId) {
    this.forumId = forumId;
  }

  public String getForumName() {
    return forumName;
  }

  public void setForumName(String forumName) {
    this.forumName = forumName;
  }

  public String getDatePublishedAsString() {
    return datePublishedAsString;
  }

  public void setDatePublishedAsString(String datePublishedAsString) {
    this.datePublishedAsString = datePublishedAsString;
  }


  public double getValence() {
    return valence;
  }

  public void setValence(double valence) {
    this.valence = valence;
  }


  public int getDocId() {
    return docId;
  }

  public void setDocId(int docId) {
    this.docId = docId;
  }

  public int getTopicId() {
    return topicId;
  }

  public void setTopicId(int topicId) {
    this.topicId = topicId;
  }

  public double getTopicScore() {
    return topicScore;
  }

  public void setTopicScore(double topicScore) {
    this.topicScore = topicScore;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getUserType() {
    return userType;
  }

  public void setUserType(String userType) {
    this.userType = userType;
  }







}
