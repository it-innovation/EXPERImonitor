package eu.wegov.web.service;

import java.sql.Timestamp;
import java.util.ArrayList;

import eu.wegov.web.vo.coordinator.SchedulerConfigView;
import eu.wegov.web.vo.scheduler.JobView;

public interface SchedulerService {

    void scheduleWorksheet(Integer worksheetId, SchedulerConfigView s) throws Exception;

    void scheduleActivitiesSubset(Integer worksheetId, SchedulerConfigView s, String[] ids) throws Exception;

    void executeWorksheet(Integer worksheetId) throws Exception;

    void executeActivitiesSubset(Integer worksheetId, String[] ids) throws Exception;

    ArrayList < JobView > getCurrentSchedulerView4Worksheet(final Integer worksheetId) throws Exception;

    void pauseJob(String name, String group) throws Exception;

    void resumeJob(String name, String group) throws Exception;

    boolean removeJob(String name, String group) throws Exception;

	Timestamp getNextStartDateForActivity(Integer worksheetId, Integer activityId) throws Exception;
	
	String getJobGroup(final Integer worksheetId);

}
