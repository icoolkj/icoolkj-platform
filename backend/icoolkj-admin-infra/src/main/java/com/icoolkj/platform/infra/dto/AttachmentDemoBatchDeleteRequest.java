package com.icoolkj.platform.infra.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

/** 批量删除附件测试业务 */
@Data
public class AttachmentDemoBatchDeleteRequest {
    @NotEmpty
    private List<String> ids;
}
