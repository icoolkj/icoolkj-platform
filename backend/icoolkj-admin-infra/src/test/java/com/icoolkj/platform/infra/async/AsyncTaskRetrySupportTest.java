package com.icoolkj.platform.infra.async;

import com.icoolkj.platform.infra.domain.AsyncTaskEntity;
import com.icoolkj.platform.infra.mapper.AsyncTaskMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsyncTaskRetrySupportTest {

    @Mock
    private AsyncTaskMapper asyncTaskMapper;

    @Test
    void preparesRetryForTransientFailure() {
        AsyncProperties props = new AsyncProperties();
        props.getRetry().setMaxTimes(5);
        AsyncTaskEntity failed = new AsyncTaskEntity();
        failed.setId("t1");
        failed.setTaskStatus(AsyncTaskStatus.FAILED);
        failed.setErrorMessage("java.net.SocketTimeoutException: timeout");
        failed.setRetryCount(0);
        failed.setMaxRetryCount(5);
        when(asyncTaskMapper.selectById("t1")).thenReturn(failed);

        AsyncTaskRetrySupport support = new AsyncTaskRetrySupport(asyncTaskMapper, props);
        assertTrue(support.prepareRetryIfApplicable("t1"));

        ArgumentCaptor<AsyncTaskEntity> cap = ArgumentCaptor.forClass(AsyncTaskEntity.class);
        verify(asyncTaskMapper).updateById(cap.capture());
        assertEquals(AsyncTaskStatus.PENDING, cap.getValue().getTaskStatus());
        assertEquals(1, cap.getValue().getRetryCount());
    }

    @Test
    void doesNotRetryBusinessError() {
        AsyncProperties props = new AsyncProperties();
        AsyncTaskEntity failed = new AsyncTaskEntity();
        failed.setId("t2");
        failed.setTaskStatus(AsyncTaskStatus.FAILED);
        failed.setErrorMessage("error.excel.import_failed");
        when(asyncTaskMapper.selectById("t2")).thenReturn(failed);

        AsyncTaskRetrySupport support = new AsyncTaskRetrySupport(asyncTaskMapper, props);
        assertFalse(support.prepareRetryIfApplicable("t2"));
    }
}
