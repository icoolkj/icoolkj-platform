package com.icoolkj.platform.system.auth.vo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * 登录页公开品牌（匿名）。
 * 素材一律 {@code catalogCode → profileCode}，避免跨目录槽位混淆。
 */
@Data
public class AuthLoginBrandVO {

    private String tenantCode;
    private String tenantName;
    private boolean tenantMatched;

    private String productName;
    private Map<String, String> productNameI18n;

    /**
     * catalogCode → (profileCode → 公开 URL)。
     * URL 形如 {@code /auth/login-brand/asset?catalogCode=...&profileCode=...}。
     */
    private Map<String, Map<String, String>> assets;

    /**
     * catalogCode → (profileCode → 说明文案)。
     */
    private Map<String, Map<String, String>> labels;

    /**
     * catalogCode → 启用中的槽位 profile_code 有序列表。
     */
    private Map<String, List<String>> catalogs;

    public Map<String, Map<String, String>> getAssets() {
        return assets == null ? Collections.emptyMap() : assets;
    }

    public Map<String, Map<String, String>> getLabels() {
        return labels == null ? Collections.emptyMap() : labels;
    }

    public Map<String, List<String>> getCatalogs() {
        return catalogs == null ? Collections.emptyMap() : catalogs;
    }
}
