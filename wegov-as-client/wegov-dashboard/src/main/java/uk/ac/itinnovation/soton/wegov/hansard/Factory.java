package uk.ac.itinnovation.soton.wegov.hansard;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Factory {
	private ArrayList<Forum> forums = new ArrayList<Forum>();
	private ArrayList<Thread> threads = new ArrayList<Thread>();
	private ArrayList<Post> posts = new ArrayList<Post>();
	private ArrayList<User> users = new ArrayList<User>();

	public Factory() {
		
	}
	
	public Forum addForum(String forumName) {
		boolean forumNotFound = true;
		Forum result = null;
		
		for(Forum forum : forums) {
			if (forum.getName().equals(forumName)) {
				forumNotFound = false;
				result = forum;
				break;
			}
		}
		
		if (forumNotFound) {
			int newForumId = forums.size();
			result = new Forum(newForumId, forumName);
			forums.add(result);
		}
		
		return result;
	}
	
	public Thread addthread(int forumId, String threadName) {
		boolean threadNotFound = true;
		Thread result = null;
		
		for (Thread thread : threads) {
			if (forumId == thread.getForumId() && threadName.equals(thread.getName())) {
				threadNotFound = false;
				result = thread;
				break;
			}
		}
		if (threadNotFound) {
			int newthreadId = threads.size();
			result = new Thread(newthreadId, forumId, threadName);
			threads.add(result);
		}
		
		return result;
	}
	
	public User addUser(String userName, String userType) {
		boolean userNotFound = true;
		User result = null;
		
		for(User user : users) {
			if (user.getName().equals(userName)) {
				userNotFound = false;
				result = user;
				break;
			}
		}
		
		if (userNotFound) {
			int newUserId = users.size();
			result = new User(newUserId, userName, userType);
			users.add(result);
		}
		
		return result;
	}
	
	public void addPost(int threadId, String subject, String message, Timestamp timePublished, int userId) {
		posts.add(new Post(posts.size(), threadId, subject, message, timePublished, userId));
	}
	
	public ArrayList<Thread> getthreadsForForum(int forumId) {
		ArrayList<Thread> threadsForForumWithId = new ArrayList<Thread>();
		
		for (Thread thread : threads) {
			if (thread.getForumId() == forumId) {
				threadsForForumWithId.add(thread);
			}
		}
		
		return threadsForForumWithId;
	}
	
	
	public Forum getForumWithId(int forumId) {
		Forum forumWithId = new Forum();
		
		for (Forum forum: forums) {
			if (forum.getId() == forumId) {
				forumWithId = forum;
			}
		}
		
		return forumWithId;
	}	
	
	public ArrayList<Thread> getthreadsWithIds(ArrayList<String> threadIds) {
		ArrayList<Thread> threadsWithIds = new ArrayList<Thread>();
		
		for (Thread thread : threads) {
			if (threadIds.contains(Integer.toString(thread.getId()))) {
				threadsWithIds.add(thread);
			}
		}
		
		return threadsWithIds;
	}
	
	public Thread getThreadWithId(int threadId) {
		Thread threadWithId = new Thread();
		
		for (Thread thread : threads) {
			if (threadId == thread.getId()) {
				threadWithId = thread;
				break;
			}
		}
		
		return threadWithId;
	}
	
	public ArrayList<Post> getPostsWithIds(ArrayList<String> postIds) {
		ArrayList<Post> postsWithIds = new ArrayList<Post>();
		
		for (Post post : posts) {
			if (postIds.contains(Integer.toString(post.getId()))) {
				postsWithIds.add(post);
			}
		}
		
		return postsWithIds;
	}
	
	public Post getPostWithId(int postId) {
		Post postWithId = new Post();
		
		for (Post post: posts) {
			if (post.getId() == postId) {
				postWithId = post;
			}
		}
		
		return postWithId;
	}
	
	public int getNumPostsInThread(int threadId) {
		return getPostsForThread(threadId).size();		
	}
	
	public ArrayList<Post> getPostsForThread(int threadId) {
		ArrayList<Post> postsForThreadWithId = new ArrayList<Post>();
		
		for (Post post : posts) {
			if (post.getThreadId() == threadId) {
				postsForThreadWithId.add(post);
			}
		}
		
		return postsForThreadWithId;		
	}
	
	public ArrayList<Post> getPosts() {
		return posts;
	}
	
	public ArrayList<Post> getPosts(Pattern searchPattern) {
		System.out.println("Searching for posts matching: " + searchPattern);
		ArrayList<Post> selectedPosts = new ArrayList<Post>();
		
		for (Post post : posts) {
			//Check if subject or message matches pattern
			if ( matchesPattern(post.getSubject(), searchPattern) || matchesPattern(post.getMessage(), searchPattern) ) {
				//System.out.println(post.getSubject() + " matched");
				selectedPosts.add(post);
			}
			else {
				//System.out.println(post.getSubject() + " not matched");
			}
		}
		
		return selectedPosts;
	}
	
	public ArrayList<Forum> getForums() {
		return forums;
	}
	
	public ArrayList<Forum> getForums(Pattern searchPattern) {
		System.out.println("Searching for forums matching: " + searchPattern);
		ArrayList<Forum> selectedForums = new ArrayList<Forum>();
		
		for (Forum forum : forums) {
			if (matchesPattern(forum.getName(), searchPattern)) {
				System.out.println(forum.getName() + " matched");
				selectedForums.add(forum);
			}
			else {
				System.out.println(forum.getName() + " not matched");
			}
		}
		
		return selectedForums;
	}
	
	private boolean matchesPattern(String text, Pattern pattern) {
		String lcText = text.toLowerCase();
		return (pattern.matcher(lcText).matches());
	}

	public ArrayList<User> getUsers() {
		return users;
	}
	
	public User getUserWithId(int userId) {
		User tempUser = new User();
		
		for (User currentUser : users) {
			if (currentUser.getId() == userId) {
				tempUser = currentUser;
				break;
			}
		}
		return tempUser;
	}
	
	
	
	public int getForumsSize() {
		return forums.size();
	}
	
	public int getthreadsSize() {
		return threads.size();
	}
	
	public int getUsersSize() {
		return users.size();
	}	
	
	public int getCurrentForumId() {
		return forums.size() - 1;
	}
	
	public int getCurrentthreadId() {
		return threads.size() - 1;
	}

	
}
