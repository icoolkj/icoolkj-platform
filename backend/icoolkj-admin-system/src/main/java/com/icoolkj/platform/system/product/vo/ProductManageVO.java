package com.icoolkj.platform.system.product.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 产品宇宙管理端列表/详情 */
@Data
public class ProductManageVO {
    private String id;
    private String code;
    private String name;
    /** 英文名称（表单回填） */
    private String nameEn;
    private String icon;
    private Integer sortNo;
    private String status;
    private String remark;
    /** 内置产品不可删除、编码不可改 */
    private Boolean builtin;
    private LocalDateTime createdAt;
}
