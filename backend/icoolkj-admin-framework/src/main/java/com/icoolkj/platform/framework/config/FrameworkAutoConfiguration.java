package com.icoolkj.platform.framework.config;

import com.icoolkj.platform.framework.cache.CacheTtlProperties;
import com.icoolkj.platform.framework.demo.DemoModeProperties;
import com.icoolkj.platform.framework.jwt.JwtProperties;
import com.icoolkj.platform.framework.logging.LoggingProperties;
import com.icoolkj.platform.framework.security.SecurityProperties;
import com.icoolkj.platform.framework.tenant.TenantProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        JwtProperties.class,
        TenantProperties.class,
        CacheTtlProperties.class,
        SecurityProperties.class,
        LoggingProperties.class,
        DemoModeProperties.class
})
public class FrameworkAutoConfiguration {

}
