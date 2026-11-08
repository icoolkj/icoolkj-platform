package com.icoolkj.platform.system.menu.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.system.menu.dto.MenuCreateRequest;
import com.icoolkj.platform.system.menu.dto.MenuPageQuery;
import com.icoolkj.platform.system.menu.dto.MenuUpdateRequest;
import com.icoolkj.platform.system.menu.vo.MenuVO;

/** 菜单权限资源表服务 */
public interface MenuService {

    PageResult<MenuVO> page(MenuPageQuery query);

    MenuVO getById(String id);

    String create(MenuCreateRequest request);

    void update(MenuUpdateRequest request);

    void deleteById(String id);
}
