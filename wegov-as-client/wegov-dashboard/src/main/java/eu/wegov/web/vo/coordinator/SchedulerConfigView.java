package eu.wegov.web.vo.coordinator;

import java.io.Serializable;
import java.util.Date;

/**
 * Value object class containing scheduling Parameters.
 * 
 * @author Francesco Timperi Ti
 * 
 */
public class SchedulerConfigView implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4245245124358155221L;

    private String startWhen;
    private Date startWhenDate;
    private int repeatEveryValue;
    private String repeatEveryCriteria;
    private Date repeatUntilDate;

    private boolean repeat;
    private boolean repeatUntil;
    
    private int repeatCount;

	public String getStartWhen() {
        return startWhen;
    }

    public void setStartWhen(final String startWhen) {
        this.startWhen = startWhen;
    }

    public Date getStartWhenDate() {
        return startWhenDate;
    }

    public void setStartWhenDate(final Date startWhenDate) {
        this.startWhenDate = startWhenDate;
    }

    public Date getRepeatUntilDate() {
        return repeatUntilDate;
    }

    public void setRepeatUntilDate(final Date repeatUntilDate) {
        this.repeatUntilDate = repeatUntilDate;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(final boolean repeat) {
        this.repeat = repeat;
    }

    public boolean isRepeatUntil() {
        return repeatUntil;
    }

    public void setRepeatUntil(final boolean repeatUntil) {
        this.repeatUntil = repeatUntil;
    }

    public int getRepeatEveryValue() {
        return repeatEveryValue;
    }

    public void setRepeatEveryValue(final int repeatEveryValue) {
        this.repeatEveryValue = repeatEveryValue;
    }

    public String getRepeatEveryCriteria() {
        return repeatEveryCriteria;
    }

    public void setRepeatEveryCriteria(final String repeatEveryCriteria) {
        this.repeatEveryCriteria = repeatEveryCriteria;
    }

    public int getRepeatCount() {
		return repeatCount;
	}

	public void setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("startWhen: " + startWhen);
		sb.append("\nstartWhenDate: " + startWhenDate);
		sb.append("\nrepeatEveryValue: " + repeatEveryValue);
		sb.append("\nrepeatEveryCriteria: " + repeatEveryCriteria);
		sb.append("\nrepeatUntilDate: " + repeatUntilDate);
		sb.append("\nrepeat: " + repeat);
		sb.append("\nrepeatUntil: " + repeatUntil);
		sb.append("\nrepeatCount: " + repeatCount + "\n");
		
		return sb.toString();
	}

}
