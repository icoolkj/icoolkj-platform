package com.icoolkj.platform.system.ai.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@TableName("tool_ai_model")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AiModelEntity extends TenantBaseEntity {

    @TableField("provider_id")
    private String providerId;

    @TableField("model_code")
    private String modelCode;

    @TableField("model_name")
    private String modelName;

    private String capabilities;

    @TableField("default_params_json")
    private String defaultParamsJson;

    private String status;

    @TableField("sort_no")
    private Integer sortNo;

    private String remark;
}
