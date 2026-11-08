package com.icoolkj.platform.oa.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** OA 实例提升槽位 */
@TableName("oa_instance_promote")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaInstancePromoteEntity extends TenantBaseEntity {

    @TableField("instance_id")
    private String instanceId;

    @TableField("slot_code")
    private String slotCode;

    @TableField("str_value")
    private String strValue;

    @TableField("field_code")
    private String fieldCode;
}
