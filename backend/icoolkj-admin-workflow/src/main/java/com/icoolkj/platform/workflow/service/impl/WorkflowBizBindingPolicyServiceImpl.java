package com.icoolkj.platform.workflow.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.workflow.constant.WfBindingMode;
import com.icoolkj.platform.workflow.convert.WorkflowBizBindingPolicyConvert;
import com.icoolkj.platform.workflow.domain.WfBizBindingPolicyEntity;
import com.icoolkj.platform.workflow.dto.WorkflowBizBindingPolicyCreateRequest;
import com.icoolkj.platform.workflow.dto.WorkflowBizBindingPolicyPageQuery;
import com.icoolkj.platform.workflow.dto.WorkflowBizBindingPolicyUpdateRequest;
import com.icoolkj.platform.workflow.mapper.WfBizBindingPolicyMapper;
import com.icoolkj.platform.workflow.service.WorkflowBizBindingPolicyService;
import com.icoolkj.platform.workflow.support.WorkflowBizBindingPolicyAccessSupport;
import com.icoolkj.platform.workflow.vo.WorkflowBizBindingPolicyVO;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 业务绑定策略管理 */
@Service
public class WorkflowBizBindingPolicyServiceImpl implements WorkflowBizBindingPolicyService {

    private final WfBizBindingPolicyMapper bindingPolicyMapper;
    private final WorkflowBizBindingPolicyAccessSupport accessSupport;

    public WorkflowBizBindingPolicyServiceImpl(WfBizBindingPolicyMapper bindingPolicyMapper,
                                               WorkflowBizBindingPolicyAccessSupport accessSupport) {
        this.bindingPolicyMapper = bindingPolicyMapper;
        this.accessSupport = accessSupport;
    }

    @Override
    public PageResult<WorkflowBizBindingPolicyVO> page(WorkflowBizBindingPolicyPageQuery query) {
        accessSupport.assertPlatformOperator();
        long total = bindingPolicyMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<WorkflowBizBindingPolicyVO> rows = bindingPolicyMapper.selectPage(query);
        return PageResult.of(rows, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public WorkflowBizBindingPolicyVO getById(String id) {
        accessSupport.assertPlatformOperator();
        return WorkflowBizBindingPolicyConvert.toVo(requirePolicy(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(WorkflowBizBindingPolicyCreateRequest request) {
        accessSupport.assertPlatformOperator();
        validateBindingMode(request.getBindingMode());
        assertUniqueBizType(request.getBizType(), null);

        WfBizBindingPolicyEntity entity = WorkflowBizBindingPolicyConvert.toEntity(request);
        entity.setId(IdGeneratorUtils.nextIdStr());
        AuditFieldHelper.fillInsert(entity);
        bindingPolicyMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WorkflowBizBindingPolicyUpdateRequest request) {
        accessSupport.assertPlatformOperator();
        validateBindingMode(request.getBindingMode());
        WfBizBindingPolicyEntity entity = requirePolicy(request.getId());
        WorkflowBizBindingPolicyConvert.copy(request, entity);
        AuditFieldHelper.fillUpdate(entity);
        bindingPolicyMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        accessSupport.assertPlatformOperator();
        requirePolicy(id);
        if (bindingPolicyMapper.deleteById(id) <= 0) {
            throw new BusinessException("error.workflow.biz_binding_policy_not_found");
        }
    }

    private WfBizBindingPolicyEntity requirePolicy(String id) {
        WfBizBindingPolicyEntity entity = bindingPolicyMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.workflow.biz_binding_policy_not_found");
        }
        return entity;
    }

    private void assertUniqueBizType(String bizType, String excludeId) {
        if (!StringUtils.hasText(bizType)) {
            return;
        }
        WfBizBindingPolicyEntity existing = bindingPolicyMapper.selectByBizType(bizType.trim());
        if (existing != null && (excludeId == null || !excludeId.equals(existing.getId()))) {
            throw new BusinessException("error.workflow.biz_binding_policy_duplicate");
        }
    }

    private void validateBindingMode(String bindingMode) {
        if (!StringUtils.hasText(bindingMode)) {
            throw new BusinessException("error.workflow.biz_binding_policy_invalid_mode");
        }
        String mode = bindingMode.trim();
        if (!WfBindingMode.PLATFORM_MANDATORY.equals(mode)
                && !WfBindingMode.TENANT_PREFERRED.equals(mode)
                && !WfBindingMode.TENANT_REQUIRED.equals(mode)) {
            throw new BusinessException("error.workflow.biz_binding_policy_invalid_mode");
        }
    }
}
