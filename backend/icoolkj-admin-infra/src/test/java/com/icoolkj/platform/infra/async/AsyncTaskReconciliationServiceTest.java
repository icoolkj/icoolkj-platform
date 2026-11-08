package com.icoolkj.platform.infra.async;

import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.infra.domain.AsyncTaskEntity;
import com.icoolkj.platform.infra.mapper.AsyncTaskMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.ObjectProvider;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AsyncTaskReconciliationServiceTest {

    @Mock
    private AsyncTaskMapper asyncTaskMapper;
    @Mock
    private AsyncTaskBus asyncTaskBus;
    @Mock
    private ObjectProvider<com.icoolkj.platform.infra.async.redis.RedisStreamTaskPublisher> publisherProvider;

    private final List<AsyncTaskEntity> store = new ArrayList<>();
    private final AtomicInteger publishCount = new AtomicInteger();

    @BeforeEach
    void setUp() {
        TenantContextHolder.clear();
        store.clear();
        publishCount.set(0);
        when(publisherProvider.getIfAvailable()).thenReturn(null);
        when(asyncTaskMapper.selectById(any())).thenAnswer(inv -> {
            String id = inv.getArgument(0);
            return store.stream().filter(e -> id.equals(e.getId())).findFirst()
                    .map(this::copy).orElse(null);
        });
        when(asyncTaskMapper.updateById(any(AsyncTaskEntity.class))).thenAnswer(inv -> {
            AsyncTaskEntity updated = inv.getArgument(0);
            for (int i = 0; i < store.size(); i++) {
                if (store.get(i).getId().equals(updated.getId())) {
                    store.set(i, copy(updated));
                    return 1;
                }
            }
            return 0;
        });
        doAnswer(inv -> {
            publishCount.incrementAndGet();
            return null;
        }).when(asyncTaskBus).publish(any());
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void requeuesStalePending() {
        AsyncTaskEntity pending = stalePending("p1", 0);
        store.add(pending);
        stubStaleQuery(AsyncTaskStatus.PENDING, List.of(pending));
        stubStaleQuery(AsyncTaskStatus.RUNNING, List.of());

        AsyncTaskReconciliationResult result = service(defaultProps()).reconcile();

        assertEquals(1, result.pendingScanned());
        assertEquals(1, result.requeued());
        assertEquals(0, result.pendingExhausted());
        assertEquals(1, publishCount.get());
        assertEquals(1, store.get(0).getRequeueCount());
        assertEquals(AsyncTaskStatus.PENDING, store.get(0).getTaskStatus());
    }

    @Test
    void exhaustsPendingAfterMaxRequeue() {
        AsyncTaskEntity pending = stalePending("p2", 3);
        store.add(pending);
        stubStaleQuery(AsyncTaskStatus.PENDING, List.of(pending));
        stubStaleQuery(AsyncTaskStatus.RUNNING, List.of());

        AsyncProperties props = defaultProps();
        props.getReconciliation().setMaxRequeueAttempts(3);

        AsyncTaskReconciliationResult result = service(props).reconcile();

        assertEquals(1, result.pendingExhausted());
        assertEquals(0, result.requeued());
        verify(asyncTaskBus, never()).publish(any());
        assertEquals(AsyncTaskStatus.FAILED, store.get(0).getTaskStatus());
        assertEquals(AsyncTaskReconciliationService.ERROR_PENDING_TIMEOUT, store.get(0).getErrorMessage());
    }

    @Test
    void failsStaleRunningWithoutRepublish() {
        AsyncTaskEntity running = new AsyncTaskEntity();
        running.setId("r1");
        running.setTenantId("t1");
        running.setTaskStatus(AsyncTaskStatus.RUNNING);
        running.setUpdatedAt(LocalDateTime.now().minusMinutes(20));
        running.setStartedAt(LocalDateTime.now().minusMinutes(20));
        running.setRequeueCount(0);
        store.add(running);
        stubStaleQuery(AsyncTaskStatus.PENDING, List.of());
        stubStaleQuery(AsyncTaskStatus.RUNNING, List.of(running));

        AsyncTaskReconciliationResult result = service(defaultProps()).reconcile();

        assertEquals(1, result.runningTimedOut());
        verify(asyncTaskBus, never()).publish(any());
        assertEquals(AsyncTaskStatus.FAILED, store.get(0).getTaskStatus());
        assertEquals(AsyncTaskReconciliationService.ERROR_RUNNING_TIMEOUT, store.get(0).getErrorMessage());
    }

    @Test
    void disabledSkipsWork() {
        AsyncProperties props = defaultProps();
        props.getReconciliation().setEnabled(false);
        AsyncTaskReconciliationResult result = service(props).reconcile();
        assertEquals(0, result.pendingScanned());
        verify(asyncTaskMapper, never()).selectStaleByStatus(any(), any(), anyInt());
    }

    private void stubStaleQuery(String status, List<AsyncTaskEntity> rows) {
        when(asyncTaskMapper.selectStaleByStatus(eq(status), any(LocalDateTime.class), anyInt()))
                .thenReturn(rows.stream().map(this::copy).toList());
    }

    private AsyncTaskReconciliationService service(AsyncProperties props) {
        return new AsyncTaskReconciliationService(
                asyncTaskMapper, asyncTaskBus, props, publisherProvider);
    }

    private static AsyncProperties defaultProps() {
        AsyncProperties props = new AsyncProperties();
        props.getReconciliation().setEnabled(true);
        props.getReconciliation().setPendingTimeoutSeconds(120);
        props.getReconciliation().setRunningTimeoutSeconds(600);
        props.getReconciliation().setMaxRequeueAttempts(3);
        props.getReconciliation().setBatchSize(50);
        return props;
    }

    private static AsyncTaskEntity stalePending(String id, int requeueCount) {
        AsyncTaskEntity e = new AsyncTaskEntity();
        e.setId(id);
        e.setTenantId("t1");
        e.setUserId("u1");
        e.setTaskStatus(AsyncTaskStatus.PENDING);
        e.setRequeueCount(requeueCount);
        e.setUpdatedAt(LocalDateTime.now().minusMinutes(5));
        return e;
    }

    private AsyncTaskEntity copy(AsyncTaskEntity src) {
        AsyncTaskEntity e = new AsyncTaskEntity();
        e.setId(src.getId());
        e.setTenantId(src.getTenantId());
        e.setUserId(src.getUserId());
        e.setTaskStatus(src.getTaskStatus());
        e.setErrorMessage(src.getErrorMessage());
        e.setRequeueCount(src.getRequeueCount());
        e.setStartedAt(src.getStartedAt());
        e.setFinishedAt(src.getFinishedAt());
        e.setUpdatedAt(src.getUpdatedAt());
        return e;
    }
}
