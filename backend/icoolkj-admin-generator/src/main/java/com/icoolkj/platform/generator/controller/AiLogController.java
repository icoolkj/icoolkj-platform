package com.icoolkj.platform.generator.controller;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.generator.constant.AiPermissionConstants;
import com.icoolkj.platform.generator.dto.ToolAiDdlCopyRequest;
import com.icoolkj.platform.generator.dto.ToolAiDdlRecordPageQuery;
import com.icoolkj.platform.generator.dto.ToolAiInvokeLogPageQuery;
import com.icoolkj.platform.generator.service.impl.AiChatFacade;
import com.icoolkj.platform.generator.vo.ToolAiDdlRecordVO;
import com.icoolkj.platform.generator.vo.ToolAiInvokeLogVO;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** AI 审计日志 — 调用记录 / DDL 记录 */
@RestController
@ConditionalOnProperty(prefix = "icoolkj.generator", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequestMapping("/tool/ai-log")
public class AiLogController {

    private final AiChatFacade aiChatFacade;

    public AiLogController(AiChatFacade aiChatFacade) {
        this.aiChatFacade = aiChatFacade;
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.QUERY + "')")
    @GetMapping("/invoke/page")
    public ApiResponse<PageResult<ToolAiInvokeLogVO>> pageInvoke(ToolAiInvokeLogPageQuery query) {
        return ApiResponse.ok(aiChatFacade.pageInvoke(query));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.QUERY + "')")
    @GetMapping("/invoke/{id}")
    public ApiResponse<ToolAiInvokeLogVO> getInvoke(@PathVariable String id) {
        return ApiResponse.ok(aiChatFacade.getInvokeDetail(id));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.QUERY + "')")
    @GetMapping("/ddl/page")
    public ApiResponse<PageResult<ToolAiDdlRecordVO>> pageDdl(ToolAiDdlRecordPageQuery query) {
        return ApiResponse.ok(aiChatFacade.pageDdl(query));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.QUERY + "')")
    @GetMapping("/ddl/{id}")
    public ApiResponse<ToolAiDdlRecordVO> getDdl(@PathVariable String id) {
        return ApiResponse.ok(aiChatFacade.getDdlDetail(id));
    }

    @PreAuthorize("@authz.hasPermission('" + AiPermissionConstants.GENERATE + "')")
    @OperationLog(module = "AI DDL Log", businessType = "COPY")
    @Idempotent("tool:ai:ddlCopied")
    @PostMapping("/ddl/{id}/copied")
    public ApiResponse<Void> markDdlCopied(@PathVariable String id,
                                           @Valid @RequestBody(required = false) ToolAiDdlCopyRequest request) {
        String finalDdl = request != null ? request.getFinalDdl() : null;
        aiChatFacade.markDdlCopied(id, finalDdl);
        return ApiResponse.ok();
    }
}
