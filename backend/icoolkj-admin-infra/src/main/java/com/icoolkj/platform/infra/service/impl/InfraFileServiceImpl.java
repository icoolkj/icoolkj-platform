package com.icoolkj.platform.infra.service.impl;

import com.icoolkj.platform.common.enums.FileScanStatusEnum;
import com.icoolkj.platform.common.enums.FileSensitivityEnum;
import com.icoolkj.platform.common.enums.YesNoEnum;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.exception.FileStorageException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.storage.FileStoragePolicyUtils;
import com.icoolkj.platform.common.storage.FileStorageSettingsProvider;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.datascope.DataScopeSpec;
import com.icoolkj.platform.framework.datascope.OwnershipColumns;
import com.icoolkj.platform.framework.datascope.RowAccessSupport;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantQueryScope;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.infra.convert.InfraFileConvert;
import com.icoolkj.platform.infra.domain.InfraFileEntity;
import com.icoolkj.platform.infra.domain.InfraFileRefEntity;
import com.icoolkj.platform.infra.dto.InfraFileBatchDeleteRequest;
import com.icoolkj.platform.infra.dto.InfraFilePageQuery;
import com.icoolkj.platform.infra.dto.InfraFileRefBinding;
import com.icoolkj.platform.infra.dto.InfraFileRefCreateRequest;
import com.icoolkj.platform.infra.dto.InfraFileRefReleaseRequest;
import com.icoolkj.platform.infra.fileaccess.FileAccessCaller;
import com.icoolkj.platform.infra.fileaccess.FileAccessGuard;
import com.icoolkj.platform.infra.excel.ExcelExportAsyncConstants;
import com.icoolkj.platform.infra.excel.ExcelImportAsyncConstants;
import com.icoolkj.platform.infra.fileaccess.FileAccessPurpose;
import com.icoolkj.platform.infra.fileprofile.FileUploadProfileValidator;
import com.icoolkj.platform.infra.mapper.InfraFileMapper;
import com.icoolkj.platform.infra.mapper.InfraFileRefMapper;
import com.icoolkj.platform.infra.security.FileContentCipher;
import com.icoolkj.platform.infra.security.FileContentSniffer;
import com.icoolkj.platform.infra.security.FileImageWatermarkSupport;
import com.icoolkj.platform.infra.security.FileMalwareScanner;
import com.icoolkj.platform.infra.security.FileUploadRateLimiter;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.infra.storage.FileDeleteRequest;
import com.icoolkj.platform.infra.storage.FileDownloadRequest;
import com.icoolkj.platform.infra.storage.FileDownloadUrlRequest;
import com.icoolkj.platform.infra.storage.FileDownloadUrlResult;
import com.icoolkj.platform.infra.storage.FileStorageRouter;
import com.icoolkj.platform.infra.storage.FileStorageService;
import com.icoolkj.platform.infra.storage.FileUploadRequest;
import com.icoolkj.platform.infra.storage.FileUploadResult;
import com.icoolkj.platform.infra.storage.StorageProperties;
import com.icoolkj.platform.infra.util.FileSizeUtils;
import com.icoolkj.platform.infra.vo.FilePreviewUrlVO;
import com.icoolkj.platform.infra.vo.FileStorageOptionsVO;
import com.icoolkj.platform.infra.vo.InfraFileRefVO;
import com.icoolkj.platform.infra.vo.InfraFileVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** 文件服务实现 */
@Service
public class InfraFileServiceImpl implements InfraFileService {

    private static final Logger log = LoggerFactory.getLogger(InfraFileServiceImpl.class);

    private final InfraFileMapper infraFileMapper;
    private final InfraFileRefMapper infraFileRefMapper;
    private final FileStorageRouter fileStorageRouter;
    private final FileStorageSettingsProvider storageSettings;
    private final FileAccessGuard fileAccessGuard;
    private final FileUploadProfileValidator fileUploadProfileValidator;
    private final StorageProperties storageProperties;
    private final FileUploadRateLimiter fileUploadRateLimiter;
    private final FileMalwareScanner fileMalwareScanner;
    private final FileContentCipher fileContentCipher;
    private final FileImageWatermarkSupport fileImageWatermarkSupport;
    private final RowAccessSupport rowAccessSupport;

