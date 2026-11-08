package com.icoolkj.platform.oa.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.enums.YesNoEnum;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.spi.DictLabelResolver;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.infra.dto.InfraFileRefBinding;
import com.icoolkj.platform.infra.util.InfraFileContentUtils;
import com.icoolkj.platform.oa.constant.OaInstanceFileConstants;
import com.icoolkj.platform.oa.constant.OaLeaveConstants;
import com.icoolkj.platform.oa.domain.OaInstanceEntity;
import com.icoolkj.platform.oa.domain.OaInstancePromoteEntity;
import com.icoolkj.platform.oa.domain.OaTenantFieldDefEntity;
import com.icoolkj.platform.oa.mapper.OaTenantFieldDefMapper;
import com.icoolkj.platform.oa.vo.OaFieldDefVO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** OA 实例动态表单：校验 / 提升槽 / schema / 附件引用（全 category 通用；请假标题摘要见 buildBusinessSummary） */
@Component
public class OaInstanceFormSupport {

    private final ObjectMapper objectMapper;
    private final OaTenantFieldDefMapper tenantFieldDefMapper;
    private final I18nMessageResolver i18nMessageResolver;
    private final ObjectProvider<DictLabelResolver> dictLabelResolver;

    public OaInstanceFormSupport(ObjectMapper objectMapper,
                                 OaTenantFieldDefMapper tenantFieldDefMapper,
                                 I18nMessageResolver i18nMessageResolver,
                                 ObjectProvider<DictLabelResolver> dictLabelResolver) {
        this.objectMapper = objectMapper;
        this.tenantFieldDefMapper = tenantFieldDefMapper;
        this.i18nMessageResolver = i18nMessageResolver;
        this.dictLabelResolver = dictLabelResolver;
    }

    public List<OaFieldDefVO> parseSnapshot(String fieldSnapshotJson) {
        if (!StringUtils.hasText(fieldSnapshotJson)) {
            return List.of();
        }
        try {
            List<OaFieldDefVO> fields = objectMapper.readValue(fieldSnapshotJson, new TypeReference<>() {
            });
            return fields == null ? List.of() : fields;
        } catch (JsonProcessingException ex) {
            throw new BusinessException("error.oa.snapshot_failed");
        }
    }

    public List<OaFieldDefVO> loadFieldsByVersionId(String versionId) {
        List<OaTenantFieldDefEntity> rows = tenantFieldDefMapper.selectByVersionId(versionId);
        if (CollectionUtils.isEmpty(rows)) {
            return List.of();
        }
        List<OaFieldDefVO> fields = new ArrayList<>(rows.size());
        for (OaTenantFieldDefEntity row : rows) {
            fields.add(com.icoolkj.platform.oa.convert.OaConvert.toTenantFieldVo(row));
        }
        return fields;
    }

    public List<OaFieldDefVO> resolveFields(String fieldSnapshotJson, String versionId) {
        List<OaFieldDefVO> fromSnapshot = parseSnapshot(fieldSnapshotJson);
        List<OaFieldDefVO> fromVersion = StringUtils.hasText(versionId)
                ? loadFieldsByVersionId(versionId) : List.of();
        if (fromSnapshot.isEmpty()) {
            return fromVersion;
        }
        // 种子/旧快照可能只有 fieldCode；用版本字段定义补齐名称、必填、组件等
        if (isIncompleteSnapshot(fromSnapshot) && !fromVersion.isEmpty()) {
            return enrichSnapshotFromVersion(fromSnapshot, fromVersion);
        }
        return fromSnapshot;
    }

    private boolean isIncompleteSnapshot(List<OaFieldDefVO> fields) {
        for (OaFieldDefVO field : fields) {
            if (field == null) {
                continue;
            }
            if (!StringUtils.hasText(field.getFieldName()) || !StringUtils.hasText(field.getFieldType())) {
                return true;
            }
        }
        return false;
    }

    private List<OaFieldDefVO> enrichSnapshotFromVersion(List<OaFieldDefVO> snapshot,
                                                         List<OaFieldDefVO> versionFields) {
        Map<String, OaFieldDefVO> byCode = new LinkedHashMap<>();
        for (OaFieldDefVO field : versionFields) {
            if (field != null && StringUtils.hasText(field.getFieldCode())) {
                byCode.put(field.getFieldCode().trim(), field);
            }
        }
        List<OaFieldDefVO> enriched = new ArrayList<>(snapshot.size());
        for (OaFieldDefVO snap : snapshot) {
            if (snap == null || !StringUtils.hasText(snap.getFieldCode())) {
                continue;
            }
            OaFieldDefVO full = byCode.remove(snap.getFieldCode().trim());
            if (full != null) {
                enriched.add(full);
            } else {
                enriched.add(snap);
            }
        }
        // 版本中多出的字段（快照过短时）也带上，保持与已发布定义一致
        enriched.addAll(byCode.values());
        return enriched;
    }

