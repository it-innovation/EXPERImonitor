package uk.ac.open.kmi.analysis.DiscussionActivity;

import java.util.Comparator;

import uk.ac.open.kmi.analysis.core.Post;

public class PostTimeComparator implements Comparator<Post>{

		public int compare(Post p1, Post p2){
			Long p1t = p1.getDateCreated().getTime();
			Long p2t = p2.getDateCreated().getTime();
			return p1t.compareTo(p2t);
		}
}
