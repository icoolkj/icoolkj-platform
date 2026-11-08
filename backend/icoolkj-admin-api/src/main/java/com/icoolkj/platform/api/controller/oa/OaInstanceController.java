package com.icoolkj.platform.api.controller.oa;

import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.oa.dto.OaInstanceFormSaveRequest;
import com.icoolkj.platform.oa.service.OaInstanceService;
import com.icoolkj.platform.oa.vo.OaInstanceApproveDetailVO;
import com.icoolkj.platform.oa.vo.OaInstanceFormSchemaVO;
import com.icoolkj.platform.oa.vo.OaInstanceVO;
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

/** 租户内统一 OA 单据 */
@RestController
@RequestMapping("/oa/instance")
public class OaInstanceController {

    private final OaInstanceService instanceService;

    public OaInstanceController(OaInstanceService instanceService) {
        this.instanceService = instanceService;
    }

    @PreAuthorize("@authz.hasPermission('oa:instance:add')")
    @GetMapping("/{categoryCode}/form-schema")
    public ApiResponse<OaInstanceFormSchemaVO> getCreateFormSchema(@PathVariable String categoryCode) {
        return ApiResponse.ok(instanceService.getCreateFormSchema(categoryCode));
    }

    @PreAuthorize("@authz.hasAnyPermission('oa:instance:query', 'oa:instance:list')")
    @GetMapping("/{id}")
    public ApiResponse<OaInstanceVO> getById(@PathVariable String id) {
        return ApiResponse.ok(instanceService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('oa:instance:add')")
    @OperationLog(module = "OA Instance", businessType = "CREATE")
    @Idempotent("oa:instance:create")
    @PostMapping("/{categoryCode}/create")
    public ApiResponse<String> create(@PathVariable String categoryCode,
                                      @Valid @RequestBody OaInstanceFormSaveRequest request) {
        return ApiResponse.ok(instanceService.create(categoryCode, request));
    }

    @PreAuthorize("@authz.hasPermission('oa:instance:update')")
    @OperationLog(module = "OA Instance", businessType = "UPDATE")
    @Idempotent("oa:instance:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody OaInstanceFormSaveRequest request) {
        instanceService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('oa:instance:delete')")
    @OperationLog(module = "OA Instance", businessType = "DELETE")
    @Idempotent("oa:instance:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        instanceService.deleteById(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('oa:instance:submit')")
    @OperationLog(module = "OA Instance", businessType = "SUBMIT")
    @Idempotent("oa:instance:submit")
    @PostMapping("/{id}/submit-approve")
    public ApiResponse<String> submitApprove(@PathVariable String id) {
        return ApiResponse.ok(instanceService.submitApprove(id));
    }

    @PreAuthorize("@authz.hasPermission('oa:instance:close')")
    @OperationLog(module = "OA Instance", businessType = "CLOSE")
    @Idempotent("oa:instance:close")
    @PostMapping("/{id}/close")
    public ApiResponse<Void> close(@PathVariable String id) {
        instanceService.close(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('workflow:approve:oa:view')"
            + " or @authz.hasPermission('workflow:submitted:view')"
            + " or @authz.hasPermission('workflow:todo:view')"
            + " or @authz.hasPermission('workflow:done:view')"
            + " or @authz.hasPermission('workflow:cc:view')"
            + " or @authz.hasPermission('oa:office:draft:view')")
    @GetMapping("/{id}/approve-detail")
    public ApiResponse<OaInstanceApproveDetailVO> getApproveDetail(@PathVariable String id) {
        return ApiResponse.ok(instanceService.getApproveDetail(id));
    }
}
