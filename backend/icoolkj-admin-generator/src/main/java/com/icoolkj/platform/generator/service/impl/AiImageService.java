package com.icoolkj.platform.generator.service.impl;

import com.icoolkj.platform.common.constant.AiCapability;
import com.icoolkj.platform.common.constant.AiInvokeBizType;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.logging.LogContext;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.generator.domain.ToolAiInvokeContent;
import com.icoolkj.platform.generator.domain.ToolAiInvokeLog;
import com.icoolkj.platform.generator.dto.AiImageGenerateRequest;
import com.icoolkj.platform.generator.mapper.ToolAiLogMapper;
import com.icoolkj.platform.generator.vo.AiImageGenerateResultVO;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.infra.vo.InfraFileVO;
import com.icoolkj.platform.system.ai.client.OpenAiCompatibleImageClient;
import com.icoolkj.platform.system.ai.client.OpenAiImageGenerateOptions;
import com.icoolkj.platform.system.ai.client.OpenAiImageResult;
import com.icoolkj.platform.system.ai.protocol.openai.OpenAiCompatibleEndpoints;
import com.icoolkj.platform.system.ai.service.AiCapabilityRuntimeService;
import com.icoolkj.platform.system.ai.support.AiResolvedRuntime;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiImageService {

    private static final Logger log = LoggerFactory.getLogger(AiImageService.class);
    private static final String BIZ_TYPE = "ai_image_gen";
    private static final int PREVIEW_LEN = 500;

    private final AiCapabilityRuntimeService capabilityRuntimeService;
    private final OpenAiCompatibleImageClient imageClient;
    private final InfraFileService infraFileService;
    private final ToolAiLogMapper toolAiLogMapper;

    public AiImageService(
            AiCapabilityRuntimeService capabilityRuntimeService,
            OpenAiCompatibleImageClient imageClient,
            InfraFileService infraFileService,
            ToolAiLogMapper toolAiLogMapper) {
        this.capabilityRuntimeService = capabilityRuntimeService;
        this.imageClient = imageClient;
        this.infraFileService = infraFileService;
        this.toolAiLogMapper = toolAiLogMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public AiImageGenerateResultVO generate(AiImageGenerateRequest request) {
        if (request == null || StringUtils.isBlank(request.getPrompt())) {
            throw new BusinessException("error.ai.image_prompt_required");
        }
        String trimmed = request.getPrompt().trim();
        AiResolvedRuntime runtime = capabilityRuntimeService.resolve(AiCapability.IMAGE);
        if (!runtime.isEnabled()) {
            throw new BusinessException("error.ai.not_enabled");
        }
        if (StringUtils.isBlank(runtime.getApiKey())) {
            throw new BusinessException("error.ai.api_key_missing");
        }

        OpenAiImageGenerateOptions options = new OpenAiImageGenerateOptions();
        options.setSize(request.getSize());
        options.setN(request.getN());
        options.setNegativePrompt(request.getNegativePrompt());
        options.setResponseFormat(request.getResponseFormat());
        options.setPromptExtend(request.getPromptExtend());
        options.setWatermark(request.getWatermark());
        options.setDefaultParamsJson(runtime.getDefaultParamsJson());

        String invokeId = IdGeneratorUtils.nextIdStr();
        String requestUrl = OpenAiCompatibleEndpoints.absoluteUrl(
                runtime.getBaseUrl(), OpenAiCompatibleEndpoints.IMAGES_GENERATIONS);
        String providerName = StringUtils.defaultIfBlank(runtime.getProviderName(), "AI");
        long start = System.currentTimeMillis();
        try {
            List<OpenAiImageResult> images = imageClient.generateAll(runtime, trimmed, options);
            List<AiImageGenerateResultVO.AiImageItemVO> items = new ArrayList<>();
            List<String> fileIds = new ArrayList<>();
            int i = 0;
            for (OpenAiImageResult image : images) {
                InfraFileVO file = infraFileService.storeBinary(
                        image.getContent(),
                        image.getContentType(),
                        image.getFilename(),
                        null,
                        BIZ_TYPE,
                        invokeId,
                        "image" + (i == 0 ? "" : "-" + i),
                        "AI text-to-image");
                String url = infraFileService.resolveAccessibleUrl(file.getId(), file.getFileUrl());
                AiImageGenerateResultVO.AiImageItemVO item = new AiImageGenerateResultVO.AiImageItemVO();
                item.setFileId(file.getId());
                item.setUrl(url);
                items.add(item);
                fileIds.add(file.getId());
                i++;
            }
            int cost = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - start);
            saveInvoke(
                    invokeId,
                    providerName,
                    runtime.getModel(),
                    requestUrl,
                    "0",
                    null,
                    cost,
                    trimmed,
                    "fileIds=" + String.join(",", fileIds));

            AiImageGenerateResultVO vo = new AiImageGenerateResultVO();
            vo.setInvokeId(invokeId);
            vo.setPrompt(trimmed);
            vo.setModel(runtime.getModel());
            vo.setProviderName(providerName);
            vo.setSize(StringUtils.defaultIfBlank(request.getSize(), "1024x1024"));
            vo.setN(items.size());
            vo.setItems(items);
            if (!items.isEmpty()) {
                vo.setFileId(items.get(0).getFileId());
                vo.setUrl(items.get(0).getUrl());
            }
            return vo;
        } catch (BusinessException ex) {
            int cost = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - start);
            saveInvoke(invokeId, providerName, runtime.getModel(), requestUrl, "1", ex.getMessage(), cost,
                    trimmed, null);
            throw ex;
        } catch (Exception ex) {
            int cost = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - start);
            saveInvoke(invokeId, providerName, runtime.getModel(), requestUrl, "1", ex.getMessage(), cost,
                    trimmed, null);
            throw new BusinessException("error.ai.image_request_failed", ex.getMessage());
        }
    }

    private void saveInvoke(
            String invokeId,
            String providerName,
            String model,
            String requestUrl,
            String status,
            String errorMessage,
            int costTime,
            String userMessage,
            String responseContent) {
        try {
            ToolAiInvokeLog row = new ToolAiInvokeLog();
            row.setId(invokeId);
            row.setTenantId(currentTenantId());
            row.setTraceId(LogContext.getTraceId());
            row.setProviderName(providerName);
            row.setModel(model);
            row.setBizType(AiInvokeBizType.IMAGE_GEN);
            row.setBizRefId(invokeId);
            row.setRequestUrl(requestUrl);
            row.setResponseStatus(status);
            row.setErrorMessage(truncate(errorMessage, 1000));
            row.setCostTime(costTime);
            row.setUserPromptPreview(truncate(userMessage, PREVIEW_LEN));
            row.setResponsePreview(truncate(responseContent, PREVIEW_LEN));
            row.setOperatorId(SecurityContextHelper.getUserId());
            row.setOperatorName(SecurityContextHelper.getUsername());
            row.setCreatedAt(LocalDateTime.now());
            toolAiLogMapper.insertInvokeLog(row);

            ToolAiInvokeContent content = new ToolAiInvokeContent();
            content.setInvokeId(invokeId);
            content.setUserMessage(userMessage);
            content.setResponseContent(responseContent);
            toolAiLogMapper.insertInvokeContent(content);
        } catch (Exception ex) {
            log.warn("Save AI image invoke log failed, invokeId={}", invokeId, ex);
        }
    }

    private static String currentTenantId() {
        String tid = SecurityContextHelper.getTenantId();
        if (!StringUtils.isNotBlank(tid)) {
            tid = TenantContextHolder.getTenantId();
        }
        return StringUtils.isNotBlank(tid) ? tid : TenantConstants.PLATFORM_TENANT_ID;
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max) + "…";
    }
}
