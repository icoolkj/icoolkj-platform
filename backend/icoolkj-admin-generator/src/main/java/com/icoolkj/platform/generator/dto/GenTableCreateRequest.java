package com.icoolkj.platform.generator.dto;

import com.icoolkj.platform.generator.vo.GenTableDesignVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenTableCreateRequest {

    @NotNull
    @Valid
    private GenTableDesignVO design;
}
