package com.icoolkj.platform.api.controller.infra;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.infra.dto.AttachmentDemoBatchDeleteRequest;
import com.icoolkj.platform.infra.dto.AttachmentDemoCreateRequest;
import com.icoolkj.platform.infra.dto.AttachmentDemoPageQuery;
import com.icoolkj.platform.infra.dto.AttachmentDemoStatusUpdateRequest;
import com.icoolkj.platform.infra.dto.AttachmentDemoUpdateRequest;
import com.icoolkj.platform.infra.service.AttachmentDemoService;
import com.icoolkj.platform.infra.vo.AttachmentDemoVO;
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

/** 附件测试业务（规范闭环样例） */
@RestController
@RequestMapping("/infra/attachmentDemo")
public class AttachmentDemoController {

    private final AttachmentDemoService attachmentDemoService;

    public AttachmentDemoController(AttachmentDemoService attachmentDemoService) {
        this.attachmentDemoService = attachmentDemoService;
    }

    @PreAuthorize("@authz.hasPermission('infra:attachmentDemo:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<AttachmentDemoVO>> page(AttachmentDemoPageQuery query) {
        return ApiResponse.ok(attachmentDemoService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('infra:attachmentDemo:list')")
    @GetMapping("/{id}")
    public ApiResponse<AttachmentDemoVO> getById(@PathVariable String id) {
        return ApiResponse.ok(attachmentDemoService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('infra:attachmentDemo:add')")
    @OperationLog(module = "Attachment Demo", businessType = "CREATE")
    @Idempotent("infra:attachmentDemo:create")
    @PostMapping("/create")
    public ApiResponse<String> create(@Valid @RequestBody AttachmentDemoCreateRequest request) {
        return ApiResponse.ok(attachmentDemoService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('infra:attachmentDemo:update')")
    @OperationLog(module = "Attachment Demo", businessType = "UPDATE")
    @Idempotent("infra:attachmentDemo:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody AttachmentDemoUpdateRequest request) {
        attachmentDemoService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('infra:attachmentDemo:update')")
    @OperationLog(module = "Attachment Demo", businessType = "UPDATE")
    @Idempotent("infra:attachmentDemo:updateStatus")
    @PutMapping("/status")
    public ApiResponse<Void> updateStatus(@Valid @RequestBody AttachmentDemoStatusUpdateRequest request) {
        attachmentDemoService.updateStatus(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('infra:attachmentDemo:delete')")
    @OperationLog(module = "Attachment Demo", businessType = "DELETE")
    @Idempotent("infra:attachmentDemo:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        attachmentDemoService.deleteById(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('infra:attachmentDemo:delete')")
    @OperationLog(module = "Attachment Demo", businessType = "DELETE")
    @Idempotent("infra:attachmentDemo:batchDelete")
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody AttachmentDemoBatchDeleteRequest request) {
        attachmentDemoService.batchDelete(request);
        return ApiResponse.ok();
    }
}
