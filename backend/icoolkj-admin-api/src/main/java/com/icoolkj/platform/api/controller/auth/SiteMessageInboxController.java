package com.icoolkj.platform.api.controller.auth;

import com.icoolkj.platform.common.exception.UnauthorizedException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.system.sitemessage.dto.SiteMessageInboxPageQuery;
import com.icoolkj.platform.system.sitemessage.service.SiteMessageService;
import com.icoolkj.platform.system.sitemessage.vo.SiteMessageInboxVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 当前用户站内信收件箱 */
@RestController
@RequestMapping("/auth/siteMessage")
public class SiteMessageInboxController {

    private final SiteMessageService siteMessageService;

    public SiteMessageInboxController(SiteMessageService siteMessageService) {
        this.siteMessageService = siteMessageService;
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount() {
        LoginUser loginUser = requireLoginUser();
        return ApiResponse.ok(siteMessageService.countUnread(loginUser.getTenantId(), loginUser.getUserId()));
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<SiteMessageInboxVO>> page(SiteMessageInboxPageQuery query) {
        LoginUser loginUser = requireLoginUser();
        return ApiResponse.ok(siteMessageService.inboxPage(query));
    }

    @GetMapping("/{recipientId}")
    public ApiResponse<SiteMessageInboxVO> detail(@PathVariable String recipientId) {
        LoginUser loginUser = requireLoginUser();
        return ApiResponse.ok(siteMessageService.inboxDetail(
                recipientId, loginUser.getTenantId(), loginUser.getUserId()));
    }

    @OperationLog(module = "Site Message Inbox", businessType = "UPDATE")
    @Idempotent("auth:siteMessage:read")
    @PutMapping("/{recipientId}/read")
    public ApiResponse<Void> markRead(@PathVariable String recipientId) {
        LoginUser loginUser = requireLoginUser();
        siteMessageService.markRead(recipientId, loginUser.getTenantId(), loginUser.getUserId());
        return ApiResponse.ok();
    }

    private LoginUser requireLoginUser() {
        LoginUser loginUser = SecurityContextHelper.getLoginUser();
        if (loginUser == null) {
            throw new UnauthorizedException("error.unauthorized");
        }
        return loginUser;
    }
}
