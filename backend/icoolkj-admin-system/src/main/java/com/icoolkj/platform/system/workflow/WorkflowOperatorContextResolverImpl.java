package com.icoolkj.platform.system.workflow;

import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.user.domain.UserPostEntity;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.system.user.mapper.UserPostMapper;
import com.icoolkj.platform.system.user.mapper.UserRoleMapper;
import com.icoolkj.platform.workflow.spi.WorkflowOperatorContextResolver;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 从用户主数据补齐发起人部门、角色编码、岗位 ID */
@Component
public class WorkflowOperatorContextResolverImpl implements WorkflowOperatorContextResolver {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserPostMapper userPostMapper;

    public WorkflowOperatorContextResolverImpl(UserMapper userMapper,
                                               UserRoleMapper userRoleMapper,
                                               UserPostMapper userPostMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.userPostMapper = userPostMapper;
    }

    @Override
    public OperatorContext resolve(String userId) {
        if (!StringUtils.hasText(userId)) {
            return new OperatorContext(userId, null, Collections.emptyList(), Collections.emptyList());
        }
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            return new OperatorContext(userId, null, Collections.emptyList(), Collections.emptyList());
        }
        List<String> roleCodes = Collections.emptyList();
        if (StringUtils.hasText(user.getTenantId())) {
            List<String> codes = userRoleMapper.selectRoleCodesByTenantAndUser(user.getTenantId(), userId);
            if (codes != null) {
                roleCodes = codes;
            }
        }
        List<String> postIds = resolvePostIds(user.getTenantId(), userId);
        return new OperatorContext(userId, user.getDeptId(), roleCodes, postIds);
    }

    private List<String> resolvePostIds(String tenantId, String userId) {
        UserPostEntity query = new UserPostEntity();
        query.setTenantId(tenantId);
        query.setUserId(userId);
        List<UserPostEntity> rows = userPostMapper.selectList(query);
        if (CollectionUtils.isEmpty(rows)) {
            return Collections.emptyList();
        }
        return rows.stream()
                .map(UserPostEntity::getPostId)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }
}
