package uk.ac.open.kmi.analysis.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.open.kmi.analysis.UserRoles.UserFeatures;
import uk.ac.open.kmi.analysis.UserRoles.UserRole;
import uk.ac.open.kmi.analysis.UserRoles.UserRoleAnalysis;
import uk.ac.open.kmi.analysis.core.Language;
import uk.ac.open.kmi.analysis.core.Post;

public class testUserRoles {

    public static void main(String[] args) throws SQLException {
        try {
            
            //for visualization purposes
            Vector<String> rolesDescriptions = listFilesInDir("./data/roleClassifiers/", ".description");
            for (String role : rolesDescriptions) {
                System.out.println(readStringFile("./data/roleClassifiers/" + role + ".description"));
            }             
            
            
            //load some random users
            TwitterDBConnexion dbConnection = new TwitterDBConnexion();
            Vector<UserFeatures> userListInput = dbConnection.getUserList(200);


            //Initialize
            UserRoleAnalysis userRoleAnalysis = new UserRoleAnalysis(userListInput, Language.GERMAN);
            Vector<String> availableRoles = listFilesInDir("./data/roleClassifiers/", ".role");             
            TreeMap<String, Integer> roleDistribution = new TreeMap<String, Integer>();
            for (String rolelabel : availableRoles) {
                roleDistribution.put(rolelabel, 0); 
            }
            
            //Obtain the fole for each user
            int numAssignedRoles = 0;
            for (UserRole ur : userRoleAnalysis.getUserRoles()) {
                String userRole = ur.getRoleLabel();
                String date = ur.getDate().toString().substring(0, 10);
                System.out.println(ur.getUserID() + " is a " + userRole + " on " + date);
                numAssignedRoles++;

                //Get the role distribution
                for (String rolelabel : availableRoles) {
                    if (rolelabel.equalsIgnoreCase(userRole)) {
                        int current = roleDistribution.get(rolelabel);
                        roleDistribution.put(rolelabel, ++current);
                    }
                }
            }
            System.out.println("\n" + roleDistribution);
            System.out.println("\nAssigned roles to " + numAssignedRoles + " out of " + userListInput.size() + " total users");


        } catch (IllegalAccessException ex) {
            Logger.getLogger(testUserRoles.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    private static Vector<String> listFilesInDir(String DirPath, String Extension) {

        Vector<String> filenames = new Vector<String>();
        File dir = new File(DirPath);
        String[] children = dir.list();
        if (children == null || children.length == 0) {
            System.out.println(DirPath + " is empty");
        } else {
            for (int i = 0; i < children.length; i++) {
                String filename = children[i];
                if (filename.endsWith(Extension)) {
                    filenames.add(filename.replaceAll(Extension, ""));
                }
            }
        }
        return filenames;
    }

    public static String readStringFile(String path) {
        String result = "";
        try {
            String str = "";
            BufferedReader in = new BufferedReader(new FileReader(path));
            while ((str = in.readLine()) != null) {
                result += (" " + str);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }
}
