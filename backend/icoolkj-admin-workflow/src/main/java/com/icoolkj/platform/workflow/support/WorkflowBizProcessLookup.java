package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.workflow.domain.WfBizProcessEntity;
import com.icoolkj.platform.workflow.mapper.WfBizProcessMapper;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 跨租户加载 wf_biz_process（业务租户存于行内，访问权由 {@link WorkflowAccessGuard} 或 {@link WorkflowCrossTenantSupport} 校验） */
@Component
public class WorkflowBizProcessLookup {

    private final WfBizProcessMapper wfBizProcessMapper;

    public WorkflowBizProcessLookup(WfBizProcessMapper wfBizProcessMapper) {
        this.wfBizProcessMapper = wfBizProcessMapper;
    }

    public WfBizProcessEntity findByProcessInstanceId(String processInstanceId) {
        if (!StringUtils.hasText(processInstanceId)) {
            return null;
        }
        return TenantScopeRunner.runIgnoringTenant(() ->
                wfBizProcessMapper.selectByProcessInstanceId(processInstanceId.trim()));
    }

    public WfBizProcessEntity requireByProcessInstanceId(String processInstanceId) {
        WfBizProcessEntity entity = findByProcessInstanceId(processInstanceId);
        if (entity == null) {
            throw new BusinessException("error.workflow.instance_not_found");
        }
        return entity;
    }

    /** Latest process start for a business document (oldest→newest list, take last). */
    public WfBizProcessEntity findLatestByBiz(String bizType, String bizId) {
        if (!StringUtils.hasText(bizType) || !StringUtils.hasText(bizId)) {
            return null;
        }
        List<WfBizProcessEntity> list = TenantScopeRunner.runIgnoringTenant(() ->
                wfBizProcessMapper.selectListByBiz(bizType.trim(), bizId.trim()));
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(list.size() - 1);
    }
}
