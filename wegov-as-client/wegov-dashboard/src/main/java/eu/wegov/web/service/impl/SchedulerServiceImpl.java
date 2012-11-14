package eu.wegov.web.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import eu.wegov.converter.SchedulerConverter;
import eu.wegov.coordinator.Activity;
import eu.wegov.coordinator.Worksheet;
import eu.wegov.helper.CoordinatorHelper;
import eu.wegov.web.service.SchedulerService;
import eu.wegov.web.vo.coordinator.SchedulerConfigView;
import eu.wegov.web.vo.scheduler.JobView;

@Service
public class SchedulerServiceImpl implements SchedulerService {

    private static final Logger LOG = Logger.getLogger(SchedulerServiceImpl.class);

    @Autowired
    private transient CoordinatorHelper helper;

    @Autowired
    private transient SchedulerFactoryBean schedulerFactory;

    //Used for testing
    public void setHelper(CoordinatorHelper helper) {
    	this.helper = helper;
    }
    
    //Used for testing
    public void setSchedulerFactory(SchedulerFactoryBean schedulerFactory) throws Exception {
    	this.schedulerFactory = schedulerFactory;
    	getCheduler();
    }
    
    protected Scheduler getCheduler() throws Exception {
        LOG.debug("Retrieving a Scheduler instance.....");
        final Scheduler theS = schedulerFactory.getScheduler();
        //Use following only for development where SchedulerFactoryBean is not available
        //Scheduler theS = StdSchedulerFactory.getDefaultScheduler();
        LOG.debug("Scheduler = " + theS);
        
        if (theS == null)
        	throw new Exception("Scheduler factory returned null scheduler");
        
        if (!theS.isStarted()) {
        	LOG.debug("Starting scheduler");
            theS.start();
        }
        else {
        	LOG.debug("Scheduler already started");
        }
        return theS;
    }

    /**
     * Schedule the execution of the given Worksheet using the assigned
     * Scheduling policy.
     */
    public void scheduleWorksheet(final Integer worksheetId, final SchedulerConfigView s) throws Exception {
        LOG.info("Adding scheduling policy for Worksheet [" + worksheetId + "]");
        final Scheduler sch = getCheduler();
        final Worksheet wk = helper.getCoordinator().getWorksheetByID(worksheetId);

        final long millis = System.currentTimeMillis();
        final JobDetail jd = SchedulerConverter.createWorksheetJobDetail(worksheetId, "worksheetJob_"
                + worksheetId
                + "_"
                + millis, "worksheet_job_" + worksheetId, wk.getName());
        final Trigger t = SchedulerConverter.createWorksheetTriggering("wktrigger_" + worksheetId + "_" + millis,
            "worksheet_trigger_" + worksheetId, s);

        sch.scheduleJob(jd, t);
        LOG.info("Worksheet [" + worksheetId + "] scheduled for [" + t.toString() + "]");
    }

    public String getJobGroup(final Integer worksheetId) {
    	return "worksheet_" + worksheetId;
    }
    
    private String getTriggerGroup(final Integer worksheetId) {
    	return "worksheet_trigger_" + worksheetId;
    }
    
    private String getActivityJobPrefix(String activityIdStr) {
    	return "activity_" + activityIdStr;
    }
    
    private String getActivityTriggerPrefix(String activityIdStr) {
    	return "act_trigger_" + activityIdStr;
    }
    
    public void scheduleActivitiesSubset(final Integer worksheetId, final SchedulerConfigView s, final String[] ids)
            throws Exception {
        LOG.info("Scheduling worksheet [" + worksheetId + "], activity " + ids[0]);
        final Scheduler sch = getCheduler();
        final long millis = System.currentTimeMillis();

        //final String jobName = "activitiesJob_" + worksheetId + "_" + millis;
        //final String jobGroup = "worksheet_job_" + worksheetId;
        //final String jobName = "activity_" + ids[0] + "_" + millis;
        
        //final String jobGroup = "worksheet_" + worksheetId;
        final String jobName = getActivityJobPrefix(ids[0]) + "_" + millis;
        final String jobGroup = getJobGroup(worksheetId);
        
        final JobDetail jd = SchedulerConverter.createActivityJobDetail(ids, jobName, jobGroup, buildActivitiesNames(ids));

        //final String triggerName = "actrigger_" + worksheetId + "_" + millis;
        //final String triggerGroup = "worksheet_trigger_" + worksheetId;
        //final String triggerName = "act_trigger_" + ids[0] + "_" + millis;
        //final String triggerGroup = "worksheet_trigger_" + worksheetId;
        final String triggerName = getActivityTriggerPrefix(ids[0]) + "_" + millis;
        final String triggerGroup = getTriggerGroup(worksheetId);
        
        final Trigger t = SchedulerConverter.createWorksheetTriggering(triggerName, triggerGroup, s);

        sch.scheduleJob(jd, t);
    }

