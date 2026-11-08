package com.icoolkj.platform.infra.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 附件测试业务分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AttachmentDemoPageQuery extends PageQuery {
    private String title;
    private String status;
}
