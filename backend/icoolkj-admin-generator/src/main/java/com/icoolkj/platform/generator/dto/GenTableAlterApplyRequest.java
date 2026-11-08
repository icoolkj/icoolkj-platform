package com.icoolkj.platform.generator.dto;

import com.icoolkj.platform.generator.vo.GenTableAlterDesignVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenTableAlterApplyRequest {

    @NotNull
    @Valid
    private GenTableAlterDesignVO design;
}
