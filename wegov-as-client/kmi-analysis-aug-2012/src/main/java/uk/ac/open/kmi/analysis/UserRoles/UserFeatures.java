package uk.ac.open.kmi.analysis.UserRoles;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class UserFeatures {
	
        //IMPORANT: ALL THESE VALUES NEED TO BE FILED FROM THE JSON FILE
        private String userID;
	private double indegree; //number of followers [followers_count:]
	private double outdegree; //number of users this user follows [friends_count: ]
        private double numOfLists; //number of lists user belongs to [listed_count:]
	private double numOfPosts; //number of posts [statuses_count:]
	private Timestamp userSignUp;
        
        
        //This values are computed by the class
        private double outInRatio; //=this.outdegree/this.indegree
        private double age;//number of days from the date of the user account creation[created_at:]
        private double postRate; // = this.numOfPosts/this.age
        
	
        public UserFeatures(){
            this.userID = new String();
            this.age = 0;
            this.indegree = 0;
            this.numOfLists = 0;
            this.numOfPosts = 0;
            this.postRate = 0;
            this.outdegree = 0;
            this.outInRatio = 0;
            this.userSignUp = null;
            
        }
        
	public double getAge() {
            return age;
	}
        
        public double getAge(Timestamp postCreation){
            if(age <= 0 && this.userSignUp !=null){
                long start = userSignUp.getTime();
                long createdLong = postCreation.getTime();
                age = createdLong - start;
                age /= 1000; // in secs
                age /= 60; // in mins
                age /= 60; // in hours
                age /= 24; // in days   
            }
            return age;
        }
        
	public void setAge(double age) {
		this.age = age;
	}
	public double getIndegree() {
		return indegree;
	}
	public void setIndegree(double numOfFollowers) {
		this.indegree = numOfFollowers;
	}
	public double getNumOfLists() {
		return numOfLists;
	}
	public void setNumOfLists(double numOfLists) {
		this.numOfLists = numOfLists;
	}
	public double getNumOfPosts() {
		return numOfPosts;
	}
	public void setNumOfPosts(double numOfPosts) {
		this.numOfPosts = numOfPosts;
	}
	public double getPostRate() {
            if(this.postRate <= 0){
                if ((this.numOfPosts > 0) && (age > 0)) {
                    this.postRate = (double) numOfPosts / (double) age;
                }
            }
            return this.postRate;
	}
	public void setPostRate(double postRate) {
            this.postRate = postRate;

	}
	public double getOutdegree() {
		return outdegree;
	}
	public void setOutdegree(double numOfFollowing) {
		this.outdegree = numOfFollowing;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public void setOutInRatio(double outInRatio) {
		this.outInRatio = outInRatio;
	}
	public double getOutInRatio() {
            if(this.outInRatio <= 0 && this.indegree > 0){
                this.outInRatio = this.indegree / this.outdegree;
            }
            return outInRatio;
	}

}
