package uk.ac.open.kmi.analysis.Buzz;

public class PostFeatures {

	private String postID;
	private String authorID;

	
	//See IEEE paper for the selection of these features.
	private double readability; //lower readability seed posts, higher readability more attention
	private double timeInTheday; //seed posts early, more attention early
	private double authorOutdegree;//higher authorOutdegree --> seedposts
	private double polarity; //lower polarity -->seedposts
	private double referalCount; //low referral count more attention
	
	//these two is used for backup, in case the seed identification fails.
	private Double authorNumlists; 
	private Double authorIndegree;	
        private double authorAge; //number of days from the date of the user account creation[created_at:]
        private double authorPostRate; // = this.numOfPosts/this.age
	
	public PostFeatures() {}

	public Double getReadability() {
		return readability;
	}

	public void setReadability(double readability) {
		this.readability = readability;
	}

	public double getTimeInTheday() {
		return timeInTheday;
	}

	public void setTimeInTheday(double timeInTheday) {
		this.timeInTheday = timeInTheday;
	}

	public double getAuthorOutdegree() {
		return authorOutdegree;
	}

	public void setAuthorOutdegree(double authorOutdegree) {
		this.authorOutdegree = authorOutdegree;
	}

	public double getPolarity() {
		return polarity;
	}

	public void setPolarity(double polarity) {
		this.polarity = polarity;
	}

	public double getReferalCount() {
		return referalCount;
	}

	public void setReferalCount(double referalCount) {
		this.referalCount = referalCount;
	}

	public java.lang.String getPostID() {
		return this.postID;
	}

	public void setPostID(String postID) {
		this.postID = postID;
	}

	public String getAuthorID() {
		return this.authorID;
	} 
	public void setAuthorID(String authorID) {
		this.authorID=authorID;
	}

	public Double getAuthorNumlists() {
		return this.authorNumlists;
	}
	public void setAuthorNumlists(Double authorNumlists) {
		this.authorNumlists = authorNumlists;
	}

	public Double getAuthorIndegree() {
		return this.authorIndegree;
	}

	public void setAuthorIndegree(Double authorIndegree) {
		this.authorIndegree = authorIndegree;
	}

        /**
        * @return the authorAge
        */
        public double getAuthorAge() {
            return authorAge;
        }

        /**
        * @param authorAge the authorAge to set
        */
        public void setAuthorAge(double authorAge) {
            this.authorAge = authorAge;
        }

        /**
        * @return the authorPostRate
        */
        public double getAuthorPostRate() {
            return authorPostRate;
        }

        /**
        * @param authorPostRate the authorPostRate to set
        */
        public void setAuthorPostRate(double authorPostRate) {
            this.authorPostRate = authorPostRate;
        }

}
