package com.icoolkj.platform.infra.async.local;

import com.icoolkj.platform.framework.tenant.TenantContext;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.infra.async.AsyncProperties;
import com.icoolkj.platform.infra.async.AsyncTaskBus;
import com.icoolkj.platform.infra.async.AsyncTaskPublisher;
import com.icoolkj.platform.infra.async.AsyncTaskRetrySupport;
import com.icoolkj.platform.infra.async.handler.DefaultAsyncTaskExecutor;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-process async bus: captures security/tenant context and runs {@link DefaultAsyncTaskExecutor}
 * on a bounded thread pool.
 */
@Component
@ConditionalOnProperty(prefix = "icoolkj.async", name = "provider", havingValue = "local", matchIfMissing = true)
public class LocalAsyncTaskBus implements AsyncTaskBus, AsyncTaskPublisher {

    private static final Logger log = LoggerFactory.getLogger(LocalAsyncTaskBus.class);

    private final DefaultAsyncTaskExecutor asyncTaskExecutor;
    private final AsyncTaskRetrySupport asyncTaskRetrySupport;
    private final ThreadPoolExecutor pool;

    public LocalAsyncTaskBus(DefaultAsyncTaskExecutor asyncTaskExecutor,
                             AsyncTaskRetrySupport asyncTaskRetrySupport,
                             AsyncProperties asyncProperties) {
        this.asyncTaskExecutor = asyncTaskExecutor;
        this.asyncTaskRetrySupport = asyncTaskRetrySupport;
        AtomicInteger seq = new AtomicInteger();
        int threads = Math.max(1, asyncProperties.getLocalPoolSize());
        this.pool = new ThreadPoolExecutor(
                threads,
                threads,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(asyncProperties.getLocalQueueCapacity()),
                r -> {
                    Thread t = new Thread(r, "async-task-local-" + seq.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public void publish(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        TenantContext tenantContext = TenantContextHolder.get();
        Locale locale = LocaleContextHolder.getLocale();
        String id = taskId.trim();
        try {
            pool.execute(() -> runWithRetry(id, authentication, tenantContext, locale));
        } catch (Exception ex) {
            log.error("Failed to enqueue async task id={}", id, ex);
            runWithRetry(id, authentication, tenantContext, locale);
        }
    }

    private void runWithRetry(String id,
                              Authentication authentication,
                              TenantContext tenantContext,
                              Locale locale) {
        asyncTaskExecutor.executeWithContext(id, authentication, tenantContext, locale);
        if (asyncTaskRetrySupport.prepareRetryIfApplicable(id)) {
            log.info("Async local task {} requeued for retry", id);
            publish(id);
        }
    }

    @PreDestroy
    public void shutdown() {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(15, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
