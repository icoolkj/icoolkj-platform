package com.icoolkj.platform.worker.async;

import com.icoolkj.platform.infra.async.AsyncTaskConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Polls Redis Stream async tasks (same pattern as {@code ManagedJobCommandConsumer}).
 */
@Component
@ConditionalOnBean(AsyncTaskConsumer.class)
public class AsyncTaskStreamPoller {

    private static final Logger log = LoggerFactory.getLogger(AsyncTaskStreamPoller.class);

    private final AsyncTaskConsumer asyncTaskConsumer;

    public AsyncTaskStreamPoller(AsyncTaskConsumer asyncTaskConsumer) {
        this.asyncTaskConsumer = asyncTaskConsumer;
    }

    @Scheduled(fixedDelayString = "${icoolkj.async.poll-ms:2000}")
    public void poll() {
        try {
            int n = asyncTaskConsumer.poll();
            if (n > 0) {
                log.debug("Processed {} async stream message(s)", n);
            }
        } catch (Exception ex) {
            log.warn("Async stream poll failed: {}", ex.getMessage());
        }
    }
}
