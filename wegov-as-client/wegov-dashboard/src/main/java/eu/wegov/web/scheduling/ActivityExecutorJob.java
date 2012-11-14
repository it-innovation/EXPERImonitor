package eu.wegov.web.scheduling;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

import eu.wegov.context.ApplicationContextProvider;
import eu.wegov.coordinator.Activity;
import eu.wegov.coordinator.Coordinator;
import eu.wegov.helper.CoordinatorHelper;

/**
 * Implementation of a Quartz Job that will we able to execute a set of
 * Activities via the Coordinator.
 * 
 * @author Francesco Timperi Tiberi
 * 
 */

public class ActivityExecutorJob implements ACoordinatorJob < Activity >, Job {

    private static final Logger LOG = Logger.getLogger(ActivityExecutorJob.class);

    public void execute(final JobExecutionContext context) throws JobExecutionException {
    	System.out.println("ActivityExecutorJob.execute() ");
        final JobDetail detail = context.getJobDetail();
        final JobDataMap data = detail.getJobDataMap();
        final String[] ids = (String[]) data.get("IDS");

        try {
            for (final String id : ids) {
                LOG.debug("Retrieving Activity [" + id + "] for execution");
                final Activity act = retrieveWorker(Integer.parseInt(id));
                act.start();
                LOG.debug("Activity retrieved successfully we can execute it!!");
            }
        } catch (final Exception e) {
            //LOG.error("Error executing scheduled Activities in job [" + e.toString());
        	e.printStackTrace(System.out);
            throw new JobExecutionException(e);
        }
    }

    public Activity retrieveWorker(final Integer ID) throws Exception {
        return getCoordinator().getActivityByID(ID.intValue());
    }

    public Coordinator getCoordinator() throws Exception {
    	ApplicationContext context = ApplicationContextProvider.getApplicationContext();
    	if (context != null) {
	        final CoordinatorHelper helper = (CoordinatorHelper) context.getBean("coordinatorHelper");
	        return helper.getCoordinator();
    	}
    	else {
    		CoordinatorHelper helper = new CoordinatorHelper();
    		return helper.getStaticCoordinator();
    	}
    }

}
