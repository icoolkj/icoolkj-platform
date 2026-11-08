package com.icoolkj.platform.system.menu.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 菜单权限资源表分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MenuPageQuery extends PageQuery {
    private String parentId;
    private String menuType;
    private String menuName;
    private String routeName;
    private String routePath;
    private String componentPath;
    private String permissionCode;
    private String icon;
    private Integer sortNo;
    private String visible;
    private String status;
    private String isExternal;
    private String isCache;
    private String remark;
    /** 为 true 时仅返回租户可授权菜单（assign_scope=TENANT） */
    private Boolean assignableOnly;
    /** 按授权范围过滤 */
    private String assignScope;
    private String productCode;
}
