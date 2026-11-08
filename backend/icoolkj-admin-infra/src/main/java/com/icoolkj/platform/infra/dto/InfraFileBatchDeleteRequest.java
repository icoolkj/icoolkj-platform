package com.icoolkj.platform.infra.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class InfraFileBatchDeleteRequest {

    @NotEmpty
    private List<String> ids;
}
