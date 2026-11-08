package com.icoolkj.platform.infra.service;

import com.icoolkj.platform.infra.vo.UserAvatarPolicyVO;
import com.icoolkj.platform.infra.vo.UserAvatarUploadVO;
import org.springframework.web.multipart.MultipartFile;

/** 用户头像上传（统一文件管理 + 多尺寸缩略图） */
public interface UserAvatarFileService {

    UserAvatarPolicyVO policy();

    UserAvatarUploadVO uploadForUser(String tenantId, String userId, MultipartFile file);

    /** 释放用户头像关联文件（如用户删除时） */
    void releaseForUser(String tenantId, String userId);
}
