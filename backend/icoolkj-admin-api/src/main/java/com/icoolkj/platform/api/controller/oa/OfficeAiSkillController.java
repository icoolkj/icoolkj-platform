package com.icoolkj.platform.api.controller.oa;

import com.icoolkj.platform.api.ai.skill.OfficeAiSkillChatService;
import com.icoolkj.platform.api.ai.skill.dto.OfficeAiChatRequest;
import com.icoolkj.platform.api.ai.skill.dto.OfficeAiChatResultVO;
import com.icoolkj.platform.api.ai.skill.dto.OfficeAiSkillVO;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.oa.vo.OaAiChatMessageVO;
import com.icoolkj.platform.oa.vo.OaAiChatSessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * User-facing office AI skills (catalog + session chat with permission-gated tools).
 */
@Tag(name = "Office AI Skills")
@RestController
@RequestMapping("/oa/office/ai")
public class OfficeAiSkillController {

    private final OfficeAiSkillChatService chatService;

    public OfficeAiSkillController(OfficeAiSkillChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(summary = "List office AI skills")
    @PreAuthorize("@authz.hasPermission('oa:office:ai:view')")
    @GetMapping("/skills")
    public ApiResponse<List<OfficeAiSkillVO>> listSkills() {
        return ApiResponse.ok(chatService.listSkills(LocaleContextHolder.getLocale()));
    }

    @Operation(summary = "Create office AI chat session")
    @PreAuthorize("@authz.hasPermission('oa:office:ai:chat')")
    @PostMapping("/sessions")
    public ApiResponse<OaAiChatSessionVO> createSession() {
        return ApiResponse.ok(chatService.createSession());
    }

    @Operation(summary = "Page my office AI chat sessions")
    @PreAuthorize("@authz.hasPermission('oa:office:ai:view')")
    @GetMapping("/sessions")
    public ApiResponse<PageResult<OaAiChatSessionVO>> pageSessions(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "50") long pageSize) {
        return ApiResponse.ok(chatService.pageSessions(pageNo, pageSize));
    }

    @Operation(summary = "List messages of my office AI session")
    @PreAuthorize("@authz.hasPermission('oa:office:ai:view')")
    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<List<OaAiChatMessageVO>> listMessages(@PathVariable String sessionId) {
        return ApiResponse.ok(chatService.listMessages(sessionId));
    }

    @Operation(summary = "Delete my office AI chat session")
    @PreAuthorize("@authz.hasPermission('oa:office:ai:chat')")
    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<Void> deleteSession(@PathVariable String sessionId) {
        chatService.deleteSession(sessionId);
        return ApiResponse.ok();
    }

    @Operation(summary = "Chat with office AI skills")
    @PreAuthorize("@authz.hasPermission('oa:office:ai:chat')")
    @PostMapping("/chat")
    public ApiResponse<OfficeAiChatResultVO> chat(@Valid @RequestBody OfficeAiChatRequest request) {
        return ApiResponse.ok(chatService.chat(
                request.getSessionId(),
                request.getMessage(),
                LocaleContextHolder.getLocale()));
    }
}
