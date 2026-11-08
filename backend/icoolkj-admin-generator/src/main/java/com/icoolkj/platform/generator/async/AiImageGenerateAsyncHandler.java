package com.icoolkj.platform.generator.async;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.generator.dto.AiImageGenerateRequest;
import com.icoolkj.platform.generator.service.impl.AiImageService;
import com.icoolkj.platform.generator.vo.AiAsyncImageResultVO;
import com.icoolkj.platform.generator.vo.AiImageGenerateResultVO;
import com.icoolkj.platform.infra.async.AsyncTaskContext;
import com.icoolkj.platform.infra.async.AsyncTaskHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class AiImageGenerateAsyncHandler implements AsyncTaskHandler {

    private final ObjectMapper objectMapper;
    private final AiImageService aiImageService;

    public AiImageGenerateAsyncHandler(ObjectMapper objectMapper, AiImageService aiImageService) {
        this.objectMapper = objectMapper;
        this.aiImageService = aiImageService;
    }

    @Override
    public String taskType() {
        return AiAsyncConstants.TASK_IMAGE_GENERATE;
    }

    @Override
    public String handle(AsyncTaskContext context) throws Exception {
        if (context == null || StringUtils.isBlank(context.requestParam())) {
            throw new BusinessException("error.ai.image_prompt_required");
        }
        AiImageGenerateRequest request =
                objectMapper.readValue(context.requestParam(), AiImageGenerateRequest.class);
        AiImageGenerateResultVO generated = aiImageService.generate(request);
        return objectMapper.writeValueAsString(toAsyncResult(generated));
    }

    static AiAsyncImageResultVO toAsyncResult(AiImageGenerateResultVO generated) {
        AiAsyncImageResultVO result = new AiAsyncImageResultVO();
        if (generated == null) {
            return result;
        }
        result.setInvokeId(generated.getInvokeId());
        result.setPrompt(generated.getPrompt());
        result.setModel(generated.getModel());
        result.setProviderName(generated.getProviderName());
        result.setSize(generated.getSize());
        result.setN(generated.getN());
        result.setFileId(generated.getFileId());
        List<AiAsyncImageResultVO.AiImageItemVO> items = new ArrayList<>();
        if (!CollectionUtils.isEmpty(generated.getItems())) {
            for (AiImageGenerateResultVO.AiImageItemVO item : generated.getItems()) {
                if (item == null) {
                    continue;
                }
                AiAsyncImageResultVO.AiImageItemVO row = new AiAsyncImageResultVO.AiImageItemVO();
                row.setFileId(item.getFileId());
                row.setUrl(item.getUrl());
                items.add(row);
            }
        }
        result.setItems(items);
        if (StringUtils.isBlank(result.getFileId()) && !items.isEmpty()) {
            result.setFileId(items.get(0).getFileId());
        }
        if (StringUtils.isNotBlank(result.getFileId()) && StringUtils.isBlank(result.getFileName())) {
            result.setFileName("ai-image-" + result.getFileId() + ".png");
        }
        return result;
    }
}
