package com.icoolkj.platform.system.ai.client;

import lombok.Data;

@Data
public class OpenAiImageGenerateOptions {

    private String size;
    private Integer n;
    private String negativePrompt;
    private String responseFormat;
    private Boolean promptExtend;
    private Boolean watermark;
    private String defaultParamsJson;
}
