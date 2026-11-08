package com.icoolkj.platform.api.controller.workflow;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.workflow.dto.WorkflowBizBindingPolicyCreateRequest;
import com.icoolkj.platform.workflow.dto.WorkflowBizBindingPolicyPageQuery;
import com.icoolkj.platform.workflow.dto.WorkflowBizBindingPolicyUpdateRequest;
import com.icoolkj.platform.workflow.service.WorkflowBizBindingPolicyService;
import com.icoolkj.platform.workflow.vo.WorkflowBizBindingPolicyVO;
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

/** 业务绑定策略（平台级） */
@RestController
@RequestMapping("/workflow/biz-binding-policy")
public class WorkflowBizBindingPolicyController {

    private final WorkflowBizBindingPolicyService bindingPolicyService;

    public WorkflowBizBindingPolicyController(WorkflowBizBindingPolicyService bindingPolicyService) {
        this.bindingPolicyService = bindingPolicyService;
    }

    @PreAuthorize("@authz.hasPermission('workflow:bizBindingPolicy:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<WorkflowBizBindingPolicyVO>> page(WorkflowBizBindingPolicyPageQuery query) {
        return ApiResponse.ok(bindingPolicyService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('workflow:bizBindingPolicy:query')")
    @GetMapping("/{id}")
    public ApiResponse<WorkflowBizBindingPolicyVO> getById(@PathVariable String id) {
        return ApiResponse.ok(bindingPolicyService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('workflow:bizBindingPolicy:update')")
    @OperationLog(module = "Workflow Biz Binding Policy", businessType = "CREATE")
    @Idempotent("workflow:bizBindingPolicy:create")
    @PostMapping("/create")
    public ApiResponse<String> create(@Valid @RequestBody WorkflowBizBindingPolicyCreateRequest request) {
        return ApiResponse.ok(bindingPolicyService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('workflow:bizBindingPolicy:update')")
    @OperationLog(module = "Workflow Biz Binding Policy", businessType = "UPDATE")
    @Idempotent("workflow:bizBindingPolicy:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody WorkflowBizBindingPolicyUpdateRequest request) {
        bindingPolicyService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('workflow:bizBindingPolicy:delete')")
    @OperationLog(module = "Workflow Biz Binding Policy", businessType = "DELETE")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        bindingPolicyService.delete(id);
        return ApiResponse.ok();
    }
}
