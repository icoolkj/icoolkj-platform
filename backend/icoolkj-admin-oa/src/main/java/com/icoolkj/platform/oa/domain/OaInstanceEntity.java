package com.icoolkj.platform.oa.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/** OA 业务实例主表 */
@TableName("oa_instance")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaInstanceEntity extends TenantBaseEntity {

    @TableField("category_code")
    private String categoryCode;

    @TableField("tenant_template_id")
    private String tenantTemplateId;

    @TableField("tenant_tpl_ver_id")
    private String tenantTplVerId;

    @TableField("platform_tpl_ver_id")
    private String platformTplVerId;

    @TableField("instance_title")
    private String instanceTitle;

    @TableField("approve_status")
    private String approveStatus;

    @TableField("process_instance_id")
    private String processInstanceId;

    @TableField("process_definition_key")
    private String processDefinitionKey;

    @TableField("starter_user_id")
    private String starterUserId;

    @TableField("user_id")
    private String userId;

    @TableField("dept_id")
    private String deptId;

    @TableField("submitted_at")
    private LocalDateTime submittedAt;

    @TableField("finished_at")
    private LocalDateTime finishedAt;

    @TableField("idx_str_01")
    private String idxStr01;
    @TableField("idx_str_02")
    private String idxStr02;
    @TableField("idx_str_03")
    private String idxStr03;
    @TableField("idx_str_04")
    private String idxStr04;
    @TableField("idx_str_05")
    private String idxStr05;
    @TableField("idx_str_06")
    private String idxStr06;
    @TableField("idx_str_07")
    private String idxStr07;
    @TableField("idx_str_08")
    private String idxStr08;
    @TableField("idx_str_09")
    private String idxStr09;
    @TableField("idx_str_10")
    private String idxStr10;

    private String status;
    private String remark;
}
