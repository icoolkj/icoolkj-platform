package com.icoolkj.platform.workflow.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 流程模型 — maps to `wf_model`. */
@TableName("wf_model")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WfModelEntity extends TenantBaseEntity {

    @TableField("model_key")
    private String modelKey;

    @TableField("model_name")
    private String modelName;

    @TableField("category_name")
    private String categoryName;

    @TableField("biz_type")
    private String bizType;

    @TableField("form_type")
    private String formType;

    @TableField("form_route")
    private String formRoute;

    @TableField("latest_version")
    private Integer latestVersion;

    private String status;

    private String remark;

    @TableField("process_config")
    private String processConfig;

    /** 租户从平台同步时的来源模型 id */
    @TableField("source_model_id")
    private String sourceModelId;
}
