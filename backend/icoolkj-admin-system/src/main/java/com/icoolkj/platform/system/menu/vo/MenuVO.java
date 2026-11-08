package com.icoolkj.platform.system.menu.vo;

import lombok.Data;

import java.util.Map;

/** 菜单权限资源表列表/详情 */
@Data
public class MenuVO {
    private String id;
    private String parentId;
    private String menuType;
    private String menuName;
    /** 多语言名称 locale -> label（来自 sys_menu.name_i18n） */
    private Map<String, String> nameI18n;
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
    /** PLATFORM | TENANT */
    private String assignScope;
    /** 产品宇宙编码 */
    private String productCode;
}
