package com.icoolkj.platform.infra.async;

import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.infra.domain.AsyncTaskEntity;
import com.icoolkj.platform.infra.mapper.AsyncTaskMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Shared FAILED → PENDING requeue decision for Redis consumer and local bus.
 *
 * @return true if the task was reset to PENDING and should be published again
 */
@Component
public class AsyncTaskRetrySupport {

    private final AsyncTaskMapper asyncTaskMapper;
    private final AsyncProperties asyncProperties;

    public AsyncTaskRetrySupport(AsyncTaskMapper asyncTaskMapper, AsyncProperties asyncProperties) {
        this.asyncTaskMapper = asyncTaskMapper;
        this.asyncProperties = asyncProperties;
    }

    public boolean prepareRetryIfApplicable(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return false;
        }
        AsyncTaskEntity after = asyncTaskMapper.selectById(taskId.trim());
        if (after == null || !AsyncTaskStatus.FAILED.equals(after.getTaskStatus())) {
            return false;
        }
        if (!AsyncTaskRetryPolicy.isRetryable(after.getErrorMessage())) {
            return false;
        }
        int retry = after.getRetryCount() == null ? 0 : after.getRetryCount();
        int max = after.getMaxRetryCount() == null
                ? asyncProperties.getRetry().getMaxTimes()
                : after.getMaxRetryCount();
        retry++;
        after.setRetryCount(retry);
        if (retry >= Math.max(0, max)) {
            AuditFieldHelper.fillUpdate(after);
            asyncTaskMapper.updateById(after);
            return false;
        }
        after.setTaskStatus(AsyncTaskStatus.PENDING);
        after.setFinishedAt(null);
        after.setStartedAt(null);
        AuditFieldHelper.fillUpdate(after);
        asyncTaskMapper.updateById(after);
        return true;
    }
}
