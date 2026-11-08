package com.icoolkj.platform.api.controller.monitor;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.system.job.dto.SysJobLogPageQuery;
import com.icoolkj.platform.system.job.service.SysJobLogService;
import com.icoolkj.platform.system.job.vo.SysJobLogVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Platform scheduled job execution logs (sys_job_log). */
@RestController
@RequestMapping("/monitor/jobLog")
public class SysJobLogController {

    private final SysJobLogService sysJobLogService;

    public SysJobLogController(SysJobLogService sysJobLogService) {
        this.sysJobLogService = sysJobLogService;
    }

    @PreAuthorize("@authz.hasPermission('monitor:jobLog:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<SysJobLogVO>> page(SysJobLogPageQuery query) {
        return ApiResponse.ok(sysJobLogService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('monitor:jobLog:list')")
    @GetMapping("/{id}")
    public ApiResponse<SysJobLogVO> get(@PathVariable String id) {
        return ApiResponse.ok(sysJobLogService.getById(id));
    }
}
