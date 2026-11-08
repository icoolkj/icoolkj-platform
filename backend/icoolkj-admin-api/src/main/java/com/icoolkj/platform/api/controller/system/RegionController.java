package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.system.region.dto.RegionCreateRequest;
import com.icoolkj.platform.system.region.dto.RegionPageQuery;
import com.icoolkj.platform.system.region.dto.RegionUpdateRequest;
import com.icoolkj.platform.system.region.service.RegionService;
import com.icoolkj.platform.system.region.vo.RegionVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 国标区划（平台维护，租户只读引用） */
@RestController
@RequestMapping("/system/region")
public class RegionController {

    private final RegionService regionService;

    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    @PreAuthorize("@authz.hasPermission('system:region:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<RegionVO>> page(RegionPageQuery query) {
        return ApiResponse.ok(regionService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('system:region:list')")
    @GetMapping("/tree")
    public ApiResponse<List<RegionVO>> tree() {
        return ApiResponse.ok(regionService.listTree());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/tenant-tree")
    public ApiResponse<List<RegionVO>> tenantTree() {
        return ApiResponse.ok(regionService.listTenantTree());
    }

    @PreAuthorize("@authz.hasPermission('tenant:tenant:list')")
    @GetMapping("/selectable-roots")
    public ApiResponse<List<RegionVO>> selectableRoots(@RequestParam(required = false) String tenantType) {
        return ApiResponse.ok(regionService.listSelectableRoots(tenantType));
    }

    @PreAuthorize("@authz.hasPermission('system:region:list')")
    @GetMapping("/{id}")
    public ApiResponse<RegionVO> getById(@PathVariable String id) {
        return ApiResponse.ok(regionService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('system:region:add')")
    @OperationLog(module = "System Region", businessType = "CREATE")
    @Idempotent("system:region:create")
    @PostMapping("/create")
    public ApiResponse<String> create(@Valid @RequestBody RegionCreateRequest request) {
        return ApiResponse.ok(regionService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('system:region:update')")
    @OperationLog(module = "System Region", businessType = "UPDATE")
    @Idempotent("system:region:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody RegionUpdateRequest request) {
        regionService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:region:delete')")
    @OperationLog(module = "System Region", businessType = "DELETE")
    @Idempotent("system:region:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        regionService.deleteById(id);
        return ApiResponse.ok();
    }
}
