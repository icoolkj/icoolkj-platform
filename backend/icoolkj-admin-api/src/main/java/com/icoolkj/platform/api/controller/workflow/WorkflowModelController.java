package com.icoolkj.platform.api.controller.workflow;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.workflow.dto.WorkflowModelCreateRequest;
import com.icoolkj.platform.workflow.dto.WorkflowModelDeployRequest;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignDeployRequest;
import com.icoolkj.platform.workflow.dto.WorkflowModelPageQuery;
import com.icoolkj.platform.workflow.dto.WorkflowModelSyncRequest;
import com.icoolkj.platform.workflow.dto.WorkflowModelUpdateRequest;
import com.icoolkj.platform.workflow.service.WorkflowModelService;
import com.icoolkj.platform.workflow.vo.WorkflowModelDeployResultVO;
import com.icoolkj.platform.workflow.vo.WorkflowModelDetailVO;
import com.icoolkj.platform.workflow.vo.WorkflowModelSyncCatalogVO;
import com.icoolkj.platform.workflow.vo.WorkflowModelVO;
import com.icoolkj.platform.workflow.vo.WorkflowModelVersionVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 流程模型 */
@RestController
@RequestMapping("/workflow/model")
public class WorkflowModelController {

    private final WorkflowModelService workflowModelService;

    public WorkflowModelController(WorkflowModelService workflowModelService) {
        this.workflowModelService = workflowModelService;
    }

    @PreAuthorize("@authz.hasPermission('workflow:model:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<WorkflowModelVO>> page(WorkflowModelPageQuery query) {
        return ApiResponse.ok(workflowModelService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('workflow:model:list')")
    @GetMapping("/sync-catalog")
    public ApiResponse<List<WorkflowModelSyncCatalogVO>> listSyncCatalog() {
        return ApiResponse.ok(workflowModelService.listSyncCatalog());
    }

    @PreAuthorize("@authz.hasPermission('workflow:model:update')")
    @OperationLog(module = "Workflow Model", businessType = "SYNC")
    @Idempotent("workflow:model:sync")
    @PostMapping("/sync")
    public ApiResponse<String> syncFromPlatform(@Valid @RequestBody WorkflowModelSyncRequest request) {
        return ApiResponse.ok(workflowModelService.syncFromPlatform(request));
    }

    @PreAuthorize("@authz.hasPermission('workflow:model:query')")
    @GetMapping("/{id}")
    public ApiResponse<WorkflowModelDetailVO> getById(@PathVariable String id) {
        return ApiResponse.ok(workflowModelService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('workflow:model:query')")
    @GetMapping("/{id}/versions")
    public ApiResponse<List<WorkflowModelVersionVO>> listVersions(@PathVariable String id) {
        return ApiResponse.ok(workflowModelService.listVersions(id));
    }

    @PreAuthorize("@authz.hasPermission('workflow:model:update')")
    @OperationLog(module = "Workflow Model", businessType = "CREATE")
    @Idempotent("workflow:model:create")
    @PostMapping("/create")
    public ApiResponse<String> create(@Valid @RequestBody WorkflowModelCreateRequest request) {
        return ApiResponse.ok(workflowModelService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('workflow:model:update')")
    @OperationLog(module = "Workflow Model", businessType = "UPDATE")
    @Idempotent("workflow:model:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody WorkflowModelUpdateRequest request) {
        workflowModelService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('workflow:model:deploy')")
    @OperationLog(module = "Workflow Model", businessType = "DEPLOY")
    @Idempotent("workflow:model:deploy")
    @PostMapping("/deploy")
    public ApiResponse<WorkflowModelDeployResultVO> deploy(@Valid @RequestBody WorkflowModelDeployRequest request) {
        return ApiResponse.ok(workflowModelService.deploy(request));
    }

    @PreAuthorize("@authz.hasPermission('workflow:model:deploy')")
    @OperationLog(module = "Workflow Model", businessType = "DESIGN_DEPLOY")
    @Idempotent("workflow:model:designDeploy")
    @PostMapping("/design-deploy")
    public ApiResponse<WorkflowModelDeployResultVO> designDeploy(
            @Valid @RequestBody WorkflowModelDesignDeployRequest request) {
        return ApiResponse.ok(workflowModelService.designDeploy(request));
    }
}
