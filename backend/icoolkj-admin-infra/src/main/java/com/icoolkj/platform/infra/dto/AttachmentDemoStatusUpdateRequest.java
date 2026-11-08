package com.icoolkj.platform.infra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 附件测试业务状态变更 */
@Data
public class AttachmentDemoStatusUpdateRequest {
    @NotBlank
    @Size(max = 20)
    private String id;

    @NotBlank
    @Size(max = 1)
    private String status;
}
