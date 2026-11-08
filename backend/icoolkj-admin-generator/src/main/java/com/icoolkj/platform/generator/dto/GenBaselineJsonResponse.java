package com.icoolkj.platform.generator.dto;

import lombok.Data;

@Data
public class GenBaselineJsonResponse {

    private String json;

    public GenBaselineJsonResponse(String json) {
        this.json = json;
    }
}
