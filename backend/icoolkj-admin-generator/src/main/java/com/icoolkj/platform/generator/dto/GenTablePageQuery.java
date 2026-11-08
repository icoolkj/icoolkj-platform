package com.icoolkj.platform.generator.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 代码生成表分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GenTablePageQuery extends PageQuery {

    private String tableName;
    private String tableComment;
}
