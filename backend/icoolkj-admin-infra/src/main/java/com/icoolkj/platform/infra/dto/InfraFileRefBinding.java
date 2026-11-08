package com.icoolkj.platform.infra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 业务文件引用绑定项 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfraFileRefBinding {

    private String fileId;
    private String refField;
}
