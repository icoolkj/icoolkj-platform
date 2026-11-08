package com.icoolkj.platform.system.ai.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@TableName("tool_ai_provider")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AiProviderEntity extends TenantBaseEntity {

    @TableField("provider_code")
    private String providerCode;

    @TableField("provider_name")
    private String providerName;

    private String protocol;

    @TableField("base_url")
    private String baseUrl;

    @TableField("api_key_cipher")
    private String apiKeyCipher;

    @TableField("timeout_seconds")
    private Integer timeoutSeconds;

    private String status;

    private String remark;
}
