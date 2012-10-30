package uk.ac.open.kmi.analysis.test;

import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.open.kmi.analysis.Buzz.BuzzPrediction;
import uk.ac.open.kmi.analysis.core.Language;
import uk.ac.open.kmi.analysis.core.Post;

public class testBuzz {

    public static void main(String[] args) throws SQLException {
        try {
            //load some random posts
            TwitterDBConnexion dbConnection = new TwitterDBConnexion();
            Vector<Post> postList = dbConnection.getPostList(300);

            //create a buzz prediction analysis
            BuzzPrediction bpAnalysis = new BuzzPrediction(Language.GERMAN);
  
            //pass the input
            bpAnalysis.setInputPosts(postList);

            //Get the top  K  Buzz Posts
            for (Entry<String, Double> entry : bpAnalysis.getTopKMostBuzzPosts(6)) {
                System.out.println("POST: " + entry.getKey() + " : " + entry.getValue());
            }

            //Get the top  K  Buzz Users
            for (Entry<String, Double> entry : bpAnalysis.getTopKMostBuzzUsers(6)) {
                System.out.println("AUTHOR: " + entry.getKey() + ":" + entry.getValue());
            }
        } catch (Exception ex) {
            Logger.getLogger(testBuzz.class.getName()).log(Level.SEVERE, null, ex);
        }


    }
}