package com.icoolkj.platform.infra.async;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.framework.tenant.TenantContext;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.infra.async.handler.DefaultAsyncTaskExecutor;
import com.icoolkj.platform.infra.async.local.LocalAsyncTaskBus;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AsyncTaskRuntimeLocalTest {

    @Mock
    private AsyncTaskMapper asyncTaskMapper;

    private AtomicReference<AsyncTaskEntity> store;
    private LocalAsyncTaskBus bus;

    @BeforeEach
    void setUp() {
        TenantContextHolder.set(TenantContext.tenant("t1", "demo"));
        LoginUser login = new LoginUser();
        login.setUserId("u1");
        login.setTenantId("t1");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(login, null, List.of()));
        store = new AtomicReference<>();
        when(asyncTaskMapper.insert(any(AsyncTaskEntity.class))).thenAnswer(inv -> {
            store.set(cloneEntity(inv.getArgument(0)));
            return 1;
        });
        when(asyncTaskMapper.selectById(any())).thenAnswer(inv -> {
            AsyncTaskEntity e = store.get();
            return e == null ? null : cloneEntity(e);
        });
        when(asyncTaskMapper.updateById(any(AsyncTaskEntity.class))).thenAnswer(inv -> {
            store.set(cloneEntity(inv.getArgument(0)));
            return 1;
        });
    }

    @AfterEach
    void tearDown() {
        if (bus != null) {
            bus.shutdown();
            bus = null;
        }
        SecurityContextHolder.clearContext();
        TenantContextHolder.clear();
    }

    @Test
    void unknownHandlerRejectedAtSubmit() {
        AsyncTaskRegistry registry = new AsyncTaskRegistry(List.of());
        AsyncProperties props = new AsyncProperties();
        AsyncTaskRuntime runtime = new AsyncTaskRuntimeImpl(asyncTaskMapper, id -> {
        }, registry, props);
        assertThrows(BusinessException.class,
                () -> runtime.submit("missing", "n", null, null, null));
    }

    @Test
    void publishFailureMarksTaskFailed() {
        AsyncTaskHandler ok = new AsyncTaskHandler() {
            @Override
            public String taskType() {
                return "test.publish";
            }

            @Override
            public String handle(AsyncTaskContext context) {
                return "{}";
            }
        };
        AsyncTaskRegistry registry = new AsyncTaskRegistry(List.of(ok));
        AsyncProperties props = new AsyncProperties();
        AsyncTaskRuntime runtime = new AsyncTaskRuntimeImpl(asyncTaskMapper, id -> {
            throw new IllegalStateException("broker down");
        }, registry, props);

        assertThrows(IllegalStateException.class,
                () -> runtime.submit("test.publish", "n", "ut", null, "{}"));
        assertEquals(AsyncTaskStatus.FAILED, store.get().getTaskStatus());
        assertEquals("error.async.publish_failed", store.get().getErrorMessage());
    }

    @Test
    void submitViaLocalBusPollsToSuccess() throws Exception {
        AsyncTaskHandler ok = new AsyncTaskHandler() {
            @Override
            public String taskType() {
                return "test.ok";
            }

            @Override
            public String handle(AsyncTaskContext context) {
                return "{\"ok\":true}";
            }
        };
        String taskId = submitAndAwait("test.ok", ok, AsyncTaskStatus.SUCCESS);
        assertEquals("{\"ok\":true}", store.get().getResultData());
        assertTrue(taskId != null && !taskId.isBlank());
    }

    @Test
    void submitViaLocalBusPollsToFailed() throws Exception {
        AsyncTaskHandler bad = new AsyncTaskHandler() {
            @Override
            public String taskType() {
                return "test.bad";
            }

            @Override
            public String handle(AsyncTaskContext context) {
                throw new BusinessException("error.async.task_failed");
            }
        };
        submitAndAwait("test.bad", bad, AsyncTaskStatus.FAILED);
        assertEquals("error.async.task_failed", store.get().getErrorMessage());
    }

    @Test
    void executorMarksSuccess() {
        AsyncTaskHandler ok = new AsyncTaskHandler() {
            @Override
            public String taskType() {
                return "test.ok";
            }

            @Override
            public String handle(AsyncTaskContext context) {
                return "{\"ok\":true}";
            }
        };
        DefaultAsyncTaskExecutor executor =
                new DefaultAsyncTaskExecutor(asyncTaskMapper, new AsyncTaskRegistry(List.of(ok)));

        AsyncTaskEntity pending = new AsyncTaskEntity();
        pending.setId("tid-ok");
        pending.setTaskType("test.ok");
        pending.setTaskStatus(AsyncTaskStatus.PENDING);
        store.set(pending);

        executor.execute("tid-ok");
        assertEquals(AsyncTaskStatus.SUCCESS, store.get().getTaskStatus());
        assertEquals("{\"ok\":true}", store.get().getResultData());
    }

    @Test
    void executorMarksFailedOnBusinessException() {
        AsyncTaskHandler bad = new AsyncTaskHandler() {
            @Override
            public String taskType() {
                return "test.bad";
            }

            @Override
            public String handle(AsyncTaskContext context) {
                throw new BusinessException("error.async.task_failed");
            }
        };
        DefaultAsyncTaskExecutor executor =
                new DefaultAsyncTaskExecutor(asyncTaskMapper, new AsyncTaskRegistry(List.of(bad)));

        AsyncTaskEntity pending = new AsyncTaskEntity();
        pending.setId("tid-1");
        pending.setTaskType("test.bad");
        pending.setTaskStatus(AsyncTaskStatus.PENDING);
        store.set(pending);

        executor.execute("tid-1");
        assertEquals(AsyncTaskStatus.FAILED, store.get().getTaskStatus());
        assertEquals("error.async.task_failed", store.get().getErrorMessage());
    }

    private String submitAndAwait(String taskType, AsyncTaskHandler handler, String expectedStatus)
            throws InterruptedException {
        AsyncTaskRegistry registry = new AsyncTaskRegistry(List.of(handler));
        DefaultAsyncTaskExecutor executor = new DefaultAsyncTaskExecutor(asyncTaskMapper, registry);
        AsyncProperties props = new AsyncProperties();
        props.setLocalPoolSize(1);
        props.setLocalQueueCapacity(8);
        bus = new LocalAsyncTaskBus(executor, new AsyncTaskRetrySupport(asyncTaskMapper, props), props);
        AsyncTaskRuntime runtime = new AsyncTaskRuntimeImpl(asyncTaskMapper, bus, registry, props);

        String taskId = runtime.submit(taskType, "unit-test", "ut", null, "{\"x\":1}");
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            AsyncTaskEntity current = store.get();
            if (current != null && expectedStatus.equals(current.getTaskStatus())) {
                return taskId;
            }
            Thread.sleep(20L);
        }
        AsyncTaskEntity finalState = store.get();
        throw new AssertionError("timeout waiting for status=" + expectedStatus
                + ", actual=" + (finalState == null ? null : finalState.getTaskStatus()));
    }

    private static AsyncTaskEntity cloneEntity(AsyncTaskEntity src) {
        AsyncTaskEntity e = new AsyncTaskEntity();
        e.setId(src.getId());
        e.setTenantId(src.getTenantId());
        e.setTaskType(src.getTaskType());
        e.setTaskName(src.getTaskName());
        e.setBizType(src.getBizType());
        e.setBizId(src.getBizId());
        e.setTaskStatus(src.getTaskStatus());
        e.setRequestParam(src.getRequestParam());
        e.setResultData(src.getResultData());
        e.setErrorMessage(src.getErrorMessage());
        e.setRetryCount(src.getRetryCount());
        e.setMaxRetryCount(src.getMaxRetryCount());
        e.setRequeueCount(src.getRequeueCount());
        e.setStartedAt(src.getStartedAt());
        e.setFinishedAt(src.getFinishedAt());
        e.setUserId(src.getUserId());
        return e;
    }
}
