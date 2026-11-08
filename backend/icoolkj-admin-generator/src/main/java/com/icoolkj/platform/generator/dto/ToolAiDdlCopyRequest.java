package com.icoolkj.platform.generator.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ToolAiDdlCopyRequest {
    @Size(max = 32000)
    private String finalDdl;
}
