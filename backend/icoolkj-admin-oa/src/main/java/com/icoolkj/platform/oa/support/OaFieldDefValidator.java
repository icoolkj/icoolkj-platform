package com.icoolkj.platform.oa.support;

import com.icoolkj.platform.common.enums.YesNoEnum;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.oa.dto.OaFieldDefItemRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

/** 字段映射校验（槽位、列表冗余） */
public final class OaFieldDefValidator {

    private static final Pattern SLOT_PATTERN = Pattern.compile("^idx_str_(\\d{2})$");
    private static final int LIST_SLOT_MAX = 10;
    private static final int PROMOTE_SLOT_MAX = 100;

    private OaFieldDefValidator() {
    }

    public static void validateBatch(List<OaFieldDefItemRequest> fields) {
        if (fields == null) {
            return;
        }
        Set<String> fieldCodes = new HashSet<>();
        Set<String> promoteSlots = new HashSet<>();
        int richTextCount = 0;
        for (OaFieldDefItemRequest field : fields) {
            if (!StringUtils.hasText(field.getFieldCode())) {
                throw new BusinessException("error.oa.field_code_required");
            }
            String code = field.getFieldCode().trim();
            if (!fieldCodes.add(code)) {
                throw new BusinessException("error.oa.field_code_duplicate", code);
            }
            if (!StringUtils.hasText(field.getFieldName())) {
                throw new BusinessException("error.oa.field_name_required", code);
            }
            if (!StringUtils.hasText(field.getFieldType())) {
                throw new BusinessException("error.oa.field_type_required", code);
            }
            boolean richText = isRichTextField(field);
            if (richText) {
                richTextCount++;
                if (richTextCount > 1) {
                    throw new BusinessException("error.oa.rich_text_one_per_template");
                }
                if (StringUtils.hasText(field.getPromoteSlot())) {
                    throw new BusinessException("error.oa.rich_text_no_slot", code);
                }
                // 富文本走 content_text，不占 idx_str / 列表冗余
                field.setPromoteSlot(null);
                field.setListSlotSync("0");
            }
            if (isFileField(field) && StringUtils.hasText(field.getPromoteSlot())) {
                throw new BusinessException("error.oa.file_no_slot", code);
            }
            if (StringUtils.hasText(field.getPromoteSlot())) {
                int slotNo = parseSlotNo(field.getPromoteSlot().trim());
                if (!promoteSlots.add(field.getPromoteSlot().trim())) {
                    throw new BusinessException("error.oa.promote_slot_duplicate", field.getPromoteSlot());
                }
                if (YesNoEnum.isYes(field.getListSlotSync()) && slotNo > LIST_SLOT_MAX) {
                    throw new BusinessException("error.oa.list_slot_sync_out_of_range", field.getPromoteSlot());
                }
            } else if (YesNoEnum.isYes(field.getListSlotSync())) {
                throw new BusinessException("error.oa.list_slot_sync_requires_promote", code);
            }
        }
    }

    private static boolean isFileField(OaFieldDefItemRequest field) {
        return "file".equalsIgnoreCase(field.getFieldType())
                || "file".equalsIgnoreCase(field.getComponentType());
    }

    private static boolean isRichTextField(OaFieldDefItemRequest field) {
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
        String c = component.trim().toLowerCase();
        return "richtext".equals(c) || "rich-editor".equals(c);
    }

    private static int parseSlotNo(String slot) {
        var matcher = SLOT_PATTERN.matcher(slot);
        if (!matcher.matches()) {
            throw new BusinessException("error.oa.promote_slot_invalid", slot);
        }
        int no = Integer.parseInt(matcher.group(1));
        if (no < 1 || no > PROMOTE_SLOT_MAX) {
            throw new BusinessException("error.oa.promote_slot_out_of_range", slot);
        }
        return no;
    }
}
