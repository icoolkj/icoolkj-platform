package com.icoolkj.platform.api.controller.workflow;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.workflow.dto.WorkflowApproverRuleCreateRequest;
import com.icoolkj.platform.workflow.dto.WorkflowApproverRulePageQuery;
import com.icoolkj.platform.workflow.dto.WorkflowApproverRuleUpdateRequest;
import com.icoolkj.platform.workflow.service.WorkflowApproverRuleService;
import com.icoolkj.platform.workflow.vo.WorkflowApproverRuleVO;
import com.icoolkj.platform.workflow.vo.WorkflowModelOptionVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 审批人规则 */
@RestController
@RequestMapping("/workflow/approver-rule")
public class WorkflowApproverRuleController {

    private final WorkflowApproverRuleService approverRuleService;

    public WorkflowApproverRuleController(WorkflowApproverRuleService approverRuleService) {
        this.approverRuleService = approverRuleService;
    }

    @PreAuthorize("@authz.hasPermission('workflow:approverRule:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<WorkflowApproverRuleVO>> page(WorkflowApproverRulePageQuery query) {
        return ApiResponse.ok(approverRuleService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('workflow:approverRule:query')")
    @GetMapping("/{id}")
    public ApiResponse<WorkflowApproverRuleVO> getById(@PathVariable String id) {
        return ApiResponse.ok(approverRuleService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('workflow:approverRule:query')")
    @GetMapping("/model-options")
    public ApiResponse<List<WorkflowModelOptionVO>> listModelOptions() {
        return ApiResponse.ok(approverRuleService.listModelOptions());
    }

    @PreAuthorize("@authz.hasPermission('workflow:approverRule:update')")
    @OperationLog(module = "Workflow Approver Rule", businessType = "CREATE")
    @Idempotent("workflow:approverRule:create")
    @PostMapping("/create")
    public ApiResponse<String> create(@Valid @RequestBody WorkflowApproverRuleCreateRequest request) {
        return ApiResponse.ok(approverRuleService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('workflow:approverRule:update')")
    @OperationLog(module = "Workflow Approver Rule", businessType = "UPDATE")
    @Idempotent("workflow:approverRule:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody WorkflowApproverRuleUpdateRequest request) {
        approverRuleService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('workflow:approverRule:delete')")
    @OperationLog(module = "Workflow Approver Rule", businessType = "DELETE")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        approverRuleService.delete(id);
        return ApiResponse.ok();
    }
}
