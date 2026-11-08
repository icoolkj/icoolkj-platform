package com.icoolkj.platform.api.controller.infra;

import com.icoolkj.platform.common.model.audit.AuditChainVerifyVO;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.infra.dto.ApiAccessLogPageQuery;
import com.icoolkj.platform.infra.service.ApiAccessLogService;
import com.icoolkj.platform.infra.service.impl.ApiAccessAuditChainVerifyService;
import com.icoolkj.platform.infra.vo.ApiAccessLogVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** API访问日志管理 */
@RestController
@RequestMapping("/infra/apiAccessLog")
public class ApiAccessLogController {

    private final ApiAccessLogService apiAccessLogService;
    private final ApiAccessAuditChainVerifyService apiAccessAuditChainVerifyService;

    public ApiAccessLogController(ApiAccessLogService apiAccessLogService,
                                  ApiAccessAuditChainVerifyService apiAccessAuditChainVerifyService) {
        this.apiAccessLogService = apiAccessLogService;
        this.apiAccessAuditChainVerifyService = apiAccessAuditChainVerifyService;
    }

    @PreAuthorize("@authz.hasPermission('monitor:apiLog:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<ApiAccessLogVO>> page(ApiAccessLogPageQuery query) {
        return ApiResponse.ok(apiAccessLogService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('monitor:apiLog:list')")
    @GetMapping("/integrity/verify")
    public ApiResponse<AuditChainVerifyVO> verifyIntegrity() {
        return ApiResponse.ok(apiAccessAuditChainVerifyService.verifyApiAccessChain());
    }

    @PreAuthorize("@authz.hasPermission('monitor:apiLog:list')")
    @GetMapping("/{id}")
    public ApiResponse<ApiAccessLogVO> getById(@PathVariable String id) {
        return ApiResponse.ok(apiAccessLogService.getById(id));
    }

}
