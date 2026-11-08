package com.icoolkj.platform.system.workflow;

import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.workflow.spi.WorkflowUserDisplayNameResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 按用户 ID 解析展示名：优先昵称，回落用户名 */
@Component
public class WorkflowUserDisplayNameResolverImpl implements WorkflowUserDisplayNameResolver {

    private final UserMapper userMapper;

    public WorkflowUserDisplayNameResolverImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Map<String, String> resolveDisplayNames(Collection<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Map.of();
        }
        List<String> ids = userIds.stream().filter(StringUtils::hasText).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<String, String> result = new HashMap<>();
        TenantScopeRunner.runIgnoringTenant(() -> userMapper.selectBriefByIds(ids)).forEach(user -> {
            String display = StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
            if (StringUtils.hasText(display)) {
                result.put(user.getId(), display);
            }
        });
        return result;
    }
}
