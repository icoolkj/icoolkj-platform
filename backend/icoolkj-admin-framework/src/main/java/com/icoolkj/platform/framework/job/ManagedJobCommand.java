package com.icoolkj.platform.framework.job;

/**
 * Redis / runner command for a managed job.
 *
 * @param jobCode         catalog / sys_job.job_code
 * @param triggerType     CRON or MANUAL
 * @param triggerUserId   login user for MANUAL; null for CRON
 */
public record ManagedJobCommand(
        String jobCode,
        JobTriggerType triggerType,
        String triggerUserId
) {
}
