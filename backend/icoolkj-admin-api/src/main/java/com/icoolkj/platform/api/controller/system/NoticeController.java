package com.icoolkj.platform.api.controller.system;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.system.notice.dto.NoticeBatchDeleteRequest;
import com.icoolkj.platform.system.notice.dto.NoticeCreateRequest;
import com.icoolkj.platform.system.notice.dto.NoticePageQuery;
import com.icoolkj.platform.system.notice.dto.NoticePublishedQuery;
import com.icoolkj.platform.system.notice.dto.NoticeUpdateRequest;
import com.icoolkj.platform.system.notice.service.NoticeService;
import com.icoolkj.platform.system.notice.vo.NoticeApproveDetailVO;
import com.icoolkj.platform.system.notice.vo.NoticeListVO;
import com.icoolkj.platform.system.notice.vo.NoticePublishedVO;
import com.icoolkj.platform.system.notice.vo.NoticeVO;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** 通知公告管理 */
@RestController
@RequestMapping("/system/notice")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @PreAuthorize("@authz.hasPermission('system:notice:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<NoticeListVO>> page(NoticePageQuery query) {
        return ApiResponse.ok(noticeService.page(query));
    }

    @PreAuthorize("@authz.isAuthenticated()")
    @GetMapping("/published")
    public ApiResponse<List<NoticePublishedVO>> listPublished(@Valid NoticePublishedQuery query) {
        return ApiResponse.ok(noticeService.listPublished(query));
    }

    @PreAuthorize("@authz.isAuthenticated()")
    @GetMapping("/published/{id}")
    public ApiResponse<NoticePublishedVO> getPublishedById(@PathVariable String id) {
        return ApiResponse.ok(noticeService.getPublishedById(id));
    }

    @PreAuthorize("@authz.hasPermission('system:notice:list')")
    @GetMapping("/{id}")
    public ApiResponse<NoticeVO> getById(@PathVariable String id) {
        return ApiResponse.ok(noticeService.getById(id));
    }

    @PreAuthorize("@authz.isAuthenticated()")
    @OperationLog(module = "System Notice", businessType = "EXPORT", recordResponse = false)
    @GetMapping("/content-file/{fileId}/download")
    public ResponseEntity<Resource> downloadContentFile(@PathVariable String fileId,
                                                      @RequestParam(value = "inline", defaultValue = "true") boolean inline) {
        Resource resource = noticeService.loadContentFile(fileId);
        String contentType = noticeService.resolveContentFileMediaType(fileId);
        MediaType mediaType = StringUtils.hasText(contentType)
                ? MediaType.parseMediaType(contentType)
                : MediaTypeFactory.getMediaType("notice-file-" + fileId)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM);
        String filename = "notice-file-" + fileId;
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        String disposition = inline ? "inline" : "attachment";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename*=UTF-8''" + encoded)
                .contentType(mediaType)
                .body(resource);
    }

    @PreAuthorize("@authz.hasPermission('system:notice:add')")
    @OperationLog(module = "System Notice", businessType = "CREATE")
    @Idempotent("system:notice:create")
    @PostMapping("/create")
    public ApiResponse<String> create(@Valid @RequestBody NoticeCreateRequest request) {
        return ApiResponse.ok(noticeService.create(request));
    }

    @PreAuthorize("@authz.hasPermission('system:notice:update')")
    @OperationLog(module = "System Notice", businessType = "UPDATE")
    @Idempotent("system:notice:update")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody NoticeUpdateRequest request) {
        noticeService.update(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:notice:delete')")
    @OperationLog(module = "System Notice", businessType = "DELETE")
    @Idempotent("system:notice:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable String id) {
        noticeService.deleteById(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:notice:delete')")
    @OperationLog(module = "System Notice", businessType = "DELETE")
    @Idempotent("system:notice:batchDelete")
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody NoticeBatchDeleteRequest request) {
        noticeService.batchDelete(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:notice:close')")
    @OperationLog(module = "System Notice", businessType = "CLOSE")
    @Idempotent("system:notice:close")
    @PostMapping("/{id}/close")
    public ApiResponse<Void> close(@PathVariable String id) {
        noticeService.close(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:notice:publish')")
    @OperationLog(module = "System Notice", businessType = "PUBLISH")
    @Idempotent("system:notice:publish")
    @PostMapping("/{id}/publish")
    public ApiResponse<Void> publish(@PathVariable String id) {
        noticeService.publish(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:notice:publish')")
    @OperationLog(module = "System Notice", businessType = "UNPUBLISH")
    @Idempotent("system:notice:unpublish")
    @PostMapping("/{id}/unpublish")
    public ApiResponse<Void> unpublish(@PathVariable String id) {
        noticeService.unpublish(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('system:notice:submit')")
    @OperationLog(module = "System Notice", businessType = "SUBMIT")
    @Idempotent("system:notice:submit")
    @PostMapping("/{id}/submit-approve")
    public ApiResponse<String> submitApprove(@PathVariable String id) {
        return ApiResponse.ok(noticeService.submitApprove(id));
    }

    @PreAuthorize("@authz.hasPermission('system:notice:withdraw')")
    @OperationLog(module = "System Notice", businessType = "WITHDRAW")
    @Idempotent("system:notice:withdraw")
    @PostMapping("/{id}/withdraw")
    public ApiResponse<Void> withdraw(@PathVariable String id) {
        noticeService.withdraw(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('workflow:approve:notice:view')"
            + " or @authz.hasPermission('system:notice:list')"
            + " or @authz.hasPermission('workflow:submitted:view')"
            + " or @authz.hasPermission('workflow:todo:view')"
            + " or @authz.hasPermission('workflow:done:view')"
            + " or @authz.hasPermission('workflow:cc:view')")
    @GetMapping("/{id}/approve-detail")
    public ApiResponse<NoticeApproveDetailVO> getApproveDetail(@PathVariable String id) {
        return ApiResponse.ok(noticeService.getApproveDetail(id));
    }
}
