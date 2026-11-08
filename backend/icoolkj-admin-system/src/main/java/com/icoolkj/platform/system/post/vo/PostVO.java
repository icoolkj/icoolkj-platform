package com.icoolkj.platform.system.post.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 岗位表列表/详情 */
@Data
public class PostVO {
    private String id;
    private String postName;
    private String postCode;
    private Integer sortNo;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
}
