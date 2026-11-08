package com.icoolkj.platform.system.menu.mapper;

import com.icoolkj.platform.system.menu.domain.MenuEntity;
import com.icoolkj.platform.system.menu.dto.MenuPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface MenuMapper {

    MenuEntity selectById(@Param("id") String id);

    List<MenuEntity> selectList(MenuEntity query);

    List<MenuEntity> selectPage(MenuPageQuery query);

    long countByQuery(MenuPageQuery query);

    int insert(MenuEntity entity);

    int updateById(MenuEntity entity);

    int deleteById(@Param("id") String id);

    List<String> selectPermissionCodesByMenuIds(@Param("menuIds") List<String> menuIds);
    List<String> selectAllActivePermissionCodes();

    List<String> selectActivePermissionCodesForPlatformSuperAdmin();

    List<String> selectActivePermissionCodesExcludingMenuIds(@Param("excludeMenuIds") List<String> excludeMenuIds);

    List<MenuEntity> selectRouterMenus();

    List<MenuEntity> selectRouterMenusByProductCode(@Param("productCode") String productCode);

    List<String> selectDistinctProductCodesByMenuIds(@Param("menuIds") Collection<String> menuIds);

    List<String> selectAllActiveMenuIds();

    /** 平台超管：排除与同父级 PLATFORM 菜单同 permission_code 的 TENANT 副本（如工作流流程模型/审批人规则） */
    List<String> selectActiveMenuIdsForPlatformSuperAdmin();

    List<String> selectActiveMenuIdsExcluding(@Param("excludeMenuIds") List<String> excludeMenuIds);

    String selectAssignScopeById(@Param("id") String id);

    long countByMenuIdsAndAssignScope(@Param("menuIds") Collection<String> menuIds, @Param("assignScope") String assignScope);

    List<String> selectActiveMenuIdsByAssignScope(@Param("assignScope") String assignScope);

    List<String> selectActivePermissionCodesByAssignScope(@Param("assignScope") String assignScope);

    /** parent_id=0 且 menu_type=M 的产品根数量（oa/files/ai 单根校验） */
    long countRootMenusByProduct(@Param("productCode") String productCode,
                                 @Param("excludeId") String excludeId);

    long countByProductCode(@Param("productCode") String productCode);

}
