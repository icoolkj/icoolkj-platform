package com.icoolkj.platform.infra.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InfraFileRefVO {

    private String id;
    private String fileId;
    private String bizType;
    private String bizId;
    private String refField;
    private String createdBy;
    private LocalDateTime createdAt;
}
