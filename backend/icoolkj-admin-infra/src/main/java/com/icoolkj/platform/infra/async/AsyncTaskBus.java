package com.icoolkj.platform.infra.async;

/**
 * Bus that accepts a persisted task id for asynchronous execution.
 * {@code local} runs in-process; {@code redis-stream} publishes for the worker.
 */
public interface AsyncTaskBus {

    void publish(String taskId);
}
