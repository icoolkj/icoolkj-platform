package com.icoolkj.platform.infra.fileprofile;

import java.util.List;

/**
 * 解析后的有效上传规则（租户覆盖合并平台边界后）。
 */
public record ResolvedFileUploadProfile(
        String profileCode,
        String profileName,
        String categoryTag,
        String sourceTenantId,
        boolean tenantOverride,
        List<String> allowedExts,
        List<String> allowedContentTypes,
        boolean required,
        int minNum,
        int maxNum,
        long minSizeBytes,
        long maxSizeBytes,
        int sortNo,
        String status
) {
}
