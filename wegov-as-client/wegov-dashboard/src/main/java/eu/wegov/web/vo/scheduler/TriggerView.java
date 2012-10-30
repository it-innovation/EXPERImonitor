package eu.wegov.web.vo.scheduler;

import java.io.Serializable;
import java.util.Date;

import org.quartz.TriggerKey;

/**
 * Trivial representation of a firing Trigger.
 * 
 * @author Francesco Timperi Tiberi
 * 
 */
public class TriggerView implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3375196390544083999L;

    private TriggerKey key;
    private String description;
    private String calendarName;
    private Date startTime;
    private Date endTime;
    private Date nextFireTime;
    private Date previousFireTime;
    private String status;

    public TriggerKey getKey() {
        return key;
    }

    public void setKey(final TriggerKey key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getCalendarName() {
        return calendarName;
    }

    public void setCalendarName(final String calendarName) {
        this.calendarName = calendarName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(final Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(final Date endTime) {
        this.endTime = endTime;
    }

    public Date getNextFireTime() {
        return nextFireTime;
    }

    public void setNextFireTime(final Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public Date getPreviousFireTime() {
        return previousFireTime;
    }

    public void setPreviousFireTime(final Date previousFireTime) {
        this.previousFireTime = previousFireTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

}
