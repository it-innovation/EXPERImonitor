package eu.wegov.converter;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.CalendarIntervalScheduleBuilder.calendarIntervalSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import org.quartz.CalendarIntervalScheduleBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;

//import eu.wegov.web.form.wizard.SchedulerFormBean;
import eu.wegov.web.scheduling.ActivityExecutorJob;
import eu.wegov.web.scheduling.WorksheetExecutorJob;
import eu.wegov.web.util.ApplicationConstants;
import eu.wegov.web.util.ApplicationUtils;
import eu.wegov.web.vo.coordinator.SchedulerConfigView;
import eu.wegov.web.vo.scheduler.JobView;
import eu.wegov.web.vo.scheduler.TriggerView;

/**
 * Convenience class providing Method for Quartz Scheduling
 * 
 * @author Francesco Timperi Tiberi
 * 
 */
public final class SchedulerConverter {

    private SchedulerConverter() {

    }

    private static CalendarIntervalScheduleBuilder secondlyCalendarBuilder(final int value) {
        return calendarIntervalSchedule().withIntervalInSeconds(value);
    }

    private static CalendarIntervalScheduleBuilder minutelyCalendarBuilder(final int value) {
        return calendarIntervalSchedule().withIntervalInMinutes(value);
    }

    private static CalendarIntervalScheduleBuilder hourlyCalendarBuilder(final int value) {
        return calendarIntervalSchedule().withIntervalInHours(value);
    }

    private static CalendarIntervalScheduleBuilder dailyCalendarBuilder(final int value) {
        return calendarIntervalSchedule().withIntervalInDays(value);
    }

    private static CalendarIntervalScheduleBuilder weeklyCalendarBuilder(final int value) {
        return calendarIntervalSchedule().withIntervalInWeeks(value);
    }

    private static CalendarIntervalScheduleBuilder monthlyCalendarBuilder(final int value) {
        return calendarIntervalSchedule().withIntervalInMonths(value);
    }

    private static CalendarIntervalScheduleBuilder yearlyCalendarBuilder(final int value) {
        return calendarIntervalSchedule().withIntervalInYears(value);
    }

    /**
     * Create a Trigger with immediate execution.
     * 
     * @param name
     * @param group
     * @return
     */
    public static Trigger startNowTrigger(final String name, final String group) {
        return newTrigger().withIdentity(name, group).startNow().build();
    }

    /**
     * Create a suitable Quartz Trigger Object using the passed Scheduler
     * configuration as policy scheduling.
     * 
     * @param name
     * @param group
     * @param s
     * @return
     */
    public static Trigger createWorksheetTriggering(final String name, final String group, final SchedulerConfigView s) {
        if (ApplicationConstants.SCHEDULER_START_NOW.equalsIgnoreCase(s.getStartWhen())) {
            return startNowTrigger(name, group);
        }

        final TriggerBuilder < Trigger > tb = newTrigger().withIdentity(name, group).withDescription(
            "trigger for [" + group + "," + name + "]");

        if (s.getStartWhenDate() != null) {
            tb.startAt(s.getStartWhenDate());
        }

        if (null != s.getRepeatUntilDate()) {
            tb.endAt(s.getRepeatUntilDate());
        }

        if (s.isRepeat()) {
            //CalendarIntervalScheduleBuilder ci = null;
            SimpleScheduleBuilder schedule = simpleSchedule();
            
            /* If the execution should be completed within a given date */
            if (ApplicationConstants.SCHEDULER_REPEAT_EVERY_MINUTES.equalsIgnoreCase(s.getRepeatEveryCriteria())) {
                //ci = minutelyCalendarBuilder(s.getRepeatEveryValue());
            	schedule.withIntervalInMinutes(s.getRepeatEveryValue());
            }

            if (ApplicationConstants.SCHEDULER_REPEAT_EVERY_HOUR.equalsIgnoreCase(s.getRepeatEveryCriteria())) {
                //ci = hourlyCalendarBuilder(s.getRepeatEveryValue());
            	schedule.withIntervalInHours(s.getRepeatEveryValue());
            }

            if (ApplicationConstants.SCHEDULER_REPEAT_EVERY_DAY.equalsIgnoreCase(s.getRepeatEveryCriteria())) {
                //ci = dailyCalendarBuilder(s.getRepeatEveryValue());
            	schedule.withIntervalInHours(s.getRepeatEveryValue() * 24);
            }

            if (ApplicationConstants.SCHEDULER_REPEAT_EVERY_WEEK.equalsIgnoreCase(s.getRepeatEveryCriteria())) {
                //ci = weeklyCalendarBuilder(s.getRepeatEveryValue());
            	schedule.withIntervalInHours(s.getRepeatEveryValue() * 24 * 7);
            }

            if (ApplicationConstants.SCHEDULER_REPEAT_EVERY_MONTH.equalsIgnoreCase(s.getRepeatEveryCriteria())) {
                //ci = monthlyCalendarBuilder(s.getRepeatEveryValue());
            	schedule.withIntervalInHours(s.getRepeatEveryValue() * 24 * 7 * 30);
            }

            //tb.withSchedule(ci);
            
            if (s.getRepeatCount() > 0) {
            	schedule.withRepeatCount(s.getRepeatCount());
            }
            else {
            	schedule.repeatForever();
            }
            
            tb.withSchedule(schedule);
            
        }

        Trigger trigger = tb.build();
        
        return trigger;
    }

