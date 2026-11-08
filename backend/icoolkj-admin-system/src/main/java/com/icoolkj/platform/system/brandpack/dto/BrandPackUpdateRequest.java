package com.icoolkj.platform.system.brandpack.dto;

import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.Data;

@Data
public class BrandPackUpdateRequest {

    @Size(max = 128)
    private String productName;

    private Map<String, String> productNameI18n;

    @Size(max = 128)
    private String copyrightBrand;

    @Size(max = 128)
    private String icpNo;

    @Size(max = 512)
    private String icpUrl;

    @Size(max = 128)
    private String domain;

    @Size(max = 512)
    private String siteUrl;

    @Size(max = 128)
    private String email;

    @Size(max = 64)
    private String contactName;

    @Size(max = 32)
    private String contactPhone;

    /** profile_code → fileId */
    private Map<String, String> assets;

    /** profile_code → label */
    private Map<String, String> labels;
}
