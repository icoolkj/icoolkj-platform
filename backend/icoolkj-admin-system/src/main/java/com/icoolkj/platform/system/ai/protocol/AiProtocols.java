package com.icoolkj.platform.system.ai.protocol;

import com.icoolkj.platform.common.constant.AiCapability;
import com.icoolkj.platform.common.exception.BusinessException;
import org.springframework.util.StringUtils;

public final class AiProtocols {

    private AiProtocols() {
    }

    public static String normalize(String protocol) {
        if (!StringUtils.hasText(protocol)) {
            return AiCapability.PROTOCOL_OPENAI_COMPATIBLE;
        }
        return protocol.trim().toLowerCase();
    }

    public static void requireSupported(String protocol) {
        String value = normalize(protocol);
        if (!AiCapability.PROTOCOL_OPENAI_COMPATIBLE.equals(value)) {
            throw new BusinessException("error.ai.protocol_unsupported", value);
        }
    }

    public static boolean isOpenAiCompatible(String protocol) {
        return AiCapability.PROTOCOL_OPENAI_COMPATIBLE.equals(normalize(protocol));
    }
}
