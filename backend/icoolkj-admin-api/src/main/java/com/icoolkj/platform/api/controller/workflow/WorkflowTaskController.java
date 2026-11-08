package com.icoolkj.platform.api.controller.workflow;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.workflow.dto.WorkflowTaskAddSignRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskCompleteRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskDelegateRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskJumpRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskPageQuery;
import com.icoolkj.platform.workflow.dto.WorkflowTaskRemoveSignRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskTransferRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskUrgeRequest;
import com.icoolkj.platform.workflow.service.WorkflowTaskService;
import com.icoolkj.platform.workflow.vo.WorkflowTaskDetailVO;
import com.icoolkj.platform.workflow.vo.WorkflowTaskVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 工作流任务 */
@RestController
@RequestMapping("/workflow/task")
public class WorkflowTaskController {

    private final WorkflowTaskService workflowTaskService;

    public WorkflowTaskController(WorkflowTaskService workflowTaskService) {
        this.workflowTaskService = workflowTaskService;
    }

    @PreAuthorize("@authz.hasPermission('workflow:todo:list')")
    @GetMapping("/todo/page")
    public ApiResponse<PageResult<WorkflowTaskVO>> pageTodo(WorkflowTaskPageQuery query) {
        return ApiResponse.ok(workflowTaskService.pageTodo(query));
    }

    @PreAuthorize("@authz.hasPermission('workflow:done:list')")
    @GetMapping("/done/page")
    public ApiResponse<PageResult<WorkflowTaskVO>> pageDone(WorkflowTaskPageQuery query) {
        return ApiResponse.ok(workflowTaskService.pageDone(query));
    }

    @PreAuthorize("@authz.hasPermission('workflow:cc:list')")
    @GetMapping("/cc/page")
    public ApiResponse<PageResult<WorkflowTaskVO>> pageCc(WorkflowTaskPageQuery query) {
        return ApiResponse.ok(workflowTaskService.pageCc(query));
    }

    @PreAuthorize("@authz.hasPermission('workflow:task:query')")
    @GetMapping("/{taskId}")
    public ApiResponse<WorkflowTaskDetailVO> getByTaskId(@PathVariable String taskId) {
        return ApiResponse.ok(workflowTaskService.getByTaskId(taskId));
    }

    @PreAuthorize("@authz.hasPermission('workflow:task:complete')")
    @OperationLog(module = "Workflow Task", businessType = "APPROVE")
    @Idempotent("workflow:task:complete")
    @PostMapping("/{taskId}/complete")
    public ApiResponse<Void> completeTask(@PathVariable String taskId,
                                          @Valid @RequestBody WorkflowTaskCompleteRequest request) {
        workflowTaskService.completeTask(taskId, request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('workflow:task:transfer')")
    @OperationLog(module = "Workflow Task", businessType = "TRANSFER")
    @Idempotent("workflow:task:transfer")
    @PostMapping("/{taskId}/transfer")
    public ApiResponse<Void> transferTask(@PathVariable String taskId,
                                          @Valid @RequestBody WorkflowTaskTransferRequest request) {
        workflowTaskService.transferTask(taskId, request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('workflow:task:delegate')")
    @OperationLog(module = "Workflow Task", businessType = "DELEGATE")
    @Idempotent("workflow:task:delegate")
    @PostMapping("/{taskId}/delegate")
    public ApiResponse<Void> delegateTask(@PathVariable String taskId,
                                          @Valid @RequestBody WorkflowTaskDelegateRequest request) {
        workflowTaskService.delegateTask(taskId, request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('workflow:task:jump')")
    @OperationLog(module = "Workflow Task", businessType = "JUMP")
    @Idempotent("workflow:task:jump")
    @PostMapping("/{taskId}/jump")
    public ApiResponse<Void> jumpTask(@PathVariable String taskId,
                                      @Valid @RequestBody WorkflowTaskJumpRequest request) {
        workflowTaskService.jumpTask(taskId, request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('workflow:cc:list')")
    @PostMapping("/{taskId}/cc-read")
    public ApiResponse<Void> markCcRead(@PathVariable String taskId) {
        workflowTaskService.markCcRead(taskId);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('workflow:task:urge') or @authz.hasPermission('workflow:submitted:list')")
    @OperationLog(module = "Workflow Task", businessType = "URGE")
    @Idempotent("workflow:task:urge")
    @PostMapping("/{taskId}/urge")
    public ApiResponse<Void> urgeTask(@PathVariable String taskId,
                                      @Valid @RequestBody(required = false) WorkflowTaskUrgeRequest request) {
        workflowTaskService.urgeTask(taskId, request == null ? new WorkflowTaskUrgeRequest() : request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('workflow:task:addSign')")
    @OperationLog(module = "Workflow Task", businessType = "ADD_SIGN")
    @Idempotent("workflow:task:addSign")
    @PostMapping("/{taskId}/add-sign")
    public ApiResponse<Void> addSignTask(@PathVariable String taskId,
                                         @Valid @RequestBody WorkflowTaskAddSignRequest request) {
        workflowTaskService.addSignTask(taskId, request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('workflow:task:removeSign')")
    @OperationLog(module = "Workflow Task", businessType = "REMOVE_SIGN")
    @Idempotent("workflow:task:removeSign")
    @PostMapping("/{taskId}/remove-sign")
    public ApiResponse<Void> removeSignTask(@PathVariable String taskId,
                                            @Valid @RequestBody WorkflowTaskRemoveSignRequest request) {
        workflowTaskService.removeSignTask(taskId, request);
        return ApiResponse.ok();
    }
}
