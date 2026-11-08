package com.icoolkj.platform.system.datascope;

import com.icoolkj.platform.framework.datascope.OwnerDeptLookup;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class OwnerDeptLookupImpl implements OwnerDeptLookup {

    private final UserMapper userMapper;

    public OwnerDeptLookupImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public String findDeptIdByUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            return null;
        }
        UserEntity user = userMapper.selectById(userId.trim());
        return user == null ? null : user.getDeptId();
    }
}
