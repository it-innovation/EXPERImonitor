package eu.wegov.coordinator.dao.data;

import java.util.ArrayList;
import java.util.HashMap;

public class HeadsUpForumAndThreadData {

  /*
      result.put("forumIds", forumIds);
      result.put("forumNames", forumNames);
      result.put("numthreadsAndPostsInForum", numthreadsAndPostsInForumArray);
      result.put("threads", threads);
      result.put("threadsstats", threadsStats);
      result.put("threadIdsOnly", threadIdsOnly);
*/

      ArrayList forumIds;
      ArrayList forumNames;
      ArrayList numthreadsAndPostsInForum;
      HashMap threads;
      HashMap threadsStats;
      HashMap threadIdsOnly;

      ArrayList posts = null;


  public HeadsUpForumAndThreadData(
          ArrayList forumIds,
          ArrayList forumNames,
          ArrayList numthreadsAndPostsInForum,
          HashMap threads,
          HashMap threadsStats,
          HashMap threadIdsOnly) {
    this(
            forumIds, forumNames, numthreadsAndPostsInForum,
            threads, threadsStats, threadIdsOnly,
            null);
  }

  public HeadsUpForumAndThreadData(
          ArrayList forumIds,
          ArrayList forumNames,
          ArrayList numthreadsAndPostsInForum,
          HashMap threads,
          HashMap threadsStats,
          HashMap threadIdsOnly,
          ArrayList posts) {
    this.forumIds = forumIds;
    this.forumNames = forumNames;
    this.numthreadsAndPostsInForum = numthreadsAndPostsInForum;
    this.threads = threads;
    this.threadsStats = threadsStats;
    this.threadIdsOnly = threadIdsOnly;
    this.posts = posts;
  }

  public ArrayList getPosts() {
    return posts;
  }

  public void setPosts(ArrayList posts) {
    this.posts = posts;
  }

  public ArrayList getForumIds() {
    return forumIds;
  }

  public void setForumIds(ArrayList forumIds) {
    this.forumIds = forumIds;
  }

  public ArrayList getForumNames() {
    return forumNames;
  }

  public void setForumNames(ArrayList forumNames) {
    this.forumNames = forumNames;
  }

  public ArrayList getNumthreadsAndPostsInForum() {
    return numthreadsAndPostsInForum;
  }

  public void setNumthreadsAndPostsInForum(ArrayList numthreadsAndPostsInForum) {
    this.numthreadsAndPostsInForum = numthreadsAndPostsInForum;
  }

  public HashMap getThreadIdsOnly() {
    return threadIdsOnly;
  }

  public void setThreadIdsOnly(HashMap threadIdsOnly) {
    this.threadIdsOnly = threadIdsOnly;
  }

  public HashMap getThreads() {
    return threads;
  }

  public void setThreads(HashMap threads) {
    this.threads = threads;
  }

  public HashMap getThreadsStats() {
    return threadsStats;
  }

  public void setThreadsStats(HashMap threadsStats) {
    this.threadsStats = threadsStats;
  }


}
