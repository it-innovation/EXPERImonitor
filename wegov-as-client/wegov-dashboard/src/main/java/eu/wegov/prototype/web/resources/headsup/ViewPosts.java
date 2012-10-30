package eu.wegov.prototype.web.resources.headsup;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import uk.ac.itinnovation.soton.wegov.hansard.Factory;
import uk.ac.itinnovation.soton.wegov.hansard.Post;
import uk.ac.itinnovation.soton.wegov.hansard.Thread;

public class ViewPosts extends ServerResource {

	@Get("json")
	public Representation retrieve() throws MalformedURLException, JSONException {

 		JSONObject results = new JSONObject();

		System.out.println("Getting posts data");
		Factory f = DataConnector.getFactory();
		
		if (f == null)
			throw new RuntimeException("Failed to init factory");
		
		System.out.println("Finished collecting data");
		
		String inputThreadsIds = getQuery().getFirstValue("input");
		System.out.println("Ids to view: " + inputThreadsIds);
		
		ArrayList<String> threadIds = new ArrayList<String>(); 
		Collections.addAll(threadIds, inputThreadsIds.split(","));
		
		JSONArray posts = new JSONArray();
		int postsCounter = 0;
		
		for (Thread thread : f.getthreadsWithIds(threadIds)) {
			System.out.println(thread);
			for (Post post : f.getPostsForThread(thread.getId())) {
				JSONObject jsonPost = new JSONObject();
				jsonPost.put("id", post.getId());
				jsonPost.put("date", post.getTimePublished());
				jsonPost.put("thread", thread.getName());
				jsonPost.put("user", f.getUserWithId(post.getUserId()).getName() );
				jsonPost.put("subject", post.getSubject());
				jsonPost.put("message", post.getMessage());
				posts.put(jsonPost);
				postsCounter++;
			}
		}
		
		System.out.println("Returning " + postsCounter + " posts" );
		
		results.put("posts", posts);
		
		return new JsonRepresentation(results);
	}
}
