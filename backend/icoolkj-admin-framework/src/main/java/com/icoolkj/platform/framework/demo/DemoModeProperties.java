package com.icoolkj.platform.framework.demo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "icoolkj.demo")
public class DemoModeProperties {

    /** When true, POST/PUT/PATCH/DELETE are rejected except allow-write-urls. */
    private boolean enabled = false;

    /** Servlet paths (same as security ignore-urls) still allowed to mutate. */
    private List<String> allowWriteUrls = new ArrayList<>(List.of(
            "/auth/login",
            "/auth/logout",
            "/auth/password"
    ));
}
