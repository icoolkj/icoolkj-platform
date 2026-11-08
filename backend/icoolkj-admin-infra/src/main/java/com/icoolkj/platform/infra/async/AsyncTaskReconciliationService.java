package com.icoolkj.platform.infra.async;

import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.infra.async.redis.RedisStreamTaskPublisher;
import com.icoolkj.platform.infra.domain.AsyncTaskEntity;
import com.icoolkj.platform.infra.mapper.AsyncTaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AsyncTaskReconciliationService {

    public static final String ERROR_PENDING_TIMEOUT = "error.async.pending_timeout";
    public static final String ERROR_RUNNING_TIMEOUT = "error.async.running_timeout";

    private static final Logger log = LoggerFactory.getLogger(AsyncTaskReconciliationService.class);

    private final AsyncTaskMapper asyncTaskMapper;
    private final AsyncTaskBus asyncTaskBus;
    private final AsyncProperties asyncProperties;
    private final ObjectProvider<RedisStreamTaskPublisher> streamPublisher;

    public AsyncTaskReconciliationService(AsyncTaskMapper asyncTaskMapper,
                                          AsyncTaskBus asyncTaskBus,
                                          AsyncProperties asyncProperties,
                                          ObjectProvider<RedisStreamTaskPublisher> streamPublisher) {
        this.asyncTaskMapper = asyncTaskMapper;
        this.asyncTaskBus = asyncTaskBus;
        this.asyncProperties = asyncProperties;
        this.streamPublisher = streamPublisher;
    }

    public AsyncTaskReconciliationResult reconcile() {
        if (!asyncProperties.isEnabled()) {
            return AsyncTaskReconciliationResult.empty();
        }
        AsyncProperties.Reconciliation cfg = asyncProperties.getReconciliation();
        if (cfg == null || !cfg.isEnabled()) {
            return AsyncTaskReconciliationResult.empty();
        }
        return TenantScopeRunner.runIgnoringTenant(() -> doReconcile(cfg));
    }

    private AsyncTaskReconciliationResult doReconcile(AsyncProperties.Reconciliation cfg) {
        int batch = Math.max(1, cfg.getBatchSize());
        int maxRequeue = Math.max(0, cfg.getMaxRequeueAttempts());
        long pendingSec = Math.max(1L, cfg.getPendingTimeoutSeconds());
        long runningSec = Math.max(1L, cfg.getRunningTimeoutSeconds());

        LocalDateTime pendingBefore = LocalDateTime.now().minusSeconds(pendingSec);
        LocalDateTime runningBefore = LocalDateTime.now().minusSeconds(runningSec);

        int pendingScanned = 0;
        int requeued = 0;
        int pendingExhausted = 0;
        int runningTimedOut = 0;
        int skipped = 0;

        List<AsyncTaskEntity> pending = asyncTaskMapper.selectStaleByStatus(
                AsyncTaskStatus.PENDING, pendingBefore, batch);
        pendingScanned = pending.size();
        for (AsyncTaskEntity row : pending) {
            Outcome o = reconcilePending(row, maxRequeue);
            switch (o) {
                case REQUEUED -> requeued++;
                case EXHAUSTED -> pendingExhausted++;
                case SKIPPED -> skipped++;
            }
        }

        List<AsyncTaskEntity> running = asyncTaskMapper.selectStaleByStatus(
                AsyncTaskStatus.RUNNING, runningBefore, batch);
        for (AsyncTaskEntity row : running) {
            Outcome o = reconcileRunning(row);
            if (o == Outcome.EXHAUSTED) {
                runningTimedOut++;
            } else {
                skipped++;
            }
        }

        AsyncTaskReconciliationResult result = new AsyncTaskReconciliationResult(
                pendingScanned, requeued, pendingExhausted, runningTimedOut, skipped);
        if (pendingExhausted > 0 || runningTimedOut > 0) {
            log.error(
                    "Async reconcile alert exhausted={} runningTimedOut={} requeued={} pendingScanned={}",
                    pendingExhausted, runningTimedOut, requeued, pendingScanned);
        } else if (requeued > 0) {
            log.info("Async reconcile requeued={} pendingScanned={}", requeued, pendingScanned);
        }
        return result;
    }

    private Outcome reconcilePending(AsyncTaskEntity snapshot, int maxRequeue) {
        if (snapshot == null || !StringUtils.hasText(snapshot.getId())) {
            return Outcome.SKIPPED;
        }
        AsyncTaskEntity fresh = asyncTaskMapper.selectById(snapshot.getId());
        if (fresh == null || !AsyncTaskStatus.PENDING.equals(fresh.getTaskStatus())) {
            return Outcome.SKIPPED;
        }
        int current = fresh.getRequeueCount() == null ? 0 : fresh.getRequeueCount();
        if (current >= maxRequeue) {
            markFailed(fresh, ERROR_PENDING_TIMEOUT);
            deadLetter(fresh, ERROR_PENDING_TIMEOUT);
            log.error("Async PENDING exhausted requeue taskId={} tenantId={} requeueCount={}",
                    fresh.getId(), fresh.getTenantId(), current);
            return Outcome.EXHAUSTED;
        }
        fresh.setRequeueCount(current + 1);
        AuditFieldHelper.fillUpdate(fresh);
        asyncTaskMapper.updateById(fresh);
        try {
            asyncTaskBus.publish(fresh.getId());
        } catch (RuntimeException ex) {
            log.error("Async requeue publish failed taskId={}", fresh.getId(), ex);
            return Outcome.REQUEUED;
        }
        log.info("Async PENDING requeued taskId={} attempt={}/{}",
                fresh.getId(), fresh.getRequeueCount(), maxRequeue);
        return Outcome.REQUEUED;
    }

    private Outcome reconcileRunning(AsyncTaskEntity snapshot) {
        if (snapshot == null || !StringUtils.hasText(snapshot.getId())) {
            return Outcome.SKIPPED;
        }
        AsyncTaskEntity fresh = asyncTaskMapper.selectById(snapshot.getId());
        if (fresh == null || !AsyncTaskStatus.RUNNING.equals(fresh.getTaskStatus())) {
            return Outcome.SKIPPED;
        }
        markFailed(fresh, ERROR_RUNNING_TIMEOUT);
        deadLetter(fresh, ERROR_RUNNING_TIMEOUT);
        log.error("Async RUNNING timed out taskId={} tenantId={} startedAt={}",
                fresh.getId(), fresh.getTenantId(), fresh.getStartedAt());
        return Outcome.EXHAUSTED;
    }

    private void markFailed(AsyncTaskEntity entity, String errorKey) {
        entity.setTaskStatus(AsyncTaskStatus.FAILED);
        entity.setErrorMessage(errorKey);
        entity.setFinishedAt(LocalDateTime.now());
        AuditFieldHelper.fillUpdate(entity);
        asyncTaskMapper.updateById(entity);
    }

    private void deadLetter(AsyncTaskEntity entity, String reason) {
        RedisStreamTaskPublisher publisher = streamPublisher.getIfAvailable();
        if (publisher == null) {
            return;
        }
        publisher.publishDeadLetter(
                AsyncTaskMessage.of(entity.getId(), entity.getTenantId(), entity.getUserId(), entity.getLocale()),
                reason);
    }

    private enum Outcome {
        REQUEUED,
        EXHAUSTED,
        SKIPPED
    }
}
