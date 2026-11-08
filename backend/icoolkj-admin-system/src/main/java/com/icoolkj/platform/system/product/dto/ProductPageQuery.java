package com.icoolkj.platform.system.product.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 产品宇宙分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPageQuery extends PageQuery {
    private String code;
    private String name;
    private String status;
}
