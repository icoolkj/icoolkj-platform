package com.icoolkj.platform.system.product.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 产品宇宙目录 — maps to {@code sys_product}（平台全局，无 tenant_id）。
 * <p>顶栏排序：业务宇宙 {@code sort_no} 宜 1–49，内置 oa/files/ai 50–89，{@code platform} 建议 90+。
 */
@TableName("sys_product")
@Getter
@Setter
public class ProductEntity extends BaseEntity {

    private String code;

    private String name;

    @TableField("name_i18n")
    private String nameI18n;

    private String icon;

    @TableField("sort_no")
    private Integer sortNo;

    private String status;

    private String remark;
}
