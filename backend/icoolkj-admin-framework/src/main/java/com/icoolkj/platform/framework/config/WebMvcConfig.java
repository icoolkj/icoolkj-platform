package com.icoolkj.platform.framework.config;

import com.icoolkj.platform.framework.tenant.TenantProperties;
import com.icoolkj.platform.framework.tenant.TenantWebInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers web-layer interceptors.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final TenantProperties tenantProperties;

    public WebMvcConfig(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TenantWebInterceptor(tenantProperties))
                .addPathPatterns("/**");
    }
}
