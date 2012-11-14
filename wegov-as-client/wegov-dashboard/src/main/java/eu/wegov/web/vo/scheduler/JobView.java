package eu.wegov.web.vo.scheduler;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.quartz.JobKey;

/**
 * Class wrapping metadata recovered from a live Quartz Scheduler.
 * 
 * @author Francesco Timperi Tiberi
 * 
 */
public class JobView implements Serializable {

    public static final String STATUS_RUNNING = "running";
    public static final String STATUS_PAUSED = "paused";

    /**
     * 
     */
    private static final long serialVersionUID = -1388355887374222434L;

    private JobKey key;
    private String description;
    private String jobType;
    /**
     * Holds the name of the Worksheet/Activity on which the job is running.
     */
    private String objectName;
    private String[] ids;
    private String id;
	private String classname;

    private List < TriggerView > triggers;

    // Trigger fields
    private String calendarName;
    private Date startTime;
    private Date endTime;
    private Date nextFireTime;
    private Date previousFireTime;
    private String status;

    public String getCalendarName() {
		return calendarName;
	}

	public void setCalendarName(String calendarName) {
		this.calendarName = calendarName;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getNextFireTime() {
		return nextFireTime;
	}

	public void setNextFireTime(Date nextFireTime) {
		this.nextFireTime = nextFireTime;
	}

	public Date getPreviousFireTime() {
		return previousFireTime;
	}

	public void setPreviousFireTime(Date previousFireTime) {
		this.previousFireTime = previousFireTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public JobKey getKey() {
        return key;
    }

    public void setKey(final JobKey key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(final String jobType) {
        this.jobType = jobType;
    }

    public String[] getIds() {
        return ids;
    }

    public void setIds(final String[] ids) {
        this.ids = ids;
    }

    
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

    public String getClassname() {
        return classname;
    }

    public void setClassname(final String classname) {
        this.classname = classname;
    }

    public List < TriggerView > getTriggers() {
        return triggers;
    }

    public void setTriggers(final List < TriggerView > triggers) {
        this.triggers = triggers;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(final String objectName) {
        this.objectName = objectName;
    }

    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append(key +", "+ description +", "+ jobType +", "+ objectName +", "+ classname);
    	return sb.toString();
    }

}
