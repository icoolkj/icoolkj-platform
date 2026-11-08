package com.icoolkj.platform.oa.service.impl;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.tenant.TenantQueryScope;
import com.icoolkj.platform.oa.convert.OaTenantTemplateConvert;
import com.icoolkj.platform.oa.domain.OaTenantTemplateEntity;
import com.icoolkj.platform.oa.domain.OaTenantTemplateVersionEntity;
import com.icoolkj.platform.oa.dto.OaFieldDefBatchSaveRequest;
import com.icoolkj.platform.oa.dto.OaTenantEnableRequest;
import com.icoolkj.platform.oa.dto.OaTenantTemplatePageQuery;
import com.icoolkj.platform.oa.dto.OaTenantTemplateUpdateRequest;
import com.icoolkj.platform.oa.dto.OaVersionCreateRequest;
import com.icoolkj.platform.oa.dto.OaVersionPublishRequest;
import com.icoolkj.platform.oa.mapper.OaTenantTemplateMapper;
import com.icoolkj.platform.oa.mapper.OaTenantTemplateVersionMapper;
import com.icoolkj.platform.oa.service.OaTenantTemplateService;
import com.icoolkj.platform.oa.support.OaTenantTemplateAccessSupport;
import com.icoolkj.platform.oa.support.OaTenantTemplateEnableSupport;
import com.icoolkj.platform.oa.support.OaTenantTemplateVersionSupport;
import com.icoolkj.platform.oa.support.OaTenantSupport;
import com.icoolkj.platform.oa.vo.OaPlatformTemplateOptionVO;
import com.icoolkj.platform.oa.vo.OaTenantSyncPreviewVO;
import com.icoolkj.platform.oa.vo.OaTenantPublishReadinessVO;
import com.icoolkj.platform.oa.vo.OaTenantTemplateDetailVO;
import com.icoolkj.platform.oa.vo.OaTenantTemplateVO;
import com.icoolkj.platform.oa.vo.OaTemplateVersionVO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/** OA 租户模板服务实现（头表 CRUD 对齐 Velocity 基线，启用/版本/字段见扩展 Support） */
@Service
public class OaTenantTemplateServiceImpl implements OaTenantTemplateService {

    private final OaTenantTemplateMapper tenantTemplateMapper;
    private final OaTenantTemplateVersionMapper tenantVersionMapper;
    private final OaTenantTemplateAccessSupport accessSupport;
    private final OaTenantTemplateEnableSupport enableSupport;
    private final OaTenantTemplateVersionSupport versionSupport;

    public OaTenantTemplateServiceImpl(OaTenantTemplateMapper tenantTemplateMapper,
                                       OaTenantTemplateVersionMapper tenantVersionMapper,
                                       OaTenantTemplateAccessSupport accessSupport,
                                       OaTenantTemplateEnableSupport enableSupport,
                                       OaTenantTemplateVersionSupport versionSupport) {
        this.tenantTemplateMapper = tenantTemplateMapper;
        this.tenantVersionMapper = tenantVersionMapper;
        this.accessSupport = accessSupport;
        this.enableSupport = enableSupport;
        this.versionSupport = versionSupport;
    }

    @Override
    public PageResult<OaTenantTemplateVO> page(OaTenantTemplatePageQuery query) {
        OaTenantSupport.assertTenantOperator();
        TenantQueryScope.applyPageQuery(query);
        long total = tenantTemplateMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<OaTenantTemplateEntity> list = tenantTemplateMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        List<OaTenantTemplateVO> vos = new ArrayList<>(list.size());
        for (OaTenantTemplateEntity entity : list) {
            OaTenantTemplateVO vo = OaTenantTemplateConvert.toVo(entity);
            OaTenantTemplateVersionEntity draft = tenantVersionMapper.selectDraftByTemplateId(entity.getId());
            OaTenantTemplateConvert.fillDraftInfo(vo, draft);
            versionSupport.enrichPlatformSyncInfo(vo, entity);
            vos.add(vo);
        }
        return PageResult.of(vos, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public List<OaPlatformTemplateOptionVO> listAvailablePlatformTemplates() {
        return enableSupport.listAvailablePlatformTemplates();
    }

    @Override
    public OaTenantTemplateDetailVO getById(String id) {
        return versionSupport.buildDetail(accessSupport.requireById(id));
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
    public String enable(OaTenantEnableRequest request) {
        return enableSupport.enable(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(OaTenantTemplateUpdateRequest request) {
        OaTenantTemplateEntity entity = accessSupport.requireById(request.getId());
        OaTenantTemplateConvert.copy(request, entity);
        AuditFieldHelper.fillUpdate(entity);
        tenantTemplateMapper.updateById(entity);
    }

    @Override
    public String createDraftVersion(OaVersionCreateRequest request) {
        return versionSupport.createDraftVersion(request);
    }

    @Override
    public OaTenantSyncPreviewVO previewSyncFromPlatform(String templateId) {
        return versionSupport.previewSyncFromPlatform(templateId);
    }

    @Override
    public String syncFromPlatform(String templateId) {
        return versionSupport.syncFromPlatform(templateId);
    }

    @Override
    public void saveFieldDefs(OaFieldDefBatchSaveRequest request) {
        versionSupport.saveFieldDefs(request);
    }

    @Override
    public OaTenantPublishReadinessVO getPublishReadiness(String versionId) {
        return versionSupport.getPublishReadiness(versionId);
    }

    @Override
    public void publishVersion(OaVersionPublishRequest request) {
        versionSupport.publishVersion(request);
    }
}
