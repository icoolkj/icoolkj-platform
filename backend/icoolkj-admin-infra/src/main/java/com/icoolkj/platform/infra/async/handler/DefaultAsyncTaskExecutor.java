package com.icoolkj.platform.infra.async.handler;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.tenant.TenantContext;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.infra.async.AsyncTaskContext;
import com.icoolkj.platform.infra.async.AsyncTaskExecutionContext;
import com.icoolkj.platform.infra.async.AsyncTaskExecutor;
import com.icoolkj.platform.infra.async.AsyncTaskHandler;
import com.icoolkj.platform.infra.async.AsyncTaskRegistry;
import com.icoolkj.platform.infra.async.AsyncTaskResult;
import com.icoolkj.platform.infra.async.AsyncTaskStatus;
import com.icoolkj.platform.infra.domain.AsyncTaskEntity;
import com.icoolkj.platform.infra.mapper.AsyncTaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Marks task RUNNING → invokes handler → SUCCESS/FAILED with result/error.
 * Caller must restore tenant/security context before invoke (see LocalAsyncTaskBus).
 */
@Component
public class DefaultAsyncTaskExecutor implements AsyncTaskExecutor {

    private static final Logger log = LoggerFactory.getLogger(DefaultAsyncTaskExecutor.class);
    private static final int ERROR_MAX = 2000;

    private final AsyncTaskMapper asyncTaskMapper;
    private final AsyncTaskRegistry asyncTaskRegistry;

    public DefaultAsyncTaskExecutor(AsyncTaskMapper asyncTaskMapper,
                                    AsyncTaskRegistry asyncTaskRegistry) {
        this.asyncTaskMapper = asyncTaskMapper;
        this.asyncTaskRegistry = asyncTaskRegistry;
    }

    @Override
    public void execute(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return;
        }
        AsyncTaskEntity entity = asyncTaskMapper.selectById(taskId.trim());
        if (entity == null) {
            log.warn("Async task not found id={}", taskId);
            return;
        }
        if (AsyncTaskStatus.isTerminal(entity.getTaskStatus())) {
            return;
        }

        entity.setTaskStatus(AsyncTaskStatus.RUNNING);
        entity.setStartedAt(LocalDateTime.now());
        entity.setErrorMessage(null);
        AuditFieldHelper.fillUpdate(entity);
        asyncTaskMapper.updateById(entity);

        Locale previousLocale = LocaleContextHolder.getLocale();
        Locale taskLocale = AsyncTaskExecutionContext.toLocale(entity.getLocale());
        try {
            if (taskLocale != null) {
                LocaleContextHolder.setLocale(taskLocale);
            }
            AsyncTaskHandler handler = asyncTaskRegistry.require(entity.getTaskType());
            AsyncTaskContext ctx = new AsyncTaskContext(
                    entity.getId(),
                    entity.getTaskType(),
                    entity.getRequestParam(),
                    entity.getBizType(),
                    entity.getBizId());
            String resultData = handler.handle(ctx);
            markSuccess(entity.getId(), resultData);
        } catch (BusinessException ex) {
            markFailed(entity.getId(), resolveBusinessMessage(ex));
        } catch (Throwable ex) {
            log.error("Async task failed id={} type={}", entity.getId(), entity.getTaskType(), ex);
            markFailed(entity.getId(), formatThrowable(ex));
        } finally {
            if (taskLocale != null) {
                LocaleContextHolder.setLocale(previousLocale);
            }
        }
    }

    /** Executes with captured security + tenant + locale context (used by local bus). */
    public AsyncTaskResult executeWithContext(String taskId,
                                              Authentication authentication,
                                              TenantContext tenantContext) {
        return executeWithContext(taskId, authentication, tenantContext, null);
    }

    public AsyncTaskResult executeWithContext(String taskId,
                                              Authentication authentication,
                                              TenantContext tenantContext,
                                              Locale locale) {
        SecurityContext previousSecurity = SecurityContextHolder.getContext();
        TenantContext previousTenant = TenantContextHolder.get();
        Locale previousLocale = LocaleContextHolder.getLocale();
        try {
            if (authentication != null) {
                SecurityContext ctx = SecurityContextHolder.createEmptyContext();
                ctx.setAuthentication(authentication);
                SecurityContextHolder.setContext(ctx);
            } else {
                SecurityContextHolder.clearContext();
            }
            if (tenantContext != null) {
                TenantContextHolder.set(tenantContext);
            } else {
                TenantContextHolder.clear();
            }
            if (locale != null) {
                LocaleContextHolder.setLocale(locale);
            }
            execute(taskId);
            AsyncTaskEntity entity = asyncTaskMapper.selectById(taskId);
            if (entity == null) {
                return new AsyncTaskResult(taskId, AsyncTaskStatus.FAILED, null, "task missing");
            }
            return new AsyncTaskResult(
                    entity.getId(), entity.getTaskStatus(), entity.getResultData(), entity.getErrorMessage());
        } finally {
            SecurityContextHolder.setContext(previousSecurity);
            if (previousTenant != null) {
                TenantContextHolder.set(previousTenant);
            } else {
                TenantContextHolder.clear();
            }
            if (locale != null) {
                LocaleContextHolder.setLocale(previousLocale);
            }
        }
    }

    private void markSuccess(String taskId, String resultData) {
        AsyncTaskEntity entity = asyncTaskMapper.selectById(taskId);
        if (entity == null) {
            return;
        }
        entity.setTaskStatus(AsyncTaskStatus.SUCCESS);
        entity.setResultData(resultData);
        entity.setErrorMessage(null);
        entity.setFinishedAt(LocalDateTime.now());
        AuditFieldHelper.fillUpdate(entity);
        asyncTaskMapper.updateById(entity);
    }

    private void markFailed(String taskId, String errorMessage) {
        AsyncTaskEntity entity = asyncTaskMapper.selectById(taskId);
        if (entity == null) {
            return;
        }
        entity.setTaskStatus(AsyncTaskStatus.FAILED);
        entity.setErrorMessage(truncate(errorMessage));
        entity.setFinishedAt(LocalDateTime.now());
        AuditFieldHelper.fillUpdate(entity);
        asyncTaskMapper.updateById(entity);
    }

    private static String resolveBusinessMessage(BusinessException ex) {
        String msg = ex.getMessage();
        String key = StringUtils.hasText(msg) ? msg.trim() : "error.async.task_failed";
        Object[] args = ex.getArgs();
        if (args == null || args.length == 0) {
            return truncate(key);
        }
        StringBuilder sb = new StringBuilder(key);
        for (Object arg : args) {
            sb.append('\u001f').append(arg == null ? "" : String.valueOf(arg));
        }
        return truncate(sb.toString());
    }

    private static String formatThrowable(Throwable ex) {
        String name = ex.getClass().getName();
        String msg = ex.getMessage();
        if (!StringUtils.hasText(msg)) {
            return truncate(name);
        }
        return truncate(name + ": " + msg);
    }

    private static String truncate(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        return s.length() <= ERROR_MAX ? s : s.substring(0, ERROR_MAX);
    }
}
