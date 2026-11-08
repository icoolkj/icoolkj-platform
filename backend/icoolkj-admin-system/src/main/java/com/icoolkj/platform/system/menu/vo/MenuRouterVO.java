package com.icoolkj.platform.system.menu.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 前端路由/侧边栏菜单 */
@Data
public class MenuRouterVO {

    private String id;
    private String menuName;
    private String routeName;
    private String path;
    private String component;
    private String icon;
    private String permissionCode;
    private String menuType;
    /** 0=侧栏显示 1=侧栏隐藏（仍注册路由，如审批表单页） */
    private String visible;
    private Integer sortNo;
    /** 产品宇宙编码 */
    private String productCode;
    private List<MenuRouterVO> children = new ArrayList<>();

    public void setChildren(List<MenuRouterVO> children) {
        this.children = children == null ? new ArrayList<>() : children;
    }
}

