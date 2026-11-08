package com.icoolkj.platform.system.dict.vo;

import lombok.Data;

/** 字典数据项表列表/详情 */
@Data
public class DictItemVO {
    private String id;
    private String dictTypeId;
    private String dictLabel;
    private String dictValue;
    private String valueType;
    private String cssClass;
    private String icon;
    private String listClass;
    private String isDefault;
    private Integer sortNo;
    private String status;
    private String remark;
}
