package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.system.sitemessage.dto.SiteMessageBatchDeleteRequest;
import com.icoolkj.platform.system.sitemessage.dto.SiteMessagePageQuery;
import com.icoolkj.platform.system.sitemessage.dto.SiteMessageSendRequest;
import com.icoolkj.platform.system.sitemessage.service.SiteMessageService;
import com.icoolkj.platform.system.sitemessage.dto.SiteMessageRecipientPageQuery;
import com.icoolkj.platform.system.sitemessage.vo.SiteMessageRecipientStatusVO;
import com.icoolkj.platform.system.sitemessage.vo.SiteMessageVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 站内信管理 */
@RestController
@RequestMapping("/system/siteMessage")
public class SiteMessageController {

    private final SiteMessageService siteMessageService;

    public SiteMessageController(SiteMessageService siteMessageService) {
        this.siteMessageService = siteMessageService;
    }

    @PreAuthorize("@authz.hasPermission('system:siteMessage:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<SiteMessageVO>> page(SiteMessagePageQuery query) {
        return ApiResponse.ok(siteMessageService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('system:siteMessage:list')")
    @GetMapping("/{id}")
    public ApiResponse<SiteMessageVO> getById(@PathVariable String id) {
        return ApiResponse.ok(siteMessageService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('system:siteMessage:list')")
    @GetMapping("/{id}/recipients/page")
    public ApiResponse<PageResult<SiteMessageRecipientStatusVO>> recipientPage(@PathVariable String id,
                                                                               SiteMessageRecipientPageQuery query) {
        return ApiResponse.ok(siteMessageService.recipientPage(id, query));
    }

    @PreAuthorize("@authz.hasPermission('system:siteMessage:send')")
    @OperationLog(module = "Site Message", businessType = "INSERT")
    @Idempotent("system:siteMessage:send")
    @PostMapping("/send")
    public ApiResponse<String> send(@Valid @RequestBody SiteMessageSendRequest request) {
        return ApiResponse.ok(siteMessageService.send(request));
    }

    @PreAuthorize("@authz.hasPermission('system:siteMessage:delete')")
    @OperationLog(module = "Site Message", businessType = "DELETE")
    @Idempotent("system:siteMessage:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        siteMessageService.deleteById(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:siteMessage:delete')")
    @OperationLog(module = "Site Message", businessType = "DELETE")
    @Idempotent("system:siteMessage:batchDelete")
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody SiteMessageBatchDeleteRequest request) {
        siteMessageService.batchDelete(request);
        return ApiResponse.ok();
    }
}
