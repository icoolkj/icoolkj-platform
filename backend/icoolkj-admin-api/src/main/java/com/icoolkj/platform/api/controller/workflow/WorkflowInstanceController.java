package com.icoolkj.platform.api.controller.workflow;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.workflow.dto.WorkflowInstanceCancelRequest;
import com.icoolkj.platform.workflow.dto.WorkflowInstancePageQuery;
import com.icoolkj.platform.workflow.service.WorkflowInstanceService;
import com.icoolkj.platform.workflow.vo.WorkflowInstanceDetailVO;
import com.icoolkj.platform.workflow.vo.WorkflowInstanceVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 工作流实例 */
@RestController
@RequestMapping("/workflow/instance")
public class WorkflowInstanceController {

    private final WorkflowInstanceService workflowInstanceService;

    public WorkflowInstanceController(WorkflowInstanceService workflowInstanceService) {
        this.workflowInstanceService = workflowInstanceService;
    }

    @PreAuthorize("@authz.hasPermission('workflow:submitted:list')")
    @GetMapping("/submitted/page")
    public ApiResponse<PageResult<WorkflowInstanceVO>> pageSubmitted(WorkflowInstancePageQuery query) {
        return ApiResponse.ok(workflowInstanceService.pageSubmitted(query));
    }

    @PreAuthorize("@authz.hasPermission('workflow:monitor:list')")
    @GetMapping("/tenant/page")
    public ApiResponse<PageResult<WorkflowInstanceVO>> pageTenant(WorkflowInstancePageQuery query) {
        return ApiResponse.ok(workflowInstanceService.pageTenant(query));
    }

    @PreAuthorize("@authz.hasAnyPermission('workflow:instance:query', 'workflow:monitor:list')")
    @GetMapping("/{instanceId}")
    public ApiResponse<WorkflowInstanceDetailVO> getByInstanceId(@PathVariable String instanceId) {
        return ApiResponse.ok(workflowInstanceService.getByInstanceId(instanceId));
    }

    @PreAuthorize("@authz.hasPermission('workflow:instance:cancel')")
    @OperationLog(module = "Workflow Instance", businessType = "CANCEL")
    @Idempotent("workflow:instance:cancel")
    @PostMapping("/{instanceId}/cancel")
    public ApiResponse<Void> cancelInstance(@PathVariable String instanceId) {
        workflowInstanceService.cancelInstance(instanceId);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('workflow:instance:terminate')")
    @OperationLog(module = "Workflow Instance", businessType = "TERMINATE")
    @Idempotent("workflow:instance:terminate")
    @PostMapping("/{instanceId}/terminate")
    public ApiResponse<Void> terminateInstance(@PathVariable String instanceId,
                                               @Valid @RequestBody(required = false) WorkflowInstanceCancelRequest request) {
        workflowInstanceService.terminateInstance(instanceId, request == null ? new WorkflowInstanceCancelRequest() : request);
        return ApiResponse.ok();
    }
}