    /**
     * Helper method to fetch data from SchedulerFormBean.
     * 
     * @param b SchedulerFormBean
     * @return SchedulerConfigView
     * @throws ParseException 
     */
    /*
    public static SchedulerConfigView schedulerForm2View(final SchedulerFormBean b) {
        final SchedulerConfigView s = new SchedulerConfigView();

        s.setRepeat(b.isRepeat());
        s.setRepeatEveryValue(b.getRepeatEveryValue());
        s.setRepeatEveryCriteria(b.getRepeatEveryCriteria());
        s.setRepeatUntil(b.isRepeatUntil());
        s.setRepeatUntilDate(ApplicationUtils.stringToDatetime(b.getRepeatUntilDate()));
        s.setStartWhen(b.getStartWhen());
        s.setStartWhenDate(ApplicationUtils.stringToDatetime(b.getStartWhenDate()));

        return s;

    }
    */

	public static SchedulerConfigView createSchedulerConfigView(final JSONObject scheduleParams) throws ParseException {
		SchedulerConfigView schedulerConfigView = new SchedulerConfigView();

		String scheduleOption = scheduleParams.getString("scheduleOption").trim();

		//String startTimeStr = scheduleParams.getString("startTime").trim();
		//String startDateStr = scheduleParams.getString("startDate").trim();
		//String stopTimeStr = scheduleParams.getString("stopTime").trim();
		//String stopDateStr = scheduleParams.getString("stopDate").trim();

		String startDateTimeStr = scheduleParams.getString("startDateTime").trim();
		String stopDateTimeStr = scheduleParams.getString("stopDateTime").trim();
		
		String timeIntervalStr = scheduleParams.getString("timeInterval").trim();
		String timeIntervalUnits = scheduleParams.getString("timeIntervalUnits").trim();
		String repeatCountStr = scheduleParams.getString("repeatCount").trim();
		
		String startWhen = null;
		Date startWhenDate = null;
		Date repeatUntilDate = null;
		boolean repeat = false;
		String repeatEveryCriteria = null; // minute, hour, day, week, month
		int repeatEveryValue = 0;
		int repeatCount = 0;
		//boolean repeatUntil = false;
		
		if (scheduleOption.equals("runNow")) {
			startWhen = ApplicationConstants.SCHEDULER_START_NOW;
		}
		else {
			startWhen = ApplicationConstants.SCHEDULER_START_DELAYED;
			
			//startWhenDate = ApplicationUtils.stringToDatetime(startDateStr + " " + startTimeStr);
			//repeatUntilDate = ApplicationUtils.stringToDatetime(stopDateStr + " " + stopTimeStr);
			
			//startWhenDate = getDateTime(startDateStr, startTimeStr);
			//repeatUntilDate = getDateTime(stopDateStr, stopTimeStr);
			if (! startDateTimeStr.equals("")) startWhenDate = ApplicationUtils.stringToDatetime(startDateTimeStr);
			if (! stopDateTimeStr.equals("")) repeatUntilDate = ApplicationUtils.stringToDatetime(stopDateTimeStr);
		}
		
		if (! timeIntervalStr.equals("")) {
			repeatEveryValue = Integer.parseInt(timeIntervalStr);
			if (timeIntervalUnits.equals("mins")) {
				repeatEveryCriteria = ApplicationConstants.SCHEDULER_REPEAT_EVERY_MINUTES;
			}
			else if (timeIntervalUnits.equals("hours")) {
				repeatEveryCriteria = ApplicationConstants.SCHEDULER_REPEAT_EVERY_HOUR;
			}
			else if (timeIntervalUnits.equals("days")) {
				repeatEveryCriteria = ApplicationConstants.SCHEDULER_REPEAT_EVERY_DAY;
			}
			else {
				throw new ParseException("Unsupported time interval units: " + timeIntervalStr, 0);
			}
			repeat = true;
		}
		
		if (! repeatCountStr.equals("")) {
			repeatCount = Integer.parseInt(repeatCountStr);
			repeat = true;
		}
		
		schedulerConfigView.setStartWhen(startWhen);
		schedulerConfigView.setStartWhenDate(startWhenDate);
		schedulerConfigView.setRepeatUntilDate(repeatUntilDate);
		schedulerConfigView.setRepeat(repeat);
		schedulerConfigView.setRepeatEveryCriteria(repeatEveryCriteria);
		schedulerConfigView.setRepeatEveryValue(repeatEveryValue);
		schedulerConfigView.setRepeatCount(repeatCount);
		//schedulerConfigView.setRepeatUntil(repeatUntil); //unused by SchedulerConverter
		
		return schedulerConfigView;
	}
	
