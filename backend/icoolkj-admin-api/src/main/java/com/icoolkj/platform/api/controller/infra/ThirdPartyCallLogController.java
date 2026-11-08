package com.icoolkj.platform.api.controller.infra;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.infra.dto.ThirdPartyCallLogPageQuery;
import com.icoolkj.platform.infra.service.ThirdPartyCallLogService;
import com.icoolkj.platform.infra.vo.ThirdPartyCallLogVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 第三方调用日志管理 */
@RestController
@RequestMapping("/infra/thirdPartyCallLog")
public class ThirdPartyCallLogController {

    private final ThirdPartyCallLogService thirdPartyCallLogService;

    public ThirdPartyCallLogController(ThirdPartyCallLogService thirdPartyCallLogService) {
        this.thirdPartyCallLogService = thirdPartyCallLogService;
    }

    @PreAuthorize("@authz.hasPermission('infra:thirdPartyCallLog:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<ThirdPartyCallLogVO>> page(ThirdPartyCallLogPageQuery query) {
        return ApiResponse.ok(thirdPartyCallLogService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('infra:thirdPartyCallLog:list')")
    @GetMapping("/{id}")
    public ApiResponse<ThirdPartyCallLogVO> getById(@PathVariable String id) {
        return ApiResponse.ok(thirdPartyCallLogService.getById(id));
    }

}
