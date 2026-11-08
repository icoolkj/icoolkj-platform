package com.icoolkj.platform.api.controller.tenant;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.system.tenant.dto.TenantAdminPageQuery;
import com.icoolkj.platform.system.tenant.service.TenantAdminService;
import com.icoolkj.platform.system.tenant.vo.TenantAdminVO;
import com.icoolkj.platform.system.tenant.vo.TenantProvisionVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台：租户超级管理员运维（列表 / 重置密码 / 解锁）。
 * 预留后续「进入租户管理」能力入口同菜单扩展。
 */
@RestController
@RequestMapping("/tenant/admin")
public class TenantAdminController {

    private final TenantAdminService tenantAdminService;

    public TenantAdminController(TenantAdminService tenantAdminService) {
        this.tenantAdminService = tenantAdminService;
    }

    @PreAuthorize("@authz.hasPermission('tenant:admin:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<TenantAdminVO>> page(TenantAdminPageQuery query) {
        return ApiResponse.ok(tenantAdminService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('tenant:admin:resetPassword')")
    @OperationLog(module = "TenantAdmin", businessType = "UPDATE")
    @Idempotent("tenant:admin:resetPassword")
    @PutMapping("/{userId}/reset-password")
    public ApiResponse<TenantProvisionVO> resetPassword(@PathVariable String userId) {
        return ApiResponse.ok(tenantAdminService.resetPassword(userId));
    }

    @PreAuthorize("@authz.hasPermission('tenant:admin:unlock')")
    @OperationLog(module = "TenantAdmin", businessType = "UPDATE")
    @Idempotent("tenant:admin:unlock")
    @PutMapping("/{userId}/unlock")
    public ApiResponse<Void> unlock(@PathVariable String userId) {
        tenantAdminService.unlock(userId);
        return ApiResponse.ok();
    }
}
