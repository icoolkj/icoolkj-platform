package com.icoolkj.platform.api.ai.skill;

import com.icoolkj.platform.api.ai.skill.handler.AiSkillHandler;
import com.icoolkj.platform.common.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime registry of executable skill handlers (code-bound; metadata from DB).
 */
@Component
public class AiSkillHandlerRegistry {

    private final Map<String, AiSkillHandler> byCode = new LinkedHashMap<>();

    public AiSkillHandlerRegistry(List<AiSkillHandler> handlers) {
        if (handlers != null) {
            for (AiSkillHandler handler : handlers) {
                if (handler == null || !StringUtils.hasText(handler.skillCode())) {
                    continue;
                }
                String code = handler.skillCode().trim();
                if (byCode.containsKey(code)) {
                    throw new IllegalStateException("Duplicate AiSkillHandler for skill_code=" + code);
                }
                byCode.put(code, handler);
            }
        }
    }

    public AiSkillHandler require(String skillCode) {
        AiSkillHandler handler = byCode.get(skillCode);
        if (handler == null) {
            throw new BusinessException("error.ai.skill_handler_missing", skillCode);
        }
        return handler;
    }

    public boolean hasHandler(String skillCode) {
        return StringUtils.hasText(skillCode) && byCode.containsKey(skillCode);
    }
}