    public void executeWorksheet(final Integer worksheetId) throws Exception {
        LOG.info("Adding immediate scheduling policy for Worksheet [" + worksheetId + "]");
        final Scheduler sch = getCheduler();
        final Worksheet wk = helper.getCoordinator().getWorksheetByID(worksheetId);

        final long millis = System.currentTimeMillis();
        final JobDetail jd = SchedulerConverter.createWorksheetJobDetail(worksheetId, "worksheetJob_"
                + worksheetId
                + "_"
                + millis, "worksheet_job_" + worksheetId, wk.getName());
        final Trigger t = SchedulerConverter.startNowTrigger("wktrigger_" + worksheetId + "_" + millis, "worksheet_trigger_"
                + worksheetId);

        sch.scheduleJob(jd, t);
        LOG.info("Worksheet [" + worksheetId + "] scheduled for immediate Execution");

    }

    public void executeActivitiesSubset(final Integer worksheetId, final String[] ids) throws Exception {
        LOG.info("Adding immediate scheduling policy for activities in Worksheet [" + worksheetId + "]");
        System.out.println("Adding immediate scheduling policy for activities in Worksheet [" + worksheetId + "]");
        final Scheduler sch = getCheduler();
        final long millis = System.currentTimeMillis();

        final String jobName = getActivityJobPrefix(ids[0]) + "_" + millis;
        final String jobGroup = getJobGroup(worksheetId);
        
        final JobDetail jd = SchedulerConverter.createActivityJobDetail(ids, jobName, jobGroup, buildActivitiesNames(ids));

        final String triggerName = getActivityTriggerPrefix(ids[0]) + "_" + millis;
        final String triggerGroup = getTriggerGroup(worksheetId);
        
        final Trigger t = SchedulerConverter.startNowTrigger(triggerName, triggerGroup);

        sch.scheduleJob(jd, t);
    }

    @SuppressWarnings("unchecked")
    public ArrayList < JobView > getCurrentSchedulerView4Worksheet(final Integer worksheetId) throws Exception {
        final ArrayList < JobView > view = new ArrayList < JobView >();

        final Scheduler sch = getCheduler();
        
        System.out.println("\nGetting scheduled jobs for worksheet " + worksheetId);
        final Set < JobKey > keys = sch.getJobKeys(GroupMatcher.groupEquals(getJobGroup(worksheetId)));

        System.out.println("Number of jobs: " + keys.size());
        
        for (final JobKey k : keys) {
            final JobDetail jd = sch.getJobDetail(k);
            List<? extends Trigger> triggers = sch.getTriggersOfJob(k);
            view.add(SchedulerConverter.convertToJobView(k, jd, triggers, sch));
        }
        return view;
    }


    public void pauseJob(final String name, final String group) throws Exception {
        final Scheduler sch = getCheduler();
        sch.pauseJob(new JobKey(name, group));
    }

    public void resumeJob(final String name, final String group) throws Exception {
        final Scheduler sch = getCheduler();
        sch.resumeJob(new JobKey(name, group));
    }

    public boolean removeJob(final String name, final String group) throws Exception {
        final Scheduler sch = getCheduler();
        return sch.deleteJob(new JobKey(name, group));
    }

    /**
     * Concatenate the activity names.
     * 
     * @param ids
     * @return
     * @throws Exception
     */
    private String buildActivitiesNames(final String[] ids) throws Exception {
        final StringBuffer sb = new StringBuffer();
        int i = 0;

        for (final String id : ids) {
            final Activity act = helper.getCoordinator().getActivityByID(Integer.parseInt(id));
            sb.append(act.getName());
            i++;
            if (i < ids.length) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }

	public Timestamp getNextStartDateForActivity(Integer worksheetId, Integer activityId) throws Exception {
		Date startDate = null;
		
        LOG.debug("\nGetting scheduled jobs for activity " + activityId + " in worksheet " + worksheetId);
 
        final Scheduler sch = getCheduler();
        
        final String triggerGroup = "worksheet_trigger_" + worksheetId;
        LOG.debug("\nGetting triggers for group " + triggerGroup);
        
        Set<TriggerKey> keys = sch.getTriggerKeys(GroupMatcher.groupEquals(triggerGroup));
        LOG.debug("Number of jobs: " + keys.size());
        
        //final String triggerPrefix = "act_trigger_" + activityId;
        final String triggerPrefix = getActivityTriggerPrefix(activityId.toString());
        LOG.debug("Searching for trigger prefix: " + triggerPrefix);
        

        for (final TriggerKey key : keys) {
        	String name = key.getName();
        	boolean match = name.startsWith(triggerPrefix);
        	LOG.debug(name + ": " + match);
        	if (match) {
        		Trigger trigger = sch.getTrigger(key);
        		Date nextFireTime = trigger.getNextFireTime();
        		startDate = nextFireTime;
        	}
        }
        
        if (startDate != null) {
        	Timestamp timestamp = new Timestamp(startDate.getTime());
        	LOG.debug("Next start time for activity " + activityId + " is " + timestamp);
        	return timestamp;
        }
        else {
        	LOG.debug("No triggers found for activity " + activityId);
        	return null;
        }
	}

}
