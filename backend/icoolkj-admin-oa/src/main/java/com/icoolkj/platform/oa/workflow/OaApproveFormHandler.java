package com.icoolkj.platform.oa.workflow;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.oa.domain.OaInstanceDataEntity;
import com.icoolkj.platform.oa.domain.OaInstanceEntity;
import com.icoolkj.platform.oa.domain.OaInstancePromoteEntity;
import com.icoolkj.platform.oa.mapper.OaInstanceDataMapper;
import com.icoolkj.platform.oa.mapper.OaInstanceMapper;
import com.icoolkj.platform.oa.mapper.OaInstancePromoteMapper;
import com.icoolkj.platform.oa.support.OaFormContentTextSupport;
import com.icoolkj.platform.oa.support.OaInstanceFormSupport;
import com.icoolkj.platform.oa.vo.OaFieldDefVO;
import com.icoolkj.platform.workflow.spi.WorkflowApproveFormHandler;
import com.icoolkj.platform.workflow.support.WorkflowCrossTenantSupport;
import com.icoolkj.platform.infra.dto.InfraFileRefBinding;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.oa.constant.OaInstanceFileConstants;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** OA 审批写回：仅合并节点 fieldPerms=W 的字段（调用方已过滤） */
@Component
public class OaApproveFormHandler implements WorkflowApproveFormHandler {

    private final OaInstanceMapper instanceMapper;
    private final OaInstanceDataMapper instanceDataMapper;
    private final OaInstancePromoteMapper promoteMapper;
    private final OaInstanceFormSupport formSupport;
    private final InfraFileService infraFileService;
    private final WorkflowCrossTenantSupport crossTenant;

    public OaApproveFormHandler(OaInstanceMapper instanceMapper,
                                OaInstanceDataMapper instanceDataMapper,
                                OaInstancePromoteMapper promoteMapper,
                                OaInstanceFormSupport formSupport,
                                InfraFileService infraFileService,
                                WorkflowCrossTenantSupport crossTenant) {
        this.instanceMapper = instanceMapper;
        this.instanceDataMapper = instanceDataMapper;
        this.promoteMapper = promoteMapper;
        this.formSupport = formSupport;
        this.infraFileService = infraFileService;
        this.crossTenant = crossTenant;
    }

    @Override
    public boolean supports(String bizType) {
        return StringUtils.hasText(bizType) && bizType.startsWith("oa_");
    }

    @Override
    public void applyWritablePatch(String bizId,
                                   String processInstanceId,
                                   String taskDefinitionKey,
                                   Map<String, Object> formData) {
        if (formData == null || formData.isEmpty()) {
            return;
        }
        crossTenant.runIgnoringBizTenant(() -> {
            OaInstanceEntity entity = instanceMapper.selectById(bizId);
            if (entity == null) {
                throw new BusinessException("error.oa.instance_not_found");
            }
            OaInstanceDataEntity data = instanceDataMapper.selectByInstanceId(bizId);
            if (data == null) {
                throw new BusinessException("error.oa.instance_not_found");
            }
            List<OaFieldDefVO> fields = formSupport.resolveFields(
                    data.getFieldSnapshotJson(), entity.getTenantTplVerId());
            Map<String, Object> current = OaFormContentTextSupport.mergeInto(
                    fields,
                    data.getContentText(),
                    formSupport.readFormJson(data.getFormData()));
            if (current == null) {
                current = new LinkedHashMap<>();
            }
            current.putAll(formData);
            formSupport.validateForm(fields, current);
            var storage = OaFormContentTextSupport.split(fields, current);
            data.setFormData(formSupport.writeFormJson(storage.formData()));
            data.setContentText(storage.contentText());
            AuditFieldHelper.fillUpdate(data);
            instanceDataMapper.updateById(data);

            promoteMapper.deleteByInstanceId(entity.getId());
            List<OaInstancePromoteEntity> promotes = formSupport.buildPromotes(entity, fields, current);
            if (!CollectionUtils.isEmpty(promotes)) {
                promoteMapper.insertBatch(promotes);
            }
            if (StringUtils.hasText(entity.getTenantId()) && StringUtils.hasText(entity.getId())) {
                infraFileService.syncBizFileRefs(
                        entity.getTenantId(),
                        OaInstanceFileConstants.BIZ_TYPE,
                        entity.getId(),
                        formSupport.collectFileBindings(fields, current));
            }
            AuditFieldHelper.fillUpdate(entity);
            instanceMapper.updateById(entity);
        });
    }
}
