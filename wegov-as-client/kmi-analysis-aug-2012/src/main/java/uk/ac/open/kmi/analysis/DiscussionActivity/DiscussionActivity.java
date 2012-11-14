package uk.ac.open.kmi.analysis.DiscussionActivity;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import uk.ac.open.kmi.analysis.core.Post;

/**
 * This class is used to --measure different discussion rates --return the top k
 * most active users (based on the number of posts they generate) --return the
 * top k most replied posts given a random set of posts.
 *
 * @author sa5298
 *
 */
public class DiscussionActivity {

    private DiscussionActivityInput input;
    private double[] discussionRate;
    private String step;
    private HashMap<String, Integer> mostRepliedPosts;
    private HashMap<String, Integer> mostActiveUsers;

    /**
     * Sets the DiscussionActivityInput to the Discussion activity, and calls
     * all the calculation functions.
     *
     * @param DiscussionActivityInput input
     */
    public void setDiscussionActivityInput(DiscussionActivityInput input) {
        this.input = input;
        this.input.calculate();
        this.mostActiveUsers = new HashMap<String, Integer>();
        this.mostRepliedPosts = new HashMap<String, Integer>();

        this.discussionRate = Measure(input.getStart(), input.getEnd(), input.getSTEP());
    }

    /**
     * Measures the rate of discussion within the specified window (startTime,
     * endTime) and returns discrete values after periods of length equivalent
     * to STEP = {YEAR, MONTH, WEEK, DAY, HOUR, MINUTE, SECOND}.
     *
     * @param startTime
     * @param endTime
     * @param STEP
     * @return Array A of discussion rates at each STEP. A[0]=
     * getDiscussionRatePerSTEP(seedID, startTime, startTime+STEP) A[1]=
     * getDiscussionRatePerSTEP(seedID, startTime+STEP, startTime+2STEP) ...
     */
    private double[] Measure(Timestamp startTime, Timestamp endTime, String STEP) {
        this.step = STEP;
        Vector<Post> tmpDisc = input.getEnrichedPosts();
        Vector<Post> done = new Vector<Post>();

        long start = startTime.getTime();
        long diff = getDiff(startTime, endTime, STEP) + 1;

        double[] windows = new double[Integer.parseInt("" + diff)];
        int i = 1;
        while (i <= diff) {
            long stepMillis = getStepMillis(STEP);
            // prime the window
            windows[i - 1] = 0;
            // get the close of the window
            long close = start + stepMillis;
            for (Post p : tmpDisc) {
                if (!done.contains(p)) {
                    //	System.out.println(postItem.getSnspostID() + " : "+ postItem.getDateCreated()+ " : "+ postItem.getDateCreated().getTime());
                    Timestamp t = (Timestamp) p.getDateCreated();
                    if ((t.getTime() >= start) && (t.getTime() < close)) {
                        windows[i - 1] += 1;
                        done.add(p);
                        mostRepliedPosts(p);
                        mostActiveUsers(p);
                    } else {
                        break;
                    }
                }
            }
            i++;
            start = close;
        }
        return windows;

    }

    private long getStepMillis(String STEP) {
        if (STEP == "SECOND") {
            return 1000;
        }
        if (STEP == "MINUTE") {
            return (1000 * 60);
        }
        if (STEP == "HOUR") {
            return (1000 * 60 * 60);
        }
        if (STEP == "DAY") {
            return (1000 * 60 * 60 * 24);
        }
        if (STEP == "WEEK") {
            return (1000 * 60 * 60 * 24 * 7);
        }
        if (STEP == "MONTH") {
            return (1000 * 60 * 60 * 24 * 30);
        } else //if (STEP=="YEAR")
        {
            return (1000 * 60 * 60 * 24 * 365);
        }


    }

    public double[] getDiscussionRate() {
        return discussionRate;
    }

    private long getDiff(Timestamp startTime, Timestamp endTime, String STEP) {

        long earliest = startTime.getTime();
        long latest = endTime.getTime();

        long diff = latest - earliest;

        if (STEP == "SECOND") {
            return diff / 1000;
        }
        if (STEP == "MINUTE") {
            return diff / (1000 * 60);
        }
        if (STEP == "HOUR") {
            return diff / (1000 * 60 * 60);
        }
        if (STEP == "DAY") {
            return diff / (1000 * 60 * 60 * 24);
        }
        if (STEP == "WEEK") {
            return diff / (1000 * 60 * 60 * 24 * 7);
        }
        if (STEP == "MONTH") {
            return diff / (1000 * 60 * 60 * 24 * 30);
        } else //if (STEP=="YEAR")
        {
            return diff / (1000 * 60 * 60 * 24 * 365);
        }
    }

    private void mostActiveUsers(Post p) {
        Integer pCount = this.mostActiveUsers.get(p.getAuthorID());
        if (pCount == null) {
            this.mostActiveUsers.put(p.getAuthorID(), 1);
        } else {
            int pCountPlus = ++pCount;
            this.mostActiveUsers.put(p.getAuthorID(), pCountPlus);
        }
    }

    private void mostRepliedPosts(Post p) {
        if (p.getInReplyToID() == null || p.getInReplyToID() == "")
			; else {
            Integer pCount = this.mostRepliedPosts.get(p.getInReplyToID());
            if (pCount == null) {
                this.mostRepliedPosts.put(p.getInReplyToID(), 1);
            } else {
                int pCountPlus = ++pCount;
                this.mostRepliedPosts.put(p.getInReplyToID(), pCountPlus);
            }
        }
    }

    public String getStep() {
        return step;
    }

    /**
     * Returns the top k active users
     *
     * @param k
     * @return
     */
    public Vector<Entry<String, Integer>> getTopKMostActiveUsr(int k) {
        Vector<Entry<String, Integer>> sortedEntries = new Vector<Entry<String, Integer>>();

        int i = 0;
        for (Entry<String, Integer> entry : entriesSortedByValues(this.mostActiveUsers)) {
            if (i < k) {
                sortedEntries.add(entry);
                i++;
            }
        }
        return sortedEntries;
    }

    /**
     * Returns the top k replied posts
     *
     * @param k
     * @return
     */
    public Vector<Entry<String, Integer>> getTopKMostRepliedPosts(int k) {

        Vector<Entry<String, Integer>> sortedEntries = new Vector<Entry<String, Integer>>();

        int i = 0;
        for (Entry<String, Integer> entry : entriesSortedByValues(this.mostRepliedPosts)) {
            if (i < k) {
                sortedEntries.add(entry);
                i++;
            }
        }
        return sortedEntries;

    }

    private static <String, Integer extends Comparable<? super Integer>> SortedSet<Map.Entry<String, Integer>> entriesSortedByValues(Map<String, Integer> map) {
        SortedSet<Map.Entry<String, Integer>> sortedEntries = new TreeSet<Map.Entry<String, Integer>>(
                new Comparator<Map.Entry<String, Integer>>() {

                    @Override
                    public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
                        int res = e2.getValue().compareTo(e1.getValue());
                        return res != 0 ? res : 1;
                    }
                });
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }
}