package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.system.role.dto.RoleMenuAssignRequest;
import com.icoolkj.platform.system.role.dto.RolePageQuery;
import com.icoolkj.platform.system.role.dto.RoleCreateRequest;
import com.icoolkj.platform.system.role.dto.RoleUpdateRequest;
import com.icoolkj.platform.system.role.service.RoleMenuService;
import com.icoolkj.platform.system.role.service.RoleService;
import com.icoolkj.platform.system.menu.vo.MenuVO;
import com.icoolkj.platform.system.role.vo.RoleVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;


/** 角色管理 */
@RestController
@RequestMapping("/system/role")
public class RoleController {

    private final RoleService roleService;
    private final RoleMenuService roleMenuService;

    public RoleController(RoleService roleService, RoleMenuService roleMenuService) {
        this.roleService = roleService;
        this.roleMenuService = roleMenuService;
    }

    @PreAuthorize("@authz.hasPermission('system:role:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<RoleVO>> page(RolePageQuery query) {
        return ApiResponse.ok(roleService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('system:role:list')")
    @GetMapping("/{id}")
    public ApiResponse<RoleVO> getById(@PathVariable String id) {
        return ApiResponse.ok(roleService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('system:role:add')")
    @OperationLog(module = "System Role", businessType = "CREATE")
    @Idempotent("system:role:create")
    @PostMapping("/create")
    public ApiResponse<String> create(@Valid @RequestBody RoleCreateRequest request) {
        return ApiResponse.ok(roleService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('system:role:update')")
    @OperationLog(module = "System Role", businessType = "UPDATE")
    @Idempotent("system:role:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody RoleUpdateRequest request) {
        roleService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:role:delete')")
    @OperationLog(module = "System Role", businessType = "DELETE")
    @Idempotent("system:role:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        roleService.deleteById(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:role:list')")
    @GetMapping("/{id}/menuIds")
    public ApiResponse<List<String>> listMenuIds(@PathVariable String id) {
        return ApiResponse.ok(roleMenuService.listMenuIdsByRole(id));
    }

    @PreAuthorize("@authz.hasPermission('system:role:update')")
    @GetMapping("/{id}/assignable-menus")
    public ApiResponse<List<MenuVO>> assignableMenus(
            @PathVariable String id,
            @RequestParam(value = "productCode", required = false) String productCode) {
        return ApiResponse.ok(roleMenuService.listAssignableMenus(id, productCode));
    }

    @PreAuthorize("@authz.hasPermission('system:role:update')")
    @OperationLog(module = "System Role", businessType = "ASSIGN_MENU")
    @Idempotent("system:role:assignMenus")
    @PutMapping("/{id}/menus")
    public ApiResponse<Void> assignMenus(@PathVariable String id, @Valid @RequestBody RoleMenuAssignRequest request) {
        roleMenuService.assignMenus(id, request.getMenuIds());
        return ApiResponse.ok();
    }

}
