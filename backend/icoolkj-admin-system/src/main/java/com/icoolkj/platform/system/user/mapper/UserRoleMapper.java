package com.icoolkj.platform.system.user.mapper;

import com.icoolkj.platform.system.user.domain.UserRoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRoleMapper {

    List<UserRoleEntity> selectList(UserRoleEntity query);

    int insert(UserRoleEntity entity);

    int delete(UserRoleEntity entity);

    List<String> selectRoleIdsByTenantAndUser(@Param("tenantId") String tenantId, @Param("userId") String userId);

    List<String> selectRoleCodesByTenantAndUser(@Param("tenantId") String tenantId, @Param("userId") String userId);

    boolean existsSuperRoleByTenantAndUser(@Param("tenantId") String tenantId, @Param("userId") String userId);

}
