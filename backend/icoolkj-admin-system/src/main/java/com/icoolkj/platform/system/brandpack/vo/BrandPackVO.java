package com.icoolkj.platform.system.brandpack.vo;

import java.util.Collections;
import java.util.Map;
import lombok.Data;

/** OEM / 站点品牌包（bootstrap 公开字段，敏感信息不放此处） */
@Data
public class BrandPackVO {

    private String productName;
    /** locale → 产品名；优先于 productName */
    private Map<String, String> productNameI18n;
    private String copyrightBrand;
    private String icpNo;
    private String icpUrl;
    private String domain;
    private String siteUrl;
    private String email;
    private String contactName;
    private String contactPhone;

    /** 品牌素材：profile_code → fileId */
    private Map<String, String> assets;
    /** 素材说明：profile_code → label */
    private Map<String, String> labels;

    /**
     * 运行时用途 → fileId（由启用目录槽位 chrome_roles + assets 解析，停用槽位不参与）。
     * 如 sidebar_logo / login_logo / favicon / qr_wechat。
     */
    private Map<String, String> chrome;

    /** 运行时用途 → 说明文案（如 qr_wechat caption） */
    private Map<String, String> chromeLabels;

    public Map<String, String> getAssets() {
        return assets == null ? Collections.emptyMap() : assets;
    }

    public Map<String, String> getLabels() {
        return labels == null ? Collections.emptyMap() : labels;
    }

    public Map<String, String> getChrome() {
        return chrome == null ? Collections.emptyMap() : chrome;
    }

    public Map<String, String> getChromeLabels() {
        return chromeLabels == null ? Collections.emptyMap() : chromeLabels;
    }
}
