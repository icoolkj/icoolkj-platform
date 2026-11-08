package com.icoolkj.platform.framework.tenant;

import com.icoolkj.platform.common.constant.TenantConstants;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "icoolkj.tenant")
public class TenantProperties {

    private boolean enabled = true;
    private String platformTenantId = TenantConstants.PLATFORM_TENANT_ID;
    private String platformTenantCode = "platform";
    private List<String> ignoreTables = new ArrayList<>();
    private List<String> ignoreUrls = new ArrayList<>();
    private String headerTenantCode = "X-Tenant-Code";
    private boolean strictMode = true;
}
