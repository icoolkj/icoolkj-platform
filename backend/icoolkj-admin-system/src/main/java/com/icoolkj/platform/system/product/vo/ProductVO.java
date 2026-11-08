package com.icoolkj.platform.system.product.vo;

import lombok.Data;

/** 当前用户可见的产品宇宙（顶栏切换） */
@Data
public class ProductVO {
    private String code;
    private String name;
    private String icon;
    private Integer sortNo;
    /** 该产品默认首页路由 path（概览页） */
    private String homePath;
}
