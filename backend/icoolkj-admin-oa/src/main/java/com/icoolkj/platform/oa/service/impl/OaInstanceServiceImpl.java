package com.icoolkj.platform.oa.service.impl;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.datascope.DataScopeSpec;
import com.icoolkj.platform.framework.datascope.OwnerDeptLookup;
import com.icoolkj.platform.framework.datascope.OwnershipColumns;
import com.icoolkj.platform.framework.datascope.RowAccessSupport;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.infra.dto.InfraFileRefBinding;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.workflow.constant.WorkflowBizApproveStatus;
import com.icoolkj.platform.oa.constant.OaEnableStatus;
import com.icoolkj.platform.oa.constant.OaInstanceFileConstants;
import com.icoolkj.platform.oa.convert.OaInstanceConvert;
import com.icoolkj.platform.oa.domain.OaInstanceDataEntity;
import com.icoolkj.platform.oa.domain.OaInstanceEntity;
import com.icoolkj.platform.oa.domain.OaInstancePromoteEntity;
import com.icoolkj.platform.oa.domain.OaTenantTemplateEntity;
import com.icoolkj.platform.oa.domain.OaTenantTemplateVersionEntity;
import com.icoolkj.platform.oa.dto.OaInstanceFormSaveRequest;
import com.icoolkj.platform.oa.mapper.OaInstanceDataMapper;
import com.icoolkj.platform.oa.mapper.OaInstanceMapper;
import com.icoolkj.platform.oa.mapper.OaInstancePromoteMapper;
import com.icoolkj.platform.oa.mapper.OaTenantTemplateMapper;
import com.icoolkj.platform.oa.mapper.OaTenantTemplateVersionMapper;
import com.icoolkj.platform.oa.service.OaInstanceService;
import com.icoolkj.platform.oa.support.OaFormContentTextSupport;
import com.icoolkj.platform.oa.support.OaInstanceFormSupport;
import com.icoolkj.platform.oa.support.OaTenantSupport;
import com.icoolkj.platform.oa.vo.OaFieldDefVO;
import com.icoolkj.platform.oa.vo.OaInstanceApproveDetailVO;
import com.icoolkj.platform.oa.vo.OaInstanceFormSchemaVO;
import com.icoolkj.platform.oa.vo.OaInstanceVO;
import com.icoolkj.platform.workflow.constant.WfFieldPerm;
import com.icoolkj.platform.oa.constant.OaWorkflowCancelReasons;
import com.icoolkj.platform.workflow.domain.WfModelEntity;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import com.icoolkj.platform.workflow.dto.WorkflowStartRequest;
import com.icoolkj.platform.workflow.service.WorkflowInstanceService;
import com.icoolkj.platform.workflow.service.WorkflowModelRegistry;
import com.icoolkj.platform.workflow.service.WorkflowProcessService;
import com.icoolkj.platform.workflow.service.WorkflowTaskLookupService;
import com.icoolkj.platform.workflow.support.WorkflowBizProcessLookup;
import com.icoolkj.platform.workflow.support.WorkflowCcTaskSupport;
import com.icoolkj.platform.workflow.support.WorkflowCrossTenantSupport;
import com.icoolkj.platform.workflow.support.WorkflowDesignNodeLookup;
import com.icoolkj.platform.workflow.support.WorkflowNodeFieldPermSupport;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class OaInstanceServiceImpl implements OaInstanceService {

    private final OaInstanceMapper instanceMapper;
    private final OaInstanceDataMapper instanceDataMapper;
    private final OaInstancePromoteMapper promoteMapper;
    private final OaTenantTemplateMapper tenantTemplateMapper;
    private final OaTenantTemplateVersionMapper tenantVersionMapper;
    private final OaInstanceFormSupport formSupport;
    private final WorkflowModelRegistry workflowModelRegistry;
    private final WorkflowProcessService workflowProcessService;
    private final WorkflowInstanceService workflowInstanceService;
    private final WorkflowTaskLookupService workflowTaskLookupService;
    private final WorkflowCrossTenantSupport workflowCrossTenantSupport;
    private final WorkflowBizProcessLookup workflowBizProcessLookup;
    private final WorkflowDesignNodeLookup designNodeLookup;
    private final TaskService taskService;
    private final InfraFileService infraFileService;
    private final OwnerDeptLookup ownerDeptLookup;
    private final RowAccessSupport rowAccessSupport;

    public OaInstanceServiceImpl(OaInstanceMapper instanceMapper,
                                 OaInstanceDataMapper instanceDataMapper,
                                 OaInstancePromoteMapper promoteMapper,
                                 OaTenantTemplateMapper tenantTemplateMapper,
                                 OaTenantTemplateVersionMapper tenantVersionMapper,
                                 OaInstanceFormSupport formSupport,
                                 WorkflowModelRegistry workflowModelRegistry,
                                 WorkflowProcessService workflowProcessService,
                                 WorkflowInstanceService workflowInstanceService,
                                 WorkflowTaskLookupService workflowTaskLookupService,
                                 WorkflowCrossTenantSupport workflowCrossTenantSupport,
                                 WorkflowBizProcessLookup workflowBizProcessLookup,
                                 WorkflowDesignNodeLookup designNodeLookup,
                                 TaskService taskService,
                                 InfraFileService infraFileService,
                                 OwnerDeptLookup ownerDeptLookup,
                                 RowAccessSupport rowAccessSupport) {
        this.instanceMapper = instanceMapper;
        this.instanceDataMapper = instanceDataMapper;
        this.promoteMapper = promoteMapper;
        this.tenantTemplateMapper = tenantTemplateMapper;
        this.tenantVersionMapper = tenantVersionMapper;
        this.formSupport = formSupport;
        this.workflowModelRegistry = workflowModelRegistry;
        this.workflowProcessService = workflowProcessService;
        this.workflowInstanceService = workflowInstanceService;
        this.workflowTaskLookupService = workflowTaskLookupService;
        this.workflowCrossTenantSupport = workflowCrossTenantSupport;
        this.workflowBizProcessLookup = workflowBizProcessLookup;
        this.designNodeLookup = designNodeLookup;
        this.taskService = taskService;
        this.infraFileService = infraFileService;
        this.ownerDeptLookup = ownerDeptLookup;
        this.rowAccessSupport = rowAccessSupport;
    }

    @Override
    public OaInstanceVO getById(String id) {
        OaTenantSupport.assertTenantOperator();
        OaInstanceEntity entity = requireInstance(id);
        assertReadable(entity);
        OaInstanceDataEntity data = instanceDataMapper.selectByInstanceId(id);
        ensureInstanceFileRefs(entity, data);
        return OaInstanceConvert.toVo(entity, data, formSupport);
    }

    @Override
    public OaInstanceFormSchemaVO getCreateFormSchema(String categoryCode) {
        OaTenantSupport.assertTenantOperator();
        String code = requireCategoryCode(categoryCode);
        String tenantId = OaTenantSupport.tenantOwnerId();
        OaTenantTemplateEntity template = requireEnabledTemplate(tenantId, code);
        OaTenantTemplateVersionEntity published = requirePublishedVersion(template);
        List<OaFieldDefVO> fields = formSupport.resolveFields(
                published.getFieldSnapshotJson(), published.getId());
        if (CollectionUtils.isEmpty(fields)) {
            throw new BusinessException("error.oa.platform_fields_empty");
        }
        OaInstanceFormSchemaVO schema = new OaInstanceFormSchemaVO();
        schema.setCategoryCode(code);
        schema.setTemplateName(template.getTemplateName());
        schema.setTenantTemplateId(template.getId());
        schema.setTenantTplVerId(published.getId());
        schema.setVersionNo(published.getVersionNo());
        schema.setFields(fields);
        schema.setAppendRemark(!formSupport.hasRemarkField(fields));
        schema.setLayoutSnapshotJson(resolveLayoutSnapshot(published));
        return schema;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(String categoryCode, OaInstanceFormSaveRequest request) {
        OaTenantSupport.assertTenantOperator();
        String code = requireCategoryCode(categoryCode);
        String tenantId = OaTenantSupport.tenantOwnerId();
        OaTenantTemplateEntity template = requireEnabledTemplate(tenantId, code);
        OaTenantTemplateVersionEntity published = requirePublishedVersion(template);
        List<OaFieldDefVO> fields = formSupport.resolveFields(
                published.getFieldSnapshotJson(), published.getId());
        Map<String, Object> form = formSupport.sanitizeFormData(fields, request.getFormData());
        formSupport.validateForm(fields, form);

        OaInstanceEntity entity = new OaInstanceEntity();
        entity.setId(IdGeneratorUtils.nextIdStr());
        entity.setTenantId(tenantId);
        entity.setCategoryCode(code);
        entity.setTenantTemplateId(template.getId());
        entity.setTenantTplVerId(published.getId());
        entity.setPlatformTplVerId(published.getPlatformTplVerId());
        String processName = firstText(resolveModelDisplayName(tenantId, code), template.getTemplateName());
        entity.setInstanceTitle(formSupport.buildTitle(form, processName, code));
        entity.setApproveStatus(WorkflowBizApproveStatus.DRAFT);
        String starterUserId = SecurityContextHelper.getUserId();
        entity.setStarterUserId(starterUserId);
        OwnershipColumns.applyUserAndDept(entity::setUserId, entity::setDeptId, starterUserId, ownerDeptLookup);
        formSupport.applyListSlots(entity, fields, form);
        entity.setStatus(CommonConstants.STATUS_NORMAL);
        entity.setRemark(request.getRemark());
        AuditFieldHelper.fillInsert(entity);
        instanceMapper.insert(entity);

        // 始终持久化解析后的完整字段快照，避免种子残缺 JSON 被带到实例上
        saveDataAndPromote(
                entity,
                fields,
                form,
                formSupport.writeFieldsSnapshot(fields),
                resolveLayoutSnapshot(published),
                true);
        syncInstanceFileRefs(entity, fields, form);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(OaInstanceFormSaveRequest request) {
        OaTenantSupport.assertTenantOperator();
        if (!StringUtils.hasText(request.getId())) {
            throw new BusinessException("error.oa.instance_not_found");
        }
        OaInstanceEntity entity = requireInstance(request.getId());
        assertStarter(entity);
        assertEditable(entity);
        OaInstanceDataEntity data = instanceDataMapper.selectByInstanceId(entity.getId());
        List<OaFieldDefVO> fields = formSupport.resolveFields(
                data == null ? null : data.getFieldSnapshotJson(), entity.getTenantTplVerId());
        Map<String, Object> form = formSupport.sanitizeFormData(fields, request.getFormData());
        formSupport.validateForm(fields, form);

        String processName = firstText(
                resolveModelDisplayName(entity.getTenantId(), entity.getCategoryCode()),
                resolveTemplateName(entity));
        entity.setInstanceTitle(formSupport.buildTitle(form, processName, entity.getCategoryCode()));
        formSupport.applyListSlots(entity, fields, form);
        entity.setRemark(request.getRemark());
        if (WorkflowBizApproveStatus.REJECTED.equals(entity.getApproveStatus())
                || WorkflowBizApproveStatus.CANCELLED.equals(entity.getApproveStatus())) {
            entity.setApproveStatus(WorkflowBizApproveStatus.DRAFT);
            entity.setProcessInstanceId(null);
            entity.setProcessDefinitionKey(null);
            entity.setSubmittedAt(null);
            entity.setFinishedAt(null);
        }
        // returned（starterEdit）：保留 processInstanceId，状态仍为 returned，待重提
        AuditFieldHelper.fillUpdate(entity);
        instanceMapper.updateById(entity);

        // 解析后的字段可能比实例内旧快照更完整，写回完整快照；布局沿用实例已锁定快照
        String layoutSnap = data == null ? null : data.getLayoutSnapshotJson();
        saveDataAndPromote(
                entity, fields, form, formSupport.writeFieldsSnapshot(fields), layoutSnap, false);
        syncInstanceFileRefs(entity, fields, form);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        OaTenantSupport.assertTenantOperator();
        OaInstanceEntity entity = requireInstance(id);
        assertStarter(entity);
        assertEditable(entity);
        if (hasApprovalHistory(entity)) {
            throw new BusinessException("error.oa.instance_history_locked");
        }
        releaseInstanceFileRefs(entity);
        instanceMapper.deleteById(id);
        instanceDataMapper.deleteByInstanceId(id);
        promoteMapper.deleteByInstanceId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(String id) {
        OaTenantSupport.assertTenantOperator();
        OaInstanceEntity entity = requireInstance(id);
        assertStarter(entity);
        String status = entity.getApproveStatus();
        // 幂等：已关闭直接成功，避免已发列表流程态未刷新时重复点关闭报错
        if (WorkflowBizApproveStatus.CLOSED.equals(status)) {
            return;
        }
        if (WorkflowBizApproveStatus.RUNNING.equals(status)) {
            throw new BusinessException("error.oa.instance_running_locked");
        }
        if (!hasApprovalHistory(entity)) {
            throw new BusinessException("error.oa.instance_status_invalid");
        }
        // starterEdit 挂起实例：关闭时终止引擎，避免孤儿流程
        if (WorkflowBizApproveStatus.RETURNED.equals(status) && StringUtils.hasText(entity.getProcessInstanceId())) {
            try {
                workflowProcessService.cancelProcess(entity.getProcessInstanceId(),
                        OaWorkflowCancelReasons.CLOSE_AFTER_RETURN);
            } catch (BusinessException ignored) {
                // 已结束则忽略
            }
        }
        // Close keeps the row so done/submitted viewers still open approve-detail + timeline.
        entity.setApproveStatus(WorkflowBizApproveStatus.CLOSED);
        if (entity.getFinishedAt() == null) {
            entity.setFinishedAt(LocalDateTime.now());
        }
        AuditFieldHelper.fillUpdate(entity);
        instanceMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitApprove(String id) {
        OaTenantSupport.assertTenantOperator();
        OaInstanceEntity entity = requireInstance(id);
        assertStarter(entity);
        String status = entity.getApproveStatus();
        if (WorkflowBizApproveStatus.RETURNED.equals(status)) {
            return resubmitAfterStarterEdit(entity);
        }
        if (WorkflowBizApproveStatus.CLOSED.equals(status)
                || (!WorkflowBizApproveStatus.DRAFT.equals(status) && !WorkflowBizApproveStatus.REJECTED.equals(status)
                && !WorkflowBizApproveStatus.CANCELLED.equals(status))) {
            throw new BusinessException("error.oa.instance_status_invalid");
        }
        requireEnabledTemplate(entity.getTenantId(), entity.getCategoryCode());

        WfModelEntity model = workflowModelRegistry.requireActiveByBizType(
                entity.getTenantId(), entity.getCategoryCode());

        // 提交时重算标题：发起人 + wf_model.model_name + 字典标签摘要
        OaInstanceDataEntity dataForTitle = instanceDataMapper.selectByInstanceId(entity.getId());
        Map<String, Object> formForTitle = formSupport.readFormJson(
                dataForTitle == null ? null : dataForTitle.getFormData());
        String title = formSupport.buildTitle(formForTitle, model.getModelName(), entity.getCategoryCode());
        entity.setInstanceTitle(title);

        WorkflowStartRequest startRequest = new WorkflowStartRequest();
        startRequest.setModelId(model.getId());
        startRequest.setProcessDefinitionKey(model.getModelKey());
        startRequest.setBizType(entity.getCategoryCode());
        startRequest.setBizId(entity.getId());
        startRequest.setBizTitle(title);
        startRequest.setTenantId(entity.getTenantId());

        String processInstanceId = workflowProcessService.startProcess(startRequest);

        entity.setApproveStatus(WorkflowBizApproveStatus.RUNNING);
        entity.setProcessInstanceId(processInstanceId);
        entity.setProcessDefinitionKey(model.getModelKey());
        entity.setSubmittedAt(LocalDateTime.now());
        entity.setFinishedAt(null);
        AuditFieldHelper.fillUpdate(entity);
        instanceMapper.updateById(entity);
        return processInstanceId;
    }

    private String resubmitAfterStarterEdit(OaInstanceEntity entity) {
        if (!StringUtils.hasText(entity.getProcessInstanceId())) {
            throw new BusinessException("error.workflow.instance_not_found");
        }
        requireEnabledTemplate(entity.getTenantId(), entity.getCategoryCode());
        WfModelEntity model = workflowModelRegistry.requireActiveByBizType(
                entity.getTenantId(), entity.getCategoryCode());
        OaInstanceDataEntity dataForTitle = instanceDataMapper.selectByInstanceId(entity.getId());
        Map<String, Object> formForTitle = formSupport.readFormJson(
                dataForTitle == null ? null : dataForTitle.getFormData());
        String title = formSupport.buildTitle(formForTitle, model.getModelName(), entity.getCategoryCode());
        entity.setInstanceTitle(title);

        workflowProcessService.resubmitFromStarterEdit(entity.getProcessInstanceId());

        entity.setApproveStatus(WorkflowBizApproveStatus.RUNNING);
        entity.setSubmittedAt(LocalDateTime.now());
        entity.setFinishedAt(null);
        AuditFieldHelper.fillUpdate(entity);
        instanceMapper.updateById(entity);
        return entity.getProcessInstanceId();
    }

    @Override
    public OaInstanceApproveDetailVO getApproveDetail(String id) {
        OaInstanceEntity entity = workflowCrossTenantSupport.runIgnoringBizTenant(() -> instanceMapper.selectById(id));
        if (entity == null) {
            throw new BusinessException("error.oa.instance_not_found");
        }
        String processInstanceId = entity.getProcessInstanceId();
        if (!StringUtils.hasText(processInstanceId)) {
            var latest = workflowBizProcessLookup.findLatestByBiz(entity.getCategoryCode(), entity.getId());
            if (latest != null) {
                processInstanceId = latest.getProcessInstanceId();
            }
        }
        workflowCrossTenantSupport.assertCanApproveDetail(processInstanceId, entity.getId());
        OaInstanceDataEntity data = workflowCrossTenantSupport.runIgnoringBizTenant(
                () -> instanceDataMapper.selectByInstanceId(id));
        // 历史单据：正文内嵌图可能未写入 file_ref，审批人下载会被可见性拦截；打开详情时补绑
        workflowCrossTenantSupport.runIgnoringBizTenant(() -> ensureInstanceFileRefs(entity, data));

        OaInstanceVO document = OaInstanceConvert.toVo(entity, data, formSupport);
        OaInstanceApproveDetailVO detail = new OaInstanceApproveDetailVO();
        detail.setDocument(document);
        detail.setApproveStatus(entity.getApproveStatus());
        detail.setProcessInstanceId(processInstanceId);
        detail.setFields(document.getFields());
        detail.setFormData(document.getFormData() == null ? Map.of() : document.getFormData());
        if (StringUtils.hasText(processInstanceId)) {
            detail.setInstance(workflowInstanceService.getByInstanceId(processInstanceId));
            detail.setTaskId(workflowTaskLookupService.findActiveTaskId(
                    processInstanceId, SecurityContextHelper.getUserId()));
        }
        applyNodeFieldPerms(detail);
        return detail;
    }

    private void applyNodeFieldPerms(OaInstanceApproveDetailVO detail) {
        if (detail == null || CollectionUtils.isEmpty(detail.getFields())) {
            return;
        }
        String taskKey = resolveTaskDefinitionKey(detail.getProcessInstanceId(), detail.getTaskId());
        WorkflowModelDesignNodeRequest node = null;
        if (StringUtils.hasText(taskKey) && StringUtils.hasText(detail.getProcessInstanceId())) {
            node = designNodeLookup.findByProcessInstanceAndTaskKey(detail.getProcessInstanceId(), taskKey);
        }
        List<OaFieldDefVO> enriched = new ArrayList<>(detail.getFields().size());
        for (OaFieldDefVO field : detail.getFields()) {
            if (field == null) {
                continue;
            }
            field.setFieldPerm(WorkflowNodeFieldPermSupport.resolve(node, field.getFieldCode()));
            enriched.add(field);
        }
        detail.setFields(enriched);
        // 隐藏字段不出现在 formData 投影（避免前端误展示）
        if (detail.getFormData() != null && !detail.getFormData().isEmpty()) {
            Map<String, Object> visible = new java.util.LinkedHashMap<>();
            for (OaFieldDefVO field : enriched) {
                if (field.getFieldCode() == null || WfFieldPerm.isHidden(field.getFieldPerm())) {
                    continue;
                }
                if (detail.getFormData().containsKey(field.getFieldCode())) {
                    visible.put(field.getFieldCode(), detail.getFormData().get(field.getFieldCode()));
                }
            }
            detail.setFormData(visible);
        }
    }

    private String resolveTaskDefinitionKey(String processInstanceId, String taskId) {
        if (StringUtils.hasText(taskId)) {
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task != null && StringUtils.hasText(task.getTaskDefinitionKey())) {
                return task.getTaskDefinitionKey();
            }
        }
        if (!StringUtils.hasText(processInstanceId)) {
            return null;
        }
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).active().list();
        if (CollectionUtils.isEmpty(tasks)) {
            return null;
        }
        for (Task task : tasks) {
            if (task != null && !WorkflowCcTaskSupport.isCcTask(task)
                    && StringUtils.hasText(task.getTaskDefinitionKey())) {
                return task.getTaskDefinitionKey();
            }
        }
        Task first = tasks.get(0);
        return first == null ? null : first.getTaskDefinitionKey();
    }

    private String resolveLayoutSnapshot(OaTenantTemplateVersionEntity published) {
        if (published == null) {
            return null;
        }
        if (StringUtils.hasText(published.getLayoutSnapshotJson())) {
            return published.getLayoutSnapshotJson();
        }
        return published.getLayoutJson();
    }

    private void saveDataAndPromote(OaInstanceEntity entity, List<OaFieldDefVO> fields, Map<String, Object> form,
                                    String fieldSnapshotJson, String layoutSnapshotJson, boolean insert) {
        // 契约：richText → content_text；其余 → form_data（API 层仍用完整 form）
        var storage = OaFormContentTextSupport.split(fields, form);
        String formJson = formSupport.writeFormJson(storage.formData());
        if (insert) {
            OaInstanceDataEntity data = new OaInstanceDataEntity();
            data.setId(IdGeneratorUtils.nextIdStr());
            data.setTenantId(entity.getTenantId());
            data.setInstanceId(entity.getId());
            data.setFormData(formJson);
            data.setContentText(storage.contentText());
            data.setFieldSnapshotJson(fieldSnapshotJson);
            data.setLayoutSnapshotJson(layoutSnapshotJson);
            AuditFieldHelper.fillInsert(data);
            instanceDataMapper.insert(data);
        } else {
            OaInstanceDataEntity data = instanceDataMapper.selectByInstanceId(entity.getId());
            if (data == null) {
                data = new OaInstanceDataEntity();
                data.setId(IdGeneratorUtils.nextIdStr());
                data.setTenantId(entity.getTenantId());
                data.setInstanceId(entity.getId());
                data.setFormData(formJson);
                data.setContentText(storage.contentText());
                data.setFieldSnapshotJson(fieldSnapshotJson);
                data.setLayoutSnapshotJson(layoutSnapshotJson);
                AuditFieldHelper.fillInsert(data);
                instanceDataMapper.insert(data);
            } else {
                data.setFormData(formJson);
                data.setContentText(storage.contentText());
                if (StringUtils.hasText(fieldSnapshotJson)) {
                    data.setFieldSnapshotJson(fieldSnapshotJson);
                }
                if (layoutSnapshotJson != null) {
                    data.setLayoutSnapshotJson(layoutSnapshotJson);
                }
                AuditFieldHelper.fillUpdate(data);
                instanceDataMapper.updateById(data);
            }
            promoteMapper.deleteByInstanceId(entity.getId());
        }

        List<OaInstancePromoteEntity> promotes = formSupport.buildPromotes(entity, fields, form);
        if (!CollectionUtils.isEmpty(promotes)) {
            promoteMapper.insertBatch(promotes);
        }
    }

    private void syncInstanceFileRefs(OaInstanceEntity entity, List<OaFieldDefVO> fields, Map<String, Object> form) {
        if (entity == null || !StringUtils.hasText(entity.getTenantId()) || !StringUtils.hasText(entity.getId())) {
            return;
        }
        List<InfraFileRefBinding> bindings = formSupport.collectFileBindings(fields, form);
        infraFileService.syncBizFileRefs(
                entity.getTenantId(), OaInstanceFileConstants.BIZ_TYPE, entity.getId(), bindings);
    }

    /**
     * 从已落库 form_data + content_text 补齐附件 / 正文内嵌图引用（幂等）。
     * 用于详情 / 审批页：修复「仅上传人能看图、审批人看不到」的历史数据。
     */
    private void ensureInstanceFileRefs(OaInstanceEntity entity, OaInstanceDataEntity data) {
        if (entity == null || data == null) {
            return;
        }
        List<OaFieldDefVO> fields = formSupport.resolveFields(
                data.getFieldSnapshotJson(), entity.getTenantTplVerId());
        Map<String, Object> form = OaFormContentTextSupport.mergeInto(
                fields, data.getContentText(), formSupport.readFormJson(data.getFormData()));
        syncInstanceFileRefs(entity, fields, form);
    }

    private void releaseInstanceFileRefs(OaInstanceEntity entity) {
        if (entity == null || !StringUtils.hasText(entity.getTenantId()) || !StringUtils.hasText(entity.getId())) {
            return;
        }
        infraFileService.releaseBizFiles(
                entity.getTenantId(), OaInstanceFileConstants.BIZ_TYPE, entity.getId());
    }

    private OaTenantTemplateEntity requireEnabledTemplate(String tenantId, String categoryCode) {
        OaTenantTemplateEntity template = tenantTemplateMapper.selectByCategoryCode(tenantId, categoryCode);
        if (template == null || !OaEnableStatus.ENABLED.equals(template.getEnableStatus())) {
            throw new BusinessException("error.oa.tenant_template_not_enabled");
        }
        return template;
    }

    private OaTenantTemplateVersionEntity requirePublishedVersion(OaTenantTemplateEntity template) {
        if (!StringUtils.hasText(template.getPublishedTplVerId())) {
            throw new BusinessException("error.oa.tenant_template_not_enabled");
        }
        OaTenantTemplateVersionEntity version = tenantVersionMapper.selectById(template.getPublishedTplVerId());
        if (version == null) {
            throw new BusinessException("error.oa.tenant_template_not_enabled");
        }
        return version;
    }

    /** 标题用流程名：优先活跃模型 {@code model_name} */
    private String resolveModelDisplayName(String tenantId, String categoryCode) {
        if (StringUtils.hasText(tenantId) && StringUtils.hasText(categoryCode)) {
            try {
                WfModelEntity model = workflowModelRegistry.requireActiveByBizType(tenantId, categoryCode);
                if (model != null && StringUtils.hasText(model.getModelName())) {
                    return model.getModelName().trim();
                }
            } catch (BusinessException ignored) {
                // 草稿阶段模型未同步时由调用方回落模板名
            }
            String byKey = workflowModelRegistry.resolveModelName(categoryCode.trim());
            if (StringUtils.hasText(byKey) && !categoryCode.trim().equals(byKey)) {
                return byKey.trim();
            }
        }
        return null;
    }

    private String resolveTemplateName(OaInstanceEntity entity) {
        if (!StringUtils.hasText(entity.getTenantTemplateId())) {
            return null;
        }
        OaTenantTemplateEntity template = tenantTemplateMapper.selectById(entity.getTenantTemplateId());
        return template == null ? null : template.getTemplateName();
    }

    private static String firstText(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private OaInstanceEntity requireInstance(String id) {
        OaInstanceEntity entity = instanceMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.oa.instance_not_found");
        }
        return entity;
    }

    private void assertReadable(OaInstanceEntity entity) {
        if (!StringUtils.hasText(entity.getUserId())) {
            throw new BusinessException("error.forbidden");
        }
        rowAccessSupport.assertReadable(
                entity.getTenantId(), DataScopeSpec.userAndDept(), entity.getUserId(), entity.getDeptId());
    }

    private void assertStarter(OaInstanceEntity entity) {
        String userId = SecurityContextHelper.getUserId();
        if (!StringUtils.hasText(userId) || !Objects.equals(userId, entity.getStarterUserId())) {
            throw new BusinessException("error.forbidden");
        }
    }

    private void assertEditable(OaInstanceEntity entity) {
        String status = entity.getApproveStatus();
        if (WorkflowBizApproveStatus.RUNNING.equals(status)) {
            throw new BusinessException("error.oa.instance_running_locked");
        }
        if (WorkflowBizApproveStatus.APPROVED.equals(status)) {
            throw new BusinessException("error.oa.instance_approved_locked");
        }
        if (WorkflowBizApproveStatus.CLOSED.equals(status)) {
            throw new BusinessException("error.oa.instance_closed_locked");
        }
        // draft / rejected / cancelled / returned 可编辑
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

    private static String requireCategoryCode(String categoryCode) {
        if (!StringUtils.hasText(categoryCode)) {
            throw new BusinessException("error.oa.instance_not_found");
        }
        return categoryCode.trim();
    }
}
