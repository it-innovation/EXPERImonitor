package uk.ac.open.kmi.analysis.test;

//select * from tblposts where text REGEXP '#fb' limit 20;
//import com.mysql.jdbc.Connection;
//import com.mysql.jdbc.Statement;
import java.sql.Connection;
import java.sql.Statement;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.open.kmi.analysis.UserRoles.UserFeatures;
import uk.ac.open.kmi.analysis.core.Post;

public class TwitterDBConnexion {

    // set up the db params
    private String url = "jdbc:mysql://localhost";
    private String dbname = "twitter_data_collection_wegov";
    private String username = "root";
    private String password = "picapiedras";
    private Connection conn;

    protected TwitterDBConnexion() throws IllegalAccessException {
        try {
            // create the connectin
            String dburl = url + "/" + dbname;
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            this.conn = (Connection) DriverManager.getConnection(dburl, username, password);
        } catch (SQLException ex) {
            Logger.getLogger(TwitterDBConnexion.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(TwitterDBConnexion.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TwitterDBConnexion.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public Vector<Post> getPostList(int numPosts) {
        Vector<Post> postList = new Vector<Post>();
        try {

            String query = "select * from tblposts limit " + numPosts;
            java.sql.Statement s = conn.createStatement();
            ResultSet results = s.executeQuery(query);

            while (results.next()) {
                Post p = new Post();

                String postId = results.getString("snspostid");
                String userId = results.getString("snsUserID");
                String text = results.getString("text");
                java.sql.Timestamp date = results.getTimestamp("posttimestamp");
                String parentId = getParent(postId);
                UserFeatures user = getUser(userId);

                p.setPostID(postId);
                p.setAuthorID(userId);
                p.setTextContent(text);
                p.setDateCreated(date);
                p.setInReplyToID(parentId);
                p.setAuthor(user);
                postList.add(p);

            }
            s.close();
        } catch (SQLException ex) {
            Logger.getLogger(TwitterDBConnexion.class.getName()).log(Level.SEVERE, null, ex);
        }

        return postList;

    }

    public Vector<UserFeatures> getUserList(int numUsers) {
        Vector<UserFeatures> usersList = new Vector<UserFeatures>();
        try {


            String query = "select * from tbluserimpacts limit " + numUsers;
            java.sql.Statement s = conn.createStatement();
            ResultSet results = s.executeQuery(query);
            Post p = new Post();

            while (results.next()) {
                UserFeatures user = new UserFeatures();
                String userId = results.getString("snsUserID");
                long outdegree = results.getLong("outdegree");
                long indegree = results.getLong("indegree");
                long numLists = results.getLong("numLists");
                java.sql.Timestamp collectData = results.getTimestamp("collectdate");
                int age = results.getInt("age");
                double postRate = results.getDouble("postrate");
                user.setUserID(userId);
                user.setOutdegree(outdegree);
                user.setIndegree(indegree);
                user.setNumOfLists(numLists);
                user.setAge(age);
                user.setPostRate(postRate);
                user.setOutInRatio(1.0 * user.getOutdegree() / user.getIndegree());
                usersList.add(user);

            }


        } catch (SQLException ex) {
            Logger.getLogger(TwitterDBConnexion.class.getName()).log(Level.SEVERE, null, ex);
        }

        return usersList;
    }

    public UserFeatures getUser(String userId) {
        UserFeatures user = new UserFeatures();
        try {
            String query = "select * from tbluserimpacts where snsUserID =" + userId;
            java.sql.Statement s = conn.createStatement();
            ResultSet results = s.executeQuery(query);
            Post p = new Post();
            while (results.next()) {
                String id = results.getString("snsUserID");
                long outdegree = results.getLong("outdegree");
                long indegree = results.getLong("indegree");
                long numLists = results.getLong("numLists");
                java.sql.Timestamp collectData = results.getTimestamp("collectdate");
                int age = results.getInt("age");
                double postRate = results.getDouble("postrate");

                user.setUserID(userId);
                user.setOutdegree(outdegree);
                user.setIndegree(indegree);
                user.setNumOfLists(numLists);
                user.setAge(age);
                user.setPostRate(postRate);
                user.setOutInRatio(1.0 * user.getOutdegree() / user.getIndegree());

            }
            s.close();

        } catch (SQLException ex) {
            Logger.getLogger(TwitterDBConnexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return user;

    }

    public String getParent(String snsPostID) {
        String result = new String();
        try {
            Statement s = (Statement) conn.createStatement();
            s.executeQuery("select origID from replies where replyid='" + snsPostID + "';");
            ResultSet rs = s.getResultSet();
            while (rs.next()) {
                String text = rs.getString("origID");
                result = text;
            }
            rs.close();
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Problem for replyid: " + snsPostID);
        }
        return result;
    }

    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
