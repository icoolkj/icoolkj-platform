package com.icoolkj.platform.infra.vo;

import lombok.Data;

import java.util.List;

@Data
public class FileStorageOptionsVO {

    private String defaultProvider;
    private List<String> enabledProviders;
    private long maxFileSizeMb;
}
