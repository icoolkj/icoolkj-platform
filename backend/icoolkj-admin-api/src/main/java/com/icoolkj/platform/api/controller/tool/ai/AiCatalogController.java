package com.icoolkj.platform.api.controller.tool.ai;

import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.system.ai.dto.AiCapabilityBindRequest;
import com.icoolkj.platform.system.ai.dto.AiModelSaveRequest;
import com.icoolkj.platform.system.ai.dto.AiProviderSaveRequest;
import com.icoolkj.platform.system.ai.dto.AiProviderTenantKeyRequest;
import com.icoolkj.platform.system.ai.service.AiCatalogService;
import com.icoolkj.platform.system.ai.vo.AiCapabilityBindingVO;
import com.icoolkj.platform.system.ai.vo.AiModelVO;
import com.icoolkj.platform.system.ai.vo.AiProviderVO;
import com.icoolkj.platform.system.ai.vo.AiRuntimeStatusVO;
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

/** AI 能力目录：连接 / 模型 / 能力当前启用 */
@RestController
@RequestMapping("/tool/ai-catalog")
public class AiCatalogController {

    private final AiCatalogService aiCatalogService;

    public AiCatalogController(AiCatalogService aiCatalogService) {
        this.aiCatalogService = aiCatalogService;
    }

    @PreAuthorize("@authz.hasPermission('tool:aiCatalog:query')")
    @GetMapping("/providers")
    public ApiResponse<List<AiProviderVO>> providers() {
        return ApiResponse.ok(aiCatalogService.listProviders());
    }

    @PreAuthorize("@authz.hasPermission('tool:aiCatalog:add')")
    @OperationLog(module = "AI Catalog", businessType = "CREATE")
    @Idempotent("tool:aiCatalog:provider:create")
    @PostMapping("/providers")
    public ApiResponse<String> createProvider(@Valid @RequestBody AiProviderSaveRequest request) {
        request.setId(null);
        return ApiResponse.ok(aiCatalogService.saveProvider(request));
    }

    @PreAuthorize("@authz.hasPermission('tool:aiCatalog:update')")
    @OperationLog(module = "AI Catalog", businessType = "UPDATE")
    @Idempotent("tool:aiCatalog:provider:update")
    @PutMapping("/providers")
    public ApiResponse<String> updateProvider(@Valid @RequestBody AiProviderSaveRequest request) {
        return ApiResponse.ok(aiCatalogService.saveProvider(request));
    }

    @PreAuthorize("@authz.hasPermission('tool:aiCatalog:delete')")
    @OperationLog(module = "AI Catalog", businessType = "DELETE")
    @Idempotent("tool:aiCatalog:provider:delete")
    @DeleteMapping("/providers/{id}")
    public ApiResponse<Void> deleteProvider(@PathVariable String id) {
        aiCatalogService.deleteProvider(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('tool:aiCatalog:query')")
    @GetMapping("/models")
    public ApiResponse<List<AiModelVO>> models() {
        return ApiResponse.ok(aiCatalogService.listSelectableModels());
    }

    @PreAuthorize("@authz.hasPermission('tool:aiCatalog:query')")
    @GetMapping("/models/owned")
    public ApiResponse<List<AiModelVO>> ownedModels() {
        return ApiResponse.ok(aiCatalogService.listOwnedModels());
    }

    @PreAuthorize("@authz.hasPermission('tool:aiCatalog:add')")
    @OperationLog(module = "AI Catalog", businessType = "CREATE")
    @Idempotent("tool:aiCatalog:model:create")
    @PostMapping("/models")
    public ApiResponse<String> createModel(@Valid @RequestBody AiModelSaveRequest request) {
        request.setId(null);
        return ApiResponse.ok(aiCatalogService.saveModel(request));
    }

    @PreAuthorize("@authz.hasPermission('tool:aiCatalog:update')")
    @OperationLog(module = "AI Catalog", businessType = "UPDATE")
    @Idempotent("tool:aiCatalog:model:update")
    @PutMapping("/models")
    public ApiResponse<String> updateModel(@Valid @RequestBody AiModelSaveRequest request) {
        return ApiResponse.ok(aiCatalogService.saveModel(request));
    }

    @PreAuthorize("@authz.hasPermission('tool:aiCatalog:delete')")
    @OperationLog(module = "AI Catalog", businessType = "DELETE")
    @Idempotent("tool:aiCatalog:model:delete")
    @DeleteMapping("/models/{id}")
    public ApiResponse<Void> deleteModel(@PathVariable String id) {
        aiCatalogService.deleteModel(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('tool:aiCatalog:update')")
    @OperationLog(module = "AI Catalog", businessType = "UPDATE")
    @Idempotent("tool:aiCatalog:provider:adopt")
    @PostMapping("/providers/{id}/adopt")
    public ApiResponse<String> adoptPlatformProvider(
            @PathVariable String id, @Valid @RequestBody AiProviderTenantKeyRequest request) {
        return ApiResponse.ok(aiCatalogService.adoptPlatformProvider(id, request.getApiKey()));
    }

    @PreAuthorize("@authz.hasPermission('tool:aiCatalog:query')")
    @GetMapping("/bindings")
    public ApiResponse<List<AiCapabilityBindingVO>> bindings() {
        return ApiResponse.ok(aiCatalogService.listEffectiveBindings());
    }

    @PreAuthorize("@authz.hasPermission('tool:aiCatalog:update')")
    @OperationLog(module = "AI Catalog", businessType = "UPDATE")
    @Idempotent("tool:aiCatalog:bind")
    @PutMapping("/bindings")
    public ApiResponse<Void> bind(@Valid @RequestBody AiCapabilityBindRequest request) {
        aiCatalogService.bindCapability(request);
        return ApiResponse.ok();
    }

    /** 功能页就绪探测：允许 AI 查询权限或目录查询权限 */
    @PreAuthorize("@authz.hasPermission('tool:ai:query') or @authz.hasPermission('tool:aiCatalog:query')")
    @GetMapping("/runtime/{capability}")
    public ApiResponse<AiRuntimeStatusVO> runtime(@PathVariable String capability) {
        return ApiResponse.ok(aiCatalogService.runtimeStatus(capability));
    }

    @PreAuthorize("@authz.hasPermission('tool:aiCatalog:update')")
    @OperationLog(module = "AI Catalog", businessType = "TEST")
    @Idempotent("tool:aiCatalog:test")
    @PostMapping("/runtime/{capability}/test")
    public ApiResponse<String> test(@PathVariable String capability) {
        return ApiResponse.ok(aiCatalogService.testCapability(capability));
    }
}
