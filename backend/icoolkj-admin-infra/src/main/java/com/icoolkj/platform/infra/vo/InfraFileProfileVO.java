package com.icoolkj.platform.infra.vo;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/** 文件上传槽位（含解析后的有效规则）。 */
@Getter
@Setter
public class InfraFileProfileVO {

    private String profileCode;
    private String profileName;
    private Map<String, String> profileNameI18n;
    private String categoryTag;
    private String status;
    private String remark;

    /** 有效规则来源租户 */
    private String ruleTenantId;
    private boolean tenantOverride;

    private List<String> allowedExts;
    private List<String> allowedContentTypes;
    private boolean required;
    private Integer minNum;
    private Integer maxNum;
    private Long minSizeBytes;
    private Long maxSizeBytes;
    private Integer sortNo;
}
