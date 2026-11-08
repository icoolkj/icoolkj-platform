package com.icoolkj.platform.framework.job;

import java.util.List;

/** Static catalog of platform job codes (bound to JobHandler + sys_job.job_code). */
public final class ManagedJobCatalog {

    public static final String AUDIT_LOG_RETENTION = "audit-log-retention";
    public static final String DATABASE_BACKUP = "database-backup";
    public static final String INFRA_FILE_GC = "infra-file-gc";
    public static final String ASYNC_TASK_PENDING_RECONCILE = "async-task-pending-reconcile";

    private ManagedJobCatalog() {
    }

    public static List<ManagedJobDescriptor> all() {
        return List.of(
                new ManagedJobDescriptor(
                        AUDIT_LOG_RETENTION,
                        "sys_job.cron_expression",
                        "PLATFORM"),
                new ManagedJobDescriptor(
                        DATABASE_BACKUP,
                        "sys_job.cron_expression",
                        "PLATFORM"),
                new ManagedJobDescriptor(
                        INFRA_FILE_GC,
                        "sys_job.cron_expression",
                        "PLATFORM"),
                new ManagedJobDescriptor(
                        ASYNC_TASK_PENDING_RECONCILE,
                        "sys_job.cron_expression",
                        "PLATFORM")
        );
    }

    public static boolean isKnown(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            return false;
        }
        return all().stream().anyMatch(j -> j.jobId().equals(jobId.trim()));
    }
}
