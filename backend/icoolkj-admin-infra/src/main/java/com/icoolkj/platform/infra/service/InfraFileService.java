package com.icoolkj.platform.infra.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.annotation.DataScope;
import com.icoolkj.platform.infra.dto.InfraFileBatchDeleteRequest;
import com.icoolkj.platform.infra.dto.InfraFilePageQuery;
import com.icoolkj.platform.infra.dto.InfraFileRefBinding;
import com.icoolkj.platform.infra.dto.InfraFileRefCreateRequest;
import com.icoolkj.platform.infra.dto.InfraFileRefReleaseRequest;
import com.icoolkj.platform.infra.fileaccess.FileAccessPurpose;
import com.icoolkj.platform.infra.vo.FilePreviewUrlVO;
import com.icoolkj.platform.infra.vo.FileStorageOptionsVO;
import com.icoolkj.platform.infra.vo.InfraFileRefVO;
import com.icoolkj.platform.infra.vo.InfraFileVO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** 文件服务 */
public interface InfraFileService {

    @DataScope(userColumn = "user_id", deptColumn = "")
    PageResult<InfraFileVO> page(InfraFilePageQuery query);

    InfraFileVO getById(String id);

    /** 按访问意图鉴权后读取元数据（如登录页匿名品牌） */
    InfraFileVO getById(String id, FileAccessPurpose purpose);

    FileStorageOptionsVO storageOptions();

    InfraFileVO upload(MultipartFile file,
                       String storageType,
                       String bizType,
                       String bizId,
                       String refField,
                       String remark);

    /**
     * 上传并替换旧文件：成功后对 {@code replaceFileId} 在无活跃引用时逻辑删除（供同槽位重传，走 upload 权限）。
     */
    InfraFileVO upload(MultipartFile file,
                       String storageType,
                       String bizType,
                       String bizId,
                       String refField,
                       String remark,
                       String replaceFileId);

    /**
     * 上传并可指定密级（{@code sensitivityLevel} 为空时默认内部）。
     */
    InfraFileVO upload(MultipartFile file,
                       String storageType,
                       String bizType,
                       String bizId,
                       String refField,
                       String remark,
                       String replaceFileId,
                       String sensitivityLevel);

    /**
     * Platform-only staging upload for async Excel import ({@code bizType=excel-import}).
     * Public {@link #upload} rejects this reserved bizType.
     */
    InfraFileVO uploadExcelImportStaging(MultipartFile file, String taskId, String remark);

    /**
     * Platform-only staging store for async Excel export ({@code bizType=excel-export}).
     */
    InfraFileVO storeExcelExportStaging(java.nio.file.Path file,
                                        String taskId,
                                        String originalFilename,
                                        String remark);

    Resource loadDownloadResource(String id);

    /** 按访问意图鉴权后加载下载资源 */
    Resource loadDownloadResource(String id, FileAccessPurpose purpose);

    FilePreviewUrlVO previewUrl(String id);

    void deleteById(String id);

    /**
     * 无活跃引用时逻辑删除（不删存储对象），供业务页换图/清空；需要 upload 权限即可。
     * 仍有引用时抛错，避免误删已绑定文件。
     */
    void discardUnreferenced(String id);

    void batchDelete(InfraFileBatchDeleteRequest request);

    List<InfraFileRefVO> listRefs(String fileId);

    void addRef(String fileId, InfraFileRefCreateRequest request);

    void releaseRef(String fileId, InfraFileRefReleaseRequest request);

    InfraFileVO storeBinary(byte[] content,
                            String contentType,
                            String originalFilename,
                            String storageType,
                            String bizType,
                            String bizId,
                            String refField,
                            String remark);

    /** 解除业务与文件的引用；无其他引用时将 infra_file 逻辑删除（不删存储对象） */
    void releaseBizFiles(String tenantId, String bizType, String bizId);

    /** 按业务期望引用列表增删 infra_file_ref；移除的引用触发文件逻辑删除（无其他引用时） */
    void syncBizFileRefs(String tenantId, String bizType, String bizId, List<InfraFileRefBinding> bindings);

    /** 返回前端可直接使用的访问路径（MinIO 为完整 URL，local 为 API 下载路径） */
    String resolveAccessibleUrl(String fileId, String storedFileUrl);
}
