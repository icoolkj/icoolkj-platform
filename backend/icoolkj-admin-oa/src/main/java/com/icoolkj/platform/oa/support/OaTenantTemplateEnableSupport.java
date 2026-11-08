package com.icoolkj.platform.oa.support;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.oa.constant.OaEnableStatus;
import com.icoolkj.platform.oa.constant.OaVersionStatus;
import com.icoolkj.platform.oa.convert.OaConvert;
import com.icoolkj.platform.oa.domain.OaPlatformFieldDefEntity;
import com.icoolkj.platform.oa.domain.OaPlatformTemplateEntity;
import com.icoolkj.platform.oa.domain.OaPlatformTemplateVersionEntity;
import com.icoolkj.platform.oa.domain.OaTenantFieldDefEntity;
import com.icoolkj.platform.oa.domain.OaTenantTemplateEntity;
import com.icoolkj.platform.oa.domain.OaTenantTemplateVersionEntity;
import com.icoolkj.platform.oa.dto.OaTenantEnableRequest;
import com.icoolkj.platform.oa.mapper.OaPlatformFieldDefMapper;
import com.icoolkj.platform.oa.mapper.OaPlatformTemplateMapper;
import com.icoolkj.platform.oa.mapper.OaPlatformTemplateVersionMapper;
import com.icoolkj.platform.oa.mapper.OaTenantFieldDefMapper;
import com.icoolkj.platform.oa.mapper.OaTenantTemplateMapper;
import com.icoolkj.platform.oa.mapper.OaTenantTemplateVersionMapper;
import com.icoolkj.platform.oa.vo.OaPlatformTemplateOptionVO;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** OA 租户模板启用扩展（手写，不随 Velocity 覆盖） */
@Component
public class OaTenantTemplateEnableSupport {

    private final OaTenantTemplateMapper tenantTemplateMapper;
    private final OaTenantTemplateVersionMapper tenantVersionMapper;
    private final OaTenantFieldDefMapper tenantFieldDefMapper;
    private final OaPlatformTemplateMapper platformTemplateMapper;
    private final OaPlatformTemplateVersionMapper platformVersionMapper;
    private final OaPlatformFieldDefMapper platformFieldDefMapper;

    public OaTenantTemplateEnableSupport(OaTenantTemplateMapper tenantTemplateMapper,
                                         OaTenantTemplateVersionMapper tenantVersionMapper,
                                         OaTenantFieldDefMapper tenantFieldDefMapper,
                                         OaPlatformTemplateMapper platformTemplateMapper,
                                         OaPlatformTemplateVersionMapper platformVersionMapper,
                                         OaPlatformFieldDefMapper platformFieldDefMapper) {
        this.tenantTemplateMapper = tenantTemplateMapper;
        this.tenantVersionMapper = tenantVersionMapper;
        this.tenantFieldDefMapper = tenantFieldDefMapper;
        this.platformTemplateMapper = platformTemplateMapper;
        this.platformVersionMapper = platformVersionMapper;
        this.platformFieldDefMapper = platformFieldDefMapper;
    }

    public List<OaPlatformTemplateOptionVO> listAvailablePlatformTemplates() {
        OaTenantSupport.assertTenantOperator();
        return TenantScopeRunner.runIgnoringTenant(
                () -> platformTemplateMapper.selectPublishedOptions(OaTenantSupport.platformTenantId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public String enable(OaTenantEnableRequest request) {
        OaTenantSupport.assertTenantOperator();
        String tenantId = OaTenantSupport.tenantOwnerId();
        OaPlatformTemplateEntity platform = TenantScopeRunner.runIgnoringTenant(
                () -> platformTemplateMapper.selectById(request.getPlatformTemplateId()));
        if (platform == null || !OaTenantSupport.platformTenantId().equals(platform.getTenantId())) {
            throw new BusinessException("error.oa.template_not_found");
        }
        if (platform.getLatestVersionNo() == null || platform.getLatestVersionNo() <= 0) {
            throw new BusinessException("error.oa.platform_not_published");
        }
        if (tenantTemplateMapper.selectByCategoryCode(tenantId, platform.getCategoryCode()) != null) {
            throw new BusinessException("error.oa.tenant_category_exists");
        }
        OaPlatformTemplateVersionEntity platformPublished = TenantScopeRunner.runIgnoringTenant(
                () -> platformVersionMapper.selectPublishedByTemplateId(platform.getId()));
        if (platformPublished == null) {
            throw new BusinessException("error.oa.platform_not_published");
        }

        OaTenantTemplateEntity tenantTemplate = new OaTenantTemplateEntity();
        tenantTemplate.setId(IdGeneratorUtils.nextIdStr());
        tenantTemplate.setTenantId(tenantId);
        tenantTemplate.setCategoryCode(platform.getCategoryCode());
        tenantTemplate.setTemplateCode(platform.getTemplateCode());
        tenantTemplate.setTemplateName(StringUtils.hasText(request.getTemplateName())
                ? request.getTemplateName().trim() : platform.getTemplateName());
        tenantTemplate.setPlatformTemplateId(platform.getId());
        tenantTemplate.setSourcePlatformTplVerId(platformPublished.getId());
        tenantTemplate.setLatestVersionNo(0);
        tenantTemplate.setEnableStatus(OaEnableStatus.DISABLED);
        tenantTemplate.setStatus(CommonConstants.STATUS_NORMAL);
        tenantTemplate.setRemark(request.getRemark());
        AuditFieldHelper.fillInsert(tenantTemplate);
        tenantTemplateMapper.insert(tenantTemplate);

        OaTenantTemplateVersionEntity draft = newTenantVersion(
                tenantTemplate, 1, platformPublished, "首次启用拷贝");
        tenantVersionMapper.insert(draft);
        copyPlatformFieldsToTenant(platformPublished.getId(), draft.getId(), tenantId);
        return tenantTemplate.getId();
    }

    private OaTenantTemplateVersionEntity newTenantVersion(
            OaTenantTemplateEntity template, int versionNo,
            OaPlatformTemplateVersionEntity platformPublished, String remark) {
        OaTenantTemplateVersionEntity version = new OaTenantTemplateVersionEntity();
        version.setId(IdGeneratorUtils.nextIdStr());
        version.setTenantId(template.getTenantId());
        version.setTenantTemplateId(template.getId());
        version.setVersionNo(versionNo);
        version.setVersionStatus(OaVersionStatus.DRAFT);
        version.setPlatformTplVerId(platformPublished.getId());
        version.setWfModelId(platformPublished.getWfModelId());
        version.setRemark(remark);
        AuditFieldHelper.fillInsert(version);
        return version;
    }

    private void copyPlatformFieldsToTenant(String platformVersionId, String tenantVersionId, String tenantId) {
        List<OaPlatformFieldDefEntity> fields = TenantScopeRunner.runIgnoringTenant(
                () -> platformFieldDefMapper.selectByVersionId(platformVersionId));
        if (CollectionUtils.isEmpty(fields)) {
            return;
        }
        List<OaTenantFieldDefEntity> copies = new ArrayList<>();
        for (OaPlatformFieldDefEntity field : fields) {
            OaTenantFieldDefEntity copy = OaConvert.copyFromPlatform(field, tenantId, tenantVersionId);
            copy.setId(IdGeneratorUtils.nextIdStr());
            AuditFieldHelper.fillInsert(copy);
            copies.add(copy);
        }
        tenantFieldDefMapper.insertBatch(copies);
    }
}
