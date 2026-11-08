package com.icoolkj.platform.infra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InfraFileProfileUpsertRequest {

    /** 平台运营代维时可指定目标租户；业务租户忽略并锁定自身 */
    private String targetTenantId;

    @NotBlank
    @Size(max = 64)
    private String profileCode;

    @Size(max = 80)
    private String profileName;

    /** locale → 名称；落库为 profile_name_i18n JSON */
    private Map<String, String> profileNameI18n;

    @Size(max = 64)
    private String categoryTag;

    /** 0正常 1停用 */
    private String status;

    private String remark;

    /** 规则字段：仅租户/平台维护规则时填写 */
    private String allowedExts;
    private String allowedContentTypes;
    private String required;
    private Integer minNum;
    private Integer maxNum;
    private Long minSizeBytes;
    private Long maxSizeBytes;
    private Integer sortNo;
}
