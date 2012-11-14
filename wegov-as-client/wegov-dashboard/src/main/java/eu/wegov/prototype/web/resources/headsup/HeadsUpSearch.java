package eu.wegov.prototype.web.resources.headsup;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

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

public class HeadsUpSearch extends ServerResource {
	
	@Get("json")
	public Representation retrieve() throws MalformedURLException, JSONException {
		
		String query = getQuery().getFirstValue("q");
		System.out.println("Query: " + query);
		
		return searchPosts(query);
	}
	
	private Pattern createSearchPattern(String query) {
		String lcQuery = query.toLowerCase();
		Pattern pattern = Pattern.compile(".*" + lcQuery + ".*");
		return pattern;
	}

	public Representation searchForums(String query) throws MalformedURLException, JSONException {
		
		Pattern searchPattern = createSearchPattern(query);

		JSONObject results = new JSONObject();
				
		Factory f = DataConnector.getFactory();
		
		if (f == null)
			throw new RuntimeException("Failed to init factory");
		
		System.out.println("Finished collecting data");

		System.out.println("\nHeadsUp search forums containing: " + query);

		JSONArray forumIds = new JSONArray();
		JSONArray forumNames = new JSONArray();
		JSONArray numthreadsAndPostsInForumArray = new JSONArray();
		JSONObject threads = new JSONObject();
		JSONObject threadsStats = new JSONObject();
		JSONObject threadIdsOnly = new JSONObject();
		
		int numMessagesInForum;
		for (Forum forum : f.getForums(searchPattern)) {
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
		
		results.put("forumIds", forumIds);
		results.put("forumNames", forumNames);
		results.put("numthreadsAndPostsInForum", numthreadsAndPostsInForumArray);
		results.put("threads", threads);
		results.put("threadsstats", threadsStats);
		results.put("threadIdsOnly", threadIdsOnly);
				
		return new JsonRepresentation(results);
		
	}
	
	public Representation searchPosts(String query) throws MalformedURLException, JSONException {
		
		Pattern searchPattern = createSearchPattern(query);

		JSONObject results = new JSONObject();
				
		Factory f = DataConnector.getFactory();
		
		if (f == null)
			throw new RuntimeException("Failed to init factory");
		
		System.out.println("Finished collecting data");

		System.out.println("\nHeadsUp search posts containing: " + query);

		JSONArray forumIds = new JSONArray();
		JSONArray forumNames = new JSONArray();
		JSONArray numthreadsAndPostsInForumArray = new JSONArray();
		JSONObject threads = new JSONObject();
		JSONObject threadsStats = new JSONObject();
		JSONObject threadIdsOnly = new JSONObject();
		
		JSONArray posts = new JSONArray();
		
		int postsCounter = 0;

		ArrayList<Post> selectedPosts = f.getPosts(searchPattern);

		TreeSet<Integer> threadIdsSet = new TreeSet<Integer>();
		HashMap<Integer, Thread> threadsMap = new HashMap<Integer, Thread>();

		// Get list of threads for returned posts
		System.out.println("\nGetting thread ids for posts");
		for (Post post : selectedPosts) {
			int threadId = post.getThreadId();
			System.out.println("Thread id: " + threadId);
			threadIdsSet.add(threadId);
		}
		
		// Initialise lists of posts for each thread
		HashMap<Integer,ArrayList<Post>> threadPosts = new HashMap<Integer,ArrayList<Post>>();
		for (Integer threadId : threadIdsSet) {
			ArrayList<Post> postsList = new ArrayList<Post>();
			threadPosts.put(threadId, postsList);
		}

		// Allocate posts to each thread
		for (Post post : selectedPosts) {
			int threadId = post.getThreadId();
			ArrayList<Post> postsList = threadPosts.get(threadId);
			postsList.add(post);
		}

		//HashSet<Integer> forumIdsSet = new HashSet<Integer>();
		TreeSet<Integer> forumIdsSet = new TreeSet<Integer>(); // naturally ordered
		//HashMap<Integer, Forum> forumsMap = new HashMap<Integer, Forum>();
		
		// Get unordered list of forums for set of thread ids
		System.out.println("\nGetting forums ids for unique threads");
		for (Integer threadId : threadIdsSet) {
			Thread thread = f.getThreadWithId(threadId);
			threadsMap.put(threadId, thread); // store in map for later
			int forumId = thread.getForumId();
			System.out.println("Thread id: " + threadId + ", forum id: " + forumId);
			forumIdsSet.add(forumId);
		}

		// Initialise lists of threads for each forum
		HashMap<Integer,ArrayList<Thread>> forumThreads = new HashMap<Integer,ArrayList<Thread>>();
		for (Integer forumId : forumIdsSet) {
			ArrayList<Thread> threadsList = new ArrayList<Thread>();
			forumThreads.put(forumId, threadsList);
		}

		// Allocate threads to each forum
		for (Integer threadId : threadIdsSet) {
			Thread thread = threadsMap.get(threadId);
			int forumId = thread.getForumId();
			ArrayList<Thread> threadsList = forumThreads.get(forumId);
			threadsList.add(thread);
		}
		
		System.out.println("\nForums:");
		for (Integer forumId : forumIdsSet) {
			int numMessagesInForum;

			ArrayList<Thread> threadsList = forumThreads.get(forumId);
			int numthreadsInForum = threadsList.size();
			numMessagesInForum = 0;
			
			JSONArray threadNames = new JSONArray();
			JSONArray threadStats = new JSONArray();
			JSONArray threadIds = new JSONArray();
			
			forumIds.put(Integer.toString(forumId));
			Forum forum = f.getForumWithId(forumId);
			System.out.println(forumId + ": " + forum.getName());
			//forumsMap.put(forumId, forum);
			forumNames.put(forum.getName());

			for (Thread thread : threadsList) {
				ArrayList<Post> postsList = threadPosts.get(thread.getId());
				int numMessagesInthread = postsList.size();
				numMessagesInForum += numMessagesInthread;
				
				threadNames.put(thread.getName().toString());
				threadStats.put(" (" + numMessagesInthread + " posts)");
				threadIds.put(thread.getId());
			}
			
			if (threadsList.size() > 1)
				numthreadsAndPostsInForumArray.put(numthreadsInForum + " threads, " + numMessagesInForum + " posts");
			else
				numthreadsAndPostsInForumArray.put(numthreadsInForum + " thread, " + numMessagesInForum + " posts");			
			
			threads.put(Integer.toString(forumId), threadNames);
			threadsStats.put(Integer.toString(forumId), threadStats);
			threadIdsOnly.put(Integer.toString(forumId), threadIds);
		}
		
		for (Post post : selectedPosts) {
			//System.out.println(post);
			Thread thread = threadsMap.get(post.getThreadId());
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
						
		System.out.println("\nReturning " + postsCounter + " posts" );
		
		results.put("forumIds", forumIds);
		results.put("forumNames", forumNames);
		results.put("numthreadsAndPostsInForum", numthreadsAndPostsInForumArray);
		results.put("threads", threads);
		results.put("threadsstats", threadsStats);
		results.put("threadIdsOnly", threadIdsOnly);
		results.put("posts", posts);
		
		return new JsonRepresentation(results);
	}

	public static void main(String[] args) throws Exception {
		String query = "endangered";
		HeadsUpSearch headsUpSearch = new HeadsUpSearch();
		headsUpSearch.searchPosts(query);
	}
}
