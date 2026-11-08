package com.icoolkj.platform.generator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ToolAiChatSendRequest {
    @NotBlank
    @Size(max = 65535)
    private String content;
}
