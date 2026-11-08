package com.icoolkj.platform.system.dict.vo;

import lombok.Data;

/** Runtime dict option for select / radio / tag / catalog icon display. */
@Data
public class DictDataVO {

    private String label;
    private String value;
    private String tagType;
    private String icon;
    private String cssClass;

    public DictDataVO() {
    }

    public DictDataVO(String label, String value, String tagType) {
        this(label, value, tagType, null, null);
    }

    public DictDataVO(String label, String value, String tagType, String icon) {
        this(label, value, tagType, icon, null);
    }

    public DictDataVO(String label, String value, String tagType, String icon, String cssClass) {
        this.label = label;
        this.value = value;
        this.tagType = tagType;
        this.icon = icon;
        this.cssClass = cssClass;
    }

}
