package com.icoolkj.platform.infra.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InfraFileProfileBatchDeleteRequest {

    private String targetTenantId;

    @NotEmpty
    private List<String> profileCodes;
}
