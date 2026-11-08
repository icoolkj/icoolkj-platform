package com.icoolkj.platform.generator.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiImageGenerateResultVO {

    private String invokeId;
    private String fileId;
    private String url;
    private String prompt;
    private String model;
    private String providerName;
    private String size;
    private Integer n;
    private List<AiImageItemVO> items = new ArrayList<>();

    @Data
    public static class AiImageItemVO {
        private String fileId;
        private String url;
    }
}
