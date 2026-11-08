package com.icoolkj.platform.api.controller.oa;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.oa.dto.OaFieldDefBatchSaveRequest;
import com.icoolkj.platform.oa.dto.OaPlatformTemplateCreateRequest;
import com.icoolkj.platform.oa.dto.OaPlatformTemplatePageQuery;
import com.icoolkj.platform.oa.dto.OaPlatformTemplateUpdateRequest;
import com.icoolkj.platform.oa.dto.OaVersionCreateRequest;
import com.icoolkj.platform.oa.dto.OaVersionPublishRequest;
import com.icoolkj.platform.oa.service.OaPlatformTemplateService;
import com.icoolkj.platform.oa.vo.OaPlatformTemplateDetailVO;
import com.icoolkj.platform.oa.vo.OaPlatformTemplateVO;
import com.icoolkj.platform.oa.vo.OaTemplateVersionVO;
import com.icoolkj.platform.workflow.vo.WorkflowModelOptionVO;
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

/** OA 平台审批模板 */
@RestController
@RequestMapping("/oa/platformTemplate")
public class OaPlatformTemplateController {

    private final OaPlatformTemplateService platformTemplateService;

    public OaPlatformTemplateController(OaPlatformTemplateService platformTemplateService) {
        this.platformTemplateService = platformTemplateService;
    }

    @PreAuthorize("@authz.hasPermission('oa:platformTemplate:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<OaPlatformTemplateVO>> page(OaPlatformTemplatePageQuery query) {
        return ApiResponse.ok(platformTemplateService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('oa:platformTemplate:query')")
    @GetMapping("/model-options")
    public ApiResponse<List<WorkflowModelOptionVO>> listModelOptions() {
        return ApiResponse.ok(platformTemplateService.listModelOptions());
    }

    @PreAuthorize("@authz.hasPermission('oa:platformTemplate:query')")
    @GetMapping("/{id}")
    public ApiResponse<OaPlatformTemplateDetailVO> getById(@PathVariable String id) {
        return ApiResponse.ok(platformTemplateService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('oa:platformTemplate:query')")
    @GetMapping("/{id}/versions")
    public ApiResponse<List<OaTemplateVersionVO>> listVersions(@PathVariable String id) {
        return ApiResponse.ok(platformTemplateService.listVersions(id));
    }

    @PreAuthorize("@authz.hasPermission('oa:platformTemplate:query')")
    @GetMapping("/version/{versionId}")
    public ApiResponse<OaTemplateVersionVO> getVersionDetail(@PathVariable String versionId) {
        return ApiResponse.ok(platformTemplateService.getVersionDetail(versionId));
    }

    @PreAuthorize("@authz.hasPermission('oa:platformTemplate:update')")
    @OperationLog(module = "OA Platform Template", businessType = "CREATE")
    @Idempotent("oa:platformTemplate:create")
    @PostMapping("/create")
    public ApiResponse<String> create(@Valid @RequestBody OaPlatformTemplateCreateRequest request) {
        return ApiResponse.ok(platformTemplateService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('oa:platformTemplate:update')")
    @OperationLog(module = "OA Platform Template", businessType = "UPDATE")
    @Idempotent("oa:platformTemplate:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody OaPlatformTemplateUpdateRequest request) {
        platformTemplateService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('oa:platformTemplate:update')")
    @OperationLog(module = "OA Platform Template", businessType = "CREATE")
    @Idempotent("oa:platformTemplate:createDraft")
    @PostMapping("/version/createDraft")
    public ApiResponse<String> createDraftVersion(@Valid @RequestBody OaVersionCreateRequest request) {
        return ApiResponse.ok(platformTemplateService.createDraftVersion(request));
    }

    @PreAuthorize("@authz.hasPermission('oa:platformTemplate:update')")
    @OperationLog(module = "OA Platform Template", businessType = "UPDATE")
    @Idempotent("oa:platformTemplate:saveFields")
    @PutMapping("/version/saveFields")
    public ApiResponse<Void> saveFieldDefs(@Valid @RequestBody OaFieldDefBatchSaveRequest request) {
        platformTemplateService.saveFieldDefs(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('oa:platformTemplate:publish')")
    @OperationLog(module = "OA Platform Template", businessType = "PUBLISH")
    @Idempotent("oa:platformTemplate:publish")
    @PostMapping("/version/publish")
    public ApiResponse<Void> publishVersion(@Valid @RequestBody OaVersionPublishRequest request) {
        platformTemplateService.publishVersion(request);
        return ApiResponse.ok();
    }
}
