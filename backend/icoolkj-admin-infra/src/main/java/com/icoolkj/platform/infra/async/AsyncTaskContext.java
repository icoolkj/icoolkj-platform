package com.icoolkj.platform.infra.async;

/**
 * Execution context passed to {@link AsyncTaskHandler}.
 *
 * @param taskId       task primary key
 * @param taskType     handler key
 * @param requestParam opaque JSON / text from submit
 * @param bizType      optional business type
 * @param bizId        optional business id
 */
public record AsyncTaskContext(
        String taskId,
        String taskType,
        String requestParam,
        String bizType,
        String bizId) {
}