    public InfraFileServiceImpl(InfraFileMapper infraFileMapper,
                                InfraFileRefMapper infraFileRefMapper,
                                FileStorageRouter fileStorageRouter,
                                FileStorageSettingsProvider storageSettings,
                                FileAccessGuard fileAccessGuard,
                                FileUploadProfileValidator fileUploadProfileValidator,
                                StorageProperties storageProperties,
                                FileUploadRateLimiter fileUploadRateLimiter,
                                FileMalwareScanner fileMalwareScanner,
                                FileContentCipher fileContentCipher,
                                FileImageWatermarkSupport fileImageWatermarkSupport,
                                RowAccessSupport rowAccessSupport) {
        this.infraFileMapper = infraFileMapper;
        this.infraFileRefMapper = infraFileRefMapper;
        this.fileStorageRouter = fileStorageRouter;
        this.storageSettings = storageSettings;
        this.fileAccessGuard = fileAccessGuard;
        this.fileUploadProfileValidator = fileUploadProfileValidator;
        this.storageProperties = storageProperties;
        this.fileUploadRateLimiter = fileUploadRateLimiter;
        this.fileMalwareScanner = fileMalwareScanner;
        this.fileContentCipher = fileContentCipher;
        this.fileImageWatermarkSupport = fileImageWatermarkSupport;
        this.rowAccessSupport = rowAccessSupport;
    }

