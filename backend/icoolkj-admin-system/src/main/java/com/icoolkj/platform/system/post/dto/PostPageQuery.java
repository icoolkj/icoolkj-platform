package com.icoolkj.platform.system.post.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 岗位表分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PostPageQuery extends PageQuery {
    private String postName;
    private String postCode;
    private Integer sortNo;
    private String status;
    private String remark;
}
