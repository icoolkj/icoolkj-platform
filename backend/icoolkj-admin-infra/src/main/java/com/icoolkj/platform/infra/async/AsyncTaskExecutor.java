package com.icoolkj.platform.infra.async;

/** Executes one async task (load → run handler → persist status/result). */
public interface AsyncTaskExecutor {

    void execute(String taskId);
}