    public boolean hasRemarkField(List<OaFieldDefVO> fields) {
        if (CollectionUtils.isEmpty(fields)) {
            return false;
        }
        for (OaFieldDefVO field : fields) {
            if ("remark".equals(field.getFieldCode())) {
                return true;
            }
        }
        return false;
    }

    public void validateForm(List<OaFieldDefVO> fields, Map<String, Object> formData) {
        if (CollectionUtils.isEmpty(fields)) {
            throw new BusinessException("error.oa.platform_fields_empty");
        }
        Map<String, Object> data = formData == null ? Map.of() : formData;
        for (OaFieldDefVO field : fields) {
            if (field == null || !StringUtils.hasText(field.getFieldCode())) {
                continue;
            }
            String code = field.getFieldCode().trim();
            Object raw = data.get(code);
            if (isFileField(field)) {
                List<String> fileIds = normalizeFileIds(raw);
                if (YesNoEnum.isYes(field.getFieldRequired()) && fileIds.isEmpty()) {
                    throw new BusinessException("error.oa.form_field_required", field.getFieldName());
                }
                if (fileIds.size() > OaInstanceFileConstants.MAX_FILES_PER_FIELD) {
                    throw new BusinessException("error.oa.form_file_max_count", field.getFieldName(),
                            String.valueOf(OaInstanceFileConstants.MAX_FILES_PER_FIELD));
                }
                continue;
            }
            if (OaFormContentTextSupport.isRichTextField(field)) {
                boolean empty = OaFormContentTextSupport.isBlankRichText(raw);
                if (YesNoEnum.isYes(field.getFieldRequired()) && empty) {
                    throw new BusinessException("error.oa.form_field_required", field.getFieldName());
                }
                // HTML 长度 ≠ 可见文本；正文落 content_text(mediumtext)，不做 maxLength 卡控
                continue;
            }
            String text = raw == null ? "" : String.valueOf(raw).trim();
            boolean empty = text.isEmpty() || "null".equals(text);
            if (YesNoEnum.isYes(field.getFieldRequired()) && empty) {
                throw new BusinessException("error.oa.form_field_required", field.getFieldName());
            }
            if (!empty && field.getMaxLength() != null && field.getMaxLength() > 0
                    && text.length() > field.getMaxLength()) {
                throw new BusinessException("error.oa.form_field_max_length", field.getFieldName(),
                        String.valueOf(field.getMaxLength()));
            }
        }
    }

    public Map<String, Object> sanitizeFormData(List<OaFieldDefVO> fields, Map<String, Object> formData) {
        Map<String, Object> cleaned = new LinkedHashMap<>();
        Map<String, Object> source = formData == null ? Map.of() : formData;
        if (CollectionUtils.isEmpty(fields)) {
            return cleaned;
        }
        for (OaFieldDefVO field : fields) {
            if (field == null || !StringUtils.hasText(field.getFieldCode())) {
                continue;
            }
            String code = field.getFieldCode().trim();
            if (!source.containsKey(code)) {
                continue;
            }
            Object value = source.get(code);
            if (value == null) {
                continue;
            }
            if (isFileField(field)) {
                List<String> fileIds = normalizeFileIds(value);
                if (!fileIds.isEmpty()) {
                    cleaned.put(code, fileIds);
                }
                continue;
            }
            if (value instanceof String s && s.isBlank()) {
                continue;
            }
            cleaned.put(code, value);
        }
        return cleaned;
    }

    /** 从已清洗 formData 收集 infra_file_ref 绑定（refField = fieldCode 或 fieldCode:fileId） */
    public List<InfraFileRefBinding> collectFileBindings(List<OaFieldDefVO> fields, Map<String, Object> formData) {
        List<InfraFileRefBinding> bindings = new ArrayList<>();
        if (CollectionUtils.isEmpty(fields) || formData == null || formData.isEmpty()) {
            return bindings;
        }
        for (OaFieldDefVO field : fields) {
            if (field == null || !StringUtils.hasText(field.getFieldCode())) {
                continue;
            }
            String code = field.getFieldCode().trim();
            if (isFileField(field)) {
                for (String fileId : normalizeFileIds(formData.get(code))) {
                    // 保持 fieldCode 槽位约定（与历史 ref 兼容）；多文件场景仍以最后一次为准
                    bindings.add(new InfraFileRefBinding(fileId, code));
                }
                continue;
            }
            if (OaFormContentTextSupport.isRichTextField(field)) {
                Object raw = formData.get(code);
                if (raw == null) {
                    continue;
                }
                // 正文可含多图：ref_field 用 code:fileId，避免 syncBizFileRefs 同槽位互相覆盖
                for (String fileId : InfraFileContentUtils.extractFileIds(String.valueOf(raw))) {
                    bindings.add(new InfraFileRefBinding(fileId, code + ":" + fileId));
                }
            }
        }
        return bindings;
    }

