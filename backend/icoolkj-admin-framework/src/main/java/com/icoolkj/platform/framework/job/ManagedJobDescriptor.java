package com.icoolkj.platform.framework.job;

/**
 * Catalog entry for a worker-managed job.
 * Display name is resolved from gen i18n {@code managedJob.keys.{jobId}} on the client
 * (or MessageSource {@code gen.managedJob.keys.{jobId}} on the server) — do not embed zh/en here.
 */
public record ManagedJobDescriptor(
        String jobId,
        String cronHint,
        String assignScope
) {
}
