package com.icoolkj.platform.infra.excel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.infra.async.AsyncTaskRuntime;
import com.icoolkj.platform.infra.vo.AsyncTaskSubmitVO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Submit async Excel export: mint {@code taskId} → persist PENDING with query JSON → publish.
 * Worker writes the xlsx and stages it under {@code bizType=excel-export}.
 */
@Component
public class ExcelExportAsyncSubmitFacade {

    private final AsyncTaskRuntime asyncTaskRuntime;
    private final ObjectMapper objectMapper;

    public ExcelExportAsyncSubmitFacade(AsyncTaskRuntime asyncTaskRuntime, ObjectMapper objectMapper) {
        this.asyncTaskRuntime = asyncTaskRuntime;
        this.objectMapper = objectMapper;
    }

    public AsyncTaskSubmitVO submit(String taskType, String taskName, Object query) throws Exception {
        if (!StringUtils.hasText(taskType)) {
            throw new BusinessException("error.validation.required", "fields.taskType");
        }
        if (!StringUtils.hasText(taskName)) {
            throw new BusinessException("error.async.task_name_required");
        }
        String taskId = IdGeneratorUtils.nextIdStr();
        String requestParam = objectMapper.writeValueAsString(query == null ? java.util.Map.of() : query);
        asyncTaskRuntime.submitWithId(
                taskId,
                taskType,
                taskName,
                ExcelExportAsyncConstants.BIZ_TYPE,
                taskId,
                requestParam);
        return new AsyncTaskSubmitVO(taskId);
    }
}
