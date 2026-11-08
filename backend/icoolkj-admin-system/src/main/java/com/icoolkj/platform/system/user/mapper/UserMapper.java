package com.icoolkj.platform.system.user.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.tenant.dto.TenantAdminPageQuery;
import com.icoolkj.platform.system.user.dto.UserPageQuery;
import com.icoolkj.platform.system.tenant.vo.TenantAdminVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    UserEntity selectById(@Param("id") String id);

    List<UserEntity> selectList(UserEntity query);

    List<UserEntity> selectPage(UserPageQuery query);

    long countByQuery(UserPageQuery query);

    int insert(UserEntity entity);

    int updateById(UserEntity entity);

    int deleteById(@Param("id") String id);

    UserEntity selectByTenantAndUsername(@Param("tenantId") String tenantId, @Param("username") String username);
    int resetLoginSuccess(@Param("id") String id, @Param("lastLoginIp") String lastLoginIp, @Param("lastLoginAt") java.time.LocalDateTime lastLoginAt);
    int updateLoginFailCount(@Param("id") String id, @Param("loginFailCount") int loginFailCount);
    int updateLockState(@Param("id") String id, @Param("loginFailCount") int loginFailCount, @Param("lockedUntil") java.time.LocalDateTime lockedUntil);

    long countByDeptId(@Param("deptId") String deptId);

    List<String> selectActiveUserIdsByTenant(@Param("tenantId") String tenantId);

    List<String> selectActiveUserIdsByDeptIds(@Param("tenantId") String tenantId,
                                              @Param("deptIds") List<String> deptIds);

    List<String> selectActiveUserIdsByRoleIds(@Param("tenantId") String tenantId,
                                              @Param("roleIds") List<String> roleIds);

    /** 按角色编码查用户，跨租户解析审批人时需忽略租户插件 */
    @InterceptorIgnore(tenantLine = "true")
    List<String> selectActiveUserIdsByRoleCodes(@Param("tenantId") String tenantId,
                                                @Param("roleCodes") List<String> roleCodes);

    List<String> selectActiveUserIdsByRoleCodesAndDeptId(@Param("tenantId") String tenantId,
                                                         @Param("deptId") String deptId,
                                                         @Param("roleCodes") List<String> roleCodes);

    /** 跨租户解析流程参与人展示名 / 站内信通知 */
    @InterceptorIgnore(tenantLine = "true")
    List<UserEntity> selectBriefByIds(@Param("ids") List<String> ids);

    /** 平台：业务租户超级管理员账号分页（跨租户） */
    @InterceptorIgnore(tenantLine = "true")
    List<TenantAdminVO> selectTenantAdminPage(TenantAdminPageQuery query);

    @InterceptorIgnore(tenantLine = "true")
    long countTenantAdmin(TenantAdminPageQuery query);
}
