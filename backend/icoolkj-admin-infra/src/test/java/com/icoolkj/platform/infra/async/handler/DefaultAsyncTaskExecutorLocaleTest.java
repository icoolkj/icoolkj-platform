package com.icoolkj.platform.infra.async.handler;

import com.icoolkj.platform.common.i18n.LocaleResolver;
import com.icoolkj.platform.framework.tenant.TenantContext;
import com.icoolkj.platform.infra.async.AsyncTaskContext;
import com.icoolkj.platform.infra.async.AsyncTaskHandler;
import com.icoolkj.platform.infra.async.AsyncTaskRegistry;
import com.icoolkj.platform.infra.async.AsyncTaskStatus;
import com.icoolkj.platform.infra.domain.AsyncTaskEntity;
import com.icoolkj.platform.infra.mapper.AsyncTaskMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultAsyncTaskExecutorLocaleTest {

    @Mock
    private AsyncTaskMapper asyncTaskMapper;
    @Mock
    private AsyncTaskRegistry asyncTaskRegistry;

    @Test
    void executeRestoresSubmitterLocaleFromEntity() {
        AsyncTaskEntity entity = new AsyncTaskEntity();
        entity.setId("t1");
        entity.setTaskType("demo.task");
        entity.setTaskStatus(AsyncTaskStatus.PENDING);
        entity.setLocale(LocaleResolver.LOCALE_EN_US);
        entity.setRequestParam("{}");
        when(asyncTaskMapper.selectById("t1")).thenReturn(entity);

        AtomicReference<Locale> seen = new AtomicReference<>();
        when(asyncTaskRegistry.require("demo.task")).thenReturn(new AsyncTaskHandler() {
            @Override
            public String taskType() {
                return "demo.task";
            }

            @Override
            public String handle(AsyncTaskContext context) {
                seen.set(LocaleContextHolder.getLocale());
                return "{}";
            }
        });

        Locale previous = LocaleContextHolder.getLocale();
        try {
            LocaleContextHolder.setLocale(Locale.forLanguageTag(LocaleResolver.LOCALE_ZH_CN));
            DefaultAsyncTaskExecutor executor =
                    new DefaultAsyncTaskExecutor(asyncTaskMapper, asyncTaskRegistry);
            executor.executeWithContext(
                    "t1",
                    null,
                    TenantContext.tenant("ten", null),
                    null);
            assertEquals(Locale.forLanguageTag(LocaleResolver.LOCALE_EN_US), seen.get());
        } finally {
            LocaleContextHolder.setLocale(previous);
        }

        ArgumentCaptor<AsyncTaskEntity> cap = ArgumentCaptor.forClass(AsyncTaskEntity.class);
        verify(asyncTaskMapper, atLeastOnce()).updateById(cap.capture());
        assertEquals(AsyncTaskStatus.SUCCESS, cap.getValue().getTaskStatus());
    }
}
