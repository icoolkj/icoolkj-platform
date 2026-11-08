package com.icoolkj.platform.infra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InfraFileAccessPolicyUpsertRequest {

    /** 有值则更新，空则按 pattern 新建 */
    @Size(max = 20)
    private String id;

    /** 精确 biz_type 或前缀（如 brand_*）；* 为默认兜底。对齐槽位 profile_code */
    @NotBlank
    @Size(max = 64)
    private String bizTypePattern;

    /** SESSION | ANON | SESSION_OR_ANON */
    @NotBlank
    @Size(max = 32)
    private String authMode;

    /** OWN | OWN_OR_PLATFORM */
    @NotBlank
    @Size(max = 32)
    private String tenantScope;

    /** ALWAYS | CUSTOM */
    @NotBlank
    @Size(max = 32)
    private String visibility;

    /** 允许匿名的 purpose，逗号分隔 */
    @Size(max = 128)
    private String anonPurposes;

    /** 会话侧附加权限（任一即可） */
    @Size(max = 256)
    private String permAnyOf;

    private Integer sortNo;

    /** 1启用 0停用 */
    @Size(max = 1)
    private String enabled;

    @Size(max = 500)
    private String remark;
}
