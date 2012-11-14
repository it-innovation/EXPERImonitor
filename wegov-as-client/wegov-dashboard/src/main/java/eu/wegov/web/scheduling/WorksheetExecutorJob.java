package eu.wegov.web.scheduling;

import org.apache.log4j.Logger;
//import org.jfree.util.Log;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import eu.wegov.context.ApplicationContextProvider;
import eu.wegov.coordinator.Coordinator;
import eu.wegov.coordinator.Worksheet;
import eu.wegov.helper.CoordinatorHelper;

/**
 * Implementation of a Quartz Job that will we able to execute a Worksheet via
 * the Coordinator.
 * 
 * @author Francesco Timperi Tiberi
 * 
 */
public class WorksheetExecutorJob implements ACoordinatorJob < Worksheet >, Job {

    private static final Logger LOG = Logger.getLogger(WorksheetExecutorJob.class);

    public void execute(final JobExecutionContext context) throws JobExecutionException {
        final JobDataMap data = context.getJobDetail().getJobDataMap();

        final int id = data.getIntValue("ID");

        try {
            LOG.debug("***********************************************");
            LOG.debug("***********************************************");
            LOG.debug(" ");
            LOG.debug("Retrieving Worksheet [" + id + "] for execution");
            final Worksheet wk = retrieveWorker(id);
            wk.start();
            LOG.debug("Worksheet [" + id + "] started.");
            LOG.debug(" ");
            LOG.debug("***********************************************");
            LOG.debug("***********************************************");
        } catch (final Exception e) {
            LOG.error("Error executing scheduled Worksheet [" + id + "] " + e.toString());
        }
    }

    public Worksheet retrieveWorker(final Integer ID) throws Exception {
        return getCoordinator().getWorksheetByID(ID.intValue());
    }

    public Coordinator getCoordinator() throws Exception {
        final CoordinatorHelper helper = (CoordinatorHelper) ApplicationContextProvider.getApplicationContext().getBean(
            "coordinatorHelper");
        return helper.getCoordinator();
    }


}
