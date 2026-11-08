package com.icoolkj.platform.api.controller.dashboard;

import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.system.workbench.service.WorkbenchService;
import com.icoolkj.platform.system.workbench.vo.WorkbenchVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 租户工作台 */
@RestController
@RequestMapping("/dashboard/workbench")
public class WorkbenchController {

    private final WorkbenchService workbenchService;

    public WorkbenchController(WorkbenchService workbenchService) {
        this.workbenchService = workbenchService;
    }

    @PreAuthorize("@authz.isAuthenticated()")
    @GetMapping
    public ApiResponse<WorkbenchVO> load() {
        return ApiResponse.ok(workbenchService.load());
    }
}
