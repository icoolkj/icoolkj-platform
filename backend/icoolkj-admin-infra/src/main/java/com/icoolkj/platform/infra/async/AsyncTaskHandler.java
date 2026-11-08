package com.icoolkj.platform.infra.async;

/**
 * Business handler for a {@code task_type}. Implementations are Spring beans collected by
 * {@link AsyncTaskRegistry}.
 */
public interface AsyncTaskHandler {

    String taskType();

    /**
     * Runs the business work. Return JSON payload for {@code result_data} (may be null).
     * Throw to mark the task failed.
     */
    String handle(AsyncTaskContext context) throws Exception;
}
