package com.icoolkj.platform.api.controller.infra;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.infra.fileaccess.FileAccessPurpose;
import com.icoolkj.platform.infra.dto.InfraFileBatchDeleteRequest;
import com.icoolkj.platform.infra.dto.InfraFilePageQuery;
import com.icoolkj.platform.infra.dto.InfraFileRefCreateRequest;
import com.icoolkj.platform.infra.dto.InfraFileRefReleaseRequest;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.infra.vo.FilePreviewUrlVO;
import com.icoolkj.platform.infra.vo.FileStorageOptionsVO;
import com.icoolkj.platform.infra.vo.InfraFileRefVO;
import com.icoolkj.platform.infra.vo.InfraFileVO;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** 文件管理 */
@RestController
@RequestMapping("/infra/file")
public class InfraFileController {

    private final InfraFileService infraFileService;

    public InfraFileController(InfraFileService infraFileService) {
        this.infraFileService = infraFileService;
    }

    @PreAuthorize("@authz.hasPermission('infra:file:list')")
    @GetMapping("/page")
    public ApiResponse<PageResult<InfraFileVO>> page(InfraFilePageQuery query) {
        return ApiResponse.ok(infraFileService.page(query));
    }

    @PreAuthorize("@authz.hasPermission('infra:file:list')")
    @GetMapping("/{id}")
    public ApiResponse<InfraFileVO> getById(@PathVariable String id) {
        return ApiResponse.ok(infraFileService.getById(id));
    }

    @PreAuthorize("@authz.hasPermission('infra:file:list')")
    @GetMapping("/storage-options")
    public ApiResponse<FileStorageOptionsVO> storageOptions() {
        return ApiResponse.ok(infraFileService.storageOptions());
    }

    @PreAuthorize("@authz.hasPermission('infra:file:upload')")
    @OperationLog(module = "InfraFile", businessType = "INSERT")
    //@Idempotent("infra:file:upload")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<InfraFileVO> upload(@RequestParam("file") MultipartFile file,
                                           @RequestParam(value = "storageType", required = false) String storageType,
                                           @RequestParam(value = "bizType", required = false) String bizType,
                                           @RequestParam(value = "bizId", required = false) String bizId,
                                           @RequestParam(value = "refField", required = false) String refField,
                                           @RequestParam(value = "remark", required = false) String remark,
                                           @RequestParam(value = "replaceFileId", required = false) String replaceFileId,
                                           @RequestParam(value = "sensitivityLevel", required = false) String sensitivityLevel) {
        return ApiResponse.ok(infraFileService.upload(
                file, storageType, bizType, bizId, refField, remark, replaceFileId, sensitivityLevel));
    }

    @PreAuthorize("@authz.hasPermission('infra:file:download')")
    @OperationLog(module = "InfraFile", businessType = "EXPORT", recordResponse = false)
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable String id,
                                             @RequestParam(value = "inline", defaultValue = "false") boolean inline) {
        InfraFileVO meta = infraFileService.getById(id, FileAccessPurpose.DOWNLOAD);
        Resource resource = infraFileService.loadDownloadResource(id);
        String encoded = URLEncoder.encode(meta.getOriginalName(), StandardCharsets.UTF_8).replace("+", "%20");
        MediaType mediaType = MediaTypeFactory.getMediaType(meta.getOriginalName())
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        String disposition = inline ? "inline" : "attachment";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename*=UTF-8''" + encoded)
                .contentType(mediaType)
                .body(resource);
    }

    @PreAuthorize("@authz.hasPermission('infra:file:list')")
    @GetMapping("/{id}/preview-url")
    public ApiResponse<FilePreviewUrlVO> previewUrl(@PathVariable String id) {
        return ApiResponse.ok(infraFileService.previewUrl(id));
    }

    @PreAuthorize("@authz.hasPermission('infra:file:delete')")
    @OperationLog(module = "InfraFile", businessType = "DELETE")
    @Idempotent("infra:file:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        infraFileService.deleteById(id);
        return ApiResponse.ok();
    }

    /** 无引用时逻辑删除（换图/清空），只需 upload 权限 */
    @PreAuthorize("@authz.hasPermission('infra:file:upload')")
    @OperationLog(module = "InfraFile", businessType = "DELETE")
    @DeleteMapping("/{id}/unreferenced")
    public ApiResponse<Void> discardUnreferenced(@PathVariable String id) {
        infraFileService.discardUnreferenced(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('infra:file:delete')")
    @OperationLog(module = "InfraFile", businessType = "DELETE")
    @Idempotent("infra:file:delete")
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody InfraFileBatchDeleteRequest request) {
        infraFileService.batchDelete(request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('infra:file:list')")
    @GetMapping("/{id}/refs")
    public ApiResponse<List<InfraFileRefVO>> listRefs(@PathVariable String id) {
        return ApiResponse.ok(infraFileService.listRefs(id));
    }

    @PreAuthorize("@authz.hasPermission('infra:file:upload')")
    @OperationLog(module = "InfraFile", businessType = "UPDATE")
    @PostMapping("/{id}/refs")
    public ApiResponse<Void> addRef(@PathVariable String id, @Valid @RequestBody InfraFileRefCreateRequest request) {
        infraFileService.addRef(id, request);
        return ApiResponse.ok();
    }

    @PreAuthorize("@authz.hasPermission('infra:file:upload')")
    @OperationLog(module = "InfraFile", businessType = "UPDATE")
    @DeleteMapping("/{id}/refs")
    public ApiResponse<Void> releaseRef(@PathVariable String id, @Valid @RequestBody InfraFileRefReleaseRequest request) {
        infraFileService.releaseRef(id, request);
        return ApiResponse.ok();
    }
}
