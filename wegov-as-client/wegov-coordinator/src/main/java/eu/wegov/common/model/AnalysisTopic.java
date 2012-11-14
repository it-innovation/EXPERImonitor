/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.wegov.common.model;

import java.util.ArrayList;


import eu.wegov.common.model.TopicDocument;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
/**
 *
 * @author sjt
 */
public class AnalysisTopic {




  // postScoreLowerThreshold determines the posts in the topic
  // each post is a member of every topic to a degree, but mostly
  // will be stronly in one topic and very little in other topics.
  // therefore by setting a threshold, we can get the posts above this.
  // the threshold here is simply to record the value set when the number
  // of posts was determined.



  int numTopicPosts;
  double postScoreLowerThreshold;

  double averageTopicSentiment;
  double topicControversy;

  String keyterms;
  String[] keyusers;
  String[] relevantdocssubjects;
  String[] relevantdocsmessages;
  String[] relevantdocsusers;
  String[] relevantdocsdates;
  String[] relevantdocscontext;
  String[] relevantdocsscores;

  TopicDocument [] topicPostsArray;

  //String [] reasons;



  public AnalysisTopic(
          String keyterms,
          String[] keyusers,
          String[] relevantdocssubjects,
          String[] relevantdocsmessages,
          String[] relevantdocsusers,
          String[] relevantdocsdates,
          String[] relevantdocscontext,
          String[] relevantdocsscores) {
    this.keyterms = keyterms;
    this.keyusers = keyusers;
    this.relevantdocssubjects = relevantdocssubjects;
    this.relevantdocsmessages = relevantdocsmessages;
    this.relevantdocsusers = relevantdocsusers;
    this.relevantdocsdates = relevantdocsdates;
    this.relevantdocscontext = relevantdocscontext;
    this.relevantdocsscores = relevantdocsscores;
  }


  public AnalysisTopic(
            String keyterms,
            ArrayList <String> keyusers,
            ArrayList <String> relevantdocssubjects,
            ArrayList <String> relevantdocsmessages,
            ArrayList <String> relevantdocsusers,
            ArrayList <String> relevantdocsdates,
            ArrayList <String> relevantdocscontext,
            ArrayList <String> relevantdocsscores) {

    this.keyterms = keyterms;

    this.keyusers = (String[]) keyusers.toArray(new String[keyusers.size()]);
    /*
    this.relevantdocssubjects =  (String[]) relevantdocssubjects.toArray(new String[relevantdocssubjects.size()]);
    this.relevantdocsmessages = (String[]) relevantdocsmessages.toArray(new String[relevantdocsmessages.size()]);
    this.relevantdocsusers = (String[]) relevantdocsusers.toArray(new String[relevantdocsusers.size()]);
    this.relevantdocsdates = (String[]) relevantdocsdates.toArray(new String[relevantdocsdates.size()]);
    this.relevantdocscontext = (String[]) relevantdocscontext.toArray(new String[relevantdocscontext.size()]);
    this.relevantdocsscores = (String[]) relevantdocsscores.toArray(new String[relevantdocsscores.size()]);
    **/

    this.relevantdocssubjects =  relevantdocssubjects.toArray(new String[relevantdocssubjects.size()]);
    this.relevantdocsmessages = relevantdocsmessages.toArray(new String[relevantdocsmessages.size()]);
    this.relevantdocsusers = relevantdocsusers.toArray(new String[relevantdocsusers.size()]);
    this.relevantdocsdates = relevantdocsdates.toArray(new String[relevantdocsdates.size()]);
    this.relevantdocscontext = relevantdocscontext.toArray(new String[relevantdocscontext.size()]);
    this.relevantdocsscores = relevantdocsscores.toArray(new String[relevantdocsscores.size()]);

  }

  public AnalysisTopic(
            int numTopicPosts,
            double postScoreLowerThreshold,
            double averageTopicSentiment,
            double topicControversy,
            String keyterms,
            ArrayList <String> keyusers,
            ArrayList <String> relevantdocssubjects,
            ArrayList <String> relevantdocsmessages,
            ArrayList <String> relevantdocsusers,
            ArrayList <String> relevantdocsdates,
            ArrayList <String> relevantdocscontext,
            ArrayList <String> relevantdocsscores
          ) {

    this.numTopicPosts = numTopicPosts;
    this.postScoreLowerThreshold =  postScoreLowerThreshold;
    this.averageTopicSentiment = averageTopicSentiment;
    this.topicControversy = topicControversy;

    this.keyterms = keyterms;

    this.keyusers = (String[]) keyusers.toArray(new String[keyusers.size()]);

    this.relevantdocssubjects =  relevantdocssubjects.toArray(new String[relevantdocssubjects.size()]);
    this.relevantdocsmessages = relevantdocsmessages.toArray(new String[relevantdocsmessages.size()]);
    this.relevantdocsusers = relevantdocsusers.toArray(new String[relevantdocsusers.size()]);
    this.relevantdocsdates = relevantdocsdates.toArray(new String[relevantdocsdates.size()]);
    this.relevantdocscontext = relevantdocscontext.toArray(new String[relevantdocscontext.size()]);
    this.relevantdocsscores = relevantdocsscores.toArray(new String[relevantdocsscores.size()]);

  }

