package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.security.model.LoginSession;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.framework.security.service.LoginSessionService;
import com.icoolkj.platform.system.auth.vo.OnlineUserVO;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 在线用户 / 强制下线 */
@RestController
@RequestMapping("/monitor/online")
public class OnlineUserController {

    private final LoginSessionService loginSessionService;

    public OnlineUserController(LoginSessionService loginSessionService) {
        this.loginSessionService = loginSessionService;
    }

    @PreAuthorize("@authz.hasPermission('monitor:online:list')")
    @GetMapping("/list")
    public ApiResponse<List<OnlineUserVO>> list() {
        LoginUser loginUser = SecurityContextHelper.getLoginUser();
        String tenantFilter = isPlatformOperator(loginUser) ? null : loginUser.getTenantId();
        List<OnlineUserVO> rows = loginSessionService.listOnlineSessions(tenantFilter).stream()
                .map(this::toVo)
                .toList();
        return ApiResponse.ok(rows);
    }

    @OperationLog(module = "Online User", businessType = "FORCE_LOGOUT")
    @Idempotent("monitor:online:forceLogout")
    @PreAuthorize("@authz.hasPermission('monitor:online:forceLogout')")
    @DeleteMapping("/{sessionId}")
    public ApiResponse<Void> forceLogout(@PathVariable String sessionId) {
        LoginUser loginUser = SecurityContextHelper.getLoginUser();
        LoginSession session = loginSessionService.getSession(sessionId).orElse(null);
        if (session == null) {
            return ApiResponse.ok();
        }
        if (!isPlatformOperator(loginUser)
                && StringUtils.hasText(loginUser.getTenantId())
                && !loginUser.getTenantId().equals(session.getTenantId())) {
            throw new BusinessException("error.forbidden");
        }
        loginSessionService.removeSession(sessionId);
        return ApiResponse.ok();
    }

    private boolean isPlatformOperator(LoginUser loginUser) {
        return loginUser != null
                && TenantConstants.PLATFORM_TENANT_ID.equals(loginUser.getTenantId());
    }

    private OnlineUserVO toVo(LoginSession session) {
        OnlineUserVO vo = new OnlineUserVO();
        vo.setSessionId(session.getSessionId());
        vo.setUserId(session.getUserId());
        vo.setUsername(session.getUsername());
        vo.setTenantId(session.getTenantId());
        vo.setTenantCode(session.getTenantCode());
        vo.setCreatedAt(session.getCreatedAt());
        return vo;
    }
}
