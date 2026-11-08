package com.icoolkj.platform.oa.support;

import com.icoolkj.platform.oa.vo.OaFieldDefVO;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * OA 富文本存储契约（与 {@code oa_tables.sql} 对齐）：
 * <ul>
 *   <li>{@code field_type=richText} → {@code oa_instance_data.content_text}（每实例最多一篇）</li>
 *   <li>其余字段 → {@code form_data} JSON</li>
 *   <li>API / 前端始终用 {@code formData[fieldCode]}；持久化时拆分，读取时合并</li>
 * </ul>
 */
public final class OaFormContentTextSupport {

    private static final Pattern BLANK_HTML = Pattern.compile(
            "^(?:\\s|<p>(?:\\s|&nbsp;|<br\\s*/?>)*</p>|<div>(?:\\s|&nbsp;|<br\\s*/?>)*</div>|<br\\s*/?>)*$",
            Pattern.CASE_INSENSITIVE);

    private OaFormContentTextSupport() {
    }

    public record SplitResult(Map<String, Object> formData, String contentText) {
    }

    public static boolean isRichTextField(OaFieldDefVO field) {
        if (field == null) {
            return false;
        }
        if ("richText".equalsIgnoreCase(field.getFieldType())) {
            return true;
        }
        String component = field.getComponentType();
        if (!StringUtils.hasText(component)) {
            return false;
        }
        String c = component.trim().toLowerCase(Locale.ROOT);
        return "richtext".equals(c) || "rich-editor".equals(c);
    }

    /** 模板内唯一富文本字段；无则 null。多篇时由 {@link OaFieldDefValidator} 拦截。 */
    public static OaFieldDefVO findRichTextField(List<OaFieldDefVO> fields) {
        if (CollectionUtils.isEmpty(fields)) {
            return null;
        }
        for (OaFieldDefVO field : fields) {
            if (isRichTextField(field) && StringUtils.hasText(field.getFieldCode())) {
                return field;
            }
        }
        return null;
    }

    public static boolean isBlankRichText(Object raw) {
        if (raw == null) {
            return true;
        }
        String text = String.valueOf(raw).trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text)) {
            return true;
        }
        return BLANK_HTML.matcher(text).matches();
    }

    /**
     * 持久化拆分：剥离 richText key，正文写入 content_text。
     * 无富文本字段时原样返回，contentText=null。
     */
    public static SplitResult split(List<OaFieldDefVO> fields, Map<String, Object> form) {
        Map<String, Object> formData = form == null ? new LinkedHashMap<>() : new LinkedHashMap<>(form);
        OaFieldDefVO rich = findRichTextField(fields);
        if (rich == null) {
            return new SplitResult(formData, null);
        }
        String code = rich.getFieldCode().trim();
        Object raw = formData.remove(code);
        String content = isBlankRichText(raw) ? null : String.valueOf(raw);
        return new SplitResult(formData, content);
    }

    /**
     * 读取合并：优先 content_text；为空时保留 form_data 遗留值（兼容未拆分的旧数据）。
     */
    public static Map<String, Object> mergeInto(List<OaFieldDefVO> fields,
                                                String contentText,
                                                Map<String, Object> form) {
        Map<String, Object> merged = form == null ? new LinkedHashMap<>() : new LinkedHashMap<>(form);
        OaFieldDefVO rich = findRichTextField(fields);
        if (rich == null || !StringUtils.hasText(rich.getFieldCode())) {
            return merged;
        }
        String code = rich.getFieldCode().trim();
        if (StringUtils.hasText(contentText)) {
            merged.put(code, contentText);
        }
        return merged;
    }
}
