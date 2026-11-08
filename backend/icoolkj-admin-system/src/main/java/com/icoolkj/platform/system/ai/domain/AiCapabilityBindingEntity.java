package com.icoolkj.platform.system.ai.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@TableName("tool_ai_capability_binding")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AiCapabilityBindingEntity extends TenantBaseEntity {

    private String capability;

    @TableField("model_id")
    private String modelId;

    private Integer enabled;

    private String remark;
}
