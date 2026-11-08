package com.icoolkj.platform.infra.async;

/** Status codes for {@code infra_async_task.task_status} (dict {@code infra_async_task_status}). */
public final class AsyncTaskStatus {

    public static final String PENDING = "0";
    public static final String RUNNING = "1";
    public static final String SUCCESS = "2";
    public static final String FAILED = "3";
    public static final String CANCELLED = "4";

    private AsyncTaskStatus() {
    }

    public static boolean isTerminal(String status) {
        return SUCCESS.equals(status) || FAILED.equals(status) || CANCELLED.equals(status);
    }
}
