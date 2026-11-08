package com.icoolkj.platform.system.brandpack.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 站点品牌 / OEM — maps to {@code sys_brand_pack}. */
@TableName("sys_brand_pack")
@Getter
@Setter
public class BrandPackEntity extends TenantBaseEntity {

    @TableField("product_name")
    private String productName;

    /** locale → 产品名 JSON 字符串 */
    @TableField("product_name_i18n")
    private String productNameI18n;

    @TableField("copyright_brand")
    private String copyrightBrand;

    @TableField("icp_no")
    private String icpNo;

    @TableField("icp_url")
    private String icpUrl;

    private String domain;

    @TableField("site_url")
    private String siteUrl;

    private String email;

    @TableField("contact_name")
    private String contactName;

    @TableField("contact_phone")
    private String contactPhone;

    /** profile_code → fileId JSON */
    @TableField("assets_json")
    private String assetsJson;

    /** profile_code → label JSON */
    @TableField("labels_json")
    private String labelsJson;

    private String remark;
}
