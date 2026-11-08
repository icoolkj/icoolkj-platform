package com.icoolkj.platform.oa.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.oa.convert.OaPlatformTemplateConvert;
import com.icoolkj.platform.oa.domain.OaPlatformTemplateEntity;
import com.icoolkj.platform.oa.domain.OaPlatformTemplateVersionEntity;
import com.icoolkj.platform.oa.dto.OaFieldDefBatchSaveRequest;
import com.icoolkj.platform.oa.dto.OaPlatformTemplateCreateRequest;
import com.icoolkj.platform.oa.dto.OaPlatformTemplatePageQuery;
import com.icoolkj.platform.oa.dto.OaPlatformTemplateUpdateRequest;
import com.icoolkj.platform.oa.dto.OaVersionCreateRequest;
import com.icoolkj.platform.oa.dto.OaVersionPublishRequest;
import com.icoolkj.platform.oa.mapper.OaPlatformTemplateMapper;
import com.icoolkj.platform.oa.mapper.OaPlatformTemplateVersionMapper;
import com.icoolkj.platform.oa.service.OaPlatformTemplateService;
import com.icoolkj.platform.oa.support.OaPlatformTemplateAccessSupport;
import com.icoolkj.platform.oa.support.OaPlatformTemplateVersionSupport;
import com.icoolkj.platform.oa.support.OaTenantSupport;
import com.icoolkj.platform.oa.vo.OaPlatformTemplateDetailVO;
import com.icoolkj.platform.oa.vo.OaPlatformTemplateVO;
import com.icoolkj.platform.oa.vo.OaTemplateVersionVO;
import com.icoolkj.platform.workflow.support.WorkflowModelAdminSupport;
import com.icoolkj.platform.workflow.vo.WorkflowModelOptionVO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/** OA 平台模板服务实现（头表 CRUD 对齐 Velocity 基线，版本/字段见 VersionSupport） */
@Service
public class OaPlatformTemplateServiceImpl implements OaPlatformTemplateService {

    private final OaPlatformTemplateMapper templateMapper;
    private final OaPlatformTemplateVersionMapper templateVersionMapper;
    private final WorkflowModelAdminSupport workflowModelAdminSupport;
    private final OaPlatformTemplateAccessSupport accessSupport;
    private final OaPlatformTemplateVersionSupport versionSupport;

    public OaPlatformTemplateServiceImpl(OaPlatformTemplateMapper templateMapper,
                                         OaPlatformTemplateVersionMapper templateVersionMapper,
                                         WorkflowModelAdminSupport workflowModelAdminSupport,
                                         OaPlatformTemplateAccessSupport accessSupport,
                                         OaPlatformTemplateVersionSupport versionSupport) {
        this.templateMapper = templateMapper;
        this.templateVersionMapper = templateVersionMapper;
        this.workflowModelAdminSupport = workflowModelAdminSupport;
        this.accessSupport = accessSupport;
        this.versionSupport = versionSupport;
    }

    @Override
    public PageResult<OaPlatformTemplateVO> page(OaPlatformTemplatePageQuery query) {
        OaTenantSupport.assertPlatformOperator();
        query.setScopeTenantId(OaTenantSupport.platformTenantId());
        long total = templateMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<OaPlatformTemplateEntity> list = templateMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        List<OaPlatformTemplateVO> vos = new ArrayList<>(list.size());
        for (OaPlatformTemplateEntity entity : list) {
            OaPlatformTemplateVO vo = OaPlatformTemplateConvert.toVo(entity);
            OaPlatformTemplateVersionEntity draft = templateVersionMapper.selectDraftByTemplateId(entity.getId());
            OaPlatformTemplateConvert.fillDraftInfo(vo, draft);
            vos.add(vo);
        }
        return PageResult.of(vos, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public OaPlatformTemplateDetailVO getById(String id) {
        return versionSupport.buildDetail(accessSupport.requireById(id));
    }

    @Override
    public List<WorkflowModelOptionVO> listModelOptions() {
        OaTenantSupport.assertPlatformOperator();
        return workflowModelAdminSupport.listOptions(OaTenantSupport.platformTenantId(), false);
    }

    @Override
    public List<OaTemplateVersionVO> listVersions(String templateId) {
        return versionSupport.listVersions(templateId);
    }

    @Override
    public OaTemplateVersionVO getVersionDetail(String versionId) {
        return versionSupport.getVersionDetail(versionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(OaPlatformTemplateCreateRequest request) {
        OaTenantSupport.assertPlatformOperator();
        String tenantId = OaTenantSupport.platformTenantId();
        if (templateMapper.selectByCategoryCode(tenantId, request.getCategoryCode().trim()) != null) {
            throw new BusinessException("error.oa.category_duplicate");
        }
        if (templateMapper.selectByTemplateCode(tenantId, request.getTemplateCode().trim()) != null) {
            throw new BusinessException("error.oa.template_code_duplicate");
        }
        OaPlatformTemplateEntity entity = OaPlatformTemplateConvert.toEntity(request, tenantId);
        entity.setId(IdGeneratorUtils.nextIdStr());
        AuditFieldHelper.fillInsert(entity);
        templateMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(OaPlatformTemplateUpdateRequest request) {
        OaPlatformTemplateEntity entity = accessSupport.requireById(request.getId());
        OaPlatformTemplateConvert.copy(request, entity);
        AuditFieldHelper.fillUpdate(entity);
        templateMapper.updateById(entity);
    }

    @Override
    public String createDraftVersion(OaVersionCreateRequest request) {
        return versionSupport.createDraftVersion(request);
    }

    @Override
    public void saveFieldDefs(OaFieldDefBatchSaveRequest request) {
        versionSupport.saveFieldDefs(request);
    }

    @Override
    public void publishVersion(OaVersionPublishRequest request) {
        versionSupport.publishVersion(request);
    }
}
