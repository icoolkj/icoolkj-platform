package com.icoolkj.platform.system.menu.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 菜单权限资源表 — maps to `sys_menu`. */
@TableName("sys_menu")
@Getter
@Setter
public class MenuEntity extends TenantBaseEntity {

    @TableField("parent_id")
    private String parentId;

    @TableField("menu_type")
    private String menuType;

    @TableField("menu_name")
    private String menuName;

    /** JSON map locale -> label, e.g. {"zh-CN":"用户管理","en-US":"Users"} */
    @TableField("name_i18n")
    private String nameI18n;

    @TableField("route_name")
    private String routeName;

    @TableField("route_path")
    private String routePath;

    @TableField("component_path")
    private String componentPath;

    @TableField("permission_code")
    private String permissionCode;

    private String icon;

    @TableField("sort_no")
    private Integer sortNo;

    private String visible;

    private String status;

    @TableField("is_external")
    private String isExternal;

    @TableField("is_cache")
    private String isCache;

    private String remark;

    /** PLATFORM=仅平台；TENANT=租户可授权 */
    @TableField("assign_scope")
    private String assignScope;

    /** Console Shell 产品宇宙编码（platform/oa/files/ai/业务码） */
    @TableField("product_code")
    private String productCode;

}
