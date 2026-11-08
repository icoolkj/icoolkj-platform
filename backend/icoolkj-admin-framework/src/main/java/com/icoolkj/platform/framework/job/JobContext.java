package com.icoolkj.platform.framework.job;

/**
 * Execution context passed to {@link JobHandler}.
 * Handlers may put a result summary JSON via {@link #setResultSummary(String)}.
 */
public class JobContext {

    private final String jobCode;
    private final JobTriggerType triggerType;
    private final String triggerUserId;
    private String resultSummary;
    private String requestParam;

    public JobContext(String jobCode, JobTriggerType triggerType, String triggerUserId) {
        this.jobCode = jobCode;
        this.triggerType = triggerType;
        this.triggerUserId = triggerUserId;
    }

    public String getJobCode() {
        return jobCode;
    }

    public JobTriggerType getTriggerType() {
        return triggerType;
    }

    public String getTriggerUserId() {
        return triggerUserId;
    }

    public String getResultSummary() {
        return resultSummary;
    }

    public void setResultSummary(String resultSummary) {
        this.resultSummary = resultSummary;
    }

    public String getRequestParam() {
        return requestParam;
    }

    public void setRequestParam(String requestParam) {
        this.requestParam = requestParam;
    }
}
