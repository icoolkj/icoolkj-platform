package com.icoolkj.platform.api.controller.monitor;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.framework.job.ManagedJobTriggerService;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.system.job.domain.SysJobEntity;
import com.icoolkj.platform.system.job.dto.SysJobPageQuery;
import com.icoolkj.platform.system.job.dto.SysJobUpdateRequest;
import com.icoolkj.platform.system.job.service.SysJobService;
import com.icoolkj.platform.system.job.vo.SysJobVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Platform scheduled job console (definitions in sys_job). */
@RestController
@RequestMapping("/monitor/job")
public class ManagedJobController {

    private final SysJobService sysJobService;
    private final ManagedJobTriggerService managedJobTriggerService;

    public ManagedJobController(SysJobService sysJobService,
                                ManagedJobTriggerService managedJobTriggerService) {
        this.sysJobService = sysJobService;
        this.managedJobTriggerService = managedJobTriggerService;
    }

    @PreAuthorize("@authz.hasPermission('monitor:job:list')")
    @GetMapping("/list")
    public ApiResponse<java.util.List<SysJobVO>> list() {
        return ApiResponse.ok(sysJobService.listWithRuntimeStatus());
    }

    @PreAuthorize("@authz.hasPermission('monitor:job:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<SysJobVO>> page(SysJobPageQuery query) {
        return ApiResponse.ok(sysJobService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('monitor:job:list')")
    @GetMapping("/{id}")
    public ApiResponse<SysJobVO> get(@PathVariable String id) {
        return ApiResponse.ok(sysJobService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('monitor:job:update')")
    @OperationLog(module = "Managed Job", businessType = "UPDATE")
    @Idempotent("monitor:job:update")
    @PutMapping
    public ApiResponse<Void> update(@Valid @RequestBody SysJobUpdateRequest request) {
        sysJobService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('monitor:job:update')")
    @OperationLog(module = "Managed Job", businessType = "STATUS")
    @Idempotent("monitor:job:status")
    @PutMapping("/{id}/status")
    public ApiResponse<Void> changeStatus(@PathVariable String id, @RequestParam String status) {
        sysJobService.changeStatus(id, status);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('monitor:job:run')")
    @OperationLog(module = "Managed Job", businessType = "RUN")
    @Idempotent("monitor:job:run")
    @PostMapping("/{jobCode}/run")
    public ApiResponse<Void> run(@PathVariable String jobCode) {
        SysJobEntity job = sysJobService.requireByJobCode(jobCode);
        if (!CommonConstants.STATUS_NORMAL.equals(job.getStatus())) {
            throw new BusinessException("error.job.paused");
        }
        managedJobTriggerService.enqueue(jobCode, SecurityContextHelper.getUserId());
        return ApiResponse.ok();
    }
}
