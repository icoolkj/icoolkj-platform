package com.icoolkj.platform.workflow.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 任务动作审计 — maps to `wf_task_action`. */
@TableName("wf_task_action")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WfTaskActionEntity extends TenantBaseEntity {

    @TableField("process_instance_id")
    private String processInstanceId;

    @TableField("task_id")
    private String taskId;

    @TableField("task_definition_key")
    private String taskDefinitionKey;

    @TableField("action_type")
    private String actionType;

    @TableField("from_user_id")
    private String fromUserId;

    @TableField("to_user_id")
    private String toUserId;

    private String comment;

    @TableField("payload_json")
    private String payloadJson;
}
