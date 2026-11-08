package com.icoolkj.platform.system.iprule.domain;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

/** IP访问规则 — maps to `sys_ip_access_rule`. */
@TableName("sys_ip_access_rule")
@Getter
@Setter
public class IpAccessRuleEntity extends TenantBaseEntity {

    @TableField("rule_name")
    private String ruleName;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("match_type")
    private String matchType;

    @TableField("rule_type")
    private String ruleType;

    @TableField("apply_scope")
    private String applyScope;

    @TableField("priority_no")
    private Integer priorityNo;

    private String status;

    @TableField("start_at")
    private LocalDateTime startAt;

    @TableField("end_at")
    private LocalDateTime endAt;

    private String remark;

}
