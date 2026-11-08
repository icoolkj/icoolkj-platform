package com.icoolkj.platform.system.dict.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 字典数据项表分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DictItemPageQuery extends PageQuery {
    private String dictTypeId;
    private String dictLabel;
    private String dictValue;
    private String valueType;
    private String cssClass;
    private String listClass;
    private String isDefault;
    private Integer sortNo;
    private String status;
    private String remark;
}
