package com.icoolkj.platform.generator.controller;

import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.generator.async.AiImageAsyncSubmitFacade;
import com.icoolkj.platform.generator.constant.AiPermissionConstants;
import com.icoolkj.platform.generator.dto.AiImageGenerateRequest;
import com.icoolkj.platform.generator.service.impl.AiImageService;
import com.icoolkj.platform.generator.vo.AiImageGenerateResultVO;
import com.icoolkj.platform.infra.vo.AsyncTaskSubmitVO;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(prefix = "icoolkj.generator", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequestMapping("/tool/ai-image")
public class AiImageController {

    private final AiImageService aiImageService;
    private final AiImageAsyncSubmitFacade aiImageAsyncSubmitFacade;

    public AiImageController(AiImageService aiImageService, AiImageAsyncSubmitFacade aiImageAsyncSubmitFacade) {
        this.aiImageService = aiImageService;
        this.aiImageAsyncSubmitFacade = aiImageAsyncSubmitFacade;
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.IMAGE + "')")
    @OperationLog(module = "AI Image", businessType = "GENERATE")
    @Idempotent("tool:ai:image:generate")
    @PostMapping("/generate")
    public ApiResponse<AiImageGenerateResultVO> generate(@Valid @RequestBody AiImageGenerateRequest request) {
        return ApiResponse.ok(aiImageService.generate(request));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.IMAGE + "')")
    @OperationLog(module = "AI Image", businessType = "GENERATE_ASYNC")
    @Idempotent("tool:ai:image:generate-async")
    @PostMapping("/generate-async")
    public ApiResponse<AsyncTaskSubmitVO> generateAsync(@Valid @RequestBody AiImageGenerateRequest request)
            throws Exception {
        return ApiResponse.ok(aiImageAsyncSubmitFacade.submit(request));
    }
}
