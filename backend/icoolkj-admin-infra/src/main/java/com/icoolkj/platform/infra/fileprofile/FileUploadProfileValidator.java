package com.icoolkj.platform.infra.fileprofile;

import com.icoolkj.platform.common.enums.StatusEnum;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.storage.FileStoragePolicyUtils;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 按 Profile 规则校验单次上传（全局天花板仍由 InfraFileService 先跑）。
 */
@Component
public class FileUploadProfileValidator {

    private final FileUploadProfileResolver profileResolver;

    public FileUploadProfileValidator(FileUploadProfileResolver profileResolver) {
        this.profileResolver = profileResolver;
    }

    public void assertUploadAllowed(MultipartFile file, String bizType) {
        if (file == null || file.isEmpty() || !StringUtils.hasText(bizType)) {
            return;
        }
        String tenantId = SecurityContextHelper.getTenantId();
        Optional<ResolvedFileUploadProfile> resolved = profileResolver.resolve(tenantId, bizType.trim());
        if (resolved.isEmpty()) {
            return;
        }
        ResolvedFileUploadProfile profile = resolved.get();
        if (StatusEnum.isDisabled(profile.status())) {
            throw new BusinessException("error.file.profile_disabled", profile.profileCode());
        }

        String ext = normalizeExt(file.getOriginalFilename());
        if (!profile.allowedExts().isEmpty()) {
            if (!StringUtils.hasText(ext) || !profile.allowedExts().contains(ext)) {
                throw new BusinessException("error.file.extension_not_allowed");
            }
        }

        String contentType = file.getContentType();
        if (!profile.allowedContentTypes().isEmpty() && StringUtils.hasText(contentType)) {
            String ct = contentType.trim().toLowerCase(Locale.ROOT);
            boolean matched = profile.allowedContentTypes().stream().anyMatch(ct::equals);
            if (!matched) {
                throw new BusinessException("error.file.content_type_not_allowed");
            }
        }

        long size = file.getSize();
        if (profile.minSizeBytes() > 0 && size < profile.minSizeBytes()) {
            throw new BusinessException("error.file.size_too_small", profile.minSizeBytes());
        }
        if (profile.maxSizeBytes() > 0 && size > profile.maxSizeBytes()) {
            throw new BusinessException("error.file.profile_size_exceeded", profile.maxSizeBytes());
        }
    }

    private static String normalizeExt(String filename) {
        String ext = FileStoragePolicyUtils.normalizeExtension(
                filename != null && filename.contains(".")
                        ? filename.substring(filename.lastIndexOf('.'))
                        : "");
        return StringUtils.hasText(ext) ? ext.toLowerCase(Locale.ROOT) : "";
    }
}
