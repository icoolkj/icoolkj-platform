package com.icoolkj.platform.system.role.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 角色表分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RolePageQuery extends PageQuery {
    private String roleName;
    private String roleCode;
    private String dataScope;
    private Integer sortNo;
    private String status;
    private String isSuperRole;
    private String remark;
}
