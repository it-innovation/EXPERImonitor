package uk.ac.open.kmi.analysis.test;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.open.kmi.analysis.DiscussionActivity.DiscussionActivity;
import uk.ac.open.kmi.analysis.DiscussionActivity.DiscussionActivityInput;
import uk.ac.open.kmi.analysis.core.Post;

public class testDiscussionActivity {

    public static void main(String[] args) throws SQLException {
        try {
            //load some posts
            TwitterDBConnexion dbConnection = new TwitterDBConnexion();
            Vector<Post> postList = dbConnection.getPostList(200);

            //create a discussion activity analysis and add the input
            //Only set posts in the DiscussionActivityInput, no start, end, step;
            DiscussionActivity discussionActivityAnalysis = new DiscussionActivity();
            DiscussionActivityInput dInput = new DiscussionActivityInput();
            dInput.setInputPosts(postList); 
            discussionActivityAnalysis.setDiscussionActivityInput(dInput); 

            
            //Example1: Discussion Activity considers the date of the first and last posts
            //and selects automatically the step
            System.out.println(discussionActivityAnalysis.getStep());
            double[] val = discussionActivityAnalysis.getDiscussionRate();
            for (double du : val) {
                System.out.println(du);
            }

            System.out.println("\n");
            
            
            
            //Example2: automatically select the time-stamp and step
    //		dInput.setStart(new Timestamp(1234566));//specify start date
    //		dInput.setEnd(new Timestamp(12345646)); //specify end date
            dInput.setSTEP("DAY");//specify STEP
            discussionActivityAnalysis.setDiscussionActivityInput(dInput); 
            System.out.println("STEP: " + discussionActivityAnalysis.getStep());//Get the STEP
            for (double du : discussionActivityAnalysis.getDiscussionRate()) {
                System.out.println(du);
            }

            System.out.println("\n");



            System.out.println("Print Active Users");
            for (Entry<String, Integer> entry : discussionActivityAnalysis.getTopKMostActiveUsr(10)) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }
            
            
        } catch (IllegalAccessException ex) {
            Logger.getLogger(testDiscussionActivity.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}