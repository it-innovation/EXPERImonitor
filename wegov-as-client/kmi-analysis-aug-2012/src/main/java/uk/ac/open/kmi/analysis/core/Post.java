package uk.ac.open.kmi.analysis.core;

import java.sql.Timestamp;
import uk.ac.open.kmi.analysis.UserRoles.UserFeatures;

public class Post {


	private String postID;
	private Timestamp dateCreated;
	private String inReplyToID;
 	private String textContent;       
        
        private UserFeatures author;
        
        //Initialize posts
	public Post() {
            this.postID = new String();
            this.dateCreated = null;
            this.inReplyToID = new String();
            this.textContent = new String();
            this.author = new UserFeatures();
	}

        
        //Geters and setter for post content
	public String getPostID() {
		return postID;
	}
	public void setPostID(String snspostID) {
		this.postID = snspostID;
	}        
        
	public Timestamp getDateCreated() {
		return this.dateCreated;
	}  
        
	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}        
 	public String getTextContent() {
		return textContent;
	}

	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}  
        
 	public String getInReplyToID() {
		return inReplyToID;
	}

	public void setInReplyToID(String inReplyToID) {
		this.inReplyToID = inReplyToID;
	}       
        
        
        //Getters and setters for author content
        public UserFeatures getAuthor(){
            return this.author;
        }
        
        public void setAuthor(UserFeatures author){
            this.author = author;
        }
        
	public String getAuthorID() {
		return this.author.getUserID();
	}
        
	public void setAuthorID(String authorID) {
		this.author.setUserID(authorID);
	}
        

	public void setAuthorInDegree(double authorInDegree) {
		this.author.setIndegree(authorInDegree);
	}

	public void setAuthorNumLists(double authorNumLists) {
		this.author.setNumOfLists(authorNumLists);
	}


	public double getAuthorOutDegree() {
		return this.author.getOutdegree();
	}

	public void setAuthorOutDegree(double authorOutDegree) {
		this.author.setOutdegree(authorOutDegree);
	}

	public double getAuthorNumLists() {
		return this.author.getNumOfLists();
	}

	public double getAuthorInDegree() {
		return this.author.getIndegree();
	}

        /**
        * @return the authorAge
        */
        public double getAuthorAge() {
            double age = this.author.getAge();
            if(age <=0 && this.dateCreated != null){
                return this.author.getAge(this.dateCreated);
            }
            
            return age;
        }

        /**
        * @param authorAge the authorAge to set
        */
        public void setAuthorAge(double authorAge) {
            this.author.setAge(authorAge);
        }

        /**
        * @return the authorPostRate
        */
        public double getAuthorPostRate() {
            return this.author.getPostRate();
        }

        /**
        * @param authorPostRate the authorPostRate to set
        */
        public void setAuthorPostRate(double authorPostRate) {
            this.author.setPostRate(authorPostRate);
        }

}
