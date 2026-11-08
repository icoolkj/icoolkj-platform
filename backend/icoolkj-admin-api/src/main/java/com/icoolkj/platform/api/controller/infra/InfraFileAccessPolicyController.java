package com.icoolkj.platform.api.controller.infra;

import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.infra.dto.InfraFileAccessPolicyUpsertRequest;
import com.icoolkj.platform.infra.service.InfraFileAccessPolicyAdminService;
import com.icoolkj.platform.infra.vo.InfraFileAccessPolicyVO;
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

/** 文件访问策略（按 biz_type_pattern / 槽位）— 平台运营配置 */
@RestController
@RequestMapping("/infra/file-access-policy")
public class InfraFileAccessPolicyController {

    private final InfraFileAccessPolicyAdminService policyAdminService;

    public InfraFileAccessPolicyController(InfraFileAccessPolicyAdminService policyAdminService) {
        this.policyAdminService = policyAdminService;
    }

    @PreAuthorize("@authz.hasPermission('infra:fileAccessPolicy:query')")
    @GetMapping("/list")
    public ApiResponse<List<InfraFileAccessPolicyVO>> list() {
        return ApiResponse.ok(policyAdminService.list());
    }

    @PreAuthorize("@authz.hasPermission('infra:fileAccessPolicy:query')")
    @GetMapping("/effective")
    public ApiResponse<List<InfraFileAccessPolicyVO>> listEffective() {
        return ApiResponse.ok(policyAdminService.listEffective());
    }

    @PreAuthorize("@authz.hasPermission('infra:fileAccessPolicy:query')")
    @GetMapping("/{id}")
    public ApiResponse<InfraFileAccessPolicyVO> get(@PathVariable String id) {
        return ApiResponse.ok(policyAdminService.getById(id));
    }

    @PreAuthorize("@authz.hasAnyPermission('infra:fileAccessPolicy:add', 'infra:fileAccessPolicy:update')")
    @OperationLog(module = "InfraFileAccessPolicy", businessType = "UPDATE")
    @Idempotent("infra:fileAccessPolicy:upsert")
    @PutMapping
    public ApiResponse<Void> upsert(@Valid @RequestBody InfraFileAccessPolicyUpsertRequest request) {
        policyAdminService.upsert(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('infra:fileAccessPolicy:delete')")
    @OperationLog(module = "InfraFileAccessPolicy", businessType = "DELETE")
    @Idempotent("infra:fileAccessPolicy:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        policyAdminService.delete(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('infra:fileAccessPolicy:update')")
    @OperationLog(module = "InfraFileAccessPolicy", businessType = "OTHER")
    @Idempotent("infra:fileAccessPolicy:refresh")
    @PostMapping("/refresh")
    public ApiResponse<Void> refresh() {
        policyAdminService.refreshRegistry();
        return ApiResponse.ok();
    }
}
