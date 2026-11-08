package com.icoolkj.platform.system.ai.protocol.openai;

public final class OpenAiCompatibleEndpoints {

    public static final String CHAT_COMPLETIONS = "/chat/completions";
    public static final String IMAGES_GENERATIONS = "/images/generations";
    public static final String EMBEDDINGS = "/embeddings";
    public static final String MODELS = "/models";

    public static final String[] RESOURCE_PATHS = {
            CHAT_COMPLETIONS,
            IMAGES_GENERATIONS,
            EMBEDDINGS,
            MODELS
    };

    private OpenAiCompatibleEndpoints() {
    }

    public static String absoluteUrl(String apiRoot, String resourcePath) {
        return OpenAiCompatibleUrls.requireApiRoot(apiRoot) + resourcePath;
    }
}
