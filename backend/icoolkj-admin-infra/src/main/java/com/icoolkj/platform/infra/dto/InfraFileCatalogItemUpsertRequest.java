package com.icoolkj.platform.infra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InfraFileCatalogItemUpsertRequest {

    @NotBlank
    @Size(max = 64)
    private String profileCode;

    private String required;
    private Integer minNum;
    private Integer maxNum;
    private Integer sortNo;
    private String status;
    /** role → priority */
    private Map<String, Integer> chromeRoles;
    private String remark;
}
