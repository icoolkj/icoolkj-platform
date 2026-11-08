package com.icoolkj.platform.infra.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InfraFileCatalogUpsertRequest {

    private String targetTenantId;

    @NotBlank
    @Size(max = 64)
    private String catalogCode;

    @NotBlank
    @Size(max = 80)
    private String catalogName;

    /** locale → 名称；落库为 catalog_name_i18n JSON */
    private Map<String, String> catalogNameI18n;

    /** 0正常 1停用 */
    private String status;

    private String remark;

    @Valid
    private List<InfraFileCatalogItemUpsertRequest> items;
}
