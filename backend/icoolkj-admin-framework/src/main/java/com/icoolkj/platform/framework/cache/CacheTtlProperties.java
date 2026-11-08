package com.icoolkj.platform.framework.cache;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "icoolkj.cache")
public class CacheTtlProperties {

    private String keyPrefix = "icoolkj";
    private long defaultTtlSeconds = 1800L;
    private long nullValueTtlSeconds = 60L;
    private Ttl ttl = new Ttl();

    @Data
    public static class Ttl {

        private long authSession = 7200L;
        private long authPermission = 1800L;
        private long authBlacklist = 7200L;
        private long authLoginFail = 1800L;
        private long authCaptcha = 120L;
        private long dict = 3600L;
        private long config = 3600L;
        private long tenantConfig = 3600L;
        private long tenantIpRule = 1800L;
    }
}
