package com.icoolkj.platform.generator.controller;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.generator.constant.AiPermissionConstants;
import com.icoolkj.platform.generator.dto.ToolAiChatSendRequest;
import com.icoolkj.platform.generator.dto.ToolAiChatSessionPageQuery;
import com.icoolkj.platform.generator.service.impl.AiChatSessionService;
import com.icoolkj.platform.generator.vo.ToolAiChatSendResultVO;
import com.icoolkj.platform.generator.vo.ToolAiChatSessionVO;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** AI 智能对话 — /tool/ai-chat */
@RestController
@ConditionalOnProperty(prefix = "icoolkj.generator", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequestMapping("/tool/ai-chat")
public class AiChatController {

    private final AiChatSessionService aiChatSessionService;

    public AiChatController(AiChatSessionService aiChatSessionService) {
        this.aiChatSessionService = aiChatSessionService;
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.QUERY + "')")
    @GetMapping("/sessions/page")
    public ApiResponse<PageResult<ToolAiChatSessionVO>> pageSessions(ToolAiChatSessionPageQuery query) {
        return ApiResponse.ok(aiChatSessionService.pageSessions(query));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.GENERATE + "')")
    @OperationLog(module = "AI Chat", businessType = "CREATE_SESSION")
    @Idempotent("tool:ai:chatSession")
    @PostMapping("/sessions")
    public ApiResponse<ToolAiChatSessionVO> createSession() {
        return ApiResponse.ok(aiChatSessionService.createSession());
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.QUERY + "')")
    @GetMapping("/sessions/{id}")
    public ApiResponse<ToolAiChatSessionVO> getSession(@PathVariable String id,
                                                       @RequestParam(defaultValue = "true") boolean mineOnly) {
        return ApiResponse.ok(aiChatSessionService.getSession(id, mineOnly));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.GENERATE + "')")
    @OperationLog(module = "AI Chat", businessType = "SEND")
    @PostMapping("/sessions/{id}/messages")
    public ApiResponse<ToolAiChatSendResultVO> sendMessage(@PathVariable String id,
                                                           @Valid @RequestBody ToolAiChatSendRequest request) {
        return ApiResponse.ok(aiChatSessionService.sendMessage(id, request));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.GENERATE + "')")
    @OperationLog(module = "AI Chat", businessType = "ARCHIVE")
    @DeleteMapping("/sessions/{id}")
    public ApiResponse<Void> archiveSession(@PathVariable String id) {
        aiChatSessionService.archiveSession(id);
        return ApiResponse.ok();
    }
}
