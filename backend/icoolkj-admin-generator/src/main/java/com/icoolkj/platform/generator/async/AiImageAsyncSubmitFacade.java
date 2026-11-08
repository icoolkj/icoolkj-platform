package com.icoolkj.platform.generator.async;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.generator.dto.AiImageGenerateRequest;
import com.icoolkj.platform.infra.async.AsyncTaskRuntime;
import com.icoolkj.platform.infra.vo.AsyncTaskSubmitVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class AiImageAsyncSubmitFacade {

    private final AsyncTaskRuntime asyncTaskRuntime;
    private final ObjectMapper objectMapper;

    public AiImageAsyncSubmitFacade(AsyncTaskRuntime asyncTaskRuntime, ObjectMapper objectMapper) {
        this.asyncTaskRuntime = asyncTaskRuntime;
        this.objectMapper = objectMapper;
    }

    public AsyncTaskSubmitVO submit(AiImageGenerateRequest request) throws Exception {
        if (request == null || StringUtils.isBlank(request.getPrompt())) {
            throw new BusinessException("error.ai.image_prompt_required");
        }
        String taskId = IdGeneratorUtils.nextIdStr();
        String requestParam = objectMapper.writeValueAsString(request);
        asyncTaskRuntime.submitWithId(
                taskId,
                AiAsyncConstants.TASK_IMAGE_GENERATE,
                "AI image generate",
                AiAsyncConstants.BIZ_TYPE_IMAGE,
                taskId,
                requestParam);
        return new AsyncTaskSubmitVO(taskId);
    }
}
