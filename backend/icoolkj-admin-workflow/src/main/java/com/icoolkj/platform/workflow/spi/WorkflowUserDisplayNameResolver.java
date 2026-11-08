package com.icoolkj.platform.workflow.spi;

import java.util.Collection;
import java.util.Map;

/** 解析流程参与人展示名（昵称优先） */
public interface WorkflowUserDisplayNameResolver {

    Map<String, String> resolveDisplayNames(Collection<String> userIds);
}
