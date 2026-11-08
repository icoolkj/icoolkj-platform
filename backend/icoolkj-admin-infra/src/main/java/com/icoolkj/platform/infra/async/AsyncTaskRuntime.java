package com.icoolkj.platform.infra.async;

/**
 * Submits user-owned async tasks (insert {@code infra_async_task} + publish to bus).
 * Distinct from Gen CRUD {@link com.icoolkj.platform.infra.service.AsyncTaskService}.
 */
public interface AsyncTaskRuntime {

    /**
     * Creates a PENDING task with OwnershipColumns, then publishes for execution.
     *
     * @return task id
     */
    String submit(String taskType, String taskName, String bizType, String bizId, String requestParam);

    /**
     * Same as {@link #submit} but uses a caller-allocated id (e.g. Excel import binds files to {@code taskId} first).
     *
     * @return the same {@code taskId}
     */
    String submitWithId(String taskId,
                        String taskType,
                        String taskName,
                        String bizType,
                        String bizId,
                        String requestParam);
}
