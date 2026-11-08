package com.icoolkj.platform.system.menu.service;

import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.system.menu.vo.MenuRouterVO;

import java.util.List;
import java.util.Set;

/** 当前用户菜单/路由查询 */
public interface MenuQueryService {

    List<MenuRouterVO> getRouters(LoginUser loginUser, String locale);

    /** 按产品宇宙过滤侧栏树；productCode 为空时返回全量（带 productCode） */
    List<MenuRouterVO> getRouters(LoginUser loginUser, String locale, String productCode);

    /** 当前用户已授权菜单 ID（含超管/租户超管规则） */
    Set<String> loadGrantedMenuIds(LoginUser loginUser);
}
