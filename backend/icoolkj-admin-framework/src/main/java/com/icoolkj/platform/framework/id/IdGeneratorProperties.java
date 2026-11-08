package com.icoolkj.platform.framework.id;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "icoolkj.id-generator")
public class IdGeneratorProperties {

    private long workerId = 1L;
    private long datacenterId = 1L;
}
