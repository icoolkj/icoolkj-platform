package com.icoolkj.platform.api.controller.infra;

import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.infra.dto.InfraFileCatalogUpsertRequest;
import com.icoolkj.platform.infra.dto.InfraFileProfileBatchDeleteRequest;
import com.icoolkj.platform.infra.dto.InfraFileProfileUpsertRequest;
import com.icoolkj.platform.infra.service.InfraFileProfileAdminService;
import com.icoolkj.platform.infra.vo.InfraFileCatalogVO;
import com.icoolkj.platform.infra.vo.InfraFileProfileVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 文件上传配置（Profile / Catalog）完整 CRUD */
@RestController
@RequestMapping("/infra/file-profile")
public class InfraFileProfileController {

    private final InfraFileProfileAdminService profileAdminService;

    public InfraFileProfileController(InfraFileProfileAdminService profileAdminService) {
        this.profileAdminService = profileAdminService;
    }

    @PreAuthorize("@authz.hasPermission('infra:fileProfile:query')")
    @GetMapping("/list")
    public ApiResponse<List<InfraFileProfileVO>> list(
            @RequestParam(value = "categoryTag", required = false) String categoryTag,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "targetTenantId", required = false) String targetTenantId) {
        return ApiResponse.ok(profileAdminService.list(categoryTag, keyword, targetTenantId));
    }

    @PreAuthorize("@authz.isAuthenticated()")
    @GetMapping("/resolve")
    public ApiResponse<InfraFileProfileVO> resolve(
            @RequestParam("code") String code,
            @RequestParam(value = "targetTenantId", required = false) String targetTenantId) {
        return ApiResponse.ok(profileAdminService.resolve(code, targetTenantId));
    }

    @PreAuthorize("@authz.hasAnyPermission('infra:fileProfile:add', 'infra:fileProfile:update')")
    @OperationLog(module = "InfraFileProfile", businessType = "UPDATE")
    @Idempotent("infra:fileProfile:upsert")
    @PutMapping
    public ApiResponse<Void> upsert(@Valid @RequestBody InfraFileProfileUpsertRequest request) {
        profileAdminService.upsert(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('infra:fileProfile:update')")
    @OperationLog(module = "InfraFileProfile", businessType = "DELETE")
    @Idempotent("infra:fileProfile:reset")
    @DeleteMapping("/{profileCode}/override")
    public ApiResponse<Void> resetOverride(
            @PathVariable String profileCode,
            @RequestParam(value = "targetTenantId", required = false) String targetTenantId) {
        profileAdminService.resetRuleOverride(profileCode, targetTenantId);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('infra:fileProfile:delete')")
    @OperationLog(module = "InfraFileProfile", businessType = "DELETE")
    @Idempotent("infra:fileProfile:delete")
    @DeleteMapping("/{profileCode}")
    public ApiResponse<Void> delete(
            @PathVariable String profileCode,
            @RequestParam(value = "targetTenantId", required = false) String targetTenantId) {
        profileAdminService.deleteProfile(profileCode, targetTenantId);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('infra:fileProfile:delete')")
    @OperationLog(module = "InfraFileProfile", businessType = "DELETE")
    @Idempotent("infra:fileProfile:batchDelete")
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody InfraFileProfileBatchDeleteRequest request) {
        profileAdminService.batchDeleteProfiles(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('infra:fileProfile:query')")
    @GetMapping("/catalogs")
    public ApiResponse<List<InfraFileCatalogVO>> listCatalogs(
            @RequestParam(value = "targetTenantId", required = false) String targetTenantId) {
        return ApiResponse.ok(profileAdminService.listCatalogs(targetTenantId));
    }

    @PreAuthorize("@authz.isAuthenticated()")
    @GetMapping("/catalogs/{catalogCode}")
    public ApiResponse<InfraFileCatalogVO> getCatalog(
            @PathVariable String catalogCode,
            @RequestParam(value = "targetTenantId", required = false) String targetTenantId) {
        return ApiResponse.ok(profileAdminService.getCatalog(catalogCode, targetTenantId));
    }

    @PreAuthorize("@authz.hasAnyPermission('infra:fileProfile:add', 'infra:fileProfile:update')")
    @OperationLog(module = "InfraFileCatalog", businessType = "UPDATE")
    @Idempotent("infra:fileProfile:catalogUpsert")
    @PutMapping("/catalogs")
    public ApiResponse<Void> upsertCatalog(@Valid @RequestBody InfraFileCatalogUpsertRequest request) {
        profileAdminService.upsertCatalog(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('infra:fileProfile:delete')")
    @OperationLog(module = "InfraFileCatalog", businessType = "DELETE")
    @Idempotent("infra:fileProfile:catalogDelete")
    @DeleteMapping("/catalogs/{catalogCode}")
    public ApiResponse<Void> deleteCatalog(
            @PathVariable String catalogCode,
            @RequestParam(value = "targetTenantId", required = false) String targetTenantId) {
        profileAdminService.deleteCatalog(catalogCode, targetTenantId);
        return ApiResponse.ok();
    }
}
