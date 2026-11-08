package com.icoolkj.platform.infra.async;

/** Optional publisher facade (local bus implements publish directly). */
public interface AsyncTaskPublisher {

    void publish(String taskId);
}
