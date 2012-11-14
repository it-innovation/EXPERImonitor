package uk.ac.open.kmi.analysis.DiscussionActivity;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Vector;

import uk.ac.open.kmi.analysis.core.Post;

/**
 * @author sa5298 This class represents the input to DiscussionActivity and
 * consists of: Vector<Post> inputPosts; String STEP; Timestamp start; Timestamp
 * end; It is necessary to pass at least a vector of (random) posts as input,
 * otherwise it will throw an illegal argument exception. start, end and STEP
 * are optional and if not set they are calculated by calculate().
 *
 * The method getParent(postID) should be replaced with the respective method ..  *
 *
 */
public class DiscussionActivityInput {

    private Vector<Post> inputPosts;
    private Vector<Post> enrichedPosts;
    private String    STEP;
    private Timestamp start;
    private Timestamp end;
    

    public DiscussionActivityInput() {
    }

    /**
     * Makes sure that the fields of DiscussionActivityInput are all complete
     * before it is used to calculate the discussion activity. 1. Checks that
     * the input posts are not null (otherwise it throws and exception) 2.
     * Created the enrichedPosts, which is the inputPosts and all their parents
     * (until we hit a post without a parent, i.e. seed) 3. Fills start, end
     * date and STEP if these haven't been specified by the user. -- Start is
     * the time of the earliest post -- End is the time of the latest post --
     * STEP is the maximum possible time period
     */
    protected void calculate() {
        if (this.inputPosts == null) {
            throw new java.lang.IllegalArgumentException();
        }

        enrichInputPosts();
        sortEnrichedPosts();

        System.out.println("------" + enrichedPosts.size());

        if (this.start == null) {
            this.start = this.enrichedPosts.firstElement().getDateCreated();
        }

        if (this.end == null) {
            this.end = this.enrichedPosts.lastElement().getDateCreated();
        }

        if (this.STEP == null) {
            this.STEP = getOptimalStep(start, end);
        }
    }

    /**
     * NOTE: WeGov collection does not enrichment
     * just leave this method here for future purposes
     */
    private void enrichInputPosts() {
        this.enrichedPosts = this.inputPosts;
    }


    private void sortEnrichedPosts() {
        Collections.sort(enrichedPosts, new PostTimeComparator());
    }

    private String getOptimalStep(Timestamp startTime, Timestamp endTime) {
        long earliest = startTime.getTime();
        long latest = endTime.getTime();

        // difference in milliseconds
        long diff = latest - earliest;
        long diffseconds = diff / 1000;
        long diffmins = diffseconds / 60;
        long diffhours = diffmins / 60;
        long diffdays = diffhours / 24;
        long diffweeks = diffdays / 7;
        long diffmonths = diffdays / 30;
        long diffyears = diffdays / 365;

        if (diffyears > 5) {
            return "YEAR";
        } else if (diffmonths > 5) {
            return "MONTH";
        } else if (diffweeks > 5) {
            return "WEEK";
        } else if (diffdays > 5) {
            return "DAY";
        } else if (diffhours > 5) {
            return "HOUR";
        } else if (diffmins > 5) {
            return "MINUTE";
        } else {
            return "SECOND";
        }
    }

    public void setInputPosts(Vector<Post> inputPosts) {
        this.inputPosts = inputPosts;
    }

    public String getSTEP() {
        return STEP;
    }

    /**
     * Options from "YEAR", "MONTH", "WEEK", "DAY", "HOUR", "MINUTE", "SECOND"
     *
     * @param STEP
     */
    public void setSTEP(String sTEP) {
        STEP = sTEP;
    }

    public Timestamp getStart() {
        return start;
    }

    public void setStart(Timestamp start) {
        this.start = start;
    }

    public Timestamp getEnd() {
        return end;
    }

    public void setEnd(Timestamp end) {
        this.end = end;
    }

    protected Vector<Post> getEnrichedPosts() {
        return enrichedPosts;
    }
}
