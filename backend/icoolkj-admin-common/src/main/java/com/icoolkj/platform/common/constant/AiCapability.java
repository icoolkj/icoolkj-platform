package com.icoolkj.platform.common.constant;

public final class AiCapability {

    public static final String CHAT = "chat";
    public static final String IMAGE = "image";
    public static final String EMBEDDING = "embedding";

    public static final String PROTOCOL_OPENAI_COMPATIBLE = "openai_compatible";

    public static boolean isImplemented(String capability) {
        if (capability == null || capability.isBlank()) {
            return false;
        }
        String cap = capability.trim().toLowerCase();
        return CHAT.equals(cap) || IMAGE.equals(cap);
    }

    private AiCapability() {
    }
}
