package com.icoolkj.platform.infra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.Data;

/** 新增附件测试业务 */
@Data
public class AttachmentDemoCreateRequest {
    @NotBlank(message = "{gen.attachmentDemo.title.required}")
    @Size(max = 200, message = "{gen.attachmentDemo.title.maxLength}")
    private String title;

    /** profile_code → fileId 或 List&lt;fileId&gt; */
    private Map<String, Object> assets;

    @NotBlank(message = "{gen.attachmentDemo.status.required}")
    @Size(max = 1, message = "{gen.attachmentDemo.status.maxLength}")
    private String status;

    @Size(max = 500, message = "{gen.attachmentDemo.remark.maxLength}")
    private String remark;
}
