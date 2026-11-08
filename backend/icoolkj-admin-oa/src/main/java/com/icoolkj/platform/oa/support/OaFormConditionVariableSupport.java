package com.icoolkj.platform.oa.support;

import com.icoolkj.platform.common.enums.YesNoEnum;
import com.icoolkj.platform.oa.domain.OaInstanceDataEntity;
import com.icoolkj.platform.oa.domain.OaInstanceEntity;
import com.icoolkj.platform.oa.mapper.OaInstanceDataMapper;
import com.icoolkj.platform.oa.mapper.OaInstanceMapper;
import com.icoolkj.platform.oa.vo.OaFieldDefVO;
import com.icoolkj.platform.workflow.domain.WfModelEntity;
import com.icoolkj.platform.workflow.support.WorkflowConditionExpressionSupport;
import com.icoolkj.platform.workflow.support.WorkflowDesignNodeLookup;
import com.icoolkj.platform.workflow.support.WorkflowDesignSoTSupport;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * OA 条件变量解析：{@code used_in_condition=1} ∪ design 条件表达式引用的 fieldCode。
 */
@Component
public class OaFormConditionVariableSupport {

    private final OaInstanceMapper instanceMapper;
    private final OaInstanceDataMapper instanceDataMapper;
    private final OaInstanceFormSupport formSupport;
    private final WorkflowDesignSoTSupport designSoTSupport;
    private final WorkflowDesignNodeLookup designNodeLookup;

    public OaFormConditionVariableSupport(OaInstanceMapper instanceMapper,
                                          OaInstanceDataMapper instanceDataMapper,
                                          OaInstanceFormSupport formSupport,
                                          WorkflowDesignSoTSupport designSoTSupport,
                                          WorkflowDesignNodeLookup designNodeLookup) {
        this.instanceMapper = instanceMapper;
        this.instanceDataMapper = instanceDataMapper;
        this.formSupport = formSupport;
        this.designSoTSupport = designSoTSupport;
        this.designNodeLookup = designNodeLookup;
    }

    public Map<String, Object> resolveForStart(String bizId, WfModelEntity model) {
        Set<String> designCodes = model == null
                ? Set.of()
                : WorkflowConditionExpressionSupport.collectIdentifiers(
                designSoTSupport.parseDesignNodes(model.getProcessConfig()));
        return resolve(bizId, designCodes);
    }

    public Map<String, Object> resolveForRuntime(String bizId, String processInstanceId) {
        Set<String> designCodes = designNodeLookup.collectConditionIdentifiers(processInstanceId);
        return resolve(bizId, designCodes);
    }

    private Map<String, Object> resolve(String bizId, Set<String> designCodes) {
        if (!StringUtils.hasText(bizId)) {
            return Map.of();
        }
        OaInstanceEntity instance = instanceMapper.selectById(bizId);
        if (instance == null) {
            return Map.of();
        }
        OaInstanceDataEntity data = instanceDataMapper.selectByInstanceId(instance.getId());
        if (data == null) {
            return Map.of();
        }
        List<OaFieldDefVO> fields = formSupport.resolveFields(
                data.getFieldSnapshotJson(), instance.getTenantTplVerId());
        if (CollectionUtils.isEmpty(fields)) {
            return Map.of();
        }
        Set<String> wanted = new LinkedHashSet<>();
        if (designCodes != null) {
            wanted.addAll(designCodes);
        }
        Map<String, Object> formData = formSupport.readFormJson(data.getFormData());
        Map<String, Object> vars = new LinkedHashMap<>();
        for (OaFieldDefVO field : fields) {
            if (field == null || !StringUtils.hasText(field.getFieldCode())) {
                continue;
            }
            if (OaInstanceFormSupport.isFileField(field) || OaFormContentTextSupport.isRichTextField(field)) {
                // 附件 / 富文本不进流程条件变量
                continue;
            }
            String code = field.getFieldCode().trim();
            boolean marked = YesNoEnum.isYes(field.getUsedInCondition());
            boolean referenced = wanted.contains(code);
            if (!marked && !referenced) {
                continue;
            }
            if (!formData.containsKey(code)) {
                continue;
            }
            Object coerced = coerceValue(formData.get(code));
            if (coerced != null) {
                vars.put(code, coerced);
            }
        }
        return vars.isEmpty() ? Map.of() : Collections.unmodifiableMap(vars);
    }

    static Object coerceValue(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number || raw instanceof Boolean) {
            return raw;
        }
        if (raw instanceof String text) {
            String trimmed = text.trim();
            if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) {
                return null;
            }
            if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
                return Boolean.parseBoolean(trimmed);
            }
            try {
                if (trimmed.contains(".") || trimmed.contains("e") || trimmed.contains("E")) {
                    return new BigDecimal(trimmed);
                }
                return Long.parseLong(trimmed);
            } catch (NumberFormatException ignored) {
                return trimmed;
            }
        }
        return String.valueOf(raw);
    }
}
