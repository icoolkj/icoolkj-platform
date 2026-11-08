package com.icoolkj.platform.infra.vo;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

/** 附件测试业务详情 / 列表 */
@Data
public class AttachmentDemoVO {
    private String id;
    private String title;
    private Map<String, Object> assets;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
}
