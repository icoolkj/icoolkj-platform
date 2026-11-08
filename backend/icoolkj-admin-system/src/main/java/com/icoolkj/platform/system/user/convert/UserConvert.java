package com.icoolkj.platform.system.user.convert;

import com.icoolkj.platform.framework.tenant.TenantFieldHelper;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.user.dto.UserCreateRequest;
import com.icoolkj.platform.system.user.dto.UserUpdateRequest;
import com.icoolkj.platform.system.user.vo.UserVO;

/** 用户表对象转换 */
public final class UserConvert {

    private UserConvert() {
    }

    public static UserVO toVo(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(entity.getId());
        vo.setDeptId(entity.getDeptId());
        vo.setDeptName(entity.getDeptName());
        vo.setUsername(entity.getUsername());
        vo.setNickname(entity.getNickname());
        vo.setRealName(entity.getRealName());
        vo.setUserType(entity.getUserType());
        vo.setEmail(entity.getEmail());
        vo.setPhone(entity.getPhone());
        vo.setGender(entity.getGender());
        vo.setAvatarUrl(entity.getAvatarUrl());
        vo.setAvatarFileId(entity.getAvatarFileId());
        vo.setAvatarUrlSm(entity.getAvatarUrlSm());
        vo.setStatus(entity.getStatus());
        vo.setIsSuperAdmin(entity.getIsSuperAdmin());
        vo.setLoginFailCount(entity.getLoginFailCount());
        vo.setLockedUntil(entity.getLockedUntil());
        vo.setLastLoginIp(entity.getLastLoginIp());
        vo.setLastLoginAt(entity.getLastLoginAt());
        vo.setPwdUpdatedAt(entity.getPwdUpdatedAt());
        vo.setMustChangePassword(entity.getMustChangePassword());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    /** 仅映射可维护字段；特权与审计字段由 {@link com.icoolkj.platform.system.user.service.impl.UserServiceImpl} 写入。 */
    public static UserEntity toEntity(UserCreateRequest request) {
        UserEntity entity = new UserEntity();
        entity.setTenantId(TenantFieldHelper.resolveTenantId(null));
        entity.setDeptId(request.getDeptId());
        entity.setUsername(trimToNull(request.getUsername()));
        entity.setNickname(trimToNull(request.getNickname()));
        entity.setRealName(trimToNull(request.getRealName()));
        entity.setEmail(trimToNull(request.getEmail()));
        entity.setPhone(trimToNull(request.getPhone()));
        entity.setGender(request.getGender());
        entity.setAvatarUrl(trimToNull(request.getAvatarUrl()));
        entity.setStatus(request.getStatus());
        entity.setRemark(trimToNull(request.getRemark()));
        return entity;
    }

    /** 不覆盖 userType、isSuperAdmin、passwordHash 及登录审计字段。 */
    public static void copyEditable(UserUpdateRequest request, UserEntity entity) {
        entity.setDeptId(request.getDeptId());
        entity.setUsername(trimToNull(request.getUsername()));
        entity.setNickname(trimToNull(request.getNickname()));
        entity.setRealName(trimToNull(request.getRealName()));
        entity.setEmail(trimToNull(request.getEmail()));
        entity.setPhone(trimToNull(request.getPhone()));
        entity.setGender(request.getGender());
        entity.setAvatarUrl(trimToNull(request.getAvatarUrl()));
        entity.setAvatarFileId(trimToNull(request.getAvatarFileId()));
        entity.setStatus(request.getStatus());
        entity.setRemark(trimToNull(request.getRemark()));
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
