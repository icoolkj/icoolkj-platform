package com.icoolkj.platform.system.role.mapper;

import com.icoolkj.platform.system.role.domain.RoleMenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface RoleMenuMapper {

    List<RoleMenuEntity> selectList(RoleMenuEntity query);

    int insert(RoleMenuEntity entity);

    int delete(RoleMenuEntity entity);

    List<String> selectMenuIdsByTenantAndRoleIds(@Param("tenantId") String tenantId, @Param("roleIds") List<String> roleIds);

    int deleteByMenuIdForNonPlatformTenants(@Param("menuId") String menuId);

    int deleteByMenuIdsForNonPlatformTenants(@Param("menuIds") Collection<String> menuIds);

    int deleteByMenuIds(@Param("menuIds") Collection<String> menuIds);

}
