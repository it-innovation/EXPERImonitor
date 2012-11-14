package eu.wegov.coordinator.dao.data;

import java.util.ArrayList;
import java.util.HashMap;
import eu.wegov.coordinator.dao.data.HeadsUpPost;

public class HeadsUpPostData {

  /*
      result.put("forumIds", forumIds);
      result.put("forumNames", forumNames);
      result.put("numthreadsAndPostsInForum", numthreadsAndPostsInForumArray);
      result.put("threads", threads);
      result.put("threadsstats", threadsStats);
      result.put("threadIdsOnly", threadIdsOnly);
*/



  ArrayList posts;

  public HeadsUpPostData(ArrayList posts) {
    this.posts = posts;
  }

  public ArrayList getPosts() {
    return posts;
  }

  public void setPosts(ArrayList posts) {
    this.posts = posts;
  }

}
