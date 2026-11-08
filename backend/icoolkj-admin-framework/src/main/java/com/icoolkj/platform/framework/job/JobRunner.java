package com.icoolkj.platform.framework.job;

/**
 * Runs a catalog job: gate by sys_job.status, write sys_job_log, invoke {@link JobHandler}.
 */
public interface JobRunner {

    /**
     * Run if the job exists, is enabled, and concurrency policy allows.
     *
     * @return true if execution started (or completed); false if skipped (paused / concurrent)
     */
    boolean run(ManagedJobCommand command);
}
