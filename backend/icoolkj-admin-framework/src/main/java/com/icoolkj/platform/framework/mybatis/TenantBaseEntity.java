package com.icoolkj.platform.framework.mybatis;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;

/**
 * Tenant-scoped entity base for tables with {@code tenant_id} isolation.
 */
@Getter
@Setter
public class TenantBaseEntity extends BaseEntity {

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
}
