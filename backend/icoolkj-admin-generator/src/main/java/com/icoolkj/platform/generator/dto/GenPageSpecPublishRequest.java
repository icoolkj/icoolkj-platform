package com.icoolkj.platform.generator.dto;

import com.icoolkj.platform.generator.vo.GenPageSpecDraftVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenPageSpecPublishRequest {

    private String tableId;

    @NotNull
    @Valid
    private GenPageSpecDraftVO draft;
}
