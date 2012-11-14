package eu.wegov.prototype.web.resources.headsup;

import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import uk.ac.itinnovation.soton.wegov.hansard.Factory;
import uk.ac.itinnovation.soton.wegov.hansard.Forum;
import uk.ac.itinnovation.soton.wegov.hansard.Post;
import uk.ac.itinnovation.soton.wegov.hansard.Thread;

public class DataResource extends ServerResource {
	
	@Get("json")
	public Representation retrieve() throws MalformedURLException, JSONException {
		
		JSONObject result = new JSONObject();
				
		System.out.println("Collecting data");
		Factory f = DataConnector.getFactory();
		
		
		if (f == null)
			throw new RuntimeException("Failed to init factory");
		
		System.out.println("Finished collecting data");
		
		JSONArray forumIds = new JSONArray();
		JSONArray forumNames = new JSONArray();
		JSONArray numthreadsAndPostsInForumArray = new JSONArray();
		JSONObject threads = new JSONObject();
		JSONObject threadsStats = new JSONObject();
		JSONObject threadIdsOnly = new JSONObject();
		
		int numMessagesInForum;
		for (Forum forum : f.getForums()) {
			ArrayList<Thread> threadsData = f.getthreadsForForum(forum.getId());
			String numthreadsInForum = Integer.toString(threadsData.size());
			numMessagesInForum = 0;
			forumIds.put(Integer.toString(forum.getId()));
			
			System.out.println(forum);
			
			JSONArray threadNames = new JSONArray();
			JSONArray threadStats = new JSONArray();
			JSONArray threadIds = new JSONArray();
			
			for (Thread thread : threadsData) {
				ArrayList<Post> postsData = f.getPostsForThread(thread.getId());
				int numMessagesInthread = postsData.size();
				numMessagesInForum += numMessagesInthread;
				
				threadNames.put(thread.getName().toString());
				threadStats.put(" (" + numMessagesInthread + " posts)");
				threadIds.put(thread.getId());
				
//				System.out.println("\t-" + thread);
			}
			
			forumNames.put(forum.getName().toString());
			if (threadsData.size() > 1)
				numthreadsAndPostsInForumArray.put(numthreadsInForum + " threads, " + numMessagesInForum + " posts");
			else
				numthreadsAndPostsInForumArray.put(numthreadsInForum + " thread, " + numMessagesInForum + " posts");			
			
			threads.put(Integer.toString(forum.getId()), threadNames);
			threadsStats.put(Integer.toString(forum.getId()), threadStats);
			threadIdsOnly.put(Integer.toString(forum.getId()), threadIds);
		}
		
		result.put("forumIds", forumIds);
		result.put("forumNames", forumNames);
		result.put("numthreadsAndPostsInForum", numthreadsAndPostsInForumArray);
		result.put("threads", threads);
		result.put("threadsstats", threadsStats);
		result.put("threadIdsOnly", threadIdsOnly);
				
		return new JsonRepresentation(result);
		
	}
}
