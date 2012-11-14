package eu.wegov.web.util;

public final class ApplicationConstants {

    private ApplicationConstants() {
    }

    // DB Constants
    public static final String DB_SCHEMA_DASHBOARD = "\"Wegov-Dashboard\"";
    public static final String DB_SCHEMA_RAW_DATA = "\"WeGovRawData\"";

    // Session constants
    public static final String SNS_FB = "SNS_FACEBOOK";
    public static final String TOPIC_OPINION_LIST = "TOPIC_OPINION_LIST";
    public static final String LOGGED_USER = "LOGGED_USER";
    public static final String OFFLINE_WORKFLOW_ID = "OFFLINE_WORKFLOW_ID";

    // JSP Page names
    public static final String JSP_NEW_POST = "newPost";
    public static final String JSP_VIEW_POST_LIST = "viewPostList";
    public static final String JSP_VIEW_LONG_TERMS = "viewLongTerms";

    public static final String NEW_WORKSHEET_FORM = "newWorksheet";
    public static final String NEW_ACTIVITY_FORM = "activityType";
    public static final String AVAILABLE_TOOLS_LIST = "availableTools";

    public static final String CURRENT_WORKSHEET = "currentWorksheet";
    public static final String SELECTED_WORKSHEETS = "selectedWorksheets";

    public static final String COORDINATOR_STATUS_FINISHED = "finished";
    public static final String COORDINATOR_STATUS_INITIALIZE = "initialising";

    public static final String COORDINATOR_TOOL_OUTPUT_TYPE = "outputOfType";

    public static final String SCHEDULER_ADD_ONLY = "addonly";
    public static final String SCHEDULER_START_NOW = "now";
    public static final String SCHEDULER_START_DELAYED = "delayed";

    public static final String SCHEDULER_REPEAT_EVERY_MINUTES = "minute";
    public static final String SCHEDULER_REPEAT_EVERY_HOUR = "hour";
    public static final String SCHEDULER_REPEAT_EVERY_DAY = "day";
    public static final String SCHEDULER_REPEAT_EVERY_WEEK = "week";
    public static final String SCHEDULER_REPEAT_EVERY_MONTH = "month";

    public static final String SCHEDULER_WORKSHEET_JOB = "worksheet";
    public static final String SCHEDULER_ACIVITY_JOB = "activity";
}
