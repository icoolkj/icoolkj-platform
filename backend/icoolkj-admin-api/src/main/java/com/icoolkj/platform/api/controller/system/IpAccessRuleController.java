package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.system.iprule.dto.IpAccessRuleCreateRequest;
import com.icoolkj.platform.system.iprule.dto.IpAccessRulePageQuery;
import com.icoolkj.platform.system.iprule.dto.IpAccessRuleUpdateRequest;
import com.icoolkj.platform.system.iprule.service.IpAccessRuleService;
import com.icoolkj.platform.system.iprule.vo.IpAccessRuleVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** IP访问规则管理 */
@RestController
@RequestMapping("/system/ipAccessRule")
public class IpAccessRuleController {

    private final IpAccessRuleService ipAccessRuleService;

    public IpAccessRuleController(IpAccessRuleService ipAccessRuleService) {
        this.ipAccessRuleService = ipAccessRuleService;
    }

    @PreAuthorize("@authz.hasPermission('system:ipAccessRule:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<IpAccessRuleVO>> page(IpAccessRulePageQuery query) {
        return ApiResponse.ok(ipAccessRuleService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('system:ipAccessRule:list')")
    @GetMapping("/{id}")
    public ApiResponse<IpAccessRuleVO> getById(@PathVariable String id) {
        return ApiResponse.ok(ipAccessRuleService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('system:ipAccessRule:add')")
    @OperationLog(module = "Ip Access Rule", businessType = "CREATE")
    @Idempotent("system:ipAccessRule:create")
    @PostMapping("/create")
    public ApiResponse<String> create(@Valid @RequestBody IpAccessRuleCreateRequest request) {
        return ApiResponse.ok(ipAccessRuleService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('system:ipAccessRule:update')")
    @OperationLog(module = "Ip Access Rule", businessType = "UPDATE")
    @Idempotent("system:ipAccessRule:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody IpAccessRuleUpdateRequest request) {
        ipAccessRuleService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:ipAccessRule:delete')")
    @OperationLog(module = "Ip Access Rule", businessType = "DELETE")
    @Idempotent("system:ipAccessRule:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        ipAccessRuleService.deleteById(id);
        return ApiResponse.ok();
    }
}
