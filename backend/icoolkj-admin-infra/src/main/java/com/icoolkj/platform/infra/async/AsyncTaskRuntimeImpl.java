package com.icoolkj.platform.infra.async;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.datascope.OwnershipColumns;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantFieldHelper;
import com.icoolkj.platform.infra.domain.AsyncTaskEntity;
import com.icoolkj.platform.infra.mapper.AsyncTaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/** Default runtime: persist PENDING row then {@link AsyncTaskBus#publish}. */
@Service
public class AsyncTaskRuntimeImpl implements AsyncTaskRuntime {

    private static final Logger log = LoggerFactory.getLogger(AsyncTaskRuntimeImpl.class);

    private final AsyncTaskMapper asyncTaskMapper;
    private final AsyncTaskBus asyncTaskBus;
    private final AsyncTaskRegistry asyncTaskRegistry;
    private final AsyncProperties asyncProperties;

    public AsyncTaskRuntimeImpl(AsyncTaskMapper asyncTaskMapper,
                                AsyncTaskBus asyncTaskBus,
                                AsyncTaskRegistry asyncTaskRegistry,
                                AsyncProperties asyncProperties) {
        this.asyncTaskMapper = asyncTaskMapper;
        this.asyncTaskBus = asyncTaskBus;
        this.asyncTaskRegistry = asyncTaskRegistry;
        this.asyncProperties = asyncProperties;
    }

    @Override
    public String submit(String taskType, String taskName, String bizType, String bizId, String requestParam) {
        return submitWithId(IdGeneratorUtils.nextIdStr(), taskType, taskName, bizType, bizId, requestParam);
    }

    @Override
    public String submitWithId(String taskId,
                               String taskType,
                               String taskName,
                               String bizType,
                               String bizId,
                               String requestParam) {
        if (!asyncProperties.isEnabled()) {
            throw new BusinessException("error.async.disabled");
        }
        if (!StringUtils.hasText(taskId)) {
            throw new BusinessException("error.validation.required", "fields.id");
        }
        asyncTaskRegistry.require(taskType);
        if (!StringUtils.hasText(taskName)) {
            throw new BusinessException("error.async.task_name_required");
        }

        AsyncTaskEntity entity = new AsyncTaskEntity();
        entity.setId(taskId.trim());
        entity.setTenantId(TenantFieldHelper.resolveTenantId(null));
        entity.setTaskType(taskType.trim());
        entity.setTaskName(taskName.trim());
        entity.setBizType(StringUtils.hasText(bizType) ? bizType.trim() : null);
        entity.setBizId(StringUtils.hasText(bizId) ? bizId.trim() : null);
        entity.setTaskStatus(AsyncTaskStatus.PENDING);
        entity.setRequestParam(requestParam);
        entity.setRetryCount(0);
        entity.setRequeueCount(0);
        entity.setMaxRetryCount(Math.max(0, asyncProperties.getRetry().getMaxTimes()));
        OwnershipColumns.applyUser(entity::setUserId, SecurityContextHelper.getUserId());
        entity.setLocale(AsyncTaskExecutionContext.captureLocaleTag());
        AuditFieldHelper.fillInsert(entity);
        asyncTaskMapper.insert(entity);

        try {
            asyncTaskBus.publish(entity.getId());
        } catch (RuntimeException ex) {
            failClosedAfterPublishError(entity.getId(), ex);
            throw ex;
        }
        return entity.getId();
    }

    /**
     * Prefer FAILED terminal; if mark fails, hard-delete the row so Facade release does not leave
     * an orphan PENDING task without staging files.
     */
    private void failClosedAfterPublishError(String taskId, RuntimeException cause) {
        try {
            AsyncTaskEntity row = asyncTaskMapper.selectById(taskId);
            if (row == null || AsyncTaskStatus.isTerminal(row.getTaskStatus())) {
                return;
            }
            row.setTaskStatus(AsyncTaskStatus.FAILED);
            row.setErrorMessage("error.async.publish_failed");
            row.setFinishedAt(LocalDateTime.now());
            AuditFieldHelper.fillUpdate(row);
            asyncTaskMapper.updateById(row);
            log.error("Async task publish failed after insert taskId={}", taskId, cause);
        } catch (Exception markEx) {
            log.error("Failed to mark async task FAILED after publish error taskId={}", taskId, markEx);
            try {
                asyncTaskMapper.deleteById(taskId);
                log.error("Deleted orphan PENDING async task after publish mark failure taskId={}", taskId);
            } catch (Exception deleteEx) {
                log.error("Failed to delete orphan PENDING async task taskId={}", taskId, deleteEx);
            }
        }
    }
}
