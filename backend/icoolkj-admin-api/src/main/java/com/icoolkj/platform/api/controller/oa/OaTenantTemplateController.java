package com.icoolkj.platform.api.controller.oa;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.oa.dto.OaFieldDefBatchSaveRequest;
import com.icoolkj.platform.oa.dto.OaTenantEnableRequest;
import com.icoolkj.platform.oa.dto.OaTenantTemplatePageQuery;
import com.icoolkj.platform.oa.dto.OaTenantTemplateUpdateRequest;
import com.icoolkj.platform.oa.dto.OaVersionCreateRequest;
import com.icoolkj.platform.oa.dto.OaVersionPublishRequest;
import com.icoolkj.platform.oa.service.OaTenantTemplateService;
import com.icoolkj.platform.oa.vo.OaPlatformTemplateOptionVO;
import com.icoolkj.platform.oa.vo.OaTenantPublishReadinessVO;
import com.icoolkj.platform.oa.vo.OaTenantSyncPreviewVO;
import com.icoolkj.platform.oa.vo.OaTenantTemplateDetailVO;
import com.icoolkj.platform.oa.vo.OaTenantTemplateVO;
import com.icoolkj.platform.oa.vo.OaTemplateVersionVO;
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

/** OA 租户审批模板映射 */
@RestController
@RequestMapping("/oa/tenantTemplate")
public class OaTenantTemplateController {

    private final OaTenantTemplateService tenantTemplateService;

    public OaTenantTemplateController(OaTenantTemplateService tenantTemplateService) {
        this.tenantTemplateService = tenantTemplateService;
    }

    @PreAuthorize("@authz.hasPermission('oa:tenantTemplate:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<OaTenantTemplateVO>> page(OaTenantTemplatePageQuery query) {
        return ApiResponse.ok(tenantTemplateService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('oa:tenantTemplate:query')")
    @GetMapping("/platformOptions")
    public ApiResponse<List<OaPlatformTemplateOptionVO>> listPlatformOptions() {
        return ApiResponse.ok(tenantTemplateService.listAvailablePlatformTemplates());
    }

    @PreAuthorize("@authz.hasPermission('oa:tenantTemplate:query')")
    @GetMapping("/{id}")
    public ApiResponse<OaTenantTemplateDetailVO> getById(@PathVariable String id) {
        return ApiResponse.ok(tenantTemplateService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('oa:tenantTemplate:query')")
    @GetMapping("/{id}/versions")
    public ApiResponse<List<OaTemplateVersionVO>> listVersions(@PathVariable String id) {
        return ApiResponse.ok(tenantTemplateService.listVersions(id));
    }

    @PreAuthorize("@authz.hasPermission('oa:tenantTemplate:query')")
    @GetMapping("/version/{versionId}")
    public ApiResponse<OaTemplateVersionVO> getVersionDetail(@PathVariable String versionId) {
        return ApiResponse.ok(tenantTemplateService.getVersionDetail(versionId));
    }

    @PreAuthorize("@authz.hasPermission('oa:tenantTemplate:enable')")
    @OperationLog(module = "OA Tenant Template", businessType = "CREATE")
    @Idempotent("oa:tenantTemplate:enable")
    @PostMapping("/enable")
    public ApiResponse<String> enable(@Valid @RequestBody OaTenantEnableRequest request) {
        return ApiResponse.ok(tenantTemplateService.enable(request));
    }

    @PreAuthorize("@authz.hasPermission('oa:tenantTemplate:update')")
    @OperationLog(module = "OA Tenant Template", businessType = "UPDATE")
    @Idempotent("oa:tenantTemplate:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody OaTenantTemplateUpdateRequest request) {
        tenantTemplateService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('oa:tenantTemplate:update')")
    @OperationLog(module = "OA Tenant Template", businessType = "CREATE")
    @Idempotent("oa:tenantTemplate:createDraft")
    @PostMapping("/version/createDraft")
    public ApiResponse<String> createDraftVersion(@Valid @RequestBody OaVersionCreateRequest request) {
        return ApiResponse.ok(tenantTemplateService.createDraftVersion(request));
    }

    @PreAuthorize("@authz.hasPermission('oa:tenantTemplate:query')")
    @GetMapping("/{id}/syncPreview")
    public ApiResponse<OaTenantSyncPreviewVO> syncPreview(@PathVariable String id) {
        return ApiResponse.ok(tenantTemplateService.previewSyncFromPlatform(id));
    }

    @PreAuthorize("@authz.hasPermission('oa:tenantTemplate:update')")
    @OperationLog(module = "OA Tenant Template", businessType = "UPDATE")
    @Idempotent("oa:tenantTemplate:syncFromPlatform")
    @PostMapping("/{id}/syncFromPlatform")
    public ApiResponse<String> syncFromPlatform(@PathVariable String id) {
        return ApiResponse.ok(tenantTemplateService.syncFromPlatform(id));
    }

    @PreAuthorize("@authz.hasPermission('oa:tenantTemplate:update')")
    @OperationLog(module = "OA Tenant Template", businessType = "UPDATE")
    @Idempotent("oa:tenantTemplate:saveFields")
    @PutMapping("/version/saveFields")
    public ApiResponse<Void> saveFieldDefs(@Valid @RequestBody OaFieldDefBatchSaveRequest request) {
        tenantTemplateService.saveFieldDefs(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('oa:tenantTemplate:query')")
    @GetMapping("/version/{versionId}/publish-readiness")
    public ApiResponse<OaTenantPublishReadinessVO> publishReadiness(@PathVariable String versionId) {
        return ApiResponse.ok(tenantTemplateService.getPublishReadiness(versionId));
    }

    @PreAuthorize("@authz.hasPermission('oa:tenantTemplate:publish')")
    @OperationLog(module = "OA Tenant Template", businessType = "PUBLISH")
    @Idempotent("oa:tenantTemplate:publish")
    @PostMapping("/version/publish")
    public ApiResponse<Void> publishVersion(@Valid @RequestBody OaVersionPublishRequest request) {
        tenantTemplateService.publishVersion(request);
        return ApiResponse.ok();
    }
}
