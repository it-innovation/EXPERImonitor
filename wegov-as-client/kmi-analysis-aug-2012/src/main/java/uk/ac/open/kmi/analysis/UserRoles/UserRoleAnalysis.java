package uk.ac.open.kmi.analysis.UserRoles;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Vector;
import uk.ac.open.kmi.analysis.core.Language;

/**
 * IMPORTANT!!!! the feature name in the constructor MUST match the method of UserFeatures. e.g., getOutdegree - Outdegree.
 * This is important in the reflection of the methods.
 * @return
 */

public class UserRoleAnalysis {
	private Vector<UserFeatures> users;
	private Vector<UserRole> userRoles;
	private Vector<RoleClassifier> roleClassifiers;
        private String language;

	public UserRoleAnalysis(Vector<UserFeatures> usIn, String language) {
		this.users = usIn;
                this.language = language;
                if(this.language!=Language.ENGLISH && this.language!= Language.GERMAN){
                    throw new IllegalArgumentException("only two languages allowed: " + 
                    Language.ENGLISH + " or " + Language.GERMAN);
                }
		process();
	}

	private void process() {
		loadCassifiers();
		userRoles = new Vector<UserRole>();
		for(UserFeatures user: users){
			
			UserRole userRole;
			try {
				userRole = calculateUserRole(user);
				if(userRole.getRoleLabel()!="")
					userRoles.add(userRole);	
				else{
					//System.out.println(user.getUserID()+": "+user.getPostRate()+": "+user.getOutInRatio());
				}

			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	private void loadCassifiers() {
		roleClassifiers = new Vector<RoleClassifier>();
			for(String file: listFilesInDir("./data/roleClassifiers/", ".role")){
				RoleClassifier cl = new RoleClassifier("./data/roleClassifiers/"+file+".role");
				roleClassifiers.add(cl);
			}
	}


	private UserRole calculateUserRole(UserFeatures user) throws NumberFormatException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		UserRole userRole = new UserRole(user);
		String label="";
		for(RoleClassifier roleClass: roleClassifiers)
			if(userHasRole(user,roleClass)){
				label=roleClass.getLabel();
				break;
			}
			else{;}
		userRole.setRoleLabel(label);
		
	    java.util.Date today = new java.util.Date();
	    Timestamp date = new java.sql.Timestamp(today.getTime());
		userRole.setDate(date);
		
		return userRole;
	}

	private static boolean userHasRole(UserFeatures user, RoleClassifier cl) throws NumberFormatException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		int sum=0;
		for(Feature f:cl.getFeatures()){
			String fname = f.getName();
			double min = f.getMinValue();
			double max = f.getMaxValue();
			double UserFeatValue = Double.valueOf(callMethod(fname, user));
			if(UserFeatValue>=min && UserFeatValue<=max)
				sum++;	
			else break;
		}
		if(sum==cl.getFeatures().size())
			return true;
		else return false;
	}
	
	private static String  callMethod(String string, UserFeatures ui) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		   String result = "";
		   Class userimpact = UserFeatures.class;
		   Method[] methods = userimpact.getDeclaredMethods();
		   for(Method m:methods){
			   if(m.getName().indexOf("get")>-1 && m.getName().indexOf(string)>-1){
				   Object o = m.invoke(ui, null);
				   if(o!=null){
					   String value = o.toString();
					   if(value!=null && value.length()>0){
						   result=value;
					   }
				   }
			   }
		   }
		   return result;
	   }

	public Vector<UserRole> getUserRoles() {
		return userRoles;
	}
	
	private static Vector<String> listFilesInDir(String DirPath, String Extension) {

		Vector<String> filenames = new Vector<String>();
		File dir = new File(DirPath);    
		String[] children = dir.list();
	    if (children == null || children.length==0) {
	    	System.out.println(DirPath+" is empty");
	    } else {
	        for (int i=0; i<children.length; i++) {
	           String filename = children[i];
	           if(filename.endsWith(Extension))
	        	   filenames.add(filename.replaceAll(Extension,""));
	        }
	    }
	    return filenames;


	}


}
