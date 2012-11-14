package uk.ac.itinnovation.soton.wegov.hansard;


import java.sql.Timestamp;

/**
 * Represents Hansard HeadsUp Subject + Message posted on a thread of a Forum
 *
 */
public class Post {
	private int id = 0;
	private int threadId = 0;
	private String subject = "";
	private String message = "";
	private Timestamp timePublished = new Timestamp(0L);
	private int userId = 0;
	
	public Post() {
		
	}
	
	public Post(int id, int threadId, String subject, String message, Timestamp timePublished, int userId) {
		this.id = id;
		this.threadId = threadId;
		this.subject = subject;
		this.message = message;
		this.timePublished = timePublished;
		this.userId = userId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getThreadId() {
		return threadId;
	}

	public void setthreadId(int threadId) {
		this.threadId = threadId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessage() {
		return message;
	}
	
	public String getMessageClean() {
		String result = message.replaceAll("\\<!--.*?-->","");
		result = result.replaceAll("\\<.*?>","").replaceAll("&nbsp;", " ").replaceAll("&quot;", "")
				.replaceAll("&lsquo;", "").replaceAll("&rsquo;", "").replaceAll("&amp;", "")
				.replaceAll(" +", " ").trim();
		return result;
	}
	
	public String getContents() {
		return getSubject() + " " + getMessageClean();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Timestamp getTimePublished() {
		return timePublished;
	}

	public void setTimePublished(Timestamp timePublished) {
		this.timePublished = timePublished;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}	
	
	@Override
	public String toString() {
		
		return "[" + id + "] from thread [" + threadId + "] " + subject + ", [message]: " + message + " - published on " + timePublished + " by user [" + userId + "]";
	}

}