  public AnalysisTopic(
            int numTopicPosts,
            double postScoreLowerThreshold,
            double averageTopicSentiment,
            double topicControversy,
            String keyterms,
            ArrayList <String> keyusers,
            ArrayList <String> relevantdocssubjects,
            ArrayList <String> relevantdocsmessages,
            ArrayList <String> relevantdocsusers,
            ArrayList <String> relevantdocsdates,
            ArrayList <String> relevantdocscontext,
            ArrayList <String> relevantdocsscores,
            ArrayList <TopicDocument> topicPosts
          ) {

    this.numTopicPosts = numTopicPosts;
    this.postScoreLowerThreshold =  postScoreLowerThreshold;
    this.averageTopicSentiment = averageTopicSentiment;
    this.topicControversy = topicControversy;

    this.keyterms = keyterms;

    this.keyusers = (String[]) keyusers.toArray(new String[keyusers.size()]);

    this.relevantdocssubjects =  relevantdocssubjects.toArray(new String[relevantdocssubjects.size()]);
    this.relevantdocsmessages = relevantdocsmessages.toArray(new String[relevantdocsmessages.size()]);
    this.relevantdocsusers = relevantdocsusers.toArray(new String[relevantdocsusers.size()]);
    this.relevantdocsdates = relevantdocsdates.toArray(new String[relevantdocsdates.size()]);
    this.relevantdocscontext = relevantdocscontext.toArray(new String[relevantdocscontext.size()]);
    this.relevantdocsscores = relevantdocsscores.toArray(new String[relevantdocsscores.size()]);


    this.topicPostsArray = new TopicDocument [topicPosts.size()];
    topicPosts.toArray(this.topicPostsArray);

  }

    public AnalysisTopic(JSONObject jsonTopic) {

    this.numTopicPosts = jsonTopic.getInt("numTopicPosts");
    this.postScoreLowerThreshold =  jsonTopic.getDouble("postScoreLowerThreshold");
    this.averageTopicSentiment = jsonTopic.getDouble("averageTopicSentiment");
    this.topicControversy = jsonTopic.getDouble("topicControversy");

    this.keyterms = jsonTopic.getString("keyterms");

    JSONArray keyusersJson = jsonTopic.getJSONArray("keyusers");
    this.keyusers = new String [keyusersJson.size()];
    for (int i=0; i < keyusersJson.size(); i++) {
      this.keyusers[i] = keyusersJson.getString(i);
    }

    //this.relevantdocssubjects =  relevantdocssubjects.toArray(new String[relevantdocssubjects.size()]);
    JSONArray relevantdocssubjectsJson = jsonTopic.getJSONArray("relevantdocssubjects");
    this.relevantdocssubjects = new String [relevantdocssubjectsJson.size()];
    for (int i=0; i < relevantdocssubjectsJson.size(); i++) {
      this.relevantdocssubjects[i] = relevantdocssubjectsJson.getString(i);
    }


    //this.relevantdocsmessages = relevantdocsmessages.toArray(new String[relevantdocsmessages.size()]);
    JSONArray relevantdocsmessagesJson = jsonTopic.getJSONArray("relevantdocsmessages");
    this.relevantdocsmessages = new String [relevantdocsmessagesJson.size()];
    for (int i=0; i < relevantdocsmessagesJson.size(); i++) {
      this.relevantdocsmessages[i] = relevantdocsmessagesJson.getString(i);
    }

    //this.relevantdocsusers = relevantdocsusers.toArray(new String[relevantdocsusers.size()]);
    JSONArray relevantdocsusersJson = jsonTopic.getJSONArray("relevantdocsusers");
    this.relevantdocsusers = new String [relevantdocsusersJson.size()];
    for (int i=0; i < relevantdocsusersJson.size(); i++) {
      this.relevantdocsusers[i] = relevantdocsusersJson.getString(i);
    }

    //this.relevantdocsdates = relevantdocsdates.toArray(new String[relevantdocsdates.size()]);
    JSONArray relevantdocsdatesJson = jsonTopic.getJSONArray("relevantdocsdates");
    this.relevantdocsdates = new String [relevantdocsdatesJson.size()];
    for (int i=0; i < relevantdocsdatesJson.size(); i++) {
      this.relevantdocsdates[i] = relevantdocsdatesJson.getString(i);
    }

    //this.relevantdocscontext = relevantdocscontext.toArray(new String[relevantdocscontext.size()]);
    JSONArray relevantdocscontextJson = jsonTopic.getJSONArray("relevantdocscontext");
    this.relevantdocscontext = new String [relevantdocscontextJson.size()];
    for (int i=0; i < relevantdocscontextJson.size(); i++) {
      this.relevantdocscontext[i] = relevantdocscontextJson.getString(i);
    }

    //this.relevantdocsscores = relevantdocsscores.toArray(new String[relevantdocsscores.size()]);
    JSONArray relevantdocsscoresJson = jsonTopic.getJSONArray("relevantdocsscores");
    this.relevantdocsscores = new String [relevantdocsscoresJson.size()];
    for (int i=0; i < relevantdocsscoresJson.size(); i++) {
      this.relevantdocsscores[i] = relevantdocsscoresJson.getString(i);
    }


    JSONArray topicPostsArrayJson = jsonTopic.getJSONArray("topicPostsArray");
    this.topicPostsArray = new TopicDocument [topicPostsArrayJson.size()];
    for (int i=0; i < topicPostsArrayJson.size(); i++) {
      this.topicPostsArray[i] = new TopicDocument(topicPostsArrayJson.getJSONObject(i));
    }

    /*
      this.docId = docJSON.getInt("docId");
    this.topicId = docJSON.getInt("topicId");
    this.topicScore = docJSON.getDouble("topicScore");
    */
   }