    public static boolean isFileField(OaFieldDefVO field) {
        if (field == null) {
            return false;
        }
        return "file".equalsIgnoreCase(field.getFieldType())
                || "file".equalsIgnoreCase(field.getComponentType());
    }

    public List<String> normalizeFileIds(Object raw) {
        if (raw == null) {
            return List.of();
        }
        Set<String> ids = new LinkedHashSet<>();
        if (raw instanceof Collection<?> collection) {
            for (Object item : collection) {
                addFileId(ids, item);
            }
        } else if (raw.getClass().isArray()) {
            Object[] arr = (Object[]) raw;
            for (Object item : arr) {
                addFileId(ids, item);
            }
        } else if (raw instanceof String text) {
            String trimmed = text.trim();
            if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) {
                return List.of();
            }
            if (trimmed.startsWith("[")) {
                // 容错：偶发把 JSON 数组当字符串提交
                try {
                    List<Object> parsed = objectMapper.readValue(trimmed, new TypeReference<>() {
                    });
                    for (Object item : parsed) {
                        addFileId(ids, item);
                    }
                    return List.copyOf(ids);
                } catch (JsonProcessingException ignored) {
                    addFileId(ids, trimmed);
                }
            } else {
                addFileId(ids, trimmed);
            }
        } else {
            addFileId(ids, raw);
        }
        return List.copyOf(ids);
    }

    private static void addFileId(Set<String> ids, Object raw) {
        if (raw == null) {
            return;
        }
        String id = String.valueOf(raw).trim();
        if (StringUtils.hasText(id) && !"null".equalsIgnoreCase(id)) {
            ids.add(id);
        }
    }

    public void applyListSlots(OaInstanceEntity entity, List<OaFieldDefVO> fields, Map<String, Object> formData) {
        clearListSlots(entity);
        if (CollectionUtils.isEmpty(fields)) {
            return;
        }
        for (OaFieldDefVO field : fields) {
            if (field == null || !YesNoEnum.isYes(field.getListSlotSync()) || !StringUtils.hasText(field.getPromoteSlot())) {
                continue;
            }
            String slot = field.getPromoteSlot().trim().toLowerCase(Locale.ROOT);
            String value = str(formData.get(field.getFieldCode()));
            switch (slot) {
                case "idx_str_01" -> entity.setIdxStr01(value);
                case "idx_str_02" -> entity.setIdxStr02(value);
                case "idx_str_03" -> entity.setIdxStr03(value);
                case "idx_str_04" -> entity.setIdxStr04(value);
                case "idx_str_05" -> entity.setIdxStr05(value);
                case "idx_str_06" -> entity.setIdxStr06(value);
                case "idx_str_07" -> entity.setIdxStr07(value);
                case "idx_str_08" -> entity.setIdxStr08(value);
                case "idx_str_09" -> entity.setIdxStr09(value);
                case "idx_str_10" -> entity.setIdxStr10(value);
                default -> {
                    // list sync only supports 01-10
                }
            }
        }
    }

    public List<OaInstancePromoteEntity> buildPromotes(OaInstanceEntity entity, List<OaFieldDefVO> fields,
                                                       Map<String, Object> formData) {
        List<OaInstancePromoteEntity> promotes = new ArrayList<>();
        if (CollectionUtils.isEmpty(fields)) {
            return promotes;
        }
        for (OaFieldDefVO field : fields) {
            if (field == null || !StringUtils.hasText(field.getPromoteSlot())
                    || !StringUtils.hasText(field.getFieldCode())) {
                continue;
            }
            String slot = field.getPromoteSlot().trim();
            String value = str(formData.get(field.getFieldCode()));
            OaInstancePromoteEntity row = new OaInstancePromoteEntity();
            row.setId(IdGeneratorUtils.nextIdStr());
            row.setTenantId(entity.getTenantId());
            row.setInstanceId(entity.getId());
            row.setSlotCode(slot);
            row.setFieldCode(field.getFieldCode());
            row.setStrValue(value);
            AuditFieldHelper.fillInsert(row);
            promotes.add(row);
        }
        return promotes;
    }

    /**
     * OA 权威标题（提交时写入 instance_title / WorkflowStartRequest.bizTitle）。
     * <p>骨架走 {@code oa.instance.title.*}；流程名用 {@code wf_model.model_name}；字典摘要用 {@code dict_label}。
     * 工作流侧不再用 titleTemplate 覆盖（见 workflow {@code WorkflowTitleContract}）。
     *
     * @param processDisplayName 流程模型名称（优先），勿再写死 category i18n
     */
    public String buildTitle(Map<String, Object> formData, String processDisplayName, String categoryCode) {
        String starter = resolveStarterDisplay();
        String processName = StringUtils.hasText(processDisplayName)
                ? processDisplayName.trim()
                : (StringUtils.hasText(categoryCode) ? categoryCode.trim() : "application");
        String summary = buildBusinessSummary(formData, categoryCode);
        if (StringUtils.hasText(summary)) {
            return i18nMessageResolver.resolve("oa.instance.title.with_summary", starter, processName, summary);
        }
        return i18nMessageResolver.resolve("oa.instance.title.base", starter, processName);
    }

    private String buildBusinessSummary(Map<String, Object> formData, String categoryCode) {
        if (!OaLeaveConstants.BIZ_TYPE.equals(categoryCode)) {
            return null;
        }
        String leaveType = str(formData == null ? null : formData.get(OaLeaveConstants.FIELD_LEAVE_TYPE));
        String start = str(formData == null ? null : formData.get(OaLeaveConstants.FIELD_START_DATE));
        String end = str(formData == null ? null : formData.get(OaLeaveConstants.FIELD_END_DATE));
        String leaveLabel = resolveDictLabel("oa_leave_type", leaveType);
        if (StringUtils.hasText(leaveLabel) && StringUtils.hasText(start) && StringUtils.hasText(end)) {
            return leaveLabel + " " + start + "~" + end;
        }
        if (StringUtils.hasText(start) && StringUtils.hasText(end)) {
            return start + "~" + end;
        }
        return leaveLabel;
    }

    private String resolveDictLabel(String dictType, String dictValue) {
        if (!StringUtils.hasText(dictType) || !StringUtils.hasText(dictValue)) {
            return null;
        }
        DictLabelResolver resolver = dictLabelResolver.getIfAvailable();
        if (resolver != null) {
            String label = resolver.resolveLabel(dictType.trim(), dictValue.trim());
            if (StringUtils.hasText(label)) {
                return label.trim();
            }
        }
        return dictValue.trim();
    }

    private static String resolveStarterDisplay() {
        String nickname = SecurityContextHelper.getNickname();
        if (StringUtils.hasText(nickname)) {
            return nickname.trim();
        }
        String username = SecurityContextHelper.getUsername();
        if (StringUtils.hasText(username)) {
            return username.trim();
        }
        String userId = SecurityContextHelper.getUserId();
        return StringUtils.hasText(userId) ? userId.trim() : "-";
    }

    public String writeFormJson(Map<String, Object> form) {
        try {
            return objectMapper.writeValueAsString(form == null ? Map.of() : form);
        } catch (JsonProcessingException e) {
            throw new BusinessException("error.oa.form_data_invalid");
        }
    }

    public String writeFieldsSnapshot(List<OaFieldDefVO> fields) {
        try {
            return objectMapper.writeValueAsString(fields == null ? List.of() : fields);
        } catch (JsonProcessingException e) {
            throw new BusinessException("error.oa.snapshot_failed");
        }
    }

    public Map<String, Object> readFormJson(String formDataJson) {
        if (!StringUtils.hasText(formDataJson)) {
            return new LinkedHashMap<>();
        }
        try {
            Map<String, Object> map = objectMapper.readValue(formDataJson, new TypeReference<>() {
            });
            return map == null ? new LinkedHashMap<>() : new LinkedHashMap<>(map);
        } catch (JsonProcessingException e) {
            return new LinkedHashMap<>();
        }
    }

    private void clearListSlots(OaInstanceEntity entity) {
        entity.setIdxStr01(null);
        entity.setIdxStr02(null);
        entity.setIdxStr03(null);
        entity.setIdxStr04(null);
        entity.setIdxStr05(null);
        entity.setIdxStr06(null);
        entity.setIdxStr07(null);
        entity.setIdxStr08(null);
        entity.setIdxStr09(null);
        entity.setIdxStr10(null);
    }

    private static String str(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() || "null".equals(text) ? null : text;
    }
}