    @Override
    public PageResult<InfraFileVO> page(InfraFilePageQuery query) {
        TenantQueryScope.applyPageQuery(query);
        if (query.getTenantId() == null || query.getTenantId().isBlank()) {
            query.setTenantId(SecurityContextHelper.getTenantId());
        }
        // List/download parity for excel staging: peers must not see others' export/import files.
        if (!StringUtils.hasText(query.getExcelStagingOwnerUserId())) {
            query.setExcelStagingOwnerUserId(SecurityContextHelper.getUserId());
        }
        String tenantId = query.getTenantId();
        long total = infraFileMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<InfraFileEntity> list = infraFileMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        FileAccessCaller downloadCaller = FileAccessCaller.fromSecurityContext(FileAccessPurpose.DOWNLOAD);
        List<InfraFileVO> rows = list.stream()
                .map(entity -> enrichVo(entity, tenantId, downloadCaller))
                .toList();
        return PageResult.of(rows, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public InfraFileVO getById(String id) {
        // Same visibility as download so detail cannot leak excel staging metadata to peers
        // (DataScope ALL on tenant_admin previously bypassed submitter-only).
        return getById(id, FileAccessPurpose.DOWNLOAD);
    }

    @Override
    public InfraFileVO getById(String id, FileAccessPurpose purpose) {
        InfraFileEntity entity = requireFile(id, purpose);
        return enrichVo(entity, entity.getTenantId());
    }

    @Override
    public FileStorageOptionsVO storageOptions() {
        FileStorageOptionsVO vo = new FileStorageOptionsVO();
        vo.setDefaultProvider(storageSettings.getDefaultProvider());
        vo.setEnabledProviders(storageSettings.getEnabledProviders());
        vo.setMaxFileSizeMb(storageSettings.getMaxFileSizeMb());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InfraFileVO upload(MultipartFile file,
                              String storageType,
                              String bizType,
                              String bizId,
                              String refField,
                              String remark) {
        return upload(file, storageType, bizType, bizId, refField, remark, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InfraFileVO upload(MultipartFile file,
                              String storageType,
                              String bizType,
                              String bizId,
                              String refField,
                              String remark,
                              String replaceFileId) {
        return upload(file, storageType, bizType, bizId, refField, remark, replaceFileId, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InfraFileVO upload(MultipartFile file,
                              String storageType,
                              String bizType,
                              String bizId,
                              String refField,
                              String remark,
                              String replaceFileId,
                              String sensitivityLevel) {
        return uploadInternal(
                file, storageType, bizType, bizId, refField, remark, replaceFileId, sensitivityLevel, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InfraFileVO uploadExcelImportStaging(MultipartFile file, String taskId, String remark) {
        if (!StringUtils.hasText(taskId)) {
            throw new BusinessException("error.validation.required", "fields.id");
        }
        return uploadInternal(
                file,
                null,
                ExcelImportAsyncConstants.BIZ_TYPE,
                taskId.trim(),
                ExcelImportAsyncConstants.REF_ROLE,
                remark,
                null,
                null,
                true);
    }

    private InfraFileVO uploadInternal(MultipartFile file,
                                       String storageType,
                                       String bizType,
                                       String bizId,
                                       String refField,
                                       String remark,
                                       String replaceFileId,
                                       String sensitivityLevel,
                                       boolean allowReservedBizType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("error.validation.required", "fields.file");
        }
        String tenantId = requireTenantId();
        fileUploadRateLimiter.checkAndIncrement(tenantId, SecurityContextHelper.getUserId());
        assertBizTypeAllowed(bizType);
        if (!allowReservedBizType) {
            assertPublicBizTypeAllowed(bizType);
        }
        String resolvedStorageType = resolveStorageType(storageType);
        validateUploadPolicy(file);
        fileUploadProfileValidator.assertUploadAllowed(file, bizType);

        String originalName = StringUtils.hasText(file.getOriginalFilename())
                ? file.getOriginalFilename()
                : "file";
        String ext = extractExt(originalName);
        Path temp = null;
        try {
            temp = Files.createTempFile("icoolkj-upload-", ".bin");
            file.transferTo(temp);

            SecuredPayload secured = securePayload(temp, originalName, ext, file.getContentType());

            FileStorageService storage = fileStorageRouter.require(resolvedStorageType);
            FileUploadResult uploaded;
            try (InputStream in = new ByteArrayInputStream(secured.storedBytes())) {
                uploaded = storage.upload(FileUploadRequest.builder()
                        .tenantId(tenantId)
                        .storageType(resolvedStorageType)
                        .originalFilename(originalName)
                        .contentType(file.getContentType())
                        .size(secured.storedBytes().length)
                        .inputStream(in)
                        .build());
            }

            InfraFileEntity entity = new InfraFileEntity();
            entity.setId(IdGeneratorUtils.nextIdStr());
            entity.setTenantId(tenantId);
            entity.setBizType(StringUtils.hasText(bizType) ? bizType.trim() : null);
            entity.setBizId(StringUtils.hasText(bizId) ? bizId.trim() : null);
            entity.setOriginalName(originalName);
            entity.setFileName(uploaded.getStoredFileName());
            entity.setFileExt(ext);
            entity.setContentType(file.getContentType());
            entity.setFileSize(secured.plainSize());
            entity.setStorageType(uploaded.getStorageType());
            entity.setBucketName(uploaded.getBucketName());
            entity.setObjectKey(uploaded.getObjectKey());
            entity.setFileUrl(resolveStoredFileUrl(entity.getId(),
                    secured.encrypted() ? null : uploaded.getFileUrl()));
            entity.setMd5(secured.md5());
            applySecurityMeta(entity, secured, sensitivityLevel);
            OwnershipColumns.applyUser(entity::setUserId, SecurityContextHelper.getUserId());
            entity.setRemark(StringUtils.hasText(remark) ? remark.trim() : null);
            AuditFieldHelper.fillInsert(entity);
            infraFileMapper.insert(entity);

            if (StringUtils.hasText(bizType) && StringUtils.hasText(bizId)) {
                createRefInternal(tenantId, entity.getId(), bizType.trim(), bizId.trim(), refField);
            }
            softDeleteReplacedIfUnreferenced(tenantId, replaceFileId, entity.getId());
            return enrichVo(entity, tenantId);
        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new BusinessException("error.file.upload_failed");
        } catch (FileStorageException ex) {
            log.warn("File storage upload failed: {}", ex.getMessage(), ex);
            throw new BusinessException("error.file.upload_failed");
        } finally {
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException ignored) {
                    // best-effort cleanup
                }
            }
        }
    }

    @Override
    public String resolveAccessibleUrl(String fileId, String storedFileUrl) {
        if (StringUtils.hasText(storedFileUrl)) {
            return storedFileUrl.trim();
        }
        if (!StringUtils.hasText(fileId)) {
            return null;
        }
        return previewUrl(fileId.trim()).getUrl();
    }

    @Override
    public Resource loadDownloadResource(String id) {
        return loadDownloadResource(id, FileAccessPurpose.DOWNLOAD);
    }

    @Override
    public Resource loadDownloadResource(String id, FileAccessPurpose purpose) {
        InfraFileEntity entity = requireFile(id, purpose);
        return loadResource(entity);
    }

    private Resource loadResource(InfraFileEntity entity) {
        FileStorageService storage = fileStorageRouter.require(entity.getStorageType());
        Resource stored = storage.loadAsResource(FileDownloadRequest.builder()
                .storageType(entity.getStorageType())
                .bucketName(entity.getBucketName())
                .objectKey(entity.getObjectKey())
                .originalFilename(entity.getOriginalName())
                .contentType(entity.getContentType())
                .build());
        try (InputStream in = stored.getInputStream()) {
            byte[] bytes = in.readAllBytes();
            if (YesNoEnum.isYes(entity.getEncrypted())) {
                bytes = fileContentCipher.decrypt(bytes);
            }
            if (storageProperties.getSecurity().isVerifyMd5OnDownload()
                    && StringUtils.hasText(entity.getMd5())) {
                String actual = HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest(bytes));
                if (!entity.getMd5().equalsIgnoreCase(actual)) {
                    throw new BusinessException("error.file.integrity_failed");
                }
            }
            bytes = fileImageWatermarkSupport.maybeWatermark(
                    bytes, entity.getFileExt(), entity.getContentType(), entity.getSensitivityLevel());
            final byte[] payload = bytes;
            final String filename = entity.getOriginalName();
            return new ByteArrayResource(payload) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };
        } catch (BusinessException ex) {
            throw ex;
        } catch (IOException | NoSuchAlgorithmException ex) {
            log.warn("File download prepare failed: {}", ex.getMessage(), ex);
            throw new BusinessException("error.file.integrity_failed");
        }
    }

    @Override
    public FilePreviewUrlVO previewUrl(String id) {
        // Preview must not bypass download visibility (excel staging / CUSTOM policies).
        InfraFileEntity entity = requireFile(id, FileAccessPurpose.DOWNLOAD);
        if (!isPreviewable(entity.getContentType(), entity.getFileExt())) {
            throw new BusinessException("error.validation.invalid", "fields.preview");
        }
        FilePreviewUrlVO vo = new FilePreviewUrlVO();
        // Encrypted objects must be served via app download (decrypt + optional watermark).
        if (YesNoEnum.isYes(entity.getEncrypted())) {
            vo.setUrl("/infra/file/" + id + "/download?inline=1");
            vo.setExternal(false);
            return vo;
        }
        FileStorageService storage = fileStorageRouter.require(entity.getStorageType());
        FileDownloadUrlResult result = storage.previewUrl(FileDownloadUrlRequest.builder()
                .storageType(entity.getStorageType())
                .bucketName(entity.getBucketName())
                .objectKey(entity.getObjectKey())
                .build());
        if (StringUtils.hasText(result.getUrl())) {
            vo.setUrl(result.getUrl());
            vo.setExternal(result.isExternal());
        } else {
            vo.setUrl("/infra/file/" + id + "/download?inline=1");
            vo.setExternal(false);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        InfraFileEntity entity = requireManageableFile(id);
        assertDeletable(entity);
        deletePhysical(entity);
        infraFileRefMapper.deleteByFileId(entity.getTenantId(), id);
        infraFileMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void discardUnreferenced(String id) {
        InfraFileEntity entity = requireManageableFile(id);
        assertDeletable(entity);
        softDeleteFileIfUnreferenced(entity.getTenantId(), entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(InfraFileBatchDeleteRequest request) {
        for (String id : request.getIds()) {
            deleteById(id);
        }
    }

    @Override
    public List<InfraFileRefVO> listRefs(String fileId) {
        InfraFileEntity entity = requireFile(fileId);
        return infraFileRefMapper.selectActiveByFileId(entity.getTenantId(), fileId).stream()
                .map(this::toRefVo)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRef(String fileId, InfraFileRefCreateRequest request) {
        InfraFileEntity entity = requireFile(fileId);
        assertPublicBizTypeAllowed(request.getBizType());
        createRefInternal(entity.getTenantId(), fileId,
                request.getBizType().trim(), request.getBizId().trim(), request.getRefField());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseRef(String fileId, InfraFileRefReleaseRequest request) {
        InfraFileEntity entity = requireFile(fileId);
        InfraFileRefEntity ref = infraFileRefMapper.selectActive(
                entity.getTenantId(),
                fileId,
                request.getBizType().trim(),
                request.getBizId().trim(),
                request.getRefField());
        if (ref != null) {
            infraFileRefMapper.logicalDeleteById(ref.getId());
            softDeleteFileIfUnreferenced(entity.getTenantId(), fileId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InfraFileVO storeExcelExportStaging(Path file,
                                               String taskId,
                                               String originalFilename,
                                               String remark) {
        if (file == null || !Files.isRegularFile(file)) {
            throw new BusinessException("error.validation.required", "fields.file");
        }
        if (!StringUtils.hasText(taskId)) {
            throw new BusinessException("error.validation.required", "fields.id");
        }
        try {
            byte[] content = Files.readAllBytes(file);
            String name = StringUtils.hasText(originalFilename) ? originalFilename.trim() : "export.xlsx";
            return storeBinaryInternal(
                    content,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    name,
                    null,
                    ExcelExportAsyncConstants.BIZ_TYPE,
                    taskId.trim(),
                    ExcelExportAsyncConstants.REF_ROLE,
                    remark,
                    true);
        } catch (IOException ex) {
            throw new BusinessException("error.file.upload_failed");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InfraFileVO storeBinary(byte[] content,
                                   String contentType,
                                   String originalFilename,
                                   String storageType,
                                   String bizType,
                                   String bizId,
                                   String refField,
                                   String remark) {
        return storeBinaryInternal(
                content, contentType, originalFilename, storageType, bizType, bizId, refField, remark, false);
    }

    private InfraFileVO storeBinaryInternal(byte[] content,
                                            String contentType,
                                            String originalFilename,
                                            String storageType,
                                            String bizType,
                                            String bizId,
                                            String refField,
                                            String remark,
                                            boolean allowReservedBizType) {
        if (content == null || content.length == 0) {
            throw new BusinessException("error.validation.required", "fields.file");
        }
        String tenantId = requireTenantId();
        fileUploadRateLimiter.checkAndIncrement(tenantId, SecurityContextHelper.getUserId());
        assertBizTypeAllowed(bizType);
        if (!allowReservedBizType) {
            assertPublicBizTypeAllowed(bizType);
        }
        String resolvedStorageType = resolveStorageType(storageType);
        String safeName = StringUtils.hasText(originalFilename) ? originalFilename : "avatar.jpg";
        String ext = extractExt(safeName);
        Path temp = null;
        try {
            temp = Files.createTempFile("icoolkj-store-", ".bin");
            Files.write(temp, content);

            SecuredPayload secured = securePayload(temp, safeName, ext, contentType);

            FileStorageService storage = fileStorageRouter.require(resolvedStorageType);
            FileUploadResult uploaded;
            try (InputStream in = new ByteArrayInputStream(secured.storedBytes())) {
                uploaded = storage.upload(FileUploadRequest.builder()
                        .tenantId(tenantId)
                        .storageType(resolvedStorageType)
                        .originalFilename(safeName)
                        .contentType(contentType)
                        .size(secured.storedBytes().length)
                        .inputStream(in)
                        .build());
            }

            InfraFileEntity entity = new InfraFileEntity();
            entity.setId(IdGeneratorUtils.nextIdStr());
            entity.setTenantId(tenantId);
            entity.setBizType(StringUtils.hasText(bizType) ? bizType.trim() : null);
            entity.setBizId(StringUtils.hasText(bizId) ? bizId.trim() : null);
            entity.setOriginalName(safeName);
            entity.setFileName(uploaded.getStoredFileName());
            entity.setFileExt(ext);
            entity.setContentType(contentType);
            entity.setFileSize(secured.plainSize());
            entity.setStorageType(uploaded.getStorageType());
            entity.setBucketName(uploaded.getBucketName());
            entity.setObjectKey(uploaded.getObjectKey());
            entity.setFileUrl(resolveStoredFileUrl(entity.getId(),
                    secured.encrypted() ? null : uploaded.getFileUrl()));
            entity.setMd5(secured.md5());
            applySecurityMeta(entity, secured, null);
            OwnershipColumns.applyUser(entity::setUserId, SecurityContextHelper.getUserId());
            entity.setRemark(StringUtils.hasText(remark) ? remark.trim() : null);
            AuditFieldHelper.fillInsert(entity);
            infraFileMapper.insert(entity);

            if (StringUtils.hasText(bizType) && StringUtils.hasText(bizId)) {
                createRefInternal(tenantId, entity.getId(), bizType.trim(), bizId.trim(), refField);
            }
            return enrichVo(entity, tenantId);
        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new BusinessException("error.file.upload_failed");
        } catch (FileStorageException ex) {
            log.warn("File storage storeBinary failed: {}", ex.getMessage(), ex);
            throw new BusinessException("error.file.upload_failed");
        } finally {
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException ignored) {
                    // best-effort cleanup
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncBizFileRefs(String tenantId, String bizType, String bizId, List<InfraFileRefBinding> bindings) {
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(bizType) || !StringUtils.hasText(bizId)) {
            return;
        }
        assertPublicBizTypeAllowed(bizType);
        // 同一 ref_field 只保留最后一次绑定（同槽位多次上传用最新）
        java.util.LinkedHashMap<String, String> desiredByField = new java.util.LinkedHashMap<>();
        if (bindings != null) {
            for (InfraFileRefBinding binding : bindings) {
                if (binding == null || !StringUtils.hasText(binding.getFileId())) {
                    continue;
                }
                String field = StringUtils.hasText(binding.getRefField()) ? binding.getRefField().trim() : "";
                desiredByField.put(field, binding.getFileId().trim());
            }
        }

        List<InfraFileRefEntity> existing = infraFileRefMapper.selectActiveByBiz(tenantId, bizType, bizId);
        java.util.Set<String> releasedFileIds = new java.util.LinkedHashSet<>();
        for (InfraFileRefEntity ref : existing) {
            String field = StringUtils.hasText(ref.getRefField()) ? ref.getRefField().trim() : "";
            String desiredFileId = desiredByField.get(field);
            if (!StringUtils.hasText(desiredFileId) || !desiredFileId.equals(ref.getFileId())) {
                infraFileRefMapper.logicalDeleteById(ref.getId());
                releasedFileIds.add(ref.getFileId());
            }
        }
        for (String fileId : releasedFileIds) {
            softDeleteFileIfUnreferenced(tenantId, fileId);
        }

        for (Map.Entry<String, String> entry : desiredByField.entrySet()) {
            String refField = entry.getKey().isEmpty() ? null : entry.getKey();
            String fileId = entry.getValue();
            createRefInternal(tenantId, fileId, bizType, bizId, refField);
            attachFileBizId(tenantId, fileId, bizId);
        }
    }

    private void attachFileBizId(String tenantId, String fileId, String bizId) {
        InfraFileEntity file = infraFileMapper.selectById(fileId);
        if (file == null || !tenantId.equals(file.getTenantId())) {
            return;
        }
        if (bizId.equals(file.getBizId())) {
            return;
        }
        file.setBizId(bizId);
        infraFileMapper.updateById(file);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseBizFiles(String tenantId, String bizType, String bizId) {
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(bizType) || !StringUtils.hasText(bizId)) {
            return;
        }
        List<InfraFileRefEntity> refs = infraFileRefMapper.selectActiveByBiz(tenantId, bizType, bizId);
        if (CollectionUtils.isEmpty(refs)) {
            return;
        }
        java.util.Set<String> fileIds = new java.util.LinkedHashSet<>();
        for (InfraFileRefEntity ref : refs) {
            infraFileRefMapper.logicalDeleteById(ref.getId());
            fileIds.add(ref.getFileId());
        }
        for (String fileId : fileIds) {
            softDeleteFileIfUnreferenced(tenantId, fileId);
        }
    }

    /** 业务解绑后：无活跃引用则逻辑删文件（保留存储对象，与业务表软删对齐） */
    private void softDeleteFileIfUnreferenced(String tenantId, String fileId) {
        if (infraFileRefMapper.countActiveByFileId(tenantId, fileId) > 0) {
            return;
        }
        InfraFileEntity entity = infraFileMapper.selectById(fileId);
        if (entity == null || !tenantId.equals(entity.getTenantId())) {
            return;
        }
        if (entity.getDeleted() != null && entity.getDeleted() != 0) {
            return;
        }
        infraFileMapper.logicalDeleteById(fileId);
    }

    /** 同槽位重传：旧文件无引用则打删除标记，供定时 GC；已绑定引用的留给 syncBizFileRefs */
    private void softDeleteReplacedIfUnreferenced(String tenantId, String replaceFileId, String newFileId) {
        if (!StringUtils.hasText(replaceFileId) || replaceFileId.trim().equals(newFileId)) {
            return;
        }
        softDeleteFileIfUnreferenced(tenantId, replaceFileId.trim());
    }

    private InfraFileVO enrichVo(InfraFileEntity entity, String tenantId) {
        return enrichVo(entity, tenantId, FileAccessCaller.fromSecurityContext(FileAccessPurpose.DOWNLOAD));
    }

    private InfraFileVO enrichVo(InfraFileEntity entity, String tenantId, FileAccessCaller downloadCaller) {
        InfraFileVO vo = InfraFileConvert.toVo(entity);
        vo.setFileSizeDisplay(FileSizeUtils.format(entity.getFileSize() == null ? 0L : entity.getFileSize()));
        long refCount = infraFileRefMapper.countActiveByFileId(tenantId, entity.getId());
        vo.setReferenceCount(refCount);
        vo.setDeletable(refCount == 0);
        vo.setPreviewable(isPreviewable(entity.getContentType(), entity.getFileExt()));
        vo.setDownloadable(fileAccessGuard.isAccessible(entity, downloadCaller));
        // Never expose raw storage URLs for non-downloadable rows (e.g. MinIO publicDomain bypass).
        if (vo.isDownloadable()) {
            vo.setFileUrl(resolveAccessibleUrl(entity.getId(), entity.getFileUrl()));
        } else {
            vo.setFileUrl(null);
        }
        return vo;
    }

    /** local 等无公网地址的存储，统一写入应用内下载路径作为 file_url */
    private String resolveStoredFileUrl(String fileId, String uploadedUrl) {
        if (StringUtils.hasText(uploadedUrl)) {
            return uploadedUrl.trim();
        }
        return "/infra/file/" + fileId + "/download?inline=1";
    }

    private InfraFileRefVO toRefVo(InfraFileRefEntity entity) {
        InfraFileRefVO vo = new InfraFileRefVO();
        vo.setId(entity.getId());
        vo.setFileId(entity.getFileId());
        vo.setBizType(entity.getBizType());
        vo.setBizId(entity.getBizId());
        vo.setRefField(entity.getRefField());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    private void createRefInternal(String tenantId,
                                   String fileId,
                                   String bizType,
                                   String bizId,
                                   String refField) {
        InfraFileRefEntity existing = infraFileRefMapper.selectActive(
                tenantId, fileId, bizType, bizId, refField);
        if (existing != null) {
            return;
        }
        InfraFileRefEntity ref = new InfraFileRefEntity();
        ref.setId(IdGeneratorUtils.nextIdStr());
        ref.setTenantId(tenantId);
        ref.setFileId(fileId);
        ref.setBizType(bizType);
        ref.setBizId(bizId);
        ref.setRefField(StringUtils.hasText(refField) ? refField.trim() : null);
        ref.setCreatedBy(SecurityContextHelper.getUserId());
        ref.setCreatedAt(LocalDateTime.now());
        ref.setDeleted(0);
        infraFileRefMapper.insert(ref);
    }

    private void assertDeletable(InfraFileEntity entity) {
        long refCount = infraFileRefMapper.countActiveByFileId(entity.getTenantId(), entity.getId());
        if (refCount > 0) {
            throw new BusinessException("error.file.reference_exists");
        }
    }

    private void deletePhysical(InfraFileEntity entity) {
        FileStorageService storage = fileStorageRouter.require(entity.getStorageType());
        storage.delete(FileDeleteRequest.builder()
                .storageType(entity.getStorageType())
                .bucketName(entity.getBucketName())
                .objectKey(entity.getObjectKey())
                .build());
    }

    private InfraFileEntity requireManageableFile(String id) {
        InfraFileEntity entity = infraFileMapper.selectById(id);
        if (entity == null || (entity.getDeleted() != null && entity.getDeleted() != 0)) {
            throw new BusinessException("error.entity.not_found", "entity.name.file");
        }
        rowAccessSupport.assertReadable(
                entity.getTenantId(), DataScopeSpec.userOnly(), entity.getUserId(), null);
        return entity;
    }

    private InfraFileEntity requireFile(String id) {
        return requireFile(id, FileAccessPurpose.DOWNLOAD);
    }

    private InfraFileEntity requireFile(String id, FileAccessPurpose purpose) {
        InfraFileEntity entity = requireExistingFile(id);
        // 登录页公开素材必须按匿名 LOGIN_BRAND 鉴权，避免残留会话租户误伤平台品牌文件
        FileAccessCaller caller = purpose == FileAccessPurpose.LOGIN_BRAND
                ? FileAccessCaller.anonymous(purpose)
                : FileAccessCaller.fromSecurityContext(purpose);
        fileAccessGuard.assertAccessible(entity, caller);
        return entity;
    }

    private InfraFileEntity requireExistingFile(String id) {
        // 必须忽略租户行：平台 brand_* / 跨租户策略由 FileAccessGuard 判定
        InfraFileEntity entity = TenantScopeRunner.runIgnoringTenant(() -> infraFileMapper.selectById(id));
        if (entity == null || (entity.getDeleted() != null && entity.getDeleted() != 0)) {
            throw new BusinessException("error.entity.not_found", "entity.name.file");
        }
        return entity;
    }

    private String requireTenantId() {
        String tenantId = SecurityContextHelper.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
            throw new BusinessException("error.auth.unauthorized");
        }
        return tenantId;
    }

    private String resolveStorageType(String storageType) {
        String resolved = StringUtils.hasText(storageType)
                ? storageType.trim().toLowerCase(Locale.ROOT)
                : storageSettings.getDefaultProvider().toLowerCase(Locale.ROOT);
        if (!storageSettings.getEnabledProviders().contains(resolved)) {
            throw new BusinessException("error.file.storage_not_enabled");
        }
        fileStorageRouter.require(resolved);
        return resolved;
    }

    private void validateUploadPolicy(MultipartFile file) {
        long maxBytes = storageSettings.getMaxFileSizeMb() * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new BusinessException("error.file.size_exceeded", storageSettings.getMaxFileSizeMb());
        }
        String ext = FileStoragePolicyUtils.normalizeExtension(extractExt(file.getOriginalFilename()));
        List<String> blocked = storageSettings.getBlockedExtensions();
        if (StringUtils.hasText(ext) && blocked.contains(ext)) {
            throw new BusinessException("error.file.extension_blocked");
        }
        List<String> allowed = storageSettings.getAllowedExtensions();
        if (!allowed.isEmpty() && (!StringUtils.hasText(ext) || !allowed.contains(ext))) {
            throw new BusinessException("error.file.extension_not_allowed");
        }
    }

    private void assertBizTypeAllowed(String bizType) {
        if (storageProperties.getSecurity().isRequireBizType() && !StringUtils.hasText(bizType)) {
            throw new BusinessException("error.file.biz_type_required");
        }
    }

    /** Client / business APIs must not bind reserved staging bizTypes (e.g. excel-import). */
    private void assertPublicBizTypeAllowed(String bizType) {
        if (ExcelImportAsyncConstants.isReservedBizType(bizType)) {
            throw new BusinessException("error.file.biz_type_reserved");
        }
    }

    private SecuredPayload securePayload(Path temp,
                                         String originalFilename,
                                         String ext,
                                         String contentType) throws IOException, NoSuchAlgorithmException {
        StorageProperties.Security security = storageProperties.getSecurity();
        if (security.isContentSniffEnabled()) {
            FileContentSniffer.validate(temp, ext, contentType);
        }
        FileMalwareScanner.FileMalwareScanResult scanResult =
                fileMalwareScanner.scan(temp, originalFilename);
        if (!scanResult.clean()) {
            throw new BusinessException("error.file.malware_detected");
        }
        byte[] plain = Files.readAllBytes(temp);
        String md5 = HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest(plain));
        boolean encrypt = security.getEncryption().isEnabled();
        byte[] stored = encrypt ? fileContentCipher.encrypt(plain) : plain;
        return new SecuredPayload(stored, (long) plain.length, md5, encrypt, scanResult);
    }

    private void applySecurityMeta(InfraFileEntity entity, SecuredPayload secured, String sensitivityLevel) {
        boolean malwareEnabled = storageProperties.getSecurity().getMalwareScan().isEnabled();
        if (malwareEnabled) {
            entity.setScanStatus(FileScanStatusEnum.CLEAN.getCode());
            entity.setScanEngine(secured.scanResult().engine());
            entity.setScanAt(LocalDateTime.now());
        } else {
            entity.setScanStatus(FileScanStatusEnum.SKIPPED.getCode());
            entity.setScanEngine(secured.scanResult().engine());
            entity.setScanAt(null);
        }
        entity.setEncrypted(secured.encrypted() ? YesNoEnum.YES.getCode() : YesNoEnum.NO.getCode());
        FileSensitivityEnum sensitivity = FileSensitivityEnum.fromCode(sensitivityLevel);
        entity.setSensitivityLevel(
                sensitivity != null ? sensitivity.getCode() : FileSensitivityEnum.INTERNAL.getCode());
    }

    private record SecuredPayload(byte[] storedBytes,
                                  long plainSize,
                                  String md5,
                                  boolean encrypted,
                                  FileMalwareScanner.FileMalwareScanResult scanResult) {
    }

    private boolean isPreviewable(String contentType, String fileExt) {
        if (StringUtils.hasText(contentType)) {
            String lower = contentType.toLowerCase(Locale.ROOT);
            if (lower.startsWith("image/") || lower.startsWith("video/") || "application/pdf".equals(lower)) {
                return true;
            }
        }
        String ext = FileStoragePolicyUtils.normalizeExtension(fileExt);
        return "pdf".equals(ext) || "png".equals(ext) || "jpg".equals(ext) || "jpeg".equals(ext)
                || "gif".equals(ext) || "webp".equals(ext) || "bmp".equals(ext)
                || "mp4".equals(ext) || "webm".equals(ext) || "ogg".equals(ext) || "ogv".equals(ext) || "mov".equals(ext);
    }

    private String extractExt(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
    }
}
