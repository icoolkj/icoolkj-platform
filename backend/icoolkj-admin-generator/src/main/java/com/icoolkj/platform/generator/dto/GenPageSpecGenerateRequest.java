package com.icoolkj.platform.generator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** AI 生成 page-spec.yaml 请求 */
@Data
public class GenPageSpecGenerateRequest {

    /** gen_table.id；可选，与 columnsText 二选一 */
    private String tableId;

    @NotBlank
    @Size(max = 64)
    private String pageKey;

    @NotBlank
    private String layout;

    @Size(max = 4000)
    private String userPrompt;

    /** 无 tableId 时可直接传列描述 */
    @Size(max = 20000)
    private String columnsText;
}
