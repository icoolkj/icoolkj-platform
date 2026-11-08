package com.icoolkj.platform.workflow.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 业务流程实例 — maps to `wf_biz_process`. */
@TableName("wf_biz_process")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WfBizProcessEntity extends TenantBaseEntity {

    @TableField("model_id")
    private String modelId;

    /** 发起时锁定的模型版本号 */
    @TableField("model_version_no")
    private Integer modelVersionNo;

    @TableField("biz_type")
    private String bizType;

    @TableField("biz_id")
    private String bizId;

    @TableField("biz_title")
    private String bizTitle;

    @TableField("process_definition_key")
    private String processDefinitionKey;

    @TableField("process_instance_id")
    private String processInstanceId;

    @TableField("parent_instance_id")
    private String parentInstanceId;

    private String status;

    @TableField("starter_user_id")
    private String starterUserId;

    @TableField("user_id")
    private String userId;

    @TableField("dept_id")
    private String deptId;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    private String remark;
}