    /**
     * Create a JobDetail to execute a Scheduled Worksheet.
     * 
     * @param ID
     * @param jobName
     * @param jobGroup
     * @return JobDetail
     */
    public static JobDetail createWorksheetJobDetail(final Integer id, final String jobName, final String jobGroup,
            final String worksheetName) {
        final JobDetail job = newJob(WorksheetExecutorJob.class).withIdentity(jobName, jobGroup)
            .withDescription("Execute Worksheet").build();
        job.getJobDataMap().put("ID", id);
        job.getJobDataMap().put("jobType", ApplicationConstants.SCHEDULER_WORKSHEET_JOB);
        job.getJobDataMap().put("objectName", worksheetName);
        return job;
    }

    /**
     * Create a JobDetail to execute a Scheduled Activity.
     * 
     * @param ID
     * @param jobName
     * @param jobGroup
     * @return JobDetail
     */
    public static JobDetail createActivityJobDetail(final String[] ids, final String jobName, final String jobGroup,
            final String activityName) {
    	String description = "Execute Activity " + ids[0]; // N.B. assume one activity for now
        final JobDetail job = newJob(ActivityExecutorJob.class).withIdentity(jobName, jobGroup)
            .withDescription(description).build();
        job.getJobDataMap().put("jobType", ApplicationConstants.SCHEDULER_ACIVITY_JOB);
        job.getJobDataMap().put("objectName", activityName);
        job.getJobDataMap().put("IDS", ids);
        return job;
    }

    /**
     * Convert Trigger information into a suitable object for the dashbaord.
     * 
     * @param t
     * @return TriggerView
     */
    public static TriggerView trigger2View(final Trigger t, final Scheduler sch) {
        final TriggerView v = new TriggerView();
        v.setCalendarName(t.getCalendarName());
        v.setKey(t.getKey());
        v.setDescription(t.getDescription());
        v.setEndTime(t.getEndTime());
        v.setNextFireTime(t.getNextFireTime());
        v.setPreviousFireTime(t.getPreviousFireTime());
        v.setStartTime(t.getStartTime());

        try {
        	TriggerState triggerState = sch.getTriggerState(t.getKey());
            switch (triggerState) {
                case NONE:
                    v.setStatus("none");
                    break;
                case NORMAL:
                    v.setStatus("normal");
                    break;
                case PAUSED:
                    v.setStatus("paused");
                    break;
                case COMPLETE:
                    v.setStatus("complete");
                    break;
                case ERROR:
                    v.setStatus("error");
                    break;
                case BLOCKED:
                    v.setStatus("blocked");
                    break;
                default:
                    v.setStatus("unknown");
                    break;
            }
            //sch.getTriggerState(t.getKey());
        } catch (final SchedulerException e) {
        	e.printStackTrace();
            v.setStatus("exception");
        }

        return v;
    }

    /**
     * Create a suitable view of a running Quartz Job.
     * 
     * @param key
     * @param detail
     * @param triggers
     * @return JobView
     */
    public static JobView convertToJobView(final JobKey key, final JobDetail detail, final List < ? extends Trigger > triggers,
            final Scheduler sch) {
        final JobView v = new JobView();
        v.setKey(key);
        v.setDescription(detail.getDescription());
        v.setClassname(detail.getClass().getCanonicalName());

        final JobDataMap jd = detail.getJobDataMap();
        v.setJobType(jd.getString("jobType"));
        v.setObjectName(jd.getString("objectName"));

        if (ApplicationConstants.SCHEDULER_ACIVITY_JOB.equalsIgnoreCase(v.getJobType())) {
        	String[] ids = (String[]) jd.get("IDS");
            v.setIds(ids);
            v.setId(ids[0]); // KEM: assume one activity
        } else {
            final Integer idInt = (Integer) jd.get("ID");
            String id = idInt.toString();
            v.setIds(new String[] { id });
            v.setId(id);
        }
        

        final ArrayList < TriggerView > tw = new ArrayList < TriggerView >();

        int i=0;
        for (final Trigger t : triggers) {
        	TriggerView tv = SchedulerConverter.trigger2View(t, sch);
            tw.add(tv);
        	if (i == 0) {
        		v.setEndTime(tv.getEndTime());
        		v.setNextFireTime(tv.getNextFireTime());
        		v.setPreviousFireTime(tv.getPreviousFireTime());
        		v.setStartTime(tv.getStartTime());
        		v.setStatus(tv.getStatus());
        	}
        	i++;
        }

        v.setTriggers(tw);
        
        System.out.println(detail.toString());
        System.out.println(v.toString());

        System.out.println("id: " + v.getId());
        System.out.println("startTime: " + v.getStartTime());
        System.out.println("endTime: " + v.getEndTime());
        System.out.println("nextFireTime: " + v.getNextFireTime());
        System.out.println("previousFireTime: " + v.getPreviousFireTime());
        System.out.println("status: " + v.getStatus());
        
        return v;
    }
}
