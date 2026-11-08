package com.icoolkj.platform.infra.service.impl;

import com.icoolkj.platform.common.constant.UserAvatarConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.storage.AvatarSettingsProvider;
import com.icoolkj.platform.common.storage.FileStoragePolicyUtils;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.infra.service.UserAvatarFileService;
import com.icoolkj.platform.infra.util.ImageContentValidator;
import com.icoolkj.platform.infra.util.ImageResizeUtils;
import com.icoolkj.platform.infra.vo.InfraFileVO;
import com.icoolkj.platform.infra.vo.UserAvatarPolicyVO;
import com.icoolkj.platform.infra.vo.UserAvatarUploadVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Service
public class UserAvatarFileServiceImpl implements UserAvatarFileService {

    private static final String CONTENT_TYPE_JPEG = "image/jpeg";

    private final InfraFileService infraFileService;
    private final AvatarSettingsProvider avatarSettings;

    public UserAvatarFileServiceImpl(InfraFileService infraFileService,
                                       AvatarSettingsProvider avatarSettings) {
        this.infraFileService = infraFileService;
        this.avatarSettings = avatarSettings;
    }

    @Override
    public UserAvatarPolicyVO policy() {
        UserAvatarPolicyVO vo = new UserAvatarPolicyVO();
        vo.setMaxSizeMb(avatarSettings.getMaxSizeMb());
        vo.setOutputSize(avatarSettings.getOutputSize());
        vo.setThumbSmSize(avatarSettings.getThumbSmSize());
        vo.setThumbMdSize(avatarSettings.getThumbMdSize());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAvatarUploadVO uploadForUser(String tenantId, String userId, MultipartFile file) {
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(userId)) {
            throw new BusinessException("error.validation.required", "fields.user");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("error.validation.required", "fields.file");
        }

        validateAvatarUpload(file);
        byte[] rawBytes = readBytes(file);
        String ext = ImageContentValidator.normalizeExt(extractExt(file.getOriginalFilename()));
        if (avatarSettings.isContentScanEnabled()) {
            ImageContentValidator.validateImageBytes(rawBytes, ext);
        }

        int outputSize = avatarSettings.getOutputSize();
        int smSize = avatarSettings.getThumbSmSize();
        int mdSize = avatarSettings.getThumbMdSize();

        byte[] mainBytes = ImageResizeUtils.resizeSquareJpeg(rawBytes, outputSize);
        byte[] smBytes = ImageResizeUtils.resizeSquareJpeg(mainBytes, smSize);
        byte[] mdBytes = ImageResizeUtils.resizeSquareJpeg(mainBytes, mdSize);

        infraFileService.releaseBizFiles(tenantId, UserAvatarConstants.BIZ_TYPE, userId);

        InfraFileVO main = infraFileService.storeBinary(
                mainBytes, CONTENT_TYPE_JPEG, buildFilename(userId, "lg"), null,
                UserAvatarConstants.BIZ_TYPE, userId, UserAvatarConstants.REF_MAIN, "avatar-main");
        InfraFileVO sm = infraFileService.storeBinary(
                smBytes, CONTENT_TYPE_JPEG, buildFilename(userId, "sm"), null,
                UserAvatarConstants.BIZ_TYPE, userId, UserAvatarConstants.REF_SM, "avatar-sm");
        InfraFileVO md = infraFileService.storeBinary(
                mdBytes, CONTENT_TYPE_JPEG, buildFilename(userId, "md"), null,
                UserAvatarConstants.BIZ_TYPE, userId, UserAvatarConstants.REF_MD, "avatar-md");

        UserAvatarUploadVO result = new UserAvatarUploadVO();
        result.setAvatarFileId(main.getId());
        result.setAvatarUrlLg(resolveUrl(main));
        result.setAvatarUrlSm(resolveUrl(sm));
        result.setAvatarUrlMd(resolveUrl(md));
        result.setAvatarUrl(resolveUrl(md));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseForUser(String tenantId, String userId) {
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(userId)) {
            return;
        }
        infraFileService.releaseBizFiles(tenantId, UserAvatarConstants.BIZ_TYPE, userId);
    }

    private void validateAvatarUpload(MultipartFile file) {
        long maxBytes = avatarSettings.getMaxSizeMb() * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new BusinessException("error.avatar.size_exceeded", avatarSettings.getMaxSizeMb());
        }
        String ext = FileStoragePolicyUtils.normalizeExtension(extractExt(file.getOriginalFilename()));
        List<String> allowed = avatarSettings.getAllowedExtensions();
        if (!allowed.isEmpty() && (!StringUtils.hasText(ext) || !allowed.contains(ext))) {
            throw new BusinessException("error.avatar.extension_not_allowed");
        }
        String contentType = file.getContentType();
        if (StringUtils.hasText(contentType) && !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BusinessException("error.avatar.invalid_image");
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new BusinessException("error.file.upload_failed");
        }
    }

    private String buildFilename(String userId, String suffix) {
        return "avatar-" + userId + "-" + suffix + ".jpg";
    }

    private String resolveUrl(InfraFileVO file) {
        return infraFileService.resolveAccessibleUrl(file.getId(), file.getFileUrl());
    }

    private String extractExt(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
    }
}
