package com.icoolkj.platform.infra.async;

/**
 * Worker-side consumer for async tasks (Redis Stream when {@code provider=redis-stream}).
 */
public interface AsyncTaskConsumer {

    /** Poll and process up to a batch of messages. */
    int poll();
}
