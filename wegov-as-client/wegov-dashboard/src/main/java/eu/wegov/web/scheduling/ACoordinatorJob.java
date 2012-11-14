package eu.wegov.web.scheduling;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import eu.wegov.coordinator.Coordinator;

public interface ACoordinatorJob < T > {

    T retrieveWorker(Integer ID) throws Exception;

    Coordinator getCoordinator() throws Exception;

	void execute(JobExecutionContext context) throws JobExecutionException;

}
