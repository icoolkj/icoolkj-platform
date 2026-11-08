package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.system.config.dto.ConfigCreateRequest;
import com.icoolkj.platform.system.config.dto.ConfigPageQuery;
import com.icoolkj.platform.system.config.dto.ConfigUpdateRequest;
import com.icoolkj.platform.system.config.service.ConfigAdminService;
import com.icoolkj.platform.system.config.vo.ConfigBootstrapVO;
import com.icoolkj.platform.system.config.vo.ConfigResolvedVO;
import com.icoolkj.platform.system.config.vo.ConfigVO;
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

/** 参数配置管理 */
@RestController
@RequestMapping("/system/config")
public class ConfigController {

    private final ConfigAdminService configAdminService;

    public ConfigController(ConfigAdminService configAdminService) {
        this.configAdminService = configAdminService;
    }

    @GetMapping("/bootstrap")
    public ApiResponse<ConfigBootstrapVO> bootstrap() {
        return ApiResponse.ok(configAdminService.getBootstrap());
    }

    /** Whitelisted tenant-resolved config (e.g. approval quick comments). */
    @GetMapping("/resolved/{configKey}")
    public ApiResponse<ConfigResolvedVO> getResolved(@PathVariable String configKey) {
        return ApiResponse.ok(configAdminService.getResolved(configKey));
    }

    @PreAuthorize("@authz.hasPermission('system:config:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<ConfigVO>> page(ConfigPageQuery query) {
        return ApiResponse.ok(configAdminService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('system:config:list')")
    @GetMapping("/key/{configKey}")
    public ApiResponse<ConfigVO> getByKey(@PathVariable String configKey) {
        return ApiResponse.ok(configAdminService.getByKey(configKey));
    }

    @PreAuthorize("@authz.hasPermission('system:config:list')")
    @GetMapping("/{id}")
    public ApiResponse<ConfigVO> getById(@PathVariable String id) {
        return ApiResponse.ok(configAdminService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('system:config:add')")
    @OperationLog(module = "System Config", businessType = "CREATE")
    @Idempotent("system:config:create")
    @PostMapping("/create")
    public ApiResponse<String> create(@Valid @RequestBody ConfigCreateRequest request) {
        return ApiResponse.ok(configAdminService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('system:config:update')")
    @OperationLog(module = "System Config", businessType = "UPDATE")
    @Idempotent("system:config:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody ConfigUpdateRequest request) {
        configAdminService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:config:update')")
    @OperationLog(module = "System Config", businessType = "REFRESH_CACHE")
    @Idempotent("system:config:refreshCache")
    @PostMapping("/refreshCache")
    public ApiResponse<Void> refreshCache() {
        configAdminService.refreshCache();
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:config:delete')")
    @OperationLog(module = "System Config", businessType = "DELETE")
    @Idempotent("system:config:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        configAdminService.deleteById(id);
        return ApiResponse.ok();
    }
}
