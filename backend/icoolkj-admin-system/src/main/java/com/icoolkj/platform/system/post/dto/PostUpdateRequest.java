package com.icoolkj.platform.system.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 修改岗位表 */
@Data
public class PostUpdateRequest {

    @NotBlank(message = "{validation.id.required}")
    private String id;
    @NotBlank(message = "{gen.post.postName.required}")
    @Size(max = 50, message = "{gen.post.postName.maxLength}")
    private String postName;
    @NotBlank(message = "{gen.post.postCode.required}")
    @Size(max = 50, message = "{gen.post.postCode.maxLength}")
    private String postCode;
    @NotNull(message = "{gen.post.sortNo.required}")
    private Integer sortNo;
    @NotBlank(message = "{gen.post.status.required}")
    @Size(max = 1, message = "{gen.post.status.maxLength}")
    private String status;
    @Size(max = 500, message = "{gen.post.remark.maxLength}")
    private String remark;
}
