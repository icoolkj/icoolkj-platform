package com.icoolkj.platform.oa.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.workflow.constant.WorkflowBizApproveStatus;
import com.icoolkj.platform.oa.constant.OaEnableStatus;
import com.icoolkj.platform.oa.constant.OaMyInstanceScope;
import com.icoolkj.platform.oa.domain.OaInstanceEntity;
import com.icoolkj.platform.oa.domain.OaTenantTemplateEntity;
import com.icoolkj.platform.oa.domain.OaTenantTemplateVersionEntity;
import com.icoolkj.platform.oa.dto.OaMyInstancePageQuery;
import com.icoolkj.platform.oa.mapper.OaInstanceMapper;
import com.icoolkj.platform.oa.mapper.OaTenantTemplateMapper;
import com.icoolkj.platform.oa.mapper.OaTenantTemplateVersionMapper;
import com.icoolkj.platform.oa.service.OaInitiateService;
import com.icoolkj.platform.oa.support.OaTenantSupport;
import com.icoolkj.platform.oa.vo.OaInitiateCatalogItemVO;
import com.icoolkj.platform.oa.vo.OaInstanceListVO;
import com.icoolkj.platform.workflow.domain.WfModelEntity;
import com.icoolkj.platform.workflow.service.WorkflowModelRegistry;
import com.icoolkj.platform.workflow.spi.WorkflowOperatorContextResolver;
import com.icoolkj.platform.workflow.support.WorkflowBizProcessLookup;
import com.icoolkj.platform.workflow.support.WorkflowProcessConfigSupport;
import com.icoolkj.platform.workflow.support.WorkflowVersionConfigLookup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class OaInitiateServiceImpl implements OaInitiateService {

    private final OaTenantTemplateMapper tenantTemplateMapper;
    private final OaTenantTemplateVersionMapper tenantTemplateVersionMapper;
    private final OaInstanceMapper instanceMapper;
    private final WorkflowBizProcessLookup workflowBizProcessLookup;
    private final WorkflowModelRegistry workflowModelRegistry;
    private final WorkflowVersionConfigLookup versionConfigLookup;
    private final WorkflowProcessConfigSupport processConfigSupport;
    private final ObjectProvider<WorkflowOperatorContextResolver> operatorContextResolver;

    public OaInitiateServiceImpl(OaTenantTemplateMapper tenantTemplateMapper,
                                 OaTenantTemplateVersionMapper tenantTemplateVersionMapper,
                                 OaInstanceMapper instanceMapper,
                                 WorkflowBizProcessLookup workflowBizProcessLookup,
                                 WorkflowModelRegistry workflowModelRegistry,
                                 WorkflowVersionConfigLookup versionConfigLookup,
                                 WorkflowProcessConfigSupport processConfigSupport,
                                 ObjectProvider<WorkflowOperatorContextResolver> operatorContextResolver) {
        this.tenantTemplateMapper = tenantTemplateMapper;
        this.tenantTemplateVersionMapper = tenantTemplateVersionMapper;
        this.instanceMapper = instanceMapper;
        this.workflowBizProcessLookup = workflowBizProcessLookup;
        this.workflowModelRegistry = workflowModelRegistry;
        this.versionConfigLookup = versionConfigLookup;
        this.processConfigSupport = processConfigSupport;
        this.operatorContextResolver = operatorContextResolver;
    }

    @Override
    public List<OaInitiateCatalogItemVO> listCatalog() {
        OaTenantSupport.assertTenantOperator();
        String tenantId = OaTenantSupport.tenantOwnerId();
        String userId = SecurityContextHelper.getUserId();
        List<OaTenantTemplateEntity> rows =
                tenantTemplateMapper.selectLaunchable(tenantId, OaEnableStatus.ENABLED);
        if (CollectionUtils.isEmpty(rows)) {
            return List.of();
        }
        List<OaInitiateCatalogItemVO> result = new ArrayList<>(rows.size());
        for (OaTenantTemplateEntity row : rows) {
            if (row == null || !StringUtils.hasText(row.getPublishedTplVerId())) {
                continue;
            }
            if (!isStartAllowedForCategory(tenantId, row.getCategoryCode(), userId)) {
                continue;
            }
            OaInitiateCatalogItemVO item = new OaInitiateCatalogItemVO();
            item.setTenantTemplateId(row.getId());
            item.setCategoryCode(row.getCategoryCode());
            item.setTemplateCode(row.getTemplateCode());
            item.setTemplateName(row.getTemplateName());
            item.setPublishedTplVerId(row.getPublishedTplVerId());
            item.setRemark(row.getRemark());
            OaTenantTemplateVersionEntity ver =
                    tenantTemplateVersionMapper.selectById(row.getPublishedTplVerId());
            if (ver != null) {
                item.setPublishedVersionNo(ver.getVersionNo());
            }
            result.add(item);
        }
        return result;
    }

    @Override
    public PageResult<OaInstanceListVO> pageMyInstances(OaMyInstancePageQuery query) {
        OaTenantSupport.assertTenantOperator();
        if (query == null) {
            query = new OaMyInstancePageQuery();
        }
        String scope = query.getScope() == null ? "" : query.getScope().trim();
        if (!OaMyInstanceScope.DRAFT.equals(scope)) {
            throw new BusinessException("error.forbidden");
        }
        String userId = SecurityContextHelper.getUserId();
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("error.forbidden");
        }
        query.setStarterUserId(userId.trim());
        query.setScope(scope);
        long total = instanceMapper.countMyByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<OaInstanceEntity> list = instanceMapper.selectMyPage(query);
        List<OaInstanceListVO> vos = new ArrayList<>(list.size());
        for (OaInstanceEntity entity : list) {
            vos.add(toListVo(entity));
        }
        return PageResult.of(vos, total, query.getPageNo(), query.getPageSize());
    }

    private boolean isStartAllowedForCategory(String tenantId, String categoryCode, String userId) {
        if (!StringUtils.hasText(categoryCode) || !StringUtils.hasText(userId)) {
            return false;
        }
        WfModelEntity model;
        try {
            model = workflowModelRegistry.requireActiveByBizType(tenantId, categoryCode);
        } catch (BusinessException ex) {
            return false;
        }
        var config = versionConfigLookup.resolvePublishedByModelId(model.getId());
        String deptId = null;
        List<String> roleCodes = Collections.emptyList();
        List<String> postIds = Collections.emptyList();
        WorkflowOperatorContextResolver resolver = operatorContextResolver.getIfAvailable();
        if (resolver != null) {
            WorkflowOperatorContextResolver.OperatorContext ctx = resolver.resolve(userId);
            if (ctx != null) {
                deptId = ctx.deptId();
                roleCodes = ctx.roleCodes() == null ? Collections.emptyList() : ctx.roleCodes();
                postIds = ctx.postIds() == null ? Collections.emptyList() : ctx.postIds();
            }
        }
        return processConfigSupport.isStartAllowed(config, userId, deptId, roleCodes, postIds);
    }

    private OaInstanceListVO toListVo(OaInstanceEntity entity) {
        OaInstanceListVO vo = new OaInstanceListVO();
        vo.setId(entity.getId());
        vo.setCategoryCode(entity.getCategoryCode());
        vo.setInstanceTitle(entity.getInstanceTitle());
        vo.setApproveStatus(entity.getApproveStatus());
        vo.setProcessInstanceId(entity.getProcessInstanceId());
        vo.setStarterUserId(entity.getStarterUserId());
        vo.setSubmittedAt(entity.getSubmittedAt());
        vo.setFinishedAt(entity.getFinishedAt());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        vo.setRemark(entity.getRemark());
        vo.setHasApprovalHistory(hasApprovalHistory(entity));
        vo.setAllowWithdraw(resolveAllowWithdraw(entity));
        return vo;
    }

    private Boolean resolveAllowWithdraw(OaInstanceEntity entity) {
        if (entity == null || !WorkflowBizApproveStatus.RUNNING.equals(entity.getApproveStatus())) {
            return false;
        }
        if (!StringUtils.hasText(entity.getProcessInstanceId())) {
            return true;
        }
        var behavior = versionConfigLookup
                .resolveByProcessInstanceId(entity.getProcessInstanceId(), null)
                .behavior();
        return behavior == null || !Boolean.FALSE.equals(behavior.getAllowWithdraw());
    }

    private boolean hasApprovalHistory(OaInstanceEntity entity) {
        if (entity == null) {
            return false;
        }
        if (StringUtils.hasText(entity.getProcessInstanceId())) {
            return true;
        }
        return workflowBizProcessLookup.findLatestByBiz(entity.getCategoryCode(), entity.getId()) != null;
    }
}
