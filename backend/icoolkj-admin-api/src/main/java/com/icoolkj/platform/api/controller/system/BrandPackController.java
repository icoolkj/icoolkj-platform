package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.system.brandpack.dto.BrandPackUpdateRequest;
import com.icoolkj.platform.system.brandpack.service.BrandPackAdminService;
import com.icoolkj.platform.system.brandpack.vo.BrandPackAdminVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站点品牌 / OEM：平台与业务租户统一维护 {@code sys_brand_pack}。
 */
@RestController
@RequestMapping("/system/brand-pack")
public class BrandPackController {

    private final BrandPackAdminService brandPackAdminService;

    public BrandPackController(BrandPackAdminService brandPackAdminService) {
        this.brandPackAdminService = brandPackAdminService;
    }

    @PreAuthorize("@authz.hasPermission('system:brandPack:query')")
    @GetMapping
    public ApiResponse<BrandPackAdminVO> get(
            @RequestParam(value = "targetTenantId", required = false) String targetTenantId) {
        return ApiResponse.ok(brandPackAdminService.getForAdmin(targetTenantId));
    }

    @PreAuthorize("@authz.hasPermission('system:brandPack:update')")
    @OperationLog(module = "BrandPack", businessType = "UPDATE")
    @Idempotent("system:brandPack:update")
    @PutMapping
    public ApiResponse<Void> update(
            @RequestParam(value = "targetTenantId", required = false) String targetTenantId,
            @Valid @RequestBody BrandPackUpdateRequest request) {
        brandPackAdminService.update(targetTenantId, request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:brandPack:update')")
    @OperationLog(module = "BrandPack", businessType = "DELETE")
    @Idempotent("system:brandPack:reset")
    @DeleteMapping
    public ApiResponse<Void> reset(
            @RequestParam(value = "targetTenantId", required = false) String targetTenantId) {
        brandPackAdminService.resetOverride(targetTenantId);
        return ApiResponse.ok();
    }
}
