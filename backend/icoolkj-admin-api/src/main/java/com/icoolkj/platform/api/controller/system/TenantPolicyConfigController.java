package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.system.tenant.dto.TenantPolicyConfigUpdateRequest;
import com.icoolkj.platform.system.tenant.service.TenantPolicyConfigService;
import com.icoolkj.platform.system.tenant.vo.TenantPolicyConfigItemVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Tenant self-service policy config (overridable keys only).
 */
@RestController
@RequestMapping("/system/tenant-policy")
public class TenantPolicyConfigController {

    private final TenantPolicyConfigService tenantPolicyConfigService;

    public TenantPolicyConfigController(TenantPolicyConfigService tenantPolicyConfigService) {
        this.tenantPolicyConfigService = tenantPolicyConfigService;
    }

    @PreAuthorize("@authz.hasPermission('system:tenantPolicy:query')")
    @GetMapping("/catalog")
    public ApiResponse<List<TenantPolicyConfigItemVO>> catalog() {
        return ApiResponse.ok(tenantPolicyConfigService.catalog());
    }

    @PreAuthorize("@authz.hasPermission('system:tenantPolicy:update')")
    @OperationLog(module = "TenantPolicy", businessType = "UPDATE")
    @Idempotent("system:tenantPolicy:update")
    @PutMapping("/{configKey:.+}")
    public ApiResponse<Void> upsert(@PathVariable("configKey") String configKey,
                                    @Valid @RequestBody TenantPolicyConfigUpdateRequest request) {
        tenantPolicyConfigService.upsert(configKey, request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:tenantPolicy:update')")
    @OperationLog(module = "TenantPolicy", businessType = "RESET")
    @Idempotent("system:tenantPolicy:reset")
    @DeleteMapping("/{configKey:.+}")
    public ApiResponse<Void> reset(@PathVariable("configKey") String configKey) {
        tenantPolicyConfigService.reset(configKey);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:tenantPolicy:update')")
    @OperationLog(module = "TenantPolicy", businessType = "REFRESH_CACHE")
    @Idempotent("system:tenantPolicy:refreshCache")
    @PostMapping("/refreshCache")
    public ApiResponse<Void> refreshCache() {
        tenantPolicyConfigService.refreshCache();
        return ApiResponse.ok();
    }
}
