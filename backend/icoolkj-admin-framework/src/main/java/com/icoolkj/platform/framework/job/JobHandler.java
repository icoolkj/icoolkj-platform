package com.icoolkj.platform.framework.job;

/**
 * Code-registered platform job. Bound by {@link #jobCode()} to {@code sys_job.job_code}.
 * Do not load arbitrary class/method names from the database.
 */
public interface JobHandler {

    String jobCode();

    void execute(JobContext context) throws Exception;
}