  public TopicDocument[] getTopicPostsArray() {
    return topicPostsArray;
  }

  public void setTopicPostsArray(TopicDocument[] topicPostsArray) {
    this.topicPostsArray = topicPostsArray;
  }




  public void setPostScoreLowerThreshold(double postScoreLowerThreshold) {
    this.postScoreLowerThreshold = postScoreLowerThreshold;
  }

  public double getPostScoreLowerThreshold() {
    return postScoreLowerThreshold;
  }

  public int getNumTopicPosts() {
    return numTopicPosts;
  }

  public void setNumTopicPosts(int numTopicPosts) {
    this.numTopicPosts = numTopicPosts;
  }

  public double getAverageTopicSentiment() {
    return averageTopicSentiment;
  }

  public void setAverageTopicSentiment(double averageTopicSentiment) {
    this.averageTopicSentiment = averageTopicSentiment;
  }

  public double getTopicControversy() {
    return topicControversy;
  }

  public void setTopicControversy(double topicControversy) {
    this.topicControversy = topicControversy;
  }

  public String getKeyterms() {
    return keyterms;
  }

  public void setKeyterms(String keyterms) {
    this.keyterms = keyterms;
  }

  public String[] getKeyusers() {
    return keyusers;
  }

  public void setKeyusers(String[] keyusers) {
    this.keyusers = keyusers;
  }

  public String[] getRelevantdocssubjects() {
    return relevantdocssubjects;
  }

  public void setRelevantdocssubjects(String[] relevantdocssubjects) {
    this.relevantdocssubjects = relevantdocssubjects;
  }

  public String[] getRelevantdocsmessages() {
    return relevantdocsmessages;
  }

  public void setRelevantdocsmessages(String[] relevantdocsmessages) {
    this.relevantdocsmessages = relevantdocsmessages;
  }

  public String[] getRelevantdocsusers() {
    return relevantdocsusers;
  }

  public void setRelevantdocsusers(String[] relevantdocsusers) {
    this.relevantdocsusers = relevantdocsusers;
  }

  public String[] getRelevantdocsdates() {
    return relevantdocsdates;
  }

  public void setRelevantdocsdates(String[] relevantdocsdates) {
    this.relevantdocsdates = relevantdocsdates;
  }

  public String[] getRelevantdocscontext() {
    return relevantdocscontext;
  }

  public void setRelevantdocscontext(String[] relevantdocscontext) {
    this.relevantdocscontext = relevantdocscontext;
  }

  public String[] getRelevantdocsscores() {
    return relevantdocsscores;
  }

  public void setRelevantdocsscores(String[] relevantdocsscores) {
    this.relevantdocsscores = relevantdocsscores;
  }

  @Override
  public String toString() {
    return "AnalysisTopic{" + "keyterms=" + keyterms + ", keyusers=" + keyusers + ", relevantdocssubjects=" + relevantdocssubjects + ", relevantdocsmessages=" + relevantdocsmessages + ", relevantdocsusers=" + relevantdocsusers + ", relevantdocsdates=" + relevantdocsdates + ", relevantdocscontext=" + relevantdocscontext + ", relevantdocsscores=" + relevantdocsscores + '}';
  }


}
