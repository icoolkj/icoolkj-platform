package com.icoolkj.platform.system.config.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 系统参数表列表/详情 */
@Data
public class ConfigVO {
    private String id;
    private String configName;
    private String configKey;
    private String configValue;
    private String configType;
    private String remark;
    private LocalDateTime createdAt;
}
