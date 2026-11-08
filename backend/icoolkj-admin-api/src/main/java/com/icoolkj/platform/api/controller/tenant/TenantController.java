package com.icoolkj.platform.api.controller.tenant;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.system.tenant.dto.TenantCreateRequest;
import com.icoolkj.platform.system.tenant.dto.TenantPageQuery;
import com.icoolkj.platform.system.tenant.dto.TenantStatusUpdateRequest;
import com.icoolkj.platform.system.tenant.dto.TenantUpdateRequest;
import com.icoolkj.platform.system.tenant.service.TenantService;
import com.icoolkj.platform.system.tenant.vo.TenantProvisionVO;
import com.icoolkj.platform.system.tenant.vo.TenantVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 租户管理 */
@RestController
@RequestMapping("/tenant/tenant")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PreAuthorize("@authz.hasPermission('tenant:tenant:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<TenantVO>> page(TenantPageQuery query) {
        return ApiResponse.ok(tenantService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('tenant:tenant:list')")
    @GetMapping("/{id}")
    public ApiResponse<TenantVO> getById(@PathVariable String id) {
        return ApiResponse.ok(tenantService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('tenant:tenant:add')")
    @OperationLog(module = "Tenant", businessType = "CREATE")
    @Idempotent("tenant:tenant:create")
    @PostMapping("/create")
    public ApiResponse<TenantProvisionVO> create(@Valid @RequestBody TenantCreateRequest request) {
        return ApiResponse.ok(tenantService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('tenant:tenant:update')")
    @OperationLog(module = "Tenant", businessType = "UPDATE")
    @Idempotent("tenant:tenant:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody TenantUpdateRequest request) {
        tenantService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('tenant:tenant:update')")
    @OperationLog(module = "Tenant", businessType = "UPDATE")
    @Idempotent("tenant:tenant:status")
    @PutMapping("/status")
    public ApiResponse<Void> updateStatus(@Valid @RequestBody TenantStatusUpdateRequest request) {
        tenantService.updateStatus(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('tenant:tenant:delete')")
    @OperationLog(module = "Tenant", businessType = "DELETE")
    @Idempotent("tenant:tenant:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        tenantService.deleteById(id);
        return ApiResponse.ok();
    }
}
