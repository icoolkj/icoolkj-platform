package com.icoolkj.platform.system.dict.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 字典类型表分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DictTypePageQuery extends PageQuery {
    private String dictName;
    private String dictType;
    private String status;
    private String remark;
}
