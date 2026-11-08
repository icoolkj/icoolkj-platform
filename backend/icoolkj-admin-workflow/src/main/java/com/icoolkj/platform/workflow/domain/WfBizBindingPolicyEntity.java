package com.icoolkj.platform.workflow.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 业务类型流程绑定策略（平台全局）— maps to {@code wf_biz_binding_policy}。
 */
@TableName("wf_biz_binding_policy")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WfBizBindingPolicyEntity extends BaseEntity {

    @TableField("biz_type")
    private String bizType;

    @TableField("binding_mode")
    private String bindingMode;

    @TableField("allow_tenant_approver_override")
    private String allowTenantApproverOverride;

    private String remark;
}
