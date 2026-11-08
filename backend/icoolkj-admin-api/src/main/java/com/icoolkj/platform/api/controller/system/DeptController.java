package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.system.dept.dto.DeptPageQuery;
import com.icoolkj.platform.system.dept.dto.DeptCreateRequest;
import com.icoolkj.platform.system.dept.dto.DeptUpdateRequest;
import com.icoolkj.platform.system.dept.service.DeptService;
import com.icoolkj.platform.system.dept.vo.DeptVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;


/** 部门管理 */
@RestController
@RequestMapping("/system/dept")
public class DeptController {

    private final DeptService deptService;

    public DeptController(DeptService deptService) {
        this.deptService = deptService;
    }

    @PreAuthorize("@authz.hasPermission('system:dept:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<DeptVO>> page(DeptPageQuery query) {
        return ApiResponse.ok(deptService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('system:dept:list')")
    @GetMapping("/{id}")
    public ApiResponse<DeptVO> getById(@PathVariable String id) {
        return ApiResponse.ok(deptService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('system:dept:add')")
    @OperationLog(module = "System Dept", businessType = "CREATE")
    @Idempotent("system:dept:create")
    @PostMapping("/create")
    public ApiResponse<String> create(@Valid @RequestBody DeptCreateRequest request) {
        return ApiResponse.ok(deptService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('system:dept:update')")
    @OperationLog(module = "System Dept", businessType = "UPDATE")
    @Idempotent("system:dept:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody DeptUpdateRequest request) {
        deptService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:dept:delete')")
    @OperationLog(module = "System Dept", businessType = "DELETE")
    @Idempotent("system:dept:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        deptService.deleteById(id);
        return ApiResponse.ok();
    }

}
