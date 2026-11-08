package com.icoolkj.platform.workflow.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 流程模型版本 — maps to `wf_model_version`. */
@TableName("wf_model_version")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WfModelVersionEntity extends TenantBaseEntity {

    @TableField("model_id")
    private String modelId;

    @TableField("version_no")
    private Integer versionNo;

    @TableField("bpmn_xml")
    private String bpmnXml;

    /** 设计快照（与 process_config.design 同构），随版本不可变 */
    @TableField("design_json")
    private String designJson;

    /** 发布时全局配置快照（startAcl/behavior/notify），运行态优先读此列 */
    @TableField("process_config_json")
    private String processConfigJson;

    @TableField("deployment_id")
    private String deploymentId;

    @TableField("process_definition_id")
    private String processDefinitionId;

    @TableField("process_definition_key")
    private String processDefinitionKey;

    private Integer published;

    private String remark;
}
